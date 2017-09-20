// Generated using Marand-EHR TDO Generator vUnknown
// Source: ISPEK - MED - Medication administration.opt
// Time: 2016-07-15T08:46:09.118+02:00
package com.marand.openehr.medications.tdo;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.marand.ispek.ehr.common.tdo.CompositionEventContext;
import com.marand.ispek.ehr.common.tdo.IspekComposition;
import com.marand.openehr.tdo.EnumTerminology;
import com.marand.openehr.tdo.TdoAccess;
import com.marand.openehr.tdo.annotations.Archetype;
import com.marand.openehr.tdo.annotations.TdoNode;
import com.marand.openehr.tdo.annotations.Template;
import com.marand.openehr.tdo.validation.constraints.DurationMin;
import com.marand.openehr.tdo.validation.constraints.ValidCodePhrase;
import com.marand.openehr.tdo.validation.constraints.ValidDuration;
import com.marand.openehr.tdo.validation.constraints.ValidDvCodedText;
import com.marand.openehr.tdo.validation.constraints.ValidDvDuration;
import com.marand.openehr.tdo.validation.constraints.ValidString;
import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.Action;
import org.openehr.jaxb.rm.Cluster;
import org.openehr.jaxb.rm.Composition;
import org.openehr.jaxb.rm.ContentItem;
import org.openehr.jaxb.rm.DvBoolean;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvDuration;
import org.openehr.jaxb.rm.DvEhrUri;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.EventContext;
import org.openehr.jaxb.rm.Instruction;
import org.openehr.jaxb.rm.Item;
import org.openehr.jaxb.rm.ItemStructure;
import org.openehr.jaxb.rm.Section;

@Template(name = "Medication Administration", templateId = "ISPEK - MED - Medication administration")
@Archetype(name = "Medication Administration", archetypeId = "openEHR-EHR-COMPOSITION.encounter.v1")
public class MedicationAdministrationComposition extends Composition implements IspekComposition
{
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

  @TdoNode(name = "CompositionEventContext", path = "/context")
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

  @TdoNode(name = "Medication detail", path = "/content[openEHR-EHR-SECTION.medication.v1]")
  public MedicationDetailSection getMedicationDetail()
  {
    return medicationDetail;
  }

  public void setMedicationDetail(MedicationDetailSection medicationDetail)
  {
    this.medicationDetail = medicationDetail;
  }

  @Archetype(name = "Medication detail", archetypeId = "openEHR-EHR-SECTION.medication.v1")
  public static class MedicationDetailSection extends Section
  {
    @Deprecated
    @Override
    public List<ContentItem> getItems()
    {
      TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
      return super.getItems();
    }

    private List<Instruction> medicationOrder;

    @TdoNode(name = "Medication order", path = "/items[at0001:INSTRUCTION]")
    public List<Instruction> getMedicationOrder()
    {
      if (medicationOrder == null)
      {
        medicationOrder = new ArrayList<Instruction>();
      }

      return medicationOrder;
    }

    public void setMedicationOrder(List<Instruction> medicationOrder)
    {
      this.medicationOrder = medicationOrder;
    }

    private List<MedicationActionAction> medicationAction;

    @TdoNode(name = "Medication action", path = "/items[openEHR-EHR-ACTION.medication.v1]")
    public List<MedicationActionAction> getMedicationAction()
    {
      if (medicationAction == null)
      {
        medicationAction = new ArrayList<MedicationActionAction>();
      }

      return medicationAction;
    }

    public void setMedicationAction(List<MedicationActionAction> medicationAction)
    {
      this.medicationAction = medicationAction;
    }

    private List<InpatientMedicationSupplyAction> inpatientMedicationSupply;

    @TdoNode(name = "Inpatient medication supply", path = "/items[openEHR-EHR-ACTION.medication_supply_uk.v1]")
    public List<InpatientMedicationSupplyAction> getInpatientMedicationSupply()
    {
      if (inpatientMedicationSupply == null)
      {
        inpatientMedicationSupply = new ArrayList<InpatientMedicationSupplyAction>();
      }

      return inpatientMedicationSupply;
    }

    public void setInpatientMedicationSupply(List<InpatientMedicationSupplyAction> inpatientMedicationSupply)
    {
      this.inpatientMedicationSupply = inpatientMedicationSupply;
    }

    @Archetype(name = "Inpatient medication supply", archetypeId = "openEHR-EHR-ACTION.medication_supply_uk.v1")
    public static class InpatientMedicationSupplyAction extends Action
    {
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

      private DvEhrUri linkToMedicationOrder;

      @TdoNode(name = "Link to medication order", path = "/description[at0001:ITEM_TREE]/items[at0004:ELEMENT]/value")
      public DvEhrUri getLinkToMedicationOrder()
      {
        return linkToMedicationOrder;
      }

      public void setLinkToMedicationOrder(DvEhrUri linkToMedicationOrder)
      {
        this.linkToMedicationOrder = linkToMedicationOrder;
      }

      private DvCodedText supplyCategory;

      @ValidDvCodedText(definingCode = @ValidCodePhrase(notNull = @NotNull, codeString = @ValidString(notNull = @NotNull, pattern = @Pattern(regexp = "at0009|at0010|at0011|at0012"))))
      @TdoNode(name = "Supply category", path = "/description[at0001:ITEM_TREE]/items[at0008:ELEMENT]/value")
      public DvCodedText getSupplyCategory()
      {
        return supplyCategory;
      }

      public void setSupplyCategory(DvCodedText supplyCategory)
      {
        this.supplyCategory = supplyCategory;
      }

      public SupplyCategory getSupplyCategoryEnum()
      {
        return supplyCategory instanceof DvCodedText ? EnumTerminology.forEnum(SupplyCategory.class)
            .getEnumByCode(((DvCodedText)supplyCategory).getDefiningCode().getCodeString()) : null;
      }

      public void setSupplyCategoryEnum(SupplyCategory supplyCategory)
      {
        this.supplyCategory = supplyCategory == null
                              ? null
                              : DataValueUtils.getLocalCodedText(
                                  supplyCategory.getTerm().getCode(),
                                  supplyCategory.getTerm().getText());
      }

      public enum SupplyCategory implements EnumTerminology.TermEnum<SupplyCategory>
      {
        WARD_STOCK("at0009", "Ward stock", "The medication is supplied from ward stock."),
        NON_STOCK("at0010", "Non-stock", "The medication is not supplied from ward stock."),
        PATIENT_OWN_SUPPLY("at0011", "Patient own supply", "The medication is available from the patient's own supply."),
        ONE_STOP_DISPENSING(
            "at0012",
            "One stop dispensing",
            "The medication is supplied to allow the patient to take home.");

        SupplyCategory(String code, String text, String description)
        {
          terminologyTerm = new EnumTerminology.Term<SupplyCategory>(this, code, text, description);
        }

        private final EnumTerminology.Term<SupplyCategory> terminologyTerm;

        public EnumTerminology.Term<SupplyCategory> getTerm()
        {
          return terminologyTerm;
        }

        public static final EnumTerminology<SupplyCategory> TERMINOLOGY = EnumTerminology.newInstance(SupplyCategory.class);
      }

      private DvDuration supplyDuration;

      @ValidDvDuration(@ValidDuration(min = @DurationMin("P0D"), pattern = @Pattern(regexp = "P(\\d+D)?")))
      @TdoNode(name = "Supply duration", path = "/description[at0001:ITEM_TREE]/items[at0003:ELEMENT]/value")
      public DvDuration getSupplyDuration()
      {
        return supplyDuration;
      }

      public void setSupplyDuration(DvDuration supplyDuration)
      {
        this.supplyDuration = supplyDuration;
      }
    }

    private List<ClinicalInterventionAction> clinicalIntervention;

    @TdoNode(name = "Clinical intervention", path = "/items[openEHR-EHR-ACTION.procedure-zn.v1]")
    public List<ClinicalInterventionAction> getClinicalIntervention()
    {
      if (clinicalIntervention == null)
      {
        clinicalIntervention = new ArrayList<ClinicalInterventionAction>();
      }

      return clinicalIntervention;
    }

    public void setClinicalIntervention(List<ClinicalInterventionAction> clinicalIntervention)
    {
      this.clinicalIntervention = clinicalIntervention;
    }

    @Archetype(name = "Clinical intervention", archetypeId = "openEHR-EHR-ACTION.procedure-zn.v1")
    public static class ClinicalInterventionAction extends Action
    {
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

      private DvCodedText intervention;

      @TdoNode(name = "Intervention", path = "/description[at0001:ITEM_TREE]/items[at0002:ELEMENT]/value")
      public DvCodedText getIntervention()
      {
        return intervention;
      }

      public void setIntervention(DvCodedText intervention)
      {
        this.intervention = intervention;
      }

      private InfusionBagAmountCluster infusionBagAmount;

      @TdoNode(name = "Infusion bag amount", path = "/description[at0001:ITEM_TREE]/items[openEHR-EHR-CLUSTER.amount.v1]")
      public InfusionBagAmountCluster getInfusionBagAmount()
      {
        return infusionBagAmount;
      }

      public void setInfusionBagAmount(InfusionBagAmountCluster infusionBagAmount)
      {
        this.infusionBagAmount = infusionBagAmount;
      }

      @Archetype(name = "Infusion bag amount", archetypeId = "openEHR-EHR-CLUSTER.amount.v1")
      public static class InfusionBagAmountCluster extends Cluster
      {
        @Deprecated
        @Override
        public List<Item> getItems()
        {
          TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
          return super.getItems();
        }

        private DvQuantity quantity;

        @TdoNode(name = "Quantity", path = "/items[at0001:ELEMENT]/value")
        public DvQuantity getQuantity()
        {
          return quantity;
        }

        public void setQuantity(DvQuantity quantity)
        {
          this.quantity = quantity;
        }

        private DvCodedText doseUnit;

        @TdoNode(name = "Dose unit", path = "/items[at0002:ELEMENT]/value")
        public DvCodedText getDoseUnit()
        {
          return doseUnit;
        }

        public void setDoseUnit(DvCodedText doseUnit)
        {
          this.doseUnit = doseUnit;
        }
      }

      private Cluster anatomicalSiteDetails;

      @TdoNode(name = "Anatomical site details", path = "/description[at0001:ITEM_TREE]/items[at0050:CLUSTER]")
      public Cluster getAnatomicalSiteDetails()
      {
        return anatomicalSiteDetails;
      }

      public void setAnatomicalSiteDetails(Cluster anatomicalSiteDetails)
      {
        this.anatomicalSiteDetails = anatomicalSiteDetails;
      }

      private DvBoolean interventionUnsuccessful;

      @NotNull
      @TdoNode(name = "Intervention unsuccessful", path = "/description[at0001:ITEM_TREE]/items[at0004:ELEMENT]/value")
      public DvBoolean getInterventionUnsuccessful()
      {
        return interventionUnsuccessful;
      }

      public void setInterventionUnsuccessful(DvBoolean interventionUnsuccessful)
      {
        this.interventionUnsuccessful = interventionUnsuccessful;
      }

      private DvText comments;

      @TdoNode(name = "Comments", path = "/description[at0001:ITEM_TREE]/items[at0005:ELEMENT]/value")
      public DvText getComments()
      {
        return comments;
      }

      public void setComments(DvText comments)
      {
        this.comments = comments;
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

      private DvText requestorRequestIdentifier;

      @TdoNode(name = "Requestor request identifier", path = "/protocol[at0053:ITEM_TREE]/items[at0054:ELEMENT]/value")
      public DvText getRequestorRequestIdentifier()
      {
        return requestorRequestIdentifier;
      }

      public void setRequestorRequestIdentifier(DvText requestorRequestIdentifier)
      {
        this.requestorRequestIdentifier = requestorRequestIdentifier;
      }

      private Cluster requestor;

      @TdoNode(name = "Requestor", path = "/protocol[at0053:ITEM_TREE]/items[at0055:CLUSTER]")
      public Cluster getRequestor()
      {
        return requestor;
      }

      public void setRequestor(Cluster requestor)
      {
        this.requestor = requestor;
      }

      private DvText receiverRequestIdentifier;

      @TdoNode(name = "Receiver request identifier", path = "/protocol[at0053:ITEM_TREE]/items[at0056:ELEMENT]/value")
      public DvText getReceiverRequestIdentifier()
      {
        return receiverRequestIdentifier;
      }

      public void setReceiverRequestIdentifier(DvText receiverRequestIdentifier)
      {
        this.receiverRequestIdentifier = receiverRequestIdentifier;
      }

      private Cluster receiver;

      @TdoNode(name = "Receiver", path = "/protocol[at0053:ITEM_TREE]/items[at0057:CLUSTER]")
      public Cluster getReceiver()
      {
        return receiver;
      }

      public void setReceiver(Cluster receiver)
      {
        this.receiver = receiver;
      }

      private List<Cluster> localisation;

      @TdoNode(name = "Localisation", path = "/protocol[at0053:ITEM_TREE]/items[at0061:CLUSTER]")
      public List<Cluster> getLocalisation()
      {
        if (localisation == null)
        {
          localisation = new ArrayList<Cluster>();
        }

        return localisation;
      }

      public void setLocalisation(List<Cluster> localisation)
      {
        this.localisation = localisation;
      }
    }
  }
}