-- ============================================================
-- AI FARMER ASSISTANT PLATFORM — SEED DATA
-- Run AFTER schema.sql
-- ============================================================

USE farmer_assistant;

-- ============================================================
-- DEFAULT ADMIN USER
-- Email: admin@farmerassist.in | Password: Admin@123 (BCrypt)
-- ============================================================
INSERT INTO users (email, password_hash, role, first_name, last_name, phone, is_active, is_email_verified)
VALUES (
    'admin@farmerassist.in',
    '$2a$12$XqVT7W4ZcRsTHoLNqQk0sOk4nOc3QL0W9JJYEKVq0kCyHxZTH/vC.',
    'ADMIN',
    'Platform',
    'Admin',
    '9999999999',
    TRUE,
    TRUE
);

-- ============================================================
-- GOVERNMENT SCHEMES — 20 Real Indian Agricultural Schemes
-- ============================================================
INSERT INTO government_schemes
  (title, description, eligibility, benefits, documents_required, official_url, category, applicable_states, applicable_crops, min_land_acres, max_land_acres, is_active, created_by)
VALUES

-- 1. PM-KISAN
(
  'PM Kisan Samman Nidhi (PM-KISAN)',
  'Pradhan Mantri Kisan Samman Nidhi is a central sector scheme launched in 2019 to provide income support to all landholder farmer families. The government provides financial benefit of Rs. 6000 per year to eligible farmer families, payable in three equal installments of Rs. 2000 each, every four months.',
  'All landholder farmer families with cultivable land, with certain exclusion criteria for high-income groups, government employees, and income-tax payees.',
  'Rs. 6,000 per year directly transferred to bank account in 3 installments of Rs. 2,000 each via Direct Benefit Transfer (DBT). No upper land limit for most states.',
  'Aadhaar Card, Land records / Khasra Khatauni, Bank passbook, Mobile number linked to Aadhaar',
  'https://pmkisan.gov.in',
  'SUBSIDY',
  'All',
  'All',
  0.1,
  NULL,
  TRUE,
  1
),

-- 2. PMFBY
(
  'Pradhan Mantri Fasal Bima Yojana (PMFBY)',
  'A comprehensive crop insurance scheme launched in 2016 to provide financial support to farmers suffering crop loss due to natural calamities, pests and diseases. The scheme aims to stabilise the income of farmers to ensure their continuance in farming.',
  'All farmers including sharecroppers and tenant farmers growing notified crops in notified areas are eligible. Loanee farmers are compulsorily covered.',
  'Insurance coverage and financial support for crop failure. Premium: 2% for Kharif crops, 1.5% for Rabi crops, 5% for commercial/horticultural crops. Full actuarial premium paid by Government.',
  'Aadhaar Card, Bank account details, Land records (7/12 or similar), Sowing certificate, Mobile number',
  'https://pmfby.gov.in',
  'INSURANCE',
  'All',
  'Wheat,Rice,Maize,Cotton,Sugarcane,Pulses,Oilseeds',
  0.25,
  NULL,
  TRUE,
  1
),

-- 3. KCC
(
  'Kisan Credit Card (KCC) Scheme',
  'The Kisan Credit Card (KCC) scheme was introduced in 1998 to provide adequate and timely credit support from banking system to the farmers for their short-term credit needs for crop cultivation, purchase of inputs, maintenance expenses and other allied activities.',
  'All farmers including individual/joint borrowers who are owner cultivators, tenant farmers, oral lessees and sharecroppers, SHGs or joint liability groups of farmers.',
  'Short-term credit for crop cultivation up to 3 lakhs at concessional interest rate of 4% per annum (with interest subvention). Additional credit for post-harvest and allied activities. Revolving credit facility.',
  'Aadhaar Card, Land documents, Bank account, 2 passport photos, No-due certificate from existing bank',
  'https://agricoop.gov.in/kisan-credit-card',
  'LOAN',
  'All',
  'All',
  0.1,
  NULL,
  TRUE,
  1
),

-- 4. Soil Health Card
(
  'Soil Health Card Scheme (SHC)',
  'The Soil Health Card (SHC) scheme was launched in 2015 to promote soil test based balanced use of fertilizers, and to assess the soil health situation in the country. A Soil Health Card is issued to farmers carrying crop-wise recommendations of nutrients and fertilisers required for the individual farms.',
  'All farmers in India are eligible for free Soil Health Cards.',
  'Free soil testing, personalized crop-wise fertilizer recommendation card, reduced fertilizer expenditure, improved crop yield through balanced nutrition.',
  'Aadhaar Card, Land records, Mobile number',
  'https://soilhealth.dac.gov.in',
  'TRAINING',
  'All',
  'All',
  0.1,
  NULL,
  TRUE,
  1
),

-- 5. RKVY
(
  'Rashtriya Krishi Vikas Yojana (RKVY)',
  'RKVY is a Centrally Sponsored Scheme launched in 2007 to achieve 4% annual growth in agriculture sector during the 11th Five Year Plan by ensuring holistic development of agriculture and allied sectors. It provides flexibility and autonomy to states in planning and executing agriculture and allied sector schemes.',
  'State governments, farmers, farmer producer organizations (FPOs), agri-startups, and rural entrepreneurs.',
  'Funding for agricultural infrastructure, agri-startups through RAFT (Remunerative Approaches for Agriculture and Allied sector Rejuvenation), skill development, value chain development, and AGRI-UDAAN accelerator program.',
  'Application through State Agriculture Department, Project proposal, Land records',
  'https://rkvy.nic.in',
  'SUBSIDY',
  'All',
  'All',
  NULL,
  NULL,
  TRUE,
  1
),

-- 6. PMKSY
(
  'Pradhan Mantri Krishi Sinchayee Yojana (PMKSY)',
  'PMKSY was launched in 2015 with the vision of ensuring access to some means of protective irrigation to all agricultural farms in India to produce MORE CROP PER DROP and to bring more area under cultivation (Har Khet Ko Pani). The scheme focuses on enhancing water use efficiency.',
  'Individual farmers, farmer groups, SHGs, water user associations, and cooperatives with agricultural land.',
  'Subsidy of 45% for small and marginal farmers and 35% for other farmers on micro-irrigation equipment (drip and sprinkler). Convergence of water conservation schemes under one umbrella.',
  'Land records, Bank account details, Aadhaar card, Electricity connection details for pump',
  'https://pmksy.gov.in',
  'EQUIPMENT',
  'All',
  'All',
  0.25,
  NULL,
  TRUE,
  1
),

-- 7. e-NAM
(
  'National Agriculture Market (e-NAM)',
  'e-NAM is a pan-India electronic trading portal that networks the existing APMC mandis to create a unified national market for agricultural commodities. The portal provides a single window service for all APMC related information and services including commodity arrivals and prices, buy and sell trade offers, and settlement of trades.',
  'Farmers, traders, and buyers registered on the e-NAM portal in participating states. Over 1.74 crore farmers already registered.',
  'Better price discovery through transparent online bidding, reduced transaction costs, access to larger market, online payment facility, quality assaying at mandis.',
  'Aadhaar Card, Bank account, Mobile number, Land records (for farmer registration at nearest APMC mandi)',
  'https://enam.gov.in',
  'TRAINING',
  'All',
  'All',
  NULL,
  NULL,
  TRUE,
  1
),

-- 8. ATMA
(
  'Agricultural Technology Management Agency (ATMA)',
  'ATMA is a district level autonomous institution under the Centrally Sponsored Scheme for extension reforms. It aims to make agricultural extension system farmer-driven and farmer-accountable by integrating all extension activities at district level and mobilizing existing resources efficiently.',
  'Farmers of all categories, with priority to small and marginal farmers, women farmers, and SC/ST farmers.',
  'Technology demonstrations, training camps, farmer field schools, kisan melas, exposure visits to research institutions, farm advisory services, access to expert knowledge.',
  'Farmer registration at district agriculture office, Aadhaar Card',
  'https://agricoop.gov.in',
  'TRAINING',
  'All',
  'All',
  NULL,
  NULL,
  TRUE,
  1
),

-- 9. MIDH
(
  'Mission for Integrated Development of Horticulture (MIDH)',
  'MIDH is a Centrally Sponsored Scheme for the holistic growth of the horticulture sector covering fruits, vegetables, root and tuber crops, mushrooms, spices, flowers, aromatic plants, coconut, cashew and cocoa. The scheme aims at production of horticulture crops through area expansion, productivity improvement, and reduction in post harvest losses.',
  'Individual farmers, farmer groups, cooperatives, NGOs, State/Central Government agencies for establishing orchards, nurseries, post-harvest infrastructure.',
  'Subsidy of 40-50% on planting material, drip/sprinkler irrigation, green houses, shade net houses, post-harvest management, and cold storage. Additional 15% top-up for SC/ST/Women/North-East farmers.',
  'Land records, Bank account, Aadhaar card, Project proposal for infrastructure schemes',
  'https://midh.gov.in',
  'SUBSIDY',
  'All',
  'Fruits,Vegetables,Spices,Flowers,Mushroom',
  0.5,
  NULL,
  TRUE,
  1
),

-- 10. PM Kisan Maan Dhan Yojana
(
  'PM Kisan Maan Dhan Yojana (PM-KMY)',
  'PM Kisan Maan Dhan Yojana is a voluntary and contributory pension scheme for small and marginal farmers. Under this scheme, assured pension of Rs. 3000 per month will be provided to the eligible beneficiaries on attaining 60 years of age. The monthly contribution is co-shared 50:50 by the Government of India.',
  'Small and marginal farmers with age between 18-40 years, with cultivable land up to 2 hectares as per land records of respective State/UTs.',
  'Guaranteed monthly pension of Rs. 3000/month after attaining 60 years of age. Government contributes matching amount equal to farmer''s contribution. In case of death, 50% of pension to spouse.',
  'Aadhaar Card, Bank account linked to Aadhaar, Land records, Mobile number',
  'https://pmkmy.gov.in',
  'SUBSIDY',
  'All',
  'All',
  0.1,
  4.94,
  TRUE,
  1
),

-- 11. National Beekeeping Mission
(
  'National Beekeeping and Honey Mission (NBHM)',
  'National Beekeeping and Honey Mission was launched in 2020 under the Atma Nirbhar Bharat Package to promote scientific beekeeping for boosting crop productivity and generating employment and income. The mission aims to produce 1,20,000 MT honey by 2021-22.',
  'Individual beekeepers, farmer groups, FPOs, cooperatives, self-help groups involved in or willing to start beekeeping activities.',
  'Financial assistance for purchase of bee colonies, bee boxes, honey extractors, and other equipment. 50% subsidy for general category and 80% for SC/ST/Women farmers. Training on beekeeping practices.',
  'Aadhaar Card, Bank account, Land documents, Project proposal',
  'https://agricoop.gov.in',
  'EQUIPMENT',
  'All',
  'All',
  NULL,
  NULL,
  TRUE,
  1
),

-- 12. Sub-Mission on Agricultural Mechanization
(
  'Sub-Mission on Agricultural Mechanization (SMAM)',
  'SMAM aims to accelerate the growth of farm mechanization in the country. Financial assistance is provided to farmers for purchase of agricultural machinery and equipment. Custom Hiring Centres (CHCs) are set up to provide agricultural machinery on rental basis to small and marginal farmers.',
  'Individual farmers, groups of farmers, cooperatives, FPOs. Priority to small and marginal farmers, SC/ST farmers, and women farmers. CHC proposals from entrepreneurs, FPOs, cooperatives.',
  'Subsidy of 40-50% for individual farmers on purchase of tractors, power tillers, threshers, sprayers. Financial assistance for establishing Custom Hiring Centres, High-Tech Hubs, and Farm Machinery Banks.',
  'Aadhaar card, Bank passbook, Land records, Quotation for machinery, Caste certificate (for SC/ST)',
  'https://agricoop.gov.in/smam',
  'EQUIPMENT',
  'All',
  'All',
  0.5,
  NULL,
  TRUE,
  1
),

-- 13. PKVY Paramparagat Krishi Vikas Yojana
(
  'Paramparagat Krishi Vikas Yojana (PKVY)',
  'PKVY is a sub-component of Soil Health Management under National Mission of Sustainable Agriculture (NMSA). It aims to support and promote organic farming. Organic farming leads to long term soil fertility building, resource conservation, sustainability, and reduction in input cost to farmers.',
  'Farmers willing to adopt organic farming in cluster mode. Minimum 50 farmers per cluster with 50 acres of land. Priority to small and marginal farmers.',
  'Rs. 50,000 per hectare per 3 years financial assistance to farmers for on-farm input production units, certification, and marketing support. Training, exposure visits, and capacity building included.',
  'Aadhaar card, Land records, Bank account, Group/cluster formation documents',
  'https://pgsindia-ncof.gov.in',
  'SUBSIDY',
  'All',
  'All',
  0.5,
  NULL,
  TRUE,
  1
),

-- 14. National Food Security Mission
(
  'National Food Security Mission (NFSM)',
  'NFSM is a centrally sponsored scheme launched in 2007-08 to increase production of rice, wheat, pulses, coarse cereals, and nutri-cereals through area expansion and productivity enhancement in a sustainable manner. It aims to restore soil fertility and productivity at the individual farm level.',
  'All farmers in identified districts. Priority to small and marginal farmers in targeted NFSM districts.',
  'Supply of certified quality seeds at subsidized rates, demonstrations of improved varieties, distribution of farm machinery and equipment at subsidy, Integrated Nutrient Management, Integrated Pest Management, training and extension activities.',
  'Land records, Aadhaar card, Bank account details, Application at nearest KVK or agriculture department office',
  'https://nfsm.gov.in',
  'SEED',
  'All',
  'Rice,Wheat,Pulses,Maize,Millet',
  0.25,
  NULL,
  TRUE,
  1
),

-- 15. Agriculture Infrastructure Fund
(
  'Agriculture Infrastructure Fund (AIF)',
  'Agriculture Infrastructure Fund is a medium to long-term debt financing facility for investment in viable projects for post-harvest management infrastructure and community farming assets. The scheme provides interest subvention of 3% per annum on loans up to Rs. 2 crore from scheduled banks and financial institutions.',
  'Farmers, FPOs, PACS, Marketing Cooperative Societies, SHGs, Joint Liability Groups, Multipurpose Cooperative Societies, Agri-Entrepreneurs, Start-ups, Central/State government bodies.',
  '3% annual interest subvention for 7 years on loans up to Rs. 2 crore. Credit guarantee coverage under CGTMSE for loans up to Rs. 2 crore. Maximum loan moratorium of 2 years. Projects: cold storage, warehouses, silos, pack houses, grading and sorting units, processing units.',
  'Project proposal, Land records or lease agreement, Bank account, Business plan, CA-certified financial statements (for existing businesses)',
  'https://agriinfra.dac.gov.in',
  'LOAN',
  'All',
  'All',
  NULL,
  NULL,
  TRUE,
  1
),

-- 16. PM FME
(
  'PM Formalisation of Micro Food Processing Enterprises (PM FME)',
  'PM FME scheme was launched in 2020 under Atma Nirbhar Bharat to support the unorganised food processing sector with financial, technical, and business support. The scheme helps existing micro food processing enterprises upgrade their units.',
  'Individual micro food processing enterprises, SHGs, FPOs, Cooperatives engaged in food processing. Annual turnover of unit should be less than Rs. 1 crore.',
  'Credit linked subsidy of 35% of eligible project cost (maximum Rs. 10 lakh) for individual units. Seed capital of Rs. 40,000 per SHG member. Support for branding, marketing, and FSSAI compliance.',
  'Aadhaar card, Bank account, GST registration (or application), FSSAI license, Existing unit proof, Project report',
  'https://pmfme.mofpi.gov.in',
  'SUBSIDY',
  'All',
  'All',
  NULL,
  NULL,
  TRUE,
  1
),

-- 17. Gramin Bhandaran Yojana
(
  'Warehouse Infrastructure Fund (WIF) / Gramin Bhandaran Yojana',
  'Scheme to incentivize construction and renovation of rural godowns (warehouses) to create scientific storage capacity in rural areas and reduce post-harvest losses. It enables farmers to store produce and reduce distress sale during harvest.',
  'Farmers, agricultural graduates, cooperatives, companies, corporations, SHGs, NGOs for constructing new or renovating old rural warehouses of minimum 100 MT capacity.',
  'Capital subsidy of 25% of project cost (33.33% for SC/ST promoters and NE region). Maximum subsidy Rs. 225 lakh for general and Rs. 300 lakh for SC/ST/NE. Subsidy channeled through NABARD.',
  'Land ownership documents, Bank loan sanction letter, Approved plan from local authority, No objection certificate',
  'https://nabard.org',
  'EQUIPMENT',
  'All',
  'All',
  NULL,
  NULL,
  TRUE,
  1
),

-- 18. NMAET
(
  'National Mission on Agricultural Extension and Technology (NMAET)',
  'NMAET aims to restructure and strengthen agricultural extension to enable delivery of appropriate technology and improved agronomic practices to farmers through a robust extension system. It covers four sub-missions including SMAM, SMAE, SMPPQ, and SMAQS.',
  'All categories of farmers, with special emphasis on small, marginal, women farmers, and SC/ST communities.',
  'Technology demonstrations, training, capacity building, ICT-based extension services (Kisan Call Centers, mKisan), farmer field schools, soil and seed testing services, quality control for seeds, fertilizers, and pesticides.',
  'Farmer registration at district agriculture office, Aadhaar Card',
  'https://agricoop.gov.in/nmaet',
  'TRAINING',
  'All',
  'All',
  NULL,
  NULL,
  TRUE,
  1
),

-- 19. PM AASHA
(
  'PM Annadata Aay Sanrakshan Abhiyan (PM-AASHA)',
  'PM-AASHA is an umbrella scheme launched in 2018 to ensure that farmers get remunerative prices for their produce and to provide protection against fall in prices for farmers. The scheme covers Price Support Scheme (PSS), Price Deficiency Payment Scheme (PDPS), and Pilot of Private Procurement and Stockist Scheme (PPPS).',
  'All farmers growing oilseeds, pulses, and copra in the states that adopt the scheme. The scheme applies when market prices fall below Minimum Support Price (MSP).',
  'Price Deficiency Payment of difference between MSP and actual market price. Direct procurement at MSP through government agencies (NAFED, FCI). Price protection for oilseeds and pulses.',
  'Land records, Aadhaar card, Bank account, Sowing declaration, Mobile number',
  'https://agricoop.gov.in',
  'SUBSIDY',
  'All',
  'Pulses,Oilseeds,Cotton,Wheat,Rice',
  0.25,
  NULL,
  TRUE,
  1
),

-- 20. DARE-ICAR
(
  'ICAR Research Grants and Training Programs',
  'Indian Council of Agricultural Research (ICAR) provides research grants, technology dissemination, and training programs through its network of 113 institutes and 71 KVKs (Krishi Vigyan Kendras) across India. Programs cover crop research, animal science, fisheries, and food technology.',
  'Farmers, rural youth, farm women, agricultural graduates. KVK vocational training open to all. Research grants for scientists and institutions.',
  'Free training programs at KVKs (2 weeks to 6 months), technology demonstrations on farmers'' fields, seed production programs, exposure visits, farm advisor consultations, soil testing at subsidized rates.',
  'Aadhaar card, Application at nearest KVK, No formal land documents required for training',
  'https://icar.org.in',
  'TRAINING',
  'All',
  'All',
  NULL,
  NULL,
  TRUE,
  1
);

-- ============================================================
-- SAMPLE MARKET PRICES (10 major commodities)
-- ============================================================
INSERT INTO market_prices (commodity, market_name, state, district, min_price, max_price, modal_price, price_unit, trade_date)
VALUES
('Wheat',     'Azadpur Mandi',    'Delhi',        'North West Delhi', 2100.00, 2350.00, 2225.00, 'per quintal', CURDATE() - INTERVAL 1 DAY),
('Rice',      'Amritsar Mandi',   'Punjab',       'Amritsar',         1850.00, 2200.00, 2050.00, 'per quintal', CURDATE() - INTERVAL 1 DAY),
('Maize',     'Gulbarga Market',  'Karnataka',    'Kalaburagi',       1700.00, 1950.00, 1830.00, 'per quintal', CURDATE() - INTERVAL 1 DAY),
('Cotton',    'Akola Market',     'Maharashtra',  'Akola',            6200.00, 6800.00, 6500.00, 'per quintal', CURDATE() - INTERVAL 1 DAY),
('Soybean',   'Indore Market',    'M.P.',         'Indore',           4500.00, 4900.00, 4700.00, 'per quintal', CURDATE() - INTERVAL 1 DAY),
('Tomato',    'Kolar Market',     'Karnataka',    'Kolar',             800.00, 1400.00, 1100.00, 'per quintal', CURDATE() - INTERVAL 1 DAY),
('Onion',     'Lasalgaon Market', 'Maharashtra',  'Nashik',           1200.00, 1800.00, 1500.00, 'per quintal', CURDATE() - INTERVAL 1 DAY),
('Potato',    'Agra Market',      'U.P.',         'Agra',              600.00,  950.00,  780.00, 'per quintal', CURDATE() - INTERVAL 1 DAY),
('Sugarcane', 'Muzaffarnagar',    'U.P.',         'Muzaffarnagar',     350.00,  380.00,  365.00, 'per quintal', CURDATE() - INTERVAL 1 DAY),
('Tur Dal',   'Latur Market',     'Maharashtra',  'Latur',            6800.00, 7500.00, 7150.00, 'per quintal', CURDATE() - INTERVAL 1 DAY),

-- Yesterday prices for trend
('Wheat',     'Azadpur Mandi',    'Delhi',        'North West Delhi', 2080.00, 2320.00, 2200.00, 'per quintal', CURDATE() - INTERVAL 2 DAY),
('Rice',      'Amritsar Mandi',   'Punjab',       'Amritsar',         1820.00, 2180.00, 2020.00, 'per quintal', CURDATE() - INTERVAL 2 DAY),
('Maize',     'Gulbarga Market',  'Karnataka',    'Kalaburagi',       1680.00, 1920.00, 1800.00, 'per quintal', CURDATE() - INTERVAL 2 DAY),
('Cotton',    'Akola Market',     'Maharashtra',  'Akola',            6100.00, 6700.00, 6400.00, 'per quintal', CURDATE() - INTERVAL 2 DAY),
('Soybean',   'Indore Market',    'M.P.',         'Indore',           4400.00, 4800.00, 4620.00, 'per quintal', CURDATE() - INTERVAL 2 DAY),
('Tomato',    'Kolar Market',     'Karnataka',    'Kolar',             750.00, 1350.00, 1050.00, 'per quintal', CURDATE() - INTERVAL 2 DAY),
('Onion',     'Lasalgaon Market', 'Maharashtra',  'Nashik',           1150.00, 1750.00, 1450.00, 'per quintal', CURDATE() - INTERVAL 2 DAY),
('Potato',    'Agra Market',      'U.P.',         'Agra',              580.00,  920.00,  750.00, 'per quintal', CURDATE() - INTERVAL 2 DAY),
('Sugarcane', 'Muzaffarnagar',    'U.P.',         'Muzaffarnagar',     348.00,  378.00,  362.00, 'per quintal', CURDATE() - INTERVAL 2 DAY),
('Tur Dal',   'Latur Market',     'Maharashtra',  'Latur',            6750.00, 7450.00, 7100.00, 'per quintal', CURDATE() - INTERVAL 2 DAY);

-- Welcome notification for admin
INSERT INTO notifications (user_id, title, message, type, is_read)
VALUES (1, 'Welcome to AI Farmer Assistant', 'Platform is now live. All systems operational.', 'SUCCESS', FALSE);
