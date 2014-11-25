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
import com.marand.maf.core.JsonSerializable;
import com.marand.maf.core.data.object.DataObject;
import com.marand.maf.core.formatter.Displayable;
import com.marand.maf.core.formatter.DisplayableFormatters;
import com.marand.thinkmed.medications.MedicationOrderFormType;
import com.marand.thinkmed.medications.TherapyTag;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public abstract class TherapyDto extends DataObject implements JsonSerializable
{
  private String compositionUid;
  private String ehrOrderName;
  private MedicationOrderFormType medicationOrderFormType;
  private boolean variable;
  private String therapyDescription;
  private MedicationRouteDto route;
  private DosingFrequencyDto dosingFrequency;
  private Integer dosingDaysFrequency; //every X-th day
  private List<String> daysOfWeek;
  @Displayable({DisplayableFormatters.ShortDate.class, DisplayableFormatters.ShortTime.class, DisplayableFormatters.ShortDateTime.class})
  private DateTime start;
  @Displayable({DisplayableFormatters.ShortDate.class, DisplayableFormatters.ShortTime.class, DisplayableFormatters.ShortDateTime.class})
  private DateTime end;
  private Boolean whenNeeded;
  private String comment;
  private String clinicalIndication;
  private String prescriberName;
  private String composerName;

  private String frequencyDisplay;
  private String daysFrequencyDisplay;
  private String whenNeededDisplay;
  private String startCriterionDisplay;
  private String daysOfWeekDisplay;
  private String formattedTherapyDisplay;
  private Integer pastDaysOfTherapy;

  private String linkFromTherapy;
  private String linkToTherapy;

  private Integer maxDailyFrequency;

  private DateTime createdTimestamp;

  private List<TherapyTag> tags = new ArrayList<>();
  private List<String> criticalWarnings = new ArrayList<>();

  private List<String> startCriterions = new ArrayList<>();

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

  public MedicationRouteDto getRoute()
  {
    return route;
  }

  public void setRoute(final MedicationRouteDto route)
  {
    this.route = route;
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

  public List<String> getStartCriterions()
  {
    return startCriterions;
  }

  public void setStartCriterions(final List<String> startCriterions)
  {
    this.startCriterions = startCriterions;
  }

  public String getComment()
  {
    return comment;
  }

  public void setComment(final String comment)
  {
    this.comment = comment;
  }

  public String getClinicalIndication()
  {
    return clinicalIndication;
  }

  public void setClinicalIndication(final String clinicalIndication)
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

  public void setFormattedTherapyDisplay(final String formattedTherapyDisplay)
  {
    this.formattedTherapyDisplay = formattedTherapyDisplay;
  }

  public void setPastDaysOfTherapy(final Integer pastDaysOfTherapy)
  {
    this.pastDaysOfTherapy = pastDaysOfTherapy;
  }

  public String getLinkFromTherapy()
  {
    return linkFromTherapy;
  }

  public void setLinkFromTherapy(final String linkFromTherapy)
  {
    this.linkFromTherapy = linkFromTherapy;
  }

  public String getLinkToTherapy()
  {
    return linkToTherapy;
  }

  public void setLinkToTherapy(final String linkToTherapy)
  {
    this.linkToTherapy = linkToTherapy;
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

  public List<TherapyTag> getTags()
  {
    return tags;
  }

  public void setTags(final List<TherapyTag> tags)
  {
    this.tags = tags;
  }

  public void addTag(final TherapyTag tag)
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

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("compositionUid", compositionUid)
        .append("ehrOrderName", ehrOrderName)
        .append("medicationOrderFormType", medicationOrderFormType)
        .append("therapyDescription", therapyDescription)
        .append("route", route)
        .append("dosingFrequency", dosingFrequency)
        .append("dosingDaysFrequency", dosingDaysFrequency)
        .append("daysOfWeek", daysOfWeek)
        .append("start", start)
        .append("end", end)
        .append("whenNeeded", whenNeeded)
        .append("startCriterions", startCriterions)
        .append("comment", comment)
        .append("clinicalIndication", clinicalIndication)
        .append("prescriberName", prescriberName)
        .append("composerName", composerName)
        .append("frequencyDisplay", frequencyDisplay)
        .append("daysFrequencyDisplay", daysFrequencyDisplay)
        .append("whenNeededDisplay", whenNeededDisplay)
        .append("startCriterionDisplay", startCriterionDisplay)
        .append("daysOfWeekDisplay", daysOfWeekDisplay)
        .append("formattedTherapyDisplay", formattedTherapyDisplay)
        .append("antibioticDays", pastDaysOfTherapy)
        .append("linkToTherapy", linkToTherapy)
        .append("linkFromTherapy", linkFromTherapy)
        .append("maxDailyFrequency", maxDailyFrequency)
        .append("createdTimestamp", createdTimestamp)
        .append("tags", tags)
        .append("criticalWarnings", criticalWarnings)
    ;
  }
}