package com.marand.thinkmed.medications.connector.impl.local.model.impl;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.marand.thinkmed.medications.connector.impl.local.model.ExternalAllergy;

/**
 * @author Bostjan Vester
 */
@Entity
@Table(name = "ext_allergy")
public class ExternalAllergyImpl extends NamedExternalEntityImpl implements ExternalAllergy
{
}
