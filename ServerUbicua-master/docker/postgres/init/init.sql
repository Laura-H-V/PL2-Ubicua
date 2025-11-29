-- Initialization script for UBICOMP schema and MEASUREMENT table (PostgreSQL)
-- The Docker Postgres image creates the database named in POSTGRES_DB before running these scripts

-- Create a dedicated schema so application SQL using UBICOMP.MEASUREMENT works
CREATE SCHEMA IF NOT EXISTS UBICOMP;

CREATE TABLE IF NOT EXISTS UBICOMP.MEASUREMENT (
   VALUE INTEGER NOT NULL,
   DATE TIMESTAMP NOT NULL
);

-- Create a dedicated user for the application
DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'ubicomp_user') THEN
      CREATE USER ubicomp_user WITH PASSWORD 'ubicomp_pass';
   END IF;
END$$;

GRANT CONNECT ON DATABASE "UBICOMP" TO ubicomp_user;
-- Grant privileges on the UBICOMP schema to the application user
GRANT USAGE ON SCHEMA UBICOMP TO ubicomp_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA UBICOMP TO ubicomp_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA UBICOMP GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO ubicomp_user;

-- Insert sample data
INSERT INTO MEASUREMENT (VALUE, DATE) VALUES (23, NOW()), (42, NOW());
