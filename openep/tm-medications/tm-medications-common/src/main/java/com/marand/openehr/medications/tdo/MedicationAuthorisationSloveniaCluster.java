// Generated using Marand-EHR TDO Generator vUnknown
// Source: ISPEK - MED - ePrescription (Slovenia).opt
// Time: 2016-11-10T09:59:00.079+01:00
package com.marand.openehr.medications.tdo;

import java.util.List;
import java.util.Locale;
import javax.validation.constraints.*;
import java.util.ArrayList;

import org.openehr.jaxb.rm.*;

import com.marand.openehr.tdo.annotations.*;
import com.marand.openehr.tdo.EnumTerminology;
import com.marand.openehr.tdo.EnumOrdinalTerminology;
import com.marand.openehr.tdo.validation.constraints.*;
import com.marand.openehr.tdo.validation.constraints.Max;
import com.marand.openehr.tdo.validation.constraints.Min;
import com.marand.openehr.tdo.validation.constraints.DecimalMax;
import com.marand.openehr.tdo.validation.constraints.DecimalMin;
import com.marand.openehr.tdo.TdoAccess;
import com.marand.openehr.util.DataValueUtils;


@Archetype(name="Medication authorisation (slovenia)", archetypeId="openEHR-EHR-CLUSTER.medication_authorisation_sl.v0")
public class MedicationAuthorisationSloveniaCluster extends Cluster  {
  @Deprecated
  @Override
  public List<Item> getItems()
  {
    TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
    return super.getItems();
  }

  private DvText packageEPrescriptionUniqueIdentifier;

  @TdoNode(name="Package ePrescription unique identifier", path="/items[at0110:ELEMENT]/value")
  public DvText getPackageEPrescriptionUniqueIdentifier()
  {
    return packageEPrescriptionUniqueIdentifier;
  }

  public void setPackageEPrescriptionUniqueIdentifier(DvText packageEPrescriptionUniqueIdentifier)
  {
    this.packageEPrescriptionUniqueIdentifier = packageEPrescriptionUniqueIdentifier;
  }

  private DvDateTime updateTimestamp;

  @TdoNode(name="Update timestamp", path="/items[at0139:ELEMENT]/value")
  public DvDateTime getUpdateTimestamp()
  {
    return updateTimestamp;
  }

  public void setUpdateTimestamp(DvDateTime updateTimestamp)
  {
    this.updateTimestamp = updateTimestamp;
  }

  private DvText ePrescriptionUniqueIdentifier;

  @TdoNode(name="ePrescription unique identifier", path="/items[at0087:ELEMENT]/value")
  public DvText getEPrescriptionUniqueIdentifier()
  {
    return ePrescriptionUniqueIdentifier;
  }

  public void setEPrescriptionUniqueIdentifier(DvText ePrescriptionUniqueIdentifier)
  {
    this.ePrescriptionUniqueIdentifier = ePrescriptionUniqueIdentifier;
  }

  private DvBoolean renewable;

  @TdoNode(name="Renewable", path="/items[at0118:ELEMENT]/value")
  public DvBoolean getRenewable()
  {
    return renewable;
  }

  public void setRenewable(DvBoolean renewable)
  {
    this.renewable = renewable;
  }

  private DvCount maximumNumberOfDispenses;

  @TdoNode(name="Maximum number of dispenses", path="/items[at0025:ELEMENT]/value")
  public DvCount getMaximumNumberOfDispenses()
  {
    return maximumNumberOfDispenses;
  }

  public void setMaximumNumberOfDispenses(DvCount maximumNumberOfDispenses)
  {
    this.maximumNumberOfDispenses = maximumNumberOfDispenses;
  }

  private DvCount numberOfRemainingDispenses;

  @TdoNode(name="Number of remaining dispenses", path="/items[at0086:ELEMENT]/value")
  public DvCount getNumberOfRemainingDispenses()
  {
    return numberOfRemainingDispenses;
  }

  public void setNumberOfRemainingDispenses(DvCount numberOfRemainingDispenses)
  {
    this.numberOfRemainingDispenses = numberOfRemainingDispenses;
  }

  private DvCodedText typeOfPrescription;

  @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0092|at0093|at0094"), notNull=@NotNull), notNull=@NotNull))
  @TdoNode(name="Type of prescription", path="/items[at0091:ELEMENT]/value")
  public DvCodedText getTypeOfPrescription()
  {
    return typeOfPrescription;
  }

  public void setTypeOfPrescription(DvCodedText typeOfPrescription)
  {
    this.typeOfPrescription = typeOfPrescription;
  }


  public TypeOfPrescription getTypeOfPrescriptionEnum()
  {
    return typeOfPrescription instanceof DvCodedText ? EnumTerminology.forEnum(TypeOfPrescription.class).getEnumByCode(((DvCodedText)typeOfPrescription).getDefiningCode().getCodeString()) : null;
  }

  public void setTypeOfPrescriptionEnum(TypeOfPrescription typeOfPrescription)
  {
    this.typeOfPrescription = typeOfPrescription == null ? null : DataValueUtils.getLocalCodedText(typeOfPrescription.getTerm().getCode(), typeOfPrescription.getTerm().getText());
  }
  public enum TypeOfPrescription implements EnumTerminology.TermEnum<TypeOfPrescription>
  {
    BRAND_NAME("at0092", "Brand name", "Name of the brand of a medicine, vaccine or other therapeutic good."),
    MAGISTRAL("at0093", "Magistral", "Magitral prescription."),
    INN("at0094", "INN", "Generic name prescription. International Nonproprietary Name ")
    ;
    TypeOfPrescription(String code, String text, String description)
    {
      terminologyTerm = new EnumTerminology.Term<TypeOfPrescription>(this, code, text, description);
    }

    private final EnumTerminology.Term<TypeOfPrescription> terminologyTerm;

    public EnumTerminology.Term<TypeOfPrescription> getTerm()
    {
      return terminologyTerm;
    }
    public static final EnumTerminology<TypeOfPrescription> TERMINOLOGY = EnumTerminology.newInstance(TypeOfPrescription.class);
  }
  private DvCodedText prescriptionDocumentType;

  @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0080|at0081"), notNull=@NotNull), notNull=@NotNull))
  @TdoNode(name="Prescription document type", path="/items[at0077:ELEMENT]/value")
  public DvCodedText getPrescriptionDocumentType()
  {
    return prescriptionDocumentType;
  }

  public void setPrescriptionDocumentType(DvCodedText prescriptionDocumentType)
  {
    this.prescriptionDocumentType = prescriptionDocumentType;
  }


  public PrescriptionDocumentType getPrescriptionDocumentTypeEnum()
  {
    return prescriptionDocumentType instanceof DvCodedText ? EnumTerminology.forEnum(PrescriptionDocumentType.class).getEnumByCode(((DvCodedText)prescriptionDocumentType).getDefiningCode().getCodeString()) : null;
  }

  public void setPrescriptionDocumentTypeEnum(PrescriptionDocumentType prescriptionDocumentType)
  {
    this.prescriptionDocumentType = prescriptionDocumentType == null ? null : DataValueUtils.getLocalCodedText(prescriptionDocumentType.getTerm().getCode(), prescriptionDocumentType.getTerm().getText());
  }
  public enum PrescriptionDocumentType implements EnumTerminology.TermEnum<PrescriptionDocumentType>
  {
    GREEN("at0080", "Green", "Prescription paid by health insurance."),
    WHITE("at0081", "White", "The patient pays for the prescription.")
    ;
    PrescriptionDocumentType(String code, String text, String description)
    {
      terminologyTerm = new EnumTerminology.Term<PrescriptionDocumentType>(this, code, text, description);
    }

    private final EnumTerminology.Term<PrescriptionDocumentType> terminologyTerm;

    public EnumTerminology.Term<PrescriptionDocumentType> getTerm()
    {
      return terminologyTerm;
    }
    public static final EnumTerminology<PrescriptionDocumentType> TERMINOLOGY = EnumTerminology.newInstance(PrescriptionDocumentType.class);
  }
  private DvCodedText prescriptionStatus;

  @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0130|at0131|at0132|at0133|at0134|at0135|at0136|at0141|at0142|at0143"), notNull=@NotNull), notNull=@NotNull))
  @TdoNode(name="Prescription status", path="/items[at0085:ELEMENT]/value")
  public DvCodedText getPrescriptionStatus()
  {
    return prescriptionStatus;
  }

  public void setPrescriptionStatus(DvCodedText prescriptionStatus)
  {
    this.prescriptionStatus = prescriptionStatus;
  }


  public PrescriptionStatus getPrescriptionStatusEnum()
  {
    return prescriptionStatus instanceof DvCodedText ? EnumTerminology.forEnum(PrescriptionStatus.class).getEnumByCode(((DvCodedText)prescriptionStatus).getDefiningCode().getCodeString()) : null;
  }

  public void setPrescriptionStatusEnum(PrescriptionStatus prescriptionStatus)
  {
    this.prescriptionStatus = prescriptionStatus == null ? null : DataValueUtils.getLocalCodedText(prescriptionStatus.getTerm().getCode(), prescriptionStatus.getTerm().getText());
  }
  public enum PrescriptionStatus implements EnumTerminology.TermEnum<PrescriptionStatus>
  {
    PRESCRIBED("at0130", "Prescribed", "*"),
    PARTIALLY_USED("at0131", "Partially used", "*"),
    PARTIALLY_USED_AND_CANCELLED("at0132", "Partially used and cancelled", "*"),
    CANCELLED("at0133", "Cancelled", "*"),
    USED("at0134", "Used", "*"),
    IN_PREPARATION("at0135", "In preparation", "*"),
    IN_DISPENSE("at0136", "In dispense", "*"),
    REJECTED("at0141", "Rejected", "*"),
    WITHDRAWN("at0142", "Withdrawn", "*"),
    PARTIALLY_USED_AND_REJECTED("at0143", "Partially used and rejected", "*")
    ;
    PrescriptionStatus(String code, String text, String description)
    {
      terminologyTerm = new EnumTerminology.Term<PrescriptionStatus>(this, code, text, description);
    }

    private final EnumTerminology.Term<PrescriptionStatus> terminologyTerm;

    public EnumTerminology.Term<PrescriptionStatus> getTerm()
    {
      return terminologyTerm;
    }
    public static final EnumTerminology<PrescriptionStatus> TERMINOLOGY = EnumTerminology.newInstance(PrescriptionStatus.class);
  }
  private DvCodedText surchargeType;

  @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0096|at0097|at0098|at0099"), notNull=@NotNull), notNull=@NotNull))
  @TdoNode(name="Surcharge type", path="/items[at0095:ELEMENT]/value")
  public DvCodedText getSurchargeType()
  {
    return surchargeType;
  }

  public void setSurchargeType(DvCodedText surchargeType)
  {
    this.surchargeType = surchargeType;
  }


  public SurchargeType getSurchargeTypeEnum()
  {
    return surchargeType instanceof DvCodedText ? EnumTerminology.forEnum(SurchargeType.class).getEnumByCode(((DvCodedText)surchargeType).getDefiningCode().getCodeString()) : null;
  }

  public void setSurchargeTypeEnum(SurchargeType surchargeType)
  {
    this.surchargeType = surchargeType == null ? null : DataValueUtils.getLocalCodedText(surchargeType.getTerm().getCode(), surchargeType.getTerm().getText());
  }
  public enum SurchargeType implements EnumTerminology.TermEnum<SurchargeType>
  {
    WITHOUT_ADDITIONAL_PAYMENT("at0096", "Without additional payment", "Without additional payment."),
    INSURANCE("at0097", "Insurance", "Insurance"),
    INSURANCE_COMPANY("at0098", "Insurance company", "Insurance company"),
    FROM_THE_STATE_BUDGET("at0099", "From the state budget", "From the state budget")
    ;
    SurchargeType(String code, String text, String description)
    {
      terminologyTerm = new EnumTerminology.Term<SurchargeType>(this, code, text, description);
    }

    private final EnumTerminology.Term<SurchargeType> terminologyTerm;

    public EnumTerminology.Term<SurchargeType> getTerm()
    {
      return terminologyTerm;
    }
    public static final EnumTerminology<SurchargeType> TERMINOLOGY = EnumTerminology.newInstance(SurchargeType.class);
  }
  private DvCodedText payer;

  @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0119|at0120|at0121|at0122"), notNull=@NotNull), notNull=@NotNull))
  @TdoNode(name="Payer", path="/items[at0100:ELEMENT]/value")
  public DvCodedText getPayer()
  {
    return payer;
  }

  public void setPayer(DvCodedText payer)
  {
    this.payer = payer;
  }


  public Payer getPayerEnum()
  {
    return payer instanceof DvCodedText ? EnumTerminology.forEnum(Payer.class).getEnumByCode(((DvCodedText)payer).getDefiningCode().getCodeString()) : null;
  }

  public void setPayerEnum(Payer payer)
  {
    this.payer = payer == null ? null : DataValueUtils.getLocalCodedText(payer.getTerm().getCode(), payer.getTerm().getText());
  }
  public enum Payer implements EnumTerminology.TermEnum<Payer>
  {
    PERSON("at0119", "Person", "Person"),
    UPB("at0120", "UPB", "The Office for Immigrants and Refugees"),
    MO("at0121", "MO", "Department of Defense"),
    OTHER("at0122", "Other", "Other")
    ;
    Payer(String code, String text, String description)
    {
      terminologyTerm = new EnumTerminology.Term<Payer>(this, code, text, description);
    }

    private final EnumTerminology.Term<Payer> terminologyTerm;

    public EnumTerminology.Term<Payer> getTerm()
    {
      return terminologyTerm;
    }
    public static final EnumTerminology<Payer> TERMINOLOGY = EnumTerminology.newInstance(Payer.class);
  }
  private DvCodedText typeOfSupply;

  @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0104|at0105"), notNull=@NotNull), notNull=@NotNull))
  @TdoNode(name="Type of supply", path="/items[at0103:ELEMENT]/value")
  public DvCodedText getTypeOfSupply()
  {
    return typeOfSupply;
  }

  public void setTypeOfSupply(DvCodedText typeOfSupply)
  {
    this.typeOfSupply = typeOfSupply;
  }


  public TypeOfSupply getTypeOfSupplyEnum()
  {
    return typeOfSupply instanceof DvCodedText ? EnumTerminology.forEnum(TypeOfSupply.class).getEnumByCode(((DvCodedText)typeOfSupply).getDefiningCode().getCodeString()) : null;
  }

  public void setTypeOfSupplyEnum(TypeOfSupply typeOfSupply)
  {
    this.typeOfSupply = typeOfSupply == null ? null : DataValueUtils.getLocalCodedText(typeOfSupply.getTerm().getCode(), typeOfSupply.getTerm().getText());
  }
  public enum TypeOfSupply implements EnumTerminology.TermEnum<TypeOfSupply>
  {
    BY_PACKAGE("at0104", "By package", "The type of dispense supply by package of the medication, vaccine or other therapeutic good."),
    BY_DAY("at0105", "By day", "Type of dispense supply by days of the medication, vaccine or other tehrapeutic good.")
    ;
    TypeOfSupply(String code, String text, String description)
    {
      terminologyTerm = new EnumTerminology.Term<TypeOfSupply>(this, code, text, description);
    }

    private final EnumTerminology.Term<TypeOfSupply> terminologyTerm;

    public EnumTerminology.Term<TypeOfSupply> getTerm()
    {
      return terminologyTerm;
    }
    public static final EnumTerminology<TypeOfSupply> TERMINOLOGY = EnumTerminology.newInstance(TypeOfSupply.class);
  }
  private DvCount supplyQuantity;

  @TdoNode(name="Supply quantity", path="/items[at0138:ELEMENT]/value")
  public DvCount getSupplyQuantity()
  {
    return supplyQuantity;
  }

  public void setSupplyQuantity(DvCount supplyQuantity)
  {
    this.supplyQuantity = supplyQuantity;
  }

  private DvCount supplyDuration;

  @TdoNode(name="Supply duration", path="/items[at0137:ELEMENT]/value")
  public DvCount getSupplyDuration()
  {
    return supplyDuration;
  }

  public void setSupplyDuration(DvCount supplyDuration)
  {
    this.supplyDuration = supplyDuration;
  }

  private DvText additionalInstructionsForPharmacist;

  @TdoNode(name="Additional instructions for pharmacist", path="/items[at0106:ELEMENT]/value")
  public DvText getAdditionalInstructionsForPharmacist()
  {
    return additionalInstructionsForPharmacist;
  }

  public void setAdditionalInstructionsForPharmacist(DvText additionalInstructionsForPharmacist)
  {
    this.additionalInstructionsForPharmacist = additionalInstructionsForPharmacist;
  }

  private DvBoolean doNotSwitch;

  @TdoNode(name="Do not switch", path="/items[at0111:ELEMENT]/value")
  public DvBoolean getDoNotSwitch()
  {
    return doNotSwitch;
  }

  public void setDoNotSwitch(DvBoolean doNotSwitch)
  {
    this.doNotSwitch = doNotSwitch;
  }

  private DvBoolean urgent;

  @TdoNode(name="Urgent", path="/items[at0140:ELEMENT]/value")
  public DvBoolean getUrgent()
  {
    return urgent;
  }

  public void setUrgent(DvBoolean urgent)
  {
    this.urgent = urgent;
  }

  private DvBoolean interactions;

  @TdoNode(name="Interactions", path="/items[at0112:ELEMENT]/value")
  public DvBoolean getInteractions()
  {
    return interactions;
  }

  public void setInteractions(DvBoolean interactions)
  {
    this.interactions = interactions;
  }

  private DvBoolean maximumDoseExceeded;

  @TdoNode(name="Maximum dose exceeded", path="/items[at0129:ELEMENT]/value")
  public DvBoolean getMaximumDoseExceeded()
  {
    return maximumDoseExceeded;
  }

  public void setMaximumDoseExceeded(DvBoolean maximumDoseExceeded)
  {
    this.maximumDoseExceeded = maximumDoseExceeded;
  }

  private DvCodedText illnessConditionType;

  @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0083|at0084"), notNull=@NotNull), notNull=@NotNull))
  @TdoNode(name="Illness condition type", path="/items[at0082:ELEMENT]/value")
  public DvCodedText getIllnessConditionType()
  {
    return illnessConditionType;
  }

  public void setIllnessConditionType(DvCodedText illnessConditionType)
  {
    this.illnessConditionType = illnessConditionType;
  }


  public IllnessConditionType getIllnessConditionTypeEnum()
  {
    return illnessConditionType instanceof DvCodedText ? EnumTerminology.forEnum(IllnessConditionType.class).getEnumByCode(((DvCodedText)illnessConditionType).getDefiningCode().getCodeString()) : null;
  }

  public void setIllnessConditionTypeEnum(IllnessConditionType illnessConditionType)
  {
    this.illnessConditionType = illnessConditionType == null ? null : DataValueUtils.getLocalCodedText(illnessConditionType.getTerm().getCode(), illnessConditionType.getTerm().getText());
  }
  public enum IllnessConditionType implements EnumTerminology.TermEnum<IllnessConditionType>
  {
    ACUTE_CONDITION("at0083", "Acute condition", "Acute illness."),
    CHRONIC_CONDITION("at0084", "Chronic condition", "Chronic illness.")
    ;
    IllnessConditionType(String code, String text, String description)
    {
      terminologyTerm = new EnumTerminology.Term<IllnessConditionType>(this, code, text, description);
    }

    private final EnumTerminology.Term<IllnessConditionType> terminologyTerm;

    public EnumTerminology.Term<IllnessConditionType> getTerm()
    {
      return terminologyTerm;
    }
    public static final EnumTerminology<IllnessConditionType> TERMINOLOGY = EnumTerminology.newInstance(IllnessConditionType.class);
  }
  private DvCodedText treatmentReason;

  @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0124|at0125|at0126|at0127|at0128"), notNull=@NotNull), notNull=@NotNull))
  @TdoNode(name="Treatment reason", path="/items[at0123:ELEMENT]/value")
  public DvCodedText getTreatmentReason()
  {
    return treatmentReason;
  }

  public void setTreatmentReason(DvCodedText treatmentReason)
  {
    this.treatmentReason = treatmentReason;
  }


  public TreatmentReason getTreatmentReasonEnum()
  {
    return treatmentReason instanceof DvCodedText ? EnumTerminology.forEnum(TreatmentReason.class).getEnumByCode(((DvCodedText)treatmentReason).getDefiningCode().getCodeString()) : null;
  }

  public void setTreatmentReasonEnum(TreatmentReason treatmentReason)
  {
    this.treatmentReason = treatmentReason == null ? null : DataValueUtils.getLocalCodedText(treatmentReason.getTerm().getCode(), treatmentReason.getTerm().getText());
  }
  public enum TreatmentReason implements EnumTerminology.TermEnum<TreatmentReason>
  {
    DISEASE("at0124", "Disease", "*"),
    INJURY_OUTSIDE_WORK("at0125", "Injury outside work", "*"),
    INDUSTRIAL_DISEASE("at0126", "Industrial disease", "*"),
    OCCUPATIONAL_INJURY("at0127", "Occupational injury", "*"),
    THIRD_PERSON_INJURY("at0128", "Third - person injury", "*")
    ;
    TreatmentReason(String code, String text, String description)
    {
      terminologyTerm = new EnumTerminology.Term<TreatmentReason>(this, code, text, description);
    }

    private final EnumTerminology.Term<TreatmentReason> terminologyTerm;

    public EnumTerminology.Term<TreatmentReason> getTerm()
    {
      return terminologyTerm;
    }
    public static final EnumTerminology<TreatmentReason> TERMINOLOGY = EnumTerminology.newInstance(TreatmentReason.class);
  }
}