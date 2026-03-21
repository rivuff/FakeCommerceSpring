DROP DATABASE fakecommerce;

CREATE DATABASE fakecommerce;

use fakecommerce;

show tables;

desc products;
desc categories;
desc orders;
desc order_products;
desc reviews;

select * from products;
select * from categories;
select * from orders;
select * from order_products;

select count(*) from products where price > 80; -- 301
EXPLAIN select * from products where price > 80; -- index scan
EXPLAIN select * from products where created_at = NOW() and price > 80; -- index
EXPLAIN select * from products where price > 80 and created_at = NOW(); -- index
EXPLAIN select * from products where price = 80 and rating = 3; -- full table scan
EXPLAIN select * from products where rating = 3 and  price = 80 ; -- full table scan

EXPLAIN select * from products where created_at = NOW() and rating = 3 ; -- not a prefix , 
explain select * from products where created_at <= NOW() ;  -- 1002
select count(*) from products where created_at <= CONVERT_TZ('2026-03-21 22:23:29', '+05:30', @@session.time_zone);  -- 1002

explain select * from products where created_at <= '2026-03-21 22:30:29' ; -- 1002
explain select * from products where created_at <= CONVERT_TZ('2026-03-21 22:23:29', '+05:30', @@session.time_zone);  -- index



explain select * from products where price = 80  and rating > 3 ; 

INSERT INTO products (title, description, price, category_id, image, rating, created_at)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 1000
)
SELECT 
    CONCAT('Awesome Product ', n),                                 
    CONCAT('This is an automatically generated description for product #', n),
    ROUND((RAND() * 100) + 10, 2),                                 
    1,                                         
    CONCAT('https://example.com/images/prod_', n, '.jpg'),         
    ROUND((RAND() * 4) + 1, 1),                                    -- FIX: Now generates a pure number like 4.2
    NOW(6)                                                         
FROM seq;

CREATE INDEX idx_price ON products (price); -- create an index
ALTER TABLE products DROP INDEX idx_price_rating_category; -- delete an index

CREATE INDEX idx_price_rating_category ON products (created_at, price, rating, category_id); -- create an index






SELECT p.*, c.name FROM products p INNER JOIN categories c on p.category_id = c.id where p.id = 1;

SELECT DISTINCT category FROM products;

select * from flyway_schema_history;

CREATE INDEX idx_price ON products (price) ALGORITHM=INPLACE LOCK=NONE;
ALTER TABLE products DROP INDEX idx_price;
EXPLAIN select * from products where price > 65000;
