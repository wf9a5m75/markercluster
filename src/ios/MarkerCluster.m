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
  self.clusters = [[NSMapTable alloc] init];
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

- (void)_onClusterEventForIOS:(CDVInvokedUrlCommand*)command {
  
  NSString *clusterObjId = [command.arguments objectAtIndex:1];
  Cluster *cluster = (Cluster *)[self.mapCtrl.overlayManager objectForKey:clusterObjId];
  [cluster _onClusterEventForIOS:command];
}

- (void)addMarkerJson:(CDVInvokedUrlCommand*)command {
  NSString *clusterId = [command.arguments objectAtIndex:1];
  MarkerClusterManager *markerCluster = (MarkerClusterManager *)[self.mapCtrl.overlayManager objectForKey:clusterId];

  NSArray *markerOptions = [command.arguments objectAtIndex:2];
  NSMutableArray *markerJsonList = [NSMutableArray array];
  
  for (int i = 0; i < markerOptions.count; i++) {
    NSObject *markerOption = [markerOptions objectAtIndex:i];
    MarkerJsonData *jsonData = [[MarkerJsonData alloc] initWithOptions:markerOption];
    [markerJsonList addObject: jsonData];
  }
  
  [markerCluster setAllMarkerOptions:markerJsonList];

  [self _asyncCluster:markerCluster markerJsons:markerJsonList command:command];
}

- (void)_asyncCluster:(MarkerClusterManager *)markerCluster markerJsons:(NSArray *)markerJsons command:(CDVInvokedUrlCommand*)command{
  UIScreen *mainScreen = [UIScreen mainScreen];
  __block float screenScale = mainScreen.scale;
  __block int clusterDistance = (int)(60.0f * screenScale);
  int zoom = self.mapCtrl.map.camera.zoom;
  zoom = zoom - 1;
  __block int resolution = zoom < 1 ? 1 : zoom;
  resolution = resolution > MAX_GEOCELL_RESOLUTION ? MAX_GEOCELL_RESOLUTION : resolution;
  __block NSMapTable *geocellHash = [[NSMapTable alloc] init];
  __block NSMapTable *geocellLocations = [[NSMapTable alloc] init];
  
  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    dispatch_sync(dispatch_get_main_queue(), ^{
      GMSProjection *projection = self.mapCtrl.map.projection;
      GMSVisibleRegion visibleRegion = projection.visibleRegion;
      GMSCoordinateBounds *visibleBounds = [[GMSCoordinateBounds alloc] initWithRegion: visibleRegion];

      
      NSString *geocell;
      CLLocationCoordinate2D latLng;
      NSMutableArray *stacks;
      CellLocation *cellLoc;
      
      for (int j = 0; j < markerJsons.count; j++) {
        MarkerJsonData *markerOptions = [markerJsons objectAtIndex:j];
        geocell = [markerOptions.geocells objectAtIndex:resolution];
        latLng = markerOptions.position;
        if ([visibleBounds containsCoordinate:latLng] == NO) {
          continue;
        }
        
        if ([geocellHash objectForKey:geocell] == NO) {
          stacks = [NSMutableArray array];
          cellLoc = [[CellLocation alloc] init];
          cellLoc.point = [projection pointForCoordinate:latLng];
          cellLoc.latLng = latLng;
          [geocellLocations setObject:cellLoc forKey:geocell];
        } else {
          stacks = [geocellHash objectForKey:geocell];
        }
        
        [stacks addObject:markerOptions];
        [geocellHash setObject:stacks forKey:geocell];
      }
      
    });
  });
  
  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    NSArray *geocells = [MarkerClusterUtil getKeys:geocellLocations];
    
    NSMapTable *unionCells;
    GMSMutablePath *unionCellBounds;
    Boolean needRetry = YES;
    NSString *geocell, *clusterCell;
    CellLocation *cellLoc, *clusterLoc;
    CGPoint cellPoint, clusterPoint;
    CLLocationCoordinate2D southWest, northEast;
    GMSCoordinateBounds *bounds;
    NSArray *locations;
    
    while (needRetry) {
      needRetry = NO;
      for (int k1 = 0; k1 < geocells.count; k1++) {
        geocell = [geocells objectAtIndex:k1];
        if ([geocellLocations objectForKey:geocell] == nil) {
          continue;
        }
        cellLoc = [geocellLocations objectForKey:geocell];
        cellPoint = cellLoc.point;
        
        unionCellBounds = nil;
        unionCells = nil;
        locations = [MarkerClusterUtil getKeys:geocellLocations];
        
        for (int k2 = 0; k2 < locations.count; k2++) {
          clusterCell = [locations objectAtIndex:k2];
          if ([clusterCell isEqualToString:geocell]) {
            continue;
          }
          clusterLoc = [geocellLocations objectForKey:geocell];
          clusterPoint = clusterLoc.point;
          
          if (cellPoint.x >= (float)(clusterPoint.x - clusterDistance) &&
              cellPoint.x <= (float)(clusterPoint.x + clusterDistance) &&
              cellPoint.y >= (float)(clusterPoint.y - clusterDistance) &&
              cellPoint.y <= (float)(clusterPoint.y + clusterDistance)) {
            if (unionCellBounds == nil) {
              unionCellBounds = [[GMSMutablePath alloc] init];
              [unionCellBounds addCoordinate:cellLoc.latLng];
              [unionCellBounds addCoordinate:clusterLoc.latLng];
              
              unionCells = [[NSMapTable alloc] init];
              [unionCells setObject:[geocellHash objectForKey:clusterCell] forKey:geocell];
            }
            [unionCells setObject:[geocellHash objectForKey:clusterCell] forKey:clusterCell];
            [unionCellBounds addCoordinate:clusterLoc.latLng];
          }
        }
        
        if (unionCells != nil) {
          bounds = [[GMSCoordinateBounds alloc] initWithPath:unionCellBounds];
          southWest = bounds.southWest;
          northEast = bounds.northEast;
          
          double centerLat = (southWest.latitude + northEast.latitude) / 2;
          double swLng = southWest.longitude;
          double neLng = northEast.longitude;
          double sumLng = swLng + neLng;
          double centerLng = sumLng / 2;
          
          if ((swLng > 0 && neLng < 0 && swLng < 180)) {
            centerLat += sumLng > 0 ? -180 : 180;
          }
          clusterCell = [MarkerClusterUtil getGeocell:centerLat lng:centerLng resolution:resolution];
          NSMutableArray *buffer = [NSMutableArray array];
          NSEnumerator *keys3 = [unionCells keyEnumerator];
          while((geocell = [keys3 nextObject])) {
            [geocellLocations removeObjectForKey:geocell];
            [geocellHash removeObjectForKey:geocell];
            [buffer addObjectsFromArray:[unionCells objectForKey:geocell]];
          }
          needRetry = YES;
          [geocellHash setObject:buffer forKey:clusterCell];
        }
      }
      geocells = [MarkerClusterUtil getKeys:geocellHash];
    }
    
  });
  
  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    dispatch_sync(dispatch_get_main_queue(), ^{
      NSString *geocell;
      
      Cluster *cluster;
      NSEnumerator *keys = [self.clusters keyEnumerator];
      while((geocell = [keys nextObject])) {
        if ([geocellHash objectForKey:geocell] == nil) {
          cluster = [self.clusters objectForKey:geocell];
          [cluster remove];
          [self.clusters removeObjectForKey:geocell];
          cluster = nil;
        }
      }
      {
        NSLog(@"geocellHash=%@", geocellHash);
      keys = [geocellHash keyEnumerator];
      while ((geocell = [keys nextObject]))
        if ([self.clusters objectForKey:geocell]) {
          cluster = [self.clusters objectForKey:geocell];
          [cluster addMarkerJsonList:[geocellHash objectForKey:geocell]];
        } else {
          cluster = [[Cluster alloc] initWithOptions:self.mapCtrl commandDelegate:self.commandDelegate command:command];
          [cluster addMarkerJsonList:[geocellHash objectForKey:geocell]];
          [self.clusters setObject:cluster forKey:geocell];
        }
      }
    });
  });
}
@end
