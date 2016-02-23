-- Creates a database for AGI.IO distributed systems
-- Example usage:
--   sudo -u postgres psql -f agidb.sql
-- This executes the script using psql as the postgres user. 
-- You need sudo to change to the postgres user without a password.
-- You only need to run this once.

-- Note: 
-- After running this, run postgREST HTTP service like so:
-- ./postgrest-0.2.10.0 --db-host localhost  --db-port 5432 --db-name agidb  --db-user agiu --db-pass password --db-pool 200  --anonymous agiu --port 3000 --v1schema public

DROP database IF EXISTS agidb;

DO
$body$
BEGIN
   IF NOT EXISTS (
      SELECT *
      FROM   pg_catalog.pg_user
      WHERE  usename = 'agiu') THEN
         CREATE ROLE agiu LOGIN PASSWORD 'password';
   END IF;
END
$body$;


CREATE database agidb OWNER agiu;

\connect agidb;


-- key-value store
CREATE TABLE properties(
   id SERIAL PRIMARY KEY NOT NULL,
   key text NOT NULL UNIQUE,
   value text NOT NULL
);

CREATE TABLE nodes(
   id SERIAL PRIMARY KEY NOT NULL,
   key text,
   host text,
   port integer
);

CREATE TABLE entities(
   id SERIAL PRIMARY KEY NOT NULL,
   type text,
   node text,
   parent text,
   key text NOT NULL UNIQUE
);

CREATE TABLE data(
   id SERIAL PRIMARY KEY NOT NULL,
--   entity text,
   key text NOT NULL UNIQUE,
   ref_key text,
   sizes text,
   elements text
);

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO agiu;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO agiu;
