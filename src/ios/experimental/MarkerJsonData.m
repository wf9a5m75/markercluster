//
//  MarkerJsonData.m
//  DevApp
//
//  Created by Katsumata Masashi on 11/9/14.
//
//

#import "MarkerJsonData.h"

@implementation MarkerJsonData

- (id)initWithOptions:(NSDictionary *) markerOptions {
  if(self == [super init]) {
    self.options = markerOptions;
    
    NSMutableArray *geocellList = [NSMutableArray array];
    
    NSObject *positionJson = [markerOptions valueForKey:@"position"];
    if (positionJson) {
      self.position = CLLocationCoordinate2DMake([[positionJson valueForKey:@"lat"] doubleValue], [[positionJson valueForKey:@"lng"] doubleValue]);
      
      for (int i = 0; i < MAX_GEOCELL_RESOLUTION; i++) {
        [geocellList addObject:[MarkerClusterUtil
                                      getGeocell:self.position.latitude lng:self.position.longitude resolution:i]];
      }
      self.geocells = [geocellList copy];
    }
    
    self.markerId = [markerOptions valueForKey:@"id"];
  }
  return self;
}

- (NSString *)getGeocell:(int) zoom {
  zoom = MIN(zoom, [self.geocells count] - 1);
  return [self.geocells objectAtIndex:zoom];
}

- (NSString *)getMarkerId {
  return self.markerId;
}
@end
