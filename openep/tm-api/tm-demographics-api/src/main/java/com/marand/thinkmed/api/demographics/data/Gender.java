package com.marand.thinkmed.api.demographics.data;

import com.marand.thinkmed.api.core.GrammaticalGender;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Primoz Prislan
 * @see <a href="http://www.ivz.si/javne_datoteke/datoteke/726-20-Metodoloskacnavodila-SPPczacletoc2004cv4c(bo).doc"/>
 *              ZDRAVSTVENI INFORMACIJSKI SISTEM BOLNIŠNIČNIH OBRAVNAV -Metodološka navodila</a>
 */
public enum Gender
{
  MALE(1, GrammaticalGender.MALE),
  FEMALE(2, GrammaticalGender.FEMALE),
  INDEFINABLE(3, GrammaticalGender.UNDEFINED),
  NOT_KNOWN(9, GrammaticalGender.UNDEFINED), ;

  private final int isoCode;
  private final GrammaticalGender grammatical;

  Gender(final int isoCode, final GrammaticalGender grammatical)
  {
    this.isoCode = isoCode;
    this.grammatical = grammatical;
  }

  public int getIsoCode()
  {
    return isoCode;
  }

  public static Gender getInstanceFromIsoCode(final String code)
  {
    for (final Gender g : values())
    {
      if (StringUtils.equals(g.toString(), code))
      {
        return g;
      }
    }
    throw new IllegalArgumentException("Unknown gender caption: " + code);
  }

  @Override
  public String toString()
  {
    return String.valueOf(isoCode);
  }

  public GrammaticalGender getGrammaticalGender()
  {
    return grammatical;
  }

  public String getLongEntryKey()
  {
    return getBaseEntryKey();
  }

  public String getShortEntryKey()
  {
    return getBaseEntryKey() + ".short";
  }

  private String getBaseEntryKey()
  {
    return Gender.class.getSimpleName() + "." + name();
  }
}