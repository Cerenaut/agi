
var Experiment = {

  entityAgeOld : null,
  entityAge : null,

  updateCallback : function( json ) {
    var result = json.status + ": " + json.responseText;
    var jsonResponse = JSON.parse( json.responseText );

    $( "#update-result" ).html( "<b>"+json.status+"</b><br><pre style='border-radius:0;border:none;'>" + JSON.stringify( jsonResponse, null, 2 ) + "</pre>" );

    var entityName = $( "#entity" ).val();
    Framework.getEntity( entityName, Experiment.onGetEntity );
  },

  onGetEntityAndUpdate : function( json ) {
    Experiment.onGetEntity( json );

    console.log( "OLD age: " + Experiment.entityAgeOld + " NEW age: " + Experiment.entityAge );
    if( Experiment.entityAgeOld == null ) {
      Experiment.entityAgeOld = Experiment.entityAge;
      Experiment.update();
    }
    else if( Experiment.entityAgeOld != Experiment.entityAge ) {
      Experiment.entityAgeOld = Experiment.entityAge;
      Experiment.update();
    }
    else {
      //console.log( "Not updating, same age as before." );
    }

  },

  onGetEntity : function( json ) {
    if( json.length == 0 ) {
      Experiment.entityAge = null;
      return;
    }

    var status = "Code " + json.status;
    if( json.status != 200 ) {
      Experiment.entityAge = null;
      return;
    }

    var entities = JSON.parse( json.responseText );
    var entity = entities[ 0 ];

    $( "#entity-type" ).html( entity.type );
    $( "#entity-age" ).html( entity.config.age );
    $( "#entity-seed" ).html( entity.config.seed );

    $( "#config-result" ).html( "<pre style='border-radius:0;border:none;'>" + JSON.stringify( entity.config, null, 2 ) + "</pre>" );
    
    Experiment.entityAge = entity.config.age;
  },

  onInterval : function() {

    // check if entity is ready to update yet.
    var entityName = $( "#entity" ).val();
    Framework.getEntity( entityName, Experiment.onGetEntityAndUpdate );    
  },

  exportEntity : function() {
    var entityName = $( "#entity" ).val();
    Framework.exportEntity( entityName, Framework.exportTypeEntity );
  },

  exportData : function() {
    var entityName = $( "#entity" ).val();
    Framework.exportEntity( entityName, Framework.exportTypeData );
  },

  importEntity : function() {
    var entityName = $( "#entity" ).val();
    var importAction = Framework.getImportUrl( entityName );

    $( "#import-form" )[ 0 ].action = importAction;
    $( "#import-form" )[ 0 ].submit( function( event ) {
      event.preventDefault();
      event.stopPropagation();
      return false;
    } );
  },

  setFlushAndReset : function() {
    var entityName = $( "#entity" ).val();
    var flushValue = $( "#flush" ).is( ":checked" );
    var resetValue = $( "#reset" ).is( ":checked" );

    Framework.setConfigPath( entityName, "flush", flushValue, Experiment.onSetFlushAndReset ); 
    Framework.setConfigPath( entityName, "reset", resetValue, Experiment.onSetFlushAndReset ); 
  },

  onSetFlushAndReset : function( response ) {
    console.log( "Response from POST config: " + JSON.stringify( response ) );
  },

  update : function() {

    var entity = $( "#entity" ).val();
    console.log( "Updating " + entity );

    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.update( entity, Experiment.updateCallback );
  },

  onParameter : function( key, value ) {
    if( key == "entity" ) {
      $("#entity").val( value ); 
    }
  },

  setup : function() {

    Parameters.extract( Experiment.onParameter );
    Loop.setup( Experiment.onInterval );
  }

};

$( document ).ready( function() {
  Experiment.setup();
} );


