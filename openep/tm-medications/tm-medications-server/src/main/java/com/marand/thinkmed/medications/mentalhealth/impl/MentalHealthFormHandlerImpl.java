package com.marand.thinkmed.medications.mentalhealth.impl;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.ispek.ehr.common.tdo.CompositionEventContext;
import com.marand.maf.core.openehr.visitor.IspekTdoDataSupport;
import com.marand.openehr.medications.tdo.MedicationConsentFormComposition;
import com.marand.openehr.medications.tdo.MedicationConsentFormComposition.MedicationListSection;
import com.marand.openehr.medications.tdo.MedicationConsentFormComposition.MedicationListSection.MedicationConsentItemAdminEntry;
import com.marand.openehr.medications.tdo.MedicationConsentFormComposition.MedicationListSection.MedicationConsentItemAdminEntry.MedicationItemCluster;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentType;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthMedicationDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTemplateDto;
import com.marand.thinkmed.medications.mentalhealth.MentalHealthFormHandler;
import com.marand.thinkmed.medications.task.MedicationsTasksHandler;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.ArchetypeId;
import org.openehr.jaxb.rm.Archetyped;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvQuantity;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Nejc Korasa
 */
public class MentalHealthFormHandlerImpl implements MentalHealthFormHandler
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private MedicationsTasksHandler medicationsTasksHandler;

  @Required
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Required
  public void setMedicationsTasksHandler(final MedicationsTasksHandler medicationsTasksHandler)
  {
    this.medicationsTasksHandler = medicationsTasksHandler;
  }

  @Override
  public void saveNewMentalHealthForm(
      @Nonnull final MentalHealthDocumentDto mentalHealthDocumentDto,
      final NamedExternalDto careProvider,
      @Nonnull final DateTime when)
  {
    Preconditions.checkNotNull(mentalHealthDocumentDto, "mentalHealthDocumentDto must not be null!");
    Preconditions.checkNotNull(when, "when must not be null!");

    final boolean medicationListExists = !mentalHealthDocumentDto.getMentalHealthMedicationDtoList().isEmpty()
        || !mentalHealthDocumentDto.getMentalHealthTemplateDtoList().isEmpty();

    Preconditions.checkNotNull(when, "Save time is required");
    Preconditions.checkNotNull(mentalHealthDocumentDto.getPatientId(), "Patient id is required");
    Preconditions.checkNotNull(mentalHealthDocumentDto.getMentalHealthDocumentType(), "Report type is required");
    Preconditions.checkArgument(medicationListExists, "Medication or template list is required");

    final MedicationConsentFormComposition medicationConsentFormComposition = buildMedicationConsentFormComposition(
        mentalHealthDocumentDto,
        careProvider,
        when);

    medicationsTasksHandler.createCheckMentalHealthMedsTask(mentalHealthDocumentDto.getPatientId(), when);

    medicationsOpenEhrDao.saveComposition(
        mentalHealthDocumentDto.getPatientId(),
        medicationConsentFormComposition,
        null);
  }

  MedicationConsentFormComposition buildMedicationConsentFormComposition(
      final MentalHealthDocumentDto mentalHealthDocumentDto,
      final NamedExternalDto careProvider,
      final DateTime when)
  {
    final MedicationConsentFormComposition composition = new MedicationConsentFormComposition();
    setConsentTypeAndBnfCumulativeMaximum(mentalHealthDocumentDto, composition);
    setMedicationListItems(mentalHealthDocumentDto, composition);
    setConsentFormCompositionContext(careProvider, when, composition);
    return composition;
  }

  private void setConsentTypeAndBnfCumulativeMaximum(
      final MentalHealthDocumentDto mentalHealthDocumentDto,
      final MedicationConsentFormComposition composition)
  {
    composition.setMedicationConsent(new MedicationConsentFormComposition.MedicationConsentAdminEntry());

    composition.getMedicationConsent().setConsentTypeEnum(
        mentalHealthDocumentDto.getMentalHealthDocumentType() == MentalHealthDocumentType.T2
        ? MedicationConsentFormComposition.MedicationConsentAdminEntry.ConsentType.FORM_T2
        : MedicationConsentFormComposition.MedicationConsentAdminEntry.ConsentType.FORM_T3);

    if (mentalHealthDocumentDto.getBnfMaximum() != null)
    {
      final DvQuantity dvQuantity = new DvQuantity();
      dvQuantity.setUnits("%");
      dvQuantity.setMagnitude(mentalHealthDocumentDto.getBnfMaximum());

      composition.getMedicationConsent().setMaximumCumulativeDose(dvQuantity);
    }

  }

  private void setMedicationListItems(
      final MentalHealthDocumentDto mentalHealthDocumentDto,
      final MedicationConsentFormComposition composition)
  {
    final MedicationListSection medicationListSection = new MedicationListSection();
    final List<MedicationConsentItemAdminEntry> medicationConsentItemAdminEntries = new ArrayList<>();

    fillMentalHealthDrugs(mentalHealthDocumentDto, medicationConsentItemAdminEntries);
    fillMentalHealthTemplates(mentalHealthDocumentDto, medicationConsentItemAdminEntries);

    medicationListSection.setMedicationConsentItem(medicationConsentItemAdminEntries);
    composition.setMedicationList(medicationListSection);
  }

  private void fillMentalHealthTemplates(
      final MentalHealthDocumentDto mentalHealthDocumentDto,
      final List<MedicationConsentItemAdminEntry> medicationConsentItemAdminEntries)
  {
    for (final MentalHealthTemplateDto mentalHealthTemplateDto : mentalHealthDocumentDto.getMentalHealthTemplateDtoList())
    {
      final MedicationConsentItemAdminEntry itemAdminEntry = new MedicationConsentItemAdminEntry();
      itemAdminEntry.setMedicationItem(createMedicationGroupItem(mentalHealthTemplateDto));

      if (mentalHealthTemplateDto.getRoute() != null)
      {
        itemAdminEntry.setRoute(createRouteItem(
            mentalHealthTemplateDto.getRoute().getId(),
            mentalHealthTemplateDto.getRoute().getName()));
      }
      medicationConsentItemAdminEntries.add(itemAdminEntry);
    }
  }

  private void fillMentalHealthDrugs(
      final MentalHealthDocumentDto mentalHealthDocumentDto,
      final List<MedicationConsentItemAdminEntry> medicationConsentItemAdminEntries)
  {
    for (final MentalHealthMedicationDto mentalHealthMedicationDto : mentalHealthDocumentDto.getMentalHealthMedicationDtoList())
    {
      final MedicationConsentItemAdminEntry itemAdminEntry = new MedicationConsentItemAdminEntry();
      itemAdminEntry.setMedicationItem(createMedicationItem(mentalHealthMedicationDto));

      if (mentalHealthMedicationDto.getRoute() != null)
      {
        itemAdminEntry.setRoute(
            createRouteItem(mentalHealthMedicationDto.getRoute().getId(),
                            mentalHealthMedicationDto.getRoute().getName()));
      }
      medicationConsentItemAdminEntries.add(itemAdminEntry);
    }
  }

  private MedicationItemCluster createMedicationItem(final MentalHealthMedicationDto mentalHealthMedicationDto)
  {
    final MedicationItemCluster medicationItemCluster = new MedicationItemCluster();

    medicationItemCluster.setTypeEnum(MedicationItemCluster.Type.MEDICATION);
    medicationItemCluster.setName(DataValueUtils.getLocalCodedText(
        String.valueOf(mentalHealthMedicationDto.getId()),
        mentalHealthMedicationDto.getName()));

    // TODO nejc save somewhere else - NEW TEMPLATE! name field is not ok - reserved for something else
    final Archetyped archetypeDetails = new Archetyped();
    archetypeDetails.setRmVersion(mentalHealthMedicationDto.getId() + "_" + mentalHealthMedicationDto.getName());

    final ArchetypeId value = new ArchetypeId();
    value.setValue(String.valueOf(mentalHealthMedicationDto.getId()));
    archetypeDetails.setArchetypeId(value);

    medicationItemCluster.setArchetypeDetails(archetypeDetails);
    return medicationItemCluster;
  }

  private MedicationItemCluster createMedicationGroupItem(final MentalHealthTemplateDto mentalHealthTemplateDto)
  {
    final MedicationItemCluster medicationItemCluster = new MedicationItemCluster();

    medicationItemCluster.setTypeEnum(MedicationItemCluster.Type.MEDICATION_GROUPE);
    medicationItemCluster.setName(DataValueUtils.getLocalCodedText(
        String.valueOf(mentalHealthTemplateDto.getId()),
        mentalHealthTemplateDto.getName()));

    // TODO nejc save somewhere else - NEW TEMPLATE! name field is not ok - reserved for something else
    final Archetyped archetypeDetails = new Archetyped();
    archetypeDetails.setRmVersion(mentalHealthTemplateDto.getId() + "_" + mentalHealthTemplateDto.getName());

    final ArchetypeId value = new ArchetypeId();
    value.setValue(String.valueOf(mentalHealthTemplateDto.getId()));
    archetypeDetails.setArchetypeId(value);

    medicationItemCluster.setArchetypeDetails(archetypeDetails);
    return medicationItemCluster;
  }

  private DvCodedText createRouteItem(final long routeCode, final String routeName)
  {
    return DataValueUtils.getLocalCodedText(String.valueOf(routeCode), routeName);
  }

  private void setConsentFormCompositionContext(
      final NamedExternalDto careProvider,
      final DateTime when,
      final MedicationConsentFormComposition composition)
  {
    final CompositionEventContext context = IspekTdoDataSupport.getEventContext(CompositionEventContext.class, when);
    composition.setCompositionEventContext(context);

    if (careProvider != null)
    {
      final CompositionEventContext.ContextDetailCluster contextDetailCluster = new CompositionEventContext.ContextDetailCluster();
      contextDetailCluster.setDepartmentalPeriodOfCareIdentifier(DataValueUtils.getLocalCodedText(
          careProvider.getId(),
          careProvider.getName()));

      context.getContextDetail().add(contextDetailCluster);
    }

    MedicationsEhrUtils.visitComposition(composition, when);
  }
}
