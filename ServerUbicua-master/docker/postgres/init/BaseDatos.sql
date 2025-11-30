

CREATE USER ubicomp_user WITH PASSWORD 'ubicomp123';
ALTER USER ubicomp_user WITH LOGIN;
ALTER USER ubicomp_user SET search_path TO public;
CREATE DATABASE ubicomp OWNER ubicomp_user;


GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ubicomp_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ubicomp_user;

\c ubicomp;

CREATE TABLE mediciones (

    id SERIAL PRIMARY KEY,
    timestamp TIMESTAMPTZ,

    temperatura NUMERIC(5,2),
    humedad NUMERIC(5,2),
    radiacion_uv NUMERIC(5,2),
    ruido_db NUMERIC(8,2),
    calidad_aire NUMERIC(8,2)

);


