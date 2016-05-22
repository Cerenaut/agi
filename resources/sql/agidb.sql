-- Creates a database for AGI.IO distributed systems
-- Example usage:
--   sudo -u postgres psql -f agidb.sql
-- This executes the script using psql as the postgres user. 
-- You need sudo to change to the postgres user without a password.
-- You only need to run this once.


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


-- Create tables
-- NB Text is unlimited length, but most efficient: http://www.postgresql.org/docs/9.1/static/datatype-character.html
CREATE TABLE nodes(
   id SERIAL PRIMARY KEY NOT NULL,
   name text,
   host text,
   port integer
);

CREATE TABLE entities(
   id SERIAL PRIMARY KEY NOT NULL,
   type text,
   node text,
   parent text,
   config text,
   name text NOT NULL UNIQUE
);

CREATE TABLE data(
   id SERIAL PRIMARY KEY NOT NULL,
   name text NOT NULL UNIQUE,
   ref_name text,
   sizes text,
   elements text
);

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO agiu;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO agiu;
