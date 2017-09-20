// Generated using Marand-EHR TDO Generator vUnknown
// Source: ISPEK - MED - ePrescription (Slovenia).opt
// Time: 2016-01-22T12:48:05.725+01:00
package com.marand.openehr.medications.tdo;

import java.util.ArrayList;
import java.util.List;

import com.marand.ispek.ehr.common.tdo.CompositionEventContext;
import com.marand.ispek.ehr.common.tdo.IspekComposition;
import com.marand.openehr.tdo.EnumTerminology;
import com.marand.openehr.tdo.TdoAccess;
import com.marand.openehr.tdo.annotations.Archetype;
import com.marand.openehr.tdo.annotations.TdoNode;
import com.marand.openehr.tdo.annotations.Template;
import org.openehr.jaxb.rm.Composition;
import org.openehr.jaxb.rm.ContentItem;
import org.openehr.jaxb.rm.EventContext;
import org.openehr.jaxb.rm.Section;
   
   @Template(name="ePrescription (Slovenia)", templateId="ISPEK - MED - ePrescription (Slovenia)")
   @Archetype(name="ePrescription (Slovenia)", archetypeId="openEHR-EHR-COMPOSITION.encounter.v1")
 public class EPrescriptionSloveniaComposition extends Composition   implements   IspekComposition  {
               @Deprecated
   @Override
   public EventContext getContext()
   {
        TdoAccess.check("Property 'Context' is deprecated in: " + getClass().getName());
    return super.getContext();
       }

        @Deprecated
    @Override
    public void setContext(EventContext value)
    {
          TdoAccess.check("Property 'Context' is deprecated in: " + getClass().getName());
     super.setContext(value);
         }
                        private CompositionEventContext compositionEventContext;

            @TdoNode(name="CompositionEventContext", path="/context")
     public CompositionEventContext getCompositionEventContext()
    {
    return compositionEventContext;
    }

    public void setCompositionEventContext(CompositionEventContext compositionEventContext)
    {
    this.compositionEventContext = compositionEventContext;
    }
     
                                @Deprecated
   @Override
   public List<ContentItem> getContent()
   {
        TdoAccess.check("Property 'Content' is deprecated in: " + getClass().getName());
    return super.getContent();
       }

                        private MedicationDetailSection medicationDetail;

            @TdoNode(name="Medication detail", path="/content[openEHR-EHR-SECTION.medication.v1]")
     public MedicationDetailSection getMedicationDetail()
    {
    return medicationDetail;
    }

    public void setMedicationDetail(MedicationDetailSection medicationDetail)
    {
    this.medicationDetail = medicationDetail;
    }
     
                       @Archetype(name="Medication detail", archetypeId="openEHR-EHR-SECTION.medication.v1")
   public static class MedicationDetailSection extends Section    {
                  @Deprecated
   @Override
   public List<ContentItem> getItems()
   {
        TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
    return super.getItems();
       }

                        private List<MedicationInstructionInstruction> medicationInstruction;

            @TdoNode(name="Medication instruction", path="/items[openEHR-EHR-INSTRUCTION.medication.v1]")
     public List<MedicationInstructionInstruction> getMedicationInstruction()
    {
    if (medicationInstruction == null)
    {
      medicationInstruction = new ArrayList<>();
    }

    return medicationInstruction;
    }

    public void setMedicationInstruction(List<MedicationInstructionInstruction> medicationInstruction)
    {
    this.medicationInstruction = medicationInstruction;
    }

                                private List<MedicationActionAction> medicationAction;

            @TdoNode(name="Medication action", path="/items[openEHR-EHR-ACTION.medication.v1]")
     public List<MedicationActionAction> getMedicationAction()
    {
    if (medicationAction == null)
    {
      medicationAction = new ArrayList<>();
    }

    return medicationAction;
    }

    public void setMedicationAction(List<MedicationActionAction> medicationAction)
    {
    this.medicationAction = medicationAction;
    }

                          public enum Role implements EnumTerminology.TermEnum<Role>
   {
         THERAPEUTIC("at0006", "Therapeutic", "The chemical has a known and desired effect which is positive."), 
         ELECTROLYTE("at0035", "Electrolyte", "This ingredient is an electrolyte."), 
         TOXIC("at0007", "Toxic", "This chemical is toxic and has no therapeutic effect."), 
         ADJUVANT("at0008", "Adjuvant", "The chemical is active but aids the therapeutic effect of another ingredient."), 
         DILUTANT("at0017", "Dilutant", "Inert dilutant."), 
         PROPELLENT("at0018", "Propellent", "Inert propellent."), 
         PRESERVATIVE("at0019", "Preservative", "The ingredient is present to prolong the life of the substance."), 
         COLOURING("at0020", "Colouring", "The ingredient is used to colour the substance."), 
         OTHER("at0009", "Other", "The chemical has another active role.")
       ;
    Role(String code, String text, String description)
   {
   terminologyTerm = new EnumTerminology.Term<>(this, code, text, description);
   }

   private final EnumTerminology.Term<Role> terminologyTerm;

   public EnumTerminology.Term<Role> getTerm()
   {
   return terminologyTerm;
   }
   public static final EnumTerminology<Role> TERMINOLOGY = EnumTerminology.newInstance(Role.class);
     }
           public enum Site implements EnumTerminology.TermEnum<Site>
   {
         LEFT_UPPER_ARM("at0006", "Left upper arm", "Left upper arm"), 
         RIGHT_UPPER_ARM("at0007", "Right upper arm", "Right upper arm"), 
         THIGH_LEFT_LEG("at0008", "Thigh left leg", "Thigh left leg"), 
         THIGH_RIGHT_LEG("at0009", "Thigh right leg", "Thigh right leg"), 
         MOUTH("at0010", "Mouth", "Mouth"), 
         GLUTEUS_LEFT("at0011", "Gluteus left", "Gluteus left"), 
         GLUTEUS_RIGHT("at0012", "Gluteus right", "Gluteus right")
       ;
    Site(String code, String text, String description)
   {
   terminologyTerm = new EnumTerminology.Term<>(this, code, text, description);
   }

   private final EnumTerminology.Term<Site> terminologyTerm;

   public EnumTerminology.Term<Site> getTerm()
   {
   return terminologyTerm;
   }
   public static final EnumTerminology<Site> TERMINOLOGY = EnumTerminology.newInstance(Site.class);
     }
           public enum InfusionAdministrationDetailsPurpose implements EnumTerminology.TermEnum<InfusionAdministrationDetailsPurpose>
   {
         BASELINE_ELECTROLYTE_INFUSION("at0008", "Baseline electrolyte infusion", "The infusion provides baseline hydration."), 
         ACTIVE_MEDICATION_INFUSION("at0009", "Active medication infusion", "The infusion carries an active pharrmaceutical ingredient.")
       ;
    InfusionAdministrationDetailsPurpose(String code, String text, String description)
   {
   terminologyTerm = new EnumTerminology.Term<>(this, code, text, description);
   }

   private final EnumTerminology.Term<InfusionAdministrationDetailsPurpose> terminologyTerm;

   public EnumTerminology.Term<InfusionAdministrationDetailsPurpose> getTerm()
   {
   return terminologyTerm;
   }
   public static final EnumTerminology<InfusionAdministrationDetailsPurpose> TERMINOLOGY = EnumTerminology.newInstance(InfusionAdministrationDetailsPurpose.class);
     }
     }
         }