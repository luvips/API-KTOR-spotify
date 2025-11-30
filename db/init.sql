CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Limpieza
DROP TABLE IF EXISTS tracks;
DROP TABLE IF EXISTS albumes;
DROP TABLE IF EXISTS artistas;

-- Tablas
CREATE TABLE artistas (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    genre VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE albumes (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    release_year INTEGER,
    artist_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_artista FOREIGN KEY (artist_id) REFERENCES artistas(id) ON DELETE RESTRICT
);

CREATE TABLE tracks (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    duration INTEGER NOT NULL,
    album_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_album FOREIGN KEY (album_id) REFERENCES albumes(id) ON DELETE RESTRICT
);

-- Índices
CREATE INDEX idx_albumes_artist_id ON albumes(artist_id);
CREATE INDEX idx_tracks_album_id ON tracks(album_id);