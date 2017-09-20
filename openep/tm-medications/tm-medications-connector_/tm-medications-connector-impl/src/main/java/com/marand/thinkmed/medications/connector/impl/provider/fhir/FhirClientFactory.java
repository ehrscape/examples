package com.marand.thinkmed.medications.connector.impl.provider.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.IClientInterceptor;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.client.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import com.google.common.base.Preconditions;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Mitja Lapajne
 */
public class FhirClientFactory
{
  private FhirClientFactory()
  {
  }

  public static IGenericClient createFhirClient(final String fhirServerUri, final String fhirAuthTicketHeaderName)
  {
    final FhirContext fhirContext = FhirContext.forDstu2();
    final IGenericClient fhirClient = fhirContext.newRestfulGenericClient(fhirServerUri);
    fhirContext.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);

    if (fhirAuthTicketHeaderName != null)
    {
      fhirClient.registerInterceptor(new IClientInterceptor()
      {
        @Override
        public void interceptRequest(final IHttpRequest theRequest)
        {
          final Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
          Preconditions.checkArgument(
              credentials instanceof String,
              "Credentials are not a String - check authentication settings");
          theRequest.addHeader(fhirAuthTicketHeaderName, (String)credentials);
        }

        @Override
        public void interceptResponse(final IHttpResponse theResponse)
        {
        }
      });
    }
    return fhirClient;
  }
}
