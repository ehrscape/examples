package com.marand.thinkmed.medications;

import java.util.Locale;

import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.prefs.MafPrefsStorageType;
import com.marand.maf.core.prefs.StringPreference;

public class MedicationPreferences
{
  private static final String UNLICENSED_MEDICATION_WARNING_KEY = "UNLICENSED_MEDICATION_WARNING";

  private MedicationPreferences()
  {
  }

  public static String getUnlicensedMedicationWarning(final Locale locale)
  {
    final StringPreference strPref =
        new StringPreference(
            UNLICENSED_MEDICATION_WARNING_KEY,
            MafPrefsStorageType.SYSTEM,
            false,
            Dictionary.getEntry("unlicensed.medication.outpatient.warning", locale));
    return strPref.getValue();
  }
}
