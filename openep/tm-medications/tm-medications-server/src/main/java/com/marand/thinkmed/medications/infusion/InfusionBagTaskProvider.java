package com.marand.thinkmed.medications.infusion;

import java.util.List;
import javax.annotation.Nonnull;

import com.marand.thinkmed.medications.dto.administration.InfusionBagTaskDto;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * @author Nejc Korasa
 */
public interface InfusionBagTaskProvider
{
  List<InfusionBagTaskDto> findInfusionBagTasks(
      @Nonnull String patientId,
      @Nonnull List<String> therapyIds,
      Interval searchInterval);

  List<TaskDto> findTasks(
      @Nonnull List<String> taskKeys,
      @Nonnull List<String> therapyIds,
      boolean historic,
      DateTime taskDueAfter,
      DateTime taskDueBefore);
}
