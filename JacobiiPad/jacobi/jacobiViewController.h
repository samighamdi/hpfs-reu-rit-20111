//
//  jacobiViewController.h
//  jacobi
//
//  Created by Daniel Bokser on 6/12/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface jacobiViewController : UIViewController {
   
    
    IBOutlet UIImageView *imgV;
    BOOL test;
}


@property (retain, nonatomic) IBOutlet UIImageView *imgV;

-(void) tapGesture: (UITapGestureRecognizer*) tgr;
-(void) sendURLRequest: (CGPoint) point;
-(void) drawImage;



@end
