package com.marand.thinkmed.medications.connector.impl.provider;

import javax.annotation.Nonnull;

import com.marand.thinkmed.medications.connector.data.object.AllergiesDto;

/**
 * @author Mitja Lapajne
 */
public interface AllergiesProvider
{
  AllergiesDto getPatientAllergies(@Nonnull String patientId);
}
