package com.marand.thinkmed.medications.connector.impl.provider;

import java.util.List;
import javax.annotation.Nonnull;

import com.marand.thinkmed.medications.connector.data.object.ObservationDto;
import org.joda.time.Interval;

/**
 * @author Mitja Lapajne
 */
public interface BloodGlucoseProvider
{
  List<ObservationDto> getPatientBloodGlucoseMeasurements(@Nonnull String patientId, @Nonnull Interval interval);
}
