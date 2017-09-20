package com.marand.thinkmed.medications.dto.mentalHealth;

import com.marand.maf.core.data.object.NamedIdentityDto;
import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class MentalHealthMedicationDto extends NamedIdentityDto implements JsonSerializable
{
  private String genericName;
  private MedicationRouteDto route;

  public MentalHealthMedicationDto(
      final long id,
      final String name,
      final String genericName,
      final MedicationRouteDto route)
  {
    setId(id);
    setName(name);
    this.genericName = genericName;
    this.route = route;
  }

  public MedicationRouteDto getRoute()
  {
    return route;
  }

  public void setRoute(final MedicationRouteDto route)
  {
    this.route = route;
  }

  public String getGenericName()
  {
    return genericName;
  }

  public void setGenericName(final String genericName)
  {
    this.genericName = genericName;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb.append("route", route).append("genericName", genericName);
  }
}
