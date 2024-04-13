

CREATE TABLE IF NOT EXISTS source_parser_map (
    id serial PRIMARY KEY,
    name VARCHAR(256) NOT NULL,
    content_type VARCHAR(32) NOT NULL,
    body TEXT
);

CREATE TABLE IF NOT EXISTS job_config (
    id serial PRIMARY KEY,
    name VARCHAR(256) NOT NULL,
    parser_map_id INTEGER NOT NULL,
    upload_method VARCHAR(32) NOT NULL,
    schedule_interval INTEGER DEFAULT NULL, -- run every N days
    schedule_time TIME DEFAULT NULL, -- null -> immediately
    url VARCHAR(4096) NOT NULL,
    pagination_selector TEXT,
    items_selector TEXT,
    idle_timeout INTEGER DEFAULT NULL,
    enabled boolean DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS job (
    id serial PRIMARY KEY,
    config_id INTEGER NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    started_at TIMESTAMP WITHOUT TIME ZONE,
    finished_at TIMESTAMP WITHOUT TIME ZONE,
    upload_counter INTEGER DEFAULT 0,
    document_counter INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS product (
    id serial PRIMARY KEY,
    domain_hash INTEGER NOT NULL,
    hash INTEGER NOT NULL,
    url VARCHAR(4096) NOT NULL,
    sku VARCHAR(32) DEFAULT NULL,
    modification_code VARCHAR(128) DEFAULT NULL,
    modification_name VARCHAR(256) DEFAULT NULL,
    name VARCHAR(1024) NOT NULL,
    price NUMERIC(8, 2) DEFAULT 0,
    stock INTEGER DEFAULT NULL,
    description TEXT,
    preview_url VARCHAR(4096) DEFAULT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);