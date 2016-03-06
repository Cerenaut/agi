
var Postgrest = {

  protocol : "http",
  host : "localhost",
  port : "8492",

  // this is an asynchronous function, the default
  getJson : function( table, callback ) {
    var url = Postgrest.protocol + "://" + Postgrest.host + ":" + Postgrest.port + "/" + table;
 
    $.ajax( url, {
        type: "GET",
        success: function( json ) {
          callback( json );
        }
      } );
  },

  postJson : function( table, json, callback ) {
    Postgrest.setJson( table, json, callback, "POST" );
  },

  patchJson : function( table, json, callback ) {
    Postgrest.setJson( table, json, callback, "PATCH" );
  },

  deleteJson : function( table, json, callback ) {
    Postgrest.setJson( table, json, callback, "DELETE" );
  },

  setJson : function( table, json, callback, verb ) {
    var url = Postgrest.protocol + "://" + Postgrest.host + ":" + Postgrest.port + "/" + table;
    var s = JSON.stringify( json );
    // console.log( "POST: "+ s );
    // TODO add error handling, success callbacks
    $.ajax( url, {
        type: verb,
        data: s,
        complete: function() {
          callback();
        }
    } );
  },

  setup : function( host, port ) {
    if( host ) Postgrest.host = host;
    if( port ) Postgrest.port = port;
  }

};


