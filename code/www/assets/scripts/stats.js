
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

    for( var d = 0; d < datas.length; ++d ) {
      var data = datas[ d ]; // TODO generalize to multiple responses.
      if( decode ) {
        Framework.decode( data );
      }

      var dataElements = data.elements;
      var dataSizes = data.sizes;
      var elements = dataElements.elements.length;

      var dataName = data.name;

      var countNonZero = 0;
      var sum = 0;
      var minVal = Number.MAX_VALUE;
      var maxVal = -Number.MAX_VALUE;//Number.MIN_VALUE;
      var variance = 0;

      for( var i = 0; i < elements; ++i ) {

        var value = dataElements.elements[ i ];
        
        sum = sum + value;

        minVal = Math.min( minVal, value );
        maxVal = Math.max( maxVal, value );
      }
        
      var mean = sum / elements;
      if( elements == 0 ) {
        mean = 0.0;
      }

      for( var i = 0; i < elements; ++i ) {

        var value = dataElements.elements[ i ];

        var diff = value - mean;
        diff = diff * diff;
        variance += diff;        
      }

      if( elements > 0 ) {
        variance /= elements;
      }

      $( "#volume" ).html( elements );
      $( "#sum" ).html( sum );
      $( "#min" ).html( minVal );
      $( "#max" ).html( maxVal );
      $( "#mean" ).html( mean );
      $( "#variance" ).html( variance );
      
    }

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
    else if( key == "interval" ) {
      $("#interval").val( value ); 
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


