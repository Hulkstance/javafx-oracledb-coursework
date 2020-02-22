-- Drop some of the tables
DROP TYPE name_t FORCE;
DROP TYPE address_t FORCE;
DROP TYPE user_t FORCE;
DROP TYPE customer_t FORCE;
DROP TYPE staff_t FORCE;
DROP TYPE service_t FORCE;
DROP TYPE rank_t FORCE;
DROP TYPE project_industry_t FORCE;
DROP TYPE project_tag_t FORCE;
DROP TYPE staff_project_t FORCE;
DROP TYPE industry_t FORCE;
DROP TYPE project_t FORCE;
DROP TYPE tag_t FORCE;
DROP TABLE "USER" CASCADE CONSTRAINTS;
DROP TABLE "RANK" CASCADE CONSTRAINTS;
DROP TABLE service CASCADE CONSTRAINTS;
DROP TABLE project CASCADE CONSTRAINTS;
DROP TABLE tag CASCADE CONSTRAINTS;
DROP TABLE project_industry CASCADE CONSTRAINTS;
DROP TABLE project_tag CASCADE CONSTRAINTS;
DROP TABLE staff_project CASCADE CONSTRAINTS;
DROP TABLE industry CASCADE CONSTRAINTS;

-- Oracle Data Types: https://www.w3resource.com/oracle/oracle-data-types.php
-- NVARCHAR2 - за ЕГН, т.е. allocate-ва място за 10 символа и винаги са си 10, независимо дали сме въвели 10
-- VARCHAR2 - за име. Динамично allocate-ва колкото символа имаме
-- В обектните таблици използваме REF вместо FOREIGN KEY
-- Oracle Object Tables Tips: http://www.dba-oracle.com/t_grid_rac_admin_object_tables.htm

CREATE TYPE name_t AS OBJECT (
  first_name     VARCHAR2(32),
  last_name      VARCHAR2(32)
);

CREATE TYPE address_t AS OBJECT (
  province       VARCHAR2(32),
  street         VARCHAR2(32),
  city           VARCHAR2(32),
  postal_code    VARCHAR2(10)
);

CREATE TYPE user_t AS OBJECT (
  idno           NUMBER,
  email          VARCHAR2(40),
  password       VARCHAR2(32),
  name           name_t,
  address        address_t,
  phone          VARCHAR2(15),
  MAP MEMBER FUNCTION get_idno RETURN NUMBER
) NOT FINAL;

CREATE TYPE rank_t AS OBJECT (
  rankno       NUMBER,
  name         VARCHAR2(40),
  description  VARCHAR2(60)
);

CREATE TABLE "RANK" OF rank_t (
  PRIMARY KEY (rankno),
  UNIQUE (name)
);
  
INSERT INTO "RANK" VALUES (1, 'User', 'Simple a user');
INSERT INTO "RANK" VALUES (2, 'Manager', 'Manager');
INSERT INTO "RANK" VALUES (3, 'Administrator', 'Admin');

CREATE TYPE customer_t UNDER user_t (
  
);

CREATE TYPE staff_t UNDER user_t (
  salary       NUMBER(7,2),
  rank         REF rank_t
);

CREATE TABLE "USER" OF user_t (
  PRIMARY KEY (idno),
  UNIQUE (email));

INSERT INTO "USER" VALUES
(customer_t(1, 'admin@test.com', '123456', name_t('Test', 'Admin'), address_t('Sofia', 'bul. Bulgaria 3', 'Sofia', '7000'), '+358888123456'));

INSERT INTO "USER" VALUES
(customer_t(2, 'ivan@test.com', '123457', name_t('Ivan', 'Ivanov'), address_t('Ruse', 'bul. Bulgaria 4', 'Ruse', '7200'), '+358888123457'));

INSERT INTO "USER" VALUES
(staff_t(3, 'maria@gmail.com', '222222', name_t('Maria', 'Petrova'), address_t('Ruse', 'unknown', 'Ruse', '7200'), '+359884555666', 700, (SELECT REF(r) FROM "RANK" r WHERE rankno = 1)));

INSERT INTO "USER" VALUES
(staff_t(4, 'alex@gmail.com', '444444', name_t('Alex', 'Peneva'), address_t('Ruse', 'Rodina 3', 'Ruse', '7200'), '+359884555677', 1200, (SELECT REF(r) FROM "RANK" r WHERE rankno = 3)));

-- All users
SELECT * FROM "USER";

-- Hide IDs for anything but customers
SELECT TREAT(VALUE(u) AS customer_t).idno, u.NAME, u.EMAIL, u.PASSWORD, u.PHONE FROM "USER" u;

-- Show only staff members
SELECT * FROM "USER" u WHERE VALUE(u) IS OF TYPE (staff_t);

CREATE TYPE service_t AS OBJECT (
  serviceno    NUMBER,
  name         VARCHAR2(40),
  description  VARCHAR2(60)
);

CREATE TABLE service OF service_t (PRIMARY KEY (serviceno));

INSERT INTO service VALUES (1, 'Дигитални услуги', 'Дигитални услуги');
INSERT INTO service VALUES (2, 'Рекламни услуги', 'Рекламни услуги');
INSERT INTO service VALUES (3, 'Брандинг', 'Брандинг');
INSERT INTO service VALUES (4, 'Медия шоп', 'Медия шоп');
INSERT INTO service VALUES (5, 'Кампании', 'Кампании');
INSERT INTO service VALUES (6, 'Стратегии', 'Стратегии');

CREATE TYPE project_t AS OBJECT (
  projectno    NUMBER,
  name         VARCHAR2(40),
  description  VARCHAR2(60),
  customer     REF customer_t,
  service      REF service_t
);

-- FK examples: https://docs.oracle.com/cd/B12037_01/appdev.101/b10799/adobjxmp.htm
CREATE TABLE project OF project_t (PRIMARY KEY (projectno));

INSERT INTO project VALUES
(1, 'btv.bg', 'лого', (SELECT TREAT(REF(u) AS REF customer_t) FROM "USER" u WHERE idno = 1 AND VALUE(u) IS OF TYPE (customer_t)), (SELECT REF(s) FROM service s WHERE serviceno = 1));

INSERT INTO project VALUES
(2, 'nova.bg', 'уебсайт', (SELECT TREAT(REF(u) AS REF customer_t) FROM "USER" u WHERE idno = 2 AND VALUE(u) IS OF TYPE (customer_t)), (SELECT REF(s) FROM service s WHERE serviceno = 2));

INSERT INTO project VALUES
(3, 'Стикер', 'дизайн на стикер', (SELECT TREAT(REF(u) AS REF customer_t) FROM "USER" u WHERE idno = 2 AND VALUE(u) IS OF TYPE (customer_t)), (SELECT REF(s) FROM service s WHERE serviceno = 2));


CREATE TYPE staff_project_t AS OBJECT (
  staff       REF staff_t,
  project     REF project_t
);

CREATE TABLE staff_project OF staff_project_t;

CREATE TYPE industry_t AS OBJECT (
  industryno   NUMBER,
  name         VARCHAR2(40)
);

CREATE TABLE industry OF industry_t (PRIMARY KEY (industryno));

INSERT INTO industry VALUES (1, 'Недвижими имоти');
INSERT INTO industry VALUES (2, 'Енергия');
INSERT INTO industry VALUES (3, 'Банки и финанси');
INSERT INTO industry VALUES (4, 'Здравеопазване');
INSERT INTO industry VALUES (5, 'IT индустрия');
INSERT INTO industry VALUES (6, 'Застраховане');
INSERT INTO industry VALUES (7, 'Трафик');
INSERT INTO industry VALUES (8, 'Ритейл');
INSERT INTO industry VALUES (9, 'Автомобили');
INSERT INTO industry VALUES (10, 'Спорт');
INSERT INTO industry VALUES (11, 'Хотели и ресторанти');
INSERT INTO industry VALUES (12, 'Хазарт');
INSERT INTO industry VALUES (13, 'Строителство');
INSERT INTO industry VALUES (14, 'Туризъм');
INSERT INTO industry VALUES (15, 'Образование');
INSERT INTO industry VALUES (16, 'Храни и напитки');
INSERT INTO industry VALUES (17, 'Услуги');
INSERT INTO industry VALUES (18, 'Финансови институции');

CREATE TYPE project_industry_t AS OBJECT (
  project      REF project_t,
  industry     REF industry_t
);

CREATE TABLE project_industry OF project_industry_t;

INSERT INTO project_industry VALUES
((SELECT REF(p) FROM project p WHERE projectno = 1), (SELECT REF(i) FROM industry i WHERE industryno = 1));

INSERT INTO project_industry VALUES
((SELECT REF(p) FROM project p WHERE projectno = 1), (SELECT REF(i) FROM industry i WHERE industryno = 2));

INSERT INTO project_industry VALUES
((SELECT REF(p) FROM project p WHERE projectno = 1), (SELECT REF(i) FROM industry i WHERE industryno = 3));

INSERT INTO project_industry VALUES
((SELECT REF(p) FROM project p WHERE projectno = 2), (SELECT REF(i) FROM industry i WHERE industryno = 2));

INSERT INTO project_industry VALUES
((SELECT REF(p) FROM project p WHERE projectno = 3), (SELECT REF(i) FROM industry i WHERE industryno = 3));

CREATE TYPE tag_t AS OBJECT (
  tagno        NUMBER,
  name         VARCHAR2(40)
);

CREATE TABLE tag OF tag_t (PRIMARY KEY (tagno));

INSERT INTO tag VALUES (1, 'брандинг');
INSERT INTO tag VALUES (2, 'външна реклама');
INSERT INTO tag VALUES (3, 'дигитална кампания');
INSERT INTO tag VALUES (4, 'изложбен щанд');
INSERT INTO tag VALUES (5, 'корпоративна идентичност');
INSERT INTO tag VALUES (6, 'лого дизайн');
INSERT INTO tag VALUES (7, 'мобилно приложение');
INSERT INTO tag VALUES (8, 'принт дизайн');
INSERT INTO tag VALUES (9, 'рекламни кампании');
INSERT INTO tag VALUES (10, 'уебсайт');

CREATE TYPE project_tag_t AS OBJECT (
  project      REF project_t,
  tag          REF tag_t
);

CREATE TABLE project_tag OF project_tag_t;

INSERT INTO project_tag VALUES
((SELECT REF(p) FROM project p WHERE projectno = 1), (SELECT REF(t) FROM tag t WHERE tagno = 1));

INSERT INTO project_tag VALUES
((SELECT REF(p) FROM project p WHERE projectno = 2), (SELECT REF(t) FROM tag t WHERE tagno = 1));

-- Имената на клиентите с повече от 3 проекта, както и броя на проектите
SELECT (TREAT(REF(u) AS REF customer_t).name.first_name ||  ' ' || (TREAT(REF(u) AS REF customer_t).name.last_name)) AS name, COUNT(p.projectno) 
FROM "USER" u JOIN project p ON u.idno = p.customer.idno 
WHERE VALUE(u) IS OF TYPE (customer_t) 
GROUP BY (TREAT(REF(u) AS REF customer_t).name.first_name ||  ' ' || (TREAT(REF(u) AS REF customer_t).name.last_name)) 
HAVING COUNT(p.projectno) >= 3;

-- Имената и e-mail адресите на администраторите
SELECT (TREAT(REF(u) AS REF staff_t).name.first_name ||  ' ' || (TREAT(REF(u) AS REF staff_t).name.last_name)) AS name, 
TREAT(REF(u) AS REF staff_t).email 
FROM "USER" u JOIN "RANK" r ON TREAT(REF(u) AS REF staff_t).rank.rankno = r.rankno
WHERE VALUE(u) IS OF TYPE (staff_t) AND r.name = 'Administrator';

-- Имената на проектите с тагове "брандинг"
SELECT p.name, pt.tag.name FROM project p JOIN project_tag pt ON p.projectno = pt.project.projectno
WHERE pt.tag.name = 'брандинг';