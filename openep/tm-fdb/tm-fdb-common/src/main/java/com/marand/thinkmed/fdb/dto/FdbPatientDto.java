package com.marand.thinkmed.fdb.dto;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
public class FdbPatientDto implements JsonSerializable
{
  private FdbNameValue Gender;
  private Long Age;
  private Double Weight;
  private Double BodySurfaceArea;

  public FdbNameValue getGender()
  {
    return Gender;
  }

  public void setGender(final FdbNameValue gender)
  {
    Gender = gender;
  }

  public Long getAge()
  {
    return Age;
  }

  public void setAge(final Long age)
  {
    Age = age;
  }

  public Double getWeight()
  {
    return Weight;
  }

  public void setWeight(final Double weight)
  {
    Weight = weight;
  }

  public Double getBodySurfaceArea()
  {
    return BodySurfaceArea;
  }

  public void setBodySurfaceArea(final Double bodySurfaceArea)
  {
    BodySurfaceArea = bodySurfaceArea;
  }
}
