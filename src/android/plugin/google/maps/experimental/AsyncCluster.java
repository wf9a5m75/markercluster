package plugin.google.maps.experimental;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.cordova.CallbackContext;

import plugin.google.maps.PluginMarker;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.TimingLogger;

import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class AsyncCluster extends AsyncTask<MarkerJsonData, Void, HashMap<String, List<MarkerJsonData>>> {

  private int resolution = 1;
  private HashMap<String, CellLocation> geocellLocations = new HashMap<String, CellLocation>();

  public static final int MAX_GEOCELL_RESOLUTION = 13;
  private MarkerCluster markerCluster;
  private LatLngBounds visibleBounds;
  private Projection projection;
  private float density;
  private float clusterDistance = 0;
  private TimingLogger logger;
  private CallbackContext callbackContext;
  
  public AsyncCluster(MarkerCluster markerCluster, CallbackContext callbackContext) {
    logger = new TimingLogger("Marker", "testTimingLogger");
    
    this.markerCluster = markerCluster;
    this.callbackContext = callbackContext;
    
    projection = markerCluster.mapCtrl.map.getProjection();
    visibleBounds = projection.getVisibleRegion().latLngBounds;
    
    LatLng ne = visibleBounds.northeast;
    LatLng sw = visibleBounds.southwest;
    String neGeocell = MarkerClusterUtil.getGeocell(ne.latitude, ne.longitude, resolution);
    String swGeocell = MarkerClusterUtil.getGeocell(sw.latitude, sw.longitude, resolution);
    
    LatLngBounds neBounds = MarkerClusterUtil.getLatLngBoundsFromGeocell(neGeocell);
    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    builder.include(neBounds.northeast);
    builder.include(neBounds.southwest);
    LatLngBounds swBounds = MarkerClusterUtil.getLatLngBoundsFromGeocell(swGeocell);
    builder.include(swBounds.northeast);
    builder.include(swBounds.southwest);
    visibleBounds = builder.build();
    
    int zoom = (int) markerCluster.mapCtrl.map.getCameraPosition().zoom;
    resolution = AsyncCluster.getResolution(zoom);
    
    density = Resources.getSystem().getDisplayMetrics().density;
    clusterDistance = 40 * density;
  }
  

  public static final int getResolution(int zoom) {
    zoom = zoom - 1;
    int resolution = zoom < 1 ? 1 : zoom;
    resolution = resolution > MAX_GEOCELL_RESOLUTION ? MAX_GEOCELL_RESOLUTION : resolution;
    return resolution;
  }

  @Override
  protected HashMap<String, List<MarkerJsonData>> doInBackground(MarkerJsonData... markerJsons) {
    MarkerJsonData markerOptions;
    String geocell, clusterCell;
    HashMap<String, List<MarkerJsonData>> geocellHash = new HashMap<String, List<MarkerJsonData>>();
    List<MarkerJsonData> stacks;
    LatLng latLng;
    Point cellPoint, clusterPoint;
    CellLocation cellLoc, clusterLoc;

    logger.addSplit("createCell ");
    //-------------------------------------
    // marker clustering based on geocell
    //-------------------------------------
    for (int j = 0; j < markerJsons.length; j++) {
      try {
        markerOptions = markerJsons[j];

        latLng = markerOptions.position;
        if (visibleBounds.contains(latLng) == false) {
          continue;
        }
        
        geocell = markerOptions.getGeocell(resolution);
        if (geocellHash.containsKey(geocell) == false) {
          stacks = new ArrayList<MarkerJsonData>();
          cellLoc = new CellLocation();
          cellLoc.latLng = latLng;
          cellLoc.point = projection.toScreenLocation(latLng);
          geocellLocations.put(geocell, cellLoc);
        } else {
          stacks = geocellHash.get(geocell);
        }
        stacks.add(markerOptions);
        geocellHash.put(geocell, stacks);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    logger.addSplit("cluster optimize");
    Iterator<String> iterator;
    String[] geocells = geocellLocations.keySet().toArray(new String[]{});
    LatLngBounds unionCellBounds;
    HashMap<String, List<MarkerJsonData>> unionCells;
    int i = 0;
    boolean needRetry = true;
    while (needRetry) {
      needRetry = false;
      for (i = 0; i < geocells.length; i++) {
        geocell = geocells[i];
        if (geocellLocations.containsKey(geocell) == false) {
          continue;
        }
        cellLoc = geocellLocations.get(geocell);
        cellPoint = cellLoc.point;
        
        unionCellBounds = null;
        unionCells = null;
        iterator = geocellLocations.keySet().iterator();
        while (iterator.hasNext()) {
          clusterCell = iterator.next();
          if (clusterCell.equals(geocell)) {
            continue;
          }
          clusterLoc = geocellLocations.get(clusterCell);
          clusterPoint = clusterLoc.point;
          if (cellPoint.x >= clusterPoint.x - clusterDistance &&
              cellPoint.x <= clusterPoint.x + clusterDistance &&
              cellPoint.y >= clusterPoint.y - clusterDistance &&
              cellPoint.y <= clusterPoint.y + clusterDistance) {
            
            if (unionCellBounds == null) {
              LatLngBounds.Builder builder = new LatLngBounds.Builder();
              builder.include(clusterLoc.latLng);
              builder.include(cellLoc.latLng);
              unionCellBounds = builder.build();
              
              unionCells = new HashMap<String, List<MarkerJsonData>>();
              unionCells.put(geocell, geocellHash.get(geocell));
            }
            unionCells.put(clusterCell, geocellHash.get(clusterCell));
            unionCellBounds.including(clusterLoc.latLng);
          }
        }
        
        if (unionCells != null) {
          latLng = unionCellBounds.getCenter();
          clusterCell = MarkerClusterUtil.getGeocell(latLng.latitude, latLng.longitude, resolution);
  
          List<MarkerJsonData> buffer = new ArrayList<MarkerJsonData>();
          iterator = unionCells.keySet().iterator();
          while(iterator.hasNext()) {
            geocell = iterator.next();
            geocellLocations.remove(geocell);
            geocellHash.remove(geocell);
            buffer.addAll(unionCells.get(geocell));
          }
  
          needRetry = true;
          geocellHash.put(clusterCell, buffer);
        }
      }
      geocells = geocellHash.keySet().toArray(new String[]{});
    }

    return geocellHash;
  }
  
  @Override
  public void onPostExecute(HashMap<String, List<MarkerJsonData>> geocellHash) {
    logger.addSplit("onPost");
    String geocell;
    Cluster cluster;
    String[] geocells = markerCluster.clusters.keySet().toArray(new String[]{});
    for (int i = 0; i < geocells.length; i++) {
      geocell = geocells[i];
      if (geocellHash.containsKey(geocell) == false) {
        cluster = markerCluster.clusters.remove(geocell);
        cluster.remove();
      }
    }
    
    logger.addSplit("addClusters");
    Iterator<String> iterator = geocellHash.keySet().iterator();
    while(iterator.hasNext()) {
      geocell = iterator.next();
      if (this.markerCluster.clusters.containsKey(geocell)) {
        cluster = markerCluster.clusters.get(geocell);
        cluster.addMarkerJsonList(geocellHash.get(geocell));
      } else {
        cluster = new Cluster(markerCluster.mapCtrl, callbackContext);
        cluster.addMarkerJsonList(geocellHash.get(geocell));
        markerCluster.clusters.put(geocell, cluster);
      }
    }
    
    logger.dumpToLog();
  }

  
  class CellLocation {
    public LatLng latLng;
    public Point point;
  }
}
