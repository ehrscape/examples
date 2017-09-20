package com.marand.thinkmed.medications.dao.openehr;

import com.marand.maf.core.exception.SystemException;

/**
 * @author Mitja Lapajne
 */
public class CompositionNotFoundException  extends SystemException
{
  public CompositionNotFoundException(final String compositionUid)
  {
    super("Composition with uid " + compositionUid + " doesn't exist!");
  }
}
