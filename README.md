# Food Ordering System - Backend

Java 17 + Spring Boot 3 + Spring Security (JWT) + Spring Data JPA + MySQL.
REST API only. The web pages are in a separate repo: `food-ordering-frontend`.

## Run
1. Install JDK 17+, Maven and MySQL. Make sure MySQL is running.
2. Create a file `application-local.properties` in this folder (next to `pom.xml`):
   `spring.datasource.password=your_mysql_password`
   (see `application-local.properties.example`)
3. Run: `mvn spring-boot:run`
4. API runs on http://localhost:8080

The database `food_ordering_v2_db` and all tables are created automatically.
Default admin: admin@food.com / admin123. Sample restaurants and food are added on first run.

## Main endpoints
- POST /api/auth/register, POST /api/auth/login
- GET /api/restaurants, GET /api/restaurants/{id}/foods
- POST /api/restaurants/{id}/rate (customer), GET /api/restaurants/{id}/my-rating
- POST /api/orders, GET /api/orders/my
- Admin: /api/restaurants, /api/foods, /api/orders/admin/*

CORS allows the frontend on http://localhost:* and http://127.0.0.1:*.
