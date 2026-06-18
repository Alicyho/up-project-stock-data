# Data Pipeline

This Spring Boot project reads stock data from PostgreSQL:

```text
stock_db.sp500_top300
```

The data preparation notebook is kept inside this project at:

```text
notebooks/stock_heatmap_data.ipynb
```

## Purpose

The notebook is used to:

1. Fetch S&P 500 company data.
2. Enrich the data with sector, industry, and market cap.
3. Format market cap display values.
4. Write the cleaned data into PostgreSQL.

## Runtime Flow

```text
notebooks/stock_heatmap_data.ipynb
        ↓
PostgreSQL stock_db.sp500_top300
        ↓
Spring Boot API (/api/stocks)
        ↓
src/main/resources/static/heatmap.html
```

## Notes

- The Spring Boot app does not execute the notebook directly.
- The notebook is an ETL/data preparation tool.
- The frontend and backend use the database output from the notebook.
- Keep the PostgreSQL columns aligned with the Java entity `Stock`.
- The canonical notebook path is `project-stock-data/notebooks/stock_heatmap_data.ipynb`.
- Warning: `save_to_postgres()` uses `if_exists="replace"`, so running the notebook will recreate `sp500_top300` and remove columns not produced by the notebook, such as `IPO_Date`.

