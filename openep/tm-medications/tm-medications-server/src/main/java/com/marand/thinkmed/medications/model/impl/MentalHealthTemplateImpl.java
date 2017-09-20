package com.marand.thinkmed.medications.model.impl;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.marand.maf.core.hibernate.entity.AbstractCatalogEntity;
import com.marand.thinkmed.medications.model.MedicationRoute;
import com.marand.thinkmed.medications.model.MentalHealthTemplate;
import com.marand.thinkmed.medications.model.MentalHealthTemplateMember;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * @author Nejc Korasa
 */
@Entity
@Table(indexes = @Index(name = "xfMedicationRoute", columnList = "medication_route_id"))
public class MentalHealthTemplateImpl extends AbstractCatalogEntity implements MentalHealthTemplate
{
  private MedicationRoute medicationRoute;
  private List<MentalHealthTemplateMember> mentalHealthTemplateMemberList;

  @Override
  @ManyToOne(targetEntity = MedicationRouteImpl.class, optional = true, fetch = FetchType.LAZY)
  public MedicationRoute getMedicationRoute()
  {
    return medicationRoute;
  }

  @Override
  @OneToMany(targetEntity = MentalHealthTemplateMemberImpl.class, mappedBy = "mentalHealthTemplate", fetch = FetchType.LAZY)
  public List<MentalHealthTemplateMember> getMentalHealthTemplateMemberList()
  {
    return mentalHealthTemplateMemberList;
  }

  @Override
  public void setMentalHealthTemplateMemberList(final List<MentalHealthTemplateMember> mentalHealthTemplateMemberList)
  {
    this.mentalHealthTemplateMemberList = mentalHealthTemplateMemberList;
  }

  @Override
  public void setMedicationRoute(final MedicationRoute medicationRoute)
  {
    this.medicationRoute = medicationRoute;
  }
}
