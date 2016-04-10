
var Entities = {

  protocol : "http",
  host : "localhost",
  port : "8491",

  entityAge : null,

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

  onGetEntityAge : function( response ) {
    if( response.length == 0 ) {
      Entities.entityAge = null;
      return;
    }

    var property = response[ 0 ];
    var age = property.value;

    $( "#age" ).html( "Age: " + age );
    //console.log( "OLD age: " + Entities.entityAge + " NEW age: " + age );

    if( Entities.entityAge == null ) {
      Entities.entityAge = age;
      Entities.update();
    }
    else if( Entities.entityAge != age ) {
      Entities.entityAge = age;
      Entities.update();
    }
    else {
      //console.log( "Not updating, same age as before." );
    }
  },

  doGetEntityAge : function( entityName, callback ) {
    // find the age of the entity
    Postgrest.getJson( "properties?key=eq."+entityName+"-age", callback );
  },

  onInterval : function() {

    // check if entity is ready to update yet.
    var key = $( "#entity" ).val();
    Entities.doGetEntityAge( key, Entities.onGetEntityAge );
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
    Loop.setup( Entities.onInterval );
  }

};

$( document ).ready( function() {
  Entities.setup();
} );


