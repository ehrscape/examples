package com.marand.thinkmed.medications.infusion;

import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.dto.administration.BolusAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */

public class AdministrationCandidateDo
{
  private final AdministrationTypeEnum administrationType;
  private final TherapyDoseDto therapyDoseDto;
  private final DateTime administrationTime;

  public AdministrationCandidateDo(final AdministrationTaskDto administrationTaskDto)
  {
    therapyDoseDto = administrationTaskDto.getTherapyDoseDto();
    administrationTime = administrationTaskDto.getPlannedAdministrationTime();
    administrationType = administrationTaskDto.getAdministrationTypeEnum();
  }

  public AdministrationCandidateDo(final AdministrationDto administrationDto)
  {
    administrationType = administrationDto.getAdministrationType();

    if (administrationDto instanceof StartAdministrationDto)
    {
      therapyDoseDto = ((StartAdministrationDto)administrationDto).getAdministeredDose();
    }
    else if (administrationDto instanceof AdjustInfusionAdministrationDto)
    {
      therapyDoseDto = ((AdjustInfusionAdministrationDto)administrationDto).getAdministeredDose();
    }
    else if (administrationDto instanceof BolusAdministrationDto)
    {
      therapyDoseDto = ((BolusAdministrationDto)administrationDto).getAdministeredDose();
    }
    else
    {
      therapyDoseDto = null;
    }

    administrationTime = administrationDto.getAdministrationTime() != null
                         ? administrationDto.getAdministrationTime()
                         : administrationDto.getPlannedTime();
  }

  public TherapyDoseDto getTherapyDose()
  {
    return therapyDoseDto;
  }

  public DateTime getAdministrationTime()
  {
    return administrationTime;
  }

  public AdministrationTypeEnum getAdministrationType()
  {
    return administrationType;
  }
}
