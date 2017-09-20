package com.marand.thinkmed.medications.warnings.additional.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.Opt;
import com.marand.maf.core.data.IdentityDto;
import com.marand.maf.core.data.object.NamedIdentityDto;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthAllowedMedicationsDo;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTemplateMemberDto;
import com.marand.thinkmed.medications.mentalhealth.MentalHealthFormProvider;
import com.marand.thinkmed.medications.warnings.MentalHealthWarningsHandler;
import com.marand.thinkmed.medicationsexternal.WarningSeverity;
import com.marand.thinkmed.medicationsexternal.WarningType;
import com.marand.thinkmed.medicationsexternal.dto.MedicationForWarningsSearchDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationsWarningDto;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Nejc Korasa
 */
public class MentalHealthWarningsHandlerImpl implements MentalHealthWarningsHandler
{
  private MedicationsBo medicationsBo;
  private MedicationsDao medicationsDao;
  private MentalHealthFormProvider mentalHealthFormProvider;

  @Required
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Required
  public void setMedicationsDao(final MedicationsDao medicationsDao)
  {
    this.medicationsDao = medicationsDao;
  }

  @Required
  public void setMentalHealthFormProvider(final MentalHealthFormProvider mentalHealthFormProvider)
  {
    this.mentalHealthFormProvider = mentalHealthFormProvider;
  }

  @Override
  public List<MedicationsWarningDto> getMentalHealthMedicationsWarnings(
      @Nonnull final String patientId,
      @Nonnull final Collection<MedicationForWarningsSearchDto> medicationsForWarnings,
      @Nonnull final DateTime when)
  {
    Preconditions.checkNotNull(patientId, "patientId must not be null");
    Preconditions.checkNotNull(medicationsForWarnings, "medicationsForWarnings must not be null");
    Preconditions.checkNotNull(when, "when must not be null");

    final Opt<MentalHealthDocumentDto> document = mentalHealthFormProvider.getLatestMentalHealthDocument(patientId);
    if (document.isAbsent())
    {
      return Collections.emptyList();
    }

    final MentalHealthAllowedMedicationsDo allowedMedications = getAllowedMedications(document.get());
    return medicationsForWarnings
        .stream()
        .filter(m -> medicationsBo.isMentalHealthMedication(m.getId()))
        .filter(m -> !isMedicationIdWithRoutesAllowed(
            m.getRouteCode() != null ? Collections.singleton(Long.valueOf(m.getRouteCode())) : Collections.emptyList(),
            allowedMedications)
            .test(m.getId()))
        .map(m -> buildMentalHealthMedicationsWarning(new NamedExternalDto(String.valueOf(m.getId()), m.getName())))
        .collect(Collectors.toList());
  }

  @Override
  public MedicationsWarningDto buildMentalHealthMedicationsWarning(@Nonnull final NamedExternalDto medication)
  {
    Preconditions.checkNotNull(medication, "medication must not be null");

    final MedicationsWarningDto warning = new MedicationsWarningDto();

    final StringBuilder description = new StringBuilder()
        .append(Dictionary.getMessage("mental.health.medication.not.in.form", medication.getName()))
        .append(" \n")
        .append(Dictionary.getMessage("mental.health.medication.reason.to.approve", medication.getName()));

    warning.setDescription(description.toString());
    warning.setType(WarningType.MENTAL_HEALTH);
    warning.setSeverity(WarningSeverity.HIGH);
    warning.setMedications(Collections.singletonList(medication));

    return warning;
  }

  @Override
  public MentalHealthAllowedMedicationsDo getAllowedMedications(@Nonnull final MentalHealthDocumentDto mentalHealthDocumentDto)
  {
    Preconditions.checkNotNull(mentalHealthDocumentDto, "mentalHealthDocumentDto must not be null!");

    final Map<Long, List<Long>> routesMapForMedications = mentalHealthDocumentDto.getMentalHealthMedicationDtoList()
        .stream()
        .filter(m -> m.getRoute() != null)
        .collect(Collectors.groupingBy(
            NamedIdentityDto::getId,
            Collectors.mapping(m -> m.getRoute().getId(), Collectors.toList())));

    final Set<Long> templateIds = mentalHealthDocumentDto.getMentalHealthTemplateDtoList()
        .stream()
        .map(NamedIdentityDto::getId)
        .collect(Collectors.toSet());

    final Map<Long, List<Long>> routesMapForTemplates = medicationsDao.getMentalHealthTemplateMembers(templateIds)
        .stream()
        .filter(m -> m.getRoute() != null)
        .collect(Collectors.groupingBy(
            MentalHealthTemplateMemberDto::getMedicationId,
            Collectors.mapping(m -> m.getRoute().getId(), Collectors.toList())));

    final Set<Long> allRouteMedications = mentalHealthDocumentDto.getMentalHealthMedicationDtoList()
        .stream()
        .filter(m -> m.getRoute() == null)
        .map(IdentityDto::getId)
        .collect(Collectors.toSet());

    allRouteMedications.addAll(
        medicationsDao.getMentalHealthTemplateMembers(templateIds)
            .stream()
            .filter(m -> m.getRoute() == null)
            .map(IdentityDto::getId)
            .collect(Collectors.toSet()));

    final SetMultimap<Long, Long> medicationIdsWithRouteIds = HashMultimap.create();

    routesMapForMedications.entrySet().forEach(e -> medicationIdsWithRouteIds.putAll(e.getKey(), e.getValue()));
    routesMapForTemplates.entrySet().forEach(e -> medicationIdsWithRouteIds.putAll(e.getKey(), e.getValue()));

    return new MentalHealthAllowedMedicationsDo(medicationIdsWithRouteIds, allRouteMedications);
  }

  @Override
  public Predicate<MedicationDto> isMedicationWithRoutesAllowed(
      @Nonnull final Collection<Long> routeIds,
      @Nonnull final MentalHealthAllowedMedicationsDo allowedMedications)
  {
    Preconditions.checkNotNull(routeIds, "routeIds must not be null!");
    Preconditions.checkNotNull(allowedMedications, "allowedMedications must not be null!");

    return medication -> isMedicationIdWithRoutesAllowed(routeIds, allowedMedications).test(medication.getId());
  }

  @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
  private Predicate<Long> isMedicationIdWithRoutesAllowed(
      @Nonnull final Collection<Long> routeIds,
      @Nonnull final MentalHealthAllowedMedicationsDo allowedMedications)
  {
    return medicationId ->
    {
      final SetMultimap<Long, Long> medicationIdsWithRouteIds = allowedMedications.getMedicationIdsWithRouteIds();
      final Set<Long> allRoutesMedicationIds = allowedMedications.getAllRoutesMedicationIds();
      if (!medicationIdsWithRouteIds.containsKey(medicationId) && !allRoutesMedicationIds.contains(medicationId))
      {
        return false;
      }
      if (allRoutesMedicationIds.contains(medicationId))
      {
        return true;
      }

      return routeIds
          .stream()
          .filter(routeId -> medicationIdsWithRouteIds.get(medicationId).contains(routeId))
          .anyMatch(Objects::nonNull);
    };
  }
}
