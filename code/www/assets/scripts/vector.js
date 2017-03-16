
// Demo URL: file:///home/dave/workspace/agi.io/agi/experimental-framework/code/web-ui2/vector-bar.html?data=myLight-random-output&data=mySwitch-light-output&data=myLight-light-output&interval=303
var Vector = {

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
    Framework.getDataList( suffix, Vector.onGetData );
  },

  onGetData : function( json ) {
    if( json.status != 200 ) {
      return;
    }

    var normalize = $( "#normalize" ).is( ":checked" );
    var chartType = $( "#type" ).val();
    var smoothingWindow = $( "#window" ).val();
    var userMinValue = $( "#min" ).val();
    var userMaxValue = $( "#max" ).val();
    var stride = parseInt( $( "#stride" ).val() );

    var datas = JSON.parse( json.responseText );

    var series = [];
    series.length = datas.length;
    var maxElements = 1;

    for( var d = 0; d < datas.length; ++d ) {
      var data = datas[ d ]; // TODO generalize to multiple responses.
      var elements = Math.floor( data.elements.length / stride );
      maxElements = Math.max( elements, maxElements );

      Framework.decode( data );
    }

    var categories = [];
    categories.length = maxElements;
    for( var i = 0; i < maxElements; ++i ) {
      categories[ i ] = i * stride;
    }

    var minValue = 0.0;
    var maxValue = 0.0;

    for( var d = 0; d < datas.length; ++d ) {
      var data = datas[ d ]; // TODO generalize to multiple responses.
      var dataElements = data.elements;
      var dataSizes = data.sizes;
      var elements = dataElements.elements.length;

      var dataName = data.name;
      var values = [];
      values.length = Math.floor( elements / stride );

      if( smoothingWindow > 1 ) {
        var values2 = [];

        for( var i = 0; i < elements; i++ ) {
          var count = 0;
          var sum = 0;
          var j0 = Math.max( 0, i-smoothingWindow-1 );
          for( var j = i; j >= j0; j-- ) {
            var value = dataElements.elements[ j ];
            sum += value;
            count += 1;
          }
          var mean = 0;
          if( count > 0 ) {
            mean = sum / count;
          }
          values2.push( mean );
        }

        dataElements.elements = values2;
      }

      if( normalize ) {
        for( var i = 0; i < elements; i += stride ) {
          var value = dataElements.elements[ i ];
          maxValue = Math.max( value, maxValue );
          minValue = Math.min( value, minValue );
        }
      }
      else {
        minValue = parseFloat( userMinValue );
        maxValue = parseFloat( userMaxValue );
      }

      var i = 0;
      var k =0 ;
      for( i = 0; i < elements; i += stride ) {
        values[ k ] = dataElements.elements[ i ];
        ++k;
      }

      while( k < maxElements ) { // pad series
        values[ k ] = 0;
        k++;
      } 
     
      series[ d ] = {
            name: dataName,
            data: values,
            animation: false
      }
    }

    var chart = {
        chart: {
              type: chartType
//            type: 'bar'
//            type: 'column'
//            type: 'line'
        },
        title: {
            text: 'Vector plot'
        },
        xAxis: {
//            categories: ['0', '1', '2'],
            categories: categories,
            title: {
                text: "Elements"
            }
        },
        yAxis: {
            min: minValue,
            max: maxValue,
            title: {
                text: 'Value',
                align: 'high'
            },
            labels: {
                overflow: 'justify'
            }
        },
        plotOptions: {
            bar: {
                dataLabels: {
                    enabled: true
                }
            }
        },
        legend: {
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'top',
            x: -40,
            y: 80,
            floating: true,
            borderWidth: 1,
            backgroundColor: ((Highcharts.theme && Highcharts.theme.legendBackgroundColor) || '#FFFFFF'),
            shadow: true
        },
        credits: {
            enabled: false
        },
        series: series
    };

    $( function() { $('#container').highcharts( chart ); } );
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
      Vector.resume();
    }
    else if( key == "max" ) {
      $("#max").val( value ); 
    }
    else if( key == "type" ) {
      $("#type").val( value ); 
    }
  },

  setup : function() {
    Framework.setNodeHost(); // in case override by param
    Parameters.extract( Vector.onParameter );
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Loop.setup( Vector.update );
  }

};

$( document ).ready( function() {
  Vector.setup();
  Vector.update(); // once
} );


