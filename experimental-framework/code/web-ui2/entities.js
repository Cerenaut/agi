
var Entities = {

  protocol : "http",
  host : "localhost",
  port : "8491",

  doAjax : function( suffix, callback, verb, idResult ) {
    // example http://localhost:8080/coordinator/control/entity/exp1/command/bob
    var url = Entities.protocol + "://" + Entities.host + ":" + Entities.port + "/" + suffix;
    // console.log( "POST: "+ s );
    // TODO add error handling, success callbacks
    $.ajax( url, {
        type: verb,
        complete: function( json ) {
          callback( json, idResult );
        },
        error: function( json ) {
          callback( json, idResult );
        }
    } );
  },

  configure : function() {
    Entities.host = $( "#host" ).val();
    Entities.port = $( "#port" ).val();
  },

  updateCallback : function( json, idResult ) {
    //var s = JSON.stringify( json );
    //console.log( "Coordinator response to command: " + s );

    var status = "Code "+json.status;
    if( json.status == 200 ) {
      status = "OK";
    }

    var result = "Status: "+status+"<br>Message: \""+json.responseText + "\"";

    $( "#result" ).html( result );
  },

  update : function() {
    // localhost:8080/update?entity=mySwitch&event=update
    var entity = $( "#entity" ).val();
    var suffix = "update" 
               + "?entity=" + entity 
               + "&event=" + "update";
    var verb = "POST";
    Entities.doAjax( suffix, Entities.updateCallback, verb, "result" );
  },

  onParameter : function( key, value ) {
    if( key == "entity" ) {
      $("#entity").val( value ); 
    }
  },

  setup : function() {
    Parameters.extract( Entities.onParameter );
    Loop.setup( Entities.update );
  }

};

$( document ).ready( function() {
  Entities.setup();
} );


