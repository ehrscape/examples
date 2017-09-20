package com.marand.thinkmed.medications.document.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.marand.thinkmed.medications.document.TherapyDocumentProvider;
import com.marand.thinkmed.medications.document.TherapyDocumentProviderPlugin;
import com.marand.thinkmed.medications.dto.document.TherapyDocumentDto;
import com.marand.thinkmed.medications.dto.document.TherapyDocumentsDto;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.joda.time.DateTime;
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
 * @author Mitja Lapajne
 */
@RunWith(MockitoJUnitRunner.class)
public class TherapyDocumentProviderTest
{
  @InjectMocks
  private TherapyDocumentProviderImpl therapyDocumentProvider = new TherapyDocumentProviderImpl();

  @Mock
  private TherapyDocumentProviderPlugin therapyDocumentProviderPlugin;

  private void setUpMocks(final int numberOfDocuments)
  {
    Mockito.reset();

    therapyDocumentProvider.setProviderPlugins(Collections.singletonList(therapyDocumentProviderPlugin));
    final List<TherapyDocumentDto> documents = new ArrayList<>();
    for (int i = 0; i < numberOfDocuments; i++)
    {
      final TherapyDocumentDto document = new TherapyDocumentDto();
      document.setCreateTimestamp(new DateTime(2015, 1, 25, 10, 0));
      documents.add(document);
    }

    Mockito.when(
        therapyDocumentProviderPlugin.getTherapyDocuments(
            ArgumentMatchers.anyString(), ArgumentMatchers.anyInt(), ArgumentMatchers.any(DateTime.class), ArgumentMatchers.any(Locale.class)))
        .thenReturn(documents);
  }

  @Test
  public void testGetTherapyDocumentsNoDocuments()
  {
    setUpMocks(0);
    final TherapyDocumentsDto documents =
        therapyDocumentProvider.getTherapyDocuments("1", 10, 0, new DateTime(2015, 1, 15, 12, 0), new Locale("en"));
    Assert.assertEquals(0, documents.getDocuments().size());
    Assert.assertFalse(documents.isMoreRecordsExist());
  }

  @Test
  public void testGetTherapyDocumentsNoOffsetNoMoreExist()
  {
    setUpMocks(3);
    final TherapyDocumentsDto documents =
        therapyDocumentProvider.getTherapyDocuments("1", 5, 0, new DateTime(2015, 1, 15, 12, 0), new Locale("en"));
    Assert.assertEquals(3, documents.getDocuments().size());
    Assert.assertFalse(documents.isMoreRecordsExist());
  }

  @Test
  public void testGetTherapyDocumentsOffsetNoMoreExist()
  {
    setUpMocks(8);
    final TherapyDocumentsDto documents =
        therapyDocumentProvider.getTherapyDocuments("1", 5, 5, new DateTime(2015, 1, 15, 12, 0), new Locale("en"));
    Assert.assertEquals(3, documents.getDocuments().size());
    Assert.assertFalse(documents.isMoreRecordsExist());
  }

  @Test
  public void testGetTherapyDocumentsNoOffsetMoreExist()
  {
    setUpMocks(8);
    final TherapyDocumentsDto documents =
        therapyDocumentProvider.getTherapyDocuments("1", 5, 0, new DateTime(2015, 1, 15, 12, 0), new Locale("en"));
    Assert.assertEquals(5, documents.getDocuments().size());
    Assert.assertTrue(documents.isMoreRecordsExist());
  }

  @Test
  public void testGetTherapyDocumentsOffsetMoreExist()
  {
    setUpMocks(12);
    final TherapyDocumentsDto documents =
        therapyDocumentProvider.getTherapyDocuments("1", 5, 5, new DateTime(2015, 1, 15, 12, 0), new Locale("en"));
    Assert.assertEquals(5, documents.getDocuments().size());
    Assert.assertTrue(documents.isMoreRecordsExist());
  }
}