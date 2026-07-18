-- Seed real market price data for Indian commodities
-- Run this to populate market_prices table with realistic data

SET @today = CURDATE();
SET @d1 = DATE_SUB(CURDATE(), INTERVAL 1 DAY);
SET @d7 = DATE_SUB(CURDATE(), INTERVAL 7 DAY);
SET @d14 = DATE_SUB(CURDATE(), INTERVAL 14 DAY);
SET @d21 = DATE_SUB(CURDATE(), INTERVAL 21 DAY);
SET @d30 = DATE_SUB(CURDATE(), INTERVAL 30 DAY);

-- Delete old seed data if any
DELETE FROM market_prices WHERE market_name IN ('Pune APMC','Nashik APMC','Kolhapur APMC','Nagpur APMC','Aurangabad APMC','Amravati APMC','Ludhiana APMC','Amritsar APMC','Jalandhar APMC','Indore APMC','Bhopal APMC','Gwalior APMC','Ahmedabad APMC','Rajkot APMC','Surat APMC','Jaipur APMC','Jodhpur APMC','Kota APMC','Lucknow APMC','Agra APMC','Varanasi APMC','Hyderabad APMC','Warangal APMC','Nizamabad APMC','Bangalore APMC','Mysore APMC','Hubli APMC');

-- ─── WHEAT — Maharashtra ─────────────────────────────────────────────────────
INSERT INTO market_prices (commodity, market_name, state, district, min_price, max_price, modal_price, price_unit, trade_date, created_at) VALUES
('Wheat','Pune APMC','maharashtra','pune',2100.00,2350.00,2250.00,'per quintal',@today,NOW()),
('Wheat','Nashik APMC','maharashtra','nashik',2050.00,2300.00,2180.00,'per quintal',@today,NOW()),
('Wheat','Kolhapur APMC','maharashtra','kolhapur',2000.00,2280.00,2150.00,'per quintal',@today,NOW()),
('Wheat','Nagpur APMC','maharashtra','nagpur',2080.00,2320.00,2200.00,'per quintal',@d7,NOW()),
('Wheat','Aurangabad APMC','maharashtra','aurangabad',2060.00,2290.00,2170.00,'per quintal',@d7,NOW()),
('Wheat','Amravati APMC','maharashtra','amravati',2040.00,2270.00,2155.00,'per quintal',@d14,NOW());

-- ─── WHEAT — Punjab ──────────────────────────────────────────────────────────
INSERT INTO market_prices (commodity, market_name, state, district, min_price, max_price, modal_price, price_unit, trade_date, created_at) VALUES
('Wheat','Ludhiana APMC','punjab','ludhiana',2100.00,2380.00,2275.00,'per quintal',@today,NOW()),
('Wheat','Amritsar APMC','punjab','amritsar',2090.00,2360.00,2260.00,'per quintal',@today,NOW()),
('Wheat','Jalandhar APMC','punjab','jalandhar',2085.00,2350.00,2245.00,'per quintal',@d7,NOW());

-- ─── WHEAT — Madhya Pradesh ──────────────────────────────────────────────────
INSERT INTO market_prices (commodity, market_name, state, district, min_price, max_price, modal_price, price_unit, trade_date, created_at) VALUES
('Wheat','Indore APMC','madhya pradesh','indore',2050.00,2320.00,2200.00,'per quintal',@today,NOW()),
('Wheat','Bhopal APMC','madhya pradesh','bhopal',2040.00,2310.00,2185.00,'per quintal',@today,NOW()),
('Wheat','Gwalior APMC','madhya pradesh','gwalior',2030.00,2290.00,2170.00,'per quintal',@d7,NOW());

-- ─── RICE — Maharashtra ──────────────────────────────────────────────────────
INSERT INTO market_prices (commodity, market_name, state, district, min_price, max_price, modal_price, price_unit, trade_date, created_at) VALUES
('Rice','Pune APMC','maharashtra','pune',2800.00,3500.00,3200.00,'per quintal',@today,NOW()),
('Rice','Nashik APMC','maharashtra','nashik',2750.00,3450.00,3150.00,'per quintal',@today,NOW()),
('Rice','Kolhapur APMC','maharashtra','kolhapur',2900.00,3600.00,3300.00,'per quintal',@d7,NOW()),
('Rice','Nagpur APMC','maharashtra','nagpur',2700.00,3400.00,3100.00,'per quintal',@d7,NOW());

-- ─── RICE — Punjab ───────────────────────────────────────────────────────────
INSERT INTO market_prices (commodity, market_name, state, district, min_price, max_price, modal_price, price_unit, trade_date, created_at) VALUES
('Rice','Ludhiana APMC','punjab','ludhiana',3200.00,4500.00,3800.00,'per quintal',@today,NOW()),
('Rice','Amritsar APMC','punjab','amritsar',3100.00,4400.00,3750.00,'per quintal',@today,NOW()),
('Rice','Jalandhar APMC','punjab','jalandhar',3050.00,4350.00,3700.00,'per quintal',@d7,NOW());

-- ─── ONION — Maharashtra ─────────────────────────────────────────────────────
INSERT INTO market_prices (commodity, market_name, state, district, min_price, max_price, modal_price, price_unit, trade_date, created_at) VALUES
('Onion','Nashik APMC','maharashtra','nashik',800.00,1800.00,1350.00,'per quintal',@today,NOW()),
('Onion','Pune APMC','maharashtra','pune',900.00,1900.00,1450.00,'per quintal',@today,NOW()),
('Onion','Kolhapur APMC','maharashtra','kolhapur',750.00,1700.00,1250.00,'per quintal',@d7,NOW()),
('Onion','Nagpur APMC','maharashtra','nagpur',850.00,1800.00,1380.00,'per quintal',@d7,NOW()),
('Onion','Aurangabad APMC','maharashtra','aurangabad',780.00,1720.00,1300.00,'per quintal',@d14,NOW()),
('Onion','Nashik APMC','maharashtra','nashik',700.00,1600.00,1200.00,'per quintal',@d14,NOW()),
('Onion','Nashik APMC','maharashtra','nashik',650.00,1550.00,1150.00,'per quintal',@d21,NOW()),
('Onion','Nashik APMC','maharashtra','nashik',600.00,1450.00,1050.00,'per quintal',@d30,NOW());

-- ─── TOMATO — Maharashtra ────────────────────────────────────────────────────
INSERT INTO market_prices (commodity, market_name, state, district, min_price, max_price, modal_price, price_unit, trade_date, created_at) VALUES
('Tomato','Pune APMC','maharashtra','pune',1200.00,3500.00,2400.00,'per quintal',@today,NOW()),
('Tomato','Nashik APMC','maharashtra','nashik',1100.00,3200.00,2200.00,'per quintal',@today,NOW()),
('Tomato','Kolhapur APMC','maharashtra','kolhapur',1300.00,3600.00,2500.00,'per quintal',@d7,NOW()),
('Tomato','Nagpur APMC','maharashtra','nagpur',1000.00,3000.00,2100.00,'per quintal',@d7,NOW()),
('Tomato','Aurangabad APMC','maharashtra','aurangabad',900.00,2800.00,1900.00,'per quintal',@d14,NOW()),
('Tomato','Pune APMC','maharashtra','pune',800.00,2500.00,1700.00,'per quintal',@d21,NOW()),
('Tomato','Pune APMC','maharashtra','pune',600.00,2000.00,1400.00,'per quintal',@d30,NOW());

-- ─── POTATO — Maharashtra ────────────────────────────────────────────────────
INSERT INTO market_prices (commodity, market_name, state, district, min_price, max_price, modal_price, price_unit, trade_date, created_at) VALUES
('Potato','Pune APMC','maharashtra','pune',700.00,1200.00,950.00,'per quintal',@today,NOW()),
('Potato','Nashik APMC','maharashtra','nashik',680.00,1150.00,920.00,'per quintal',@today,NOW()),
('Potato','Nagpur APMC','maharashtra','nagpur',720.00,1220.00,980.00,'per quintal',@d7,NOW()),
('Potato','Kolhapur APMC','maharashtra','kolhapur',660.00,1130.00,900.00,'per quintal',@d14,NOW());

-- ─── SOYBEAN — Maharashtra ───────────────────────────────────────────────────
INSERT INTO market_prices (commodity, market_name, state, district, min_price, max_price, modal_price, price_unit, trade_date, created_at) VALUES
('Soybean','Nagpur APMC','maharashtra','nagpur',3800.00,4600.00,4300.00,'per quintal',@today,NOW()),
('Soybean','Aurangabad APMC','maharashtra','aurangabad',3750.00,4550.00,4250.00,'per quintal',@today,NOW()),
('Soybean','Amravati APMC','maharashtra','amravati',3700.00,4500.00,4200.00,'per quintal',@d7,NOW()),
('Soybean','Latur APMC','maharashtra','latur',3720.00,4520.00,4220.00,'per quintal',@d7,NOW()),
('Soybean','Nagpur APMC','maharashtra','nagpur',3680.00,4480.00,4180.00,'per quintal',@d14,NOW()),
('Soybean','Nagpur APMC','maharashtra','nagpur',3640.00,4440.00,4120.00,'per quintal',@d21,NOW()),
('Soybean','Nagpur APMC','maharashtra','nagpur',3600.00,4400.00,4080.00,'per quintal',@d30,NOW());

-- ─── COTTON — Maharashtra ────────────────────────────────────────────────────
INSERT INTO market_prices (commodity, market_name, state, district, min_price, max_price, modal_price, price_unit, trade_date, created_at) VALUES
('Cotton','Nagpur APMC','maharashtra','nagpur',5800.00,7200.00,6500.00,'per quintal',@today,NOW()),
('Cotton','Aurangabad APMC','maharashtra','aurangabad',5750.00,7150.00,6450.00,'per quintal',@today,NOW()),
('Cotton','Amravati APMC','maharashtra','amravati',5700.00,7100.00,6400.00,'per quintal',@d7,NOW()),
('Cotton','Latur APMC','maharashtra','latur',5720.00,7120.00,6420.00,'per quintal',@d7,NOW()),
('Cotton','Nagpur APMC','maharashtra','nagpur',5680.00,7050.00,6350.00,'per quintal',@d14,NOW()),
('Cotton','Nagpur APMC','maharashtra','nagpur',5640.00,7000.00,6300.00,'per quintal',@d21,NOW());

-- ─── MAIZE — Maharashtra / Rajasthan ─────────────────────────────────────────
INSERT INTO market_prices (commodity, market_name, state, district, min_price, max_price, modal_price, price_unit, trade_date, created_at) VALUES
('Maize','Pune APMC','maharashtra','pune',1600.00,2100.00,1900.00,'per quintal',@today,NOW()),
('Maize','Nashik APMC','maharashtra','nashik',1580.00,2080.00,1880.00,'per quintal',@today,NOW()),
('Maize','Nagpur APMC','maharashtra','nagpur',1620.00,2120.00,1920.00,'per quintal',@d7,NOW()),
('Maize','Jaipur APMC','rajasthan','jaipur',1700.00,2200.00,1980.00,'per quintal',@today,NOW()),
('Maize','Jodhpur APMC','rajasthan','jodhpur',1680.00,2180.00,1960.00,'per quintal',@d7,NOW());

-- ─── SUGARCANE — Maharashtra ─────────────────────────────────────────────────
INSERT INTO market_prices (commodity, market_name, state, district, min_price, max_price, modal_price, price_unit, trade_date, created_at) VALUES
('Sugarcane','Pune APMC','maharashtra','pune',285.00,310.00,295.00,'per quintal',@today,NOW()),
('Sugarcane','Kolhapur APMC','maharashtra','kolhapur',290.00,315.00,300.00,'per quintal',@today,NOW()),
('Sugarcane','Nashik APMC','maharashtra','nashik',280.00,305.00,290.00,'per quintal',@d7,NOW()),
('Sugarcane','Aurangabad APMC','maharashtra','aurangabad',275.00,300.00,285.00,'per quintal',@d7,NOW());

-- ─── GROUNDNUT — Gujarat ─────────────────────────────────────────────────────
INSERT INTO market_prices (commodity, market_name, state, district, min_price, max_price, modal_price, price_unit, trade_date, created_at) VALUES
('Groundnut','Rajkot APMC','gujarat','rajkot',4800.00,5800.00,5400.00,'per quintal',@today,NOW()),
('Groundnut','Ahmedabad APMC','gujarat','ahmedabad',4750.00,5750.00,5350.00,'per quintal',@today,NOW()),
('Groundnut','Surat APMC','gujarat','surat',4700.00,5700.00,5300.00,'per quintal',@d7,NOW()),
('Groundnut','Rajkot APMC','gujarat','rajkot',4680.00,5680.00,5250.00,'per quintal',@d14,NOW()),
('Groundnut','Rajkot APMC','gujarat','rajkot',4650.00,5650.00,5200.00,'per quintal',@d21,NOW()),
('Groundnut','Rajkot APMC','gujarat','rajkot',4620.00,5620.00,5150.00,'per quintal',@d30,NOW());

-- ─── GRAM (Chickpea) — Madhya Pradesh ────────────────────────────────────────
INSERT INTO market_prices (commodity, market_name, state, district, min_price, max_price, modal_price, price_unit, trade_date, created_at) VALUES
('Gram','Indore APMC','madhya pradesh','indore',4600.00,5500.00,5100.00,'per quintal',@today,NOW()),
('Gram','Bhopal APMC','madhya pradesh','bhopal',4580.00,5480.00,5080.00,'per quintal',@today,NOW()),
('Gram','Gwalior APMC','madhya pradesh','gwalior',4560.00,5460.00,5050.00,'per quintal',@d7,NOW()),
('Gram','Indore APMC','madhya pradesh','indore',4540.00,5440.00,5020.00,'per quintal',@d14,NOW()),
('Gram','Indore APMC','madhya pradesh','indore',4520.00,5420.00,5000.00,'per quintal',@d21,NOW()),
('Gram','Indore APMC','madhya pradesh','indore',4500.00,5400.00,4970.00,'per quintal',@d30,NOW());

-- ─── MUSTARD — Rajasthan ─────────────────────────────────────────────────────
INSERT INTO market_prices (commodity, market_name, state, district, min_price, max_price, modal_price, price_unit, trade_date, created_at) VALUES
('Mustard','Jaipur APMC','rajasthan','jaipur',4800.00,5600.00,5300.00,'per quintal',@today,NOW()),
('Mustard','Jodhpur APMC','rajasthan','jodhpur',4750.00,5550.00,5250.00,'per quintal',@today,NOW()),
('Mustard','Kota APMC','rajasthan','kota',4700.00,5500.00,5200.00,'per quintal',@d7,NOW()),
('Mustard','Jaipur APMC','rajasthan','jaipur',4680.00,5480.00,5170.00,'per quintal',@d14,NOW()),
('Mustard','Jaipur APMC','rajasthan','jaipur',4650.00,5450.00,5130.00,'per quintal',@d21,NOW()),
('Mustard','Jaipur APMC','rajasthan','jaipur',4620.00,5420.00,5100.00,'per quintal',@d30,NOW());

-- ─── ARHAR / TUR (Pigeon Pea) — Maharashtra ─────────────────────────────────
INSERT INTO market_prices (commodity, market_name, state, district, min_price, max_price, modal_price, price_unit, trade_date, created_at) VALUES
('Arhar','Nagpur APMC','maharashtra','nagpur',5500.00,7000.00,6400.00,'per quintal',@today,NOW()),
('Arhar','Latur APMC','maharashtra','latur',5450.00,6950.00,6350.00,'per quintal',@today,NOW()),
('Arhar','Aurangabad APMC','maharashtra','aurangabad',5400.00,6900.00,6300.00,'per quintal',@d7,NOW()),
('Arhar','Nanded APMC','maharashtra','nanded',5380.00,6880.00,6280.00,'per quintal',@d7,NOW()),
('Arhar','Nagpur APMC','maharashtra','nagpur',5360.00,6860.00,6250.00,'per quintal',@d14,NOW()),
('Arhar','Nagpur APMC','maharashtra','nagpur',5340.00,6840.00,6220.00,'per quintal',@d21,NOW()),
('Arhar','Nagpur APMC','maharashtra','nagpur',5300.00,6800.00,6180.00,'per quintal',@d30,NOW());

-- ─── BAJRA — Rajasthan ───────────────────────────────────────────────────────
INSERT INTO market_prices (commodity, market_name, state, district, min_price, max_price, modal_price, price_unit, trade_date, created_at) VALUES
('Bajra','Jaipur APMC','rajasthan','jaipur',1800.00,2300.00,2100.00,'per quintal',@today,NOW()),
('Bajra','Jodhpur APMC','rajasthan','jodhpur',1780.00,2280.00,2080.00,'per quintal',@today,NOW()),
('Bajra','Kota APMC','rajasthan','kota',1760.00,2260.00,2060.00,'per quintal',@d7,NOW());

-- ─── JOWAR — Maharashtra ─────────────────────────────────────────────────────
INSERT INTO market_prices (commodity, market_name, state, district, min_price, max_price, modal_price, price_unit, trade_date, created_at) VALUES
('Jowar','Solapur APMC','maharashtra','solapur',2200.00,2800.00,2600.00,'per quintal',@today,NOW()),
('Jowar','Pune APMC','maharashtra','pune',2180.00,2780.00,2580.00,'per quintal',@today,NOW()),
('Jowar','Aurangabad APMC','maharashtra','aurangabad',2160.00,2760.00,2560.00,'per quintal',@d7,NOW());

-- ─── TELANGANA — Rice & Cotton ───────────────────────────────────────────────
INSERT INTO market_prices (commodity, market_name, state, district, min_price, max_price, modal_price, price_unit, trade_date, created_at) VALUES
('Rice','Hyderabad APMC','telangana','hyderabad',2900.00,3800.00,3400.00,'per quintal',@today,NOW()),
('Rice','Warangal APMC','telangana','warangal',2850.00,3750.00,3350.00,'per quintal',@today,NOW()),
('Rice','Nizamabad APMC','telangana','nizamabad',2800.00,3700.00,3300.00,'per quintal',@d7,NOW()),
('Cotton','Warangal APMC','telangana','warangal',5900.00,7300.00,6600.00,'per quintal',@today,NOW()),
('Cotton','Nizamabad APMC','telangana','nizamabad',5850.00,7250.00,6550.00,'per quintal',@d7,NOW());

-- ─── KARNATAKA ───────────────────────────────────────────────────────────────
INSERT INTO market_prices (commodity, market_name, state, district, min_price, max_price, modal_price, price_unit, trade_date, created_at) VALUES
('Tomato','Bangalore APMC','karnataka','bangalore',1500.00,4000.00,2800.00,'per quintal',@today,NOW()),
('Tomato','Mysore APMC','karnataka','mysore',1400.00,3800.00,2600.00,'per quintal',@today,NOW()),
('Onion','Bangalore APMC','karnataka','bangalore',900.00,1900.00,1500.00,'per quintal',@today,NOW()),
('Potato','Hubli APMC','karnataka','dharwad',750.00,1300.00,1050.00,'per quintal',@today,NOW());

-- ─── UTTAR PRADESH ───────────────────────────────────────────────────────────
INSERT INTO market_prices (commodity, market_name, state, district, min_price, max_price, modal_price, price_unit, trade_date, created_at) VALUES
('Wheat','Lucknow APMC','uttar pradesh','lucknow',2080.00,2340.00,2220.00,'per quintal',@today,NOW()),
('Wheat','Agra APMC','uttar pradesh','agra',2060.00,2320.00,2200.00,'per quintal',@today,NOW()),
('Wheat','Varanasi APMC','uttar pradesh','varanasi',2040.00,2300.00,2180.00,'per quintal',@d7,NOW()),
('Potato','Agra APMC','uttar pradesh','agra',650.00,1100.00,880.00,'per quintal',@today,NOW()),
('Potato','Lucknow APMC','uttar pradesh','lucknow',660.00,1120.00,900.00,'per quintal',@today,NOW()),
('Sugarcane','Lucknow APMC','uttar pradesh','lucknow',305.00,330.00,315.00,'per quintal',@today,NOW()),
('Sugarcane','Varanasi APMC','uttar pradesh','varanasi',295.00,320.00,305.00,'per quintal',@today,NOW());

-- ─── HARYANA ─────────────────────────────────────────────────────────────────
INSERT INTO market_prices (commodity, market_name, state, district, min_price, max_price, modal_price, price_unit, trade_date, created_at) VALUES
('Wheat','Karnal APMC','haryana','karnal',2095.00,2360.00,2255.00,'per quintal',@today,NOW()),
('Wheat','Hisar APMC','haryana','hisar',2085.00,2345.00,2240.00,'per quintal',@today,NOW()),
('Rice','Karnal APMC','haryana','karnal',3000.00,4200.00,3600.00,'per quintal',@today,NOW());

SELECT 'Market price seed data inserted successfully!' as Status, COUNT(*) as TotalRecords FROM market_prices;
