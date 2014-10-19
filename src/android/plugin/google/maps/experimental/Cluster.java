package plugin.google.maps.experimental;

import java.util.ArrayList;
import java.util.List;

import com.example.myapp.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Cluster {
  
  private List<Marker> markers = new ArrayList<Marker>();
  private GoogleMap mMap;
  private Marker clusterMarker = null;
  private LatLng centerLatLng = null;
  
  public Cluster(GoogleMap map) {
    mMap = map;
  };
  
  public void addMarker(Marker marker) {
    if (markers.indexOf(marker) > -1) {
      return;
    }

    markers.add(marker);
    LatLng latLng = marker.getPosition();
    if (centerLatLng == null) {
      centerLatLng = latLng;
      marker.setVisible(true);
      
    } else {
      marker.setVisible(false);

      if (clusterMarker == null) {
        MarkerOptions opts = new MarkerOptions();
        opts.position(latLng);
        opts.icon(BitmapDescriptorFactory.fromResource(R.drawable.m1));
        opts.anchor(0.5f, 0.5f);
        
        markers.get(0).setVisible(false);
        clusterMarker = mMap.addMarker(opts);
        clusterMarker.setTitle("cluster_" + this.hashCode());
        clusterMarker.setPosition(markers.get(0).getPosition());
        
      }
      //clusterMarker.setTitle("Cnt = " + markers.size());
    }
  }

  public void remove() {
    if (clusterMarker != null) {
      clusterMarker.remove();
      clusterMarker = null;
    }
  }
  
  public LatLng getCenter() {
    return centerLatLng;
  }
}
