/*
 * Copyright (c) 2010-2014 Marand d.o.o. (www.marand.com)
 *
 * This file is part of Think!Med Clinical Medication Management.
 *
 * Think!Med Clinical Medication Management is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Think!Med Clinical Medication Management is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Think!Med Clinical Medication Management.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.marand.thinkmed.medications.dto;

import com.marand.maf.core.data.object.HourMinuteDto;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Mitja Lapajne
 */
public class HourMinuteDtoTest
{
  @Test
  public void testCombine()
  {
    final DateTime combined = new HourMinuteDto(2, 30).combine(new DateTime(2017, 3, 25, 0, 0));
    Assert.assertTrue(combined.isEqual(new DateTime(2017, 3, 25, 2, 30)));
  }

  @Test
  public void testCombineStartOfDstGap()
  {
    final DateTime combined = new HourMinuteDto(2, 30).combine(new DateTime(2017, 3, 26, 0, 0));
    Assert.assertTrue(combined.isEqual(new DateTime(2017, 3, 26, 3, 0)));
  }

  @Test
  public void testCombineInDstGap()
  {
    final DateTime combined = new HourMinuteDto(2, 30).combine(new DateTime(2017, 3, 26, 0, 0));
    Assert.assertTrue(combined.isEqual(new DateTime(2017, 3, 26, 3, 0)));
  }

  @Test
  public void testCombineEndOfDstGap()
  {
    final DateTime combined = new HourMinuteDto(2, 30).combine(new DateTime(2017, 3, 26, 0, 0));
    Assert.assertTrue(combined.isEqual(new DateTime(2017, 3, 26, 3, 0)));
  }
}
