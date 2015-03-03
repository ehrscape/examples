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

#import "NSDate+MARAdditions.h"
#include <time.h>
#include <xlocale.h>

#define ISO8601_MAX_LEN 25

@implementation NSDate(MARAdditions)

+ (NSDate *)bd_dateFromISO8601String:(NSString *)iso8601
{
	if (!iso8601) {
		return nil;
	}

	const char *str = [iso8601 cStringUsingEncoding:NSUTF8StringEncoding];
	char newStr[ISO8601_MAX_LEN];
	bzero(newStr, ISO8601_MAX_LEN);

	size_t len = strlen(str);
	if (len == 0) {
		return nil;
	}

	if (len == 20 && str[len - 1] == 'Z') {
		memcpy(newStr, str, len - 1);
		strncpy(newStr + len - 1, "+0000\0", 6);
	}
	else if (len == 25 && str[22] == ':') {
		memcpy(newStr, str, 22);
		memcpy(newStr + 22, str + 23, 2);
	}
	else if (len == 29 && str[19] == '.') {
		memcpy(newStr, str, 19);
		memcpy(newStr + 19, str + 23, strlen(str) - 23);
		memcpy(newStr + 22, str + 27, 2);
	}
	else {
		memcpy(newStr, str, len > ISO8601_MAX_LEN - 1 ? ISO8601_MAX_LEN - 1 : len);
	}

	newStr[sizeof(newStr) - 1] = 0;

	struct tm tm = {
		.tm_sec = 0,
		.tm_min = 0,
		.tm_hour = 0,
		.tm_mday = 0,
		.tm_mon = 0,
		.tm_year = 0,
		.tm_wday = 0,
		.tm_yday = 0,
		.tm_isdst = -1,
	};

	if (strptime_l(newStr, "%FT%T%z", &tm, NULL) == NULL) {
		return nil;
	}

	return [NSDate dateWithTimeIntervalSince1970:mktime(&tm)];
}

+ (NSString *)bd_ISO8601StringFromDate:(NSDate *)date
{
	return [[self bd_ISO8601DateFormatter] stringFromDate:date];
}

+ (NSString *)bd_keyStringFromDate:(NSDate *)date
{
	return [[self bd_keyDateFormatter] stringFromDate:date];
}

+ (NSDateFormatter *)bd_keyDateFormatter
{
	static NSDateFormatter *_keyDateFormatter = nil;
	static dispatch_once_t onceToken;
	dispatch_once(&onceToken, ^{
		_keyDateFormatter = [[NSDateFormatter alloc] init];
		_keyDateFormatter.locale = [NSLocale localeWithLocaleIdentifier:@"en_US_POSIX"];
		_keyDateFormatter.dateFormat = @"YYYY-MM-dd HH:mm";
	});

	return _keyDateFormatter;
}

+ (NSDateFormatter *)bd_ISO8601DateFormatter
{
	static NSDateFormatter *_ISO8601DateFormatter = nil;
	static dispatch_once_t onceToken;
	dispatch_once(&onceToken, ^{
		_ISO8601DateFormatter = [[NSDateFormatter alloc] init];
		_ISO8601DateFormatter.locale = [NSLocale localeWithLocaleIdentifier:@"en_US_POSIX"];
		_ISO8601DateFormatter.dateFormat = @"YYYY-MM-dd'T'HH:mm:ssZZZ";
	});

	return _ISO8601DateFormatter;
}
@end
