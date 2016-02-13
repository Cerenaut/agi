This README goes into more information about modifying the framework API.

## Before you start

You'll need to have Swagger installed and setup. There is plenty of information on the [Swagger website](https://github.com/swagger-api/swagger-codegen). 

AGIEF uses Version 2.1.5 (the latest stable release at the time of writing).

For AGIEF, you'll need to: 
* grab a copy of it (clone or download), and
* make sure you set the path to it in **variables.sh**
* run /**bin/codegen/setup.sh**

Note that in order to work with AGIEF, you need to use the modified Mustache template files, located at ( /**resources/swagger-mustache** ). These are copied to the appropriate folder in **swagger-codegen** when you run **setup.sh**

There are a bunch of scripts in ( /**bin/codegen** ) for using Swagger to generate the network layers as libraries. They do the following:
* use **swagger-codegen** to generate network libs
* build those libs
* install them to the local maven repository m2 (**mvn install**)

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



GOTCHA: You must copy the modified Mustache files as described above, BEFORE you build the swagger-codger project. If you make any further changes to any Mustache files, you must re-compile the project before the changes take effect.





