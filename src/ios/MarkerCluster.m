//
//  PluginMarkerCluster.m
//  DevApp
//
//  Created by Katsumata Masashi on 11/8/14.
//
//

#import "MarkerCluster.h"

@implementation MarkerCluster
int prevZoom = -1;

-(void)setGoogleMapsViewController:(GoogleMapsViewController *)viewCtrl
{
  NSLog(@"---setGoogelMapsViewController");
  self.mapCtrl = viewCtrl;
}

- (void)createMarkerCluster:(CDVInvokedUrlCommand*)command {
  MarkerClusterManager *cluster = [[MarkerClusterManager alloc] initWithOptions:self.mapCtrl];
  
  NSString *id = [NSString stringWithFormat:@"cluster_%lu", (unsigned long)cluster.hash];
  [self.mapCtrl.overlayManager setObject:cluster forKey: id];
  if (prevZoom < 0) {
    prevZoom = self.mapCtrl.map.camera.zoom;
  }
  
  CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:id];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (void)addMarkerJson:(CDVInvokedUrlCommand*)command {
  NSString *clusterId = [command.arguments objectAtIndex:1];
  MarkerClusterManager *markerCluster = (MarkerClusterManager *)[self.mapCtrl.overlayManager objectForKey:clusterId];

  NSArray *markerOptions = [command.arguments objectAtIndex:2];
  NSMutableArray *markerJsonList = [NSMutableArray array];
  NSObject *markerOption;
 
  for (int i = 0; i < [markerOptions count]; i++) {
    markerOption = [markerOptions objectAtIndex:i];
    [markerJsonList addObject:[[MarkerJsonData alloc] initWithOptions:markerOption]];
  }
  
  [markerCluster setAllMarkerOptions:markerJsonList];
/*
  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
  
  });
/*
  String clusterId = args.getString(1);
  final MarkerCluster markerCluster = (MarkerCluster) this.objects.get(clusterId);
  JSONArray markerOptions = args.getJSONArray(2);
  List<MarkerJsonData> markerJsonList = new ArrayList<MarkerJsonData>();
  for (int i = 0; i < markerOptions.length(); i++) {
    markerJsonList.add(new MarkerJsonData(markerOptions.getJSONObject(i)));
  }
  
  markerCluster.setAllMarkerOptions(markerJsonList);
  
  if (currentTask != null) {
    currentTask.cancel(true);
  }
  AsyncCluster asyncTask = new AsyncCluster(markerCluster, callbackContext);
  asyncTask.execute(markerJsonList.toArray(new MarkerJsonData[]{}));
  currentTask = asyncTask;
  */
}

@end
