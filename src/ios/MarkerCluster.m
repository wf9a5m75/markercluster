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

  [self _asyncCluster:markerCluster markerJsons:markerJsonList command:command];
}

- (void)_asyncCluster:(MarkerClusterManager *)markerCluster markerJsons:(NSArray *)markerJsons command:(CDVInvokedUrlCommand*)command{
  __block int clusterDistance = 40;
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

      
      MarkerJsonData *markerOptions;
      NSString *geocell;
      CLLocationCoordinate2D latLng;
      NSMutableArray *stacks;
      CellLocation *cellLoc;
      
      for (int j = 0; j < [markerJsons count]; j++) {
        markerOptions = [markerJsons objectAtIndex:j];
        geocell = [markerOptions.geocells objectAtIndex:j];
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
      
      NSLog(@"%@", geocellHash);
    });
  });
  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    NSEnumerator *locationsEnumerator = [geocellLocations keyEnumerator];
    NSEnumerator *locationsEnumerator2;
    
    NSMapTable *unionCells;
    GMSMutablePath *unionCellBounds;
    Boolean needRetry = YES;
    NSString *geocell, *clusterCell;
    CellLocation *cellLoc, *clusterLoc;
    CGPoint cellPoint, clusterPoint;
    CLLocationCoordinate2D *latLng;
    
    while (needRetry) {
      needRetry = NO;
      while ((geocell = [locationsEnumerator nextObject])) {
        if ([geocellLocations objectForKey:geocell] == nil) {
          continue;
        }
        cellLoc = [geocellLocations objectForKey:geocell];
        cellPoint = cellLoc.point;
        
        unionCellBounds = nil;
        unionCells = nil;
        locationsEnumerator2 = [geocellLocations keyEnumerator];
        while ((clusterCell = [locationsEnumerator2 nextObject])) {
          if ([clusterCell isEqualToString:geocell]) {
            continue;
          }
          clusterLoc = [geocellLocations objectForKey:geocell];
          clusterPoint = clusterLoc.point;
          
          if (cellPoint.x >= clusterPoint.x - clusterDistance &&
              cellPoint.x <= clusterPoint.x + clusterDistance &&
              cellPoint.y >= clusterPoint.y - clusterDistance &&
              cellPoint.y <= clusterPoint.y + clusterDistance) {
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
          latLng = unionCellBounds
        }
      }
    }
  });
  
  /*
  
    Iterator<String> iterator;
    boolean needRetry = true;
    while (needRetry) {
      needRetry = false;
      for (i = 0; i < geocells.length; i++) {
        geocell = geocells[i];
        if (geocellLocations.containsKey(geocell) == false) {
          continue;
        }
        cellLoc = geocellLocations.get(geocell);
        cellPoint = cellLoc.point;
        
        unionCellBounds = null;
        unionCells = null;
        iterator = geocellLocations.keySet().iterator();
        while (iterator.hasNext()) {
          clusterCell = iterator.next();
          if (clusterCell.equals(geocell)) {
            continue;
          }
          clusterLoc = geocellLocations.get(clusterCell);
          clusterPoint = clusterLoc.point;
          if (cellPoint.x >= clusterPoint.x - clusterDistance &&
              cellPoint.x <= clusterPoint.x + clusterDistance &&
              cellPoint.y >= clusterPoint.y - clusterDistance &&
              cellPoint.y <= clusterPoint.y + clusterDistance) {
            
            if (unionCellBounds == null) {
              LatLngBounds.Builder builder = new LatLngBounds.Builder();
              builder.include(clusterLoc.latLng);
              builder.include(cellLoc.latLng);
              unionCellBounds = builder.build();
              
              unionCells = new HashMap<String, List<MarkerJsonData>>();
              unionCells.put(geocell, geocellHash.get(geocell));
            }
            unionCells.put(clusterCell, geocellHash.get(clusterCell));
            unionCellBounds.including(clusterLoc.latLng);
          }
        }
        
        if (unionCells != null) {
          latLng = unionCellBounds.getCenter();
          clusterCell = MarkerClusterUtil.getGeocell(latLng.latitude, latLng.longitude, resolution);
  
          List<MarkerJsonData> buffer = new ArrayList<MarkerJsonData>();
          iterator = unionCells.keySet().iterator();
          while(iterator.hasNext()) {
            geocell = iterator.next();
            geocellLocations.remove(geocell);
            geocellHash.remove(geocell);
            buffer.addAll(unionCells.get(geocell));
          }
  
          needRetry = true;
          geocellHash.put(clusterCell, buffer);
        }
      }
      geocells = geocellHash.keySet().toArray(new String[]{});
    }
  */
}
@end
