package com.marand.thinkmed.medications.event;

import com.google.common.base.Preconditions;
import org.joda.time.DateTime;

/**
 * @author Bostjan Vester
 */
public final class AdmitPatientEvent extends PatientEvent
{
  private final DateTime when;
  private final PatientDetails patientDetails;
  private final String careProviderId;
  private final CentralCaseDetails centralCaseDetails;

  public AdmitPatientEvent(
      final String patientId,
      final DateTime when,
      final PatientDetails patientDetails,
      final String careProviderId,
      final CentralCaseDetails centralCaseDetails)
  {
    super(patientId);
    this.when = Preconditions.checkNotNull(when);
    this.patientDetails = patientDetails;
    this.careProviderId = Preconditions.checkNotNull(careProviderId);
    this.centralCaseDetails = Preconditions.checkNotNull(centralCaseDetails);
  }

  public DateTime getWhen()
  {
    return when;
  }

  public PatientDetails getPatientDetails()
  {
    return patientDetails;
  }

  public String getCareProviderId()
  {
    return careProviderId;
  }

  public CentralCaseDetails getCentralCaseDetails()
  {
    return centralCaseDetails;
  }

  @Override
  public void handleWith(final EventHandler eventHandler)
  {
    eventHandler.handle(this);
  }
}
