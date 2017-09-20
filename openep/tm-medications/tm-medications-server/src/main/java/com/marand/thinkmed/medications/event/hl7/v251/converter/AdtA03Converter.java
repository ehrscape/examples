package com.marand.thinkmed.medications.event.hl7.v251.converter;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v251.message.ADT_A03;
import com.marand.thinkmed.medications.event.DischargePatientEvent;
import com.marand.thinkmed.medications.event.hl7.Converter;

/**
 * @author Bostjan Vester
 */
public class AdtA03Converter implements Converter<ADT_A03, DischargePatientEvent>
{
  @Override
  public boolean accepts(final Class<? extends Message> messageType)
  {
    return ADT_A03.class.isAssignableFrom(messageType);
  }

  @Override
  public DischargePatientEvent convert(final ADT_A03 from)
  {
    return new DischargePatientEvent();
  }
}
