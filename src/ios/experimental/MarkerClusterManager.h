//
//  MarkerCluster.h
//  DevApp
//
//  Created by Katsumata Masashi on 11/9/14.
//
//

#import "GoogleMaps.h"
#import <Foundation/Foundation.h>

@interface MarkerClusterManager : NSObject
@property (nonatomic, strong) GoogleMapsViewController* mapCtrl;
- (id)initWithOptions:(GoogleMapsViewController *) mapCtrl;
- (void)setAllMarkerOptions:(NSArray *) allMarkerOption;
- (NSArray *)getAllMarkerOptions;
- (void)clean;

@end
