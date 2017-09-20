package com.marand.thinkmed.medications.connector.impl.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.CollectionUtils;
import com.marand.maf.core.Opt;
import com.marand.maf.core.StringUtils;
import com.marand.thinkmed.api.externals.data.object.ExternalCatalogDto;
import com.marand.thinkmed.api.externals.data.object.ExternalIdentityDto;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.connector.MedicationsConnector;
import com.marand.thinkmed.medications.connector.data.object.EncounterDto;
import com.marand.thinkmed.medications.connector.data.object.EncounterType;
import com.marand.thinkmed.medications.connector.data.object.MedicationsCentralCaseDto;
import com.marand.thinkmed.medications.connector.data.object.ObservationDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDemographicsDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDemographicsEncounterDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDto;
import com.marand.thinkmed.medications.connector.data.object.QuantityWithTimeDto;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Mitja Lapajne
 */
public class ProviderBasedMedicationsConnector implements MedicationsConnector
{
  private PatientDemographicsProvider patientDemographicsProvider;
  private EncounterProvider encounterProvider;
  private AllergiesProvider allergiesProvider;
  private DiseasesProvider diseasesProvider;
  private WeightProvider weightProvider;
  private HeightProvider heightProvider;
  private BloodGlucoseProvider bloodGlucoseProvider;

  @Required
  public void setPatientDemographicsProvider(final PatientDemographicsProvider patientDemographicsProvider)
  {
    this.patientDemographicsProvider = patientDemographicsProvider;
  }

  @Required
  public void setEncounterProvider(final EncounterProvider encounterProvider)
  {
    this.encounterProvider = encounterProvider;
  }

  @Required
  public void setAllergiesProvider(final AllergiesProvider allergiesProvider)
  {
    this.allergiesProvider = allergiesProvider;
  }

  @Required
  public void setDiseasesProvider(final DiseasesProvider diseasesProvider)
  {
    this.diseasesProvider = diseasesProvider;
  }

  @Required
  public void setWeightProvider(final WeightProvider weightProvider)
  {
    this.weightProvider = weightProvider;
  }

  @Required
  public void setHeightProvider(final HeightProvider heightProvider)
  {
    this.heightProvider = heightProvider;
  }

  @Required
  public void setBloodGlucoseProvider(final BloodGlucoseProvider bloodGlucoseProvider)
  {
    this.bloodGlucoseProvider = bloodGlucoseProvider;
  }

  @Override
  public PatientDataForMedicationsDto getPatientData(
      @Nonnull final String patientId, @Nonnull final DateTime when)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");
    Preconditions.checkNotNull(when, "when must not be null");

    final PatientDto patientData = getPatientData(patientId);
    final List<ExternalCatalogDto> diseases = patientData.getDiseases()
        .stream()
        .map(d -> new ExternalCatalogDto(d.getId(), d.getName(), d.getId()))
        .collect(Collectors.toList());

    final List<NamedExternalDto> allergies = patientData.getAllergies().getAllergies()
        .stream()
        .map(a -> new NamedExternalDto(a.getAllergen().getId(), a.getAllergen().getName()))
        .collect(Collectors.toList());

    MedicationsCentralCaseDto centralCaseDto = null;
    final EncounterDto encounter = patientData.getEncounter();
    if (encounter != null)
    {
      centralCaseDto = new MedicationsCentralCaseDto();
      centralCaseDto.setCareProvider(encounter.getWard());
      centralCaseDto.setCentralCaseEffective(new Interval(encounter.getStart(), encounter.getEnd()));
      centralCaseDto.setCentralCaseId(encounter.getId());
      centralCaseDto.setOutpatient(encounter.getType() == EncounterType.OUTPATIENT);
    }
    return new PatientDataForMedicationsDto(
        patientData.getDemographics().getBirthDate(),
        patientData.getWeight() != null ? patientData.getWeight().getValue() : null,
        patientData.getHeight() != null ? patientData.getHeight().getValue() : null,
        patientData.getDemographics().getGender(),
        diseases,
        allergies,
        centralCaseDto);
  }

  @Override
  public PatientDataForTherapyReportDto getPatientDataForTherapyReport(
      final String patientId, final boolean mainDiseaseTypeOnly, final DateTime when, final Locale locale)
  {
    return null;
  }

  @Override
  public Interval getLastDischargedCentralCaseEffectiveInterval(final String patientId)
  {
    return null;
  }

  @Override
  public byte[] getPdfDocument(final String reference)
  {
    return new byte[0];
  }

  @Override
  public List<NamedExternalDto> getCurrentUserCareProviders()
  {
    return new ArrayList<>();
  }

  @Override
  public Map<String, PatientDisplayWithLocationDto> getPatientDisplayWithLocationMap(
      final Collection<String> careProviderIds, final Collection<String> patientIds)
  {
    final List<PatientDemographicsEncounterDto> patientsEncounters = getPatientsEncounters(patientIds);
    final Map<String, PatientDisplayWithLocationDto> patientsMap = new HashMap<>();
    patientsEncounters.forEach(
        p ->
        {
          final PatientDemographicsDto demographics = p.getPatientDemographics();
          final PatientDisplayDto patientDisplayDto = new PatientDisplayDto(
              demographics.getId(),
              demographics.getName(),
              demographics.getBirthDate(),
              demographics.getGender(),
              null);
          patientsMap.put(demographics.getId(), new PatientDisplayWithLocationDto(
              patientDisplayDto,
              Opt.of(p.getEncounter()).map(e -> e.getWard().getName()).orElse(null),
              Opt.of(p.getEncounter()).map(EncounterDto::getLocation).orElse(null)));
        }
    );

    return patientsMap;
  }

  @Override
  public boolean assertPasswordForUsername(final String username, final String password)
  {
    return true;
  }

  @Override
  public List<QuantityWithTimeDto> getBloodSugarObservations(
      @Nonnull final String patientId, @Nonnull final Interval interval)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");
    Preconditions.checkNotNull(interval, "interval must not be null");

    return getPatientBloodGlucoseMeasurements(patientId, interval).stream()
        .map(m -> new QuantityWithTimeDto(m.getTimestamp(), m.getValue(), m.getComment()))
        .collect(Collectors.toList());
  }

  //TODO expose it in MedicationsConnector and replace old methods with this one
  private List<PatientDemographicsEncounterDto> getPatientsEncounters(@Nonnull final Collection<String> patientsIds)
  {
    Preconditions.checkNotNull(patientsIds, "patientIds is required");

    final List<PatientDemographicsEncounterDto> demographicsEncounters = new ArrayList<>();
    if (!patientsIds.isEmpty())
    {
      final List<PatientDemographicsDto> patientsDemographics =
          patientDemographicsProvider.getPatientsDemographics(patientsIds);
      final Map<String, PatientDemographicsDto> patientsDemographicsMap = patientsDemographics.stream()
          .collect(Collectors.toMap(ExternalIdentityDto::getId, p -> p));

      final List<EncounterDto> encounters = encounterProvider.getPatientsActiveEncounters(patientsIds);
      final Map<String, EncounterDto> patientsEncountersMap = encounters.stream()
          .collect(Collectors.toMap(ExternalIdentityDto::getId, p -> p));

      patientsIds.forEach(
          patientId ->
              demographicsEncounters.add(
                  new PatientDemographicsEncounterDto(
                      patientsDemographicsMap.get(patientId),
                      patientsEncountersMap.get(patientId))));
    }
    return demographicsEncounters;
  }

  //TODO expose it in MedicationsConnector and replace old methods with this one
  private PatientDto getPatientData(@Nonnull final String patientId)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");

    final PatientDto patient = new PatientDto();
    final List<PatientDemographicsDto> patientsDemographics =
        patientDemographicsProvider.getPatientsDemographics(Collections.singleton(patientId));
    patient.setDemographics(CollectionUtils.getFirstOrNull(patientsDemographics));

    final List<EncounterDto> encounters = encounterProvider.getPatientsActiveEncounters(Collections.singleton(patientId));
    patient.setEncounter(CollectionUtils.getFirstOrNull(encounters));

    patient.setAllergies(allergiesProvider.getPatientAllergies(patientId));
    patient.setDiseases(diseasesProvider.getPatientDiseases(patientId));
    patient.setWeight(weightProvider.getPatientWeight(patientId).get());
    patient.setHeight(heightProvider.getPatientHeight(patientId).get());
    return patient;
  }

  //TODO expose it in MedicationsConnector and replace old methods with this one
  private List<ObservationDto> getPatientBloodGlucoseMeasurements(
      @Nonnull final String patientId, @Nonnull final Interval interval)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");
    Preconditions.checkNotNull(interval, "interval is required");

    return bloodGlucoseProvider.getPatientBloodGlucoseMeasurements(patientId, interval);
  }
}
