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

package com.marand.thinkmed.medications.infusion.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.marand.maf.core.Pair;
import com.marand.thinkmed.medications.AdministrationResultEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.administration.impl.AdministrationUtilsImpl;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.BolusAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.InfusionBagDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StopAdministrationDto;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Nejc Korasa
 */
@RunWith(MockitoJUnitRunner.class)
public class InfusionBagHandlerTest
{
  @InjectMocks
  private InfusionBagHandlerImpl infusionBagHandler;

  @Mock
  private MedicationsTasksProvider medicationsTasksProvider;

  @Mock
  private AdministrationUtilsImpl administrationUtils;

  @Test
  public void calculatePlannedInfusionBagChangeForContinuousInfusion()
  {
    final String patientId = "1";
    final String therapyId = "therapy1";

    final InfusionBagDto infusionBagDto = new InfusionBagDto(1000.0, "ml");
    final Pair<DateTime, InfusionBagDto> lastInfusionBag = Pair.of(new DateTime(2016, 1, 20, 2, 0, 0), infusionBagDto);


    final List<AdministrationDto> givenAdministrations = new ArrayList<>();

    final StartAdministrationDto administrationDto = new StartAdministrationDto(); // 10ml/h * 3h = 30ml
    final TherapyDoseDto administrationTherapyDoseDto = new TherapyDoseDto();
    administrationTherapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto.setSecondaryNumerator(100.0);
    administrationTherapyDoseDto.setSecondaryNumeratorUnit("mg");
    administrationTherapyDoseDto.setSecondaryDenominator(1.0);
    administrationTherapyDoseDto.setSecondaryDenominatorUnit("l");
    administrationTherapyDoseDto.setNumerator(10.0);
    administrationTherapyDoseDto.setNumeratorUnit("ml/h");
    administrationDto.setAdministrationId("Administration1");
    administrationDto.setAdministeredDose(administrationTherapyDoseDto);
    administrationDto.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto.setAdministrationTime(new DateTime(2016, 1, 20, 2, 0, 0));
    administrationDto.setTherapyId(therapyId);
    givenAdministrations.add(administrationDto);

    final AdjustInfusionAdministrationDto administrationDto2 = new AdjustInfusionAdministrationDto(); // 30ml/h * 3h = 90ml
    final TherapyDoseDto administrationTherapyDoseDto2 = new TherapyDoseDto();
    administrationTherapyDoseDto2.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto2.setNumerator(30.0);
    administrationTherapyDoseDto2.setNumeratorUnit("ml/h");
    administrationDto2.setAdministrationId("Administration2");
    administrationDto2.setAdministeredDose(administrationTherapyDoseDto2);
    administrationDto2.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto2.setAdministrationTime(new DateTime(2016, 1, 20, 5, 0, 0));
    administrationDto2.setTherapyId(therapyId);
    givenAdministrations.add(administrationDto2);

    final AdjustInfusionAdministrationDto administrationDto3 = new AdjustInfusionAdministrationDto(); // 40ml/h -> remains : 22h
    final TherapyDoseDto administrationTherapyDoseDto3 = new TherapyDoseDto();
    administrationTherapyDoseDto3.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto3.setNumerator(40.0);
    administrationTherapyDoseDto3.setNumeratorUnit("ml/h");
    administrationDto3.setAdministrationId("Administration3");
    administrationDto3.setAdministeredDose(administrationTherapyDoseDto3);
    administrationDto3.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto3.setAdministrationTime(new DateTime(2016, 1, 20, 8, 0, 0));
    administrationDto3.setTherapyId(therapyId);
    givenAdministrations.add(administrationDto3);

    // 20.1 8:00 + 22h = 21.1 6:00
    final DateTime dateTime = infusionBagHandler.calculatePlannedInfusionBagChangeForContinuousInfusion(
        null,
        Collections.emptyList(),
        givenAdministrations,
        infusionBagDto,
        lastInfusionBag.getFirst());

    assertEquals(new DateTime(2016, 1, 21, 6, 0, 0), dateTime);
  }

  @Test
  public void calculatePlannedInfusionBagChangeForContinuousInfusionWithStopTaskAfterEmptyBag()
  {
    final String patientId = "1";
    final String therapyId = "therapy1";

    final InfusionBagDto infusionBagDto = new InfusionBagDto(1000.0, "ml");
    final Pair<DateTime, InfusionBagDto> lastInfusionBag = Pair.of(new DateTime(2016, 1, 20, 2, 0, 0), infusionBagDto);


    final List<AdministrationDto> givenAdministrations = new ArrayList<>();

    final StartAdministrationDto administrationDto = new StartAdministrationDto(); // 10ml/h * 3h = 30ml
    final TherapyDoseDto administrationTherapyDoseDto = new TherapyDoseDto();
    administrationTherapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto.setSecondaryNumerator(100.0);
    administrationTherapyDoseDto.setSecondaryNumeratorUnit("mg");
    administrationTherapyDoseDto.setSecondaryDenominator(1.0);
    administrationTherapyDoseDto.setSecondaryDenominatorUnit("l");
    administrationTherapyDoseDto.setNumerator(10.0);
    administrationTherapyDoseDto.setNumeratorUnit("ml/h");
    administrationDto.setAdministrationId("Administration1");
    administrationDto.setAdministeredDose(administrationTherapyDoseDto);
    administrationDto.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto.setAdministrationTime(new DateTime(2016, 1, 20, 2, 0, 0));
    administrationDto.setTherapyId(therapyId);
    givenAdministrations.add(administrationDto);

    final AdjustInfusionAdministrationDto administrationDto2 = new AdjustInfusionAdministrationDto(); // 30ml/h * 3h = 90ml
    final TherapyDoseDto administrationTherapyDoseDto2 = new TherapyDoseDto();
    administrationTherapyDoseDto2.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto2.setNumerator(30.0);
    administrationTherapyDoseDto2.setNumeratorUnit("ml/h");
    administrationDto2.setAdministrationId("Administration2");
    administrationDto2.setAdministeredDose(administrationTherapyDoseDto2);
    administrationDto2.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto2.setAdministrationTime(new DateTime(2016, 1, 20, 5, 0, 0));
    administrationDto2.setTherapyId(therapyId);
    givenAdministrations.add(administrationDto2);

    final AdjustInfusionAdministrationDto administrationDto3 = new AdjustInfusionAdministrationDto(); // 40ml/h -> remains : 22h
    final TherapyDoseDto administrationTherapyDoseDto3 = new TherapyDoseDto();
    administrationTherapyDoseDto3.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto3.setNumerator(40.0);
    administrationTherapyDoseDto3.setNumeratorUnit("ml/h");
    administrationDto3.setAdministrationId("Administration3");
    administrationDto3.setAdministeredDose(administrationTherapyDoseDto3);
    administrationDto3.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto3.setAdministrationTime(new DateTime(2016, 1, 20, 8, 0, 0));
    administrationDto3.setTherapyId(therapyId);
    givenAdministrations.add(administrationDto3);

    final StopAdministrationDto administrationDto4 = new StopAdministrationDto();
    administrationDto4.setAdministrationId("Administration4");
    administrationDto4.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto4.setAdministrationTime(new DateTime(2016, 1, 21, 10, 0, 0));
    administrationDto4.setTherapyId(therapyId);
    givenAdministrations.add(administrationDto4);

    // 20.1 8:00 + 22h = 21.1 6:00
    final DateTime dateTime = infusionBagHandler.calculatePlannedInfusionBagChangeForContinuousInfusion(
        null,
        Collections.emptyList(),
        givenAdministrations,
        infusionBagDto,
        lastInfusionBag.getFirst());

    assertEquals(new DateTime(2016, 1, 21, 6, 0, 0), dateTime);
  }

  @Test
  public void calculatePlannedInfusionBagChangeForContinuousInfusionWithStop()
  {
    final String patientId = "1";
    final String therapyId = "therapy1";

    final InfusionBagDto infusionBagDto = new InfusionBagDto(1000.0, "ml");
    final Pair<DateTime, InfusionBagDto> lastInfusionBag = Pair.of(new DateTime(2016, 1, 20, 2, 0, 0), infusionBagDto);

    final List<AdministrationDto> givenAdministrations = new ArrayList<>();

    final StartAdministrationDto administrationDto = new StartAdministrationDto(); // 10ml/h * 3h = 30ml
    final TherapyDoseDto administrationTherapyDoseDto = new TherapyDoseDto();
    administrationTherapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto.setSecondaryNumerator(100.0);
    administrationTherapyDoseDto.setSecondaryNumeratorUnit("mg");
    administrationTherapyDoseDto.setSecondaryDenominator(1.0);
    administrationTherapyDoseDto.setSecondaryDenominatorUnit("l");
    administrationTherapyDoseDto.setNumerator(10.0);
    administrationTherapyDoseDto.setNumeratorUnit("ml/h");
    administrationDto.setAdministrationId("Administration1");
    administrationDto.setAdministeredDose(administrationTherapyDoseDto);
    administrationDto.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto.setAdministrationTime(new DateTime(2016, 1, 20, 2, 0, 0));
    administrationDto.setTherapyId(therapyId);
    givenAdministrations.add(administrationDto);

    final AdjustInfusionAdministrationDto administrationDto2 = new AdjustInfusionAdministrationDto(); // 30ml/h * 3h = 90ml
    final TherapyDoseDto administrationTherapyDoseDto2 = new TherapyDoseDto();
    administrationTherapyDoseDto2.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto2.setNumerator(30.0);
    administrationTherapyDoseDto2.setNumeratorUnit("ml/h");
    administrationDto2.setAdministrationId("Administration2");
    administrationDto2.setAdministeredDose(administrationTherapyDoseDto2);
    administrationDto2.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto2.setAdministrationTime(new DateTime(2016, 1, 20, 5, 0, 0));
    administrationDto2.setTherapyId(therapyId);
    givenAdministrations.add(administrationDto2);

    final StopAdministrationDto administrationDto3 = new StopAdministrationDto(); // stop
    administrationDto3.setAdministrationId("Administration3");
    administrationDto3.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto3.setAdministrationTime(new DateTime(2016, 1, 20, 8, 0, 0));
    administrationDto3.setTherapyId(therapyId);
    givenAdministrations.add(administrationDto3);

    // stop task timestamp
    final DateTime dateTime = infusionBagHandler.calculatePlannedInfusionBagChangeForContinuousInfusion(
        null,
        Collections.emptyList(),
        givenAdministrations,
        infusionBagDto,
        lastInfusionBag.getFirst());

    assertNull(dateTime);
  }

  @Test
  public void getRemainingBagQuantityForContinuousInfusion()
  {
    final DateTime when = new DateTime(2016, 1, 20, 16, 0, 0);
    final String patientId = "1";
    final String therapyId = "therapy1";

    final InfusionBagDto infusionBagDto = new InfusionBagDto(1000.0, "ml");
    final Pair<DateTime, InfusionBagDto> lastInfusionBag = Pair.of(new DateTime(2016, 1, 20, 2, 0, 0), infusionBagDto);

    final List<AdministrationDto> givenAdministrations = new ArrayList<>();

    final StartAdministrationDto administrationDto = new StartAdministrationDto(); // 10ml/h * 3h = 30ml
    final TherapyDoseDto administrationTherapyDoseDto = new TherapyDoseDto();
    administrationTherapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto.setSecondaryNumerator(100.0);
    administrationTherapyDoseDto.setSecondaryNumeratorUnit("mg");
    administrationTherapyDoseDto.setSecondaryDenominator(1.0);
    administrationTherapyDoseDto.setSecondaryDenominatorUnit("l");
    administrationTherapyDoseDto.setNumerator(10.0);
    administrationTherapyDoseDto.setNumeratorUnit("ml/h");
    administrationDto.setAdministrationId("Administration1");
    administrationDto.setAdministeredDose(administrationTherapyDoseDto);
    administrationDto.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto.setAdministrationTime(new DateTime(2016, 1, 20, 2, 0, 0));
    administrationDto.setTherapyId(therapyId);
    givenAdministrations.add(administrationDto);

    final AdjustInfusionAdministrationDto administrationDto2 = new AdjustInfusionAdministrationDto(); // 30ml/h * 3h = 90ml
    final TherapyDoseDto administrationTherapyDoseDto2 = new TherapyDoseDto();
    administrationTherapyDoseDto2.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto2.setNumerator(30.0);
    administrationTherapyDoseDto2.setNumeratorUnit("ml/h");
    administrationDto2.setAdministrationId("Administration2");
    administrationDto2.setAdministeredDose(administrationTherapyDoseDto2);
    administrationDto2.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto2.setAdministrationTime(new DateTime(2016, 1, 20, 5, 0, 0));
    administrationDto2.setTherapyId(therapyId);
    givenAdministrations.add(administrationDto2);

    final AdjustInfusionAdministrationDto administrationDto3 = new AdjustInfusionAdministrationDto(); // 40ml/h * 8h = 320ml
    final TherapyDoseDto administrationTherapyDoseDto3 = new TherapyDoseDto();
    administrationTherapyDoseDto3.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto3.setNumerator(40.0);
    administrationTherapyDoseDto3.setNumeratorUnit("ml/h");
    administrationDto3.setAdministrationId("Administration3");
    administrationDto3.setAdministeredDose(administrationTherapyDoseDto3);
    administrationDto3.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto3.setAdministrationTime(new DateTime(2016, 1, 20, 8, 0, 0));
    administrationDto3.setTherapyId(therapyId);
    givenAdministrations.add(administrationDto3);

    final Double result = infusionBagHandler.getRemainingBagQuantityForContinuousInfusion(
        Collections.singletonList(therapyId),
        patientId,
        givenAdministrations,
        lastInfusionBag,
        when);

    assertEquals(560, result.intValue());
  }

  @Test
  public void getRemainingBagQuantityForContinuousInfusionWithBolus()
  {
    final DateTime when = new DateTime(2016, 1, 20, 16, 0, 0);
    final String patientId = "1";
    final String therapyId = "therapy1";

    final InfusionBagDto infusionBagDto = new InfusionBagDto(1000.0, "ml");
    final Pair<DateTime, InfusionBagDto> lastInfusionBag = Pair.of(new DateTime(2016, 1, 20, 2, 0, 0), infusionBagDto);

    final List<AdministrationDto> givenAdministrations = new ArrayList<>();

    final StartAdministrationDto administrationDto = new StartAdministrationDto(); // 10ml/h * 3h = 30ml
    final TherapyDoseDto administrationTherapyDoseDto = new TherapyDoseDto();
    administrationTherapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto.setSecondaryNumerator(100.0);
    administrationTherapyDoseDto.setSecondaryNumeratorUnit("mg");
    administrationTherapyDoseDto.setSecondaryDenominator(1.0);
    administrationTherapyDoseDto.setSecondaryDenominatorUnit("l");
    administrationTherapyDoseDto.setNumerator(10.0);
    administrationTherapyDoseDto.setNumeratorUnit("ml/h");
    administrationDto.setAdministrationId("Administration1");
    administrationDto.setAdministeredDose(administrationTherapyDoseDto);
    administrationDto.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto.setAdministrationTime(new DateTime(2016, 1, 20, 2, 0, 0));
    administrationDto.setTherapyId(therapyId);
    givenAdministrations.add(administrationDto);

    final AdjustInfusionAdministrationDto administrationDto2 = new AdjustInfusionAdministrationDto(); // 30ml/h * 3h = 90ml
    final TherapyDoseDto administrationTherapyDoseDto2 = new TherapyDoseDto();
    administrationTherapyDoseDto2.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto2.setNumerator(30.0);
    administrationTherapyDoseDto2.setNumeratorUnit("ml/h");
    administrationDto2.setAdministrationId("Administration2");
    administrationDto2.setAdministeredDose(administrationTherapyDoseDto2);
    administrationDto2.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto2.setAdministrationTime(new DateTime(2016, 1, 20, 5, 0, 0));
    administrationDto2.setTherapyId(therapyId);
    givenAdministrations.add(administrationDto2);

    final AdjustInfusionAdministrationDto administrationDto3 = new AdjustInfusionAdministrationDto(); // 40ml/h * 8h = 320ml
    final TherapyDoseDto administrationTherapyDoseDto3 = new TherapyDoseDto();
    administrationTherapyDoseDto3.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto3.setNumerator(40.0);
    administrationTherapyDoseDto3.setNumeratorUnit("ml/h");
    administrationDto3.setAdministrationId("Administration3");
    administrationDto3.setAdministeredDose(administrationTherapyDoseDto3);
    administrationDto3.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto3.setAdministrationTime(new DateTime(2016, 1, 20, 8, 0, 0));
    administrationDto3.setTherapyId(therapyId);
    givenAdministrations.add(administrationDto3);

    final BolusAdministrationDto administrationDto4 = new BolusAdministrationDto(); // 200ml
    final TherapyDoseDto administrationTherapyDoseDto4 = new TherapyDoseDto();
    administrationTherapyDoseDto4.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.QUANTITY);
    administrationTherapyDoseDto4.setNumerator(200.0);
    administrationTherapyDoseDto4.setNumeratorUnit("ml");
    administrationDto4.setAdministrationId("Administration4");
    administrationDto4.setAdministeredDose(administrationTherapyDoseDto4);
    administrationDto4.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto4.setAdministrationTime(new DateTime(2016, 1, 20, 2, 30, 0));
    administrationDto4.setTherapyId(therapyId);
    givenAdministrations.add(administrationDto4);

    final Double result = infusionBagHandler.getRemainingBagQuantityForContinuousInfusion(
        Collections.singletonList(therapyId),
        patientId,
        givenAdministrations,
        lastInfusionBag,
        when);

    assertEquals(360, result.intValue());
  }

  @Test
  public void getRemainingBagQuantityForContinuousInfusionWithBolusAndLastBagStartTaskAfterWhen()
  {
    final DateTime when = new DateTime(2016, 1, 20, 16, 0, 0);
    final String patientId = "1";
    final String therapyId = "therapy1";

    final InfusionBagDto infusionBagDto = new InfusionBagDto(1000.0, "ml");
    final Pair<DateTime, InfusionBagDto> lastInfusionBag = Pair.of(new DateTime(2016, 1, 20, 3, 0, 0), infusionBagDto);

    final List<AdministrationDto> givenAdministrations = new ArrayList<>();

    final StartAdministrationDto administrationDto = new StartAdministrationDto(); // 10ml/h * 3h = 20ml
    final TherapyDoseDto administrationTherapyDoseDto = new TherapyDoseDto();
    administrationTherapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto.setSecondaryNumerator(100.0);
    administrationTherapyDoseDto.setSecondaryNumeratorUnit("mg");
    administrationTherapyDoseDto.setSecondaryDenominator(1.0);
    administrationTherapyDoseDto.setSecondaryDenominatorUnit("l");
    administrationTherapyDoseDto.setNumerator(10.0);
    administrationTherapyDoseDto.setNumeratorUnit("ml/h");
    administrationDto.setAdministrationId("Administration1");
    administrationDto.setAdministeredDose(administrationTherapyDoseDto);
    administrationDto.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto.setAdministrationTime(new DateTime(2016, 1, 20, 2, 0, 0));
    administrationDto.setTherapyId(therapyId);
    givenAdministrations.add(administrationDto);

    final AdjustInfusionAdministrationDto administrationDto2 = new AdjustInfusionAdministrationDto(); // 30ml/h * 3h = 90ml
    final TherapyDoseDto administrationTherapyDoseDto2 = new TherapyDoseDto();
    administrationTherapyDoseDto2.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto2.setNumerator(30.0);
    administrationTherapyDoseDto2.setNumeratorUnit("ml/h");
    administrationDto2.setAdministrationId("Administration2");
    administrationDto2.setAdministeredDose(administrationTherapyDoseDto2);
    administrationDto2.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto2.setAdministrationTime(new DateTime(2016, 1, 20, 5, 0, 0));
    administrationDto2.setTherapyId(therapyId);
    givenAdministrations.add(administrationDto2);

    final AdjustInfusionAdministrationDto administrationDto3 = new AdjustInfusionAdministrationDto(); // 40ml/h * 8h = 320ml
    final TherapyDoseDto administrationTherapyDoseDto3 = new TherapyDoseDto();
    administrationTherapyDoseDto3.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto3.setNumerator(40.0);
    administrationTherapyDoseDto3.setNumeratorUnit("ml/h");
    administrationDto3.setAdministrationId("Administration3");
    administrationDto3.setAdministeredDose(administrationTherapyDoseDto3);
    administrationDto3.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto3.setAdministrationTime(new DateTime(2016, 1, 20, 8, 0, 0));
    administrationDto3.setTherapyId(therapyId);
    givenAdministrations.add(administrationDto3);

    final BolusAdministrationDto administrationDto4 = new BolusAdministrationDto(); // is before last infusion bag -> 0ml
    final TherapyDoseDto administrationTherapyDoseDto4 = new TherapyDoseDto();
    administrationTherapyDoseDto4.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.QUANTITY);
    administrationTherapyDoseDto4.setNumerator(200.0);
    administrationTherapyDoseDto4.setNumeratorUnit("ml");
    administrationDto4.setAdministrationId("Administration4");
    administrationDto4.setAdministeredDose(administrationTherapyDoseDto4);
    administrationDto4.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto4.setAdministrationTime(new DateTime(2016, 1, 20, 2, 30, 0));
    administrationDto4.setTherapyId(therapyId);
    givenAdministrations.add(administrationDto4);

    final Double result = infusionBagHandler.getRemainingBagQuantityForContinuousInfusion(
        Collections.singletonList(therapyId),
        patientId,
        givenAdministrations,
        lastInfusionBag,
        when);

    assertEquals(570, result.intValue());
  }

  @Test
  public void getRemainingBagQuantityForContinuousInfusionLastInfusionTimeAfterWhen()
  {
    final DateTime when = new DateTime(2016, 1, 20, 16, 0, 0);
    final String patientId = "1";
    final String therapyId = "therapy1";

    final InfusionBagDto infusionBagDto = new InfusionBagDto(1000.0, "ml");
    final Pair<DateTime, InfusionBagDto> lastInfusionBag = Pair.of(new DateTime(2016, 1, 21, 2, 0, 0), infusionBagDto);

    final Double result = infusionBagHandler.getRemainingBagQuantityForContinuousInfusion(
        Collections.singletonList(therapyId),
        patientId,
        Collections.emptyList(),
        lastInfusionBag,
        when);

    assertNull(result);
  }
}
