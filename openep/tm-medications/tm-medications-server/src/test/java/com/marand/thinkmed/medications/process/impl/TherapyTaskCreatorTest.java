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

import java.util.List;

import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.maf.core.daterule.service.MafDateRuleService;
import com.marand.maf.core.time.Intervals;
import com.marand.maf.core.Pair;
import com.marand.maf.core.time.DayType;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.IngredientsAndFormCluster;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.MedicationDeliveryMethodEnum;
import com.marand.thinkmed.medications.MedicationPreferencesUtil;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.process.task.MedicationTaskDef;
import com.marand.thinkmed.medications.test.MedicationsTestUtils;
import com.marand.thinkmed.process.definition.TaskVariable;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openehr.jaxb.rm.DvTime;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.database.annotations.Transactional;
import org.unitils.database.util.TransactionMode;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;

import static com.marand.openehr.medications.tdo.AdministrationDetailsCluster.InfusionAdministrationDetailsCluster;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction.OrderActivity;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster.TimingCluster.DayOfWeek;
import static org.junit.Assert.assertEquals;

/**
 * @author Mitja Lapajne
 */
@RunWith(UnitilsJUnit4TestClassRunner.class)
@Transactional(TransactionMode.ROLLBACK)
@SpringApplicationContext(
    {
        "/com/marand/maf_test/unitils/tc-unitils.xml",
        "com/marand/thinkmed/medications/process/impl/TherapyTaskCreatorTest-context.xml"

    }
)
public class TherapyTaskCreatorTest
{
  @SpringBeanByName
  private TherapyTaskCreatorImpl therapyTaskCreator;

  @SpringBeanByName
  private MafDateRuleService mafDateRuleService;

  @Before
  public void setUpMocks()
  {
    Mockito.reset(mafDateRuleService);
    Mockito.when(mafDateRuleService.isDateOfType(new DateTime(2014, 2, 3, 0, 0), DayType.WORKING_DAY)).thenReturn(true);
    Mockito.when(mafDateRuleService.isDateOfType(new DateTime(2014, 2, 4, 0, 0), DayType.WORKING_DAY)).thenReturn(true);
    Mockito.when(mafDateRuleService.isDateOfType(new DateTime(2014, 2, 5, 0, 0), DayType.WORKING_DAY)).thenReturn(true);
    Mockito.when(mafDateRuleService.isDateOfType(new DateTime(2014, 2, 6, 0, 0), DayType.WORKING_DAY)).thenReturn(true);
    Mockito.when(mafDateRuleService.isDateOfType(new DateTime(2014, 2, 7, 0, 0), DayType.WORKING_DAY)).thenReturn(true);
    Mockito.when(mafDateRuleService.isDateOfType(new DateTime(2014, 2, 8, 0, 0), DayType.WORKING_DAY)).thenReturn(false);
    Mockito.when(mafDateRuleService.isDateOfType(new DateTime(2014, 2, 9, 0, 0), DayType.WORKING_DAY)).thenReturn(false);
    Mockito.when(mafDateRuleService.isDateOfType(new DateTime(2014, 2, 10, 0, 0), DayType.WORKING_DAY)).thenReturn(true);
    Mockito.when(mafDateRuleService.isDateOfType(new DateTime(2014, 2, 11, 0, 0), DayType.WORKING_DAY)).thenReturn(true);
  }

  @Test
  public void testCalculateAdministrationTasksInterval1() //in rounds
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00
    final MedicationInstructionInstruction instruction = MedicationsTestUtils.buildTestMedicationInstruction("1");
    instruction.getOrder().add(new OrderActivity());
    instruction.getOrder().get(0).setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 4, 12, 0),
            Intervals.INFINITE.getEnd(), null)); //4.2.2014 was Tuesday

    final List<Interval> intervals =
        therapyTaskCreator.calculateAdministrationTasksInterval(
            roundsIntervalDto, instruction, new DateTime(2014, 2, 4, 9, 0), true);
    assertEquals(1, intervals.size());
    assertEquals(new DateTime(2014, 2, 4, 12, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 5, 17, 0), intervals.get(0).getEnd());
  }

  @Test
  public void testCalculateAdministrationTasksInterval2() //after rounds same day
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00
    final MedicationInstructionInstruction instruction = MedicationsTestUtils.buildTestMedicationInstruction("1");
    instruction.getOrder().add(new OrderActivity());
    instruction.getOrder().get(0).setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 4, 20, 0),
            Intervals.INFINITE.getEnd(), null)); //4.2.2014 was Tuesday
    final List<Interval> intervals =
        therapyTaskCreator.calculateAdministrationTasksInterval(
            roundsIntervalDto, instruction, new DateTime(2014, 2, 4, 9, 0), true);
    assertEquals(1, intervals.size());
    assertEquals(new DateTime(2014, 2, 4, 20, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 5, 17, 0), intervals.get(0).getEnd());
  }

  @Test
  public void testCalculateAdministrationTasksInterval3() //after rounds next day
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00
    final MedicationInstructionInstruction instruction = MedicationsTestUtils.buildTestMedicationInstruction("1");
    instruction.getOrder().add(new OrderActivity());
    instruction.getOrder().get(0).setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null,
            new DateTime(2014, 2, 5, 1, 0), Intervals.INFINITE.getEnd(), null)); //4.2.2014 was Tuesday

    final List<Interval> intervals =
        therapyTaskCreator.calculateAdministrationTasksInterval(
            roundsIntervalDto, instruction, new DateTime(2014, 2, 4, 9, 0), true);
    assertEquals(1, intervals.size());
    assertEquals(new DateTime(2014, 2, 5, 1, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 5, 17, 0), intervals.get(0).getEnd());
  }

  @Test
  public void testCalculateAdministrationTasksInterval4() //in rounds with daysOfWeek, next day valid
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00
    final MedicationInstructionInstruction instruction = MedicationsTestUtils.buildTestMedicationInstruction("1");
    instruction.getOrder().add(new OrderActivity());
    instruction.getOrder().get(0).setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 4, 12, 0), Intervals.INFINITE.getEnd(), null,
            DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY)); //4.2.2014 was Tuesday

    final List<Interval> intervals =
        therapyTaskCreator.calculateAdministrationTasksInterval(
            roundsIntervalDto, instruction, new DateTime(2014, 2, 4, 9, 0), true);
    assertEquals(1, intervals.size());
    assertEquals(new DateTime(2014, 2, 4, 12, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 5, 17, 0), intervals.get(0).getEnd());
  }

  @Test
  public void testCalculateAdministrationTasksInterval5() //in rounds with daysOfWeek, next day inactive day of week
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00
    final MedicationInstructionInstruction instruction = MedicationsTestUtils.buildTestMedicationInstruction("1");
    instruction.getOrder().add(new OrderActivity());
    instruction.getOrder().get(0).setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 4, 12, 0), Intervals.INFINITE.getEnd(), null,
            DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)); //4.2.2014 was Tuesday

    final List<Interval> intervals =
        therapyTaskCreator.calculateAdministrationTasksInterval(
            roundsIntervalDto, instruction, new DateTime(2014, 2, 4, 9, 0), true);
    assertEquals(1, intervals.size());
    assertEquals(new DateTime(2014, 2, 4, 12, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 5, 7, 0), intervals.get(0).getEnd());
  }

  @Test
  public void testCalculateAdministrationTasksInterval6() //next day work free (weekend)
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00
    final MedicationInstructionInstruction instruction = MedicationsTestUtils.buildTestMedicationInstruction("1");
    instruction.getOrder().add(new OrderActivity());
    instruction.getOrder().get(0).setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 7, 12, 0), Intervals.INFINITE.getEnd(), null)); //7.2.2014 was Friday

    final List<Interval> intervals =
        therapyTaskCreator.calculateAdministrationTasksInterval(
            roundsIntervalDto, instruction, new DateTime(2014, 2, 7, 9, 0), true);
    assertEquals(1, intervals.size());
    assertEquals(new DateTime(2014, 2, 7, 12, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 10, 17, 0), intervals.get(0).getEnd());
  }

  @Test
  public void testCalculateAdministrationTasksInterval7() //next day work free (weekend), next day inactive day of week
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00
    final MedicationInstructionInstruction instruction = MedicationsTestUtils.buildTestMedicationInstruction("1");
    instruction.getOrder().add(new OrderActivity());
    instruction.getOrder().get(0).setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 7, 12, 0), Intervals.INFINITE.getEnd(), null,
            DayOfWeek.FRIDAY, DayOfWeek.SUNDAY, DayOfWeek.MONDAY)); //7.2.2014 was Friday

    final List<Interval> intervals =
        therapyTaskCreator.calculateAdministrationTasksInterval(
            roundsIntervalDto, instruction, new DateTime(2014, 2, 7, 9, 0), true);
    assertEquals(2, intervals.size());
    assertEquals(new DateTime(2014, 2, 7, 12, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 8, 7, 0), intervals.get(0).getEnd());
    assertEquals(new DateTime(2014, 2, 9, 7, 0), intervals.get(1).getStart());
    assertEquals(new DateTime(2014, 2, 10, 17, 0), intervals.get(1).getEnd());
  }

  @Test
  public void testCalculateAdministrationTasksInterval8() //next day inactive frequency
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00
    final MedicationInstructionInstruction instruction = MedicationsTestUtils.buildTestMedicationInstruction("1");
    instruction.getOrder().add(new OrderActivity());
    instruction.getOrder().get(0).setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 4, 12, 0), Intervals.INFINITE.getEnd(), 2)); //4.2.2014 was Tuesday

    final List<Interval> intervals =
        therapyTaskCreator.calculateAdministrationTasksInterval(
            roundsIntervalDto, instruction, new DateTime(2014, 2, 4, 9, 0), true);
    assertEquals(1, intervals.size());
    assertEquals(new DateTime(2014, 2, 4, 12, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 5, 7, 0), intervals.get(0).getEnd());
  }

  @Test
  public void testCalculateAdministrationTasksInterval9() //next day work free (weekend), next day inactive frequency
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00
    final MedicationInstructionInstruction instruction = MedicationsTestUtils.buildTestMedicationInstruction("1");
    instruction.getOrder().add(new OrderActivity());
    instruction.getOrder().get(0).setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 7, 12, 0), Intervals.INFINITE.getEnd(), 2)); //7.2.2014 was Friday

    final List<Interval> intervals =
        therapyTaskCreator.calculateAdministrationTasksInterval(
            roundsIntervalDto, instruction, new DateTime(2014, 2, 7, 9, 0), true);
    assertEquals(2, intervals.size());
    assertEquals(new DateTime(2014, 2, 7, 12, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 8, 7, 0), intervals.get(0).getEnd());
    assertEquals(new DateTime(2014, 2, 9, 7, 0), intervals.get(1).getStart());
    assertEquals(new DateTime(2014, 2, 10, 7, 0), intervals.get(1).getEnd());
  }

  @Test
  public void testCalculateAdministrationTasksInterval10() //therapy created one day before medication_timing.start
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00
    final MedicationInstructionInstruction instruction = MedicationsTestUtils.buildTestMedicationInstruction("1");
    instruction.getOrder().add(new OrderActivity());
    instruction.getOrder().get(0).setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 4, 12, 0), Intervals.INFINITE.getEnd(), null));
    //4.2.2014 was Tuesday

    final List<Interval> intervals =
        therapyTaskCreator.calculateAdministrationTasksInterval(
            roundsIntervalDto, instruction, new DateTime(2014, 2, 3, 9, 0), true);
    assertEquals(1, intervals.size());
    assertEquals(new DateTime(2014, 2, 4, 12, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 4, 17, 0), intervals.get(0).getEnd());
  }

  @Test
  public void testCalculateAdministrationTasksInterval11() //therapy created three days before medication_timing.start
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00
    final MedicationInstructionInstruction instruction = MedicationsTestUtils.buildTestMedicationInstruction("1");
    instruction.getOrder().add(new OrderActivity());
    instruction.getOrder().get(0).setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 4, 12, 0), Intervals.INFINITE.getEnd(), null));
    //4.2.2014 was Tuesday

    final List<Interval> intervals =
        therapyTaskCreator.calculateAdministrationTasksInterval(
            roundsIntervalDto, instruction, new DateTime(2014, 2, 1, 9, 0), true);
    assertEquals(0, intervals.size());
  }

  @Test
  public void testCalculateAdministrationTasksInterval12() //therapy ends on same day
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00
    final MedicationInstructionInstruction instruction = MedicationsTestUtils.buildTestMedicationInstruction("1");
    instruction.getOrder().add(new OrderActivity());
    instruction.getOrder().get(0).setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 4, 12, 0), new DateTime(2014, 2, 4, 23, 0), null));
    //4.2.2014 was Tuesday

    final List<Interval> intervals =
        therapyTaskCreator.calculateAdministrationTasksInterval(
            roundsIntervalDto, instruction, new DateTime(2014, 2, 4, 9, 0), true);
    assertEquals(1, intervals.size());
    assertEquals(new DateTime(2014, 2, 4, 12, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 4, 23, 0), intervals.get(0).getEnd());
  }

  @Test
  public void testCalculateAdministrationTasksInterval13() //therapy ends next day
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00
    final MedicationInstructionInstruction instruction = MedicationsTestUtils.buildTestMedicationInstruction("1");
    instruction.getOrder().add(new OrderActivity());
    instruction.getOrder().get(0).setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 4, 12, 0), new DateTime(2014, 2, 5, 12, 0), null));
    //4.2.2014 was Tuesday

    final List<Interval> intervals =
        therapyTaskCreator.calculateAdministrationTasksInterval(
            roundsIntervalDto, instruction, new DateTime(2014, 2, 4, 9, 0), true);
    assertEquals(1, intervals.size());
    assertEquals(new DateTime(2014, 2, 4, 12, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 5, 12, 0), intervals.get(0).getEnd());
  }

  @Test
  public void testCalculateAdministrationTasksInterval14() //therapy ends after two days on weekend
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00
    final MedicationInstructionInstruction instruction = MedicationsTestUtils.buildTestMedicationInstruction("1");
    instruction.getOrder().add(new OrderActivity());
    instruction.getOrder().get(0).setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 7, 12, 0), new DateTime(2014, 2, 9, 12, 0), null));
    //7.2.2014 was Friday

    final List<Interval> intervals =
        therapyTaskCreator.calculateAdministrationTasksInterval(
            roundsIntervalDto, instruction, new DateTime(2014, 2, 7, 9, 0), true);
    assertEquals(1, intervals.size());
    assertEquals(new DateTime(2014, 2, 7, 12, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 9, 12, 0), intervals.get(0).getEnd());
  }

  @Test
  public void testCalculateAdministrationTasksInterval15() //therapy ends next day on weekend, next day inactive frequency
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00
    final MedicationInstructionInstruction instruction = MedicationsTestUtils.buildTestMedicationInstruction("1");
    instruction.getOrder().add(new OrderActivity());
    instruction.getOrder().get(0).setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 7, 12, 0), new DateTime(2014, 2, 8, 12, 0), 2));
    //7.2.2014 was Friday

    final List<Interval> intervals =
        therapyTaskCreator.calculateAdministrationTasksInterval(
            roundsIntervalDto, instruction, new DateTime(2014, 2, 7, 9, 0), true);
    assertEquals(1, intervals.size());
    assertEquals(new DateTime(2014, 2, 7, 12, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 8, 7, 0), intervals.get(0).getEnd());
  }

  @Test
  public void testCalculateAdministrationTasksInterval16() //therapy ends after two days on weekend, next day inactive frequency
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00
    final MedicationInstructionInstruction instruction = MedicationsTestUtils.buildTestMedicationInstruction("1");
    instruction.getOrder().add(new OrderActivity());
    instruction.getOrder().get(0).setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 7, 12, 0), new DateTime(2014, 2, 9, 12, 0), 2));
    //7.2.2014 was Friday

    final List<Interval> intervals =
        therapyTaskCreator.calculateAdministrationTasksInterval(
            roundsIntervalDto, instruction, new DateTime(2014, 2, 7, 9, 0), true);
    assertEquals(2, intervals.size());
    assertEquals(new DateTime(2014, 2, 7, 12, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 8, 7, 0), intervals.get(0).getEnd());
    assertEquals(new DateTime(2014, 2, 9, 7, 0), intervals.get(1).getStart());
    assertEquals(new DateTime(2014, 2, 9, 12, 0), intervals.get(1).getEnd());
  }

  @Test
  public void testCalculateAdministrationTasksInterval17() //review therapy, today and tomorrow inactive days of week
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00
    final MedicationInstructionInstruction instruction = MedicationsTestUtils.buildTestMedicationInstruction("1");
    instruction.getOrder().add(new OrderActivity());
    instruction.getOrder().get(0).setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            4L, null, null, null, new DateTime(2014, 2, 1, 12, 0), Intervals.INFINITE.getEnd(), null,
            DayOfWeek.SATURDAY)); //4.2.2014 was Tuesday

    final List<Interval> intervals =
        therapyTaskCreator.calculateAdministrationTasksInterval(
            roundsIntervalDto, instruction, new DateTime(2014, 2, 4, 9, 0), false);
    assertEquals(0, intervals.size());
  }

  @Test
  public void testCalculateAdministrationTasksInterval18() //review therapy, today inactive day of week
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00
    final MedicationInstructionInstruction instruction = MedicationsTestUtils.buildTestMedicationInstruction("1");
    instruction.getOrder().add(new OrderActivity());
    instruction.getOrder().get(0).setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            4L, null, null, null, new DateTime(2014, 2, 1, 12, 0), Intervals.INFINITE.getEnd(), null,
            DayOfWeek.SATURDAY, DayOfWeek.WEDNESDAY)); //4.2.2014 was Tuesday

    final List<Interval> intervals =
        therapyTaskCreator.calculateAdministrationTasksInterval(
            roundsIntervalDto, instruction, new DateTime(2014, 2, 4, 9, 0), false);
    assertEquals(1, intervals.size());
    assertEquals(new DateTime(2014, 2, 5, 7, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 5, 17, 0), intervals.get(0).getEnd());
  }


  @Test
  public void testCalculateAdministrationTasksInterval19() //review therapy, tomorrow inactive day of week
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00
    final MedicationInstructionInstruction instruction = MedicationsTestUtils.buildTestMedicationInstruction("1");
    instruction.getOrder().add(new OrderActivity());
    instruction.getOrder().get(0).setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            4L, null, null, null, new DateTime(2014, 2, 1, 12, 0), Intervals.INFINITE.getEnd(), null,
            DayOfWeek.SATURDAY, DayOfWeek.TUESDAY)); //4.2.2014 was Tuesday

    final List<Interval> intervals =
        therapyTaskCreator.calculateAdministrationTasksInterval(
            roundsIntervalDto, instruction, new DateTime(2014, 2, 4, 9, 0), false);
    assertEquals(1, intervals.size());
    assertEquals(new DateTime(2014, 2, 4, 9, 0), intervals.get(0).getStart());
    assertEquals(new DateTime(2014, 2, 5, 7, 0), intervals.get(0).getEnd());
  }

  @Test
  public void testCalculateAdministrationTasksInterval20() //therapy created one day before medication_timing.start, medication_timing.start after next days rounds
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00
    final MedicationInstructionInstruction instruction = MedicationsTestUtils.buildTestMedicationInstruction("1");
    instruction.getOrder().add(new OrderActivity());
    instruction.getOrder().get(0).setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 5, 20, 0), null, null, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY)
    );
    //5.2.2014 was Wednesday

    final List<Interval> intervals =
        therapyTaskCreator.calculateAdministrationTasksInterval(
            roundsIntervalDto, instruction, new DateTime(2014, 2, 4, 9, 0), true);
    assertEquals(0, intervals.size());
  }

  @Test
  public void testCreateTasks1() //simple constant therapy, 3X per day
  {
    final MedicationOrderComposition composition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", new DateTime(2014, 2, 4, 12, 0), null);
    final MedicationInstructionInstruction instruction =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction");
    final OrderActivity orderActivity =
        MedicationsTestUtils.buildTestOrderActivity(
            "Daleron", 2L, 500.0, "mg", null, null, null, null, "O", "ORAL", "1", "TBL");
    instruction.getOrder().add(orderActivity);
    orderActivity.setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 4, 12, 0),
            Intervals.INFINITE.getEnd(), null)); //4.2.2014 was Tuesday
    // 3X = 8:00, 13:00, 20:00
    //rounds interval = 8:00 - 17:00
    final List<NewTaskRequestDto> tasks = therapyTaskCreator.createTasks(
        1L,
        Pair.of(composition, instruction),
        MedicationPreferencesUtil.getDefaultAdministrationTimingDto(),
        MedicationPreferencesUtil.getDefaultRoundsIntervalDto(),
        new DateTime(2014, 2, 4, 12, 0),
        true,
        null);

    assertEquals(5, tasks.size());
    assertTaskRequest(tasks.get(0), 1L, new DateTime(2014, 2, 4, 12, 0), 500.0, "mg", null, null);
    assertTaskRequest(tasks.get(1), 1L, new DateTime(2014, 2, 4, 13, 0), 500.0, "mg", null, null);
    assertTaskRequest(tasks.get(2), 1L, new DateTime(2014, 2, 4, 20, 0), 500.0, "mg", null, null);
    assertTaskRequest(tasks.get(3), 1L, new DateTime(2014, 2, 5, 8, 0), 500.0, "mg", null, null);
    assertTaskRequest(tasks.get(4), 1L, new DateTime(2014, 2, 5, 13, 0), 500.0, "mg", null, null);
  }

  @Test
  public void testCreateTasks2() //simple constant therapy, 3X per day, start close to first administration
  {
    final MedicationOrderComposition composition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", new DateTime(2014, 2, 4, 12, 0), null);
    final MedicationInstructionInstruction instruction =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction");
    final OrderActivity orderActivity =
        MedicationsTestUtils.buildTestOrderActivity(
            "Daleron", 2L, 500.0, "mg", null, null, null, null, "O", "ORAL", "1", "TBL");
    instruction.getOrder().add(orderActivity);
    orderActivity.setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 4, 12, 45),
            Intervals.INFINITE.getEnd(), null)); //4.2.2014 was Tuesday
    // 3X = 8:00, 13:00, 20:00
    //rounds interval = 8:00 - 17:00
    final List<NewTaskRequestDto> tasks = therapyTaskCreator.createTasks(
        1L,
        Pair.of(composition, instruction),
        MedicationPreferencesUtil.getDefaultAdministrationTimingDto(),
        MedicationPreferencesUtil.getDefaultRoundsIntervalDto(),
        new DateTime(2014, 2, 4, 12, 45),
        true,
        null);

    assertEquals(4, tasks.size());
    assertTaskRequest(tasks.get(0), 1L, new DateTime(2014, 2, 4, 12, 45), 500.0, "mg", null, null);
    assertTaskRequest(tasks.get(1), 1L, new DateTime(2014, 2, 4, 20, 0), 500.0, "mg", null, null);
    assertTaskRequest(tasks.get(2), 1L, new DateTime(2014, 2, 5, 8, 0), 500.0, "mg", null, null);
    assertTaskRequest(tasks.get(3), 1L, new DateTime(2014, 2, 5, 13, 0), 500.0, "mg", null, null);
  }

  @Test
  public void testCreateTasks3() //simple constant therapy, every 6 hours
  {
    final MedicationOrderComposition composition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", new DateTime(2014, 2, 4, 12, 0), null);
    final MedicationInstructionInstruction instruction =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction");
    final OrderActivity orderActivity =
        MedicationsTestUtils.buildTestOrderActivity(
            "Daleron", 2L, 500.0, "mg", null, null, null, null, "O", "ORAL", "1", "TBL");
    instruction.getOrder().add(orderActivity);
    orderActivity.setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            null, 6, null, null, new DateTime(2014, 2, 4, 12, 0),
            Intervals.INFINITE.getEnd(), null)); //4.2.2014 was Tuesday
    //rounds interval = 8:00 - 17:00
    final List<NewTaskRequestDto> tasks = therapyTaskCreator.createTasks(
        1L,
        Pair.of(composition, instruction),
        MedicationPreferencesUtil.getDefaultAdministrationTimingDto(),
        MedicationPreferencesUtil.getDefaultRoundsIntervalDto(),
        new DateTime(2014, 2, 4, 12, 0),
        true,
        null);

    assertEquals(5, tasks.size());
    assertTaskRequest(tasks.get(0), 1L, new DateTime(2014, 2, 4, 12, 0), 500.0, "mg", null, null);
    assertTaskRequest(tasks.get(1), 1L, new DateTime(2014, 2, 4, 18, 0), 500.0, "mg", null, null);
    assertTaskRequest(tasks.get(2), 1L, new DateTime(2014, 2, 5, 0, 0), 500.0, "mg", null, null);
    assertTaskRequest(tasks.get(3), 1L, new DateTime(2014, 2, 5, 6, 0), 500.0, "mg", null, null);
    assertTaskRequest(tasks.get(4), 1L, new DateTime(2014, 2, 5, 12, 0), 500.0, "mg", null, null);
  }

  @Test
  public void testCreateTasks4() //simple constant therapy, morning
  {
    final MedicationOrderComposition composition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", new DateTime(2014, 2, 4, 12, 0), null);
    final MedicationInstructionInstruction instruction =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction");
    final OrderActivity orderActivity =
        MedicationsTestUtils.buildTestOrderActivity(
            "Daleron", 2L, 500.0, "mg", null, null, null, null, "O", "ORAL", "1", "TBL");
    instruction.getOrder().add(orderActivity);
    orderActivity.setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            null, null, null, DosingFrequencyTypeEnum.MORNING, new DateTime(2014, 2, 4, 8, 0),
            Intervals.INFINITE.getEnd(), null)); //4.2.2014 was Tuesday
    //rounds interval = 8:00 - 17:00
    //morning = 8:00
    final List<NewTaskRequestDto> tasks = therapyTaskCreator.createTasks(
        1L,
        Pair.of(composition, instruction),
        MedicationPreferencesUtil.getDefaultAdministrationTimingDto(),
        MedicationPreferencesUtil.getDefaultRoundsIntervalDto(),
        new DateTime(2014, 2, 4, 8, 0),
        true,
        null);

    assertEquals(2, tasks.size());
    assertTaskRequest(tasks.get(0), 1L, new DateTime(2014, 2, 4, 8, 0), 500.0, "mg", null, null);
    assertTaskRequest(tasks.get(1), 1L, new DateTime(2014, 2, 5, 8, 0), 500.0, "mg", null, null);
  }

  @Test
  public void testCreateTasks5() //simple constant therapy, 5x per day
  {
    final MedicationOrderComposition composition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", new DateTime(2014, 2, 4, 12, 0), null);
    final MedicationInstructionInstruction instruction =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction");
    final OrderActivity orderActivity =
        MedicationsTestUtils.buildTestOrderActivity(
            "Daleron", 2L, 500.0, "mg", null, null, null, null, "O", "ORAL", "1", "TBL");
    instruction.getOrder().add(orderActivity);
    orderActivity.setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            5L, null, null, null, new DateTime(2014, 2, 4, 12, 0),
            Intervals.INFINITE.getEnd(), null)); //4.2.2014 was Tuesday
    //rounds interval = 8:00 - 17:00
    final List<NewTaskRequestDto> tasks = therapyTaskCreator.createTasks(
        1L,
        Pair.of(composition, instruction),
        MedicationPreferencesUtil.getDefaultAdministrationTimingDto(),
        MedicationPreferencesUtil.getDefaultRoundsIntervalDto(),
        new DateTime(2014, 2, 4, 12, 0),
        true,
        null);

    assertEquals(7, tasks.size());
    assertTaskRequest(tasks.get(0), 1L, new DateTime(2014, 2, 4, 12, 0), 500.0, "mg", null, null);
    assertTaskRequest(tasks.get(1), 1L, new DateTime(2014, 2, 4, 16, 0), 500.0, "mg", null, null);
    assertTaskRequest(tasks.get(2), 1L, new DateTime(2014, 2, 4, 20, 0), 500.0, "mg", null, null);
    assertTaskRequest(tasks.get(3), 1L, new DateTime(2014, 2, 5, 0, 0), 500.0, "mg", null, null);
    assertTaskRequest(tasks.get(4), 1L, new DateTime(2014, 2, 5, 8, 0), 500.0, "mg", null, null);
    assertTaskRequest(tasks.get(5), 1L, new DateTime(2014, 2, 5, 12, 0), 500.0, "mg", null, null);
    assertTaskRequest(tasks.get(6), 1L, new DateTime(2014, 2, 5, 16, 0), 500.0, "mg", null, null);
  }

  @Test
  public void testCreateTasks6() //simple constant therapy, once then ex
  {
    final MedicationOrderComposition composition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", new DateTime(2014, 2, 4, 12, 0), null);
    final MedicationInstructionInstruction instruction =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction");
    final OrderActivity orderActivity =
        MedicationsTestUtils.buildTestOrderActivity(
            "Daleron", 2L, 500.0, "mg", null, null, null, null, "O", "ORAL", "1", "TBL");
    instruction.getOrder().add(orderActivity);
    orderActivity.setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            null, null, null, DosingFrequencyTypeEnum.ONCE_THEN_EX, new DateTime(2014, 2, 4, 12, 0),
            new DateTime(2014, 2, 4, 12, 0), null)); //4.2.2014 was Tuesday
    //rounds interval = 8:00 - 17:00
    final List<NewTaskRequestDto> tasks = therapyTaskCreator.createTasks(
        1L,
        Pair.of(composition, instruction),
        MedicationPreferencesUtil.getDefaultAdministrationTimingDto(),
        MedicationPreferencesUtil.getDefaultRoundsIntervalDto(),
        new DateTime(2014, 2, 4, 12, 0),
        true,
        null);

    assertEquals(1, tasks.size());
    assertTaskRequest(tasks.get(0), 1L, new DateTime(2014, 2, 4, 12, 0), 500.0, "mg", null, null);
  }

  @Test
  public void testCreateTasks7() //simple variable therapy, 3x per day
  {
    final MedicationOrderComposition composition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", new DateTime(2014, 2, 4, 13, 0), null);
    final MedicationInstructionInstruction instruction =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction");

    //9:00 - 500mg
    final OrderActivity orderActivity1 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Daleron", 2L, 500.0, "mg", null, null, null, null, "O", "ORAL", "1", "TBL");
    instruction.getOrder().add(orderActivity1);
    orderActivity1.setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 4, 13, 0), Intervals.INFINITE.getEnd(), null));
    final DvTime dvTime1 = MedicationsTestUtils.buildDvTime(new HourMinuteDto(9, 0));
    orderActivity1.getMedicationTiming().getTiming().getTime().add(dvTime1);

    //13:00 - 1000mg
    final OrderActivity orderActivity2 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Daleron", 2L, 1000.0, "mg", null, null, null, null, "O", "ORAL", "1", "TBL");
    instruction.getOrder().add(orderActivity2);
    orderActivity2.setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 4, 13, 0), Intervals.INFINITE.getEnd(), null));
    final DvTime dvTime2 = MedicationsTestUtils.buildDvTime(new HourMinuteDto(13, 0));
    orderActivity2.getMedicationTiming().getTiming().getTime().add(dvTime2);

    //19:00 - 1500mg
    final OrderActivity orderActivity3 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Daleron", 2L, 1500.0, "mg", null, null, null, null, "O", "ORAL", "1", "TBL");
    instruction.getOrder().add(orderActivity3);
    orderActivity3.setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 4, 13, 0), Intervals.INFINITE.getEnd(), null));
    final DvTime dvTime3 = MedicationsTestUtils.buildDvTime(new HourMinuteDto(19, 0));
    orderActivity3.getMedicationTiming().getTiming().getTime().add(dvTime3);

    //rounds interval = 8:00 - 17:00
    final List<NewTaskRequestDto> tasks = therapyTaskCreator.createTasks(
        1L,
        Pair.of(composition, instruction),
        MedicationPreferencesUtil.getDefaultAdministrationTimingDto(),
        MedicationPreferencesUtil.getDefaultRoundsIntervalDto(),
        new DateTime(2014, 2, 4, 13, 0),
        true,
        null);

    assertEquals(4, tasks.size());
    assertTaskRequest(tasks.get(0), 1L, new DateTime(2014, 2, 4, 13, 0), 1000.0, "mg", null, null);
    assertTaskRequest(tasks.get(1), 1L, new DateTime(2014, 2, 4, 19, 0), 1500.0, "mg", null, null);
    assertTaskRequest(tasks.get(2), 1L, new DateTime(2014, 2, 5, 9, 0), 500.0, "mg", null, null);
    assertTaskRequest(tasks.get(3), 1L, new DateTime(2014, 2, 5, 13, 0), 1000.0, "mg", null, null);
  }

  @Test
  public void testCreateTasks8() //complex constant therapy, 3X per day
  {
    final MedicationOrderComposition composition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", new DateTime(2014, 2, 4, 12, 0), null);
    final MedicationInstructionInstruction instruction =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction");
    final OrderActivity orderActivity = new OrderActivity();
    instruction.getOrder().add(orderActivity);
    orderActivity.setMedicine(DataValueUtils.getText("Dopamin 50 mg / 200 ml, IV"));
    orderActivity.setIngredientsAndForm(new IngredientsAndFormCluster());
    orderActivity.getIngredientsAndForm().getIngredient().add(new IngredientsAndFormCluster.IngredientCluster());
    orderActivity.setAdministrationDetails(new AdministrationDetailsCluster());
    orderActivity.getAdministrationDetails().getInfusionAdministrationDetails().add(
        MedicationsTestUtils.buildInfusionAdministrationDetails(20.0, "ml/h"));
    orderActivity.setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 4, 12, 0),
            Intervals.INFINITE.getEnd(), null)); //4.2.2014 was Tuesday
    // 3X = 8:00, 13:00, 20:00
    //rounds interval = 8:00 - 17:00
    final List<NewTaskRequestDto> tasks = therapyTaskCreator.createTasks(
        1L,
        Pair.of(composition, instruction),
        MedicationPreferencesUtil.getDefaultAdministrationTimingDto(),
        MedicationPreferencesUtil.getDefaultRoundsIntervalDto(),
        new DateTime(2014, 2, 4, 12, 0),
        true,
        null);

    assertEquals(5, tasks.size());
    assertTaskRequest(tasks.get(0), 1L, new DateTime(2014, 2, 4, 12, 0), 20.0, "ml/h", null, null);
    assertTaskRequest(tasks.get(1), 1L, new DateTime(2014, 2, 4, 13, 0), 20.0, "ml/h", null, null);
    assertTaskRequest(tasks.get(2), 1L, new DateTime(2014, 2, 4, 20, 0), 20.0, "ml/h", null, null);
    assertTaskRequest(tasks.get(3), 1L, new DateTime(2014, 2, 5, 8, 0), 20.0, "ml/h", null, null);
    assertTaskRequest(tasks.get(4), 1L, new DateTime(2014, 2, 5, 13, 0), 20.0, "ml/h", null, null);
  }

  @Test
  public void testCreateTasks9() //continuous infusion
  {
    final MedicationOrderComposition composition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", new DateTime(2014, 2, 4, 12, 0), null);
    final MedicationInstructionInstruction instruction =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction");
    final OrderActivity orderActivity = new OrderActivity();
    instruction.getOrder().add(orderActivity);
    orderActivity.setMedicine(DataValueUtils.getText("Dopamin IV"));
    orderActivity.setIngredientsAndForm(new IngredientsAndFormCluster());
    orderActivity.getIngredientsAndForm().getIngredient().add(new IngredientsAndFormCluster.IngredientCluster());
    orderActivity.setAdministrationDetails(new AdministrationDetailsCluster());
    orderActivity.getAdministrationDetails().getInfusionAdministrationDetails().add(
        MedicationsTestUtils.buildInfusionAdministrationDetails(20.0, "ml/h"));
    orderActivity.getAdministrationDetails().setDeliveryMethod(
        DataValueUtils.getText(
            MedicationDeliveryMethodEnum.getFullString(MedicationDeliveryMethodEnum.CONTINUOUS_INFUSION)));
    orderActivity.setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            null, null, null, null, new DateTime(2014, 2, 4, 12, 0),
            Intervals.INFINITE.getEnd(), null)); //4.2.2014 was Tuesday
    //rounds interval = 8:00 - 17:00
    final List<NewTaskRequestDto> tasks = therapyTaskCreator.createTasks(
        1L,
        Pair.of(composition, instruction),
        MedicationPreferencesUtil.getDefaultAdministrationTimingDto(),
        MedicationPreferencesUtil.getDefaultRoundsIntervalDto(),
        new DateTime(2014, 2, 4, 12, 0),
        true,
        null);

    assertEquals(1, tasks.size());
    assertTaskRequest(tasks.get(0), 1L, new DateTime(2014, 2, 4, 12, 0), 20.0, "ml/h", null, null);
  }

  @Test
  public void testCreateTasks10() //continuous infusion that ends today
  {
    final MedicationOrderComposition composition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", new DateTime(2014, 2, 4, 12, 0), null);
    final MedicationInstructionInstruction instruction =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction");
    final OrderActivity orderActivity = new OrderActivity();
    instruction.getOrder().add(orderActivity);
    orderActivity.setMedicine(DataValueUtils.getText("Dopamin IV"));
    orderActivity.setIngredientsAndForm(new IngredientsAndFormCluster());
    orderActivity.getIngredientsAndForm().getIngredient().add(new IngredientsAndFormCluster.IngredientCluster());
    orderActivity.setAdministrationDetails(new AdministrationDetailsCluster());
    orderActivity.getAdministrationDetails().setDeliveryMethod(
        DataValueUtils.getText(
            MedicationDeliveryMethodEnum.getFullString(MedicationDeliveryMethodEnum.CONTINUOUS_INFUSION)));
    orderActivity.getAdministrationDetails().getInfusionAdministrationDetails().add(new InfusionAdministrationDetailsCluster());
    orderActivity.setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            null, null, null, null, new DateTime(2014, 2, 4, 12, 0),
            new DateTime(2014, 2, 4, 23, 0), null)); //4.2.2014 was Tuesday
    //rounds interval = 8:00 - 17:00
    final List<NewTaskRequestDto> tasks = therapyTaskCreator.createTasks(
        1L,
        Pair.of(composition, instruction),
        MedicationPreferencesUtil.getDefaultAdministrationTimingDto(),
        MedicationPreferencesUtil.getDefaultRoundsIntervalDto(),
        new DateTime(2014, 2, 4, 12, 0),
        true,
        null);

    assertEquals(2, tasks.size());
    assertTaskRequest(tasks.get(0), 1L, new DateTime(2014, 2, 4, 12, 0), null, null, null, null);
    assertTaskRequest(tasks.get(1), 1L, new DateTime(2014, 2, 4, 23, 0), null, null, null, null);
  }

  @Test
  public void testCreateTasks11() //continuous infusion with various rate
  {
    final MedicationOrderComposition composition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", new DateTime(2014, 2, 4, 12, 0), null);
    final MedicationInstructionInstruction instruction =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction");

    // 20
    final OrderActivity orderActivity = new OrderActivity();
    instruction.getOrder().add(orderActivity);
    orderActivity.setMedicine(DataValueUtils.getText("Dopamin IV"));
    orderActivity.setIngredientsAndForm(new IngredientsAndFormCluster());
    orderActivity.getIngredientsAndForm().getIngredient().add(new IngredientsAndFormCluster.IngredientCluster());
    orderActivity.setAdministrationDetails(new AdministrationDetailsCluster());
    orderActivity.getAdministrationDetails().setDeliveryMethod(
        DataValueUtils.getText(
            MedicationDeliveryMethodEnum.getFullString(MedicationDeliveryMethodEnum.CONTINUOUS_INFUSION)));
    orderActivity.getAdministrationDetails().getInfusionAdministrationDetails().add(new InfusionAdministrationDetailsCluster());
    orderActivity.setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            null, null, null, null, new DateTime(2014, 2, 4, 12, 0),
            new DateTime(2014, 2, 4, 23, 0), null)); //4.2.2014 was Tuesday
    //rounds interval = 8:00 - 17:00
    final List<NewTaskRequestDto> tasks = therapyTaskCreator.createTasks(
        1L,
        Pair.of(composition, instruction),
        MedicationPreferencesUtil.getDefaultAdministrationTimingDto(),
        MedicationPreferencesUtil.getDefaultRoundsIntervalDto(),
        new DateTime(2014, 2, 4, 12, 0),
        true,
        null);

    assertEquals(2, tasks.size());
    assertTaskRequest(tasks.get(0), 1L, new DateTime(2014, 2, 4, 12, 0), null, null, null, null);
    assertTaskRequest(tasks.get(1), 1L, new DateTime(2014, 2, 4, 23, 0), null, null, null, null);
  }

  @Test
  public void testCreateTasks12() //simple constant therapy, 1X per day
  {
    final MedicationOrderComposition composition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", new DateTime(2014, 2, 4, 12, 0), null);
    final MedicationInstructionInstruction instruction =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction");
    final OrderActivity orderActivity =
        MedicationsTestUtils.buildTestOrderActivity(
            "Daleron", 2L, 500.0, "mg", null, null, null, null, "O", "ORAL", "1", "TBL");
    instruction.getOrder().add(orderActivity);
    orderActivity.setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            1L, null, null, null, new DateTime(2014, 2, 4, 12, 0),
            Intervals.INFINITE.getEnd(), null)); //4.2.2014 was Tuesday
    //rounds interval = 8:00 - 17:00
    final List<NewTaskRequestDto> tasks = therapyTaskCreator.createTasks(
        1L,
        Pair.of(composition, instruction),
        MedicationPreferencesUtil.getDefaultAdministrationTimingDto(),
        MedicationPreferencesUtil.getDefaultRoundsIntervalDto(),
        new DateTime(2014, 2, 4, 12, 0),
        true,
        null);

    assertEquals(2, tasks.size());
    assertTaskRequest(tasks.get(0), 1L, new DateTime(2014, 2, 4, 12, 0), 500.0, "mg", null, null);
    assertTaskRequest(tasks.get(1), 1L, new DateTime(2014, 2, 5, 12, 0), 500.0, "mg", null, null);
  }

  @Test
  public void testCreateTasksOnReview1() //review in rounds
  {
    final MedicationOrderComposition composition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", new DateTime(2014, 2, 4, 12, 0), null);
    final MedicationInstructionInstruction instruction =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction");
    final OrderActivity orderActivity =
        MedicationsTestUtils.buildTestOrderActivity(
            "Daleron", 2L, 500.0, "mg", null, null, null, null, "O", "ORAL", "1", "TBL");
    instruction.getOrder().add(orderActivity);
    orderActivity.setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 4, 12, 0),
            Intervals.INFINITE.getEnd(), null)); //4.2.2014 was Tuesday
    // 3X = 8:00, 13:00, 20:00
    //rounds interval = 8:00 - 17:00
    final List<NewTaskRequestDto> tasks = therapyTaskCreator.createTasks(
        1L,
        Pair.of(composition, instruction),
        MedicationPreferencesUtil.getDefaultAdministrationTimingDto(),
        MedicationPreferencesUtil.getDefaultRoundsIntervalDto(),
        new DateTime(2014, 2, 5, 10, 0),
        false,
        new DateTime(2014, 2, 5, 13, 0));

    assertEquals(3, tasks.size());
    assertTaskRequest(tasks.get(0), 1L, new DateTime(2014, 2, 5, 20, 0), 500.0, "mg", null, null);
    assertTaskRequest(tasks.get(1), 1L, new DateTime(2014, 2, 6, 8, 0), 500.0, "mg", null, null);
    assertTaskRequest(tasks.get(2), 1L, new DateTime(2014, 2, 6, 13, 0), 500.0, "mg", null, null);
  }

  @Test
  public void testCreateTasksOnReview2() //review after rounds
  {
    final MedicationOrderComposition composition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", new DateTime(2014, 2, 4, 12, 0), null);
    final MedicationInstructionInstruction instruction =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction");
    final OrderActivity orderActivity =
        MedicationsTestUtils.buildTestOrderActivity(
            "Daleron", 2L, 500.0, "mg", null, null, null, null, "O", "ORAL", "1", "TBL");
    instruction.getOrder().add(orderActivity);
    orderActivity.setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2014, 2, 4, 12, 0),
            Intervals.INFINITE.getEnd(), null)); //4.2.2014 was Tuesday
    // 3X = 8:00, 13:00, 20:00
    //rounds interval = 8:00 - 17:00
    final List<NewTaskRequestDto> tasks = therapyTaskCreator.createTasks(
        1L,
        Pair.of(composition, instruction),
        MedicationPreferencesUtil.getDefaultAdministrationTimingDto(),
        MedicationPreferencesUtil.getDefaultRoundsIntervalDto(),
        new DateTime(2014, 2, 5, 21, 0),
        false,
        new DateTime(2014, 2, 5, 13, 0));

    assertEquals(2, tasks.size());
    assertTaskRequest(tasks.get(0), 1L, new DateTime(2014, 2, 6, 8, 0), 500.0, "mg", null, null);
    assertTaskRequest(tasks.get(1), 1L, new DateTime(2014, 2, 6, 13, 0), 500.0, "mg", null, null);
  }

  @Test
  public void testCreateTasksOnReview3() //review continuous infusion
  {
    final MedicationOrderComposition composition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", new DateTime(2014, 2, 4, 12, 0), null);
    final MedicationInstructionInstruction instruction =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction");
    final OrderActivity orderActivity = new OrderActivity();
    instruction.getOrder().add(orderActivity);
    orderActivity.setMedicine(DataValueUtils.getText("Dopamin IV"));
    orderActivity.setIngredientsAndForm(new IngredientsAndFormCluster());
    orderActivity.getIngredientsAndForm().getIngredient().add(new IngredientsAndFormCluster.IngredientCluster());
    orderActivity.setAdministrationDetails(new AdministrationDetailsCluster());
    orderActivity.getAdministrationDetails().getInfusionAdministrationDetails().add(
        MedicationsTestUtils.buildInfusionAdministrationDetails(20.0, "ml/h"));
    orderActivity.getAdministrationDetails().setDeliveryMethod(
        DataValueUtils.getText(
            MedicationDeliveryMethodEnum.getFullString(MedicationDeliveryMethodEnum.CONTINUOUS_INFUSION)));
    orderActivity.setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            null, null, null, null, new DateTime(2014, 2, 4, 9, 0),
            Intervals.INFINITE.getEnd(), null)); //4.2.2014 was Tuesday
    //rounds interval = 8:00 - 17:00
    final List<NewTaskRequestDto> tasks = therapyTaskCreator.createTasks(
        1L,
        Pair.of(composition, instruction),
        MedicationPreferencesUtil.getDefaultAdministrationTimingDto(),
        MedicationPreferencesUtil.getDefaultRoundsIntervalDto(),
        new DateTime(2014, 2, 5, 12, 0),
        false,
        null);

    assertEquals(0, tasks.size());
  }

  @Test
  public void testCreateTasksOnReview4() //review variable continuous infusion
  {
    final MedicationOrderComposition composition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", new DateTime(2014, 2, 4, 12, 0), null);
    final MedicationInstructionInstruction instruction =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction");

    //12:00-13:00 -> 10 ml/h
    final OrderActivity orderActivity1 = new OrderActivity();
    instruction.getOrder().add(orderActivity1);
    orderActivity1.setMedicine(DataValueUtils.getText("Dopamin IV"));
    orderActivity1.setIngredientsAndForm(new IngredientsAndFormCluster());
    orderActivity1.getIngredientsAndForm().getIngredient().add(new IngredientsAndFormCluster.IngredientCluster());
    orderActivity1.setAdministrationDetails(new AdministrationDetailsCluster());
    orderActivity1.getAdministrationDetails().getInfusionAdministrationDetails().add(
        MedicationsTestUtils.buildInfusionAdministrationDetails(10.0, "ml/h"));
    orderActivity1.getAdministrationDetails().setDeliveryMethod(
        DataValueUtils.getText(
            MedicationDeliveryMethodEnum.getFullString(MedicationDeliveryMethodEnum.CONTINUOUS_INFUSION)));
    orderActivity1.setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            null, null, null, null, new DateTime(2014, 2, 4, 12, 0),
            Intervals.INFINITE.getEnd(), null)); //4.2.2014 was Tuesday
    final DvTime dvTime1 = new DvTime();
    dvTime1.setValue(ISODateTimeFormat.time().print(new DateTime(1800, 1, 1, 12, 0)));
    orderActivity1.getMedicationTiming().getTiming().getTime().add(dvTime1);
    //13:00-14:00 -> 20 ml/h
    final OrderActivity orderActivity2 = new OrderActivity();
    instruction.getOrder().add(orderActivity2);
    orderActivity2.setMedicine(DataValueUtils.getText("Dopamin IV"));
    orderActivity2.setIngredientsAndForm(new IngredientsAndFormCluster());
    orderActivity2.getIngredientsAndForm().getIngredient().add(new IngredientsAndFormCluster.IngredientCluster());
    orderActivity2.setAdministrationDetails(new AdministrationDetailsCluster());
    orderActivity2.getAdministrationDetails().getInfusionAdministrationDetails().add(
        MedicationsTestUtils.buildInfusionAdministrationDetails(20.0, "ml/h"));
    orderActivity2.getAdministrationDetails().setDeliveryMethod(
        DataValueUtils.getText(
            MedicationDeliveryMethodEnum.getFullString(MedicationDeliveryMethodEnum.CONTINUOUS_INFUSION)));
    orderActivity2.setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            null, null, null, null, new DateTime(2014, 2, 4, 12, 0),
            Intervals.INFINITE.getEnd(), null)); //4.2.2014 was Tuesday
    final DvTime dvTime2 = new DvTime();
    dvTime2.setValue(ISODateTimeFormat.time().print(new DateTime(1800, 1, 1, 13, 0)));
    orderActivity2.getMedicationTiming().getTiming().getTime().add(dvTime2);
    //rounds interval = 8:00 - 17:00
    final List<NewTaskRequestDto> tasks = therapyTaskCreator.createTasks(
        1L,
        Pair.of(composition, instruction),
        MedicationPreferencesUtil.getDefaultAdministrationTimingDto(),
        MedicationPreferencesUtil.getDefaultRoundsIntervalDto(),
        new DateTime(2014, 2, 5, 11, 0),
        false,
        null);

    assertEquals(0, tasks.size());
  }


  private void assertTaskRequest(
      final NewTaskRequestDto taskRequest,
      final long patientId,
      final DateTime time,
      final Double numerator,
      final String numeratorUnit,
      final Double denominator,
      final String denominatorUnit)
  {
    assertEquals(patientId, getTaskVariableValue(taskRequest, MedicationTaskDef.PATIENT_ID));
    assertEquals(time, taskRequest.getDue());
    assertEquals(numerator, getTaskVariableValue(taskRequest, MedicationTaskDef.DOSE_NUMERATOR));
    assertEquals(numeratorUnit, getTaskVariableValue(taskRequest, MedicationTaskDef.DOSE_NUMERATOR_UNIT));
    assertEquals(denominator, getTaskVariableValue(taskRequest, MedicationTaskDef.DOSE_DENOMINATOR));
    assertEquals(denominatorUnit, getTaskVariableValue(taskRequest, MedicationTaskDef.DOSE_DENOMINATOR_UNIT));
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
