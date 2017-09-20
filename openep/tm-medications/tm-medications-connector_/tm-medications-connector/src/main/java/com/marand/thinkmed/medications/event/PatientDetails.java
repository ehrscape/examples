package com.marand.thinkmed.medications.event;

import com.marand.thinkmed.api.demographics.data.Gender;
import org.joda.time.DateTime;

/**
 * @author Bostjan Vester
 */
public class PatientDetails
{
  private final String name;
  private final DateTime birthDate;
  private final Gender gender;
  private final String address;

  public PatientDetails(final String name, final DateTime birthDate, final Gender gender, final String address)
  {
    this.name = name;
    this.birthDate = birthDate;
    this.gender = gender;
    this.address = address;
  }

  public String getName()
  {
    return name;
  }

  public DateTime getBirthDate()
  {
    return birthDate;
  }

  public Gender getGender()
  {
    return gender;
  }

  public String getAddress()
  {
    return address;
  }
}
