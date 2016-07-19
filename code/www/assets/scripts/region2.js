
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

  regionSuffixes : [ "-input-1", "-input-2", "-context-free-response", "-context-free-weights", "-context-free-biases-2", "-context-free-activity-new", "-context-free-activity-old", "-prediction-fp", "-prediction-fn", "-prediction-old", "-prediction-new-real", "-prediction-new", "-output", "-output-age" ],
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
    Region.selectCells( "-context-free-activity-old" );
  },

  selectActive : function() {
    Region.selectCells( "-context-free-activity-new" );
  },
  selectPredicted : function() {
    Region.selectCells( "-prediction-new" );
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
/*    Region.selectedInput1 = [];
    Region.selectedInput2 = [];

    var data1 = Region.findData( "-ff-input-1" );
    if( !data1 ) {
      return; // can't paint
    }

    var dataSize1 = Framework.getDataSize( data1 );
    var w1 = dataSize1.w;
    var h1 = dataSize1.h;

    if( ( w1 == 0 ) || ( h1 == 0 ) ) {
      return;
    }

    var data2 = Region.findData( "-ff-input-2" );
    if( !data2 ) {
      return; // can't paint
    }

    var dataSize2 = Framework.getDataSize( data2 );
    var w2 = dataSize2.w;
    var h2 = dataSize2.h;

    if( ( w2 == 0 ) || ( h2 == 0 ) ) {
      return;
    }

    var dataActivityNew = Region.findData( "-activity-new" );
    if( !dataActivityNew ) {
      return; // can't paint
    }

    var dataOrganizerOutputMask = Region.findData( "-organizer-output-mask" );
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
    var cd = Region.config.classifierDepthCells;

    var classifierInputStride = w1 * h1 + w2 * h2;
    var classifierInputOffset = 0;

    Region.selectThresholdForInput( 0, Region.selectedInput1, w1, h1, rw, rh, cw, ch, cd, ow, oh, classifierInputStride, classifierInputOffset );

    classifierInputOffset = w1 * h1;

    Region.selectThresholdForInput( 1, Region.selectedInput2, w2, h2, rw, rh, cw, ch, cd, ow, oh, classifierInputStride, classifierInputOffset );

    Region.updateSelection( "sel-input-1", Region.selectedInput1 );
    Region.updateSelection( "sel-input-2", Region.selectedInput2 );*/
    Region.repaint();
  },

  selectThresholdForInput : function( inputIndex, selectedInput, w, h, rw, rh, cw, ch, cd, ow, oh, classifierInputStride, classifierInputOffset ) {
/*    var threshold = $( "#threshold" ).val();

    var dataClassifierOutputWeights = Region.findData( "-classifier-output-weights" );

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

      var classifierHeight = ( ch / cd );
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

    }    */
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

    var dataNew = Region.findData( "-context-free-activity-new" );
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
    var data1 = Region.findData( "-input-1" );
    if( !data1 ) {
      return; // can't paint
    }

    var data2 = Region.findData( "-input-2" );
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
    Region.repaintContextual();
    Region.repaintContextFree();
    Region.repaintInput();
  },

  repaintContextFree : function() {
    var dataNew = Region.findData( "-context-free-activity-new" );
    if( !dataNew ) {
      return; // can't paint
    }

    var dataOld = Region.findData( "-context-free-activity-old" );
    if( !dataOld ) {
      return; // can't paint
    }

    var canvasDataSize = Region.resizeCanvas( "#centre-canvas", dataNew, 1, 1 );
    if( !canvasDataSize ) {
      return; // can't paint
    }

    var x0 = 0;
    var y0 = 0;

    Region.fillDataRgbBinary( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, dataNew, null, dataOld );//, null );
    Region.strokeElements( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, Region.selectedCells, "#00ffff" );
  },

  repaintContextual : function() {
    var c = $( "#left-canvas" )[ 0 ];

    var dataNew = Region.findData( "-context-free-activity-new" );
    if( !dataNew ) {
      return; // can't paint
    }

    var dataFp = Region.findData( "-prediction-fp" );
    if( !dataFp ) {
      return; // can't paint
    }

    var dataFn = Region.findData( "-prediction-fn" );
    if( !dataFn ) {
      return; // can't paint
    }

    var dataPredictionOld = Region.findData( "-prediction-old" );
    if( !dataPredictionOld ) {
      return; // can't paint
    }

    var dataPredictionNew = Region.findData( "-prediction-new" );
    if( !dataPredictionNew ) {
      return; // can't paint
    }

    var dataPredictionNewReal = Region.findData( "-prediction-new-real" );
    if( !dataPredictionNewReal ) {
      return; // can't paint
    }

    var dataOutput = Region.findData( "-output" );
    if( !dataOutput ) {
      return; // can't paint
    }

    var dataOutputAge = Region.findData( "-output-age" );
    if( !dataOutputAge ) {
      return; // can't paint
    }

    var canvasDataSize = Region.resizeCanvas( "#left-canvas", dataNew, 1, 3 );
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
    Region.fillDataRgbBinary( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, dataPredictionOld, dataNew, null );//, null );
    Region.strokeDataBinary( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, dataPredictionNew, "#0000ff", true );

    y0 += ( Region.pixelsPerBit * canvasDataSize.h ); 
    y0 += ( Region.pixelsPerGap );
 
    Region.fillDataRgba( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, dataPredictionNewReal, 255,0,0, false );
    Region.strokeDataBinary( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, dataPredictionNew, "#0000ff", true );

    y0 += ( Region.pixelsPerBit * canvasDataSize.h ); 
    y0 += ( Region.pixelsPerGap );

    Region.fillDataRgba( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, dataOutputAge, 255,0,0, true );
    Region.strokeDataBinary( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, dataOutput, "#00ffff", true );
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

    var maxValue = 0.01;

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

  repaintInput : function() {
    var c = $( "#right-canvas" )[ 0 ];

    var data1 = Region.findData( "-input-1" );
    if( !data1 ) {
      return; // can't paint
    }

    var dataSize1 = Framework.getDataSize( data1 );
    var w1 = dataSize1.w;
    var h1 = dataSize1.h;

    if( ( w1 == 0 ) || ( h1 == 0 ) ) {
      return;
    }

    var data2 = Region.findData( "-input-2" );
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

    var dataWeights = Region.findData( "-context-free-weights" );
    if( !dataWeights ) {
      return; // can't paint
    }

    var dataBiases = Region.findData( "-context-free-biases-2" );
    if( !dataBiases ) {
      return; // can't paint
    }

    var dataResponse = Region.findData( "-context-free-response" );
    if( !dataResponse ) {
      return; // can't paint
    }

    var gain = $( "#gain" ).val();

    var dataSizeW = Framework.getDataSize( dataWeights );
    var weightsSize = dataSizeW.w * dataSizeW.h;
    var weightsStride = w1 * h1 + w2 * h2;

    var x0 = 0;
    var y0 = 0;

    var inputOffset = 0;

    Region.paintInputData( ctx, x0, y0, w1, h1, data1, Region.selectedInput1 );
    Region.paintInputWeights( ctx, x0, y0, w1, h1, inputOffset, data1, weightsSize, weightsStride, dataWeights, dataBiases, dataResponse, Region.selectedCells, gain );

    y0 = y0 + h1 * Region.pixelsPerBit + Region.pixelsMargin;
    inputOffset = w1 * h1;

    Region.paintInputData( ctx, x0, y0, w2, h2, data2, Region.selectedInput2 );
    Region.paintInputWeights( ctx, x0, y0, w2, h2, inputOffset, data2, weightsSize, weightsStride, dataWeights, dataBiases, dataResponse, Region.selectedCells, gain );

  },

  paintInputWeights : function( ctx, x0, y0, w, h, inputOffset, dataInput, weightsSize, weightsStride, dataWeights, dataBiases, dataResponse, selectedElements, gain ) {

    if( selectedElements.length < 1 ) {
      return;
    }

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var inputBit = y * w + x;

        var sumWeight = 0.0;

        for( var i = 0; i < selectedElements.length; ++i ) {
          var e = selectedElements[ i ];

          //var r = dataResponse.elements.elements[ e ];
	  var r = gain;
//          if( e <= 0.0 ) {
//            r = 0.0;
//          }

          var weightsOffset = weightsStride * e 
                            + inputOffset 
                            + inputBit;//*/

/*          var weightsOffset = weightsStride * e 
                            + inputOffset 
                            + inputBit;//*/
//          var weightsOffset = ( inputOffset + inputBit ) * weightsSize + e;

          var weight = dataWeights.elements.elements[ weightsOffset ];
//          if( weight > 0.0 ) {
////            sumMaxWeight += weight;//Math.min( minWeight, weight );
//console.log( "weight: " + weight );
//          }
//          else {
//           sumMinWeight += Math.abs( weight );//Math.max( maxWeight, weight );
//          }
          sumWeight += ( weight * r ); // * 1, which doesn't matter
  
//          minWeight = Math.min( weight, minWeight );
//          maxWeight = Math.max( weight, maxWeight );
        }
 
        var biasesOffset = inputOffset + inputBit;
        var bias = dataBiases.elements.elements[ biasesOffset ];
        sumWeight += bias;      
//        minWeight = minWeight / minWeightGlobal;        
//        maxWeight = maxWeight / maxWeightGlobal;        
//        var meanWeight = sumWeight / selectedElements.length;
//        var maxWeight = maxWeight / weightScale;
//        var minWeight = minWeight / weightScale;

//sumWeight = sumWeight * 0.5;

        sumWeight = Math.min(  1.0, sumWeight );
        sumWeight = Math.max( -1.0, sumWeight );

        var cx = x * Region.pixelsPerBit;
        var cy = y * Region.pixelsPerBit;

        if( sumWeight > 0.0 ) {
          ctx.fillStyle = "rgba(255,0,0,"+sumWeight+")";
          ctx.fillRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
        }
        else {
          ctx.fillStyle = "rgba(0,0,255,"+Math.abs( sumWeight )+")";
        }

        ctx.fillRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
        ctx.fill();
      }
    }

/*    var minWeightGlobal = 0.0;
    var maxWeightGlobal = 0.0;
//var maxWeightAt = 0;

    for( var j = 0; j < selectedElements.length; ++j ) {
//    for( var e = 0; e < weightsSize; ++e ) {
      var e = selectedElements[ j ];

      for( var i = 0; i < weightsStride; ++i ) {
        var weightsOffset = weightsStride * e + i;
        var weight = dataWeights.elements.elements[ weightsOffset ];
//if( weight > maxWeightGlobal ) maxWeightAt = e;
        minWeightGlobal = Math.min( minWeightGlobal, weight );
        maxWeightGlobal = Math.max( maxWeightGlobal, weight );
      }
    }

    minWeightLimit = 0.01;

    var weightScale = Math.max( minWeightLimit, maxWeightGlobal );
        weightScale = Math.max( weightScale,   -minWeightGlobal );

//    if( minWeightGlobal > -minWeightLimit ) minWeightGlobal = -minWeightLimit;
//    if( maxWeightGlobal <  minWeightLimit ) maxWeightGlobal =  minWeightLimit;

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var inputBit = y * w + x;

        var cx = x * Region.pixelsPerBit;
        var cy = y * Region.pixelsPerBit;
        
//        var sumMinWeight = 0.0;
//        var sumMaxWeight = 0.0;
        var maxWeight = 0.0;
        var minWeight = 0.0;

        for( var i = 0; i < selectedElements.length; ++i ) {
          var e = selectedElements[ i ];
          var weightsOffset = weightsStride * e 
                            + inputOffset 
                            + inputBit;
//          var weightsOffset = inputBit * weightsSize + e;

          var weight = dataWeights.elements.elements[ weightsOffset ];
//          if( weight > 0.0 ) {
////            sumMaxWeight += weight;//Math.min( minWeight, weight );
//console.log( "weight: " + weight );
//          }
//          else {
//           sumMinWeight += Math.abs( weight );//Math.max( maxWeight, weight );
//          }
//          sumWeight += weight;
  
          minWeight = Math.min( weight, minWeight );
          maxWeight = Math.max( weight, maxWeight );
        }

//        minWeight = minWeight / minWeightGlobal;        
//        maxWeight = maxWeight / maxWeightGlobal;        
//        var meanWeight = sumWeight / selectedElements.length;
        var maxWeight = maxWeight / weightScale;
        var minWeight = minWeight / weightScale;

//        if( unitWeight > 0.0 ) {
          ctx.fillStyle = "rgba(255,0,0,"+maxWeight+")";
          ctx.fillRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
//        }
//        else {
          ctx.fillStyle = "rgba(0,0,255,"+Math.abs( minWeight )+")";
//        }

        ctx.fillRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
        ctx.fill();
      }
    }*/
  },

  paintInputData : function( ctx, x0, y0, w, h, dataInput, selectedElements ) {

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


