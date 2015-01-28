package plugin.google.maps.experimental;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import plugin.google.maps.GoogleMaps;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Cluster {

  private HashMap<Integer, MarkerJsonData> markerHash = new HashMap<Integer, MarkerJsonData>();
  private GoogleMaps mapCtrl;
  private Marker clusterMarker = null;
  private CordovaWebView mWebView;
  private LatLng centerLatLng = null;
  private Bitmap currentIconBitmap = null;
  private String markerId = null;
  private String clusterMarkerId = null;
  private CallbackContext callbackContext;
  private ClusterIcon[] icons;
  
  public Cluster(GoogleMaps mapCtrl, ClusterIcon[] icons, CallbackContext callbackContext) {
    this.mapCtrl = mapCtrl;
    this.mWebView = mapCtrl.webView;
    this.callbackContext = callbackContext;
    this.icons = icons;
  }
  
  public void addMarkerJsonList(List<MarkerJsonData> list) {
    int markerHashSize = markerHash.size();
    MarkerJsonData markerOption;
    Iterator<MarkerJsonData> iterator = list.iterator();
    int cnt = 0;
    while (iterator.hasNext()) {
      markerOption = iterator.next();
      if (markerHash.containsKey(markerOption.hashCode()) == false) {
        cnt++;
        this.markerHash.put(markerOption.hashCode(), markerOption);
      }
    }
    if (cnt == 0) {
      return;
    }

    if (markerHashSize == 1) {
      if (clusterMarker != null) {
        clusterMarker.remove();
      }
      markerHashSize = 0;
    }

    if (markerHashSize != 0) {
      return;
    }
    
    Iterator<Integer> iterator2 = markerHash.keySet().iterator();
    int jsonHash = iterator2.next();
    markerOption = markerHash.get(jsonHash);
    centerLatLng = markerOption.position;
    clusterMarkerId = markerOption.getMarkerId();
    
    MarkerOptions opts = new MarkerOptions();
    opts.position(centerLatLng);
    if (list.size() > 1) {
      cnt = list.size();
      ClusterIcon icon, matchIcon = null;
      for (int i = 0; i < icons.length; i++) {
        icon = icons[i];
        if (cnt <= icon.maxCnt) {
          matchIcon = icon;
          Log.d("Marker",  list.size() + " <= " + icon.maxCnt + " -> " + i);
          break;
        }
      }
      if (matchIcon == null) {
        return;
      }
      if (matchIcon.iconData == null) {
        return;
      }
      if (currentIconBitmap != null) {
        currentIconBitmap.recycle();
      }
      this.onIconUpdate();
      /*
      currentIconBitmap = BitmapFactory.decodeByteArray(matchIcon.iconData, 0, matchIcon.iconData.length);
      Canvas iconCanvas = new Canvas(currentIconBitmap);
      
      String txt = "" + markerHash.size();
      Paint paint = new Paint();
      paint.setColor(Color.WHITE);
      paint.setTextSize(20);
      float txtWidth = paint.measureText(txt, 0, txt.length());
      int xPos = (int) ((iconCanvas.getWidth() - txtWidth) / 2);
      int yPos = (int) ((iconCanvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ; 
      iconCanvas.drawText(txt, xPos, yPos, paint);
      opts.icon(BitmapDescriptorFactory.fromBitmap(currentIconBitmap));
      opts.anchor(0.5f, 0.5f);
      clusterMarker = mapCtrl.map.addMarker(opts);
      */
    } else {
      JSONObject options = markerOption.options;
      JSONArray args = new JSONArray();
      args.put("Marker.createMarker");
      args.put(options);
      try {
        mapCtrl.execute("exec", args, new CallbackContext(null, mWebView) {
          public void sendPluginResult(PluginResult pluginResult) {
            try {
              if (pluginResult.getMessageType() == PluginResult.MESSAGE_TYPE_JSON) {
                JSONObject result = new JSONObject(pluginResult.getMessage());
                Cluster.this.markerId = result.getString("id");
                result.put("action", "bind");
                result.put("clusterMarkerId", clusterMarkerId);
                pluginResult = new PluginResult(PluginResult.Status.OK, result);
                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
              }
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        });
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }
  
  public void remove() {
    if (this.currentIconBitmap != null) {
      currentIconBitmap.recycle();
    }
    if (markerId != null) {
      JSONArray args = new JSONArray();
      args.put("Marker.remove");
      args.put(markerId);
      try {
        mapCtrl.execute("exec", args, new CallbackContext(null, mWebView) {
          public void sendPluginResult(PluginResult pluginResult) {
            JSONObject result = new JSONObject();
            try {
              result.put("action", "unbind");
              result.put("clusterMarkerId", clusterMarkerId);
              result.put("id", markerId);
            } catch (JSONException e) {
              e.printStackTrace();
            }
            pluginResult = new PluginResult(PluginResult.Status.OK, result);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
          }
        });
      } catch (JSONException e) {
        e.printStackTrace();
      }
      
    }
    if (currentIconBitmap != null) {
      currentIconBitmap.recycle();
    }
    if (clusterMarker != null) {
      clusterMarker.remove();
      clusterMarker = null;
    }
  }

  public void onIconUpdate() {
    if (this.icons != null && this.currentIconBitmap == null) {
      int cnt = markerHash.size();
      ClusterIcon icon, matchIcon = null;
      for (int i = 0; i < icons.length; i++) {
        icon = icons[i];
        if (cnt <= icon.maxCnt) {
          matchIcon = icon;
        }
      }
      Bitmap iconBmp = BitmapFactory.decodeByteArray(matchIcon.iconData, 0, matchIcon.iconData.length);
      this.currentIconBitmap = iconBmp.copy(Bitmap.Config.ARGB_8888, true);
      iconBmp.recycle();
      Canvas iconCanvas = new Canvas(currentIconBitmap);
      
      String txt = "" + markerHash.size();
      Paint paint = new Paint();
      paint.setColor(Color.WHITE);
      paint.setTextSize(20);
      float txtWidth = paint.measureText(txt, 0, txt.length());
      int xPos = (int) ((iconCanvas.getWidth() - txtWidth) / 2);
      int yPos = (int) ((iconCanvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ; 
      iconCanvas.drawText(txt, xPos, yPos, paint);
      
      BitmapDescriptor bitmapDiscriptor = BitmapDescriptorFactory.fromBitmap(currentIconBitmap);
      if (clusterMarker == null) {
        MarkerOptions opts = new MarkerOptions();
        opts.position(centerLatLng);
        opts.icon(bitmapDiscriptor);
        opts.anchor(0.5f, 0.5f);
        clusterMarker = mapCtrl.map.addMarker(opts);
      } else {
        clusterMarker.setIcon(bitmapDiscriptor);
      }
    }
  }
}
