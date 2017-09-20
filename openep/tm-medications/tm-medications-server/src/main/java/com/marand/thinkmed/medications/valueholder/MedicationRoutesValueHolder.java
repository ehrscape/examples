package com.marand.thinkmed.medications.valueholder;

import java.util.Map;

import com.marand.maf.core.time.CurrentTime;
import com.marand.maf.core.valueholder.ValueHolderImpl;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;

/**
 * @author Mitja Lapajne
 */
public class MedicationRoutesValueHolder extends ValueHolderImpl<Map<Long, MedicationRouteDto>>
{
  private MedicationsDao medicationsDao;

  public void setMedicationsDao(final MedicationsDao medicationsDao)
  {
    this.medicationsDao = medicationsDao;
  }

  @Override
  protected String getKey()
  {
    return "MEDICATIONS"; //uses same key as MedicationsValueHolder for easier use
  }

  @Override
  protected Map<Long, MedicationRouteDto> loadValue()
  {
    return medicationsDao.loadRoutesMap(CurrentTime.get());
  }

  @Override
  protected long getReloadIntervalInSeconds()
  {
    return 10 * 60;
  }
}
