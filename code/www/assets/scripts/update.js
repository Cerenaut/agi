
var Update = {

  entityAgeOld : null,
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

  onGetEntityAgeAndUpdate : function( json ) {
    Update.onGetEntityAge( json );

    console.log( "OLD age: " + Update.entityAgeOld + " NEW age: " + Update.entityAge );
    if( Update.entityAgeOld == null ) {
      Update.entityAgeOld = Update.entityAge;
      Update.update();
    }
    else if( Update.entityAgeOld != Update.entityAge ) {
      Update.entityAgeOld = Update.entityAge;
      Update.update();
    }
    else {
      //console.log( "Not updating, same age as before." );
    }

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

    Update.entityAge = age;
  },

  onInterval : function() {

    // check if entity is ready to update yet.
    var entityName = $( "#entity" ).val();
    Framework.getConfig( entityName, Update.onGetEntityAgeAndUpdate );    
  },

  update : function() {

    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );

    // localhost:8080/update?entity=mySwitch&event=update
    var entity = $( "#entity" ).val();
    console.log( "Updating " + entity );
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


