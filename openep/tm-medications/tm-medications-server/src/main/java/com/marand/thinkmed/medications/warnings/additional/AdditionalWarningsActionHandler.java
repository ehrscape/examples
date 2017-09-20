package com.marand.thinkmed.medications.warnings.additional;

import javax.annotation.Nonnull;

import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsActionDto;

/**
 * @author Nejc Korasa
 */
public interface AdditionalWarningsActionHandler
{
  void handleAdditionalWarningsAction(@Nonnull final AdditionalWarningsActionDto additionalWarningsActionDto);
}
