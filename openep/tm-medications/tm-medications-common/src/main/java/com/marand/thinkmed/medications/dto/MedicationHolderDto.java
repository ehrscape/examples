/*
 * Copyright (c) 2010-2014 Marand d.o.o. (www.marand.com)
 *
 * This file is part of Think!Med Clinical Medication Management.
 *
 * Think!Med Clinical Medication Management is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Think!Med Clinical Medication Management is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Think!Med Clinical Medication Management.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.marand.thinkmed.medications.dto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.marand.maf.core.Pair;
import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.MedicationTypeEnum;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class MedicationHolderDto extends DataTransferObject implements JsonSerializable
{
  private Long id;
  private String name;
  private String shortName;
  private String genericName;
  private boolean active;
  private boolean orderable;
  private String atcGroupCode;
  private String atcGroupName;
  private DoseFormDto doseFormDto;
  private boolean antibiotic;
  private boolean suggestSwitchToOral;
  private boolean reviewReminder;
  private boolean mentalHealthDrug;
  private boolean inpatientMedication;
  private boolean outpatientMedication;
  private MedicationTypeEnum medicationType;

  private MedicationLevelEnum medicationLevel;
  private Long vtmId;
  private Long vmpId;
  private Long ampId;

  private MedicationIngredientDto definingIngredient;

  private Map<String, Pair<String, Integer>> customGroupNameSortOrder = new HashMap<>();
  private Set<MedicationRuleEnum> medicationRules = new HashSet<>();

  private boolean formulary;
  private Set<String> formularyCareProviders = new HashSet<>();

  public Long getId()
  {
    return id;
  }

  public void setId(final Long id)
  {
    this.id = id;
  }

  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }

  public String getShortName()
  {
    return shortName;
  }

  public void setShortName(final String shortName)
  {
    this.shortName = shortName;
  }

  public String getGenericName()
  {
    return genericName;
  }

  public void setGenericName(final String genericName)
  {
    this.genericName = genericName;
  }

  public boolean isActive()
  {
    return active;
  }

  public void setActive(final boolean active)
  {
    this.active = active;
  }

  public boolean isOrderable()
  {
    return orderable;
  }

  public void setOrderable(final boolean orderable)
  {
    this.orderable = orderable;
  }

  public String getAtcGroupCode()
  {
    return atcGroupCode;
  }

  public void setAtcGroupCode(final String atcGroupCode)
  {
    this.atcGroupCode = atcGroupCode;
  }

  public String getAtcGroupName()
  {
    return atcGroupName;
  }

  public void setAtcGroupName(final String atcGroupName)
  {
    this.atcGroupName = atcGroupName;
  }

  public boolean isAntibiotic()
  {
    return antibiotic;
  }

  public void setAntibiotic(final boolean antibiotic)
  {
    this.antibiotic = antibiotic;
  }

  public boolean isSuggestSwitchToOral()
  {
    return suggestSwitchToOral;
  }

  public void setSuggestSwitchToOral(final boolean suggestSwitchToOral)
  {
    this.suggestSwitchToOral = suggestSwitchToOral;
  }

  public boolean isReviewReminder()
  {
    return reviewReminder;
  }

  public void setReviewReminder(final boolean reviewReminder)
  {
    this.reviewReminder = reviewReminder;
  }

  public boolean isMentalHealthDrug()
  {
    return mentalHealthDrug;
  }

  public void setMentalHealthDrug(final boolean mentalHealthDrug)
  {
    this.mentalHealthDrug = mentalHealthDrug;
  }

  public boolean isInpatientMedication()
  {
    return inpatientMedication;
  }

  public void setInpatientMedication(final boolean inpatientMedication)
  {
    this.inpatientMedication = inpatientMedication;
  }

  public boolean isOutpatientMedication()
  {
    return outpatientMedication;
  }

  public void setOutpatientMedication(final boolean outpatientMedication)
  {
    this.outpatientMedication = outpatientMedication;
  }

  public DoseFormDto getDoseFormDto()
  {
    return doseFormDto;
  }

  public void setDoseFormDto(final DoseFormDto doseFormDto)
  {
    this.doseFormDto = doseFormDto;
  }

  public MedicationTypeEnum getMedicationType()
  {
    return medicationType;
  }

  public void setMedicationType(final MedicationTypeEnum medicationType)
  {
    this.medicationType = medicationType;
  }

  public MedicationLevelEnum getMedicationLevel()
  {
    return medicationLevel;
  }

  public void setMedicationLevel(final MedicationLevelEnum medicationLevel)
  {
    this.medicationLevel = medicationLevel;
  }

  public Long getVtmId()
  {
    return vtmId;
  }

  public void setVtmId(final Long vtmId)
  {
    this.vtmId = vtmId;
  }

  public Long getVmpId()
  {
    return vmpId;
  }

  public void setVmpId(final Long vmpId)
  {
    this.vmpId = vmpId;
  }

  public Long getAmpId()
  {
    return ampId;
  }

  public void setAmpId(final Long ampId)
  {
    this.ampId = ampId;
  }

  public MedicationIngredientDto getDefiningIngredient()
  {
    return definingIngredient;
  }

  public void setDefiningIngredient(final MedicationIngredientDto definingIngredient)
  {
    this.definingIngredient = definingIngredient;
  }

  public Map<String, Pair<String, Integer>> getCustomGroupNameSortOrder()
  {
    return customGroupNameSortOrder;
  }

  public void setCustomGroupNameSortOrder(final Map<String, Pair<String, Integer>> customGroupNameSortOrder)
  {
    this.customGroupNameSortOrder = customGroupNameSortOrder;
  }

  public Set<MedicationRuleEnum> getMedicationRules()
  {
    return medicationRules;
  }

  public void setMedicationRules(final Set<MedicationRuleEnum> medicationRules)
  {
    this.medicationRules = medicationRules;
  }

  public boolean isFormulary()
  {
    return formulary;
  }

  public void setFormulary(final boolean formulary)
  {
    this.formulary = formulary;
  }

  public Set<String> getFormularyCareProviders()
  {
    return formularyCareProviders;
  }

  public void setFormularyCareProviders(final Set<String> formularyCareProviders)
  {
    this.formularyCareProviders = formularyCareProviders;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("id", id)
        .append("name", name)
        .append("shortName", shortName)
        .append("genericName", genericName)
        .append("active", active)
        .append("orderable", orderable)
        .append("atcGroupCode", atcGroupCode)
        .append("atcGroupName", atcGroupName)
        .append("antibiotic", antibiotic)
        .append("suggestSwitchToOral", suggestSwitchToOral)
        .append("mentalHealthDrug", mentalHealthDrug)
        .append("reviewReminder", reviewReminder)
        .append("doseFormDto", doseFormDto)
        .append("outpatientMedication", outpatientMedication)
        .append("inpatientMedication", inpatientMedication)
        .append("medicationType", medicationType)
        .append("medicationLevel", medicationLevel)
        .append("vtmId", vtmId)
        .append("vmpId", vmpId)
        .append("ampId", ampId)
        .append("medicationRules", medicationRules)
        .append("formulary", formulary)
        .append("formularyCareProviders", formularyCareProviders)
    ;
  }
}
