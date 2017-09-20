// Generated using Marand-EHR TDO Generator vUnknown
// Source: OPENEP - Pharmacy Review Report.opt
// Time: 2016-01-22T12:48:06.146+01:00
package com.marand.openehr.medications.tdo;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.marand.openehr.tdo.EnumTerminology;
import com.marand.openehr.tdo.TdoAccess;
import com.marand.openehr.tdo.annotations.Archetype;
import com.marand.openehr.tdo.annotations.TdoNode;
import com.marand.openehr.tdo.validation.constraints.ValidCodePhrase;
import com.marand.openehr.tdo.validation.constraints.ValidDvCodedText;
import com.marand.openehr.tdo.validation.constraints.ValidString;
import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.Action;
import org.openehr.jaxb.rm.DvBoolean;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvCount;
import org.openehr.jaxb.rm.DvDate;
import org.openehr.jaxb.rm.DvDateTime;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.ItemStructure;
   
   @Archetype(name="Medication action", archetypeId="openEHR-EHR-ACTION.medication.v1")
 public class MedicationActionAction extends Action  {
               @Deprecated
   @Override
   public ItemStructure getDescription()
   {
        TdoAccess.check("Property 'Description' is deprecated in: " + getClass().getName());
    return super.getDescription();
       }

        @Deprecated
    @Override
    public void setDescription(ItemStructure value)
    {
          TdoAccess.check("Property 'Description' is deprecated in: " + getClass().getName());
     super.setDescription(value);
         }
                        private DvText medicine;

            @TdoNode(name="Medicine", path="/description[at0017:ITEM_TREE]/items[at0020:ELEMENT]/value")
     public DvText getMedicine()
    {
    return medicine;
    }

    public void setMedicine(DvText medicine)
    {
    this.medicine = medicine;
    }
     
                                         private List<DvText> instructions;

            @TdoNode(name="Instructions", path="/description[at0017:ITEM_TREE]/items[at0033:ELEMENT]/value")
     public List<DvText> getInstructions()
    {
    if (instructions == null)
    {
      instructions = new ArrayList<>();
    }

    return instructions;
    }

    public void setInstructions(List<DvText> instructions)
    {
    this.instructions = instructions;
    }

                             private IngredientsAndFormCluster ingredientsAndForm;

            @TdoNode(name="Ingredients and form", path="/description[at0017:ITEM_TREE]/items[openEHR-EHR-CLUSTER.chemical_description_mnd.v1]")
     public IngredientsAndFormCluster getIngredientsAndForm()
    {
    return ingredientsAndForm;
    }

    public void setIngredientsAndForm(IngredientsAndFormCluster ingredientsAndForm)
    {
    this.ingredientsAndForm = ingredientsAndForm;
    }
     
                                private List<DvText> reason;

            @TdoNode(name="Reason", path="/description[at0017:ITEM_TREE]/items[at0021:ELEMENT]/value")
     public List<DvText> getReason()
    {
    if (reason == null)
    {
      reason = new ArrayList<>();
    }

    return reason;
    }

    public void setReason(List<DvText> reason)
    {
    this.reason = reason;
    }

                             private StructuredDoseCluster structuredDose;

            @TdoNode(name="Structured dose", path="/description[at0017:ITEM_TREE]/items[openEHR-EHR-CLUSTER.medication_amount.v1]")
     public StructuredDoseCluster getStructuredDose()
    {
    return structuredDose;
    }

    public void setStructuredDose(StructuredDoseCluster structuredDose)
    {
    this.structuredDose = structuredDose;
    }
     
                                private DvText comment;

            @TdoNode(name="Comment", path="/description[at0017:ITEM_TREE]/items[at0024:ELEMENT]/value")
     public DvText getComment()
    {
    return comment;
    }

    public void setComment(DvText comment)
    {
    this.comment = comment;
    }
     
                                         private DvCount sequenceNumber;

            @TdoNode(name="Sequence number", path="/description[at0017:ITEM_TREE]/items[at0025:ELEMENT]/value")
     public DvCount getSequenceNumber()
    {
    return sequenceNumber;
    }

    public void setSequenceNumber(DvCount sequenceNumber)
    {
    this.sequenceNumber = sequenceNumber;
    }
     
                                         private AdministrationDetailsCluster administrationDetails;

            @TdoNode(name="Administration details", path="/description[at0017:ITEM_TREE]/items[openEHR-EHR-CLUSTER.medication_admin.v1]")
     public AdministrationDetailsCluster getAdministrationDetails()
    {
    return administrationDetails;
    }

    public void setAdministrationDetails(AdministrationDetailsCluster administrationDetails)
    {
    this.administrationDetails = administrationDetails;
    }
     
                                private DvBoolean brandSubstituted;

            @TdoNode(name="Brand substituted", path="/description[at0017:ITEM_TREE]/items[at0036:ELEMENT]/value")
     public DvBoolean getBrandSubstituted()
    {
    return brandSubstituted;
    }

    public void setBrandSubstituted(DvBoolean brandSubstituted)
    {
    this.brandSubstituted = brandSubstituted;
    }
     
                                         @Deprecated
   @Override
   public ItemStructure getProtocol()
   {
        TdoAccess.check("Property 'Protocol' is deprecated in: " + getClass().getName());
    return super.getProtocol();
       }

        @Deprecated
    @Override
    public void setProtocol(ItemStructure value)
    {
          TdoAccess.check("Property 'Protocol' is deprecated in: " + getClass().getName());
     super.setProtocol(value);
         }
                        private DvText batchID;

            @TdoNode(name="BatchID", path="/protocol[at0030:ITEM_TREE]/items[at0026:ELEMENT]/value")
     public DvText getBatchID()
    {
    return batchID;
    }

    public void setBatchID(DvText batchID)
    {
    this.batchID = batchID;
    }
     
                                         private DvDate expiryDate;

            @TdoNode(name="Expiry date", path="/protocol[at0030:ITEM_TREE]/items[at0040:ELEMENT]/value")
     public DvDate getExpiryDate()
    {
    return expiryDate;
    }

    public void setExpiryDate(DvDate expiryDate)
    {
    this.expiryDate = expiryDate;
    }
     
                                         private DvCodedText selfAdministrationType;

            @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0050|at0051|at0052"), notNull=@NotNull), notNull=@NotNull))
   @TdoNode(name="Self-administration type", path="/protocol[at0030:ITEM_TREE]/items[at0048:ELEMENT]/value")
     public DvCodedText getSelfAdministrationType()
    {
    return selfAdministrationType;
    }

    public void setSelfAdministrationType(DvCodedText selfAdministrationType)
    {
    this.selfAdministrationType = selfAdministrationType;
    }
     
                
    public SelfAdministrationType getSelfAdministrationTypeEnum()
  {
  return selfAdministrationType instanceof DvCodedText ? EnumTerminology.forEnum(SelfAdministrationType.class).getEnumByCode(
      selfAdministrationType.getDefiningCode().getCodeString()) : null;
  }

  public void setSelfAdministrationTypeEnum(SelfAdministrationType selfAdministrationType)
  {
  this.selfAdministrationType = selfAdministrationType == null ? null : DataValueUtils.getLocalCodedText(selfAdministrationType.getTerm().getCode(), selfAdministrationType.getTerm().getText());
  }
                          public enum SelfAdministrationType implements EnumTerminology.TermEnum<SelfAdministrationType>
   {
         LEVEL_1("at0050", "Level 1", "UK self-administration level 1."), 
         LEVEL_2("at0051", "Level 2", "UK self-administration level 2."), 
         LEVEL_3("at0052", "Level 3", "UK self-administration level 3.")
       ;
    SelfAdministrationType(String code, String text, String description)
   {
   terminologyTerm = new EnumTerminology.Term<>(this, code, text, description);
   }

   private final EnumTerminology.Term<SelfAdministrationType> terminologyTerm;

   public EnumTerminology.Term<SelfAdministrationType> getTerm()
   {
   return terminologyTerm;
   }
   public static final EnumTerminology<SelfAdministrationType> TERMINOLOGY = EnumTerminology.newInstance(SelfAdministrationType.class);
     }
                 private DvDateTime scheduledActionTime;

            @TdoNode(name="Scheduled action time", path="/protocol[at0030:ITEM_TREE]/items[at0043:ELEMENT]/value")
     public DvDateTime getScheduledActionTime()
    {
    return scheduledActionTime;
    }

    public void setScheduledActionTime(DvDateTime scheduledActionTime)
    {
    this.scheduledActionTime = scheduledActionTime;
    }
     
                                       }