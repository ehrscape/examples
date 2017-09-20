/*
 * Copyright (c) 2010-2016 Marand d.o.o. (www.marand.com)
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

package com.marand.thinkmed.medications.batch.impl;

import java.util.ArrayList;
import java.util.List;

import com.marand.maf.core.Pair;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.batch.TherapyBatchActionHandler;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.CodedNameDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonEnum;
import com.marand.thinkmed.medications.therapy.TherapyCacheInvalidator;
import com.marand.thinkmed.medications.therapy.TherapyUpdater;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openehr.jaxb.rm.ObjectVersionId;
import org.openehr.jaxb.rm.UidBasedId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

/**
 * @author Mitja Lapajne
 */
@RunWith(MockitoJUnitRunner.class)
public class TherapyBatchActionHandlerTest
{
  @InjectMocks
  private TherapyBatchActionHandler therapyBatchActionHandler = new TherapyBatchActionHandlerImpl();

  @Mock
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Mock
  private MedicationsBo medicationsBo;

  @Mock
  private TherapyUpdater therapyUpdater;

  @Mock
  private TherapyCacheInvalidator therapyCacheInvalidator;

  @Before
  public void setUpMocks()
  {
    Mockito.reset(therapyUpdater);
    Mockito.reset(medicationsOpenEhrDao);
    Mockito.reset(medicationsBo);

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs = new ArrayList<>();

    final MedicationInstructionInstruction instruction = new MedicationInstructionInstruction();
    instruction.setName(DataValueUtils.getText("Medication instruction"));

    final MedicationOrderComposition composition = new MedicationOrderComposition();
    final UidBasedId uidBasedId = new ObjectVersionId();
    uidBasedId.setValue("uid1");
    composition.setUid(uidBasedId);
    final MedicationDetailSection medicationDetail = new MedicationDetailSection();
    medicationDetail.getMedicationInstruction().add(instruction);
    composition.setMedicationDetail(medicationDetail);

    instructionPairs.add(Pair.of(composition, instruction));

    final TherapyChangeReasonDto therapyChangeReasonDto = new TherapyChangeReasonDto();
    therapyChangeReasonDto.setChangeReason(
        new CodedNameDto(
            TherapyChangeReasonEnum.TEMPORARY_LEAVE.toFullString(),
            TherapyChangeReasonEnum.TEMPORARY_LEAVE.toFullString()));
  }

  @Test
  public void testAbortAllTherapies()
  {
    therapyBatchActionHandler.abortAllTherapies("1", new DateTime(2016, 6, 10, 12, 0));
  }

  @Test
  public void testSuspendAllTherapies()
  {
    therapyBatchActionHandler.suspendAllTherapies("1", new DateTime(2016, 6, 10, 12, 0));
  }

  @Test
  public void testSuspendAllTherapiesOnTemporaryLeave()
  {
    therapyBatchActionHandler.suspendAllTherapiesOnTemporaryLeave("1", new DateTime(2016, 6, 10, 12, 0));
  }

  @Test
  public void testReissueAllTherapiesOnReturnFromTemporaryLeave()
  {
    therapyBatchActionHandler.reissueAllTherapiesOnReturnFromTemporaryLeave("1", new DateTime(2016, 6, 10, 12, 0));
  }
}
