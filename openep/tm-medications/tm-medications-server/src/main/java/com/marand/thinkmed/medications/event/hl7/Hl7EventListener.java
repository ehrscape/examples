package com.marand.thinkmed.medications.event.hl7;

import java.util.List;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import com.google.common.base.Preconditions;
import com.marand.thinkmed.medications.event.Event;
import com.marand.thinkmed.medications.event.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bostjan Vester
 */
public class Hl7EventListener extends EventListener
{
  private static final Logger LOG = LoggerFactory.getLogger(Hl7EventListener.class);

  private final List<Converter<Message, ?>> converters;

  public Hl7EventListener(final List<Converter<Message, ?>> converters)
  {
    this.converters = Preconditions.checkNotNull(converters);
  }

  public final void receive(final String hl7message)
  {
    final HapiContext context = new DefaultHapiContext();
    final Parser parser = context.getGenericParser();
    try
    {
      final Message message = parser.parse(hl7message);
      final Event event = convert(message);
      if (event != null)
      {
        handle(event);
      }
    }
    catch (HL7Exception ex)
    {
      LOG.warn("Error parsing HL7 message! Ignoring message...\n" + hl7message, ex);
    }
  }

  private Event convert(final Message message) throws DataTypeException
  {
    for (final Converter<Message, ?> converter : converters)
    {
      if (converter.accepts(message.getClass()))
      {
        return converter.convert(message);
      }
    }
    return null;
  }
}
