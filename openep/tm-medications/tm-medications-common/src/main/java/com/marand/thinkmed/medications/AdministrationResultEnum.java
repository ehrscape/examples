package com.marand.thinkmed.medications;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Nejc Korasa
 */
public enum AdministrationResultEnum
{
  GIVEN, DEFER, SELF_ADMINISTERED, NOT_GIVEN;

  public static final Set<AdministrationResultEnum> ADMINISTERED = EnumSet.of(GIVEN, SELF_ADMINISTERED);
}
