package com.marand.thinkmed.medications.discharge.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;

import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.openehr.dao.openehr.TaggingOpenEhrDao;
import com.marand.maf.core.openehr.visitor.IspekTdoDataSupport;
import com.marand.maf.core.openehr.visitor.TdoPopulatingVisitor;
import com.marand.maf.core.service.RequestContextHolder;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOnDischargeComposition;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.rm.RmPath;
import com.marand.openehr.rm.TdoPathable;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkehr.tagging.dto.TagDto;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.ParticipationTypeEnum;
import com.marand.thinkmed.medications.TherapySourceGroupEnum;
import com.marand.thinkmed.medications.TherapyTagEnum;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.business.util.TherapyIdUtils;
import com.marand.thinkmed.medications.converter.therapy.MedicationConverterSelector;
import com.marand.thinkmed.medications.converter.therapy.MedicationFromEhrConverter;
import com.marand.thinkmed.medications.converter.therapy.MedicationToEhrConverter;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.discharge.MedicationOnDischargeHandler;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeReconciliationDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeStatus;
import com.marand.thinkmed.medications.therapy.TherapyUpdater;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.openehr.jaxb.rm.DvEhrUri;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.Link;
import org.openehr.jaxb.rm.PartyIdentified;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Nejc Korasa
 */
public class MedicationOnDischargeHandlerImpl implements MedicationOnDischargeHandler
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private TherapyDisplayProvider therapyDisplayProvider;
  private MedicationFromEhrConverter.MedicationDataProvider medicationDataProvider;
  private TherapyUpdater therapyUpdater;
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
  public void setMedicationDataProvider(final MedicationFromEhrConverter.MedicationDataProvider medicationDataProvider)
  {
    this.medicationDataProvider = medicationDataProvider;
  }

  @Required
  public void setTherapyUpdater(final TherapyUpdater therapyUpdater)
  {
    this.therapyUpdater = therapyUpdater;
  }

  @Required
  public void setTaggingOpenEhrDao(final TaggingOpenEhrDao taggingOpenEhrDao)
  {
    this.taggingOpenEhrDao = taggingOpenEhrDao;
  }

  @Override
  public List<String> saveMedicationsOnDischarge(
      final String patientId,
      final List<MedicationOnDischargeDto> therapiesList,
      final List<String> compositionIdsToDelete,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final String userId,
      final NamedExternalDto prescriber,
      final DateTime when,
      final Locale locale)
  {
    final List<String> savedCompositionIds = new ArrayList<>();
    for (final MedicationOnDischargeDto dischargeDto : therapiesList)
    {
      final TherapyDto therapy = dischargeDto.getTherapy();
      final String sourceId = dischargeDto.getSourceId();

      if (therapy.getCompositionUid() == null)
      {
        final MedicationOnDischargeComposition composition =
            buildMedicationOnDischargeComposition(
                dischargeDto,
                centralCaseId,
                careProviderId,
                userId,
                prescriber,
                when,
                locale);

        createMedicationOnDischargeLinks(
            composition.getMedicationDetail().getMedicationInstruction().get(0),
            dischargeDto,
            patientId);

        final String savedCompositionId = medicationsOpenEhrDao.saveComposition(
            patientId,
            composition,
            therapy.getCompositionUid());

        savedCompositionIds.add(savedCompositionId);
        if (sourceId != null)
        {
          taggingOpenEhrDao.tag(sourceId, new TagDto(TherapyTagEnum.REVIEWED_ON_DISCHARGE.getPrefix()));
        }
      }
      else
      {
        final String compositionId = modifyDischargeComposition(patientId, locale, therapy);
        savedCompositionIds.add(compositionId);
      }
    }

    if (!compositionIdsToDelete.isEmpty())
    {
      deleteMedicationsOnDischarge(patientId, compositionIdsToDelete);
    }
    return savedCompositionIds;
  }

  private void createMedicationOnDischargeLinks(
      final MedicationInstructionInstruction instruction,
      final MedicationOnDischargeDto dischargeDto,
      final String patientId)
  {
    final TherapySourceGroupEnum sourceGroupEnum = dischargeDto.getSourceGroupEnum();
    final String sourceId = dischargeDto.getSourceId();

    if (sourceGroupEnum == TherapySourceGroupEnum.INPATIENT_THERAPIES)
    {
      final MedicationOrderComposition orderComposition = medicationsOpenEhrDao.loadMedicationOrderComposition(
          patientId,
          sourceId);

      if (orderComposition != null)
      {
        createInpatientSourceLink(instruction, orderComposition);

        final List<Link> admissionLinks = MedicationsEhrUtils.getLinksOfType(
            orderComposition.getMedicationDetail().getMedicationInstruction().get(0),
            EhrLinkType.MEDICATION_ON_ADMISSION);

        instruction.getLinks().addAll(admissionLinks);

        //TODO Nejc check
        if (dischargeDto.getTherapy().isLinkedToAdmission() && admissionLinks.isEmpty())
        {
          therapyUpdater.addMedicationOnAdmissionLinkToInstruction(patientId, sourceId, instruction);
        }
      }
    }
    else if (sourceGroupEnum == TherapySourceGroupEnum.MEDICATION_ON_ADMISSION)
    {
      therapyUpdater.addMedicationOnAdmissionLinkToInstruction(patientId, sourceId, instruction);
    }
  }

  private void createInpatientSourceLink(
      final MedicationInstructionInstruction instructionToLink,
      final MedicationOrderComposition sourceComposition)
  {
    if (sourceComposition != null)
    {
      final MedicationInstructionInstruction linkedInstruction =
          sourceComposition.getMedicationDetail().getMedicationInstruction().get(0);

      final Link link = new Link();
      final DvText linkTypeValue = DataValueUtils.getText(EhrLinkType.SOURCE.getName());
      final RmPath rmPath = TdoPathable.pathOfItem(sourceComposition, linkedInstruction);
      final DvEhrUri linkEhrUri = DataValueUtils.getEhrUri(sourceComposition.getUid().getValue(), rmPath);

      link.setType(linkTypeValue);
      link.setMeaning(linkTypeValue);
      link.setTarget(linkEhrUri);

      instructionToLink.getLinks().add(link);
    }
  }

  private MedicationOnDischargeComposition buildMedicationOnDischargeComposition(
      final MedicationOnDischargeDto therapy,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final String userId,
      final NamedExternalDto prescriber,
      final DateTime when,
      final Locale locale)
  {
    final MedicationOnDischargeComposition composition = new MedicationOnDischargeComposition();

    final MedicationInstructionInstruction instruction = buildInstructionFromTherapyDto(locale, therapy.getTherapy());

    composition.setMedicationDetail(new MedicationOnDischargeComposition.MedicationDetailSection());
    composition.getMedicationDetail().getMedicationInstruction().add(instruction);

    final MedicationOnDischargeStatus dischargeStatus = therapy.getStatus();
    if (dischargeStatus == MedicationOnDischargeStatus.EDITED_AND_PRESCRIBED || dischargeStatus == MedicationOnDischargeStatus.NOT_PRESCRIBED)
    {
      final MedicationActionAction completeAction = MedicationsEhrUtils.buildMedicationAction(
          composition,
          dischargeStatus == MedicationOnDischargeStatus.EDITED_AND_PRESCRIBED
          ? MedicationActionEnum.MODIFY_EXISTING
          : MedicationActionEnum.CANCEL,
          when);

      MedicationsEhrUtils.setTherapyChangeReasonToAction(therapy.getChangeReasonDto(), completeAction);
      composition.getMedicationDetail().getMedicationAction().add(completeAction);
    }

    addMedicationOnDischargeContext(composition, prescriber, centralCaseId, careProviderId, when);

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

  private void addMedicationOnDischargeContext(
      final MedicationOnDischargeComposition medicationOrder,
      final NamedExternalDto prescriber,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final DateTime when)
  {
    MedicationsEhrUtils.addContext(medicationOrder, centralCaseId, careProviderId, when);

    final PartyIdentified compositionComposer = RequestContextHolder.getContext().getUserMetadata()
        .map(meta -> IspekTdoDataSupport.getPartyIdentified(meta.getFullName(), meta.getId()))
        .get();

    final TdoPopulatingVisitor.DataContext dataContext =
        TdoPopulatingVisitor.getSloveneContext(when)
            .withCompositionDynamic(true)
            .withReplaceParticipation(true)
            .withCompositionComposer(compositionComposer);

    MedicationsEhrUtils.setContextParticipation(dataContext, prescriber, ParticipationTypeEnum.PRESCRIBER);
    new TdoPopulatingVisitor().visitBean(medicationOrder, dataContext);
  }

  private String modifyDischargeComposition(final String patientId, final Locale locale, final TherapyDto therapy)
  {
    final MedicationOnDischargeComposition composition =
        medicationsOpenEhrDao.loadMedicationOnDischargeComposition(
            patientId,
            therapy.getCompositionUid());

    final MedicationInstructionInstruction instruction = buildInstructionFromTherapyDto(
        locale,
        therapy);

    final List<Link> existingLinks = composition.getMedicationDetail().getMedicationInstruction().get(0).getLinks();
    instruction.getLinks().addAll(existingLinks);

    final List<MedicationInstructionInstruction> instructionList = new ArrayList<>();
    instructionList.add(instruction);
    composition.getMedicationDetail().setMedicationInstruction(instructionList);

    updateMedicationOnDischargeComposition(composition);

    return medicationsOpenEhrDao.saveComposition(
        patientId,
        composition,
        therapy.getCompositionUid());
  }

  @Override
  public List<MedicationOnDischargeDto> getMedicationsOnDischarge(
      final String patientId,
      final Interval searchInterval,
      final DateTime when,
      final Locale locale)
  {
    final List<MedicationOnDischargeDto> onDischargeDtoList = new ArrayList<>();
    final List<MedicationOnDischargeComposition> compositions = medicationsOpenEhrDao.findMedicationOnDischargeCompositions(
        patientId,
        searchInterval);

    for (final MedicationOnDischargeComposition composition : compositions)
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

      final MedicationOnDischargeDto dischargeDto = new MedicationOnDischargeDto();
      dischargeDto.setTherapy(therapyDto);

      setSourceDataAndStatusFromInstruction(composition, instruction, dischargeDto);

      dischargeDto.setChangeReasonDto(getChangeReasonFromDischargeComposition(composition));

      onDischargeDtoList.add(dischargeDto);
    }
    return onDischargeDtoList;
  }

  private void setSourceDataAndStatusFromInstruction(
      final MedicationOnDischargeComposition composition,
      final MedicationInstructionInstruction instruction,
      final MedicationOnDischargeDto dischargeDto)
  {
    final List<Link> sourceLinks = MedicationsEhrUtils.getLinksOfType(
        instruction,
        EhrLinkType.SOURCE);

    if (sourceLinks.isEmpty())
    {
      final List<Link> onAdmissionLinks = MedicationsEhrUtils.getLinksOfType(
          instruction,
          EhrLinkType.MEDICATION_ON_ADMISSION);

      if (!onAdmissionLinks.isEmpty())
      {
        dischargeDto.setSourceGroupEnum(TherapySourceGroupEnum.MEDICATION_ON_ADMISSION);
        dischargeDto.setSourceId(MedicationsEhrUtils.getTargetCompositionIdFromLink(onAdmissionLinks.get(0)));
      }
    }
    else
    {
      dischargeDto.setSourceGroupEnum(TherapySourceGroupEnum.INPATIENT_THERAPIES);
      dischargeDto.setSourceId(MedicationsEhrUtils.getTargetCompositionIdFromLink(sourceLinks.get(0)));
    }

    dischargeDto.setStatus(
        getMedicationOnDischargeStatusFromAction(
            MedicationsEhrUtils.getLatestAction(composition.getMedicationDetail().getMedicationAction())));
  }

  private MedicationOnDischargeStatus getMedicationOnDischargeStatusFromAction(final MedicationActionAction latestAction)
  {
    if (latestAction != null)
    {
      MedicationOnDischargeStatus dischargeStatus = MedicationOnDischargeStatus.PRESCRIBED;
      final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(latestAction);
      if (actionEnum == MedicationActionEnum.MODIFY_EXISTING)
      {
        dischargeStatus = MedicationOnDischargeStatus.EDITED_AND_PRESCRIBED;
      }
      else if (actionEnum == MedicationActionEnum.CANCEL)
      {
        dischargeStatus = MedicationOnDischargeStatus.NOT_PRESCRIBED;
      }
      return dischargeStatus;
    }
    else
    {
      return MedicationOnDischargeStatus.PRESCRIBED;
    }
  }

  private void setAdmissionCompositionIdFromInstructionLinks(
      final MedicationInstructionInstruction instruction,
      final MedicationOnDischargeReconciliationDto dischargeDto)
  {
    final List<Link> onAdmissionLinks = MedicationsEhrUtils.getLinksOfType(
        instruction,
        EhrLinkType.MEDICATION_ON_ADMISSION);

    if (!onAdmissionLinks.isEmpty())
    {
      dischargeDto.setLinkedAdmissionCompositionId(MedicationsEhrUtils.getTargetCompositionIdFromLink(onAdmissionLinks.get(0)));
    }
  }

  private void setSourceOrderCompositionIdFromInstructionLinks(
      final MedicationInstructionInstruction instruction,
      final MedicationOnDischargeReconciliationDto dischargeDto)
  {
    final List<Link> sourceLinks = MedicationsEhrUtils.getLinksOfType(
        instruction,
        EhrLinkType.SOURCE);

    if (!sourceLinks.isEmpty())
    {
      dischargeDto.setLinkedOrderCompositionId(MedicationsEhrUtils.getTargetCompositionIdFromLink(sourceLinks.get(0)));
    }
  }

  @Override
  public List<MedicationOnDischargeReconciliationDto> getMedicationsOnDischargeForReconciliation(
      final String patientId,
      final DateTime hospitalizationStart,
      final DateTime when,
      final Locale locale)
  {
    final List<MedicationOnDischargeReconciliationDto> onDischargeDtoList = new ArrayList<>();
    final List<MedicationOnDischargeComposition> compositions = medicationsOpenEhrDao.findMedicationOnDischargeCompositions(
        patientId,
        Intervals.infiniteFrom(hospitalizationStart));

    for (final MedicationOnDischargeComposition composition : compositions)
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

      final MedicationOnDischargeReconciliationDto dischargeReconciliationDto = new MedicationOnDischargeReconciliationDto();
      dischargeReconciliationDto.setTherapy(therapyDto);

      setAdmissionCompositionIdFromInstructionLinks(instruction, dischargeReconciliationDto);
      setSourceOrderCompositionIdFromInstructionLinks(instruction, dischargeReconciliationDto);

      dischargeReconciliationDto.setChangeReasonDto(getChangeReasonFromDischargeComposition(composition));
      dischargeReconciliationDto.setStatus(
          getMedicationOnDischargeStatusFromAction(
              MedicationsEhrUtils.getLatestAction(composition.getMedicationDetail().getMedicationAction())));

      onDischargeDtoList.add(dischargeReconciliationDto);
    }
    return onDischargeDtoList;
  }

  private TherapyChangeReasonDto getChangeReasonFromDischargeComposition(final MedicationOnDischargeComposition composition)
  {
    final MedicationActionAction latestAction =
        MedicationsEhrUtils.getLatestAction(composition.getMedicationDetail().getMedicationAction());

    return latestAction != null ? MedicationsEhrUtils.getTherapyChangeReasonDtoFromAction(latestAction) : null;
  }

  private void updateMedicationOnDischargeComposition(final MedicationOnDischargeComposition dischargeComposition)
  {
    final DateTime when = DataValueUtils.getDateTime(dischargeComposition.getCompositionEventContext().getStartTime());
    MedicationsEhrUtils.visitComposition(dischargeComposition, dischargeComposition.getComposer(), when);
  }

  @Override
  public List<String> getMedicationsOnDischargeIds(
      final String patientId, final DateTime fromDate)
  {
    final List<String> compositionIds = new ArrayList<>();
    final List<MedicationOnDischargeComposition> compositions = medicationsOpenEhrDao.findMedicationOnDischargeCompositions(
        patientId,
        Intervals.infiniteFrom(fromDate));

    for (final MedicationOnDischargeComposition composition : compositions)
    {
      compositionIds.add(TherapyIdUtils.getCompositionUidWithoutVersion(composition.getUid().getValue()));
    }
    return compositionIds;
  }

  @Override
  public void deleteMedicationsOnDischarge(
      final String patientId,
      final List<String> compositionIds)
  {
    for (final String compositionId : compositionIds)
    {
      removeReviewedOnDischargeTagFromComposition(patientId, compositionId);
      medicationsOpenEhrDao.deleteComposition(patientId, compositionId);
    }
  }

  private void removeReviewedOnDischargeTagFromComposition(final String patientId, final String compositionId)
  {
    final MedicationOnDischargeComposition dischargeComposition =
        medicationsOpenEhrDao.loadMedicationOnDischargeComposition(patientId, compositionId);

    final MedicationInstructionInstruction instruction =
        dischargeComposition.getMedicationDetail().getMedicationInstruction().get(0);

    final List<Link> sourceLinks = MedicationsEhrUtils.getLinksOfType(instruction, EhrLinkType.SOURCE);

    if (sourceLinks.isEmpty())
    {
      final List<Link> admissionLinks =
          MedicationsEhrUtils.getLinksOfType(instruction, EhrLinkType.MEDICATION_ON_ADMISSION);

      if (!admissionLinks.isEmpty())
      {
        taggingOpenEhrDao.deleteTags(
            MedicationsEhrUtils.getTargetCompositionIdFromLink(admissionLinks.get(0)),
            new TagDto(TherapyTagEnum.REVIEWED_ON_DISCHARGE.getPrefix()));
      }
    }
    else
    {
      taggingOpenEhrDao.deleteTags(
          MedicationsEhrUtils.getTargetCompositionIdFromLink(sourceLinks.get(0)),
          new TagDto(TherapyTagEnum.REVIEWED_ON_DISCHARGE.getPrefix()));
    }
  }
}