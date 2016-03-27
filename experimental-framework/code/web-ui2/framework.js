
  // URL specification:
  // Context    Method Parameters       Response
  // nodes      GET                     All nodes, as JSON        (no filter, because there are few nodes)
  // entities   GET                     All entities, as JSON
  // entities   GET    Name, ...        Named entities, as JSON   (because there may be many entities)
  // data       GET    Name, ...        Named data, as JSON       (no all-data query as this would be very large)
  // properties GET    Name, ...        Named properties, as JSON  
  // properties PUT    Name=Value, ...  Named properties, as JSON  

var Framework = {

  protocol : "http",
  host : "localhost",
  port : "8491",

  contextData : "data",
  contextEntities : "entities",
  contextProperties : "properties",
  contextNodes : "nodes",
  contextUpdate : "update",

  update : function( entity, callback ) {
    var suffix = Framework.contextUpdate 
               + "?entity=" + entity 
               + "&event=" + "update";
    var verb = "POST";
    Framework.doAjaxJson( suffix, callback, verb );
  },

  getData : function( key, callback ) {
    var suffix = Framework.contextData + "?" + key;
    Framework.doAjaxJson( suffix, callback, "GET" );
  },

  getProperty : function( key, callback ) {
    var suffix = Framework.contextProperties + "?" + key;
    Framework.doAjaxJson( suffix, callback, "GET" );
  },

  setProperty : function( key, value, callback ) {
    var suffix = Framework.contextProperties + "?" + key + "=" + value;
    Framework.doAjaxJson( suffix, callback, "POST" );
  },

  doAjaxJson : function( suffix, callback, method ) {
    var url = Framework.protocol + "://" + Framework.host + ":" + Framework.port + "/" + suffix;

    $.ajax( url, {
      type: method,
//      contentType: 'application/json',
      complete: function( jqxhr ) {
          if( callback ) {
            callback( jqxhr );
          }
      }
    } );
  },

  setup : function( host, port ) {
    if( host ) Framework.host = host;
    if( port ) Framework.port = port;
  }

};


