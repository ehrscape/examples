package com.marand.thinkmed.fdb.dto;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
public class FdbScreeningDto implements JsonSerializable
{
  private List<Integer> ScreeningModules = new ArrayList<>();
  private FdbPatientDto PatientInformation;
  private List<FdbTerminologyDto> Conditions = new ArrayList<>();
  private List<FdbTerminologyWithConceptDto> Allergens = new ArrayList<>();
  private List<FdbTerminologyWithConceptDto> CurrentDrugs = new ArrayList<>();
  private List<FdbTerminologyWithConceptDto> ProspectiveDrugs = new ArrayList<>();
  private boolean CheckAllDrugs;
  private boolean ValidateInput;
  private FdbNameValue MinimumConditionAlertSeverity;
  private FdbNameValue MinimumInteractionAlertSeverity;

  public List<Integer> getScreeningModules()
  {
    return ScreeningModules;
  }

  public void setScreeningModules(final List<Integer> ScreeningModules)
  {
    this.ScreeningModules = ScreeningModules;
  }

  public FdbPatientDto getPatientInformation()
  {
    return PatientInformation;
  }

  public void setPatientInformation(final FdbPatientDto PatientInformation)
  {
    this.PatientInformation = PatientInformation;
  }

  public List<FdbTerminologyDto> getConditions()
  {
    return Conditions;
  }

  public void setConditions(final List<FdbTerminologyDto> conditions)
  {
    Conditions = conditions;
  }

  public List<FdbTerminologyWithConceptDto> getAllergens()
  {
    return Allergens;
  }

  public void setAllergens(final List<FdbTerminologyWithConceptDto> allergens)
  {
    Allergens = allergens;
  }

  public List<FdbTerminologyWithConceptDto> getCurrentDrugs()
  {
    return CurrentDrugs;
  }

  public void setCurrentDrugs(final List<FdbTerminologyWithConceptDto> CurrentDrugs)
  {
    this.CurrentDrugs = CurrentDrugs;
  }

  public List<FdbTerminologyWithConceptDto> getProspectiveDrugs()
  {
    return ProspectiveDrugs;
  }

  public void setProspectiveDrugs(final List<FdbTerminologyWithConceptDto> ProspectiveDrugs)
  {
    this.ProspectiveDrugs = ProspectiveDrugs;
  }

  public boolean isCheckAllDrugs()
  {
    return CheckAllDrugs;
  }

  public void setCheckAllDrugs(final boolean CheckAllDrugs)
  {
    this.CheckAllDrugs = CheckAllDrugs;
  }

  public boolean isValidateInput()
  {
    return ValidateInput;
  }

  public void setValidateInput(final boolean ValidateInput)
  {
    this.ValidateInput = ValidateInput;
  }

  public FdbNameValue getMinimumConditionAlertSeverity()
  {
    return MinimumConditionAlertSeverity;
  }

  public void setMinimumConditionAlertSeverity(final FdbNameValue MinimumConditionAlertSeverity)
  {
    this.MinimumConditionAlertSeverity = MinimumConditionAlertSeverity;
  }

  public FdbNameValue getMinimumInteractionAlertSeverity()
  {
    return MinimumInteractionAlertSeverity;
  }

  public void setMinimumInteractionAlertSeverity(final FdbNameValue MinimumInteractionAlertSeverity)
  {
    this.MinimumInteractionAlertSeverity = MinimumInteractionAlertSeverity;
  }
}
