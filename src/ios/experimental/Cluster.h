//
//  Cluster.h
//  DevApp
//
//  Created by Katsumata Masashi on 11/11/14.
//
//

#import <Foundation/Foundation.h>
#import "GoogleMaps.h"
#import "GoogleMapsViewController.h"
#import "MarkerJsonData.h"

@interface Cluster : NSObject
@property (nonatomic, strong) GoogleMapsViewController* mapCtrl;
@property (nonatomic, strong) CDVInvokedUrlCommand* command;
@property (nonatomic, strong) NSMapTable* markerHash;
@property (nonatomic, strong) GMSMarker* clusterMarker;
@property (nonatomic, strong) CDVCommandDelegateImpl *commandDelegate;
@property (nonatomic, strong) NSString *clusterId;
@property (nonatomic, strong) NSString *clusterMarkerId;
@property (nonatomic, strong) NSString *markerId;

- (id)initWithOptions:(GoogleMapsViewController *) mapCtrl commandDelegate:(CDVCommandDelegateImpl *)commandDelegate command:(CDVInvokedUrlCommand*)command;
- (void)addMarkerJsonList:(NSArray *) list;
- (void)_implementToMap:(NSString *)className methodName:(NSString *)methodName options:(NSObject *)options action:(NSString *)action;
- (void)_execOtherClassMethod:(NSString *)className methodName:(NSString *)methodName options:(NSObject *)options callbackId:(NSString *)callbackId;
- (void)_onClusterEventForIOS:(CDVInvokedUrlCommand*)command;
- (void)remove;
@end
