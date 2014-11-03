Notices:
- **This is beta version. Not product level.**
- **It's very unstable version.  The behavior is not guaranteed.**
- **Please note that the specification may change without prior notice.**

markercluster
=============

###How to install?
```bash
$> cordova plugin add plugin.google.maps --variable ...
$> cordova plugin add https://github.com/wf9a5m75/markercluster
```

###How to use?
```js
plugin.google.maps.experimental.MarkerCluster.createCluster(map, function(cluster) {
  cluster.addMarkers(markers);
});
```

- `plugin.google.maps.experimental.MarkerCluster.createCluster` returns a new cluster instance.
- `cluster.addMarkers` accepts an array of the options of [map.addMarker()](https://github.com/wf9a5m75/phonegap-googlemaps-plugin/wiki/Marker). **Do not pass the marker instance.**


###Example
```js
plugin.google.maps.experimental.MarkerCluster.createCluster(map, function(cluster) {
  async.map(data.photos, function(info, next) {
    next(null, {
      'position': new plugin.google.maps.LatLng(info.lat, info.lng),
      'photoData': info,
      'icon': info.photo_file_url.replace("medium", "mini_square"),
      'markerClick': onMarkerClicked
    });
  }, function(err, markers) {
    cluster.addMarkers(markers);
  });
});
```
