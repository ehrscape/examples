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

#if DEBUG
	static DDLogLevel const ddLogLevel								= DDLogLevelVerbose;
#else
	static DDLogLevel const ddLogLevel								= DDLogLevelInfo;
#endif

typedef NS_ENUM(u_int8_t, MARHKUnitType)
{
	MARHKUnitTypeNone = 0,
	MARHKUnitTypeMass,
	MARHKUnitTypeLength,
	MARHKUnitTypeVolume,
	MARHKUnitTypePressure,
	MARHKUnitTypeTime,
	MARHKUnitTypeEnergy,
	MARHKUnitTypeTemperature,
	MARHKUnitTypeConductance,
	MARHKUnitTypeCount,
	MARHKUnitTypePercent,
	MARHKUnitTypeNumber,
	MARHKUnitTypeDate,
	MARHKUnitTypeComplex,

	MARHKUnitTypeMax = INT8_MAX
};

typedef NS_ENUM(u_int8_t, MARExportTimeRange)
{
	MARExportTimeRangeAllTime = 0,
	MARExportTimeRangeDay,
	MARExportTimeRangeWeek,
	MARExportTimeRangeMonth,

	MARExportTimeRangeMax = UINT8_MAX
};

typedef NS_ENUM(u_int8_t, MARExportFormat)
{
	MARExportFormatJSON = 0,
	MARExportFormatCSV,
	MARExportFormatMax = UINT8_MAX
};

static NSString * const MARSegueExport										= @"ExportSettings";
static NSString * const MARCellIdentifierSample								= @"SampleCell";

static NSString * const MARDefaultsTypeKey									= @"MARHKDefaultsTypeKey";

static NSString * const MARHKTypeColumnsKey									= @"columns";
static NSString * const MARHKTypeNameKey										= @"name";
static NSString * const MARHKTypeUnitKey										= @"unit";
static NSString * const MARHKTypeUnitStringKey								= @"unitString";
static NSString * const MARHKTypeQuantityKey									= @"quantity";
static NSString * const MARHKTypeCharacteristicKey							= @"characteristic";
static NSString * const MARHKTypeCorrelationKey								= @"correlation";
static NSString * const MARHKTypeWorkoutKey									= @"workout";
static NSString * const MARHKTypeIsCumulativeKey								= @"isCumulative";

static NSString * const MARExportResultNameKey								= @"name";
static NSString * const MARExportResultUnitKey								= @"unit";
static NSString * const MARExportResultStartDateKey							= @"startDate";
static NSString * const MARExportResultEndDateKey							= @"endDate";
static NSString * const MARExportResultQuantityKey							= @"quantity";
static NSString * const MARExportResultMetadataKey							= @"metadata";
static NSString * const MARExportResultSamplesKey							= @"samples";