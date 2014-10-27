package plugin.google.maps.experimental;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.cordova.CordovaWebView;
import org.json.JSONException;
import org.json.JSONObject;

import plugin.google.maps.GoogleMaps;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.example.myapp.R;
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
  
  public Cluster(GoogleMaps mapCtrl) {
    this.mapCtrl = mapCtrl;
    
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
      clusterMarker.remove();
      markerHashSize = 0;
    }
    
    if (markerHashSize == 0) {
      Iterator<Integer> iterator2 = markerHash.keySet().iterator();
      int jsonHash = iterator2.next();
      markerOption = markerHash.get(jsonHash);
      centerLatLng = markerOption.position;
      
      MarkerOptions opts = new MarkerOptions();
      opts.position(centerLatLng);
      if (list.size() > 1) {
        int markerId = R.drawable.m1;
        cnt = markerHash.size();
        markerId = cnt > 20 ? R.drawable.m2 : markerId;
        markerId = cnt > 50 ? R.drawable.m3 : markerId;
        markerId = cnt > 100 ? R.drawable.m4 : markerId;
        markerId = cnt > 200 ? R.drawable.m5 : markerId;
        Bitmap iconBitmap = BitmapFactory.decodeResource(mapCtrl.cordova.getActivity().getResources(), markerId);
        Canvas iconCanvas = new Canvas(iconBitmap);
        
        String txt = "" + markerHash.size();
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(20);
        float txtWidth = paint.measureText(txt, 0, txt.length());
        int xPos = (int) ((iconCanvas.getWidth() - txtWidth) / 2);
        int yPos = (int) ((iconCanvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ; 
        iconCanvas.drawText(txt, xPos, yPos, paint);
        opts.icon(BitmapDescriptorFactory.fromBitmap(iconBitmap));
        opts.anchor(0.5f, 0.5f);
      } else {
        JSONObject options = markerOption.options;
        try {
          String title = options.getString("title");
          opts.title(title);
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
      clusterMarker = mapCtrl.map.addMarker(opts);
    }
  }
  
  public void remove() {
    if (clusterMarker != null) {
      clusterMarker.remove();
      clusterMarker = null;
    }
  }
}
