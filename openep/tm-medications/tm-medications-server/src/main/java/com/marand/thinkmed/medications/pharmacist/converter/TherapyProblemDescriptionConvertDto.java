package com.marand.thinkmed.medications.pharmacist.converter;

import java.util.ArrayList;
import java.util.List;

import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */
public class TherapyProblemDescriptionConvertDto
{
  private List<DvCodedText> categories = new ArrayList<>();
  private DvCodedText outcome;
  private DvCodedText impact;
  private DvText recommendation;

  public List<DvCodedText> getCategories()
  {
    return categories;
  }

  public void setCategories(final List<DvCodedText> categories)
  {
    this.categories = categories;
  }

  public DvCodedText getOutcome()
  {
    return outcome;
  }

  public void setOutcome(final DvCodedText outcome)
  {
    this.outcome = outcome;
  }

  public DvCodedText getImpact()
  {
    return impact;
  }

  public void setImpact(final DvCodedText impact)
  {
    this.impact = impact;
  }

  public DvText getRecommendation()
  {
    return recommendation;
  }

  public void setRecommendation(final DvText recommendation)
  {
    this.recommendation = recommendation;
  }
}
