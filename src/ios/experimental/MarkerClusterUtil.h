//
//  MarkerClusterUtil.h
//  DevApp
//
//  Created by Katsumata Masashi on 11/9/14.
//
//

#import <Foundation/Foundation.h>

#ifndef MIN
#import <NSObjCRuntime.h>
#endif

@interface MarkerClusterUtil : NSObject

+ (NSObject *)getLatLngBoundsFromGeocell:(NSString *) geocell;
+ (NSString *)getGeocell:(double) lat lng:(double)lng resolution:(int)resolution;
+ (NSString *)_subdiv_char:(int) posX posY:(int)posY;
+ (NSArray *)getKeys:(NSMapTable *) hashMap;
@end
