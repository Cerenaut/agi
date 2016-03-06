
// Demo URL: file:///home/dave/workspace/agi.io/agi/experimental-framework/code/web-ui2/vector-bar.html?data=myLight-random-output&data=mySwitch-light-output&data=myLight-light-output&interval=303
var Loop = {

  callback : null,
  updating : false,
  updater : null,

  togglePause : function() {
    if( Loop.isUpdating() ) {
      Loop.pause();      
    } 
    else {
      Loop.resume();      
    }
  },

  isUpdating : function() {
    return Loop.updating;
  },

  pause : function() {
    console.log( "Loop pausing..." );
    clearInterval( Loop.updater );
    Loop.updater = null;
    Loop.updating = false;
    $("#pause").val( "Resume" );
  },

  resume : function() {
    if( Loop.updating == true ) {
      return; // don't add multiple timers
    }

    console.log( "Loop resuming..." );
    var updateInterval = $("#interval").val();
    Loop.updater = setInterval( Loop.update, updateInterval );
    Loop.updating = true;
    $("#pause").val( "Pause" );
  },

  update : function() {
    //console.log( "loop update..." );
    Loop.callback();
  },

  setup : function( callback ) {
    Loop.callback = callback;
  }

};

