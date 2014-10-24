package plugin.google.maps.experimental;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import plugin.google.maps.GoogleMaps;
import android.os.AsyncTask;

public class AsyncAddMarkerTask extends AsyncTask<JSONArray, Void, HashMap<String, List<JSONObject>>> {

  private MarkerCluster mMarkerCluster;
  private GoogleMaps mMapCtrl;
  private int resolution = 1;
  
  // The maximum *practical* geocell resolution.
  private final int MAX_GEOCELL_RESOLUTION = 13;
  private final int GEOCELL_GRID_SIZE = 4;
  private final String GEOCELL_ALPHABET = "0123456789abcdef";
  
  public AsyncAddMarkerTask(MarkerCluster markerCluster, GoogleMaps mapCtrl) {
    mMarkerCluster = markerCluster;
    mMapCtrl = mapCtrl;
    resolution = (int) mapCtrl.map.getCameraPosition().zoom;
    resolution = resolution < 1 ? 1 : resolution;
    resolution = resolution > MAX_GEOCELL_RESOLUTION ? MAX_GEOCELL_RESOLUTION : resolution;
  }

  @Override
  protected HashMap<String, List<JSONObject>> doInBackground(JSONArray... markerJsonArray) {
    JSONObject markerOptions, position;
    double lat, lng;
    String geocell;
    HashMap<String, List<JSONObject>> geocellHash = new HashMap<String, List<JSONObject>>();
    List<JSONObject> stacks;
    
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
  
  /*
  public  void onPostExecute(HashMap<String, List<JSONObject>> geocellHash) {
    try {
      mMarkerCluster.clear();
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }
  */

  /**
   * https://code.google.com/p/geomodel/source/browse/trunk/geo/geocell.py#370
   * @param latLng
   * @param resolution
   * @return
   */
  private String getGeocell(double lat, double lng, int resolution) {
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
