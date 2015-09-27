
// Object prototype: http://stackoverflow.com/questions/10014552/javascript-prototypes-with-callbacks-and-this

function AgiData( data ) {

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // Variables
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  //this.data = data;
  this.size = null;
  this.elements = null;

  this.setup( data );
}

///////////////////////////////////////////////////////////////////////////////////////////////////
// Methods
///////////////////////////////////////////////////////////////////////////////////////////////////
AgiData.prototype = {

  getElements : function() {
    if( this.elements == null ) {
      return null;
    }
    return this.elements.elements;
  },
  getLength : function() {
    if( this.elements == null ) {
      return 0;
    }
    return this.elements.length;
  },

  getSize2d : function() {
    // Since the data structure may be anything from 1-d to N-d 
    // But we must present it as 2D plot... we need a policy to convert these structures to 2-D
    // 
    // First we look to see if it's actually 2D. If so, we keep the true 2-D structure.
    // 
    // If not, if it's 1D we try to make it square so we can fit it better on the screen.
    // If it's higher than 2D, we treat it as 1D.
    if( this.size == null ) {
      return {w:0,h:0};
    }

    var w = 0;
    var h = 0;

    if( this.size.sizes.length == 2 ) {
      w = this.size.sizes[ 1 ]; // row major format
      h = this.size.sizes[ 0 ];
    }
    else {
      var volume = 1;
      for( var d = 0; d < this.size.sizes.length; d++ ) {
        size = this.size.sizes[ d ];
        volume = volume * size;
      }
      square = Math.sqrt( volume );
      square = Math.floor( square );
      w = square;
      h = square;
      while( ( w * h ) < volume ) {
        w = w +1;
      }
    }    
    return {w:w,h:h};
  },

  setup : function( data ) {
    this.size = JSON.parse( data.size );
    this.elements = JSON.parse( data.elements );
  },

  plot2d : function( idElement, wCell, wAxes ) {

    // clear old data
    $( "#data-plot" ).html( "" );
  
    var e = this.getElements();
    //var l = this.getLength();
    var sizeData = this.getSize2d();

    var wData = sizeData.w;
    var hData = sizeData.h;

    var wPlot = wCell * wData;
    var hPlot = wCell * hData;

    var wElement = wPlot + wAxes;
    var hElement = hPlot + wAxes;

    // offset plot providing space for axes
    var xPlot = wAxes;
    var yPlot = wAxes;
 
   function make_x_axis() {        
     return d3.svg.axis()
           .scale(x)
           .orient("top")
           .ticks(5)
   }

   function make_y_axis() {        
      return d3.svg.axis()
          .scale(y)
          .orient("left")
          .ticks(5)
    }

    // create SVG container for the plot
    var svg = d3.select("#"+idElement)
                .append("svg")
                .attr("width", wElement)
                .attr("height", hElement);

    // Domain and Range of a d3 scale explained:
    // https://www.dashingd3js.com/d3js-scales
    var x = d3.scale.linear()
              .domain( [0, wData] )
              .range( [0, wPlot] );      // the space available
    var y = d3.scale.linear()
              .domain( [0, hData] )
              .range( [0, hPlot] );

    var xAxis = d3.svg.axis().scale(x).orient("top"),
        yAxis = d3.svg.axis().scale(y).orient("left");

    // origin coordinates for these axes
    var xAxis_x = wAxes;
    var xAxis_y = wAxes;
    var yAxis_x = wAxes;
    var yAxis_y = wAxes;

    var halfAxis = wAxes * 0.5;

    // create the grid axes (behind)
    svg.append("g")         
      .attr("class", "grid")
      .attr("transform", "translate("+xAxis_x+", "+xAxis_y+")")
      .call(make_x_axis()
        .tickSize(-hPlot, 0, 0)
        .tickFormat("")
      )

    svg.append("g")         
      .attr("class", "grid")
      .attr("transform", "translate("+yAxis_x+", "+yAxis_y+")")
      .call(make_y_axis()
        .tickSize(-wPlot, 0, 0)
        .tickFormat("")
      )

    svg.append("g")
        .attr("class", "axis")
        .attr("transform", "translate("+xAxis_x+", "+(xAxis_y-halfAxis)+")")
        .call(xAxis);
 
    svg.append("g")
         .attr("class", "axis")
        .attr("transform", "translate("+(yAxis_x-halfAxis)+", "+yAxis_y+")")
        .call(yAxis);

    if( e == null ) {  
      return; // no data to plot
    }

    // add the actual datapoints
    svg.selectAll("circle")
        .data(e)
        .enter()
        .append("circle")
        .attr("class", "circle")
        .attr("cx", function ( d, i ) { 
             xe = i % wData;
             return xPlot + xe * wCell; 
        })
        .attr("cy", function ( d, i ) { 
             ye = Math.floor( i / wData );
             return yPlot + ye * wCell; 
        })
        .attr("title", function ( d, i ) { 
             xe = i % wData;
             ye = Math.floor( i / wData );
             return xe + "," + ye;
        })
        .attr("data-content", function ( d, i ) { 
             return e[ i ];
        })
//        .transition()
//        .duration(10)
        .attr("r", function (d,i) { return d * wCell * 0.5; });

    // set the options – read more on the Bootstrap page linked to above
    $('svg .circle').popover({
       'trigger':'hover'
       ,'container': 'body'
       ,'placement': 'top'
       ,'white-space': 'nowrap'
       ,'html':'true'
    });
  }

}

