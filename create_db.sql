-- Aktiviert die UUID-Erweiterung, falls nicht vorhanden:
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS cards;
DROP TABLE IF EXISTS packages;
DROP TABLE IF EXISTS user_packages;

-- Tabelle für user
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    token VARCHAR(255),
    name VARCHAR(100),
    bio TEXT,
    image TEXT,
    coins INT DEFAULT 20
);

CREATE TABLE packages (
    package_id UUID PRIMARY KEY DEFAULT uuid_generate_v4()
);

-- Tabelle for cards
CREATE TABLE cards (
    card_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    damage DOUBLE PRECISION NOT NULL,
    type VARCHAR(50) NOT NULL,
    element_type VARCHAR(50) NOT NULL,
    package_id UUID REFERENCES packages(package_id) ON DELETE CASCADE
);

-- Zwischentabelle für user und packages
CREATE TABLE user_packages (
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    package_id UUID REFERENCES packages(package_id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, package_id)
);