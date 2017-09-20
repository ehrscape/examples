package com.marand.thinkmed.medications.document.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.document.TherapyDocumentProviderPlugin;
import com.marand.thinkmed.medications.dto.document.TherapyDocumentDto;
import com.marand.thinkmed.medications.dto.document.TherapyDocumentTypeEnum;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentType;
import com.marand.thinkmed.medications.mentalhealth.MentalHealthFormProvider;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;

/**
 * @author Nejc Korasa
 */
@RunWith(MockitoJUnitRunner.class)
public class MentalHealthDocumentProviderPluginTest
{
  @Mock
  private MentalHealthFormProvider mentalHealthFormProvider;

  @InjectMocks
  private TherapyDocumentProviderPlugin mentalHealthDocumentProviderPlugin = new MentalHealthDocumentProviderPluginImpl();

  private void setUpMocks(final int numberOfMentalHealthForms)
  {
    Mockito.reset();
    final List<MentalHealthDocumentDto> list = new ArrayList<>();
    for (int i = 0; i < numberOfMentalHealthForms; i++)
    {
      list.add(new MentalHealthDocumentDto(
          "composition",
          new DateTime(),
          new NamedExternalDto("creator", "creator"),
          "patientId",
          new NamedExternalDto("careProvider", "careProvider"),
          i % 2 == 0 ? MentalHealthDocumentType.T2 : MentalHealthDocumentType.T3,
          100,
          Collections.emptyList(),
          Collections.emptyList()));
    }

    Mockito.when(mentalHealthFormProvider.getMentalHealthDocuments(
        ArgumentMatchers.anyString(),
        ArgumentMatchers.any(Interval.class), ArgumentMatchers.anyInt()))
            .thenReturn(list);

    Mockito.when(mentalHealthFormProvider.getMentalHealthDocument(
        ArgumentMatchers.anyString(),
        ArgumentMatchers.anyString()))
            .thenReturn(list.isEmpty() ? null : list.get(0));
  }

  @Test
  public void testGetTherapyDocumentsNoDocuments()
  {
    setUpMocks(0);
    final List<TherapyDocumentDto> documents = mentalHealthDocumentProviderPlugin.getTherapyDocuments(
        "1",
        10,
        new DateTime(2015, 1, 15, 12, 0),
        new Locale("en"));

    Assert.assertEquals(0, documents.size());
  }

  @Test
  public void testGetTherapyDocuments()
  {
    setUpMocks(10);
    final List<TherapyDocumentDto> documents = mentalHealthDocumentProviderPlugin.getTherapyDocuments(
        "1",
        10,
        new DateTime(2015, 1, 15, 12, 0),
        new Locale("en"));

    Assert.assertEquals(TherapyDocumentTypeEnum.T2, documents.get(0).getDocumentType());
    Assert.assertEquals(new NamedExternalDto("careProvider", "careProvider"), documents.get(0).getCareProvider());
    Assert.assertEquals(new NamedExternalDto("creator", "creator"), documents.get(0).getCreator());

    Assert.assertEquals(TherapyDocumentTypeEnum.T3, documents.get(1).getDocumentType());
    Assert.assertEquals(new NamedExternalDto("careProvider", "careProvider"), documents.get(1).getCareProvider());
    Assert.assertEquals(new NamedExternalDto("creator", "creator"), documents.get(1).getCreator());
  }

  @Test
  public void testGetTherapyDocument()
  {
    setUpMocks(1);
    final TherapyDocumentDto document = mentalHealthDocumentProviderPlugin.getTherapyDocument(
        "1",
        "1",
        new DateTime(2015, 1, 15, 12, 0),
        new Locale("en"));

    Assert.assertEquals(TherapyDocumentTypeEnum.T2, document.getDocumentType());
    Assert.assertEquals(new NamedExternalDto("careProvider", "careProvider"), document.getCareProvider());
    Assert.assertEquals(new NamedExternalDto("creator", "creator"), document.getCreator());
  }
}