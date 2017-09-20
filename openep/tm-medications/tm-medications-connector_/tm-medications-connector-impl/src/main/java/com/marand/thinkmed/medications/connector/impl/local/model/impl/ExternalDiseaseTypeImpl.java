package com.marand.thinkmed.medications.connector.impl.local.model.impl;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.marand.thinkmed.medications.connector.impl.local.model.ExternalDiseaseType;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 */
@Entity
@Table(name = "ext_disease_type")
public class ExternalDiseaseTypeImpl extends NamedExternalEntityImpl implements ExternalDiseaseType
{
  private String code;

  @Override
  public String getCode()
  {
    return code;
  }

  @Override
  public void setCode(final String code)
  {
    this.code = code;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("code", code)
        ;
  }
}
