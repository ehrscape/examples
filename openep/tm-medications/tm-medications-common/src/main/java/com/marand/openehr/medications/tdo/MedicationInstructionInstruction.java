// Generated using Marand-EHR TDO Generator vUnknown
// Source: OPENEP - Pharmacy Review Report.opt
// Time: 2016-01-22T12:48:06.108+01:00
package com.marand.openehr.medications.tdo;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.marand.openehr.tdo.EnumTerminology;
import com.marand.openehr.tdo.TdoAccess;
import com.marand.openehr.tdo.annotations.Archetype;
import com.marand.openehr.tdo.annotations.TdoNode;
import com.marand.openehr.tdo.validation.constraints.DecimalMin;
import com.marand.openehr.tdo.validation.constraints.DurationMin;
import com.marand.openehr.tdo.validation.constraints.ValidCodePhrase;
import com.marand.openehr.tdo.validation.constraints.ValidDouble;
import com.marand.openehr.tdo.validation.constraints.ValidDuration;
import com.marand.openehr.tdo.validation.constraints.ValidDvCodedText;
import com.marand.openehr.tdo.validation.constraints.ValidDvDuration;
import com.marand.openehr.tdo.validation.constraints.ValidDvQuantities;
import com.marand.openehr.tdo.validation.constraints.ValidDvQuantity;
import com.marand.openehr.tdo.validation.constraints.ValidString;
import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.Activity;
import org.openehr.jaxb.rm.Cluster;
import org.openehr.jaxb.rm.DataValue;
import org.openehr.jaxb.rm.DvBoolean;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvCount;
import org.openehr.jaxb.rm.DvDate;
import org.openehr.jaxb.rm.DvDateTime;
import org.openehr.jaxb.rm.DvDuration;
import org.openehr.jaxb.rm.DvIdentifier;
import org.openehr.jaxb.rm.DvParsable;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.DvTime;
import org.openehr.jaxb.rm.Instruction;
import org.openehr.jaxb.rm.Item;
import org.openehr.jaxb.rm.ItemStructure;

@Archetype(name = "Medication instruction", archetypeId = "openEHR-EHR-INSTRUCTION.medication.v1")
public class MedicationInstructionInstruction extends Instruction
{
  @Deprecated
  @Override
  public List<Activity> getActivities()
  {
    TdoAccess.check("Property 'Activities' is deprecated in: " + getClass().getName());
    return super.getActivities();
  }

  private List<OrderActivity> order;

  @TdoNode(name = "Order", path = "/activities[at0001]")
  public List<OrderActivity> getOrder()
  {
    if (order == null)
    {
      order = new ArrayList<>();
    }

    return order;
  }

  public void setOrder(List<OrderActivity> order)
  {
    this.order = order;
  }

  public static class OrderActivity extends Activity
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

    private DvText medicine;

    @NotNull
    @TdoNode(name = "Medicine", path = "/description[at0002:ITEM_TREE]/items[at0003:ELEMENT]/value")
    public DvText getMedicine()
    {
      return medicine;
    }

    public void setMedicine(DvText medicine)
    {
      this.medicine = medicine;
    }

    private DvText directions;

    @TdoNode(name = "Directions", path = "/description[at0002:ITEM_TREE]/items[at0009:ELEMENT]/value")
    public DvText getDirections()
    {
      return directions;
    }

    public void setDirections(DvText directions)
    {
      this.directions = directions;
    }

    private DvParsable formattedDirections;

    @TdoNode(name = "Formatted directions", path = "/description[at0002:ITEM_TREE]/items[at0047:ELEMENT]/value")
    public DvParsable getFormattedDirections()
    {
      return formattedDirections;
    }

    public void setFormattedDirections(DvParsable formattedDirections)
    {
      this.formattedDirections = formattedDirections;
    }

    private IngredientsAndFormCluster ingredientsAndForm;

    @TdoNode(name = "Ingredients and form", path = "/description[at0002:ITEM_TREE]/items[openEHR-EHR-CLUSTER.chemical_description_mnd.v1]")
    public IngredientsAndFormCluster getIngredientsAndForm()
    {
      return ingredientsAndForm;
    }

    public void setIngredientsAndForm(IngredientsAndFormCluster ingredientsAndForm)
    {
      this.ingredientsAndForm = ingredientsAndForm;
    }

    private DvText doseDescription;

    @TdoNode(name = "Dose description", path = "/description[at0002:ITEM_TREE]/items[at0005:ELEMENT]/value")
    public DvText getDoseDescription()
    {
      return doseDescription;
    }

    public void setDoseDescription(DvText doseDescription)
    {
      this.doseDescription = doseDescription;
    }

    private DvParsable parsableDoseDescription;

    @TdoNode(name = "Parsable dose description", path = "/description[at0002:ITEM_TREE]/items[at0055:ELEMENT]/value")
    public DvParsable getParsableDoseDescription()
    {
      return parsableDoseDescription;
    }

    public void setParsableDoseDescription(DvParsable parsableDoseDescription)
    {
      this.parsableDoseDescription = parsableDoseDescription;
    }

    private StructuredDoseCluster structuredDose;

    @TdoNode(name = "Structured dose", path = "/description[at0002:ITEM_TREE]/items[openEHR-EHR-CLUSTER.medication_amount.v1]")
    public StructuredDoseCluster getStructuredDose()
    {
      return structuredDose;
    }

    public void setStructuredDose(StructuredDoseCluster structuredDose)
    {
      this.structuredDose = structuredDose;
    }

    private MedicationTimingCluster medicationTiming;

    @TdoNode(name = "Medication timing", path = "/description[at0002:ITEM_TREE]/items[at0010:CLUSTER]")
    public MedicationTimingCluster getMedicationTiming()
    {
      return medicationTiming;
    }

    public void setMedicationTiming(MedicationTimingCluster medicationTiming)
    {
      this.medicationTiming = medicationTiming;
    }

    public static class MedicationTimingCluster extends Cluster
    {
      @Deprecated
      @Override
      public List<Item> getItems()
      {
        TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
        return super.getItems();
      }

      private DvText timingDescription;

      @TdoNode(name = "Timing description", path = "/items[at0008:ELEMENT]/value")
      public DvText getTimingDescription()
      {
        return timingDescription;
      }

      public void setTimingDescription(DvText timingDescription)
      {
        this.timingDescription = timingDescription;
      }

      private TimingCluster timing;

      @TdoNode(name = "Timing", path = "/items[openEHR-EHR-CLUSTER.timing.v1]")
      public TimingCluster getTiming()
      {
        return timing;
      }

      public void setTiming(TimingCluster timing)
      {
        this.timing = timing;
      }

      @Archetype(name = "Timing", archetypeId = "openEHR-EHR-CLUSTER.timing.v1")
      public static class TimingCluster extends Cluster
      {
        @Deprecated
        @Override
        public List<Item> getItems()
        {
          TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
          return super.getItems();
        }

        private DvCount dailyCount;

        @TdoNode(name = "Daily count", path = "/items[at0001:ELEMENT]/value")
        public DvCount getDailyCount()
        {
          return dailyCount;
        }

        public void setDailyCount(DvCount dailyCount)
        {
          this.dailyCount = dailyCount;
        }

        private DvQuantity frequency;

        @ValidDvQuantities({
            @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = "\\/d"), notNull = @NotNull)),
            @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = "\\/wk"), notNull = @NotNull)),
            @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = "\\/mo"), notNull = @NotNull)),
            @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = "\\/yr"), notNull = @NotNull)),
            @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = "\\/min"), notNull = @NotNull)),
            @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = "\\/s"), notNull = @NotNull)),
            @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = "\\/h"), notNull = @NotNull))})
        @TdoNode(name = "Frequency", path = "/items[at0003:ELEMENT]/value")
        public DvQuantity getFrequency()
        {
          return frequency;
        }

        public void setFrequency(DvQuantity frequency)
        {
          this.frequency = frequency;
        }

        private DvDuration interval;

        @TdoNode(name = "Interval", path = "/items[at0014:ELEMENT]/value")
        public DvDuration getInterval()
        {
          return interval;
        }

        public void setInterval(DvDuration interval)
        {
          this.interval = interval;
        }

        private VariableFrequencyCluster variableFrequency;

        @TdoNode(name = "Variable frequency", path = "/items[at0015:CLUSTER]")
        public VariableFrequencyCluster getVariableFrequency()
        {
          return variableFrequency;
        }

        public void setVariableFrequency(VariableFrequencyCluster variableFrequency)
        {
          this.variableFrequency = variableFrequency;
        }

        public static class VariableFrequencyCluster extends Cluster
        {
          @Deprecated
          @Override
          public List<Item> getItems()
          {
            TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
            return super.getItems();
          }

          private UpperCluster upper;

          @TdoNode(name = "Upper", path = "/items[at0016:CLUSTER]")
          public UpperCluster getUpper()
          {
            return upper;
          }

          public void setUpper(UpperCluster upper)
          {
            this.upper = upper;
          }

          public static class UpperCluster extends Cluster
          {
            @Deprecated
            @Override
            public List<Item> getItems()
            {
              TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
              return super.getItems();
            }

            private DvQuantity frequency;

            @ValidDvQuantities({
                @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = "\\/d"), notNull = @NotNull)),
                @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = "\\/wk"), notNull = @NotNull)),
                @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = "\\/mo"), notNull = @NotNull)),
                @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = "\\/yr"), notNull = @NotNull)),
                @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = "\\/min"), notNull = @NotNull)),
                @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = "\\/s"), notNull = @NotNull)),
                @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = "\\/h"), notNull = @NotNull))})
            @TdoNode(name = "Frequency", path = "/items[at0003:ELEMENT]/value")
            public DvQuantity getFrequency()
            {
              return frequency;
            }

            public void setFrequency(DvQuantity frequency)
            {
              this.frequency = frequency;
            }
          }

          private LowerCluster lower;

          @TdoNode(name = "Lower", path = "/items[at0017:CLUSTER]")
          public LowerCluster getLower()
          {
            return lower;
          }

          public void setLower(LowerCluster lower)
          {
            this.lower = lower;
          }

          public static class LowerCluster extends Cluster
          {
            @Deprecated
            @Override
            public List<Item> getItems()
            {
              TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
              return super.getItems();
            }

            private DvQuantity frequency;

            @ValidDvQuantities({
                @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = "\\/d"), notNull = @NotNull)),
                @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = "\\/wk"), notNull = @NotNull)),
                @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = "\\/mo"), notNull = @NotNull)),
                @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = "\\/yr"), notNull = @NotNull)),
                @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = "\\/min"), notNull = @NotNull)),
                @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = "\\/s"), notNull = @NotNull)),
                @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = "\\/h"), notNull = @NotNull))})
            @TdoNode(name = "Frequency", path = "/items[at0003:ELEMENT]/value")
            public DvQuantity getFrequency()
            {
              return frequency;
            }

            public void setFrequency(DvQuantity frequency)
            {
              this.frequency = frequency;
            }
          }
        }

        private VariableIntervalCluster variableInterval;

        @TdoNode(name = "Variable interval", path = "/items[at0019:CLUSTER]")
        public VariableIntervalCluster getVariableInterval()
        {
          return variableInterval;
        }

        public void setVariableInterval(VariableIntervalCluster variableInterval)
        {
          this.variableInterval = variableInterval;
        }

        public static class VariableIntervalCluster extends Cluster
        {
          @Deprecated
          @Override
          public List<Item> getItems()
          {
            TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
            return super.getItems();
          }

          private UpperCluster upper;

          @TdoNode(name = "Upper", path = "/items[at0020:CLUSTER]")
          public UpperCluster getUpper()
          {
            return upper;
          }

          public void setUpper(UpperCluster upper)
          {
            this.upper = upper;
          }

          public static class UpperCluster extends Cluster
          {
            @Deprecated
            @Override
            public List<Item> getItems()
            {
              TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
              return super.getItems();
            }

            private DvDuration interval;

            @TdoNode(name = "Interval", path = "/items[at0014:ELEMENT]/value")
            public DvDuration getInterval()
            {
              return interval;
            }

            public void setInterval(DvDuration interval)
            {
              this.interval = interval;
            }
          }

          private LowerCluster lower;

          @TdoNode(name = "Lower", path = "/items[at0021:CLUSTER]")
          public LowerCluster getLower()
          {
            return lower;
          }

          public void setLower(LowerCluster lower)
          {
            this.lower = lower;
          }

          public static class LowerCluster extends Cluster
          {
            @Deprecated
            @Override
            public List<Item> getItems()
            {
              TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
              return super.getItems();
            }

            private DvDuration interval;

            @TdoNode(name = "Interval", path = "/items[at0014:ELEMENT]/value")
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

        private List<DvTime> time;

        @TdoNode(name = "Time", path = "/items[at0004:ELEMENT]/value")
        public List<DvTime> getTime()
        {
          if (time == null)
          {
            time = new ArrayList<>();
          }

          return time;
        }

        public void setTime(List<DvTime> time)
        {
          this.time = time;
        }

        private List<DvCodedText> dayOfWeek;

        @ValidDvCodedText(definingCode = @ValidCodePhrase(codeString = @ValidString(pattern = @Pattern(regexp = "at0007|at0008|at0009|at0010|at0011|at0012|at0013"), notNull = @NotNull), notNull = @NotNull))
        @TdoNode(name = "Day of week", path = "/items[at0006:ELEMENT]/value")
        public List<DvCodedText> getDayOfWeek()
        {
          if (dayOfWeek == null)
          {
            dayOfWeek = new ArrayList<>();
          }

          return dayOfWeek;
        }

        public void setDayOfWeek(List<DvCodedText> dayOfWeek)
        {
          this.dayOfWeek = dayOfWeek;
        }

        public enum DayOfWeek implements EnumTerminology.TermEnum<DayOfWeek>
        {
          MONDAY("at0007", "Monday", "Monday."),
          TUESDAY("at0008", "Tuesday", "Tuesday."),
          WEDNESDAY("at0009", "Wednesday", "Wednesday."),
          THURSDAY("at0010", "Thursday", "Thursday."),
          FRIDAY("at0011", "Friday", "Friday."),
          SATURDAY("at0012", "Saturday", "Saturday."),
          SUNDAY("at0013", "Sunday", "Sunday.");

          DayOfWeek(String code, String text, String description)
          {
            terminologyTerm = new EnumTerminology.Term<>(this, code, text, description);
          }

          private final EnumTerminology.Term<DayOfWeek> terminologyTerm;

          public EnumTerminology.Term<DayOfWeek> getTerm()
          {
            return terminologyTerm;
          }

          public static final EnumTerminology<DayOfWeek> TERMINOLOGY = EnumTerminology.newInstance(DayOfWeek.class);
        }

        private List<DvCount> dayOfMonth;

        @TdoNode(name = "Day of month", path = "/items[at0005:ELEMENT]/value")
        public List<DvCount> getDayOfMonth()
        {
          if (dayOfMonth == null)
          {
            dayOfMonth = new ArrayList<>();
          }

          return dayOfMonth;
        }

        public void setDayOfMonth(List<DvCount> dayOfMonth)
        {
          this.dayOfMonth = dayOfMonth;
        }

        private List<DvDate> date;

        @TdoNode(name = "Date", path = "/items[at0018:ELEMENT]/value")
        public List<DvDate> getDate()
        {
          if (date == null)
          {
            date = new ArrayList<>();
          }

          return date;
        }

        public void setDate(List<DvDate> date)
        {
          this.date = date;
        }
      }

      private DvBoolean pRN;

      @TdoNode(name = "PRN", path = "/items[at0029:ELEMENT]/value")
      public DvBoolean getPRN()
      {
        return pRN;
      }

      public void setPRN(DvBoolean pRN)
      {
        this.pRN = pRN;
      }

      private List<DvText> startCriterion;

      @TdoNode(name = "Start criterion", path = "/items[at0011:ELEMENT]/value")
      public List<DvText> getStartCriterion()
      {
        if (startCriterion == null)
        {
          startCriterion = new ArrayList<>();
        }

        return startCriterion;
      }

      public void setStartCriterion(List<DvText> startCriterion)
      {
        this.startCriterion = startCriterion;
      }

      private DvDateTime startDate;

      @TdoNode(name = "Start date", path = "/items[at0012:ELEMENT]/value")
      public DvDateTime getStartDate()
      {
        return startDate;
      }

      public void setStartDate(DvDateTime startDate)
      {
        this.startDate = startDate;
      }

      private List<DvText> stopCriterion;

      @TdoNode(name = "Stop criterion", path = "/items[at0016:ELEMENT]/value")
      public List<DvText> getStopCriterion()
      {
        if (stopCriterion == null)
        {
          stopCriterion = new ArrayList<>();
        }

        return stopCriterion;
      }

      public void setStopCriterion(List<DvText> stopCriterion)
      {
        this.stopCriterion = stopCriterion;
      }

      private DvDateTime stopDate;

      @TdoNode(name = "Stop date", path = "/items[at0013:ELEMENT]/value")
      public DvDateTime getStopDate()
      {
        return stopDate;
      }

      public void setStopDate(DvDateTime stopDate)
      {
        this.stopDate = stopDate;
      }

      private DvDuration durationOfTreatment;

      @TdoNode(name = "Duration of treatment", path = "/items[at0014:ELEMENT]/value")
      public DvDuration getDurationOfTreatment()
      {
        return durationOfTreatment;
      }

      public void setDurationOfTreatment(DvDuration durationOfTreatment)
      {
        this.durationOfTreatment = durationOfTreatment;
      }

      private DvDuration durationOfPriorTreatment;

      @ValidDvDuration(@ValidDuration(min = @DurationMin("PT0H"), pattern = @Pattern(regexp = "P(\\d+W)?(\\d+D)?T(\\d+H)?")))
      @TdoNode(name = "Duration of prior treatment", path = "/items[at0050:ELEMENT]/value")
      public DvDuration getDurationOfPriorTreatment()
      {
        return durationOfPriorTreatment;
      }

      public void setDurationOfPriorTreatment(DvDuration durationOfPriorTreatment)
      {
        this.durationOfPriorTreatment = durationOfPriorTreatment;
      }

      private DvCount numberOfAdministrations;

      @TdoNode(name = "Number of administrations", path = "/items[at0015:ELEMENT]/value")
      public DvCount getNumberOfAdministrations()
      {
        return numberOfAdministrations;
      }

      public void setNumberOfAdministrations(DvCount numberOfAdministrations)
      {
        this.numberOfAdministrations = numberOfAdministrations;
      }

      private DvBoolean longTerm;

      @TdoNode(name = "Long-term", path = "/items[at0017:ELEMENT]/value")
      public DvBoolean getLongTerm()
      {
        return longTerm;
      }

      public void setLongTerm(DvBoolean longTerm)
      {
        this.longTerm = longTerm;
      }
    }

    private List<MaximumDoseCluster> maximumDose;

    @TdoNode(name = "Maximum dose", path = "/description[at0002:ITEM_TREE]/items[at0051:CLUSTER]")
    public List<MaximumDoseCluster> getMaximumDose()
    {
      if (maximumDose == null)
      {
        maximumDose = new ArrayList<>();
      }

      return maximumDose;
    }

    public void setMaximumDose(List<MaximumDoseCluster> maximumDose)
    {
      this.maximumDose = maximumDose;
    }

    public static class MaximumDoseCluster extends Cluster
    {
      @Deprecated
      @Override
      public List<Item> getItems()
      {
        TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
        return super.getItems();
      }

      private MedicationAmountCluster medicationAmount;

      @TdoNode(name = "Medication amount", path = "/items[openEHR-EHR-CLUSTER.medication_amount.v1]")
      public MedicationAmountCluster getMedicationAmount()
      {
        return medicationAmount;
      }

      public void setMedicationAmount(MedicationAmountCluster medicationAmount)
      {
        this.medicationAmount = medicationAmount;
      }

      @Archetype(name = "Medication amount", archetypeId = "openEHR-EHR-CLUSTER.medication_amount.v1")
      public static class MedicationAmountCluster extends Cluster
      {
        @Deprecated
        @Override
        public List<Item> getItems()
        {
          TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
          return super.getItems();
        }

        private DataValue amount;

        @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = ""), notNull = @NotNull))
        @TdoNode(name = "Amount", path = "/items[at0001:ELEMENT]/value")
        public DataValue getAmount()
        {
          return amount;
        }

        public void setAmount(DataValue amount)
        {
          this.amount = amount;
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

        private DvText description;

        @TdoNode(name = "Description", path = "/items[at0003:ELEMENT]/value")
        public DvText getDescription()
        {
          return description;
        }

        public void setDescription(DvText description)
        {
          this.description = description;
        }

        private RatioNumeratorCluster ratioNumerator;

        @TdoNode(name = "Ratio numerator", path = "/items[at0008:CLUSTER]")
        public RatioNumeratorCluster getRatioNumerator()
        {
          return ratioNumerator;
        }

        public void setRatioNumerator(RatioNumeratorCluster ratioNumerator)
        {
          this.ratioNumerator = ratioNumerator;
        }

        public static class RatioNumeratorCluster extends Cluster
        {
          @Deprecated
          @Override
          public List<Item> getItems()
          {
            TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
            return super.getItems();
          }

          private DataValue amount;

          @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = ""), notNull = @NotNull))
          @TdoNode(name = "Amount", path = "/items[at0001:ELEMENT]/value")
          public DataValue getAmount()
          {
            return amount;
          }

          public void setAmount(DataValue amount)
          {
            this.amount = amount;
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

        private RatioDenominatorCluster ratioDenominator;

        @TdoNode(name = "Ratio denominator", path = "/items[at0007:CLUSTER]")
        public RatioDenominatorCluster getRatioDenominator()
        {
          return ratioDenominator;
        }

        public void setRatioDenominator(RatioDenominatorCluster ratioDenominator)
        {
          this.ratioDenominator = ratioDenominator;
        }

        public static class RatioDenominatorCluster extends Cluster
        {
          @Deprecated
          @Override
          public List<Item> getItems()
          {
            TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
            return super.getItems();
          }

          private DataValue amount;

          @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = ""), notNull = @NotNull))
          @TdoNode(name = "Amount", path = "/items[at0001:ELEMENT]/value")
          public DataValue getAmount()
          {
            return amount;
          }

          public void setAmount(DataValue amount)
          {
            this.amount = amount;
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
      }

      private DataValue allowedPeriod;

      @ValidDvDuration(@ValidDuration(min = @DurationMin("P0Y"), pattern = @Pattern(regexp = "P(\\d+Y)?(\\d+M)?(\\d+W)?(\\d+D)?")))
      @ValidDvCodedText(definingCode = @ValidCodePhrase(codeString = @ValidString(pattern = @Pattern(regexp = "at0054"), notNull = @NotNull), notNull = @NotNull))
      @TdoNode(name = "Allowed period", path = "/items[at0053:ELEMENT]/value")
      public DataValue getAllowedPeriod()
      {
        return allowedPeriod;
      }

      public void setAllowedPeriod(DataValue allowedPeriod)
      {
        this.allowedPeriod = allowedPeriod;
      }

      public AllowedPeriod getAllowedPeriodEnum()
      {
        return allowedPeriod instanceof DvCodedText ? EnumTerminology.forEnum(AllowedPeriod.class)
            .getEnumByCode(((DvCodedText)allowedPeriod).getDefiningCode().getCodeString()) : null;
      }

      public void setAllowedPeriodEnum(AllowedPeriod allowedPeriod)
      {
        this.allowedPeriod = allowedPeriod == null
                             ? null
                             : DataValueUtils.getLocalCodedText(
                                 allowedPeriod.getTerm().getCode(),
                                 allowedPeriod.getTerm().getText());
      }

      public enum AllowedPeriod implements EnumTerminology.TermEnum<AllowedPeriod>
      {
        LIFETIME("at0054", "Lifetime", "The maximal dose amount applies ot a whole lifetime.");

        AllowedPeriod(String code, String text, String description)
        {
          terminologyTerm = new EnumTerminology.Term<>(this, code, text, description);
        }

        private final EnumTerminology.Term<AllowedPeriod> terminologyTerm;

        public EnumTerminology.Term<AllowedPeriod> getTerm()
        {
          return terminologyTerm;
        }

        public static final EnumTerminology<AllowedPeriod> TERMINOLOGY = EnumTerminology.newInstance(AllowedPeriod.class);
      }
    }

    private List<DvText> additionalInstruction;

    @TdoNode(name = "Additional instruction", path = "/description[at0002:ITEM_TREE]/items[at0044:ELEMENT]/value")
    public List<DvText> getAdditionalInstruction()
    {
      if (additionalInstruction == null)
      {
        additionalInstruction = new ArrayList<>();
      }

      return additionalInstruction;
    }

    public void setAdditionalInstruction(List<DvText> additionalInstruction)
    {
      this.additionalInstruction = additionalInstruction;
    }

    private List<DvText> clinicalIndication;

    @TdoNode(name = "Clinical Indication", path = "/description[at0002:ITEM_TREE]/items[at0018:ELEMENT]/value")
    public List<DvText> getClinicalIndication()
    {
      if (clinicalIndication == null)
      {
        clinicalIndication = new ArrayList<>();
      }

      return clinicalIndication;
    }

    public void setClinicalIndication(List<DvText> clinicalIndication)
    {
      this.clinicalIndication = clinicalIndication;
    }

    private AdministrationDetailsCluster administrationDetails;

    @TdoNode(name = "Administration details", path = "/description[at0002:ITEM_TREE]/items[openEHR-EHR-CLUSTER.medication_admin.v1]")
    public AdministrationDetailsCluster getAdministrationDetails()
    {
      return administrationDetails;
    }

    public void setAdministrationDetails(AdministrationDetailsCluster administrationDetails)
    {
      this.administrationDetails = administrationDetails;
    }

    private List<DvText> comment;

    @TdoNode(name = "Comment", path = "/description[at0002:ITEM_TREE]/items[at0035:ELEMENT]/value")
    public List<DvText> getComment()
    {
      if (comment == null)
      {
        comment = new ArrayList<>();
      }

      return comment;
    }

    public void setComment(List<DvText> comment)
    {
      this.comment = comment;
    }

    private DvCount pastDaysOfTherapy;

    @TdoNode(name = "Past days of therapy", path = "/description[at0002:ITEM_TREE]/items[at0049:ELEMENT]/value")
    public DvCount getPastDaysOfTherapy()
    {
      return pastDaysOfTherapy;
    }

    public void setPastDaysOfTherapy(DvCount pastDaysOfTherapy)
    {
      this.pastDaysOfTherapy = pastDaysOfTherapy;
    }

    private List<MedicationAuthorisationSloveniaCluster> authorisationDetails;

    @TdoNode(name = "Medication authorisation (slovenia)", path = "/description[at0002:ITEM_TREE]/items[openEHR-EHR-CLUSTER.medication_authorisation_sl.v0]")
    public List<MedicationAuthorisationSloveniaCluster> getAuthorisationDetails()
    {
      if (authorisationDetails == null)
      {
        authorisationDetails = new ArrayList<>();
      }

      return authorisationDetails;
    }

    public void setAuthorisationDetails(List<MedicationAuthorisationSloveniaCluster> authorisationDetails)
    {
      this.authorisationDetails = authorisationDetails;
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

  private List<DvText> indicationForAuthorisedUse;

  @TdoNode(name = "Indication for authorised use", path = "/protocol[at0031:ITEM_TREE]/items[at0038:ELEMENT]/value")
  public List<DvText> getIndicationForAuthorisedUse()
  {
    if (indicationForAuthorisedUse == null)
    {
      indicationForAuthorisedUse = new ArrayList<>();
    }

    return indicationForAuthorisedUse;
  }

  public void setIndicationForAuthorisedUse(List<DvText> indicationForAuthorisedUse)
  {
    this.indicationForAuthorisedUse = indicationForAuthorisedUse;
  }

  private List<DvIdentifier> medicationInstructionId;

  @TdoNode(name = "Medication Instruction Id", path = "/protocol[at0031:ITEM_TREE]/items[at0032:ELEMENT]/value")
  public List<DvIdentifier> getMedicationInstructionId()
  {
    if (medicationInstructionId == null)
    {
      medicationInstructionId = new ArrayList<>();
    }

    return medicationInstructionId;
  }

  public void setMedicationInstructionId(List<DvIdentifier> medicationInstructionId)
  {
    this.medicationInstructionId = medicationInstructionId;
  }

  private DvText concessionBenefit;

  @TdoNode(name = "Concession benefit", path = "/protocol[at0031:ITEM_TREE]/items[at0042:ELEMENT]/value")
  public DvText getConcessionBenefit()
  {
    return concessionBenefit;
  }

  public void setConcessionBenefit(DvText concessionBenefit)
  {
    this.concessionBenefit = concessionBenefit;
  }
}