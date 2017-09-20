package com.marand.thinkmed.medications.connector.impl.local.model.impl;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.marand.thinkmed.medications.connector.impl.local.model.ExternalCentralCaseDisease;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalCentralCase;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalMedicalStaff;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalPatient;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;
import org.joda.time.Interval;

/**
 * @author Bostjan Vester
 */
@Entity
@Table(name = "ext_central_case")
public class ExternalCentralCaseImpl extends ExternalEntityImpl implements ExternalCentralCase
{
  private ExternalPatient patient;
  private boolean outpatient;
  private Interval effective;
  private ExternalMedicalStaff curingCareProfessional;
  private ExternalMedicalStaff supervisoryCareProfessional;
  private String roomAndBed;
  private Set<ExternalCentralCaseDisease> diseases = new HashSet<>();

  @Override
  @ManyToOne(targetEntity = ExternalPatientImpl.class, optional = false)
  public ExternalPatient getPatient()
  {
    return patient;
  }

  @Override
  public void setPatient(final ExternalPatient patient)
  {
    this.patient = patient;
  }

  @Override
  public boolean isOutpatient()
  {
    return outpatient;
  }

  @Override
  public void setOutpatient(final boolean outpatient)
  {
    this.outpatient = outpatient;
  }

  @Override
  @Type(type = "com.marand.maf.core.hibernate.type.IntervalType")
  @Columns(columns = {@Column(name = "effective_start", nullable = false), @Column(name = "effective_end", nullable = false)})
  public Interval getEffective()
  {
    return effective;
  }

  @Override
  public void setEffective(final Interval effective)
  {
    this.effective = effective;
  }

  @Override
  @ManyToOne(targetEntity = ExternalMedicalStaffImpl.class, optional = false)
  @JoinColumn(name = "curing_professional_id")
  public ExternalMedicalStaff getCuringCareProfessional()
  {
    return curingCareProfessional;
  }

  @Override
  public void setCuringCareProfessional(final ExternalMedicalStaff curingCareProfessional)
  {
    this.curingCareProfessional = curingCareProfessional;
  }

  @Override
  @ManyToOne(targetEntity = ExternalMedicalStaffImpl.class)
  @JoinColumn(name = "supervisory_professional_id")
  public ExternalMedicalStaff getSupervisoryCareProfessional()
  {
    return supervisoryCareProfessional;
  }

  @Override
  public void setSupervisoryCareProfessional(final ExternalMedicalStaff supervisoryCareProfessional)
  {
    this.supervisoryCareProfessional = supervisoryCareProfessional;
  }

  @Override
  public String getRoomAndBed()
  {
    return roomAndBed;
  }

  @Override
  public void setRoomAndBed(final String roomAndBed)
  {
    this.roomAndBed = roomAndBed;
  }

  @Override
  @OneToMany(targetEntity = ExternalCentralCaseDiseaseImpl.class, mappedBy = "centralCase")
  public Set<ExternalCentralCaseDisease> getDiseases()
  {
    return diseases;
  }

  @Override
  public void setDiseases(final Set<ExternalCentralCaseDisease> diseases)
  {
    this.diseases = diseases;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("patient", patient)
        .append("outpatient", outpatient)
        .append("effective", effective)
        .append("curingCareProfessional", curingCareProfessional)
        .append("supervisoryCareProfessional", supervisoryCareProfessional)
        .append("roomAndBed", roomAndBed)
        .append("diseases", diseases)
        ;
  }
}
