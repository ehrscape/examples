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

package com.marand.thinkmed.medications.converter.therapy;

import com.marand.maf.core.JsonUtil;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.SelfAdministeringActionEnum;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.dto.TherapyDto;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.DvParsable;

/**
 * @author Bostjan Vester
 */
public abstract class MedicationToEhrConverter<M extends TherapyDto>
{
  public abstract boolean isFor(final TherapyDto therapy);

  public final MedicationInstructionInstruction createInstructionFromTherapy(final TherapyDto therapy)
  {
    final MedicationInstructionInstruction instruction = MedicationsEhrUtils.createEmptyMedicationInstruction();
    fillInstructionFromTherapy(instruction, (M)therapy);
    return instruction;
  }

  public void fillSelfAdministrationDataToEhr(final MedicationInstructionInstruction instruction, final TherapyDto therapy)
  {
    final SelfAdministeringActionEnum selfAdministeringActionEnum = therapy.getSelfAdministeringActionEnum();
    final DateTime selfAdministeringLastChange = therapy.getSelfAdministeringLastChange();
    if (selfAdministeringActionEnum != null)
    {
      //TODO Nejc save somewhere else in new template
      final DvParsable parsableDoseDescription = new DvParsable();
      parsableDoseDescription.setValue(selfAdministeringActionEnum.name());
      parsableDoseDescription.setFormalism(JsonUtil.toJson(selfAdministeringLastChange));
      instruction.getOrder().get(0).setParsableDoseDescription(parsableDoseDescription);
    }
  }

  public void fillBnfToEhr(final MedicationInstructionInstruction instruction, final TherapyDto therapy)
  {
    if (therapy.getBnfMaximumPercentage() != null)
    {
      //TODO Nejc save somewhere else in new template
      instruction.setConcessionBenefit(DataValueUtils.getText(String.valueOf(therapy.getBnfMaximumPercentage())));
    }
  }

  public abstract void fillInstructionFromTherapy(MedicationInstructionInstruction instruction, M order);
}
