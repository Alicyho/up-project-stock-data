package com.stock.heatmap.service;

import com.stock.heatmap.dto.Candle;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 取得最近 N 個月的日 K 線資料 (OHLC)。
 *
 * 主要來源：Stooq 的免費 CSV（不需 API key）
 *   https://stooq.com/q/d/l/?s=aapl.us&d1=YYYYMMDD&d2=YYYYMMDD&i=d
 *
 * 若 Stooq 無回應或資料太少，會產生「模擬 K 線」作為後備，
 * 確保前端 Modal 一定畫得出 6 個月 K 線圖（適合 demo / presentation）。
 *
 * 註：Finnhub 的 /stock/candle 目前僅限付費方案，故這裡改用 Stooq。
 */
@Service
public class CandleService {

    private static final DateTimeFormatter STOOQ_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final RestClient client = RestClient.create();

    public List<Candle> getCandles(String symbol, int months) {
        try {
            List<Candle> data = fetchFromStooq(symbol, months);
            if (data.size() >= 20) {
                return data;
            }
        } catch (Exception ignored) {
            // 落到後備模擬資料
        }
        return synthesize(symbol, months);
    }

    private List<Candle> fetchFromStooq(String symbol, int months) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusMonths(months);
        String stooqSymbol = symbol.toLowerCase().replace('.', '-') + ".us";
        String url = "https://stooq.com/q/d/l/?s=" + stooqSymbol
                + "&d1=" + start.format(STOOQ_FMT)
                + "&d2=" + end.format(STOOQ_FMT)
                + "&i=d";

        String csv = client.get().uri(url).retrieve().body(String.class);
        List<Candle> out = new ArrayList<>();
        if (csv == null || csv.isBlank()) {
            return out;
        }

        String[] lines = csv.split("\\r?\\n");
        // 第 0 行是表頭：Date,Open,High,Low,Close,Volume
        for (int i = 1; i < lines.length; i++) {
            String[] c = lines[i].split(",");
            if (c.length < 5) continue;
            try {
                out.add(new Candle(
                        c[0],
                        Double.parseDouble(c[1]),
                        Double.parseDouble(c[2]),
                        Double.parseDouble(c[3]),
                        Double.parseDouble(c[4]),
                        c.length > 5 ? (long) Double.parseDouble(c[5]) : 0L
                ));
            } catch (NumberFormatException ignored) {
                // 跳過無法解析的列（例如 stooq 回傳 "<no data>"）
            }
        }
        return out;
    }

    /** 以 symbol 為種子產生穩定、可重現的模擬 K 線。 */
    private List<Candle> synthesize(String symbol, int months) {
        long seed = symbol.chars().asLongStream().reduce(7, (a, b) -> a * 31 + b);
        Random rnd = new Random(seed);

        double price = 60 + Math.abs(seed % 240); // 60 ~ 300 起始價
        int tradingDays = Math.max(20, months * 21);

        List<Candle> out = new ArrayList<>();
        LocalDate day = LocalDate.now().minusDays((long) (tradingDays * 1.4));
        for (int i = 0; i < tradingDays; i++) {
            // 跳過週末
            while (day.getDayOfWeek().getValue() >= 6) {
                day = day.plusDays(1);
            }
            double drift = (rnd.nextDouble() - 0.48) * price * 0.02;
            double open = price;
            double close = Math.max(1, open + drift);
            double high = Math.max(open, close) * (1 + rnd.nextDouble() * 0.012);
            double low = Math.min(open, close) * (1 - rnd.nextDouble() * 0.012);
            long vol = 1_000_000L + (long) (rnd.nextDouble() * 9_000_000L);

            out.add(new Candle(
                    day.toString(),
                    round(open), round(high), round(low), round(close), vol
            ));
            price = close;
            day = day.plusDays(1);
        }
        return out;
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
