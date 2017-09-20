package com.marand.thinkmed.medications.automatic;

import javax.annotation.Nonnull;

import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.thinkmed.medications.charting.AutomaticChartingType;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public interface AdministrationAutoTaskConfirmerHandler
{
  void autoConfirmAdministrationTask(
      @Nonnull AutomaticChartingType type,
      @Nonnull String patientId,
      @Nonnull MedicationOrderComposition composition,
      @Nonnull TaskDto administrationTask,
      @Nonnull DateTime when);
}
