package com.marand.thinkmed.fdb.dto;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
public class FdbScreeningResultDto implements JsonSerializable
{
  private List<FdbWarningDto> DrugInteractions = new ArrayList<>();
  private List<FdbDrugSensitivityWarningDto> DrugSensitivities = new ArrayList<>();
  private List<FdbWarningDto> DuplicateTherapies = new ArrayList<>();
  private List<FdbWarningDto> DrugDoublings = new ArrayList<>();
  private List<FdbWarningDto> DrugEquivalences = new ArrayList<>();
  private List<FdbPatientChecksWarningDto> PatientChecks = new ArrayList<>();

  public List<FdbWarningDto> getDrugInteractions()
  {
    return DrugInteractions;
  }

  public void setDrugInteractions(final List<FdbWarningDto> drugInteractions)
  {
    DrugInteractions = drugInteractions;
  }

  public List<FdbDrugSensitivityWarningDto> getDrugSensitivities()
  {
    return DrugSensitivities;
  }

  public void setDrugSensitivities(final List<FdbDrugSensitivityWarningDto> drugSensitivities)
  {
    DrugSensitivities = drugSensitivities;
  }

  public List<FdbWarningDto> getDuplicateTherapies()
  {
    return DuplicateTherapies;
  }

  public void setDuplicateTherapies(final List<FdbWarningDto> duplicateTherapies)
  {
    DuplicateTherapies = duplicateTherapies;
  }

  public List<FdbWarningDto> getDrugDoublings()
  {
    return DrugDoublings;
  }

  public void setDrugDoublings(final List<FdbWarningDto> drugDoublings)
  {
    DrugDoublings = drugDoublings;
  }

  public List<FdbWarningDto> getDrugEquivalences()
  {
    return DrugEquivalences;
  }

  public void setDrugEquivalences(final List<FdbWarningDto> drugEquivalences)
  {
    DrugEquivalences = drugEquivalences;
  }

  public List<FdbPatientChecksWarningDto> getPatientChecks()
  {
    return PatientChecks;
  }

  public void setPatientChecks(final List<FdbPatientChecksWarningDto> patientChecks)
  {
    PatientChecks = patientChecks;
  }
}
