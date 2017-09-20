package com.marand.thinkmed.api.core.time;

import java.util.TimeZone;

import com.marand.thinkmed.api.core.time.impl.CurrentTimestampFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * @author Nejc Korasa
 */
public class CurrentTimeProvider
{
  private static volatile TimestampFactory instance = new CurrentTimestampFactory();

  private CurrentTimeProvider()
  {
  }

  public static void init(final TimestampFactory timestampFactory)
  {
    instance = timestampFactory;
    changeTimeZone();
  }

  private static void changeTimeZone()
  {
    final DateTime referenceDateTime = get();
    final DateTimeZone zone = referenceDateTime.getZone();
    TimeZone.setDefault(TimeZone.getTimeZone(zone.getID()));
    DateTimeZone.setDefault(zone);
  }

  public static DateTime get()
  {
    if (instance == null)
    {
      throw new IllegalStateException("TimestampFactory not initialized");
    }
    return instance.getTimestamp();
  }
}


