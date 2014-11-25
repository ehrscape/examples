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

import javax.persistence.Entity;

import com.marand.thinkmed.medications.model.FdbCrossTab;
import com.marand.maf.core.hibernate.entity.AbstractPermanentEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Index;

/**
 * @author Bostjan Vester
 */
@Entity
public class FdbCrossTabImpl extends AbstractPermanentEntity implements FdbCrossTab
{
  private String ukcWc;
  private String ukcTitle;
  private String ukcCbz;
  private String fdbGcnSeqNo;

  @Override
  @Index(name = "xeFdbGcnSeqNo")
  public String getFdbGcnSeqNo()
  {
    return fdbGcnSeqNo;
  }

  @Override
  public void setFdbGcnSeqNo(final String fdbGcnSeqNo)
  {
    this.fdbGcnSeqNo = fdbGcnSeqNo;
  }

  @Override
  public String getUkcCbz()
  {
    return ukcCbz;
  }

  @Override
  public void setUkcCbz(final String ukcCbz)
  {
    this.ukcCbz = ukcCbz;
  }

  @Override
  @Index(name = "xeUkcTitle")
  public String getUkcTitle()
  {
    return ukcTitle;
  }

  @Override
  public void setUkcTitle(final String ukcTitle)
  {
    this.ukcTitle = ukcTitle;
  }

  @Override
  public String getUkcWc()
  {
    return ukcWc;
  }

  @Override
  public void setUkcWc(final String ukcWc)
  {
    this.ukcWc = ukcWc;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("ukcWc", ukcWc)
        .append("ukcTitle", ukcTitle)
        .append("ukcCbz", ukcCbz)
        .append("fdbGcnSeqNo", fdbGcnSeqNo)
        ;
  }
}
