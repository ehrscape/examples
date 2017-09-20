package com.marand.thinkmed.medications.connector.impl.provider;

import java.util.List;
import javax.annotation.Nonnull;

import com.marand.thinkmed.medications.connector.data.object.DiseaseDto;

/**
 * @author Mitja Lapajne
 */
public interface DiseasesProvider
{
  List<DiseaseDto> getPatientDiseases(@Nonnull String patientId);
}
