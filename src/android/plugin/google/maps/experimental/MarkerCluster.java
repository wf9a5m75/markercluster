package plugin.google.maps.experimental;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.cordova.CordovaWebView;
import org.json.JSONException;
import org.json.JSONObject;

import plugin.google.maps.GoogleMaps;
import android.content.res.Configuration;
import android.view.Display;

import com.example.myapp.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

public class MarkerCluster {

  private List<JSONObject> markerOptionList = new ArrayList<JSONObject>();
  private List<LatLng> markerLatLngList = new ArrayList<LatLng>();
  private HashMap<String, Cluster> clusters = new HashMap<String, Cluster>();
  private GoogleMap mMap;
  
  private CordovaWebView mWebView = null;
  private GoogleMaps mMapCtrl;
  
  public MarkerCluster(CordovaWebView webView, GoogleMaps mapCtrl) {
    this.mWebView = webView;
    this.mMap = mapCtrl.map;
    this.mMapCtrl = mapCtrl;
    
    
    /*
    if (markerInfoList.size() > 1) {
      MarkerOptions opts = new MarkerOptions();
      opts.position(markerLatLng);
      opts.icon(BitmapDescriptorFactory.fromResource(R.drawable.m1));
      opts.anchor(0.5f, 0.5f);
      
      JSONObject tmpOptions = markerOptionList.get(0);
      position = tmpOptions.getJSONObject("position");
      lat = position.getDouble("lat");
      lng = position.getDouble("lng");
      LatLng markerCenter = new LatLng(lat, lng);
      
      clusterMarker = mMap.addMarker(opts);
    } else {
      
    }
    */
  }
  
  public void addMarkerJson(List<JSONObject> markerInfoList) throws JSONException {
    /*
    if (isRefresh == false) {
      markerOptionList.add(markerOptions);
      markerLatLngList.add(markerLatLng);
    }
    
    //Find the nearest cluster
    Cluster cluster;
    String geocell = getGeocell(markerLatLng);
    if (clusters.containsKey(geocell)) {
      cluster = clusters.get(geocell);
      cluster.addMarkerJson(markerOptions);
      return;
    }

    cluster = new Cluster(mWebView, mMapCtrl);
    cluster.addMarkerJson(markerOptions);
    clusters.put(geocell, cluster);
    */
  }

  public void clear() throws JSONException {
    Cluster cluster;
    String key;
    Set<String> keys = clusters.keySet();
    Iterator<String> iterator = keys.iterator();
    while(iterator.hasNext()) {
      key = iterator.next();
      cluster = clusters.get(key);
      cluster.remove();
      cluster = null;
    }
    clusters.clear();
  }
}
