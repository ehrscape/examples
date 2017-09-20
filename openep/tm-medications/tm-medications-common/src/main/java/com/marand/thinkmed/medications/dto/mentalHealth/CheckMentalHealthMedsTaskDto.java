package com.marand.thinkmed.medications.dto.mentalHealth;

import javax.annotation.Nonnull;

import com.marand.thinkmed.medications.dto.warning.AdditionalWarningTaskDto;

/**
 * @author Nejc Korasa
 */
public class CheckMentalHealthMedsTaskDto extends AdditionalWarningTaskDto
{
  public CheckMentalHealthMedsTaskDto(@Nonnull final String taskId)
  {
    super(taskId);
  }
}
