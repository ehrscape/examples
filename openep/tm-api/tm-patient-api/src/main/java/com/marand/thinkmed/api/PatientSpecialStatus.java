package com.marand.thinkmed.api;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Bostjan Vester
 */
public enum PatientSpecialStatus
{
  MY, WATCHED;

  public static Collection<PatientSpecialStatus> create(final boolean my, final boolean watched)
  {
    final Collection<PatientSpecialStatus> result = new ArrayList<>();

    if (my)
    {
      result.add(MY);
    }
    if (watched)
    {
      result.add(WATCHED);
    }

    return result;
  }
}
