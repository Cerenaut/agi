
var Data = {

  persist : function() {
    var string = $( "#data-json" ).val();
    var json = JSON.parse( string ); // strips whitespace etc, makes it nice
    var dataName = json.name;
    var jsonString = "[" + JSON.stringify( json ) + "]";
    console.log( "persisting: " + dataName + " as: " + jsonString );
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.setData( jsonString, Data.onSetData );
  },

  update : function() {
    var names = $( "#data" ).val();
    var nameList = names.split( "," );
    var suffix = "";
    for( var i = 0; i < nameList.length; ++i ) {
      dataName = nameList[ i ]; 
      if( i > 0 ) {
        suffix = suffix + "&";
      }
      suffix = suffix + "name=" + dataName;
    }
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.getDataList( suffix, Data.onGetData );
  },

  onSetData : function( json ) {
    $("#result").val( json.responseText );
  },

  onGetData : function( json ) {
    if( json.status != 200 ) {
      return;
    }

    var decode = $( "#decode" ).is( ":checked" );
    var textAreaValue = "";

    var datas = JSON.parse( json.responseText );

    var series = [];
    series.length = datas.length;
    var maxElements = 1;

    var decode = $( "#type" ).val();
    var elementsOffset = $( "#offset" ).val();
    var elementsLength = $( "#length" ).val();

    for( var d = 0; d < datas.length; ++d ) {
      var data = datas[ d ]; // TODO generalize to multiple responses.
      if( decode ) {
        Framework.decode( data );
      }

      if( ( elementsOffset != 0 ) || ( elementsLength != 0 ) ) {
        var maxOffset = Math.min( elementsOffset + elementsLength, data.elements.elements.length -1 );
        data.elements.elements = data.elements.elements.slice( elementsOffset, maxOffset );
      }
      textAreaValue = textAreaValue + JSON.stringify( data, null, 2 ) + "\n\n";
    }

    $( "#data-json" ).val( textAreaValue );
  },

  onParameter : function( key, value ) {
    if( key == "data" ) {
      var oldValue = $("#data").val();
      if( oldValue.length > 0 ) {
        oldValue = oldValue + ",";
      }
      var newValue = oldValue + value;
      $("#data").val( newValue ); 
    }
    else if( key == "offset" ) {
      $("#offset").val( value ); 
      $("#offset").trigger('mouseup');
    }
    else if( key == "length" ) {
      $("#length").val( value ); 
      $("#length").trigger('mouseup');
    }
    else if( key == "interval" ) {
      $("#interval").val( value ); 
      $("#interval").trigger('mouseup');
    }
    else if( key == "start" ) {
      Data.resume();
    }
  },

  setup : function() {
    Parameters.extract( Data.onParameter );
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Loop.setup( Data.update );
  }

};

$( document ).ready( function() {
  Framework.setNodeHost(); // in case override by param
  Data.setup();
  Data.update(); // once
} );


