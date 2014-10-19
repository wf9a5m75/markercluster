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

  private List<JSONObject> markerOptionList = new ArrayList<JSONObject>();
  private GoogleMap mMap;
  private GoogleMaps mMapCtrl;
  private Marker clusterMarker = null;
  private CordovaWebView mWebView;
  private String firstMarkerId = null;
  
  public Cluster(CordovaWebView webView, GoogleMaps mapCtrl) {
    mMap = mapCtrl.map;
    mMapCtrl = mapCtrl;
    mWebView = webView;
  };
  
  public void addMarkerJson(JSONObject markerOptions) throws JSONException {
    markerOptionList.add(markerOptions);
    if (markerOptionList.size() == 1) {
      JSONArray params = new JSONArray();
      params.put("Marker.createMarker");
      params.put(markerOptions);
      this.implementToMap(params, new CallbackContext("111", mWebView) {
        @Override
        public void success(JSONObject message) {
          try {
            firstMarkerId = message.getString("id");
          } catch (JSONException e) {
            e.printStackTrace();
          }
          
        }
      });
      //Log.d("GoogleMaps", markerOptions.toString(2));
    } else {
      if (firstMarkerId != null) {
        JSONArray params = new JSONArray();
        params.put("Marker.remove");
        params.put(firstMarkerId);
        this.implementToMap(params, new CallbackContext("222", mWebView));
        firstMarkerId = null;
      }
      
      JSONObject position = markerOptions.getJSONObject("position");
      double lat = position.getDouble("lat");
      double lng = position.getDouble("lng");
      LatLng markerLatLng = new LatLng(lat, lng);

      if (clusterMarker == null) {
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
        //clusterMarker.setTitle("cluster_" + this.hashCode());
        clusterMarker.setPosition(markerCenter);
        
      }
      clusterMarker.setTitle("Cnt = " + markerOptionList.size());
    }
  }

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
