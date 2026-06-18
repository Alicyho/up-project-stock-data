package com.stock.heatmap.dto;

/**
 * Company Profile API response for the frontend modal.
 *
 * Field names intentionally match the JSON consumed by heatmap.html:
 * country, currency, exchange, ipo, industry, logo, marketCap, marketCapDisplay, etc.
 */
public record CompanyProfile(
        String symbol,
        String name,
        String country,
        String currency,
        String exchange,
        String ipo,
        String industry,
        String logo,
        String weburl,
        Double marketCap,
        String marketCapDisplay,
        boolean liveData
) {
}
