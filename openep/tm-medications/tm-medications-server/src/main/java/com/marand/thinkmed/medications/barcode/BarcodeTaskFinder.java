package com.marand.thinkmed.medications.barcode;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.Pair;
import com.marand.maf.core.StringUtils;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.MedicationsFinder;
import com.marand.thinkmed.medications.business.util.TherapyIdUtils;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.barcode.BarcodeTaskSearchDto;
import com.marand.thinkmed.medications.dto.barcode.BarcodeTaskSearchDto.BarcodeSearchResult;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Mitja Lapajne
 */
public class BarcodeTaskFinder
{
  private MedicationsDao medicationsDao;
  private MedicationsTasksProvider medicationsTasksProvider;
  private MedicationsBo medicationsBo;
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private MedicationsFinder medicationsFinder;

  @Required
  public void setMedicationsDao(final MedicationsDao medicationsDao)
  {
    this.medicationsDao = medicationsDao;
  }

  @Required
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Required
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Required
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Required
  public void setMedicationsFinder(final MedicationsFinder medicationsFinder)
  {
    this.medicationsFinder = medicationsFinder;
  }

  public BarcodeTaskSearchDto getAdministrationTaskForBarcode(
      @Nonnull final String patientId,
      @Nonnull final String medicationBarcode,
      @Nonnull final DateTime when)
  {
    StringUtils.checkNotBlank(patientId, "patientId");
    StringUtils.checkNotBlank(medicationBarcode, "barcode");
    Preconditions.checkNotNull(when, "when");

    final Long barcodeMedicationId = medicationsDao.getMedicationIdForBarcode(medicationBarcode);
    String barcodeTherapyId = null;

    if (barcodeMedicationId == null)
    {
      final boolean validTherapyId = TherapyIdUtils.isValidTherapyId(medicationBarcode);
      if (validTherapyId)
      {
        barcodeTherapyId = medicationBarcode;
      }
      else
      {
        return new BarcodeTaskSearchDto(BarcodeSearchResult.NO_MEDICATION);
      }
    }

    final List<TaskDto> dueTasks = medicationsTasksProvider.findAdministrationTasks(
        Collections.singleton(patientId),
        when.minusHours(1),
        when.plusHours(1));

    final List<TaskDto> dueStartTasks = dueTasks.stream()
        .filter(t -> AdministrationTypeEnum.START.name().equals(
            t.getVariables().get(AdministrationTaskDef.ADMINISTRATION_TYPE.getName())))
        .collect(Collectors.toList());

    if (dueStartTasks.isEmpty())
    {
      return new BarcodeTaskSearchDto(BarcodeSearchResult.NO_TASK);
    }

    final MultiValueMap<String, String> therapyIdTasksIdMap = buildTherapyIdTaskIdMap(dueStartTasks);

    final Set<String> matchingTherapyIds;

    if (barcodeMedicationId != null)
    {
      matchingTherapyIds = getTherapiesWithMatchingMedication(
          barcodeMedicationId,
          therapyIdTasksIdMap.keySet(),
          when);
    }
    else
    {
      matchingTherapyIds = getTherapiesWithMatchingOriginalTherapyIds(patientId, barcodeTherapyId, therapyIdTasksIdMap);
    }

    final Set<String> matchingTaskIds = matchingTherapyIds.stream()
        .flatMap(therapyId -> therapyIdTasksIdMap.get(therapyId).stream())
        .collect(Collectors.toSet());

    if (matchingTaskIds.size() == 1)
    {
      return new BarcodeTaskSearchDto(
          BarcodeSearchResult.TASK_FOUND,
          matchingTaskIds.iterator().next(),
          barcodeMedicationId);
    }
    else if (matchingTaskIds.size() > 1)
    {
      return new BarcodeTaskSearchDto(BarcodeSearchResult.MULTIPLE_TASKS);
    }
    else
    {
      return new BarcodeTaskSearchDto(BarcodeSearchResult.NO_TASK);
    }
  }

  private Set<String> getTherapiesWithMatchingOriginalTherapyIds(
      final String patientId,
      final String barcodeTherapyId,
      final MultiValueMap<String, String> therapyIdTasksIdMap)
  {
    return therapyIdTasksIdMap.keySet().stream()
        .filter(therapyId -> getTherapyOriginalId(patientId, therapyId).equals(barcodeTherapyId))
        .collect(Collectors.toSet());
  }

  private String getTherapyOriginalId(final String patientId, final String therapyId)
  {
    return medicationsBo.getOriginalTherapyId(patientId, TherapyIdUtils.parseTherapyId(therapyId).getFirst());
  }

  private MultiValueMap<String, String> buildTherapyIdTaskIdMap(final List<TaskDto> tasks)
  {
    final MultiValueMap<String, String> therapyIdTasksIdMap = new LinkedMultiValueMap<>();
    tasks.forEach(
        task -> therapyIdTasksIdMap.add(
            (String)task.getVariables().get(AdministrationTaskDef.THERAPY_ID.getName()),
            task.getId()));
    return therapyIdTasksIdMap;
  }

  private Set<String> getTherapiesWithMatchingMedication(
      final Long barcodeMedicationId,
      final Set<String> therapyIds,
      final DateTime when)
  {
    final Set<String> therapyCompositionUids = therapyIds.stream()
        .map(therapyId -> TherapyIdUtils.parseTherapyId(therapyId).getFirst())
        .collect(Collectors.toSet());

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> therapyInstructionPairs =
        medicationsOpenEhrDao.getTherapyInstructionPairs(therapyCompositionUids);

    return therapyInstructionPairs.stream()
        .filter(pair -> isTherapyWithMatchingMedication(pair.getSecond(), barcodeMedicationId, when))
        .map(pair -> TherapyIdUtils.createTherapyId(pair.getFirst()))
        .collect(Collectors.toSet());
  }

  private boolean isTherapyWithMatchingMedication(
      final MedicationInstructionInstruction instruction,
      final Long barcodeMedicationId,
      final DateTime when)
  {
    final OrderActivity orderActivity = instruction.getOrder().get(0);
    final List<Long> medicationIds = medicationsBo.getMedicationIds(orderActivity);
    for (final Long therapyMedicationId : medicationIds)
    {
      if (therapyMedicationId.equals(barcodeMedicationId))
      {
        return true;
      }
      else
      {
        final Set<Long> exchangableMedicationIds = getExchangableMedications(therapyMedicationId, orderActivity, when);
        if (exchangableMedicationIds.contains(barcodeMedicationId))
        {
          return true;
        }
      }
    }
    return false;
  }

  private Set<Long> getExchangableMedications(
      final Long therapyMedicationId,
      final OrderActivity orderActivity,
      final DateTime when)
  {
    final List<Long> routeIds = orderActivity.getAdministrationDetails().getRoute().stream()
        .map(r -> Long.valueOf(r.getDefiningCode().getCodeString()))
        .collect(Collectors.toList());

    final List<MedicationDto> exchangeableMedications = medicationsFinder.findMedicationProducts(
        therapyMedicationId,
        routeIds,
        when);

    return exchangeableMedications.stream()
        .map(MedicationDto::getId)
        .collect(Collectors.toSet());
  }
}
