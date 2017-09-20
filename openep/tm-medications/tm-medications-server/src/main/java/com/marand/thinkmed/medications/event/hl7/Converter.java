package com.marand.thinkmed.medications.event.hl7;

import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import com.marand.thinkmed.medications.event.Event;

/**
 * @author Bostjan Vester
 */
public interface Converter<F extends Message, T extends Event>
{
  T convert(F from) throws DataTypeException;
  boolean accepts(Class<? extends Message> messageType);
}
