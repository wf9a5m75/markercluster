package plugin.google.maps.experimental;

import java.util.List;

import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import plugin.google.maps.GoogleMaps;
import android.os.AsyncTask;
import android.os.Bundle;

public class AsyncAddMarkerTask extends AsyncTask<JSONObject, Void, List<Bundle>> {

  private MarkerCluster mMarkerCluster;
  private GoogleMaps mMapCtrl;
  private boolean mIsRefresh = false;
  
  // The maximum *practical* geocell resolution.
  private final int MAX_GEOCELL_RESOLUTION = 13;
  private final int GEOCELL_GRID_SIZE = 4;
  private final String GEOCELL_ALPHABET = "0123456789abcdef";
  
  public AsyncAddMarkerTask(MarkerCluster markerCluster, GoogleMaps mapCtrl, boolean isRefresh) {
    mMarkerCluster = markerCluster;
    mMapCtrl = mapCtrl;
    mIsRefresh = isRefresh;
  }

  @Override
  protected List<Bundle> doInBackground(JSONObject... markerJsonArray) {
    JSONObject markerOptions, position;
    double lat, lng;
    LatLng markerLatLng;
    
    //---------------------
    // calculate geocells
    //---------------------
    for (int i = 0; i < markerJsonArray.length; i++) {
      try {
        markerOptions = markerJsonArray[i];

        position = markerOptions.getJSONObject("position");
        lat = position.getDouble("lat");
        lng = position.getDouble("lng");
        markerLatLng = new LatLng(lat, lng);
        
        mMarkerCluster.addMarkerJson(markerOptions, markerLatLng, mIsRefresh);
        
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return null;
  }
  

  /**
   * https://code.google.com/p/geomodel/source/browse/trunk/geo/geocell.py#370
   * @param latLng
   * @param resolution
   * @return
   */
  private String getGeocell(LatLng latLng, int resolution) {
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
