/*
 * Copyright (c) 2010-2014 Marand d.o.o. (www.marand.com)
 *
 * This file is part of Think!Med Clinical Medication Management.
 *
 * Think!Med Clinical Medication Management is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Think!Med Clinical Medication Management is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Think!Med Clinical Medication Management.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.marand.thinkmed.medicationsexternal.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medicationsexternal.dto.DoseRangeCheckDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationForWarningsSearchDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationsWarningDto;
import com.marand.thinkmed.medicationsexternal.dto.WarningsProviderDto;
import com.marand.thinkmed.medicationsexternal.plugin.MedicationExternalDataPlugin;
import com.marand.thinkmed.medicationsexternal.service.MedicationsExternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mitja Lapajne
 */
public class MedicationsExternalServiceImpl implements MedicationsExternalService
{
  private static final Logger LOG = LoggerFactory.getLogger(MedicationsExternalServiceImpl.class);
  private Map<String, MedicationExternalDataPlugin> plugins;

  public Map<String, MedicationExternalDataPlugin> getPlugins()
  {
    return plugins;
  }

  public void setPlugins(final Map<String, MedicationExternalDataPlugin> plugins)
  {
    this.plugins = plugins;
  }

  @Override
  public void preparePlugins()
  {
    for (final MedicationExternalDataPlugin plugin : plugins.values())
    {
      plugin.reloadCache();
    }
  }

  @Override
  public List<DoseRangeCheckDto> findDoseRangeChecks(final String externalSystem, final String externalId)
  {
    final MedicationExternalDataPlugin plugin = plugins.get(externalSystem);
    if (plugin != null)
    {
      return plugin.findDoseRangeChecks(externalId);
    }
    return new ArrayList<>();
  }

  @Override
  public List<MedicationsWarningDto> findMedicationWarnings(
      final String externalSystem,
      final long patientAgeInDays,
      final Double patientWeightInKg,
      final Double bsaInM2,
      final boolean isFemale,
      final List<NamedExternalDto> diseaseTypeValues,
      final List<NamedExternalDto> allergiesExternalValues,
      final List<MedicationForWarningsSearchDto> medicationSummaries)
  {
    final MedicationExternalDataPlugin plugin = plugins.get(externalSystem);
    if (plugin != null)
    {
      try
      {
        return plugin.findMedicationWarnings(
            patientAgeInDays,
            patientWeightInKg,
            bsaInM2,
            isFemale,
            diseaseTypeValues,
            allergiesExternalValues,
            medicationSummaries);
      }
      catch (Exception ex)
      {
        LOG.error(ex.toString());
      }
    }
    return new ArrayList<>();
  }

  @Override
  public List<WarningsProviderDto> getWarningProviders()
  {
    final List<WarningsProviderDto> warningProviders = new ArrayList<>();
    for (final String pluginCode : plugins.keySet())
    {
      final MedicationExternalDataPlugin externalDataPlugin = plugins.get(pluginCode);
      if (externalDataPlugin.isWarningsProvider())
      {
        final WarningsProviderDto warningsProviderDto = new WarningsProviderDto();
        warningsProviderDto.setExternalSystem(pluginCode);
        warningsProviderDto.setRequiresDiseaseCodesTranslation(externalDataPlugin.requiresDiseaseCodesTranslation());
        warningProviders.add(warningsProviderDto);
      }
    }
    return warningProviders;
  }

  @Override
  public String getMedicationOverviewProvider()
  {
    String overviewProvider = null;
    for (final String pluginCode : plugins.keySet())
    {
      if (plugins.get(pluginCode).isMedicationOverviewProvider())
      {
        if (overviewProvider != null)
        {
          throw new IllegalArgumentException("Max one medication overview provider allowed");
        }
        overviewProvider = pluginCode;
      }
    }
    return overviewProvider;
  }

  @Override
  public String getDoseRangeChecksProvider()
  {
    String overviewProvider = null;
    for (final String pluginCode : plugins.keySet())
    {
      if (plugins.get(pluginCode).isDoseRangeChecksProvider())
      {
        if (overviewProvider != null)
        {
          throw new IllegalArgumentException("Max one medication dose range check provider allowed");
        }
        overviewProvider = pluginCode;
      }
    }
    return overviewProvider;
  }
}
