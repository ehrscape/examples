package com.marand.thinkmed.medications.event;

/**
 * @author Bostjan Vester
 */
public final class DischargePatientEvent extends Event
{
  @Override
  public void handleWith(final EventHandler eventHandler)
  {
    eventHandler.handle(this);
  }
}
