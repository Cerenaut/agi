
//       new & old   new,old,pred,fp,fn     
// input contextfree contextual
// input contextfree contextual
// input contextfree contextual

function getScrollbarWidth() {
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
}

var Region = {

  pixelsReceptiveField : 8,
  pixelsColumnGap : 3,
  pixelsMargin : 4,
  pixelsPerBit : 8,
  pixelsPerGap : 4,
  prefix : "",

  selectedCells : [  ],
  selectedInput1 : [  ],
  selectedInput2 : [  ],

  regionSuffixes : [ "-input-ff-1", "-input-ff-2", "-region-activity", "-region-activity-inferred", "-region-prediction-weights", "-region-likelihood", "-region-prediction", "-organizer-cell-mask", "-organizer-cell-weights", "-classifier-cell-weights", "-classifier-cell-errors", "-classifier-cell-mask" ],
  regionSuffixIdx : 0,
  dataMap : {
  },

  setStatus : function( status ) {
    $( "#status" ).html( status );
  },

  selectClear : function() {
    Region.selectedCells = [];
    Region.updateSelection( "sel-cells", Region.selectedCells );
    Region.repaint();
  },

  selectText : function() {
    var value = $( "#sel-cells" ).val();
    var values = value.split( "," );

    Region.selectedCells = [];

    for( var i = 0; i < values.length; ++i ) {
      var value = values[ i ];
      if( value.length < 1 ) {
        continue;
      }

      var cell = parseInt( value );
      Region.selectedCells.push( cell );
    }

    Region.updateSelection( "sel-cells", Region.selectedCells );
    Region.repaint();
  },

  selectCells : function( dataSuffix ) {
    var data = Region.findData( dataSuffix );
    if( !data ) {
      return; // can't paint
    }

    Region.selectedCells = [];

    for( var i = 0; i < data.elements.elements.length; ++i ) {
      var value = data.elements.elements[ i ];
      if( value > 0.5 ) {
        Region.selectedCells.push( i );
      }
    }

    Region.updateSelection( "sel-cells", Region.selectedCells );
    Region.repaint();
  },

  selectPrevious : function() {
    Region.selectCells( "-region-activity-old" );
  },

  selectActive : function() {
    Region.selectCells( "-region-activity" );
  },
  selectPredicted : function() {
    Region.selectCells( "-region-activity-inferred" );
  },

  toggleSelectCell : function( offset ) {
    Region.toggleSelection( Region.selectedCells, offset );
    Region.updateSelection( "sel-cells", Region.selectedCells );
    Region.repaint();
  },
  toggleSelectInput1 : function( offset ) {
    Region.toggleSelection( Region.selectedInput1, offset );
    Region.updateSelection( "sel-input-1", Region.selectedInput1 );
    Region.repaint();
  },
  toggleSelectInput2 : function( offset ) {
    Region.toggleSelection( Region.selectedInput2, offset );
    Region.updateSelection( "sel-input-2", Region.selectedInput2 );
    Region.repaint();
  },

  selectThreshold : function() {
    Region.selectedInput1 = [];
    Region.selectedInput2 = [];

    var data1 = Region.findData( "-input-ff-1" );
    if( !data1 ) {
      return; // can't paint
    }

    var dataSize1 = Framework.getDataSize( data1 );
    var w1 = dataSize1.w;
    var h1 = dataSize1.h;

    if( ( w1 == 0 ) || ( h1 == 0 ) ) {
      return;
    }

    var data2 = Region.findData( "-input-ff-2" );
    if( !data2 ) {
      return; // can't paint
    }

    var dataSize2 = Framework.getDataSize( data2 );
    var w2 = dataSize2.w;
    var h2 = dataSize2.h;

    if( ( w2 == 0 ) || ( h2 == 0 ) ) {
      return;
    }

    var dataActivityNew = Region.findData( "-region-activity" );
    if( !dataActivityNew ) {
      return; // can't paint
    }

    var dataOrganizerOutputMask = Region.findData( "-organizer-cell-mask" );
    if( !dataOrganizerOutputMask ) {
      return; // can't paint
    }

    var organizerDataSize = Framework.getDataSize( dataOrganizerOutputMask );
    var ow = organizerDataSize.w;
    var oh = organizerDataSize.h;

    if( ( ow == 0 ) || ( oh == 0 ) ) {
      return;
    }

    var regionDataSize = Framework.getDataSize( dataActivityNew );
    var rw = regionDataSize.w;
    var rh = regionDataSize.h;
    var cw = rw / ow;
    var ch = rh / oh;
//    var cd = Region.config.classifierDepthCells;

    var classifierInputStride = w1 * h1 + w2 * h2;
    var classifierInputOffset = 0;

    Region.selectThresholdForInput( 0, Region.selectedInput1, w1, h1, rw, rh, cw, ch, ow, oh, classifierInputStride, classifierInputOffset );

    classifierInputOffset = w1 * h1;

    Region.selectThresholdForInput( 1, Region.selectedInput2, w2, h2, rw, rh, cw, ch, ow, oh, classifierInputStride, classifierInputOffset );

    Region.updateSelection( "sel-input-1", Region.selectedInput1 );
    Region.updateSelection( "sel-input-2", Region.selectedInput2 );

    Region.repaint();
  },

  selectThresholdForInput : function( inputIndex, selectedInput, w, h, rw, rh, cw, ch, ow, oh, classifierInputStride, classifierInputOffset ) {
    var threshold = $( "#threshold" ).val();

    var dataClassifierOutputWeights = Region.findData( "-classifier-cell-weights" );

    for( var c = 0; c < Region.selectedCells.length; ++c ) {
      var regionOffset = Region.selectedCells[ c ];

      if( !dataClassifierOutputWeights ) {
        continue; // can't paint
      }

      var rx = Math.floor( regionOffset % rw );
      var ry = Math.floor( regionOffset / rw );

      var ox = Math.floor( rx / cw ); // coordinates of column in grid (region).
      var oy = Math.floor( ry / ch );    

      var cx = rx - ( ox * cw ); // coordinates in column.
      var cy = ry - ( oy * ch );    

      var classifierHeight = ( ch );/// cd );
      var classifierY = cy - ( classifierHeight * Math.floor( cy / classifierHeight ) );
      var classifierOffset = ( ( classifierY * cw ) + cx ) * classifierInputStride;

      var classifierSize = cw * classifierHeight * classifierInputStride; // Each classifier has this number of cells. 
      var classifierIndex = oy * ow + ox;
      var packedOffset = classifierIndex * classifierSize;

      // examine each input:
      for( var y = 0; y < h; ++y ) {
        for( var x = 0; x < w; ++x ) {
          var inputOffset = y * w + x;
          var weightsOffset = packedOffset + classifierOffset + classifierInputOffset + inputOffset;

          var value = dataClassifierOutputWeights.elements.elements[ weightsOffset ];
          if( value < threshold ) { // if less than this frac of max weight, don't bother drawing
            continue;
          }

          selectedInput.push( inputOffset );
        }
      }

    }   
  },

  updateSelection : function( id, list ) {
    var values = "";
    for( var i = 0; i < list.length; ++i ) {
      var selected = list[ i ];
      if( i > 0 ) {
        values = values + ",";
      }
      values = values + selected;
    }    
    $( "#"+id ).val( values );
  },

  toggleSelection : function( list, item ) {
    var found = false;

    for( var i = 0; i < list.length; ++i ) {
      var selected = list[ i ];
      if( selected == item ) { 
        found = true;
        list.splice( i, 1 ); // remove selection
        break;
      }
    }
 
    if( !found ) {
      list.push( item );
    }
  },

  // info about mouse actions
  setMouseLeft : function( status ) {
//    $( "#mouse-left" ).html( status );
  },
  setMouseRight : function( status ) {
//    $( "#mouse-right" ).html( status );
  },

  onMouseMoveLeft : function( e, mx, my ) {
  },
  onMouseMoveCentre : function( e, mx, my ) {
  },
  onMouseMoveRight : function( e, mx, my ) {
  },

  onMouseClickLeft : function( e, mx, my ) {
    Region.onMouseClickCentre( e, mx, my );
  },

  onMouseClickCentre : function( e, mx, my ) {

    var dataNew = Region.findData( "-region-activity" );
    if( !dataNew ) {
      return; // can't paint
    }

    var dataSize = Framework.getDataSize( dataNew );
    var w = dataSize.w;
    var h = dataSize.h;

    if( ( w == 0 ) || ( h == 0 ) ) {
      return;
    }

    var cx = Math.floor( mx / ( Region.pixelsPerBit ) );
    var cy = Math.floor( my / ( Region.pixelsPerBit ) );

    var offset = cy * w + cx;

    Region.toggleSelectCell( offset );
    //Region.toggleSelection( Region.selectedCells, offset );
    //Region.updateSelection( "sel-cells", Region.selectedCells );

    //Region.
    //Region.repaint();
  },

  onMouseClickRight : function( e, mx, my ) {
    var data1 = Region.findData( "-input-ff-1" );
    if( !data1 ) {
      return; // can't paint
    }

    var data2 = Region.findData( "-input-ff-2" );
    if( !data2 ) {
      return; // can't paint
    }

    var dataSize1 = Framework.getDataSize( data1 );
    var iw1 = dataSize1.w;
    var ih1 = dataSize1.h;

    if( ( iw1 == 0 ) || ( ih1 == 0 ) ) {
      return;
    }

    var dataSize2 = Framework.getDataSize( data2 );
    var iw2 = dataSize2.w;
    var ih2 = dataSize2.h;

    if( ( iw2 == 0 ) || ( ih2 == 0 ) ) {
      return;
    }

    var ix = Math.floor( mx / Region.pixelsPerBit );
    var iy = Math.floor( my / Region.pixelsPerBit );

    if( ( ix < 0 ) || ( iy < 0 ) ) {
      return;
    }

    var i = 1;

    if( iy < ih1 ) { // 1st FF input
      if( ix >= iw1 ) {
        return; // out of bounds
      }      

      var offset = iy * iw1 + ix;
      Region.toggleSelectInput1( offset );
    }
    else { // maybe 2nd FF input
      if( ix >= iw2 ) {
        return; // out of bounds
      }      

      my -= ( Region.pixelsPerBit * ih1 + Region.pixelsMargin );    
      iy = Math.floor( my / Region.pixelsPerBit );
 
      var offset = iy * iw2 + ix;
      Region.toggleSelectInput2( offset );

      i = 2;
    }

    Region.setMouseRight( "Bit: I#" + i + ": [" + ix + "," + iy + "]" );
    Region.repaint();
  },

  repaint : function() {
    Region.repaintLeft();
    Region.repaintCentre();
    Region.repaintRight();
  },

  repaintCentre : function() {
    var dataNew = Region.findData( "-region-activity" );
    if( !dataNew ) {
      return; // can't paint
    }

    var dataPost = Region.findData( "-region-activity-inferred" );
    if( !dataPost ) {
      return; // can't paint
    }

//    var dataOld = Region.findData( "-region-activity-old" );
//    if( !dataOld ) {
//      return; // can't paint
//    }

    var canvasDataSize = Region.resizeCanvas( "#centre-canvas", dataNew, 1, 1 );
    if( !canvasDataSize ) {
      return; // can't paint
    }

    var x0 = 0;
    var y0 = 0;

    Region.fillDataRgbBinary( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, dataNew, dataPost, null );//, null );
    Region.strokeElements( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, Region.selectedCells, "#00ffff" );
  },

  repaintLeft : function() {
    var c = $( "#left-canvas" )[ 0 ];

    var dataNew = Region.findData( "-region-activity" );
    if( !dataNew ) {
      return; // can't paint
    }

    var dataP = Region.findData( "-region-prediction" );
    if( !dataP ) {
      return; // can't paint
    }

    var dataL = Region.findData( "-region-likelihood" );
    if( !dataL ) {
      return; // can't paint
    }

    var canvasDataSize = Region.resizeCanvas( "#left-canvas", dataNew, 1, 2 );
    if( !canvasDataSize ) {
      return; // can't paint
    }

    var x0 = 0;
    var y0 = 0;

    //       Fill         Hilight    Select
    // top:  TP/TN/FP/FN  Pred-new     Outline yellow
    // mid:  Pred-raw     Pred-new     Outline yellow
    // bot:  Output-Age   Output       Outline yellow

    // Red: Active-new Green: Active-new Blue: Pred-old
    // TP: white
    // TN: black
    // FP: Yellow
    // FN: Blue
//    Region.fillDataRgbBinary( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, dataNew, null, null );//, null );
    Region.fillDataRgba( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, dataP, 255,0,0, true );
    Region.strokeElements( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, Region.selectedCells, "#ffffff" );
//    Region.strokeDataBinary( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, dataPredictionNew, "#0000ff", true );

    y0 += ( Region.pixelsPerBit * canvasDataSize.h ); 
    y0 += ( Region.pixelsPerGap );
 
    Region.fillDataRgba( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, dataL, 0,255,0, true );
    Region.strokeElements( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, Region.selectedCells, "#ffffff" );
/*    Region.strokeDataBinary( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, dataPredictionNew, "#0000ff", true );

    y0 += ( Region.pixelsPerBit * canvasDataSize.h ); 
    y0 += ( Region.pixelsPerGap );

    Region.fillDataRgba( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, dataOutputAge, 255,0,0, true );
    Region.strokeDataBinary( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, dataOutput, "#00ffff", true );*/
  },

  strokeElements : function( ctx, x0, y0, w, h, elements, strokeStyle ) {

    ctx.strokeStyle = strokeStyle;

    for( var i = 0; i < elements.length; ++i ) {
      var offset = elements[ i ];
      var xs = Math.floor( offset % w );
      var ys = Math.floor( offset / w );

      var x = x0 + xs * Region.pixelsPerBit;
      var y = y0 + ys * Region.pixelsPerBit;
      
      ctx.strokeRect( x, y, Region.pixelsPerBit, Region.pixelsPerBit );        
    }
  },

  strokeDataBinary : function( ctx, x0, y0, w, h, data, strokeStyle, small ) {

    var dx = 0;
    var size = Region.pixelsPerBit;

    if( small ) {
      size = Region.pixelsPerBit * 0.5;
      dx = size;
    }

    ctx.strokeStyle = strokeStyle;

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var cx = x0 + x * Region.pixelsPerBit;
        var cy = y0 + y * Region.pixelsPerBit;
        var offset = y * w + x;
//        var valueR = 0.0;
//        var valueG = 0.0;
//        var valueB = 0.0;

//        if( dataR ) valueR = dataR.elements.elements[ offset ];
//        if( dataG ) valueG = dataG.elements.elements[ offset ];
//        if( dataB ) valueB = dataB.elements.elements[ offset ];
  
        var value = data.elements.elements[ offset ];
        if( value > 0.5 ) {
          ctx.strokeRect( cx+dx, cy, size, size );        
        }
       
//        ctx.strokeStyle = Region.getStyleRgb( valueR, valueG, valueB );
      }
    }
  },

  getStyleRgb : function( r, g, b ) {
    var style = "#";
    if( r > 0.5 ) {
      style += "FF";
    }
    else {
      style += "00";
    }
    if( g > 0.5 ) {
      style += "FF";
    }
    else {
      style += "00";
    }
    if( b > 0.5 ) {
      style += "FF";
    }
    else {
      style += "00";
    }
    return style;
  },

  fillDataRgbBinary : function( ctx, x0, y0, w, h, dataR, dataG, dataB ) {

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var cx = x * Region.pixelsPerBit;
        var cy = y * Region.pixelsPerBit;
        var offset = y * w + x;
        var valueR = 0.0;
        var valueG = 0.0;
        var valueB = 0.0;

        if( dataR ) valueR = dataR.elements.elements[ offset ];
        if( dataG ) valueG = dataG.elements.elements[ offset ];
        if( dataB ) valueB = dataB.elements.elements[ offset ];
         
        ctx.fillStyle = Region.getStyleRgb( valueR, valueG, valueB );
        ctx.fillRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
        ctx.fill();

        ctx.strokeStyle = "#808080";

/*        if( selectedElements ) {
          for( var i = 0; i < selectedElements.length; ++i ) {
            if( selectedElements[ i ] == offset ) { // select one at a time
              ctx.strokeStyle = "#FFFFFF";
            }
          }
        }*/

        ctx.strokeRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
      }
    }
  },

  fillDataRgba : function( ctx, x0, y0, w, h, data, r,g,b, scale ) {

    var maxValue = 0.00001;

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var offset = y * w + x;
        maxValue = Math.max( maxValue, data.elements.elements[ offset ] );
      }
    }

    ctx.fillStyle = "#000000";
    ctx.fillRect( x0, y0, Region.pixelsPerBit * w, Region.pixelsPerBit * h );        

    var fillStyle1 = "rgba("+r+","+g+","+b+",";

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var cx = x * Region.pixelsPerBit;
        var cy = y * Region.pixelsPerBit;
        var offset = y * w + x;
        var value = data.elements.elements[ offset ];
        if( scale ) {
          value = value / maxValue; 
        }
        ctx.fillStyle = fillStyle1 + value.toFixed( 3 ) + ")";
        ctx.fillRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
        ctx.fill();

        ctx.strokeStyle = "#808080";
        ctx.strokeRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
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
    c.width  = ( w * Region.pixelsPerBit ) * repeatX + Region.pixelsPerGap * (repeatX -1);
    c.height = ( h * Region.pixelsPerBit ) * repeatY + Region.pixelsPerGap * (repeatY -1);

    var ctx = c.getContext( "2d" );
    ctx.fillStyle = "#505050";
    ctx.fillRect( 0, 0, c.width, c.height );
    
    var size = {
      w: w,
      h: h,
      ctx: ctx
    };

    return size;
  },

  repaintRight : function() {
    var data1 = Region.findData( "-input-ff-1" );
    if( !data1 ) {
      return; // can't paint
    }

    var dataSize1 = Framework.getDataSize( data1 );
    var w1 = dataSize1.w;
    var h1 = dataSize1.h;

    if( ( w1 == 0 ) || ( h1 == 0 ) ) {
      return;
    }

    var data2 = Region.findData( "-input-ff-2" );
    if( !data2 ) {
      return; // can't paint
    }

    var dataSize2 = Framework.getDataSize( data2 );
    var w2 = dataSize2.w;
    var h2 = dataSize2.h;

    if( ( w2 == 0 ) || ( h2 == 0 ) ) {
      return;
    }

    var dataActivityNew = Region.findData( "-region-activity" );
    if( !dataActivityNew ) {
      return; // can't paint
    }

    var dataOrganizerOutputMask = Region.findData( "-organizer-cell-mask" );
    if( !dataOrganizerOutputMask ) {
      return; // can't paint
    }

    var organizerDataSize = Framework.getDataSize( dataOrganizerOutputMask );
    var ow = organizerDataSize.w;
    var oh = organizerDataSize.h;

    if( ( ow == 0 ) || ( oh == 0 ) ) {
      return;
    }

    var regionDataSize = Framework.getDataSize( dataActivityNew );
    var rw = regionDataSize.w;
    var rh = regionDataSize.h;
    var cw = rw / ow;
    var ch = rh / oh;
//    var cd = Region.config.classifierDepthCells;

    var dataOrganizerOutputWeights = Region.findData( "-organizer-cell-weights" );
    if( !dataOrganizerOutputWeights ) {
      return; // can't paint
    }

    var c = $( "#right-canvas" )[ 0 ];
    c.width  = w1 * Region.pixelsPerBit;
    c.height = h1 * Region.pixelsPerBit + Region.pixelsMargin + h2 * Region.pixelsPerBit;

    var ctx = c.getContext( "2d" );
    ctx.fillStyle = "#808080";
    ctx.fillRect( 0, 0, c.width, c.height );

    var classifierInputStride = w1 * h1 + w2 * h2;
    var classifierInputOffset = 0;

    var x0 = 0;
    var y0 = 0;

    Region.paintInputData( ctx, x0, y0, w1, h1, 0, data1, dataOrganizerOutputMask, dataOrganizerOutputWeights, ow, oh, rw, rh, cw, ch, Region.selectedInput1, Region.selectedCells, classifierInputStride, classifierInputOffset );

    y0 = y0 + h1 * Region.pixelsPerBit + Region.pixelsMargin;
    classifierInputOffset = w1 * h1;

    Region.paintInputData( ctx, x0, y0, w2, h2, 1, data2, dataOrganizerOutputMask, dataOrganizerOutputWeights, ow, oh, rw, rh, cw, ch, Region.selectedInput2, Region.selectedCells, classifierInputStride, classifierInputOffset );
  },

  paintInputData : function( ctx, x0, y0, w, h, inputIndex, dataInput, dataOrganizerOutputMask, dataOrganizerOutputWeights, ow, oh, rw, rh, cw, ch, selectedInput, selectedCells, classifierInputStride, classifierInputOffset ) {

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var cx = x * Region.pixelsPerBit;
        var cy = y * Region.pixelsPerBit;
        var offset = y * w + x;
        var value = dataInput.elements.elements[ offset ];
         
        ctx.fillStyle = "#000000";
        if( value > 0.5 ) {
          ctx.fillStyle = "#FFFFFF";
        }
        ctx.fillRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
        ctx.fill();

        ctx.strokeStyle = "#808080";

        if( selectedInput.length > 0 ) {
          if( selectedInput[ 0 ] == offset ) { // select one at a time
            ctx.strokeStyle = "#00FFFF";
          }
        }

        ctx.strokeRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
      }
    }

    // paint organizer receptive field centroids
    var rw = ow * cw;
//    var inputArea = w * h;

    // paint weights of selected cells
    for( var i = 0; i < selectedCells.length; ++i ) {
      var regionOffset = selectedCells[ i ]; // a cell within the region     
      var rx = Math.floor( regionOffset % rw );
      var ry = Math.floor( regionOffset / rw );

      var ox = Math.floor( rx / cw ); // coordinates of column in grid (region).
      var oy = Math.floor( ry / ch );    

      var dataClassifierOutputWeights = Region.findData( "-classifier-cell-weights" );
      if( !dataClassifierOutputWeights ) {
        continue; // can't paint
      }

      var cx = rx - ( ox * cw ); // coordinates in column.
      var cy = ry - ( oy * ch );    

      var classifierHeight = ( ch );/// cd );
      var classifierY = cy - ( classifierHeight * Math.floor( cy / classifierHeight ) );
      var classifierOffset = ( ( classifierY * cw ) + cx ) * classifierInputStride;

      var classifierSize = cw * classifierHeight * classifierInputStride; // Each classifier has this number of cells. 
      var classifierIndex = oy * ow + ox;
      var packedOffset = classifierIndex * classifierSize;

      // find max input
      maxWeight = 0.0;

      for( var y = 0; y < h; ++y ) {
        for( var x = 0; x < w; ++x ) {
          var inputOffset = y * w + x;
          var weightsOffset = packedOffset + classifierOffset + inputOffset + classifierInputOffset;

          var value = dataClassifierOutputWeights.elements.elements[ weightsOffset ];
          if( value > maxWeight ) {
            maxWeight = value;
          }
        }
      }

      var minWeight = 0.01;
      if( maxWeight < minWeight ) maxWeight = minWeight;
      // examine each input:
      for( var y = 0; y < h; ++y ) {
        for( var x = 0; x < w; ++x ) {
          var inputOffset = y * w + x;
          var weightsOffset = packedOffset + classifierOffset + inputOffset + classifierInputOffset;

          var value = dataClassifierOutputWeights.elements.elements[ weightsOffset ];
          value = value / maxWeight;
          if( value < minWeight ) { // if less than this frac of max weight, don't bother drawing
            continue;
          }

          ctx.fillStyle = "rgba(255,0,0,"+value.toFixed( 3 ) + ")";
          var cx = x * Region.pixelsPerBit;
          var cy = y * Region.pixelsPerBit;
       
          ctx.fillRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
        }
      }

    }

    for( var oy = 0; oy < oh; ++oy ) {
      for( var ox = 0; ox < ow; ++ox ) {
        var offset = oy * ow + ox;
        var mask = dataOrganizerOutputMask.elements.elements[ offset ];
        if( mask == 0 ) {
          continue; // dead, don't paint
        }

        var organizerDimensions = 2;
        var organizerInputs = 2;
        var organizerStride = organizerDimensions * organizerInputs;
        var organizerWeightsOffset = offset * organizerStride + inputIndex * organizerDimensions;

        var xWeight = dataOrganizerOutputWeights.elements.elements[ organizerWeightsOffset +0 ];
        var yWeight = dataOrganizerOutputWeights.elements.elements[ organizerWeightsOffset +1 ];

        var x = w * xWeight * Region.pixelsPerBit;
        var y = h * yWeight * Region.pixelsPerBit;

        ctx.strokeStyle = "rgb(0,255,0)";

        for( var i = 0; i < Region.selectedCells.length; ++i ) {
          var regionOffset = Region.selectedCells[ i ]; // a cell within the region     

          var rx = Math.floor( regionOffset % rw );
          var ry = Math.floor( regionOffset / rw );

          var oxSelected = Math.floor( rx / cw ); // coordinates of column in grid (region).
          var oySelected = Math.floor( ry / ch );    

          if( ( oxSelected == ox ) && ( oySelected == oy ) ) {
            ctx.strokeStyle = "rgb(255,255,0)";
            //console.log( "selected col is : " + ox + ", " + oy );

            break;
          }
        }

        ctx.beginPath();
        ctx.moveTo( x0 + x,                              y0 + y -Region.pixelsReceptiveField );
        ctx.lineTo( x0 + x,                              y0 + y +Region.pixelsReceptiveField );
        ctx.moveTo( x0 + x -Region.pixelsReceptiveField, y0 + y );
        ctx.lineTo( x0 + x +Region.pixelsReceptiveField, y0 + y );
        ctx.stroke();   
      }
    }

    // paint selected input bits
    ctx.strokeStyle = "#0000ff";
    for( var i = 0; i < selectedInput.length; ++i ) {
      var offset = selectedInput[ i ];
      var x = Math.floor( offset % w );
      var y = Math.floor( offset / w );
      var cx = x * Region.pixelsPerBit;
      var cy = y * Region.pixelsPerBit;
      
      ctx.strokeRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
    }
  },

  repaintRight2 : function() {
    var c = $( "#right-canvas" )[ 0 ];

    var data1 = Region.findData( "-input-ff-1" );
    if( !data1 ) {
      return; // can't paint
    }

    var dataSize1 = Framework.getDataSize( data1 );
    var w1 = dataSize1.w;
    var h1 = dataSize1.h;

    if( ( w1 == 0 ) || ( h1 == 0 ) ) {
      return;
    }

    var data2 = Region.findData( "-input-ff-2" );
    if( !data2 ) {
      return; // can't paint
    }

    var dataSize2 = Framework.getDataSize( data2 );
    var w2 = dataSize2.w;
    var h2 = dataSize2.h;

    if( ( w2 == 0 ) || ( h2 == 0 ) ) {
      return;
    }

    c.width  = w1 * Region.pixelsPerBit;
    c.height = h1 * Region.pixelsPerBit + Region.pixelsMargin + h2 * Region.pixelsPerBit;

    var ctx = c.getContext( "2d" );
    ctx.fillStyle = "#505050";
    ctx.fillRect( 0, 0, c.width, c.height );

    var dataWeights = Region.findData( "-classifier-cell-weights" );
    if( !dataWeights ) {
      return; // can't paint
    }

    var dataSizeW = Framework.getDataSize( dataWeights );
    var weightsSize = dataSizeW.w * dataSizeW.h;
    var weightsStride = w1 * h1 + w2 * h2;

    var x0 = 0;
    var y0 = 0;

    var inputOffset = 0;

    Region.paintInputData( ctx, x0, y0, w1, h1, data1, Region.selectedInput1 );
    Region.paintInputWeights( ctx, x0, y0, w1, h1, inputOffset, data1, weightsSize, weightsStride, dataWeights, Region.selectedCells );

    y0 = y0 + h1 * Region.pixelsPerBit + Region.pixelsMargin;
    inputOffset = w1 * h1;

    Region.paintInputData( ctx, x0, y0, w2, h2, data2, Region.selectedInput2 );
    Region.paintInputWeights( ctx, x0, y0, w2, h2, inputOffset, data2, weightsSize, weightsStride, dataWeights, Region.selectedCells );
  },

  paintInputWeights2 : function( ctx, x0, y0, w, h, inputOffset, dataInput, weightsSize, weightsStride, dataWeights, selectedElements ) {

    if( selectedElements.length < 1 ) {
      return;
    }

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var inputBit = y * w + x;

        var sumWeight = 0.0;

        for( var i = 0; i < selectedElements.length; ++i ) {
          var e = selectedElements[ i ];

          var weightsOffset = weightsStride * e 
                            + inputOffset 
                            + inputBit;

          var weight = dataWeights.elements.elements[ weightsOffset ];
          sumWeight += ( weight * 1.0 ); // * 1, which doesn't matter
        }
 
        // clip:
        sumWeight = Math.min( 1.0, sumWeight );
        sumWeight = Math.max( 0.0, sumWeight );

        var cx = x * Region.pixelsPerBit;
        var cy = y * Region.pixelsPerBit;

        ctx.fillStyle = "rgba(255,0,0,"+sumWeight+")";
        ctx.fillRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
      }
    }

  },

  paintInputData2 : function( ctx, x0, y0, w, h, dataInput, selectedElements ) {

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var cx = x * Region.pixelsPerBit;
        var cy = y * Region.pixelsPerBit;
        var offset = y * w + x;
        var value = dataInput.elements.elements[ offset ];
         
        ctx.fillStyle = "#000000";
        if( value > 0.5 ) {
          ctx.fillStyle = "#FFFFFF";
        }
        ctx.fillRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
        ctx.fill();

        ctx.strokeStyle = "#808080";

        for( var i = 0; i < selectedElements.length; ++i ) {
          if( selectedElements[ i ] == offset ) { // select one at a time
            ctx.strokeStyle = "#FFFFFF";
          }
        }

        ctx.strokeRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
      }
    }
  },

  findData : function( suffix ) {
    var dataName = Region.prefix + suffix;
    var data = Region.dataMap[ dataName ];
    return data;
  },

  onGotData : function() {
    Region.setStatus( "Getting config... " );
    var entity = $( "#entity" ).val();
    Framework.getConfig( entity, Region.onGetRegionConfig );
  },

  onGetRegionConfig : function( json ) {
    var entityConfig = JSON.parse( json.responseText );
    Region.config = entityConfig.value;
    Region.setStatus( "Repainting... " );
    Region.repaint();    
    Region.setStatus( "Ready." );
  },

  getData : function() {

    if( Region.regionSuffixIdx >= Region.regionSuffixes.length ) {
      Region.onGotData();
      return;
    }

    var dataName = Region.prefix + Region.regionSuffixes[ Region.regionSuffixIdx ];

    Region.setStatus( "Getting " + Region.regionSuffixIdx + " of " + Region.regionSuffixes.length + ", name: " + dataName );
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Region.regionSuffixIdx = Region.regionSuffixIdx +1; // get next
    Framework.getData( dataName, Region.onGetData );
  },

  onGetData : function( json ) {
    if( json.status != 200 ) {
      Region.setStatus( "Error getting data" );
      return;
    }

    var datas = JSON.parse( json.responseText );

    if( datas.length > 1 ) {
      Region.getData(); // get next data.
      return;
    }

    var data = datas[ 0 ];

    Framework.decode( data );

    Region.dataMap[ data.name ] = data;
    Region.getData(); // get next data.
  },

  resizeDataArea : function() {
    var dataElement = $( "#region-data" )[ 0 ];
    var infoElement = $( "#region-info" )[ 0 ];
    var infoArea = infoElement.getBoundingClientRect();
    var height = window.innerHeight - infoArea.height -1;// -getScrollbarWidth() -1;
    $( ".column" ).css( "height", height );
  },

  update : function() {
    var entity = $( "#root-entity" ).val();
    console.log( "Updating " + entity );

    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.update( entity, Region.onUpdate );
  },

  onUpdate : function() {
    // TODO: Poll for changes then 
  },

  refresh : function() {

    var pxPerBit = $( "#size" ).val();
    Region.pixelsPerBit = pxPerBit;

    var entity = $( "#entity" ).val();
    console.log( "Repainting " + entity );

    Region.resizeDataArea();

    Region.setStatus( "Refreshing..." );
    Region.dataMap = {};
    Region.regionSuffixIdx = 0;
    Region.prefix = entity;
    Region.getData();    
  },

  onParameter : function( key, value ) {
    if( key == "entity" ) {
      $("#entity").val( value ); 
    }
    if( key == "root-entity" ) {
      $("#root-entity").val( value ); 
    }
  },

  setup : function() {
    Parameters.extract( Region.onParameter );

    $( "#left-canvas" )[ 0 ].addEventListener( 'mousemove', function( e ) {
      Region.onMouseMoveLeft( e, e.offsetX, e.offsetY );
    } );
    $( "#centre-canvas" )[ 0 ].addEventListener( 'mousemove', function( e ) {
      Region.onMouseMoveCentre( e, e.offsetX, e.offsetY );
    } );
    $( "#right-canvas" )[ 0 ].addEventListener( 'mousemove', function( e ) {
      Region.onMouseMoveRight( e, e.offsetX, e.offsetY );
    } );

    $( "#left-canvas" )[ 0 ].addEventListener( 'click', function( e ) {
      Region.onMouseClickLeft( e, e.offsetX, e.offsetY );
    } );
    $( "#centre-canvas" )[ 0 ].addEventListener( 'click', function( e ) {
      Region.onMouseClickCentre( e, e.offsetX, e.offsetY );
    } );
    $( "#right-canvas" )[ 0 ].addEventListener( 'click', function( e ) {
      Region.onMouseClickRight( e, e.offsetX, e.offsetY );
    } );
  }

};

$( document ).ready( function() {
  Region.setup();
} );


