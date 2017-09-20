/*
 * Copyright (c) 2010-2014 Marand d.o.o. (www.marand.com)
 *
 * This file is part of Think!Med Clinical Medication Management.
 *
 * Think!Med Clinical Medication Management is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Think!Med Clinical Medication Management is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Think!Med Clinical Medication Management.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.marand.thinkmed.medications.connector.impl.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.StringUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.connector.MedicationsConnector;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.medications.connector.data.object.QuantityWithTimeDto;
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
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Mitja Lapajne
 */
public class RestMedicationsConnector implements MedicationsConnector, InitializingBean
{
  private MedicationsConnectorRestClient restClient;
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
    restClient = target.proxy(MedicationsConnectorRestClient.class);
  }

  @Override
  public PatientDataForMedicationsDto getPatientData(
      @Nonnull final String patientId, @Nonnull final DateTime when)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");
    Preconditions.checkNotNull(when, "when is required");
    final String patientJson = restClient.getPatientData(patientId);
    return JsonUtil.fromJson(patientJson, PatientDataForMedicationsDto.class);
  }

  @Override
  public Map<String, PatientDisplayWithLocationDto> getPatientDisplayWithLocationMap(
      final Collection<String> careProviderIds, final Collection<String> patientIds)
  {
    final String patientsListJson;
    if (patientIds != null)
    {
      if (patientIds.isEmpty())
      {
        return Collections.emptyMap();
      }
      final String patientIdsString = patientIds.stream().collect(Collectors.joining(","));
      patientsListJson = restClient.getPatientsSummariesList(patientIdsString);
    }
    else
    {
      Preconditions.checkArgument(careProviderIds != null, "Both patientIds and careProviderId are null");
      final String careProviderIdsString = careProviderIds.stream().collect(Collectors.joining(","));
      patientsListJson = restClient.getCareProvidersPatientsSummariesList(careProviderIdsString);
    }
    final List<PatientDisplayWithLocationDto> patientsList =
        Arrays.asList(JsonUtil.fromJson(patientsListJson, PatientDisplayWithLocationDto[].class));

    return patientsList.stream()
        .collect(Collectors.toMap(p -> p.getPatientDisplayDto().getId(), Function.identity()));
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
    return null;
  }

  @Override
  public boolean assertPasswordForUsername(final String username, final String password)
  {
    return false;
  }

  @Override
  public List<QuantityWithTimeDto> getBloodSugarObservations(
      @Nonnull final String patientId, @Nonnull final Interval interval)
  {
    return new ArrayList<>();
  }
}
