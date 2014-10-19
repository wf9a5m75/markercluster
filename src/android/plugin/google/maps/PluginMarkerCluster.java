package plugin.google.maps;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginEntry;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import plugin.google.maps.experimental.MarkerCluster;

import com.google.android.gms.maps.model.Marker;

public class PluginMarkerCluster extends MyPlugin {
  private PluginMarker markerPlugin = null;
  private int prevZoom = -1;
  
  @SuppressWarnings({ "unused", "unchecked" })
  private void createMarkerCluster(JSONArray args, CallbackContext callbackContext) throws JSONException {
    MarkerCluster cluster = new MarkerCluster(map);
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
  private void addMarker(JSONArray args, CallbackContext callbackContext) throws JSONException {
    String clusterId = args.getString(1);
    
    MarkerCluster cluster = (MarkerCluster) this.objects.get(clusterId);
    
    String markerId = args.getString(2);

    Marker marker = markerPlugin.getMarker(markerId);
    cluster.addMarker(marker, false);
    
    PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
    callbackContext.sendPluginResult(result);
  }
  @SuppressWarnings("unused")
  private void refresh(JSONArray args, CallbackContext callbackContext) throws JSONException {
    int zoom = (int) this.map.getCameraPosition().zoom;
    if (zoom != prevZoom) {
      prevZoom = zoom;
      String clusterId = args.getString(1);
      MarkerCluster cluster = (MarkerCluster) this.objects.get(clusterId);
      cluster.refresh();
    }
    PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
    callbackContext.sendPluginResult(result);
  }
}
