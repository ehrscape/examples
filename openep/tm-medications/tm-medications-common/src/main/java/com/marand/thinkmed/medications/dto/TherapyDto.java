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

import com.google.common.base.Preconditions;
import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.MedicationOrderFormType;
import com.marand.thinkmed.medications.PrescriptionLocalDetailsDto;
import com.marand.thinkmed.medications.SelfAdministeringActionEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.TherapyTagEnum;
import com.marand.thinkmed.medications.dto.supply.PrescriptionSupplyDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public abstract class TherapyDto extends DataTransferObject implements JsonSerializable
{
  private String compositionUid;
  private String ehrOrderName;
  private MedicationOrderFormType medicationOrderFormType;
  private boolean variable;
  private String therapyDescription;
  private List<MedicationRouteDto> routes = new ArrayList<>();
  private DosingFrequencyDto dosingFrequency;
  private Integer dosingDaysFrequency; //every X-th day
  private List<String> daysOfWeek;
  private DateTime start;
  private DateTime end;
  private Boolean whenNeeded;
  private String comment;
  private IndicationDto clinicalIndication;
  private String prescriberName;
  private String composerName;
  private String startCriterion;
  private String applicationPrecondition;
  private Integer reviewReminderDays;

  private String frequencyDisplay;
  private String daysFrequencyDisplay;
  private String whenNeededDisplay;
  private String startCriterionDisplay;
  private String daysOfWeekDisplay;
  private String applicationPreconditionDisplay;

  private String formattedTherapyDisplay;
  private Integer pastDaysOfTherapy;

  private String linkName;

  private Integer maxDailyFrequency;

  private DateTime createdTimestamp;

  private List<TherapyTagEnum> tags = new ArrayList<>();
  private List<String> criticalWarnings = new ArrayList<>();
  private boolean linkedToAdmission;
  private Integer bnfMaximumPercentage;

  private TherapyDoseTypeEnum doseType;
  private SelfAdministeringActionEnum selfAdministeringActionEnum;
  private DateTime selfAdministeringLastChange;

  private PrescriptionLocalDetailsDto prescriptionLocalDetails;
  private PrescriptionSupplyDto prescriptionSupply;

  protected TherapyDto(final MedicationOrderFormType type, final boolean variable)
  {
    medicationOrderFormType = Preconditions.checkNotNull(type);
    this.variable = variable;
  }

  public String getCompositionUid()
  {
    return compositionUid;
  }

  public void setCompositionUid(final String compositionUid)
  {
    this.compositionUid = compositionUid;
  }

  public String getEhrOrderName()
  {
    return ehrOrderName;
  }

  public void setEhrOrderName(final String ehrOrderName)
  {
    this.ehrOrderName = ehrOrderName;
  }

  public MedicationOrderFormType getMedicationOrderFormType()
  {
    return medicationOrderFormType;
  }

  protected final void setMedicationOrderFormType(final MedicationOrderFormType medicationOrderFormType)
  {
    this.medicationOrderFormType = medicationOrderFormType;
  }

  public boolean isVariable()
  {
    return variable;
  }

  public void setVariable(final boolean variable)
  {
    this.variable = variable;
  }

  public String getTherapyDescription()
  {
    return therapyDescription;
  }

  public void setTherapyDescription(final String therapyDescription)
  {
    this.therapyDescription = therapyDescription;
  }

  public List<MedicationRouteDto> getRoutes()
  {
    return routes;
  }

  public void setRoutes(final List<MedicationRouteDto> routes)
  {
    this.routes = routes;
  }

  public DosingFrequencyDto getDosingFrequency()
  {
    return dosingFrequency;
  }

  public void setDosingFrequency(final DosingFrequencyDto dosingFrequency)
  {
    this.dosingFrequency = dosingFrequency;
  }

  public Integer getDosingDaysFrequency()
  {
    return dosingDaysFrequency;
  }

  public void setDosingDaysFrequency(final Integer dosingDaysFrequency)
  {
    this.dosingDaysFrequency = dosingDaysFrequency;
  }

  public List<String> getDaysOfWeek()
  {
    return daysOfWeek;
  }

  public void setDaysOfWeek(final List<String> daysOfWeek)
  {
    this.daysOfWeek = daysOfWeek;
  }

  public DateTime getStart()
  {
    return start;
  }

  public void setStart(final DateTime start)
  {
    this.start = start;
  }

  public DateTime getEnd()
  {
    return end;
  }

  public void setEnd(final DateTime end)
  {
    this.end = end;
  }

  public Boolean getWhenNeeded()
  {
    return whenNeeded;
  }

  public void setWhenNeeded(final Boolean whenNeeded)
  {
    this.whenNeeded = whenNeeded;
  }

  public String getComment()
  {
    return comment;
  }

  public void setComment(final String comment)
  {
    this.comment = comment;
  }

  public IndicationDto getClinicalIndication()
  {
    return clinicalIndication;
  }

  public void setClinicalIndication(final IndicationDto clinicalIndication)
  {
    this.clinicalIndication = clinicalIndication;
  }

  public String getPrescriberName()
  {
    return prescriberName;
  }

  public void setPrescriberName(final String prescriberName)
  {
    this.prescriberName = prescriberName;
  }

  public String getComposerName()
  {
    return composerName;
  }

  public void setComposerName(final String composerName)
  {
    this.composerName = composerName;
  }

  public String getStartCriterion()
  {
    return startCriterion;
  }

  public void setStartCriterion(final String startCriterion)
  {
    this.startCriterion = startCriterion;
  }

  public String getApplicationPrecondition()
  {
    return applicationPrecondition;
  }

  public void setApplicationPrecondition(final String applicationPrecondition)
  {
    this.applicationPrecondition = applicationPrecondition;
  }

  public Integer getReviewReminderDays()
  {
    return reviewReminderDays;
  }

  public void setReviewReminderDays(final Integer reviewReminderDays)
  {
    this.reviewReminderDays = reviewReminderDays;
  }

  public String getFrequencyDisplay()
  {
    return frequencyDisplay;
  }

  public void setFrequencyDisplay(final String frequencyDisplay)
  {
    this.frequencyDisplay = frequencyDisplay;
  }

  public String getDaysFrequencyDisplay()
  {
    return daysFrequencyDisplay;
  }

  public void setDaysFrequencyDisplay(final String daysFrequencyDisplay)
  {
    this.daysFrequencyDisplay = daysFrequencyDisplay;
  }

  public String getWhenNeededDisplay()
  {
    return whenNeededDisplay;
  }

  public void setWhenNeededDisplay(final String whenNeededDisplay)
  {
    this.whenNeededDisplay = whenNeededDisplay;
  }

  public String getStartCriterionDisplay()
  {
    return startCriterionDisplay;
  }

  public void setStartCriterionDisplay(final String startCriterionDisplay)
  {
    this.startCriterionDisplay = startCriterionDisplay;
  }

  public String getDaysOfWeekDisplay()
  {
    return daysOfWeekDisplay;
  }

  public void setDaysOfWeekDisplay(final String daysOfWeekDisplay)
  {
    this.daysOfWeekDisplay = daysOfWeekDisplay;
  }

  public Integer getPastDaysOfTherapy()
  {
    return pastDaysOfTherapy;
  }

  public String getFormattedTherapyDisplay()
  {
    return formattedTherapyDisplay;
  }

  public String getApplicationPreconditionDisplay()
  {
    return applicationPreconditionDisplay;
  }

  public void setApplicationPreconditionDisplay(final String applicationPreconditionDisplay)
  {
    this.applicationPreconditionDisplay = applicationPreconditionDisplay;
  }

  public void setFormattedTherapyDisplay(final String formattedTherapyDisplay)
  {
    this.formattedTherapyDisplay = formattedTherapyDisplay;
  }

  public void setPastDaysOfTherapy(final Integer pastDaysOfTherapy)
  {
    this.pastDaysOfTherapy = pastDaysOfTherapy;
  }

  public String getLinkName()
  {
    return linkName;
  }

  public void setLinkName(final String linkName)
  {
    this.linkName = linkName;
  }

  public Integer getMaxDailyFrequency()
  {
    return maxDailyFrequency;
  }

  public void setMaxDailyFrequency(final Integer maxDailyFrequency)
  {
    this.maxDailyFrequency = maxDailyFrequency;
  }

  public DateTime getCreatedTimestamp()
  {
    return createdTimestamp;
  }

  public void setCreatedTimestamp(final DateTime createdTimestamp)
  {
    this.createdTimestamp = createdTimestamp;
  }

  public List<TherapyTagEnum> getTags()
  {
    return tags;
  }

  public void setTags(final List<TherapyTagEnum> tags)
  {
    this.tags = tags;
  }

  public void addTag(final TherapyTagEnum tag)
  {
    tags.add(tag);
  }

  public List<String> getCriticalWarnings()
  {
    return criticalWarnings;
  }

  public void setCriticalWarnings(final List<String> criticalWarnings)
  {
    this.criticalWarnings = criticalWarnings;
  }

  public boolean isLinkedToAdmission()
  {
    return linkedToAdmission;
  }

  public void setLinkedToAdmission(final boolean linkedToAdmission)
  {
    this.linkedToAdmission = linkedToAdmission;
  }

  public Integer getBnfMaximumPercentage()
  {
    return bnfMaximumPercentage;
  }

  public void setBnfMaximumPercentage(final Integer bnfMaximumPercentage)
  {
    this.bnfMaximumPercentage = bnfMaximumPercentage;
  }

  public PrescriptionLocalDetailsDto getPrescriptionLocalDetails()
  {
    return prescriptionLocalDetails;
  }

  public void setPrescriptionLocalDetails(final PrescriptionLocalDetailsDto prescriptionLocalDetails)
  {
    this.prescriptionLocalDetails = prescriptionLocalDetails;
  }

  public PrescriptionSupplyDto getPrescriptionSupply()
  {
    return prescriptionSupply;
  }

  public void setPrescriptionSupply(final PrescriptionSupplyDto prescriptionSupply)
  {
    this.prescriptionSupply = prescriptionSupply;
  }

  public TherapyDoseTypeEnum getDoseType()
  {
    return doseType;
  }

  public void setDoseType(final TherapyDoseTypeEnum doseType)
  {
    this.doseType = doseType;
  }

  public SelfAdministeringActionEnum getSelfAdministeringActionEnum()
  {
    return selfAdministeringActionEnum;
  }

  public void setSelfAdministeringActionEnum(final SelfAdministeringActionEnum selfAdministeringActionEnum)
  {
    this.selfAdministeringActionEnum = selfAdministeringActionEnum;
  }

  public DateTime getSelfAdministeringLastChange()
  {
    return selfAdministeringLastChange;
  }

  public void setSelfAdministeringLastChange(final DateTime selfAdministeringLastChange)
  {
    this.selfAdministeringLastChange = selfAdministeringLastChange;
  }

  public String getTherapyId()
  {
    Preconditions.checkNotNull(compositionUid, "compositionUid must not be null!");
    Preconditions.checkNotNull(ehrOrderName, "ehrOrderName must not be null!");

    return (compositionUid.contains("::") ? compositionUid.substring(0, compositionUid.indexOf("::")) : compositionUid)
        + '|'
        + ehrOrderName;
  }

  public boolean isWithRate()
  {
    return TherapyDoseTypeEnum.WITH_RATE.contains(doseType);
  }

  public abstract boolean isNormalInfusion();

  public abstract List<MedicationDto> getMedications();

  public abstract Long getMainMedicationId();

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("compositionUid", compositionUid)
        .append("ehrOrderName", ehrOrderName)
        .append("medicationOrderFormType", medicationOrderFormType)
        .append("therapyDescription", therapyDescription)
        .append("route", routes)
        .append("dosingFrequency", dosingFrequency)
        .append("dosingDaysFrequency", dosingDaysFrequency)
        .append("daysOfWeek", daysOfWeek)
        .append("start", start)
        .append("end", end)
        .append("whenNeeded", whenNeeded)
        .append("comment", comment)
        .append("clinicalIndication", clinicalIndication)
        .append("prescriberName", prescriberName)
        .append("composerName", composerName)
        .append("startCriterion", startCriterion)
        .append("applicationPrecondition", applicationPrecondition)
        .append("reviewReminderDays", reviewReminderDays)
        .append("frequencyDisplay", frequencyDisplay)
        .append("daysFrequencyDisplay", daysFrequencyDisplay)
        .append("whenNeededDisplay", whenNeededDisplay)
        .append("startCriterionDisplay", startCriterionDisplay)
        .append("daysOfWeekDisplay", daysOfWeekDisplay)
        .append("applicationPreconditionDisplay", applicationPreconditionDisplay)
        .append("formattedTherapyDisplay", formattedTherapyDisplay)
        .append("antibioticDays", pastDaysOfTherapy)
        .append("linkName", linkName)
        .append("maxDailyFrequency", maxDailyFrequency)
        .append("createdTimestamp", createdTimestamp)
        .append("tags", tags)
        .append("criticalWarnings", criticalWarnings)
        .append("linkedToAdmission", linkedToAdmission)
        .append("bnfMaximumPercentage", bnfMaximumPercentage)
        .append("prescriptionLocalDetails", prescriptionLocalDetails)
        .append("prescriptionSupply", prescriptionSupply)
        .append("selfAdministeringActionEnum", selfAdministeringActionEnum)
        .append("selfAdministeringLastChange", selfAdministeringLastChange)
        .append("doseType", doseType)
    ;
  }
}