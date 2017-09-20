package com.marand.thinkmed.medications.dto.mentalHealth;

import com.marand.maf.core.data.object.NamedIdentityDto;
import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class MentalHealthTemplateDto extends NamedIdentityDto implements JsonSerializable
{
  private MedicationRouteDto route;

  public MentalHealthTemplateDto(final long id, final String name, final MedicationRouteDto route)
  {
    setId(id);
    setName(name);
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

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb.append("route", route);
  }
}
