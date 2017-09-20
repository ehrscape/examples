package com.marand.thinkmed.api.medical.data;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Primoz Prislan
 */
public enum CareProvisionType
{
  CLINIC(true),
  HOSPITAL(false),
  DIAGNOSTIC(true),
  HOSPITAL_ADMISSION(true),
  PROCEDURE(false),
  TREATMENT(true);

  public static final Set<CareProvisionType> HOSPITAL_TYPES = EnumSet.of(HOSPITAL, HOSPITAL_ADMISSION);
  public static final Set<CareProvisionType> INPATIENT_TYPES = EnumSet.of(HOSPITAL, PROCEDURE);

  private final boolean outpatient;

  CareProvisionType(final boolean outpatient)
  {
    this.outpatient = outpatient;
  }

  public boolean isOutpatient()
  {
    return outpatient;
  }
}
