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

package com.marand.thinkmed.medications.client;

import com.marand.thinkmed.medications.service.MedicationsService;
import com.marand.maf.core.spring.beans.SpringBean;
import com.marand.maf.core.spring.beans.SpringReference;

/**
 * @author Bostjan Vester
 */
@SpringReference(beanFactoryId = "com.marand.thinkmed.medications.client", beanId = "medicationsContext")
public final class MedicationsContext
{
  private MedicationsService service;

  private MedicationsContext()
  {
  }

  public MedicationsService getService()
  {
    return service;
  }

  public void setService(final MedicationsService service)
  {
    this.service = service;
  }

  public static MedicationsService getMedicationsService()
  {
    return SpringBean.getInstance(MedicationsContext.class).getService();
  }
}
