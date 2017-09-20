package com.marand.thinkmed.api;

import java.util.Collection;

import com.marand.thinkmed.api.core.time.CurrentTimeProvider;
import com.marand.thinkmed.api.demographics.PersonImageUtils;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.api.medical.data.CareProvisionType;
import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 * @author Primoz Prislan
 */
public class PatientImageUtils
{
  private static final String PATIENT_PREFIX = "patient";
  private static final String AGE_00 = "00";
  private static final String AGE_01M = "01m";
  private static final String AGE_01Y = "01y";
  private static final String AGE_03Y = "03y";
  private static final String AGE_06Y = "06y";
  private static final String AGE_11Y = "11y";
  private static final String AGE_18Y = "18y";
  private static final String AGE_25Y = "25y";
  private static final String AGE_40Y = "40y";
  private static final String AGE_50Y = "50y";
  private static final String AGE_60Y = "60y";
  private static final String AGE_70Y = "70y";
  private static final String AGE_80Y = "80y";

  private static final String ACCOMPANYING_PERSON_PREFIX = "accompaniedPerson";

  private static final String INVALID_INSURANCE_WARNING_PATH = PersonImageUtils.IMAGES_PATH + "status_notUrgent_16.png";
  private static final String SELF_PAYER_WARNING = PersonImageUtils.IMAGES_PATH + "euroRed_status_16.png";
  private static final String INVALID_VOLUNTARY_INSURANCE_WARNING = PersonImageUtils.IMAGES_PATH + "euroYellow_status_16.png";

  private static final String MY = PersonImageUtils.IMAGES_PATH + "starWhite_status_16.png";
  private static final String MY_WATCHED = PersonImageUtils.IMAGES_PATH + "starYellowBlack_status_16.png";
  private static final String NO_TREATING_PHYSICIAN = PersonImageUtils.IMAGES_PATH + "starNostar_status_16.png";
  private static final String NOT_SYNCED = PersonImageUtils.IMAGES_PATH + "update_16.png";
  private static final String WATCHED = PersonImageUtils.IMAGES_PATH + "starYellow_status_16.png";
  private static final String WATCHED_NO_TREATING_PHYSICIAN =
      PersonImageUtils.IMAGES_PATH + "starYellowExclamation_status_16.png";
  private static final String DEATH_STATUS = PersonImageUtils.IMAGES_PATH + "death_status_16.png";
  private static final String NOT_ACTIVE = PersonImageUtils.IMAGES_PATH + "patientNotActive_status_16.png";

  private static final String NO_IMAGE = PersonImageUtils.IMAGES_PATH + "empty_16.png";
  private static final String CLINIC = PersonImageUtils.IMAGES_PATH + "outpatient_status_16.png";
  private static final String HOSPITAL = PersonImageUtils.IMAGES_PATH + "inpatient_status_16.png";

  private static final String ANONYMOUS_PATIENT = PersonImageUtils.IMAGES_PATH + "patient_anonymous_48.png";

  private PatientImageUtils()
  {
  }

  public static String getPatientImagePath(final Gender gender, final DateTime birthDate)
  {
    final Period period = new Period(birthDate, CurrentTimeProvider.get());

    return getPatientImagePath(gender, period.getMonths(), period.getYears());
  }

  public static String getPatientImagePath(final Gender gender, final int ageMonths, final int ageYears)
  {
    final String basicImageName;
    if (ageYears >= 80)
    {
      basicImageName = AGE_80Y;
    }
    else if (ageYears >= 70)
    {
      basicImageName = AGE_70Y;
    }
    else if (ageYears >= 60)
    {
      basicImageName = AGE_60Y;
    }
    else if (ageYears >= 50)
    {
      basicImageName = AGE_50Y;
    }
    else if (ageYears >= 40)
    {
      basicImageName = AGE_40Y;
    }
    else if (ageYears >= 25)
    {
      basicImageName = AGE_25Y;
    }
    else if (ageYears >= 18)
    {
      basicImageName = AGE_18Y;
    }
    else if (ageYears >= 11)
    {
      basicImageName = AGE_11Y;
    }
    else if (ageYears >= 6)
    {
      basicImageName = AGE_06Y;
    }
    else if (ageYears >= 3)
    {
      basicImageName = AGE_03Y;
    }
    else if (ageYears >= 1)
    {
      basicImageName = AGE_01Y;
    }
    else if (ageMonths >= 1)
    {
      basicImageName = AGE_01M;
    }
    else
    {
      basicImageName = AGE_00;
    }

    final String prefix = PATIENT_PREFIX + PersonImageUtils.NAME_GROUP_SEPARATOR;
    if (gender != Gender.FEMALE && gender != Gender.MALE)
    {
      return PersonImageUtils.getFullImagePath(
          prefix + AGE_00 + PersonImageUtils.NAME_GROUP_SEPARATOR + PersonImageUtils.SUFFIX_UNKNOWN);
    }
    return PersonImageUtils.getFullImagePath(prefix + basicImageName, gender);
  }

  public static String getAccompanyingPersonImagePath(final Gender gender)
  {
    return PersonImageUtils.getFullImagePath(ACCOMPANYING_PERSON_PREFIX, gender);
  }

  public static String getAnonymousPatientImagePath()
  {
    return ANONYMOUS_PATIENT;
  }

  public static String getDiedIconPath()
  {
    return DEATH_STATUS;
  }

  public static String getPatientSpecialStatusIconPath(
      final Collection<PatientSpecialStatus> specialStatuses,
      final boolean hasTreatingPhysician,
      final boolean activePatient)
  {
    final String iconPath;
    if (!activePatient)
    {
      iconPath = NOT_ACTIVE;
    }
    else if (specialStatuses.isEmpty())
    {
      iconPath = hasTreatingPhysician ? null : NO_TREATING_PHYSICIAN;
    }
    else if (specialStatuses.size() == 1)
    {
      if (specialStatuses.contains(PatientSpecialStatus.MY))
      {
        iconPath = MY;
      }
      else
      {
        iconPath = hasTreatingPhysician ? WATCHED : WATCHED_NO_TREATING_PHYSICIAN;
      }
    }
    else
    {
      iconPath = MY_WATCHED;
    }

    return iconPath;
  }

  public static String getPatientStatusIconPath(final CareProvisionType careProvisionType, final boolean synced)
  {
    return synced ? getCareProvisionTypeIconPath(careProvisionType) : NOT_SYNCED;
  }

  public static String getInvalidInsuranceWarningIconPath()
  {
    return INVALID_INSURANCE_WARNING_PATH;
  }

  public static String getSelfPayerIcon()
  {
    return SELF_PAYER_WARNING;
  }

  public static String getInvalidVoluntaryInsuranceWarning()
  {
    return INVALID_VOLUNTARY_INSURANCE_WARNING;
  }

  public static String getCareProvisionTypeIconPath(final CareProvisionType careProvisionType)
  {
    if (careProvisionType == null)
    {
      return NO_IMAGE;
    }
    else
    {
      return careProvisionType.isOutpatient() ? CLINIC : HOSPITAL;
    }
  }
}