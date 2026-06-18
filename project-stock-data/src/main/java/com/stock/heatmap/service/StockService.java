package com.stock.heatmap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.stock.heatmap.dto.CompanyProfile;
import com.stock.heatmap.entity.Stock;
import com.stock.heatmap.repository.StockRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Stock business logic.
 *
 * Responsibilities:
 * - Read stock rows from PostgreSQL through StockRepository.
 * - Build Company Profile data by combining local DB data with Finnhub profile data.
 */
@Service
public class StockService {

    private static final String FALLBACK_COUNTRY = "US";
    private static final String FALLBACK_CURRENCY = "USD";
    private static final String FALLBACK_EXCHANGE = "Listed Exchange";

    private final StockRepository stockRepository;
    private final RestClient finnhubClient;
    private final String finnhubApiKey;

    public StockService(
            StockRepository stockRepository,
            @Value("${finnhub.api-key:}") String finnhubApiKey,
            @Value("${finnhub.base-url:https://finnhub.io/api/v1}") String finnhubBaseUrl) {
        this.stockRepository = stockRepository;
        this.finnhubApiKey = finnhubApiKey;
        this.finnhubClient = RestClient.builder()
                .baseUrl(finnhubBaseUrl)
                .build();
    }

    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    public Stock getStockBySymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return null;
        }
        return stockRepository.findById(symbol.toUpperCase()).orElse(null);
    }

    public List<Stock> getStocksBySector(String sector) {
        return stockRepository.findBySector(sector);
    }

    public CompanyProfile getCompanyProfile(String symbol) {
        String normalizedSymbol = symbol == null ? "" : symbol.toUpperCase();
        Stock dbStock = getStockBySymbol(normalizedSymbol);

        CompanyProfile fallback = buildFallbackProfile(normalizedSymbol, dbStock);
        if (finnhubApiKey == null || finnhubApiKey.isBlank()) {
            return fallback;
        }

        try {
            JsonNode node = finnhubClient.get()
                    .uri(builder -> builder.path("/stock/profile2")
                            .queryParam("symbol", normalizedSymbol)
                            .queryParam("token", finnhubApiKey)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            if (node == null || !node.hasNonNull("name")) {
                return fallback;
            }

            Double finnhubMarketCap = node.hasNonNull("marketCapitalization")
                    ? node.get("marketCapitalization").asDouble() * 1_000_000d
                    : fallback.marketCap();

            return new CompanyProfile(
                    normalizedSymbol,
                    text(node, "name", fallback.name()),
                    text(node, "country", fallback.country()),
                    text(node, "currency", fallback.currency()),
                    text(node, "exchange", fallback.exchange()),
                    text(node, "ipo", fallback.ipo()),
                    text(node, "finnhubIndustry", fallback.industry()),
                    text(node, "logo", fallback.logo()),
                    text(node, "weburl", fallback.weburl()),
                    finnhubMarketCap,
                    fallback.marketCapDisplay(),
                    true
            );
        } catch (Exception ex) {
            return fallback;
        }
    }

    private CompanyProfile buildFallbackProfile(String symbol, Stock stock) {
        String name = stock != null ? stock.getSecurity() : symbol;
        String industry = stock != null ? stock.getIndustry() : null;
        Double marketCap = stock != null ? stock.getMarketCap() : null;
        String marketCapDisplay = stock != null ? stock.getMarketCapDisplay() : null;
        String ipo = stock != null ? stock.getIpoDate() : null;
        String logo = symbol == null || symbol.isBlank()
                ? null
                : "https://financialmodelingprep.com/image-stock/" + symbol + ".png";

        return new CompanyProfile(
                symbol,
                name,
                FALLBACK_COUNTRY,
                FALLBACK_CURRENCY,
                FALLBACK_EXCHANGE,
                ipo,
                industry,
                logo,
                null,
                marketCap,
                marketCapDisplay,
                false
        );
    }

    private static String text(JsonNode node, String field, String fallback) {
        return node.hasNonNull(field) ? node.get(field).asText() : fallback;
    }
}
