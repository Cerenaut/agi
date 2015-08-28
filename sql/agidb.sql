-- Creates a database for AGI.IO distributed systems
-- Example usage:
--   sudo -u postgres psql -f agidb.sql
-- This executes the script using psql as the postgres user. 
-- You need sudo to change to the postgres user without a password.
-- You only need to run this once.

-- Note: 
-- After running this, run postgREST HTTP service like so:
-- ./postgrest-0.2.10.0 --db-host localhost  --db-port 5432 --db-name agidb  --db-user agiu --db-pass password --db-pool 200  --anonymous agiu --port 3000 --v1schema public

DROP database agidb;

CREATE USER agiu WITH PASSWORD 'password';
CREATE database agidb OWNER agiu;

\connect agidb;

CREATE TABLE nodes(
   id SERIAL PRIMARY KEY NOT NULL,
   name text,
   host text,
   port integer
);

CREATE TABLE coordinator(
   id SERIAL PRIMARY KEY NOT NULL,
   id_node integer REFERENCES nodes( id ) ON DELETE CASCADE
);

CREATE TABLE properties(
   id SERIAL PRIMARY KEY NOT NULL,
   name text NOT NULL UNIQUE,
   value text NOT NULL
);

CREATE TABLE entities(
   id_entity SERIAL UNIQUE NOT NULL
);

CREATE TABLE data(
   id SERIAL PRIMARY KEY NOT NULL,
   id_entity integer REFERENCES entities( id_entity ) ON DELETE CASCADE
   name text NOT NULL UNIQUE,
   sizes text NOT NULL,
   values text NOT NULL,
);

CREATE TABLE experiments(
   id SERIAL PRIMARY KEY NOT NULL,
   name text NOT NULL UNIQUE
) INHERITS ( entities );

CREATE TABLE agents(
   id SERIAL PRIMARY KEY NOT NULL,
   name text NOT NULL UNIQUE,
   id_experiment integer REFERENCES experiments( id ) ON DELETE CASCADE
) INHERITS ( entities );

CREATE TABLE worlds(
   id SERIAL PRIMARY KEY NOT NULL,
   name text NOT NULL UNIQUE,
   id_experiment integer REFERENCES experiments( id ) ON DELETE CASCADE
) INHERITS ( entities );

CREATE TABLE sensors(
   id SERIAL PRIMARY KEY NOT NULL,
   name text NOT NULL UNIQUE,
   id_agent integer REFERENCES agents( id ),
   id_world integer REFERENCES worlds( id )
) INHERITS ( entities );

CREATE TABLE motors(
   id SERIAL PRIMARY KEY NOT NULL,
   name text NOT NULL UNIQUE,
   id_agent integer REFERENCES agents( id ),
   id_world integer REFERENCES worlds( id )
) INHERITS ( entities );

CREATE TABLE thread(
   id SERIAL PRIMARY KEY NOT NULL,
   name text NOT NULL UNIQUE,
   id_agent integer REFERENCES agents( id ),
   id_world integer REFERENCES worlds( id )
) INHERITS ( entities );


GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO agiu;

