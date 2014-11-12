//
//  MarkerJsonData.h
//  DevApp
//
//  Created by Katsumata Masashi on 11/9/14.
//
//

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>
#import "MarkerClusterUtil.h"
#define MAX_GEOCELL_RESOLUTION (13)

#ifndef MIN
#import <NSObjCRuntime.h>
#endif

@interface MarkerJsonData : NSObject
@property (nonatomic, strong) NSDictionary *options;
@property (nonatomic, strong) NSMutableArray *geocells;
@property (nonatomic, strong) NSString *markerId;
@property (nonatomic, nonatomic) CLLocationCoordinate2D position;


- (id)initWithOptions:(NSObject *) markerOptions;
- (NSString *)getGeocell:(int) zoom;
- (NSString *)getMarkerId;

@end
