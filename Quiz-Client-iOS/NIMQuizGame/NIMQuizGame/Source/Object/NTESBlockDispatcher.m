//
//  NTESBlockDispatcher.m
//  NIMQuizGame
//
//  Created by chrisRay on 2018/1/11.
//  Copyright © 2018年 chris. All rights reserved.
//

#import "NTESBlockDispatcher.h"
#import "NSDictionary+NTESJson.h"

@interface NTESBlockDispatchObject : NSObject

@property (nonatomic, copy) NTESDispatchBlock block;

@property (nonatomic, assign) NSTimeInterval timestamp;

@end

@interface NTESBlockDispatcher()
{
    NSTimeInterval _lastFireTimestamp;
}

@property (nonatomic, strong) NSMutableArray<NTESBlockDispatchObject *> *dispatchObjects;

@end

@implementation NTESBlockDispatcher

- (instancetype)init
{
    self = [super init];
    if (self)
    {        
        _dispatchObjects = [[NSMutableArray alloc] init];
        _lastFireTimestamp = 0;
        [self addListener];
    }
    return self;
}

- (void)dealloc
{
    [self removeListener];
}

- (void)addListener
{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onFire:) name:NTES_CURRENT_TIMETAG_NOTIFICATION object:nil];
}

- (void)removeListener
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}


- (void)addDispatchBlock:(NTESDispatchBlock)block
                    time:(NSTimeInterval)timestamp;
{
    if (timestamp <= _lastFireTimestamp)
    {
        DDLogInfo(@"ignore dispatch block because of timeout! block timestamp is %.2f, last fire timestamp is %.2f", timestamp, _lastFireTimestamp);
        return;
    }
    NTESBlockDispatchObject *object = [[NTESBlockDispatchObject alloc] init];
    object.block = block;
    object.timestamp = timestamp;

    NSInteger index = [self.dispatchObjects indexOfObject:object inSortedRange:NSMakeRange(0, self.dispatchObjects.count) options:NSBinarySearchingInsertionIndex | NSBinarySearchingLastEqual usingComparator:^NSComparisonResult(id  _Nonnull obj1, id  _Nonnull obj2) {
        NTESBlockDispatchObject *object1 = obj1;
        NTESBlockDispatchObject *object2 = obj2;
        if (object1.timestamp < object2.timestamp) return NSOrderedAscending;
        else if (object1.timestamp == object2.timestamp) return NSOrderedSame;
        else return NSOrderedDescending;
    }];

    [self.dispatchObjects insertObject:object atIndex:index];
    DDLogInfo(@"add dispatch block, last stream timestamp is %.2f",_lastFireTimestamp);
}


- (void)onFire:(NSNotification *)notification
{
    NSDictionary *info = notification.userInfo;
    NSTimeInterval timestamp = [info jsonDouble:@"realTime"];
    [self dispatch:timestamp];
}

- (void)dispatch:(NSTimeInterval)timestamp
{
    NSArray *objects = [NSArray arrayWithArray:self.dispatchObjects];
    NSMutableArray *dispatchObjects = [[NSMutableArray alloc] init];
    for (NTESBlockDispatchObject *object in objects)
    {
        if (object.timestamp < timestamp && object.block)
        {
            [dispatchObjects addObject:object];
            [self.dispatchObjects removeObject:object];
        }
        else
            break;
    }
    if (dispatchObjects.count)
    {
        DDLogInfo(@"will dispatch objects: %@, current stream timestamp %.2f",dispatchObjects,timestamp);
        _lastFireTimestamp = timestamp;
        NTESBlockDispatchObject *object = dispatchObjects.lastObject;
        object.block();
    }    
}

@end


@implementation NTESBlockDispatchObject
@end
