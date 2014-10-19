const PLUGIN_NAME = "GoogleMaps";
var clusters = [];

function Cluster(id) {
  plugin.google.maps.BaseClass.apply(this);
  var self = this;
  
  Object.defineProperty(this, "type", {
    value: "Cluster",
    writable: false
  });
  Object.defineProperty(this, "id", {
    value: id,
    writable: false
  });
  
  self.addMarker = function(marker) {
    cordova.exec(function() {
    }, function() {
    }, PLUGIN_NAME, "exec", ["MarkerCluster.addMarker", id, marker.getId()]);
  };
  self.refresh = function() {
    cordova.exec(function() {
    }, function() {
    }, PLUGIN_NAME, "exec", ["MarkerCluster.refresh", id]);
  };
  return self;
}

module.exports = {
  "MarkerCluster": {
    "createCluster": function(map, callback) {
      cordova.exec(function(clusterId) {
        var cluster = new Cluster(clusterId);
        clusters.push(cluster);
        callback(cluster);
        
        map.on(plugin.google.maps.event.CAMERA_CHANGE, function() {
          cluster.refresh();
        });
      }, function() {
        callback(null);
      }, PLUGIN_NAME, "exec", ["MarkerCluster.createMarkerCluster"]);
      
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