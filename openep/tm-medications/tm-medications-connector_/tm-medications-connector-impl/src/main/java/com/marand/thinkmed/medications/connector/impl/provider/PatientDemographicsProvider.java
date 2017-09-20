package com.marand.thinkmed.medications.connector.impl.provider;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;

import com.marand.thinkmed.medications.connector.data.object.PatientDemographicsDto;

/**
 * @author Mitja Lapajne
 */
public interface PatientDemographicsProvider
{
  List<PatientDemographicsDto> getPatientsDemographics(@Nonnull Collection<String> patientsIds);
}
