// Generated using Marand-EHR TDO Generator vUnknown
// Source: ISPEK - MED - Medication Order.opt
// Time: 2016-08-19T10:36:15.122+02:00
package com.marand.openehr.medications.tdo;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.InfusionAdministrationDetailsPurpose;
import com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.Site;
import com.marand.openehr.tdo.EnumTerminology;
import com.marand.openehr.tdo.TdoAccess;
import com.marand.openehr.tdo.annotations.Archetype;
import com.marand.openehr.tdo.annotations.TdoNode;
import com.marand.openehr.tdo.validation.constraints.Contained;
import com.marand.openehr.tdo.validation.constraints.DecimalMax;
import com.marand.openehr.tdo.validation.constraints.DecimalMin;
import com.marand.openehr.tdo.validation.constraints.DurationMin;
import com.marand.openehr.tdo.validation.constraints.Max;
import com.marand.openehr.tdo.validation.constraints.Min;
import com.marand.openehr.tdo.validation.constraints.ValidCodePhrase;
import com.marand.openehr.tdo.validation.constraints.ValidDouble;
import com.marand.openehr.tdo.validation.constraints.ValidDuration;
import com.marand.openehr.tdo.validation.constraints.ValidDvCodedText;
import com.marand.openehr.tdo.validation.constraints.ValidDvDuration;
import com.marand.openehr.tdo.validation.constraints.ValidDvProportion;
import com.marand.openehr.tdo.validation.constraints.ValidDvQuantities;
import com.marand.openehr.tdo.validation.constraints.ValidDvQuantity;
import com.marand.openehr.tdo.validation.constraints.ValidNumber;
import com.marand.openehr.tdo.validation.constraints.ValidString;
import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.Cluster;
import org.openehr.jaxb.rm.DataValue;
import org.openehr.jaxb.rm.DvBoolean;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvCount;
import org.openehr.jaxb.rm.DvDuration;
import org.openehr.jaxb.rm.DvProportion;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.Item;

@Archetype(name="Administration details", archetypeId="openEHR-EHR-CLUSTER.medication_admin.v1")
public class AdministrationDetailsCluster extends Cluster  {
            @Deprecated
@Override
public List<Item> getItems()
{
     TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
 return super.getItems();
    }

                     private List<DvCodedText> route;

         @TdoNode(name="Route", path="/items[at0001:ELEMENT]/value")
  public List<DvCodedText> getRoute()
 {
 if (route == null)
  route = new ArrayList<DvCodedText>();

 return route;
 }

 public void setRoute(List<DvCodedText> route)
 {
 this.route = route;
 }

                          private DvCodedText site;

         @ValidDvCodedText(definingCode=@ValidCodePhrase(notNull=@NotNull, codeString=@ValidString(pattern=@Pattern(regexp="at0006|at0007|at0008|at0009|at0010|at0011|at0012"), notNull=@NotNull)))
@TdoNode(name="Site", path="/items[at0002:ELEMENT]/value")
  public DvCodedText getSite()
 {
 return site;
 }

 public void setSite(DvCodedText site)
 {
 this.site = site;
 }


 public Site getSiteEnum()
{
return site instanceof DvCodedText ? EnumTerminology.forEnum(Site.class).getEnumByCode(((DvCodedText)site).getDefiningCode().getCodeString()) : null;
}

public void setSiteEnum(Site site)
{
this.site = site == null ? null : DataValueUtils.getLocalCodedText(site.getTerm().getCode(), site.getTerm().getText());
}
                             private DvText deliveryMethod;

         @TdoNode(name="Delivery method", path="/items[at0003:ELEMENT]/value")
  public DvText getDeliveryMethod()
 {
 return deliveryMethod;
 }

 public void setDeliveryMethod(DvText deliveryMethod)
 {
 this.deliveryMethod = deliveryMethod;
 }

                                      private DvDuration doseDuration;

         @ValidDvDuration(@ValidDuration(min=@DurationMin("PT0S")))
@TdoNode(name="Dose duration", path="/items[at0004:ELEMENT]/value")
  public DvDuration getDoseDuration()
 {
 return doseDuration;
 }

 public void setDoseDuration(DvDuration doseDuration)
 {
 this.doseDuration = doseDuration;
 }

                                      private List<ConditionalMedicationInstructionsCluster> conditionalMedicationInstructions;

         @TdoNode(name="Conditional medication instructions", path="/items[openEHR-EHR-CLUSTER.conditional_medication_rules.v0]")
  public List<ConditionalMedicationInstructionsCluster> getConditionalMedicationInstructions()
 {
 if (conditionalMedicationInstructions == null)
  conditionalMedicationInstructions = new ArrayList<ConditionalMedicationInstructionsCluster>();

 return conditionalMedicationInstructions;
 }

 public void setConditionalMedicationInstructions(List<ConditionalMedicationInstructionsCluster> conditionalMedicationInstructions)
 {
 this.conditionalMedicationInstructions = conditionalMedicationInstructions;
 }

                    @Archetype(name="Conditional medication instructions", archetypeId="openEHR-EHR-CLUSTER.conditional_medication_rules.v0")
public static class ConditionalMedicationInstructionsCluster extends Cluster    {
               @Deprecated
@Override
public List<Item> getItems()
{
     TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
 return super.getItems();
    }

                     private List<ConditionRuleCluster> conditionRule;

         @TdoNode(name="Condition rule", path="/items[at0005:CLUSTER]")
  public List<ConditionRuleCluster> getConditionRule()
 {
 if (conditionRule == null)
  conditionRule = new ArrayList<ConditionRuleCluster>();

 return conditionRule;
 }

 public void setConditionRule(List<ConditionRuleCluster> conditionRule)
 {
 this.conditionRule = conditionRule;
 }

                    public static class ConditionRuleCluster extends Cluster    {
               @Deprecated
@Override
public List<Item> getItems()
{
     TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
 return super.getItems();
    }

                     private DataValue condition;

         @TdoNode(name="Condition", path="/items[at0002:ELEMENT]/value")
  public DataValue getCondition()
 {
 return condition;
 }

 public void setCondition(DataValue condition)
 {
 this.condition = condition;
 }

                                      private DvQuantity doseAmount;

         @ValidDvQuantity(magnitude=@ValidDouble(min=@DecimalMin(value=0.0, inclusive=true)), units=@ValidString(pattern=@Pattern(regexp="1"), notNull=@NotNull))
@TdoNode(name="Dose amount", path="/items[at0004:ELEMENT]/value")
  public DvQuantity getDoseAmount()
 {
 return doseAmount;
 }

 public void setDoseAmount(DvQuantity doseAmount)
 {
 this.doseAmount = doseAmount;
 }

                                      private DvQuantity doseAdministrationRate;

         @ValidDvQuantities({@ValidDvQuantity(magnitude=@ValidDouble(min=@DecimalMin(value=0.0, inclusive=true)), units=@ValidString(pattern=@Pattern(regexp="l\\/h"), notNull=@NotNull)),@ValidDvQuantity(magnitude=@ValidDouble(min=@DecimalMin(value=0.0, inclusive=true)), units=@ValidString(pattern=@Pattern(regexp="ml\\/h"), notNull=@NotNull)),@ValidDvQuantity(magnitude=@ValidDouble(min=@DecimalMin(value=0.0, inclusive=true)), units=@ValidString(pattern=@Pattern(regexp="ml\\/min"), notNull=@NotNull)),@ValidDvQuantity(magnitude=@ValidDouble(min=@DecimalMin(value=0.0, inclusive=true)), units=@ValidString(pattern=@Pattern(regexp="ml\\/s"), notNull=@NotNull))})
@TdoNode(name="Dose administration rate", path="/items[at0006:ELEMENT]/value")
  public DvQuantity getDoseAdministrationRate()
 {
 return doseAdministrationRate;
 }

 public void setDoseAdministrationRate(DvQuantity doseAdministrationRate)
 {
 this.doseAdministrationRate = doseAdministrationRate;
 }

                                      private DvText dosageFormula;

         @TdoNode(name="Dosage formula", path="/items[at0007:ELEMENT]/value")
  public DvText getDosageFormula()
 {
 return dosageFormula;
 }

 public void setDosageFormula(DvText dosageFormula)
 {
 this.dosageFormula = dosageFormula;
 }

                          }
  }
              private List<InfusionAdministrationDetailsCluster> infusionAdministrationDetails;

         @TdoNode(name="Infusion Administration Details", path="/items[openEHR-EHR-CLUSTER.infusion_details.v1]")
  public List<InfusionAdministrationDetailsCluster> getInfusionAdministrationDetails()
 {
 if (infusionAdministrationDetails == null)
  infusionAdministrationDetails = new ArrayList<InfusionAdministrationDetailsCluster>();

 return infusionAdministrationDetails;
 }

 public void setInfusionAdministrationDetails(List<InfusionAdministrationDetailsCluster> infusionAdministrationDetails)
 {
 this.infusionAdministrationDetails = infusionAdministrationDetails;
 }

                    @Archetype(name="Infusion Administration Details", archetypeId="openEHR-EHR-CLUSTER.infusion_details.v1")
public static class InfusionAdministrationDetailsCluster extends Cluster    {
               @Deprecated
@Override
public List<Item> getItems()
{
     TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
 return super.getItems();
    }

                     private DataValue doseAdministrationFormula;

         @TdoNode(name="Dose Administration Formula", path="/items[at0006:ELEMENT]/value")
  public DataValue getDoseAdministrationFormula()
 {
 return doseAdministrationFormula;
 }

 public void setDoseAdministrationFormula(DataValue doseAdministrationFormula)
 {
 this.doseAdministrationFormula = doseAdministrationFormula;
 }

                                      private DataValue doseAdministrationRate;

         @ValidDvQuantities({@ValidDvQuantity(magnitude=@ValidDouble(min=@DecimalMin(value=0.0, inclusive=true)), units=@ValidString(pattern=@Pattern(regexp="l\\/h"), notNull=@NotNull)),@ValidDvQuantity(units=@ValidString(pattern=@Pattern(regexp="ml\\/min"), notNull=@NotNull)),@ValidDvQuantity(units=@ValidString(pattern=@Pattern(regexp="ml\\/s"), notNull=@NotNull)),@ValidDvQuantity(units=@ValidString(pattern=@Pattern(regexp="ml\\/h"), notNull=@NotNull))})
@TdoNode(name="Dose Administration Rate", path="/items[at0001:ELEMENT]/value")
  public DataValue getDoseAdministrationRate()
 {
 return doseAdministrationRate;
 }

 public void setDoseAdministrationRate(DataValue doseAdministrationRate)
 {
 this.doseAdministrationRate = doseAdministrationRate;
 }

                                      private DvCodedText purpose;

         @ValidDvCodedText(definingCode=@ValidCodePhrase(notNull=@NotNull, codeString=@ValidString(pattern=@Pattern(regexp="at0008|at0009"), notNull=@NotNull)))
@TdoNode(name="Purpose", path="/items[at0007:ELEMENT]/value")
  public DvCodedText getPurpose()
 {
 return purpose;
 }

 public void setPurpose(DvCodedText purpose)
 {
 this.purpose = purpose;
 }


 public InfusionAdministrationDetailsPurpose getPurposeEnum()
{
return purpose instanceof DvCodedText ? EnumTerminology.forEnum(InfusionAdministrationDetailsPurpose.class).getEnumByCode(((DvCodedText)purpose).getDefiningCode().getCodeString()) : null;
}

public void setPurposeEnum(InfusionAdministrationDetailsPurpose purpose)
{
this.purpose = purpose == null ? null : DataValueUtils.getLocalCodedText(purpose.getTerm().getCode(), purpose.getTerm().getText());
}
                             private List<Cluster> infusionDevice;

         @TdoNode(name="Infusion device", path="/items[at0005:CLUSTER]")
  public List<Cluster> getInfusionDevice()
 {
 if (infusionDevice == null)
  infusionDevice = new ArrayList<Cluster>();

 return infusionDevice;
 }

 public void setInfusionDevice(List<Cluster> infusionDevice)
 {
 this.infusionDevice = infusionDevice;
 }

              }
              private List<OxygenDeliveryCluster> oxygenDelivery;

         @TdoNode(name="Oxygen delivery", path="/items[openEHR-EHR-CLUSTER.gas_delivery-oxygen.v1]")
  public List<OxygenDeliveryCluster> getOxygenDelivery()
 {
 if (oxygenDelivery == null)
  oxygenDelivery = new ArrayList<OxygenDeliveryCluster>();

 return oxygenDelivery;
 }

 public void setOxygenDelivery(List<OxygenDeliveryCluster> oxygenDelivery)
 {
 this.oxygenDelivery = oxygenDelivery;
 }

                    @Archetype(name="Oxygen delivery", archetypeId="openEHR-EHR-CLUSTER.gas_delivery-oxygen.v1")
public static class OxygenDeliveryCluster extends Cluster    {
               @Deprecated
@Override
public List<Item> getItems()
{
     TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
 return super.getItems();
    }

                     private DvCodedText nameOfGasOxygen;

         @ValidDvCodedText(definingCode=@ValidCodePhrase(notNull=@NotNull, codeString=@ValidString(pattern=@Pattern(regexp="at0\\.96"), notNull=@NotNull)))
@TdoNode(name="Name of gas - oxygen", path="/items[at0022.1:ELEMENT]/value", order=1)
  public DvCodedText getNameOfGasOxygen()
 {
 return nameOfGasOxygen;
 }

 public void setNameOfGasOxygen(DvCodedText nameOfGasOxygen)
 {
 this.nameOfGasOxygen = nameOfGasOxygen;
 }


 public NameOfGasOxygen getNameOfGasOxygenEnum()
{
return nameOfGasOxygen instanceof DvCodedText ? EnumTerminology.forEnum(NameOfGasOxygen.class).getEnumByCode(((DvCodedText)nameOfGasOxygen).getDefiningCode().getCodeString()) : null;
}

public void setNameOfGasOxygenEnum(NameOfGasOxygen nameOfGasOxygen)
{
this.nameOfGasOxygen = nameOfGasOxygen == null ? null : DataValueUtils.getLocalCodedText(nameOfGasOxygen.getTerm().getCode(), nameOfGasOxygen.getTerm().getText());
}
                       public enum NameOfGasOxygen implements EnumTerminology.TermEnum<NameOfGasOxygen>
{
      OXYGEN("at0.96", "Oxygen", "The gas delivered is oxygen.")
    ;
 NameOfGasOxygen(String code, String text, String description)
{
terminologyTerm = new EnumTerminology.Term<NameOfGasOxygen>(this, code, text, description);
}

private final EnumTerminology.Term<NameOfGasOxygen> terminologyTerm;

public EnumTerminology.Term<NameOfGasOxygen> getTerm()
{
return terminologyTerm;
}
public static final EnumTerminology<NameOfGasOxygen> TERMINOLOGY = EnumTerminology.newInstance(NameOfGasOxygen.class);
  }
              private DvCodedText route;

         @ValidDvCodedText(definingCode=@ValidCodePhrase(notNull=@NotNull, codeString=@ValidString(pattern=@Pattern(regexp="at0066|at0067|at0068|at0\\.104|at0069|at0070|at0\\.105|at0071|at0072|at0073|at0074|at0075|at0\\.100|at0\\.99|at0\\.106"), notNull=@NotNull)))
@TdoNode(name="Route", path="/items[at0001:ELEMENT]/value", order=2)
  public DvCodedText getRoute()
 {
 return route;
 }

 public void setRoute(DvCodedText route)
 {
 this.route = route;
 }


 public Route getRouteEnum()
{
return route instanceof DvCodedText ? EnumTerminology.forEnum(Route.class).getEnumByCode(((DvCodedText)route).getDefiningCode().getCodeString()) : null;
}

public void setRouteEnum(Route route)
{
this.route = route == null ? null : DataValueUtils.getLocalCodedText(route.getTerm().getCode(), route.getTerm().getText());
}
                       public enum Route implements EnumTerminology.TermEnum<Route>
{
      CPAP_MASK("at0066", "CPAP (mask)", "CPAP (mask)."),
      CPAP_NASAL("at0067", "CPAP (nasal)", "CPAP (nasal)."),
      FULL_FACE_MASK("at0068", "Full face mask", "Close-fitting, full face mask."),
      NASAL_NIV_MASK("at0.104", "Nasal NIV mask", "Close fitting nasal mask for use during  non-assisted ventilation."),
      OXYGEN_MASK("at0069", "Oxygen mask", "Oxygen mask."),
      NASAL_CATHETER("at0070", "Nasal catheter", "Nasal catheter."),
      HIGH_FLOW_NASAL_CATHETER("at0.105", "High-flow nasal catheter", "A nasal catheter designed for high flow administration is used."),
      VENTURI_MASK("at0071", "Venturi mask", "Venturi mask."),
      OHIO_MASK("at0072", "Ohio mask", "Ohio mask."),
      INCUBATOR("at0073", "Incubator", "Incubator."),
      TENT("at0074", "Tent", "Tent."),
      T_TUBE("at0075", "T-Tube", "T-Tube."),
      TRACHEAL_TUBE("at0.100", "Tracheal Tube", "Tracheal Tube"),
      TRACHEAL_CANNULA("at0.99", "Tracheal Cannula", "Tracheal Cannula"),
      HIGH_FLOW_TRACHEAL_CANNULA("at0.106", "High-flow tracheal cannula", "A tracheal cannula designed for high-flow administration")
    ;
 Route(String code, String text, String description)
{
terminologyTerm = new EnumTerminology.Term<Route>(this, code, text, description);
}

private final EnumTerminology.Term<Route> terminologyTerm;

public EnumTerminology.Term<Route> getTerm()
{
return terminologyTerm;
}
public static final EnumTerminology<Route> TERMINOLOGY = EnumTerminology.newInstance(Route.class);
  }
              private List<DeviceCluster> device;

         @TdoNode(name="Device", path="/items[openEHR-EHR-CLUSTER.device-ehrscape.v1]", order=3)
  public List<DeviceCluster> getDevice()
 {
 if (device == null)
  device = new ArrayList<DeviceCluster>();

 return device;
 }

 public void setDevice(List<DeviceCluster> device)
 {
 this.device = device;
 }

                    @Archetype(name="Device", archetypeId="openEHR-EHR-CLUSTER.device-ehrscape.v1")
public static class DeviceCluster extends Cluster    {
               @Deprecated
@Override
public List<Item> getItems()
{
     TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
 return super.getItems();
    }

                     private DvText deviceName;

         @TdoNode(name="Device name", path="/items[at0001:ELEMENT]/value")
  public DvText getDeviceName()
 {
 return deviceName;
 }

 public void setDeviceName(DvText deviceName)
 {
 this.deviceName = deviceName;
 }

                                      private DvText description;

         @TdoNode(name="Description", path="/items[at0002:ELEMENT]/value")
  public DvText getDescription()
 {
 return description;
 }

 public void setDescription(DvText description)
 {
 this.description = description;
 }

                                      private DvText type;

         @TdoNode(name="Type", path="/items[at0003:ELEMENT]/value")
  public DvText getType()
 {
 return type;
 }

 public void setType(DvText type)
 {
 this.type = type;
 }

                                      private List<DvCount> size;

         @TdoNode(name="Size", path="/items[at0016:ELEMENT]/value")
  public List<DvCount> getSize()
 {
 if (size == null)
  size = new ArrayList<DvCount>();

 return size;
 }

 public void setSize(List<DvCount> size)
 {
 this.size = size;
 }

                          private List<Cluster> dimensions;

         @TdoNode(name="Dimensions", path="/items[at0017:CLUSTER]")
  public List<Cluster> getDimensions()
 {
 if (dimensions == null)
  dimensions = new ArrayList<Cluster>();

 return dimensions;
 }

 public void setDimensions(List<Cluster> dimensions)
 {
 this.dimensions = dimensions;
 }

                          private List<Item> additionalDetails;

         @TdoNode(name="Additional Details", path="/items[at0009:ITEM]")
  public List<Item> getAdditionalDetails()
 {
 if (additionalDetails == null)
  additionalDetails = new ArrayList<Item>();

 return additionalDetails;
 }

 public void setAdditionalDetails(List<Item> additionalDetails)
 {
 this.additionalDetails = additionalDetails;
 }

                          private List<Cluster> components;

         @TdoNode(name="Components", path="/items[at0020:CLUSTER]")
  public List<Cluster> getComponents()
 {
 if (components == null)
  components = new ArrayList<Cluster>();

 return components;
 }

 public void setComponents(List<Cluster> components)
 {
 this.components = components;
 }

              }
              private DvCodedText flowRateMode;

         @ValidDvCodedText(definingCode=@ValidCodePhrase(notNull=@NotNull, codeString=@ValidString(pattern=@Pattern(regexp="at0\\.108|at0\\.109"), notNull=@NotNull)))
@TdoNode(name="Flow rate mode", path="/items[at0.107:ELEMENT]/value", order=4)
  public DvCodedText getFlowRateMode()
 {
 return flowRateMode;
 }

 public void setFlowRateMode(DvCodedText flowRateMode)
 {
 this.flowRateMode = flowRateMode;
 }


 public FlowRateMode getFlowRateModeEnum()
{
return flowRateMode instanceof DvCodedText ? EnumTerminology.forEnum(FlowRateMode.class).getEnumByCode(((DvCodedText)flowRateMode).getDefiningCode().getCodeString()) : null;
}

public void setFlowRateModeEnum(FlowRateMode flowRateMode)
{
this.flowRateMode = flowRateMode == null ? null : DataValueUtils.getLocalCodedText(flowRateMode.getTerm().getCode(), flowRateMode.getTerm().getText());
}
                       public enum FlowRateMode implements EnumTerminology.TermEnum<FlowRateMode>
{
      LOW_FLOW("at0.108", "Low-flow", "A low-flow rate mode of oxygen delivery."),
      HIGH_FLOW("at0.109", "High-flow", "A high-flow rate mode of oxygen delivery.")
    ;
 FlowRateMode(String code, String text, String description)
{
terminologyTerm = new EnumTerminology.Term<FlowRateMode>(this, code, text, description);
}

private final EnumTerminology.Term<FlowRateMode> terminologyTerm;

public EnumTerminology.Term<FlowRateMode> getTerm()
{
return terminologyTerm;
}
public static final EnumTerminology<FlowRateMode> TERMINOLOGY = EnumTerminology.newInstance(FlowRateMode.class);
  }
              private AmbientOxygenCluster ambientOxygen;

         @TdoNode(name="Ambient oxygen", path="/items[openEHR-EHR-CLUSTER.ambient_oxygen-mnd.v1]", order=5)
  public AmbientOxygenCluster getAmbientOxygen()
 {
 return ambientOxygen;
 }

 public void setAmbientOxygen(AmbientOxygenCluster ambientOxygen)
 {
 this.ambientOxygen = ambientOxygen;
 }

                    @Archetype(name="Ambient oxygen", archetypeId="openEHR-EHR-CLUSTER.ambient_oxygen-mnd.v1")
public static class AmbientOxygenCluster extends Cluster    {
               @Deprecated
@Override
public List<Item> getItems()
{
     TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
 return super.getItems();
    }

                     private DvQuantity oxygenFlowRate;

         @ValidDvQuantity(precision=@ValidNumber(min=@Min(value=2, inclusive=true), max=@Max(value=2, inclusive=true)), magnitude=@ValidDouble(min=@DecimalMin(value=0.0, inclusive=true), max=@DecimalMax(value=100.0, inclusive=true)), units=@ValidString(pattern=@Pattern(regexp="l\\/m"), notNull=@NotNull))
@TdoNode(name="Oxygen flow rate", path="/items[at0051:ELEMENT]/value")
  public DvQuantity getOxygenFlowRate()
 {
 return oxygenFlowRate;
 }

 public void setOxygenFlowRate(DvQuantity oxygenFlowRate)
 {
 this.oxygenFlowRate = oxygenFlowRate;
 }

                                      private DvProportion fiO2;

         @ValidDvProportion(type=@ValidNumber(contained=@Contained({"1"})), numerator=@ValidDouble(min=@DecimalMin(value=0.0, inclusive=true), max=@DecimalMax(value=1.0, inclusive=true)))
@TdoNode(name="FiO2", path="/items[at0052:ELEMENT]/value")
  public DvProportion getFiO2()
 {
 return fiO2;
 }

 public void setFiO2(DvProportion fiO2)
 {
 this.fiO2 = fiO2;
 }

                                      private DvProportion minimumPercentO2;

         @ValidDvProportion(numerator=@ValidDouble(min=@DecimalMin(value=0.0, inclusive=true), max=@DecimalMax(value=100.0, inclusive=true)))
@TdoNode(name="Minimum Percent O2", path="/items[at0053.1:ELEMENT]/value")
  public DvProportion getMinimumPercentO2()
 {
 return minimumPercentO2;
 }

 public void setMinimumPercentO2(DvProportion minimumPercentO2)
 {
 this.minimumPercentO2 = minimumPercentO2;
 }

                                      private DvProportion maximumPercentO2;

         @ValidDvProportion(numerator=@ValidDouble(min=@DecimalMin(value=0.0, inclusive=true), max=@DecimalMax(value=100.0, inclusive=true)))
@TdoNode(name="Maximum Percent O2", path="/items[at0.54:ELEMENT]/value")
  public DvProportion getMaximumPercentO2()
 {
 return maximumPercentO2;
 }

 public void setMaximumPercentO2(DvProportion maximumPercentO2)
 {
 this.maximumPercentO2 = maximumPercentO2;
 }

                          }
              private Cluster trachealTubeDetail;

         @TdoNode(name="Tracheal tube detail", path="/items[at0097:CLUSTER]", order=6)
  public Cluster getTrachealTubeDetail()
 {
 return trachealTubeDetail;
 }

 public void setTrachealTubeDetail(Cluster trachealTubeDetail)
 {
 this.trachealTubeDetail = trachealTubeDetail;
 }

                                      private Cluster trachealCannulaDetail;

         @TdoNode(name="Tracheal cannula detail", path="/items[at0.97:CLUSTER]", order=7)
  public Cluster getTrachealCannulaDetail()
 {
 return trachealCannulaDetail;
 }

 public void setTrachealCannulaDetail(Cluster trachealCannulaDetail)
 {
 this.trachealCannulaDetail = trachealCannulaDetail;
 }

                                      private HumidifierCluster humidifier;

         @TdoNode(name="Humidifier", path="/items[at0003:CLUSTER]", order=8)
  public HumidifierCluster getHumidifier()
 {
 return humidifier;
 }

 public void setHumidifier(HumidifierCluster humidifier)
 {
 this.humidifier = humidifier;
 }

                    public static class HumidifierCluster extends Cluster    {
               @Deprecated
@Override
public List<Item> getItems()
{
     TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
 return super.getItems();
    }

                     private DvBoolean humidiferUsed;

         @TdoNode(name="Humidifer used", path="/items[at0016:ELEMENT]/value", order=1)
  public DvBoolean getHumidiferUsed()
 {
 return humidiferUsed;
 }

 public void setHumidiferUsed(DvBoolean humidiferUsed)
 {
 this.humidiferUsed = humidiferUsed;
 }

                          }
  }
      }