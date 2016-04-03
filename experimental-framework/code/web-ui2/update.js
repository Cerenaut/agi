
var Update = {

  entityAge : null,

  updateCallback : function( json ) {
    //var s = JSON.stringify( json );
    //console.log( "Coordinator response to command: " + s );

    var status = "Code " + json.status;
//    if( json.status == 200 ) {
//      status = "OK";
//    }

    var result = status+":<br>" +json.responseText;

    $( "#result" ).html( result );

    var entityName = $( "#entity" ).val();
    Framework.getConfig( entityName, Update.onGetEntityAge );
  },

  onGetEntityAge : function( json ) {
    if( json.length == 0 ) {
      Update.entityAge = null;
      return;
    }

    var status = "Code " + json.status;
    if( json.status != 200 ) {
      Update.entityAge = null;
      return;
    }

    var entityConfig = JSON.parse( json.responseText );
    var config = entityConfig.value;
    var age = config.age;

    $( "#age" ).html( age );
    //console.log( "OLD age: " + Update.entityAge + " NEW age: " + age );

    if( Update.entityAge == null ) {
      Update.entityAge = age;
      Update.update();
    }
    else if( Update.entityAge != age ) {
      Update.entityAge = age;
      Update.update();
    }
    else {
      //console.log( "Not updating, same age as before." );
    }
  },

  onInterval : function() {

    // check if entity is ready to update yet.
    var entityName = $( "#entity" ).val();
    Framework.getConfig( entityName, Update.onGetEntityAge );
  },

  update : function() {

    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );

    // localhost:8080/update?entity=mySwitch&event=update
    var entity = $( "#entity" ).val();
    Framework.update( entity, Update.updateCallback );
/*    var suffix = "update" 
               + "?entity=" + entity 
               + "&event=" + "update";
    var verb = "POST";
    Update.doAjax( suffix, Update.updateCallback, verb, "result" );*/
  },

  onParameter : function( key, value ) {
    if( key == "entity" ) {
      $("#entity").val( value ); 
    }
  },

  setup : function() {
    Parameters.extract( Update.onParameter );
    Loop.setup( Update.onInterval );
  }

};

$( document ).ready( function() {
  Update.setup();
} );


