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

#import "MARExportViewController.h"
#import "MARExportSettings.h"
#import "AppDelegate.h"
#import <M13ProgressSuite/M13ProgressHUD.h>
#import <M13ProgressSuite/M13ProgressViewRing.h>

@interface MARExportViewController()

@property (nonatomic, strong) MARHealthStore *healthStore;
@property (nonatomic, strong) UIBarButtonItem *exportBarButton;
@property (nonatomic, strong) M13ProgressHUD *progressHUD;

@end

@implementation MARExportViewController

- (instancetype)initWithCoder:(NSCoder *)coder
{
	self = [super initWithCoder:coder];
	if (self) {
		self.healthStore = [MARHealthStore sharedInstance];
		_progressHUD = [[M13ProgressHUD alloc] initWithProgressView:[[M13ProgressViewRing alloc] init]];
		self.progressHUD.indeterminate = YES;
		UIWindow *window = ((AppDelegate *)[UIApplication sharedApplication].delegate).window;
		[window addSubview:self.progressHUD];
	}
	return self;
}

#pragma mark - View management

- (void)awakeFromNib
{
	self.formController.form = [[MARExportSettings alloc] init];
	self.progressHUD.progressViewSize = CGSizeMake(60.0, 60.0);
	self.progressHUD.animationPoint = CGPointMake(CGRectGetMidX([UIScreen mainScreen].bounds), CGRectGetMidY([UIScreen mainScreen].bounds));

}

#pragma mark - Export settings

- (IBAction)exportForm:(id<FXFormFieldCell>)sender
{
	MARExportSettings *form = self.formController.form;

	if ([MARHealthStore isHealthDataAvailable]) {
		[self.healthStore requestAuthorizationToShareTypes:self.shareTypes
												 readTypes:self.readTypes
												completion:^(BOOL success, NSError *error) {
													if (error) {
														DDLogError(@"Could not authorize for export! %@", error.localizedDescription);
														return;
													}

													[self exportDataForRange:form.timeRange
																	inFormat:form.format];
												}];
	}
}

- (void)exportDataForRange:(MARExportTimeRange)range
				  inFormat:(MARExportFormat)format
{
	NSCalendar *calendar = [NSCalendar currentCalendar];
	NSDate *now = [NSDate date];
	NSDateComponents *components = [calendar components:NSCalendarUnitYear|NSCalendarUnitMonth|NSCalendarUnitDay
											   fromDate:now];

	NSDate *startDate = [calendar dateFromComponents:components];
	NSDate *endDate = [calendar dateByAddingUnit:NSCalendarUnitDay
										   value:1
										  toDate:startDate
										 options:0];
	switch (range) {
		default:
		case MARExportTimeRangeAllTime:
			startDate = [NSDate dateWithTimeIntervalSince1970:0];
			break;
		case MARExportTimeRangeDay:
			startDate = [calendar dateFromComponents:components];
			break;
		case MARExportTimeRangeWeek:
			startDate = [calendar dateByAddingUnit:NSCalendarUnitWeekOfYear
											 value:-1
											toDate:endDate
										   options:0];
			break;
		case MARExportTimeRangeMonth:
			startDate = [calendar dateByAddingUnit:NSCalendarUnitMonth
											 value:-1
											toDate:endDate
										   options:0];
			break;
	}

	__block NSMutableDictionary *exportedResults = @{}.mutableCopy;
	NSMutableDictionary *columns = @{}.mutableCopy;

	void(^noResultsBlock)() = ^ {
		[self.progressHUD hide:YES];
		UIAlertView *alert =[[UIAlertView alloc ] initWithTitle:@"Export"
														message:@"No results were found for export!"
													   delegate:self
											  cancelButtonTitle:@"OK"
											  otherButtonTitles:nil];
		[alert show];
	};

	void(^JSONCompletionBlock)(BOOL) = ^(BOOL success) {
		self.progressHUD.status = NSLocalizedString(@"Finishing...", @"");
		if (exportedResults.count) {
			[self.progressHUD performAction:M13ProgressViewActionSuccess
								   animated:YES];
			[self.progressHUD hide:YES];
			NSError *error = nil;
			NSData *jsonData = [NSJSONSerialization dataWithJSONObject:exportedResults
															   options:NSJSONWritingPrettyPrinted
																 error:&error];
			if (jsonData) {
				NSString *jsonString = [[NSString alloc] initWithData:jsonData
															 encoding:NSUTF8StringEncoding];
				NSString *tempFilename = [NSTemporaryDirectory() stringByAppendingPathComponent:@"HealthKitExport.json"];

				[jsonString writeToFile:tempFilename
							 atomically:NO
							   encoding:NSUTF8StringEncoding
								  error:&error];

				if (error) {
					DDLogError(@"Could not save temporary file! Error: %@", error.localizedDescription);
					return;
				}
				DDLogVerbose(@"JSON:\n\n%@", jsonString);
				NSURL *tempURL = [NSURL fileURLWithPath:tempFilename];

				UIDocumentInteractionController *documentIC = [UIDocumentInteractionController interactionControllerWithURL:tempURL];
				documentIC.delegate = self;
				[documentIC presentPreviewAnimated:YES];
			} else {
				DDLogError(@"Error: %@", error.localizedDescription);
			}
		}
		else {
			[self.progressHUD performAction:M13ProgressViewActionFailure
								   animated:YES];
			noResultsBlock();
		}
	};

	void(^CSVCompletionBlock)(BOOL) = ^(BOOL success) {
		self.progressHUD.status = NSLocalizedString(@"Finishing...", @"");
		if (exportedResults.count) {
			[self.progressHUD performAction:M13ProgressViewActionSuccess
								   animated:YES];
			[self.progressHUD hide:YES];
			NSMutableString *csvString = @"".mutableCopy;

			NSMutableArray *line = @[].mutableCopy;
			[line addObject:@"Date"];
			for (NSString *columnKey in columns) {
				[line addObject:columns[columnKey]];
			}

			[csvString appendFormat:@"%@\n", [line componentsJoinedByString:@","]];

			for (NSString *dateKey in exportedResults) {
				NSDictionary *dateResults = exportedResults[dateKey];
				line = @[].mutableCopy;

				[line addObject:dateKey];
				for (NSString *columnKey in columns) {
					if (dateResults[columnKey]) {
						[line addObject:dateResults[columnKey]];
					} else {
						[line addObject:@(0)];
					}
				}
				[csvString appendFormat:@"%@\n", [line componentsJoinedByString:@","]];
			}

			NSError *error = nil;
			NSString *tempFilename = [NSTemporaryDirectory() stringByAppendingPathComponent:@"HealthKitExport.csv"];

			[csvString writeToFile:tempFilename
						atomically:NO
						  encoding:NSUTF8StringEncoding
							 error:&error];

			if (error) {
				DDLogError(@"Could not save temporary file! Error: %@", error.localizedDescription);
				return;
			}

			DDLogVerbose(@"CSV:\n\n%@", csvString);
			NSURL *tempURL = [NSURL fileURLWithPath:tempFilename];

			UIDocumentInteractionController *documentIC = [UIDocumentInteractionController interactionControllerWithURL:tempURL];
			documentIC.delegate = self;
			[documentIC presentPreviewAnimated:YES];
		}
		else {
			[self.progressHUD performAction:M13ProgressViewActionFailure
								   animated:YES];
			noResultsBlock();
		}
	};

	u_int32_t typeIndex = 0;
	__block BOOL isLastType = NO;

	[self.progressHUD show:YES];
	self.progressHUD.status = NSLocalizedString(@"Exporting HealthKit data...", @"");
	NSLock *lock = [[NSLock alloc] init];

	__block u_int32_t totalLeft = (u_int32_t)self.readTypes.count;

	for (id type in self.readTypes) {
		if ((typeIndex + 1) == self.readTypes.count) {
			isLastType = YES;
		}

		HKQuantityType *sampleType = (HKQuantityType *)type;
		NSPredicate *predicate = [HKQuery predicateForSamplesWithStartDate:startDate
																   endDate:endDate
																   options:HKQueryOptionNone];

		NSDictionary *typeData = [MARHealthStore dataForType:sampleType.identifier];
		HKUnit *unit = [MARHealthStore unitForType:sampleType.identifier];

		HKQuery *query;
		if (format == MARExportFormatJSON) {
			query = [[HKSampleQuery alloc] initWithSampleType:(HKSampleType *)sampleType
													predicate:predicate
														limit:0
											  sortDescriptors:nil
											   resultsHandler:^(HKSampleQuery *query, NSArray *sampleResults, NSError *error) {
												   @synchronized(lock) {
													   --totalLeft;

													   isLastType = (totalLeft == 0);
													   if (!sampleResults.count || error) {
														   DDLogError(@"Cannot fetch results for %@! Error: %@", sampleType.identifier, error.localizedDescription);

														   if (isLastType) {
															   dispatch_sync(dispatch_get_main_queue(), ^{
																   JSONCompletionBlock(NO);
															   });
														   }

														   return;
													   }

													   dispatch_sync(dispatch_get_main_queue(), ^{
														   NSMutableDictionary *sampleData = @{}.mutableCopy;
														   sampleData[MARExportResultNameKey] = sampleType.identifier;
														   sampleData[MARExportResultUnitKey] = unit.unitString;
														   NSMutableArray *processedSampleResults = @[].mutableCopy;
														   for (HKQuantitySample *sample in sampleResults) {
															   NSString *startDateString = [NSDate bd_ISO8601StringFromDate:sample.startDate];
															   NSString *endDateString = [NSDate bd_ISO8601StringFromDate:sample.endDate];
															   [processedSampleResults addObject: @{
																									MARExportResultStartDateKey	: startDateString,
																									MARExportResultEndDateKey	: endDateString,
																									MARExportResultQuantityKey	: @([sample.quantity doubleValueForUnit:unit]),
																									MARExportResultMetadataKey	: [NSDictionary dictionaryWithDictionary:sample.metadata],
																									}];
														   }
														   sampleData[MARExportResultSamplesKey] = [NSArray arrayWithArray:processedSampleResults];

														   exportedResults[sampleType.identifier] = sampleData;

														   if (isLastType) {
															   JSONCompletionBlock(YES);
														   }
													   });
												   }
											   }];
		}
		else {
			if (![typeData[MARHKTypeIsCumulativeKey] boolValue]) {
				--totalLeft;
				if (isLastType) {
					CSVCompletionBlock(YES);
				}
				typeIndex++;
				continue;
			}

			NSDate *anchorDate;
			NSDateComponents *intervalComponents = [[NSDateComponents alloc] init];

			NSDateComponents *anchorComponents = [calendar components:NSCalendarUnitDay | NSCalendarUnitMonth | NSCalendarUnitYear | NSCalendarUnitWeekday
															 fromDate:[NSDate date]];

			NSInteger offset = (7 + anchorComponents.weekday - 2) % 7;
			anchorComponents.day -= offset;
			anchorComponents.hour = 0;

			switch (range) {
				default:
				case MARExportTimeRangeAllTime:
					// 1 month
					intervalComponents.month = 1;
					break;

				case MARExportTimeRangeDay:
					// 1 hour
					intervalComponents.hour = 1;
					break;
				case MARExportTimeRangeWeek:
				case MARExportTimeRangeMonth:
					// 1 day
					intervalComponents.day = 1;
					break;
			}

			anchorDate = [calendar dateFromComponents:anchorComponents];

			query = [[HKStatisticsCollectionQuery alloc] initWithQuantityType:sampleType
													  quantitySamplePredicate:predicate
																	  options:HKStatisticsOptionCumulativeSum
																   anchorDate:anchorDate
														   intervalComponents:intervalComponents];

			((HKStatisticsCollectionQuery *)query).initialResultsHandler = ^(HKStatisticsCollectionQuery *query, HKStatisticsCollection *sampleResults, NSError *error) {
				@synchronized(lock) {
					--totalLeft;

					isLastType = (totalLeft == 0);
					if (!sampleResults.statistics.count || error) {
						DDLogError(@"Cannot fetch results for %@! Error: %@", sampleType.identifier, error.localizedDescription);

						if (isLastType) {
							dispatch_sync(dispatch_get_main_queue(), ^{
								CSVCompletionBlock(NO);
							});
						}

						return;
					}

					columns[sampleType.identifier] = [NSString stringWithFormat:@"%@ (%@)", typeData[MARHKTypeNameKey], typeData[MARHKTypeUnitStringKey]];

					BOOL isLastResult = NO;
					for (u_int32_t resultIndex = 0; resultIndex < sampleResults.statistics.count; resultIndex++) {
						if ((resultIndex + 1) == sampleResults.statistics.count) {
							isLastResult = YES && isLastType;
						}

						HKStatistics *result = sampleResults.statistics[resultIndex];
						HKQuantity *quantity = result.sumQuantity;
						if (quantity) {

							NSString *dateString = [NSDate bd_keyStringFromDate:result.startDate];

							NSMutableDictionary *dateResultDictionary = [NSMutableDictionary dictionaryWithDictionary:exportedResults[dateString]];

							double resultValue = [quantity doubleValueForUnit:unit];
							dateResultDictionary[sampleType.identifier] = @(resultValue);

							exportedResults[dateString] = dateResultDictionary;
						}

						if (isLastResult) {
							dispatch_sync(dispatch_get_main_queue(), ^{
								CSVCompletionBlock(YES);
							});
						}
					}
				}
			};
		}

		[self.healthStore executeQuery:query];

		typeIndex++;
	}
}

#pragma mark - Document interaction delegate

- (UIViewController *)documentInteractionControllerViewControllerForPreview:(UIDocumentInteractionController *)controller
{
	return self;
}

- (UIView *)documentInteractionControllerViewForPreview:(UIDocumentInteractionController *)controller
{
	return self.view;
}

- (CGRect)documentInteractionControllerRectForPreview:(UIDocumentInteractionController *)controller
{
	return self.view.frame;
}

@end
