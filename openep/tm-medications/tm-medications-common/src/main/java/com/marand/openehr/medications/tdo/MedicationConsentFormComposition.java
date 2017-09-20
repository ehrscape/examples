// Generated using Marand-EHR TDO Generator vUnknown
// Source: ISPEK - MED - Medication Consent Form.opt
// Time: 2016-01-22T12:48:06.000+01:00
package com.marand.openehr.medications.tdo;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.marand.ispek.ehr.common.tdo.CompositionEventContext;
import com.marand.openehr.tdo.EnumTerminology;
import com.marand.openehr.tdo.TdoAccess;
import com.marand.openehr.tdo.annotations.Archetype;
import com.marand.openehr.tdo.annotations.TdoNode;
import com.marand.openehr.tdo.annotations.Template;
import com.marand.openehr.tdo.validation.constraints.DecimalMax;
import com.marand.openehr.tdo.validation.constraints.DecimalMin;
import com.marand.openehr.tdo.validation.constraints.ValidCodePhrase;
import com.marand.openehr.tdo.validation.constraints.ValidDouble;
import com.marand.openehr.tdo.validation.constraints.ValidDvCodedText;
import com.marand.openehr.tdo.validation.constraints.ValidDvQuantity;
import com.marand.openehr.tdo.validation.constraints.ValidString;
import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.AdminEntry;
import org.openehr.jaxb.rm.Cluster;
import org.openehr.jaxb.rm.Composition;
import org.openehr.jaxb.rm.ContentItem;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.EventContext;
import org.openehr.jaxb.rm.Item;
import org.openehr.jaxb.rm.ItemStructure;
import org.openehr.jaxb.rm.Section;
   
   @Template(name="Medication consent form", templateId="ISPEK - MED - Medication Consent Form")
   @Archetype(name="Medication consent form", archetypeId="openEHR-EHR-COMPOSITION.encounter.v1")
 public class MedicationConsentFormComposition extends Composition  {
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

                        private MedicationConsentAdminEntry medicationConsent;

            @TdoNode(name="Medication consent", path="/content[openEHR-EHR-ADMIN_ENTRY.medication_consent.v0]")
     public MedicationConsentAdminEntry getMedicationConsent()
    {
    return medicationConsent;
    }

    public void setMedicationConsent(MedicationConsentAdminEntry medicationConsent)
    {
    this.medicationConsent = medicationConsent;
    }
     
                       @Archetype(name="Medication consent", archetypeId="openEHR-EHR-ADMIN_ENTRY.medication_consent.v0")
   public static class MedicationConsentAdminEntry extends AdminEntry    {
                  @Deprecated
   @Override
   public ItemStructure getData()
   {
        TdoAccess.check("Property 'Data' is deprecated in: " + getClass().getName());
    return super.getData();
       }

        @Deprecated
    @Override
    public void setData(ItemStructure value)
    {
          TdoAccess.check("Property 'Data' is deprecated in: " + getClass().getName());
     super.setData(value);
         }
                        private DvCodedText consentType;

            @NotNull
   @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0005|at0006"), notNull=@NotNull), notNull=@NotNull))
   @TdoNode(name="Consent type", path="/data[at0001:ITEM_TREE]/items[at0002:ELEMENT]/value")
     public DvCodedText getConsentType()
    {
    return consentType;
    }

    public void setConsentType(DvCodedText consentType)
    {
    this.consentType = consentType;
    }
     
                
    public ConsentType getConsentTypeEnum()
  {
  return consentType instanceof DvCodedText ? EnumTerminology.forEnum(ConsentType.class).getEnumByCode(consentType.getDefiningCode().getCodeString()) : null;
  }

  public void setConsentTypeEnum(ConsentType consentType)
  {
  this.consentType = consentType == null ? null : DataValueUtils.getLocalCodedText(consentType.getTerm().getCode(), consentType.getTerm().getText());
  }
                          public enum ConsentType implements EnumTerminology.TermEnum<ConsentType>
   {
         FORM_T2("at0005", "Form T2", "Section 58 - Certificate of consent to treatment "), 
         FORM_T3("at0006", "Form T3", "Section 58(3) b) – Certificate of second opinion ")
       ;
    ConsentType(String code, String text, String description)
   {
   terminologyTerm = new EnumTerminology.Term<>(this, code, text, description);
   }

   private final EnumTerminology.Term<ConsentType> terminologyTerm;

   public EnumTerminology.Term<ConsentType> getTerm()
   {
   return terminologyTerm;
   }
   public static final EnumTerminology<ConsentType> TERMINOLOGY = EnumTerminology.newInstance(ConsentType.class);
     }
                 private DvQuantity maximumCumulativeDose;

            @ValidDvQuantity(magnitude=@ValidDouble(min=@DecimalMin(value=100.0, inclusive=true), max=@DecimalMax(value=200.0, inclusive=true)), units=@ValidString(pattern=@Pattern(regexp="\\%"), notNull=@NotNull))
   @TdoNode(name="Maximum cumulative dose", path="/data[at0001:ITEM_TREE]/items[at0003:ELEMENT]/value")
     public DvQuantity getMaximumCumulativeDose()
    {
    return maximumCumulativeDose;
    }

    public void setMaximumCumulativeDose(DvQuantity maximumCumulativeDose)
    {
    this.maximumCumulativeDose = maximumCumulativeDose;
    }
     
                             }
                 private MedicationListSection medicationList;

            @TdoNode(name="Medication list", path="/content[openEHR-EHR-SECTION.ispek_dialog.v1]")
     public MedicationListSection getMedicationList()
    {
    return medicationList;
    }

    public void setMedicationList(MedicationListSection medicationList)
    {
    this.medicationList = medicationList;
    }
     
                       @Archetype(name="Medication list", archetypeId="openEHR-EHR-SECTION.ispek_dialog.v1")
   public static class MedicationListSection extends Section    {
                  @Deprecated
   @Override
   public List<ContentItem> getItems()
   {
        TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
    return super.getItems();
       }

                        private List<MedicationConsentItemAdminEntry> medicationConsentItem;

            @TdoNode(name="Medication consent item", path="/items[openEHR-EHR-ADMIN_ENTRY.medication_consent_item.v0]")
     public List<MedicationConsentItemAdminEntry> getMedicationConsentItem()
    {
    if (medicationConsentItem == null)
    {
      medicationConsentItem = new ArrayList<>();
    }

    return medicationConsentItem;
    }

    public void setMedicationConsentItem(List<MedicationConsentItemAdminEntry> medicationConsentItem)
    {
    this.medicationConsentItem = medicationConsentItem;
    }

                       @Archetype(name="Medication consent item", archetypeId="openEHR-EHR-ADMIN_ENTRY.medication_consent_item.v0")
   public static class MedicationConsentItemAdminEntry extends AdminEntry    {
                  @Deprecated
   @Override
   public ItemStructure getData()
   {
        TdoAccess.check("Property 'Data' is deprecated in: " + getClass().getName());
    return super.getData();
       }

        @Deprecated
    @Override
    public void setData(ItemStructure value)
    {
          TdoAccess.check("Property 'Data' is deprecated in: " + getClass().getName());
     super.setData(value);
         }
                        private MedicationItemCluster medicationItem;

            @TdoNode(name="Medication item", path="/data[at0001:ITEM_TREE]/items[at0002:CLUSTER]")
     public MedicationItemCluster getMedicationItem()
    {
    return medicationItem;
    }

    public void setMedicationItem(MedicationItemCluster medicationItem)
    {
    this.medicationItem = medicationItem;
    }
     
                       public static class MedicationItemCluster extends Cluster    {
                  @Deprecated
   @Override
   public List<Item> getItems()
   {
        TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
    return super.getItems();
       }

                        private DvText name;

            @NotNull
   @TdoNode(name="Name", path="/items[at0003:ELEMENT]/value")
     public DvText getName()
    {
    return name;
    }

    public void setName(DvText name)
    {
    this.name = name;
    }
     
                                         private DvCodedText type;

            @NotNull
   @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0009|at0011"), notNull=@NotNull), notNull=@NotNull))
   @TdoNode(name="Type", path="/items[at0004:ELEMENT]/value")
     public DvCodedText getType()
    {
    return type;
    }

    public void setType(DvCodedText type)
    {
    this.type = type;
    }
     
                
    public Type getTypeEnum()
  {
  return type instanceof DvCodedText ? EnumTerminology.forEnum(Type.class).getEnumByCode(type.getDefiningCode().getCodeString()) : null;
  }

  public void setTypeEnum(Type type)
  {
  this.type = type == null ? null : DataValueUtils.getLocalCodedText(type.getTerm().getCode(), type.getTerm().getText());
  }
                          public enum Type implements EnumTerminology.TermEnum<Type>
   {
         MEDICATION("at0009", "Medication", "Single medication"), 
         MEDICATION_GROUPE("at0011", "Medication groupe", "*")
       ;
    Type(String code, String text, String description)
   {
   terminologyTerm = new EnumTerminology.Term<>(this, code, text, description);
   }

   private final EnumTerminology.Term<Type> terminologyTerm;

   public EnumTerminology.Term<Type> getTerm()
   {
   return terminologyTerm;
   }
   public static final EnumTerminology<Type> TERMINOLOGY = EnumTerminology.newInstance(Type.class);
     }
     }
                 private DvText route;

            @TdoNode(name="Route", path="/data[at0001:ITEM_TREE]/items[at0008:ELEMENT]/value")
     public DvText getRoute()
    {
    return route;
    }

    public void setRoute(DvText route)
    {
    this.route = route;
    }
     
                             }
     }
      }