package plugin.google.maps.experimental;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class MarkerClusterUtil {

  // The maximum *practical* geocell resolution.
  private static final int GEOCELL_GRID_SIZE = 4;
  public static final String GEOCELL_ALPHABET = "0123456789abcdef";
  
  public static LatLngBounds getLatLngBoundsFromGeocell(String geocell) {
    String geoChar;
    double north = 89.9;
    double south = -89.9;
    double east = 179.9;
    double west = -179.9;
    int pos;
    
    double subcell_lng_span, subcell_lat_span;
    int x, y;
    for (int i = 0; i < geocell.length(); i++) {
      geoChar = geocell.substring(i, i + 1);
      pos = GEOCELL_ALPHABET.indexOf(geoChar);
      
      subcell_lng_span = (east - west) / GEOCELL_GRID_SIZE;
      subcell_lat_span = (north - south) / GEOCELL_GRID_SIZE;
      
      x = (int) (Math.floor(pos / 4) % 2 * 2 + pos % 2);
      y = (int) (pos - Math.floor(pos / 4) * 4);
      y = y >> 1;
      y += Math.floor(pos / 4) > 1 ? 2 : 0;
      
      south += subcell_lat_span * y;
      north = south + subcell_lat_span;

      west += subcell_lng_span * x;
      east = west + subcell_lng_span;
    }
    LatLng sw = new LatLng(south, west);
    LatLng ne = new LatLng(north, east);
    return new LatLngBounds(sw, ne);
  }
  /**
   * https://code.google.com/p/geomodel/source/browse/trunk/geo/geocell.py#370
   * @param latLng
   * @param resolution
   * @return
   */
  public static String getGeocell(double lat, double lng, int resolution) {
    String cell = "";
    double north = 89.9;
    double south = -89.9;
    double east = 179.9;
    double west = -179.9;
    
    double subcell_lng_span, subcell_lat_span;
    byte x, y;
    while(cell.length() < resolution) {
      subcell_lng_span = (east - west) / GEOCELL_GRID_SIZE;
      subcell_lat_span = (north - south) / GEOCELL_GRID_SIZE;

      x = (byte) Math.min(Math.floor(GEOCELL_GRID_SIZE * (lng - west) / (east - west)), GEOCELL_GRID_SIZE - 1);
      y = (byte) Math.min(Math.floor(GEOCELL_GRID_SIZE * (lat - south) / (north - south)),GEOCELL_GRID_SIZE - 1);
      cell += _subdiv_char(x, y);
      

      south += subcell_lat_span * y;
      north = south + subcell_lat_span;

      west += subcell_lng_span * x;
      east = west + subcell_lng_span;
    }
    return cell;
  }

  /**
   * Returns the geocell character in the 4x4 alphabet grid at pos. (x, y).
   * @return
   */
  private static String _subdiv_char(byte posX, byte posY) {
    // NOTE: This only works for grid size 4.
    int start = ((posY & 2) << 2 |
        (posX & 2) << 1 |
        (posY & 1) << 1 |
        (posX & 1) << 0);
    
    return GEOCELL_ALPHABET.substring(start, start + 1);
  }
}
