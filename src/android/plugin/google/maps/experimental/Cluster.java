package plugin.google.maps.experimental;

import java.util.ArrayList;
import java.util.List;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import plugin.google.maps.GoogleMaps;
import android.util.Log;

import com.example.myapp.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Cluster {

  private List<JSONObject> markerOptionList;
  private GoogleMap mMap;
  private GoogleMaps mMapCtrl;
  private Marker clusterMarker = null;
  private CordovaWebView mWebView;
  private String firstMarkerId = null;
  
  public Cluster(GoogleMaps mapCtrl, String geocell, List<JSONObject> list) {
    mMap = mapCtrl.map;
    mMapCtrl = mapCtrl;
    markerOptionList = list;

    JSONObject firstMarkerOptions = list.get(0);

    try {
      MarkerOptions opts = new MarkerOptions();
      JSONObject position = firstMarkerOptions.getJSONObject("position");
      double lat = position.getDouble("lat");
      double lng = position.getDouble("lng");
      LatLng markerLatLng = new LatLng(lat, lng);
      opts.position(markerLatLng);
      if (list.size() > 1) {
        opts.icon(BitmapDescriptorFactory.fromResource(R.drawable.m1));
        opts.anchor(0.5f, 0.5f);
      }
      clusterMarker = mapCtrl.map.addMarker(opts);
      clusterMarker.setTitle("Cnt = " + list.size());
      
    } catch (Exception e) {};
     
  };
  

  public void remove() {
    if (firstMarkerId != null) {
      JSONArray params = new JSONArray();
      params.put("Marker.remove");
      params.put(firstMarkerId);
      this.implementToMap(params, new CallbackContext("222", mWebView));
      firstMarkerId = null;
    }
    if (clusterMarker != null) {
      clusterMarker.remove();
      clusterMarker = null;
    }
  }

  private void implementToMap(JSONArray params, CallbackContext callbackContext)  {
    try {
      mMapCtrl.execute("exec", params, callbackContext);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }
}
