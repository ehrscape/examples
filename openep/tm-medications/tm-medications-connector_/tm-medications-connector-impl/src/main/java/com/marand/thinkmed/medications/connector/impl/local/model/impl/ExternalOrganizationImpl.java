package com.marand.thinkmed.medications.connector.impl.local.model.impl;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.marand.thinkmed.medications.connector.impl.local.model.ExternalOrganization;

/**
 * @author Bostjan Vester
 */
@Entity
@Table(name = "ext_organization")
public class ExternalOrganizationImpl extends NamedExternalEntityImpl implements ExternalOrganization
{
}
