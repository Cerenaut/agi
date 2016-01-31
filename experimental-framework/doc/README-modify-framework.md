This README goes into more information about modifying the framework API.

## Before you start

You'll need to have Swagger installed and setup. There is plenty of information on the [Swagger website](https://github.com/swagger-api/swagger-codegen). In summary you'll need to: 
* grab a copy of it (clone or download), and
* build it (mvn package)

AGIEF uses Version 2.1.5 (the latest stable release at the time of writing)

In order to work with AGIEF, you need to use the modified Mustache template files, located at ( /resources/swagger-mustache )

There are a bunch of scripts in ( /bin/codegen )
They do the following:
* use swagger-code-gen to build generate server code in the /lib folder
* install lib into local maven repository m2 (mvn install)

These libraries are included in the AGIEF project as dependencies in their respective POM files.




## Making changes to one of the API specs

- Open the relevant /api-spec/spec.yaml file in a text editor
- Copy contents into the left pane in a browser at editor.swagger.io
- Have a look at the generated documentation. 
- Make changes as you wish
- Try it out by selecting 'Try Out Operation'
- If it doesn't work, just copy the JSON and URL into 'Simple REST client' chrome extension (or other favourite extenstion) or use Curl on the command line
- Once happy, just copy the contents into the open **spec.yaml** in your text editor
- run the appropriate codegen script ( /bin/db ) and keep developing 

NOTE: for this you must follow instructions in README for generating code, it requires Swagger to be installed and it requires the custom Mustach files have been copied to the Swagger folders. You can use **copyMustache.sh** script.


