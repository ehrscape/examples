package com.marand.thinkmed.medications.event;

/**
 * @author Bostjan Vester
 */
public abstract class Event
{
  public abstract void handleWith(final EventHandler eventHandler);
}
