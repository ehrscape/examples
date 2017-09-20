// Generated using Marand-EHR TDO Generator vUnknown
// Source: ISPEK - MED - Medication Order.opt
// Time: 2016-10-18T14:09:56.978+02:00
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
import com.marand.openehr.tdo.validation.constraints.DecimalMin;
import com.marand.openehr.tdo.validation.constraints.ValidDouble;
import com.marand.openehr.tdo.validation.constraints.ValidDvQuantities;
import com.marand.openehr.tdo.validation.constraints.ValidDvQuantity;
import com.marand.openehr.tdo.validation.constraints.ValidString;
import org.openehr.jaxb.rm.Activity;
import org.openehr.jaxb.rm.Cluster;
import org.openehr.jaxb.rm.Composition;
import org.openehr.jaxb.rm.ContentItem;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvCount;
import org.openehr.jaxb.rm.DvDateTime;
import org.openehr.jaxb.rm.DvDuration;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.EventContext;
import org.openehr.jaxb.rm.Instruction;
import org.openehr.jaxb.rm.Item;
import org.openehr.jaxb.rm.ItemStructure;
import org.openehr.jaxb.rm.Section;

@Template(name="Medication order", templateId="ISPEK - MED - Medication Order")
@Archetype(name="Medication order", archetypeId="openEHR-EHR-COMPOSITION.encounter.v1")
public class MedicationOrderComposition extends Composition   implements   IspekComposition  {
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

                     private List<HealthcareServiceRequestInstruction> healthcareServiceRequest;

         @TdoNode(name="Healthcare service request", path="/content[openEHR-EHR-INSTRUCTION.request-zn.v1]")
  public List<HealthcareServiceRequestInstruction> getHealthcareServiceRequest()
 {
 if (healthcareServiceRequest == null)
  healthcareServiceRequest = new ArrayList<HealthcareServiceRequestInstruction>();

 return healthcareServiceRequest;
 }

 public void setHealthcareServiceRequest(List<HealthcareServiceRequestInstruction> healthcareServiceRequest)
 {
 this.healthcareServiceRequest = healthcareServiceRequest;
 }

                    @Archetype(name="Healthcare service request", archetypeId="openEHR-EHR-INSTRUCTION.request-zn.v1")
public static class HealthcareServiceRequestInstruction extends Instruction    {
               @Deprecated
@Override
public List<Activity> getActivities()
{
     TdoAccess.check("Property 'Activities' is deprecated in: " + getClass().getName());
 return super.getActivities();
    }

                     private List<RequestActivity> request;

         @TdoNode(name="Request", path="/activities[at0001]")
  public List<RequestActivity> getRequest()
 {
 if (request == null)
  request = new ArrayList<RequestActivity>();

 return request;
 }

 public void setRequest(List<RequestActivity> request)
 {
 this.request = request;
 }

                    public static class RequestActivity extends Activity    {
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
                     private DvCodedText serviceRequested;

         @NotNull
@TdoNode(name="Service requested", path="/description[at0009:ITEM_TREE]/items[at0121:ELEMENT]/value")
  public DvCodedText getServiceRequested()
 {
 return serviceRequested;
 }

 public void setServiceRequested(DvCodedText serviceRequested)
 {
 this.serviceRequested = serviceRequested;
 }

                                      private List<Cluster> specificDetails;

         @TdoNode(name="Specific details", path="/description[at0009:ITEM_TREE]/items[at0132:CLUSTER]")
  public List<Cluster> getSpecificDetails()
 {
 if (specificDetails == null)
  specificDetails = new ArrayList<Cluster>();

 return specificDetails;
 }

 public void setSpecificDetails(List<Cluster> specificDetails)
 {
 this.specificDetails = specificDetails;
 }

                          private DvDateTime dateTimeServiceRequired;

         @TdoNode(name="Date-time service required", path="/description[at0009:ITEM_TREE]/items[at0040:ELEMENT]/value")
  public DvDateTime getDateTimeServiceRequired()
 {
 return dateTimeServiceRequired;
 }

 public void setDateTimeServiceRequired(DvDateTime dateTimeServiceRequired)
 {
 this.dateTimeServiceRequired = dateTimeServiceRequired;
 }

                                      private List<Cluster> patientRequirements;

         @TdoNode(name="Patient requirements", path="/description[at0009:ITEM_TREE]/items[at0116:CLUSTER]")
  public List<Cluster> getPatientRequirements()
 {
 if (patientRequirements == null)
  patientRequirements = new ArrayList<Cluster>();

 return patientRequirements;
 }

 public void setPatientRequirements(List<Cluster> patientRequirements)
 {
 this.patientRequirements = patientRequirements;
 }

                          private HealthcareServiceRequestCluster healthcareServiceRequest;

         @TdoNode(name="Healthcare service request", path="/description[at0009:ITEM_TREE]/items[openEHR-EHR-CLUSTER.timing.v1]")
  public HealthcareServiceRequestCluster getHealthcareServiceRequest()
 {
 return healthcareServiceRequest;
 }

 public void setHealthcareServiceRequest(HealthcareServiceRequestCluster healthcareServiceRequest)
 {
 this.healthcareServiceRequest = healthcareServiceRequest;
 }

                    @Archetype(name="Healthcare service request", archetypeId="openEHR-EHR-CLUSTER.timing.v1")
public static class HealthcareServiceRequestCluster extends Cluster    {
               @Deprecated
@Override
public List<Item> getItems()
{
     TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
 return super.getItems();
    }

                     private DvCount dailyCount;

         @TdoNode(name="Daily count", path="/items[at0001:ELEMENT]/value")
  public DvCount getDailyCount()
 {
 return dailyCount;
 }

 public void setDailyCount(DvCount dailyCount)
 {
 this.dailyCount = dailyCount;
 }

                                      private DvQuantity frequency;

         @ValidDvQuantities({@ValidDvQuantity(magnitude=@ValidDouble(min=@DecimalMin(value=0.0, inclusive=true)), units=@ValidString(pattern=@Pattern(regexp="\\/d"), notNull=@NotNull)),@ValidDvQuantity(magnitude=@ValidDouble(min=@DecimalMin(value=0.0, inclusive=true)), units=@ValidString(pattern=@Pattern(regexp="\\/wk"), notNull=@NotNull)),@ValidDvQuantity(magnitude=@ValidDouble(min=@DecimalMin(value=0.0, inclusive=true)), units=@ValidString(pattern=@Pattern(regexp="\\/mo"), notNull=@NotNull)),@ValidDvQuantity(magnitude=@ValidDouble(min=@DecimalMin(value=0.0, inclusive=true)), units=@ValidString(pattern=@Pattern(regexp="\\/yr"), notNull=@NotNull)),@ValidDvQuantity(magnitude=@ValidDouble(min=@DecimalMin(value=0.0, inclusive=true)), units=@ValidString(pattern=@Pattern(regexp="\\/min"), notNull=@NotNull)),@ValidDvQuantity(magnitude=@ValidDouble(min=@DecimalMin(value=0.0, inclusive=true)), units=@ValidString(pattern=@Pattern(regexp="\\/s"), notNull=@NotNull)),@ValidDvQuantity(magnitude=@ValidDouble(min=@DecimalMin(value=0.0, inclusive=true)), units=@ValidString(pattern=@Pattern(regexp="\\/h"), notNull=@NotNull))})
@TdoNode(name="Frequency", path="/items[at0003:ELEMENT]/value")
  public DvQuantity getFrequency()
 {
 return frequency;
 }

 public void setFrequency(DvQuantity frequency)
 {
 this.frequency = frequency;
 }

                                      private DvDuration interval;

         @TdoNode(name="Interval", path="/items[at0014:ELEMENT]/value")
  public DvDuration getInterval()
 {
 return interval;
 }

 public void setInterval(DvDuration interval)
 {
 this.interval = interval;
 }

                          }
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
                     private List<Cluster> requestor;

         @TdoNode(name="Requestor", path="/protocol[at0008:ITEM_TREE]/items[at0141:CLUSTER]")
  public List<Cluster> getRequestor()
 {
 if (requestor == null)
  requestor = new ArrayList<Cluster>();

 return requestor;
 }

 public void setRequestor(List<Cluster> requestor)
 {
 this.requestor = requestor;
 }

                          private List<Cluster> receiver;

         @TdoNode(name="Receiver", path="/protocol[at0008:ITEM_TREE]/items[at0142:CLUSTER]")
  public List<Cluster> getReceiver()
 {
 if (receiver == null)
  receiver = new ArrayList<Cluster>();

 return receiver;
 }

 public void setReceiver(List<Cluster> receiver)
 {
 this.receiver = receiver;
 }

                          private List<Cluster> distributionListForResponse;

         @TdoNode(name="Distribution list for response", path="/protocol[at0008:ITEM_TREE]/items[at0128:CLUSTER]")
  public List<Cluster> getDistributionListForResponse()
 {
 if (distributionListForResponse == null)
  distributionListForResponse = new ArrayList<Cluster>();

 return distributionListForResponse;
 }

 public void setDistributionListForResponse(List<Cluster> distributionListForResponse)
 {
 this.distributionListForResponse = distributionListForResponse;
 }

                          private List<Cluster> localisation;

         @TdoNode(name="Localisation", path="/protocol[at0008:ITEM_TREE]/items[at0112:CLUSTER]")
  public List<Cluster> getLocalisation()
 {
 if (localisation == null)
  localisation = new ArrayList<Cluster>();

 return localisation;
 }

 public void setLocalisation(List<Cluster> localisation)
 {
 this.localisation = localisation;
 }

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
  medicationInstruction = new ArrayList<MedicationInstructionInstruction>();

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
  medicationAction = new ArrayList<MedicationActionAction>();

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
terminologyTerm = new EnumTerminology.Term<Role>(this, code, text, description);
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
terminologyTerm = new EnumTerminology.Term<Site>(this, code, text, description);
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
terminologyTerm = new EnumTerminology.Term<InfusionAdministrationDetailsPurpose>(this, code, text, description);
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