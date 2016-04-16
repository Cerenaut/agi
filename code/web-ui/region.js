
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

  pixelsColumnGap : 3,
  pixelsMargin : 4,
  pixelsPerBit : 8,
  prefix : "",

  selectedLeft : [  ],
  selectedRight : [  ],

  suffixes : [ "-activity-new", "-prediction-old", "-prediction-new", "-ff-input", "-organizer-output-mask", "-organizer-output-weights" ],
  suffixIdx : 0,
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
    Region.repaintCells();
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
    var c = $( "#left-canvas" )[ 0 ];
    c.width  = rw * Region.pixelsPerBit + (ow-1) * Region.pixelsColumnGap;
    c.height = rh * Region.pixelsPerBit + (oh-1) * Region.pixelsColumnGap;

    var ctx = c.getContext( "2d" );
    ctx.fillStyle = "#808080";
    ctx.fillRect( 0, 0, c.width, c.height );

    for( var oy = 0; oy < oh; ++oy ) {
      for( var ox = 0; ox < ow; ++ox ) {

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
          }
        }
      }
    }

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
    Region.setStatus( "Got all data. " );
    Region.repaint();    
  },

  getData : function() {

    if( Region.suffixIdx >= Region.suffixes.length ) {
      Region.onGotData();
      return;
    }

    var dataName = Region.prefix + Region.suffixes[ Region.suffixIdx ];

    Region.setStatus( "Getting " + Region.suffixIdx + " of " + Region.suffixes.length + ", name: " + dataName );

    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Region.suffixIdx = Region.suffixIdx +1; // get next
    Framework.getData( dataName, Region.onGetData );
  },

  onGetData : function( json ) {
    if( json.status != 200 ) {
      Region.setStatus( "Error getting data" );
      return;
    }

    var datas = JSON.parse( json.responseText );
    var data = datas[ 0 ];

    Framework.removeSparseUnitCoding( data );

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

  refresh : function() {

    var pxPerBit = $( "#size" ).val();
    Region.pixelsPerBit = pxPerBit;

    var entity = $( "#entity" ).val();
    console.log( "Updating " + entity );

    Region.resizeDataArea();

    Region.setStatus( "Refreshing..." );
    Region.dataMap = {};
    Region.suffixIdx = 0;
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
    } );
    $( "#right-canvas" )[ 0 ].addEventListener( 'click', function( e ) {
      Region.onMouseClickRight( e, e.offsetX, e.offsetY );
    } );
  }

};

$( document ).ready( function() {
  Region.setup();
} );


