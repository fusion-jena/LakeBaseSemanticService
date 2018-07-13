---
-- #%L
-- LakeBase Semantic Service
-- %%
-- Copyright (C) 2018 Heinz Nixdorf Chair for Distributed Information Systems, Friedrich Schiller University Jena
-- %%
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--      http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- #L%
---
CREATE TABLE data.parameter
(
    id integer NOT NULL,
    name text,
    subject text,
    document jsonb,
    touch_by_cron boolean,
    replacement integer,
    CONSTRAINT parameter_id PRIMARY KEY (id)
);
    
INSERT INTO data.parameter VALUES (26, 'Biovolume', 'biology', '{"synonyms": [], "standard_unit": "µm³"}', true, NULL);
INSERT INTO data.parameter VALUES (14, 'phycocyanin', 'biology', '{"synonyms": [], "description": "description ...", "standard_unit": "RFU"}', true, NULL);
INSERT INTO data.parameter VALUES (21, 'Ammonium', 'chemistry', '{"symbole": "NH4+", "synonyms": [], "description": "description ...", "standard_unit": "mg/l"}', true, NULL);
INSERT INTO data.parameter VALUES (22, 'nitrate', 'chemistry', '{"symbole": "NO3_N", "synonyms": []}', true, NULL);
INSERT INTO data.parameter VALUES (24, 'nitrite', 'chemistry', '{"symbole": "NO2-N", "synonyms": ["Nitrit"], "standard_unit": "mg/L"}', true, NULL);
INSERT INTO data.parameter VALUES (16, 'Oxygen concentration', 'chemistry', '{"symbole": "O2, O2_con", "synonyms": ["Sauerstoffkonzentration"], "standard_unit": "mg/l"}', true, NULL);
INSERT INTO data.parameter VALUES (15, 'Oxygen satuation', 'chemistry', '{"symbole": "O2, O2_sat", "synonyms": ["Sauerstoffsättigung"], "standard_unit": "%"}', true, NULL);
INSERT INTO data.parameter VALUES (29, 'iron (dissolved iron)', 'chemistry', '{"symbole": "Fe", "synonyms": ["Eisen", "Ferrum"], "method_id": "36", "standard_unit": "mg/L"}', true, NULL);
INSERT INTO data.parameter VALUES (19, 'total dissolved phosphorus', 'chemistry', '{"symbole": "TDP", "synonyms": [], "standard_unit": "µg/l"}', true, NULL);
INSERT INTO data.parameter VALUES (30, 'total nitrogen', 'chemistry', '{"symbole": "TN", "synonyms": ["Gesamtstickstoff"], "standard_unit": "mg L-1"}', true, NULL);
INSERT INTO data.parameter VALUES (20, 'total phosphorus', 'chemistry', '{"symbole": "TP", "synonyms": []}', true, NULL);
INSERT INTO data.parameter VALUES (28, 'fish abundance and biomass', 'fish monitoring', '{"synonyms": [], "standard_unit": "catch per unit effort"}', true, NULL);
INSERT INTO data.parameter VALUES (4, 'air pressure', 'meteorology', '{"symbole": "p, a press, a_press", "synonyms": ["Luftdruck", "Athmosphäre"], "standard_unit": "hPa"}', true, NULL);
INSERT INTO data.parameter VALUES (2, 'air temperature', 'meteorology', '{"symbole": "at, T", "synonyms": ["Lufttemperatur"], "standard_unit": "°C"}', true, NULL);
INSERT INTO data.parameter VALUES (9, 'global radiation', 'meteorology', '{"symbole": "gr", "synonyms": ["Globalstrahlung", "Einstrahlung"], "standard_unit": "W/m²"}', true, NULL);
INSERT INTO data.parameter VALUES (5, 'precipitation', 'meteorology', '{"symbole": "precip", "synonyms": ["Niederschlag", "Rain", "Regen"], "description": "description ...", "standard_unit": "mm"}', true, NULL);
INSERT INTO data.parameter VALUES (3, 'relative humidity', 'meteorology', '{"symbole": "hum", "synonyms": ["relative Luftfeuchtigkeit"], "description": "description ...", "standard_unit": "%"}', true, NULL);
INSERT INTO data.parameter VALUES (8, 'wind direction', 'meteorology', '{"symbole": "wd, wind dir", "synonyms": ["Windrichtung"], "description": "description ...", "standard_unit": "°"}', true, NULL);
INSERT INTO data.parameter VALUES (7, 'wind speed', 'meteorology', '{"symbole": "ws, Vwind", "synonyms": ["Windgeschwindigkeit", "Windstärke"], "reference": "6", "standard_unit": "m/s"}', true, NULL);
INSERT INTO data.parameter VALUES (17, 'Secchi depth', 'physics', '{"symbole": "secchi", "synonyms": ["Sichttiefe", "Transparancy"], "description": "description ...", "standard_unit": "m"}', true, NULL);
INSERT INTO data.parameter VALUES (12, 'turbidity', 'physics', '{"synonyms": ["Trübung"]}', true, NULL);
INSERT INTO data.parameter VALUES (6, 'velocity', 'physics', '{"symbole": "V", "synonyms": ["speed", "Geschwindigkeit"], "standard_unit": "m/s"}', true, NULL);
INSERT INTO data.parameter VALUES (1, 'water temperature', 'physics', '{"symbole": "wt, T", "synonyms": ["water temp", "Wassertemperatur"], "description": "description ...", "standard_unit": "°C"}', true, NULL);
INSERT INTO data.parameter VALUES (18, 'soluble reactive phosphorus', 'chemistry', '{"symbole": "SRP", "synonyms": ["Phosphor gelöst"], "method_id": "75", "description": "description ...", "standard_unit": "µg/l"}', true, NULL);
INSERT INTO data.parameter VALUES (11, 'pH', 'chemistry', '{"synonyms": [], "method_id": "45"}', true, NULL);
INSERT INTO data.parameter VALUES (10, 'Conductivity', 'physics', '{"symbole": "cond, L", "synonyms": ["elektrische Leitfähigkeit", "Salinität", "Salzgehalt", "Konduktivität"], "method_id": "74", "standard_unit": "µS/cm"}', true, NULL);
INSERT INTO data.parameter VALUES (31, 'total inorganic carbon', 'chemistry', '{"symbole": "TIC, DIC", "synonyms": ["Gesamt anorganischer Kohlenstoff"], "method_id": "69", "description": "description ...", "standard_unit": "mg L-1"}', true, NULL);
INSERT INTO data.parameter VALUES (34, 'Abundance', 'biology', '{"synonyms": []}', true, NULL);
INSERT INTO data.parameter VALUES (25, 'biomass', 'biology', '{"synonyms": [], "reference": "mass", "description": "description ...", "standard_unit": "mg/l"}', true, NULL);
INSERT INTO data.parameter VALUES (13, 'chlorophyll a', 'biology', '{"symbole": "chl_a", "synonyms": ["Chlorophyllkonzentration"], "description": "description ...", "standard_unit": "µg/l"}', true, NULL);
INSERT INTO data.parameter VALUES (35, 'Production', 'biology', '{"synonyms": []}', true, NULL);
INSERT INTO data.parameter VALUES (33, 'Vertical mixing intensity', 'physics', '{"synonyms": []}', true, NULL);
INSERT INTO data.parameter VALUES (27, 'Dissolved organic carbon', 'chemistry', '{"symbole": "DOC", "synonyms": ["dissolved organic material (DOM)"], "method_id": "67", "description": "description ...", "standard_unit": "mg C L-1"}', true, NULL);
INSERT INTO data.parameter VALUES (55, 'sulfate', 'chemistry', '{"symbole": "SO4 2-", "synonyms": ["sulphate"], "method_id": "50", "description": "description ..."}', true, NULL);
INSERT INTO data.parameter VALUES (56, 'chloride', 'chemistry', '{"symbole": "Cl", "synonyms": [], "method_id": "62"}', true, NULL);
INSERT INTO data.parameter VALUES (57, 'Calcium', 'chemistry', '{"symbole": "Ca", "synonyms": ["Kalzium"]}', true, NULL);
INSERT INTO data.parameter VALUES (58, 'Magnesium', 'chemistry', '{"symbole": "Mg", "synonyms": []}', true, NULL);
INSERT INTO data.parameter VALUES (32, 'Manganese', 'chemistry', '{"symbole": "Mn", "synonyms": ["Mangan"], "standard_unit": "mg L-1"}', true, NULL);
INSERT INTO data.parameter VALUES (59, 'Kalium', 'chemistry', '{"symbole": "K", "synonyms": ["Potassium"], "description": "description ..."}', true, NULL);
INSERT INTO data.parameter VALUES (60, 'Natrium', 'chemistry', '{"symbole": "Na", "synonyms": ["Sodium"]}', true, NULL);
INSERT INTO data.parameter VALUES (61, 'nitrogen (dissolved nitrogen)', 'chemistry', '{"symbole": "DN", "synonyms": ["Stickstoff gelöst"], "method_id": "65"}', true, NULL);
