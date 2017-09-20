package com.marand.thinkmed.medications.mentalhealth.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.ispek.ehr.common.tdo.CompositionEventContext;
import com.marand.maf.core.Opt;
import com.marand.maf.core.valueholder.ValueHolder;
import com.marand.openehr.medications.tdo.MedicationConsentFormComposition;
import com.marand.openehr.medications.tdo.MedicationConsentFormComposition.MedicationConsentAdminEntry.ConsentType;
import com.marand.openehr.medications.tdo.MedicationConsentFormComposition.MedicationListSection.MedicationConsentItemAdminEntry;
import com.marand.openehr.medications.tdo.MedicationConsentFormComposition.MedicationListSection.MedicationConsentItemAdminEntry.MedicationItemCluster;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.MedicationHolderDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentType;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthMedicationDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTemplateDto;
import com.marand.thinkmed.medications.mentalhealth.MentalHealthFormProvider;
import org.joda.time.Interval;
import org.openehr.jaxb.rm.Archetyped;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.PartyIdentified;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Nejc Korasa
 */
public class MentalHealthFormProviderImpl implements MentalHealthFormProvider
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private ValueHolder<Map<Long, MedicationHolderDto>> medicationsValueHolder;
  private ValueHolder<Map<Long, MedicationRouteDto>> medicationRoutesValueHolder;

  @Required
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Required
  public void setMedicationsValueHolder(final ValueHolder<Map<Long, MedicationHolderDto>> medicationsValueHolder)
  {
    this.medicationsValueHolder = medicationsValueHolder;
  }

  @Required
  public void setMedicationRoutesValueHolder(final ValueHolder<Map<Long, MedicationRouteDto>> medicationRoutesValueHolder)
  {
    this.medicationRoutesValueHolder = medicationRoutesValueHolder;
  }

  @Override
  public Opt<MentalHealthDocumentDto> getLatestMentalHealthDocument(@Nonnull final String patientId)
  {
    Preconditions.checkNotNull(patientId, "patientId must not be null");

    return medicationsOpenEhrDao
        .findLatestConsentFormComposition(patientId)
        .map(form -> buildMentalHealthDocumentDto(form, patientId));
  }

  @Override
  public MentalHealthDocumentDto getMentalHealthDocument(
      @Nonnull final String patientId,
      @Nonnull final String compositionUId)
  {
    Preconditions.checkNotNull(patientId, "patientId must not be null");
    Preconditions.checkNotNull(compositionUId, "compositionUId must not be null");

    return buildMentalHealthDocumentDto(medicationsOpenEhrDao.loadConsentFormComposition(patientId, compositionUId), patientId);
  }

  @Override
  public Collection<MentalHealthDocumentDto> getMentalHealthDocuments(
      @Nonnull final String patientId,
      final Interval interval,
      final Integer fetchCount)
  {
    Preconditions.checkNotNull(patientId, "patientId must not be null");

    return medicationsOpenEhrDao
        .findMedicationConsentFormCompositions(patientId, interval, fetchCount)
        .stream()
        .map(form -> buildMentalHealthDocumentDto(form, patientId))
        .collect(Collectors.toList());
  }

  private MentalHealthDocumentDto buildMentalHealthDocumentDto(
      final MedicationConsentFormComposition composition,
      final String patientId)
  {
    final Set<MentalHealthMedicationDto> mentalHealthMedications = new HashSet<>();
    final Set<MentalHealthTemplateDto> mentalHealthTemplates = new HashSet<>();
    if (composition.getMedicationList() != null)
    {
      for (final MedicationConsentItemAdminEntry entry : composition.getMedicationList().getMedicationConsentItem())
      {
        if (entry.getMedicationItem().getTypeEnum() == MedicationItemCluster.Type.MEDICATION_GROUPE)
        {
          mentalHealthTemplates.add(buildMentalHealthTemplateDto(entry, getRoute(entry)));
        }
        else if (entry.getMedicationItem().getTypeEnum() == MedicationItemCluster.Type.MEDICATION)
        {
          mentalHealthMedications.add(buildMentalHealthMedicationDto(entry, getRoute(entry)));
        }
        else
        {
          throw new IllegalArgumentException("Type " + entry.getMedicationItem().getTypeEnum() + " is not supported!");
        }
      }
    }

    final String composerId = composition.getComposer().getExternalRef().getId().getValue();
    final String composerName = ((PartyIdentified)composition.getComposer()).getName();

    return new MentalHealthDocumentDto(
        composition.getUid().getValue(),
        DataValueUtils.getDateTime(composition.getCompositionEventContext().getStartTime()),
        new NamedExternalDto(composerId, composerName),
        patientId,
        getCareProvider(composition),
        getDocumentType(composition),
        getBnfMaximum(composition),
        mentalHealthMedications,
        mentalHealthTemplates);
  }

  private NamedExternalDto getCareProvider(final MedicationConsentFormComposition composition)
  {
    final List<CompositionEventContext.ContextDetailCluster> contextDetail = composition
        .getCompositionEventContext()
        .getContextDetail();

    if (!contextDetail.isEmpty())
    {
      final DvCodedText careProvider = (DvCodedText)contextDetail.get(0).getDepartmentalPeriodOfCareIdentifier();
      return new NamedExternalDto(careProvider.getDefiningCode().getCodeString(), careProvider.getValue());
    }

    return null;
  }

  private MentalHealthDocumentType getDocumentType(final MedicationConsentFormComposition composition)
  {
    final ConsentType consentTypeEnum = composition.getMedicationConsent().getConsentTypeEnum();
    if (consentTypeEnum != null)
    {
      return consentTypeEnum == ConsentType.FORM_T2 ? MentalHealthDocumentType.T2 : MentalHealthDocumentType.T3;
    }
    return null;
  }

  private Integer getBnfMaximum(final MedicationConsentFormComposition composition)
  {
    final DvQuantity maximumCumulativeDose = composition.getMedicationConsent().getMaximumCumulativeDose();
    if (maximumCumulativeDose != null)
    {
      //noinspection NumericCastThatLosesPrecision
      return (int)maximumCumulativeDose.getMagnitude();
    }
    return null;
  }

  private MedicationRouteDto getRoute(final MedicationConsentItemAdminEntry itemAdminEntry)
  {
    if (itemAdminEntry.getRoute() != null && itemAdminEntry.getRoute() instanceof DvCodedText)
    {
      final DvCodedText route = (DvCodedText)itemAdminEntry.getRoute();
      return medicationRoutesValueHolder.getValue().get(Long.valueOf(route.getDefiningCode().getCodeString()));
    }
    return null;
  }

  private MentalHealthTemplateDto buildMentalHealthTemplateDto(
      final MedicationConsentItemAdminEntry itemAdminEntry,
      final MedicationRouteDto route)
  {
    // TODO nejc save somewhere else - NEW TEMPLATE! name field is not ok - reserved for something else
    //final DvCodedText name = (DvCodedText)itemAdminEntry.getMedicationItem().getName();
    final Archetyped archetypeDetails = itemAdminEntry.getMedicationItem().getArchetypeDetails();

    final String mentalHealthTemplate = archetypeDetails.getRmVersion();
    //final String mentalHealthTemplateName = name.getValue();

    //noinspection DynamicRegexReplaceableByCompiledPattern
    final String[] split = mentalHealthTemplate.split("_"); // temporary, see comment above

    //mentalHealthTemplateDto.setName(mentalHealthTemplateName);

    return new MentalHealthTemplateDto(Long.valueOf(split[0]), split[1], route);
  }

  private MentalHealthMedicationDto buildMentalHealthMedicationDto(
      final MedicationConsentItemAdminEntry itemAdminEntry,
      final MedicationRouteDto route)
  {
    // TODO nejc save somewhere else - NEW TEMPLATE! name field is not ok - reserved for something else
    //final DvCodedText name = (DvCodedText)itemAdminEntry.getMedicationItem().getName();
    final Archetyped archetypeDetails = itemAdminEntry.getMedicationItem().getArchetypeDetails();

    final String mentalHealthMedication = archetypeDetails.getRmVersion();
    //final String mentalHealthMedicationName = name.getValue();

    //noinspection DynamicRegexReplaceableByCompiledPattern
    final String[] split = mentalHealthMedication.split("_"); // temporary, see comment above

    //mentalHealthMedicationDto.setName(mentalHealthMedicationName);

    final MedicationHolderDto medicationHolderDto = medicationsValueHolder.getValue().get(Long.valueOf(split[0]));
    return new MentalHealthMedicationDto(
        medicationHolderDto.getId(),
        medicationHolderDto.getShortName(),
        medicationHolderDto.getGenericName(),
        route);
  }
}
