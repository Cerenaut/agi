
  // URL specification:
  // Context    Method Parameters       Response
  // nodes      GET                     All nodes, as JSON        (no filter, because there are few nodes)
  // entities   GET                     All entities, as JSON
  // entities   GET    Name, ...        Named entities, as JSON   (because there may be many entities)
  // data       GET    Name, ...        Named data, as JSON       (no all-data query as this would be very large)
  // properties GET    Name, ...        Named properties, as JSON  
  // properties PUT    Name=Value, ...  Named properties, as JSON  
  // subtree    GET    Name,            Entities, as JSON

var Framework = {

  protocol : "http",
  host : "localhost",
  port : "8491",

  contextData : "data",
  contextEntities : "entities",
  contextConfig : "config",
  contextNodes : "nodes",
  contextUpdate : "update",
  contextImport : "import",
  contextExport : "export",
  contextVersion : "version",

  exportTypeEntity : "entity",  
  exportTypeData : "data",  
  exportTypeDataRefs : "data-refs",


  update : function( entity, callback ) {
    var suffix = Framework.contextUpdate 
               + "?entity=" + entity 
               + "&event=" + "update";
    var verb = "POST";
    Framework.doAjaxJson( suffix, callback, verb );
  },

  getDataSize : function( data ) {
    var w = 0; 
    var h = 0; 
    var dataSizes = data.sizes;

    for( var i = 0; i < dataSizes.labels.length; ++i ) {
      var label = dataSizes.labels[ i ];
      if( label == "x" ) w = dataSizes.sizes[ i ];
      if( label == "y" ) h = dataSizes.sizes[ i ];
    }

    var size = { w: w, h: h };
    return size;
  },

  getDataNames : function( callback ) {
    var suffix = Framework.contextData;
    Framework.doAjaxJson( suffix, callback, "GET" );
  },

  getData : function( dataName, callback ) {
    var suffix = Framework.contextData + "?name=" + dataName;
    Framework.doAjaxJson( suffix, callback, "GET" );
  },

  getDataList : function( dataNames, callback ) {
    var suffix = Framework.contextData + "?" + dataNames;
    Framework.doAjaxJson( suffix, callback, "GET" );
  },

  getEntities : function( key, callback ) {
    var suffix = Framework.contextEntities + "?" + key;
    Framework.doAjaxJson( suffix, callback, "GET" );
  },

  getSubtree : function( key, callback ) {
    var suffix = "export?type=entity&entity=" + key;
    Framework.doAjaxJson( suffix, callback, "GET" );
  },

  decode : function( data ) {
    if( !data.elements ) {
      return;
    }

    var dataElements = data.elements;

    // undo the sparse coding, if present:
    if( dataElements.encoding == "sparse-binary" ) {
      var dataElementsLength = dataElements["length"]; // note: official length.
      var dataElementsDense = new Array( dataElementsLength );//.join('0').split('').map(parseFloat);//Array.apply( null, Array( dataElementsLength ) ).map( Number.prototype.valueOf, 0 );
      for( var i = 0; i < dataElementsLength; ++i ) {
        dataElementsDense[ i ] = 0;
      }
      for( var i = 0; i < dataElements.elements.length; ++i ) {
        var j = dataElements.elements[ i ];
        dataElementsDense[ j ] = 1.0;
      }    
      dataElements.elements = dataElementsDense;
    }
    else if( dataElements.encoding == "sparse-real" ) {
      var dataElementsLength = dataElements["length"]; // note: official length.
      var dataElementsDense = new Array( dataElementsLength );//.join('0').split('').map(parseFloat);//Array.apply( null, Array( dataElementsLength ) ).map( Number.prototype.valueOf, 0 ); // create large array of zeros, preallocated
      for( var i = 0; i < dataElementsLength; ++i ) {
        dataElementsDense[ i ] = 0;
      }
      var encodedValues = dataElements.elements.length / 2;
      for( var i = 0; i < encodedValues; ++i ) { // the encoded length
        var i1 = i * 2;
        var i2 = i1 +1;
        var index = Math.floor( dataElements.elements[ i1 ] );
        var value =             dataElements.elements[ i2 ];
        dataElementsDense[ index ] = value;
      }    

      dataElements.elements = dataElementsDense;
    }
  },

  exportEntity : function( entityName, type ) {
    var suffix = Framework.contextExport + "?entity=" + entityName + "&type="+type;
    var url = Framework.getUrl( suffix );
    window.open( url, "_blank" );
  },

  getImportUrl : function( entityName ) {
//  importEntity : function( entityName, callback ) {
    var suffix = Framework.contextImport + "?entity=" + entityName;
    var url = Framework.getUrl( suffix );
    return url;    
  },

  getNodes : function( callback ) {
    var suffix = Framework.contextNodes;
    Framework.doAjaxJson( suffix, callback, "GET" );
  },

  getEntities : function( callback ) {
    var suffix = Framework.contextEntities;
    Framework.doAjaxJson( suffix, callback, "GET" );
  },

  getEntity : function( entityName, callback ) {
    var suffix = Framework.contextEntities + "?name=" + entityName;
    Framework.doAjaxJson( suffix, callback, "GET" );
  },

  getConfig : function( entityName, callback ) {
    var suffix = Framework.contextConfig + "?entity=" + entityName;
    Framework.doAjaxJson( suffix, callback, "GET" );
  },

  setConfigPath : function( entityName, configPath, value, callback ) {
    var suffix = Framework.contextConfig + "?entity=" + entityName + "&path=" + configPath + "&value=" + value;
    Framework.doAjaxJson( suffix, callback, "POST" );
  },

  setConfig : function( entityName, configValue, callback ) {
    var suffix = Framework.contextConfig + "?entity=" + entityName + "&config=" + configValue;
    Framework.doAjaxJson( suffix, callback, "POST" );
  },

  version : function( callback ) {
    var suffix = Framework.contextVersion;
    Framework.doAjaxJson( suffix, callback, "GET" );  
  },

  getUrl : function( suffix ) {
    var url = Framework.protocol + "://" + Framework.host + ":" + Framework.port + "/" + suffix;
    var urlEncoded = encodeURI( url );    
    return urlEncoded;
  },

  doAjaxJson : function( suffix, callback, method ) {
    var url = Framework.getUrl( suffix );   
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


