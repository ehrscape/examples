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

package com.marand.thinkmed.medications.process.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.database.annotations.Transactional;
import org.unitils.database.util.TransactionMode;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;

/**
 * @author Mitja Lapajne
 */
@RunWith(UnitilsJUnit4TestClassRunner.class)
@Transactional(TransactionMode.ROLLBACK)
@SpringApplicationContext(
    {
        "/com/marand/maf_test/unitils/tc-unitils.xml",
        "com/marand/thinkmed/medications/process/impl/MedicationsProcessServiceTest-context.xml"
    }
)
public class MedicationsProcessServiceTest
{
  @SpringBeanByName
  private MedicationsProcessServiceImpl medicationsProcessService;

  @Test
  public void todo()
  {
    //TODO THERAPY_TASK
  }
}
