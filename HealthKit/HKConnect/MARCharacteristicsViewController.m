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

#import "MARCharacteristicsViewController.h"

@interface MARCharacteristicsViewController()

@property (nonatomic, strong) MARHealthStore *healthStore;

@end

@implementation MARCharacteristicsViewController

#pragma mark - Lifecycle

- (instancetype)initWithCoder:(NSCoder *)coder
{
	self = [super initWithCoder:coder];
	if (self) {
		self.healthStore = [MARHealthStore sharedInstance];
		if ([MARHealthStore isHealthDataAvailable]) {
			[MARHealthStore requestAuthorizationForCharacteristicsWithCompletion:^(BOOL success, NSError *error) {
				if (error) {
					DDLogError(@"Could not authorize! %@", error.localizedDescription);
					return;
				}

				DDLogInfo(@"Successfully authorized for reading characteristics!");
			}];
		}
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
	return 3;
}

- (UITableViewCell *)tableView:(UITableView *)tableView
		 cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	UITableViewCell *cellView = [tableView dequeueReusableCellWithIdentifier:@"CharacteristicCell"
																forIndexPath:indexPath];
	if (cellView) {
		switch (indexPath.row) {
			default:
			case 0: {
				NSString *sexString = [MARHealthStore biologicalSexString:[self.healthStore biologicalSexWithError:nil]];
				cellView.textLabel.text = NSLocalizedString(@"Sex", @"");
				cellView.detailTextLabel.text = sexString;
				break;
			}
			case 1: {
				NSString *bloodTypeString = [MARHealthStore bloodTypeString:[self.healthStore bloodTypeWithError:nil]];
				cellView.textLabel.text = NSLocalizedString(@"Blood type", @"");
				cellView.detailTextLabel.text = bloodTypeString;
				break;
			}
			case 2: {
				NSString *birthDateString = [self birthDateString:[self.healthStore dateOfBirthWithError:nil]];
				cellView.textLabel.text = NSLocalizedString(@"Date of birth", @"");
				cellView.detailTextLabel.text = birthDateString;
				break;
			}

		}
	}
	return cellView;
}

#pragma mark - Resolve values

- (NSString *)birthDateString:(NSDate *)date
{
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
	dateFormatter.dateStyle = NSDateFormatterShortStyle;

	return [dateFormatter stringFromDate:date];
}


@end
