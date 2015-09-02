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


-- key-value store
CREATE TABLE properties(
   id SERIAL PRIMARY KEY NOT NULL,
   name text NOT NULL UNIQUE,
   value text NOT NULL
);

-- administration of distributed system
CREATE TABLE roles(
   id SERIAL PRIMARY KEY NOT NULL,
   name text NOT NULL UNIQUE
);

CREATE TABLE nodes(
   id SERIAL PRIMARY KEY NOT NULL,
   name text,
   host text,
   port integer
);

CREATE TABLE node_roles(
   id SERIAL PRIMARY KEY NOT NULL,
   id_node integer NOT NULL REFERENCES nodes( id ) ON DELETE CASCADE,
   id_role integer NOT NULL REFERENCES roles( id ) ON DELETE CASCADE
);

-- allow expandable list of entities with common structure.
CREATE TABLE entity_types(
   id SERIAL PRIMARY KEY NOT NULL,
   name text NOT NULL UNIQUE
);

-- entities have a treelike structure of ownership, that allows us to easily manage lifetime of dependent objects.
CREATE TABLE entities(
   id SERIAL PRIMARY KEY NOT NULL,
   id_entity_type integer NOT NULL REFERENCES entity_types( id ) ON DELETE CASCADE,
   id_entity_parent integer REFERENCES entities( id ) ON DELETE CASCADE,
   name text NOT NULL UNIQUE
);

-- build arbitrary graphs of relationships between entities
CREATE TABLE entity_relation_types(
   id SERIAL PRIMARY KEY NOT NULL,
   name text NOT NULL UNIQUE
);

CREATE TABLE entity_relations(
   id SERIAL PRIMARY KEY NOT NULL,
   id_entity_relation_type integer NOT NULL REFERENCES entity_relation_types( id ) ON DELETE CASCADE,
   id_entity_1 integer NOT NULL REFERENCES entities( id ) ON DELETE CASCADE,
   id_entity_2 integer NOT NULL REFERENCES entities( id ) ON DELETE CASCADE,
   name text NOT NULL UNIQUE
);

CREATE TABLE data(
   id SERIAL PRIMARY KEY NOT NULL,
   id_entity integer NOT NULL REFERENCES entities( id ) ON DELETE CASCADE,
   name text NOT NULL UNIQUE,
   size text NOT NULL,
   elements text NOT NULL
);

-- Create views as joins
CREATE VIEW entities_entities AS 
SELECT 
  e1.id as id_1, 
  e1.id_entity_type as id_entity_type_1, 
  e1.id_entity_parent as id_entity_parent_1,  
  e1.name as name_1,
  er.id as id_entity_relation, 
  ert.id as id_entity_relation_type, 
  ert.name as name_entity_relation_type, 
  e2.id as id_2, 
  e2.id_entity_type as id_entity_type_2, 
  e2.id_entity_parent as id_entity_parent_2,  
  e2.name as name_2
FROM entities e1 
INNER JOIN entity_relations er ON er.id_entity_1 = e1.id 
INNER JOIN entity_relation_types ert ON er.id_entity_relation_type = ert.id 
INNER JOIN entities e2 ON er.id_entity_2 = e2.id;

CREATE VIEW parents_entities AS 
SELECT 
  e1.id as id_1, 
  e1.id_entity_type as id_entity_type_1, 
  e1.id_entity_parent as id_entity_parent_1,  
  e1.name as name_1,
  e2.id as id_2, 
  e2.id_entity_type as id_entity_type_2, 
  e2.id_entity_parent as id_entity_parent_2,  
  e2.name as name_2
FROM entities e1 
INNER JOIN entities e2 ON e1.id_entity_parent = e2.id;

CREATE VIEW entities_types AS 
SELECT 
  et.id as id_entity_type,
  et.name as name_entity_type,
  e.id as id,
  e.name as name,
  e.id_entity_parent as id_entity_parent
FROM entities e 
INNER JOIN entity_types et ON e.id_entity_type = et.id;

-- query /data directly for specific items including all fields, such as data.
-- for meta-data, use this view instead:
CREATE VIEW entities_data AS 
SELECT 
  d.id as id_data,
  d.name as name_data,
  d.size as size_data,
  --d.elements as name_elements,
  e.id as id,
  e.name as name,
  e.id_entity_parent as id_entity_parent
FROM entities e 
INNER JOIN data d ON d.id_entity = e.id;

CREATE VIEW nodes_roles AS 
SELECT 
  r.id as id_role,
  r.name as name_role,
  nr.id as id_node_role,
  n.id as id,
  n.name as name,
  n.host as host,
  n.port as port
FROM nodes n 
INNER JOIN node_roles nr ON nr.id_node = n.id
INNER JOIN roles r ON nr.id_role = r.id;

-- Pre-populate database with key value:
INSERT INTO roles ( name ) VALUES ( 'coordinator' );

INSERT INTO entity_types ( name ) VALUES ( 'experiment' );
INSERT INTO entity_types ( name ) VALUES ( 'agent' );
INSERT INTO entity_types ( name ) VALUES ( 'world' );
INSERT INTO entity_types ( name ) VALUES ( 'sensor' );
INSERT INTO entity_types ( name ) VALUES ( 'motor' );
INSERT INTO entity_types ( name ) VALUES ( 'thread' );

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO agiu;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO agiu;
