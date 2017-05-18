// based on http://bl.ocks.org/mbostock/1153292
function AgiGraph( elementId, nodes, links, typesArray, _width, _height, _arrows ) {

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // Variables
  ///////////////////////////////////////////////////////////////////////////////////////////////////
  var self = this;
  self.nodes = null;
  self.elementId = null;

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // Methods
  ///////////////////////////////////////////////////////////////////////////////////////////////////
  this.linkArc = function( d ) {
    var dx = d.target.x - d.source.x,
        dy = d.target.y - d.source.y,
        dr = Math.sqrt(dx * dx + dy * dy);
    return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
  };

  this.transform = function( d ) {
    return "translate(" + d.x + "," + d.y + ")";
  };

  this.tick = function() {
    self.path.attr( "d", self.linkArc );
    self.circle.attr( "transform", self.transform );
    self.text.attr( "transform", self.transform );
  };

  this.setup = function( elementId, nodes, links, typesArray, width, height ) {

    // remove any existing graph:
    $( elementId ).html( "" );

    self.elementId = elementId;
    self.nodes = nodes;

    if( !width ) {
      width = screen.height * 0.7;
    }
    if( !height ) {
      height = screen.height * 0.5;
    }

    self.force = d3.layout.force()
      .nodes( d3.values( self.nodes ) )
      .links(links)
      .linkStrength(0.02)
      .size([width, height])
      .linkDistance(180)
      .charge(-600)
      .on("tick", self.tick )
      .start();

    var e = d3.select( elementId );

    var svg = e.append( "svg" )
               .attr("width", "100%")
               .attr("height", height );

    // Per-type markers, as they don't inherit styles.
    var arrowPath = "";
    if( _arrows ) {
      arrowPath = "M0,-5L10,0L0,5";
    }
            
    svg.append("defs")
       .selectAll("marker")
       .data( typesArray )
       .enter().append("marker")
               .attr("id", function(d) { return d; })
               .attr("viewBox", "0 -5 10 10")
               .attr("refX", 15)
               .attr("refY", -1.5)
               .attr("class", function(d) { return "marker all "; })
               .attr("markerWidth", 12)
               .attr("markerHeight", 12)
               .attr("orient", "auto")
       .append("path")
       .attr("d", arrowPath );


    self.path = svg.append("g").selectAll("path")
                   .data( self.force.links() )
                   .enter().append("path")
                           .attr("class", function(d) { return "link " + d.classValue; })
                           .style("stroke-width", function(d) { return d.strokeWidth; })
                           .attr("marker-end", function(d) { return "url(#" + d.type + ")"; });

    self.circle = svg.append("g")
                     .selectAll("circle")
                     .data( self.force.nodes() )
                     .enter().append("circle")
                             .attr("r", function(d) { if( d.value ){ return d.value; } else { return 12; } } )
                             .style( "stroke", function(d) { return d.strokeStyle; } )
                             .style( "fill", function(d) { return d.fillStyle; } )
                             .call( self.force.drag );
//                             .attr("r", 12 )
//                             .style( "stroke", function(d) { return d.strokeStyle; } )
//                             .style( "fill", function(d) { return d.fillStyle; } )

    self.text = svg.append("g")
                   .selectAll("text")
                   .data( self.force.nodes() )
                   .enter().append("text")
                           .attr("x", 8)
                           .attr("y", ".31em")
                   .text(function(d) { return d.name; });

  };

  self.setup( elementId, nodes, links, typesArray, _width, _height, _arrows );
}

