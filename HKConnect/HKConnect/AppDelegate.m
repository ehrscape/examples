/*
 * HKConnect
 * Copyright (c) 2014 Marand d.o.o. (www.marand.com)
 *
 * This file is part of HKConnect.
 *
 * HKConnect is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

#import "AppDelegate.h"
#import "MARImportData.h"
#import <Fabric/Fabric.h>
#import <Crashlytics/Crashlytics.h>

@interface AppDelegate ()

@property (nonatomic, strong) MARHealthStore *healthStore;

@end

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
	[Fabric with:@[CrashlyticsKit]];
	DDTTYLogger *logger = [DDTTYLogger sharedInstance];
	logger.colorsEnabled = YES;
	logger.logFormatter = [[MARLogFormatter alloc] init];
	[DDLog addLogger:logger];

	self.healthStore = [MARHealthStore sharedInstance];

	return YES;
}

- (BOOL)application:(UIApplication *)application
			openURL:(NSURL *)url
  sourceApplication:(NSString *)sourceApplication
		 annotation:(id)annotation
{
	if (url) {
		MARImportData *dataImporter = [[MARImportData alloc] initWithURL:url];
		return [dataImporter loadData];
	}

	return NO;
}

@end
