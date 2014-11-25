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

import com.marand.maf.core.data.object.DataObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * User: MihaA
 */

public class DocumentationTherapiesDto extends DataObject
{
  private List<String> therapies = new ArrayList<>();
  private List<String> dischargeTherapies = new ArrayList<>();
  private List<String> admissionTherapies = new ArrayList<>();
  private List<String> taggedTherapiesForPrescription =  new ArrayList<>();

  public DocumentationTherapiesDto(
      final List<String> therapies,
      final List<String> dischargeTherapies,
      final List<String> admissionTherapies,
      final List<String> taggedTherapiesForPrescription)
  {
    this.therapies = therapies;
    this.dischargeTherapies = dischargeTherapies;
    this.admissionTherapies = admissionTherapies;
    this.taggedTherapiesForPrescription = taggedTherapiesForPrescription;
  }

  public List<String> getTherapies()
  {
    return therapies;
  }

  public List<String> getDischargeTherapies()
  {
    return dischargeTherapies;
  }

  public List<String> getAdmissionTherapies()
  {
    return admissionTherapies;
  }

  public List<String> getTaggedTherapiesForPrescription()
  {
    return taggedTherapiesForPrescription;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("therapies", therapies)
        .append("dischargeTherapies", dischargeTherapies)
        .append("admissionTherapies", admissionTherapies)
        .append("taggedTherapiesForPrescription", taggedTherapiesForPrescription)
        ;
  }
}
