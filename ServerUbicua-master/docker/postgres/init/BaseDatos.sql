
CREATE USER ubicomp_user WITH PASSWORD 'ubicomp123';
ALTER USER ubicomp_user WITH LOGIN;
ALTER USER ubicomp_user SET search_path TO public;

ALTER DATABASE ubicomp OWNER TO ubicomp_user;

\c ubicomp

CREATE TABLE IF NOT EXISTS mediciones  (

    id SERIAL PRIMARY KEY,
    timestamp TIMESTAMPTZ,

    temperatura NUMERIC(5,2),
    humedad NUMERIC(5,2),
    radiacion_uv NUMERIC(5,2),
    ruido_db NUMERIC(8,2),
    calidad_aire NUMERIC(8,2)

);

GRANT ALL PRIVILEGES ON TABLE mediciones TO ubicomp_user;
GRANT ALL PRIVILEGES ON SEQUENCE mediciones_id_seq TO ubicomp_user;

