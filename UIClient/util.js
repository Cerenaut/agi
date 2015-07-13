var Http = {

  postJson : function( url, onSuccess, onFailure ) {
    Http.request( "POST", url, onSuccess, onFailure );
  },

  post : function( url, onSuccess, onFailure ) {
    Http.request( "POST", url, onSuccess, onFailure );
  },

  getJson : function( url, onSuccess, onFailure ) {
    Http.request( "GET", url, onSuccess, onFailure );
  },

  get : function( url, onSuccess, onFailure ) {
    Http.request( "GET", url, onSuccess, onFailure );
  },

  requestJson : function( method, url, onSuccess, onFailure ) {
    Http.request( 
      method, 
      url, 
      function( response ) {
        var o = JSON.parse( response ); // parse into object
        if( onSuccess ) {
          onSuccess( o );
        }
      }, 
      function( response ) {
        var o = JSON.parse( response ); // parse into object
        if( onSuccess ) {
          onFailure( o );
        }
      } );
  },

  request : function( method, url, onSuccess, onFailure ) {
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
      if( xhr.readyState == 4 ) { // finished
        var response = xhr.responseText;
        
        if( xhr.status == 200 ) { // success
          if( onSuccess ) {
            onSuccess( response );
            return;
          }
        }
        else { // not 200, fail
          if( onFailure ) {
            onFailure( response );
            return;
          }
        }

      }
    };

    xhr.open( method, url, true );
    xhr.send();
  }

};
