package com.stock.heatmap.repository;

import com.stock.heatmap.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * StockRepository 的作用：
 * 負責跟資料庫中的 "sp500_top300" 表格進行溝通
 * （新增、查詢、修改、刪除資料）
 */
@Repository

public interface StockRepository extends JpaRepository <Stock,String>{
// JpaRepository 已經內建以下常用方法：
    // findAll()     → 查詢全部資料
    // findById(id)  → 依照 Symbol 查詢單筆
    // save()        → 新增或更新資料
    // delete()      → 刪除資料

    // ==================== 自訂查詢方法（之後可以慢慢增加） ====================
    
    // 例如：依照產業別查詢
    List<Stock> findBySector(String sector);

    // 例如：依照公司名稱模糊搜尋
    List<Stock> findBySecurityContaining(String keyword);
}
  

