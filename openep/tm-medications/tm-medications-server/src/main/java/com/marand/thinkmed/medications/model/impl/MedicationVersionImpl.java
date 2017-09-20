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

package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.marand.maf.core.hibernate.entity.AbstractTemporalEntity;
import com.marand.thinkmed.medications.TitrationType;
import com.marand.thinkmed.medications.model.AtcClassification;
import com.marand.thinkmed.medications.model.DoseForm;
import com.marand.thinkmed.medications.model.Medication;
import com.marand.thinkmed.medications.model.MedicationBasicUnit;
import com.marand.thinkmed.medications.model.MedicationGeneric;
import com.marand.thinkmed.medications.model.MedicationVersion;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.ColumnDefault;

/**
 * @author Klavdij Lapajne
 */
@Entity
@Table(indexes = {
    @Index(name = "xfMedicationGeneric", columnList = "medication_generic_id"),
    @Index(name = "xfMedicationDoseForm", columnList = "dose_form_id"),
    @Index(name = "xfMedicationAtcClass", columnList = "atc_classification_id"),
    @Index(name = "xfMedicationBasicUnit", columnList = "basic_unit_id")})
public class MedicationVersionImpl extends AbstractTemporalEntity implements MedicationVersion
{
  private String name;
  private String shortName;
  private String longName;
  private MedicationGeneric medicationGeneric;
  private DoseForm doseForm;
  private AtcClassification atcClassification;
  private MedicationBasicUnit basicUnit;
  private Medication medication;
  private Boolean active;
  private Boolean suggestSwitchToOral;
  private Boolean reviewReminder;
  private Boolean controlledDrug;
  private Boolean mentalHealthDrug;
  private String medicationPackaging;
  private Boolean clinicalTrialMedication;
  private Boolean unlicensedMedication;
  private Boolean highAlertMedication;
  private Boolean blackTriangleMedication;
  private Boolean notForPrn;
  private Boolean inpatientMedication;
  private Boolean outpatientMedication;
  private Double roundingFactor;
  private TitrationType titration;
  private String price;
  private Boolean expensiveDrug;

  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public void setName(final String name)
  {
    this.name = name;
  }

  @Override
  public String getShortName()
  {
    return shortName;
  }

  @Override
  public void setShortName(final String shortName)
  {
    this.shortName = shortName;
  }

  @Override
  public String getLongName()
  {
    return longName;
  }

  @Override
  public void setLongName(final String longName)
  {
    this.longName = longName;
  }

  @Override
  @ManyToOne(targetEntity = MedicationGenericImpl.class, fetch = FetchType.LAZY)
  public MedicationGeneric getMedicationGeneric()
  {
    return medicationGeneric;
  }

  @Override
  public void setMedicationGeneric(final MedicationGeneric medicationGeneric)
  {
    this.medicationGeneric = medicationGeneric;
  }

  @Override
  @ManyToOne(targetEntity = DoseFormImpl.class, fetch = FetchType.LAZY)
  public DoseForm getDoseForm()
  {
    return doseForm;
  }

  @Override
  @ManyToOne(targetEntity = MedicationVersionImpl.class, fetch = FetchType.LAZY)
  public void setDoseForm(final DoseForm doseForm)
  {
    this.doseForm = doseForm;
  }

  @Override
  @ManyToOne(targetEntity = AtcClassificationImpl.class, fetch = FetchType.LAZY)
  public AtcClassification getAtcClassification()
  {
    return atcClassification;
  }

  @Override
  public void setAtcClassification(final AtcClassification atcClassification)
  {
    this.atcClassification = atcClassification;
  }

  @Override
  @ManyToOne(targetEntity = MedicationBasicUnitImpl.class, fetch = FetchType.LAZY)
  public MedicationBasicUnit getBasicUnit()
  {
    return basicUnit;
  }

  @Override
  public void setBasicUnit(final MedicationBasicUnit basicUnit)
  {
    this.basicUnit = basicUnit;
  }

  @Override
  @ManyToOne(targetEntity = MedicationImpl.class, fetch = FetchType.LAZY)
  public Medication getMedication()
  {
    return medication;
  }

  @Override
  public void setMedication(final Medication medication)
  {
    this.medication = medication;
  }

  @Override
  @Column(nullable = false)
  @ColumnDefault("1")
  public Boolean getActive()
  {
    return active;
  }

  @Override
  public void setActive(final Boolean active)
  {
    this.active = active;
  }

  @Override
  @Column(nullable = false)
  @ColumnDefault("0")
  public Boolean getSuggestSwitchToOral()
  {
    return suggestSwitchToOral;
  }

  @Override
  public void setSuggestSwitchToOral(final Boolean suggestSwitchToOral)
  {
    this.suggestSwitchToOral = suggestSwitchToOral;
  }

  @Override
  @Column(nullable = false)
  @ColumnDefault("0")
  public Boolean getReviewReminder()
  {
    return reviewReminder;
  }

  @Override
  public void setReviewReminder(final Boolean reviewReminder)
  {
    this.reviewReminder = reviewReminder;
  }

  @Override
  public Boolean getControlledDrug()
  {
    return controlledDrug;
  }

  @Override
  public void setControlledDrug(final Boolean controlledDrug)
  {
    this.controlledDrug = controlledDrug;
  }

  @Override
  @Column(nullable = false)
  @ColumnDefault("0")
  public Boolean isMentalHealthDrug()
  {
    return mentalHealthDrug;
  }

  @Override
  public void setMentalHealthDrug(final Boolean mentalHealthDrug)
  {
    this.mentalHealthDrug = mentalHealthDrug;
  }

  @Override
  @Column(nullable = false)
  @ColumnDefault("0")
  public Boolean getInpatientMedication()
  {
    return inpatientMedication;
  }

  @Override
  public void setInpatientMedication(final Boolean inpatientMedication)
  {
    this.inpatientMedication = inpatientMedication;
  }

  @Override
  @Column(nullable = false)
  @ColumnDefault("0")
  public Boolean getOutpatientMedication()
  {
    return outpatientMedication;
  }

  @Override
  public void setOutpatientMedication(final Boolean outpatientMedication)
  {
    this.outpatientMedication = outpatientMedication;
  }

  @Override
  public String getMedicationPackaging()
  {
    return medicationPackaging;
  }
  @Override
  public void setMedicationPackaging(final String medicationPackaging)
  {
    this.medicationPackaging = medicationPackaging;
  }

  @Override
  @ColumnDefault("0")
  public Boolean getClinicalTrialMedication()
  {
    return clinicalTrialMedication;
  }

  @Override
  public void setClinicalTrialMedication(final Boolean clinicalTrialMedication)
  {
    this.clinicalTrialMedication = clinicalTrialMedication;
  }

  @Override
  @ColumnDefault("0")
  public Boolean getUnlicensedMedication()
  {
    return unlicensedMedication;
  }

  @Override
  public void setUnlicensedMedication(final Boolean unlicensedMedication)
  {
    this.unlicensedMedication = unlicensedMedication;
  }

  @Override
  @ColumnDefault("0")
  public Boolean getHighAlertMedication()
  {
    return highAlertMedication;
  }

  @Override
  public void setHighAlertMedication(final Boolean highAlertMedication)
  {
    this.highAlertMedication = highAlertMedication;
  }

  @Override
  @ColumnDefault("0")
  public Boolean getBlackTriangleMedication()
  {
    return blackTriangleMedication;
  }

  @Override
  public void setBlackTriangleMedication(final Boolean blackTriangleMedication)
  {
    this.blackTriangleMedication = blackTriangleMedication;
  }

  @Override
  @ColumnDefault("0")
  public Boolean getNotForPrn()
  {
    return notForPrn;
  }

  @Override
  public void setNotForPrn(final Boolean notForPrn)
  {
    this.notForPrn = notForPrn;
  }

  @Override
  public Double getRoundingFactor()
  {
    return roundingFactor;
  }

  @Override
  public void setRoundingFactor(final Double roundingFactor)
  {
    this.roundingFactor = roundingFactor;
  }

  @Override
  @Enumerated(EnumType.STRING)
  public TitrationType getTitration()
  {
    return titration;
  }

  @Override
  public void setTitration(final TitrationType titration)
  {
    this.titration = titration;
  }

  @Override
  public String getPrice()
  {
    return price;
  }

  @Override
  public void setPrice(final String price)
  {
    this.price = price;
  }

  @Override
  @ColumnDefault("0")
  public Boolean isExpensiveDrug()
  {
    return expensiveDrug;
  }

  @Override
  public void setExpensiveDrug(final Boolean expensiveDrug)
  {
    this.expensiveDrug = expensiveDrug;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb.append("shortName", shortName)
        .append("longName", longName)
        .append("name", name)
        .append("doseForm", doseForm)
        .append("atcClassification", atcClassification)
        .append("basicUnit", basicUnit)
        .append("medication", medication)
        .append("active", active)
        .append("suggestSwitchToOral", suggestSwitchToOral)
        .append("reviewReminder", reviewReminder)
        .append("controlledDrug", controlledDrug)
        .append("mentalHealthDrug", mentalHealthDrug)
        .append("medicationPackaging", medicationPackaging)
        .append("clinicalTrialMedication", clinicalTrialMedication)
        .append("unlicensedMedication", unlicensedMedication)
        .append("highAlertMedication", highAlertMedication)
        .append("blackTriangleMedication", blackTriangleMedication)
        .append("notForPrn", notForPrn)
        .append("inpatientMedication", inpatientMedication)
        .append("outpatientMedication", outpatientMedication)
        .append("roundingFactor", roundingFactor)
        .append("titration", titration)
        .append("expensiveDrug", expensiveDrug)
        .append("price", price)
    ;
  }
}
