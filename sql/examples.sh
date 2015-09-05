
#
#1. Entity Types
#http://localhost:3000/entity_types?
#[{"id":1,"name":"experiment"},
# {"id":2,"name":"agent"},
# {"id":3,"name":"world"},
# {"id":4,"name":"sensor"},
# {"id":5,"name":"motor"},
# {"id":6,"name":"thread"} ]
#
# Create Experiment
#
curl -H "Content-Type: application/json" -X POST -d '{"name":"myExperiment","id_entity_type":1,"id_entity_parent":null}' http://localhost:$1/entities
#
# Add data to experiment:
#
curl -H "Content-Type: application/json" -X POST -d '{"name":"vec1","id_entity":1,"size":"{ x:10 }","elements":"1,2,3,4,5,1,2,3,4,0" }' http://localhost:$1/data
#
# Add a World to the Experiment
#
curl -H "Content-Type: application/json" -X POST -d '{"name":"myWorld","id_entity_type":3,"id_entity_parent":1}' http://localhost:$1/entities
#
# Add a Sensor to the World
#
curl -H "Content-Type: application/json" -X POST -d '{"name":"mySensor","id_entity_type":4,"id_entity_parent":2}' http://localhost:$1/entities
#
# Add a node.
#
curl -H "Content-Type: application/json" -X POST -d '{"name":"myNode","host":"127.0.0.1","port":3001}' http://localhost:$1/nodes
#
# Make it a coordinator:
#
curl -H "Content-Type: application/json" -X POST -d '{"id_node":1,"id_role":1}' http://localhost:$1/node_roles
#
# Add another node
#
curl -H "Content-Type: application/json" -X POST -d '{"name":"myNode2","host":"127.0.0.1","port":3002}' http://localhost:$1/nodes
#
# Add property
#
curl -H "Content-Type: application/json" -X POST -d '{"name":"hello","value":"world"}' http://localhost:$1/properties
#
# Add data to sensor:
#
curl -H "Content-Type: application/json" -X POST -d '{"name":"vec2","id_entity":3,"size":"{ x:10 }","elements":"1,2,3,4,5,1,2,3,4,0" }' http://localhost:$1/data
#
# Add an Agent to the World
#
curl -H "Content-Type: application/json" -X POST -d '{"name":"myAgent","id_entity_type":2,"id_entity_parent":1}' http://localhost:$1/entities
#
# Add a thread to the Agent
#
curl -H "Content-Type: application/json" -X POST -d '{"name":"processorThread","id_entity_type":6,"id_entity_parent":4}' http://localhost:$1/entities
#
# Make agent processor update on sensor data change
#
curl -H "Content-Type: application/json" -X POST -d '{"id_entity":5,"id_data":2,"id_data_relation_type":1}' http://localhost:$1/entity_data_relations
#
# Make agent update on experiment step
#
curl -H "Content-Type: application/json" -X POST -d '{"id_entity_1":1,"id_entity_2":4,"id_entity_relation_type":2}' http://localhost:$1/entity_relations

