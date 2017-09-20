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

package com.marand.thinkmed.medications.business.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.jgroups.util.Util.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author Mitja Lapajne
 */
@RunWith(MockitoJUnitRunner.class)
public class TherapyIdUtilTest
{
  @Test
  public void testIsValidTherapyIdNull()
  {
    assertFalse(TherapyIdUtils.isValidTherapyId(null));
  }

  @Test
  public void testIsValidTherapyIdNoPipe()
  {
    assertFalse(TherapyIdUtils.isValidTherapyId("abc"));
  }

  @Test
  public void testIsValidTherapyIdNotAUUID()
  {
    assertFalse(TherapyIdUtils.isValidTherapyId("abc|Medication instruction"));
  }

  @Test
  public void testIsValidTherapyIdValid()
  {
    assertTrue(TherapyIdUtils.isValidTherapyId("123e4567-e89b-12d3-a456-426655440000|Medication instruction"));
  }
}
