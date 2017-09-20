package com.marand.thinkmed.medications.dto.allergies;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningTaskDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class CheckNewAllergiesTaskDto extends AdditionalWarningTaskDto
{
  private Set<NamedExternalDto> allergies = new HashSet<>();

  public CheckNewAllergiesTaskDto(@Nonnull final String taskId, @Nonnull final Set<NamedExternalDto> allergies)
  {
    super(taskId);
    this.allergies = Preconditions.checkNotNull(allergies, "allergies must not be null!");
  }

  public Set<NamedExternalDto> getAllergies()
  {
    return allergies;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("allergies", allergies);
  }
}
