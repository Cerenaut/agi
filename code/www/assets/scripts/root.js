
var Root = {

  entityAgeOld : null,
  entityAge : null,

  updateCallback : function( json ) {
    var result = json.status + ": " + json.responseText;
    var jsonResponse = JSON.parse( json.responseText );

    $( "#update-result" ).html( "<b>"+json.status+"</b><br><pre style='border-radius:0;border:none;'>" + JSON.stringify( jsonResponse, null, 2 ) + "</pre>" );

    var entityName = $( "#entity" ).val();
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.getEntity( entityName, Root.onGetEntity );
  },

  onGetEntityAndUpdate : function( json ) {
    Root.onGetEntity( json );

    console.log( "OLD age: " + Root.entityAgeOld + " NEW age: " + Root.entityAge );
    if( Root.entityAgeOld == null ) {
      Root.entityAgeOld = Root.entityAge;
      Root.update();
    }
    else if( Root.entityAgeOld != Root.entityAge ) {
      Root.entityAgeOld = Root.entityAge;
      Root.update();
    }
    else {
      //console.log( "Not updating, same age as before." );
    }

  },

  onGetEntity : function( json ) {
    if( json.length == 0 ) {
      Root.entityAge = null;
      return;
    }

    var status = "Code " + json.status;
    if( json.status != 200 ) {
      Root.entityAge = null;
      return;
    }

    var entities = JSON.parse( json.responseText );
    var entity = entities[ 0 ];

    $( "#entity-type" ).val( entity.type );
    $( "#entity-age" ).val( entity.config.age );
    $( "#entity-seed" ).val( entity.config.seed );

    $( "#flush").prop('checked', entity.config.flush );
    $( "#reset").prop('checked', entity.config.reset );
    $( "#learn").prop('checked', entity.config.learn );
    $( "#config-result" ).html( "<pre style='border-radius:0;border:none;'>" + JSON.stringify( entity.config, null, 2 ) + "</pre>" );
    
    Root.entityAge = entity.config.age;
  },

  onInterval : function() {
    // check if entity is ready to update yet.
    var entityName = $( "#entity" ).val();
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.getEntity( entityName, Root.onGetEntityAndUpdate );    
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

  importEntity : function() {
    var entityName = $( "#entity" ).val();
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    var importAction = Framework.getImportUrl( entityName );

    $( "#import-form" )[ 0 ].action = importAction;
    $( "#import-form" )[ 0 ].submit( function( event ) {
      event.preventDefault();
      event.stopPropagation();
      return false;
    } );
  },

  getEntityConfig : function() {
    var entityName = $( "#entity" ).val();
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.getEntity( entityName, Root.onGetEntity );    
  },

  setEntityConfig : function() {
    var entityName = $( "#entity" ).val();
    var flushValue = $( "#flush" ).is( ":checked" );
    var resetValue = $( "#reset" ).is( ":checked" );
    var learnValue = $( "#learn" ).is( ":checked" );

    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.setConfigPath( entityName, "flush", flushValue, Root.onSetEntityConfig ); 
    Framework.setConfigPath( entityName, "reset", resetValue, Root.onSetEntityConfig ); 
    Framework.setConfigPath( entityName, "learn", learnValue, Root.onSetEntityConfig ); 
  },

  onSetEntityConfig : function( response ) {
    console.log( "Response from POST config: " + JSON.stringify( response ) );
  },

  version : function() {
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.version( Root.onGetVersion )
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
    Framework.update( entity, Root.updateCallback );
  },

  onParameter : function( key, value ) {
    if( key == "entity" ) {
      $("#entity").val( value ); 
    }
  },

  setup : function() {
    Framework.setNodeHost(); // in case override by param
    Parameters.extract( Root.onParameter );
    Loop.setup( Root.onInterval );
  }

};

$( document ).ready( function() {
  Root.setup();
} );


