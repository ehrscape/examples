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

package com.marand.thinkmed.medications.service.impl;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import com.marand.maf.core.Pair;
import com.marand.maf.core.activity.stream.UseCase;
import com.marand.maf.core.data.object.NamedIdentityDto;
import com.marand.maf.core.openehr.dao.EhrTaggingDao;
import com.marand.maf.core.service.RequestContextHolder;
import com.marand.maf.core.service.ServiceMethod;
import com.marand.maf.core.service.auditing.Auditing;
import com.marand.maf.core.service.auditing.Level;
import com.marand.maf.core.time.DateTimeFormatters;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.medications.tdo.MedicationAdministrationComposition;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.medications.tdo.MedicationReferenceWeightComposition;
import com.marand.openehr.rm.RmPath;
import com.marand.openehr.rm.TdoPathable;
import com.marand.thinkehr.session.EhrSessionCallback;
import com.marand.thinkehr.session.EhrSessioned;
import com.marand.thinkehr.session.template.EhrSessionTemplate;
import com.marand.thinkehr.tagging.dto.TagDto;
import com.marand.thinkmed.api.core.data.Catalog;
import com.marand.thinkmed.api.core.data.Named;
import com.marand.thinkmed.api.core.data.NamedIdentity;
import com.marand.thinkmed.api.medical.data.Care;
import com.marand.thinkmed.api.organization.data.KnownClinic;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.MedicationPreferencesUtil;
import com.marand.thinkmed.medications.MedicationsExternalValueType;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.TherapyTag;
import com.marand.thinkmed.medications.TherapyTaggingUtils;
import com.marand.thinkmed.medications.b2b.MedicationsConnector;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.impl.MedicationsEhrUtils;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.dao.EhrMedicationsDao;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dto.AdministrationTimingDto;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.DocumentationTherapiesDto;
import com.marand.thinkmed.medications.dto.DoseFormDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.MedicationSearchDto;
import com.marand.thinkmed.medications.dto.MedicationSimpleDto;
import com.marand.thinkmed.medications.dto.MedicationsCentralCaseDto;
import com.marand.thinkmed.medications.dto.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.dto.TherapyCardInfoDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.TherapyFlowDto;
import com.marand.thinkmed.medications.dto.TherapyFlowRowDto;
import com.marand.thinkmed.medications.dto.TherapyReloadAfterActionDto;
import com.marand.thinkmed.medications.dto.TherapyTemplateDto;
import com.marand.thinkmed.medications.dto.TherapyTemplatesDto;
import com.marand.thinkmed.medications.dto.TherapyViewDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.TherapyTaskDto;
import com.marand.thinkmed.medications.dto.administration.TherapyTimelineRowDto;
import com.marand.thinkmed.medications.dto.report.PatientDataForTherapyReportDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayElementReportDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayReportDto;
import com.marand.thinkmed.medications.process.MedicationsProcessService;
import com.marand.thinkmed.medications.provider.TherapyTasksProvider;
import com.marand.thinkmed.medications.service.MedicationsService;
import com.marand.thinkmed.medicationsexternal.dto.DoseRangeCheckDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationForWarningsSearchDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationsWarningDto;
import com.marand.thinkmed.medicationsexternal.service.MedicationsExternalService;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction;

/**
 * @author Bostjan Vester
 */
public class MedicationsServiceImpl implements MedicationsService, InitializingBean
{
  private MedicationsBo medicationsBo;
  private MedicationsDao medicationsDao;
  private EhrMedicationsDao ehrMedicationsDao;
  private MedicationsExternalService medicationsExternalService;
  private MedicationsProcessService medicationsProcessService;
  private TherapyTasksProvider therapyTasksProvider;
  private EhrTaggingDao ehrTaggingDao;

  private EhrSessionTemplate ehrSessionTemplate;

  private TherapyDisplayProvider therapyDisplayProvider;

  private MedicationsConnector medicationsConnector;

  public MedicationsBo getMedicationsBo()
  {
    return medicationsBo;
  }

  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  public void setMedicationsDao(final MedicationsDao medicationsDao)
  {
    this.medicationsDao = medicationsDao;
  }

  public void setEhrMedicationsDao(final EhrMedicationsDao ehrMedicationsDao)
  {
    this.ehrMedicationsDao = ehrMedicationsDao;
  }

  public void setMedicationsExternalService(final MedicationsExternalService medicationsExternalService)
  {
    this.medicationsExternalService = medicationsExternalService;
  }

  public void setMedicationsProcessService(final MedicationsProcessService medicationsProcessService)
  {
    this.medicationsProcessService = medicationsProcessService;
  }

  public void setEhrSessionTemplate(final EhrSessionTemplate ehrSessionTemplate)
  {
    this.ehrSessionTemplate = ehrSessionTemplate;
  }

  public void setTherapyTasksProvider(final TherapyTasksProvider therapyTasksProvider)
  {
    this.therapyTasksProvider = therapyTasksProvider;
  }

  public void setEhrTaggingDao(final EhrTaggingDao ehrTaggingDao)
  {
    this.ehrTaggingDao = ehrTaggingDao;
  }

  public void setTherapyDisplayProvider(final TherapyDisplayProvider therapyDisplayProvider)
  {
    this.therapyDisplayProvider = therapyDisplayProvider;
  }

  public void setMedicationsConnector(final MedicationsConnector medicationsConnector)
  {
    this.medicationsConnector = medicationsConnector;
  }

  @Override
  public void afterPropertiesSet() throws Exception
  {
    Assert.notNull(medicationsDao, "medicationsDao is required");
    Assert.notNull(ehrMedicationsDao, "ehrMedicationsDao is required");
    Assert.notNull(medicationsExternalService, "medicationsExternalService is required");
    Assert.notNull(medicationsProcessService, "medicationsProcessService is required");
    Assert.notNull(ehrSessionTemplate, "ehrSessionTemplate is required");
    Assert.notNull(therapyDisplayProvider, "therapyDisplayProvider is required");
    Assert.notNull(medicationsConnector, "medicationsConnector is required");
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_OUTPUT_RESULTS))
  @EhrSessioned
  public List<MedicationSimpleDto> findMedications()
  {
    return medicationsDao.findMedications(RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_INPUT_OUTPUT_RECORD))
  @EhrSessioned
  public List<MedicationSearchDto> findMedications(final String searchString)
  {
    final List<MedicationSearchDto> medications = medicationsConnector.loadMedicationsTree();
    return medicationsBo.filterMedicationsTree(medications, searchString);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_OUTPUT_RESULTS))
  @EhrSessioned
  public MedicationDataDto getMedicationData(final long medicationId)
  {
    return medicationsDao.getMedicationData(medicationId, RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_OUTPUT_RESULTS))
  @EhrSessioned
  public List<DoseRangeCheckDto> findDoseRangeChecks(final long medicationId)
  {
    final String doseRangeChecksProvider = medicationsExternalService.getDoseRangeChecksProvider();
    if (doseRangeChecksProvider != null)
    {
      final String externalId =
          medicationsDao.getMedicationExternalId(
              doseRangeChecksProvider, medicationId, RequestContextHolder.getContext().getRequestTimestamp());
      if (externalId != null)
      {
        return medicationsExternalService.findDoseRangeChecks("FDB", externalId);
      }
    }
    return new ArrayList<>();
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_OUTPUT_RESULTS))
  @EhrSessioned
  public List<MedicationsWarningDto> findMedicationWarnings(
      final long patientAgeInDays,
      final Double patientWeightInKg,
      final Integer gabInWeeks,
      final Double bsaInM2,
      final boolean isFemale,
      final List<String> diseaseTypeCodes,
      final List<String> patientAllergiesList,
      final List<MedicationForWarningsSearchDto> medicationSummaries)
  {
    //final List<String> diseaseTypeIcd9Codes = medicationsDao.getIcd9Codes(diseaseTypeCode);     //mapping for fdb
    final List<MedicationForWarningsSearchDto> medicationsForWarnings = new ArrayList<>();
    medicationsForWarnings.addAll(medicationSummaries);

    final List<MedicationsWarningDto> warnings = new ArrayList<>();
    for (final String externalSystem : medicationsExternalService.getWarningProviders())
    {
      fillMedicationsProductFlag(medicationsForWarnings);
      fillMedicationExternalIds(externalSystem, medicationsForWarnings);
      if (!medicationsForWarnings.isEmpty() || !patientAllergiesList.isEmpty() || !diseaseTypeCodes.isEmpty())
      {
        fillMedicationExternalValues(externalSystem, medicationsForWarnings);
        final List<String> allergiesExternalValues = getAllergiesExternalValues(externalSystem, patientAllergiesList);

        warnings.addAll(
            medicationsExternalService.findMedicationWarnings(
                externalSystem,
                patientAgeInDays,
                patientWeightInKg,
                gabInWeeks,
                bsaInM2,
                isFemale,
                diseaseTypeCodes,
                allergiesExternalValues,
                medicationsForWarnings));
        resetMedicationIdsToInternalIds(warnings, externalSystem);
      }
    }
    return warnings;
  }

  private void fillMedicationsProductFlag(final List<MedicationForWarningsSearchDto> medicationsForWarnings)
  {
    final Set<Long> medicationIdsSet = new HashSet<>();
    for (final MedicationForWarningsSearchDto medication : medicationsForWarnings)
    {
      medicationIdsSet.add(medication.getId());
    }
    final Map<Long, MedicationLevelEnum> medicationIdLevelMap = medicationsDao.getMedicationsLevels(medicationIdsSet);

    for (final MedicationForWarningsSearchDto medication : medicationsForWarnings)
    {
      final MedicationLevelEnum medicationLevelEnum = medicationIdLevelMap.get(medication.getId());
      medication.setProduct(medicationLevelEnum != MedicationLevelEnum.VTM);
    }
  }

  private void fillMedicationExternalIds(
      final String externalSystem,
      final List<MedicationForWarningsSearchDto> medicationsForWarnings)
  {
    final List<MedicationForWarningsSearchDto> medicationsWithExternalId = new ArrayList<>();
    for (final MedicationForWarningsSearchDto medication : medicationsForWarnings)
    {
      final String medicationExternalId = medicationsDao.getMedicationExternalId(
          externalSystem,
          medication.getId(),
          RequestContextHolder.getContext().getRequestTimestamp());
      if (medicationExternalId != null)
      {
        medication.setExternalId(medicationExternalId);
        medicationsWithExternalId.add(medication);
      }
    }
    medicationsForWarnings.clear();
    medicationsForWarnings.addAll(medicationsWithExternalId);
  }

  private void fillMedicationExternalValues(
      final String externalSystem,
      final List<MedicationForWarningsSearchDto> medicationsForWarnings)
  {
    final Set<String> unitsSet = new HashSet<>();
    final Set<String> routesSet = new HashSet<>();
    for (final MedicationForWarningsSearchDto medication : medicationsForWarnings)
    {
      if (medication.getDoseUnit() != null)
      {
        unitsSet.add(medication.getDoseUnit());
      }
      routesSet.add(medication.getRouteCode());
    }
    final Map<String, String> externalUnitsMap =
        medicationsDao.getMedicationExternalValues(externalSystem, MedicationsExternalValueType.UNIT, unitsSet);
    final Map<String, String> externalRoutesMap =
        medicationsDao.getMedicationExternalValues(externalSystem, MedicationsExternalValueType.ROUTE, routesSet);

    for (final MedicationForWarningsSearchDto medication : medicationsForWarnings)
    {
      if (medication.getDoseUnit() != null)
      {
        final String externalUnit = externalUnitsMap.get(medication.getDoseUnit());
        if (externalUnit != null)
        {
          medication.setDoseUnit(externalUnit);
        }
      }
      final String externalRoute = externalRoutesMap.get(medication.getRouteCode());
      if (externalRoute != null)
      {
        medication.setRouteCode(externalRoute);
      }
    }
  }

  private void resetMedicationIdsToInternalIds(final List<MedicationsWarningDto> warnings, final String externalSystem)
  {
    final DateTime requestTimestamp = RequestContextHolder.getContext().getRequestTimestamp();
    final Set<String> medicationExternalIds = new HashSet<>();
    for (final MedicationsWarningDto warning : warnings)
    {
      if (warning.getPrimaryMedication() != null)
      {
        medicationExternalIds.add(String.valueOf(warning.getPrimaryMedication().getId()));
      }
      if (warning.getSecondaryMedication() != null)
      {
        medicationExternalIds.add(String.valueOf(warning.getSecondaryMedication().getId()));
      }
    }
    final Map<Long, Long> medicationExternalIdMedicationIdMap =
        medicationsDao.getMedicationIdsFromExternalIds(externalSystem, medicationExternalIds, requestTimestamp);

    for (final MedicationsWarningDto warning : warnings)
    {
      final NamedIdentityDto primaryMedication = warning.getPrimaryMedication();
      if (primaryMedication != null)
      {
        primaryMedication.setId(medicationExternalIdMedicationIdMap.get(primaryMedication.getId()));
      }
      final NamedIdentityDto secondaryMedication = warning.getSecondaryMedication();
      if (secondaryMedication != null)
      {
        secondaryMedication.setId(medicationExternalIdMedicationIdMap.get(secondaryMedication.getId()));
      }
    }
  }

  private List<String> getAllergiesExternalValues(final String externalSystem, final List<String> patientAllergiesList)
  {
    final Map<String, String> externalAllergiesMap = medicationsDao.getMedicationExternalValues(
        externalSystem, MedicationsExternalValueType.ALLERGY, new HashSet<>(patientAllergiesList));

    return new ArrayList<>(externalAllergiesMap.values());
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_INPUT_OUTPUT_PAYLOAD))
  @EhrSessioned
  public String getMedicationOverviewExternalId(final long medicationId)
  {
    final String externalSystem = medicationsExternalService.getMedicationOverviewProvider();
    if (externalSystem == null)
    {
      return null;
    }
    return medicationsDao.getMedicationExternalId(
        externalSystem, medicationId, RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_INPUT_OUTPUT_PAYLOAD))
  @EhrSessioned
  public void tagTherapyForPrescription(
      final Long patientId,
      final String compositionId,
      final Long centralCaseId,
      final String ehrOrderName)
  {
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyInstructionPair =
        ehrMedicationsDao.getTherapyInstructionPair(patientId, compositionId, ehrOrderName);
    final RmPath rmPath = TdoPathable.pathOfItem(therapyInstructionPair.getFirst(), therapyInstructionPair.getSecond());

    ehrTaggingDao.tag(
        compositionId,
        new TagDto(
            TherapyTaggingUtils.generateTag(TherapyTag.PRESCRIPTION, centralCaseId),
            rmPath.getCanonicalString()));
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_INPUT_OUTPUT_PAYLOAD))
  @EhrSessioned
  public void untagTherapyForPrescription(
      final Long patientId,
      final String compositionId,
      final Long centralCaseId,
      final String ehrOrderName)
  {
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyInstructionPair =
        ehrMedicationsDao.getTherapyInstructionPair(patientId, compositionId, ehrOrderName);
    final RmPath rmPath = TdoPathable.pathOfItem(therapyInstructionPair.getFirst(), therapyInstructionPair.getSecond());

    ehrTaggingDao.deleteTags(
        compositionId,
        new TagDto(
            TherapyTaggingUtils.generateTag(TherapyTag.PRESCRIPTION, centralCaseId),
            rmPath.getCanonicalString()));
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void saveNewMedicationOrder(
      final long patientId,
      final List<TherapyDto> therapiesList,
      final Long centralCaseId,
      final Long careProviderId,
      final Long sessionId,
      final Named knownOrganizationalEntity,
      final NamedIdentity prescriber,
      final DateTime saveDateTime,
      final Locale locale)
  {
    final RoundsIntervalDto roundsInterval =
        MedicationPreferencesUtil.getRoundsInterval(sessionId, knownOrganizationalEntity);
    final DateTime when = saveDateTime != null ? saveDateTime : RequestContextHolder.getContext().getRequestTimestamp();
    final MedicationOrderComposition composition = ehrSessionTemplate.execute(
        new EhrSessionCallback<MedicationOrderComposition>()
        {
          @Override
          public MedicationOrderComposition doInSession() throws Throwable
          {
            return medicationsBo.saveNewMedicationOrder(
                patientId,
                therapiesList,
                centralCaseId,
                careProviderId,
                RequestContextHolder.getContext().getUserId(),
                prescriber,
                roundsInterval,
                when,
                locale);
          }
        }
    );
    try
    {
      medicationsProcessService.startTherapyProcess(patientId, composition, sessionId, knownOrganizationalEntity, when);
    }
    catch (final RuntimeException e)
    {
      ehrSessionTemplate.execute(
          new EhrSessionCallback<Void>()
          {
            @Override
            public Void doInSession() throws Throwable
            {
              ehrMedicationsDao.deleteTherapy(patientId, composition.getUid().getValue());
              return null;
            }
          }
      );
      throw new RuntimeException(e);
    }
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public TherapyFlowDto getTherapyFlow(
      final long patientId,
      final long centralCaseId,
      final Double patientHeight,
      final DateTime startDate,
      final int dayCount,
      final Integer todayIndex,
      final RoundsIntervalDto roundsInterval,
      final TherapySortTypeEnum therapySortTypeEnum,
      final KnownClinic department,
      final Locale locale)
  {
    final TherapyFlowDto therapyFlow = new TherapyFlowDto();
    final List<TherapyFlowRowDto> therapyRows =
        medicationsBo.getTherapyFlow(
            patientId,
            centralCaseId,
            patientHeight,
            startDate,
            dayCount,
            todayIndex,
            roundsInterval,
            therapySortTypeEnum,
            department,
            RequestContextHolder.getContext().getRequestTimestamp(),
            locale);
    therapyFlow.setTherapyRows(therapyRows);

    for (int day = 0; day < dayCount; day++)
    {
      final Double referenceWeight =
          ehrMedicationsDao.getPatientLastReferenceWeight(
              patientId,
              Intervals.infiniteTo(startDate.plusDays(day).plusDays(1)));
      if (referenceWeight != null)
      {
        therapyFlow.getReferenceWeightsDayMap().put(day, referenceWeight);
      }
    }
    return therapyFlow;
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public TherapyReloadAfterActionDto reloadSingleTherapyAfterAction(
      final long patientId,
      final String compositionUid,
      final String ehrOrderName,
      final RoundsIntervalDto roundsInterval)
  {
    final DateTime now = RequestContextHolder.getContext().getRequestTimestamp();
    return medicationsBo.reloadSingleTherapyAfterAction(patientId, compositionUid, ehrOrderName, roundsInterval, now);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  @UseCase("modifyTherapyUseCase")
  public void modifyTherapy(
      final long patientId,
      final TherapyDto therapy,
      final Long centralCaseId,
      final Long careProviderId,
      final Long sessionId,
      final Named knownOrganizationalEntity,
      final NamedIdentity prescriber,
      final DateTime saveDateTime,
      final Locale locale)
  {
    final MedicationOrderComposition composition =
        ehrMedicationsDao.loadMedicationOrderComposition(patientId, therapy.getCompositionUid());
    final MedicationInstructionInstruction instruction =
        MedicationsEhrUtils.getMedicationInstructionByEhrName(composition, therapy.getEhrOrderName());

    if (medicationsBo.isMedicationTherapyCompleted(composition, instruction))
    {
      throw new IllegalArgumentException("Therapy is finished.");
    }

    final boolean therapySuspended = medicationsBo.isTherapySuspended(composition, instruction);
    medicationsProcessService.sendModifyTherapyRequest(
        patientId,
        therapy,
        therapySuspended,
        centralCaseId,
        careProviderId,
        sessionId,
        knownOrganizationalEntity,
        prescriber,
        saveDateTime != null ? saveDateTime : RequestContextHolder.getContext().getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void abortTherapy(final long patientId, final String compositionUid, final String ehrOrderName)
  {
    final MedicationOrderComposition composition =
        ehrMedicationsDao.loadMedicationOrderComposition(patientId, compositionUid);
    final MedicationInstructionInstruction instruction =
        MedicationsEhrUtils.getMedicationInstructionByEhrName(composition, ehrOrderName);

    if (medicationsBo.isMedicationTherapyCompleted(composition, instruction))
    {
      throw new IllegalArgumentException("Therapy is finished.");
    }

    final boolean therapySuspended = medicationsBo.isTherapySuspended(composition, instruction);
    medicationsProcessService.sendAbortTherapyRequest(
        patientId, compositionUid, ehrOrderName, therapySuspended, RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void abortAllTherapies(final long patientId, final DateTime when)
  {
    updateAllTherapiesWithAction(patientId, when, MedicationActionEnum.ABORT);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void reviewTherapy(
      final long patientId,
      final String compositionUid,
      final String ehrOrderName,
      final Long sessionId,
      final Named knownOrganizationalEntity)
  {
    medicationsProcessService.sendReviewTherapyRequest(
        patientId,
        compositionUid,
        ehrOrderName,
        sessionId,
        knownOrganizationalEntity);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void suspendTherapy(final long patientId, final String compositionUid, final String ehrOrderName)
  {
    medicationsProcessService.sendSuspendTherapyRequest(
        patientId, compositionUid, ehrOrderName, RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void reissueTherapy(
      final long patientId,
      final String compositionUid,
      final String ehrOrderName,
      final Long sessionId,
      final Named knownOrganizationalEntity)
  {
    medicationsProcessService.sendReissueTherapyRequest(
        patientId,
        compositionUid,
        ehrOrderName,
        sessionId,
        knownOrganizationalEntity);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void createAdditionalAdministrationTask(
      final String therapyCompositionUid,
      final String ehrOrderName,
      final Long patientId,
      final StartAdministrationDto administrationDto)
  {
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyInstructionPair =
        ehrMedicationsDao.getTherapyInstructionPair(patientId, therapyCompositionUid, ehrOrderName);
    medicationsProcessService.createSingleAdministrationTask(
        therapyInstructionPair.getSecond(),
        therapyCompositionUid,
        patientId,
        administrationDto.getPlannedTime(),
        AdministrationTypeEnum.START,
        administrationDto.getPlannedDose());
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void suspendAllTherapies(final long patientId, final DateTime when)
  {
    updateAllTherapiesWithAction(patientId, when, MedicationActionEnum.SUSPEND);
  }

  private void updateAllTherapiesWithAction(
      final long patientId,
      final DateTime when,
      final MedicationActionEnum action)
  {
    final Interval searchInterval = Intervals.infiniteFrom(when);
    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionsList =
        ehrMedicationsDao.findMedicationInstructions(patientId, searchInterval, null);

    // we must update same instance of composition
    final Map<String, MedicationOrderComposition> compositionsMap = new HashMap<>();
    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> pair : instructionsList)
    {
      final MedicationOrderComposition composition = pair.getFirst();
      final String uid = composition.getUid().getValue();
      if (!compositionsMap.containsKey(uid))
      {
        compositionsMap.put(uid, composition);
      }
    }

    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> pair : instructionsList)
    {
      if (!medicationsBo.isMedicationTherapyCompleted(pair.getFirst(), pair.getSecond()))
      {
        final String compositionUid = pair.getFirst().getUid().getValue();
        final MedicationOrderComposition composition = compositionsMap.get(compositionUid);
        final MedicationInstructionInstruction instruction =
            MedicationsEhrUtils.getMedicationInstructionByEhrName(composition, pair.getSecond().getName().getValue());
        final boolean therapySuspended = medicationsBo.isTherapySuspended(composition, instruction);

        if (action == MedicationActionEnum.ABORT)
        {
          medicationsProcessService.sendAbortTherapyRequest(
              patientId, compositionUid, pair.getSecond().getName().getValue(), therapySuspended, when);
        }
        else if (action == MedicationActionEnum.SUSPEND)
        {
          if (!therapySuspended)
          {
            medicationsProcessService.sendSuspendTherapyRequest(
                patientId, compositionUid, pair.getSecond().getName().getValue(), when);
          }
        }
        else
        {
          throw new IllegalArgumentException("Action not supported");
        }
      }
    }
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_INPUT_OUTPUT_PAYLOAD))
  @EhrSessioned
  public List<MedicationForWarningsSearchDto> getTherapiesForWarningsSearch(final long patientId)
  {
    return medicationsBo.getTherapiesForWarningsSearch(patientId, RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public PatientDataForMedicationsDto getPatientData(final Long patientId, final Long episodeId)
  {
    return medicationsConnector.getPatientData(
        patientId, episodeId, RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public TherapyViewDto getTherapyViewDto(final Long patientId, final Catalog knownOrganizationalEntity)
  {
    final DateTime requestTimestamp = RequestContextHolder.getContext().getRequestTimestamp();
    final TherapyViewDto therapyViewDto = new TherapyViewDto();
    final MedicationsCentralCaseDto centralCaseDto =
        medicationsConnector.getCentralCaseForMedicationsDto(patientId, knownOrganizationalEntity);
    therapyViewDto.setMedicationsCentralCase(centralCaseDto);
    final Interval referenceWeightSearchInterval;
    final Catalog encounterKnownOrganizationalEntity;
    final Long sessionId;
    if (centralCaseDto != null)
    {
      if (!centralCaseDto.getCare().isOutpatient())
      {
        referenceWeightSearchInterval =
            new Interval(centralCaseDto.getCentralCaseEffective().getStart(), requestTimestamp);
        final Interval recentHospitalizationInterval = new Interval(requestTimestamp.minusHours(12), requestTimestamp);
        centralCaseDto.setRecentHospitalization(
            recentHospitalizationInterval.contains(centralCaseDto.getCentralCaseEffective().getStart()));
      }
      else
      {
        referenceWeightSearchInterval = new Interval(requestTimestamp.minusHours(24), requestTimestamp);
      }
      encounterKnownOrganizationalEntity = KnownClinic.Utils.fromName(centralCaseDto.getDepartmentName());
      sessionId = centralCaseDto.getSessionId();
    }
    else
    {
      referenceWeightSearchInterval = new Interval(requestTimestamp.minusHours(24), requestTimestamp);
      encounterKnownOrganizationalEntity = knownOrganizationalEntity;
      sessionId = null;
    }
    final Double referenceWeight = ehrMedicationsDao.getPatientLastReferenceWeight(patientId, referenceWeightSearchInterval);
    therapyViewDto.setReferenceWeight(referenceWeight);

    final AdministrationTimingDto administrationTiming =
        MedicationPreferencesUtil.getAdministrationTiming(sessionId, encounterKnownOrganizationalEntity);
    therapyViewDto.setAdministrationTiming(administrationTiming);

    if (encounterKnownOrganizationalEntity != null)
    {
      final List<String> customGroups = medicationsDao.getCustomGroupNames(encounterKnownOrganizationalEntity.name());
      therapyViewDto.setCustomGroups(customGroups);
    }

    final RoundsIntervalDto roundsInterval =
        MedicationPreferencesUtil.getRoundsInterval(sessionId, encounterKnownOrganizationalEntity);
    therapyViewDto.setRoundsInterval(roundsInterval);

    return therapyViewDto;
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public byte[] getMedicationDocument(final String reference)
  {
    return medicationsConnector.getPdfDocument(reference);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public List<Interval> getPatientBaselineInfusionIntervals(final Long patientId)
  {
    return ehrMedicationsDao.getPatientBaselineInfusionIntervals(
        patientId, Intervals.infiniteFrom(RequestContextHolder.getContext().getRequestTimestamp()));
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public PatientDataForTherapyReportDto getTherapyDayPatientReportData(
      final long patientId,
      final Long encounterId,
      final Care encounterCareProvisionType,
      final Locale locale,
      final DateTime when)
  {
    final PatientDataForTherapyReportDto patientData =
        medicationsConnector.getPatientDataForTherapyReport(
            patientId,
            encounterId,
            encounterCareProvisionType,
            when,
            locale);
    if (patientData != null)
    {
      final Double referenceWeight =
          ehrMedicationsDao.getPatientLastReferenceWeight(patientId, Intervals.infiniteTo(when));
      patientData.setWeight(referenceWeight != null ? (referenceWeight + " " + "kg") : null);
    }

    return patientData;
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public TherapyDayReportDto getTherapyDayReportData(
      final long patientId,
      final Double patientHeight,
      final Long encounterId,
      final Care encounterCare,
      final RoundsIntervalDto roundsInterval,
      final Locale locale,
      final DateTime when)
  {
    final TherapyDayReportDto reportDto = new TherapyDayReportDto(false);
    reportDto.setPatientSortOrder(0);
    final PatientDataForTherapyReportDto therapyDayPatientReportData = getTherapyDayPatientReportData(
        patientId,
        encounterId,
        encounterCare,
        locale,
        when);
    reportDto.setPatientData(therapyDayPatientReportData);

    if (therapyDayPatientReportData == null)
    {
      return null;
    }

    final List<TherapyDto> therapiesList =
        medicationsBo.getPatientTherapiesForReport(patientId, patientHeight, when, roundsInterval, when);

    final List<TherapyDayElementReportDto<SimpleTherapyDto>> simpleElementsList = new ArrayList<>();
    final List<TherapyDayElementReportDto<ComplexTherapyDto>> complexElementsList = new ArrayList<>();

    final KnownClinic clinic = medicationsConnector.getClinic(therapyDayPatientReportData.getDepartmentCode());

    final Set<Long> medicationIds = getMedicationIds(therapiesList);

    final Map<Long, Pair<String, Integer>> customGroupNameSortOrderMap =
        medicationsDao.getCustomGroupNameSortOrderMap(clinic.name(), medicationIds);

    for (final TherapyDto therapy : therapiesList)
    {
      therapyDisplayProvider.fillDisplayValues(therapy, true, true, locale);
      final DateTime therapyStart =
          medicationsBo.getTherapyStart(patientId, therapy.getCompositionUid(), therapy.getEhrOrderName());
      final int therapyConsecutiveDay =
          medicationsBo.getTherapyConsecutiveDay(therapyStart, when, when, therapy.getPastDaysOfTherapy());
      final boolean therapyActive =
          medicationsBo.isTherapyActive(
              therapy.getDaysOfWeek(),
              therapy.getDosingDaysFrequency(),
              new Interval(therapy.getStart(), therapy.getEnd() != null ? therapy.getEnd() : Intervals.INFINITE.getEnd()),
              when);

      if (therapy instanceof SimpleTherapyDto)
      {
        final TherapyDayElementReportDto<SimpleTherapyDto> elementDto =
            createTherapyDayElementReportDto(
                (SimpleTherapyDto)therapy,
                therapyConsecutiveDay,
                therapyActive,
                customGroupNameSortOrderMap);
        simpleElementsList.add(elementDto);
      }
      else
      {
        final TherapyDayElementReportDto<ComplexTherapyDto> elementDto =
            createTherapyDayElementReportDto(
                (ComplexTherapyDto)therapy,
                therapyConsecutiveDay,
                therapyActive,
                customGroupNameSortOrderMap);
        complexElementsList.add(elementDto);
      }
    }

    final Collator collator = Collator.getInstance();
    Collections.sort(
        simpleElementsList, new Comparator<TherapyDayElementReportDto<SimpleTherapyDto>>()
    {
      @Override
      public int compare(
          final TherapyDayElementReportDto<SimpleTherapyDto> o1,
          final TherapyDayElementReportDto<SimpleTherapyDto> o2)
      {
        //inactive therapies last
        if (o1.isActive() && !o2.isActive())
        {
          return -1;
        }
        if (!o1.isActive() && o2.isActive())
        {
          return 1;
        }
        return collator.compare(o1.getOrder().getTherapyDescription(), o2.getOrder().getTherapyDescription());
      }
    });
    Collections.sort(
        complexElementsList, new Comparator<TherapyDayElementReportDto<ComplexTherapyDto>>()
    {
      @Override
      public int compare(
          final TherapyDayElementReportDto<ComplexTherapyDto> o1,
          final TherapyDayElementReportDto<ComplexTherapyDto> o2)
      {
        final boolean firstOrderIsBaselineInfusion = o1.getOrder().isBaselineInfusion();
        final boolean secondOrderIsBaselineInfusion = o2.getOrder().isBaselineInfusion();

        //inactive therapies last
        if (o1.isActive() && !o2.isActive())
        {
          return -1;
        }
        if (!o1.isActive() && o2.isActive())
        {
          return 1;
        }
        //baseline infusions first
        if (firstOrderIsBaselineInfusion && !secondOrderIsBaselineInfusion)
        {
          return -1;
        }
        if (!firstOrderIsBaselineInfusion && secondOrderIsBaselineInfusion)
        {
          return 1;
        }
        return collator.compare(o1.getOrder().getTherapyDescription(), o2.getOrder().getTherapyDescription());
      }
    });

    reportDto.setSimpleElements(simpleElementsList);
    reportDto.setComplexElements(complexElementsList);
    return reportDto;
  }

  private Set<Long> getMedicationIds(final List<TherapyDto> therapiesList)
  {
    final Set<Long> medicationCodesList = new HashSet<>();

    for (final TherapyDto therapyDto : therapiesList)
    {
      final Long mainMedicationId = medicationsBo.getMainMedicationId(therapyDto);
      if (mainMedicationId != null)
      {
        medicationCodesList.add(mainMedicationId);
      }
    }

    return medicationCodesList;
  }

  private <M extends TherapyDto> TherapyDayElementReportDto<M> createTherapyDayElementReportDto(
      final M therapy,
      final int therapyConsecutiveDay,
      final boolean therapyActive,
      final Map<Long, Pair<String, Integer>> customGroupNameSortOrderMap)
  {
    final TherapyDayElementReportDto<M> reportElement = new TherapyDayElementReportDto<>();
    reportElement.setTherapyConsecutiveDay(String.valueOf(therapyConsecutiveDay));
    reportElement.setOrder(therapy);
    reportElement.setTherapyStart(DateTimeFormatters.shortDateTime().print(therapy.getStart()));
    if (therapy.getEnd() != null)
    {
      reportElement.setTherapyEnd(DateTimeFormatters.shortDateTime().print(therapy.getEnd()));
    }
    reportElement.setActive(therapyActive);
    final Long medicationId = medicationsBo.getMainMedicationId(therapy);
    if (medicationId != null)
    {
      final Pair<String, Integer> customGroupNameSortOrderPair = customGroupNameSortOrderMap.get(medicationId);

      if (customGroupNameSortOrderPair != null)
      {
        reportElement.setCustomGroupName(customGroupNameSortOrderPair.getFirst());
        reportElement.setCustomGroupSortOrder(
            customGroupNameSortOrderPair.getSecond() != null ? customGroupNameSortOrderPair.getSecond() : Integer.MAX_VALUE);
      }
      else
      {
        reportElement.setCustomGroupSortOrder(Integer.MAX_VALUE);
      }
    }
    else
    {
      reportElement.setCustomGroupSortOrder(Integer.MAX_VALUE);
    }
    return reportElement;
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public String getTherapyFormattedDisplay(final long patientId, final String therapyId, final Locale locale)
  {
    return medicationsBo.getTherapyFormattedDisplay(
        patientId, therapyId, RequestContextHolder.getContext().getRequestTimestamp(), locale);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public TherapyCardInfoDto getTherapyCardInfoData(
      final long patientId,
      final Double patientHeight,
      final String compositionId,
      final String ehrOrderName,
      final Interval similarTherapiesInterval,
      final Locale locale)
  {
    return medicationsBo.getTherapyCardInfoData(
        patientId,
        patientHeight,
        compositionId,
        ehrOrderName,
        locale,
        similarTherapiesInterval,
        RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public TherapyDto fillTherapyDisplayValues(final TherapyDto therapy, final Locale locale)
  {
    therapyDisplayProvider.fillDisplayValues(therapy, true, true, locale);
    return therapy;
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public List<MedicationDto> findSimilarMedications(final long medicationId, final String routeCode)
  {
    return medicationsDao.findSimilarMedications(
        medicationId, routeCode, RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public List<MedicationDto> getMedicationProducts(final long medicationId, final String routeCode)
  {
    return medicationsDao.getMedicationProducts(
        medicationId, routeCode, RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_INPUT_OUTPUT_PAYLOAD))
  @EhrSessioned
  public List<MedicationRouteDto> getRoutes()
  {
    return medicationsDao.getRoutes(RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_INPUT_OUTPUT_PAYLOAD))
  @EhrSessioned
  public List<DoseFormDto> getDoseForms()
  {
    return medicationsDao.getDoseForms(RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_INPUT_OUTPUT_PAYLOAD))
  @EhrSessioned
  public List<String> getMedicationBasicUnits()
  {
    return medicationsDao.getMedicationBasicUnits();
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void savePatientReferenceWeight(final long patientId, final double weight)
  {
    final MedicationReferenceWeightComposition comp = medicationsBo.buildReferenceWeightComposition(
        weight,
        RequestContextHolder.getContext().getRequestTimestamp(),
        RequestContextHolder.getContext().getUserId());
    ehrMedicationsDao.savePatientReferenceWeight(patientId, comp);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void saveConsecutiveDays(
      final Long patientId, final String compositionUid, final String ehrOrderName, final Integer consecutiveDays)
  {
    medicationsBo.saveConsecutiveDays(
        patientId,
        compositionUid,
        ehrOrderName,
        RequestContextHolder.getContext().getUserId(),
        consecutiveDays);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public List<TherapyDto> getLastTherapiesForPreviousHospitalization(final long patientId, final Double patientHeight, final Locale locale)
  {
    final DateTime when = RequestContextHolder.getContext().getRequestTimestamp();
    final DateTime dischargeTimestamp = medicationsConnector.getLastDischargeEncounterTime(patientId);
    if (dischargeTimestamp != null)
    {
      final Interval lastDayBeforeDischarge =
          new Interval(dischargeTimestamp.minusHours(24), dischargeTimestamp.plusMinutes(1));
      final Interval referenceWeightInterval = Intervals.infiniteTo(when);
      final Double referenceWeight =
          ehrMedicationsDao.getPatientLastReferenceWeight(patientId, referenceWeightInterval);

      //??
      //final Double height = patientDataForMedicationsProvider.getPatientLastHeight(patientId, new Interval(Intervals.INFINITE.getStart(), when));
      final List<TherapyDto> therapies = medicationsBo.getTherapies(patientId, lastDayBeforeDischarge, null, patientHeight, null, when);

      for (final TherapyDto therapyDto : therapies)
      {
        if (therapyDto instanceof ComplexTherapyDto && referenceWeight != null)
        {
          if (((ComplexTherapyDto)therapyDto).isContinuousInfusion())
          {
            medicationsBo.fillInfusionRateFromFormula((ComplexTherapyDto)therapyDto, referenceWeight, patientHeight, when);
          }
          else
          {
            medicationsBo.fillInfusionFormulaFromRate((ComplexTherapyDto)therapyDto, referenceWeight, patientHeight, when);
          }
        }
        therapyDisplayProvider.fillDisplayValues(therapyDto, true, true, locale);
      }

      return therapies;
    }
    return new ArrayList<>();
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_OUTPUT_RESULTS))
  @EhrSessioned
  public TherapyTemplatesDto getTherapyTemplates(
      final Long userId,
      @Nullable final Long departmentId,
      @Nullable final Long patientId,
      @Nullable final Double referenceWeight,
      @Nullable final Double height,
      final Locale locale)
  {
    return medicationsDao.getTherapyTemplates(
        userId,
        departmentId,
        patientId,
        referenceWeight,
        height,
        RequestContextHolder.getContext().getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public long saveTherapyTemplate(final TherapyTemplateDto therapyTemplate)
  {
    return medicationsDao.saveTherapyTemplate(therapyTemplate);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void deleteTherapyTemplate(final long templateId)
  {
    medicationsDao.deleteTherapyTemplate(templateId);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_OUTPUT_RESULTS))
  @EhrSessioned
  public List<TherapyTimelineRowDto> getTherapyTimelineData(
      final long patientId,
      final long centralCaseId,
      final Interval interval,
      final RoundsIntervalDto roundsInterval,
      final TherapySortTypeEnum therapySortTypeEnum,
      final KnownClinic department,
      final Locale locale)
  {
    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs =
        ehrMedicationsDao.findMedicationInstructions(patientId, interval, null);

    final Map<String, List<MedicationAdministrationComposition>> administrations =
        ehrMedicationsDao.getTherapiesAdministrations(patientId, instructionPairs);

    final List<TherapyTaskDto> tasks = therapyTasksProvider.findTherapyTasks(patientId, null, interval);
    return medicationsBo.buildTherapyTimeline(
        patientId,
        centralCaseId,
        instructionPairs,
        administrations,
        tasks,
        interval,
        roundsInterval,
        therapySortTypeEnum,
        department,
        RequestContextHolder.getContext().getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_OUTPUT_RESULTS))
  @EhrSessioned
  public DocumentationTherapiesDto getTherapyDataForDocumentation(
      final Long patientId,
      final Interval centralCaseEffective,
      final Long centralCaseId,
      final KnownClinic department,
      final boolean isOutpatient,
      final Locale locale)
  {
    final DateTime when = RequestContextHolder.getContext().getRequestTimestamp();
    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs =
        ehrMedicationsDao.findMedicationInstructions(patientId, null, centralCaseId);

    return medicationsBo.findTherapyGroupsForDocumentation(
        patientId,
        centralCaseId,
        centralCaseEffective,
        instructionPairs,
        department,
        isOutpatient,
        when,
        locale);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void confirmTherapyAdministration(
      final String therapyUid,
      final String ehrOrderName,
      final Long patientId,
      final AdministrationDto administrationDto,
      final boolean editMode,
      final boolean administrationSuccessful,
      final Long centralCaseId,
      final String departmentName,
      final Long careProviderId,
      final Long sessionId,
      final Locale locale)
  {
    final Long userId = RequestContextHolder.getContext().getUserId();
    final DateTime when = RequestContextHolder.getContext().getRequestTimestamp();
    final String administrationUid = medicationsBo.confirmTherapyAdministration(
        therapyUid,
        ehrOrderName,
        patientId,
        userId,
        centralCaseId,
        careProviderId,
        administrationDto,
        administrationSuccessful,
        when,
        locale);
    if (!administrationDto.isAdditionalAdministration() && !editMode)
    {
      therapyTasksProvider.associateTaskWithAdministration(administrationDto.getTaskId(), administrationUid);
      therapyTasksProvider.completeTasks(administrationDto.getTaskId());
    }
    if (administrationDto.getTriggersTherapyId() != null)
    {
      medicationsProcessService.generateInitialTasks(
          patientId,
          administrationDto.getTriggersTherapyId(),
          sessionId,
          departmentName,
          true,
          administrationDto.getAdministrationTime().getMillis());
    }
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void rescheduleTasks(
      final String taskId,
      final DateTime fromTime,
      final long moveTimeInMillis,
      final boolean rescheduleSingleTask,
      final String therapyId)
  {
    therapyTasksProvider.rescheduleTasks(taskId, fromTime, moveTimeInMillis, rescheduleSingleTask, therapyId);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void deleteTask(final String taskId, final String comment)
  {
    therapyTasksProvider.deleteTask(taskId, comment);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void deleteAdministration(
      final long patientId,
      final String administrationId,
      final String taskId,
      final String comment)
  {
    medicationsBo.deleteAdministration(patientId, administrationId, comment);
    if (taskId != null)
    {
      medicationsProcessService.undoCompleteTask(taskId);
    }
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_OUTPUT_RESULTS))
  @EhrSessioned
  public List<? extends NamedIdentity> getCareProfessionals()
  {
    return medicationsConnector.getMedicalStaff();
  }
}
