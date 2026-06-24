CREATE TABLE IF NOT EXISTS app_users (
    id          BIGSERIAL    PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS inventory_items (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(50)     NOT NULL,
    sku         VARCHAR(30)     NOT NULL UNIQUE,
    quantity    INTEGER         NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    price       NUMERIC(19, 2)  NOT NULL DEFAULT 0.00 CHECK (price >= 0),
    category    VARCHAR(30)     NOT NULL,
    location    VARCHAR(40),
    description VARCHAR(500),
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW()
    );

CREATE INDEX idx_items_name     ON inventory_items (LOWER(name));
CREATE INDEX idx_items_sku      ON inventory_items (LOWER(sku));
CREATE INDEX idx_items_category ON inventory_items (category);
