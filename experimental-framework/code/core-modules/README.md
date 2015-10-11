* modify the mustache file
* build the swagger code gen (mvn package)
* use swagger-code-gen to build generate server code in the /lib folder (/bin/genServer.sh), referred to as server-lib
* install server-lib into local maven repository m2 (mvn install)
* include server-lib package in CoordinatorServer project (via pom.xml file)


