package com.stock.heatmap.dto;

/**
 * 單一根 K 線（OHLC）的資料傳輸物件（DTO）。
 *
 * <p>用途：Company Profile 彈窗中的「6-Month Price (Candlestick)」走勢圖。
 * 後端透過 {@code GET /api/stocks/{symbol}/candles?months=6} 回傳
 * {@code List<Candle>}，前端以 Lightweight Charts 繪製蠟燭圖。</p>
 *
 * <p>資料流：</p>
 * <pre>
 * 使用者點擊股票 → StockController → CandleService（Stooq CSV 或模擬資料）
 *        → JSON List&lt;Candle&gt; → heatmap.html renderCandles()
 * </pre>
 *
 * <p>與 {@link CompanyProfile} 的差異：{@code CompanyProfile} 是公司基本資料；
 * {@code Candle} 是股價走勢，不存入 PostgreSQL，僅作 API 回應格式。</p>
 *
 * <p>欄位名稱刻意對齊 Lightweight Charts 所需的
 * {@code { time, open, high, low, close }}。</p>
 *
 * @param time   日期，格式 {@code yyyy-MM-dd}
 * @param open   開盤價
 * @param high   最高價
 * @param low    最低價
 * @param close  收盤價
 * @param volume 成交量
 */
public record Candle(
        String time,
        double open,
        double high,
        double low,
        double close,
        long volume
) {}
