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

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.TitrationType;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 * @author Mitja Lapajne
 */
public class MedicationDataDto extends DataTransferObject implements JsonSerializable
{
  private MedicationDto medication;
  private MedicationRouteDto defaultRoute;
  private DoseFormDto doseForm;
  private Double doseSplitFactor;
  private String basicUnit;
  private boolean antibiotic;
  private boolean reviewReminder;
  private boolean mentalHealthDrug;
  private boolean controlledDrug;
  private TitrationType titration;
  private boolean blackTriangleMedication;
  private boolean clinicalTrialMedication;
  private boolean highAlertMedication;
  private boolean unlicensedMedication;
  private boolean expensiveDrug;
  private String price;
  private boolean inpatientMedication;
  private boolean outpatientMedication;
  private boolean formulary;
  private Double roundingFactor;
  private MedicationIngredientDto descriptiveIngredient;
  private List<MedicationIngredientDto> medicationIngredients = new ArrayList<>();
  private List<MedicationRouteDto> routes = new ArrayList<>();
  private List<MedicationDocumentDto> medicationDocuments = new ArrayList<>();
  private List<IndicationDto> indications = new ArrayList<>();
  private String medicationPackaging;

  public MedicationDto getMedication()
  {
    return medication;
  }

  public void setMedication(final MedicationDto medication)
  {
    this.medication = medication;
  }

  public MedicationRouteDto getDefaultRoute()
  {
    return defaultRoute;
  }

  public void setDefaultRoute(final MedicationRouteDto defaultRoute)
  {
    this.defaultRoute = defaultRoute;
  }

  public DoseFormDto getDoseForm()
  {
    return doseForm;
  }

  public void setDoseForm(final DoseFormDto doseForm)
  {
    this.doseForm = doseForm;
  }

  public Double getDoseSplitFactor()
  {
    return doseSplitFactor;
  }

  public void setDoseSplitFactor(final Double doseSplitFactor)
  {
    this.doseSplitFactor = doseSplitFactor;
  }

  public String getBasicUnit()
  {
    return basicUnit;
  }

  public void setBasicUnit(final String basicUnit)
  {
    this.basicUnit = basicUnit;
  }

  public boolean isAntibiotic()
  {
    return antibiotic;
  }

  public void setAntibiotic(final boolean antibiotic)
  {
    this.antibiotic = antibiotic;
  }

  public boolean isReviewReminder()
  {
    return reviewReminder;
  }

  public void setReviewReminder(final boolean reviewReminder)
  {
    this.reviewReminder = reviewReminder;
  }

  public Boolean getControlledDrug()
  {
    return controlledDrug;
  }

  public void setControlledDrug(final boolean controlledDrug)
  {
    this.controlledDrug = controlledDrug;
  }

  public boolean getMentalHealthDrug()
  {
    return mentalHealthDrug;
  }

  public void setMentalHealthDrug(final boolean mentalHealthDrug)
  {
    this.mentalHealthDrug = mentalHealthDrug;
  }

  public TitrationType getTitration()
  {
    return titration;
  }

  public void setTitration(final TitrationType titration)
  {
    this.titration = titration;
  }


  public boolean isBlackTriangleMedication()
  {
    return blackTriangleMedication;
  }

  public void setBlackTriangleMedication(final boolean blackTriangleMedication)
  {
    this.blackTriangleMedication = blackTriangleMedication;
  }

  public boolean isClinicalTrialMedication()
  {
    return clinicalTrialMedication;
  }

  public void setClinicalTrialMedication(final boolean clinicalTrialMedication)
  {
    this.clinicalTrialMedication = clinicalTrialMedication;
  }

  public boolean isHighAlertMedication()
  {
    return highAlertMedication;
  }

  public void setHighAlertMedication(final boolean highAlertMedication)
  {
    this.highAlertMedication = highAlertMedication;
  }

  public boolean isUnlicensedMedication()
  {
    return unlicensedMedication;
  }

  public void setUnlicensedMedication(final boolean unlicensedMedication)
  {
    this.unlicensedMedication = unlicensedMedication;
  }

  public boolean isExpensiveDrug()
  {
    return expensiveDrug;
  }

  public void setExpensiveDrug(final boolean expensiveDrug)
  {
    this.expensiveDrug = expensiveDrug;
  }

  public String getPrice()
  {
    return price;
  }

  public void setPrice(final String price)
  {
    this.price = price;
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

  public boolean isFormulary()
  {
    return formulary;
  }

  public void setFormulary(final boolean formulary)
  {
    this.formulary = formulary;
  }

  public Double getRoundingFactor()
  {
    return roundingFactor;
  }

  public void setRoundingFactor(final Double roundingFactor)
  {
    this.roundingFactor = roundingFactor;
  }

  public MedicationIngredientDto getDescriptiveIngredient()
  {
    return descriptiveIngredient;
  }

  public void setDescriptiveIngredient(final MedicationIngredientDto descriptiveIngredient)
  {
    this.descriptiveIngredient = descriptiveIngredient;
  }

  public List<MedicationIngredientDto> getMedicationIngredients()
  {
    return medicationIngredients;
  }

  public void setMedicationIngredients(final List<MedicationIngredientDto> medicationIngredients)
  {
    this.medicationIngredients = medicationIngredients;
  }

  public List<MedicationRouteDto> getRoutes()
  {
    return routes;
  }

  public void setRoutes(final List<MedicationRouteDto> routes)
  {
    this.routes = routes;
  }

  public List<MedicationDocumentDto> getMedicationDocuments()
  {
    return medicationDocuments;
  }

  public void setMedicationDocuments(final List<MedicationDocumentDto> medicationDocuments)
  {
    this.medicationDocuments = medicationDocuments;
  }

  public List<IndicationDto> getIndications()
  {
    return indications;
  }

  public void setIndications(final List<IndicationDto> indications)
  {
    this.indications = indications;
  }

  public String getMedicationPackaging()
  {
    return medicationPackaging;
  }

  public void setMedicationPackaging(final String medicationPackaging)
  {
    this.medicationPackaging = medicationPackaging;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("medication", medication)
        .append("defaultRoute", defaultRoute)
        .append("doseForm", doseForm)
        .append("doseSplitFactor", doseSplitFactor)
        .append("basicUnit", basicUnit)
        .append("descriptiveIngredient", descriptiveIngredient)
        .append("medicationIngredients", medicationIngredients)
        .append("routes", routes)
        .append("antibiotic", antibiotic)
        .append("reviewReminder", reviewReminder)
        .append("medicationDocuments", medicationDocuments)
        .append("indications", indications)
        .append("controlledDrug", controlledDrug)
        .append("mentalHealthDrug", mentalHealthDrug)
        .append("titration", titration)
        .append("blackTriangleMedication", blackTriangleMedication)
        .append("clinicalTrialMedication", clinicalTrialMedication)
        .append("highAlertMedication", highAlertMedication)
        .append("unlicensedMedication", unlicensedMedication)
        .append("inpatientMedication", inpatientMedication)
        .append("outpatientMedication", outpatientMedication)
        .append("formulary", formulary)
        .append("roundingFactor", roundingFactor)
        .append("medicationPackaging", medicationPackaging)
        .append("expensiveDrug", expensiveDrug)
        .append("price", price)
    ;
  }
}