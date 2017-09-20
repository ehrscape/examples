package com.marand.thinkmed.medications.b2b;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.marand.maf.core.Decoder;
import com.marand.maf.core.NumberUtils;

/**
 * @author Bostjan Vester
 */
public final class MedicationsConnectorUtils
{
  private MedicationsConnectorUtils()
  {
  }

  public static <F, T> T convert(final F from, final Decoder<F, T> decoder)
  {
    return from != null ? decoder.decode(from) : null;
  }

  public static Long getId(final String externalId)
  {
    try
    {
      return NumberUtils.parseLong(externalId);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  public static <F, T> List<T> convert(final List<F> froms, final Decoder<F, T> decoder)
  {
    return new ArrayList<>(Collections2.transform(
        froms,
        new Function<F, T>()
        {
          @Nullable
          @Override
          public T apply(@Nullable final F input)
          {
            return convert(input, decoder);
          }
        }));
  }

  public static <F, T> Set<T> convert(final Set<F> froms, final Decoder<F, T> decoder)
  {
    return new HashSet<>(Collections2.transform(
        froms,
        new Function<F, T>()
        {
          @Nullable
          @Override
          public T apply(@Nullable final F input)
          {
            return convert(input, decoder);
          }
        }));
  }

  public static Set<String> convert(final Collection<Long> internalIds)
  {
    return new HashSet<>(Collections2.transform(
        internalIds,
        new Function<Long, String>()
        {
          @Nullable
          @Override
          public String apply(@Nullable final Long input)
          {
            return input != null ? String.valueOf(input) : null;
          }
        }
    ));
  }
}
