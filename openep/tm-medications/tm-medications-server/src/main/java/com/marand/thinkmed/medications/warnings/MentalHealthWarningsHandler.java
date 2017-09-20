package com.marand.thinkmed.medications.warnings;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nonnull;

import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthAllowedMedicationsDo;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationForWarningsSearchDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationsWarningDto;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
public interface MentalHealthWarningsHandler
{
  List<MedicationsWarningDto> getMentalHealthMedicationsWarnings(
      @Nonnull String patientId,
      @Nonnull Collection<MedicationForWarningsSearchDto> medicationsForWarnings,
      @Nonnull DateTime when);

  MedicationsWarningDto buildMentalHealthMedicationsWarning(@Nonnull NamedExternalDto medication);

  MentalHealthAllowedMedicationsDo getAllowedMedications(@Nonnull MentalHealthDocumentDto mentalHealthDocumentDto);

  Predicate<MedicationDto> isMedicationWithRoutesAllowed(
      @Nonnull Collection<Long> routeIds,
      @Nonnull MentalHealthAllowedMedicationsDo allowedMedications);
}
