package com.marand.thinkmed.medications.automatic;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.thinkmed.medications.dto.AutomaticAdministrationTaskCreatorDto;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public interface AdministrationAutoTaskCreatorHandler
{
  void createAdministrationTasksOnAutoCreate(
      @Nonnull AutomaticAdministrationTaskCreatorDto automaticAdministrationTaskCreatorDto,
      @Nonnull DateTime actionTimestamp);

  List<AutomaticAdministrationTaskCreatorDto> getAutoAdministrationTaskCreatorDtos(
      @Nonnull DateTime when,
      @Nonnull Map<MedicationOrderComposition, String> activeInstructionsWithPatientIds);
}
