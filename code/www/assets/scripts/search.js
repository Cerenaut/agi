
var Search = {

  getNodes : function() {
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.getNodes( Search.onGetObjects );    
  },

  getData : function() {
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.getDataNames( Search.onGetObjects );    
  },

  getEntities : function() {
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.getEntities( Search.onGetObjects );    
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
      var entity = objects[ i ];
      var entityName = entity.name;
      var parentName = entity.parent;

      var val = entityName;
      if ( parentName == "null" ) {
        val = val + " *** ROOT ***";
      }


      html = html + "<tr><td style='text-align:left;'>" + val + "</td></tr>";
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


