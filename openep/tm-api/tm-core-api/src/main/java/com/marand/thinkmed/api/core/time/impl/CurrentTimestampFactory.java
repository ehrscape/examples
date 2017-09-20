package com.marand.thinkmed.api.core.time.impl;

import com.marand.thinkmed.api.core.time.TimestampFactory;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public class CurrentTimestampFactory implements TimestampFactory
{
  @Override
  public DateTime getTimestamp()
  {
    return new DateTime();
  }
}
