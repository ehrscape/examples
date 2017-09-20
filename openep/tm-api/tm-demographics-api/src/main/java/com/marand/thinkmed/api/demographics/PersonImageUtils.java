package com.marand.thinkmed.api.demographics;

import com.marand.thinkmed.api.demographics.data.Gender;

/**
 * @author Jure Grom
 * @author Primoz Prislan
 */
public class PersonImageUtils
{
  public static final char NAME_GROUP_SEPARATOR = '-';

  public static final String IMAGES_PATH = "/images/icons/";
  private static final String PARTY_IMAGES_PATH = IMAGES_PATH + "party/";

  private static final String SUFFIX_FEMALE = "f.png";
  private static final String SUFFIX_MALE = "m.png";
  public static final String SUFFIX_UNKNOWN = "s.png";

  private static final String DEFAULT_PREFIX = "personDefault";
  private static final String ADMIN_PREFIX = "admin";
  private static final String TRUSTED_PERSON_PREFIX = "personOfTrust";

  private PersonImageUtils()
  {
  }

  public static String getDefaultPersonImagePath(final Gender gender)
  {
    return getFullImagePath(DEFAULT_PREFIX, gender);
  }

  public static String getFullImagePath(final String basicName)
  {
    return PARTY_IMAGES_PATH + basicName;
  }

  public static String getFullImagePath(final String basicName, final Gender gender)
  {
    return getFullImagePath(basicName) + NAME_GROUP_SEPARATOR + getGenderSuffix(gender);
  }

  private static String getGenderSuffix(final Gender gender)
  {
    return gender == Gender.FEMALE ? SUFFIX_FEMALE : SUFFIX_MALE;
  }

  public static String getAdminImagePath(final Gender gender)
  {
    return getFullImagePath(ADMIN_PREFIX, gender);
  }

  public static String getTrustedPersonImagePath(final Gender gender)
  {
    return getFullImagePath(TRUSTED_PERSON_PREFIX, gender);
  }
}