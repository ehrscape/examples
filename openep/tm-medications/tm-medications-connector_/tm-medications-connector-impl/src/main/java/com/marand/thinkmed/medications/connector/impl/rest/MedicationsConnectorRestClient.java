package com.marand.thinkmed.medications.connector.impl.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Mitja Lapajne
 */
public interface MedicationsConnectorRestClient
{
  @GET
  @Path("patients/{id}")
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.APPLICATION_JSON)
  String getPatientData(@PathParam("id") String patientId);

  @GET
  @Path("careProviders/{careProviderIds}/patientsSummaries")
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.APPLICATION_JSON)
  String getCareProvidersPatientsSummariesList(@PathParam("careProviderIds") final String careProviderIds);

  @GET
  @Path("patientsSummaries/{patientIds}/")
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.APPLICATION_JSON)
  String getPatientsSummariesList(@PathParam("patientIds") final String patientIds);
}
