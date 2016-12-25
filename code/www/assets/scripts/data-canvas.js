
var DataCanvas = {

  pxPerElement : 8,
  pxMargin : 2,

  getScrollbarWidth : function() {
    var outer = document.createElement("div");
    outer.style.visibility = "hidden";
    outer.style.width = "100px";
    outer.style.msOverflowStyle = "scrollbar"; // needed for WinJS apps

    document.body.appendChild(outer);

    var widthNoScroll = outer.offsetWidth;
    // force scrollbars
    outer.style.overflow = "scroll";

    // add innerdiv
    var inner = document.createElement("div");
    inner.style.width = "100%";
    outer.appendChild(inner);        

    var widthWithScroll = inner.offsetWidth;

    // remove divs
    outer.parentNode.removeChild(outer);

    return widthNoScroll - widthWithScroll;
  },

  strokeElementsInArray : function( ctx, x0, y0, w, h, selectedElements, strokeStyle ) {

    ctx.strokeStyle = "#00FF00";

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var cx = x * DataCanvas.pxPerElement;
        var cy = y * DataCanvas.pxPerElement;
        var offset = y * w + x;
         
        for( var i = 0; i < selectedElements.length; ++i ) {
          if( selectedElements[ i ] == offset ) { // select one at a time
            ctx.strokeRect( x0 + cx, y0 + cy, DataCanvas.pxPerElement, DataCanvas.pxPerElement );        
          }
        }

      }
    }
  },

  strokeElements : function( ctx, x0, y0, w, h, elements, strokeStyle ) {

    ctx.strokeStyle = strokeStyle;

    for( var i = 0; i < elements.length; ++i ) {
      var offset = elements[ i ];
      var xs = Math.floor( offset % w );
      var ys = Math.floor( offset / w );

      var x = x0 + xs * DataCanvas.pxPerElement;
      var y = y0 + ys * DataCanvas.pxPerElement;
      
      ctx.strokeRect( x, y, DataCanvas.pxPerElement, DataCanvas.pxPerElement );        
    }
  },

  strokeElementsThreshold : function( ctx, x0, y0, w, h, data, strokeStyle, threshold ) {

    var dx = 0;
    var size = DataCanvas.pxPerElement;

    if( small ) {
      size = DataCanvas.pxPerElement * 0.5;
      dx = size;
    }

    ctx.strokeStyle = strokeStyle;

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var cx = x0 + x * DataCanvas.pxPerElement;
        var cy = y0 + y * DataCanvas.pxPerElement;
        var offset = y * w + x;
  
        var value = data.elements.elements[ offset ];
        if( value > threshold ) {
          ctx.strokeRect( cx+dx, cy, size, size );        
        }
      }
    }
  },

  fillElementsRgbRange : function( ctx, x0, y0, w, h, dataR, dataG, dataB, minValue, maxValue ) {

    var range = maxValue - minValue;
    var rangeScale = 1.0 / range; // ie if range = 3, then values scaled by 1/3=0.3 

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var cx = x * DataCanvas.pxPerElement;
        var cy = y * DataCanvas.pxPerElement;
        var offset = y * w + x;
        var valueR = 0.0;
        var valueG = 0.0;
        var valueB = 0.0;
        if( dataR ) valueR = dataR.elements.elements[ offset ];
        if( dataG ) valueG = dataG.elements.elements[ offset ];
        if( dataB ) valueB = dataB.elements.elements[ offset ];

if( valueG > 0.5 ) {
var g = 0;
g++;
}

        valueR -= minValue;
        valueG -= minValue;
        valueB -= minValue;

        valueR = Math.max( 0, valueR );
        valueG = Math.max( 0, valueG );
        valueB = Math.max( 0, valueB );

        valueR = Math.min( maxValue, valueR );
        valueG = Math.min( maxValue, valueG );
        valueB = Math.min( maxValue, valueB );

        valueR *= rangeScale;     
        valueG *= rangeScale;     
        valueB *= rangeScale;     

        ctx.fillStyle = DataCanvas.getStyleUnitRgb( valueR, valueG, valueB );
        ctx.fillRect( x0 + cx, y0 + cy, DataCanvas.pxPerElement, DataCanvas.pxPerElement );        
        ctx.fill();

        ctx.strokeStyle = "#808080";
        ctx.strokeRect( x0 + cx, y0 + cy, DataCanvas.pxPerElement, DataCanvas.pxPerElement );        
      }
    }
  },

  fillElementsRgb : function( ctx, x0, y0, w, h, dataR, dataG, dataB ) {

    var maxValue = 0.0;

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var offset = y * w + x;
        var valueR = 0.0;
        var valueG = 0.0;
        var valueB = 0.0;
        if( dataR ) valueR = dataR.elements.elements[ offset ];
        if( dataG ) valueG = dataG.elements.elements[ offset ];
        if( dataB ) valueB = dataB.elements.elements[ offset ];
        maxValue = Math.max( maxValue, valueR );
        maxValue = Math.max( maxValue, valueG );
        maxValue = Math.max( maxValue, valueB );
      }
    }

    var scale = 1.0;
    if( maxValue > 0.0 ) {
      scale /= maxValue;
    }

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var cx = x * DataCanvas.pxPerElement;
        var cy = y * DataCanvas.pxPerElement;
        var offset = y * w + x;
        var valueR = 0.0;
        var valueG = 0.0;
        var valueB = 0.0;

        if( dataR ) valueR = dataR.elements.elements[ offset ];
        if( dataG ) valueG = dataG.elements.elements[ offset ];
        if( dataB ) valueB = dataB.elements.elements[ offset ];

        valueR *= scale;
        valueG *= scale;
        valueB *= scale;

        ctx.fillStyle = DataCanvas.getStyleUnitRgb( valueR, valueG, valueB );
        ctx.fillRect( x0 + cx, y0 + cy, DataCanvas.pxPerElement, DataCanvas.pxPerElement );        
        ctx.fill();

        ctx.strokeStyle = "#808080";
        ctx.strokeRect( x0 + cx, y0 + cy, DataCanvas.pxPerElement, DataCanvas.pxPerElement );        
      }
    }
  },

  byte2Hex : function( b ) {
    return ("0"+(b.toString(16))).slice(-2);//.toUpperCase();
  },

  getStyleUnitRgb : function( r, g, b ) {
// clip to unit, scale into a colour
    r = Math.max( 0, Math.min( 255, Math.floor( r * 255 ) ) );
    g = Math.max( 0, Math.min( 255, Math.floor( g * 255 ) ) );
    b = Math.max( 0, Math.min( 255, Math.floor( b * 255 ) ) );

    var style = "#" + DataCanvas.byte2Hex( r ) + DataCanvas.byte2Hex( g ) + DataCanvas.byte2Hex( b );
    return style;
  },

  fillElementsUnitRgb : function( ctx, x0, y0, w, h, dataR, dataG, dataB ) {

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var cx = x * DataCanvas.pxPerElement;
        var cy = y * DataCanvas.pxPerElement;
        var offset = y * w + x;
        var valueR = 0.0;
        var valueG = 0.0;
        var valueB = 0.0;

        if( dataR ) valueR = dataR.elements.elements[ offset ];
        if( dataG ) valueG = dataG.elements.elements[ offset ];
        if( dataB ) valueB = dataB.elements.elements[ offset ];
         
        ctx.fillStyle = DataCanvas.getStyleUnitRgb( valueR, valueG, valueB );
        ctx.fillRect( x0 + cx, y0 + cy, DataCanvas.pxPerElement, DataCanvas.pxPerElement );        
        ctx.fill();

        ctx.strokeStyle = "#808080";
        ctx.strokeRect( x0 + cx, y0 + cy, DataCanvas.pxPerElement, DataCanvas.pxPerElement );        
      }
    }
  },

  fillElementsAlpha : function( ctx, x0, y0, w, h, data, r,g,b, scale ) {

    var maxValue = 0.01;

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var offset = y * w + x;
        maxValue = Math.max( maxValue, data.elements.elements[ offset ] );
      }
    }

    ctx.fillStyle = "#000000";
    ctx.fillRect( x0, y0, DataCanvas.pxPerElement * w, DataCanvas.pxPerElement * h );        

    var fillStyle1 = "rgba("+r+","+g+","+b+",";

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var cx = x * DataCanvas.pxPerElement;
        var cy = y * DataCanvas.pxPerElement;
        var offset = y * w + x;
        var value = data.elements.elements[ offset ];
        if( scale ) {
          value = value / maxValue; 
        }
        ctx.fillStyle = fillStyle1 + value.toFixed( 3 ) + ")";
        ctx.fillRect( x0 + cx, y0 + cy, DataCanvas.pxPerElement, DataCanvas.pxPerElement );        
        ctx.fill();

        ctx.strokeStyle = "#808080";
        ctx.strokeRect( x0 + cx, y0 + cy, DataCanvas.pxPerElement, DataCanvas.pxPerElement );        
      }
    }
  },

  resizeCanvas : function( canvasSelector, data, repeatX, repeatY ) {
    var dataSize = Framework.getDataSize( data );
    var w = dataSize.w;
    var h = dataSize.h;

    if( ( w == 0 ) || ( h == 0 ) ) {
      return null;
    }

    if( !repeatX ) repeatX = 1;
    if( !repeatY ) repeatY = 1;

    var c = $( canvasSelector )[ 0 ];
    var cw = ( w * DataCanvas.pxPerElement ) * repeatX + DataCanvas.pxMargin * (repeatX -1);
    var ch = ( h * DataCanvas.pxPerElement ) * repeatY + DataCanvas.pxMargin * (repeatY -1);

    if( window.innerWidth < cw ) cw += DataCanvas.getScrollbarWidth();
    if( (window.innerWidth) < ch ) ch += DataCanvas.getScrollbarWidth();
    
    c.width  = cw;
    c.height = ch;

    var ctx = c.getContext( "2d" );
    ctx.fillStyle = "#505050";
    ctx.fillRect( 0, 0, c.width, c.height );
    
    var size = {
      w: w,
      h: h,
      ctx: ctx
    };

    return size;
  }

};

