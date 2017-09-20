package com.marand.thinkmed.medications.warnings.additional.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.marand.maf.core.Opt;
import com.marand.maf.core.StringUtils;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsType;
import com.marand.thinkmed.medications.dto.warning.TherapyAdditionalWarningDto;
import com.marand.thinkmed.medications.warnings.additional.AdditionalWarningsDelegator;
import com.marand.thinkmed.medications.warnings.additional.AdditionalWarningsProvider;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Nejc Korasa
 */
public class AdditionalWarningsDelegatorImpl implements AdditionalWarningsDelegator
{
  private Map<AdditionalWarningsType, AdditionalWarningsProvider> additionalWarningsProviders;

  @Required
  public void setAdditionalWarningsProviders(final Map<AdditionalWarningsType, AdditionalWarningsProvider> additionalWarningsProviders)
  {
    this.additionalWarningsProviders = additionalWarningsProviders;
  }

  @Override
  public Opt<AdditionalWarningsDto> getAdditionalWarnings(
      @Nonnull final Collection<AdditionalWarningsType> types,
      @Nonnull final String patientId,
      @Nonnull final PatientDataForMedicationsDto patientData,
      @Nonnull final DateTime when,
      @Nonnull final Locale locale)
  {
    Preconditions.checkNotNull(types, "type must not be null");
    StringUtils.checkNotBlank(patientId, "patientId must be defined");
    Preconditions.checkNotNull(patientData, "patientData must not be null");
    Preconditions.checkNotNull(when, "when must not be null");
    Preconditions.checkNotNull(locale, "locale must not be null");

    final List<AdditionalWarningsDto> additionalWarnings = types
        .stream()
        .map(type -> getProviderImplementation(type).getAdditionalWarnings(patientId, patientData, when, locale))
        .filter(Opt::isPresent)
        .map(Opt::get)
        .collect(Collectors.toList());

    return joinWarnings(additionalWarnings);
  }

  private Opt<AdditionalWarningsDto> joinWarnings(final List<AdditionalWarningsDto> additionalWarnings)
  {
    if (additionalWarnings.size() == 1)
    {
      return Opt.of(additionalWarnings.get(0));
    }
    if (additionalWarnings.isEmpty())
    {
      return Opt.none();
    }

    final Set<String> allTaskIds = additionalWarnings
        .stream()
        .flatMap(a -> a.getTaskIds().stream())
        .collect(Collectors.toSet());

    final Multimap<TherapyDto, AdditionalWarningDto> allAdditionalWarningsMap = HashMultimap.create();
    additionalWarnings
        .stream()
        .flatMap(a -> a.getWarnings().stream())
        .forEach(t -> allAdditionalWarningsMap.putAll(t.getTherapy(), t.getWarnings()));

    final AdditionalWarningsDto joinedAdditionalWarnings = new AdditionalWarningsDto();
    joinedAdditionalWarnings.setTaskIds(allTaskIds);
    joinedAdditionalWarnings.setWarnings(
        allAdditionalWarningsMap.keySet()
            .stream()
            .map(e -> new TherapyAdditionalWarningDto(e, new ArrayList<>(allAdditionalWarningsMap.get(e))))
            .collect(Collectors.toList()));

    return Opt.of(joinedAdditionalWarnings);
  }

  private AdditionalWarningsProvider getProviderImplementation(final AdditionalWarningsType type)
  {
    final AdditionalWarningsProvider impl = additionalWarningsProviders.get(type);
    if (impl == null)
    {
      throw new IllegalArgumentException("No additional warnings implementation found for: " + type);
    }
    return impl;
  }
}
