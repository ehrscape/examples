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

package com.marand.thinkmed.medications.connector.data.object;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.api.externals.data.object.ExternalCatalogDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public final class PatientDataForTherapyReportDto extends DataTransferObject implements JsonSerializable
{
  private final boolean inpatient;
  private final String prescriptionDateTime;
  private final String patientName;
  private final String birthDateAndAge;
  private final String addressDisplay;
  private final Gender gender;
  private final String patientIdentificatorType;
  private final String patientIdentificator;
  private final String centralCaseIdNumber;
  private final String organization;
  private final String cureingCareProfessional;
  private final String supervisoryCareProfessional;
  private final String chargeNurse;
  private final String roomAndBed;
  private final String admissionDate;
  private final Integer hospitalizationConsecutiveDay;
  private final Integer operationDaysAfter;
  private List<ExternalCatalogDto> diseases = new ArrayList<>();
  private String weight;
  private List<String> allergies;
  private String careProviderId;

  private String currentUser;

  public PatientDataForTherapyReportDto(
      final boolean inpatient,
      final String prescriptionDateTime,
      final String patientName,
      final String birthDateAndAge,
      final Gender gender,
      final String patientIdentificatorType,
      final String patientIdentificator,
      final String centralCaseIdNumber,
      final String organization,
      final String roomAndBed,
      final String admissionDate,
      final Integer hospitalizationConsecutiveDay,
      final List<ExternalCatalogDto> diseases,
      final String weight,
      final String addressDisplay,
      final String cureingCareProfessional,
      final String supervisoryCareProfessional,
      final String chargeNurse,
      final Integer operationDaysAfter,
      final List<String> allergies,
      final String careProviderId)
  {
    this.inpatient = inpatient;
    this.prescriptionDateTime = prescriptionDateTime;
    this.patientName = patientName;
    this.birthDateAndAge = birthDateAndAge;
    this.gender = gender;
    this.patientIdentificatorType = patientIdentificatorType;
    this.patientIdentificator = patientIdentificator;
    this.centralCaseIdNumber = centralCaseIdNumber;
    this.organization = organization;
    this.roomAndBed = roomAndBed;
    this.admissionDate = admissionDate;
    this.hospitalizationConsecutiveDay = hospitalizationConsecutiveDay;
    this.diseases = diseases;
    this.weight = weight;
    this.addressDisplay = addressDisplay;
    this.cureingCareProfessional = cureingCareProfessional;
    this.supervisoryCareProfessional = supervisoryCareProfessional;
    this.chargeNurse = chargeNurse;
    this.operationDaysAfter = operationDaysAfter;
    this.allergies = allergies;
    this.careProviderId = careProviderId;
  }

  public boolean isInpatient()
  {
    return inpatient;
  }

  public String getPrescriptionDateTime()
  {
    return prescriptionDateTime;
  }

  public String getPatientName()
  {
    return patientName;
  }

  public String getBirthDateAndAge()
  {
    return birthDateAndAge;
  }

  public String getAddressDisplay()
  {
    return addressDisplay;
  }

  public Gender getGender()
  {
    return gender;
  }

  public String getPatientIdentificatorType()
  {
    return patientIdentificatorType;
  }

  public String getPatientIdentificator()
  {
    return patientIdentificator;
  }

  public String getCentralCaseIdNumber()
  {
    return centralCaseIdNumber;
  }

  public String getOrganization()
  {
    return organization;
  }

  public String getChargeNurse()
  {
    return chargeNurse;
  }

  public String getCureingCareProfessional()
  {
    return cureingCareProfessional;
  }

  public String getSupervisoryCareProfessional()
  {
    return supervisoryCareProfessional;
  }

  public String getRoomAndBed()
  {
    return roomAndBed;
  }

  public String getAdmissionDate()
  {
    return admissionDate;
  }

  public Integer getHospitalizationConsecutiveDay()
  {
    return hospitalizationConsecutiveDay;
  }

  public Integer getOperationDaysAfter()
  {
    return operationDaysAfter;
  }

  public List<ExternalCatalogDto> getDiseases()
  {
    return diseases;
  }

  public void setDiseases(List<ExternalCatalogDto> diseases)
  {
    this.diseases=diseases;
  }

  public String getWeight()
  {
    return weight;
  }

  public void setWeight(final String weight)
  {
    this.weight = weight;
  }

  public List<String> getAllergies()
  {
    return allergies;
  }

  public void setAllergies(final List<String> allergies)
  {
    this.allergies = allergies;
  }

  public String getCareProviderId()
  {
    return careProviderId;
  }

  public void setCareProviderId(final String careProviderId)
  {
    this.careProviderId = careProviderId;
  }

  public String getCurrentUser()
  {
    return currentUser;
  }

  public void setCurrentUser(final String currentUser)
  {
    this.currentUser = currentUser;
  }

  public String getStringDiseases()
  {
    final Set<String> diseaseDescriptions = new HashSet<>();
    final StringBuilder returnValue = new StringBuilder();
    for (final ExternalCatalogDto dto : diseases)
    {
      final String diseaseDescription = dto.getCode() + " - " + dto.getName() + "  ";
      if (!diseaseDescriptions.contains(diseaseDescription))
      {
        returnValue.append(diseaseDescription);
        diseaseDescriptions.add(diseaseDescription);
      }
    }
    return returnValue.toString();
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("patientName", patientName)
        .append("birthDate", birthDateAndAge)
        .append("addressDisplay", addressDisplay)
        .append("gender", gender)
        .append("patientIdNumber", patientIdentificator)
        .append("centralCaseIdNumber", centralCaseIdNumber)
        .append("organization", organization)
        .append("cureingCareProfessional", cureingCareProfessional)
        .append("supervisoryCareProfessional", supervisoryCareProfessional)
        .append("chargeNurse", chargeNurse)
        .append("roomAndBed", roomAndBed)
        .append("hospitalizationConsecutiveDay", hospitalizationConsecutiveDay)
        .append("operationDayAfter", operationDaysAfter)
        .append("diseases", diseases)
        .append("allergies", allergies)
        .append("careProviderId", careProviderId)
        .append("currentUser", currentUser);
  }
}