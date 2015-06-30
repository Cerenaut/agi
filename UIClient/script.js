var Demo = {

  onResponse : function( response ) {
       var e = document.getElementById( "target" );
       var o = JSON.parse( response ); // parse into object
       // pick out property of object
       var kind = o.kind;
       var s = JSON.stringify( kind );// back to string
       e.innerHTML = "Kind: " + s;
  },

  onClick : function() {
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function() {
      if( xmlhttp.readyState==4 && xmlhttp.status==200 ) {
         console.log( "ready, successful" );
         Demo.onResponse( xmlhttp.responseText );
      }
    };

    var url = "http://www.reddit.com/r/pics.json";
    xmlhttp.open( "GET", url, true );
    xmlhttp.send();
  }
};