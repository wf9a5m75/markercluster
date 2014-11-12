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
#import "Cluster.h"

@interface MarkerCluster : CDVPlugin<MyPlgunProtocol>
@property (nonatomic, strong) GoogleMapsViewController* mapCtrl;
@property (nonatomic, strong) NSMapTable* clusters;

- (void)createMarkerCluster:(CDVInvokedUrlCommand*)command;
- (void)addMarkerJson:(CDVInvokedUrlCommand*)command;
- (void)_onClusterEventForIOS:(CDVInvokedUrlCommand*)command;
@end
