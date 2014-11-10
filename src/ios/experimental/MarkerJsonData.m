//
//  MarkerJsonData.m
//  DevApp
//
//  Created by Katsumata Masashi on 11/9/14.
//
//

#import "MarkerJsonData.h"

@implementation MarkerJsonData
NSString *markerId;

- (id)initWithOptions:(NSObject *) markerOptions {
  if(self == [super init]) {
    self.options = markerOptions;
    
    NSMutableArray *geocellList = [NSMutableArray array];
    
    NSObject *positionJson = [markerOptions valueForKey:@"position"];
    if (positionJson) {
      self.latitude = [NSNumber numberWithDouble: [[positionJson valueForKey:@"lat"] doubleValue]];
      self.longitude = [NSNumber numberWithDouble: [[positionJson valueForKey:@"lng"] doubleValue]];
      
      for (int i = 0; i < MAX_GEOCELL_RESOLUTION; i++) {
        [geocellList addObject:[MarkerClusterUtil
                                      getGeocell:[self.latitude doubleValue] lng:[self.longitude doubleValue] resolution:i]];
      }
      self.geocells = [geocellList copy];
    }
    
    markerId = [markerOptions valueForKey:@"id"];
  }
  return self;
}

- (NSString *)getGeocell:(int) zoom {
  zoom = MIN(zoom, [self.geocells count] - 1);
  return [self.geocells objectAtIndex:zoom];
}

- (NSString *)getMarkerId {
  return markerId;
}
@end
