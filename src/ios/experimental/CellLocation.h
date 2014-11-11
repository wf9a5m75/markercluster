//
//  CellLocation.h
//  DevApp
//
//  Created by Katsumata Masashi on 11/11/14.
//
//

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>

@interface CellLocation : NSObject
@property (nonatomic, nonatomic) CGPoint point;
@property (nonatomic, nonatomic) CLLocationCoordinate2D latLng;
@end
