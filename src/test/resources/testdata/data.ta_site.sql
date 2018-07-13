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
CREATE TABLE data.ta_site
(
    id integer NOT NULL,
    name text,
    type text,
    county text,
    document jsonb,
    broader_iri text,
    CONSTRAINT ta_site_pkey PRIMARY KEY (id)
);

INSERT INTO data.ta_site VALUES (6, 'Saidenbach Reservoir', 'reservoir', 'Saxony, Germany', '{"area": "1.46407", "notes": "notes ...", "rtime": "325", "trophy": "mesotroph", "volume": "22.427623", "altitude": "438.8", "flowrate": "0.799", "location": [{"lat": 50.733074, "lng": 13.221099}], "synonyms": ["Saidenbachtalsperre"], "catchment": "60.8", "depth_avg": "15.32", "depth_max": "47.8", "shoreline": "11.1", "catch_type": "22 % forests, 68 % agriculture (thereof 28 % arable land and 78 % grassland), 10 % urban"}', NULL);
INSERT INTO data.ta_site VALUES (3, 'Müggelsee', 'lake', 'Berlin, Germany', '{"area": "7.3", "rtime": "60", "mixing": "polymictic", "trophy": "unknown", "volume": "36", "altitude": "32", "flowrate": "7", "location": [{"lat": 52.437751974295, "lng": 13.652572631836}], "synonyms": ["großer Müggelsee", "Mueggelsee"], "catchment": "7000", "depth_avg": "4.9", "depth_max": "8", "shoreline": "11.5", "catch_type": "agriculture, forested, urbana"}', NULL);
INSERT INTO data.ta_site VALUES (2, 'Fuchskuhle', 'lake', 'Brandenburg, Germany', '{"location": [{"lat": 53.105741739556, "lng": 12.984895706177}], "synonyms": []}', NULL);
INSERT INTO data.ta_site VALUES (5, 'Lake Constance', 'lake', 'Baden Würtemberg, Germany', '{"area": "476", "mixing": "monomictic", "trophy": "oligitroph", "volume": "48", "altitude": "395", "location": [{"lat": 47.614958395344, "lng": 9.3891906738281}], "synonyms": ["Bodensee"], "catchment": "11500", "depth_avg": "101", "depth_max": "252"}', NULL);
INSERT INTO data.ta_site VALUES (1, 'Lake Stechlin', 'lake', 'Brandenburg, Germany', '{"mixing": "dimictic", "location": [{"lat": 53.150708250618, "lng": 13.030385971069}], "synonyms": ["Stechlinsee", "Stechlin"]}', NULL);
INSERT INTO data.ta_site VALUES (8, 'Lake Tegel', 'lake', 'Berlin, Germany', '{"area": "4,6", "mixing": "dimictic", "volume": "32", "location": [{"lat": 52.579661948703, "lng": 13.261528015137}], "synonyms": ["Tegeler See"], "depth_avg": "6", "depth_max": "15"}', NULL);
INSERT INTO data.ta_site VALUES (7, 'Schlachtensee', 'lake', 'Berlin, Germany', '{"location": [{"lat": 52.441656506755, "lng": 13.214557170868}], "synonyms": []}', NULL);
INSERT INTO data.ta_site VALUES (4, 'Bautzen Reservoir', 'reservoir', 'Saxony, Germany', '{"area": "5.33", "notes": "notes ...", "rtime": "193", "trophy": "eutroph", "volume": "41", "altitude": "168", "flowrate": "2.73", "location": [{"lat": 51.217529376577, "lng": 14.456462860107}], "synonyms": ["Talsperre Bautzen"], "catchment": "310.5", "depth_avg": "7.4", "depth_max": "13.5"}', NULL);
INSERT INTO data.ta_site VALUES (10, 'Spree', 'river', 'Brandenburg, Saxony/ Germany', '{"location": [{"lat": 52.519145709999, "lng": 13.379974365234}, {"lat": 52.29546223185, "lng": 14.263000488281}, {"lat": 51.823047011882, "lng": 14.352264404297}, {"lat": 51.009704556253, "lng": 14.639539718628}], "synonyms": []}', NULL);
INSERT INTO data.ta_site VALUES (48, 'Moor westlich Töpchin-Waldeck', 'peatland/ fen', 'Germany, Brandenburg, NP Dahme Heideseen', '{"location": [{"lat": 52.162043, "lng": 13.580804}]}', NULL);
INSERT INTO data.ta_site VALUES (49, 'Moor am Pastlingsee bei Grabko', 'peatland/ fen', 'Germany, Brandenburg, NP Schlaubetal', '{"location": [{"lat": 51.910493, "lng": 14.543889}]}', NULL);
INSERT INTO data.ta_site VALUES (50, 'Moor N Kieselwitzer Mühle östlich der Schlaube', 'peatland/ fen', 'Germany, Brandenburg, NP Schlaubetal', '{"location": [{"lat": 52.117989, "lng": 14.475241}]}', NULL);
INSERT INTO data.ta_site VALUES (51, 'Rambower Moor', 'peatland/ fen', 'Germany, Brandenburg, Prignitz', '{"location": [{"lat": 53.127022, "lng": 11.566635}]}', NULL);
INSERT INTO data.ta_site VALUES (52, 'Moor am Kleinen Gollinsee', 'peatland/ fen', 'Germany, Brandenburg, Schorfheide', '{"location": [{"lat": 53.027, "lng": 13.5888}]}', NULL);
INSERT INTO data.ta_site VALUES (53, 'Moor am Dagowsee', 'peatland/ fen', 'Germany, Brandenburg, Stechlinseegebiet', '{"location": [{"lat": 53.153533, "lng": 13.044617}]}', NULL);
INSERT INTO data.ta_site VALUES (54, 'Moor am Wittwesee (Ostufer)', 'peatland/ fen', 'Germany, Brandenburg, Stechlinseegebiet', '{"location": [{"lat": 53.127784, "lng": 12.958199}]}', NULL);
INSERT INTO data.ta_site VALUES (55, 'Moor an der Großen Fuchskuhle', 'peatland/ fen', 'Germany, Brandenburg, Stechlinseegebiet', '{"location": [{"lat": 53.104955, "lng": 12.983628}]}', NULL);
INSERT INTO data.ta_site VALUES (56, 'Moor am Bollwinfließ nördlich Gollin', 'peatland/ fen', 'Germany, Brandenburg, Uckermark', '{"location": [{"lat": 53.060989, "lng": 13.595038}]}', NULL);
INSERT INTO data.ta_site VALUES (57, 'Moor am Ostufer des Lehstsees bei Lychen', 'peatland/ fen', 'Germany, Brandenburg, Uckermark', '{"location": [{"lat": 53.223717, "lng": 13.345533}]}', NULL);
INSERT INTO data.ta_site VALUES (58, 'Sernitzniederung bei Greiffenberg', 'peatland/ fen', 'Germany, Brandenburg, Uckermark', '{"location": [{"lat": 53.087206, "lng": 13.917308}]}', NULL);
INSERT INTO data.ta_site VALUES (59, 'Moor am Triebschsee (NSG)', 'peatland/ fen', 'Germany, Brandenburg, Valley of River Spree', '{"location": [{"lat": 52.342472, "lng": 13.801127}]}', NULL);
INSERT INTO data.ta_site VALUES (18, 'Large european rivers', 'river', 'Europe', '{"notes": "notes ...", "location": [{"lat": 52.895079089858, "lng": 9.2739200592041}, {"lat": 53.312288096256, "lng": 22.465281486511}, {"lat": 48.553659923865, "lng": 13.436965942383}, {"lat": 53.394384930567, "lng": 10.44319152832}, {"lat": 53.201457051277, "lng": 7.4111366271973}, {"lat": 52.780962211263, "lng": 12.22297668457}, {"lat": 52.501541464295, "lng": 6.0505056381226}, {"lat": 51.412979045428, "lng": 22.099717855453}, {"lat": 51.968278868825, "lng": 5.1785087585449}, {"lat": 51.705119166685, "lng": 5.9561347961426}, {"lat": 52.718202641689, "lng": 21.104135513306}, {"lat": 50.309186529573, "lng": 20.714120864868}, {"lat": 52.769435361554, "lng": 14.294586181641}, {"lat": 50.583127617161, "lng": 7.2226524353027}, {"lat": 51.759792055111, "lng": 11.702198982239}, {"lat": 52.297273209025, "lng": 14.246048927307}, {"lat": 46.527365087709, "lng": 20.159268379211}, {"lat": 53.785034997864, "lng": 18.840522766113}, {"lat": 52.813086272227, "lng": 9.1870594024658}], "synonyms": []}', NULL);
INSERT INTO data.ta_site VALUES (15, 'Running water bodies', 'river', 'Austria', '{"notes": "notes ...", "location": [{"lat": 48.452887283381, "lng": 15.232543945312}], "synonyms": []}', NULL);
INSERT INTO data.ta_site VALUES (73, 'Moor am Kleinen Milasee', 'peatland/ fen', 'Germany, Brandenburg, NP Dahme Heideseen', '{"location": [{"lat": 52.152070, "lng": 13.957539}]}', NULL);
INSERT INTO data.ta_site VALUES (16, 'Arendsee', 'lake', 'Saxony-Anhalt', '{"area": "5,1", "rtime": "53 years", "mixing": "dimictic", "trophy": "polytroph", "volume": "149", "location": [{"lat": 52.88896866209, "lng": 11.47590637207}], "synonyms": ["Lake Arendsee"], "catchment": "29,5", "depth_avg": "29", "depth_max": "49", "catch_type": "agriculture 32,6%, forest 30,6%, greenland 19,5%, urban 17,3%"}', NULL);
INSERT INTO data.ta_site VALUES (21, 'Moor Esäkeste', 'peatland/ bog', 'Estonia', '{"location": [{"lat": 58.21469, "lng": 27.36042}]}', NULL);
INSERT INTO data.ta_site VALUES (20, 'Collrunge', 'lake', 'germany', '{"area": "0.0498", "notes": "notes ...", "trophy": "mesotroph", "volume": "0.0002", "flowrate": "0", "location": [{"lat": 53.501704144976, "lng": 7.6828765869141}], "synonyms": [], "depth_avg": "3.1", "depth_max": "9.2", "shoreline": "0.84"}', NULL);
INSERT INTO data.ta_site VALUES (17, 'Aquaponik Müritzfischer', 'other', 'Germany, Mecklenburg-Vorpommern, Waren', '{"location": [{"lat": 53.512171702592, "lng": 12.639502286911}], "synonyms": []}', NULL);
INSERT INTO data.ta_site VALUES (22, 'Moor Koordi', 'peatland/ bog', 'Estonia', '{"location": [{"lat": 58.97708, "lng": 25.64855}]}', NULL);
INSERT INTO data.ta_site VALUES (23, 'Moor Moksi', 'peatland/ bog', 'Estonia', '{"location": [{"lat": 58.09839, "lng": 25.04408}]}', NULL);
INSERT INTO data.ta_site VALUES (24, 'Moor Tellissaare', 'peatland/ bog', 'Estonia', '{"location": [{"lat": 59.04218, "lng": 25.52013}]}', NULL);
INSERT INTO data.ta_site VALUES (25, 'Moor Umbusi', 'peatland/ bog', 'Estonia', '{"location": [{"lat": 58.57205, "lng": 26.18375}]}', NULL);
INSERT INTO data.ta_site VALUES (26, 'Moor Vedelsoo', 'peatland/ bog', 'Estonia', '{"location": [{"lat": 58.08425, "lng": 25.13166}]}', NULL);
INSERT INTO data.ta_site VALUES (27, 'Moor bei Kobuleti', 'peatland/ bog', 'Georgia, Kolchis', '{"location": [{"lat": 41.863489, "lng": 41.801848}]}', NULL);
INSERT INTO data.ta_site VALUES (28, 'Hundekehlefenn', 'peatland/ fen', 'Germany, Berlin, Grunewald', '{"location": [{"lat": 52.4779, "lng": 13.2654}]}', NULL);
INSERT INTO data.ta_site VALUES (29, 'Moor am Barssee', 'peatland/ fen', 'Germany, Berlin, Grunewald', '{"location": [{"lat": 52.4779, "lng": 13.2156}]}', NULL);
INSERT INTO data.ta_site VALUES (30, 'Pechsee', 'peatland/ fen', 'Germany, Berlin, Grunewald', '{"location": [{"lat": 52.4824, "lng": 13.213}]}', NULL);
INSERT INTO data.ta_site VALUES (31, 'Kleine Pelzlake', 'peatland/ fen', 'Germany, Berlin, Köpenick', '{"location": [{"lat": 52.421, "lng": 13.7124}]}', NULL);
INSERT INTO data.ta_site VALUES (32, 'Krumme Lake', 'peatland/ fen', 'Germany, Berlin, Köpenick', '{"location": [{"lat": 52.4167, "lng": 13.68338}]}', NULL);
INSERT INTO data.ta_site VALUES (33, 'Teufelsseemoor', 'peatland/ fen', 'Germany, Berlin, Köpenick', '{"location": [{"lat": 52.41979, "lng": 13.632744}]}', NULL);
INSERT INTO data.ta_site VALUES (34, 'Bad Saarow', 'peatland/ fen', 'Germany, Brandenburg', '{"location": [{"lat": 52.2944, "lng": 14.047067}]}', NULL);
INSERT INTO data.ta_site VALUES (35, 'Demnitzer Mühlenfließ', 'peatland/ fen', 'Germany, Brandenburg', '{"location": [{"lat": 52.345433, "lng": 14.1969}]}', NULL);
INSERT INTO data.ta_site VALUES (36, 'Krummes Luch bei Königs Wusterhausen OT Uckley', 'peatland/ fen', 'Germany, Brandenburg', '{"location": [{"lat": 52.335933, "lng": 13.731767}]}', NULL);
INSERT INTO data.ta_site VALUES (37, 'Löcknitztal bei Erkner', 'peatland/ fen', 'Germany, Brandenburg', '{"location": [{"lat": 52.46308, "lng": 13.97963}]}', NULL);
INSERT INTO data.ta_site VALUES (38, 'Nieplitzniederung', 'peatland/ fen', 'Germany, Brandenburg', '{"location": [{"lat": 52.2081, "lng": 13.094067}]}', NULL);
INSERT INTO data.ta_site VALUES (39, 'NSG Langedammwiesen bei Torfhaus/Strausberg', 'peatland/ fen', 'Germany, Brandenburg', '{"location": [{"lat": 52.515842, "lng": 13.844359}]}', NULL);
INSERT INTO data.ta_site VALUES (40, 'Sophienstädt bei Eberswalde', 'peatland/ fen', 'Germany, Brandenburg', '{"location": [{"lat": 52.802604, "lng": 13.578935}]}', NULL);
INSERT INTO data.ta_site VALUES (41, 'Moor nördlich Kablow Ziegelei', 'peatland/ fen', 'Germany, Brandenburg', '{"location": [{"lat": 52.32725, "lng": 13.7274}]}', NULL);
INSERT INTO data.ta_site VALUES (42, 'Großes Skabybruch bei Spreenhagen', 'peatland/ fen', 'Germany, Brandenburg, NP Dahme Heideseen', '{"location": [{"lat": 52.312117, "lng": 13.821133}]}', NULL);
INSERT INTO data.ta_site VALUES (43, 'Luchsee bei Krausnick', 'peatland/ fen', 'Germany, Brandenburg, NP Dahme Heideseen', '{"location": [{"lat": 52.0418, "lng": 13.791933}]}', NULL);
INSERT INTO data.ta_site VALUES (44, 'Moor am Kleinen Milasee', 'peatland/ fen', 'Germany, Brandenburg, NP Dahme Heideseen', '{"location": [{"lat": 52.151933, "lng": 13.957567}]}', NULL);
INSERT INTO data.ta_site VALUES (45, 'Moor am Südufer des Pätzer Hintersees', 'peatland/ fen', 'Germany, Brandenburg, NP Dahme Heideseen', '{"location": [{"lat": 52.196995, "lng": 13.624717}]}', NULL);
INSERT INTO data.ta_site VALUES (46, 'Moor am SW-Ufer des Dollgensees (Dollgener Grund)', 'peatland/ fen', 'Germany, Brandenburg, NP Dahme Heideseen', '{"location": [{"lat": 52.004817, "lng": 14.019917}]}', NULL);
INSERT INTO data.ta_site VALUES (47, 'Moor östlich Halbe', 'peatland/ fen', 'Germany, Brandenburg, NP Dahme Heideseen', '{"location": [{"lat": 52.105442, "lng": 13.723919}]}', NULL);
INSERT INTO data.ta_site VALUES (60, 'Kleiner Landgraben bei Neubrandenburg', 'peatland/ fen', 'Germany, Mecklenburg-Vorpommern', '{"location": [{"lat": 53.668059, "lng": 13.293002}]}', NULL);
INSERT INTO data.ta_site VALUES (61, 'Moore im Peenetal', 'peatland/ fen', 'Germany, Mecklenburg-Vorpommern', '{"location": [{"lat": 53.916561, "lng": 13.388995}]}', NULL);
INSERT INTO data.ta_site VALUES (62, 'Ahlenmoor bei Wanna', 'peatland/ bog', 'Germany, Niedersachsen', '{"location": [{"lat": 53.685525, "lng": 8.828369}]}', NULL);
INSERT INTO data.ta_site VALUES (63, 'Moor am Rospuda bei Szczebra nördlich Augustow', 'peatland/ fen', 'Poland, NE part, region Augustow', '{"location": [{"lat": 53.901608, "lng": 22.953672}]}', NULL);
INSERT INTO data.ta_site VALUES (64, 'Moor nördlich Zolwia Bloc', 'peatland/ fen', 'Poland, NW part', '{"location": [{"lat": 53.635217, "lng": 14.875983}]}', NULL);
INSERT INTO data.ta_site VALUES (65, 'Mszar (Moor nördlich Sosnowo)', 'peatland/ fen', 'Poland, NW part', '{"location": [{"lat": 53.7925, "lng": 15.521933}]}', NULL);
INSERT INTO data.ta_site VALUES (66, 'Rosiczka (Moor 6 km NW Babigoszcz)', 'peatland/ fen', 'Poland, NW part', '{"location": [{"lat": 53.700990, "lng": 14.706112}]}', NULL);
INSERT INTO data.ta_site VALUES (67, 'Moor bei Rzecin', 'peatland/ fen', 'Poland, western part', '{"location": [{"lat": 52.76155, "lng": 16.309467}]}', NULL);
INSERT INTO data.ta_site VALUES (68, 'Moor Cross Lochs, pristine site', 'peatland/ bog', 'Scotland', '{"location": [{"lat": 58.372689, "lng": -3.959525}]}', NULL);
INSERT INTO data.ta_site VALUES (69, 'Moor Cross Lochs, rewetted site', 'peatland/ bog', 'Scotland', '{"location": [{"lat": 58.375958, "lng": -3.951811}]}', NULL);
INSERT INTO data.ta_site VALUES (70, 'Moor Munsary', 'peatland/ bog', 'Scotland', '{"location": [{"lat": 58.396844, "lng": -3.340869}]}', NULL);
INSERT INTO data.ta_site VALUES (71, 'Likstermossen', 'peatland/ bog', 'Sweden', '{"location": [{"lat": 59.669922, "lng": 14.177270}]}', NULL);
INSERT INTO data.ta_site VALUES (72, 'Lungsmossen', 'peatland/ bog', 'Sweden', '{"location": [{"lat": 59.549312, "lng": 14.237662}]}', NULL);
