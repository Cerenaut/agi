var Demo = {

  onStep : function( json ) {
       var e = document.getElementById( "stepTarget" );
       // pick out property of object
       var kind = json.message;
       var s = JSON.stringify( kind );// back to string
       e.innerHTML = "Message: " + s;
  },

  onClick : function() {
    var url = "http://localhost:9999/control/step";
    Http.getJson( url, Demo.onStep );
  }
};
