
// Demo URL: file:///home/dave/workspace/agi.io/agi/experimental-framework/code/web-ui2/vector-bar.html?data=myLight-random-output&data=mySwitch-light-output&data=myLight-light-output&interval=303
var AgiLoop = {

  callback : null,
  updating : false,
  updater : null,

  togglePause : function() {
    if( AgiLoop.isUpdating() ) {
      AgiLoop.pause();      
    } 
    else {
      AgiLoop.resume();      
    }
  },

  isUpdating : function() {
    return AgiLoop.updating;
  },

  pause : function() {
    console.log( "pausing..." );
    clearInterval( AgiLoop.updater );
    AgiLoop.updater = null;
    AgiLoop.updating = false;
    $("#pause").val( "Resume" );
  },

  resume : function() {
    if( AgiLoop.updating == true ) {
      return; // don't add multiple timers
    }

    console.log( "resuming..." );
    var updateInterval = $("#interval").val();
    AgiLoop.updater = setInterval( AgiLoop.update, updateInterval );
    AgiLoop.updating = true;
    $("#pause").val( "Pause" );
  },

  update : function() {
    console.log( "updating..." );
    AgiLoop.callback();
  },

  setup : function( callback ) {
    AgiLoop.callback = callback;
  }

};

