Notices:
- **This is a beta version. Not product level.**
- **It's still very unstable. The behavior is not guaranteed.**
- **Please note that the specification may change without prior notice.**
- **Currently there is Android version only, but I will develop for iOS version**

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
(This demo uses [async.js](https://github.com/caolan/async) library. You can find it under the `example/www/js/` directory.)
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

###Demo
http://youtu.be/5PfPKpcQpiM

###Demo APK
https://googledrive.com/host/0B1ECfqTCcLE8SHVUX25xcmNIUTQ/markercluster_demo_20141102.apk
