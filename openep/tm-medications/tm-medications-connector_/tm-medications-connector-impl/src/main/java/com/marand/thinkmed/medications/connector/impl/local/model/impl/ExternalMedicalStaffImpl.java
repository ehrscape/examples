package com.marand.thinkmed.medications.connector.impl.local.model.impl;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.marand.thinkmed.medications.connector.impl.local.model.ExternalMedicalStaff;

/**
 * @author Bostjan Vester
 */
@Entity
@Table(name = "ext_medical_staff")
public class ExternalMedicalStaffImpl extends NamedExternalEntityImpl implements ExternalMedicalStaff
{
}
