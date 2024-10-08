-- Aktiviert die UUID-Erweiterung, falls nicht vorhanden:
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS cards;
DROP TABLE IF EXISTS packages;

-- Erstelle die users-Tabelle
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    token VARCHAR(255),
    name VARCHAR(100),
    bio TEXT,
    image TEXT
);

CREATE TABLE packages (
    package_id UUID PRIMARY KEY DEFAULT uuid_generate_v4()
);

-- Table for cards
CREATE TABLE cards (
    card_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    damage INT NOT NULL,
    package_id UUID REFERENCES packages(package_id) ON DELETE CASCADE
);