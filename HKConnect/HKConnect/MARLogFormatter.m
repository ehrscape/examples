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

#import "MARLogFormatter.h"

@interface MARLogFormatter()
{
	int _loggerCount;
	NSDateFormatter *_dateFormatter;
}

@end

@implementation MARLogFormatter

- (id)init
{
	self = [super init];
	if(self) {
		_dateFormatter = [[NSDateFormatter alloc] init];
		_dateFormatter.formatterBehavior = NSDateFormatterBehavior10_4;
		_dateFormatter.locale = [NSLocale currentLocale];
		_dateFormatter.dateFormat = @"yyyy.MM.dd HH:mm:ss+zzz";
	}
	return self;
}

- (NSString *)formatLogMessage:(DDLogMessage *)logMessage
{
	NSString *logLevel;
	switch (logMessage.flag)
	{
		case DDLogFlagError    : logLevel = @"E"; break;
		case DDLogFlagWarning  : logLevel = @"W"; break;
		case DDLogFlagInfo     : logLevel = @"I"; break;
		case DDLogFlagDebug    : logLevel = @"D"; break;
		default                : logLevel = @"V"; break;
	}

	NSString *dateString = [_dateFormatter stringFromDate:logMessage->_timestamp];

	NSString *fileName = [[logMessage->_file componentsSeparatedByString:@"/"] lastObject];

	return [NSString stringWithFormat:@"[%@]:[%@:%@]:[%@:%llu, %@]: %@\n", dateString, logLevel, logMessage->_threadID, fileName, (u_int64_t)logMessage->_line,logMessage->_function, logMessage->_message];
}

- (void)didAddToLogger:(id <DDLogger>)logger
{
	_loggerCount++;
	NSAssert(_loggerCount <= 1, @"This logger isn't thread-safe");
}
- (void)willRemoveFromLogger:(id <DDLogger>)logger
{
	_loggerCount--;
}

@end
