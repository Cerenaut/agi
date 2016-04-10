
// Demo URL: file:///home/dave/workspace/agi.io/agi/experimental-framework/code/web-ui2/vector-bar.html?data=myLight-random-output&data=mySwitch-light-output&data=myLight-light-output&interval=303
var Parameters = {

  extract : function( parameterCallback ) {

    // http://stackoverflow.com/questions/6944744/javascript-get-portion-of-url-path
    var params = window.location.search.split( "&" );
    //console.log( "params = " + params );

    // remove ? prefix, if present
    if( params.length > 0 ) {
      if( params[ 0 ].indexOf( "?" ) == 0 ) {
        params[ 0 ] = params[ 0 ].slice( 1 ); 
      }

      for( var i = 0; i < params.length; ++i ) {
        var param = params[ i ];
        var index = param.indexOf( "=" );
        var key = param.slice( 0, index );
        var value = param.slice( index+1 );
        console.log( "Parameter: "+key+","+value );
        parameterCallback( key, value );
      }
    }

  }

};

