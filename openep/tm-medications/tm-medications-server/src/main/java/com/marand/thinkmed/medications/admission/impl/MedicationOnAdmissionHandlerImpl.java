package com.marand.thinkmed.medications.admission.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nullable;

import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.EnumUtils;
import com.marand.maf.core.Pair;
import com.marand.maf.core.openehr.dao.openehr.TaggingOpenEhrDao;
import com.marand.maf.core.openehr.visitor.IspekTdoDataSupport;
import com.marand.maf.core.openehr.visitor.TdoPopulatingVisitor;
import com.marand.maf.core.service.RequestContextHolder;
import com.marand.maf.core.service.UserMetadata;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOnAdmissionComposition;
import com.marand.openehr.medications.tdo.MedicationOnAdmissionComposition.MedicationDetailSection;
import com.marand.openehr.rm.RmPath;
import com.marand.openehr.rm.TdoPathable;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkehr.tagging.dto.TagDto;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.MedicationOrderActionEnum;
import com.marand.thinkmed.medications.ParticipationTypeEnum;
import com.marand.thinkmed.medications.TherapySourceGroupEnum;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.TherapyTaggingUtils;
import com.marand.thinkmed.medications.admission.MedicationOnAdmissionHandler;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.business.util.TherapyIdUtils;
import com.marand.thinkmed.medications.connector.MedicationsConnector;
import com.marand.thinkmed.medications.converter.therapy.MedicationConverterSelector;
import com.marand.thinkmed.medications.converter.therapy.MedicationFromEhrConverter;
import com.marand.thinkmed.medications.converter.therapy.MedicationToEhrConverter;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.discharge.MedicationOnDischargeHandler;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.admission.AdmissionSourceMedicationDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionGroupDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionReconciliationDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionStatus;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeDto;
import com.marand.thinkmed.medications.dto.reconsiliation.SourceMedicationDto;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Nejc Korasa
 */
public class MedicationOnAdmissionHandlerImpl implements MedicationOnAdmissionHandler
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private TherapyDisplayProvider therapyDisplayProvider;
  private MedicationsConnector medicationsConnector;
  private MedicationOnDischargeHandler medicationOnDischargeHandler;
  private MedicationFromEhrConverter.MedicationDataProvider medicationDataProvider;
  private TaggingOpenEhrDao taggingOpenEhrDao;

  @Required
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Required
  public void setTherapyDisplayProvider(final TherapyDisplayProvider therapyDisplayProvider)
  {
    this.therapyDisplayProvider = therapyDisplayProvider;
  }

  @Required
  public void setMedicationsConnector(final MedicationsConnector medicationsConnector)
  {
    this.medicationsConnector = medicationsConnector;
  }

  @Required
  public void setMedicationDataProvider(final MedicationFromEhrConverter.MedicationDataProvider medicationDataProvider)
  {
    this.medicationDataProvider = medicationDataProvider;
  }

  @Required
  public void setTaggingOpenEhrDao(final TaggingOpenEhrDao taggingOpenEhrDao)
  {
    this.taggingOpenEhrDao = taggingOpenEhrDao;
  }

  @Required
  public void setMedicationOnDischargeHandler(final MedicationOnDischargeHandler medicationOnDischargeHandler)
  {
    this.medicationOnDischargeHandler = medicationOnDischargeHandler;
  }

  @Override
  public List<String> saveMedicationsOnAdmission(
      final String patientId,
      final List<MedicationOnAdmissionDto> medicationElementDtoList,
      @Nullable final List<String> compositionIdsToDelete,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final String userId,
      final NamedExternalDto prescriber,
      final DateTime when,
      final Locale locale)
  {
    final List<String> savedCompositionsIds = new ArrayList<>();
    for (final MedicationOnAdmissionDto medicationGroupElementDto : medicationElementDtoList)
    {
      final TherapyDto therapy = medicationGroupElementDto.getTherapy();
      if (therapy.getCompositionUid() == null)
      {
        final MedicationOnAdmissionComposition composition =
            buildMedicationOnAdmissionComposition(
                medicationGroupElementDto,
                centralCaseId,
                careProviderId,
                userId,
                prescriber,
                when,
                locale);

        final String compositionId = medicationsOpenEhrDao.saveComposition(
            patientId,
            composition,
            therapy.getCompositionUid());

        tagAdmissionCompositionWithSourceId(
            medicationGroupElementDto.getSourceGroupEnum(),
            medicationGroupElementDto.getSourceId(),
            compositionId,
            patientId);

        savedCompositionsIds.add(compositionId);
      }
      else
      {
        final String compositionId = modifyAdmissionComposition(patientId, locale, therapy);
        savedCompositionsIds.add(compositionId);
      }
    }

    if (compositionIdsToDelete != null)
    {
      deleteMedicationsOnAdmission(patientId, compositionIdsToDelete);
    }
    return savedCompositionsIds;
  }

  @Override
  public String modifyAdmissionComposition(final String patientId, final Locale locale, final TherapyDto therapy)
  {
    final String oldCompositionUid = therapy.getCompositionUid();
    final MedicationOnAdmissionComposition composition =
        medicationsOpenEhrDao.loadMedicationOnAdmissionComposition(patientId, oldCompositionUid);

    final MedicationInstructionInstruction instruction = buildInstructionFromTherapyDto(
        locale,
        therapy);

    final List<MedicationInstructionInstruction> instructionList = new ArrayList<>();
    instructionList.add(instruction);

    composition.getMedicationDetail().setMedicationInstruction(instructionList);

    updateMedicationOnAdmissionContext(composition);

    final Set<TagDto> oldTags = taggingOpenEhrDao.getTags(oldCompositionUid);

    final String updatedCompositionId =  medicationsOpenEhrDao.saveComposition(
        patientId,
        composition,
        oldCompositionUid);

    // copy all old tags
    taggingOpenEhrDao.tag(updatedCompositionId, oldTags.toArray(new TagDto[oldTags.size()]));

    return updatedCompositionId;
  }

  private void tagAdmissionCompositionWithSourceId(
      final TherapySourceGroupEnum groupEnum,
      final String sourceId,
      final String compositionId,
      final String patientId)
  {
    if (sourceId != null && groupEnum != null)
    {
      final String sourceIdTag = TherapySourceGroupEnum.createTherapyTagSourceId(
          groupEnum,
          sourceId);

      final MedicationOnAdmissionComposition medicationOnAdmissionComposition =
          medicationsOpenEhrDao.loadMedicationOnAdmissionComposition(patientId, compositionId);

      final RmPath rmPath = TdoPathable.pathOfItem(medicationOnAdmissionComposition, medicationOnAdmissionComposition);
      taggingOpenEhrDao.tag(
          compositionId,
          new TagDto(TherapyTaggingUtils.generateSourceTag(sourceIdTag), rmPath.getCanonicalString()));
    }
  }

  private MedicationOnAdmissionComposition buildMedicationOnAdmissionComposition(
      final MedicationOnAdmissionDto medicationGroupElement,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final String userId,
      final NamedExternalDto prescriber,
      final DateTime when,
      final Locale locale)
  {
    final MedicationOnAdmissionComposition composition = new MedicationOnAdmissionComposition();
    final TherapyDto therapyDto = medicationGroupElement.getTherapy();

    final MedicationInstructionInstruction instruction = buildInstructionFromTherapyDto(locale, therapyDto);

    composition.setMedicationDetail(new MedicationDetailSection());
    composition.getMedicationDetail().getMedicationInstruction().add(instruction);

    addMedicationOnAdmissionContext(
        composition,
        RequestContextHolder.getContext().getUserMetadata().map(UserMetadata::getFullName).get(),
        prescriber,
        centralCaseId,
        careProviderId,
        when);

    return composition;
  }

  private MedicationInstructionInstruction buildInstructionFromTherapyDto(final Locale locale, final TherapyDto therapyDto)
  {
    if (therapyDto.getTherapyDescription() == null)
    {
      therapyDisplayProvider.fillDisplayValues(therapyDto, true, true, locale);
    }

    final MedicationToEhrConverter<?> therapyConverter = MedicationConverterSelector.getConverter(therapyDto);
    return therapyConverter.createInstructionFromTherapy(therapyDto);
  }

  private void addMedicationOnAdmissionContext(
      final MedicationOnAdmissionComposition admissionComposition,
      final String composer,
      final NamedExternalDto prescriber,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final DateTime when)
  {
    MedicationsEhrUtils.addContext(admissionComposition, centralCaseId, careProviderId, when);
    final TdoPopulatingVisitor.DataContext dataContext =
        TdoPopulatingVisitor.getSloveneContext(when)
            .withCompositionDynamic(true)
            .withReplaceParticipation(true)
            .withCompositionComposer(
                IspekTdoDataSupport.getPartyIdentified(composer, RequestContextHolder.getContext().getUserId()));

    MedicationsEhrUtils.setContextParticipation(dataContext, prescriber, ParticipationTypeEnum.PRESCRIBER);
    new TdoPopulatingVisitor().visitBean(admissionComposition, dataContext);
  }

  @Override
  public void deleteMedicationsOnAdmission(
      final String patientId,
      final List<String> compositionIds)
  {
    for (final String compositionId : compositionIds)
    {
      medicationsOpenEhrDao.deleteComposition(patientId, compositionId);
    }
  }

  @Override
  public List<MedicationOnAdmissionDto> getMedicationsOnAdmission(
      final String patientId,
      final DateTime fromDate,
      final DateTime when,
      final Locale locale)
  {
    final List<MedicationOnAdmissionDto> medicationOnAdmissionDtoList = new ArrayList<>();
    final List<MedicationOnAdmissionComposition> compositions = medicationsOpenEhrDao.findMedicationOnAdmissionCompositions(
        patientId,
        fromDate);

    for (final MedicationOnAdmissionComposition composition : compositions)
    {
      final MedicationInstructionInstruction instruction = composition.getMedicationDetail()
          .getMedicationInstruction()
          .get(0);

      final MedicationFromEhrConverter<?> converter = MedicationConverterSelector.getConverter(instruction);
      final TherapyDto therapyDto = converter.createTherapyFromInstruction(
          instruction,
          composition.getUid().getValue(),
          instruction.getName().getValue(),
          DataValueUtils.getDateTime(composition.getCompositionEventContext().getStartTime()),
          when,
          medicationDataProvider);

      therapyDisplayProvider.fillDisplayValues(therapyDto, true, true, locale);

      final MedicationOnAdmissionDto onAdmissionDto = new MedicationOnAdmissionDto();
      onAdmissionDto.setTherapy(therapyDto);

      fillStatusAndChangeReasonFromComposition(onAdmissionDto, composition);

      setSourceDataToMedicationOnAdmissionDto(composition.getUid().getValue(), onAdmissionDto);

      medicationOnAdmissionDtoList.add(onAdmissionDto);
    }
    return medicationOnAdmissionDtoList;
  }

  @Override
  public List<MedicationOnAdmissionReconciliationDto> getMedicationsOnAdmissionForReconciliation(
      final String patientId,
      final DateTime fromDate,
      final DateTime when,
      final Locale locale)
  {
    final List<MedicationOnAdmissionReconciliationDto> medicationOnAdmissionDtoList = new ArrayList<>();
    final List<MedicationOnAdmissionComposition> compositions = medicationsOpenEhrDao.findMedicationOnAdmissionCompositions(
        patientId,
        fromDate);

    for (final MedicationOnAdmissionComposition composition : compositions)
    {
      final MedicationInstructionInstruction instruction = composition.getMedicationDetail()
          .getMedicationInstruction()
          .get(0);

      final MedicationFromEhrConverter<?> converter = MedicationConverterSelector.getConverter(instruction);
      final TherapyDto therapyDto = converter.createTherapyFromInstruction(
          instruction,
          composition.getUid().getValue(),
          instruction.getName().getValue(),
          DataValueUtils.getDateTime(composition.getCompositionEventContext().getStartTime()),
          when,
          medicationDataProvider);

      therapyDisplayProvider.fillDisplayValues(therapyDto, true, true, locale);

      final MedicationOnAdmissionReconciliationDto onAdmissionDto = new MedicationOnAdmissionReconciliationDto();
      onAdmissionDto.setTherapy(therapyDto);

      final MedicationActionAction latestAction =
          MedicationsEhrUtils.getLatestAction(composition.getMedicationDetail().getMedicationAction());

      if (latestAction != null)
      {
        onAdmissionDto.setChangeReasonDto(MedicationsEhrUtils.getTherapyChangeReasonDtoFromAction(latestAction));

        final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(latestAction);
        if (actionEnum == MedicationActionEnum.ABORT)
        {
          onAdmissionDto.setStatusEnum(TherapyStatusEnum.ABORTED);
        }
        else if (actionEnum == MedicationActionEnum.CANCEL)
        {
          onAdmissionDto.setStatusEnum(TherapyStatusEnum.CANCELLED);
        }
        else if (actionEnum == MedicationActionEnum.SUSPEND)
        {
          onAdmissionDto.setStatusEnum(TherapyStatusEnum.SUSPENDED);
        }
      }
      medicationOnAdmissionDtoList.add(onAdmissionDto);
    }
    return medicationOnAdmissionDtoList;
  }

  private void setSourceDataToMedicationOnAdmissionDto(
      final String compositionId,
      final MedicationOnAdmissionDto onAdmissionDto)
  {
    final Set<TagDto> tags = taggingOpenEhrDao.getTags(compositionId);
    for (final TagDto tag : tags)
    {
      final String therapySourceId = TherapyTaggingUtils.getTherapySourceIdFromTag(tag.getTag());
      if (therapySourceId != null)
      {
        final Pair<TherapySourceGroupEnum, String> sourceIdValues =
            TherapySourceGroupEnum.getSourceGroupEnumAndSourceIdFromTherapyTag(therapySourceId);
        if (sourceIdValues != null)
        {
          onAdmissionDto.setSourceGroupEnum(sourceIdValues.getFirst());
          onAdmissionDto.setSourceId(sourceIdValues.getSecond());
        }
      }
    }
  }

  private void fillStatusAndChangeReasonFromComposition(
      final MedicationOnAdmissionDto onAdmissionDto,
      final MedicationOnAdmissionComposition composition)
  {
    final MedicationActionAction latestAction =
        MedicationsEhrUtils.getLatestAction(composition.getMedicationDetail().getMedicationAction());

    final MedicationOnAdmissionStatus status = latestAction == null
                                               ? MedicationOnAdmissionStatus.PENDING
                                               : getMedicationOnAdmissionStatusFromMedicationAction(latestAction);
    if (status == null)
    {
      onAdmissionDto.setStatus(MedicationOnAdmissionStatus.PENDING);
    }
    else
    {
      onAdmissionDto.setStatus(status);
    }
    if (latestAction != null)
    {
      onAdmissionDto.setChangeReasonDto(MedicationsEhrUtils.getTherapyChangeReasonDtoFromAction(latestAction));
    }
  }

  @Override
  public MedicationOnAdmissionStatus getMedicationOnAdmissionStatusFromMedicationAction(final MedicationActionAction action)
  {
    final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
    if (actionEnum == null)
    {
      return null;
    }
    if (actionEnum == MedicationActionEnum.SUSPEND)
    {
      return MedicationOnAdmissionStatus.SUSPENDED;
    }
    else if (actionEnum == MedicationActionEnum.ABORT)
    {
      return MedicationOnAdmissionStatus.ABORTED;
    }
    else if (actionEnum == MedicationActionEnum.SCHEDULE)
    {
      return MedicationOnAdmissionStatus.PRESCRIBED;
    }
    else
    {
      return actionEnum == MedicationActionEnum.MODIFY_EXISTING
             ? MedicationOnAdmissionStatus.EDITED_AND_PRESCRIBED
             : MedicationOnAdmissionStatus.PENDING;
    }
  }

  @Override
  public List<String> getMedicationsOnAdmissionCompositionIds(
      final String patientId,
      final DateTime fromDate,
      final Locale locale)
  {
    final List<String> compositionIds = new ArrayList<>();
    final List<MedicationOnAdmissionComposition> compositions =
        medicationsOpenEhrDao.findMedicationOnAdmissionCompositions(patientId, fromDate);

    for (final MedicationOnAdmissionComposition composition : compositions)
    {
      compositionIds.add(TherapyIdUtils.getCompositionUidWithoutVersion(composition.getUid().getValue()));
    }
    return compositionIds;
  }

  @Override
  public void updateMedicationOnAdmissionAction(
      final String patientId,
      final String compositionId,
      final MedicationOrderActionEnum actionEnum,
      final TherapyChangeReasonDto changeReasonDto,
      final DateTime when,
      final Locale locale)
  {
    final MedicationOnAdmissionComposition admissionComposition = medicationsOpenEhrDao.loadMedicationOnAdmissionComposition(
        patientId,
        compositionId);

    final Set<TagDto> oldTags = taggingOpenEhrDao.getTags(compositionId);

    addActionToMedicationOnAdmissionComposition(actionEnum, admissionComposition, changeReasonDto, when);
    updateMedicationOnAdmissionContext(admissionComposition);

    final String updatedCompositionId = medicationsOpenEhrDao.saveComposition(
        patientId,
        admissionComposition,
        admissionComposition.getUid().getValue());

    // copy all old tags
    taggingOpenEhrDao.tag(updatedCompositionId, oldTags.toArray(new TagDto[oldTags.size()]));
  }

  @Override
  public List<MedicationOnAdmissionGroupDto> getTherapiesOnAdmissionGroups(
      final String patientId,
      final DateTime currentHospitalizationStart,
      final DateTime actionTime,
      final Locale locale)
  {
    final Interval lastHospitalizationInterval = medicationsConnector.getLastDischargedCentralCaseEffectiveInterval(patientId);
    final DateTime lastHospitalizationStart = lastHospitalizationInterval != null
                                              ? lastHospitalizationInterval.getStart()
                                              : new DateTime(Long.MIN_VALUE);

    final List<MedicationOnAdmissionGroupDto> therapyOnAdmissionGroups = new ArrayList<>();
    final List<MedicationOnDischargeDto> previousDischargeMedications = medicationOnDischargeHandler.getMedicationsOnDischarge(
        patientId,
        new Interval(lastHospitalizationStart, currentHospitalizationStart),
        actionTime,
        locale);

    final MedicationOnAdmissionGroupDto previousDischargeMedicationsGroup = new MedicationOnAdmissionGroupDto();

    previousDischargeMedicationsGroup.setGroupEnum(TherapySourceGroupEnum.LAST_DISCHARGE_MEDICATIONS);
    previousDischargeMedicationsGroup.setGroupName(
        Dictionary.getEntry(EnumUtils.getIdentifier(TherapySourceGroupEnum.LAST_DISCHARGE_MEDICATIONS), locale));

    final List<SourceMedicationDto> lastHospitalizationGroupElements = new ArrayList<>();
    for (final MedicationOnDischargeDto dischargeDto : previousDischargeMedications)
    {
      final AdmissionSourceMedicationDto sourceMedicationDto = new AdmissionSourceMedicationDto();

      final TherapyDto therapy = dischargeDto.getTherapy();
      final String compositionId = therapy.getCompositionUid();

      // some therapy data should not be copied
      therapy.setCompositionUid(null);
      therapy.setPrescriptionSupply(null);
      therapy.setPrescriptionLocalDetails(null);

      sourceMedicationDto.setSourceId(compositionId);
      sourceMedicationDto.setTherapy(therapy);

      lastHospitalizationGroupElements.add(sourceMedicationDto);
    }

    previousDischargeMedicationsGroup.setGroupElements(lastHospitalizationGroupElements);
    therapyOnAdmissionGroups.add(previousDischargeMedicationsGroup);

    return therapyOnAdmissionGroups;
  }

  private void updateMedicationOnAdmissionContext(final MedicationOnAdmissionComposition admissionComposition)
  {
    final DateTime when = DataValueUtils.getDateTime(admissionComposition.getCompositionEventContext().getStartTime());
    MedicationsEhrUtils.visitComposition(admissionComposition, admissionComposition.getComposer(), when);
  }

  private void addActionToMedicationOnAdmissionComposition(
      final MedicationOrderActionEnum actionEnum,
      final MedicationOnAdmissionComposition admissionComposition,
      final TherapyChangeReasonDto changeReasonDto,
      final DateTime when)
  {
    if (actionEnum == MedicationOrderActionEnum.ABORT)
    {
      addAction(admissionComposition, MedicationActionEnum.ABORT, changeReasonDto, when);
    }
    else if (actionEnum == MedicationOrderActionEnum.SUSPEND || actionEnum == MedicationOrderActionEnum.SUSPEND_ADMISSION)
    {
      addAction(admissionComposition, MedicationActionEnum.SUSPEND, changeReasonDto, when);
    }
    else if (actionEnum == MedicationOrderActionEnum.PRESCRIBE)
    {
      addAction(admissionComposition, MedicationActionEnum.SCHEDULE, changeReasonDto, when);
    }
    else if (actionEnum == MedicationOrderActionEnum.EDIT)
    {
      addAction(admissionComposition, MedicationActionEnum.MODIFY_EXISTING, changeReasonDto, when);
    }
  }

  private void addAction(
      final MedicationOnAdmissionComposition admissionComposition,
      final MedicationActionEnum actionEnum,
      final TherapyChangeReasonDto changeReasonDto,
      final DateTime when)
  {
    final MedicationActionAction completeAction = MedicationsEhrUtils.buildMedicationAction(
        admissionComposition,
        actionEnum,
        when);

    MedicationsEhrUtils.setTherapyChangeReasonToAction(changeReasonDto, completeAction);
    admissionComposition.getMedicationDetail().getMedicationAction().add(completeAction);
  }
}
