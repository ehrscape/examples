package com.marand.thinkmed.medications.event.hl7.v251.converter;

import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v251.datatype.CX;
import ca.uhn.hl7v2.model.v251.datatype.DTM;
import ca.uhn.hl7v2.model.v251.message.ADT_A01;
import ca.uhn.hl7v2.model.v251.segment.PID;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.event.AdmitPatientEvent;
import com.marand.thinkmed.medications.event.CentralCaseDetails;
import com.marand.thinkmed.medications.event.PatientDetails;
import com.marand.thinkmed.medications.event.hl7.Converter;
import org.joda.time.DateTime;

/**
 * @author Bostjan Vester
 */
public class AdtA01Converter implements Converter<ADT_A01, AdmitPatientEvent>
{
  @Override
  public boolean accepts(final Class<? extends Message> messageType)
  {
    return ADT_A01.class.isAssignableFrom(messageType);
  }

  @Override
  public AdmitPatientEvent convert(final ADT_A01 from) throws DataTypeException
  {
    final String patientId = getPatientId(from);
    final DateTime when = getWhen(from);
    final String careProviderId = getCareProviderId(from);
    final CentralCaseDetails centralCaseDetails = getCentralCaseDetails(from);
    final PatientDetails patientDetails = getPatientDetails(from);

    return new AdmitPatientEvent(
        patientId,
        when,
        patientDetails,
        careProviderId,
        centralCaseDetails);
  }

  private CentralCaseDetails getCentralCaseDetails(final ADT_A01 message)
  {
    final String room = message.getPV1().getAssignedPatientLocation().getRoom().getValue();
    final String bed = message.getPV1().getAssignedPatientLocation().getBed().getValue();
    return new CentralCaseDetails(
        false,
        message.getPV1().getAdmittingDoctor()[0].getIDNumber().getValue(),
        message.getPV1().getAttendingDoctor()[0].getIDNumber().getValue(),
        room + " - " + bed);
  }

  private String getCareProviderId(final ADT_A01 message)
  {
    return message.getPV1().getAssignedPatientLocation().getFacility().getUniversalID().getValue();
  }

  private String getPatientId(final ADT_A01 message)
  {
    final PID pid = message.getPID();
    final CX patientID = pid.getPatientID();
    return patientID.getIDNumber().getValue();
  }

  private DateTime getWhen(final ADT_A01 message) throws DataTypeException
  {
    final DTM admitTime = message.getPV1().getAdmitDateTime().getTime();
    return new DateTime(admitTime.getYear(), admitTime.getMonth(), admitTime.getDay(), admitTime.getHour(), admitTime.getMinute());
  }

  private PatientDetails getPatientDetails(final ADT_A01 message)
  {
    try
    {
      final PID pid = message.getPID();
      final String patientName = pid.getPatientName()[0].getGivenName().getValue() + " " + pid.getPatientName()[0].getFamilyName().getSurname().getValue();
      final DTM time = pid.getDateTimeOfBirth().getTime();
      final DateTime birthDate = new DateTime(time.getYear(), time.getMonth(), time.getDay(), 0, 0);
      final String genderString = pid.getAdministrativeSex().getValue();
      final Gender gender = "M".equals(genderString) ? Gender.MALE : Gender.FEMALE;
      final String address = pid.getPatientAddress()[0].getStreetAddress().getStreetOrMailingAddress().getVersion();
      return new PatientDetails(patientName, birthDate, gender, address);
    }
    catch (DataTypeException e)
    {
      e.printStackTrace();
    }
    return null;
  }
}
