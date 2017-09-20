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

package com.marand.thinkmed.medications.admission.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.marand.maf.core.openehr.dao.openehr.TaggingOpenEhrDao;
import com.marand.thinkmed.medications.TherapySourceGroupEnum;
import com.marand.thinkmed.medications.admission.MedicationOnAdmissionHandler;
import com.marand.thinkmed.medications.business.impl.DefaultMedicationsBo;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.connector.MedicationsConnector;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.discharge.MedicationOnDischargeHandler;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionGroupDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeDto;
import com.marand.thinkmed.medications.dto.supply.PrescriptionSupplyDto;
import junit.framework.Assert;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openehr.jaxb.rm.Composition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;

/**
 * @author Nejc Korasa
 */
@RunWith(MockitoJUnitRunner.class)
public class MedicationOnAdmissionHandlerTest
{
  @Mock
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Mock
  private TherapyDisplayProvider therapyDisplayProvider;

  @Mock
  private MedicationsConnector medicationsConnector;

  @Mock
  private MedicationOnDischargeHandler medicationOnDischargeHandler;

  @Mock
  private TaggingOpenEhrDao taggingOpenEhrDao;

  @Mock
  private DefaultMedicationsBo defaultMedicationsBo;

  @InjectMocks
  private MedicationOnAdmissionHandler medicationOnAdmissionHandler = new MedicationOnAdmissionHandlerImpl();

  @Before
  public void setUpMocks()
  {
    Mockito.when(medicationsConnector.getLastDischargedCentralCaseEffectiveInterval("1"))
        .thenReturn(new Interval(new DateTime(2015, 2, 2, 0, 0),
                                 new DateTime(2015, 3, 3, 0, 0)));

    /******** 1 previous discharge medications list - for patient "1" *********/
    final List<MedicationOnDischargeDto> previousDischargeMedications = new ArrayList<>();

    MedicationOnDischargeDto medicationOnDischargeDto = new MedicationOnDischargeDto();
    final ConstantSimpleTherapyDto therapy1 = new ConstantSimpleTherapyDto();
    therapy1.setTherapyDescription("Lekadol 500mg 1x per day po");
    therapy1.setCompositionUid("11");
    therapy1.setPrescriptionSupply(new PrescriptionSupplyDto());
    medicationOnDischargeDto.setTherapy(therapy1);
    previousDischargeMedications.add(medicationOnDischargeDto);

    MedicationOnDischargeDto medicationOnDischargeDto2 = new MedicationOnDischargeDto();
    final ConstantSimpleTherapyDto therapy2 = new ConstantSimpleTherapyDto();
    therapy2.setTherapyDescription("Lekadol 500mg 2x per day po");
    therapy2.setCompositionUid("12");
    therapy2.setPrescriptionSupply(new PrescriptionSupplyDto());
    medicationOnDischargeDto2.setTherapy(therapy2);
    previousDischargeMedications.add(medicationOnDischargeDto2);

    MedicationOnDischargeDto medicationOnDischargeDto3 = new MedicationOnDischargeDto();
    final ConstantSimpleTherapyDto therapy3 = new ConstantSimpleTherapyDto();
    therapy3.setTherapyDescription("Lekadol 500mg 3x per day po");
    therapy3.setCompositionUid("13");
    therapy3.setPrescriptionSupply(new PrescriptionSupplyDto());
    medicationOnDischargeDto3.setTherapy(therapy3);
    previousDischargeMedications.add(medicationOnDischargeDto3);

    Mockito.when(medicationOnDischargeHandler.getMedicationsOnDischarge(
        ArgumentMatchers.startsWith("1"),
        ArgumentMatchers.any(Interval.class),
        ArgumentMatchers.any(DateTime.class),
        ArgumentMatchers.any(Locale.class))).thenReturn(previousDischargeMedications);

    /******** 2 previous discharge medications list - for patient "2" *********/
    final List<MedicationOnDischargeDto> previousDischargeMedications2 = new ArrayList<>();
    Mockito.when(medicationOnDischargeHandler.getMedicationsOnDischarge(
        ArgumentMatchers.startsWith("2"),
        ArgumentMatchers.any(Interval.class),
        ArgumentMatchers.any(DateTime.class),
        ArgumentMatchers.any(Locale.class))).thenReturn(previousDischargeMedications2);
  }

  @Test
  public void getTherapiesOnAdmissionGroupsTest()
  {
    final String patientId = "1";
    final DateTime startDate = new DateTime(2015, 3, 3, 0, 0);
    final DateTime now = new DateTime(2015, 5, 6, 0, 0);
    final Locale locale = new Locale("en Gb");

    final List<MedicationOnAdmissionGroupDto> therapiesOnAdmissionGroups = medicationOnAdmissionHandler.getTherapiesOnAdmissionGroups(
        patientId,
        startDate,
        now,
        locale);

    Assert.assertEquals(therapiesOnAdmissionGroups.size(), 1);
    Assert.assertEquals(therapiesOnAdmissionGroups.get(0).getGroupEnum(), TherapySourceGroupEnum.LAST_DISCHARGE_MEDICATIONS);
    Assert.assertEquals(therapiesOnAdmissionGroups.get(0).getGroupElements().size(), 3);

    Assert.assertEquals(therapiesOnAdmissionGroups.get(0).getGroupElements().get(0).getSourceId(), "11");
    Assert.assertEquals(therapiesOnAdmissionGroups.get(0).getGroupElements().get(1).getSourceId(), "12");
    Assert.assertEquals(therapiesOnAdmissionGroups.get(0).getGroupElements().get(2).getSourceId(), "13");

    Assert.assertNull(therapiesOnAdmissionGroups.get(0).getGroupElements().get(0).getTherapy().getPrescriptionSupply());
    Assert.assertNull(therapiesOnAdmissionGroups.get(0).getGroupElements().get(1).getTherapy().getPrescriptionSupply());
    Assert.assertNull(therapiesOnAdmissionGroups.get(0).getGroupElements().get(2).getTherapy().getPrescriptionSupply());

    Assert.assertNull(therapiesOnAdmissionGroups.get(0).getGroupElements().get(0).getTherapy().getCompositionUid());
    Assert.assertNull(therapiesOnAdmissionGroups.get(0).getGroupElements().get(1).getTherapy().getCompositionUid());
    Assert.assertNull(therapiesOnAdmissionGroups.get(0).getGroupElements().get(2).getTherapy().getCompositionUid());

    Assert.assertNull(therapiesOnAdmissionGroups.get(0).getGroupElements().get(0).getTherapy().getPrescriptionLocalDetails());
    Assert.assertNull(therapiesOnAdmissionGroups.get(0).getGroupElements().get(1).getTherapy().getPrescriptionLocalDetails());
    Assert.assertNull(therapiesOnAdmissionGroups.get(0).getGroupElements().get(2).getTherapy().getPrescriptionLocalDetails());
  }

  @Test
  public void getTherapiesOnAdmissionGroupsTest2()
  {
    final String patientId = "2";
    final DateTime startDate = new DateTime(2015, 3, 3, 0, 0);
    final DateTime now = new DateTime(2015, 5, 6, 0, 0);
    final Locale locale = new Locale("en");

    final List<MedicationOnAdmissionGroupDto> therapiesOnAdmissionGroups = medicationOnAdmissionHandler.getTherapiesOnAdmissionGroups(
        patientId,
        startDate,
        now,
        locale);

    Assert.assertEquals(therapiesOnAdmissionGroups.size(), 1);
    Assert.assertEquals(therapiesOnAdmissionGroups.get(0).getGroupEnum(), TherapySourceGroupEnum.LAST_DISCHARGE_MEDICATIONS);
    Assert.assertEquals(therapiesOnAdmissionGroups.get(0).getGroupElements().size(), 0);
  }
}
