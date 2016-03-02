
// Demo URL: file:///home/dave/workspace/agi.io/agi/experimental-framework/code/web-ui2/vector-bar.html?data=myLight-random-output&data=mySwitch-light-output&data=myLight-light-output&interval=303
var AgiChart = {

/*  updating : false,
  updater : null,

  start : function() {
    setInterval( Viewing.frameRecorder.recordFrame, AgiChart.updateInterval );
  },

  togglePause : function() {
    if( AgiChart.isUpdating() ) {
      AgiChart.pause();      
    } 
    else {
      AgiChart.resume();      
    }
  },

  isUpdating : function() {
    return AgiChart.updating;
  },

  pause : function() {
    console.log( "pausing chart..." );
    clearInterval( AgiChart.updater );
    AgiChart.updater = null;
    AgiChart.updating = false;
    $("#pause").val( "Resume" );
  },

  resume : function() {
    if( AgiChart.updating == true ) {
      return; // don't add multiple timers
    }

    console.log( "resuming chart..." );
    var updateInterval = $("#interval").val();
    AgiChart.updater = setInterval( AgiChart.update, updateInterval );
    AgiChart.updating = true;
    $("#pause").val( "Pause" );
  },
*/
  update : function() {

    //console.log( "updating chart..." );

    var key = $( "#data" ).val();
    Agidb.getJson( "data?key=in."+key+"&order=key.asc", AgiChart.onGetData );
  },

  onGetData : function( response ) {
    if( response.length == 0 ) {
      return;
    }

    var series = [];
    series.length = response.length;
    var maxElements = 1;

    for( var d = 0; d < response.length; ++d ) {
      var data = response[ d ]; // TODO generalize to multiple responses.
      var dataElements = JSON.parse( data.elements );
      var elements = dataElements.elements.length;
      maxElements = Math.max( elements, maxElements );
    }

    var categories = [];
    categories.length = maxElements;
    for( var i = 0; i < maxElements; ++i ) {
      categories[ i ] = i;
    }

    for( var d = 0; d < response.length; ++d ) {
      var data = response[ d ]; // TODO generalize to multiple responses.
      var dataElements = JSON.parse( data.elements );
      var dataSizes = JSON.parse( data.sizes );
      var elements = dataElements.elements.length;

      var key = data.key;
      var values = [];
      values.length = elements;

      var i = 0;
      for( i = 0; i < elements; ++i ) {
        values[ i ] = dataElements.elements[ i ];
      }

      while( i < maxElements ) { // pad series
        values[ i ] = 0;
        ++i;
      } 
     
      series[ d ] = {
            name: key,
            data: values,
            animation: false
      }
    }

    var chart = {
        chart: {
//            type: 'bar'
            type: 'column'
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
            min: 0,
            max: 1,
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
      AgiChart.resume();
    }
  },

  setup : function() {

    AgiParams.extract( AgiChart.onParameter );

/*    // http://stackoverflow.com/questions/6944744/javascript-get-portion-of-url-path
    var params = window.location.search.split( "&" );
    console.log( "params = " + params );

    // remove ? prefix, if present
    if( params.length > 0 ) {
      if( params[ 0 ].indexOf( "?" ) == 0 ) {
        params[ 0 ] = params[ 0 ].slice( 1 ); 
      }

      for( var i = 0; i < params.length; ++i ) {
        var param = params[ i ];
        var index = param.indexOf( "=" );
        var key = param.slice( 0, index );
        var value = param.slice( index+1 );
        console.log( "param: "+key+","+value );
        parameterCallback( key, value );
      }
    }*/

    Agidb.setup();
    AgiLoop.setup( AgiChart.update );
  }

};

$( document ).ready( function() {
  AgiChart.setup();
  AgiChart.update(); // once
} );


