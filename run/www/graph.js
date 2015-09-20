// based on http://bl.ocks.org/mbostock/1153292
function AgiGraph( elementId, nodes, links ) {

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

// Use elliptical arc path segments to doubly-encode directionality.
/*
function tick() {
    path.attr( "d", linkArc );
    circle.attr( "transform", transform );
    text.attr( "transform", transform );
}
function linkArc(d) {
    var dx = d.target.x - d.source.x,
        dy = d.target.y - d.source.y,
        dr = Math.sqrt(dx * dx + dy * dy);
    return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
}

function transform(d) {
  return "translate(" + d.x + "," + d.y + ")";
}
*/

/*var links = [
  {source: "Microsoft", target: "Amazon", type: "licensing"},
  {source: "Microsoft", target: "HTC", type: "licensing"},
  {source: "Samsung", target: "Apple", type: "suit"},
  {source: "Motorola", target: "Apple", type: "suit"},
  {source: "Nokia", target: "Apple", type: "resolved"},
  {source: "HTC", target: "Apple", type: "suit"},
  {source: "Kodak", target: "Apple", type: "suit"},
  {source: "Microsoft", target: "Barnes & Noble", type: "suit"},
  {source: "Microsoft", target: "Foxconn", type: "suit"},
  {source: "Oracle", target: "Google", type: "suit"},
  {source: "Apple", target: "HTC", type: "suit"},
  {source: "Microsoft", target: "Inventec", type: "suit"},
  {source: "Samsung", target: "Kodak", type: "resolved"},
  {source: "LG", target: "Kodak", type: "resolved"},
  {source: "RIM", target: "Kodak", type: "suit"},
  {source: "Sony", target: "LG", type: "suit"},
  {source: "Kodak", target: "LG", type: "resolved"},
  {source: "Apple", target: "Nokia", type: "resolved"},
  {source: "Qualcomm", target: "Nokia", type: "resolved"},
  {source: "Apple", target: "Motorola", type: "suit"},
  {source: "Microsoft", target: "Motorola", type: "suit"},
  {source: "Motorola", target: "Microsoft", type: "suit"},
  {source: "Huawei", target: "ZTE", type: "suit"},
  {source: "Ericsson", target: "ZTE", type: "suit"},
  {source: "Kodak", target: "Samsung", type: "resolved"},
  {source: "Apple", target: "Samsung", type: "suit"},
  {source: "Kodak", target: "RIM", type: "suit"},
  {source: "Nokia", target: "Qualcomm", type: "suit"}
];*/

  this.setup = function( elementId, nodes, links ) {
    self.elementId = elementId;
    self.nodes = nodes;//{};

    var width = screen.height * 0.7;
    var height = screen.height * 0.5;

    // Compute the distinct nodes from the links.
    //links.forEach(function(link) {
    //  link.source = self.nodes[link.source] || (self.nodes[link.source] = {name: link.source});
    //  link.target = self.nodes[link.target] || (self.nodes[link.target] = {name: link.target});
    //});

    self.force = d3.layout.force()
      .nodes( d3.values( self.nodes ) )
      .links(links)
      .size([width, height])
      .linkDistance(60)
      .charge(-300)
      .on("tick", self.tick )
      .start();

    var e = d3.select( elementId );
    //    e.selectAll("*").remove(); // remove children
    var svg = e.append( "svg" )
               .attr("width", "100%")
               .attr("height", height );

    // Per-type markers, as they don't inherit styles.
    svg.append("defs")
       .selectAll("marker")
       .data(["suit", "licensing", "resolved"])
       .enter().append("marker")
               .attr("id", function(d) { return d; })
               .attr("viewBox", "0 -5 10 10")
               .attr("refX", 15)
               .attr("refY", -1.5)
               .attr("markerWidth", 6)
               .attr("markerHeight", 6)
               .attr("orient", "auto")
       .append("path")
       .attr("d", "M0,-5L10,0L0,5");

    self.path = svg.append("g").selectAll("path")
                   .data( self.force.links() )
                   .enter().append("path")
                           .attr("class", function(d) { return "link " + d.type; })
                           .attr("marker-end", function(d) { return "url(#" + d.type + ")"; });

    self.circle = svg.append("g")
                     .selectAll("circle")
                     .data( self.force.nodes() )
                     .enter().append("circle")
                             .attr("r", 6)
                             .call( self.force.drag );

    self.text = svg.append("g")
                   .selectAll("text")
                   .data( self.force.nodes() )
                   .enter().append("text")
                           .attr("x", 8)
                           .attr("y", ".31em")
                   .text(function(d) { return d.name; });

  };

  self.setup( elementId, nodes, links );
}

