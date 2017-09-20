package com.marand.thinkmed.medications.connector.data.object;

import java.util.HashMap;
import java.util.Map;

import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class UserPersonDto extends NamedExternalDto
{
  private Map<String, Boolean> authorities = new HashMap<>();

  public UserPersonDto(final String id, final String name, final Map<String, Boolean> authorities)
  {
    super(id, name);
    this.authorities = authorities;
  }

  public Map<String, Boolean> getAuthorities()
  {
    return authorities;
  }

  public void setAuthorities(final Map<String, Boolean> authorities)
  {
    this.authorities = authorities;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("authorities", authorities);
  }
}


