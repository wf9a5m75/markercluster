package plugin.google.maps;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginEntry;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import plugin.google.maps.experimental.AsyncCluster;
import plugin.google.maps.experimental.Cluster;
import plugin.google.maps.experimental.MarkerCluster;
import plugin.google.maps.experimental.MarkerClusterUtil;
import plugin.google.maps.experimental.MarkerJsonData;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;

public class PluginMarkerCluster extends MyPlugin {
  private int prevZoom = -1;
  private String prevGeocells = "";
  private CordovaWebView mWebView;
  private AsyncCluster currentTask = null;
  
  @Override
  public void initialize(final CordovaInterface cordova, final CordovaWebView webView) {
    super.initialize(cordova, webView);
    mWebView = webView;
  }
  
  @SuppressWarnings({ "unused", "unchecked" })
  private void createMarkerCluster(JSONArray args, CallbackContext callbackContext) throws JSONException {
    MarkerCluster cluster = new MarkerCluster(this.mWebView, mapCtrl);
    String clusterId = "cluster_" + cluster.hashCode();
    this.objects.put(clusterId, cluster);
    if (prevZoom < 0) {
      prevZoom = (int) this.map.getCameraPosition().zoom;
    }
    
    callbackContext.success(clusterId);
  }
  @SuppressWarnings("unused")
  private void addMarkerJson(JSONArray args, CallbackContext callbackContext) throws JSONException {
    
    String clusterId = args.getString(1);
    final MarkerCluster markerCluster = (MarkerCluster) this.objects.get(clusterId);
    JSONArray markerOptions = args.getJSONArray(2);
    List<MarkerJsonData> markerJsonList = new ArrayList<MarkerJsonData>();
    for (int i = 0; i < markerOptions.length(); i++) {
      markerJsonList.add(new MarkerJsonData(markerOptions.getJSONObject(i)));
    }
    
    markerCluster.setAllMarkerOptions(markerJsonList);
    
    if (currentTask != null) {
      currentTask.cancel(true);
    }
    AsyncCluster asyncTask = new AsyncCluster(markerCluster, callbackContext);
    asyncTask.execute(markerJsonList.toArray(new MarkerJsonData[]{}));
    currentTask = asyncTask;
    /*
    PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
    result.setKeepCallback(true);
    callbackContext.sendPluginResult(result);
    */
  }
  @SuppressWarnings("unused")
  private void refresh(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
    Handler handler = new Handler();
    handler.post(new Runnable() {

      @Override
      public void run() {
        try {
          int zoom = (int) mapCtrl.map.getCameraPosition().zoom;

          int resolution = AsyncCluster.getResolution(zoom);
          LatLngBounds visibleBounds = mapCtrl.map.getProjection().getVisibleRegion().latLngBounds;
          LatLng ne = visibleBounds.northeast;
          LatLng sw = visibleBounds.southwest;
          String neGeocell = MarkerClusterUtil.getGeocell(ne.latitude, ne.longitude, resolution);
          String swGeocell = MarkerClusterUtil.getGeocell(sw.latitude, sw.longitude, resolution);
          String currentGeocells = neGeocell + ":" + swGeocell;
          if (currentGeocells.equals(prevGeocells) == true) {
            return;
          }
          prevGeocells = currentGeocells;
          
          String clusterId = args.getString(1);
          MarkerCluster markerCluster = (MarkerCluster) objects.get(clusterId);
          if (markerCluster == null) {
            return;
          }
          if (prevZoom != zoom) {
            markerCluster.clear();
            prevZoom = zoom;
          }

          if (currentTask != null) {
            currentTask.cancel(true);
          }
          AsyncCluster asyncTask = new AsyncCluster(markerCluster, callbackContext);
          asyncTask.execute(markerCluster.getAllMarkerOptions().toArray(new MarkerJsonData[]{}));
          currentTask = asyncTask;
          
        } catch (JSONException e) {
          e.printStackTrace();
        }
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
      }
      
    });
  }
  
}
