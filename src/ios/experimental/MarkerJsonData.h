//
//  MarkerJsonData.h
//  DevApp
//
//  Created by Katsumata Masashi on 11/9/14.
//
//

#import <Foundation/Foundation.h>
#import "MarkerClusterUtil.h"
#define MAX_GEOCELL_RESOLUTION (13)

#ifndef MIN
#import <NSObjCRuntime.h>
#endif

@interface MarkerJsonData : NSObject
@property (nonatomic, strong) NSObject *options;
@property (nonatomic, strong) NSMutableArray *geocells;
@property (nonatomic, strong) NSNumber *latitude;
@property (nonatomic, strong) NSNumber *longitude;

- (id)initWithOptions:(NSObject *) markerOptions;
- (NSString *)getGeocell:(int) zoom;
- (NSString *)getMarkerId;

@end
