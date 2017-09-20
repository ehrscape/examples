// Generated using Marand-EHR TDO Generator vUnknown
// Source: ISPEK - MED - Medication reference weight.opt
// Time: 2016-01-22T12:48:05.985+01:00
package com.marand.openehr.medications.tdo;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.marand.ispek.ehr.common.tdo.CompositionEventContext;
import com.marand.openehr.tdo.TdoAccess;
import com.marand.openehr.tdo.annotations.Archetype;
import com.marand.openehr.tdo.annotations.TdoNode;
import com.marand.openehr.tdo.annotations.Template;
import com.marand.openehr.tdo.validation.constraints.DecimalMax;
import com.marand.openehr.tdo.validation.constraints.DecimalMin;
import com.marand.openehr.tdo.validation.constraints.ValidDouble;
import com.marand.openehr.tdo.validation.constraints.ValidDvQuantities;
import com.marand.openehr.tdo.validation.constraints.ValidDvQuantity;
import com.marand.openehr.tdo.validation.constraints.ValidString;
import org.openehr.jaxb.rm.Cluster;
import org.openehr.jaxb.rm.Composition;
import org.openehr.jaxb.rm.ContentItem;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.Event;
import org.openehr.jaxb.rm.EventContext;
import org.openehr.jaxb.rm.History;
import org.openehr.jaxb.rm.ItemStructure;
import org.openehr.jaxb.rm.Observation;
   
   @Template(name="Medication reference weight", templateId="ISPEK - MED - Medication reference weight")
   @Archetype(name="Medication reference weight", archetypeId="openEHR-EHR-COMPOSITION.encounter.v1")
 public class MedicationReferenceWeightComposition extends Composition  {
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

                        private MedicationReferenceBodyWeightObservation medicationReferenceBodyWeight;

            @TdoNode(name="Medication reference body weight", path="/content[openEHR-EHR-OBSERVATION.body_weight.v1]")
     public MedicationReferenceBodyWeightObservation getMedicationReferenceBodyWeight()
    {
    return medicationReferenceBodyWeight;
    }

    public void setMedicationReferenceBodyWeight(MedicationReferenceBodyWeightObservation medicationReferenceBodyWeight)
    {
    this.medicationReferenceBodyWeight = medicationReferenceBodyWeight;
    }
     
                       @Archetype(name="Medication reference body weight", archetypeId="openEHR-EHR-OBSERVATION.body_weight.v1")
   public static class MedicationReferenceBodyWeightObservation extends Observation    {
                  @Deprecated
   @Override
   public History getData()
   {
        TdoAccess.check("Property 'Data' is deprecated in: " + getClass().getName());
    return super.getData();
       }

        @Deprecated
    @Override
    public void setData(History value)
    {
          TdoAccess.check("Property 'Data' is deprecated in: " + getClass().getName());
     super.setData(value);
         }
                        private HistoryHistory historyHistory;

            @TdoNode(name="history", path="/data[at0002]")
     public HistoryHistory getHistoryHistory()
    {
    return historyHistory;
    }

    public void setHistoryHistory(HistoryHistory historyHistory)
    {
    this.historyHistory = historyHistory;
    }
     
                       public static class HistoryHistory extends History    {
                  @Deprecated
   @Override
   public List<Event> getEvents()
   {
        TdoAccess.check("Property 'Events' is deprecated in: " + getClass().getName());
    return super.getEvents();
       }

                        private List<AnyEventEvent> anyEvent;

            @TdoNode(name="Any event", path="/events[at0003:EVENT]")
     public List<AnyEventEvent> getAnyEvent()
    {
    if (anyEvent == null)
    {
      anyEvent = new ArrayList<>();
    }

    return anyEvent;
    }

    public void setAnyEvent(List<AnyEventEvent> anyEvent)
    {
    this.anyEvent = anyEvent;
    }

                       public static class AnyEventEvent extends Event    {
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
                        private DvQuantity weight;

            @ValidDvQuantities({@ValidDvQuantity(magnitude=@ValidDouble(min=@DecimalMin(value=0.0, inclusive=true), max=@DecimalMax(value=1000.0, inclusive=true)), units=@ValidString(pattern=@Pattern(regexp="kg"), notNull=@NotNull)),@ValidDvQuantity(magnitude=@ValidDouble(min=@DecimalMin(value=0.0, inclusive=true), max=@DecimalMax(value=2000.0, inclusive=true)), units=@ValidString(pattern=@Pattern(regexp="lb"), notNull=@NotNull))})
   @TdoNode(name="Weight", path="/data[at0001:ITEM_TREE]/items[at0004:ELEMENT]/value")
     public DvQuantity getWeight()
    {
    return weight;
    }

    public void setWeight(DvQuantity weight)
    {
    this.weight = weight;
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
                        private Cluster device;

            @TdoNode(name="Device", path="/protocol[at0015:ITEM_TREE]/items[at0020:CLUSTER]", order=1)
     public Cluster getDevice()
    {
    return device;
    }

    public void setDevice(Cluster device)
    {
    this.device = device;
    }
     
                             }
      }