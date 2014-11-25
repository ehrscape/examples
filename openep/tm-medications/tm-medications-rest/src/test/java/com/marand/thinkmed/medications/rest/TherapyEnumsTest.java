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

package com.marand.thinkmed.medications.rest;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.marand.maf.core.JsonUtil;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.InfusionSetChangeEnum;
import com.marand.thinkmed.medications.MedicationOrderFormType;
import com.marand.thinkmed.medications.MedicationRouteTypeEnum;
import com.marand.thinkmed.medications.MedicationStartCriterionEnum;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.TherapyTag;
import com.marand.thinkmed.medications.TherapyTemplateTypeEnum;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.jaxb.json.JsonParsing;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.database.annotations.Transactional;
import org.unitils.database.util.TransactionMode;
import org.unitils.spring.annotation.SpringApplicationContext;

/**
 * @author Mitja Lapajne
 */
@RunWith(UnitilsJUnit4TestClassRunner.class)
@Transactional(TransactionMode.ROLLBACK)
@SpringApplicationContext( {"/com/marand/maf_test/unitils/tc-unitils.xml"}
)
public class TherapyEnumsTest
{
  private static final Pattern NEW_LINE_PATTERN = Pattern.compile("\\r?\\n");
  private static final Pattern COLON_PATTERN = Pattern.compile(":");

  @Test
  public void testEnums() throws Exception
  {
    final InputStream inputStream = JsonParsing.class.getResourceAsStream("/app/views/medications/jquery/TherapyEnums.js");
    final String enumsString = IOUtils.toString(inputStream);
    assertEnum("templateTypeEnum", enumsString, TherapyTemplateTypeEnum.values());
    assertEnum("medicationRouteTypeEnum", enumsString, MedicationRouteTypeEnum.values());
    assertEnum("dosingFrequencyTypeEnum", enumsString, DosingFrequencyTypeEnum.values());
    assertEnum("medicationOrderFormType", enumsString, MedicationOrderFormType.values());
    assertEnum("therapyStatusEnum", enumsString, TherapyStatusEnum.values());
    assertEnum("administrationTypeEnum", enumsString, AdministrationTypeEnum.values());
    assertEnum("infusionSetChangeEnum", enumsString, InfusionSetChangeEnum.values());
    assertEnum("therapySortTypeEnum", enumsString, TherapySortTypeEnum.values());
    assertEnum("medicationStartCriterionEnum", enumsString, MedicationStartCriterionEnum.values());
    assertEnum("therapyTag", enumsString, TherapyTag.values());
  }

  private void assertEnum(final String enumName, final String enumsString, final Object[] enumValues)
  {
    final int enumIndex = enumsString.indexOf(enumName);
    final int start = enumsString.indexOf('{', enumIndex);
    final int end = enumsString.indexOf('}', enumIndex) + 1;
    final String enumString = enumsString.substring(start, end);

    final List<Object> enums = new ArrayList<>();
    final String[] lines = NEW_LINE_PATTERN.split(enumString);
    for (final String line : lines)
    {
      final String[] strings = COLON_PATTERN.split(line);
      if (strings.length > 1)
      {
        enums.add(JsonUtil.fromJson(strings[0], enumValues[0].getClass()));
      }
    }

    Assert.assertArrayEquals(enums.toArray(), enumValues);
  }
}
