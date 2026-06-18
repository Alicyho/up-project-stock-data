package com.stock.heatmap.entity;

import jakarta.persistence.*;

/**
 * 對應資料庫 sp500_top300 表格。
 * 欄位名稱使用大寫（Symbol、Security...），與 PostgreSQL 實際欄位一致。
 * 即時股價欄位為 snake_case：current_price、change_percent。
 */
@Entity
@Table(name = "sp500_top300")
@Access(AccessType.FIELD)
public class Stock {

    @Id
    @Column(name = "Symbol")
    private String symbol;

    @Column(name = "Security")
    private String security;

    @Column(name = "Sector")
    private String sector;

    @Column(name = "Industry")
    private String industry;

    @Column(name = "MarketCap")
    private Double marketCap;

    @Column(name = "MarketCap_Display")
    private String marketCapDisplay;

    @Column(name = "IPO_Date")
    private String ipoDate;

    @Column(name = "current_price")
    private Double currentPrice;

    @Column(name = "change_percent")
    private Double changePercent;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public Double getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(Double marketCap) {
        this.marketCap = marketCap;
    }

    public String getMarketCapDisplay() {
        return marketCapDisplay;
    }

    public void setMarketCapDisplay(String marketCapDisplay) {
        this.marketCapDisplay = marketCapDisplay;
    }

    public String getIpoDate() {
        return ipoDate;
    }

    public void setIpoDate(String ipoDate) {
        this.ipoDate = ipoDate;
    }

    public Double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(Double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public Double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(Double changePercent) {
        this.changePercent = changePercent;
    }
}
