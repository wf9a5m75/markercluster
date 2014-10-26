package plugin.google.maps;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

import plugin.google.maps.experimental.AsyncAddMarkerTask;
import plugin.google.maps.experimental.Cluster;
import plugin.google.maps.experimental.MarkerCluster;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;

public class PluginMarkerCluster extends MyPlugin {
  private PluginMarker markerPlugin = null;
  private int prevZoom = -1;
  private CordovaWebView mWebView;
  
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
    
    if (markerPlugin == null) {
      try {
        Field field = mapCtrl.getClass().getDeclaredField("plugins");
        field.setAccessible(true);
        HashMap<String, PluginEntry> plugins = (HashMap<String, PluginEntry>) field.get(mapCtrl);
        if (plugins.containsKey("Marker") == false) {
          Method loadPlugin = mapCtrl.getClass().getDeclaredMethod("loadPlugin", String.class);
          loadPlugin.setAccessible(true);
          loadPlugin.invoke(mapCtrl, "Marker");
          plugins = (HashMap<String, PluginEntry>) field.get(mapCtrl);
        }
        PluginEntry pluginEntry = plugins.get("Marker");
        markerPlugin = (PluginMarker) pluginEntry.plugin;
      } catch (Exception e) {
        e.printStackTrace();
        callbackContext.error(e.getMessage());
        return;
      }
    }
    
    callbackContext.success(clusterId);
  }
  @SuppressWarnings("unused")
  private void addMarkerJson(JSONArray args, CallbackContext callbackContext) throws JSONException {
    
    String clusterId = args.getString(1);
    final MarkerCluster markerCluster = (MarkerCluster) this.objects.get(clusterId);
    JSONArray markerOptions = args.getJSONArray(2);
    markerCluster.setAllMarkerOptions(markerOptions);
    
    AsyncAddMarkerTask asyncTask = new AsyncAddMarkerTask(markerCluster);
    asyncTask.execute(markerOptions);
    
    PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
    callbackContext.sendPluginResult(result);
  }
  @SuppressWarnings("unused")
  private void refresh(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
    Handler handler = new Handler();
    handler.post(new Runnable() {

      @Override
      public void run() {
        try {
          int zoom = (int) mapCtrl.map.getCameraPosition().zoom;
          String clusterId = args.getString(1);
          MarkerCluster markerCluster = (MarkerCluster) objects.get(clusterId);
          if (prevZoom != zoom) {
            markerCluster.clear();
            prevZoom = zoom;
          }
          
          AsyncAddMarkerTask asyncTask = new AsyncAddMarkerTask(markerCluster);
          asyncTask.execute(markerCluster.getAllMarkerOptions());
          
        } catch (JSONException e) {
          e.printStackTrace();
        }
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        callbackContext.sendPluginResult(result);
      }
      
    });
  }
}
