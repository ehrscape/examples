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

#import "MARSampleViewCell.h"

@implementation MARSampleViewCell

- (IBAction)accessSwitchChanged:(UISwitch *)sender
{
	NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
	NSData *typeData = [defaults objectForKey:MARDefaultsTypeKey];
	NSMutableDictionary *mutableTypes = nil;
	if (typeData) {
		mutableTypes = [NSMutableDictionary dictionaryWithDictionary:[NSKeyedUnarchiver unarchiveObjectWithData:typeData]];
		mutableTypes[self.name] = @(sender.on);
	}

	typeData = [NSKeyedArchiver archivedDataWithRootObject:[NSDictionary dictionaryWithDictionary:mutableTypes]];
	[defaults setObject:typeData
				 forKey:MARDefaultsTypeKey];
	[defaults synchronize];

}

@end
