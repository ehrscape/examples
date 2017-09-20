package com.marand.thinkmed.medications.administration.impl;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.Pair;
import com.marand.openehr.medications.tdo.MedicationAdministrationComposition;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.thinkmed.medications.administration.AdministrationFromEhrConverter;
import com.marand.thinkmed.medications.administration.AdministrationProvider;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Nejc Korasa
 */
public class AdministrationProviderImpl implements AdministrationProvider
{
  private AdministrationFromEhrConverter administrationFromEhrConverter;
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Required
  public void setAdministrationFromEhrConverter(final AdministrationFromEhrConverter administrationFromEhrConverter)
  {
    this.administrationFromEhrConverter = administrationFromEhrConverter;
  }

  @Required
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Override
  public List<AdministrationDto> getTherapiesAdministrations(
      @Nonnull final String patientId,
      @Nonnull final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs,
      final Interval searchInterval)
  {
    Preconditions.checkNotNull(patientId, "patientId must not be null");
    Preconditions.checkNotNull(instructionPairs, "instructionPairs must not be null");

    final Map<String, List<MedicationAdministrationComposition>> therapiesAdministrations = medicationsOpenEhrDao.getTherapiesAdministrations(
        patientId,
        instructionPairs,
        searchInterval);

    return administrationFromEhrConverter.convertToAdministrationDtos(therapiesAdministrations, instructionPairs);
  }
}
