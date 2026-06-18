package com.stock.heatmap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot 專案入口。
 * @SpringBootApplication 已內建 @ComponentScan，會自動掃描
 * com.stock.heatmap 及其子套件（controller、service、repository 等）。
 */
@SpringBootApplication
@EnableScheduling // 啟用定時任務
public class StockDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockDataApplication.class, args);

        System.out.println("========================================");
        System.out.println("✅ Stock Data Service 啟動成功！");
        System.out.println("🌐 請在瀏覽器測試：");
        System.out.println("   http://localhost:8080/api/stocks");
        System.out.println("========================================");
    }
}
