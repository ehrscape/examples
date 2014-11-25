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

package com.marand.thinkmed.api.demographics.data.object;

import com.marand.thinkmed.api.core.data.object.Dto;
import com.marand.thinkmed.api.demographics.DemographicsUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Jani Vrhovnik
 */
public class NameDto extends Dto
{
  private static final long serialVersionUID = 3905267224947630632L;

  private String firstName;
  private String firstName1;
  private String firstName2;
  private String firstNameHyphen;
  private String lastName;
  private String lastName1;
  private String lastName2;
  private String lastNameHyphen;
  private String birthLastName;
  private String prefix;
  private String suffix;
  private String specialization;
  private String jobTitle;

  public NameDto()
  {
  }

  public NameDto(
      final String firstName1,
      final String firstName2,
      final String firstNameHyphen,
      final String lastName1,
      final String lastName2,
      final String lastNameHyphen)
  {
    this.firstName1 = firstName1;
    this.firstName2 = firstName2;
    this.firstNameHyphen = firstNameHyphen;
    this.lastName1 = lastName1;
    this.lastName2 = lastName2;
    this.lastNameHyphen = lastNameHyphen;
  }

  public String getFirstName()
  {
    if (StringUtils.isBlank(firstName))
    {
      firstName = DemographicsUtils.calculateName(firstName1, firstName2, firstNameHyphen);
    }
    return firstName;
  }

  public void setFirstName(final String firstName)
  {
    this.firstName = firstName;
  }

  public String getFirstName1()
  {
    return firstName1;
  }

  public void setFirstName1(final String firstName1)
  {
    this.firstName1 = firstName1;
    resetFirstName();
  }

  public String getFirstName2()
  {
    return firstName2;
  }

  public void setFirstName2(final String firstName2)
  {
    this.firstName2 = firstName2;
    resetFirstName();
  }

  public String getFirstNameHyphen()
  {
    return firstNameHyphen;
  }

  public void setFirstNameHyphen(final String firstNameHyphen)
  {
    this.firstNameHyphen = firstNameHyphen;
    resetFirstName();
  }

  public String getLastName()
  {
    if (StringUtils.isBlank(lastName))
    {
      lastName = DemographicsUtils.calculateName(lastName1, lastName2, lastNameHyphen);
    }
    return lastName;
  }

  public void setLastName(final String lastName)
  {
    this.lastName = lastName;
  }

  public String getLastName1()
  {
    return lastName1;
  }

  public void setLastName1(final String lastName1)
  {
    this.lastName1 = lastName1;
    resetLastName();
  }

  public String getLastName2()
  {
    return lastName2;
  }

  public void setLastName2(final String lastName2)
  {
    this.lastName2 = lastName2;
    resetLastName();
  }

  public String getLastNameHyphen()
  {
    return lastNameHyphen;
  }

  public void setLastNameHyphen(final String lastNameHyphen)
  {
    this.lastNameHyphen = lastNameHyphen;
    resetLastName();
  }

  private void resetFirstName()
  {
    firstName = null;
  }

  private void resetLastName()
  {
    lastName = null;
  }

  public String getBirthLastName()
  {
    return birthLastName;
  }

  public void setBirthLastName(final String birthLastName)
  {
    this.birthLastName = birthLastName;
  }

  public String getPrefix()
  {
    return prefix;
  }

  public void setPrefix(final String prefix)
  {
    this.prefix = prefix;
  }

  public String getSuffix()
  {
    return suffix;
  }

  public void setSuffix(final String suffix)
  {
    this.suffix = suffix;
  }

  public String getSpecialization()
  {
    return specialization;
  }

  public void setSpecialization(final String specialization)
  {
    this.specialization = specialization;
  }

  public String getJobTitle()
  {
    return jobTitle;
  }

  public void setJobTitle(final String jobTitle)
  {
    this.jobTitle = jobTitle;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("firstName", firstName)
        .append("firstName1", firstName1)
        .append("firstNameHyphen", firstNameHyphen)
        .append("firstName2", firstName2)
        .append("lastName", lastName)
        .append("lastName1", lastName1)
        .append("lastNameHyphen", lastNameHyphen)
        .append("lastName2", lastName2)
        .append("birthLastName", birthLastName)
        .append("prefix", prefix)
        .append("suffix", suffix)
        .append("specialization", specialization)
        .append("jobTitle", jobTitle);
  }

  public static NameDto createDummy(final String firstName, final String lastName)
  {
    final NameDto name = new NameDto();

    name.setFirstName1(firstName);
    name.setLastName1(lastName);

    return name;
  }
}