//
//  Cluster.m
//  DevApp
//
//  Created by Katsumata Masashi on 11/11/14.
//
//

#import "Cluster.h"

@implementation Cluster

- (id)initWithOptions:(GoogleMapsViewController *) mapCtrl commandDelegate:(CDVCommandDelegateImpl *)commandDelegate command:(CDVInvokedUrlCommand*)command{
  if(self == [super init]) {
    self.mapCtrl = mapCtrl;
    self.command = command;
    self.markerHash = [[NSMapTable alloc] init];
    self.clusterMarker = nil;
    self.commandDelegate = commandDelegate;
    self.clusterId = [NSString stringWithFormat:@"cluster_%lu", (unsigned long)self.hash];
    [self.mapCtrl.overlayManager setObject:self forKey:self.clusterId];
  }
  return self;
}

- (void)addMarkerJsonList:(NSArray *) list {
  int markerHashSize = [self.markerHash count];
  NSString *hashCode;
  int cnt = 0;
  for (int i = 0; i < [list count]; i++) {
    MarkerJsonData *markerOption = [list objectAtIndex:i];
    hashCode = [NSString stringWithFormat:@"hash%lu", (unsigned long)markerOption.hash];
    if ([self.markerHash objectForKey:hashCode] == NO) {
      cnt++;
      [self.markerHash setObject:markerOption forKey:hashCode];
    }
  }
  
  if (cnt == 0) {
    return;
  }
  
  if (markerHashSize == 1) {
    if (self.clusterMarker != nil) {
      self.clusterMarker.map = nil;
      self.clusterMarker = nil;
    }
    markerHashSize = 0;
  }
  
  if (markerHashSize != 0) {
    return;
  }
  
  CLLocationCoordinate2D centerLatLng;
  
  NSEnumerator *iterator2 = [self.markerHash keyEnumerator];
  NSString *key = [iterator2 nextObject];
  MarkerJsonData *markerOption = [self.markerHash objectForKey:key];
  centerLatLng = markerOption.position;
  self.clusterMarkerId = [markerOption getMarkerId];

  if ([list count] > 1) {
    self.clusterMarker = [GMSMarker markerWithPosition:centerLatLng];
    NSString *file =@"m1.png";
    if (cnt > 200) {
      file = @"m5.png";
    } else if (cnt > 100) {
      file = @"m4.png";
    } else if (cnt > 50) {
      file = @"m3.png";
    } else if (cnt > 20) {
      file = @"m2.png";
    }
    self.clusterMarker.title = [NSString stringWithFormat:@"cnt = %d", cnt];
    self.clusterMarker.icon = [UIImage imageNamed:file];
    self.clusterMarker.map = self.mapCtrl.map;
  } else {
    [self _implementToMap:@"Marker" methodName:@"createMarker" options:markerOption.options action:@"bind"];
  }
}

- (void)_onClusterEventForIOS:(CDVInvokedUrlCommand*)command {
  NSString *action = [command.arguments objectAtIndex:2];
  NSDictionary *resultJson = [command.arguments objectAtIndex:3];
  self.markerId = [resultJson objectForKey:@"id"];
  NSLog(@"_onClusterEventForIOS cluster=%@ , action=%@, marker = %@", self.clusterId, action, self.markerId);
  
  NSMutableDictionary *result = [NSMutableDictionary dictionary];
  [result setObject:action forKey:@"action"];
  [result setObject:self.clusterMarkerId forKey:@"clusterMarkerId"];
  
  CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
  [pluginResult setKeepCallbackAsBool:YES];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:self.command.callbackId];
}

- (void)remove {
  if (self.markerId != nil) {
    [self _implementToMap:@"Marker" methodName:@"remove" options:self.markerId action:@"unbind"];
  }
  if (self.clusterMarker != nil) {
    self.clusterMarker.map = nil;
    self.clusterMarker = nil;
  }
}

- (void)evalJsHelper:(NSString*)jsString
{
  [self.mapCtrl.webView stringByEvaluatingJavaScriptFromString:jsString];
}

- (void)_implementToMap:(NSString *)className methodName:(NSString *)methodName options:(NSObject *)options action:(NSString *)action
{
  NSString* callbackId = [NSString stringWithFormat:@"%@_%d", className, arc4random()];
  //Add callback(_onClusterEventForIOS)
  NSString* jsString = [NSString
                          stringWithFormat:@"cordova.callbacks['%@']={'success': function(result) {plugin.google.maps.experimental.MarkerCluster._onClusterEventForIOS('%@', '%@',result);}, 'fail': null};", callbackId, self.clusterId, action];
  [self performSelectorOnMainThread:@selector(evalJsHelper:) withObject:jsString waitUntilDone:YES];


  [self _execOtherClassMethod:className
        methodName:methodName
        options:options
        callbackId:callbackId];
}

/**
 * @Private
 * Execute the method of other plugin class internally.
 */
-(void)_execOtherClassMethod:(NSString *)className methodName:(NSString *)methodName options:(NSObject *)options callbackId:(NSString *)callbackId
{
  NSArray* args = [NSArray arrayWithObjects:@"exec", options, nil];
  NSArray* jsonArr = [NSArray arrayWithObjects:callbackId, className, methodName, args, nil];
  CDVInvokedUrlCommand* command2 = [CDVInvokedUrlCommand commandFromJson:jsonArr];
  
  CDVPlugin<MyPlgunProtocol> *pluginClass = [self.mapCtrl.plugins objectForKey:className];
  if (!pluginClass) {
    pluginClass = [[NSClassFromString(className)alloc] initWithWebView:self.mapCtrl.webView];
    if (pluginClass) {
      pluginClass.commandDelegate = self.commandDelegate;
      [pluginClass setGoogleMapsViewController:self.mapCtrl];
      [self.mapCtrl.plugins setObject:pluginClass forKey:className];
    }
  }
  SEL selector = NSSelectorFromString([NSString stringWithFormat:@"%@:", methodName]);
  if ([pluginClass respondsToSelector:selector]){
    [pluginClass performSelectorOnMainThread:selector withObject:command2 waitUntilDone:YES];
  }
}
@end
