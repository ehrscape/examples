// Generated using Marand-EHR TDO Generator vUnknown
// Source: OPENEP - Pharmacy Review Report.opt
// Time: 2016-01-22T12:48:06.153+01:00
package com.marand.openehr.medications.tdo;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition.MiscellaneousSection.Role;
import com.marand.openehr.tdo.EnumTerminology;
import com.marand.openehr.tdo.TdoAccess;
import com.marand.openehr.tdo.annotations.Archetype;
import com.marand.openehr.tdo.annotations.TdoNode;
import com.marand.openehr.tdo.validation.constraints.ValidCodePhrase;
import com.marand.openehr.tdo.validation.constraints.ValidDvCodedText;
import com.marand.openehr.tdo.validation.constraints.ValidString;
import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.Cluster;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.Item;
   
   @Archetype(name="Ingredients and form", archetypeId="openEHR-EHR-CLUSTER.chemical_description_mnd.v1")
 public class IngredientsAndFormCluster extends Cluster  {
               @Deprecated
   @Override
   public List<Item> getItems()
   {
        TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
    return super.getItems();
       }

                        private DvText form;

            @TdoNode(name="Form", path="/items[at0010:ELEMENT]/value")
     public DvText getForm()
    {
    return form;
    }

    public void setForm(DvText form)
    {
    this.form = form;
    }
     
                                         private DvCodedText role;

            @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0006|at0035|at0007|at0008|at0017|at0018|at0019|at0020|at0009"), notNull=@NotNull), notNull=@NotNull))
   @TdoNode(name="Role", path="/items[at0005:ELEMENT]/value")
     public DvCodedText getRole()
    {
    return role;
    }

    public void setRole(DvCodedText role)
    {
    this.role = role;
    }
     
                
    public Role getRoleEnum()
  {
  return role instanceof DvCodedText ? EnumTerminology.forEnum(Role.class).getEnumByCode(role.getDefiningCode().getCodeString()) : null;
  }

  public void setRoleEnum(Role role)
  {
  this.role = role == null ? null : DataValueUtils.getLocalCodedText(role.getTerm().getCode(), role.getTerm().getText());
  }
                                private MedicationStrengthCluster medicationStrength;

            @TdoNode(name="Medication strength", path="/items[openEHR-EHR-CLUSTER.medication_amount.v1]")
     public MedicationStrengthCluster getMedicationStrength()
    {
    return medicationStrength;
    }

    public void setMedicationStrength(MedicationStrengthCluster medicationStrength)
    {
    this.medicationStrength = medicationStrength;
    }
     
                       @Archetype(name="Medication strength", archetypeId="openEHR-EHR-CLUSTER.medication_amount.v1")
   public static class MedicationStrengthCluster extends Cluster    {
                  @Deprecated
   @Override
   public List<Item> getItems()
   {
        TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
    return super.getItems();
       }

                        private DvQuantity strength;

            @TdoNode(name="Strength", path="/items[at0001:ELEMENT]/value")
     public DvQuantity getStrength()
    {
    return strength;
    }

    public void setStrength(DvQuantity strength)
    {
    this.strength = strength;
    }
     
                                         private DvCodedText doseUnit;

            @TdoNode(name="Dose unit", path="/items[at0002:ELEMENT]/value")
     public DvCodedText getDoseUnit()
    {
    return doseUnit;
    }

    public void setDoseUnit(DvCodedText doseUnit)
    {
    this.doseUnit = doseUnit;
    }
     
                                         private DvText description;

            @TdoNode(name="Description", path="/items[at0003:ELEMENT]/value")
     public DvText getDescription()
    {
    return description;
    }

    public void setDescription(DvText description)
    {
    this.description = description;
    }
     
                                         private RatioNumeratorCluster ratioNumerator;

            @TdoNode(name="Ratio numerator", path="/items[at0008:CLUSTER]")
     public RatioNumeratorCluster getRatioNumerator()
    {
    return ratioNumerator;
    }

    public void setRatioNumerator(RatioNumeratorCluster ratioNumerator)
    {
    this.ratioNumerator = ratioNumerator;
    }
     
                       public static class RatioNumeratorCluster extends Cluster    {
                  @Deprecated
   @Override
   public List<Item> getItems()
   {
        TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
    return super.getItems();
       }

                        private DvQuantity amount;

            @TdoNode(name="Amount", path="/items[at0001:ELEMENT]/value")
     public DvQuantity getAmount()
    {
    return amount;
    }

    public void setAmount(DvQuantity amount)
    {
    this.amount = amount;
    }
     
                                         private DvCodedText doseUnit;

            @TdoNode(name="Dose unit", path="/items[at0002:ELEMENT]/value")
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

            @TdoNode(name="Ratio denominator", path="/items[at0007:CLUSTER]")
     public RatioDenominatorCluster getRatioDenominator()
    {
    return ratioDenominator;
    }

    public void setRatioDenominator(RatioDenominatorCluster ratioDenominator)
    {
    this.ratioDenominator = ratioDenominator;
    }
     
                       public static class RatioDenominatorCluster extends Cluster    {
                  @Deprecated
   @Override
   public List<Item> getItems()
   {
        TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
    return super.getItems();
       }

                        private DvQuantity amount;

            @TdoNode(name="Amount", path="/items[at0001:ELEMENT]/value")
     public DvQuantity getAmount()
    {
    return amount;
    }

    public void setAmount(DvQuantity amount)
    {
    this.amount = amount;
    }
     
                                         private DvCodedText doseUnit;

            @TdoNode(name="Dose unit", path="/items[at0002:ELEMENT]/value")
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
                 private List<IngredientCluster> ingredient;

            @TdoNode(name="Ingredient", path="/items[at0001:CLUSTER]")
     public List<IngredientCluster> getIngredient()
    {
    if (ingredient == null)
    {
      ingredient = new ArrayList<>();
    }

    return ingredient;
    }

    public void setIngredient(List<IngredientCluster> ingredient)
    {
    this.ingredient = ingredient;
    }

                       public static class IngredientCluster extends Cluster    {
                  @Deprecated
   @Override
   public List<Item> getItems()
   {
        TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
    return super.getItems();
       }

                        private DvText name;

            @NotNull
   @TdoNode(name="Name", path="/items[at0002:ELEMENT]/value")
     public DvText getName()
    {
    return name;
    }

    public void setName(DvText name)
    {
    this.name = name;
    }
     
                                         private DvText form;

            @TdoNode(name="Form", path="/items[at0010:ELEMENT]/value")
     public DvText getForm()
    {
    return form;
    }

    public void setForm(DvText form)
    {
    this.form = form;
    }
     
                                         private DvCodedText role;

            @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0006|at0035|at0007|at0008|at0017|at0018|at0019|at0020|at0009"), notNull=@NotNull), notNull=@NotNull))
   @TdoNode(name="Role", path="/items[at0005:ELEMENT]/value")
     public DvCodedText getRole()
    {
    return role;
    }

    public void setRole(DvCodedText role)
    {
    this.role = role;
    }
     
                
    public Role getRoleEnum()
  {
  return role instanceof DvCodedText ? EnumTerminology.forEnum(Role.class).getEnumByCode(role.getDefiningCode().getCodeString()) : null;
  }

  public void setRoleEnum(Role role)
  {
  this.role = role == null ? null : DataValueUtils.getLocalCodedText(role.getTerm().getCode(), role.getTerm().getText());
  }
                                private IngredientStrengthCluster ingredientStrength;

            @TdoNode(name="Ingredient strength", path="/items[openEHR-EHR-CLUSTER.medication_amount.v1 and name/value='Ingredient strength']")
     public IngredientStrengthCluster getIngredientStrength()
    {
    return ingredientStrength;
    }

    public void setIngredientStrength(IngredientStrengthCluster ingredientStrength)
    {
    this.ingredientStrength = ingredientStrength;
    }
     
                       @Archetype(name="Ingredient strength", archetypeId="openEHR-EHR-CLUSTER.medication_amount.v1")
   public static class IngredientStrengthCluster extends Cluster    {
                  @Deprecated
   @Override
   public List<Item> getItems()
   {
        TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
    return super.getItems();
       }

                        private DvQuantity strength;

            @TdoNode(name="Strength", path="/items[at0001:ELEMENT]/value")
     public DvQuantity getStrength()
    {
    return strength;
    }

    public void setStrength(DvQuantity strength)
    {
    this.strength = strength;
    }
     
                                         private DvCodedText doseUnit;

            @TdoNode(name="Dose unit", path="/items[at0002:ELEMENT]/value")
     public DvCodedText getDoseUnit()
    {
    return doseUnit;
    }

    public void setDoseUnit(DvCodedText doseUnit)
    {
    this.doseUnit = doseUnit;
    }
     
                                         private DvText description;

            @TdoNode(name="Description", path="/items[at0003:ELEMENT]/value")
     public DvText getDescription()
    {
    return description;
    }

    public void setDescription(DvText description)
    {
    this.description = description;
    }
     
                                         private RatioNumeratorCluster ratioNumerator;

            @TdoNode(name="Ratio numerator", path="/items[at0008:CLUSTER]")
     public RatioNumeratorCluster getRatioNumerator()
    {
    return ratioNumerator;
    }

    public void setRatioNumerator(RatioNumeratorCluster ratioNumerator)
    {
    this.ratioNumerator = ratioNumerator;
    }
     
                       public static class RatioNumeratorCluster extends Cluster    {
                  @Deprecated
   @Override
   public List<Item> getItems()
   {
        TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
    return super.getItems();
       }

                        private DvQuantity amount;

            @TdoNode(name="Amount", path="/items[at0001:ELEMENT]/value")
     public DvQuantity getAmount()
    {
    return amount;
    }

    public void setAmount(DvQuantity amount)
    {
    this.amount = amount;
    }
     
                                         private DvCodedText doseUnit;

            @TdoNode(name="Dose unit", path="/items[at0002:ELEMENT]/value")
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

            @TdoNode(name="Ratio denominator", path="/items[at0007:CLUSTER]")
     public RatioDenominatorCluster getRatioDenominator()
    {
    return ratioDenominator;
    }

    public void setRatioDenominator(RatioDenominatorCluster ratioDenominator)
    {
    this.ratioDenominator = ratioDenominator;
    }
     
                       public static class RatioDenominatorCluster extends Cluster    {
                  @Deprecated
   @Override
   public List<Item> getItems()
   {
        TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
    return super.getItems();
       }

                        private DvQuantity amount;

            @TdoNode(name="Amount", path="/items[at0001:ELEMENT]/value")
     public DvQuantity getAmount()
    {
    return amount;
    }

    public void setAmount(DvQuantity amount)
    {
    this.amount = amount;
    }
     
                                         private DvCodedText doseUnit;

            @TdoNode(name="Dose unit", path="/items[at0002:ELEMENT]/value")
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
                 private IngredientQuantityCluster ingredientQuantity;

            @TdoNode(name="Ingredient quantity", path="/items[openEHR-EHR-CLUSTER.medication_amount.v1 and name/value='Ingredient quantity']")
     public IngredientQuantityCluster getIngredientQuantity()
    {
    return ingredientQuantity;
    }

    public void setIngredientQuantity(IngredientQuantityCluster ingredientQuantity)
    {
    this.ingredientQuantity = ingredientQuantity;
    }
     
                       @Archetype(name="Ingredient quantity", archetypeId="openEHR-EHR-CLUSTER.medication_amount.v1")
   public static class IngredientQuantityCluster extends Cluster    {
                  @Deprecated
   @Override
   public List<Item> getItems()
   {
        TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
    return super.getItems();
       }

                        private DvQuantity quantity;

            @TdoNode(name="Quantity", path="/items[at0001:ELEMENT]/value")
     public DvQuantity getQuantity()
    {
    return quantity;
    }

    public void setQuantity(DvQuantity quantity)
    {
    this.quantity = quantity;
    }
     
                                         private DvCodedText doseUnit;

            @TdoNode(name="Dose unit", path="/items[at0002:ELEMENT]/value")
     public DvCodedText getDoseUnit()
    {
    return doseUnit;
    }

    public void setDoseUnit(DvCodedText doseUnit)
    {
    this.doseUnit = doseUnit;
    }
     
                                         private DvText description;

            @TdoNode(name="Description", path="/items[at0003:ELEMENT]/value")
     public DvText getDescription()
    {
    return description;
    }

    public void setDescription(DvText description)
    {
    this.description = description;
    }
     
                                         private RatioNumeratorCluster ratioNumerator;

            @TdoNode(name="Ratio numerator", path="/items[at0008:CLUSTER]")
     public RatioNumeratorCluster getRatioNumerator()
    {
    return ratioNumerator;
    }

    public void setRatioNumerator(RatioNumeratorCluster ratioNumerator)
    {
    this.ratioNumerator = ratioNumerator;
    }
     
                       public static class RatioNumeratorCluster extends Cluster    {
                  @Deprecated
   @Override
   public List<Item> getItems()
   {
        TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
    return super.getItems();
       }

                        private DvQuantity amount;

            @TdoNode(name="Amount", path="/items[at0001:ELEMENT]/value")
     public DvQuantity getAmount()
    {
    return amount;
    }

    public void setAmount(DvQuantity amount)
    {
    this.amount = amount;
    }
     
                                         private DvCodedText doseUnit;

            @TdoNode(name="Dose unit", path="/items[at0002:ELEMENT]/value")
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

            @TdoNode(name="Ratio denominator", path="/items[at0007:CLUSTER]")
     public RatioDenominatorCluster getRatioDenominator()
    {
    return ratioDenominator;
    }

    public void setRatioDenominator(RatioDenominatorCluster ratioDenominator)
    {
    this.ratioDenominator = ratioDenominator;
    }
     
                       public static class RatioDenominatorCluster extends Cluster    {
                  @Deprecated
   @Override
   public List<Item> getItems()
   {
        TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
    return super.getItems();
       }

                        private DvQuantity amount;

            @TdoNode(name="Amount", path="/items[at0001:ELEMENT]/value")
     public DvQuantity getAmount()
    {
    return amount;
    }

    public void setAmount(DvQuantity amount)
    {
    this.amount = amount;
    }
     
                                         private DvCodedText doseUnit;

            @TdoNode(name="Dose unit", path="/items[at0002:ELEMENT]/value")
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
     }
         }