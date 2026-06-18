package com.stock.heatmap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.stock.heatmap.entity.Stock;
import com.stock.heatmap.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 定時從 Finnhub Quote API 更新 sp500_top300 的 current_price、change_percent。
 *
 * Finnhub 免費方案約 60 次/分鐘，298 檔股票需加請求間隔避免 429。
 */
@Service
public class PriceUpdateService {

    private static final Logger log = LoggerFactory.getLogger(PriceUpdateService.class);
    private static final long REQUEST_INTERVAL_MS = 1_100L;

    private final StockRepository stockRepository;
    private final RestClient finnhubClient;
    private final String finnhubApiKey;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public PriceUpdateService(
            StockRepository stockRepository,
            @Value("${finnhub.api-key:}") String finnhubApiKey,
            @Value("${finnhub.base-url:https://finnhub.io/api/v1}") String finnhubBaseUrl) {
        this.stockRepository = stockRepository;
        this.finnhubApiKey = finnhubApiKey;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));

        this.finnhubClient = RestClient.builder()
                .baseUrl(finnhubBaseUrl)
                .requestFactory(factory)
                .build();
    }

    /** 啟動後 30 秒先跑一輪，之後每次完成再等 10 分鐘（避免與上一輪重疊）。 */
    @Scheduled(fixedDelay = 600_000, initialDelay = 30_000)
    public void updateAllStockPricesScheduled() {
        updateAllStockPrices();
    }

    public void updateAllStockPrices() {
        if (finnhubApiKey == null || finnhubApiKey.isBlank()) {
            log.warn("No Finnhub API key configured; skipping price update");
            return;
        }

        if (!running.compareAndSet(false, true)) {
            log.warn("Price update already in progress; skipping this run");
            return;
        }

        try {
            List<Stock> stocks = stockRepository.findAll();
            List<Stock> updated = new ArrayList<>();
            int failed = 0;
            int skipped = 0;

            log.info("Starting price update for {} symbols", stocks.size());

            for (int i = 0; i < stocks.size(); i++) {
                Stock stock = stocks.get(i);
                try {
                    if (applyQuote(stock)) {
                        updated.add(stock);
                    } else {
                        skipped++;
                    }
                } catch (Exception e) {
                    failed++;
                    log.debug("Quote failed for {}: {}", stock.getSymbol(), e.getMessage());
                }

                if (i < stocks.size() - 1) {
                    Thread.sleep(REQUEST_INTERVAL_MS);
                }
            }

            if (!updated.isEmpty()) {
                stockRepository.saveAll(updated);
            }

            log.info("Price update done: updated={}, skipped={}, failed={}, total={}",
                    updated.size(), skipped, failed, stocks.size());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Price update interrupted");
        } finally {
            running.set(false);
        }
    }

    /** @return true 若成功解析並寫入記憶體中的 stock 欄位 */
    private boolean applyQuote(Stock stock) {
        String symbol = stock.getSymbol();
        if (symbol == null || symbol.isBlank()) {
            return false;
        }

        JsonNode quote = finnhubClient.get()
                .uri(b -> b.path("/quote")
                        .queryParam("symbol", symbol)
                        .queryParam("token", finnhubApiKey)
                        .build())
                .retrieve()
                .body(JsonNode.class);

        if (quote == null || quote.isEmpty()) {
            return false;
        }

        double currentPrice = quote.path("c").asDouble(0);
        double previousClose = quote.path("pc").asDouble(0);

        if (currentPrice <= 0 || previousClose <= 0) {
            return false;
        }

        double changePercent = quote.hasNonNull("dp")
                ? quote.path("dp").asDouble()
                : (currentPrice - previousClose) / previousClose * 100.0;

        stock.setCurrentPrice(currentPrice);
        stock.setChangePercent(Math.round(changePercent * 100.0) / 100.0);
        return true;
    }
}
