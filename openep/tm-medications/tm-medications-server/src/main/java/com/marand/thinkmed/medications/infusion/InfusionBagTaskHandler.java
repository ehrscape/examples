package com.marand.thinkmed.medications.infusion;

import java.util.List;
import javax.annotation.Nonnull;

import com.marand.thinkmed.medications.dto.administration.InfusionBagTaskDto;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public interface InfusionBagTaskHandler
{
  InfusionBagTaskDto convertTaskToInfusionBagTask(@Nonnull TaskDto task);

  void deleteInfusionBagTasks(
      @Nonnull String patientId,
      @Nonnull List<String> therapyIds,
      String comment);

  void createInfusionBagTask(
      @Nonnull String patientId,
      @Nonnull String therapyId,
      @Nonnull DateTime plannedInfusionBagChange);
}
