package com.marand.thinkmed.medications.connector.impl.provider;

import javax.annotation.Nonnull;

import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.connector.data.object.ObservationDto;

/**
 * @author Mitja Lapajne
 */
public interface WeightProvider
{
  Opt<ObservationDto> getPatientWeight(@Nonnull String patientsId);
}
