package com.marand.thinkmed.medications.event;

/**
 * @author Bostjan Vester
 */
public class CentralCaseDetails
{
  private final boolean outpatient;
  private final String curingCareProfessionalId;
  private final String supervisoryCareProfessionalId;
  private final String roomAndBed;

  public CentralCaseDetails(
      final boolean outpatient,
      final String curingCareProfessionalId,
      final String supervisoryCareProfessionalId, final String roomAndBed)
  {
    this.outpatient = outpatient;
    this.curingCareProfessionalId = curingCareProfessionalId;
    this.supervisoryCareProfessionalId = supervisoryCareProfessionalId;
    this.roomAndBed = roomAndBed;
  }

  public boolean isOutpatient()
  {
    return outpatient;
  }

  public String getCuringCareProfessionalId()
  {
    return curingCareProfessionalId;
  }

  public String getSupervisoryCareProfessionalId()
  {
    return supervisoryCareProfessionalId;
  }

  public String getRoomAndBed()
  {
    return roomAndBed;
  }
}
