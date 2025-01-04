-- Aktiviert die UUID-Erweiterung, falls nicht vorhanden:
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TABLE IF EXISTS cards CASCADE;
DROP TABLE IF EXISTS packages CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS decks CASCADE;

-- Tabelle für user
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    token VARCHAR(255),
    name VARCHAR(100),
    bio TEXT,
    image TEXT,
    coins INT DEFAULT 20,
    elo INT DEFAULT 100,
    wins INT DEFAULT 0,
    draws INT DEFAULT 0,
    losses INT DEFAULT 0
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
    package_id UUID REFERENCES packages(package_id),
    user_id UUID REFERENCES users(id) -- NULL, wenn nicht zugeordnet
);

-- Tabelle for decks
CREATE TABLE decks (
   user_id UUID REFERENCES users(id) ON DELETE CASCADE,
   card_id UUID REFERENCES cards(card_id) ON DELETE CASCADE,
   PRIMARY KEY (user_id, card_id)
);