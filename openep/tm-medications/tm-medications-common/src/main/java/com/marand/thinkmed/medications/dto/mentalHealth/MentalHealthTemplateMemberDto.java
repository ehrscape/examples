package com.marand.thinkmed.medications.dto.mentalHealth;

import com.marand.maf.core.data.IdentityDto;
import com.marand.maf.core.data.object.NamedIdentityDto;
import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class MentalHealthTemplateMemberDto extends IdentityDto implements JsonSerializable
{
  private final long medicationId;
  private final MedicationRouteDto route;
  private final NamedIdentityDto mentalHealthTemplate;

  public MentalHealthTemplateMemberDto(
      final long id,
      final long medicationId,
      final MedicationRouteDto route,
      final NamedIdentityDto mentalHealthTemplate)
  {
    super(id);
    this.medicationId = medicationId;
    this.route = route;
    this.mentalHealthTemplate = mentalHealthTemplate;
  }

  public long getMedicationId()
  {
    return medicationId;
  }

  public MedicationRouteDto getRoute()
  {
    return route;
  }

  public NamedIdentityDto getMentalHealthTemplate()
  {
    return mentalHealthTemplate;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb.append("medicationId", medicationId);
    tsb.append("route", route);
    tsb.append("mentalHealthTemplate", mentalHealthTemplate);
  }
}
