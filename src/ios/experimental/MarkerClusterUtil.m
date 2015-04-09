//
//  MarkerClusterUtil.m
//  DevApp
//
//  Created by Katsumata Masashi on 11/9/14.
//
//

#import "MarkerClusterUtil.h"

@implementation MarkerClusterUtil
const NSString *GEOCELL_ALPHABET = @"0123456789abcdef";
const int GEOCELL_GRID_SIZE = 4;

+ (NSObject *)getLatLngBoundsFromGeocell:(NSString *) geocell {
  NSString *geoChar;
  double north = 89.9;
  double south = -89.9;
  double east = 179.9;
  double west = -179.9;

  double subcell_lng_span, subcell_lat_span;
  int x, y, pos;
  NSRange range;
  
  for (int i = 0; i < [geocell length]; i++) {
    geoChar = [geocell substringWithRange:NSMakeRange(i, 1)];
    range = [GEOCELL_ALPHABET rangeOfString:geoChar];
    pos = (int)range.location;
    
    subcell_lng_span = (east - west) / GEOCELL_GRID_SIZE;
    subcell_lat_span = (north - south) / GEOCELL_GRID_SIZE;
    
    x = (int) ((int)floor(pos / 4) % 2 * 2 + pos % 2);
    y = (int) (pos - floor(pos / 4) * 4);
    y = y >> 1;
    y += floor(pos / 4) > 1 ? 2 : 0;
    
    south += subcell_lat_span * y;
    north = south + subcell_lat_span;

    west += subcell_lng_span * x;
    east = west + subcell_lng_span;
  }
  
  NSObject *result = [[NSObject alloc] init];
  NSObject *southWest = [[NSObject alloc] init];
  [southWest setValue:[NSNumber numberWithDouble:south] forKey:@"latitude"];
  [southWest setValue:[NSNumber numberWithDouble:west] forKey:@"longitude"];
  [result setValue:southWest forKey:@"southwest"];

  NSObject *northEast = [[NSObject alloc] init];
  [northEast setValue:[NSNumber numberWithDouble:north] forKey:@"latitude"];
  [northEast setValue:[NSNumber numberWithDouble:east] forKey:@"longitude"];
  [result setValue:northEast forKey:@"northeast"];
  
  return result;
}


/**
 * https://code.google.com/p/geomodel/source/browse/trunk/geo/geocell.py#370
 * @param latLng
 * @param resolution
 * @return
 */
+ (NSString *)getGeocell:(double) lat lng:(double)lng resolution:(int)resolution {
  NSString *cell = @"";
  double north = 89.9;
  double south = -89.9;
  double east = 179.9;
  double west = -179.9;
  double subcell_lng_span, subcell_lat_span;
  
  int x, y;
  while([cell length] < resolution) {
    subcell_lng_span = (east - west) / GEOCELL_GRID_SIZE;
    subcell_lat_span = (north - south) / GEOCELL_GRID_SIZE;

    x = MIN(floor(GEOCELL_GRID_SIZE * (lng - west) / (east - west)), GEOCELL_GRID_SIZE - 1);
    y = MIN(floor(GEOCELL_GRID_SIZE * (lat - south) / (north - south)),GEOCELL_GRID_SIZE - 1);
    
    cell = [NSString stringWithFormat:@"%@%@",
              cell, [MarkerClusterUtil _subdiv_char:x posY:y]];
    
    south += subcell_lat_span * y;
    north = south + subcell_lat_span;

    west += subcell_lng_span * x;
    east = west + subcell_lng_span;
  }
  return cell;

}

+ (NSString *)_subdiv_char:(int) posX posY:(int)posY {
  // NOTE: This only works for grid size 4.
  int start = ((posY & 2) << 2 |
        (posX & 2) << 1 |
        (posY & 1) << 1 |
        (posX & 1) << 0);
  
  return [GEOCELL_ALPHABET substringWithRange:NSMakeRange(start, 1)];
}
+ (NSArray *)getKeys:(NSMapTable *) hashMap {
  NSEnumerator *enumulator = [hashMap keyEnumerator];
  NSMutableArray *keys = [NSMutableArray array];
  NSString *key;
  while ((key = [enumulator nextObject])) {
    [keys addObject:key];
  }
  return keys;
}

@end
