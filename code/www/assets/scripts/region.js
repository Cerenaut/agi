
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

  selectedLeft : [  ],
  selectedRight : [  ],

  classifierSuffixes : [ "-output-weights", "-output-mask" ],
//  classifierSuffixes : [ "-output-mask" ],
  classifierDataCount : 0,
  classifierDataRequests : 0,

  regionSuffixes : [ "-activity-new", "-prediction-old", "-prediction-new", "-ff-input", "-organizer-output-mask", "-organizer-output-weights" ],
  regionSuffixIdx : 0,
  dataMap : {
  },

  setStatus : function( status ) {
    $( "#status" ).html( status );
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
    var ch = rh / oh;

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

    Region.toggleSelection( Region.selectedLeft, offset );
    Region.repaint();
  },

  onMouseClickRight : function( e, mx, my ) {
    var data = Region.findData( "-ff-input" );
    if( !data ) {
      return; // can't paint
    }

    var dataSize = Framework.getDataSize( data );
    var iw = dataSize.w;
    var ih = dataSize.h;

    if( ( iw == 0 ) || ( ih == 0 ) ) {
      return;
    }

    var ix = Math.floor( mx / Region.pixelsPerBit );
    var iy = Math.floor( my / Region.pixelsPerBit );

    if( ( ix < 0 ) || ( iy < 0 ) || ( ix >= iw ) || ( iy >= ih ) ) {
      return;
    }

    var offset = iy * iw + ix;

    Region.toggleSelection( Region.selectedRight, offset );
    Region.repaint();
  },

  toggleSelection : function( list, item ) {
    var found = false;

    for( var i = 0; i < list.length; ++i ) {
      var selected = list[ i ];
      if( selected == item ) { 
        // remove selection
        found = true;
        list.splice( i );
        break;
      }
    }
 
    if( !found ) {
      list.push( item );
    }
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
            var dataName = Region.getClassifierDataName( ox, oy, "-output-mask" );
            var dataClassifierOutputMask = Region.findData( dataName );
            if( !dataClassifierOutputMask ) {
              continue;
            }

        for( var cy = 0; cy < ch; ++cy ) {
          for( var cx = 0; cx < cw; ++cx ) {

            var x = cx * Region.pixelsPerBit + ox * cw * Region.pixelsPerBit + (ox) * Region.pixelsColumnGap;
            var y = cy * Region.pixelsPerBit + oy * ch * Region.pixelsPerBit + (oy) * Region.pixelsColumnGap;

            var rx = cx + ox * cw;
            var ry = cy + oy * ch;

            var offset = ry * rw + rx;
            var activityNew   = dataActivityNew  .elements.elements[ offset ];
            var predictionOld = dataPredictionOld.elements.elements[ offset ];
            var predictionNew = dataPredictionNew.elements.elements[ offset ];
        
            if( activityNew > 0.5 ) {
              if( predictionOld > 0.5 ) {
                ctx.fillStyle = "#00FF00";
              }
              else { // active, unpredicted
                ctx.fillStyle = "#FF0000";
              }
            }
            else { // inactive now
              if( predictionOld > 0.5 ) {
                ctx.fillStyle = "#FF00FF";
              }
              else { // inactive, unpredicted
                ctx.fillStyle = "#000000";
              }
            }

            ctx.fillRect( x, y, Region.pixelsPerBit, Region.pixelsPerBit );        
            ctx.fill();

            ctx.strokeStyle = "#808080";
            ctx.strokeRect( x, y, Region.pixelsPerBit, Region.pixelsPerBit );        

            offset = cy * cw + cx;
   
            var maskValue = dataClassifierOutputMask.elements.elements[ offset ];
            if( maskValue < 0.5 ) {
              ctx.strokeRect( x, y, half, half );        
//              ctx.moveTo( x, y ); KILLS chrome renderer, insanely slow.
//              ctx.lineTo( x + Region.pixelsPerBit, y + Region.pixelsPerBit );        
            }

          }
        }
      }
    }

    // mark dead columns (classifiers)
    var w = Region.pixelsPerBit * cw + Region.pixelsColumnGap;
    var h = Region.pixelsPerBit * cw + Region.pixelsColumnGap;
    ctx.fillStyle = "rgba( 100,100,100, 0.5 )";

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
    ctx.strokeStyle = "#FFFFFF";
    for( var i = 0; i < Region.selectedLeft.length; ++i ) {
      var offset = Region.selectedLeft[ i ];
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
    var data = Region.findData( "-ff-input" );
    if( !data ) {
      return; // can't paint
    }

    var dataSize = Framework.getDataSize( data );
    var w = dataSize.w;
    var h = dataSize.h;

    if( ( w == 0 ) || ( h == 0 ) ) {
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

    var dataOrganizerOutputWeights = Region.findData( "-organizer-output-weights" );
    if( !dataOrganizerOutputWeights ) {
      return; // can't paint
    }

    var c = $( "#right-canvas" )[ 0 ];
    c.width = w * Region.pixelsPerBit;
    c.height = h * Region.pixelsPerBit;

    var ctx = c.getContext( "2d" );
    ctx.fillStyle = "#808080";
    ctx.fillRect( 0, 0, c.width, c.height );

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var cx = x * Region.pixelsPerBit;
        var cy = y * Region.pixelsPerBit;
        var offset = y * w + x;
        var value = data.elements.elements[ offset ];
         
        ctx.fillStyle = "#000000";
        if( value > 0.5 ) {
          ctx.fillStyle = "#FFFF00";
        }
        ctx.fillRect( cx, cy, Region.pixelsPerBit, Region.pixelsPerBit );        
        ctx.fill();

        ctx.strokeStyle = "#808080";

        if( Region.selectedRight.length > 0 ) {
          if( Region.selectedRight[ 0 ] == offset ) {
            ctx.strokeStyle = "#FF0000";
          }
        }

        ctx.strokeRect( cx, cy, Region.pixelsPerBit, Region.pixelsPerBit );        
      }
    }

    // paint organizer receptive field centroids
    var rw = ow * cw;
    var inputArea = w *h;

    for( var oy = 0; oy < oh; ++oy ) {
      for( var ox = 0; ox < ow; ++ox ) {
        var offset = oy * ow + ox;
        var mask = dataOrganizerOutputMask.elements.elements[ offset ];
        if( mask == 0 ) {
          continue; // dead, don't paint
        }

        offset = oy * ( ow * 2 ) + ( ox * 2 );
        var xWeight = dataOrganizerOutputWeights.elements.elements[ offset +0 ];
        var yWeight = dataOrganizerOutputWeights.elements.elements[ offset +1 ];

        var x = w * xWeight * Region.pixelsPerBit;
        var y = h * yWeight * Region.pixelsPerBit;

        ctx.strokeStyle = "rgb(0,255,0)";

        for( var i = 0; i < Region.selectedLeft.length; ++i ) {
          var offset = Region.selectedLeft[ i ]; // a cell within the region     

          var rx = Math.floor( offset % rw );
          var ry = Math.floor( offset / rw );

          var oxSelected = Math.floor( rx / cw ); // coordinates of column in grid (region).
          var oySelected = Math.floor( ry / ch );    

          if( ( oxSelected == ox ) && ( oySelected == oy ) ) {
            ctx.strokeStyle = "rgb(255,0,255)";

            break;
          }
        }

        ctx.beginPath();
        ctx.moveTo( x,y-Region.pixelsReceptiveField );
        ctx.lineTo( x,y+Region.pixelsReceptiveField );
        ctx.moveTo( x-Region.pixelsReceptiveField,y );
        ctx.lineTo( x+Region.pixelsReceptiveField,y );
        ctx.stroke();   
      }
    }

    // paint weights of selected cells
    for( var i = 0; i < Region.selectedLeft.length; ++i ) {
      var offset = Region.selectedLeft[ i ]; // a cell within the region     
      var rx = Math.floor( offset % rw );
      var ry = Math.floor( offset / rw );

      var ox = Math.floor( rx / cw ); // coordinates of column in grid (region).
      var oy = Math.floor( ry / ch );    

      var dataName = Region.getClassifierDataName( ox, oy, "-output-weights" );
      var dataClassifierOutputWeights = Region.findData( dataName );
      if( !dataClassifierOutputWeights ) {
        continue; // can't paint
      }

      var cx = rx - ( ox * cw ); // coordinates in column.
      var cy = ry - ( oy * ch );    

      var classifierOffset = cy * cw * inputArea 
                           + cx      * inputArea

      for( var y = 0; y < h; ++y ) {
        for( var x = 0; x < w; ++x ) {
          var inputOffset = y * w + x;
          var offset = classifierOffset + inputOffset;

          var value = dataClassifierOutputWeights.elements.elements[ offset ];
 
          if( value < 0.01 ) {
            continue;
          }

          ctx.fillStyle = "rgba(255,0,255,"+value.toFixed( 3 ) + ")";
          var cx = x * Region.pixelsPerBit;
          var cy = y * Region.pixelsPerBit;
       
          ctx.fillRect( cx, cy, Region.pixelsPerBit, Region.pixelsPerBit );        
        }
      }

    }

    // paint selected input bits
    ctx.strokeStyle = "#FF0000";
    for( var i = 0; i < Region.selectedRight.length; ++i ) {
      var offset = Region.selectedRight[ i ];
      var x = Math.floor( offset % w );
      var y = Math.floor( offset / w );
      var cx = x * Region.pixelsPerBit;
      var cy = y * Region.pixelsPerBit;
      
      ctx.strokeRect( cx, cy, Region.pixelsPerBit, Region.pixelsPerBit );        
    }
  },

  findData : function( suffix ) {
    var dataName = Region.prefix + suffix;
    var data = Region.dataMap[ dataName ];
    return data;
  },

  onGotData : function() {
    Region.setStatus( "Got all region data, getting classifier data... " );
    //Region.repaint();    
    Region.getClassifierData();
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

  onGotClassifierData : function() {
    Region.setStatus( "Repainting... " );
    Region.repaint();    
  },

  findClassifierData : function( ox, oy, suffix ) {
    var dataName = Region.getClassifierDataName( ox, oy, suffix );
    var data = Region.dataMap[ dataName ];
    return data;
  },
  
  getClassifierDataName : function( ox, oy, suffix ) {
    var dataName = "-classifier-" + ox + "-" + oy + suffix;
    return dataName;
  },

  getClassifierData : function() {
    Region.classifierDataCount = 0;
    Region.classifierDataRequests = 0;

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

    // ask for all suffixes for each classifier    
    for( var oy = 0; oy < oh; ++oy ) {
      for( var ox = 0; ox < ow; ++ox ) {
        for( var i = 0; i < Region.classifierSuffixes.length; ++i ) {
          Region.classifierDataRequests += 1;
        }
      }
    }

    for( var oy = 0; oy < oh; ++oy ) {
      for( var ox = 0; ox < ow; ++ox ) {
        for( var i = 0; i < Region.classifierSuffixes.length; ++i ) {
          var suffix = Region.classifierSuffixes[ i ];
          var dataName = Region.prefix + Region.getClassifierDataName( ox, oy, suffix );
          console.log( "Requesting data: " + dataName );
          Framework.getData( dataName, Region.onGetClassifierData );
        }
      }
    }

  },

  onGetClassifierData : function( json ) {
    if( json.status != 200 ) {
      Region.setStatus( "Error getting data" );
      return;
    }

    var datas = JSON.parse( json.responseText );
    var data = datas[ 0 ];
    Framework.decode( data );
    Region.dataMap[ data.name ] = data;

    console.log( "Received data: " + data.name );

    Region.classifierDataCount = Region.classifierDataCount +1;

    Region.setStatus( "Got " + Region.classifierDataCount + " of " + Region.classifierDataRequests + " classifier data." );
    if( Region.classifierDataCount == Region.classifierDataRequests ) {
      Region.onGotClassifierData();
    }
  },

  resizeDataArea : function() {
    var dataElement = $( "#region-data" )[ 0 ];
    var infoElement = $( "#region-info" )[ 0 ];
    var infoArea = infoElement.getBoundingClientRect();
    var height = window.innerHeight - infoArea.height -getScrollbarWidth() -1;
    $( "#region-data-left" ).css( "height", height );
    $( "#region-data-right" ).css( "height", height );
  },

  refresh : function() {

    var pxPerBit = $( "#size" ).val();
    Region.pixelsPerBit = pxPerBit;

    var entity = $( "#entity" ).val();
    console.log( "Updating " + entity );

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


