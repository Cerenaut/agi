
var Search = {

  getNodes : function() {
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.getNodes( Search.onGetEntities );    
  },

  getData : function() {
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.getDataNames( Search.onGetEntities );    
  },

  getEntities : function() {
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.getEntities( Search.onGetEntities );    
  },

  onGetObjects : function( json ) {
    if( json.length == 0 ) {
      return;
    }

    if( json.status != 200 ) {
      return;
    }

    var html = "";
    var objects = JSON.parse( json.responseText );
    for( var i = 0; i < objects.length; ++i ) {
      var object = objects[ i ];
      var objectName = object.name;
      html = html + "<tr><td>" + objectName + "</td></tr>";      
    }

    $( "#table-body" ).html( html );
  },

  update : function() {

    var entity = $( "#entity" ).val();
    console.log( "Updating " + entity );

    Framework.update( entity, Search.updateCallback );
  },

  setup : function() {

  }

};

$( document ).ready( function() {
  Search.setup();
} );


