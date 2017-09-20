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

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.Interval;

/**
 * @author Mitja Lapajne
 */

public class MedicationsCentralCaseDto extends DataTransferObject implements JsonSerializable
{
  private boolean outpatient;
  private String centralCaseId;
  private Interval centralCaseEffective;
  private String episodeId;
  private NamedExternalDto careProvider;

  public boolean isOutpatient()
  {
    return outpatient;
  }

  public void setOutpatient(final boolean outpatient)
  {
    this.outpatient = outpatient;
  }

  public String getCentralCaseId()
  {
    return centralCaseId;
  }

  public void setCentralCaseId(final String centralCaseId)
  {
    this.centralCaseId = centralCaseId;
  }

  public Interval getCentralCaseEffective()
  {
    return centralCaseEffective;
  }

  public void setCentralCaseEffective(final Interval centralCaseEffective)
  {
    this.centralCaseEffective = centralCaseEffective;
  }

  public String getEpisodeId()
  {
    return episodeId;
  }

  public void setEpisodeId(final String episodeId)
  {
    this.episodeId = episodeId;
  }

  public NamedExternalDto getCareProvider()
  {
    return careProvider;
  }

  public void setCareProvider(final NamedExternalDto careProvider)
  {
    this.careProvider = careProvider;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("outpatient", outpatient)
        .append("centralCaseId", centralCaseId)
        .append("centralCaseEffective", centralCaseEffective)
        .append("episodeId", episodeId)
        .append("careProvider", careProvider);
  }
}
