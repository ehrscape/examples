package com.marand.thinkmed.medications.warnings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.marand.maf.core.data.IdentityDto;
import com.marand.maf.core.service.RequestContextHolder;
import com.marand.thinkmed.api.externals.data.object.ExternalCatalogDto;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.MedicationsExternalValueType;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medicationsexternal.WarningSeverity;
import com.marand.thinkmed.medicationsexternal.WarningType;
import com.marand.thinkmed.medicationsexternal.dto.MedicationForWarningsSearchDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationWarningsDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationsWarningDto;
import com.marand.thinkmed.medicationsexternal.dto.WarningsProviderDto;
import com.marand.thinkmed.medicationsexternal.service.MedicationsExternalService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Klavdij Lapajne
 */

public class TherapyWarningsProvider
{
  private MedicationsDao medicationsDao;
  private MedicationsExternalService medicationsExternalService;
  private MentalHealthWarningsHandler mentalHealthWarningsHandler;

  @Required
  public void setMedicationsDao(final MedicationsDao medicationsDao)
  {
    this.medicationsDao = medicationsDao;
  }

  @Required
  public void setMedicationsExternalService(final MedicationsExternalService medicationsExternalService)
  {
    this.medicationsExternalService = medicationsExternalService;
  }

  @Required
  public void setMentalHealthWarningsHandler(final MentalHealthWarningsHandler mentalHealthWarningsHandler)
  {
    this.mentalHealthWarningsHandler = mentalHealthWarningsHandler;
  }

  public MedicationWarningsDto findMedicationWarnings(
      final String patientId,
      final long patientAgeInDays,
      final Double patientWeightInKg,
      final Double bsaInM2,
      final boolean isFemale,
      final List<ExternalCatalogDto> patientDiseasesList,
      final List<NamedExternalDto> patientAllergiesList,
      final List<MedicationForWarningsSearchDto> medicationSummaries,
      final Set<WarningSeverity> severityFilterValues,
      final boolean loadCustomWarnings,
      final DateTime when)
  {
    //final List<String> diseaseTypeIcd9Codes = medicationsDao.getIcd9Codes(diseaseTypeCode);     //mapping for fdb
    final List<MedicationForWarningsSearchDto> medicationsForWarnings = new ArrayList<>();
    medicationsForWarnings.addAll(medicationSummaries);

    final List<MedicationsWarningDto> warnings = new ArrayList<>();
    for (final WarningsProviderDto provider : medicationsExternalService.getWarningProviders())
    {
      fillMedicationsProductFlag(medicationsForWarnings);
      fillMedicationExternalIds(provider.getExternalSystem(), medicationsForWarnings);
      final MedicationsWarningDto medicationsWithNoExternalIdsWarning =
          removeMedicationsWithNoExternalIdAndCreateWarning(medicationsForWarnings);
      if (medicationsWithNoExternalIdsWarning != null)
      {
        warnings.add(medicationsWithNoExternalIdsWarning);
      }
      if (!medicationsForWarnings.isEmpty() || !patientAllergiesList.isEmpty() || !patientDiseasesList.isEmpty())
      {
        fillMedicationExternalValues(provider.getExternalSystem(), medicationsForWarnings);
        final List<NamedExternalDto> allergiesExternalValues =
            getExternalValues(provider.getExternalSystem(), MedicationsExternalValueType.ALLERGY, patientAllergiesList);

        final List<NamedExternalDto> diseaseTypeValues = new ArrayList<>();
        if (provider.isRequiresDiseaseCodesTranslation())
        {
          diseaseTypeValues.addAll(
              getExternalValues(
                  provider.getExternalSystem(),
                  MedicationsExternalValueType.DISEASE,
                  patientDiseasesList));
        }
        else
        {
          //todo refactor
          for (final ExternalCatalogDto disease : patientDiseasesList)
          {
            final NamedExternalDto namedExternalDto = new NamedExternalDto(disease.getCode(), disease.getName());
            diseaseTypeValues.add(namedExternalDto);
          }
        }

        warnings.addAll(
            medicationsExternalService.findMedicationWarnings(
                provider.getExternalSystem(),
                patientAgeInDays,
                patientWeightInKg,
                bsaInM2,
                isFemale,
                diseaseTypeValues,
                allergiesExternalValues,
                medicationsForWarnings));
      }
    }

    final List<MedicationForWarningsSearchDto> prospectiveMedications = medicationSummaries
        .stream()
        .filter(MedicationForWarningsSearchDto::isProspective)
        .collect(Collectors.toList());

    if (loadCustomWarnings)
    {
      final Set<Long> prospectiveMedicationIds = prospectiveMedications
          .stream()
          .map(IdentityDto::getId)
          .collect(Collectors.toSet());

      warnings.addAll(medicationsDao.getCustomWarningsForMedication(prospectiveMedicationIds, when));
    }

    warnings.addAll(mentalHealthWarningsHandler.getMentalHealthMedicationsWarnings(patientId, prospectiveMedications, when));

    return filterSortAndCountWarnings(warnings, severityFilterValues);
  }

  private MedicationWarningsDto filterSortAndCountWarnings(
      final List<MedicationsWarningDto> warnings,
      final Set<WarningSeverity> severityFilterValues)
  {
    final MedicationWarningsDto medicationWarningsDto = new MedicationWarningsDto();
    int highSeverityWarningsCount = 0;
    int significantSeverityWarningsCount = 0;
    int lowSeverityWarningsCount = 0;
    int noSeverityWarningsCount = 0;

    for (final MedicationsWarningDto warning : warnings)
    {
      if (warning.getSeverity() == null)
      {
        if (warning.getType() != WarningType.UNMATCHED)
        {
          noSeverityWarningsCount++;
        }
        medicationWarningsDto.getWarnings().add(warning);
      }
      else
      {
        if (warning.getSeverity() == WarningSeverity.HIGH)
        {
          highSeverityWarningsCount++;
        }
        else if (warning.getSeverity() == WarningSeverity.SIGNIFICANT)
        {
          significantSeverityWarningsCount++;
        }
        else if (warning.getSeverity() == WarningSeverity.LOW)
        {
          lowSeverityWarningsCount++;
        }

        if (severityFilterValues.contains(warning.getSeverity()))
        {
          medicationWarningsDto.getWarnings().add(warning);
        }
      }
    }

    medicationWarningsDto.setHighSeverityWarningsCount(highSeverityWarningsCount);
    medicationWarningsDto.setSignificantSeverityWarningsCount(significantSeverityWarningsCount);
    medicationWarningsDto.setLowSeverityWarningsCount(lowSeverityWarningsCount);
    medicationWarningsDto.setNoSeverityWarningsCount(noSeverityWarningsCount);

    medicationWarningsDto.getWarnings().sort(getMedicationsWarningDtoComparator());

    return medicationWarningsDto;
  }

  Comparator<MedicationsWarningDto> getMedicationsWarningDtoComparator()
  {
    return Comparator
        .comparing((MedicationsWarningDto w) -> w.getType() != WarningType.UNMATCHED)
        .thenComparing(MedicationsWarningDto::getSeverity, Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(MedicationsWarningDto::getType);
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
    for (final MedicationForWarningsSearchDto medication : medicationsForWarnings)
    {
      final String medicationExternalId = medicationsDao.getMedicationExternalId(
          externalSystem,
          medication.getId(),
          RequestContextHolder.getContext().getRequestTimestamp());
      if (medicationExternalId != null)
      {
        medication.setExternalId(medicationExternalId);
      }
    }
  }

  @Nullable
  private MedicationsWarningDto removeMedicationsWithNoExternalIdAndCreateWarning(
      final List<MedicationForWarningsSearchDto> medicationsForWarnings)
  {
    final List<MedicationForWarningsSearchDto> medicationsWithExternalId = new ArrayList<>();
    final MedicationsWarningDto warningDto = new MedicationsWarningDto();
    String medicationsWithNoExternalIdNames = "";
    for (final MedicationForWarningsSearchDto medication : medicationsForWarnings)
    {
      if (medication.getExternalId() != null)
      {
        medicationsWithExternalId.add(medication);
      }
      else
      {
        if (medication.isProspective())
        {
          final String medicationName = medication.getShortName() != null ? medication.getShortName() : medication.getName();
          warningDto.getMedications().add(new NamedExternalDto(String.valueOf(medication.getId()), medicationName));
          if (!medicationsWithNoExternalIdNames.isEmpty())
          {
            medicationsWithNoExternalIdNames += " &ndash; ";
          }
          medicationsWithNoExternalIdNames += medicationName;
        }
      }
    }
    medicationsForWarnings.clear();
    medicationsForWarnings.addAll(medicationsWithExternalId);

    if (!medicationsWithNoExternalIdNames.isEmpty())
    {
      medicationsWithNoExternalIdNames += "!";
      warningDto.setDescription(medicationsWithNoExternalIdNames);
      warningDto.setType(WarningType.UNMATCHED);
      return warningDto;
    }
    return null;
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
      if (medication.getRouteCode() != null)
      {
        routesSet.add(medication.getRouteCode());
      }
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

  private <T extends  NamedExternalDto> List<NamedExternalDto> getExternalValues(
      final String externalSystem,
      final MedicationsExternalValueType valueType,
      final List<T> valuesList)
  {
    final Set<String> allergyIds = new HashSet<>();
    for (final NamedExternalDto value : valuesList)
    {
      allergyIds.add(value.getId());
    }

    final Map<String, String> externalAllergiesMap = medicationsDao.getMedicationExternalValues(
        externalSystem, valueType, allergyIds);

    return new ArrayList<>(
        Collections2.transform(
            Collections2.filter(
                valuesList,
                new Predicate<NamedExternalDto>()
                {
                  @Override
                  public boolean apply(@Nullable final NamedExternalDto input)
                  {
                    return input != null && externalAllergiesMap.containsKey(input.getId());
                  }
                }),
            new Function<NamedExternalDto, NamedExternalDto>()
            {
              @Nullable
              @Override
              public NamedExternalDto apply(@Nullable final NamedExternalDto input)
              {
                return new NamedExternalDto(externalAllergiesMap.get(input.getId()), input.getName());
          }
        }
    ));
  }
}
