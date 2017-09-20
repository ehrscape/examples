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

package com.marand.thinkmed.medications.business;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.valueholder.ValueHolder;
import com.marand.thinkmed.html.components.tree.TreeNodeData;
import com.marand.thinkmed.medications.MedicationFinderFilterEnum;
import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.business.mapper.MedicationHolderDtoMapper;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationHolderDto;
import com.marand.thinkmed.medications.dto.MedicationSimpleDto;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Mitja Lapajne
 */
public class MedicationsFinderImpl implements MedicationsFinder
{
  private ValueHolder<Map<Long, MedicationHolderDto>> medicationsValueHolder;
  private MedicationsDao medicationsDao;
  private MedicationHolderDtoMapper medicationHolderDtoMapper;

  @Required
  public void setMedicationsValueHolder(final ValueHolder<Map<Long, MedicationHolderDto>> medicationsValueHolder)
  {
    this.medicationsValueHolder = medicationsValueHolder;
  }

  @Required
  public void setMedicationsDao(final MedicationsDao medicationsDao)
  {
    this.medicationsDao = medicationsDao;
  }

  @Required
  public void setMedicationHolderDtoMapper(final MedicationHolderDtoMapper medicationHolderDtoMapper)
  {
    this.medicationHolderDtoMapper = medicationHolderDtoMapper;
  }

  @Override
  public List<TreeNodeData> findMedications(
      @Nonnull final String searchString,
      final boolean startMustMatch,
      final String careProviderId,
      @Nonnull final EnumSet<MedicationFinderFilterEnum> filters)
  {
    Preconditions.checkNotNull(searchString, "searchString is required");
    Preconditions.checkNotNull(filters, "filters is required");

    final Map<Long, MedicationHolderDto> medicationsMap = new LinkedHashMap<>(medicationsValueHolder.getValue());
    filters.forEach(filter -> applyAdditionalFilter(medicationsMap, filter, careProviderId));

    final List<TreeNodeData> medicationsTree = buildMedicationsTree(medicationsMap);
    return filterMedicationsTree(medicationsTree, searchString, startMustMatch);
  }

  @Override
  public List<TreeNodeData> findSimilarMedications(
      final long medicationId,
      @Nonnull final List<Long> routeIds,
      @Nonnull final DateTime when)
  {
    Preconditions.checkNotNull(routeIds, "routeIds must not be null");
    Preconditions.checkNotNull(when, "when must not be null");

    final Set<Long> similarMedicationsIds = medicationsDao.findSimilarMedicationsIds(medicationId, routeIds, when);

    final Map<Long, MedicationHolderDto> similarMedicationsMap = similarMedicationsIds.stream()
        .map(i -> medicationsValueHolder.getValue().get(i))
        .filter(m -> m != null)
        .collect(Collectors.toMap(MedicationHolderDto::getId, m -> m));

    return buildMedicationsTree(similarMedicationsMap);
  }

  @Override
  public List<MedicationDto> findMedicationProducts(
      final long medicationId,
      @Nonnull final List<Long> routeIds,
      @Nonnull final DateTime when)
  {
    Preconditions.checkNotNull(routeIds, "routeIds must not be null");
    Preconditions.checkNotNull(when, "when must not be null");

    final boolean productBasedMedication = medicationsDao.isProductBasedMedication(medicationId);

    if (productBasedMedication)
    {
      final Set<Long> similarMedicationsIds = medicationsDao.findSimilarMedicationsIds(medicationId, routeIds, when);
      return similarMedicationsIds.stream()
          .map(i -> medicationsValueHolder.getValue().get(i))
          .map(m -> medicationHolderDtoMapper.mapToMedicationDto(m))
          .collect(Collectors.toList());
    }
    return medicationsDao.getMedicationChildProducts(medicationId, routeIds, when);
  }

  private void applyAdditionalFilter(
      final Map<Long, MedicationHolderDto> medicationsMap,
      final MedicationFinderFilterEnum filter,
      final String careProviderId)
  {
    if (filter == MedicationFinderFilterEnum.MENTAL_HEALTH)
    {
      final Iterator<Map.Entry<Long, MedicationHolderDto>> iterator = medicationsMap.entrySet().iterator();
      while (iterator.hasNext())
      {
        final Map.Entry<Long, MedicationHolderDto> entry = iterator.next();
        if (!entry.getValue().isMentalHealthDrug())
        {
          iterator.remove();
        }
      }
    }
    else if (filter == MedicationFinderFilterEnum.OUTPATIENT_PRESCRIPTION)
    {
      final Iterator<Map.Entry<Long, MedicationHolderDto>> iterator = medicationsMap.entrySet().iterator();
      while (iterator.hasNext())
      {
        final Map.Entry<Long, MedicationHolderDto> entry = iterator.next();
        if (!entry.getValue().isOutpatientMedication())
        {
          iterator.remove();
        }
      }
    }
    else if (filter == MedicationFinderFilterEnum.INPATIENT_PRESCRIPTION)
    {
      final Iterator<Map.Entry<Long, MedicationHolderDto>> iterator = medicationsMap.entrySet().iterator();
      while (iterator.hasNext())
      {
        final Map.Entry<Long, MedicationHolderDto> entry = iterator.next();
        if (!entry.getValue().isInpatientMedication())
        {
          iterator.remove();
        }
      }
    }
    else if (filter == MedicationFinderFilterEnum.FORMULARY)
    {
      final Iterator<Map.Entry<Long, MedicationHolderDto>> iterator = medicationsMap.entrySet().iterator();
      while (iterator.hasNext())
      {
        final Map.Entry<Long, MedicationHolderDto> entry = iterator.next();
        final boolean medicationFormulary = isFormularyMedication(entry.getValue(), careProviderId);
        if (!medicationFormulary)
        {
          iterator.remove();
        }
      }
    }
  }

  private boolean isFormularyMedication(final MedicationHolderDto medicationHolderDto, final String careProviderId)
  {
    if (!medicationHolderDto.isFormulary())
    {
      return false;
    }
    final boolean formularyForAllCareProviders = medicationHolderDto.getFormularyCareProviders().isEmpty();
    return careProviderId == null ||
        formularyForAllCareProviders ||
        medicationHolderDto.getFormularyCareProviders().contains(careProviderId);
  }

  private List<TreeNodeData> buildMedicationsTree(final Map<Long, MedicationHolderDto> medicationsMap)
  {
    final List<TreeNodeData> medicationTree = new ArrayList<>();

    final Map<Long, TreeNodeData> vtmMap = new LinkedHashMap<>();
    for (final MedicationHolderDto holderDto : medicationsMap.values())
    {
      if (holderDto.getMedicationLevel() == MedicationLevelEnum.VTM)
      {
        final TreeNodeData searchDto = medicationHolderDtoMapper.mapToTreeNodeDto(holderDto);
        medicationTree.add(searchDto);
        vtmMap.put(holderDto.getVtmId(), searchDto);
      }
    }

    final Map<Long, TreeNodeData> vmpMap = new LinkedHashMap<>();
    for (final MedicationHolderDto holderDto : medicationsMap.values())
    {
      if (holderDto.getMedicationLevel() == MedicationLevelEnum.VMP)
      {
        final TreeNodeData searchDto = medicationHolderDtoMapper.mapToTreeNodeDto(holderDto);
        if (vtmMap.containsKey(holderDto.getVtmId()))
        {
          vtmMap.get(holderDto.getVtmId()).getChildren().add(searchDto);
        }
        else
        {
          medicationTree.add(searchDto);
        }
        vmpMap.put(holderDto.getVmpId(), searchDto);
      }
    }

    for (final MedicationHolderDto holderDto : medicationsMap.values())
    {
      if (holderDto.getMedicationLevel() == MedicationLevelEnum.AMP)
      {
        final TreeNodeData searchDto = medicationHolderDtoMapper.mapToTreeNodeDto(holderDto);
        if (vmpMap.containsKey(holderDto.getVmpId()))
        {
          vmpMap.get(holderDto.getVmpId()).getChildren().add(searchDto);
        }
        else
        {
          medicationTree.add(searchDto);
        }
      }
    }
    return medicationTree;
  }

  List<TreeNodeData> filterMedicationsTree(
      final List<TreeNodeData> medications, final String searchString, final boolean startMustMatch)
  {
    if (searchString == null)
    {
      return medications;
    }
    final String[] searchSubstrings = searchString.split(" ");
    return filterMedicationsTree(medications, searchSubstrings, startMustMatch);
  }

  private List<TreeNodeData> filterMedicationsTree(
      final List<TreeNodeData> medications, final String[] searchSubstrings, final boolean startMustMatch)
  {
    final List<TreeNodeData> filteredMedications = new ArrayList<>();

    for (final TreeNodeData medicationNode : medications)
    {
      final MedicationSimpleDto medicationSimpleDto = (MedicationSimpleDto)medicationNode.getData();
      final String medicationSearchName =
          medicationSimpleDto.getGenericName() != null ?
          medicationSimpleDto.getGenericName() + " " + medicationNode.getTitle() :
          medicationNode.getTitle();

      medicationNode.setExpanded(false);
      boolean match = true;

      if (startMustMatch && searchSubstrings.length > 0)
      {
        final String firstSearchString = searchSubstrings[0];
        final boolean genericStartsWithFirstSearchString =
            medicationSimpleDto.getGenericName() != null &&
                StringUtils.startsWithIgnoreCase(medicationSimpleDto.getGenericName(), firstSearchString);
        final boolean medicationStartsWithFirstSearchString =
            StringUtils.startsWithIgnoreCase(medicationNode.getTitle(), firstSearchString);
        if (!genericStartsWithFirstSearchString && !medicationStartsWithFirstSearchString)
        {
          match = false;
        }
      }
      if (match)
      {
        for (int i = startMustMatch ? 1 : 0; i < searchSubstrings.length; i++)
        {
          if (!StringUtils.containsIgnoreCase(medicationSearchName, searchSubstrings[i]))
          {
            match = false;
            break;
          }
        }
      }
      if (match)
      {
        filteredMedications.add(medicationNode);
      }
      else
      {
        if (!medicationNode.getChildren().isEmpty())
        {
          final List<TreeNodeData> filteredChildren =
              filterMedicationsTree(medicationNode.getChildren(), searchSubstrings, startMustMatch);
          if (!filteredChildren.isEmpty())
          {
            medicationNode.setChildren(filteredChildren);
            filteredMedications.add(medicationNode);
            medicationNode.setExpanded(true);
          }
        }
      }
    }
    return filteredMedications;
  }
}
