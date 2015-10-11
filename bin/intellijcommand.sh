$JAVA_HOME/bin/java -Dfile.encoding=UTF-8  \
-cp \
/Users/gideon/.m2/repository/com/sun/jersey/jersey-core/1.18/jersey-core-1.18.jar:\
$AGI_HOME/src/experimental-framework/core-modules/target/io-agi-agief-core-modules-1.1.0-jar-with-dependencies.jar \
io.agi.ef.http.node.NodeMain localhost 8080 NodeName 8081 server.properties COORDINATOR