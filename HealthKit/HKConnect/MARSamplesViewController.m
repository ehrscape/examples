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

#import "MARSamplesViewController.h"
#import "MARSampleViewCell.h"
#import "MARExportViewController.h"

@interface MARSamplesViewController()

@property (nonatomic, strong) MARHealthStore *healthStore;
@property (nonatomic, strong) NSSet *exportableTypes;

@end

@implementation MARSamplesViewController
#pragma mark - Lifecycle

- (instancetype)initWithCoder:(NSCoder *)coder
{
	self = [super initWithCoder:coder];
	if (self) {
		self.healthStore = [MARHealthStore sharedInstance];
		self.exportableTypes = self.healthStore.quantityTypes;
	}
	return self;
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return 1;
}

- (NSInteger)tableView:(UITableView *)tableView
 numberOfRowsInSection:(NSInteger)section
{
	return self.exportableTypes.count;
}

- (NSString *)tableView:(UITableView *)tableView
titleForHeaderInSection:(NSInteger)section
{
	return MARHKTypeQuantityKey.capitalizedString;
}

- (UITableViewCell *)tableView:(UITableView *)tableView
		 cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	MARSampleViewCell *cellView = [tableView dequeueReusableCellWithIdentifier:MARCellIdentifierSample
																 forIndexPath:indexPath];

	if (cellView) {
		HKQuantityType *type = self.exportableTypes.allObjects[indexPath.row];
		NSString *identifier = type.identifier;
		NSDictionary *typeData = self.healthStore.types[MARHKTypeQuantityKey][identifier];

		NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
		NSData *storedData = [defaults objectForKey:MARDefaultsTypeKey];
		if (storedData) {
			NSDictionary *types = [NSKeyedUnarchiver unarchiveObjectWithData:storedData];
			cellView.accessSwitch.on = [types[identifier] boolValue];
		}

		cellView.nameLabel.text = typeData[MARHKTypeNameKey];
		cellView.name = identifier;
		cellView.accessSwitch.tag = indexPath.row;
	}

	return cellView;
}

#pragma mark - Segue

- (void)prepareForSegue:(UIStoryboardSegue *)segue
				 sender:(id)sender
{
	if ([segue.identifier isEqualToString:MARSegueExport]) {
		NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
		NSData *typeData = [defaults objectForKey:MARDefaultsTypeKey];
		if (typeData) {
			NSDictionary *types = [NSKeyedUnarchiver unarchiveObjectWithData:typeData];

			NSArray *requestedKeys = [types allKeysForObject:@(YES)];

			NSArray *shareTypes = [MARHealthStore sharedInstance].shareTypes.allObjects;
			NSArray *readTypes = [MARHealthStore sharedInstance].readTypes.allObjects;

			NSMutableArray *requestedShareTypes = @[].mutableCopy;
			[shareTypes enumerateObjectsUsingBlock:^(HKObjectType *type, NSUInteger idx, BOOL *stop) {
				if ([requestedKeys containsObject:type.identifier]) {
					[requestedShareTypes addObject:type];
				}
			}];

			NSMutableArray *requestedReadTypes = @[].mutableCopy;
			[readTypes enumerateObjectsUsingBlock:^(HKObjectType *type, NSUInteger idx, BOOL *stop) {
				if ([requestedKeys containsObject:type.identifier]) {
					[requestedReadTypes addObject:type];
				}
			}];

			MARExportViewController *exportVC = (MARExportViewController *)segue.destinationViewController;
			exportVC.readTypes = [NSSet setWithArray:requestedReadTypes];
			exportVC.shareTypes = [NSSet setWithArray:requestedShareTypes];
		}
	}
}

@end
