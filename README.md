# Inventory Service

REST CRUD сервис для управления товарными позициями (Inventory Items) с RBAC-авторизацией на базе JWT.

## Стек технологий

| Компонент | Версия |
|---|---|
| Java | 17 |
| Spring Boot | 3.3 |
| Spring Security | 6.x |
| Spring Data JPA / Hibernate | 6.x |
| PostgreSQL | 15 |
| Flyway | 10.x |
| OpenAPI / Swagger UI | springdoc 2.5 |
| JWT | JJWT 0.12.5 |
| Micrometer / Prometheus | — |
| MapStruct | 1.5.5 |
| Gradle | 8.8 |
| Testcontainers | 1.19 |

---

## Быстрый старт (Docker Compose)

### Предварительные требования

- Docker 24+
- Docker Compose 2.x

### Запуск

```bash
# Клонируем / переходим в директорию
cd inventory-service

# Сборка образа и запуск всех сервисов
docker compose up --build
```

Сервис будет доступен по адресу: **http://localhost:8080**

Swagger UI: **http://localhost:8080/swagger-ui.html**

OpenAPI JSON: **http://localhost:8080/api-docs**

### Запуск с Prometheus

```bash
docker compose --profile observability up --build
```

Prometheus: **http://localhost:9090**

### Остановка

```bash
docker compose down -v   # -v удаляет volume с данными
```

---

## Локальная разработка (без Docker)

### Зависимости

- JDK 17+
- Gradle 8.8+ (wrapper включён — отдельная установка не нужна)
- PostgreSQL 15 (или запущенный контейнер)

### Создание БД

```sql
CREATE DATABASE inventory;
CREATE USER inventory_user WITH PASSWORD 'inventory_pass';
GRANT ALL PRIVILEGES ON DATABASE inventory TO inventory_user;
```

### Сборка и запуск

```bash
./gradlew bootJar -x test
java -jar target/inventory-service-1.0.0.jar
```

Или через Gradle:

```bash
./gradlew bootRun
```

---

## Переменные окружения

| Переменная | По умолчанию | Описание |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/inventory` | JDBC URL |
| `DB_USERNAME` | `inventory_user` | Пользователь БД |
| `DB_PASSWORD` | `inventory_pass` | Пароль БД |
| `JWT_SECRET` | (встроен) | Base64 HS256 ключ (≥256 бит) |
| `JWT_EXPIRATION_MS` | `86400000` | Время жизни токена (мс) |
| `SERVER_PORT` | `8080` | HTTP порт |
| `LOG_LEVEL` | `INFO` | Уровень логирования |

---

## Миграции базы данных (Flyway)

Скрипты находятся в `src/main/resources/db/migration/`:

| Файл | Описание |
|---|---|
| `V1__init_schema.sql` | Создание таблиц `app_users`, `inventory_items`, индексы |
| `V2__seed_users.sql` | Тестовые пользователи (admin, viewer) |

Flyway запускается автоматически при старте приложения.

---

## Тестовые учётные записи

| Username | Password | Роль |
|---|---|---|
| `admin` | `admin123` | `ADMINISTRATOR` |
| `viewer` | `viewer123` | `VIEWER` |

---

## API Reference

### Аутентификация

#### POST /api/auth/login

Получение JWT токена.

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq .
```

Ответ:
```json
{
  "token": "eyJhbGci...",
  "type": "Bearer",
  "username": "admin",
  "role": "ADMINISTRATOR"
}
```

Сохраните токен:
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r .token)
```

---

### Inventory Items

#### POST /api/items — Создать item (ADMINISTRATOR)

```bash
curl -s -X POST http://localhost:8080/api/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wireless Mouse",
    "sku": "WM-12345",
    "quantity": 100,
    "price": 29.99,
    "category": "ELECTRONICS",
    "location": "warehouse-1",
    "description": "Ergonomic wireless mouse"
  }' | jq .
```

HTTP 201 Created.

#### GET /api/items — Список с фильтрами

```bash
# Все items
curl -s http://localhost:8080/api/items \
  -H "Authorization: Bearer $TOKEN" | jq .

# Фильтр по name (частичное совпадение)
curl -s "http://localhost:8080/api/items?name=mouse" \
  -H "Authorization: Bearer $TOKEN" | jq .

# Фильтр по category + пагинация + сортировка
curl -s "http://localhost:8080/api/items?category=ELECTRONICS&_page=0&_size=5&_sort=price,desc" \
  -H "Authorization: Bearer $TOKEN" | jq .

# Фильтр по SKU
curl -s "http://localhost:8080/api/items?sku=WM" \
  -H "Authorization: Bearer $TOKEN" | jq .
```

#### GET /api/items/{id} — Получить по ID

```bash
curl -s http://localhost:8080/api/items/1 \
  -H "Authorization: Bearer $TOKEN" | jq .
```

#### PUT /api/items/{id} — Обновить item (ADMINISTRATOR)

```bash
curl -s -X PUT http://localhost:8080/api/items/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wireless Mouse Pro",
    "sku": "WM-12345",
    "quantity": 150,
    "price": 39.99,
    "category": "ELECTRONICS",
    "location": "warehouse-2",
    "description": "Upgraded ergonomic wireless mouse"
  }' | jq .
```

#### DELETE /api/items/{id} — Удалить item (ADMINISTRATOR)

```bash
curl -s -X DELETE http://localhost:8080/api/items/1 \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nHTTP Status: %{http_code}\n"
```

HTTP 204 No Content.

#### GET /api/stats — Статистика

```bash
curl -s "http://localhost:8080/api/stats?from=2024-01-01&to=2024-12-31" \
  -H "Authorization: Bearer $TOKEN" | jq .
```

---

### Пример ответа ошибки

```json
{
  "timestamp": "2024-06-01T12:34:56",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/items",
  "fieldErrors": {
    "name": "Name must be between 3 and 50 characters",
    "sku": "SKU must contain only Latin letters, digits, hyphens and underscores"
  }
}
```

---

## Правила авторизации

| Endpoint | ADMINISTRATOR | VIEWER |
|---|---|---|
| `POST /api/items` | ✅ | ❌ 403 |
| `PUT /api/items/{id}` | ✅ | ❌ 403 |
| `DELETE /api/items/{id}` | ✅ | ❌ 403 |
| `GET /api/items` | ✅ | ✅ |
| `GET /api/items/{id}` | ✅ | ✅ |
| `GET /api/stats` | ✅ | ✅ |
| Любой без токена | ❌ 401 | ❌ 401 |

---

## Валидация полей

| Поле | Правило |
|---|---|
| `name` | Обязательное, 3–50 символов |
| `sku` | Обязательное, уникальное, ≤30 символов, только `[A-Za-z0-9\-_]` |
| `quantity` | Обязательное, целое ≥ 0 |
| `price` | Обязательное, ≥ 0.00, макс. 2 знака после запятой |
| `category` | Обязательное, одно из: `ELECTRONICS`, `FURNITURE`, `GROCERY`, `CLOTHING` |
| `location` | Необязательное, ≤40 символов |
| `description` | Необязательное, ≤500 символов |

---

## Параметры пагинации и сортировки

| Параметр | По умолчанию | Описание |
|---|---|---|
| `_page` | `0` | Номер страницы (0-based) |
| `_size` | `20` | Размер страницы |
| `_sort` | `id,asc` | Поле и направление. Допустимые поля: `id`, `name`, `sku`, `price`, `quantity`, `category`, `createdAt`, `updatedAt` |

---

## Запуск тестов

```bash
# Все тесты (требуется Docker для Testcontainers)
./gradlew test

# Только unit-тесты (без Testcontainers)
./gradlew test -Dgroups="unit"

# С отчётом покрытия
./gradlew check
```

---

## Метрики (Prometheus / Micrometer)

| Endpoint | Описание |
|---|---|
| `GET /actuator/health` | Проверка состояния |
| `GET /actuator/prometheus` | Метрики для Prometheus |
| `GET /actuator/metrics` | Список метрик |

---

## Структура проекта

```
src/
├── main/
│   ├── java/com/inventory/
│   │   ├── InventoryApplication.java
│   │   ├── config/
│   │   │   ├── OpenApiConfig.java
│   │   │   └── SecurityConfig.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   └── InventoryItemController.java
│   │   ├── dto/
│   │   │   ├── AuthRequest / AuthResponse
│   │   │   ├── InventoryItemRequest / Response
│   │   │   ├── InventoryItemMapper.java
│   │   │   ├── PagedResponse.java
│   │   │   ├── StatsResponse.java
│   │   │   └── ErrorResponse.java
│   │   ├── entity/
│   │   │   ├── AppUser.java
│   │   │   ├── InventoryItem.java
│   │   │   ├── Category.java (enum)
│   │   │   └── Role.java (enum)
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── ResourceNotFoundException.java
│   │   │   └── DuplicateSkuException.java
│   │   ├── repository/
│   │   │   ├── InventoryItemRepository.java
│   │   │   └── UserRepository.java
│   │   ├── security/
│   │   │   ├── JwtService.java
│   │   │   └── JwtAuthenticationFilter.java
│   │   │   └── CustomUserDetailService.java
│   │   └── service/
│   │       ├── AuthService.java
│   │       └── InventoryItemService.java
│   └── resources/
│       ├── application.yml
│       └── db/migration/
│           ├── V1__init_schema.sql
│           └── V2__seed_users.sql
└── test/
    ├── java/com/inventory/
    │   ├── integration/
    │   │   ├── BaseIntegrationTest.java
    │   │   ├── AuthControllerIntegrationTest.java
    │   │   └── InventoryItemControllerIntegrationTest.java
    │   ├── repository/
    │   │   └── InventoryItemRepositoryTest.java
    │   └── service/
    │       ├── InventoryItemServiceTest.java
    │       └── JwtServiceTest.java
    └── resources/
        └── application-test.yml
```
