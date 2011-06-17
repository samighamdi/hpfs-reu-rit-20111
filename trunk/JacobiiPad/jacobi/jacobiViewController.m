//
//  jacobiViewController.m
//  jacobi
//
//  Created by Daniel Bokser on 6/12/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import "jacobiViewController.h"



@implementation jacobiViewController

@synthesize imgV;

- (void)dealloc
{
    [imgV release];
    [super dealloc];
}

- (void) tapGesture:(UITapGestureRecognizer *)tgr
{
    CGPoint p = [tgr locationInView:self.view];
    
    
    [self sendURLRequest:p];
    
}
 

- (void) sendURLRequest:(CGPoint)point
{
    
    int trueX = (point.x * (imgV.image.size.height/1004.0));
    int trueY = (point.y * (imgV.image.size.width/768.0));
    
    NSString *urlStr = [NSString stringWithFormat: @"http://kfd", trueX, trueY];
    
    
    NSURLRequest *urlReq = [NSURLRequest requestWithURL: [NSURL URLWithString:urlStr]];
    
    NSURLConnection *connect = [[NSURLConnection alloc] initWithRequest: urlReq delegate:self];
    
    NSLog(@"%d, %d, %@", trueX, trueY, NSStringFromCGSize(imgV.image.size));

    
    
    
    [connect release];
}


- (void) drawImage
{
    NSString *str; 
    str = @"http://server.daquanne.com/images.php";
    /*
    if (test) {
        str = @"http://www.sprintwallpaper.com/images/wallpapers/70136098/Nature/Landscape%201/Landscape%2050.jpg";
        test = NO;
    }
    else
    {
        str = @"http://www.webdesign.org/img_articles/7072/BW-kitten.jpg";
        test = YES;
    }
     */
    
    UIImage *img = [[UIImage alloc] initWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:str]]];
    //UIImage *img = [self getImageWithUrl:str];
    //UIScrollView *scrollView;
    //create an image view
    
    
    
    if (img) {
        [imgV setImage:img];
    }
    
    
    [img release];
    
    
    /*
    scrollView = (UIScrollView *)[self.view viewWithTag:100];
    scrollView.contentSize = imgV.image.size;
    [scrollView addSubview:imgV];
     */
   
    
   
            
        
    
}




- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle


// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad
{
    [super viewDidLoad];
    
   
    
    UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapGesture:)];
    
    [tapGesture setNumberOfTapsRequired:1];
    [tapGesture setNumberOfTouchesRequired:1];
    
    tapGesture.cancelsTouchesInView = NO;
    [self.view addGestureRecognizer:tapGesture];
    [tapGesture release];
    
    
    /*
    UIScrollView *imageScrollView = [[UIScrollView alloc] initWithFrame:[[self view] bounds]];
    imageScrollView.tag = 100;
    [imageScrollView setBackgroundColor:[UIColor blackColor]];
    [imageScrollView setDelegate:self];
    [imageScrollView setBouncesZoom:YES];
    [[self view] addSubview:imageScrollView];
     
    
    //add image view to view
    
    
    [imageScrollView release];
     */
    
    test = YES;
    
    [NSTimer scheduledTimerWithTimeInterval:0.1 target:self selector:@selector(drawImage) userInfo:nil repeats:YES];
    

}


- (void)viewDidUnload
{
    
    [imgV release];
    imgV = nil;
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return YES;
}

@end
