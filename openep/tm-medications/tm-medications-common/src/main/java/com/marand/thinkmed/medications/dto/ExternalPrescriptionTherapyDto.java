package com.marand.thinkmed.medications.dto;

import com.marand.openehr.medications.tdo.MedicationAuthorisationSloveniaCluster;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Miha Anzicek
 */
public class ExternalPrescriptionTherapyDto extends PrescriptionDto
{
  private DateTime issueDate;
  private boolean deliverToDoctor;
  private boolean renewable;
  private PrescriptionSupplyType prescriptionSupplyType;
  private Long prescribedRepeats;
  private String numberOfPrescribedUnits;
  private String prescribedUnit;
  private String notificationToDoctor;
  private boolean urgent;
  private boolean doNotChange;
  private String notificationToPatient;
  private String nationalMedicationCode;
  private String nationalPrescribedMedicationCode;
  private String shortMedicationName;
  private String shortPrescribedMedicationName;
  private String prescriptionClassification;
  private String consultation;
  private String prescribedMedicationQuantity;
  private MedicationAuthorisationSloveniaCluster.SurchargeType surchargeType;
  private MedicationAuthorisationSloveniaCluster.TreatmentReason treatmentReason;
  private MedicationAuthorisationSloveniaCluster.IllnessConditionType illnessConditionType;
  private MedicationAuthorisationSloveniaCluster.PrescriptionDocumentType prescriptionDocumentType;
  private boolean issuedWithoutPrescription;
  private boolean prescribedMedicationSwitched;
  private String pharmacistBPI;
  private String notificationToPharmacist;
  private String prescribingIssuedMedicationId;
  private String prescribedIssuedMedicationId;
  private String issuingOrgBPI;
  private String medicationIssuingMode;
  private String prescriberBPI;
  private String prescribingOrgBPI;

  public DateTime getIssueDate()
  {
    return issueDate;
  }

  public void setIssueDate(final DateTime issueDate)
  {
    this.issueDate = issueDate;
  }

  public boolean isDeliverToDoctor()
  {
    return deliverToDoctor;
  }

  public void setDeliverToDoctor(final boolean deliverToDoctor)
  {
    this.deliverToDoctor = deliverToDoctor;
  }

  public boolean isRenewable()
  {
    return renewable;
  }

  public void setRenewable(final boolean renewable)
  {
    this.renewable = renewable;
  }

  public PrescriptionSupplyType getPrescriptionSupplyType()
  {
    return prescriptionSupplyType;
  }

  public void setPrescriptionSupplyType(final PrescriptionSupplyType prescriptionSupplyType)
  {
    this.prescriptionSupplyType = prescriptionSupplyType;
  }

  public Long getPrescribedRepeats()
  {
    return prescribedRepeats;
  }

  public void setPrescribedRepeats(final Long prescribedRepeats)
  {
    this.prescribedRepeats = prescribedRepeats;
  }

  public String getNumberOfPrescribedUnits()
  {
    return numberOfPrescribedUnits;
  }

  public void setNumberOfPrescribedUnits(final String numberOfPrescribedUnits)
  {
    this.numberOfPrescribedUnits = numberOfPrescribedUnits;
  }

  public String getPrescribedUnit()
  {
    return prescribedUnit;
  }

  public void setPrescribedUnit(final String prescribedUnit)
  {
    this.prescribedUnit = prescribedUnit;
  }

  public String getNotificationToDoctor()
  {
    return notificationToDoctor;
  }

  public void setNotificationToDoctor(final String notificationToDoctor)
  {
    this.notificationToDoctor = notificationToDoctor;
  }

  public boolean isUrgent()
  {
    return urgent;
  }

  public void setUrgent(final boolean urgent)
  {
    this.urgent = urgent;
  }

  public boolean isDoNotChange()
  {
    return doNotChange;
  }

  public void setDoNotChange(final boolean doNotChange)
  {
    this.doNotChange = doNotChange;
  }

  public String getNotificationToPatient()
  {
    return notificationToPatient;
  }

  public void setNotificationToPatient(final String notificationToPatient)
  {
    this.notificationToPatient = notificationToPatient;
  }

  public String getNationalMedicationCode()
  {
    return nationalMedicationCode;
  }

  public void setNationalMedicationCode(final String nationalMedicationCode)
  {
    this.nationalMedicationCode = nationalMedicationCode;
  }

  public String getNationalPrescribedMedicationCode()
  {
    return nationalPrescribedMedicationCode;
  }

  public void setNationalPrescribedMedicationCode(final String nationalPrescribedMedicationCode)
  {
    this.nationalPrescribedMedicationCode = nationalPrescribedMedicationCode;
  }

  public String getShortMedicationName()
  {
    return shortMedicationName;
  }

  public void setShortMedicationName(final String shortMedicationName)
  {
    this.shortMedicationName = shortMedicationName;
  }

  public String getShortPrescribedMedicationName()
  {
    return shortPrescribedMedicationName;
  }

  public void setShortPrescribedMedicationName(final String shortPrescribedMedicationName)
  {
    this.shortPrescribedMedicationName = shortPrescribedMedicationName;
  }

  public String getPrescriptionClassification()
  {
    return prescriptionClassification;
  }

  public void setPrescriptionClassification(final String prescriptionClassification)
  {
    this.prescriptionClassification = prescriptionClassification;
  }

  public String getConsultation()
  {
    return consultation;
  }

  public void setConsultation(final String consultation)
  {
    this.consultation = consultation;
  }

  public String getPrescribedMedicationQuantity()
  {
    return prescribedMedicationQuantity;
  }

  public void setPrescribedMedicationQuantity(final String prescribedMedicationQuantity)
  {
    this.prescribedMedicationQuantity = prescribedMedicationQuantity;
  }

  public MedicationAuthorisationSloveniaCluster.SurchargeType getSurchargeType()
  {
    return surchargeType;
  }

  public void setSurchargeType(final MedicationAuthorisationSloveniaCluster.SurchargeType surchargeType)
  {
    this.surchargeType = surchargeType;
  }

  public MedicationAuthorisationSloveniaCluster.TreatmentReason getTreatmentReason()
  {
    return treatmentReason;
  }

  public void setTreatmentReason(final MedicationAuthorisationSloveniaCluster.TreatmentReason treatmentReason)
  {
    this.treatmentReason = treatmentReason;
  }

  public MedicationAuthorisationSloveniaCluster.IllnessConditionType getIllnessConditionType()
  {
    return illnessConditionType;
  }

  public void setIllnessConditionType(final MedicationAuthorisationSloveniaCluster.IllnessConditionType illnessConditionType)
  {
    this.illnessConditionType = illnessConditionType;
  }

  public MedicationAuthorisationSloveniaCluster.PrescriptionDocumentType getPrescriptionDocumentType()
  {
    return prescriptionDocumentType;
  }

  public void setPrescriptionDocumentType(final MedicationAuthorisationSloveniaCluster.PrescriptionDocumentType prescriptionDocumentType)
  {
    this.prescriptionDocumentType = prescriptionDocumentType;
  }

  public boolean issuedWithoutPrescription()
  {
    return issuedWithoutPrescription;
  }

  public void setIssuedWithoutPrescription(final boolean issuedWithoutPrescription)
  {
    this.issuedWithoutPrescription = issuedWithoutPrescription;
  }

  public boolean isPrescribedMedicationSwitched()
  {
    return prescribedMedicationSwitched;
  }

  public void setPrescribedMedicationSwitched(final boolean prescribedMedicationSwitched)
  {
    this.prescribedMedicationSwitched = prescribedMedicationSwitched;
  }

  public String getPharmacistBPI()
  {
    return pharmacistBPI;
  }

  public void setPharmacistBPI(final String pharmacistBPI)
  {
    this.pharmacistBPI = pharmacistBPI;
  }

  public String getNotificationToPharmacist()
  {
    return notificationToPharmacist;
  }

  public void setNotificationToPharmacist(final String notificationToPharmacist)
  {
    this.notificationToPharmacist = notificationToPharmacist;
  }

  public String getPrescribingIssuedMedicationId()
  {
    return prescribingIssuedMedicationId;
  }

  public void setPrescribingIssuedMedicationId(final String prescribingIssuedMedicationId)
  {
    this.prescribingIssuedMedicationId = prescribingIssuedMedicationId;
  }

  public String getPrescribedIssuedMedicationId()
  {
    return prescribedIssuedMedicationId;
  }

  public void setPrescribedIssuedMedicationId(final String prescribedIssuedMedicationId)
  {
    this.prescribedIssuedMedicationId = prescribedIssuedMedicationId;
  }

  public String getIssuingOrgBPI()
  {
    return issuingOrgBPI;
  }

  public void setIssuingOrgBPI(final String issuingOrgBPI)
  {
    this.issuingOrgBPI = issuingOrgBPI;
  }

  public String getMedicationIssuingMode()
  {
    return medicationIssuingMode;
  }

  public void setMedicationIssuingMode(final String medicationIssuingMode)
  {
    this.medicationIssuingMode = medicationIssuingMode;
  }

  public String getPrescriberBPI()
  {
    return prescriberBPI;
  }

  public void setPrescriberBPI(final String prescriberBPI)
  {
    this.prescriberBPI = prescriberBPI;
  }

  public String getPrescribingOrgBPI()
  {
    return prescribingOrgBPI;
  }

  public void setPrescribingOrgBPI(final String prescribingOrgBPI)
  {
    this.prescribingOrgBPI = prescribingOrgBPI;
  }


  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("issueDate", issueDate)
        .append("deliverToDoctor", deliverToDoctor)
        .append("renewable", renewable)
        .append("prescriptionSupplyType", prescriptionSupplyType)
        .append("prescribedRepeats", prescribedRepeats)
        .append("numberOfPrescribedUnits", numberOfPrescribedUnits)
        .append("prescribedUnit", prescribedUnit)
        .append("notificationToDoctor", notificationToDoctor)
        .append("urgent", urgent)
        .append("doNotChange", doNotChange)
        .append("notificationToPatient", notificationToPatient)
        .append("nationalMedicationCode", nationalMedicationCode)
        .append("nationalPrescribedMedicationCode", nationalPrescribedMedicationCode)
        .append("shortMedicationName", shortMedicationName)
        .append("shortPrescribedMedicationName", shortPrescribedMedicationName)
        .append("prescriptionClassification", prescriptionClassification)
        .append("consultation", consultation)
        .append("prescribedMedicationQuantity", prescribedMedicationQuantity)
        .append("surchargeType", surchargeType)
        .append("treatmentReason", treatmentReason)
        .append("illnessConditionType", illnessConditionType)
        .append("prescriptionDocumentType", prescriptionDocumentType)
        .append("issuedWithoutPrescription", issuedWithoutPrescription)
        .append("prescribedMedicationSwitched", prescribedMedicationSwitched)
        .append("pharmacistBPI", pharmacistBPI)
        .append("notificationToPharmacist", notificationToPharmacist)
        .append("prescribingIssuedMedicationId", prescribingIssuedMedicationId)
        .append("prescribedIssuedMedicationId", prescribedIssuedMedicationId)
        .append("issuingOrgBPI", issuingOrgBPI)
        .append("medicationIssuingMode", medicationIssuingMode)
        .append("prescriberBPI", prescriberBPI)
        .append("prescribingOrgBPI", prescribingOrgBPI)
    ;
  }
}
