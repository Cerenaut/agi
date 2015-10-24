
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