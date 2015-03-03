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

#import "MARHealthStore.h"

@interface MARHealthStore()

@property (nonatomic, strong) NSSet *readTypes;
@property (nonatomic, strong) NSSet *shareTypes;
@property (nonatomic, strong) NSDictionary *types;

@end

@implementation MARHealthStore

#pragma mark - Singleton

+ (instancetype)sharedInstance
{
	static id __sharedInstance = nil;
	static dispatch_once_t __onceToken;
	dispatch_once(&__onceToken, ^{
		__sharedInstance = [[self alloc] init];
	});
	return __sharedInstance;
}

#pragma mark - Authorization

+ (void)requestAuthorizationForCharacteristicsWithCompletion:(void(^)(BOOL success, NSError *error))completion
{
	if ([MARHealthStore isHealthDataAvailable]) {
		[[MARHealthStore sharedInstance] requestAuthorizationToShareTypes:nil
															   readTypes:[MARHealthStore sharedInstance].characteristicTypes
															  completion:completion];
	}
}

+ (void)requestAuthorizationWithCompletion:(void(^)(BOOL success, NSError *error))completion
{
	if ([MARHealthStore isHealthDataAvailable]) {
		[[MARHealthStore sharedInstance] requestAuthorizationToShareTypes:[MARHealthStore sharedInstance].shareTypes
															   readTypes:[MARHealthStore sharedInstance].readTypes
															  completion:completion];
	}
}

#pragma mark - Properties

- (NSSet *)readTypes
{
	if (!_readTypes) {
		NSMutableSet *mutableTypes = [NSMutableSet set];

		[mutableTypes unionSet:self.quantityTypes];

		[mutableTypes removeObject:[HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierNikeFuel]];

		[mutableTypes unionSet:self.workoutTypes];
		[mutableTypes unionSet:self.characteristicTypes];

		_readTypes = [NSSet setWithSet:mutableTypes];
	}

	return _readTypes;
}

- (NSSet *)shareTypes
{
	if (!_shareTypes) {
		NSMutableSet *mutableTypes = [NSMutableSet set];

		[mutableTypes unionSet:self.quantityTypes];

		[mutableTypes removeObject:[HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierNikeFuel]];

		[mutableTypes unionSet:self.workoutTypes];

		_shareTypes = [NSSet setWithSet:mutableTypes];
	}
	return _shareTypes;
}

- (NSDictionary *)types
{
	if (!_types) {
		NSFileManager *fileManager = [NSFileManager defaultManager];
		NSError *error = nil;
		NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
		NSString *path = paths.firstObject;

		NSString *filename = @"HealthStore";
		NSString *extension = @"plist";

		NSString *destinationPath = [[path stringByAppendingPathComponent:filename] stringByAppendingPathExtension:extension];

		if (![fileManager fileExistsAtPath:destinationPath]) {
			NSString *sourcePath = [[NSBundle mainBundle] pathForResource:filename
															   ofType:extension];
			[fileManager copyItemAtPath:sourcePath
								 toPath:destinationPath
								  error:&error];
		}

		if (!error) {
			_types = [NSDictionary dictionaryWithContentsOfFile:destinationPath];
		}
	}

	return _types;
}

- (NSSet *)cumulativeTypes
{
	NSMutableSet *mutableTypes = [NSMutableSet set];
	for (HKObjectType *type in self.quantityTypes) {
		NSString *identifier = type.identifier;
		NSDictionary *typeData = self.types[MARHKTypeQuantityKey][identifier];
		if ([typeData[MARHKTypeIsCumulativeKey] boolValue]) {
			[mutableTypes addObject:[HKObjectType quantityTypeForIdentifier:identifier]];
		}
	}

	return mutableTypes;
}

- (NSSet *)quantityTypes
{
	return [self typeForKey:MARHKTypeQuantityKey];
}

- (NSSet *)characteristicTypes
{
	return [self typeForKey:MARHKTypeCharacteristicKey];
}

- (NSSet *)correlationTypes
{
	return [self typeForKey:MARHKTypeCorrelationKey];
}

- (NSSet *)workoutTypes
{
	return [self typeForKey:MARHKTypeWorkoutKey];
}

- (NSSet *)typeForKey:(NSString *)key
{
	if (!key.length || !self.types.count) {
		return nil;
	}

	NSMutableSet *mutableTypes = [NSMutableSet set];
	NSSet *types = [NSSet setWithArray:[self.types[key] allKeys]];
	for (NSString *identifier in types) {
		HKObjectType *type = nil;
		if ([key isEqualToString:MARHKTypeQuantityKey]) {
			type = [HKObjectType quantityTypeForIdentifier:identifier];
		}
		else if ([key isEqualToString:MARHKTypeCharacteristicKey]) {
			type = [HKObjectType characteristicTypeForIdentifier:identifier];
		}
		else if ([key isEqualToString:MARHKTypeCorrelationKey]) {
			type = [HKObjectType correlationTypeForIdentifier:identifier];
		}
		else if ([key isEqualToString:MARHKTypeWorkoutKey]) {
			type = [HKObjectType workoutType];
		}

		[mutableTypes addObject:type];
	}

	return [NSSet setWithSet:mutableTypes];
}

+ (NSDictionary *)dataForType:(NSString *)identifier
{
	NSDictionary *dictionary = nil;
	for (NSString *typeKey in [MARHealthStore sharedInstance].types) {
		NSDictionary *type = [MARHealthStore sharedInstance].types[typeKey];
		if (type[identifier] != nil) {
			dictionary = type[identifier];
			break;
		}
	}

	return dictionary;
}

+ (HKUnit *)unitForType:(NSString *)identifier
{
	NSDictionary *dictionary = [self dataForType:identifier];
	HKUnit *unit = nil;

	if (dictionary) {
		NSString *unitString = dictionary[MARHKTypeUnitStringKey];
		if (unitString.length) {
			unit = [HKUnit unitFromString:unitString];
		}

		if (!unit) {
			switch ([dictionary[MARHKTypeUnitKey] integerValue]) {
				default:
				case MARHKUnitTypeNone:
				case MARHKUnitTypeComplex:
				case MARHKUnitTypeNumber:
				case MARHKUnitTypeDate:
					break;

				case MARHKUnitTypeMass:
					unit = [HKUnit gramUnit];
					break;

				case MARHKUnitTypeLength:
					unit = [HKUnit meterUnit];
					break;

				case MARHKUnitTypeVolume:
					unit = [HKUnit literUnit];
					break;

				case MARHKUnitTypePressure:
					unit = [HKUnit pascalUnit];
					break;

				case MARHKUnitTypeTime:
					unit = [HKUnit secondUnit];
					break;

				case MARHKUnitTypeEnergy:
					unit = [HKUnit jouleUnit];
					break;

				case MARHKUnitTypeTemperature:
					unit = [HKUnit kelvinUnit];
					break;

				case MARHKUnitTypeConductance:
					unit = [HKUnit siemenUnit];
					break;

				case MARHKUnitTypeCount:
					unit = [HKUnit countUnit];
					break;

				case MARHKUnitTypePercent:
					unit = [HKUnit percentUnit];
					break;
			}
		}
	}
	return unit;
}

#pragma mark - String helpers

+ (NSString *)biologicalSexString:(HKBiologicalSexObject *)sex
{
	NSString *sexString;
	switch (sex.biologicalSex) {
		case HKBiologicalSexFemale: {
			sexString = @"Female";
			break;
		}

		case HKBiologicalSexMale: {
			sexString = @"Male";
			break;
		}

		case HKBiologicalSexNotSet:
		default: {
			sexString = @"Not set";
			break;
		}
	}
	return NSLocalizedString(sexString, sexString);
}

+ (NSString *)bloodTypeString:(HKBloodTypeObject *)bloodType
{
	NSString *bloodTypeString;
	switch (bloodType.bloodType) {
		case HKBloodTypeABNegative: {
			bloodTypeString = @"AB-";
			break;
		}

		case HKBloodTypeABPositive: {
			bloodTypeString = @"AB+";
			break;
		}

		case HKBloodTypeANegative: {
			bloodTypeString = @"A-";
			break;
		}

		case HKBloodTypeAPositive: {
			bloodTypeString = @"A+";
			break;
		}

		case HKBloodTypeBNegative: {
			bloodTypeString = @"B-";
			break;
		}

		case HKBloodTypeBPositive: {
			bloodTypeString = @"B+";
			break;
		}

		case HKBloodTypeONegative: {
			bloodTypeString = @"O-";
			break;
		}

		case HKBloodTypeOPositive: {
			bloodTypeString = @"O+";
			break;
		}

		case HKBloodTypeNotSet:
		default: {
			bloodTypeString = @"Not set";
			break;
		}
	}
	return NSLocalizedString(bloodTypeString, bloodTypeString);
}

@end