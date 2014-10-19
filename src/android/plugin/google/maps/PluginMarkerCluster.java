package plugin.google.maps;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginEntry;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import plugin.google.maps.experimental.MarkerCluster;

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
    MarkerCluster cluster = (MarkerCluster) this.objects.get(clusterId);
    
    JSONObject markerOptions = args.getJSONObject(2);
    
    cluster.addMarkerJson(markerOptions, null, false);
    
    PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
    callbackContext.sendPluginResult(result);
  }
  @SuppressWarnings("unused")
  private void refresh(JSONArray args, CallbackContext callbackContext) throws JSONException {
    int zoom = (int) this.map.getCameraPosition().zoom;
    
      prevZoom = zoom;
      String clusterId = args.getString(1);
      MarkerCluster cluster = (MarkerCluster) this.objects.get(clusterId);
      if (zoom != prevZoom) {
        cluster.clear();
      }
      cluster.refresh();
    PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
    callbackContext.sendPluginResult(result);
  }
}
