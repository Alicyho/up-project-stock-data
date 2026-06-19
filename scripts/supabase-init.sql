-- Run this in Supabase → SQL Editor before importing stock data.

CREATE TABLE IF NOT EXISTS public.sp500_top300 (
    "Symbol" VARCHAR(20) PRIMARY KEY,
    "Security" VARCHAR(255),
    "Sector" VARCHAR(255),
    "Industry" VARCHAR(255),
    "MarketCap" DOUBLE PRECISION,
    "MarketCap_Display" VARCHAR(50),
    "IPO_Date" VARCHAR(50),
    current_price NUMERIC(15, 4),
    change_percent NUMERIC(10, 4)
);

-- After creating the table, import data from sp500_top300_with_industry.csv
-- via Supabase Table Editor → Import CSV, or use psql COPY.
