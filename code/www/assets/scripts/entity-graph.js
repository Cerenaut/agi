
var EntityGraph = {

  show : function() {

    var entity = $( "#entity" ).val();
    console.log( "Generate graph for subtree starting with entity: " + entity );

    EntityGraph.drawSubGraph( entity );
  },

  drawSubGraph : function( entity ) {
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.getSubtree( entity, EntityGraph.onGetEntities );
  },

  onGetEntities : function( json ) {
    if( json.length == 0 ) {
      return;
    }

    if( json.status != 200 ) {
      return;
    }

    var nodes = {};
    var links = [];
    var classValue = "entity";

    var entities = JSON.parse( json.responseText );
    for( var i = 0; i < entities.length; ++i ) {
      var entity = entities[ i ];
      var entityName = entity.name;
      var entityParent = entity.parent

      if ( entityParent == null || entityParent == "null" ) {
        continue;
      }
  
      var link = { source: entityName, target: entityParent, classValue: classValue, strokeWidth: 2 };
      links.push( link );

      console.log( link.source + " ----> " + link.target );  
    }
    
    // Compute the distinct nodes from the links.
    strokeStyle = "#555555";
    fillStyle = "#eeeeee";
    nodeValue = "20";

    links.forEach(function(link) {
      link.source = nodes[link.source] || (nodes[link.source] = { name:link.source, strokeStyle:strokeStyle, fillStyle:fillStyle, value:nodeValue });
      link.target = nodes[link.target] || (nodes[link.target] = { name:link.target, strokeStyle:strokeStyle, fillStyle:fillStyle, value:nodeValue });
    });

    var typesArray = [];
    typesArray.push( classValue ); // not needed?
    new AgiGraph( "#entities-graph", nodes, links, typesArray );

  },

  onParameter : function( key, value ) {
    if( key == "entity" ) {
      $("#entity").val( value ); 
    }
  },

  setup : function() {
    Framework.setNodeHost(); // in case override by param
    Parameters.extract( EntityGraph.onParameter );
    EntityGraph.show();
  }

};

$( document ).ready( function() {
  EntityGraph.setup();
} );
