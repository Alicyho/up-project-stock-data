package com.stock.heatmap.controller;

import com.stock.heatmap.dto.Candle;
import com.stock.heatmap.dto.CompanyProfile;
import com.stock.heatmap.entity.Stock;
import com.stock.heatmap.service.CandleService;
import com.stock.heatmap.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API for stock heatmap data.
 */
@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private static final Logger log = LoggerFactory.getLogger(StockController.class);

    private final StockService stockService;
    private final CandleService candleService;

    public StockController(StockService stockService, CandleService candleService) {
        this.stockService = stockService;
        this.candleService = candleService;
    }

    @GetMapping
    public List<Stock> getAllStocks() {
        try {
            return stockService.getAllStocks();
        } catch (DataAccessException ex) {
            log.error("Failed to load stocks from database", ex);
            return List.of();
        }
    }

    @GetMapping("/{symbol}/profile")
    public CompanyProfile getCompanyProfile(@PathVariable String symbol) {
        return stockService.getCompanyProfile(symbol);
    }

    @GetMapping("/{symbol}/candles")
    public List<Candle> getCandles(@PathVariable String symbol,
                                   @RequestParam(defaultValue = "6") int months) {
        return candleService.getCandles(symbol.toUpperCase(), months);
    }

    @GetMapping("/sector/{sector}")
    public List<Stock> getStocksBySector(@PathVariable String sector) {
        return stockService.getStocksBySector(sector);
    }

    @GetMapping("/{symbol}")
    public Stock getStockBySymbol(@PathVariable String symbol) {
        return stockService.getStockBySymbol(symbol);
    }
}
