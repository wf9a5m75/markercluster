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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

public class MarkerCluster {

  private List<JSONObject> markerOptionList = new ArrayList<JSONObject>();
  private List<LatLng> markerLatLngList = new ArrayList<LatLng>();
  private HashMap<String, Cluster> clusters = new HashMap<String, Cluster>();
  private GoogleMap mMap;
  private final int MIN_RESOLUTION = 1;
  private int resolution = MIN_RESOLUTION;
  
  // The maximum *practical* geocell resolution.
  private final int MAX_GEOCELL_RESOLUTION = 13;
  private final int GEOCELL_GRID_SIZE = 4;
  private final String GEOCELL_ALPHABET = "0123456789abcdef";
  private CordovaWebView mWebView = null;
  private GoogleMaps mMapCtrl;
  
  public MarkerCluster(CordovaWebView webView, GoogleMaps mapCtrl) {
    super();
    this.mWebView = webView;
    this.mMap = mapCtrl.map;
    this.mMapCtrl = mapCtrl;
    this.resolution = (int) mMap.getCameraPosition().zoom - 1;
    this.resolution = this.resolution < MIN_RESOLUTION ? MIN_RESOLUTION : this.resolution;
    this.resolution = this.resolution > MAX_GEOCELL_RESOLUTION ? MAX_GEOCELL_RESOLUTION : this.resolution;
    
  }
  
  public void addMarkerJson(JSONObject markerOptions, LatLng markerLatLng, boolean isRefresh) throws JSONException {
    if (isRefresh == false) {
      markerOptionList.add(markerOptions);
      JSONObject position = markerOptions.getJSONObject("position");
      double lat = position.getDouble("lat");
      double lng = position.getDouble("lng");
      markerLatLng = new LatLng(lat, lng);
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
  }

  public void refresh() throws JSONException {
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
    this.resolution = (int) mMap.getCameraPosition().zoom - 1;
    this.resolution = this.resolution < MIN_RESOLUTION ? MIN_RESOLUTION : this.resolution;
    this.resolution = this.resolution > MAX_GEOCELL_RESOLUTION ? MAX_GEOCELL_RESOLUTION : this.resolution;
    
    for (int i = 0; i < markerOptionList.size(); i++) {
      this.addMarkerJson(markerOptionList.get(i), markerLatLngList.get(i), true);
    }
  }
  
  /**
   * https://code.google.com/p/geomodel/source/browse/trunk/geo/geocell.py#370
   * @param latLng
   * @param resolution
   * @return
   */
  private String getGeocell(LatLng latLng) {
    String cell = "";
    double north = 90.0;
    double south = -90.0;
    double east = 180.0;
    double west = -180.0;
    
    double subcell_lng_span, subcell_lat_span;
    byte x, y;
    while(cell.length() < resolution) {
      subcell_lng_span = (east - west) / GEOCELL_GRID_SIZE;
      subcell_lat_span = (north - south) / GEOCELL_GRID_SIZE;

      x = (byte) Math.min(Math.floor(GEOCELL_GRID_SIZE * (latLng.longitude - west) / (east - west)), GEOCELL_GRID_SIZE - 1);
      y = (byte) Math.min(Math.floor(GEOCELL_GRID_SIZE * (latLng.latitude - south) / (north - south)),GEOCELL_GRID_SIZE - 1);
      cell += _subdiv_char(x, y);
      

      south += subcell_lat_span * y;
      north = south + subcell_lat_span;

      west += subcell_lng_span * x;
      east = west + subcell_lng_span;
    }
    return cell;
  }
  
  /**
   * Returns the geocell character in the 4x4 alphabet grid at pos. (x, y).
   * @return
   */
  private String _subdiv_char(byte posX, byte posY) {
    // NOTE: This only works for grid size 4.
    int start = ((posY & 2) << 2 |
        (posX & 2) << 1 |
        (posY & 1) << 1 |
        (posX & 1) << 0);
    return GEOCELL_ALPHABET.substring(start, start + 1);
  }

}
