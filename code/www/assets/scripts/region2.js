
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
  prefix : "",

  selectedContextFreeCells : [  ],
  selectedContextualCells : [  ],
  selectedInput1 : [  ],
  selectedInput2 : [  ],

  regionSuffixes : [ "-input-1", "-input-2", "-context-free-activity-new", "-context-free-activity-old", "-contextual-activity-new", "-contextual-activity-old", "-contextual-activity-fp", "-contextual-activity-fn", "-contextual-prediction-old", "-contextual-prediction-new", "-contextual-output", "-contextual-output-age" ],
  regionSuffixIdx : 0,
  dataMap : {
  },

  setStatus : function( status ) {
    $( "#status" ).html( status );
  },

  selectText : function() {
/*    var value = $( "#sel-cells" ).val();
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

    Region.updateSelection( "sel-cells", Region.selectedCells );*/
    Region.repaint();
  },
  selectActive : function() {
/*    var dataActivityNew = Region.findData( "-activity-new" );
    if( !dataActivityNew ) {
      return; // can't paint
    }
    var dataActivityOld = Region.findData( "-activity-old" );
    if( !dataActivityOld ) {
      return; // can't paint
    }

    Region.selectedCells = [];
    for( var i = 0; i < dataActivityNew.elements.elements.length; ++i ) {
      var valueNew = dataActivityNew.elements.elements[ i ];
      var valueOld = dataActivityOld.elements.elements[ i ];
      if( ( valueNew > 0.5 ) && ( valueOld < 0.5 ) ) {
        Region.selectedCells.push( i );
      }
    }

    Region.updateSelection( "sel-cells", Region.selectedCells );*/
    Region.repaint();
  },
  selectPredicted : function() {
/*    var dataPredictionNew = Region.findData( "-prediction-new" );
    if( !dataPredictionNew ) {
      return; // can't paint
    }

    Region.selectedCells = [];
    for( var i = 0; i < dataPredictionNew.elements.elements.length; ++i ) {
      var valueNew = dataPredictionNew.elements.elements[ i ];
      if( ( valueNew > 0.5 ) ) {
        Region.selectedCells.push( i );
      }
    }

    Region.updateSelection( "sel-cells", Region.selectedCells );*/
    Region.repaint();
  },

  toggleSelectCell : function( offset ) {
//    Region.toggleSelection( Region.selectedCells, offset );
//    Region.updateSelection( "sel-cells", Region.selectedCells );
  },
  toggleSelectInput1 : function( offset ) {
//    Region.toggleSelection( Region.selectedInput1, offset );
//    Region.updateSelection( "sel-input-1", Region.selectedInput1 );
  },
  toggleSelectInput2 : function( offset ) {
//    Region.toggleSelection( Region.selectedInput2, offset );
    Region.updateSelection( "sel-input-2", Region.selectedInput2 );
  },

  selectThreshold : function() {
    Region.selectedInput1 = [];
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
    Region.updateSelection( "sel-input-2", Region.selectedInput2 );
    Region.repaint();
  },

  selectThresholdForInput : function( inputIndex, selectedInput, w, h, rw, rh, cw, ch, cd, ow, oh, classifierInputStride, classifierInputOffset ) {
    var threshold = $( "#threshold" ).val();

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
    $( "#mouse-left" ).html( status );
  },
  setMouseRight : function( status ) {
    $( "#mouse-right" ).html( status );
  },

  onMouseMoveLeft : function( e, mx, my ) {
  },
  onMouseMoveRight : function( e, mx, my ) {
  },

  onMouseClickLeft : function( e, mx, my ) {
    var dataActivityNew = Region.findData( "-activity-new" );
    if( !dataActivityNew ) {
      return; // can't paint
    }

    var dataOrganizerOutputMask = Region.findData( "-organizer-output-mask" );
    if( !dataOrganizerOutputMask ) {
      return; // can't paint
    }

    // rw = RegionWidth
    // ow = OrganizerWidth
    // cw = Column or Classifier Width
    var regionDataSize = Framework.getDataSize( dataActivityNew );
    var rw = regionDataSize.w;
    var rh = regionDataSize.h;

    var organizerDataSize = Framework.getDataSize( dataOrganizerOutputMask );
    var ow = organizerDataSize.w;
    var oh = organizerDataSize.h;

    // computer classifier size (column size)
    if( ( rw == 0 ) || ( rh == 0 ) ) {
      return;
    }

    if( ( ow == 0 ) || ( oh == 0 ) ) {
      return;
    }
    
    var cw = rw / ow;
    var ch = rh / oh; // this is column not classifier

    var ox = Math.floor( mx / ( cw * Region.pixelsPerBit + Region.pixelsColumnGap ) );
    var oy = Math.floor( my / ( ch * Region.pixelsPerBit + Region.pixelsColumnGap ) );

    var cx = mx - ( ox * ( cw * Region.pixelsPerBit + Region.pixelsColumnGap ) );
    var cy = my - ( oy * ( ch * Region.pixelsPerBit + Region.pixelsColumnGap ) );

    cx = Math.floor( cx / Region.pixelsPerBit );    
    cy = Math.floor( cy / Region.pixelsPerBit );    

    if( ( cx < 0 ) || ( cy < 0 ) || ( cx >= cw ) || ( cy >= ch ) ) {
      return; // may be in the gap
    }

    var rx = ox * cw + cx;
    var ry = oy * ch + cy;

    var offset = ry * rw + rx;

    Region.setMouseLeft( "Column: [" + ox + ", " + oy + "] Cell: [" + cx + ", " + cy + "] Region: [" + rx + ", " + ry + "]" );
    Region.toggleSelectCell( offset );
    Region.repaint();
  },

  onMouseClickRight : function( e, mx, my ) {
    var data1 = Region.findData( "-ff-input-1" );
    if( !data1 ) {
      return; // can't paint
    }

    var data2 = Region.findData( "-ff-input-2" );
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
    console.log( "repaint cells" );
    Region.repaintCells();
    console.log( "repaint input" );
    Region.repaintInput();
  },

  repaintCells : function() {
    var dataActivityNew = Region.findData( "-activity-new" );
    if( !dataActivityNew ) {
      return; // can't paint
    }

    var dataActivityOld = Region.findData( "-activity-old" );
    if( !dataActivityOld ) {
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

    var dataOrganizerOutputMask = Region.findData( "-organizer-output-mask" );
    if( !dataOrganizerOutputMask ) {
      return; // can't paint
    }

    // rw = RegionWidth
    // ow = OrganizerWidth
    // cw = Column or Classifier Width
    var regionDataSize = Framework.getDataSize( dataActivityNew );
    var rw = regionDataSize.w;
    var rh = regionDataSize.h;

    var organizerDataSize = Framework.getDataSize( dataOrganizerOutputMask );
    var ow = organizerDataSize.w;
    var oh = organizerDataSize.h;

    // computer classifier size (column size)
    if( ( rw == 0 ) || ( rh == 0 ) ) {
      return;
    }

    if( ( ow == 0 ) || ( oh == 0 ) ) {
      return;
    }
    
    var cw = rw / ow;
    var ch = rh / oh;
    var cd = Region.config.classifierDepthCells;
    var classifierHeight = ( ch / cd );

    // draw the data
    var half = Region.pixelsPerBit * 0.5;
    var c = $( "#left-canvas" )[ 0 ];
    c.width  = rw * Region.pixelsPerBit + (ow-1) * Region.pixelsColumnGap;
    c.height = rh * Region.pixelsPerBit + (oh-1) * Region.pixelsColumnGap;

    var ctx = c.getContext( "2d" );
    ctx.fillStyle = "#808080";
    ctx.fillRect( 0, 0, c.width, c.height );

    for( var oy = 0; oy < oh; ++oy ) {
      for( var ox = 0; ox < ow; ++ox ) {

        // now mark dead cells so we can ignore them
//        var dataName = Region.getClassifierDataName( ox, oy, "-output-mask" );
//        var dataClassifierOutputMask = Region.findData( dataName );
        var dataClassifierOutputMask = Region.findData( "-classifier-output-mask" );
        if( !dataClassifierOutputMask ) {
          continue;
        }

        var classifierSize = cw * ( ch / cd ); // Each classifier has this number of cells. 
        var classifierIndex = oy * ow + ox;
        var packedOffset = classifierIndex * classifierSize;

        var classifierUnchanged = false;

        for( var cy = 0; cy < ch; ++cy ) {
          for( var cx = 0; cx < cw; ++cx ) {
            var rx = cx + ox * cw;
            var ry = cy + oy * ch;

            var offset = ry * rw + rx;
            var activityNew   = dataActivityNew  .elements.elements[ offset ];
            var activityOld   = dataActivityOld  .elements.elements[ offset ];

            if( ( activityNew > 0.5 ) && ( activityOld > 0.5 ) ) {
              classifierUnchanged = true;
              cx = cw;
              cy = ch; // break loops
            }
          }
        }

        for( var cy = 0; cy < ch; ++cy ) {
          for( var cx = 0; cx < cw; ++cx ) {

            var x = cx * Region.pixelsPerBit + ox * cw * Region.pixelsPerBit + (ox) * Region.pixelsColumnGap;
            var y = cy * Region.pixelsPerBit + oy * ch * Region.pixelsPerBit + (oy) * Region.pixelsColumnGap;

            var rx = cx + ox * cw;
            var ry = cy + oy * ch;

            var offset = ry * rw + rx;
            var activityNew   = dataActivityNew  .elements.elements[ offset ];
            var activityOld   = dataActivityOld  .elements.elements[ offset ];
            var predictionOld = dataPredictionOld.elements.elements[ offset ];
            var predictionNew = dataPredictionNew.elements.elements[ offset ];
        
            if( activityNew > 0.5 ) {
              if( classifierUnchanged ) {
                ctx.fillStyle = "#444400";
              }
              else if( predictionOld > 0.5 ) {
                ctx.fillStyle = "#00FF00";
              }
              else { // active, unpredicted
                ctx.fillStyle = "#FF0000";
              }
            }
            else { // inactive now
              if( predictionOld > 0.5 ) {
                if( classifierUnchanged ) {
                  ctx.fillStyle = "#440044";
                }
                else {
                  ctx.fillStyle = "#FF00FF";
                }
              }
              else { // inactive, unpredicted
                ctx.fillStyle = "#000000";
              }
            }

            ctx.fillRect( x, y, Region.pixelsPerBit, Region.pixelsPerBit );        
            ctx.fill();

            ctx.strokeStyle = "#808080";
            ctx.strokeRect( x, y, Region.pixelsPerBit, Region.pixelsPerBit );        

//            offset = packedOffset + ( cy * cw + cx );
//            var classifierY = cy - ( cd * Math.floor( cy / cd ) );
            var classifierY = cy - ( classifierHeight * Math.floor( cy / classifierHeight ) );
            offset = packedOffset + ( ( classifierY * cw ) + cx );
   
            var maskValue = dataClassifierOutputMask.elements.elements[ offset ];
            if( maskValue < 0.5 ) {
              ctx.strokeRect( x, y, half, half );        
//              ctx.moveTo( x, y ); KILLS chrome renderer, insanely slow.
//              ctx.lineTo( x + Region.pixelsPerBit, y + Region.pixelsPerBit );        
            }

            if( predictionNew > 0.5 ) {
              ctx.strokeStyle = "#FFFF00";
              ctx.strokeRect( x+half, y, half, half );        
            }

          }
        }
      }

      // paint depth boundaries
      ctx.strokeStyle = "#0000AA";
      var w = ow * cw * Region.pixelsPerBit + (ow) * Region.pixelsColumnGap;
      for( var d = 1; d < cd; ++d ) {
        var yd = d * classifierHeight * Region.pixelsPerBit + oy * ch * Region.pixelsPerBit + (oy) * Region.pixelsColumnGap;
        ctx.beginPath();
        ctx.moveTo( 0, yd );
        ctx.lineTo( w, yd );
        ctx.stroke();
      }

    }

    // mark dead columns (classifiers)
    var w = Region.pixelsPerBit * cw + Region.pixelsColumnGap;
    var h = Region.pixelsPerBit * ch + Region.pixelsColumnGap;
    ctx.fillStyle = "rgba( 255,255,0, 0.5 )";

    for( var oy = 0; oy < oh; ++oy ) {
      for( var ox = 0; ox < ow; ++ox ) {
        var offset = oy * ow + ox;
        var mask = dataOrganizerOutputMask.elements.elements[ offset ];
        if( mask != 0 ) {
          continue; // live, don't paint
        }

        var x = ox * w;
        var y = oy * h;

        ctx.fillRect( x, y, w, h );        
      }
    }

    // paint selections
    ctx.strokeStyle = "#FFFF00";
    for( var i = 0; i < Region.selectedCells.length; ++i ) {
      var offset = Region.selectedCells[ i ];
      var rx = Math.floor( offset % rw );
      var ry = Math.floor( offset / rw );

      var ox = Math.floor( rx / cw );    
      var oy = Math.floor( ry / ch );    
      var cx = rx - ( ox * cw );    
      var cy = ry - ( oy * ch );    

      var x = cx * Region.pixelsPerBit + ox * cw * Region.pixelsPerBit + (ox) * Region.pixelsColumnGap;
      var y = cy * Region.pixelsPerBit + oy * ch * Region.pixelsPerBit + (oy) * Region.pixelsColumnGap;
      
      ctx.strokeRect( x, y, Region.pixelsPerBit, Region.pixelsPerBit );        
    }

  },

  repaintInput : function() {
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

    var dataOrganizerOutputWeights = Region.findData( "-organizer-output-weights" );
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

    Region.paintInputData( ctx, x0, y0, w1, h1, 0, data1, dataOrganizerOutputMask, dataOrganizerOutputWeights, ow, oh, rw, rh, cw, ch, cd, Region.selectedInput1, Region.selectedCells, classifierInputStride, classifierInputOffset );

    y0 = y0 + h1 * Region.pixelsPerBit + Region.pixelsMargin;
    classifierInputOffset = w1 * h1;

    Region.paintInputData( ctx, x0, y0, w2, h2, 1, data2, dataOrganizerOutputMask, dataOrganizerOutputWeights, ow, oh, rw, rh, cw, ch, cd, Region.selectedInput2, Region.selectedCells, classifierInputStride, classifierInputOffset );
  },

  paintInputData : function( ctx, x0, y0, w, h, inputIndex, dataInput, dataOrganizerOutputMask, dataOrganizerOutputWeights, ow, oh, rw, rh, cw, ch, cd, selectedInput, selectedCells, classifierInputStride, classifierInputOffset ) {

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

      var dataClassifierOutputWeights = Region.findData( "-classifier-output-weights" );
      if( !dataClassifierOutputWeights ) {
        continue; // can't paint
      }

      var cx = rx - ( ox * cw ); // coordinates in column.
      var cy = ry - ( oy * ch );    

      var classifierHeight = ( ch / cd );
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

  findData : function( suffix ) {
    var dataName = Region.prefix + suffix;
    var data = Region.dataMap[ dataName ];
    return data;
  },

  onGotData : function() {

    Region.setStatus( "Getting config... " );
    var entity = $( "#entity" ).val();
    Framework.getConfig( entity, Region.onGetRegionConfig );

//    Region.setStatus( "Got all region data, getting classifier data... " );
//    //Region.repaint();    
//    Region.getClassifierData();
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
    var height = window.innerHeight - infoArea.height -getScrollbarWidth() -1;
    $( "#region-data-left" ).css( "height", height );
    $( "#region-data-right" ).css( "height", height );
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
    $( "#right-canvas" )[ 0 ].addEventListener( 'mousemove', function( e ) {
      Region.onMouseMoveRight( e, e.offsetX, e.offsetY );
    } );

    $( "#left-canvas" )[ 0 ].addEventListener( 'click', function( e ) {
      Region.onMouseClickLeft( e, e.offsetX, e.offsetY );
//      Region.onMouseClickLeft( e, e.clientX, e.clientY );
    } );
    $( "#right-canvas" )[ 0 ].addEventListener( 'click', function( e ) {
      Region.onMouseClickRight( e, e.offsetX, e.offsetY );
//      Region.onMouseClickRight( e, e.clientX, e.clientY );
    } );
  }

};

$( document ).ready( function() {
  Region.setup();
} );


