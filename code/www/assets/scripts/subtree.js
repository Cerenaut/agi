
var Subtree = {

  entityAgeOld : null,
  entityAge : null,

  updateCallback : function( json ) {
    var result = json.status + ": " + json.responseText;
    var jsonResponse = JSON.parse( json.responseText );

    $( "#update-result" ).html( "<b>"+json.status+"</b><br><pre style='border-radius:0;border:none;'>" + JSON.stringify( jsonResponse, null, 2 ) + "</pre>" );

    var entityName = $( "#entity" ).val();
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.getEntity( entityName, Subtree.onGetEntity );
  },

  onGetEntityAndUpdate : function( json ) {
    Subtree.onGetEntity( json );

    console.log( "OLD age: " + Subtree.entityAgeOld + " NEW age: " + Subtree.entityAge );
    if( Subtree.entityAgeOld == null ) {
      Subtree.entityAgeOld = Subtree.entityAge;
      Subtree.update();
    }
    else if( Subtree.entityAgeOld != Subtree.entityAge ) {
      Subtree.entityAgeOld = Subtree.entityAge;
      Subtree.update();
    }
    else {
      //console.log( "Not updating, same age as before." );
    }

  },

  onGetEntity : function( json ) {
    if( json.length == 0 ) {
      Subtree.entityAge = null;
      return;
    }

    var status = "Code " + json.status;
    if( json.status != 200 ) {
      Subtree.entityAge = null;
      return;
    }

    var entities = JSON.parse( json.responseText );
    var entity = entities[ 0 ];

    $( "#entity-type" ).html( entity.type );
    $( "#entity-age" ).html( entity.config.age );
    $( "#entity-seed" ).html( entity.config.seed );

    $( "#config-result" ).html( "<pre style='border-radius:0;border:none;'>" + JSON.stringify( entity.config, null, 2 ) + "</pre>" );
    
    Subtree.entityAge = entity.config.age;
  },

  onInterval : function() {

    // check if entity is ready to update yet.
    var entityName = $( "#entity" ).val();
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.getEntity( entityName, Subtree.onGetEntityAndUpdate );    
  },

  exportEntity : function() {
    var entityName = $( "#entity" ).val();
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.exportEntity( entityName, Framework.exportTypeEntity );
  },

  exportData : function() {
    var entityName = $( "#entity" ).val();
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.exportEntity( entityName, Framework.exportTypeData );
  },

  exportDataRefs : function() {
    var entityName = $( "#entity" ).val();
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.exportEntity( entityName, Framework.exportTypeDataRefs );
  },

  importData : function() {
    console.log( "Importing data... " );
    $("#entity-file").attr( "name", "" );
    $("#data-file").attr( "name", "data-file" );

    Subtree.importFormSubmit();
  },

  importEntity : function() {
    console.log( "Importing entities... " );
    $("#entity-file").attr( "name", "entity-file" );
    $("#data-file").attr( "name", "" );

    Subtree.importFormSubmit();
  },

  importEntityAndData : function() {
    console.log( "Importing entities and data... " );
    $("#entity-file").attr( "name", "entity-file" );
    $("#data-file").attr( "name", "data-file" );

    Subtree.importFormSubmit();
  },

  importFormSubmit : function() {
    var entityName = $( "#entity" ).val();
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );

    var importAction = Framework.getImportUrl( entityName );

    $( "#import-form" )[ 0 ].action = importAction;
    $( "#import-form" )[ 0 ].submit( function( event ) {
      console.log( "Importing/submitting... " );

      event.preventDefault();
      event.stopPropagation();
      return false;
    } );
  },

  setFlushAndReset : function() {
    var entityName = $( "#entity" ).val();
    var flushValue = $( "#flush" ).is( ":checked" );
    var resetValue = $( "#reset" ).is( ":checked" );

    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.setConfigPath( entityName, "flush", flushValue, Subtree.onSetFlushAndReset ); 
    Framework.setConfigPath( entityName, "reset", resetValue, Subtree.onSetFlushAndReset ); 
  },

  onSetFlushAndReset : function( response ) {
    console.log( "Response from POST config: " + JSON.stringify( response ) );
  },

  version : function() {
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.version( Subtree.onGetVersion )
  },

  onGetVersion : function( response ) {
    if( response.length == 0 ) {
        return;
    }

    if  ( response.status != 200 ) {
      return;
    }

    var versionObject = JSON.parse( response.responseText );
    var version = versionObject.version;
    $( "#version-label" ).html( version )
  },

  update : function() {

    var entity = $( "#entity" ).val();
    console.log( "Updating " + entity );

    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.update( entity, Subtree.updateCallback );
  },

  onParameter : function( key, value ) {
    if( key == "entity" ) {
      $("#entity").val( value ); 
    }
  },

  setup : function() {
    Framework.setNodeHost(); // in case override by param
    Parameters.extract( Subtree.onParameter );
    Loop.setup( Subtree.onInterval );
  }

};

$( document ).ready( function() {
  Subtree.setup();
} );


