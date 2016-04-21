
var Config = {

  refresh : function() {
    $( ".entity-name" ).each( function( index ) {
      var entityName = this.innerHTML;
      Framework.getConfig( entityName, Config.onGetEntityConfig );
    } );
  },

  loadNew : function() {
    var entityName = $( "#entity-new-value" ).val();
    Framework.getConfig( entityName, Config.onGetEntityConfig );
  },

  saveAll : function() {
    $( ".new-value" ).each( function( index ) {
      var entityName = this.getAttribute( "data-entity" );
      Config.save( entityName );
    } );
  },

  save : function( entityName ) {
    var configValue = $( "#"+entityName+"-new-config" ).val();
    Framework.setConfig( entityName, configValue, Config.onPostData );
  },

  onPostData : function( response ) {
    console.log( "Response from POST save: " + JSON.stringify( response ) );
  },

  onGetEntityConfig : function( json ) {

    var entityConfig = JSON.parse( json.responseText );
    var config = entityConfig.value;
    var pretty = JSON.stringify( config, null, 2 );
    var entity = entityConfig.entity;

    var innerHtml = "";
    innerHtml = innerHtml + "<td class='entity-name'>" + entity + "</td><td><textarea class='old-value' readonly>" + pretty;
    innerHtml = innerHtml + "</textarea></td><td><textarea class='new-value' data-entity='"+entity+"' id='"+entity+"-new-config'>"+ pretty;
    innerHtml = innerHtml + "</textarea></td><td><input type='button' id='save-"+entity+"' class='btn btn-sm btn-danger' onclick='Config.save( \"" + entity + "\" );' value='Save'/></td>";

    var outerHtml1 = "<tr id='tr-" + entity + "'>";
    var outerHtml2 = "</tr>";
    var html = outerHtml1 + innerHtml + outerHtml2;

    if( $( "#tr-"+entity ).length ) {
      $( "#tr-"+entity ).replaceWith( html );
    }
    else {
      $( "#table-body" ).append( html );
    }
  },

  onParameter : function( key, value ) {
    if( key == "entity" ) {
      $("#entity-new-value").val( value );
      Config.loadNew();
    }
    else if( key == "interval" ) {
      $("#interval").val( value ); 
    }
    else if( key == "start" ) {
      Loop.resume();
    }
  },

  setup : function() {
    Parameters.extract( Config.onParameter );
    Framework.setup();
    Loop.setup( Config.refresh );
  }

};

$( document ).ready( function() {
  Config.setup();
  Config.refresh(); // once
} );
