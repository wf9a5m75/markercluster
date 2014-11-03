##It's very unstable version.  The behavior is not guaranteed.


markercluster
=============

###How to use?
```js
plugin.google.maps.experimental.MarkerCluster.createCluster(map, function(cluster) {
  cluster.addMarkers(markers);
});
```


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
