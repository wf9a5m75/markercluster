package plugin.google.maps.experimental;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class AsyncAddMarkerTask extends AsyncTask<JSONArray, Void, HashMap<String, List<JSONObject>>> {

  private int resolution = 1;
  
  // The maximum *practical* geocell resolution.
  private final int MAX_GEOCELL_RESOLUTION = 13;
  private final int GEOCELL_GRID_SIZE = 4;
  private final String GEOCELL_ALPHABET = "0123456789abcdef";
  private MarkerCluster markerCluster;
  private LatLngBounds visibleBounds;
  
  public AsyncAddMarkerTask(MarkerCluster markerCluster) {
    this.markerCluster = markerCluster;
    resolution = (int) markerCluster.mapCtrl.map.getCameraPosition().zoom - 1;
    resolution = resolution < 1 ? 1 : resolution;
    resolution = resolution > MAX_GEOCELL_RESOLUTION ? MAX_GEOCELL_RESOLUTION : resolution;
    
    visibleBounds = markerCluster.mapCtrl.map.getProjection().getVisibleRegion().latLngBounds;
  }

  @Override
  protected HashMap<String, List<JSONObject>> doInBackground(JSONArray... markerJsonArray) {
    JSONObject markerOptions, position;
    double lat, lng;
    String geocell;
    HashMap<String, List<JSONObject>> geocellHash = new HashMap<String, List<JSONObject>>();
    List<JSONObject> stacks;
    LatLng latlng;
    
    //-------------------------------------
    // marker clustering based on geocell
    //-------------------------------------
    for (int i = 0; i < markerJsonArray.length; i++) {
      for (int j = 0; j < markerJsonArray[i].length(); j++) {
        try {
          markerOptions = markerJsonArray[i].getJSONObject(j);
  
          position = markerOptions.getJSONObject("position");
          lat = position.getDouble("lat");
          lng = position.getDouble("lng");
          latlng = new LatLng(lat, lng);
          if (visibleBounds.contains(latlng) == false) {
            continue;
          }
          
          geocell = getGeocell(lat, lng, resolution);
          if (geocellHash.containsKey(geocell) == false) {
            stacks = new ArrayList<JSONObject>();
          } else {
            stacks = geocellHash.get(geocell);
          }
          stacks.add(markerOptions);
          geocellHash.put(geocell, stacks);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    return geocellHash;
  }
  
  @Override
  public  void onPostExecute(HashMap<String, List<JSONObject>> geocellHash) {
    Log.d("GoogleMaps", "--geocellhash: " + geocellHash.size());
    Set<String> keys = geocellHash.keySet();
    Iterator<String> iterator = keys.iterator();
    String geocell;
    Cluster cluster;
    LatLngBounds geocellBounds;
    while(iterator.hasNext()) {
      geocell = iterator.next();
      if (this.markerCluster.clusters.containsKey(geocell)) {
        cluster = markerCluster.clusters.get(geocell);
        cluster.addMarkerJsonList(geocellHash.get(geocell));
      } else {
        geocellBounds = this.getLatLngBoundsFromGeocell(geocell);
        cluster = new Cluster(markerCluster.mapCtrl, geocellBounds.getCenter());
        cluster.addMarkerJsonList(geocellHash.get(geocell));
        markerCluster.clusters.put(geocell, cluster);
      }
    }
    
    this.markerCluster.clusters.keySet();
    
  }

  private LatLngBounds getLatLngBoundsFromGeocell(String geocell) {
    String geoChar;
    double north = 89.9;
    double south = -89.9;
    double east = 179.9;
    double west = -179.9;
    int pos;
    
    double subcell_lng_span, subcell_lat_span;
    int x, y;
    for (int i = 0; i < geocell.length(); i++) {
      geoChar = geocell.substring(i, i + 1);
      pos = GEOCELL_ALPHABET.indexOf(geoChar);
      
      subcell_lng_span = (east - west) / GEOCELL_GRID_SIZE;
      subcell_lat_span = (north - south) / GEOCELL_GRID_SIZE;
      
      x = (int) (Math.floor(pos / 4) % 2 * 2 + pos % 2);
      y = (int) (pos - Math.floor(pos / 4) * 4);
      y = y >> 1;
      y += Math.floor(pos / 4) > 1 ? 2 : 0;
      
      south += subcell_lat_span * y;
      north = south + subcell_lat_span;

      west += subcell_lng_span * x;
      east = west + subcell_lng_span;
    }
    LatLng sw = new LatLng(south, west);
    LatLng ne = new LatLng(north, east);
    return new LatLngBounds(sw, ne);
  }
  /**
   * https://code.google.com/p/geomodel/source/browse/trunk/geo/geocell.py#370
   * @param latLng
   * @param resolution
   * @return
   */
  private String getGeocell(double lat, double lng, int resolution) {
    String cell = "";
    double north = 89.9;
    double south = -89.9;
    double east = 179.9;
    double west = -179.9;
    
    double subcell_lng_span, subcell_lat_span;
    byte x, y;
    while(cell.length() < resolution) {
      subcell_lng_span = (east - west) / GEOCELL_GRID_SIZE;
      subcell_lat_span = (north - south) / GEOCELL_GRID_SIZE;

      x = (byte) Math.min(Math.floor(GEOCELL_GRID_SIZE * (lng - west) / (east - west)), GEOCELL_GRID_SIZE - 1);
      y = (byte) Math.min(Math.floor(GEOCELL_GRID_SIZE * (lat - south) / (north - south)),GEOCELL_GRID_SIZE - 1);
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
