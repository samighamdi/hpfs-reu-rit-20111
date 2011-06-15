//
//  jacobiAppDelegate.h
//  jacobi
//
//  Created by Daniel Bokser on 6/14/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

@class jacobiViewController;

@interface jacobiAppDelegate : NSObject <UIApplicationDelegate> {

}

@property (nonatomic, retain) IBOutlet UIWindow *window;

@property (nonatomic, retain) IBOutlet jacobiViewController *viewController;

@end
