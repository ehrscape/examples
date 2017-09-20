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

package com.marand.thinkmed.medications.therapy.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.marand.thinkcache.CacheKeys;
import com.marand.thinkcache.config.CacheConfig;
import com.marand.thinkcache.config.CacheProducerConfig;
import com.marand.thinkcache.producer.CacheInvalidationServiceProvider;
import com.marand.thinkmed.medications.b2b.MedicationsConnectorUtils;
import com.marand.thinkmed.medications.therapy.TherapyCacheInvalidator;

/**
 * @author Nejc Korasa
 */
public class TherapyCacheInvalidatorImpl implements TherapyCacheInvalidator
{
  private CacheInvalidationServiceProvider cacheInvalidationServiceProvider;
  private CacheProducerConfig cacheProducerConfig;

  public void setCacheInvalidationServiceProvider(final CacheInvalidationServiceProvider cacheInvalidationServiceProvider)
  {
    this.cacheInvalidationServiceProvider = cacheInvalidationServiceProvider;
  }

  public void setCacheProducerConfig(final CacheProducerConfig cacheProducerConfig)
  {
    this.cacheProducerConfig = cacheProducerConfig;
  }

  @Override
  public void invalidateTherapyTasksCache(final String patientId)
  {
    if (cacheProducerConfig != null && cacheInvalidationServiceProvider != null)
    {
      final CacheConfig cacheConfig = cacheProducerConfig.getCacheConfig();
      final Long patientLongId = MedicationsConnectorUtils.getId(patientId);
      if (patientLongId != null)
      {
        final List<CacheKeys<?>> invalidation = new ArrayList<>();
        invalidation.add(
            new CacheKeys<>(
                cacheConfig.getDomain(),
                cacheConfig.getName(),
                Collections.singleton(patientLongId)));
        cacheInvalidationServiceProvider.selectCacheInvalidationService().getService().invalidatePerform(invalidation);
      }
    }
  }
}
