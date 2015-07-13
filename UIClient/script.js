var Demo = {

  onStep : function( json ) {
       var e = document.getElementById( "stepTarget" );
       //var o = JSON.parse( response ); // parse into object
       // pick out property of object
       var kind = json.kind;
       var s = JSON.stringify( kind );// back to string
       e.innerHTML = "Kind: " + s;
  },

  onClick : function() {
    var url = "http://localhost:9999/control/step";
    Http.getJson( url, Demo.onStep );

/*    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function() {
      if( xmlhttp.readyState==4 && xmlhttp.status==200 ) {
         console.log( "ready, successful" );
         Demo.onResponse( xmlhttp.responseText );
      }
    };

    var url = "http://localhost:9999/control/step";
    xmlhttp.open( "GET", url, true );
    xmlhttp.send();*/
  }
};
