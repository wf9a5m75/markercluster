package plugin.google.maps.experimental;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.cordova.CordovaWebView;
import org.json.JSONException;

import plugin.google.maps.GoogleMaps;
import android.graphics.Bitmap;

public class MarkerCluster {

  private List<MarkerJsonData> allMarkerOptionList;
  public HashMap<String, Cluster> clusters = new HashMap<String, Cluster>();
  
  public CordovaWebView mWebView = null;
  public GoogleMaps mapCtrl;
  
  public MarkerCluster(CordovaWebView webView, GoogleMaps mapCtrl) {
    this.mWebView = webView;
    this.mapCtrl = mapCtrl;
  }
  

  public void setAllMarkerOptions(List<MarkerJsonData> allMarkerOption) {
    if (allMarkerOptionList == null) {
      allMarkerOptionList = allMarkerOption;
    } else {
      allMarkerOptionList.addAll(allMarkerOption);
    }
  }
  public List<MarkerJsonData> getAllMarkerOptions() {
    return allMarkerOptionList;
  }
  

  public void clear() throws JSONException {
    Set<String> keys = clusters.keySet();
    Iterator<String> iterator = keys.iterator();
    Cluster cluster;
    String geocell;
    while(iterator.hasNext()) {
      geocell = iterator.next();
      cluster = clusters.get(geocell);
      cluster.remove();
    }
    clusters.clear();
  }

}
