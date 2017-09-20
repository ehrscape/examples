package com.marand.thinkmed.medications.valueholder;

import java.util.Map;

import com.marand.maf.core.time.CurrentTime;
import com.marand.maf.core.valueholder.ValueHolderImpl;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dto.MedicationHolderDto;

/**
 * @author Mitja Lapajne
 */
public class MedicationsValueHolder extends ValueHolderImpl<Map<Long, MedicationHolderDto>>
{
  private MedicationsDao medicationsDao;

  public void setMedicationsDao(final MedicationsDao medicationsDao)
  {
    this.medicationsDao = medicationsDao;
  }

  @Override
  protected String getKey()
  {
    return "MEDICATIONS";
  }

  @Override
  protected Map<Long, MedicationHolderDto> loadValue()
  {
    return medicationsDao.loadMedicationsMap(CurrentTime.get());
  }

  @Override
  protected long getReloadIntervalInSeconds()
  {
    return 10*60;
  }
}
