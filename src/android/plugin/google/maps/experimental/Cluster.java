package plugin.google.maps.experimental;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.cordova.CordovaWebView;
import org.json.JSONObject;

import plugin.google.maps.GoogleMaps;

import com.example.myapp.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Cluster {

  private HashMap<Integer, JSONObject> markerHash = new HashMap<Integer, JSONObject>();
  private GoogleMaps mapCtrl;
  private Marker clusterMarker = null;
  private CordovaWebView mWebView;
  private LatLng clusterCenterLatLng;
  
  public Cluster(GoogleMaps mapCtrl, LatLng geocellCenter) {
    this.mapCtrl = mapCtrl;
    this.clusterCenterLatLng = geocellCenter;
  }
  
  public void addMarkerJsonList(List<JSONObject> markerJsonList) {
    int markerHashSize = markerHash.size();
    
    JSONObject markerOption;
    Iterator<JSONObject> iterator = markerJsonList.iterator();
    while (iterator.hasNext()) {
      markerOption = iterator.next();
      if (markerHash.containsKey(markerOption.hashCode()) == false) {
        this.markerHash.put(markerOption.hashCode(), markerOption);
      }
    }

    if (markerHashSize == 1) {
      clusterMarker.remove();
      markerHashSize = 0;
    }
    
    if (markerHashSize == 0) {
      MarkerOptions opts = new MarkerOptions();
      opts.position(clusterCenterLatLng);
      if (markerJsonList.size() > 1) {
        opts.icon(BitmapDescriptorFactory.fromResource(R.drawable.m1));
        opts.anchor(0.5f, 0.5f);
      }
      clusterMarker = mapCtrl.map.addMarker(opts);
      clusterMarker.setTitle("Cnt = " + markerJsonList.size());
    }
  }
  
  public void remove() {
    if (clusterMarker != null) {
      clusterMarker.remove();
      clusterMarker = null;
    }
  }
}
