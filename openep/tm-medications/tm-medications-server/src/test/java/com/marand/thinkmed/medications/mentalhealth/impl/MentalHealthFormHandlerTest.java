package com.marand.thinkmed.medications.mentalhealth.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.marand.maf.core.Opt;
import com.marand.maf.core.security.remoting.GlobalAuditContext;
import com.marand.maf.core.service.ConstantUserMetadataProvider;
import com.marand.maf.core.service.RequestContextHolder;
import com.marand.maf.core.service.RequestContextImpl;
import com.marand.openehr.medications.tdo.MedicationConsentFormComposition;
import com.marand.openehr.medications.tdo.MedicationConsentFormComposition.MedicationListSection;
import com.marand.openehr.medications.tdo.MedicationConsentFormComposition.MedicationListSection.MedicationConsentItemAdminEntry.MedicationItemCluster;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentType;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthMedicationDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTemplateDto;
import com.marand.thinkmed.medications.task.MedicationsTasksHandler;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openehr.jaxb.rm.Composition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertFalse;

/**
 * @author Nejc Korasa
 */
@RunWith(MockitoJUnitRunner.class)
public class MentalHealthFormHandlerTest
{
  @InjectMocks
  private MentalHealthFormHandlerImpl mentalHealthFormHandler;

  @Mock
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Mock
  private MedicationsTasksHandler medicationsTasksHandler;

  @Before
  public void setUpMocks()
  {
    RequestContextHolder.clearContext();
    RequestContextHolder.setContext(
        new RequestContextImpl(
            1L,
            GlobalAuditContext.current(),
            new DateTime(),
            Opt.of(ConstantUserMetadataProvider.createMetadata("User"))));
  }

  @Test
  public void testSaveNewMentalHealthReport()
  {
    final List<MentalHealthMedicationDto> mentalHealthMedicationDtos = new ArrayList<>();
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(1L, 1));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(1L, 2));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(2L, 3));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(2L, 4));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(2L, 5));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(2L, 6));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(2L, 7));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(2L, 8));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(2L, 9));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(3L, 10));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(3L, 11));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(3L, 12));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(3L, 13));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(3L, 14));

    final List<MentalHealthTemplateDto> mentalHealthTemplateDtos = new ArrayList<>();
    mentalHealthTemplateDtos.add(createMentalHealthTemplateDto("template1", 1L, "oral", 1L));
    mentalHealthTemplateDtos.add(createMentalHealthTemplateDto("template2", 2L, "oral", 1L));
    mentalHealthTemplateDtos.add(createMentalHealthTemplateDto("template3", 3L, "oral", 1L));

    final DateTime when = DateTime.now();

    final MentalHealthDocumentDto mentalHealthDocumentDto = new MentalHealthDocumentDto(
        null,
        null,
        null,
        "2",
        null,
        MentalHealthDocumentType.T2,
        null,
        mentalHealthMedicationDtos,
        mentalHealthTemplateDtos);

    mentalHealthFormHandler.saveNewMentalHealthForm(mentalHealthDocumentDto, null, when);
  }

  @Test
  public void testBuildComposition()
  {
    final List<MentalHealthMedicationDto> mentalHealthMedicationDtos = new ArrayList<>();
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(1L, 1));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(1L, 2));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(2L, 3));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(2L, 4));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(2L, 5));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(2L, 6));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(2L, 7));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(2L, 8));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(2L, 9));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(3L, 10));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(3L, 11));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(3L, 12));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(3L, 13));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(3L, 14));

    final List<MentalHealthTemplateDto> mentalHealthTemplateDtos = new ArrayList<>();
    mentalHealthTemplateDtos.add(createMentalHealthTemplateDto("template1", 1L, "oral", 1L));
    mentalHealthTemplateDtos.add(createMentalHealthTemplateDto("template2", 2L, "oral", 1L));
    mentalHealthTemplateDtos.add(createMentalHealthTemplateDto("template3", 3L, "oral", 1L));

    final MentalHealthDocumentDto mentalHealthDocumentDto = new MentalHealthDocumentDto(
        null,
        null,
        null,
        "2",
        null,
        MentalHealthDocumentType.T2,
        150,
        mentalHealthMedicationDtos,
        mentalHealthTemplateDtos);

    final DateTime when = DateTime.now();

    final MedicationConsentFormComposition medicationConsentFormComposition = mentalHealthFormHandler.buildMedicationConsentFormComposition(
        mentalHealthDocumentDto,
        null,
        when);

    assertNotNull(medicationConsentFormComposition);
    assertEquals(
        MedicationConsentFormComposition.MedicationConsentAdminEntry.ConsentType.FORM_T2,
        medicationConsentFormComposition.getMedicationConsent().getConsentTypeEnum());

    assertEquals(
        150.0,
        medicationConsentFormComposition.getMedicationConsent().getMaximumCumulativeDose().getMagnitude());

    final List<MedicationListSection.MedicationConsentItemAdminEntry> medicationConsentItem = medicationConsentFormComposition
        .getMedicationList()
        .getMedicationConsentItem();

    assertFalse(medicationConsentItem.isEmpty());
    assertEquals(17, medicationConsentItem.size());

    int numberOfGroupeItems = 0;
    for (final MedicationListSection.MedicationConsentItemAdminEntry itemAdminEntry : medicationConsentItem)
    {
      if (itemAdminEntry.getMedicationItem()
          .getTypeEnum() == MedicationItemCluster.Type.MEDICATION_GROUPE)
      {
        numberOfGroupeItems++;
      }
    }

    assertEquals(3, numberOfGroupeItems);
  }

  @Test
  public void testBuildComposition2()
  {
    final List<MentalHealthTemplateDto> mentalHealthTemplateDtos = new ArrayList<>();
    mentalHealthTemplateDtos.add(createMentalHealthTemplateDto("template1", 1L, "oral", 1L));
    mentalHealthTemplateDtos.add(createMentalHealthTemplateDto("template2", 2L, "oral", 1L));

    final DateTime when = DateTime.now();

    final MentalHealthDocumentDto mentalHealthDocumentDto = new MentalHealthDocumentDto(
        null,
        null,
        null,
        "2",
        null,
        MentalHealthDocumentType.T3,
        null,
        Collections.emptyList(),
        mentalHealthTemplateDtos);

    final MedicationConsentFormComposition medicationConsentFormComposition = mentalHealthFormHandler.buildMedicationConsentFormComposition(
        mentalHealthDocumentDto,
        null,
        when);

    assertNotNull(medicationConsentFormComposition);
    assertEquals(
        MedicationConsentFormComposition.MedicationConsentAdminEntry.ConsentType.FORM_T3,
        medicationConsentFormComposition.getMedicationConsent().getConsentTypeEnum());

    assertNull(medicationConsentFormComposition.getMedicationConsent().getMaximumCumulativeDose());

    final List<MedicationListSection.MedicationConsentItemAdminEntry> medicationConsentItem = medicationConsentFormComposition
        .getMedicationList()
        .getMedicationConsentItem();

    assertFalse(medicationConsentItem.isEmpty());

    int numberOfGroupeItems = 0;
    for (final MedicationListSection.MedicationConsentItemAdminEntry itemAdminEntry : medicationConsentItem)
    {
      if (itemAdminEntry.getMedicationItem()
          .getTypeEnum() == MedicationItemCluster.Type.MEDICATION_GROUPE)
      {
        numberOfGroupeItems++;
      }
    }

    assertEquals(2, numberOfGroupeItems);
  }

  private MentalHealthMedicationDto createMentalHealthDrugDto(final long routeId, final long medicationId)
  {
    final MedicationRouteDto route = new MedicationRouteDto();
    route.setId(routeId);
    route.setName(String.valueOf(routeId));

    return new MentalHealthMedicationDto(medicationId, "medication name", "generic name", route);
  }

  private MentalHealthTemplateDto createMentalHealthTemplateDto(
      final String templateName,
      final Long templateId,
      final String routeName,
      final Long routeId)
  {
    final MedicationRouteDto route = new MedicationRouteDto();
    route.setId(routeId);
    route.setName(routeName);

    return  new MentalHealthTemplateDto(templateId, templateName, route);
  }
}