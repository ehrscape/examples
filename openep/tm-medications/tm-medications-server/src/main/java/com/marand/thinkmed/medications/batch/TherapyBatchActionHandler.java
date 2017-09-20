package com.marand.thinkmed.medications.batch;

import javax.annotation.Nonnull;

import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public interface TherapyBatchActionHandler
{
  void abortAllTherapies(@Nonnull String patientId, @Nonnull DateTime when);

  void suspendAllTherapies(@Nonnull String patientId, @Nonnull DateTime when);

  void suspendAllTherapiesOnTemporaryLeave(@Nonnull String patientId, @Nonnull DateTime when);

  void reissueAllTherapiesOnReturnFromTemporaryLeave(@Nonnull String patientId, @Nonnull DateTime when);
}
