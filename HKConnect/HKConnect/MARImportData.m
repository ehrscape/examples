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

#import "MARImportData.h"

@interface MARImportData()

@property (nonatomic, strong) MARHealthStore *healthStore;

@end

@implementation MARImportData

#pragma mark - Lifecycle

- (instancetype)initWithURL:(NSURL *)URL
{
	self = [super init];
	if (self) {
		self.healthStore = [MARHealthStore sharedInstance];
		_URL = URL;
	}
	return self;
}

#pragma mark - Data manipulation

- (BOOL)loadData
{
	NSError *error = nil;
	if (_URL && [self.URL checkResourceIsReachableAndReturnError:&error]) {
		NSData *data = [NSData dataWithContentsOfURL:self.URL
											 options:0
											   error:&error];
		if (!data || error) {
			DDLogError(@"Could not load imported data! Error: %@", error.localizedDescription);
			return NO;
		}

		NSDictionary *unpackedData = [NSJSONSerialization JSONObjectWithData:data
																	 options:NSJSONReadingAllowFragments
																	   error:&error];

		if (unpackedData.count && !error) {
			[self importIntoHealthKit:unpackedData];
			return YES;
		}
	}

	return NO;
}

- (void)importIntoHealthKit:(NSDictionary *)dictionary
{
	NSMutableArray *importedSamples = @[].mutableCopy;

	for (NSString *identifier in dictionary) {
		NSDictionary *typeData = dictionary[identifier];
		HKQuantityType *type = [HKQuantityType quantityTypeForIdentifier:identifier];
		HKUnit *unit = [HKUnit unitFromString:typeData[MARExportResultUnitKey]];

		if (!type || !unit) {
			continue;
		}

		NSArray *samples = typeData[MARExportResultSamplesKey];
		for (NSDictionary *rawSample in samples) {

			NSDate *startDate = [NSDate bd_dateFromISO8601String:rawSample[MARExportResultStartDateKey]];
			NSDate *endDate = [NSDate bd_dateFromISO8601String:rawSample[MARExportResultEndDateKey]];

			double value = [rawSample[MARExportResultQuantityKey] doubleValue];
			HKQuantity *quantity = [HKQuantity quantityWithUnit:unit
													doubleValue:value];

			HKQuantitySample *sample = [HKQuantitySample quantitySampleWithType:type
																	   quantity:quantity
																	  startDate:startDate
																		endDate:endDate
																	   metadata:rawSample[MARExportResultMetadataKey]];

			[importedSamples addObject:sample];
		}
	}

	if (importedSamples.count) {
		[self.healthStore saveObjects:importedSamples
					   withCompletion:^(BOOL success, NSError *error) {
						   if (!success || error) {
							   DDLogError(@"Could not store the samples into HealthKit! Error: %@", error.localizedDescription);
							   return;
						   }

						   DDLogInfo(@"Successfully imported the samples into HealthKit!");
					   }];
	}
}


@end
