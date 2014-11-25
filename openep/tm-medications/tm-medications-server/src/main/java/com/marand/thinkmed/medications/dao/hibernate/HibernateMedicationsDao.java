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

package com.marand.thinkmed.medications.dao.hibernate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.Pair;
import com.marand.maf.core.data.CatalogIdentity;
import com.marand.maf.core.data.mapper.CatalogIdentityDoMapper;
import com.marand.maf.core.hibernate.query.Alias;
import com.marand.maf.core.hibernate.query.Hql;
import com.marand.maf.core.resultrow.ProcessingException;
import com.marand.maf.core.resultrow.ResultRowProcessor;
import com.marand.maf.core.resultrow.TupleProcessor;
import com.marand.maf.core.resultrow.TwoLevelJoinProcessor;
import com.marand.maf.core.server.catalog.dao.CatalogDao;
import com.marand.thinkmed.api.organization.data.KnownClinic;
import com.marand.thinkmed.medications.MedicationExternalSystemType;
import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.MedicationRouteTypeEnum;
import com.marand.thinkmed.medications.MedicationTypeEnum;
import com.marand.thinkmed.medications.MedicationsExternalValueType;
import com.marand.thinkmed.medications.TherapyJsonAdapter;
import com.marand.thinkmed.medications.TherapyTemplateTypeEnum;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.DoseFormDto;
import com.marand.thinkmed.medications.dto.IngredientDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.MedicationDataForTherapyDto;
import com.marand.thinkmed.medications.dto.MedicationDocumentDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.MedicationSearchDto;
import com.marand.thinkmed.medications.dto.MedicationSimpleDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.TherapyTemplateDto;
import com.marand.thinkmed.medications.dto.TherapyTemplateElementDto;
import com.marand.thinkmed.medications.dto.TherapyTemplatesDto;
import com.marand.thinkmed.medications.mapper.DoseFormDtoMapper;
import com.marand.thinkmed.medications.model.AtcClassification;
import com.marand.thinkmed.medications.model.DoseForm;
import com.marand.thinkmed.medications.model.FdbDiseaseCrossTab;
import com.marand.thinkmed.medications.model.Medication;
import com.marand.thinkmed.medications.model.MedicationBasicUnit;
import com.marand.thinkmed.medications.model.MedicationCustomGroup;
import com.marand.thinkmed.medications.model.MedicationCustomGroupMember;
import com.marand.thinkmed.medications.model.MedicationExternal;
import com.marand.thinkmed.medications.model.MedicationExternalCrossTab;
import com.marand.thinkmed.medications.model.MedicationGeneric;
import com.marand.thinkmed.medications.model.MedicationIngredient;
import com.marand.thinkmed.medications.model.MedicationIngredientLink;
import com.marand.thinkmed.medications.model.MedicationRoute;
import com.marand.thinkmed.medications.model.MedicationRouteLink;
import com.marand.thinkmed.medications.model.MedicationRouteRelation;
import com.marand.thinkmed.medications.model.MedicationType;
import com.marand.thinkmed.medications.model.MedicationVersion;
import com.marand.thinkmed.medications.model.TherapyTemplate;
import com.marand.thinkmed.medications.model.TherapyTemplateElement;
import com.marand.thinkmed.medications.model.impl.DoseFormImpl;
import com.marand.thinkmed.medications.model.impl.MedicationImpl;
import com.marand.thinkmed.medications.model.impl.MedicationIngredientImpl;
import com.marand.thinkmed.medications.model.impl.TherapyTemplateElementImpl;
import com.marand.thinkmed.medications.model.impl.TherapyTemplateImpl;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.util.Assert;

import static com.marand.maf.core.hibernate.query.Alias.effectiveEntities;
import static com.marand.maf.core.hibernate.query.Alias.permanentEntities;
import static com.marand.maf.core.hibernate.query.Criterion.and;
import static com.marand.maf.core.hibernate.query.Criterion.or;

/**
 * @author Mitja Lapajne
 */
public class HibernateMedicationsDao extends HibernateDaoSupport implements MedicationsDao
{
  private static final Alias.Effective<Medication> medication = Alias.forEffectiveEntity(Medication.class);
  private static final Alias.Effective<MedicationVersion> medicationVersion = Alias.forEffectiveEntity(MedicationVersion.class);
  private static final Alias.Effective<MedicationIngredientLink> medicationIngredientLink =
      Alias.forEffectiveEntity(MedicationIngredientLink.class);
  private static final Alias.Effective<MedicationIngredient> medicationIngredient =
      Alias.forEffectiveEntity(MedicationIngredient.class);
  private static final Alias.Effective<MedicationRouteLink> medicationRouteLink =
      Alias.forEffectiveEntity(MedicationRouteLink.class);
  private static final Alias.Effective<MedicationRoute> medicationRoute = Alias.forEffectiveEntity(MedicationRoute.class);
  private static final Alias.Effective<MedicationRouteRelation> medicationRouteRelation =
      Alias.forEffectiveEntity(MedicationRouteRelation.class);
  private static final Alias.Effective<DoseForm> doseForm = Alias.forEffectiveEntity(DoseForm.class);
  private static final Alias.Permanent<FdbDiseaseCrossTab> fdbDiseaseCrossTab = Alias.forPermanentEntity(FdbDiseaseCrossTab.class);
  private static final Alias.Effective<MedicationExternal> medicationExternal = Alias.forEffectiveEntity(MedicationExternal.class);
  private static final Alias.Effective<AtcClassification> atcClassification = Alias.forEffectiveEntity(AtcClassification.class);
  private static final Alias.Permanent<MedicationExternalCrossTab> medicationExternalTranslator =
      Alias.forPermanentEntity(MedicationExternalCrossTab.class);
  private static final Alias.Permanent<MedicationType> medicationType = Alias.forPermanentEntity(MedicationType.class);
  private static final Alias.Permanent<MedicationBasicUnit> medicationBasicUnit =
      Alias.forPermanentEntity(MedicationBasicUnit.class);
  private static final Alias.Effective<MedicationGeneric> medicationGeneric =
      Alias.forEffectiveEntity(MedicationGeneric.class);
  private static final Alias.Permanent<MedicationCustomGroup> medicationCustomGroup =
      Alias.forPermanentEntity(MedicationCustomGroup.class);
  private static final Alias.Permanent<MedicationCustomGroupMember> medicationCustomGroupMember =
      Alias.forPermanentEntity(MedicationCustomGroupMember.class);
  private static final Alias.Permanent<TherapyTemplateImpl> therapyTemplate =
      Alias.forPermanentEntity(TherapyTemplateImpl.class);
  private static final Alias.Permanent<TherapyTemplateElement> therapyTemplateElement =
      Alias.forPermanentEntity(TherapyTemplateElement.class);

  private CatalogDao catalogDao;
  private CatalogIdentityDoMapper<CatalogIdentity> catalogMapper;
  private DoseFormDtoMapper doseFormDtoMapper;
  private MedicationsBo medicationsBo;

  private TherapyDisplayProvider therapyDisplayProvider;

  public void setCatalogDao(final CatalogDao catalogDao)
  {
    this.catalogDao = catalogDao;
  }

  public void setCatalogMapper(final CatalogIdentityDoMapper<CatalogIdentity> catalogMapper)
  {
    this.catalogMapper = catalogMapper;
  }

  public void setDoseFormDtoMapper(final DoseFormDtoMapper doseFormDtoMapper)
  {
    this.doseFormDtoMapper = doseFormDtoMapper;
  }

  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  public void setTherapyDisplayProvider(final TherapyDisplayProvider therapyDisplayProvider)
  {
    this.therapyDisplayProvider = therapyDisplayProvider;
  }

  @Override
  protected void initDao() throws Exception
  {
    Assert.notNull(catalogDao, "catalogDao is required");
    Assert.notNull(catalogMapper, "catalogMapper is required");
    Assert.notNull(doseFormDtoMapper, "doseFormDtoMapper is required");
    Assert.notNull(therapyDisplayProvider, "therapyDisplayProvider is required");
  }

  @Override
  public List<MedicationSimpleDto> findMedications(final DateTime when)
  {
    return getHibernateTemplate().execute(
        new HibernateCallback<List<MedicationSimpleDto>>()
        {
          @Override
          public List<MedicationSimpleDto> doInHibernate(final Session requestSession)
              throws HibernateException, SQLException
          {
            return new Hql()
                .select(
                    medication.id(),
                    medicationVersion.get("name"),
                    medicationVersion.get("active"),
                    medicationGeneric.get("name")
                )
                .from(
                    medication.innerJoin("versions").as(medicationVersion),
                    medicationVersion.leftOuterJoin("medicationGeneric").as(medicationGeneric)
                )
                .where(
                    effectiveEntities(medication, medicationVersion).notDeletedAndEffectiveAt(when)
                ).orderBy(
                    medicationVersion.get("active").desc(),
                    medication.get("sortOrder"),
                    medicationGeneric.get("name"),
                    medicationVersion.get("name")
                )
                .buildQuery(requestSession, Object[].class)
                .list(
                    new TupleProcessor<MedicationSimpleDto>()
                    {
                      @Override
                      protected MedicationSimpleDto process(boolean hasNextTuple) throws ProcessingException
                      {
                        final MedicationSimpleDto dto = new MedicationSimpleDto();
                        dto.setId(next(Long.class));
                        dto.setName(next(String.class));
                        dto.setActive(nextBoolean());
                        dto.setGenericName(next(String.class));
                        return dto;
                      }
                    });
          }
        });
  }

  @Override
  public List<MedicationSearchDto> loadMedicationsTree(final DateTime when)
  {
    return getHibernateTemplate().execute(
        new HibernateCallback<List<MedicationSearchDto>>()
        {
          @Override
          public List<MedicationSearchDto> doInHibernate(final Session requestSession)
              throws HibernateException
          {
            final List<MedicationSearchDto> medications = new Hql()
                .select(
                    medication.id(),
                    medicationVersion.get("name"),
                    medicationVersion.get("active"),
                    medicationGeneric.get("name"),
                    medication.get("vtmId"),
                    medication.get("vmpId"),
                    medication.get("ampId"),
                    medication.get("medicationLevel"),
                    medication.get("orderable")
                )
                .from(
                    medication.innerJoin("versions").as(medicationVersion),
                    medicationVersion.leftOuterJoin("medicationGeneric").as(medicationGeneric)
                )
                .where(
                    medication.get("medicationLevel").isNotNull(),
                    effectiveEntities(medication, medicationVersion).notDeletedAndEffectiveAt(when)
                ).orderBy(
                    medication.get("medicationLevel").desc(),
                    medicationVersion.get("active").desc(),
                    medication.get("sortOrder"),
                    medicationGeneric.get("name"),
                    medicationVersion.get("name")
                )
                .buildQuery(requestSession, Object[].class)
                .list(
                    new ResultRowProcessor<Object[], MedicationSearchDto>()
                    {
                      @Override
                      public MedicationSearchDto process(final Object[] resultRow, final boolean hasNext)
                          throws ProcessingException
                      {
                        final MedicationSimpleDto dto = new MedicationSimpleDto();
                        dto.setId((Long)resultRow[0]);
                        dto.setName((String)resultRow[1]);
                        final Boolean active = (Boolean)resultRow[2];
                        dto.setActive(active);
                        dto.setGenericName((String)resultRow[3]);

                        final MedicationSearchDto searchDto = new MedicationSearchDto();
                        searchDto.setTitle(dto.getName());
                        searchDto.setMedication(dto);

                        final Long vtmId = (Long)resultRow[4];
                        final Long vmpId = (Long)resultRow[5];
                        final Long ampId =(Long)resultRow[6];
                        final MedicationLevelEnum medicationLevel = (MedicationLevelEnum)resultRow[7];
                        final boolean orderable = (Boolean)resultRow[8];
                        searchDto.setUnselectable(!orderable || !active);
                        searchDto.setFolder(searchDto.isUnselectable());

                        if (medicationLevel == MedicationLevelEnum.VTM)
                        {
                          searchDto.setKey(vtmId);
                        }
                        else if (medicationLevel == MedicationLevelEnum.VMP)
                        {
                          searchDto.setKey(vmpId);
                          searchDto.setParentId(vtmId);
                        }
                        else if (medicationLevel == MedicationLevelEnum.AMP)
                        {
                          searchDto.setKey(ampId);
                          searchDto.setParentId(vmpId);
                        }

                        return searchDto;
                      }
                    });

            return convertMedicationListToTree(medications);
          }
        });
  }

  private List<MedicationSearchDto> convertMedicationListToTree(final List<MedicationSearchDto> medications)
  {
    final Map<Long, List<MedicationSearchDto>> medicationsMap = new HashMap<>();
    final List<MedicationSearchDto> topLevelMedications = new ArrayList<>();

    for (final MedicationSearchDto medicationDto : medications)
    {
      final Long parentId = medicationDto.getParentId();
      if (parentId != null)
      {
        if (!medicationsMap.containsKey(parentId))
        {
          medicationsMap.put(parentId, new ArrayList<MedicationSearchDto>());
        }
        medicationsMap.get(parentId).add(medicationDto);
      }
      else
      {
        topLevelMedications.add(medicationDto);
      }
    }

    fillMedicationsTree(medicationsMap, topLevelMedications);

    return topLevelMedications;
  }

  private void fillMedicationsTree(
      final Map<Long, List<MedicationSearchDto>> medicationsMap, final List<MedicationSearchDto> parentMedications)
  {
    for (final MedicationSearchDto parentMedication : parentMedications)
    {
      final List<MedicationSearchDto> childMedications = medicationsMap.get(parentMedication.getKey());
      if (childMedications != null)
      {
        parentMedication.setSublevelMedications(childMedications);
        for (final MedicationSearchDto childMedication : childMedications)
        {
          if (medicationsMap.containsKey(childMedication.getKey()))
          {
            fillMedicationsTree(medicationsMap, childMedications);
          }
        }
      }
    }
  }

  @Override
  public MedicationDataDto getMedicationData(final long medicationId, final DateTime when)
  {
    //returns null if medication not active on "when"

    final Alias.Effective<MedicationRoute> childRoute = Alias.forEffectiveEntity(MedicationRoute.class);
    final Alias.Effective<MedicationRouteRelation> medicationRouteChildRelation =
        Alias.forEffectiveEntity(MedicationRouteRelation.class);

    final MedicationDataDto medicationDataDto = getHibernateTemplate().execute(
        new HibernateCallback<MedicationDataDto>()
        {
          @Override
          public MedicationDataDto doInHibernate(final Session requestSession)
              throws HibernateException, SQLException
          {
            final MedicationDataDto dto = new MedicationDataDto();
            new Hql()
                .select(
                    medicationIngredient,
                    medicationIngredientLink.id(),
                    medicationIngredientLink.get("strengthNumerator"),
                    medicationIngredientLink.get("strengthNumeratorUnit"),
                    medicationIngredientLink.get("strengthDenominator"),
                    medicationIngredientLink.get("strengthDenominatorUnit"),
                    medicationIngredientLink.get("descriptive"),
                    medicationRoute.id(),
                    medicationRoute.get("code"),
                    medicationRoute.get("shortName"),
                    medicationRoute.get("type"),
                    childRoute.id(),
                    childRoute.get("code"),
                    childRoute.get("shortName"),
                    childRoute.get("type"),
                    medicationRouteLink.get("defaultRoute"),
                    doseForm,
                    medicationBasicUnit.get("name"),
                    medicationType.id()
                )
                .from(
                    medication.leftOuterJoin("ingredients").as(medicationIngredientLink)
                        .with(medicationIngredientLink.notDeletedAndEffectiveAt(when)),
                    medicationIngredientLink.leftOuterJoin("ingredient").as(medicationIngredient)
                        .with(medicationIngredient.notDeletedAndEffectiveAt(when)),
                    medication.innerJoin("versions").as(medicationVersion),
                    medicationVersion.leftOuterJoin("doseForm").as(doseForm),
                    medication.innerJoin("routes").as(medicationRouteLink),
                    medicationRouteLink.innerJoin("route").as(medicationRoute),
                    medicationRoute.leftOuterJoin("childRelations").as(medicationRouteChildRelation)
                        .with(medicationRouteChildRelation.notDeletedAndEffectiveAt(when)),
                    medicationRouteChildRelation.leftOuterJoin("childRoute").as(childRoute)
                        .with(childRoute.notDeletedAndEffectiveAt(when)),
                    medicationVersion.leftOuterJoin("basicUnit").as(medicationBasicUnit),
                    medication.leftOuterJoin("types").as(medicationType)
                        .with(
                            medicationType.get("type").eq(MedicationTypeEnum.ANTIBIOTIC),
                            medicationType.notDeleted())
                )
                .where(
                    medication.id().eq(medicationId),
                    effectiveEntities(medicationRouteLink, medicationVersion).notDeletedAndEffectiveAt(when)
                )
                .buildQuery(requestSession, Object[].class)
                .list(
                    new TupleProcessor<Void>()
                    {
                      @Override
                      protected Void process(boolean hasNextTuple) throws ProcessingException
                      {
                        final MedicationIngredientImpl aMedicationIngredient = next(MedicationIngredientImpl.class);
                        final Long medicationIngredientLinkId = next(Long.class);
                        final Double strengthNumerator = next(Double.class);
                        final String strengthNumeratorUnit = next(String.class);
                        final Double strengthDenominator = next(Double.class);
                        final String strengthDenominatorUnit = next(String.class);
                        final Boolean descriptiveIngredient = next(Boolean.class);

                        if (aMedicationIngredient != null)
                        {
                          final MedicationIngredientDto medicationIngredientDto = new MedicationIngredientDto();

                          final IngredientDto ingredientDto = catalogMapper.map(aMedicationIngredient, IngredientDto.class);
                          medicationIngredientDto.setIngredient(ingredientDto);
                          medicationIngredientDto.setId(medicationIngredientLinkId);
                          medicationIngredientDto.setStrengthNumerator(strengthNumerator);
                          medicationIngredientDto.setStrengthNumeratorUnit(strengthNumeratorUnit);
                          medicationIngredientDto.setStrengthDenominator(strengthDenominator);
                          medicationIngredientDto.setStrengthDenominatorUnit(strengthDenominatorUnit);
                          medicationIngredientDto.setDescriptive(descriptiveIngredient);

                          if (descriptiveIngredient)
                          {
                            dto.setDescriptiveIngredient(medicationIngredientDto);
                          }
                          else if (!dto.getMedicationIngredients().contains(medicationIngredientDto))
                          {
                            dto.getMedicationIngredients().add(medicationIngredientDto);
                          }
                        }

                        final Long routeId = next(Long.class);
                        final String routeCode = next(String.class);
                        final String routeShortName = next(String.class);
                        final MedicationRouteTypeEnum routeType = next(MedicationRouteTypeEnum.class);
                        final Long childRouteId = next(Long.class);
                        final String childRouteCode = next(String.class);
                        final String childRouteShortName = next(String.class);
                        final MedicationRouteTypeEnum childRouteType = next(MedicationRouteTypeEnum.class);

                        Boolean defaultRoute = next(Boolean.class);

                        final MedicationRouteDto routeDto = new MedicationRouteDto();
                        if (childRouteId != null)
                        {
                          defaultRoute = false;
                          routeDto.setId(childRouteId);
                          routeDto.setCode(childRouteCode);
                          routeDto.setName(childRouteShortName);
                          routeDto.setType(childRouteType);
                        }
                        else
                        {
                          routeDto.setId(routeId);
                          routeDto.setCode(routeCode);
                          routeDto.setName(routeShortName);
                          routeDto.setType(routeType);
                        }

                        if (!dto.getRoutes().contains(routeDto))
                        {
                          dto.getRoutes().add(routeDto);
                          if (defaultRoute)
                          {
                            dto.setDefaultRoute(routeDto);
                          }
                        }
                        else if (defaultRoute)
                        {
                          dto.setDefaultRoute(routeDto);
                        }

                        final DoseForm aDoseForm = next(DoseFormImpl.class);
                        if (aDoseForm != null)
                        {
                          dto.setDoseForm(doseFormDtoMapper.map(aDoseForm, DoseFormDto.class));
                          dto.setDoseSplitFactor(aDoseForm.getSplitFactor());
                        }
                        dto.setBasicUnit(next(String.class));

                        final Long medicationTypeId = next(Long.class);
                        dto.setAntibiotic(medicationTypeId != null);
                        return null;
                      }
                    });
            return dto;
          }
        });

    if (medicationDataDto != null)
    {
      final MedicationDto medicationDto = getMedicationById(medicationId, when);
      medicationDataDto.setMedication(medicationDto);
      final List<MedicationDocumentDto> documents = getMedicationDocuments(medicationDataDto.getMedication().getId());
      medicationDataDto.setMedicationDocuments(documents);
      return medicationDataDto;
    }
    return null;
  }

  private List<MedicationDocumentDto> getMedicationDocuments(final long medicationId)
  {
    return getHibernateTemplate().execute(
        new HibernateCallback<List<MedicationDocumentDto>>()
        {
          @Override
          public List<MedicationDocumentDto> doInHibernate(final Session session) throws HibernateException
          {
            return new Hql()
                .select(
                    medicationExternal.get("externalId"),
                    medicationExternal.get("externalSystem")
                )
                .from(
                    medicationExternal.innerJoin("medication").as(medication)
                )
                .where(
                    medication.id().eq(medicationId),
                    medicationExternal.get("externalSystemType").eq(MedicationExternalSystemType.DOCUMENTS_PROVIDER),
                    medicationExternal.notDeleted()
                )
                .buildQuery(session, Object[].class)
                .list(
                    new TupleProcessor<MedicationDocumentDto>()
                    {
                      @Override
                      protected MedicationDocumentDto process(boolean hasNextTuple) throws ProcessingException
                      {
                        final MedicationDocumentDto dto = new MedicationDocumentDto();
                        dto.setDocumentReference(next(String.class));
                        dto.setExternalSystem(next(String.class));
                        return dto;
                      }
                    });
          }
        });
  }

  @Override
  public String getMedicationExternalId(final String externalSystem, final long medicationId, final DateTime when)
  {
    return getHibernateTemplate().execute(
        new HibernateCallback<String>()
        {
          @Override
          public String doInHibernate(final Session session) throws HibernateException, SQLException
          {
            return new Hql()
                .select(
                    medicationExternal.get("externalId")
                )
                .from(
                    medicationExternal
                )
                .where(
                    effectiveEntities(medicationExternal).notDeletedAndEffectiveAt(when),
                    medicationExternal.get("externalSystem").eq(externalSystem),
                    medicationExternal.get("medication").id().eq(medicationId)
                )
                .buildQuery(session, String.class)
                .getSingleRowOrNull();
          }
        });
  }

  @Override
  public MedicationIngredientDto getMedicationDefiningIngredient(final long medicationId, final DateTime when)
  {
    return getHibernateTemplate().execute(
        new HibernateCallback<MedicationIngredientDto>()
        {
          @Override
          public MedicationIngredientDto doInHibernate(final Session session) throws HibernateException
          {
            final List<MedicationIngredientDto> ingredientsList = new Hql()
                .select(
                    medicationIngredient,
                    medicationIngredientLink.id(),
                    medicationIngredientLink.get("strengthNumerator"),
                    medicationIngredientLink.get("strengthNumeratorUnit"),
                    medicationIngredientLink.get("strengthDenominator"),
                    medicationIngredientLink.get("strengthDenominatorUnit"),
                    medicationIngredientLink.get("descriptive")
                )
                .from(
                    medication.innerJoin("ingredients").as(medicationIngredientLink),
                    medicationIngredientLink.innerJoin("ingredient").as(medicationIngredient)
                )
                .where(
                    medication.id().eq(medicationId),
                    effectiveEntities(medicationIngredientLink, medicationIngredient).notDeletedAndEffectiveAt(when)
                )
                .buildQuery(session, Object[].class)
                .list(
                    new TupleProcessor<MedicationIngredientDto>()
                    {
                      @Override
                      protected MedicationIngredientDto process(final boolean hasNextTuple) throws ProcessingException
                      {
                        final MedicationIngredientDto dto = new MedicationIngredientDto();
                        final IngredientDto ingredientDto = catalogMapper.map(
                            next(MedicationIngredientImpl.class),
                            IngredientDto.class);
                        dto.setIngredient(ingredientDto);
                        dto.setId(nextLong());
                        dto.setStrengthNumerator(next(Double.class));
                        dto.setStrengthNumeratorUnit(next(String.class));
                        dto.setStrengthDenominator(next(Double.class));
                        dto.setStrengthDenominatorUnit(next(String.class));
                        dto.setDescriptive(next(Boolean.class));
                        return dto;
                      }
                    });
            if (ingredientsList.size() == 1)
            {
              return ingredientsList.get(0);
            }
            for (final MedicationIngredientDto ingredient : ingredientsList)
            {
              if (ingredient.isDescriptive())
              {
                return ingredient;
              }
            }
            return null;
          }
        });
  }

  @Override
  public List<String> getIcd9Codes(final String icd10Code)
  {
    if (StringUtils.isBlank(icd10Code))
    {
      return Collections.emptyList();
    }
    return getHibernateTemplate().execute(
        new HibernateCallback<List<String>>()
        {
          @Override
          public List<String> doInHibernate(final Session session) throws HibernateException, SQLException
          {
            return new Hql()
                .select(
                    fdbDiseaseCrossTab.get("icd9Code")
                )
                .from(
                    fdbDiseaseCrossTab
                )
                .where(
                    permanentEntities(fdbDiseaseCrossTab).notDeleted(),
                    fdbDiseaseCrossTab.get("icd10Code").eq(icd10Code)
                )
                .buildQuery(session, String.class)
                .list();
          }
        });
  }

  @Override
  public Map<Long, MedicationDataForTherapyDto> getMedicationDataForTherapies(
      final Set<Long> medicationIds,
      @Nullable final KnownClinic department,
      final DateTime when)
  {
    final Map<Long, MedicationDataForTherapyDto> medicationDataMap = new HashMap<>();

    final Alias.Effective<AtcClassification> topAtcClassification = Alias.forEffectiveEntity(AtcClassification.class);

    getHibernateTemplate().execute(
        new HibernateCallback<Void>()
        {
          @Override
          public Void doInHibernate(final Session session) throws HibernateException
          {
            new Hql()
                .select(
                    medication.id(),
                    medicationGeneric.get("name"),
                    topAtcClassification.get("code"),
                    topAtcClassification.get("name"),
                    medicationType.id(),
                    medicationCustomGroup.get("name"),
                    medicationCustomGroup.get("organizationalEntityName"),
                    medicationCustomGroup.get("sortOrder")
                )
                .from(
                    medication.innerJoin("versions").as(medicationVersion),
                    medicationVersion.leftOuterJoin("medicationGeneric").as(medicationGeneric),
                    medicationVersion.leftOuterJoin("atcClassification").as(atcClassification),
                    atcClassification.leftOuterJoin("topParent").as(topAtcClassification),
                    medication.leftOuterJoin("customGroupMembers").as(medicationCustomGroupMember)
                        .with(
                            medicationCustomGroupMember.notDeleted()),
                    medicationCustomGroupMember.leftOuterJoin("medicationCustomGroup").as(medicationCustomGroup),
                    medication.leftOuterJoin("types").as(medicationType)
                        .with(
                            medicationType.get("type").eq(MedicationTypeEnum.ANTIBIOTIC),
                            medicationType.notDeleted())
                )
                .where(
                    medication.id().in(medicationIds),
                    medicationVersion.notDeletedAndEffectiveAt(when)
                )
                .buildQuery(session, Object[].class)
                .list(
                    new TupleProcessor<Void>()
                    {
                      @Override
                      protected Void process(final boolean hasNextTuple) throws ProcessingException
                      {
                        final Long medicationId = nextLong();
                        MedicationDataForTherapyDto dto = new MedicationDataForTherapyDto();
                        dto.setGenericName(next(String.class));
                        dto.setAtcCode(next(String.class));
                        dto.setAtcName(next(String.class));
                        dto.setAntibiotic(nextLong() != null);

                        final String customGroupName = next(String.class);
                        final String customGroupOeName = next(String.class);
                        final Integer customGroupSortOrder = next(Integer.class);
                        final KnownClinic oeEnum = KnownClinic.Utils.fromName(customGroupOeName);

                        final MedicationDataForTherapyDto oldDto = medicationDataMap.get(medicationId);
                        if (oldDto != null)
                        {
                          dto = oldDto;
                        }
                        if (department != null && oeEnum != null && oeEnum.equals(department))
                        {
                          dto.setCustomGroupName(customGroupName);
                          dto.setCustomGroupSortOrder(customGroupSortOrder);
                        }
                        medicationDataMap.put(medicationId, dto);
                        return null;
                      }
                    });
            return null;
          }
        });
    return medicationDataMap;
  }

  @Override
  public List<MedicationDto> findSimilarMedications(
      final long medicationId,
      final String routeCode,
      final DateTime when)
  {
    //Similar medications have same generic, route, atc and custom group

    final Alias.Effective<Medication> referenceMedication = Alias.forEffectiveEntity(Medication.class);
    final Alias.Effective<MedicationVersion> referenceMedicationVersion = Alias.forEffectiveEntity(MedicationVersion.class);
    final Alias.Effective<MedicationGeneric> referenceGeneric = Alias.forEffectiveEntity(MedicationGeneric.class);
    final Alias.Effective<MedicationRoute> childRoute = Alias.forEffectiveEntity(MedicationRoute.class);
    final Alias.Effective<MedicationRouteRelation> medicationRouteChildRelation =
        Alias.forEffectiveEntity(MedicationRouteRelation.class);
    final Alias.Effective<AtcClassification> referenceAtcClassification = Alias.forEffectiveEntity(AtcClassification.class);
    final Alias.Effective<AtcClassification> referenceTopAtcClassification = Alias.forEffectiveEntity(AtcClassification.class);
    final Alias.Effective<AtcClassification> topAtcClassification = Alias.forEffectiveEntity(AtcClassification.class);

    return getHibernateTemplate().execute(
        new HibernateCallback<List<MedicationDto>>()
        {
          @Override
          public List<MedicationDto> doInHibernate(final Session requestSession) throws HibernateException
          {
            final Hql hql = new Hql()
                .select(
                    medication.id(),
                    medicationVersion.get("name"),
                    medicationVersion.get("shortName"),
                    medicationGeneric.get("name")
                )
                .distinct()
                .from(
                    referenceMedication.innerJoin("versions").as(referenceMedicationVersion),
                    referenceMedicationVersion.innerJoin("medicationGeneric").as(referenceGeneric),
                    referenceMedicationVersion.innerJoin("atcClassification").as(referenceAtcClassification),
                    referenceAtcClassification.innerJoin("topParent").as(referenceTopAtcClassification),
                    medication.innerJoin("versions").as(medicationVersion),
                    medicationVersion.innerJoin("medicationGeneric").as(medicationGeneric),
                    medicationVersion.innerJoin("atcClassification").as(atcClassification),
                    atcClassification.innerJoin("topParent").as(topAtcClassification)
                );

            return hql.from(
                medication.innerJoin("routes").as(medicationRouteLink),
                medicationRouteLink.innerJoin("route").as(medicationRoute),
                medicationRoute.leftOuterJoin("childRelations").as(medicationRouteChildRelation)
                    .with(medicationRouteChildRelation.notDeletedAndEffectiveAt(when)),
                medicationRouteChildRelation.leftOuterJoin("childRoute").as(childRoute)
                    .with(childRoute.notDeletedAndEffectiveAt(when))
            )
                .where(
                    referenceMedication.id().eq(medicationId),
                    medicationGeneric.id().eq(referenceGeneric.id()),
                    topAtcClassification.id().eq(referenceTopAtcClassification.id()),
                    or(routeCode == null, medicationRoute.get("code").eq(routeCode), childRoute.get("code").eq(routeCode)),
                    effectiveEntities(medicationVersion, referenceMedicationVersion).notDeletedAndEffectiveAt(when)
                ).orderBy(medicationVersion.get("name"))
                .buildQuery(requestSession, Object[].class)
                .list(
                    new TupleProcessor<MedicationDto>()
                    {
                      @Override
                      protected MedicationDto process(boolean hasNextTuple) throws ProcessingException
                      {
                        final MedicationDto dto = new MedicationDto();
                        dto.setId(next(Long.class));
                        dto.setName(next(String.class));
                        dto.setShortName(next(String.class));
                        dto.setGenericName(next(String.class));
                        return dto;
                      }
                    });
          }
        });
  }

  @Override
  public List<MedicationDto> getMedicationProducts(
      final long medicationId,
      final String routeCode,
      final DateTime when)
  {
    final Medication medicationEntity = getHibernateTemplate().load(MedicationImpl.class, medicationId);
    final MedicationLevelEnum medicationLevel = medicationEntity.getMedicationLevel();

    return getHibernateTemplate().execute(
        new HibernateCallback<List<MedicationDto>>()
        {
          @Override
          public List<MedicationDto> doInHibernate(final Session requestSession) throws HibernateException
          {
            final Hql hql = new Hql()
                .select(
                    medication.id(),
                    medicationVersion.get("name"),
                    medicationVersion.get("shortName"),
                    medicationGeneric.get("name")
                )
                .from(
                    medication.innerJoin("versions").as(medicationVersion),
                    medicationVersion.leftOuterJoin("medicationGeneric").as(medicationGeneric),
                    medication.innerJoin("routes").as(medicationRouteLink),
                    medicationRouteLink.innerJoin("route").as(medicationRoute)
                )
                .where(
                    medication.get("medicationLevel").eq(MedicationLevelEnum.AMP),
                    or(routeCode == null, medicationRoute.get("code").eq(routeCode)),
                    effectiveEntities(medicationVersion, medicationRouteLink).notDeletedAndEffectiveAt(when)
                ).orderBy(medicationVersion.get("name"));

            if (medicationLevel == MedicationLevelEnum.VTM)
            {
              hql.where(medication.get("vtmId").eq(medicationEntity.getVtmId()));
            }
            else if (medicationLevel == MedicationLevelEnum.VMP)
            {
              hql.where(medication.get("vmpId").eq(medicationEntity.getVmpId()));
            }
            else if (medicationLevel == MedicationLevelEnum.AMP)
            {
              hql.where(medication.get("ampId").eq(medicationEntity.getAmpId()));
            }
            else
            {
              throw new IllegalArgumentException("Medication level " + medicationLevel + " not jet supported.");
            }
            return hql
                .buildQuery(requestSession, Object[].class)
                .list(
                    new TupleProcessor<MedicationDto>()
                    {
                      @Override
                      protected MedicationDto process(final boolean hasNextTuple) throws ProcessingException
                      {
                        final MedicationDto dto = new MedicationDto();
                        dto.setId(next(Long.class));
                        dto.setName(next(String.class));
                        dto.setShortName(next(String.class));
                        dto.setGenericName(next(String.class));
                        return dto;
                      }
                    });
          }
        });
  }

  @Override
  public MedicationDto getMedicationById(final Long medicationId, final DateTime when)
  {
    Preconditions.checkNotNull(medicationId);
    return getHibernateTemplate().execute(
        new HibernateCallback<MedicationDto>()
        {
          @Override
          public MedicationDto doInHibernate(final Session session) throws HibernateException, SQLException
          {
            return new Hql()
                .select(
                    medication.id(),
                    medicationVersion.get("name"),
                    medicationVersion.get("shortName"),
                    medicationGeneric.get("name"),
                    medicationType.get("type")
                )
                .from(
                    medication.innerJoin("versions").as(medicationVersion),
                    medicationVersion.leftOuterJoin("medicationGeneric").as(medicationGeneric),
                    medication.leftOuterJoin("types").as(medicationType)
                        .with(
                            medicationType.get("type").in(MedicationTypeEnum.MAIN_TYPES),
                            medicationType.notDeleted())
                )
                .where(
                    medication.id().eq(medicationId),
                    effectiveEntities(medicationVersion).notDeletedAndEffectiveAt(when))

                .buildQuery(session, Object[].class)
                .getSingleRowOrNull(
                    new TupleProcessor<MedicationDto>()
                    {
                      @Override
                      protected MedicationDto process(boolean hasNextTuple) throws ProcessingException
                      {
                        final MedicationDto dto = new MedicationDto();
                        dto.setId(next(Long.class));
                        dto.setName(next(String.class));
                        dto.setShortName(next(String.class));
                        dto.setGenericName(next(String.class));
                        MedicationTypeEnum medicationTypeEnum = next(MedicationTypeEnum.class);
                        if (medicationTypeEnum == null)
                        {
                          medicationTypeEnum = MedicationTypeEnum.MEDICATION;
                        }
                        dto.setMedicationType(medicationTypeEnum);
                        return dto;
                      }
                    });
          }
        });
  }

  @Override
  public DoseFormDto getDoseFormByCode(final String doseFormCode, final DateTime when)
  {
    final DoseFormImpl aDoseForm = catalogDao.findCatalogValueByCode(DoseFormImpl.class, doseFormCode, when);
    return doseFormDtoMapper.map(aDoseForm);
  }

  @Override
  public Map<String, String> getMedicationExternalValues(
      final String externalSystem, final MedicationsExternalValueType valueType, final Set<String> valuesSet)
  {
    final Map<String, String> valuesMap = new HashMap<>();
    if (valuesSet.isEmpty())
    {
      return valuesMap;
    }
    getHibernateTemplate().execute(
        new HibernateCallback<Void>()
        {
          @Override
          public Void doInHibernate(final Session session) throws HibernateException
          {
            new Hql()
                .select(
                    medicationExternalTranslator.get("value"),
                    medicationExternalTranslator.get("externalValue")
                )
                .from(
                    medicationExternalTranslator
                )
                .where(
                    medicationExternalTranslator.get("externalSystem").eq(externalSystem),
                    medicationExternalTranslator.get("valueType").eq(valueType),
                    medicationExternalTranslator.get("value").in(valuesSet),
                    medicationExternalTranslator.notDeleted()
                )
                .buildQuery(session, Object[].class)
                .list(
                    new TupleProcessor<Void>()
                    {
                      @Override
                      protected Void process(boolean hasNextTuple) throws ProcessingException
                      {
                        final String value = next(String.class);
                        final String externalValue = next(String.class);
                        valuesMap.put(value, externalValue);

                        return null;
                      }
                    });
            return null;
          }
        });
    return valuesMap;
  }

  @Override
  public List<MedicationRouteDto> getRoutes(final DateTime when)
  {
    return getHibernateTemplate().execute(
        new HibernateCallback<List<MedicationRouteDto>>()
        {
          @Override
          public List<MedicationRouteDto> doInHibernate(final Session session) throws HibernateException
          {
            return new Hql()
                .select(
                    medicationRoute.id(),
                    medicationRoute.get("code"),
                    medicationRoute.get("shortName"),
                    medicationRoute.get("type")
                )
                .from(
                    medicationRoute.leftOuterJoin("childRelations").as(medicationRouteRelation)
                        .with(medicationRouteRelation.notDeletedAndEffectiveAt(when))
                )
                .where(
                    medicationRoute.notDeletedAndEffectiveAt(when),
                    medicationRouteRelation.id().isNull(),
                    medicationRoute.get("shortName").isNotNull()
                )
                .orderBy(medicationRoute.get("sortOrder"))
                .buildQuery(session, Object[].class)
                .list(
                    new TupleProcessor<MedicationRouteDto>()
                    {
                      @Override
                      protected MedicationRouteDto process(boolean hasNextTuple) throws ProcessingException
                      {
                        final MedicationRouteDto routeDto = new MedicationRouteDto();
                        routeDto.setId(nextLong());
                        routeDto.setCode(next(String.class));
                        routeDto.setName(next(String.class));
                        routeDto.setType(next(MedicationRouteTypeEnum.class));
                        return routeDto;
                      }
                    });
          }
        });
  }

  @Override
  public List<DoseFormDto> getDoseForms(final DateTime when)
  {
    return getHibernateTemplate().execute(
        new HibernateCallback<List<DoseFormDto>>()
        {
          @Override
          public List<DoseFormDto> doInHibernate(final Session session) throws HibernateException
          {
            return new Hql()
                .select(
                    doseForm
                )
                .from(
                    doseForm
                )
                .where(
                    doseForm.notDeletedAndEffectiveAt(when)
                )
                .buildQuery(session, DoseForm.class)
                .list(
                    new ResultRowProcessor<DoseForm, DoseFormDto>()
                    {
                      @Override
                      public DoseFormDto process(final DoseForm resultRow, boolean hasNext) throws ProcessingException
                      {
                        return doseFormDtoMapper.map(resultRow);
                      }
                    });
          }
        });
  }

  @Override
  public List<String> getMedicationBasicUnits()
  {
    return getHibernateTemplate().execute(
        new HibernateCallback<List<String>>()
        {
          @Override
          public List<String> doInHibernate(final Session session) throws HibernateException
          {
            return new Hql()
                .select(
                    medicationBasicUnit.get("name")
                )
                .from(
                    medicationBasicUnit
                )
                .where(
                    medicationBasicUnit.notDeleted()
                )
                .buildQuery(session, String.class)
                .list();
          }
        });
  }

  @Override
  public Map<Long, Pair<String, Integer>> getCustomGroupNameSortOrderMap(
      final String knownOrganizationalEntityName,
      final Set<Long> medicationIds)
  {
    if (medicationIds.isEmpty())
    {
      return new HashMap<>();
    }
    final Map<Long, Pair<String, Integer>> customGroupNameSortOrderMap = new HashMap<>();
    getHibernateTemplate().execute(
        new HibernateCallback<List<Void>>()
        {
          @Override
          public List<Void> doInHibernate(final Session session) throws HibernateException
          {
            return new Hql()
                .select(
                    medicationCustomGroup.get("name"),
                    medicationCustomGroup.get("sortOrder"),
                    medication.id()
                )
                .from(
                    medication.innerJoin("customGroupMembers").as(medicationCustomGroupMember),
                    medicationCustomGroupMember.innerJoin("medicationCustomGroup").as(medicationCustomGroup)
                )
                .where(
                    medication.id().in(medicationIds),
                    medicationCustomGroup.get("organizationalEntityName").eq(knownOrganizationalEntityName)
                )
                .buildQuery(session, Object[].class)
                .list(
                    new TupleProcessor<Void>()
                    {
                      @Override
                      protected Void process(boolean hasNextTuple) throws ProcessingException
                      {
                        final Pair<String, Integer> nameSortOrderPair = Pair.of(next(String.class), next(Integer.class));
                        customGroupNameSortOrderMap.put(next(Long.class), nameSortOrderPair);
                        return null;
                      }
                    }
                );
          }
        });
    return customGroupNameSortOrderMap;
  }

  @Override
  public List<String> getCustomGroupNames(final String knownOrganizationalEntityName)
  {
    return getHibernateTemplate().execute(
        new HibernateCallback<List<String>>()
        {
          @Override
          public List<String> doInHibernate(final Session session) throws HibernateException
          {
            return new Hql()
                .select(
                    medicationCustomGroup.get("name"),
                    medicationCustomGroup.get("sortOrder")
                )
                .distinct()
                .from(
                    medicationCustomGroupMember.innerJoin("medicationCustomGroup").as(medicationCustomGroup)
                )
                .where(
                    medicationCustomGroup.get("organizationalEntityName").eq(knownOrganizationalEntityName)
                )
                .orderBy(medicationCustomGroup.get("sortOrder"))
                .buildQuery(session, Object[].class)
                .list(
                    new TupleProcessor<String>()
                    {
                      @Override
                      protected String process(final boolean hasNextTuple) throws ProcessingException
                      {
                        return nextString();
                      }
                    });
          }
        }
    );
  }

  @Override
  public TherapyTemplatesDto getTherapyTemplates(
      final Long userId,
      @Nullable final Long departmentId,
      @Nullable final Long patientId,
      @Nullable final Double referenceWeight,
      @Nullable final Double height,
      final DateTime when,
      final Locale locale)
  {
    return getHibernateTemplate().execute(
        new HibernateCallback<TherapyTemplatesDto>()
        {
          @Override
          public TherapyTemplatesDto doInHibernate(final Session session) throws HibernateException
          {

            final TherapyTemplatesDto allTemplatesDto = new TherapyTemplatesDto();
            new Hql()
                .select(
                    therapyTemplate.id(),
                    therapyTemplate.get("version"),
                    therapyTemplate.get("name"),
                    therapyTemplate.get("type"),
                    therapyTemplate.get("userId"),
                    therapyTemplate.get("departmentId"),
                    therapyTemplate.get("patientId"),
                    therapyTemplateElement.get("therapy"),
                    therapyTemplateElement.get("completed")
                )
                .from(
                    therapyTemplate.innerJoin("therapyTemplateElements").as(therapyTemplateElement)
                )
                .where(
                    or(
                        therapyTemplate.get("userId").eq(userId),
                        and(patientId != null, therapyTemplate.get("patientId").eq(patientId)),
                        and(departmentId != null, therapyTemplate.get("departmentId").eq(departmentId))
                    ),
                    permanentEntities(therapyTemplate, therapyTemplateElement).notDeleted()
                )
                .orderBy(therapyTemplate.get("name"))
                .buildQuery(session, Object[].class)
                .list(
                    new TwoLevelJoinProcessor.ToList<TherapyTemplateDto, TherapyTemplateElementDto>()
                    {
                      @Override
                      protected TherapyTemplateDto mapParent()
                      {
                        final TherapyTemplateDto templateDto = new TherapyTemplateDto();
                        templateDto.setId(nextLong());
                        templateDto.setVersion(next(Integer.class));
                        templateDto.setName(next(String.class));
                        templateDto.setType(next(TherapyTemplateTypeEnum.class));
                        templateDto.setUserId(nextLong());
                        templateDto.setDepartmentId(nextLong());
                        templateDto.setPatientId(nextLong());

                        if (templateDto.getType() == TherapyTemplateTypeEnum.ORGANIZATIONAL)
                        {
                          allTemplatesDto.getOrganizationTemplates().add(templateDto);
                        }
                        else if (templateDto.getType() == TherapyTemplateTypeEnum.USER)
                        {
                          allTemplatesDto.getUserTemplates().add(templateDto);
                        }
                        else if (templateDto.getType() == TherapyTemplateTypeEnum.PATIENT)
                        {
                          allTemplatesDto.getPatientTemplates().add(templateDto);
                        }
                        return templateDto;
                      }

                      @Override
                      protected TherapyTemplateElementDto mapChild()
                      {
                        final TherapyTemplateElementDto templateElementDto = new TherapyTemplateElementDto();
                        final String therapyJson = next(String.class);
                        final TherapyDto therapyDto =
                            JsonUtil.fromJson(
                                therapyJson, TherapyDto.class, Lists.newArrayList(TherapyJsonAdapter.INSTANCE));
                        if (therapyDto instanceof ComplexTherapyDto && referenceWeight != null)
                        {
                          if (((ComplexTherapyDto)therapyDto).isContinuousInfusion())
                          {
                            medicationsBo.fillInfusionRateFromFormula(
                                (ComplexTherapyDto)therapyDto,
                                referenceWeight,
                                height,
                                when);
                          }
                          else
                          {
                            medicationsBo.fillInfusionFormulaFromRate(
                                (ComplexTherapyDto)therapyDto,
                                referenceWeight,
                                height,
                                when);
                          }
                        }
                        therapyDisplayProvider.fillDisplayValues(therapyDto, true, true, locale);
                        templateElementDto.setTherapy(therapyDto);
                        templateElementDto.setCompleted(next(Boolean.class));
                        return templateElementDto;
                      }

                      @Override
                      protected void associate(
                          final TherapyTemplateDto parentDto, final TherapyTemplateElementDto childDto)
                      {
                        parentDto.getTemplateElements().add(childDto);
                      }
                    });
            return allTemplatesDto;
          }
        });
  }

  @Override
  public long saveTherapyTemplate(final TherapyTemplateDto templateDto)
  {
    Preconditions.checkArgument(
        BooleanUtils.xor(
            new Boolean[]{
                templateDto.getDepartmentId() != null,
                templateDto.getUserId() != null,
                templateDto.getPatientId() != null}));

    if (templateDto.getId() > 0L)
    {
      deleteTherapyTemplate(templateDto.getId(), templateDto.getVersion());
    }

    final TherapyTemplate template = new TherapyTemplateImpl();
    template.setName(templateDto.getName());
    template.setType(templateDto.getType());
    template.setUserId(templateDto.getUserId());
    template.setPatientId(templateDto.getPatientId());
    template.setDepartmentId(templateDto.getDepartmentId());
    getHibernateTemplate().save(template);

    for (final TherapyTemplateElementDto templateElementDto : templateDto.getTemplateElements())
    {
      final TherapyTemplateElement element = new TherapyTemplateElementImpl();
      final TherapyDto therapyDto = templateElementDto.getTherapy();
      therapyDto.setStart(null);
      therapyDto.setEnd(null);
      element.setTherapy(JsonUtil.toJson(therapyDto));
      element.setCompleted(templateElementDto.isCompleted());
      element.setTherapyTemplate(template);
      getHibernateTemplate().save(element);
    }
    return template.getId();
  }

  @Override
  public void deleteTherapyTemplate(final long templateId)
  {
    deleteTherapyTemplate(templateId, null);
  }

  private void deleteTherapyTemplate(final long templateId, final Integer templateVersion)
  {
    final TherapyTemplate template = getHibernateTemplate().load(TherapyTemplateImpl.class, templateId);
    if (templateVersion != null)
    {
      template.setVersion(templateVersion);
    }
    template.setDeleted(true);
    for (final TherapyTemplateElement element : template.getTherapyTemplateElements())
    {
      element.setDeleted(true);
    }
  }

  @Override
  public Map<Long, MedicationLevelEnum> getMedicationsLevels(final Set<Long> medicationIds)
  {
    final Map<Long, MedicationLevelEnum> medicationLevelMap = new HashMap<>();
    getHibernateTemplate().execute(
        new HibernateCallback<Void>()
        {
          @Override
          public Void doInHibernate(final Session session) throws HibernateException
          {
            new Hql()
                .select(
                    medication.id(),
                    medication.get("medicationLevel")
                )
                .from(
                    medication
                )
                .where(
                    medication.id().in(medicationIds)
                )
                .buildQuery(session, Object[].class)
                .list(
                    new TupleProcessor<Void>()
                    {
                      @Override
                      protected Void process(final boolean hasNextTuple) throws ProcessingException
                      {
                        medicationLevelMap.put(nextLong(), next(MedicationLevelEnum.class));
                        return null;
                      }
                    });
            return null;
          }
        }
    );
    return medicationLevelMap;
  }

  @Override
  public Map<Long, Long> getMedicationIdsFromExternalIds(
      final String externalSystem, final Set<String> medicationExternalIds, final DateTime when)
  {
    final Map<Long, Long> medicationExternalIdMedicationIdMap = new HashMap<>();
    if (medicationExternalIds.isEmpty())
    {
      return medicationExternalIdMedicationIdMap;
    }

    getHibernateTemplate().execute(
        new HibernateCallback<Void>()
        {
          @Override
          public Void doInHibernate(final Session session) throws HibernateException
          {
            new Hql()
                .select(
                    medicationExternal.get("externalId"),
                    medicationExternal.get("medication").id()
                )
                .from(
                    medicationExternal
                )
                .where(
                    effectiveEntities(medicationExternal).notDeletedAndEffectiveAt(when),
                    medicationExternal.get("externalSystem").eq(externalSystem),
                    medicationExternal.get("externalId").in(medicationExternalIds)
                )
                .buildQuery(session, Object[].class)
                .list(
                    new TupleProcessor<Void>()
                    {
                      @Override
                      protected Void process(final boolean hasNextTuple) throws ProcessingException
                      {
                        medicationExternalIdMedicationIdMap.put(Long.valueOf(nextString()), nextLong());
                        return null;
                      }
                    });
            return null;
          }
        }
    );
    return medicationExternalIdMedicationIdMap;
  }
}
