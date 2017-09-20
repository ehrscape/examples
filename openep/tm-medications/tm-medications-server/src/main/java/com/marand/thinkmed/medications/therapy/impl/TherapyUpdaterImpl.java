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

package com.marand.thinkmed.medications.therapy.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.maf.core.PartialList;
import com.marand.maf.core.eventbus.EventProducer;
import com.marand.maf.core.exception.UserWarning;
import com.marand.maf.core.openehr.visitor.IspekTdoDataSupport;
import com.marand.maf.core.openehr.visitor.TdoPopulatingVisitor;
import com.marand.maf.core.service.RequestContextHolder;
import com.marand.maf.core.time.DateTimeFormatters;
import com.marand.maf.core.time.Intervals;
import com.marand.maf.core.valueholder.ValueHolder;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOnAdmissionComposition;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition;
import com.marand.openehr.rm.RmPath;
import com.marand.openehr.rm.TdoPathable;
import com.marand.openehr.util.DataValueUtils;
import com.marand.openehr.util.OpenEhrRefUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.ActionReasonType;
import com.marand.thinkmed.medications.AdministrationResultEnum;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.MedicationOrderActionEnum;
import com.marand.thinkmed.medications.MedicationRouteTypeEnum;
import com.marand.thinkmed.medications.ParticipationTypeEnum;
import com.marand.thinkmed.medications.PharmacistReviewTaskStatusEnum;
import com.marand.thinkmed.medications.PrescriptionChangeTypeEnum;
import com.marand.thinkmed.medications.SelfAdministeringActionEnum;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.administration.AdministrationHandler;
import com.marand.thinkmed.medications.administration.AdministrationProvider;
import com.marand.thinkmed.medications.administration.AdministrationTaskCreator;
import com.marand.thinkmed.medications.admission.MedicationOnAdmissionHandler;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.business.util.TherapyIdUtils;
import com.marand.thinkmed.medications.change.TherapyChangeCalculator;
import com.marand.thinkmed.medications.converter.therapy.MedicationConverterSelector;
import com.marand.thinkmed.medications.converter.therapy.MedicationToEhrConverter;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.CodedNameDto;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.MedicationHolderDto;
import com.marand.thinkmed.medications.dto.OxygenTherapyDto;
import com.marand.thinkmed.medications.dto.SaveMedicationOrderDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StopAdministrationDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeType;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskHandler;
import com.marand.thinkmed.medications.pharmacist.PreparePerfusionSyringeProcessHandler;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.AbortTherapy;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.ReissueTherapy;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.SuspendTherapy;
import com.marand.thinkmed.medications.task.AdministrationTaskCreateActionEnum;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.DoctorReviewTaskDef;
import com.marand.thinkmed.medications.task.MedicationsTasksHandler;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.task.MedsTaskDef;
import com.marand.thinkmed.medications.task.SwitchToOralTaskDef;
import com.marand.thinkmed.medications.task.TherapyTaskDef;
import com.marand.thinkmed.medications.task.TherapyTaskUtils;
import com.marand.thinkmed.medications.therapy.TherapyUpdater;
import com.marand.thinkmed.process.dto.AbstractTaskDto;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import com.marand.thinkmed.process.dto.TaskDetailsEnum;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.openehr.jaxb.rm.DvCount;
import org.openehr.jaxb.rm.DvEhrUri;
import org.openehr.jaxb.rm.DvParsable;
import org.openehr.jaxb.rm.Link;
import org.openehr.jaxb.rm.PartyIdentified;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Mitja Lapajne
 */
public class TherapyUpdaterImpl implements TherapyUpdater
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private MedicationsBo medicationsBo;
  private ValueHolder<Map<Long, MedicationHolderDto>> medicationsValueHolder;
  private TherapyDisplayProvider therapyDisplayProvider;
  private MedicationOnAdmissionHandler medicationOnAdmissionHandler;
  private AdministrationHandler administrationHandler;
  private MedicationsDao medicationsDao;
  private PharmacistTaskHandler pharmacistTaskHandler;
  private PreparePerfusionSyringeProcessHandler preparePerfusionSyringeProcessHandler;
  private MedicationsTasksHandler medicationsTasksHandler;
  private MedicationsTasksProvider medicationsTasksProvider;
  private AdministrationTaskCreator administrationTaskCreator;
  private ProcessService processService;
  private AdministrationProvider administrationProvider;
  private TherapyChangeCalculator therapyChangeCalculator;

  @Required
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Required
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Required
  public void setMedicationsValueHolder(final ValueHolder<Map<Long, MedicationHolderDto>> medicationsValueHolder)
  {
    this.medicationsValueHolder = medicationsValueHolder;
  }

  @Required
  public void setTherapyDisplayProvider(final TherapyDisplayProvider therapyDisplayProvider)
  {
    this.therapyDisplayProvider = therapyDisplayProvider;
  }

  @Required
  public void setProcessService(final ProcessService processService)
  {
    this.processService = processService;
  }

  @Required
  public void setMedicationOnAdmissionHandler(final MedicationOnAdmissionHandler medicationOnAdmissionHandler)
  {
    this.medicationOnAdmissionHandler = medicationOnAdmissionHandler;
  }

  @Required
  public void setAdministrationHandler(final AdministrationHandler administrationHandler)
  {
    this.administrationHandler = administrationHandler;
  }

  @Required
  public void setMedicationsDao(final MedicationsDao medicationsDao)
  {
    this.medicationsDao = medicationsDao;
  }

  public void setAdministrationTaskCreator(final AdministrationTaskCreator administrationTaskCreator)
  {
    this.administrationTaskCreator = administrationTaskCreator;
  }

  @Required
  public void setPharmacistTaskHandler(final PharmacistTaskHandler pharmacistTaskHandler)
  {
    this.pharmacistTaskHandler = pharmacistTaskHandler;
  }

  @Required
  public void setMedicationsTasksHandler(final MedicationsTasksHandler medicationsTasksHandler)
  {
    this.medicationsTasksHandler = medicationsTasksHandler;
  }

  @Required
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Required
  public void setPreparePerfusionSyringeProcessHandler(final PreparePerfusionSyringeProcessHandler preparePerfusionSyringeProcessHandler)
  {
    this.preparePerfusionSyringeProcessHandler = preparePerfusionSyringeProcessHandler;
  }

  @Required
  public void setAdministrationProvider(final AdministrationProvider administrationProvider)
  {
    this.administrationProvider = administrationProvider;
  }

  @Required
  public void setTherapyChangeCalculator(final TherapyChangeCalculator therapyChangeCalculator)
  {
    this.therapyChangeCalculator = therapyChangeCalculator;
  }

  @Override
  public List<MedicationOrderComposition> saveTherapies(
      final String patientId,
      final List<SaveMedicationOrderDto> medicationOrders,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final NamedExternalDto prescriber,
      final DateTime when,
      final Locale locale)
  {
    final List<MedicationOrderComposition> savedTherapies = new ArrayList<>();
    final Map<String, MedicationOrderComposition> savedLinkedTherapies = new HashMap<>();

    final List<SaveMedicationOrderDto> sortedMedicationOrders = sortMedicationOrdersByLinkName(medicationOrders);
    for (final SaveMedicationOrderDto medicationOrder : sortedMedicationOrders)
    {
      final TherapyDto therapy = medicationOrder.getTherapy();
      therapy.setCompositionUid(null);

      final MedicationOrderActionEnum actionEnum = medicationOrder.getActionEnum();
      final String sourceId = medicationOrder.getSourceId();
      final TherapyChangeReasonDto changeReasonDto = medicationOrder.getChangeReasonDto();

      if (sourceId != null)
      {
        medicationOnAdmissionHandler.updateMedicationOnAdmissionAction(
            patientId,
            sourceId,
            actionEnum,
            changeReasonDto,
            when,
            locale);
      }

      if (actionEnum != MedicationOrderActionEnum.SUSPEND_ADMISSION)
      {
        final MedicationOrderComposition composition =
            buildMedicationOrderComposition(
                therapy,
                actionEnum,
                changeReasonDto,
                centralCaseId,
                careProviderId,
                prescriber,
                when,
                locale);

        final MedicationInstructionInstruction instruction =
            composition.getMedicationDetail().getMedicationInstruction().get(0);

        final String linkName = therapy.getLinkName();
        if (linkName != null)
        {
          createTherapyFollowLink(
              patientId,
              instruction,
              linkName,
              medicationOrder.getLinkCompositionUid(),
              savedLinkedTherapies);
        }

        if (sourceId != null)
        {
          addMedicationOnAdmissionLinkToInstruction(patientId, sourceId, instruction);
        }

        final MedicationOrderComposition savedComposition =
            medicationsOpenEhrDao.saveNewMedicationOrderComposition(patientId, composition);

        final String therapyId =
            TherapyIdUtils.createTherapyId(
                savedComposition, savedComposition.getMedicationDetail().getMedicationInstruction().get(0));
        createReminders(therapy, therapyId, therapy.getReviewReminderDays(), patientId, locale);

        final boolean activeTherapy = actionEnum == MedicationOrderActionEnum.PRESCRIBE || actionEnum == MedicationOrderActionEnum.EDIT;
        if (activeTherapy)
        {
          createTherapyTasks(patientId, savedComposition, AdministrationTaskCreateActionEnum.PRESCRIBE, null, when);
        }

        if (linkName != null)
        {
          savedLinkedTherapies.put(linkName, savedComposition);
        }
        savedTherapies.add(savedComposition);
      }
    }
    return savedTherapies;
  }

  private void createReminders(
      final TherapyDto therapyDto,
      final String therapyId,
      final Integer reviewReminderDays,
      final String patientId,
      final Locale locale)
  {
    final Long mainMedicationId = therapyDto.getMainMedicationId();
    final MedicationHolderDto medicationHolderDto = medicationsValueHolder.getValue().get(mainMedicationId);

    final DateTime therapyStart = therapyDto.getStart();

    if (medicationHolderDto != null && medicationHolderDto.isSuggestSwitchToOral())
    {
      if (therapyDto.getRoutes().size() == 1 && therapyDto.getRoutes().get(0).getType() == MedicationRouteTypeEnum.IV)
      {
        final DateTime dueDate = therapyStart.withTimeAtStartOfDay()
            .plusDays(2);       //TODO TMC-7170 antibiotic - from preference (care provider)
        final NewTaskRequestDto taskRequest = new NewTaskRequestDto(
            SwitchToOralTaskDef.INSTANCE,
            SwitchToOralTaskDef.getTaskTypeEnum().buildKey(String.valueOf(patientId)),
            "Switch to oral medication " + dueDate.toString(DateTimeFormatters.shortDate(locale)),
            "Switch to oral medication " + dueDate.toString(DateTimeFormatters.shortDate(locale)),
            TherapyAssigneeEnum.DOCTOR.name(),
            dueDate,
            null,
            Pair.of(MedsTaskDef.PATIENT_ID, patientId),
            Pair.of(TherapyTaskDef.ORIGINAL_THERAPY_ID, therapyId)
        );

        processService.createTasks(taskRequest);
      }
    }
    if (reviewReminderDays != null)
    {
      final DateTime dueDate = therapyStart.withTimeAtStartOfDay().plusDays(reviewReminderDays);
      final NewTaskRequestDto taskRequest = new NewTaskRequestDto(
          DoctorReviewTaskDef.INSTANCE,
          DoctorReviewTaskDef.getTaskTypeEnum().buildKey(String.valueOf(patientId)),
          "Doctor review task " + dueDate.toString(DateTimeFormatters.shortDate(locale)),
          "Doctor review task " + dueDate.toString(DateTimeFormatters.shortDate(locale)),
          TherapyAssigneeEnum.DOCTOR.name(),
          dueDate,
          null,
          Pair.of(MedsTaskDef.PATIENT_ID, patientId),
          Pair.of(TherapyTaskDef.ORIGINAL_THERAPY_ID, therapyId)
      );
      processService.createTasks(taskRequest);
    }
  }

  private MedicationOrderComposition buildMedicationOrderComposition(
      final TherapyDto therapy,
      final MedicationOrderActionEnum actionEnum,
      final TherapyChangeReasonDto changeReasonDto,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final NamedExternalDto prescriber,
      final DateTime when,
      final Locale locale)
  {
    final MedicationOrderComposition composition = MedicationsEhrUtils.createEmptyMedicationOrderComposition();
    if (therapy.getTherapyDescription() == null)
    {
      therapyDisplayProvider.fillDisplayValues(therapy, true, true, locale);
    }
    final MedicationToEhrConverter<?> therapyConverter = MedicationConverterSelector.getConverter(therapy);
    final MedicationInstructionInstruction instruction = therapyConverter.createInstructionFromTherapy(therapy);

    MedicationsEhrUtils.addInstructionTo(composition, instruction);

    MedicationsEhrUtils.addMedicationActionTo(composition, MedicationActionEnum.SCHEDULE, prescriber, when);

    final boolean therapyStartsWhenAnotherTherapyEnds =
        therapy.getLinkName() != null && (therapy.getLinkName().length() > 2 || !therapy.getLinkName().endsWith("1"));
    if (!therapyStartsWhenAnotherTherapyEnds)
    {
      final DateTime therapyStart = therapy.getStart().isAfter(when) ? therapy.getStart() : when;
      MedicationsEhrUtils.addMedicationActionTo(composition, MedicationActionEnum.START, prescriber, therapyStart);
    }

    if (actionEnum == MedicationOrderActionEnum.SUSPEND)
    {
      final NamedExternalDto user = RequestContextHolder.getContext().getUserMetadata()
          .map(meta -> new NamedExternalDto(meta.getId(), meta.getFullName()))
          .get();

      MedicationsEhrUtils.addMedicationActionTo(
          composition,
          MedicationActionEnum.CANCEL,
          user,
          when);
    }
    else if (actionEnum == MedicationOrderActionEnum.ABORT)
    {
      MedicationsEhrUtils.addMedicationActionTo(composition, MedicationActionEnum.ABORT, prescriber, changeReasonDto, when);
    }

    addMedicationOrderCompositionEventContext(composition, prescriber, centralCaseId, careProviderId, when);
    return composition;
  }

  private void createTherapyFollowLink(
      final String patientId,
      final MedicationInstructionInstruction instruction,
      final String linkName,
      final String linkCompositionUid,
      final Map<String, MedicationOrderComposition> savedTherapiesWithLinks)
  {
    final String previousLink = MedicationsEhrUtils.getPreviousLinkName(linkName);

    final MedicationOrderComposition linkOrder =
        linkCompositionUid == null
        ? savedTherapiesWithLinks.get(previousLink)
        : medicationsOpenEhrDao.loadMedicationOrderComposition(patientId, linkCompositionUid);

    // link: linkInstruction <- instruction
    if (linkOrder != null)
    {
      final Link link = new Link();
      link.setMeaning(DataValueUtils.getText(linkName));
      link.setType(DataValueUtils.getText(EhrLinkType.FOLLOW.getName()));

      final MedicationInstructionInstruction linkInstruction = linkOrder.getMedicationDetail()
          .getMedicationInstruction()
          .get(0);

      final RmPath rmPath = TdoPathable.pathOfItem(linkOrder, linkInstruction);
      final DvEhrUri linkEhrUri = DataValueUtils.getEhrUri(linkOrder.getUid().getValue(), rmPath);

      link.setTarget(linkEhrUri);
      instruction.getLinks().add(link);
    }
  }

  @Override
  public void addMedicationOnAdmissionLinkToInstruction(
      final String patientId,
      final String onAdmissionCompositionId,
      final MedicationInstructionInstruction instruction)
  {
    final MedicationOnAdmissionComposition linkedTherapy =
        medicationsOpenEhrDao.loadMedicationOnAdmissionComposition(patientId, onAdmissionCompositionId);

    if (linkedTherapy != null)
    {
      final MedicationInstructionInstruction linkedInstruction =
          linkedTherapy.getMedicationDetail().getMedicationInstruction().get(0);

      final Link link = new Link();
      final RmPath rmPath = TdoPathable.pathOfItem(linkedTherapy, linkedInstruction);
      final DvEhrUri linkEhrUri = DataValueUtils.getEhrUri(linkedTherapy.getUid().getValue(), rmPath);

      link.setType(DataValueUtils.getText(EhrLinkType.MEDICATION_ON_ADMISSION.getName()));
      link.setMeaning(DataValueUtils.getText(EhrLinkType.MEDICATION_ON_ADMISSION.getName()));
      link.setTarget(linkEhrUri);
      link.setMeaning(link.getType());

      instruction.getLinks().add(link);
    }
  }

  private List<SaveMedicationOrderDto> sortMedicationOrdersByLinkName(final List<SaveMedicationOrderDto> medicationOrders)
  {
    final List<SaveMedicationOrderDto> sortedOrders = new ArrayList<>();
    sortedOrders.addAll(medicationOrders);
    Collections.sort(
        sortedOrders, (therapy1, therapy2) ->
        {
          if (therapy1.getTherapy().getLinkName() != null && therapy2.getTherapy().getLinkName() != null)
          {
            return therapy1.getTherapy().getLinkName().compareTo(therapy2.getTherapy().getLinkName());
          }
          if (therapy1.getTherapy().getLinkName() != null)
          {
            return 1;
          }
          if (therapy2.getTherapy().getLinkName() != null)
          {
            return -1;
          }
          return Integer.valueOf(medicationOrders.indexOf(therapy1)).compareTo(medicationOrders.indexOf(therapy2));
        });
    return sortedOrders;
  }

  @Override
  public void modifyTherapy(
      final String patientId,
      final TherapyDto modifiedTherapy,
      final TherapyChangeReasonDto changeReason,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final NamedExternalDto prescriber,
      final boolean therapyAlreadyStarted,
      final String basedOnPharmacyReviewId,
      final DateTime when,
      final Locale locale)
  {
    medicationsBo.fillDisplayValues(modifiedTherapy, null, null, false, locale);

    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> oldInstructionPair =
        medicationsOpenEhrDao.getTherapyInstructionPair(
            patientId,
            modifiedTherapy.getCompositionUid(),
            modifiedTherapy.getEhrOrderName());

    final TherapyDto oldTherapy = medicationsBo.convertInstructionToTherapyDtoWithDisplayValues(
        oldInstructionPair.getFirst(),
        oldInstructionPair.getSecond(),
        null,
        null,
        when,
        false,
        locale);

    final List<TherapyChangeDto<?, ?>> changes = therapyChangeCalculator.calculateTherapyChanges(
        oldTherapy,
        modifiedTherapy,
        true,
        locale);

    final boolean significantChange = changes
        .stream()
        .anyMatch(c -> c.getType().getLevel() == TherapyChangeType.TherapyChangeLevel.SIGNIFICANT);

    final boolean startChange = changes
        .stream()
        .anyMatch(c -> c.getType() == TherapyChangeType.START);

    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> newInstructionPair =
        modifyTherapy(
            patientId,
            modifiedTherapy,
            changeReason,
            centralCaseId,
            careProviderId,
            prescriber,
            therapyAlreadyStarted,
            significantChange,
            when,
            locale);

    final boolean basedOnLinksRemoved = MedicationsEhrUtils.removeLinksOfType(
        newInstructionPair.getSecond(),
        EhrLinkType.BASED_ON);

    if (basedOnPharmacyReviewId == null)
    {
      pharmacistTaskHandler.handleReviewTaskOnTherapiesChange(
          patientId,
          null,
          when,
          prescriber != null ? prescriber.getName() : null,
          when,
          PrescriptionChangeTypeEnum.ADDITION_TO_EXISTING_PRESCRIPTION,
          PharmacistReviewTaskStatusEnum.PENDING);

      if (basedOnLinksRemoved)
      {
        medicationsOpenEhrDao.modifyMedicationOrderComposition(patientId, newInstructionPair.getFirst());
      }
    }
    else
    {
      final PharmacyReviewReportComposition pharmacistsReviewComposition =
          medicationsOpenEhrDao.loadPharmacistsReviewComposition(patientId, basedOnPharmacyReviewId);

      final Link linkToPharmacyReview =
          OpenEhrRefUtils.getLinkToTdoTarget(
              "based on pharmacy review",
              EhrLinkType.BASED_ON.getName(),
              pharmacistsReviewComposition,
              pharmacistsReviewComposition);

      newInstructionPair.getSecond().getLinks().add(linkToPharmacyReview);
      medicationsOpenEhrDao.modifyMedicationOrderComposition(patientId, newInstructionPair.getFirst());
    }

    // generate tasks

    final AdministrationTaskCreateActionEnum action;
    if (medicationsBo.isTherapySuspended(oldInstructionPair.getFirst(), oldInstructionPair.getSecond()))
    {
      action = AdministrationTaskCreateActionEnum.REISSUE;
    }
    else if (when.isBefore(oldTherapy.getStart()))
    {
      action = AdministrationTaskCreateActionEnum.MODIFY_BEFORE_START;
    }
    else
    {
      action = AdministrationTaskCreateActionEnum.MODIFY;
    }

    final boolean startChangeOnNotStartedTherapy = !therapyAlreadyStarted && startChange;
    if (startChangeOnNotStartedTherapy || significantChange || action == AdministrationTaskCreateActionEnum.REISSUE)
    {
      final DateTime newTherapyStart = modifiedTherapy.getStart();
      final DateTime deleteTasksFrom = when.isBefore(newTherapyStart) ? when : newTherapyStart;
      deleteTherapyAdministrationTasks(
          patientId,
          TherapyIdUtils.createTherapyId(modifiedTherapy.getCompositionUid(), modifiedTherapy.getEhrOrderName()),
          oldInstructionPair,
          false,
          false,
          deleteTasksFrom,
          when);

      createTherapyTasks(patientId, newInstructionPair.getFirst(), action, null, when);
    }
  }

  private <M extends TherapyDto> Pair<MedicationOrderComposition, MedicationInstructionInstruction> modifyTherapy(
      final String patientId,
      final M therapy,
      final TherapyChangeReasonDto changeReason,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final NamedExternalDto prescriber,
      final boolean therapyAlreadyStarted,
      final boolean significantChange,
      final DateTime when,
      final Locale locale)
  {
    final DateTime updateTime = new DateTime(when.withSecondOfMinute(0).withMillisOfSecond(0));
    therapyDisplayProvider.fillDisplayValues(therapy, true, true, locale);
    final MedicationOrderComposition oldComposition =
        medicationsOpenEhrDao.loadMedicationOrderComposition(patientId, therapy.getCompositionUid());
    final MedicationInstructionInstruction oldInstruction =
        MedicationsEhrUtils.getMedicationInstructionByEhrName(oldComposition, therapy.getEhrOrderName());

    final DateTime oldTherapyStart = DataValueUtils.getDateTime(oldInstruction.getOrder().get(0).getMedicationTiming().getStartDate());

    //noinspection unchecked
    final MedicationToEhrConverter<M> converter = (MedicationToEhrConverter<M>)MedicationConverterSelector.getConverter(
        therapy);

    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> savedTherapy;

    if (therapyAlreadyStarted && significantChange)
    {
      //create new instruction
      final MedicationOrderComposition newComposition = MedicationsEhrUtils.createEmptyMedicationOrderComposition();
      final MedicationInstructionInstruction newInstruction = converter.createInstructionFromTherapy(therapy);
      newComposition.getMedicationDetail().getMedicationInstruction().add(newInstruction);

      //link new instruction to old instruction
      final Link linkToExisting =
          OpenEhrRefUtils.getLinkToTdoTarget("update", EhrLinkType.UPDATE.getName(), oldComposition, oldInstruction);
      newInstruction.getLinks().add(linkToExisting);

      //link new instruction to first instruction
      linkToOriginInstruction(oldComposition, oldInstruction, newInstruction);

      //new therapy must keep FOLLOW links from old therapy
      final List<Link> followLinks = MedicationsEhrUtils.getLinksOfType(oldInstruction, EhrLinkType.FOLLOW);
      newInstruction.getLinks().addAll(followLinks);

      //new therapy must keep links to medication on ADMISSION
      final List<Link> admissionLinks =
          MedicationsEhrUtils.getLinksOfType(oldInstruction, EhrLinkType.MEDICATION_ON_ADMISSION);
      newInstruction.getLinks().addAll(admissionLinks);

      rewriteSelfAdministeringType(newInstruction, oldInstruction);

      //add SCHEDULE and START actions
      final DateTime therapyStart = therapy.getStart().isAfter(when) ? therapy.getStart() : when;
      MedicationsEhrUtils.addMedicationActionTo(newComposition, MedicationActionEnum.SCHEDULE, prescriber, when);
      MedicationsEhrUtils.addMedicationActionTo(newComposition, MedicationActionEnum.START, prescriber, therapyStart);

      addMedicationOrderCompositionEventContext(
          newComposition,
          prescriber,
          centralCaseId,
          careProviderId,
          when);

      final String newCompositionUid =
          medicationsOpenEhrDao.saveNewMedicationOrderComposition(patientId, newComposition).getUid().getValue();
      newComposition.setUid(OpenEhrRefUtils.getObjectVersionId(newCompositionUid));
      newInstruction.setName(DataValueUtils.getText("Medication instruction"));

      //fix FOLLOW links that point to this therapy
      fixLinksToTherapy(
          patientId,
          oldComposition,
          oldInstruction,
          newComposition,
          newInstruction,
          EhrLinkType.FOLLOW);

      //old composition
      final DateTime oldCompositionStopDate = updateTime.isBefore(therapy.getStart()) ? updateTime : therapy.getStart();

      if (therapy.getStart().isBefore(oldTherapyStart))
      {
        throw new UserWarning("Cannot edit therapy in the past.");
      }

      for (final MedicationInstructionInstruction.OrderActivity orderActivity : oldInstruction.getOrder())
      {
        orderActivity.getMedicationTiming().setStopDate(DataValueUtils.getDateTime(oldCompositionStopDate));
      }

      //only when client time is not synced
      if (oldCompositionStopDate.isBefore(oldTherapyStart))
      {
        for (final MedicationInstructionInstruction.OrderActivity orderActivity : oldInstruction.getOrder())
        {
          orderActivity.getMedicationTiming().setStartDate(orderActivity.getMedicationTiming().getStopDate());
        }
      }
      MedicationsEhrUtils.addMedicationActionTo(
          oldComposition, MedicationActionEnum.COMPLETE, prescriber, changeReason, when);

      medicationsOpenEhrDao.modifyMedicationOrderComposition(patientId, oldComposition);
      savedTherapy = Pair.of(newComposition, newInstruction);
    }
    else
    {
      converter.fillInstructionFromTherapy(oldInstruction, therapy);

      if (therapyAlreadyStarted) // and insignificantChange
      {
        oldInstruction.getOrder()
            .forEach(o -> o.getMedicationTiming().setStartDate(DataValueUtils.getDateTime(oldTherapyStart)));
      }
      else // fix start
      {
        final MedicationActionAction startAction = medicationsBo.getInstructionAction(
            oldComposition,
            oldInstruction,
            MedicationActionEnum.START,
            null);
        startAction.setTime(DataValueUtils.getDateTime(updateTime));
      }

      final boolean therapySuspended = medicationsBo.isTherapySuspended(oldComposition, oldInstruction);
      if (therapySuspended)
      {
        MedicationsEhrUtils.addMedicationActionTo(oldComposition, MedicationActionEnum.REISSUE, prescriber, when);
      }

      MedicationsEhrUtils.addMedicationActionTo(
          oldComposition, MedicationActionEnum.MODIFY_EXISTING, prescriber, changeReason, when);

      MedicationsEhrUtils.visitEhrBean(oldInstruction, prescriber, when);
      medicationsOpenEhrDao.modifyMedicationOrderComposition(patientId, oldComposition);
      savedTherapy = Pair.of(oldComposition, oldInstruction);
    }

    handleTherapyTasksOnModify(
        patientId,
        therapy,
        savedTherapy.getSecond(),
        TherapyIdUtils.createTherapyId(savedTherapy.getFirst(), savedTherapy.getSecond()),
        when);

    return savedTherapy;
  }

  private void handleTherapyTasksOnModify(
      final String patientId,
      final TherapyDto therapy,
      final MedicationInstructionInstruction instruction,
      final String therapyId,
      final DateTime when)
  {
    final List<String> taskKeys = new ArrayList<>();
    taskKeys.add(DoctorReviewTaskDef.getTaskTypeEnum().buildKey(String.valueOf(patientId)));
    taskKeys.add(SwitchToOralTaskDef.getTaskTypeEnum().buildKey(String.valueOf(patientId)));

    //noinspection unchecked
    final PartialList<TaskDto> tasks = processService.findTasks(
        TherapyAssigneeEnum.DOCTOR.name(),
        null,
        null,
        false,
        null,
        null,
        taskKeys,
        EnumSet.of(TaskDetailsEnum.VARIABLES));

    final List<Link> originLinks = MedicationsEhrUtils.getLinksOfType(instruction, EhrLinkType.ORIGIN);

    final String originalTherapyId =
        originLinks.isEmpty() ? therapyId : TherapyIdUtils.getTherapyIdFromLink(originLinks.get(0));

    if (tasks != null)
    {
      for (final TaskDto task : tasks)
      {
        final String taskTherapyId = (String)task.getVariables().get(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName());
        if (originalTherapyId.equals(taskTherapyId))
        {
          if (task.getTaskExecutionStrategyId().equals(TaskTypeEnum.DOCTOR_REVIEW.getName()))
          {
            if (!when.withTimeAtStartOfDay().isBefore(task.getDueTime().withTimeAtStartOfDay()))
            {
              processService.completeTasks(task.getId());
            }
          }
          else
          {
            if (task.getTaskExecutionStrategyId().equals(TaskTypeEnum.SWITCH_TO_ORAL.getName()))
            {
              final boolean ivRouteExists = therapy.getRoutes()
                  .stream()
                  .filter(route -> route.getType() == MedicationRouteTypeEnum.IV)
                  .anyMatch(Objects::nonNull);

              if (!ivRouteExists)
              {
                processService.completeTasks(task.getId());
              }
            }
          }
        }
      }
    }
  }

  private void rewriteSelfAdministeringType(
      final MedicationInstructionInstruction newInstruction,
      final MedicationInstructionInstruction oldInstruction)
  {
    // TODO Nejc change where self admin is saved
    newInstruction.getOrder()
        .get(0)
        .setParsableDoseDescription(oldInstruction.getOrder().get(0).getParsableDoseDescription());
  }

  private void linkToOriginInstruction(
      final MedicationOrderComposition oldComposition,
      final MedicationInstructionInstruction oldInstruction,
      final MedicationInstructionInstruction newInstruction)
  {
    Link originLink = null;
    boolean oldInstructionIsFirstInstruction = true;
    for (final Link link : oldInstruction.getLinks())
    {
      if (link.getType().getValue().equals(EhrLinkType.ORIGIN.getName()))
      {
        originLink = link;
      }
      if (link.getType().getValue().equals(EhrLinkType.UPDATE.getName()))
      {
        oldInstructionIsFirstInstruction = false;
      }
    }
    if (oldInstructionIsFirstInstruction)
    {
      final Link linkToFirst =
          OpenEhrRefUtils.getLinkToTdoTarget("origin", EhrLinkType.ORIGIN.getName(), oldComposition, oldInstruction);
      newInstruction.getLinks().add(linkToFirst);
    }
    else if (originLink != null)
    {
      newInstruction.getLinks().add(originLink);
    }
  }

  private void fixLinksToTherapy(
      final String patientId,
      final MedicationOrderComposition oldComposition,
      final MedicationInstructionInstruction oldInstruction,
      final MedicationOrderComposition newComposition,
      final MedicationInstructionInstruction newInstruction,
      final EhrLinkType linkType)
  {
    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> linkedTherapies =
        medicationsOpenEhrDao.getLinkedTherapies(
            patientId,
            oldComposition.getUid().getValue(),
            oldInstruction.getName().getValue(),
            linkType);

    if (!linkedTherapies.isEmpty())
    {
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> linkedTherapy = linkedTherapies.get(0);
      Link linkToFix = null;
      for (final Link link : linkedTherapy.getSecond().getLinks())
      {
        if (link.getType().getValue().equals(linkType.getName()))
        {
          linkToFix = link;
          break;
        }
      }
      linkedTherapy.getSecond().getLinks().remove(linkToFix);

      if (linkToFix != null)
      {
        final Link link = new Link();
        link.setMeaning(DataValueUtils.getText(linkToFix.getMeaning().getValue()));
        link.setType(DataValueUtils.getText(linkType.getName()));
        final RmPath rmPath = TdoPathable.pathOfItem(newComposition, newInstruction);
        final DvEhrUri linkEhrUri = DataValueUtils.getEhrUri(newComposition.getUid().getValue(), rmPath);
        link.setTarget(linkEhrUri);
        linkedTherapy.getSecond().getLinks().add(link);
        medicationsOpenEhrDao.modifyMedicationOrderComposition(patientId, linkedTherapy.getFirst());
      }
    }
  }

  @Override
  @EventProducer(AbortTherapy.class)
  public void abortTherapy(
      final String patientId,
      final String compositionUid,
      final String ehrOrderName,
      final TherapyChangeReasonDto changeReason,
      final DateTime when)
  {
    final String userId = RequestContextHolder.getContext().getUserId();

    final MedicationOrderComposition composition = medicationsOpenEhrDao.loadMedicationOrderComposition(patientId, compositionUid);
    final MedicationInstructionInstruction instruction = MedicationsEhrUtils.getMedicationInstructionByEhrName(composition, ehrOrderName);

    abortTherapy(patientId, composition, instruction, userId, changeReason, when);
    handleTasksOnTherapyStop(patientId, compositionUid, ehrOrderName, true, false, when);

    final String originalTherapyId = medicationsBo.getOriginalTherapyId(patientId, compositionUid);
    cancelTherapyRelatedTasks(patientId, originalTherapyId);
  }

  private void cancelTherapyRelatedTasks(final String patientId, final String originalTherapyId)
  {
    preparePerfusionSyringeProcessHandler.handleTherapyCancellationMessage(patientId, originalTherapyId);

    medicationsTasksHandler.deleteTherapyTasksOfType(
        patientId,
        EnumSet.of(TaskTypeEnum.PHARMACIST_REMINDER, TaskTypeEnum.PHARMACIST_REVIEW),
        RequestContextHolder.getContext().getUserId(),
        originalTherapyId);
  }

  private void handleTasksOnTherapyStop(
      final String patientId,
      final String compositionUid,
      final String instructionName,
      final boolean completePreviousTasks,
      final boolean suspend,
      final DateTime when)
  {
    final String therapyId = TherapyIdUtils.createTherapyId(compositionUid, instructionName);

    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair = medicationsOpenEhrDao.getTherapyInstructionPair(
        patientId,
        compositionUid,
        instructionName);

    deleteTherapyAdministrationTasks(
        patientId,
        therapyId,
        instructionPair,
        completePreviousTasks,
        true,
        when,
        when);

    final MedicationOrderComposition composition = instructionPair.getFirst();
    final MedicationInstructionInstruction instruction = instructionPair.getSecond();

    final List<Link> originLinks = MedicationsEhrUtils.getLinksOfType(instruction, EhrLinkType.ORIGIN);

    medicationsTasksHandler.deleteTherapyTasksOfType(
        patientId,
        EnumSet.of(
            TaskTypeEnum.SUPPLY_REMINDER,
            TaskTypeEnum.DISPENSE_MEDICATION,
            TaskTypeEnum.SUPPLY_REVIEW,
            TaskTypeEnum.PHARMACIST_REMINDER,
            TaskTypeEnum.SWITCH_TO_ORAL,
            TaskTypeEnum.INFUSION_BAG_CHANGE_TASK,
            TaskTypeEnum.DOCTOR_REVIEW),
        RequestContextHolder.getContext().getUserId(),
        originLinks.isEmpty() ? therapyId : TherapyIdUtils.getTherapyIdFromLink(originLinks.get(0)));

    final TherapyDto therapyDto = medicationsBo.convertInstructionToTherapyDtoWithDisplayValues(
        composition,
        instruction,
        null,
        null,
        when,
        false,
        null);

    final boolean continuousInfusion = therapyDto instanceof ComplexTherapyDto && ((ComplexTherapyDto)therapyDto).isContinuousInfusion();
    final boolean therapyWithRate = therapyDto.isWithRate();
    final boolean oxygenTherapy = therapyDto instanceof OxygenTherapyDto;

    if (continuousInfusion || therapyWithRate || oxygenTherapy)
    {
      if (suspend || !medicationsBo.isTherapySuspended(composition, instruction))
      {
        final DateTime originalTherapyStart = medicationsBo.getOriginalTherapyStart(patientId, instructionPair.getFirst());
        final boolean therapyAlreadyStarted = originalTherapyStart.isBefore(when);
        //noinspection OverlyComplexBooleanExpression
        if ((oxygenTherapy || continuousInfusion) && therapyAlreadyStarted 
            || isLastAdministrationNotStop(patientId, when, therapyId, composition))
        {
          createSingleAdministrationTask(therapyDto, patientId, AdministrationTypeEnum.STOP, when, null);
        }
      }
    }
  }

  private boolean isLastAdministrationNotStop(
      final String patientId,
      final DateTime until,
      final String therapyId,
      final MedicationOrderComposition composition)
  {
    final Interval interval = Intervals.infiniteTo(until);

    final Opt<AdministrationTaskDto> lastTask = medicationsTasksProvider.findLastAdministrationTaskForTherapy(
        patientId,
        therapyId,
        interval,
        false);

    final Opt<AdministrationDto> lastGivenAdministration = Opt.from(
        administrationProvider.getTherapiesAdministrations(
            patientId,
            Collections.singletonList(Pair.of(composition, composition.getMedicationDetail().getMedicationInstruction().get(0))),
            interval)
            .stream()
            .filter(a -> AdministrationResultEnum.ADMINISTERED.contains(a.getAdministrationResult()))
            .max(Comparator.comparing(AdministrationDto::getAdministrationTime)));

    final boolean administrationNotStop = Opt.resolve(() -> lastGivenAdministration.get().getAdministrationType())
        .map(AdministrationTypeEnum.NOT_STOP::contains)
        .orElse(false);

    final boolean taskNotStop = Opt.resolve(() -> lastTask.get().getAdministrationTypeEnum())
        .map(AdministrationTypeEnum.NOT_STOP::contains)
        .orElse(false);

    if (lastTask.isPresent())
    {
      if (lastGivenAdministration.isPresent())
      {
        if (lastGivenAdministration.get().getAdministrationTime().isAfter(lastTask.get().getPlannedAdministrationTime()))
        {
          return administrationNotStop;
        }
        return taskNotStop;
      }
      return taskNotStop;
    }
    return administrationNotStop;
  }

  private void deleteTherapyAdministrationTasks(
      final String patientId,
      final String therapyId,
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyInstructionPair,
      final boolean completePastTasks,
      final boolean therapyStop,
      final DateTime fromDate,
      final DateTime when)
  {
    final List<TaskDto> tasks = medicationsTasksProvider.findAdministrationTasks(
        patientId,
        Collections.singletonList(therapyId),
        null,
        null,
        null,
        false);

    final CodedNameDto notRecordedReason =
        completePastTasks
        ? medicationsDao.getActionReasons(when, ActionReasonType.NOT_RECORDED).get(ActionReasonType.NOT_RECORDED).get(0)
        : null;

    final List<String> futureTasksIds = new ArrayList<>();
    for (final TaskDto task : tasks)
    {
      final DateTime taskTimestamp = task.getDueTime();
      if (taskTimestamp.isAfter(fromDate) || taskTimestamp.equals(fromDate))
      {
        futureTasksIds.add(task.getId());
      }
      else if (completePastTasks)
      {
        final AdministrationDto administrationDto;
        final AdministrationTypeEnum administrationTypeEnum = getAdministrationType(task);
        if (administrationTypeEnum == AdministrationTypeEnum.START)
        {
          administrationDto = new StartAdministrationDto();
          final TherapyDoseDto therapyDoseDto = TherapyTaskUtils.buildTherapyDoseDtoFromTask(task);
          ((StartAdministrationDto)administrationDto).setPlannedDose(therapyDoseDto);
        }
        else if (administrationTypeEnum == AdministrationTypeEnum.ADJUST_INFUSION)
        {
          administrationDto = new AdjustInfusionAdministrationDto();
          final TherapyDoseDto therapyDoseDto = TherapyTaskUtils.buildTherapyDoseDtoFromTask(task);
          ((AdjustInfusionAdministrationDto)administrationDto).setPlannedDose(therapyDoseDto);
        }
        else if (administrationTypeEnum == AdministrationTypeEnum.STOP)
        {
          administrationDto = new StopAdministrationDto();
        }
        else
        {
          throw new IllegalArgumentException("Inconsistent AdministrationTypeEnum.");
        }

        administrationDto.setTaskId(task.getId());
        administrationDto.setPlannedTime(taskTimestamp);
        administrationDto.setAdministrationTime(taskTimestamp);
        administrationDto.setNotAdministeredReason(notRecordedReason);

        final String userId = RequestContextHolder.getContext().getUserId();
        final String administrationUid = administrationHandler.confirmTherapyAdministration(
            therapyInstructionPair.getFirst(),
            patientId,
            userId,
            administrationDto,
            MedicationActionEnum.WITHHOLD,
            false,
            null,
            null,
            when);

        medicationsTasksHandler.associateTaskWithAdministration(administrationDto.getTaskId(), administrationUid);
        processService.completeTasks(administrationDto.getTaskId());
      }
    }

    if (!therapyStop)
    {
      final List<String> activeStopOrAdjustTaskIdsWithGivenStart = getActiveStopOrAdjustTaskIdsWithGivenStart(tasks);
      futureTasksIds.removeAll(activeStopOrAdjustTaskIdsWithGivenStart);
    }

    if (!futureTasksIds.isEmpty())
    {
      processService.deleteTasks(futureTasksIds);
    }
  }

  private List<String> getActiveStopOrAdjustTaskIdsWithGivenStart(final List<TaskDto> tasks)
  {
    final Map<String, List<TaskDto>> groupedTasks = tasks
        .stream()
        .filter(t -> t.getVariables().get(AdministrationTaskDef.GROUP_UUID.getName()) != null)
        .collect(Collectors.groupingBy(t -> ((String)t.getVariables().get(AdministrationTaskDef.GROUP_UUID.getName()))));

    final List<String> stopTaskIds = new ArrayList<>();
    for (final Map.Entry<String, List<TaskDto>> entry : groupedTasks.entrySet())
    {
      if (entry.getValue().stream().noneMatch(t -> getAdministrationType(t) == AdministrationTypeEnum.START))
      {
        entry.getValue()
            .stream()
            .filter(t -> getAdministrationType(t) == AdministrationTypeEnum.STOP || getAdministrationType(t) == AdministrationTypeEnum.ADJUST_INFUSION)
            .filter(Objects::nonNull)
            .findAny()
            .map(AbstractTaskDto::getId)
            .ifPresent(stopTaskIds::add);
      }
    }
    return stopTaskIds;
  }

  private AdministrationTypeEnum getAdministrationType(final TaskDto t)
  {
    return AdministrationTypeEnum.valueOf((String)t.getVariables().get(AdministrationTaskDef.ADMINISTRATION_TYPE.getName()));
  }

  private void abortTherapy(
      final String patientId,
      final MedicationOrderComposition composition,
      final MedicationInstructionInstruction instruction,
      final String userId,
      final TherapyChangeReasonDto changeReason,
      final DateTime when)
  {
    final DateTime medicationTimingStart =
        DataValueUtils.getDateTime(instruction.getOrder().get(0).getMedicationTiming().getStartDate());
    final DateTime medicationTimingEnd =
        DataValueUtils.getDateTime(instruction.getOrder().get(0).getMedicationTiming().getStopDate());

    //if medication not started yet
    final NamedExternalDto user = RequestContextHolder.getContext().getUserMetadata()
        .map(meta -> new NamedExternalDto(meta.getId(), meta.getFullName()))
        .get();
    if (medicationTimingStart.isAfter(when))
    {
      for (final MedicationInstructionInstruction.OrderActivity orderActivity : instruction.getOrder())
      {
        orderActivity.getMedicationTiming().setStartDate(DataValueUtils.getDateTime(when));
        orderActivity.getMedicationTiming().setStopDate(DataValueUtils.getDateTime(when));
      }
      MedicationsEhrUtils.addMedicationActionTo(
          composition,
          MedicationActionEnum.CANCEL,
          user,
          changeReason,
          when);
    }
    else
    {
      if (medicationTimingEnd == null || medicationTimingEnd.isAfter(when))
      {
        for (final MedicationInstructionInstruction.OrderActivity orderActivity : instruction.getOrder())
        {
          orderActivity.getMedicationTiming().setStopDate(DataValueUtils.getDateTime(when));
        }
      }
      MedicationsEhrUtils.addMedicationActionTo(
          composition,
          MedicationActionEnum.ABORT,
          user,
          changeReason,
          when);
    }

    medicationsOpenEhrDao.modifyMedicationOrderComposition(patientId, composition);
  }

  private void addMedicationOrderCompositionEventContext(
      final MedicationOrderComposition medicationOrder,
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

  @Override
  @EventProducer(SuspendTherapy.class)
  public String suspendTherapy(
      final String patientId,
      final String compositionUid,
      final String ehrOrderName,
      final TherapyChangeReasonDto changeReason,
      final DateTime when)
  {
    final String newCompositionUid = addMedicationActionAndSaveComposition(
        patientId,
        compositionUid,
        MedicationActionEnum.SUSPEND,
        changeReason,
        when);

    handleTasksOnTherapyStop(patientId, compositionUid, ehrOrderName, false, true, when);

    return newCompositionUid;
  }

  private String addMedicationActionAndSaveComposition(
      final String patientId,
      final String compositionUid,
      final MedicationActionEnum actionEnum,
      final TherapyChangeReasonDto changeReason,
      final DateTime when)
  {
    final MedicationOrderComposition composition =
        medicationsOpenEhrDao.loadMedicationOrderComposition(patientId, compositionUid);

    final NamedExternalDto user = RequestContextHolder.getContext().getUserMetadata()
        .map(meta -> new NamedExternalDto(meta.getId(), meta.getFullName()))
        .get();

    MedicationsEhrUtils.addMedicationActionTo(
        composition,
        actionEnum,
        user,
        changeReason,
        when);

    return medicationsOpenEhrDao.modifyMedicationOrderComposition(patientId, composition);
  }

  @Override
  @EventProducer(ReissueTherapy.class)
  public void reissueTherapy(
      final String patientId,
      final String compositionUid,
      final String ehrOrderName,
      final DateTime when)
  {
    final MedicationOrderComposition composition =
        medicationsOpenEhrDao.loadMedicationOrderComposition(patientId, compositionUid);

    final NamedExternalDto user = RequestContextHolder.getContext().getUserMetadata()
        .map(meta -> new NamedExternalDto(meta.getId(), meta.getFullName()))
        .get();
    MedicationsEhrUtils.addMedicationActionTo(
        composition,
        MedicationActionEnum.REISSUE,
        user,
        when);

    medicationsOpenEhrDao.modifyMedicationOrderComposition(patientId, composition);

    final DateTime lastTaskTimestamp = medicationsTasksProvider.findLastAdministrationTaskTimeForTherapy(
        patientId,
        TherapyIdUtils.createTherapyId(compositionUid, ehrOrderName),
        Intervals.infiniteFrom(when.minusDays(30)),
        true)
        .orElse(when);

    createTherapyTasks(
        patientId,
        medicationsOpenEhrDao.getTherapyInstructionPair(patientId, compositionUid, ehrOrderName).getFirst(),
        AdministrationTaskCreateActionEnum.REISSUE,
        lastTaskTimestamp,
        when);
  }

  @Override
  public String reviewTherapy(final String patientId, final String compositionUid)
  {
    final DateTime when = RequestContextHolder.getContext().getRequestTimestamp();
    return addMedicationActionAndSaveComposition(patientId, compositionUid, MedicationActionEnum.REVIEW, null, when);
  }

  @Override
  public void saveConsecutiveDays(
      final String patientId,
      final String compositionUid,
      final String ehrOrderName,
      final String userId,
      final Integer consecutiveDays)
  {
    final MedicationOrderComposition composition =
        medicationsOpenEhrDao.loadMedicationOrderComposition(patientId, compositionUid);
    final MedicationInstructionInstruction instruction =
        MedicationsEhrUtils.getMedicationInstructionByEhrName(composition, ehrOrderName);

    updateConsecutiveDays(instruction, consecutiveDays);
    medicationsOpenEhrDao.modifyMedicationOrderComposition(patientId, composition);
  }

  private void updateConsecutiveDays(final MedicationInstructionInstruction instruction, final Integer consecutiveDays)
  {
    for (final MedicationInstructionInstruction.OrderActivity order : instruction.getOrder())
    {
      if (consecutiveDays != null)
      {
        order.setPastDaysOfTherapy(new DvCount());
        order.getPastDaysOfTherapy().setMagnitude(consecutiveDays);
      }
      else
      {
        order.setPastDaysOfTherapy(null);
      }
    }
  }

  @Override
  public boolean startLinkedTherapy(
      final String patientId,
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> linkedTherapy,
      final DateTime startTimestamp)
  {
    final MedicationOrderComposition composition = linkedTherapy.getFirst();
    final MedicationInstructionInstruction instruction = linkedTherapy.getSecond();

    final boolean therapyCancelledOrAborted = medicationsBo.isTherapyCancelledOrAborted(composition, instruction);
    if (!therapyCancelledOrAborted)
    {
      final PartyIdentified composer = (PartyIdentified)composition.getComposer();
      final NamedExternalDto composersName =
          new NamedExternalDto(composer.getExternalRef().getId().getValue(), composer.getName());

      MedicationsEhrUtils.addMedicationActionTo(composition, MedicationActionEnum.START, composersName, startTimestamp);
      moveTherapyInterval(patientId, composition, instruction, startTimestamp, false);
      medicationsOpenEhrDao.modifyMedicationOrderComposition(patientId, composition);
      return true;
    }
    return false;
  }

  @Override
  public void updateTherapySelfAdministeringStatus(
      final String patientId,
      final MedicationOrderComposition orderComposition,
      final SelfAdministeringActionEnum selfAdministeringActionEnum,
      final String userId,
      final DateTime when)
  {
    final MedicationInstructionInstruction instruction = orderComposition.getMedicationDetail()
        .getMedicationInstruction()
        .get(0);

    if (selfAdministeringActionEnum == SelfAdministeringActionEnum.STOP_SELF_ADMINISTERING)
    {
      instruction.getOrder().get(0).setParsableDoseDescription(null);
    }
    else
    {
      final DvParsable parsableDoseDescription = new DvParsable();
      parsableDoseDescription.setValue(selfAdministeringActionEnum.name());
      parsableDoseDescription.setFormalism(JsonUtil.toJson(when));
      instruction.getOrder().get(0).setParsableDoseDescription(parsableDoseDescription);
    }
    //TODO nejc save differently?

    medicationsOpenEhrDao.modifyMedicationOrderComposition(patientId, orderComposition);
  }

  private void moveTherapyInterval(
      final String patientId,
      final MedicationOrderComposition composition,
      final MedicationInstructionInstruction instruction,
      final DateTime startTimestamp,
      final boolean save)
  {
    DateTime endDate = null;
    for (final MedicationInstructionInstruction.OrderActivity orderActivity : instruction.getOrder())
    {
      final MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster medicationTiming = orderActivity.getMedicationTiming();

      if (medicationTiming.getStopDate() != null)
      {
        final DateTime start = DataValueUtils.getDateTime(medicationTiming.getStartDate());
        final DateTime end = DataValueUtils.getDateTime(medicationTiming.getStopDate());
        final long durationInMillis = end.getMillis() - start.getMillis();
        final DateTime newEnd = new DateTime(startTimestamp.getMillis() + durationInMillis);
        medicationTiming.setStopDate(DataValueUtils.getDateTime(newEnd));
        endDate = newEnd;
      }
      medicationTiming.setStartDate(DataValueUtils.getDateTime(startTimestamp));
    }
    if (save)
    {
      medicationsOpenEhrDao.modifyMedicationOrderComposition(patientId, composition);
    }
    if (endDate != null)
    {
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> linkedTherapies =
          medicationsOpenEhrDao.getLinkedTherapies(
              patientId,
              composition.getUid().getValue(),
              instruction.getName().getValue(),
              EhrLinkType.FOLLOW);

      Preconditions.checkArgument(linkedTherapies.size() < 2, "not more than 1 follow therapy should exist");

      if (!linkedTherapies.isEmpty())
      {
        final Pair<MedicationOrderComposition, MedicationInstructionInstruction> linkedTherapy = linkedTherapies.get(0);
        moveTherapyInterval(patientId, linkedTherapy.getFirst(), linkedTherapy.getSecond(), endDate, true);
      }
    }
  }

  @Override
  public void createTherapyTasks(
      final String patientId,
      final MedicationOrderComposition composition,
      final AdministrationTaskCreateActionEnum action,
      final DateTime lastTaskTimestamp,
      final DateTime when)
  {
    final MedicationActionAction startAction =
        medicationsBo.getInstructionAction(
            composition,
            composition.getMedicationDetail().getMedicationInstruction().get(0),
            MedicationActionEnum.START,
            null);

    //noinspection VariableNotUsedInsideIf
    if (startAction != null)
    {
      final TherapyDto therapyDto = medicationsBo.convertInstructionToTherapyDtoWithDisplayValues(
          composition,
          composition.getMedicationDetail().getMedicationInstruction().get(0),
          null,
          null,
          when,
          false,
          null);

      final List<NewTaskRequestDto> taskRequests =
          administrationTaskCreator.createTaskRequests(
              patientId,
              therapyDto,
              action,
              when,
              lastTaskTimestamp);

      processService.createTasks(taskRequests.toArray(new NewTaskRequestDto[taskRequests.size()]));
    }
  }

  private void createSingleAdministrationTask(
      final TherapyDto therapy,
      final String patientId,
      final AdministrationTypeEnum administrationTypeEnum,
      final DateTime when,
      final TherapyDoseDto dose)
  {
    final NewTaskRequestDto taskRequest = administrationTaskCreator.createMedicationTaskRequest(
        patientId,
        therapy,
        administrationTypeEnum,
        when,
        dose);

    processService.createTasks(taskRequest);
  }

  @Override
  public void createAdditionalAdministrationTask(
      final MedicationOrderComposition composition,
      final String patientId,
      final DateTime timestamp,
      final AdministrationTypeEnum type,
      final TherapyDoseDto dose)
  {
    final TherapyDto therapy = medicationsBo.convertInstructionToTherapyDtoWithDisplayValues(
        composition,
        composition.getMedicationDetail().getMedicationInstruction().get(0),
        null,
        null,
        timestamp,
        false,
        null);

    final List<NewTaskRequestDto> requests = administrationTaskCreator.createTaskRequestsForAdditionalAdministration(
        patientId,
        therapy,
        type,
        timestamp,
        dose);

    processService.createTasks(requests.toArray(new NewTaskRequestDto[requests.size()]));
  }
}
