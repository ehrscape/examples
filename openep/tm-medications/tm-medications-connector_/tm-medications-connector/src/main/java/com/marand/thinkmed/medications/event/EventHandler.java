package com.marand.thinkmed.medications.event;

/**
 * @author Bostjan Vester
 */
public interface EventHandler
{
  void handle(AdmitPatientEvent event);
  void handle(DischargePatientEvent event);
}
