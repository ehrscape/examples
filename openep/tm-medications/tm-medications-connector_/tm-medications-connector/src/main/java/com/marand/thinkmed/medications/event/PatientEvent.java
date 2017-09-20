package com.marand.thinkmed.medications.event;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Bostjan Vester
 */
public abstract class PatientEvent extends Event
{
  private final String patientId;

  protected PatientEvent(final String patientId)
  {
    this.patientId = Preconditions.checkNotNull(patientId);
    Preconditions.checkArgument(StringUtils.isNotBlank(patientId));
  }

  public String getPatientId()
  {
    return patientId;
  }
}
