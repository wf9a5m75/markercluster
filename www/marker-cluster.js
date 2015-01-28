const PLUGIN_NAME = "GoogleMaps";
var clusters = [];
var _org_onMarkerEvent = null;
var MARKERS = {};
var CLUSTER_MARKER_MAP = {};

function Cluster(clusterId) {
  plugin.google.maps.BaseClass.apply(this);
  var self = this;
  
  if (!_org_onMarkerEvent) {
    _org_onMarkerEvent = plugin.google.maps.Map._onMarkerEvent;
    plugin.google.maps.Map._onMarkerEvent = function(eventName, markerId) {
      for (var clusterMarkerId in CLUSTER_MARKER_MAP) {
        if (CLUSTER_MARKER_MAP[clusterMarkerId] === markerId) {
          var marker = MARKERS[clusterMarkerId];
          marker.trigger(eventName, marker);
          return;
        }
      }
      _org_onMarkerEvent.call(plugin.google.maps.Map, eventName, markerId);
    };
  }
  
  Object.defineProperty(this, "type", {
    value: "Cluster",
    writable: false
  });
  Object.defineProperty(this, "id", {
    value: clusterId,
    writable: false
  });
  
  self.addMarkers = function(markerData) {
    var markers;
    if (Object.prototype.toString.call(markerData) === '[object Array]') {
      markers = markerData;
    } else {
      markers = [markerData];
    }
    
    var self = this;
    
    for (var i = 0; i < markers.length; i++) {
      (function(markerOptions) {
        markerOptions.position = markerOptions.position || {};
        markerOptions.position.lat = markerOptions.position.lat || 0.0;
        markerOptions.position.lng = markerOptions.position.lng || 0.0;
        markerOptions.anchor = markerOptions.anchor || [0.5, 0.5];
        markerOptions.draggable = markerOptions.draggable || false;
        markerOptions.icon = markerOptions.icon || undefined;
        markerOptions.snippet = markerOptions.snippet || undefined;
        markerOptions.title = markerOptions.title || undefined;
        markerOptions.visible = markerOptions.visible === undefined ? true : markerOptions.visible;
        markerOptions.flat = markerOptions.flat || false;
        markerOptions.rotation = markerOptions.rotation || 0;
        markerOptions.opacity = parseFloat("" + markerOptions.opacity, 10) || 1;
        markerOptions.disableAutoPan = markerOptions.disableAutoPan === undefined ? false: markerOptions.disableAutoPan;
        if ("styles" in markerOptions) {
          markerOptions.styles = typeof markerOptions.styles === "object" ? markerOptions.styles : {};
          
          if ("color" in markerOptions.styles) {
            markerOptions.styles.color = HTMLColor2RGBA(markerOptions.styles.color || "#000000");
          }
        }
        
        var idx = Math.floor(Math.random() * Date.now());
        var clusterMarkerId = "cMarker_" + idx;
        markerOptions.id = clusterMarkerId;
        markerOptions.hashCode = idx;
        
        var marker = new plugin.google.maps.Marker(plugin.google.maps.Map, clusterMarkerId, markerOptions);
        if (typeof markerOptions.markerClick === "function") {
          marker.on(plugin.google.maps.event.MARKER_CLICK, markerOptions.markerClick);
        }
        if (typeof markerOptions.infoClick === "function") {
          marker.on(plugin.google.maps.event.INFO_CLICK, markerOptions.infoClick);
        }
        MARKERS[clusterMarkerId] = marker;
        
      })(markers[i]);
    }
    
    var callback = function(result) {
      if (result.action == "bind") {
        CLUSTER_MARKER_MAP[result.clusterMarkerId] = result.id;
      } else {
        //delete CLUSTER_MARKER_MAP[result.clusterMarkerId];
      }
    };
    cordova.exec(callback, callback, PLUGIN_NAME, "exec", ["MarkerCluster.addMarkerJson", clusterId, markers]);
  };
  self.refresh = function() {
    var callback = function(result) {
      if (result.action === "bind") {
        CLUSTER_MARKER_MAP[result.clusterMarkerId] = result.id;
      } else {
        //delete CLUSTER_MARKER_MAP[result.clusterMarkerId];
      }
    };
    cordova.exec(callback, function() {
    }, PLUGIN_NAME, "exec", ["MarkerCluster.refresh", clusterId]);
  };
  return self;
}

module.exports = {
  "MarkerCluster": {
    "createCluster": function(map, options, callback) {
      if (typeof options === "function") {
        callback = options;
        options = undefined;
      }
      options = options || {};
      
      cordova.exec(function(clusterId) {
        var cluster = new Cluster(clusterId);
        clusters.push(cluster);
        callback(cluster);
        
        map.on(plugin.google.maps.event.CAMERA_CHANGE, function() {
          cluster.refresh();
        });
        window.addEventListener("orientationchange", function() {
          cluster.refresh();
        });
      }, function() {
        callback(null);
      }, PLUGIN_NAME, "exec", ["MarkerCluster.createMarkerCluster", options]);
      
    },
    "_onClusterEventForIOS": function(clusterId, action, result) {
      cordova.exec(null, null, PLUGIN_NAME, "exec", ["MarkerCluster._onClusterEventForIOS", clusterId, action, result]);
    }
  }
};

cordova.addConstructor(function() {
  if (!window.Cordova) {
    window.Cordova = cordova;
  };
  window.plugin = window.plugin || {};
  window.plugin.google = window.plugin.google || {};
  window.plugin.google.maps = window.plugin.google.maps || {};
  window.plugin.google.maps.experimental = window.plugin.google.maps.experimental || module.exports;
});

function HTMLColor2RGBA(colorStr, defaultOpacity) {
  defaultOpacity = !defaultOpacity ? 1.0 : defaultOpacity;
  if (colorStr === "transparent" || !colorStr) {
    return [0, 0, 0, 0];
  }
  var alpha = Math.floor(255 * defaultOpacity),
      matches,
      compStyle,
      result = {
        r: 0,
        g: 0,
        b: 0
      };
  if (colorStr.match(/^#[0-9A-F]{4}$/i)) {
    alpha = colorStr.substr(4, 1);
    alpha = parseInt(alpha + alpha, 16);
    colorStr = colorStr.substr(0, 4);
  }
  
  if (colorStr.match(/^#[0-9A-F]{8}$/i)) {
    alpha = colorStr.substr(7, 2);
    alpha = parseInt(alpha, 16);
    colorStr = colorStr.substring(0, 7);
  }
  
  // convert rgba() -> rgb()
  if (colorStr.match(/^rgba\([\d,.\s]+\)$/)) {
    matches = colorStr.match(/([\d.]+)/g);
    alpha = Math.floor(parseFloat(matches.pop()) * 256);
    matches = "rgb(" +  matches.join(",") + ")";
  }
    
  // convert hsla() -> hsl()
  if (colorStr.match(/^hsla\([\d%,.\s]+\)$/)) {
    matches = colorStr.match(/([\d%.]+)/g);
    alpha = Math.floor(parseFloat(matches.pop()) * 256);
    matches = "hsl(" +  matches.join(",") + ")";
  }
   
  colorDiv.style.color = colorStr;
  if (window.getComputedStyle) {
    compStyle = window.getComputedStyle(colorDiv, null);
    try {
      var value = compStyle.getPropertyCSSValue ("color");
      var valueType = value.primitiveType;
      if (valueType === CSSPrimitiveValue.CSS_RGBCOLOR) {
        var rgb = value.getRGBColorValue ();
        result.r = rgb.red.getFloatValue (CSSPrimitiveValue.CSS_NUMBER);
        result.g = rgb.green.getFloatValue (CSSPrimitiveValue.CSS_NUMBER);
        result.b = rgb.blue.getFloatValue (CSSPrimitiveValue.CSS_NUMBER);
      }
    } catch (e) {
      console.log("The browser does not support the getPropertyCSSValue method!");
    }
  }
  return [result.r, result.g, result.b, alpha];
}