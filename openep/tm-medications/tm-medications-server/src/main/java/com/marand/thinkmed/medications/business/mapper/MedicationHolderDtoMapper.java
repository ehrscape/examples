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

package com.marand.thinkmed.medications.business.mapper;

import java.util.Map;
import javax.annotation.Nullable;

import com.marand.maf.core.Pair;
import com.marand.thinkmed.html.components.tree.TreeNodeData;
import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.dto.MedicationDataForTherapyDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationHolderDto;
import com.marand.thinkmed.medications.dto.MedicationSimpleDto;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Mitja Lapajne
 */
public class MedicationHolderDtoMapper
{
  private TherapyDisplayProvider therapyDisplayProvider;
  private boolean markNonFormularyMedication;

  @Required
  public void setTherapyDisplayProvider(final TherapyDisplayProvider therapyDisplayProvider)
  {
    this.therapyDisplayProvider = therapyDisplayProvider;
  }

  @Required
  public void setMarkNonFormularyMedication(final boolean markNonFormularyMedication)
  {
    this.markNonFormularyMedication = markNonFormularyMedication;
  }

  public MedicationDataForTherapyDto mapToMedicationDataForTherapyDto(
      final MedicationHolderDto holderDto, @Nullable final String careProviderId)
  {
    final MedicationDataForTherapyDto dto = new MedicationDataForTherapyDto();
    dto.setGenericName(holderDto.getGenericName());
    dto.setAntibiotic(holderDto.isAntibiotic());
    dto.setAtcGroupCode(holderDto.getAtcGroupCode());
    dto.setAtcGroupName(holderDto.getAtcGroupName());
    if (careProviderId != null)
    {
      final Map<String, Pair<String, Integer>> customGroupMap = holderDto.getCustomGroupNameSortOrder();
      final Pair<String, Integer> customGroup = customGroupMap.get(careProviderId);
      if (customGroup != null)
      {
        dto.setCustomGroupName(customGroup.getFirst());
        dto.setCustomGroupSortOrder(customGroup.getSecond());
      }
    }
    return dto;
  }

  public MedicationDto mapToMedicationDto(final MedicationHolderDto holderDto)
  {
    final MedicationDto dto = new MedicationDto();
    dto.setId(holderDto.getId());
    dto.setName(holderDto.getName());
    dto.setShortName(holderDto.getShortName());
    dto.setGenericName(holderDto.getGenericName());
    dto.setMedicationType(holderDto.getMedicationType());
    return dto;
  }

  public TreeNodeData mapToTreeNodeDto(final MedicationHolderDto holderDto)
  {
    final TreeNodeData searchDto = new TreeNodeData();
    final MedicationSimpleDto simpleDto = new MedicationSimpleDto();
    simpleDto.setId(holderDto.getId());
    simpleDto.setName(holderDto.getName());
    simpleDto.setActive(holderDto.isActive());
    simpleDto.setGenericName(holderDto.getGenericName());
    simpleDto.setOutpatientMedication(holderDto.isOutpatientMedication());
    simpleDto.setInpatientMedication(holderDto.isInpatientMedication());

    if (markNonFormularyMedication && !holderDto.isFormulary())
    {
      searchDto.setExtraClasses("non-formulary");
    }

    searchDto.setData(simpleDto);

    searchDto.setUnselectable(!holderDto.isOrderable() || !holderDto.isActive());

    if (holderDto.getMedicationLevel()== MedicationLevelEnum.VTM)
    {
      searchDto.setKey(String.valueOf(holderDto.getVtmId()));
      searchDto.setTitle(simpleDto.getName());
    }
    else if (holderDto.getMedicationLevel() == MedicationLevelEnum.VMP)
    {
      searchDto.setKey(String.valueOf(holderDto.getVmpId()));
      searchDto.setTitle(simpleDto.getName());
    }
    else if (holderDto.getMedicationLevel() == MedicationLevelEnum.AMP)
    {
      searchDto.setKey(String.valueOf(holderDto.getAmpId()));
      if (holderDto.getVmpId() == null) //amp without a parent
      {
        searchDto.setTitle(therapyDisplayProvider.getMedicationDisplay(simpleDto));
      }
      else
      {
        searchDto.setTitle(simpleDto.getName());
      }
    }
    return searchDto;
  }
}
