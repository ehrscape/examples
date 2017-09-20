package com.marand.thinkmed.medications.dto.pharmacist.review;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

//used in javascript
public class TherapyProblemDescriptionDto extends DataTransferObject
{
  private List<NamedExternalDto> categories = new ArrayList<>();
  private NamedExternalDto outcome;
  private NamedExternalDto impact;
  private String recommendation;

  public List<NamedExternalDto> getCategories()
  {
    return categories;
  }

  public void setCategories(final List<NamedExternalDto> categories)
  {
    this.categories = categories;
  }

  public NamedExternalDto getOutcome()
  {
    return outcome;
  }

  public void setOutcome(final NamedExternalDto outcome)
  {
    this.outcome = outcome;
  }

  public NamedExternalDto getImpact()
  {
    return impact;
  }

  public void setImpact(final NamedExternalDto impact)
  {
    this.impact = impact;
  }

  public String getRecommendation()
  {
    return recommendation;
  }

  public void setRecommendation(final String recommendation)
  {
    this.recommendation = recommendation;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("categories", categories)
        .append("outcome", outcome)
        .append("impact", impact)
        .append("recommendation", recommendation)
    ;
  }
}
