package com.marand.thinkmed.medications.administration.impl;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.openehr.dao.openehr.TaggingOpenEhrDao;
import com.marand.openehr.medications.tdo.MedicationAdministrationComposition;
import com.marand.openehr.rm.RmPath;
import com.marand.openehr.rm.TdoPathable;
import com.marand.thinkehr.tagging.dto.TagDto;
import com.marand.thinkmed.medications.TherapyTaggingUtils;
import com.marand.thinkmed.medications.administration.AdministrationSaver;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Nejc Korasa
 */
public class AdministrationSaverImpl implements AdministrationSaver
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private TaggingOpenEhrDao taggingOpenEhrDao;

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Autowired
  public void setTaggingOpenEhrDao(final TaggingOpenEhrDao taggingOpenEhrDao)
  {
    this.taggingOpenEhrDao = taggingOpenEhrDao;
  }

  @Override
  public String save(
      @Nonnull final String patientId,
      @Nonnull final MedicationAdministrationComposition composition,
      @Nonnull final AdministrationDto dto)
  {
    Preconditions.checkNotNull(patientId, "patientId");
    Preconditions.checkNotNull(composition, "composition");
    Preconditions.checkNotNull(dto, "dto");

    final String compositionUId = medicationsOpenEhrDao.saveMedicationAdministrationComposition(
        patientId,
        composition,
        dto.getAdministrationId());

    if (dto.getGroupUUId() != null)
    {
      final RmPath rmPath = TdoPathable.pathOfItem(composition, composition);
      final TagDto tag = new TagDto(TherapyTaggingUtils.createGroupUUIdTag(dto.getGroupUUId()), rmPath.getCanonicalString());
      taggingOpenEhrDao.tag(compositionUId, tag);
    }

    return compositionUId;
  }
}
