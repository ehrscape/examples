package com.marand.thinkmed.fdb.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.data.object.NamedIdentityDto;
import com.marand.thinkmed.fdb.dto.FdbDrugSensitivityWarningDto;
import com.marand.thinkmed.fdb.dto.FdbEnums;
import com.marand.thinkmed.fdb.dto.FdbPatientChecksWarningDto;
import com.marand.thinkmed.fdb.dto.FdbPatientDto;
import com.marand.thinkmed.fdb.dto.FdbScreeningDto;
import com.marand.thinkmed.fdb.dto.FdbScreeningResultDto;
import com.marand.thinkmed.fdb.dto.FdbTerminologyDto;
import com.marand.thinkmed.fdb.dto.FdbWarningDto;
import com.marand.thinkmed.fdb.rest.FdbRestService;
import com.marand.thinkmed.medicationsexternal.dto.DoseRangeCheckDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationForWarningsSearchDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationsWarningDto;
import com.marand.thinkmed.medicationsexternal.plugin.MedicationExternalDataPlugin;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.access.annotation.Secured;

/**
 * @author Mitja Lapajne
 */
@Secured("ROLE_User")
public class FdbServiceImpl implements MedicationExternalDataPlugin, InitializingBean
{
  private FdbRestService service;
  private String host;
  private Integer port;
  private String username;
  private String password;
  private String restUri;

  public void setHost(final String host)
  {
    this.host = host;
  }

  public void setPort(final Integer port)
  {
    this.port = port;
  }

  public void setUsername(final String username)
  {
    this.username = username;
  }

  public void setPassword(final String password)
  {
    this.password = password;
  }

  public void setRestUri(final String restUri)
  {
    this.restUri = restUri;
  }

  @Override
  public void afterPropertiesSet() throws Exception
  {
    final HttpClientContext context = HttpClientContext.create();
    final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(new AuthScope(host, port), new UsernamePasswordCredentials(username, password));
    context.setCredentialsProvider(credentialsProvider);

    final ResteasyClient client = new ResteasyClientBuilder().httpEngine(
        new ApacheHttpClient4Engine(
            HttpClientBuilder.create().setConnectionManager(
                new PoolingHttpClientConnectionManager()).build(), context)).build();
    final ResteasyWebTarget target = client.target(restUri);
    service = target.proxy(FdbRestService.class);
  }

  @Override
  public void reloadCache()
  {
  }

  @Override
  public List<DoseRangeCheckDto> findDoseRangeChecks(final String externalId)
  {
    return null;
  }

  @Override
  public List<MedicationsWarningDto> findMedicationWarnings(
      final long patientAgeInDays,
      final Double patientWeightInKg,
      final Integer gabInWeeks,
      final Double bsaInM2,
      final boolean isFemale,
      final List<String> diseaseTypeCodes,
      final List<String> allergiesExternalValues,
      final List<MedicationForWarningsSearchDto> medicationSummaries)
  {
    final FdbScreeningDto screeningDto =
        buildFdbScreeningDto(
            patientAgeInDays,
            patientWeightInKg,
            bsaInM2,
            isFemale,
            medicationSummaries,
            allergiesExternalValues);
    final String json = JsonUtil.toJson(screeningDto);
    final String warningJson = service.scanForWarnings(json);
    final FdbScreeningResultDto resultDto = JsonUtil.fromJson(warningJson, FdbScreeningResultDto.class);

    final List<MedicationsWarningDto> warnings = new ArrayList<>();
    fillWarnings(resultDto.getDrugInteractions(), warnings);
    fillSensitivityWarnings(resultDto.getDrugSensitivities(), warnings);
    fillWarnings(resultDto.getDuplicateTherapies(), warnings);
    fillWarnings(resultDto.getDrugDoublings(), warnings);
    fillWarnings(resultDto.getDrugEquivalences(), warnings);
    fillPatientChecksWarnings(resultDto.getPatientChecks(), warnings);

    sortWarningsBySeverity(warnings);
    return warnings;
  }

  private void sortWarningsBySeverity(final List<MedicationsWarningDto> warnings)
  {
    Collections.sort(
        warnings, new Comparator<MedicationsWarningDto>()
        {
          @Override
          public int compare(
              final MedicationsWarningDto o1, final MedicationsWarningDto o2)
          {
            if (o1.getSeverity() == null && o2.getSeverity() == null)
            {
              return 0;
            }
            if (o2.getSeverity() == null)
            {
              return -1;
            }
            if (o1.getSeverity() == null)
            {
              return 1;
            }
            return o2.getSeverity().compareTo(o1.getSeverity());
          }
        });
  }

  private void fillWarnings(final List<FdbWarningDto> fdbWarnings, final List<MedicationsWarningDto> warnings)
  {
    if (fdbWarnings != null)
    {
      for (final FdbWarningDto fdbWarning : fdbWarnings)
      {
        final MedicationsWarningDto warning = new MedicationsWarningDto();
        warning.setDescription(fdbWarning.getFullAlertMessage());
        if (fdbWarning.getAlertSeverity() != null)
        {
          final MedicationsWarningDto.Severity severity = getSeverity(fdbWarning.getAlertSeverity().getValue());
          warning.setSeverity(severity);
        }

        if (fdbWarning.getPrimaryDrug() != null)
        {
          final NamedIdentityDto primaryMedication = new NamedIdentityDto();
          primaryMedication.setId(fdbWarning.getPrimaryDrug().getId());
          primaryMedication.setName(fdbWarning.getPrimaryDrug().getName());
          warning.setPrimaryMedication(primaryMedication);
        }
        if (fdbWarning.getSecondaryDrug() != null)
        {
          final NamedIdentityDto secondaryMedication = new NamedIdentityDto();
          secondaryMedication.setId(fdbWarning.getSecondaryDrug().getId());
          secondaryMedication.setName(fdbWarning.getSecondaryDrug().getName());
          warning.setSecondaryMedication(secondaryMedication);
        }
        warnings.add(warning);
      }
    }
  }

  private void fillSensitivityWarnings(
      final List<FdbDrugSensitivityWarningDto> fdbWarnings,
      final List<MedicationsWarningDto> warnings)
  {
    if (fdbWarnings != null)
    {
      for (final FdbDrugSensitivityWarningDto fdbWarning : fdbWarnings)
      {
        final MedicationsWarningDto warning = new MedicationsWarningDto();
        warning.setDescription(fdbWarning.getFullAlertMessage());

        if (fdbWarning.getDrug() != null)
        {
          final NamedIdentityDto primaryMedication = new NamedIdentityDto();
          primaryMedication.setId(fdbWarning.getDrug().getId());
          primaryMedication.setName(fdbWarning.getDrug().getName());
          warning.setPrimaryMedication(primaryMedication);
        }

        warnings.add(warning);
      }
    }
  }

  private void fillPatientChecksWarnings(
      final List<FdbPatientChecksWarningDto> fdbWarnings, final List<MedicationsWarningDto> warnings)
  {
    if (fdbWarnings != null)
    {
      for (final FdbPatientChecksWarningDto fdbWarning : fdbWarnings)
      {
        final MedicationsWarningDto warning = new MedicationsWarningDto();
        warning.setDescription(fdbWarning.getFullAlertMessage());

        if (fdbWarning.getDrug() != null)
        {
          final NamedIdentityDto primaryMedication = new NamedIdentityDto();
          primaryMedication.setId(fdbWarning.getDrug().getId());
          primaryMedication.setName(fdbWarning.getDrug().getName());
          warning.setPrimaryMedication(primaryMedication);
        }

        warnings.add(warning);
      }
    }
  }

  private FdbScreeningDto buildFdbScreeningDto(
      final long patientAgeInDays,
      final Double patientWeightInKg,
      final Double bsaInM2,
      final boolean isFemale,
      final List<MedicationForWarningsSearchDto> medicationSummaries,
      final List<String> allergiesExternalValues)
  {
    final FdbScreeningDto screeningDto = new FdbScreeningDto();

    screeningDto.getScreeningModules().add(4);
    screeningDto.getScreeningModules().add(64);
    screeningDto.getScreeningModules().add(16);
    screeningDto.getScreeningModules().add(2);
    screeningDto.getScreeningModules().add(1);
    screeningDto.getScreeningModules().add(32);
    screeningDto.getScreeningModules().add(128);

    final FdbPatientDto patientInformation = new FdbPatientDto();
    patientInformation.setAge(patientAgeInDays);
    patientInformation.setWeight(patientWeightInKg);
    patientInformation.setBodySurfaceArea(bsaInM2 != null ? bsaInM2 : 1.0); //TODO Mitja
    patientInformation.setGender(isFemale ? FdbEnums.GENDER_FEMALE.getNameValue() : FdbEnums.GENDER_MALE.getNameValue());
    screeningDto.setPatientInformation(patientInformation);

    fillMedicationsToScreeningDto(screeningDto, medicationSummaries);
    fillAllergiesToScreeningDto(screeningDto, allergiesExternalValues);

    screeningDto.setCheckAllDrugs(true);
    screeningDto.setValidateInput(true);

    screeningDto.setMinimumConditionAlertSeverity(FdbEnums.MINIMUM_CONDITION_ALERT_SEVERITY_CONTRAINDICATION.getNameValue());
    screeningDto.setMinimumInteractionAlertSeverity(FdbEnums.MINIMUM_INTERACTION_ALERT_SEVERITY_SIGNIFICANT_RISK.getNameValue());
    return screeningDto;
  }

  private void fillMedicationsToScreeningDto(
      final FdbScreeningDto screeningDto,
      final List<MedicationForWarningsSearchDto> medicationSummaries)
  {
    for (final MedicationForWarningsSearchDto medication : medicationSummaries)
    {
      final FdbTerminologyDto drug = new FdbTerminologyDto();
      drug.setId(medication.getExternalId());
      drug.setName(medication.getDescription());
      drug.setTerminology(FdbEnums.SNOMED_TERMINOLOGY.getNameValue());
      drug.setConceptType(
          medication.isProduct() ? FdbEnums.PRODUCT_CONCEPT_TYPE.getNameValue() : FdbEnums.DRUG_CONCEPT_TYPE.getNameValue());

      if (medication.isProspective())
      {
        screeningDto.getProspectiveDrugs().add(drug);
      }
      else
      {
        screeningDto.getCurrentDrugs().add(drug);
      }
    }
  }

  private void fillAllergiesToScreeningDto(final FdbScreeningDto screeningDto, final List<String> allergiesExternalValues)
  {
    //for (final String allergenExternalId : allergiesExternalValues)
    //{
    //  final FdbTerminologyDto allergen = new FdbTerminologyDto();
    //  allergen.setId(allergenExternalId);
    //  //allergy.setName();
    //  allergen.setTerminology(FdbEnums.SNOMED_TERMINOLOGY.getNameValue());
    //  allergen.setConceptType(FdbEnums.SUBSTANCE_CONCEPT_TYPE.getNameValue());
    //  screeningDto.getAllergens().add(allergen);
    //}

    //TODO Mitja
    final FdbTerminologyDto allergen = new FdbTerminologyDto();
    allergen.setId("91936005");
    allergen.setName("penicillin");
    allergen.setTerminology(FdbEnums.SNOMED_TERMINOLOGY.getNameValue());
    allergen.setConceptType(FdbEnums.DRUG_CONCEPT_TYPE.getNameValue());
    screeningDto.getAllergens().add(allergen);
  }

  private MedicationsWarningDto.Severity getSeverity(final Long fdbSeverityId)
  {
    if (fdbSeverityId == 1L)
    {
      return MedicationsWarningDto.Severity.LOW;
    }
    if (fdbSeverityId == 2L)
    {
      return MedicationsWarningDto.Severity.MEDIUM;
    }
    if (fdbSeverityId == 3L)
    {
      return MedicationsWarningDto.Severity.SIGNIFICANT;
    }
    if (fdbSeverityId == 4L)
    {
      return MedicationsWarningDto.Severity.HIGH;
    }
    return null;
  }

  @Override
  public boolean isWarningsProvider()
  {
    return true;
  }

  @Override
  public boolean isMedicationOverviewProvider()
  {
    return false;
  }

  @Override
  public boolean isDoseRangeChecksProvider()
  {
    return false;
  }
}
