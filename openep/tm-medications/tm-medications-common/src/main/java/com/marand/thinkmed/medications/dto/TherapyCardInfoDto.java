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

import com.marand.maf.core.JsonSerializable;
import com.marand.maf.core.data.object.DataObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Igor Horvat
 */

public class TherapyCardInfoDto extends DataObject implements JsonSerializable
{
  private TherapyDto currentTherapy;
  private TherapyDto originalTherapy;
  private List<TherapyChangeHistoryDto> changeHistoryList = new ArrayList<>();
  private List<String> similarTherapies = new ArrayList<>();

  public TherapyDto getCurrentTherapy()
  {
    return currentTherapy;
  }

  public void setCurrentTherapy(final TherapyDto currentTherapy)
  {
    this.currentTherapy = currentTherapy;
  }

  public TherapyDto getOriginalTherapy()
  {
    return originalTherapy;
  }

  public void setOriginalTherapy(final TherapyDto originalTherapy)
  {
    this.originalTherapy = originalTherapy;
  }

  public List<TherapyChangeHistoryDto> getChangeHistoryList()
  {
    return changeHistoryList;
  }

  public void setChangeHistoryList(final List<TherapyChangeHistoryDto> changeHistoryList)
  {
    this.changeHistoryList = changeHistoryList;
  }

  public List<String> getSimilarTherapies()
  {
    return similarTherapies;
  }

  public void setSimilarTherapies(final List<String> similarTherapies)
  {
    this.similarTherapies = similarTherapies;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("currentTherapy", currentTherapy)
        .append("originalTherapy", originalTherapy)
        .append("changeHistoryList", changeHistoryList)
        .append("similarTherapies", similarTherapies);
  }
}