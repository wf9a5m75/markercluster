//
//  MarkerCluster.m
//  DevApp
//
//  Created by Katsumata Masashi on 11/9/14.
//
//

#import "MarkerClusterManager.h"

@implementation MarkerClusterManager
NSMutableArray *allMarkerOptionList = nil;

- (id)initWithOptions:(GoogleMapsViewController *) mapCtrl {
  if(self == [super init]) {
    self.mapCtrl = mapCtrl;
  }
  allMarkerOptionList = [NSMutableArray array];
  return self;
}

- (void)setAllMarkerOptions:(NSArray *) allMarkerOption {
  [allMarkerOptionList addObjectsFromArray:allMarkerOption];
}

- (NSArray *)getAllMarkerOptions {
  return allMarkerOptionList;
}


- (void)clean {
  //TODO: Implement this method later
}

@end
