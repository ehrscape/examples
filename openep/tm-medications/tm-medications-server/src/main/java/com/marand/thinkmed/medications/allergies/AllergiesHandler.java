package com.marand.thinkmed.medications.allergies;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;

import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationsWarningDto;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public interface AllergiesHandler
{
  void handleNewAllergies(
      @Nonnull String patientId,
      @Nonnull PatientDataForMedicationsDto patientData,
      @Nonnull Collection<NamedExternalDto> newAllergies,
      @Nonnull DateTime when);

  List<MedicationsWarningDto> getAllergyWarnings(
      @Nonnull String patientId,
      @Nonnull PatientDataForMedicationsDto patientData,
      @Nonnull Collection<NamedExternalDto> allergies,
      @Nonnull DateTime when);
}
