export JAVA_HOME=/home/dave/workspace/agi.io/java/jdk1.8.0_60
export PATH=${JAVA_HOME}/bin:${PATH}

java  -cp "/home/dave/workspace/agi.io/agi/algorithms/code/core/out/artifacts/core_jar/org.json-2.0.jar:/home/dave/workspace/agi.io/agi/algorithms/code/core/out/artifacts/core_jar/postgresql-9.2-1002.jdbc4.jar:/home/dave/workspace/agi.io/agi/algorithms/code/core/out/artifacts/core_jar/core.jar" io.agi.ef.demo.LightDemo node.properties entities.json refs.json
