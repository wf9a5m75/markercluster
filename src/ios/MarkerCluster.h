//
//  PluginMarkerCluster.h
//  DevApp
//
//  Created by Katsumata Masashi on 11/8/14.
//
//

#import "GoogleMaps.h"
#import "MyPlgunProtocol.h"
#import "MarkerClusterManager.h"
#import "MarkerJsonData.h"
#import "CellLocation.h"
#import "MarkerClusterUtil.h"

@interface MarkerCluster : CDVPlugin<MyPlgunProtocol>
@property (nonatomic, strong) GoogleMapsViewController* mapCtrl;

- (void)createMarkerCluster:(CDVInvokedUrlCommand*)command;
- (void)addMarkerJson:(CDVInvokedUrlCommand*)command;
@end
