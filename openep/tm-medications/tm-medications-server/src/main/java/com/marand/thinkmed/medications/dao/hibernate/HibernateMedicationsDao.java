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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.CollectionUtils;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.Pair;
import com.marand.maf.core.StringUtils;
import com.marand.maf.core.data.object.NamedIdentityDto;
import com.marand.maf.core.hibernate.query.Alias;
import com.marand.maf.core.hibernate.query.Hql;
import com.marand.maf.core.resultrow.ProcessingException;
import com.marand.maf.core.resultrow.ResultRowProcessor;
import com.marand.maf.core.resultrow.TupleProcessor;
import com.marand.maf.core.resultrow.TwoLevelJoinProcessor;
import com.marand.maf.core.server.catalog.dao.CatalogDao;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.ActionReasonType;
import com.marand.thinkmed.medications.DoseFormType;
import com.marand.thinkmed.medications.MedicationExternalSystemType;
import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.MedicationOrderFormType;
import com.marand.thinkmed.medications.MedicationRouteTypeEnum;
import com.marand.thinkmed.medications.MedicationTypeEnum;
import com.marand.thinkmed.medications.MedicationsExternalValueType;
import com.marand.thinkmed.medications.TherapyJsonDeserializer;
import com.marand.thinkmed.medications.TherapyTemplateModeEnum;
import com.marand.thinkmed.medications.TherapyTemplateTypeEnum;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dto.BnfMaximumDto;
import com.marand.thinkmed.medications.dto.BnfMaximumUnitType;
import com.marand.thinkmed.medications.dto.CodedNameDto;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.DoseFormDto;
import com.marand.thinkmed.medications.dto.IndicationDto;
import com.marand.thinkmed.medications.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.MedicationDocumentDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationHolderDto;
import com.marand.thinkmed.medications.dto.MedicationIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.TherapyTemplateDto;
import com.marand.thinkmed.medications.dto.TherapyTemplateElementDto;
import com.marand.thinkmed.medications.dto.TherapyTemplatesDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTemplateDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTemplateMemberDto;
import com.marand.thinkmed.medications.dto.supply.MedicationSupplyCandidateDto;
import com.marand.thinkmed.medications.mapper.DoseFormDtoMapper;
import com.marand.thinkmed.medications.model.AtcClassification;
import com.marand.thinkmed.medications.model.DoseForm;
import com.marand.thinkmed.medications.model.Medication;
import com.marand.thinkmed.medications.model.MedicationBasicUnit;
import com.marand.thinkmed.medications.model.MedicationCustomGroup;
import com.marand.thinkmed.medications.model.MedicationCustomGroupMember;
import com.marand.thinkmed.medications.model.MedicationExternal;
import com.marand.thinkmed.medications.model.MedicationExternalCrossTab;
import com.marand.thinkmed.medications.model.MedicationFormulary;
import com.marand.thinkmed.medications.model.MedicationGeneric;
import com.marand.thinkmed.medications.model.MedicationIndication;
import com.marand.thinkmed.medications.model.MedicationIndicationLink;
import com.marand.thinkmed.medications.model.MedicationIngredient;
import com.marand.thinkmed.medications.model.MedicationIngredientLink;
import com.marand.thinkmed.medications.model.MedicationRoute;
import com.marand.thinkmed.medications.model.MedicationRouteLink;
import com.marand.thinkmed.medications.model.MedicationRouteRelation;
import com.marand.thinkmed.medications.model.MedicationType;
import com.marand.thinkmed.medications.model.MedicationVersion;
import com.marand.thinkmed.medications.model.MedicationWarning;
import com.marand.thinkmed.medications.model.MentalHealthTemplate;
import com.marand.thinkmed.medications.model.MentalHealthTemplateMember;
import com.marand.thinkmed.medications.model.PatientTherapyLastLinkName;
import com.marand.thinkmed.medications.model.TherapyTemplate;
import com.marand.thinkmed.medications.model.TherapyTemplateElement;
import com.marand.thinkmed.medications.model.impl.ActionReasonImpl;
import com.marand.thinkmed.medications.model.impl.DoseFormImpl;
import com.marand.thinkmed.medications.model.impl.MedicationImpl;
import com.marand.thinkmed.medications.model.impl.PatientTherapyLastLinkNameImpl;
import com.marand.thinkmed.medications.model.impl.TherapyTemplateElementImpl;
import com.marand.thinkmed.medications.model.impl.TherapyTemplateImpl;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import com.marand.thinkmed.medicationsexternal.WarningSeverity;
import com.marand.thinkmed.medicationsexternal.WarningType;
import com.marand.thinkmed.medicationsexternal.dto.MedicationsWarningDto;
import org.apache.commons.lang.BooleanUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

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
  private static final Alias.Effective<MedicationIngredientLink> medicationIngredientLink = Alias.forEffectiveEntity(MedicationIngredientLink.class);
  private static final Alias.Effective<MedicationIndication> medicationIndication = Alias.forEffectiveEntity(MedicationIndication.class);
  private static final Alias.Effective<MedicationIndicationLink> medicationIndicationLink = Alias.forEffectiveEntity(MedicationIndicationLink.class);
  private static final Alias.Effective<MedicationIngredient> medicationIngredient = Alias.forEffectiveEntity(MedicationIngredient.class);
  private static final Alias.Effective<MedicationRouteLink> medicationRouteLink = Alias.forEffectiveEntity(MedicationRouteLink.class);
  private static final Alias.Effective<MedicationRoute> medicationRoute = Alias.forEffectiveEntity(MedicationRoute.class);
  private static final Alias.Effective<MedicationRouteRelation> medicationRouteRelation = Alias.forEffectiveEntity(MedicationRouteRelation.class);
  private static final Alias.Effective<DoseForm> doseForm = Alias.forEffectiveEntity(DoseForm.class);
  private static final Alias.Effective<MedicationExternal> medicationExternal = Alias.forEffectiveEntity(MedicationExternal.class);
  private static final Alias.Effective<AtcClassification> atcClassification = Alias.forEffectiveEntity(AtcClassification.class);
  private static final Alias.Permanent<MedicationExternalCrossTab> medicationExternalTranslator = Alias.forPermanentEntity(MedicationExternalCrossTab.class);
  private static final Alias.Permanent<MedicationType> medicationType = Alias.forPermanentEntity(MedicationType.class);
  private static final Alias.Permanent<MedicationFormulary> medicationFormulary = Alias.forPermanentEntity(MedicationFormulary.class);
  private static final Alias.Permanent<MedicationBasicUnit> medicationBasicUnit = Alias.forPermanentEntity(MedicationBasicUnit.class);
  private static final Alias.Effective<MedicationGeneric> medicationGeneric = Alias.forEffectiveEntity(MedicationGeneric.class);
  private static final Alias.Permanent<MedicationCustomGroup> medicationCustomGroup = Alias.forPermanentEntity(MedicationCustomGroup.class);
  private static final Alias.Permanent<MedicationCustomGroupMember> medicationCustomGroupMember = Alias.forPermanentEntity(MedicationCustomGroupMember.class);
  private static final Alias.Permanent<MentalHealthTemplate> mentalHealthTemplate = Alias.forPermanentEntity(MentalHealthTemplate.class);
  private static final Alias.Permanent<MentalHealthTemplateMember> mentalHealthTemplateMember = Alias.forPermanentEntity(MentalHealthTemplateMember.class);
  private static final Alias.Permanent<TherapyTemplateImpl> therapyTemplate = Alias.forPermanentEntity(TherapyTemplateImpl.class);
  private static final Alias.Permanent<TherapyTemplateElement> therapyTemplateElement = Alias.forPermanentEntity(TherapyTemplateElement.class);
  private static final Alias.Permanent<PatientTherapyLastLinkName> patientTherapyLastLinkName = Alias.forPermanentEntity(PatientTherapyLastLinkName.class);
  private static final Alias.Effective<ActionReasonImpl> actionReason = Alias.forEffectiveEntity(ActionReasonImpl.class);
  private static final Alias.Effective<MedicationWarning> medicationWarning = Alias.forEffectiveEntity(MedicationWarning.class);

  private CatalogDao catalogDao;
  private DoseFormDtoMapper doseFormDtoMapper;
  private MedicationsBo medicationsBo;

  private TherapyDisplayProvider therapyDisplayProvider;

  @Required
  public void setCatalogDao(final CatalogDao catalogDao)
  {
    this.catalogDao = catalogDao;
  }

  @Required
  public void setDoseFormDtoMapper(final DoseFormDtoMapper doseFormDtoMapper)
  {
    this.doseFormDtoMapper = doseFormDtoMapper;
  }

  @Required
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Required
  public void setTherapyDisplayProvider(final TherapyDisplayProvider therapyDisplayProvider)
  {
    this.therapyDisplayProvider = therapyDisplayProvider;
  }

  @Override
  public Map<Long, MedicationHolderDto> loadMedicationsMap(final DateTime when)
  {
    final Alias.Permanent<MedicationType> antibioticMedicationType = Alias.forPermanentEntity(MedicationType.class);
    final Alias.Effective<AtcClassification> topAtcClassification = Alias.forEffectiveEntity(AtcClassification.class);

    return getHibernateTemplate().execute(
        requestSession -> {
          final Map<Long, MedicationHolderDto> medicationsMap = new LinkedHashMap<>();
          new Hql()
              .select(
                  medication.id(),
                  medicationVersion.get("name"),
                  medicationVersion.get("shortName"),
                  medicationVersion.get("active"),
                  medicationVersion.get("suggestSwitchToOral"),
                  medicationVersion.get("reviewReminder"),
                  medicationVersion.get("mentalHealthDrug"),
                  medicationVersion.get("inpatientMedication"),
                  medicationVersion.get("outpatientMedication"),
                  medicationGeneric.get("name"),
                  topAtcClassification.get("code"),
                  topAtcClassification.get("name"),
                  doseForm.id(),
                  doseForm.get("code"),
                  doseForm.get("name"),
                  doseForm.get("doseFormType"),
                  doseForm.get("medicationOrderFormType"),
                  antibioticMedicationType.id(),
                  medicationType.get("type"),
                  medication.get("medicationLevel"),
                  medication.get("vtmId"),
                  medication.get("vmpId"),
                  medication.get("ampId"),
                  medication.get("orderable"),
                  medicationIngredient.get("name"),
                  medicationIngredient.get("rule"),
                  medicationIngredientLink.id(),
                  medicationIngredientLink.get("strengthNumerator"),
                  medicationIngredientLink.get("strengthNumeratorUnit"),
                  medicationIngredientLink.get("strengthDenominator"),
                  medicationIngredientLink.get("strengthDenominatorUnit"),
                  medicationIngredientLink.get("descriptive"),
                  medicationCustomGroup.get("name"),
                  medicationCustomGroup.get("careProviderId"),
                  medicationCustomGroup.get("sortOrder"),
                  medicationFormulary.get("id"),
                  medicationFormulary.get("careProviderId")
              )
              .from(
                  medication.innerJoin("versions").as(medicationVersion),
                  medicationVersion.leftOuterJoin("medicationGeneric").as(medicationGeneric),
                  medicationVersion.leftOuterJoin("atcClassification").as(atcClassification),
                  atcClassification.leftOuterJoin("topParent").as(topAtcClassification),
                  medicationVersion.leftOuterJoin("doseForm").as(doseForm),
                  medication.leftOuterJoin("types").as(antibioticMedicationType)
                      .with(
                          antibioticMedicationType.get("type").eq(MedicationTypeEnum.ANTIBIOTIC),
                          antibioticMedicationType.notDeleted()),
                  medication.leftOuterJoin("types").as(medicationType)
                      .with(
                          medicationType.get("type").in(MedicationTypeEnum.MAIN_TYPES),
                          medicationType.notDeleted()),
                  medication.leftOuterJoin("ingredients").as(medicationIngredientLink)
                      .with(medicationIngredientLink.notDeletedAndEffectiveAt(when)),
                  medicationIngredientLink.leftOuterJoin("ingredient").as(medicationIngredient),
                  medication.leftOuterJoin("customGroupMembers")
                      .as(medicationCustomGroupMember).with(medicationCustomGroupMember.notDeleted()),
                  medicationCustomGroupMember.leftOuterJoin("medicationCustomGroup")
                      .as(medicationCustomGroup).with(medicationCustomGroup.notDeleted()),
                  medication.leftOuterJoin("formulary").as(medicationFormulary).with(medicationFormulary.notDeleted())
              )
              .where(
                  medication.get("medicationLevel").isNotNull(),
                  effectiveEntities(medication, medicationVersion).notDeletedAndEffectiveAt(when)
              )
              .orderBy(
                  medication.get("medicationLevel").desc(),
                  medicationVersion.get("active").desc(),
                  medication.get("sortOrder"),
                  medicationGeneric.get("name"),
                  medicationVersion.get("name"),
                  medicationFormulary.get("careProviderId")
              )
              .buildQuery(requestSession, Object[].class)
              .list(
                  (ResultRowProcessor<Object[], Void>)(resultRow, hasNext) -> {
                    final Long medicationId = (Long)resultRow[0];
                    final MedicationHolderDto dto;
                    if (medicationsMap.get(medicationId) != null)
                    {
                      dto = medicationsMap.get(medicationId);
                    }
                    else
                    {
                      dto = new MedicationHolderDto();

                      dto.setId(medicationId);
                      dto.setName((String)resultRow[1]);
                      dto.setShortName((String)resultRow[2]);
                      dto.setActive((Boolean)resultRow[3]);
                      dto.setSuggestSwitchToOral((Boolean)resultRow[4]);
                      dto.setReviewReminder((Boolean)resultRow[5]);

                      final Boolean mentalHealthDrug = (Boolean)resultRow[6];
                      final Boolean inpatientMedication = (Boolean)resultRow[7];
                      final Boolean outpatientMedication = (Boolean)resultRow[8];

                      dto.setMentalHealthDrug(mentalHealthDrug != null ? mentalHealthDrug : false);
                      dto.setInpatientMedication(inpatientMedication != null ? inpatientMedication : false);
                      dto.setOutpatientMedication(outpatientMedication != null ? outpatientMedication : false);

                      dto.setGenericName((String)resultRow[9]);

                      final String atcCode = (String)resultRow[10];
                      if (atcCode != null)
                      {
                        dto.setAtcGroupCode(atcCode);
                        dto.setAtcGroupName((String)resultRow[11]);
                      }

                      final Long doseFormId = (Long)resultRow[12];
                      if (doseFormId != null)
                      {
                        final DoseFormDto doseFormDto = new DoseFormDto();
                        doseFormDto.setId(doseFormId);
                        doseFormDto.setCode((String)resultRow[13]);
                        doseFormDto.setName((String)resultRow[14]);
                        doseFormDto.setDoseFormType((DoseFormType)resultRow[15]);
                        doseFormDto.setMedicationOrderFormType((MedicationOrderFormType)resultRow[16]);
                        dto.setDoseFormDto(doseFormDto);
                      }

                      dto.setAntibiotic(resultRow[17] != null);
                      dto.setMedicationType((MedicationTypeEnum)resultRow[18]);

                      dto.setMedicationLevel((MedicationLevelEnum)resultRow[19]);
                      dto.setVtmId((Long)resultRow[20]);
                      dto.setVmpId((Long)resultRow[21]);
                      dto.setAmpId((Long)resultRow[22]);
                      dto.setOrderable((Boolean)resultRow[23]);

                      medicationsMap.put(medicationId, dto);
                    }

                    final String ingredientName = (String)resultRow[24];
                    if (ingredientName != null)
                    {
                      final MedicationIngredientDto medicationIngredientDto = new MedicationIngredientDto();
                      final MedicationRuleEnum medicationRuleEnum = (MedicationRuleEnum)resultRow[25];

                      medicationIngredientDto.setIngredientName(ingredientName);
                      medicationIngredientDto.setId((Long)resultRow[26]);
                      medicationIngredientDto.setStrengthNumerator((Double)resultRow[27]);
                      medicationIngredientDto.setStrengthNumeratorUnit((String)resultRow[28]);
                      medicationIngredientDto.setStrengthDenominator((Double)resultRow[29]);
                      medicationIngredientDto.setStrengthDenominatorUnit((String)resultRow[30]);
                      medicationIngredientDto.setDescriptive((Boolean)resultRow[31]);

                      //defining ingredient is only ingredient (if there is only only) or descriptive ingredient (if there are multiple)
                      if (dto.getDefiningIngredient() == null || medicationIngredientDto.isDescriptive())
                      {
                        dto.setDefiningIngredient(medicationIngredientDto);
                      }
                      else if (!dto.getDefiningIngredient().isDescriptive() &&
                          dto.getDefiningIngredient().getId() != medicationIngredientDto.getId())
                      {
                        dto.setDefiningIngredient(null);
                      }

                      if (medicationRuleEnum != null)
                      {
                        dto.getMedicationRules().add(medicationRuleEnum);
                      }
                    }

                    final String customGroupName = (String)resultRow[32];
                    final String customGroupCareProviderId = (String)resultRow[33];
                    final Integer customGroupSort = (Integer)resultRow[34];
                    if (customGroupName != null)
                    {
                      dto.getCustomGroupNameSortOrder().put(
                          customGroupCareProviderId, Pair.of(customGroupName, customGroupSort));
                    }

                    final Long formularyId = (Long)resultRow[35];
                    final String formularyCareProviderId = (String)resultRow[36];
                    dto.setFormulary(formularyId != null);
                    if (formularyCareProviderId == null)
                    {
                      dto.getFormularyCareProviders().clear();
                    }
                    else
                    {
                      dto.getFormularyCareProviders().add(formularyCareProviderId);
                    }
                    return null;
                  }
              );

          return medicationsMap;
        });
  }

  @Override
  public MedicationDataDto getMedicationData(
      final long medicationId,
      final String careProviderId,
      @Nonnull final DateTime when)
  {
    Preconditions.checkNotNull(when, "when is required");
    final Map<Long, MedicationDataDto> medicationDataMap =
        getMedicationDataMap(Collections.singleton(medicationId), careProviderId, when);

    return medicationDataMap.containsKey(medicationId) ? medicationDataMap.get(medicationId) : null;
  }

  @Override
  public Map<Long, MedicationDataDto> getMedicationDataMap(
      @Nonnull final Set<Long> medicationIds,
      final String careProviderId,
      @Nonnull final DateTime when)
  {
    Preconditions.checkNotNull(medicationIds, "medicationIds is required");
    Preconditions.checkNotNull(when, "when is required");
    if (medicationIds.isEmpty())
    {
      return Collections.emptyMap();
    }

    final Alias.Effective<MedicationRoute> childRoute = Alias.forEffectiveEntity(MedicationRoute.class);
    final Alias.Effective<MedicationRouteRelation> medicationRouteChildRelation =
        Alias.forEffectiveEntity(MedicationRouteRelation.class);

    final Map<Long, MedicationDataDto> medicationDataMap = new HashMap<>();
    getHibernateTemplate().execute(
        (HibernateCallback<Void>)requestSession -> {
          new Hql()
              .select(
                  medication.id(),
                  medicationIngredient.get("name"),
                  medicationIngredient.id(),
                  medicationIngredient.get("rule"),
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
                  medicationRouteLink.get("unlicensed"),
                  medicationRouteLink.get("discretionary"),
                  medicationRouteLink.get("bnfMaximum"),
                  medicationRouteLink.get("bnfMaximumUnitType"),
                  childRoute.id(),
                  childRoute.get("code"),
                  childRoute.get("shortName"),
                  childRoute.get("type"),
                  medicationRouteLink.get("defaultRoute"),
                  doseForm,
                  medicationBasicUnit.get("name"),
                  medicationType.id(),
                  medicationVersion.get("reviewReminder"),
                  medicationVersion.get("controlledDrug"),
                  medicationVersion.get("mentalHealthDrug"),
                  medicationVersion.get("titration"),
                  medicationVersion.get("medicationPackaging"),
                  medicationVersion.get("roundingFactor"),
                  medicationVersion.get("clinicalTrialMedication"),
                  medicationVersion.get("unlicensedMedication"),
                  medicationVersion.get("highAlertMedication"),
                  medicationVersion.get("blackTriangleMedication"),
                  medicationVersion.get("inpatientMedication"),
                  medicationVersion.get("outpatientMedication"),
                  medicationFormulary.get("id"),
                  medicationVersion.get("expensiveDrug"),
                  medicationVersion.get("price")
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
                          medicationType.notDeleted()),
                  medication.leftOuterJoin("formulary").as(medicationFormulary)
                      .with(
                          medicationFormulary.notDeleted(),
                          or(
                              careProviderId == null,
                              medicationFormulary.get("careProviderId").isNull(),
                              medicationFormulary.get("careProviderId").eq(careProviderId))
                      )
              )
              .where(
                  medication.id().in(medicationIds),
                  effectiveEntities(medicationRouteLink, medicationVersion).notDeletedAndEffectiveAt(when)
              )
              .orderBy(medicationFormulary.get("careProviderId"))
              .buildQuery(requestSession, Object[].class)
              .list(
                  new TupleProcessor<Void>()
                  {
                    @Override
                    protected Void process(final boolean hasNextTuple) throws ProcessingException
                    {
                      final Long medicationId = nextLong();
                      final String ingredientName = next();
                      final Long ingredientId = next(Long.class);
                      final MedicationRuleEnum ingredientRuleEnum = next(MedicationRuleEnum.class);
                      final Long medicationIngredientLinkId = next();
                      final Double strengthNumerator = next();
                      final String strengthNumeratorUnit = next();
                      final Double strengthDenominator = next();
                      final String strengthDenominatorUnit = next();
                      final Boolean descriptiveIngredient = next();

                      final MedicationDataDto dto =
                          medicationDataMap.containsKey(medicationId) ?
                          medicationDataMap.get(medicationId) :
                          new MedicationDataDto();

                      if (ingredientName != null)
                      {
                        final MedicationIngredientDto medicationIngredientDto = new MedicationIngredientDto();

                        medicationIngredientDto.setIngredientName(ingredientName);
                        medicationIngredientDto.setIngredientRule(ingredientRuleEnum);
                        if (ingredientId != null)
                        {
                          medicationIngredientDto.setIngredientId(ingredientId);
                        }
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

                      final Long routeId = next();
                      final String routeCode = next();
                      final String routeShortName = next();
                      final MedicationRouteTypeEnum routeType = next();
                      final Boolean unlicensedRoute = next();
                      final Boolean discretionaryRoute = next();
                      final Integer bnfMaximum = next();
                      final BnfMaximumUnitType bnfMaximumUnitType = next();
                      final Long childRouteId = next();
                      final String childRouteCode = next();
                      final String childRouteShortName = next();
                      final MedicationRouteTypeEnum childRouteType = next();

                      Boolean defaultRoute = next();

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

                      routeDto.setUnlicensedRoute(unlicensedRoute != null && unlicensedRoute);
                      routeDto.setDiscretionary(discretionaryRoute != null && discretionaryRoute);

                      if (bnfMaximum != null)
                      {
                        final BnfMaximumDto bnfMaximumDto = new BnfMaximumDto();
                        bnfMaximumDto.setQuantity(bnfMaximum);
                        bnfMaximumDto.setQuantityUnit(bnfMaximumUnitType);
                        routeDto.setBnfMaximumDto(bnfMaximumDto);
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

                      final DoseForm aDoseForm = next();
                      if (aDoseForm != null)
                      {
                        dto.setDoseForm(doseFormDtoMapper.map(aDoseForm, DoseFormDto.class));
                        dto.setDoseSplitFactor(aDoseForm.getSplitFactor());
                      }
                      dto.setBasicUnit(nextString());

                      final Long medicationTypeAntibioticId = next();
                      dto.setAntibiotic(medicationTypeAntibioticId != null);
                      dto.setReviewReminder(nextBoolean());
                      final Boolean controlledDrug = next();
                      dto.setControlledDrug(controlledDrug != null ? controlledDrug : false);
                      final Boolean mentalHealthDrug = next();
                      dto.setMentalHealthDrug(mentalHealthDrug != null ? mentalHealthDrug : false);
                      dto.setTitration(next());
                      dto.setMedicationPackaging(next());
                      dto.setRoundingFactor(next());
                      final Boolean clinicalTrialMedication = nextBoolean();
                      dto.setClinicalTrialMedication(clinicalTrialMedication != null ? clinicalTrialMedication : false);
                      final Boolean unlicensedMedication = nextBoolean();
                      dto.setUnlicensedMedication(unlicensedMedication != null ? unlicensedMedication : false);
                      final Boolean highAlertMedication = nextBoolean();
                      dto.setHighAlertMedication(highAlertMedication != null ? highAlertMedication : false);
                      final Boolean blackTriangleMedication = nextBoolean();
                      dto.setBlackTriangleMedication(blackTriangleMedication != null ? blackTriangleMedication : false);
                      final Boolean inpatientMedication = nextBoolean();
                      dto.setInpatientMedication(inpatientMedication != null ? inpatientMedication : false);
                      final Boolean outpatientMedication = nextBoolean();
                      dto.setOutpatientMedication(outpatientMedication != null ? outpatientMedication : false);
                      dto.setFormulary(nextLong() != null);
                      final Boolean expensiveDrug = nextBoolean();
                      dto.setExpensiveDrug(expensiveDrug != null ? expensiveDrug : false);
                      dto.setPrice(next());

                      medicationDataMap.put(medicationId, dto);
                      return null;
                    }
                  });

          new Hql()
              .select(
                  medication.id(),
                  medicationIndication.get("code"),
                  medicationIndication.get("name")
              )
              .from(
                  medication.innerJoin("indications").as(medicationIndicationLink),
                  medicationIndicationLink.innerJoin("indication").as(medicationIndication)
              )
              .where(
                  medication.id().in(medicationIds),
                  effectiveEntities(medicationIndicationLink, medicationIndication).notDeletedAndEffectiveAt(when)
              )
              .buildQuery(requestSession, Object[].class)
              .list(
                  new TupleProcessor<Void>()
                  {
                    @Override
                    protected Void process(final boolean hasNextTuple) throws ProcessingException
                    {
                      medicationDataMap.get(nextLong()).getIndications().add(new IndicationDto(nextString(), nextString()));
                      return null;
                    }
                  });

          return null;
        });

    if (!medicationDataMap.isEmpty())
    {
      final Map<Long, MedicationDto> medicationsMap = getMedicationsMap(medicationDataMap.keySet(), when);
      final Map<Long, List<MedicationDocumentDto>> medicationDocumentsMap = getMedicationDocumentsMap(medicationDataMap.keySet());

      for (final Map.Entry<Long, MedicationDataDto> entry : medicationDataMap.entrySet())
      {
        final Long medicationId = entry.getKey();

        if (medicationsMap.containsKey(medicationId))
        {
          entry.getValue().setMedication(medicationsMap.get(medicationId));
        }
        if (medicationDocumentsMap.containsKey(medicationId))
        {
          entry.getValue().setMedicationDocuments(medicationDocumentsMap.get(medicationId));
        }
      }
    }

    return medicationDataMap;
  }

  @Override
  public List<Long> getMedicationIdsWithIngredientId(final long ingredientId, final DateTime when)
  {
    Preconditions.checkNotNull(when);

    return getHibernateTemplate().execute(
        requestSession -> {
          final Hql hql = new Hql()
              .select(
                  medication.id()
              )
              .from(
                  medication.innerJoin("versions").as(medicationVersion),
                  medication.innerJoin("ingredients").as(medicationIngredientLink),
                  medicationIngredientLink.innerJoin("ingredient").as(medicationIngredient)
              )
              .where(
                  effectiveEntities(
                      medicationVersion,
                      medicationIngredientLink).notDeletedAndEffectiveAt(when),
                  medicationIngredient.id().eq(ingredientId)
              );
          return hql
              .buildQuery(requestSession, Long.class)
              .list();
        });
  }

  @Override
  public Set<Long> getMedicationIdsWithIngredientRule(
      final MedicationRuleEnum medicationRuleEnum,
      final DateTime when)
  {
    Preconditions.checkNotNull(medicationRuleEnum);
    Preconditions.checkNotNull(when);

    final List<Long> result = getHibernateTemplate().execute(
        requestSession -> new Hql()
            .select(
                medication.id()
            )
            .from(
                medication.innerJoin("ingredients").as(medicationIngredientLink),
                medicationIngredientLink.innerJoin("ingredient").as(medicationIngredient)
            )
            .where(
                medicationIngredient.get("rule").eq(medicationRuleEnum)
            )
            .buildQuery(requestSession, Long.class)
            .list());

    return new HashSet<>(result);
  }

  @Override
  public List<MedicationRouteDto> getMedicationRoutes(final long medicationId, final DateTime when)
  {
    final MedicationDataDto medicationData = getMedicationData(medicationId, null, when);
    return medicationData != null ? medicationData.getRoutes() : Collections.<MedicationRouteDto>emptyList();
  }

  private Map<Long, List<MedicationDocumentDto>> getMedicationDocumentsMap(final Set<Long> medicationIds)
  {
    final Map<Long, List<MedicationDocumentDto>> medicationDocumentDtoMap = new HashMap<>();

    getHibernateTemplate().execute(
        (HibernateCallback<Void>)session -> {
          new Hql()
              .select(
                  medication.id(),
                  medicationExternal.get("externalId"),
                  medicationExternal.get("externalSystem")
              )
              .from(
                  medicationExternal.innerJoin("medication").as(medication)
              )
              .where(
                  medication.id().in(medicationIds),
                  medicationExternal.get("externalSystemType").eq(MedicationExternalSystemType.DOCUMENTS_PROVIDER),
                  medicationExternal.notDeleted()
              )
              .buildQuery(session, Object[].class)
              .list(
                  new TupleProcessor<Void>()
                  {
                    @Override
                    protected Void process(final boolean hasNextTuple) throws ProcessingException
                    {
                      final Long medicationId = nextLong();

                      final MedicationDocumentDto dto = new MedicationDocumentDto();
                      dto.setDocumentReference(nextString());
                      dto.setExternalSystem(nextString());

                      if (medicationDocumentDtoMap.containsKey(medicationId))
                      {
                        medicationDocumentDtoMap.get(medicationId).add(dto);
                      }
                      else
                      {
                        final List<MedicationDocumentDto> medicationDocumentDtos = new ArrayList<>();
                        medicationDocumentDtos.add(dto);
                        medicationDocumentDtoMap.put(medicationId, medicationDocumentDtos);
                      }
                      return null;
                    }
                  });
          return null;
        });

    return medicationDocumentDtoMap;
  }

  @Override
  public String getMedicationExternalId(final String externalSystem, final long medicationId, final DateTime when)
  {
    return getHibernateTemplate().execute(
        session -> new Hql()
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
            .getSingleRowOrNull());
  }

  @Override
  public Set<Long> findSimilarMedicationsIds(
      final long medicationId,
      @Nonnull final Collection<Long> routeIds,
      @Nonnull final DateTime when)
  {
    //Similar medications have same generic, route and atc code

    Preconditions.checkNotNull(medicationId, "routeIds is required");
    Preconditions.checkNotNull(medicationId, "when is required");

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
        requestSession ->
        {
          final List<Long> results = new Hql()
              .select(
                  medication.id()
              )
              .from(
                  referenceMedication.innerJoin("versions").as(referenceMedicationVersion),
                  referenceMedicationVersion.leftOuterJoin("medicationGeneric").as(referenceGeneric),
                  referenceMedicationVersion.leftOuterJoin("atcClassification").as(referenceAtcClassification),
                  referenceAtcClassification.leftOuterJoin("topParent").as(referenceTopAtcClassification),
                  medication.innerJoin("versions").as(medicationVersion),
                  medicationVersion.leftOuterJoin("medicationGeneric").as(medicationGeneric),
                  medicationVersion.innerJoin("atcClassification").as(atcClassification),
                  atcClassification.innerJoin("topParent").as(topAtcClassification),
                  medication.innerJoin("routes").as(medicationRouteLink),
                  medicationRouteLink.innerJoin("route").as(medicationRoute),
                  medicationRoute.leftOuterJoin("childRelations").as(medicationRouteChildRelation)
                      .with(medicationRouteChildRelation.notDeletedAndEffectiveAt(when)),
                  medicationRouteChildRelation.leftOuterJoin("childRoute").as(childRoute)
                      .with(childRoute.notDeletedAndEffectiveAt(when))
              )
              .where(
                  or(
                      medication.id().eq(medicationId),
                      and(
                          referenceMedication.id().eq(medicationId),
                          medicationGeneric.id().eq(referenceGeneric.id()),
                          topAtcClassification.id().eq(referenceTopAtcClassification.id()),
                          or(
                              CollectionUtils.isEmpty(routeIds),
                              medicationRoute.id().in(routeIds),
                              childRoute.id().in(routeIds))
                      )),
                  effectiveEntities(medicationVersion, referenceMedicationVersion).notDeletedAndEffectiveAt(when)
              )
              .orderBy(medicationVersion.get("name"))
              .buildQuery(requestSession, Long.class)
              .list();

          return new HashSet<>(results);
        });
  }

  @Override
  public boolean isProductBasedMedication(final long medicationId)
  {
    final Medication medicationEntity = getHibernateTemplate().load(MedicationImpl.class, medicationId);
    return medicationEntity.getMedicationLevel() == MedicationLevelEnum.AMP && medicationEntity.getVmpId() == null;
  }

  @Override
  public List<MedicationSupplyCandidateDto> getMedicationSupplyCandidates(
      final long medicationId,
      final long routeId,
      final DateTime when)
  //TODO nejc - for now only for tablets or capsules...
  {
    final Medication medicationEntity = getHibernateTemplate().load(MedicationImpl.class, medicationId);
    if (medicationEntity.getMedicationLevel() != MedicationLevelEnum.VTM)
    {
      throw new IllegalArgumentException("Medication level is not VTM, supported only for VTM levels");
    }
    final long vtmId = medicationEntity.getVtmId();

    return getHibernateTemplate().execute(
        requestSession -> {
          final Hql hql = new Hql()
              .select(
                  medication.id(),
                  medicationVersion.get("name"),
                  medicationBasicUnit.get("name"),
                  medicationIngredientLink.get("strengthNumerator"),
                  medicationIngredientLink.get("strengthNumeratorUnit")
              )
              .from(
                  medication.innerJoin("versions").as(medicationVersion),
                  medicationVersion.innerJoin("basicUnit").as(medicationBasicUnit),
                  medication.innerJoin("routes").as(medicationRouteLink),
                  medication.innerJoin("ingredients").as(medicationIngredientLink)
              )
              .where(
                  medication.get("vtmId").eq(vtmId),
                  medication.get("medicationLevel").eq(MedicationLevelEnum.VMP),
                  medicationRouteLink.get("route").id().eq(routeId),
                  or(
                      medicationBasicUnit.get("name").eq("tablet"),
                      medicationBasicUnit.get("name").eq("capsule"),
                      medicationBasicUnit.get("name").eq("sachet")),
                  effectiveEntities(
                      medicationVersion,
                      medicationRouteLink,
                      medicationIngredientLink).notDeletedAndEffectiveAt(when)
              )
              .orderBy(medicationBasicUnit.get("name"), medicationVersion.get("name"));

          return hql
              .buildQuery(requestSession, Object[].class)
              .list(
                  new TupleProcessor<MedicationSupplyCandidateDto>()
                  {
                    @Override
                    protected MedicationSupplyCandidateDto process(final boolean hasNextTuple) throws ProcessingException
                    {
                      final MedicationSupplyCandidateDto dto = new MedicationSupplyCandidateDto();
                      dto.setId(nextLong());
                      dto.setName(nextString());
                      dto.setBasicUnit(nextString());
                      dto.setStrengthNumerator(next(Double.class));
                      dto.setStrengthNumeratorUnit(nextString());
                      return dto;
                    }
                  });
        });
  }

  @Override
  public List<MedicationDto> getMedicationChildProducts(
      final long medicationId,
      @Nonnull final Collection<Long> routeIds,
      @Nonnull final DateTime when)
  {
    Preconditions.checkNotNull(medicationId, "routeIds is required");
    Preconditions.checkNotNull(medicationId, "when is required");

    final Medication medicationEntity = getHibernateTemplate().load(MedicationImpl.class, medicationId);
    final MedicationLevelEnum medicationLevel = medicationEntity.getMedicationLevel();

    return getHibernateTemplate().execute(
        requestSession -> {
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
                  or(
                      CollectionUtils.isEmpty(routeIds),
                      medicationRoute.id().in(routeIds)
                  ),
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
            throw new IllegalArgumentException("Medication level " + medicationLevel + " not yet supported.");
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
                      dto.setId(nextLong());
                      dto.setName(nextString());
                      dto.setShortName(nextString());
                      dto.setGenericName(nextString());
                      return dto;
                    }
                  });
        });
  }

  @Override
  public MedicationDto getMedicationById(final Long medicationId, final DateTime when)
  {
    Preconditions.checkNotNull(medicationId);
    Preconditions.checkNotNull(when);

    return getHibernateTemplate().execute(
        session -> new Hql()
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
                  protected MedicationDto process(final boolean hasNextTuple) throws ProcessingException
                  {
                    final MedicationDto dto = new MedicationDto();
                    dto.setId(nextLong());
                    dto.setName(nextString());
                    dto.setShortName(nextString());
                    dto.setGenericName(nextString());
                    MedicationTypeEnum medicationTypeEnum = next();
                    if (medicationTypeEnum == null)
                    {
                      medicationTypeEnum = MedicationTypeEnum.MEDICATION;
                    }
                    dto.setMedicationType(medicationTypeEnum);
                    return dto;
                  }
                }));
  }

  @Override
  public Map<Long, MedicationDto> getMedicationsMap(final Set<Long> medicationIds, final DateTime when)
  {
    Preconditions.checkNotNull(medicationIds);

    final Map<Long, MedicationDto> medicationDtoMap = new HashMap<>();

    getHibernateTemplate().execute(
        (HibernateCallback<Void>)session -> {
          new Hql()
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
                  medication.id().in(medicationIds),
                  effectiveEntities(medicationVersion).notDeletedAndEffectiveAt(when))

              .buildQuery(session, Object[].class)
              .list(
                  new TupleProcessor<Void>()
                  {
                    @Override
                    protected Void process(final boolean hasNextTuple) throws ProcessingException
                    {
                      final Long medicationId = nextLong();

                      final MedicationDto dto = new MedicationDto();
                      dto.setId(medicationId);
                      dto.setName(nextString());
                      dto.setShortName(nextString());
                      dto.setGenericName(nextString());
                      MedicationTypeEnum medicationTypeEnum = next();
                      if (medicationTypeEnum == null)
                      {
                        medicationTypeEnum = MedicationTypeEnum.MEDICATION;
                      }
                      dto.setMedicationType(medicationTypeEnum);
                      medicationDtoMap.put(medicationId, dto);
                      return null;
                    }
                  });

          return null;
        });

    return medicationDtoMap;
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
        (HibernateCallback<Void>)session -> {
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
                    protected Void process(final boolean hasNextTuple) throws ProcessingException
                    {
                      final String value = next();
                      final String externalValue = next();
                      valuesMap.put(value, externalValue);
                      return null;
                    }
                  });
          return null;
        });
    return valuesMap;
  }

  @Override
  public List<MedicationRouteDto> getRoutes(final DateTime when)
  {
    return getHibernateTemplate().execute(
        session -> new Hql()
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
                  protected MedicationRouteDto process(final boolean hasNextTuple) throws ProcessingException
                  {
                    final MedicationRouteDto routeDto = new MedicationRouteDto();
                    routeDto.setId(nextLong());
                    routeDto.setCode(nextString());
                    routeDto.setName(nextString());
                    routeDto.setType(next(MedicationRouteTypeEnum.class));
                    return routeDto;
                  }
                }));
  }

  @Override
  public List<DoseFormDto> getDoseForms(final DateTime when)
  {
    return getHibernateTemplate().execute(
        session -> new Hql()
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
                (resultRow, hasNext) -> doseFormDtoMapper.map(resultRow)));
  }

  @Override
  public List<String> getMedicationBasicUnits()
  {
    return getHibernateTemplate().execute(
        session -> new Hql()
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
            .list());
  }

  @Override
  public Map<Long, Pair<String, Integer>> getCustomGroupNameSortOrderMap(
      final String careProviderId, final Set<Long> medicationIds)
  {
    if (medicationIds.isEmpty())
    {
      return new HashMap<>();
    }
    final Map<Long, Pair<String, Integer>> customGroupNameSortOrderMap = new HashMap<>();
    getHibernateTemplate().execute(
        session -> new Hql()
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
                medicationCustomGroup.get("careProviderId").eq(careProviderId)
            )
            .buildQuery(session, Object[].class)
            .list(
                new TupleProcessor<Void>()
                {
                  @Override
                  protected Void process(final boolean hasNextTuple) throws ProcessingException
                  {
                    final Pair<String, Integer> nameSortOrderPair = Pair.of(nextString(), next(Integer.class));
                    customGroupNameSortOrderMap.put(nextLong(), nameSortOrderPair);
                    return null;
                  }
                }
            ));
    return customGroupNameSortOrderMap;
  }

  @Override
  public List<String> getCustomGroupNames(final String careProviderId)
  {
    return getHibernateTemplate().execute(
        session -> new Hql()
            .select(
                medicationCustomGroup.get("name"),
                medicationCustomGroup.get("sortOrder")
            )
            .distinct()
            .from(
                medicationCustomGroupMember.innerJoin("medicationCustomGroup").as(medicationCustomGroup)
            )
            .where(
                medicationCustomGroup.get("careProviderId").eq(careProviderId)
            )
            .orderBy(medicationCustomGroup.get("sortOrder"))
            .buildQuery(session, Object[].class)
            .list(
                new TupleProcessor<String>()
                {
                  @Override
                  protected String process(final boolean hasNextTuple) throws ProcessingException
                  {
                    return next();
                  }
                })
    );
  }

  @Override
  public TherapyTemplatesDto getTherapyTemplates(
      @Nonnull final String patientId,
      @Nonnull final String userId,
      @Nonnull final TherapyTemplateModeEnum templateMode,
      final String careProviderId,
      final Double referenceWeight,
      final Double patientHeight,
      @Nonnull final Locale locale)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");
    StringUtils.checkNotBlank(userId, "userId is required");
    Preconditions.checkNotNull(templateMode, "templateMode is required");
    Preconditions.checkNotNull(locale, "locale is required");

    return getHibernateTemplate().execute(
        session -> {

          final TherapyTemplatesDto allTemplatesDto = new TherapyTemplatesDto();
          new Hql()
              .select(
                  therapyTemplate.id(),
                  therapyTemplate.get("version"),
                  therapyTemplate.get("name"),
                  therapyTemplate.get("type"),
                  therapyTemplate.get("userId"),
                  therapyTemplate.get("careProviderId"),
                  therapyTemplate.get("patientId"),
                  therapyTemplateElement.get("therapy"),
                  therapyTemplateElement.get("completed")
              )
              .from(
                  therapyTemplate.innerJoin("therapyTemplateElements").as(therapyTemplateElement)
              )
              .where(
                  therapyTemplate.get("templateMode").eq(templateMode),
                  or(
                      therapyTemplate.get("userId").eq(userId),
                      therapyTemplate.get("patientId").eq(patientId),
                      and(careProviderId != null, therapyTemplate.get("careProviderId").eq(careProviderId))
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
                      templateDto.setName(nextString());
                      templateDto.setType(next(TherapyTemplateTypeEnum.class));
                      templateDto.setUserId(nextString());
                      templateDto.setCareProviderId(nextString());
                      templateDto.setPatientId(nextString());

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
                      final String therapyJson = next();
                      final TherapyDto therapyDto =
                          JsonUtil.fromJson(
                              therapyJson, TherapyDto.class, TherapyJsonDeserializer.INSTANCE.getTypeAdapters());
                      if (therapyDto instanceof ComplexTherapyDto && referenceWeight != null)
                      {
                        if (((ComplexTherapyDto)therapyDto).isContinuousInfusion())
                        {
                          medicationsBo.fillInfusionRateFromFormula(
                              (ComplexTherapyDto)therapyDto,
                              referenceWeight,
                              patientHeight);
                        }
                        else
                        {
                          medicationsBo.fillInfusionFormulaFromRate(
                              (ComplexTherapyDto)therapyDto,
                              referenceWeight,
                              patientHeight);
                        }
                      }

                      //old templates were saved without routeId
                      if (therapyDto.getRoutes() != null)
                      {
                        therapyDto.getRoutes()
                            .stream()
                            .filter(medicationRouteDto -> medicationRouteDto.getId() == 0)
                            .forEach(medicationRouteDto -> medicationRouteDto.setId(Long.parseLong(medicationRouteDto.getCode())));
                      }

                      //in old templates solutions had only volume without quantity
                      if (therapyDto instanceof ComplexTherapyDto)
                      {
                        for (final InfusionIngredientDto ingredient : ((ComplexTherapyDto)therapyDto).getIngredientsList())
                        {
                          if (ingredient.getQuantity() == null && ingredient.getQuantityDenominator() != null)
                          {
                            ingredient.setQuantity(ingredient.getQuantityDenominator());
                            ingredient.setQuantityUnit(ingredient.getQuantityDenominatorUnit());
                            ingredient.setQuantityDenominator(null);
                          }
                        }
                      }

                      therapyDisplayProvider.fillDisplayValues(therapyDto, true, true, true, locale, false);
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
        });
  }

  @Override
  public long saveTherapyTemplate(
      final TherapyTemplateDto templateDto,
      final TherapyTemplateModeEnum templateMode,
      final String userId)
  {
    Preconditions.checkArgument(
        BooleanUtils.xor(
            new Boolean[]{
                templateDto.getCareProviderId() != null,
                templateDto.getType() == TherapyTemplateTypeEnum.USER,
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
    template.setCareProviderId(templateDto.getCareProviderId());
    template.setTemplateMode(templateMode);

    if (templateDto.getType() == TherapyTemplateTypeEnum.USER)
    {
      template.setUserId(userId);
    }

    getHibernateTemplate().save(template);

    for (final TherapyTemplateElementDto templateElementDto : templateDto.getTemplateElements())
    {
      final TherapyTemplateElement element = new TherapyTemplateElementImpl();
      final TherapyDto therapyDto = templateElementDto.getTherapy();
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
    if (medicationIds.isEmpty())
    {
      return medicationLevelMap;
    }
    getHibernateTemplate().execute(
        (HibernateCallback<Void>)session -> {
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
    );
    return medicationLevelMap;
  }

  @Override
  public String getPatientLastLinkName(final long patientId)
  {
    return getHibernateTemplate().execute(
        session -> new Hql()
            .select(
                patientTherapyLastLinkName.get("lastLinkName")
            )
            .from(
                patientTherapyLastLinkName
            )
            .where(
                patientTherapyLastLinkName.get("patientId").eq(patientId),
                patientTherapyLastLinkName.notDeleted()
            )
            .buildQuery(session, String.class)
            .getSingleRowOrNull()
    );
  }

  @Override
  public void savePatientLastLinkName(final long patientId, final String lastLinkName)
  {
    PatientTherapyLastLinkName lastLink = getHibernateTemplate().execute(
        (HibernateCallback<PatientTherapyLastLinkName>)session -> new Hql()
            .select(
                patientTherapyLastLinkName
            )
            .from(
                patientTherapyLastLinkName
            )
            .where(
                patientTherapyLastLinkName.get("patientId").eq(patientId),
                patientTherapyLastLinkName.notDeleted()
            )
            .buildQuery(session, PatientTherapyLastLinkNameImpl.class)
            .getSingleRowOrNull());

    if (lastLink == null)
    {
      lastLink = new PatientTherapyLastLinkNameImpl();
      lastLink.setPatientId(patientId);
    }
    lastLink.setLastLinkName(lastLinkName);
    getHibernateTemplate().saveOrUpdate(lastLink);
  }

  @Override
  public Map<ActionReasonType, List<CodedNameDto>> getActionReasons(
      @Nonnull final DateTime when,
      final ActionReasonType type)
  {
    Preconditions.checkNotNull(when, "when must not be null");

    return getHibernateTemplate().execute(
        session ->
        {
          //noinspection MapReplaceableByEnumMap
          final Map<ActionReasonType, List<CodedNameDto>> actionReasonMap = new HashMap<>();
          //noinspection ConstantConditions
          new Hql()
              .select(
                  actionReason.get("code"),
                  actionReason.get("name"),
                  actionReason.get("reasonType")
              )
              .from(
                  actionReason
              )
              .where(
                  effectiveEntities(actionReason).notDeletedAndEffectiveAt(when),
                  or(type == null, actionReason.get("reasonType").eq(type))
              )
              .buildQuery(session, Object[].class)
              .list(
                  new TupleProcessor<Void>()
                  {
                    @Override
                    protected Void process(final boolean hasNextTuple) throws ProcessingException
                    {
                      final String code = nextString();
                      final String name = nextString();

                      final CodedNameDto dto = new CodedNameDto(code, name);
                      final ActionReasonType reasonType = next();

                      if (actionReasonMap.containsKey(reasonType))
                      {
                        actionReasonMap.get(reasonType).add(dto);
                      }
                      else
                      {
                        final List<CodedNameDto> dtoList = new ArrayList<>();
                        dtoList.add(dto);
                        actionReasonMap.put(reasonType, dtoList);
                      }
                      return null;
                    }
                  });

          return actionReasonMap;
        }
    );
  }

  @Override
  public List<MentalHealthTemplateDto> getMentalHealthTemplates()
  {
    return getHibernateTemplate().execute(
        session -> new Hql()
            .select(
                mentalHealthTemplate.id(),
                mentalHealthTemplate.get("name"),
                medicationRoute.id(),
                medicationRoute.get("code"),
                medicationRoute.get("shortName"),
                medicationRoute.get("type")
            )
            .from(
                mentalHealthTemplate,
                mentalHealthTemplate.leftOuterJoin("medicationRoute").as(medicationRoute)
            )
            .buildQuery(session, Object[].class)
            .list(
                new TupleProcessor<MentalHealthTemplateDto>()
                {
                  @Override
                  protected MentalHealthTemplateDto process(final boolean hasNextTuple) throws ProcessingException
                  {
                    final Long id = nextLong();
                    final String name = nextString();
                    final Long routeId = nextLong();
                    final String routeCode = nextString();
                    final String routeName = nextString();
                    final MedicationRouteTypeEnum routeTypeEnum = next(MedicationRouteTypeEnum.class);

                    MedicationRouteDto route = null;
                    if (routeId != null)
                    {
                      route = new MedicationRouteDto();
                      route.setId(routeId);
                      route.setCode(routeCode);
                      route.setName(routeName);
                      route.setType(routeTypeEnum);
                    }

                    return new MentalHealthTemplateDto(id, name, route);
                  }
                })
    );
  }

  @Override
  public List<MentalHealthTemplateMemberDto> getMentalHealthTemplateMembers(@Nonnull final Collection<Long> mentalHealthTemplateIds)
  {
    Preconditions.checkNotNull(mentalHealthTemplateIds, "mentalHealthTemplateIds must not be null!");

    return getHibernateTemplate().execute(
        session -> new Hql()
            .select(
                mentalHealthTemplateMember.id(),
                mentalHealthTemplate.id(),
                mentalHealthTemplate.get("name"),
                medication.id(),
                medicationRoute.id(),
                medicationRoute.get("code"),
                medicationRoute.get("shortName"),
                medicationRoute.get("type")
            )
            .from(
                mentalHealthTemplate.innerJoin("mentalHealthTemplateMemberList").as(mentalHealthTemplateMember),
                mentalHealthTemplate.leftOuterJoin("medicationRoute").as(medicationRoute),
                mentalHealthTemplateMember.innerJoin("medication").as(medication)
            )
            .where(
                mentalHealthTemplateMember.notDeleted(),
                mentalHealthTemplate.notDeleted(),
                mentalHealthTemplate.id().in(mentalHealthTemplateIds)
            )

            .buildQuery(session, Object[].class)
            .list(
                new TupleProcessor<MentalHealthTemplateMemberDto>()
                {
                  @Override
                  protected MentalHealthTemplateMemberDto process(final boolean hasNextTuple) throws ProcessingException
                  {
                    final Long id = nextLong();
                    final Long templateId = nextLong();
                    final String templateName = nextString();
                    final Long medicationId = nextLong();
                    final Long routeId = nextLong();
                    final String routeCode = nextString();
                    final String routeName = nextString();
                    final MedicationRouteTypeEnum routeTypeEnum = next(MedicationRouteTypeEnum.class);

                    final NamedIdentityDto template = new NamedIdentityDto();
                    template.setName(templateName);
                    template.setId(templateId);

                    if (routeId != null)
                    {
                      final MedicationRouteDto medicationRouteDto = new MedicationRouteDto();
                      medicationRouteDto.setId(routeId);
                      medicationRouteDto.setCode(routeCode);
                      medicationRouteDto.setName(routeName);
                      medicationRouteDto.setType(routeTypeEnum);

                      return new MentalHealthTemplateMemberDto(id, medicationId, medicationRouteDto, template);
                    }
                    return new MentalHealthTemplateMemberDto(id, medicationId, null, template);
                  }
                })
    );
  }

  @Override
  public Map<Long, MedicationRouteDto> loadRoutesMap(final DateTime when)
  {
    final Map<Long, MedicationRouteDto> routesMap = new HashMap<>();
    getHibernateTemplate().execute(
        session -> new Hql()
            .select(
                medicationRoute.id(),
                medicationRoute.get("code"),
                medicationRoute.get("shortName"),
                medicationRoute.get("type")
            )
            .from(
                medicationRoute
            )
            .where(
                medicationRoute.notDeletedAndEffectiveAt(when)
            )
            .orderBy(medicationRoute.get("sortOrder"))
            .buildQuery(session, Object[].class)
            .list(
                new TupleProcessor<MedicationRouteDto>()
                {
                  @Override
                  protected MedicationRouteDto process(final boolean hasNextTuple) throws ProcessingException
                  {
                    final MedicationRouteDto routeDto = new MedicationRouteDto();
                    final Long routeId = next();
                    routeDto.setId(routeId);
                    routeDto.setCode(nextString());
                    routeDto.setName(nextString());
                    routeDto.setType(next());
                    routesMap.put(routeId, routeDto);
                    return routeDto;
                  }
                })
    );
    return routesMap;
  }

  @Override
  public Collection<MedicationsWarningDto> getCustomWarningsForMedication(
      @Nonnull final Set<Long> medicationIds,
      @Nonnull final DateTime when)
  {
    Preconditions.checkNotNull(medicationIds, "medicationIds must not be null!");
    Preconditions.checkNotNull(when, "when must not be null!");

    if (medicationIds.isEmpty())
    {
      return Collections.emptyList();
    }

    return getHibernateTemplate().execute(
        session -> new Hql()
            .select(
                medicationWarning.get("description"),
                medicationWarning.get("severity"),
                medication.id(),
                medicationVersion.get("name")
            )
            .from(
                medicationWarning.innerJoin("medication").as(medication),
                medication.innerJoin("versions").as(medicationVersion)
            )
            .where(
                medication.id().in(medicationIds),
                effectiveEntities(medicationVersion, medicationWarning).notDeletedAndEffectiveAt(when)
            )
            .buildQuery(session, Object[].class)
            .list(
                new TupleProcessor<MedicationsWarningDto>()
                {
                  @Override
                  protected MedicationsWarningDto process(final boolean hasNextTuple) throws ProcessingException
                  {
                    final String description = nextString();
                    final WarningSeverity severity = next(WarningSeverity.class);
                    final Long medicationId = nextLong();
                    final String medicationName = nextString();

                    final MedicationsWarningDto warningDto = new MedicationsWarningDto();
                    warningDto.setDescription(description);
                    warningDto.setSeverity(severity);
                    warningDto.setType(WarningType.CUSTOM);

                    warningDto.setMedications(Collections.singletonList(new NamedExternalDto(
                        String.valueOf(medicationId),
                        medicationName)));

                    return warningDto;
                  }
                }));
  }

  @Override
  public Long getMedicationIdForBarcode(@Nonnull final String barcode)
  {
    StringUtils.checkNotBlank(barcode, "barcode");

    return getHibernateTemplate().execute(
        session -> new Hql()
            .select(
                medication.id()
            )
            .from(
                medication
            )
            .where(
                medication.get("barcode").eq(barcode),
                medication.notDeleted()
            )
            .buildQuery(session, Long.class)
            .getSingleRowOrNull()
    );
  }
}
