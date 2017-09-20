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

package com.marand.thinkmed.medications.administration.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.marand.maf.core.Pair;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.administration.AdministrationTaskConverter;
import com.marand.thinkmed.medications.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.dose.ComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.SimpleDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.TimedSimpleDoseElementDto;
import com.marand.thinkmed.medications.overview.OverviewContentProvider;
import com.marand.thinkmed.medications.task.AdministrationTaskCreateActionEnum;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.MedsTaskDef;
import com.marand.thinkmed.process.definition.TaskVariable;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Mitja Lapajne
 */
@RunWith(MockitoJUnitRunner.class)
public class AdministrationTaskCreatorTest
{
  @InjectMocks
  private AdministrationTaskCreatorImpl administrationTaskCreator = new AdministrationTaskCreatorImpl();

  @Spy
  private AdministrationUtilsImpl administrationUtils = new AdministrationUtilsImpl();

  @Spy
  private AdministrationTaskConverter administrationTaskConverter = new AdministrationTaskConverterImpl();

  @Mock
  private OverviewContentProvider overviewContentProvider;

  @Before
  public void beforeTest()
  {
    administrationTaskCreator.setTaskCreationDays(2);
  }
  @Test
  public void testCalculateAdministrationTasksIntervalOnPrescribe()
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setStart(new DateTime(2014, 2, 4, 12, 0));

    final List<Interval> intervals =
        administrationTaskCreator.calculateAdministrationTasksInterval(
            therapy,
            new DateTime(2014, 2, 4, 9, 0),
            AdministrationTaskCreateActionEnum.PRESCRIBE,
            7,
            null);
    assertEquals(1, intervals.size());
    assertEquals(new DateTime(2014, 2, 4, 12, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 12, 0, 0), intervals.get(0).getEnd());
  }

  @Test
  public void testCalculateAdministrationTasksIntervalOnPrescribeWithDaysOfWeek()
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setStart(new DateTime(2014, 2, 4, 12, 0)); //4.2.2014 was Tuesday

    final List<String> daysOfWeek = new ArrayList<>();
    daysOfWeek.add("TUESDAY");
    daysOfWeek.add("WEDNESDAY");
    daysOfWeek.add("FRIDAY");
    daysOfWeek.add("SUNDAY");
    therapy.setDaysOfWeek(daysOfWeek);

    final List<Interval> intervals =
        administrationTaskCreator.calculateAdministrationTasksInterval(
            therapy,
            new DateTime(2014, 2, 4, 9, 0),
            AdministrationTaskCreateActionEnum.PRESCRIBE,
            7,
            null);
    assertEquals(4, intervals.size());

    assertEquals(new DateTime(2014, 2, 4, 12, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 6, 0, 0), intervals.get(0).getEnd());

    assertEquals(new DateTime(2014, 2, 7, 0, 0), intervals.get(1).getStart());
    assertEquals(new DateTime(2014, 2, 8, 0, 0), intervals.get(1).getEnd());

    assertEquals(new DateTime(2014, 2, 9, 0, 0), intervals.get(2).getStart());
    assertEquals(new DateTime(2014, 2, 10, 0, 0), intervals.get(2).getEnd());

    assertEquals(new DateTime(2014, 2, 11, 0, 0), intervals.get(3).getStart());
    assertEquals(new DateTime(2014, 2, 12, 0, 0), intervals.get(3).getEnd());
  }

  @Test
  public void testCalculateAdministrationTasksIntervalOnPrescribeEverySecondDayWithEnd()
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setStart(new DateTime(2014, 2, 4, 12, 0));
    therapy.setEnd(new DateTime(2014, 2, 10, 13, 0));
    therapy.setDosingDaysFrequency(2);

    final List<Interval> intervals =
        administrationTaskCreator.calculateAdministrationTasksInterval(
            therapy,
            new DateTime(2014, 2, 4, 9, 0),
            AdministrationTaskCreateActionEnum.PRESCRIBE,
            7,
            null);
    assertEquals(4, intervals.size());

    assertEquals(new DateTime(2014, 2, 4, 12, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 5, 0, 0), intervals.get(0).getEnd());

    assertEquals(new DateTime(2014, 2, 6, 0, 0), intervals.get(1).getStart());
    assertEquals(new DateTime(2014, 2, 7, 0, 0), intervals.get(1).getEnd());

    assertEquals(new DateTime(2014, 2, 8, 0, 0), intervals.get(2).getStart());
    assertEquals(new DateTime(2014, 2, 9, 0, 0), intervals.get(2).getEnd());

    assertEquals(new DateTime(2014, 2, 10, 0, 0), intervals.get(3).getStart());
    assertEquals(new DateTime(2014, 2, 10, 13, 0), intervals.get(3).getEnd());
  }

  @Test
  public void testCalculateAdministrationTasksIntervalOnPrescribeWithEnd()
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setStart(new DateTime(2014, 2, 4, 12, 0));
    therapy.setEnd(new DateTime(2014, 2, 5, 13, 0));

    final List<Interval> intervals =
        administrationTaskCreator.calculateAdministrationTasksInterval(
            therapy,
            new DateTime(2014, 2, 4, 9, 0),
            AdministrationTaskCreateActionEnum.PRESCRIBE,
            7,
            null);
    assertEquals(1, intervals.size());

    assertEquals(new DateTime(2014, 2, 4, 12, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 5, 13, 0), intervals.get(0).getEnd());
  }

  @Test
  public void testCalculateAdministrationTasksIntervalOnAutoCreate()
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setStart(new DateTime(2014, 2, 4, 12, 0));

    final List<Interval> intervals =
        administrationTaskCreator.calculateAdministrationTasksInterval(
            therapy,
            new DateTime(2014, 2, 5, 1, 0),
            AdministrationTaskCreateActionEnum.AUTO_CREATE,
            7,
            new DateTime(2014, 2, 11, 22, 0));
    assertEquals(1, intervals.size());

    assertEquals(new DateTime(2014, 2, 11, 22, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 13, 0, 0), intervals.get(0).getEnd());
  }

  @Test
  public void testCalculateAdministrationTasksIntervalOnAutoCreateWithDaysOfWeek()
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setStart(new DateTime(2014, 2, 4, 12, 0)); //4.2.2014 was Tuesday

    final List<String> daysOfWeek = new ArrayList<>();
    daysOfWeek.add("TUESDAY");
    daysOfWeek.add("WEDNESDAY");
    daysOfWeek.add("FRIDAY");
    daysOfWeek.add("SUNDAY");
    therapy.setDaysOfWeek(daysOfWeek);

    final List<Interval> intervals =
        administrationTaskCreator.calculateAdministrationTasksInterval(
            therapy,
            new DateTime(2014, 2, 15, 1, 0),
            AdministrationTaskCreateActionEnum.AUTO_CREATE,
            7,
            new DateTime(2014, 2, 19, 17, 0));
    assertEquals(3, intervals.size());

    assertEquals(new DateTime(2014, 2, 19, 17, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 20, 0, 0), intervals.get(0).getEnd());

    assertEquals(new DateTime(2014, 2, 21, 0, 0), intervals.get(1).getStart());
    assertEquals(new DateTime(2014, 2, 22, 0, 0), intervals.get(1).getEnd());

    assertEquals(new DateTime(2014, 2, 23, 0, 0), intervals.get(2).getStart());
    assertEquals(new DateTime(2014, 2, 23, 0, 0), intervals.get(2).getEnd());
  }

  @Test
  public void testCalculateAdministrationTasksIntervalOnAutoCreateEverySecondDay()
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setStart(new DateTime(2014, 2, 4, 12, 0));
    therapy.setDosingDaysFrequency(2);

    final List<Interval> intervals =
        administrationTaskCreator.calculateAdministrationTasksInterval(
            therapy,
            new DateTime(2014, 2, 5, 1, 0),
            AdministrationTaskCreateActionEnum.AUTO_CREATE,
            7,
            new DateTime(2014, 2, 10, 18, 0));
    assertEquals(2, intervals.size());

    assertEquals(new DateTime(2014, 2, 10, 18, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 11, 0, 0), intervals.get(0).getEnd());

    assertEquals(new DateTime(2014, 2, 12, 0, 0), intervals.get(1).getStart());
    assertEquals(new DateTime(2014, 2, 13, 0, 0), intervals.get(1).getEnd());
  }

  @Test
  public void testCreateTasksOnPrescribeSimple3x()
  {
    final TherapyDto therapy = getSimpleConstantTherapy3X500mg(new DateTime(2014, 2, 4, 13, 0));
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2014, 2, 4, 11, 0),
        null);

    assertEquals(8, tasks.size());
    assertQuantityStartTaskRequest(tasks.get(0), "1", new DateTime(2014, 2, 4, 13, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(1), "1", new DateTime(2014, 2, 4, 20, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(2), "1", new DateTime(2014, 2, 5, 8, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(3), "1", new DateTime(2014, 2, 5, 13, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(4), "1", new DateTime(2014, 2, 5, 20, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(5), "1", new DateTime(2014, 2, 6, 8, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(6), "1", new DateTime(2014, 2, 6, 13, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(7), "1", new DateTime(2014, 2, 6, 20, 0), 500.0, "mg", null, null);
  }

  @Test
  public void testCreateTasksOnPrescribeSimple3xDSTGap()
  {
    final ConstantSimpleTherapyDto therapy = getSimpleConstantTherapy3X500mg(new DateTime(2017, 3, 25, 20, 0));

    final List<HourMinuteDto> doseTimes = new ArrayList<>();
    doseTimes.add(new HourMinuteDto(3, 0));
    doseTimes.add(new HourMinuteDto(13, 0));
    doseTimes.add(new HourMinuteDto(20, 0));
    therapy.setDoseTimes(doseTimes);

    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2017, 3, 25, 20, 0),
        null);

    assertEquals(7, tasks.size());
    assertQuantityStartTaskRequest(tasks.get(0), "1", new DateTime(2017, 3, 25, 20, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(1), "1", new DateTime(2017, 3, 26, 3, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(2), "1", new DateTime(2017, 3, 26, 13, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(3), "1", new DateTime(2017, 3, 26, 20, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(4), "1", new DateTime(2017, 3, 27, 3, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(5), "1", new DateTime(2017, 3, 27, 13, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(6), "1", new DateTime(2017, 3, 27, 20, 0), 500.0, "mg", null, null);
  }

  @Test
  public void testCreateTasksOnPresentTimeSimple3x()
  {
    final TherapyDto therapy = getSimpleConstantTherapy3X500mg(new DateTime(2014, 2, 4, 13, 0));
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PRESET_TIME_ON_NEW_PRESCRIPTION,
        new DateTime(2014, 2, 4, 11, 0),
        null);

    assertEquals(8, tasks.size());
    assertQuantityStartTaskRequest(tasks.get(0), "1", new DateTime(2014, 2, 4, 13, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(1), "1", new DateTime(2014, 2, 4, 20, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(2), "1", new DateTime(2014, 2, 5, 8, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(3), "1", new DateTime(2014, 2, 5, 13, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(4), "1", new DateTime(2014, 2, 5, 20, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(5), "1", new DateTime(2014, 2, 6, 8, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(6), "1", new DateTime(2014, 2, 6, 13, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(7), "1", new DateTime(2014, 2, 6, 20, 0), 500.0, "mg", null, null);
  }

  @Test
  public void testCreateTasksOnReissueSimple3x()
  {
    final TherapyDto therapy = getSimpleConstantTherapy3X500mg(new DateTime(2014, 2, 4, 13, 0));
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.REISSUE,
        new DateTime(2014, 2, 5, 15, 0),
        null);

    assertEquals(7, tasks.size());
    assertQuantityStartTaskRequest(tasks.get(0), "1", new DateTime(2014, 2, 5, 20, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(1), "1", new DateTime(2014, 2, 6, 8, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(2), "1", new DateTime(2014, 2, 6, 13, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(3), "1", new DateTime(2014, 2, 6, 20, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(4), "1", new DateTime(2014, 2, 7, 8, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(5), "1", new DateTime(2014, 2, 7, 13, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(6), "1", new DateTime(2014, 2, 7, 20, 0), 500.0, "mg", null, null);
  }

  @Test
  public void testCreateTasksOnAutoCreateSimple3x()
  {
    final TherapyDto therapy = getSimpleConstantTherapy3X500mg(new DateTime(2014, 2, 4, 13, 0));
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.AUTO_CREATE,
        new DateTime(2014, 2, 5, 15, 0),
        new DateTime(2014, 2, 6, 13, 0));

    assertEquals(4, tasks.size());
    assertQuantityStartTaskRequest(tasks.get(0), "1", new DateTime(2014, 2, 6, 20, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(1), "1", new DateTime(2014, 2, 7, 8, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(2), "1", new DateTime(2014, 2, 7, 13, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(3), "1", new DateTime(2014, 2, 7, 20, 0), 500.0, "mg", null, null);
  }

  @Test
  public void testCreateTasksOnPrescribeSimple3XWhenNeeded()
  {
    final TherapyDto therapy = getSimpleConstantTherapy3X500mg(new DateTime(2014, 2, 4, 13, 0));
    therapy.setWhenNeeded(true);
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2014, 2, 4, 11, 0),
        null);

    assertEquals(0, tasks.size());
  }

  @Test
  public void testCreateTasksOnPresetTimeSimple3xWhenNeeded()
  {
    final TherapyDto therapy = getSimpleConstantTherapy3X500mg(new DateTime(2014, 2, 4, 13, 0));
    therapy.setWhenNeeded(true);
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PRESET_TIME_ON_NEW_PRESCRIPTION,
        new DateTime(2014, 2, 4, 11, 0),
        null);

    assertEquals(8, tasks.size());
    assertQuantityStartTaskRequest(tasks.get(0), "1", new DateTime(2014, 2, 4, 13, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(1), "1", new DateTime(2014, 2, 4, 20, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(2), "1", new DateTime(2014, 2, 5, 8, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(3), "1", new DateTime(2014, 2, 5, 13, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(4), "1", new DateTime(2014, 2, 5, 20, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(5), "1", new DateTime(2014, 2, 6, 8, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(6), "1", new DateTime(2014, 2, 6, 13, 0), 500.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(7), "1", new DateTime(2014, 2, 6, 20, 0), 500.0, "mg", null, null);
  }

  @Test
  public void testCreateTasksOnPreviewSimple3xNoDose()
  {
    final ConstantSimpleTherapyDto therapy = getSimpleConstantTherapy3X500mg(new DateTime(2014, 2, 4, 13, 0));
    therapy.setDoseElement(null);

    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PREVIEW_TIMES_ON_NEW_PRESCRIPTION,
        new DateTime(2014, 2, 4, 11, 0),
        null);

    assertEquals(7, tasks.size());
    assertQuantityStartTaskRequest(tasks.get(0), "1", new DateTime(2014, 2, 4, 13, 0), null, null, null, null);
    assertQuantityStartTaskRequest(tasks.get(1), "1", new DateTime(2014, 2, 4, 20, 0), null, null, null, null);
    assertQuantityStartTaskRequest(tasks.get(2), "1", new DateTime(2014, 2, 5, 8, 0), null, null, null, null);
    assertQuantityStartTaskRequest(tasks.get(3), "1", new DateTime(2014, 2, 5, 13, 0), null, null, null, null);
    assertQuantityStartTaskRequest(tasks.get(4), "1", new DateTime(2014, 2, 5, 20, 0), null, null, null, null);
    assertQuantityStartTaskRequest(tasks.get(5), "1", new DateTime(2014, 2, 6, 8, 0), null, null, null, null);
    assertQuantityStartTaskRequest(tasks.get(6), "1", new DateTime(2014, 2, 6, 13, 0), null, null, null, null);
  }

  @Test
  public void testCreateTasksOnPrescribeSimple8h()
  {
    final TherapyDto therapy = getSimpleConstantTherapy8h100mg3ml(new DateTime(2014, 2, 4, 16, 0));
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2014, 2, 4, 11, 0),
        null);

    assertEquals(8, tasks.size());
    assertQuantityStartTaskRequest(tasks.get(0), "1", new DateTime(2014, 2, 4, 16, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(1), "1", new DateTime(2014, 2, 5, 0, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(2), "1", new DateTime(2014, 2, 5, 8, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(3), "1", new DateTime(2014, 2, 5, 16, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(4), "1", new DateTime(2014, 2, 6, 0, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(5), "1", new DateTime(2014, 2, 6, 8, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(6), "1", new DateTime(2014, 2, 6, 16, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(7), "1", new DateTime(2014, 2, 7, 0, 0), 100.0, "mg", 3.0, "ml");
  }

  @Test
  public void testCreateTasksOnPresentTimeSimple8h()
  {
    final TherapyDto therapy = getSimpleConstantTherapy8h100mg3ml(new DateTime(2014, 2, 4, 13, 0));
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PRESET_TIME_ON_NEW_PRESCRIPTION,
        new DateTime(2014, 2, 4, 11, 0),
        null);

    assertEquals(8, tasks.size());
    assertQuantityStartTaskRequest(tasks.get(0), "1", new DateTime(2014, 2, 4, 16, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(1), "1", new DateTime(2014, 2, 5, 0, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(2), "1", new DateTime(2014, 2, 5, 8, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(3), "1", new DateTime(2014, 2, 5, 16, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(4), "1", new DateTime(2014, 2, 6, 0, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(5), "1", new DateTime(2014, 2, 6, 8, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(6), "1", new DateTime(2014, 2, 6, 16, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(7), "1", new DateTime(2014, 2, 7, 0, 0), 100.0, "mg", 3.0, "ml");
  }

  @Test
  public void testCreateTasksOnReissueSimple8h()
  {
    final TherapyDto therapy = getSimpleConstantTherapy8h100mg3ml(new DateTime(2014, 2, 4, 8, 0));
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.REISSUE,
        new DateTime(2014, 2, 5, 15, 0),
        null);

    assertEquals(8, tasks.size());
    assertQuantityStartTaskRequest(tasks.get(0), "1", new DateTime(2014, 2, 5, 16, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(1), "1", new DateTime(2014, 2, 6, 0, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(2), "1", new DateTime(2014, 2, 6, 8, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(3), "1", new DateTime(2014, 2, 6, 16, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(4), "1", new DateTime(2014, 2, 7, 0, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(5), "1", new DateTime(2014, 2, 7, 8, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(6), "1", new DateTime(2014, 2, 7, 16, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(7), "1", new DateTime(2014, 2, 8, 0, 0), 100.0, "mg", 3.0, "ml");
  }

  @Test
  public void testCreateTasksOnAutoCreateSimple8h()
  {
    final TherapyDto therapy = getSimpleConstantTherapy8h100mg3ml(new DateTime(2014, 2, 4, 8, 0));
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.AUTO_CREATE,
        new DateTime(2014, 2, 5, 15, 0),
        new DateTime(2014, 2, 6, 13, 0));

    assertEquals(5, tasks.size());
    assertQuantityStartTaskRequest(tasks.get(0), "1", new DateTime(2014, 2, 6, 16, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(1), "1", new DateTime(2014, 2, 7, 0, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(2), "1", new DateTime(2014, 2, 7, 8, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(3), "1", new DateTime(2014, 2, 7, 16, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(4), "1", new DateTime(2014, 2, 8, 0, 0), 100.0, "mg", 3.0, "ml");
  }

  @Test
  public void testCreateTasksOnPrescribeSimple36h() //simple constant therapy, every 36 hours
  {
    final TherapyDto therapy = getSimpleConstantTherapy36h100mg3ml(new DateTime(2014, 2, 4, 8, 0));

    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2014, 2, 4, 7, 0),
        null);

    assertEquals(2, tasks.size());
    assertQuantityStartTaskRequest(tasks.get(0), "1", new DateTime(2014, 2, 4, 8, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(1), "1", new DateTime(2014, 2, 5, 20, 0), 100.0, "mg", 3.0, "ml");
  }

  @Test
  public void testCreateTasksOnAutoCreateSimple36h() //simple constant therapy, every 36 hours
  {
    final TherapyDto therapy = getSimpleConstantTherapy36h100mg3ml(new DateTime(2014, 2, 4, 8, 0));

    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.AUTO_CREATE,
        new DateTime(2014, 2, 6, 10, 0),
        new DateTime(2014, 2, 5, 20, 0));

    assertEquals(2, tasks.size());
    assertQuantityStartTaskRequest(tasks.get(0), "1", new DateTime(2014, 2, 7, 8, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(1), "1", new DateTime(2014, 2, 8, 20, 0), 100.0, "mg", 3.0, "ml");
  }

  @Test
  public void testCreateTasksOnPrescribeSimple1ex()
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setCompositionUid("uid1");
    therapy.setEhrOrderName("1");
    therapy.setStart(new DateTime(2014, 2, 4, 12, 0));
    therapy.setEnd(new DateTime(2014, 2, 4, 12, 0));
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.ONCE_THEN_EX, null));
    therapy.setQuantityUnit("mg");
    therapy.setQuantityDenominatorUnit("ml");
    therapy.setMedication(getMedication(123L, "Paracetamol"));

    final List<HourMinuteDto> doseTimes = new ArrayList<>();
    doseTimes.add(new HourMinuteDto(12, 0));
    therapy.setDoseTimes(doseTimes);

    therapy.setDoseElement(getSimpleDoseElement(100.0, 3.0));

    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2014, 2, 4, 11, 0),
        null);

    assertEquals(1, tasks.size());
    assertQuantityStartTaskRequest(tasks.get(0), "1", new DateTime(2014, 2, 4, 12, 0), 100.0, "mg", 3.0, "ml");
  }

  @Test
  public void testCreateTasksOnPrescribeSimple2xEverySecondDay()
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setCompositionUid("uid1");
    therapy.setEhrOrderName("1");
    therapy.setStart(new DateTime(2014, 2, 4, 8, 0));
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 2));
    therapy.setDosingDaysFrequency(2);
    therapy.setQuantityUnit("mg");
    therapy.setQuantityDenominatorUnit("ml");
    therapy.setMedication(getMedication(123L, "Paracetamol"));

    final List<HourMinuteDto> doseTimes = new ArrayList<>();
    doseTimes.add(new HourMinuteDto(9, 0));
    doseTimes.add(new HourMinuteDto(21, 0));
    therapy.setDoseTimes(doseTimes);

    therapy.setDoseElement(getSimpleDoseElement(100.0, 3.0));

    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2014, 2, 4, 8, 0),
        null);

    assertEquals(4, tasks.size());
    assertQuantityStartTaskRequest(tasks.get(0), "1", new DateTime(2014, 2, 4, 9, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(1), "1", new DateTime(2014, 2, 4, 21, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(2), "1", new DateTime(2014, 2, 6, 9, 0), 100.0, "mg", 3.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(3), "1", new DateTime(2014, 2, 6, 21, 0), 100.0, "mg", 3.0, "ml");
  }

  @Test
  public void testCreateTasksOnPrescribeSimpleVariable()
  {
    final VariableSimpleTherapyDto therapy = new VariableSimpleTherapyDto();
    therapy.setCompositionUid("uid1");
    therapy.setEhrOrderName("1");
    therapy.setStart(new DateTime(2014, 2, 4, 9, 0));
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 3));
    therapy.setQuantityUnit("mg");
    therapy.setQuantityDenominatorUnit("ml");
    therapy.setMedication(getMedication(123L, "Paracetamol"));

    therapy.getTimedDoseElements().add(getTimedSimpleDoseElement(100.0, new HourMinuteDto(9, 0)));
    therapy.getTimedDoseElements().add(getTimedSimpleDoseElement(200.0, new HourMinuteDto(13, 0)));
    therapy.getTimedDoseElements().add(getTimedSimpleDoseElement(300.0, new HourMinuteDto(21, 0)));

    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2014, 2, 4, 9, 0),
        null);

    assertEquals(9, tasks.size());
    assertQuantityStartTaskRequest(tasks.get(0), "1", new DateTime(2014, 2, 4, 9, 0), 100.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(1), "1", new DateTime(2014, 2, 4, 13, 0), 200.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(2), "1", new DateTime(2014, 2, 4, 21, 0), 300.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(3), "1", new DateTime(2014, 2, 5, 9, 0), 100.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(4), "1", new DateTime(2014, 2, 5, 13, 0), 200.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(5), "1", new DateTime(2014, 2, 5, 21, 0), 300.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(6), "1", new DateTime(2014, 2, 6, 9, 0), 100.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(7), "1", new DateTime(2014, 2, 6, 13, 0), 200.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(8), "1", new DateTime(2014, 2, 6, 21, 0), 300.0, "mg", null, null);
  }

  @Test
  public void testCreateTasksOnPrescribeComplex3x()
  {
    final TherapyDto therapy = getComplexConstantTherapy3x(new DateTime(2014, 2, 4, 13, 0));
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2014, 2, 4, 11, 0),
        null);

    assertEquals(8, tasks.size());
    assertQuantityStartTaskRequest(tasks.get(0), "1", new DateTime(2014, 2, 4, 13, 0), 50.0, "mg", 200.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(1), "1", new DateTime(2014, 2, 4, 20, 0), 50.0, "mg", 200.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(2), "1", new DateTime(2014, 2, 5, 8, 0), 50.0, "mg", 200.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(3), "1", new DateTime(2014, 2, 5, 13, 0), 50.0, "mg", 200.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(4), "1", new DateTime(2014, 2, 5, 20, 0), 50.0, "mg", 200.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(5), "1", new DateTime(2014, 2, 6, 8, 0), 50.0, "mg", 200.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(6), "1", new DateTime(2014, 2, 6, 13, 0), 50.0, "mg", 200.0, "ml");
    assertQuantityStartTaskRequest(tasks.get(7), "1", new DateTime(2014, 2, 6, 20, 0), 50.0, "mg", 200.0, "ml");
  }

  @Test
  public void testCreateTasksOnPrescribeVariableComplex()
  {
    final TherapyDto therapy = getComplexVariableTherapyWithRate(new DateTime(2014, 2, 4, 9, 0));
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2014, 2, 4, 11, 0),
        null);

    assertEquals(12, tasks.size());

    assertStartTaskRequest(tasks.get(0), "1", new DateTime(2014, 2, 4, 9, 0), 20.0, "ml/h", null, null, TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(1),
        "1",
        new DateTime(2014, 2, 4, 13, 0),
        AdministrationTypeEnum.ADJUST_INFUSION,
        30.0,
        "ml/h",
        null,
        null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(2),
        "1",
        new DateTime(2014, 2, 4, 21, 0),
        AdministrationTypeEnum.ADJUST_INFUSION,
        40.0,
        "ml/h",
        null,
        null,
        TherapyDoseTypeEnum.RATE);

    assertStartTaskRequest(tasks.get(4), "1", new DateTime(2014, 2, 5, 9, 0), 20.0, "ml/h", null, null, TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(5),
        "1",
        new DateTime(2014, 2, 5, 13, 0),
        AdministrationTypeEnum.ADJUST_INFUSION,
        30.0,
        "ml/h",
        null,
        null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(6),
        "1",
        new DateTime(2014, 2, 5, 21, 0),
        AdministrationTypeEnum.ADJUST_INFUSION,
        40.0,
        "ml/h",
        null,
        null,
        TherapyDoseTypeEnum.RATE);

    assertStartTaskRequest(tasks.get(8), "1", new DateTime(2014, 2, 6, 9, 0), 20.0, "ml/h", null, null, TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(9),
        "1",
        new DateTime(2014, 2, 6, 13, 0),
        AdministrationTypeEnum.ADJUST_INFUSION,
        30.0,
        "ml/h",
        null,
        null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(10),
        "1",
        new DateTime(2014, 2, 6, 21, 0),
        AdministrationTypeEnum.ADJUST_INFUSION,
        40.0,
        "ml/h",
        null,
        null,
        TherapyDoseTypeEnum.RATE);
  }

  @Test
  public void testCreateTasksOnPrescribeContinuousInfusion()
  {
    final TherapyDto therapy = getContinuousInfusion(new DateTime(2014, 2, 4, 13, 0), new DateTime(2014, 2, 5, 18, 0));
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2014, 2, 4, 11, 0),
        null);

    assertEquals(2, tasks.size());
    assertTaskRequest(
        tasks.get(0), "1", new DateTime(2014, 2, 4, 13, 0), AdministrationTypeEnum.START, 20.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(1), "1", new DateTime(2014, 2, 5, 18, 0), AdministrationTypeEnum.STOP, null, null, null, null, null);
  }

  @Test
  public void testCreateTasksOnAutoCreateContinuousInfusionAfterAWeek()
  {
    final TherapyDto therapy = getContinuousInfusion(new DateTime(2014, 2, 4, 13, 0), new DateTime(2014, 2, 5, 18, 0));
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.AUTO_CREATE,
        new DateTime(2014, 2, 4, 11, 0),
        null);

    assertEquals(2, tasks.size());
    assertTaskRequest(
        tasks.get(0), "1", new DateTime(2014, 2, 4, 13, 0), AdministrationTypeEnum.START, 20.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(1), "1", new DateTime(2014, 2, 5, 18, 0), AdministrationTypeEnum.STOP, null, null, null, null, null);
  }

  @Test
  public void testCreateTasksOnPrescribeContinuousInfusionAfterAWeek()
  {
    final TherapyDto therapy = getContinuousInfusion(new DateTime(2014, 2, 14, 13, 0), new DateTime(2014, 2, 5, 18, 0));
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.AUTO_CREATE,
        new DateTime(2014, 2, 4, 11, 0),
        null);

    assertEquals(0, tasks.size());
  }

  @Test
  public void testCreateTasksOnModifyContinuousInfusion()
  {
    final TherapyDto therapy = getContinuousInfusion(new DateTime(2014, 2, 4, 13, 0), new DateTime(2014, 2, 5, 18, 0));
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.MODIFY,
        new DateTime(2014, 2, 4, 11, 0),
        null);

    assertEquals(2, tasks.size());
    assertTaskRequest(
        tasks.get(0), "1", new DateTime(2014, 2, 4, 13, 0), AdministrationTypeEnum.ADJUST_INFUSION, 20.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(1), "1", new DateTime(2014, 2, 5, 18, 0), AdministrationTypeEnum.STOP, null, null, null, null, null);
  }

  @Test
  public void testCreateTasksOnModifyBeforeStartContinuousInfusion()
  {
    final TherapyDto therapy = getContinuousInfusion(new DateTime(2014, 2, 4, 13, 0), new DateTime(2014, 2, 5, 18, 0));
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.MODIFY_BEFORE_START,
        new DateTime(2014, 2, 4, 11, 0),
        null);

    assertEquals(2, tasks.size());
    assertTaskRequest(
        tasks.get(0), "1", new DateTime(2014, 2, 4, 13, 0), AdministrationTypeEnum.START, 20.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(1), "1", new DateTime(2014, 2, 5, 18, 0), AdministrationTypeEnum.STOP, null, null, null, null, null);
  }

  @Test
  public void testCreateTasksOnPrescribeVariableContinuousInfusion()
  {
    final TherapyDto therapy =
        getVariableContinuousInfusion(new DateTime(2014, 2, 4, 9, 0), new DateTime(2014, 2, 5, 18, 0));
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2014, 2, 4, 11, 0),
        null);

    assertEquals(4, tasks.size());
    assertTaskRequest(
        tasks.get(0), "1", new DateTime(2014, 2, 4, 9, 0), AdministrationTypeEnum.START, 20.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(1), "1", new DateTime(2014, 2, 4, 13, 0), AdministrationTypeEnum.ADJUST_INFUSION, 30.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(2), "1", new DateTime(2014, 2, 4, 21, 0), AdministrationTypeEnum.ADJUST_INFUSION, 40.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(3), "1", new DateTime(2014, 2, 5, 18, 0), AdministrationTypeEnum.STOP, null, null, null, null, null);
  }

  @Test
  public void testCreateTasksOnPrescribeVariableContinuousInfusionOverMidnight()
  {
    final TherapyDto therapy =
        getVariableContinuousInfusionOverMidnight(new DateTime(2014, 2, 4, 22, 0), null);
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2014, 2, 4, 11, 0),
        null);

    assertEquals(3, tasks.size());
    assertTaskRequest(
        tasks.get(0), "1", new DateTime(2014, 2, 4, 22, 0), AdministrationTypeEnum.START, 20.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(1), "1", new DateTime(2014, 2, 4, 23, 0), AdministrationTypeEnum.ADJUST_INFUSION, 30.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(2), "1", new DateTime(2014, 2, 5, 3, 0), AdministrationTypeEnum.ADJUST_INFUSION, 40.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
  }

  @Test
  public void testCreateTasksOnAutoCreateVariableContinuousInfusionBeforeStart()
  {
    administrationTaskCreator.setTaskCreationDays(7);
    final TherapyDto therapy =
        getVariableContinuousInfusion(new DateTime(2014, 2, 4, 9, 0), new DateTime(2014, 2, 5, 18, 0));
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.AUTO_CREATE,
        new DateTime(2014, 1, 30, 11, 0),
        null);

    assertEquals(4, tasks.size());
    assertTaskRequest(
        tasks.get(0), "1", new DateTime(2014, 2, 4, 9, 0), AdministrationTypeEnum.START, 20.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(1), "1", new DateTime(2014, 2, 4, 13, 0), AdministrationTypeEnum.ADJUST_INFUSION, 30.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(2), "1", new DateTime(2014, 2, 4, 21, 0), AdministrationTypeEnum.ADJUST_INFUSION, 40.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(3), "1", new DateTime(2014, 2, 5, 18, 0), AdministrationTypeEnum.STOP, null, null, null, null, null);

    administrationTaskCreator.setTaskCreationDays(2);
  }

  @Test
  public void testCreateTasksOnAutoCreateVariableContinuousInfusionAfterStart()
  {
    administrationTaskCreator.setTaskCreationDays(7);
    final TherapyDto therapy =
        getVariableContinuousInfusion(new DateTime(2014, 2, 4, 9, 0), new DateTime(2014, 2, 5, 18, 0));
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.AUTO_CREATE,
        new DateTime(2014, 2, 4, 11, 0),
        new DateTime(2014, 2, 4, 9, 0));

    assertEquals(3, tasks.size());
    assertTaskRequest(
        tasks.get(0), "1", new DateTime(2014, 2, 4, 13, 0), AdministrationTypeEnum.ADJUST_INFUSION, 30.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(1), "1", new DateTime(2014, 2, 4, 21, 0), AdministrationTypeEnum.ADJUST_INFUSION, 40.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(2), "1", new DateTime(2014, 2, 5, 18, 0), AdministrationTypeEnum.STOP, null, null, null, null, null);

    administrationTaskCreator.setTaskCreationDays(2);
  }

  @Test
  public void testCreateTasksOnAutoCreateVariableContinuousInfusionAllTasksCreated()
  {
    final TherapyDto therapy =
        getVariableContinuousInfusion(new DateTime(2014, 2, 4, 9, 0), null);
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.AUTO_CREATE,
        new DateTime(2014, 2, 4, 11, 0),
        new DateTime(2014, 2, 4, 21, 0));

    assertEquals(0, tasks.size());
  }

  @Test
  public void testCreateTasksOnPrescribeRecurringVariableContinuousInfusion() //therapy start at the start of interval
  {
    final VariableComplexTherapyDto therapy =
        getVariableContinuousInfusion(new DateTime(2014, 2, 4, 9, 0), new DateTime(2014, 2, 5, 18, 0));
    therapy.setRecurringContinuousInfusion(true);
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2014, 2, 4, 8, 0),
        null);

    assertEquals(6, tasks.size());
    assertTaskRequest(
        tasks.get(0), "1", new DateTime(2014, 2, 4, 9, 0), AdministrationTypeEnum.START, 20.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(1), "1", new DateTime(2014, 2, 4, 13, 0), AdministrationTypeEnum.ADJUST_INFUSION, 30.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(2), "1", new DateTime(2014, 2, 4, 21, 0), AdministrationTypeEnum.ADJUST_INFUSION, 40.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(3), "1", new DateTime(2014, 2, 5, 9, 0), AdministrationTypeEnum.ADJUST_INFUSION, 20.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(4), "1", new DateTime(2014, 2, 5, 13, 0), AdministrationTypeEnum.ADJUST_INFUSION, 30.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(5), "1", new DateTime(2014, 2, 5, 18, 0), AdministrationTypeEnum.STOP, null, null, null, null, null);
  }

  @Test
  public void testCreateTasksOnPrescribeRecurringVariableContinuousInfusion2() //therapy start in the middle of interval
  {
    final VariableComplexTherapyDto therapy =
        getVariableContinuousInfusion(new DateTime(2014, 2, 4, 14, 0), new DateTime(2014, 2, 5, 18, 0));
    therapy.setRecurringContinuousInfusion(true);
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2014, 2, 4, 11, 0),
        null);

    assertEquals(5, tasks.size());
    assertTaskRequest(
        tasks.get(0), "1", new DateTime(2014, 2, 4, 14, 0), AdministrationTypeEnum.START, 30.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(1), "1", new DateTime(2014, 2, 4, 21, 0), AdministrationTypeEnum.ADJUST_INFUSION, 40.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(2), "1", new DateTime(2014, 2, 5, 9, 0), AdministrationTypeEnum.ADJUST_INFUSION, 20.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(3), "1", new DateTime(2014, 2, 5, 13, 0), AdministrationTypeEnum.ADJUST_INFUSION, 30.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(4), "1", new DateTime(2014, 2, 5, 18, 0), AdministrationTypeEnum.STOP, null, null, null, null, null);
  }

  @Test
  public void testCreateTasksOnAutoCreateRecurringVariableContinuousInfusion()
  {
    final VariableComplexTherapyDto therapy =
        getVariableContinuousInfusion(new DateTime(2014, 2, 4, 9, 0), new DateTime(2014, 2, 5, 18, 0));
    therapy.setRecurringContinuousInfusion(true);
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.AUTO_CREATE,
        new DateTime(2014, 2, 4, 22, 0),
        new DateTime(2014, 2, 5, 9, 0));

    assertEquals(2, tasks.size());
    assertTaskRequest(
        tasks.get(0), "1", new DateTime(2014, 2, 5, 13, 0), AdministrationTypeEnum.ADJUST_INFUSION, 30.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(1), "1", new DateTime(2014, 2, 5, 18, 0), AdministrationTypeEnum.STOP, null, null, null, null, null);
  }

  @Test
  public void testCreateTasksOnReissueRecurringVariableContinuousInfusion()
  {
    final VariableComplexTherapyDto therapy =
        getVariableContinuousInfusion(new DateTime(2014, 2, 4, 9, 0), new DateTime(2014, 2, 5, 18, 0));
    therapy.setRecurringContinuousInfusion(true);
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.REISSUE,
        new DateTime(2014, 2, 4, 22, 0),
        null);

    assertEquals(4, tasks.size());
    assertTaskRequest(
        tasks.get(0), "1", new DateTime(2014, 2, 4, 22, 0), AdministrationTypeEnum.START, 40.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(1), "1", new DateTime(2014, 2, 5, 9, 0), AdministrationTypeEnum.ADJUST_INFUSION, 20.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(2), "1", new DateTime(2014, 2, 5, 13, 0), AdministrationTypeEnum.ADJUST_INFUSION, 30.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(3), "1", new DateTime(2014, 2, 5, 18, 0), AdministrationTypeEnum.STOP, null, null, null, null, null);
  }

  @Test
  public void testCreateTasksOnModifyRecurringVariableContinuousInfusion()
  {
    final VariableComplexTherapyDto therapy =
        getVariableContinuousInfusion(new DateTime(2014, 2, 4, 22, 0), new DateTime(2014, 2, 5, 18, 0));
    therapy.setRecurringContinuousInfusion(true);
    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.MODIFY,
        new DateTime(2014, 2, 4, 19, 0),
        null);

    assertEquals(4, tasks.size());
    assertTaskRequest(
        tasks.get(0), "1", new DateTime(2014, 2, 4, 22, 0), AdministrationTypeEnum.ADJUST_INFUSION, 40.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(1), "1", new DateTime(2014, 2, 5, 9, 0), AdministrationTypeEnum.ADJUST_INFUSION, 20.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(2), "1", new DateTime(2014, 2, 5, 13, 0), AdministrationTypeEnum.ADJUST_INFUSION, 30.0, "ml/h", null, null,
        TherapyDoseTypeEnum.RATE);
    assertTaskRequest(
        tasks.get(3), "1", new DateTime(2014, 2, 5, 18, 0), AdministrationTypeEnum.STOP, null, null, null, null, null);
  }

  @Test
  public void testCreateTasksOnPrescribeSimpleVariableDays()
  {
    final VariableSimpleTherapyDto therapy = new VariableSimpleTherapyDto();
    therapy.setCompositionUid("uid1");
    therapy.setEhrOrderName("1");
    therapy.setStart(new DateTime(2014, 2, 4, 9, 0));
    therapy.setEnd(new DateTime(2014, 2, 5, 20, 0));
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 2));
    therapy.setQuantityUnit("mg");
    therapy.setQuantityDenominatorUnit("ml");
    therapy.setMedication(getMedication(123L, "Paracetamol"));

    therapy.getTimedDoseElements().add(
        getTimedSimpleDoseElement(100.0, new HourMinuteDto(9, 0), new DateTime(2014, 2, 4, 0, 0)));
    therapy.getTimedDoseElements().add(
        getTimedSimpleDoseElement(200.0, new HourMinuteDto(20, 0), new DateTime(2014, 2, 4, 0, 0)));
    therapy.getTimedDoseElements().add(
        getTimedSimpleDoseElement(300.0, new HourMinuteDto(9, 0), new DateTime(2014, 2, 5, 0, 0)));
    therapy.getTimedDoseElements().add(
        getTimedSimpleDoseElement(400.0, new HourMinuteDto(20, 0), new DateTime(2014, 2, 5, 0, 0)));

    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2014, 2, 4, 9, 0),
        null);

    assertEquals(4, tasks.size());
    assertQuantityStartTaskRequest(tasks.get(0), "1", new DateTime(2014, 2, 4, 9, 0), 100.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(1), "1", new DateTime(2014, 2, 4, 20, 0), 200.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(2), "1", new DateTime(2014, 2, 5, 9, 0), 300.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(3), "1", new DateTime(2014, 2, 5, 20, 0), 400.0, "mg", null, null);
  }

  @Test
  public void testCreateTasksOnPrescribeSimpleVariableDaysRepeatLastDay()
  {
    final VariableSimpleTherapyDto therapy = new VariableSimpleTherapyDto();
    therapy.setCompositionUid("uid1");
    therapy.setEhrOrderName("1");
    therapy.setStart(new DateTime(2014, 2, 4, 9, 0));
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 2));
    therapy.setQuantityUnit("mg");
    therapy.setQuantityDenominatorUnit("ml");
    therapy.setMedication(getMedication(123L, "Paracetamol"));

    therapy.getTimedDoseElements().add(
        getTimedSimpleDoseElement(
            100.0, new HourMinuteDto(9, 0), new DateTime(2014, 2, 4, 0, 0)));
    therapy.getTimedDoseElements().add(
        getTimedSimpleDoseElement(
            200.0, new HourMinuteDto(20, 0), new DateTime(2014, 2, 4, 0, 0)));
    therapy.getTimedDoseElements().add(
        getTimedSimpleDoseElement(
            300.0, new HourMinuteDto(9, 0), new DateTime(2014, 2, 5, 0, 0)));
    therapy.getTimedDoseElements().add(
        getTimedSimpleDoseElement(
            400.0, new HourMinuteDto(20, 0), new DateTime(2014, 2, 5, 0, 0)));

    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2014, 2, 4, 9, 0),
        null);

    assertEquals(6, tasks.size());
    assertQuantityStartTaskRequest(tasks.get(0), "1", new DateTime(2014, 2, 4, 9, 0), 100.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(1), "1", new DateTime(2014, 2, 4, 20, 0), 200.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(2), "1", new DateTime(2014, 2, 5, 9, 0), 300.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(3), "1", new DateTime(2014, 2, 5, 20, 0), 400.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(4), "1", new DateTime(2014, 2, 6, 9, 0), 300.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(5), "1", new DateTime(2014, 2, 6, 20, 0), 400.0, "mg", null, null);
  }

  @Test
  public void testCreateTasksOnPrescribeSimpleVariableDaysRepeatLastDayWithEnd()
  {
    final VariableSimpleTherapyDto therapy = new VariableSimpleTherapyDto();
    therapy.setCompositionUid("uid1");
    therapy.setEhrOrderName("1");
    therapy.setStart(new DateTime(2014, 2, 4, 9, 0));
    therapy.setEnd(new DateTime(2014, 2, 6, 9, 0));
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 2));
    therapy.setQuantityUnit("mg");
    therapy.setQuantityDenominatorUnit("ml");
    therapy.setMedication(getMedication(123L, "Paracetamol"));

    therapy.getTimedDoseElements().add(
        getTimedSimpleDoseElement(
            100.0, new HourMinuteDto(9, 0), new DateTime(2014, 2, 4, 0, 0)));
    therapy.getTimedDoseElements().add(
        getTimedSimpleDoseElement(
            200.0, new HourMinuteDto(20, 0), new DateTime(2014, 2, 4, 0, 0)));
    therapy.getTimedDoseElements().add(
        getTimedSimpleDoseElement(
            300.0, new HourMinuteDto(9, 0), new DateTime(2014, 2, 5, 0, 0)));
    therapy.getTimedDoseElements().add(
        getTimedSimpleDoseElement(
            400.0, new HourMinuteDto(20, 0), new DateTime(2014, 2, 5, 0, 0)));

    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2014, 2, 4, 9, 0),
        null);

    assertEquals(5, tasks.size());
    assertQuantityStartTaskRequest(tasks.get(0), "1", new DateTime(2014, 2, 4, 9, 0), 100.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(1), "1", new DateTime(2014, 2, 4, 20, 0), 200.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(2), "1", new DateTime(2014, 2, 5, 9, 0), 300.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(3), "1", new DateTime(2014, 2, 5, 20, 0), 400.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(4), "1", new DateTime(2014, 2, 6, 9, 0), 300.0, "mg", null, null);
  }

  @Test
  public void testCreateTasksOnPrescribeSimpleVariableDaysNullDose()
  {
    final VariableSimpleTherapyDto therapy = new VariableSimpleTherapyDto();
    therapy.setCompositionUid("uid1");
    therapy.setEhrOrderName("1");
    therapy.setStart(new DateTime(2014, 2, 4, 9, 0));
    therapy.setEnd(new DateTime(2014, 2, 5, 20, 0));
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 2));
    therapy.setQuantityUnit("mg");
    therapy.setQuantityDenominatorUnit("ml");
    therapy.setMedication(getMedication(123L, "Paracetamol"));

    therapy.getTimedDoseElements().add(
        getTimedSimpleDoseElement(100.0, new HourMinuteDto(9, 0), new DateTime(2014, 2, 4, 0, 0)));
    therapy.getTimedDoseElements().add(
        getTimedSimpleDoseElement(null, new HourMinuteDto(20, 0), new DateTime(2014, 2, 4, 0, 0)));
    therapy.getTimedDoseElements().add(
        getTimedSimpleDoseElement(300.0, new HourMinuteDto(9, 0), new DateTime(2014, 2, 5, 0, 0)));
    therapy.getTimedDoseElements().add(
        getTimedSimpleDoseElement(400.0, new HourMinuteDto(20, 0), new DateTime(2014, 2, 5, 0, 0)));

    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2014, 2, 4, 9, 0),
        null);

    assertEquals(3, tasks.size());
    assertQuantityStartTaskRequest(tasks.get(0), "1", new DateTime(2014, 2, 4, 9, 0), 100.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(1), "1", new DateTime(2014, 2, 5, 9, 0), 300.0, "mg", null, null);
    assertQuantityStartTaskRequest(tasks.get(2), "1", new DateTime(2014, 2, 5, 20, 0), 400.0, "mg", null, null);
  }

  @Test
  public void testCreateTasksOnAutoCreateSimpleVariableDays()
  {
    final VariableSimpleTherapyDto therapy = new VariableSimpleTherapyDto();
    therapy.setCompositionUid("uid1");
    therapy.setEhrOrderName("1");
    therapy.setStart(new DateTime(2014, 2, 4, 9, 0));
    therapy.setEnd(new DateTime(2014, 2, 5, 20, 0));
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 2));
    therapy.setQuantityUnit("mg");
    therapy.setQuantityDenominatorUnit("ml");
    therapy.setMedication(getMedication(123L, "Paracetamol"));

    therapy.getTimedDoseElements().add(
        getTimedSimpleDoseElement(100.0, new HourMinuteDto(9, 0), new DateTime(2014, 2, 4, 0, 0)));
    therapy.getTimedDoseElements().add(
        getTimedSimpleDoseElement(200.0, new HourMinuteDto(20, 0), new DateTime(2014, 2, 4, 0, 0)));
    therapy.getTimedDoseElements().add(
        getTimedSimpleDoseElement(300.0, new HourMinuteDto(9, 0), new DateTime(2014, 2, 5, 0, 0)));
    therapy.getTimedDoseElements().add(
        getTimedSimpleDoseElement(400.0, new HourMinuteDto(20, 0), new DateTime(2014, 2, 5, 0, 0)));

    final List<NewTaskRequestDto> tasks = administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.AUTO_CREATE,
        new DateTime(2014, 2, 5, 1, 0),
        new DateTime(2014, 2, 5, 20, 0));

    assertTrue(tasks.isEmpty());
  }

  @Test
  public void testCalculateAdministrationTimesSimple8h()
  {
    final TherapyDto therapy = getSimpleConstantTherapy8h100mg3ml(new DateTime(2014, 2, 4, 15, 0));
    final List<AdministrationDto> administrations =
        administrationTaskCreator.calculateTherapyAdministrationTimes(therapy, new DateTime(2014, 2, 4, 12, 0));

    assertEquals(3, administrations.size());
    assertEquals(new DateTime(2014, 2, 4, 15, 0), administrations.get(0).getPlannedTime());
    assertEquals(new DateTime(2014, 2, 4, 23, 0), administrations.get(1).getPlannedTime());
    assertEquals(new DateTime(2014, 2, 5, 7, 0), administrations.get(2).getPlannedTime());
  }

  @Test
  public void testCalculateAdministrationTimesComplex3x()
  {
    final TherapyDto therapy = getComplexConstantTherapy3x(new DateTime(2014, 2, 4, 15, 0));
    final List<AdministrationDto> administrations =
        administrationTaskCreator.calculateTherapyAdministrationTimes(therapy, new DateTime(2014, 2, 4, 12, 0));

    assertEquals(3, administrations.size());
    assertEquals(new DateTime(2014, 2, 4, 20, 0), administrations.get(0).getPlannedTime());
    assertEquals(new DateTime(2014, 2, 5, 8, 0), administrations.get(1).getPlannedTime());
    assertEquals(new DateTime(2014, 2, 5, 13, 0), administrations.get(2).getPlannedTime());
  }

  @Test
  public void testCalculateAdministrationTimesComplexVariableTherapyWithRate()
  {
    final List<AdministrationDto> administrations = administrationTaskCreator.calculateTherapyAdministrationTimes(
        getComplexVariableTherapyWithRate(new DateTime(2014, 2, 4, 9, 0)),
        new DateTime(2014, 2, 4, 12, 0));

    assertEquals(4, administrations.size());

    assertEquals(new DateTime(2014, 2, 4, 9, 0), administrations.get(0).getPlannedTime());
    assertEquals(20.0, ((StartAdministrationDto)administrations.get(0)).getPlannedDose().getNumerator(), 0);

    assertEquals(new DateTime(2014, 2, 4, 13, 0), administrations.get(1).getPlannedTime());
    assertEquals(30.0, ((AdjustInfusionAdministrationDto)administrations.get(1)).getPlannedDose().getNumerator(), 0);

    assertEquals(new DateTime(2014, 2, 4, 21, 0), administrations.get(2).getPlannedTime());
    assertEquals(40.0, ((AdjustInfusionAdministrationDto)administrations.get(2)).getPlannedDose().getNumerator(), 0);

    assertEquals(new DateTime(2014, 2, 4, 22, 30), administrations.get(3).getPlannedTime());
    assertEquals(AdministrationTypeEnum.STOP, administrations.get(3).getAdministrationType());
  }

  @Test
  public void testCreateTasksComplexVariableTherapyWithRateOnPRESCRIBE()
  {
    final VariableComplexTherapyDto therapy = (VariableComplexTherapyDto)getComplexVariableTherapyWithRate(
        new DateTime(2014, 2, 5, 7, 0));

    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 5));
    therapy.getTimedDoseElements().add(getTimedComplexDoseElement(20.0, 60, new HourMinuteDto(7, 0)));
    therapy.getTimedDoseElements().add(getTimedComplexDoseElement(10.0, 60, new HourMinuteDto(8, 0)));
    therapy.getTimedDoseElements().sort(Comparator.comparing(element -> element.getDoseTime().getHour()));

    /*
      rates
      - 20
      - 10
      - 20
      - 30
      - 40
     */

    final List<NewTaskRequestDto> requests = administrationTaskCreator.createTaskRequests(
        "patient",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2014, 2, 4, 12, 0),
        null);

    assertEquals(12, requests.size());

    assertEquals(AdministrationTypeEnum.START, findTypeVariable(requests.get(0).getVariables()));
    final String uuid1 = findGroupUUIdVariable(requests.get(0).getVariables());
    assertEquals(AdministrationTypeEnum.ADJUST_INFUSION, findTypeVariable(requests.get(1).getVariables()));
    assertEquals(uuid1, findGroupUUIdVariable(requests.get(1).getVariables()));
    assertEquals(AdministrationTypeEnum.ADJUST_INFUSION, findTypeVariable(requests.get(2).getVariables()));
    assertEquals(uuid1, findGroupUUIdVariable(requests.get(2).getVariables()));
    assertEquals(AdministrationTypeEnum.ADJUST_INFUSION, findTypeVariable(requests.get(3).getVariables()));
    assertEquals(uuid1, findGroupUUIdVariable(requests.get(3).getVariables()));
    assertEquals(AdministrationTypeEnum.ADJUST_INFUSION, findTypeVariable(requests.get(4).getVariables()));
    assertEquals(uuid1, findGroupUUIdVariable(requests.get(4).getVariables()));
    assertEquals(40.0, findDoseNumeratorVariable(requests.get(4).getVariables()), 0);
    assertEquals(AdministrationTypeEnum.STOP, findTypeVariable(requests.get(5).getVariables()));
    assertEquals(uuid1, findGroupUUIdVariable(requests.get(5).getVariables()));

    // last element before stop
    assertEquals(AdministrationTypeEnum.ADJUST_INFUSION, findTypeVariable(requests.get(10).getVariables()));
    assertEquals(40.0, findDoseNumeratorVariable(requests.get(10).getVariables()), 0);

    // last is stop
    assertEquals(AdministrationTypeEnum.STOP, findTypeVariable(requests.get(11).getVariables()));
    assertEquals(findGroupUUIdVariable(requests.get(10).getVariables()), findGroupUUIdVariable(requests.get(11).getVariables()));

  }

  @Test
  public void testCreateTasksComplexConstantTherapyWithRateOnPRESCRIBEWithTherapyEnd()
  {
    final DateTime therapyStart = new DateTime(2017, 1, 1, 8, 0);
    final DateTime therapyEnd = therapyStart.plusDays(2);

    final TherapyDto therapy = getComplexConstantTherapyWithRate(therapyStart, therapyEnd);

    // 2 times per day : 8, 13
    // 2 days

    final List<NewTaskRequestDto> requests = administrationTaskCreator.createTaskRequests(
        "patient",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2017, 1, 1, 7, 0),
        null);

    // START with STOP 8am 1.1
    // START with STOP 1pm 1.1
    // START with STOP 8am 1.2
    // START with STOP 1pm 1.2

    assertEquals(8, requests.size());

    // starts with 20.0
    assertEquals(AdministrationTypeEnum.START, findTypeVariable(requests.get(0).getVariables()));
    final String uuid0 = findGroupUUIdVariable(requests.get(0).getVariables());
    assertEquals(20.0, findDoseNumeratorVariable(requests.get(0).getVariables()), 0);

    assertEquals(AdministrationTypeEnum.STOP, findTypeVariable(requests.get(1).getVariables()));
    assertEquals(uuid0, findGroupUUIdVariable(requests.get(1).getVariables()));

    assertEquals(AdministrationTypeEnum.STOP, findTypeVariable(requests.get(7).getVariables()));
    assertEquals(new DateTime(2017, 1, 2, 13, 30), requests.get(7).getDue());

  }

  @Test
  public void testCreateTasksComplexVariableTherapyWithRateOnPRESCRIBEWithTherapyEnd2()
  {
    final DateTime therapyStart = new DateTime(2014, 2, 4, 7, 0);
    final VariableComplexTherapyDto therapy = (VariableComplexTherapyDto)getComplexVariableTherapyWithRate(
        therapyStart);

    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 5));
    therapy.getTimedDoseElements().add(getTimedComplexDoseElement(20.0, 60, new HourMinuteDto(7, 0)));
    therapy.getTimedDoseElements().add(getTimedComplexDoseElement(10.0, 60, new HourMinuteDto(8, 0)));
    therapy.getTimedDoseElements().sort(Comparator.comparing(element -> element.getDoseTime().getHour()));
    therapy.setEnd(therapyStart.plusDays(1));

    final List<NewTaskRequestDto> requests = administrationTaskCreator.createTaskRequests(
        "patient",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2014, 2, 4, 12, 0),
        null);

    assertEquals(6, requests.size());

    // starts with 20.0
    assertEquals(AdministrationTypeEnum.START, findTypeVariable(requests.get(0).getVariables()));
    final String uuid0 = findGroupUUIdVariable(requests.get(0).getVariables());
    assertEquals(20.0, findDoseNumeratorVariable(requests.get(0).getVariables()), 0);

    assertEquals(AdministrationTypeEnum.ADJUST_INFUSION, findTypeVariable(requests.get(1).getVariables()));
    assertEquals(10.0, findDoseNumeratorVariable(requests.get(1).getVariables()), 0);
    assertEquals(uuid0, findGroupUUIdVariable(requests.get(1).getVariables()));

    assertEquals(AdministrationTypeEnum.ADJUST_INFUSION, findTypeVariable(requests.get(2).getVariables()));
    assertEquals(20.0, findDoseNumeratorVariable(requests.get(2).getVariables()), 0);
    assertEquals(uuid0, findGroupUUIdVariable(requests.get(2).getVariables()));

    assertEquals(AdministrationTypeEnum.ADJUST_INFUSION, findTypeVariable(requests.get(3).getVariables()));
    assertEquals(30.0, findDoseNumeratorVariable(requests.get(3).getVariables()), 0);
    assertEquals(uuid0, findGroupUUIdVariable(requests.get(3).getVariables()));

    assertEquals(AdministrationTypeEnum.ADJUST_INFUSION, findTypeVariable(requests.get(4).getVariables()));
    assertEquals(40.0, findDoseNumeratorVariable(requests.get(4).getVariables()), 0);
    assertEquals(uuid0, findGroupUUIdVariable(requests.get(4).getVariables()));

    assertEquals(AdministrationTypeEnum.STOP, findTypeVariable(requests.get(5).getVariables()));
    assertEquals(uuid0, findGroupUUIdVariable(requests.get(5).getVariables()));
  }

  @Test
  public void testCreateTasksComplexVariableTherapyWithRateOnPRESCRIBEWithTherapyEnd()
  {
    final VariableComplexTherapyDto therapy = (VariableComplexTherapyDto)getComplexVariableTherapyWithRate(
        new DateTime(2014, 2, 4, 7, 0));

    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 5));
    therapy.getTimedDoseElements().add(getTimedComplexDoseElement(20.0, 60, new HourMinuteDto(7, 0)));
    therapy.getTimedDoseElements().add(getTimedComplexDoseElement(10.0, 60, new HourMinuteDto(8, 0)));
    therapy.getTimedDoseElements().sort(Comparator.comparing(element -> element.getDoseTime().getHour()));
    therapy.setEnd(new DateTime(2014, 2, 5, 8, 30));

    /*
      rates
      - 20
      - 10
      - 20
      - 30
      - 40
     */

    final List<NewTaskRequestDto> requests = administrationTaskCreator.createTaskRequests(
        "patient",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2014, 2, 4, 12, 0),
        null);

    assertEquals(9, requests.size());

    // starts with 20.0
    assertEquals(AdministrationTypeEnum.START, findTypeVariable(requests.get(0).getVariables()));
    final String uuid0 = findGroupUUIdVariable(requests.get(0).getVariables());
    assertEquals(20.0, findDoseNumeratorVariable(requests.get(0).getVariables()), 0);

    assertEquals(AdministrationTypeEnum.ADJUST_INFUSION, findTypeVariable(requests.get(1).getVariables()));
    assertEquals(10.0, findDoseNumeratorVariable(requests.get(1).getVariables()), 0);
    assertEquals(uuid0, findGroupUUIdVariable(requests.get(1).getVariables()));

    assertEquals(AdministrationTypeEnum.ADJUST_INFUSION, findTypeVariable(requests.get(2).getVariables()));
    assertEquals(20.0, findDoseNumeratorVariable(requests.get(2).getVariables()), 0);
    assertEquals(uuid0, findGroupUUIdVariable(requests.get(2).getVariables()));

    assertEquals(AdministrationTypeEnum.ADJUST_INFUSION, findTypeVariable(requests.get(3).getVariables()));
    assertEquals(30.0, findDoseNumeratorVariable(requests.get(3).getVariables()), 0);
    assertEquals(uuid0, findGroupUUIdVariable(requests.get(3).getVariables()));

    assertEquals(AdministrationTypeEnum.ADJUST_INFUSION, findTypeVariable(requests.get(4).getVariables()));
    assertEquals(40.0, findDoseNumeratorVariable(requests.get(4).getVariables()), 0);
    assertEquals(uuid0, findGroupUUIdVariable(requests.get(4).getVariables()));

    assertEquals(AdministrationTypeEnum.STOP, findTypeVariable(requests.get(5).getVariables()));
    assertEquals(uuid0, findGroupUUIdVariable(requests.get(5).getVariables()));

    assertEquals(AdministrationTypeEnum.START, findTypeVariable(requests.get(6).getVariables()));
    final String uuid1 = findGroupUUIdVariable(requests.get(6).getVariables());

    // last element before stop
    assertEquals(AdministrationTypeEnum.ADJUST_INFUSION, findTypeVariable(requests.get(7).getVariables()));
    assertEquals(10.0, findDoseNumeratorVariable(requests.get(7).getVariables()), 0);
    assertEquals(uuid1, findGroupUUIdVariable(requests.get(7).getVariables()));

    // last is stop
    assertEquals(AdministrationTypeEnum.STOP, findTypeVariable(requests.get(8).getVariables()));
    assertEquals(uuid1, findGroupUUIdVariable(requests.get(8).getVariables()));
  }

  @Test
  public void testCreateTasksComplexVariableTherapyWithRateOnREISSUEWithTherapyEnd()
  {
    final VariableComplexTherapyDto therapy = (VariableComplexTherapyDto)getComplexVariableTherapyWithRate(
        new DateTime(2014, 2, 4, 15, 0));

    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 5));
    therapy.getTimedDoseElements().add(getTimedComplexDoseElement(20.0, 60, new HourMinuteDto(7, 0)));
    therapy.getTimedDoseElements().add(getTimedComplexDoseElement(10.0, 60, new HourMinuteDto(8, 0)));
    therapy.getTimedDoseElements().sort(Comparator.comparing(element -> element.getDoseTime().getHour()));
    therapy.setEnd(new DateTime(2014, 2, 5, 8, 30));

    /*
      rates
      - 20
      - 10
      - 20
      - 30
      - 40
     */

    final List<NewTaskRequestDto> requests = administrationTaskCreator.createTaskRequests(
        "patient",
        therapy,
        AdministrationTaskCreateActionEnum.REISSUE,
        new DateTime(2014, 2, 4, 12, 0),
        null);

    assertEquals(3, requests.size());

    // starts with 20.0
    assertEquals(AdministrationTypeEnum.START, findTypeVariable(requests.get(0).getVariables()));
    final String uuid1 = findGroupUUIdVariable(requests.get(0).getVariables());

    // last element before stop
    assertEquals(AdministrationTypeEnum.ADJUST_INFUSION, findTypeVariable(requests.get(1).getVariables()));
    assertEquals(10.0, findDoseNumeratorVariable(requests.get(1).getVariables()), 0);
    assertEquals(uuid1, findGroupUUIdVariable(requests.get(1).getVariables()));

    // last is stop
    assertEquals(AdministrationTypeEnum.STOP, findTypeVariable(requests.get(2).getVariables()));
    assertEquals(uuid1, findGroupUUIdVariable(requests.get(2).getVariables()));
  }

  private AdministrationTypeEnum findTypeVariable(final List<Pair<TaskVariable, ?>> variables)
  {
    return variables.stream()
        .filter(pair -> pair.getFirst().getName().equals(AdministrationTaskDef.ADMINISTRATION_TYPE.getName()))
        .filter(Objects::nonNull)
        .findFirst()
        .map(pair -> AdministrationTypeEnum.valueOf((String)pair.getSecond()))
        .orElse(null);
  }

  private String findGroupUUIdVariable(final List<Pair<TaskVariable, ?>> variables)
  {
    return variables.stream()
        .filter(pair -> pair.getFirst().getName().equals(AdministrationTaskDef.GROUP_UUID.getName()))
        .filter(Objects::nonNull)
        .findFirst()
        .map(pair -> ((String)pair.getSecond()))
        .orElse(null);
  }

  private Double findDoseNumeratorVariable(final List<Pair<TaskVariable, ?>> variables)
  {
    return variables.stream()
        .filter(pair -> pair.getFirst().getName().equals(AdministrationTaskDef.DOSE_NUMERATOR.getName()))
        .filter(Objects::nonNull)
        .findFirst()
        .map(pair -> ((Double)pair.getSecond()))
        .orElse(null);
  }

  @Test
  public void testCalculateAdministrationTimesSimple8hFutureTime()
  {
    final TherapyDto therapy = getSimpleConstantTherapy8h100mg3ml(new DateTime(2014, 2, 20, 15, 0));
    final List<AdministrationDto> administrations =
        administrationTaskCreator.calculateTherapyAdministrationTimes(therapy, new DateTime(2014, 2, 4, 12, 0));

    assertEquals(3, administrations.size());
    assertEquals(new DateTime(2014, 2, 20, 15, 0), administrations.get(0).getPlannedTime());
    assertEquals(new DateTime(2014, 2, 20, 23, 0), administrations.get(1).getPlannedTime());
    assertEquals(new DateTime(2014, 2, 21, 7, 0), administrations.get(2).getPlannedTime());
  }

  @Test
  public void testCalculateNextTherapyAdministrationTimeSimple3x()
  {
    final TherapyDto therapy = getSimpleConstantTherapy3X500mg(null);
    final DateTime nextTherapyAdministrationTime =
        administrationTaskCreator.calculateNextTherapyAdministrationTime(
            therapy, true, new DateTime(2014, 2, 4, 12, 0));

    assertEquals(new DateTime(2014, 2, 4, 13, 0), nextTherapyAdministrationTime);
  }

  @Test
  public void testCalculateNextTherapyAdministrationTimeSimple3xDstGap()
  {
    final ConstantSimpleTherapyDto therapy = getSimpleConstantTherapy3X500mg(null);

    final List<HourMinuteDto> doseTimes = new ArrayList<>();
    doseTimes.add(new HourMinuteDto(2, 30));
    doseTimes.add(new HourMinuteDto(13, 0));
    doseTimes.add(new HourMinuteDto(20, 0));
    therapy.setDoseTimes(doseTimes);

    final DateTime nextTherapyAdministrationTime =
        administrationTaskCreator.calculateNextTherapyAdministrationTime(
            therapy, true, new DateTime(2017, 3, 26, 1, 0));

    assertEquals(new DateTime(2017, 3, 26, 3, 0), nextTherapyAdministrationTime);
  }

  @Test
  public void testCalculateNextTherapyAdministrationTimeSimple3xStartNow()
  {
    final DateTime start = new DateTime(2016, 2, 4, 8, 0);
    final TherapyDto therapy = getSimpleConstantTherapy3X500mg(null);
    therapy.setStart(start);

    final DateTime nextTherapyAdministrationTime = administrationTaskCreator.calculateNextTherapyAdministrationTime(
        therapy,
        true,
        start);

    assertEquals(start, nextTherapyAdministrationTime);
  }

  @Test
  public void testCalculateNextTherapyAdministrationTimeSimple8h()
  {
    final TherapyDto therapy = getSimpleConstantTherapy8h100mg3ml(null);
    final DateTime nextTherapyAdministrationTime =
        administrationTaskCreator.calculateNextTherapyAdministrationTime(
            therapy, true, new DateTime(2014, 2, 4, 12, 0));

    assertEquals(new DateTime(2014, 2, 4, 16, 0), nextTherapyAdministrationTime);
  }

  @Test
  public void testCalculateNextTherapyAdministrationTimeSimple3xNextDay()
  {
    final TherapyDto therapy = getSimpleConstantTherapy3X500mg(null);
    final DateTime nextTherapyAdministrationTime =
        administrationTaskCreator.calculateNextTherapyAdministrationTime(
            therapy, true, new DateTime(2014, 2, 4, 21, 0));

    assertEquals(new DateTime(2014, 2, 5, 8, 0), nextTherapyAdministrationTime);
  }

  @Test
  public void testCalculateNextTherapyAdministrationTimeSimple36Next()
  {
    final TherapyDto therapy = getSimpleConstantTherapy36h100mg3ml(null);
    final DateTime nextTherapyAdministrationTime =
        administrationTaskCreator.calculateNextTherapyAdministrationTime(
            therapy, true, new DateTime(2014, 2, 4, 7, 0));

    assertEquals(new DateTime(2014, 2, 4, 8, 0), nextTherapyAdministrationTime);
  }

  @Test
  public void testCalculateNextTherapyAdministrationTimeSimple36NextDay()
  {
    final TherapyDto therapy = getSimpleConstantTherapy36h100mg3ml(null);
    final DateTime nextTherapyAdministrationTime =
        administrationTaskCreator.calculateNextTherapyAdministrationTime(
            therapy, true, new DateTime(2014, 2, 4, 9, 0));

    assertEquals(new DateTime(2014, 2, 5, 8, 0), nextTherapyAdministrationTime);
  }

  @Test
  public void testCalculateNextTherapyAdministrationTimeSimple36ModifyNextDay()
  {
    final TherapyDto therapy = getSimpleConstantTherapy36h100mg3ml(new DateTime(2014, 2, 4, 8, 0));
    final DateTime nextTherapyAdministrationTime =
        administrationTaskCreator.calculateNextTherapyAdministrationTime(
            therapy, false, new DateTime(2014, 2, 5, 9, 0));

    assertEquals(new DateTime(2014, 2, 5, 20, 0), nextTherapyAdministrationTime);
  }

  @Test
  public void testGetDoseTaskVariablesForInfusion()
  {
    final TherapyDoseDto dose = new TherapyDoseDto();
    dose.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE_QUANTITY);
    dose.setNumerator(10.0);
    dose.setNumeratorUnit("ml/h");
    dose.setDenominator(50.0);
    dose.setDenominatorUnit("mg/kg/h");
    dose.setSecondaryNumerator(100.0);
    dose.setSecondaryNumeratorUnit("mg");
    dose.setSecondaryDenominator(500.0);
    dose.setSecondaryDenominatorUnit("ml");

    final List<Pair<TaskVariable, Object>> variables = administrationTaskCreator.getDoseTaskVariables(dose);

    assertEquals(5, variables.size());
    assertSame(AdministrationTaskDef.DOSE_TYPE, variables.get(0).getFirst());
    assertEquals(TherapyDoseTypeEnum.RATE_QUANTITY.name(), variables.get(0).getSecond());
    assertSame(AdministrationTaskDef.DOSE_NUMERATOR, variables.get(1).getFirst());
    assertEquals(10.0, variables.get(1).getSecond());
    assertSame(AdministrationTaskDef.DOSE_NUMERATOR_UNIT, variables.get(2).getFirst());
    assertEquals("ml/h", variables.get(2).getSecond());
    assertSame(AdministrationTaskDef.DOSE_DENOMINATOR, variables.get(3).getFirst());
    assertEquals(50.0, variables.get(3).getSecond());
    assertSame(AdministrationTaskDef.DOSE_DENOMINATOR_UNIT, variables.get(4).getFirst());
    assertEquals("mg/kg/h", variables.get(4).getSecond());
  }

  @Test
  public void testGetDoseTaskVariablesForSimpleTherapy()
  {
    final TherapyDoseDto dose = new TherapyDoseDto();
    dose.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.QUANTITY);
    dose.setNumerator(500.0);
    dose.setNumeratorUnit("mg");

    final List<Pair<TaskVariable, Object>> variables = administrationTaskCreator.getDoseTaskVariables(dose);

    assertEquals(3, variables.size());
    assertSame(AdministrationTaskDef.DOSE_TYPE, variables.get(0).getFirst());
    assertEquals(TherapyDoseTypeEnum.QUANTITY.name(), variables.get(0).getSecond());
    assertSame(AdministrationTaskDef.DOSE_NUMERATOR, variables.get(1).getFirst());
    assertEquals(500.0, variables.get(1).getSecond());
    assertSame(AdministrationTaskDef.DOSE_NUMERATOR_UNIT, variables.get(2).getFirst());
    assertEquals("mg", variables.get(2).getSecond());
  }

  private ConstantSimpleTherapyDto getSimpleConstantTherapy3X500mg(final DateTime therapyStart)
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setCompositionUid("uid1");
    therapy.setEhrOrderName("1");
    therapy.setStart(therapyStart);
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 3));
    therapy.setQuantityUnit("mg");
    therapy.setMedication(getMedication(222L, "Aspirin"));

    final List<HourMinuteDto> doseTimes = new ArrayList<>();
    doseTimes.add(new HourMinuteDto(8, 0));
    doseTimes.add(new HourMinuteDto(13, 0));
    doseTimes.add(new HourMinuteDto(20, 0));
    therapy.setDoseTimes(doseTimes);

    therapy.setDoseElement(getSimpleDoseElement(500.0, null));
    return therapy;
  }

  private TherapyDto getSimpleConstantTherapy8h100mg3ml(final DateTime therapyStart)
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setCompositionUid("uid1");
    therapy.setEhrOrderName("1");
    therapy.setStart(therapyStart);
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.BETWEEN_DOSES, 8));
    therapy.setQuantityUnit("mg");
    therapy.setQuantityDenominatorUnit("ml");
    therapy.setMedication(getMedication(123L, "Paracetamol"));

    final List<HourMinuteDto> doseTimes = new ArrayList<>();
    doseTimes.add(new HourMinuteDto(8, 0));
    therapy.setDoseTimes(doseTimes);

    therapy.setDoseElement(getSimpleDoseElement(100.0, 3.0));
    return therapy;
  }

  private TherapyDto getSimpleConstantTherapy36h100mg3ml(final DateTime therapyStart)
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setCompositionUid("uid1");
    therapy.setEhrOrderName("1");
    therapy.setStart(therapyStart);
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.BETWEEN_DOSES, 36));
    therapy.setQuantityUnit("mg");
    therapy.setQuantityDenominatorUnit("ml");
    therapy.setMedication(getMedication(123L, "Paracetamol"));

    final List<HourMinuteDto> doseTimes = new ArrayList<>();
    doseTimes.add(new HourMinuteDto(8, 0));
    therapy.setDoseTimes(doseTimes);

    therapy.setDoseElement(getSimpleDoseElement(100.0, 3.0));
    return therapy;
  }

  private ConstantComplexTherapyDto getComplexConstantTherapy3x(final DateTime therapyStart)
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setCompositionUid("uid1");
    therapy.setEhrOrderName("1");
    therapy.setStart(therapyStart);
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 3));

    final InfusionIngredientDto infusionIngredient = new InfusionIngredientDto();
    infusionIngredient.setMedication(getMedication(333L, "Dopamine"));
    infusionIngredient.setQuantity(50.0);
    infusionIngredient.setQuantityUnit("mg");
    infusionIngredient.setQuantityDenominator(200.0);
    infusionIngredient.setQuantityDenominatorUnit("ml");
    therapy.getIngredientsList().add(infusionIngredient);

    therapy.setDoseElement(new ComplexDoseElementDto());

    final List<HourMinuteDto> doseTimes = new ArrayList<>();
    doseTimes.add(new HourMinuteDto(8, 0));
    doseTimes.add(new HourMinuteDto(13, 0));
    doseTimes.add(new HourMinuteDto(20, 0));
    therapy.setDoseTimes(doseTimes);

    return therapy;
  }

  private TherapyDto getComplexVariableTherapyWithRate(final DateTime therapyStart)
  {
    final VariableComplexTherapyDto therapy = new VariableComplexTherapyDto();
    therapy.setCompositionUid("uid1");
    therapy.setEhrOrderName("1");
    therapy.setStart(therapyStart);
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 3));

    final InfusionIngredientDto infusionIngredient = new InfusionIngredientDto();
    infusionIngredient.setMedication(getMedication(333L, "Dopamine"));
    infusionIngredient.setQuantity(50.0);
    infusionIngredient.setQuantityUnit("mg");
    infusionIngredient.setQuantityDenominator(200.0);
    infusionIngredient.setQuantityDenominatorUnit("ml");
    therapy.getIngredientsList().add(infusionIngredient);

    therapy.getTimedDoseElements().add(getTimedComplexDoseElement(20.0, 30, new HourMinuteDto(9, 0)));
    therapy.getTimedDoseElements().add(getTimedComplexDoseElement(30.0, 60, new HourMinuteDto(13, 0)));
    therapy.getTimedDoseElements().add(getTimedComplexDoseElement(40.0, 90, new HourMinuteDto(21, 0)));

    return therapy;
  }

  private TherapyDto getComplexConstantTherapyWithRate(final DateTime therapyStart, final DateTime therapyEnd)
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setCompositionUid("uid1");
    therapy.setEhrOrderName("1");
    therapy.setStart(therapyStart);
    therapy.setEnd(therapyEnd);
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 2));

    final InfusionIngredientDto infusionIngredient = new InfusionIngredientDto();
    infusionIngredient.setMedication(getMedication(333L, "Dopamine"));
    infusionIngredient.setQuantity(50.0);
    infusionIngredient.setQuantityUnit("mg");
    infusionIngredient.setQuantityDenominator(200.0);
    infusionIngredient.setQuantityDenominatorUnit("ml");
    therapy.getIngredientsList().add(infusionIngredient);

    final ComplexDoseElementDto doseElement = new ComplexDoseElementDto();
    doseElement.setRate(20.0);
    doseElement.setRateUnit("ml");
    doseElement.setDuration(30);
    therapy.setDoseElement(doseElement);

    therapy.getDoseTimes().add(new HourMinuteDto(8, 0));
    therapy.getDoseTimes().add(new HourMinuteDto(13, 0));

    return therapy;
  }

  private TherapyDto getContinuousInfusion(final DateTime therapyStart, final DateTime therapyEnd)
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setCompositionUid("uid1");
    therapy.setEhrOrderName("1");
    therapy.setStart(therapyStart);
    therapy.setEnd(therapyEnd);
    therapy.setContinuousInfusion(true);

    final InfusionIngredientDto infusionIngredient = new InfusionIngredientDto();
    infusionIngredient.setMedication(getMedication(333L, "Dopamine"));
    therapy.getIngredientsList().add(infusionIngredient);

    final ComplexDoseElementDto doseElement = new ComplexDoseElementDto();
    doseElement.setRate(20.0);
    doseElement.setRateUnit("ml/h");
    therapy.setDoseElement(doseElement);

    final List<HourMinuteDto> doseTimes = new ArrayList<>();
    doseTimes.add(new HourMinuteDto(8, 0));
    doseTimes.add(new HourMinuteDto(13, 0));
    doseTimes.add(new HourMinuteDto(20, 0));
    therapy.setDoseTimes(doseTimes);

    return therapy;
  }

  private VariableComplexTherapyDto getVariableContinuousInfusion(final DateTime therapyStart, final DateTime therapyEnd)
  {
    final VariableComplexTherapyDto therapy = getVariableContinuousInfusionWithoutDoses(therapyStart, therapyEnd);
    therapy.getTimedDoseElements().add(getTimedComplexDoseElement(20.0, 30, new HourMinuteDto(9, 0)));
    therapy.getTimedDoseElements().add(getTimedComplexDoseElement(30.0, 60, new HourMinuteDto(13, 0)));
    therapy.getTimedDoseElements().add(getTimedComplexDoseElement(40.0, 90, new HourMinuteDto(21, 0)));
    return therapy;
  }

  private VariableComplexTherapyDto getVariableContinuousInfusionWithoutDoses(
      final DateTime therapyStart,
      final DateTime therapyEnd)
  {
    final VariableComplexTherapyDto therapy = new VariableComplexTherapyDto();
    therapy.setCompositionUid("uid1");
    therapy.setEhrOrderName("1");
    therapy.setStart(therapyStart);
    therapy.setEnd(therapyEnd);
    therapy.setContinuousInfusion(true);

    final InfusionIngredientDto infusionIngredient = new InfusionIngredientDto();
    infusionIngredient.setMedication(getMedication(333L, "Dopamine"));
    infusionIngredient.setQuantity(50.0);
    infusionIngredient.setQuantityUnit("mg");
    infusionIngredient.setQuantityDenominator(200.0);
    infusionIngredient.setQuantityDenominatorUnit("ml");
    therapy.getIngredientsList().add(infusionIngredient);
    return therapy;
  }

  private VariableComplexTherapyDto getVariableContinuousInfusionOverMidnight(
      final DateTime therapyStart,
      final DateTime therapyEnd)
  {
    final VariableComplexTherapyDto therapy = getVariableContinuousInfusionWithoutDoses(therapyStart, therapyEnd);
    therapy.getTimedDoseElements().add(getTimedComplexDoseElement(20.0, 60, new HourMinuteDto(22, 0)));
    therapy.getTimedDoseElements().add(getTimedComplexDoseElement(30.0, 240, new HourMinuteDto(23, 0)));
    therapy.getTimedDoseElements().add(getTimedComplexDoseElement(40.0, null, new HourMinuteDto(3, 0)));
    return therapy;
  }

  private MedicationDto getMedication(final Long id, final String name)
  {
    final MedicationDto medicationDto = new MedicationDto();
    medicationDto.setId(id);
    medicationDto.setName(name);
    return medicationDto;
  }

  private TimedSimpleDoseElementDto getTimedSimpleDoseElement(
      final Double numerator,
      final HourMinuteDto hourMinuteDto,
      final DateTime date)
  {
    final TimedSimpleDoseElementDto timedDoseElement = new TimedSimpleDoseElementDto();
    timedDoseElement.setDoseElement(getSimpleDoseElement(numerator, null));
    timedDoseElement.setDoseTime(hourMinuteDto);
    timedDoseElement.setDate(date);
    return timedDoseElement;
  }

  private TimedSimpleDoseElementDto getTimedSimpleDoseElement(final Double numerator, final HourMinuteDto hourMinuteDto)
  {
    return getTimedSimpleDoseElement(numerator, hourMinuteDto, null);
  }

  private SimpleDoseElementDto getSimpleDoseElement(final Double numerator, final Double denominator)
  {
    final SimpleDoseElementDto doseElement = new SimpleDoseElementDto();
    doseElement.setQuantity(numerator);
    doseElement.setQuantityDenominator(denominator);
    return doseElement;
  }

  private TimedComplexDoseElementDto getTimedComplexDoseElement(
      final Double rate,
      final Integer duration,
      final HourMinuteDto hourMinuteDto)
  {
    final TimedComplexDoseElementDto timedDoseElement = new TimedComplexDoseElementDto();
    timedDoseElement.setDoseElement(getComplexDoseElement(rate, duration));
    timedDoseElement.setDoseTime(hourMinuteDto);
    return timedDoseElement;
  }

  private ComplexDoseElementDto getComplexDoseElement(final Double rate, final Integer duration)
  {
    final ComplexDoseElementDto doseElement = new ComplexDoseElementDto();
    doseElement.setRate(rate);
    doseElement.setRateUnit("ml/h");
    doseElement.setDuration(duration);
    return doseElement;
  }

  private void assertQuantityStartTaskRequest(
      final NewTaskRequestDto taskRequest,
      final String patientId,
      final DateTime time,
      final Double numerator,
      final String numeratorUnit,
      final Double denominator,
      final String denominatorUnit)
  {
    assertStartTaskRequest(
        taskRequest,
        patientId,
        time,
        numerator,
        numeratorUnit,
        denominator,
        denominatorUnit,
        TherapyDoseTypeEnum.QUANTITY);
  }

  private void assertStartTaskRequest(
      final NewTaskRequestDto taskRequest,
      final String patientId,
      final DateTime time,
      final Double numerator,
      final String numeratorUnit,
      final Double denominator,
      final String denominatorUnit,
      final TherapyDoseTypeEnum doseType)
  {
    assertTaskRequest(
        taskRequest,
        patientId,
        time,
        AdministrationTypeEnum.START,
        numerator,
        numeratorUnit,
        denominator,
        denominatorUnit,
        doseType);
  }

  private void assertTaskRequest(
      final NewTaskRequestDto taskRequest,
      final String patientId,
      final DateTime time,
      final AdministrationTypeEnum type,
      final Double numerator,
      final String numeratorUnit,
      final Double denominator,
      final String denominatorUnit,
      final TherapyDoseTypeEnum doseType)
  {
    assertEquals(patientId, getTaskVariableValue(taskRequest, MedsTaskDef.PATIENT_ID));
    assertEquals(time, taskRequest.getDue());
    assertEquals(type.name(), getTaskVariableValue(taskRequest, AdministrationTaskDef.ADMINISTRATION_TYPE));
    assertEquals(numerator, getTaskVariableValue(taskRequest, AdministrationTaskDef.DOSE_NUMERATOR));
    assertEquals(numeratorUnit, getTaskVariableValue(taskRequest, AdministrationTaskDef.DOSE_NUMERATOR_UNIT));
    assertEquals(denominator, getTaskVariableValue(taskRequest, AdministrationTaskDef.DOSE_DENOMINATOR));
    assertEquals(denominatorUnit, getTaskVariableValue(taskRequest, AdministrationTaskDef.DOSE_DENOMINATOR_UNIT));
    assertEquals(
        doseType != null ? doseType.name() : null,
        getTaskVariableValue(taskRequest, AdministrationTaskDef.DOSE_TYPE));
  }

  private Object getTaskVariableValue(final NewTaskRequestDto taskRequest, final TaskVariable variable)
  {
    for (final Pair<TaskVariable, ?> taskVariablePair : taskRequest.getVariables())
    {
      if (taskVariablePair.getFirst().equals(variable))
      {
        return taskVariablePair.getSecond();
      }
    }
    return null;
  }
}
