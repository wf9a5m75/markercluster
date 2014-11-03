package plugin.google.maps.experimental;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Point;

import com.google.android.gms.maps.model.LatLng;

public class MarkerJsonData {
  public LatLng position;
  public JSONObject options;
  public Point point;
  private String[] geocells;
  private String markerId;
  
  public MarkerJsonData(JSONObject markerOptions) {
    options = markerOptions;
    JSONObject positionJson;
    List<String> geocellList = new ArrayList<String>();
    try {
      positionJson = markerOptions.getJSONObject("position");
      double lat = positionJson.getDouble("lat");
      double lng = positionJson.getDouble("lng");
      this.position = new LatLng(lat, lng);
      
      for (int i = 0; i < AsyncCluster.MAX_GEOCELL_RESOLUTION; i++) {
        geocellList.add(MarkerClusterUtil.getGeocell(lat, lng, i));
      }
      geocells = geocellList.toArray(new String[]{});
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    try {
      markerId = markerOptions.getString("id");
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }
  
  public String getGeocell(int zoom) {
    zoom = Math.min(zoom, geocells.length - 1);
    return this.geocells[zoom];
  }
  
  public String getMarkerId() {
    return markerId;
  }
}
