// Generated using Marand-EHR TDO Generator vUnknown
// Source: OPENEP - Pharmacy Review Report.opt
// Time: 2016-01-22T12:48:06.090+01:00
package com.marand.openehr.medications.tdo;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

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
import org.openehr.jaxb.rm.Activity;
import org.openehr.jaxb.rm.Cluster;
import org.openehr.jaxb.rm.Composition;
import org.openehr.jaxb.rm.ContentItem;
import org.openehr.jaxb.rm.DvBoolean;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvDateTime;
import org.openehr.jaxb.rm.DvDuration;
import org.openehr.jaxb.rm.DvEhrUri;
import org.openehr.jaxb.rm.DvMultimedia;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.DvUri;
import org.openehr.jaxb.rm.Evaluation;
import org.openehr.jaxb.rm.EventContext;
import org.openehr.jaxb.rm.Instruction;
import org.openehr.jaxb.rm.Item;
import org.openehr.jaxb.rm.ItemStructure;
import org.openehr.jaxb.rm.Section;

        
   @Template(name="Pharmacy review report", templateId="OPENEP - Pharmacy Review Report.v0")
   @Archetype(name="Pharmacy review report", archetypeId="openEHR-EHR-COMPOSITION.report.v1")
 public class PharmacyReviewReportComposition extends Composition  {
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
     
                       public static class CompositionEventContext extends EventContext    {
                  @Deprecated
   @Override
   public ItemStructure getOtherContext()
   {
        TdoAccess.check("Property 'OtherContext' is deprecated in: " + getClass().getName());
    return super.getOtherContext();
       }

        @Deprecated
    @Override
    public void setOtherContext(ItemStructure value)
    {
          TdoAccess.check("Property 'OtherContext' is deprecated in: " + getClass().getName());
     super.setOtherContext(value);
         }
                        private DvText status;

            @TdoNode(name="Status", path="/other_context[at0001:ITEM_TREE]/items[at0005:ELEMENT]/value")
     public DvText getStatus()
    {
    return status;
    }

    public void setStatus(DvText status)
    {
    this.status = status;
    }
     
                                         private List<ContextDetailCluster> contextDetail;

            @TdoNode(name="Context detail", path="/other_context[at0001:ITEM_TREE]/items[openEHR-EHR-CLUSTER.composition_context_detail.v1]")
     public List<ContextDetailCluster> getContextDetail()
    {
    if (contextDetail == null)
    {
      contextDetail = new ArrayList<>();
    }

    return contextDetail;
    }

    public void setContextDetail(List<ContextDetailCluster> contextDetail)
    {
    this.contextDetail = contextDetail;
    }

                       @Archetype(name="Context detail", archetypeId="openEHR-EHR-CLUSTER.composition_context_detail.v1")
   public static class ContextDetailCluster extends Cluster    {
                  @Deprecated
   @Override
   public List<Item> getItems()
   {
        TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
    return super.getItems();
       }

                        private DvText periodOfCareIdentifier;

            @TdoNode(name="Period of care identifier", path="/items[at0001:ELEMENT]/value")
     public DvText getPeriodOfCareIdentifier()
    {
    return periodOfCareIdentifier;
    }

    public void setPeriodOfCareIdentifier(DvText periodOfCareIdentifier)
    {
    this.periodOfCareIdentifier = periodOfCareIdentifier;
    }
     
                                         private DvText departmentalPeriodOfCareIdentifier;

            @TdoNode(name="Departmental period of care identifier", path="/items[at0002:ELEMENT]/value")
     public DvText getDepartmentalPeriodOfCareIdentifier()
    {
    return departmentalPeriodOfCareIdentifier;
    }

    public void setDepartmentalPeriodOfCareIdentifier(DvText departmentalPeriodOfCareIdentifier)
    {
    this.departmentalPeriodOfCareIdentifier = departmentalPeriodOfCareIdentifier;
    }
     
                                         private DvText portletId;

            @TdoNode(name="Portlet Id", path="/items[at0003:ELEMENT]/value")
     public DvText getPortletId()
    {
    return portletId;
    }

    public void setPortletId(DvText portletId)
    {
    this.portletId = portletId;
    }
     
                                         private DvCodedText medicationOrderType;

            @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0005|at0006|at0007"), notNull=@NotNull), notNull=@NotNull))
   @TdoNode(name="Medication order type", path="/items[at0004:ELEMENT]/value")
     public DvCodedText getMedicationOrderType()
    {
    return medicationOrderType;
    }

    public void setMedicationOrderType(DvCodedText medicationOrderType)
    {
    this.medicationOrderType = medicationOrderType;
    }
     
                
    public MedicationOrderType getMedicationOrderTypeEnum()
  {
  return medicationOrderType instanceof DvCodedText ? EnumTerminology.forEnum(MedicationOrderType.class).getEnumByCode(
      medicationOrderType.getDefiningCode().getCodeString()) : null;
  }

  public void setMedicationOrderTypeEnum(MedicationOrderType medicationOrderType)
  {
  this.medicationOrderType = medicationOrderType == null ? null : DataValueUtils.getLocalCodedText(medicationOrderType.getTerm().getCode(), medicationOrderType.getTerm().getText());
  }
                          public enum MedicationOrderType implements EnumTerminology.TermEnum<MedicationOrderType>
   {
         ORAL("at0005", "Oral", "*"), 
         INTRAVENOUS("at0006", "Intravenous", "*"), 
         MIXTURE("at0007", "Mixture", "*")
       ;
    MedicationOrderType(String code, String text, String description)
   {
   terminologyTerm = new EnumTerminology.Term<>(this, code, text, description);
   }

   private final EnumTerminology.Term<MedicationOrderType> terminologyTerm;

   public EnumTerminology.Term<MedicationOrderType> getTerm()
   {
   return terminologyTerm;
   }
   public static final EnumTerminology<MedicationOrderType> TERMINOLOGY = EnumTerminology.newInstance(MedicationOrderType.class);
     }
                 private List<DvText> tags;

            @TdoNode(name="Tags", path="/items[at0008:ELEMENT]/value")
     public List<DvText> getTags()
    {
    if (tags == null)
    {
      tags = new ArrayList<>();
    }

    return tags;
    }

    public void setTags(List<DvText> tags)
    {
    this.tags = tags;
    }

                             private DvCodedText documentStatus;

            @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0011|at0012|at0013"), notNull=@NotNull), notNull=@NotNull))
   @TdoNode(name="Document status", path="/items[at0010:ELEMENT]/value")
     public DvCodedText getDocumentStatus()
    {
    return documentStatus;
    }

    public void setDocumentStatus(DvCodedText documentStatus)
    {
    this.documentStatus = documentStatus;
    }
     
                
    public DocumentStatus getDocumentStatusEnum()
  {
  return documentStatus instanceof DvCodedText ? EnumTerminology.forEnum(DocumentStatus.class).getEnumByCode(documentStatus.getDefiningCode().getCodeString()) : null;
  }

  public void setDocumentStatusEnum(DocumentStatus documentStatus)
  {
  this.documentStatus = documentStatus == null ? null : DataValueUtils.getLocalCodedText(documentStatus.getTerm().getCode(), documentStatus.getTerm().getText());
  }
                          public enum DocumentStatus implements EnumTerminology.TermEnum<DocumentStatus>
   {
         AUTHORISED("at0011", "Authorised", "*"), 
         CONFIRMED("at0012", "Confirmed", "*"), 
         OPEN("at0013", "Open", "*")
       ;
    DocumentStatus(String code, String text, String description)
   {
   terminologyTerm = new EnumTerminology.Term<>(this, code, text, description);
   }

   private final EnumTerminology.Term<DocumentStatus> terminologyTerm;

   public EnumTerminology.Term<DocumentStatus> getTerm()
   {
   return terminologyTerm;
   }
   public static final EnumTerminology<DocumentStatus> TERMINOLOGY = EnumTerminology.newInstance(DocumentStatus.class);
     }
                 private List<DvMultimedia> attachment;

            @TdoNode(name="Attachment", path="/items[at0009:ELEMENT]/value")
     public List<DvMultimedia> getAttachment()
    {
    if (attachment == null)
    {
      attachment = new ArrayList<>();
    }

    return attachment;
    }

    public void setAttachment(List<DvMultimedia> attachment)
    {
    this.attachment = attachment;
    }

                 }
     }
                 @Deprecated
   @Override
   public List<ContentItem> getContent()
   {
        TdoAccess.check("Property 'Content' is deprecated in: " + getClass().getName());
    return super.getContent();
       }

                        private PharmacistMedicationReviewEvaluation pharmacistMedicationReview;

            @TdoNode(name="Pharmacist medication review", path="/content[openEHR-EHR-EVALUATION.pharmacy_meds_review.v1]")
     public PharmacistMedicationReviewEvaluation getPharmacistMedicationReview()
    {
    return pharmacistMedicationReview;
    }

    public void setPharmacistMedicationReview(PharmacistMedicationReviewEvaluation pharmacistMedicationReview)
    {
    this.pharmacistMedicationReview = pharmacistMedicationReview;
    }
     
                       @Archetype(name="Pharmacist medication review", archetypeId="openEHR-EHR-EVALUATION.pharmacy_meds_review.v1")
   public static class PharmacistMedicationReviewEvaluation extends Evaluation    {
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
                        private DvCodedText medicationListCategory;

            @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0053|at0054|at0055|at0056|at0059"), notNull=@NotNull), notNull=@NotNull))
   @TdoNode(name="Medication list category", path="/data[at0001:ITEM_TREE]/items[at0052:ELEMENT]/value")
     public DvCodedText getMedicationListCategory()
    {
    return medicationListCategory;
    }

    public void setMedicationListCategory(DvCodedText medicationListCategory)
    {
    this.medicationListCategory = medicationListCategory;
    }
     
                
    public MedicationListCategory getMedicationListCategoryEnum()
  {
  return medicationListCategory instanceof DvCodedText ? EnumTerminology.forEnum(MedicationListCategory.class).getEnumByCode(
      medicationListCategory.getDefiningCode().getCodeString()) : null;
  }

  public void setMedicationListCategoryEnum(MedicationListCategory medicationListCategory)
  {
  this.medicationListCategory = medicationListCategory == null ? null : DataValueUtils.getLocalCodedText(medicationListCategory.getTerm().getCode(), medicationListCategory.getTerm().getText());
  }
                          public enum MedicationListCategory implements EnumTerminology.TermEnum<MedicationListCategory>
   {
         ADMISSION("at0053", "Admission", "A list of medication at admission is being reviewed."), 
         INPATIENT("at0054", "Inpatient", "The list of current inpatient medicaitons is being reviewed."), 
         OUTPATIENT("at0055", "Outpatient", "The list of current outpatient medications is being reviewed.."), 
         DISCHARGE("at0056", "Discharge", "The list of discharge / transfer of care medications is being reviewed."), 
         SHORT_TERM_LEAVE("at0059", "Short-term leave", "Short-term leave medication list.")
       ;
    MedicationListCategory(String code, String text, String description)
   {
   terminologyTerm = new EnumTerminology.Term<>(this, code, text, description);
   }

   private final EnumTerminology.Term<MedicationListCategory> terminologyTerm;

   public EnumTerminology.Term<MedicationListCategory> getTerm()
   {
   return terminologyTerm;
   }
   public static final EnumTerminology<MedicationListCategory> TERMINOLOGY = EnumTerminology.newInstance(MedicationListCategory.class);
     }
                 private List<MedicationItemAssessmentCluster> medicationItemAssessment;

            @TdoNode(name="Medication item assessment", path="/data[at0001:ITEM_TREE]/items[at0002:CLUSTER]")
     public List<MedicationItemAssessmentCluster> getMedicationItemAssessment()
    {
    if (medicationItemAssessment == null)
    {
      medicationItemAssessment = new ArrayList<>();
    }

    return medicationItemAssessment;
    }

    public void setMedicationItemAssessment(List<MedicationItemAssessmentCluster> medicationItemAssessment)
    {
    this.medicationItemAssessment = medicationItemAssessment;
    }

                       public static class MedicationItemAssessmentCluster extends Cluster    {
                  @Deprecated
   @Override
   public List<Item> getItems()
   {
        TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
    return super.getItems();
       }

                        private List<DvEhrUri> relatedMedicationItem;

            @TdoNode(name="Related medication item", path="/items[at0004:ELEMENT]/value")
     public List<DvEhrUri> getRelatedMedicationItem()
    {
    if (relatedMedicationItem == null)
    {
      relatedMedicationItem = new ArrayList<>();
    }

    return relatedMedicationItem;
    }

    public void setRelatedMedicationItem(List<DvEhrUri> relatedMedicationItem)
    {
    this.relatedMedicationItem = relatedMedicationItem;
    }

                             private DvBoolean noProblemIdentified;

            @TdoNode(name="No problem identified", path="/items[at0003:ELEMENT]/value")
     public DvBoolean getNoProblemIdentified()
    {
    return noProblemIdentified;
    }

    public void setNoProblemIdentified(DvBoolean noProblemIdentified)
    {
    this.noProblemIdentified = noProblemIdentified;
    }
     
                                         private DvText overallRecommendation;

            @TdoNode(name="Overall recommendation", path="/items[at0064:ELEMENT]/value")
     public DvText getOverallRecommendation()
    {
    return overallRecommendation;
    }

    public void setOverallRecommendation(DvText overallRecommendation)
    {
    this.overallRecommendation = overallRecommendation;
    }
     
                                         private DrugRelatedProblemCluster drugRelatedProblem;

            @TdoNode(name="Drug-related problem", path="/items[at0007:CLUSTER]")
     public DrugRelatedProblemCluster getDrugRelatedProblem()
    {
    return drugRelatedProblem;
    }

    public void setDrugRelatedProblem(DrugRelatedProblemCluster drugRelatedProblem)
    {
    this.drugRelatedProblem = drugRelatedProblem;
    }
     
                       public static class DrugRelatedProblemCluster extends Cluster    {
                  @Deprecated
   @Override
   public List<Item> getItems()
   {
        TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
    return super.getItems();
       }

                        private List<DvCodedText> category;

            @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0012|at0013|at0014|at0015|at0016|at0017|at0018|at0019|at0020|at0021|at0022"), notNull=@NotNull), notNull=@NotNull))
   @TdoNode(name="Category", path="/items[at0008:ELEMENT]/value")
     public List<DvCodedText> getCategory()
    {
    if (category == null)
    {
      category = new ArrayList<>();
    }

    return category;
    }

    public void setCategory(List<DvCodedText> category)
    {
    this.category = category;
    }

                       public enum Category implements EnumTerminology.TermEnum<Category>
   {
         NO_INDICATION_FOR_DRUG_THERAPY("at0012", "No indication for drug therapy", "There does not appear to be an indication for the medication."), 
         NO_DRUG_ORDER_FOR_MEDICAL_CONDITION("at0013", "No drug order for medical condition", "????"), 
         INAPPROPRIATE_DRUG_SELECTION("at0014", "Inappropriate drug selection", "An inappropriate drug appears to have been ordered."), 
         INAPPROPRIATE_DOSE_AMOUNT("at0015", "Inappropriate dose amount", "The dose amount is inappropriate for the medication item."), 
         INAPPROPRIATE_DOSE_FREQUENCY("at0016", "Inappropriate dose frequency", "The dose frequency of the medicaiton item is inappropriate."), 
         INAPPROPRIATE_ROUTE("at0017", "Inappropriate route", "The route of the medication administration appears to be inappropriate."), 
         INAPPROPRIATE_RATE("at0018", "Inappropriate rate", "The rate of medication administration apperars to be inappropriate."), 
         PRESCRIBED_DRUG_NOT_ADMINISTERED("at0019", "Prescribed drug not administered", "The medication appears to have been ordered but never administered."), 
         EXPERIENCING_ADRS_OR_SES("at0020", "Experiencing ADRs or SEs", "The patient is experiencing adverse drug reactions or side-effects."), 
         EXPERIENCING_DRUG_INTERACTIONS("at0021", "Experiencing drug interactions", "The patient is experiencing the sequelae of drug interactions."), 
         DEVIATION_FROM_POLICY_PROTOCOL("at0022", "Deviation from policy / protocol", "The medication order deviates from prescribing policy or protocol.")
       ;
    Category(String code, String text, String description)
   {
   terminologyTerm = new EnumTerminology.Term<>(this, code, text, description);
   }

   private final EnumTerminology.Term<Category> terminologyTerm;

   public EnumTerminology.Term<Category> getTerm()
   {
   return terminologyTerm;
   }
   public static final EnumTerminology<Category> TERMINOLOGY = EnumTerminology.newInstance(Category.class);
     }
                 private DvCodedText outcome;

            @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0023|at0024|at0025"), notNull=@NotNull), notNull=@NotNull))
   @TdoNode(name="Outcome", path="/items[at0009:ELEMENT]/value")
     public DvCodedText getOutcome()
    {
    return outcome;
    }

    public void setOutcome(DvCodedText outcome)
    {
    this.outcome = outcome;
    }
     
                
    public Outcome getOutcomeEnum()
  {
  return outcome instanceof DvCodedText ? EnumTerminology.forEnum(Outcome.class).getEnumByCode(outcome.getDefiningCode().getCodeString()) : null;
  }

  public void setOutcomeEnum(Outcome outcome)
  {
  this.outcome = outcome == null ? null : DataValueUtils.getLocalCodedText(outcome.getTerm().getCode(), outcome.getTerm().getText());
  }
                                private DvCodedText impact;

            @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0026|at0027|at0028"), notNull=@NotNull), notNull=@NotNull))
   @TdoNode(name="Impact", path="/items[at0011:ELEMENT]/value")
     public DvCodedText getImpact()
    {
    return impact;
    }

    public void setImpact(DvCodedText impact)
    {
    this.impact = impact;
    }
     
                
    public Impact getImpactEnum()
  {
  return impact instanceof DvCodedText ? EnumTerminology.forEnum(Impact.class).getEnumByCode(impact.getDefiningCode().getCodeString()) : null;
  }

  public void setImpactEnum(Impact impact)
  {
  this.impact = impact == null ? null : DataValueUtils.getLocalCodedText(impact.getTerm().getCode(), impact.getTerm().getText());
  }
                                private DvText recommendation;

            @TdoNode(name="Recommendation", path="/items[at0010:ELEMENT]/value")
     public DvText getRecommendation()
    {
    return recommendation;
    }

    public void setRecommendation(DvText recommendation)
    {
    this.recommendation = recommendation;
    }
     
                             }
                 private List<PharmacokineticIssueCluster> pharmacokineticIssue;

            @TdoNode(name="Pharmacokinetic issue", path="/items[at0029:CLUSTER]")
     public List<PharmacokineticIssueCluster> getPharmacokineticIssue()
    {
    if (pharmacokineticIssue == null)
    {
      pharmacokineticIssue = new ArrayList<>();
    }

    return pharmacokineticIssue;
    }

    public void setPharmacokineticIssue(List<PharmacokineticIssueCluster> pharmacokineticIssue)
    {
    this.pharmacokineticIssue = pharmacokineticIssue;
    }

                       public static class PharmacokineticIssueCluster extends Cluster    {
                  @Deprecated
   @Override
   public List<Item> getItems()
   {
        TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
    return super.getItems();
       }

                        private List<DvCodedText> category;

            @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0060|at0061|at0062|at0063"), notNull=@NotNull), notNull=@NotNull))
   @TdoNode(name="Category", path="/items[at0030:ELEMENT]/value")
     public List<DvCodedText> getCategory()
    {
    if (category == null)
    {
      category = new ArrayList<>();
    }

    return category;
    }

    public void setCategory(List<DvCodedText> category)
    {
    this.category = category;
    }

                       public enum Category implements EnumTerminology.TermEnum<Category>
   {
         LOADING_DOSE("at0060", "Loading dose", "An issue was identified with the loading dose."), 
         MAINTENANCE_DOSE("at0061", "Maintenance dose", "An issue was identified with the maintenance dose."), 
         INAPPROPRIATE_DOSAGE("at0062", "Inappropriate dosage", "The dosage was inapproopriate."), 
         EXPERIENCING_ADRS_OR_SES("at0063", "Experiencing ADRs or SEs", "The patient is experiencing adverse drug reactiions or side effects.")
       ;
    Category(String code, String text, String description)
   {
   terminologyTerm = new EnumTerminology.Term<>(this, code, text, description);
   }

   private final EnumTerminology.Term<Category> terminologyTerm;

   public EnumTerminology.Term<Category> getTerm()
   {
   return terminologyTerm;
   }
   public static final EnumTerminology<Category> TERMINOLOGY = EnumTerminology.newInstance(Category.class);
     }
                 private DvCodedText outcome;

            @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0023|at0024|at0025"), notNull=@NotNull), notNull=@NotNull))
   @TdoNode(name="Outcome", path="/items[at0009:ELEMENT]/value")
     public DvCodedText getOutcome()
    {
    return outcome;
    }

    public void setOutcome(DvCodedText outcome)
    {
    this.outcome = outcome;
    }
     
                
    public Outcome getOutcomeEnum()
  {
  return outcome instanceof DvCodedText ? EnumTerminology.forEnum(Outcome.class).getEnumByCode(outcome.getDefiningCode().getCodeString()) : null;
  }

  public void setOutcomeEnum(Outcome outcome)
  {
  this.outcome = outcome == null ? null : DataValueUtils.getLocalCodedText(outcome.getTerm().getCode(), outcome.getTerm().getText());
  }
                                private DvCodedText impact;

            @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0026|at0027|at0028"), notNull=@NotNull), notNull=@NotNull))
   @TdoNode(name="Impact", path="/items[at0011:ELEMENT]/value")
     public DvCodedText getImpact()
    {
    return impact;
    }

    public void setImpact(DvCodedText impact)
    {
    this.impact = impact;
    }
     
                
    public Impact getImpactEnum()
  {
  return impact instanceof DvCodedText ? EnumTerminology.forEnum(Impact.class).getEnumByCode(impact.getDefiningCode().getCodeString()) : null;
  }

  public void setImpactEnum(Impact impact)
  {
  this.impact = impact == null ? null : DataValueUtils.getLocalCodedText(impact.getTerm().getCode(), impact.getTerm().getText());
  }
                                private DvText recommendation;

            @TdoNode(name="Recommendation", path="/items[at0010:ELEMENT]/value")
     public DvText getRecommendation()
    {
    return recommendation;
    }

    public void setRecommendation(DvText recommendation)
    {
    this.recommendation = recommendation;
    }
     
                             }
                 private List<PatientRelatedProblemCluster> patientRelatedProblem;

            @TdoNode(name="Patient related problem", path="/items[at0038:CLUSTER]")
     public List<PatientRelatedProblemCluster> getPatientRelatedProblem()
    {
    if (patientRelatedProblem == null)
    {
      patientRelatedProblem = new ArrayList<>();
    }

    return patientRelatedProblem;
    }

    public void setPatientRelatedProblem(List<PatientRelatedProblemCluster> patientRelatedProblem)
    {
    this.patientRelatedProblem = patientRelatedProblem;
    }

                       public static class PatientRelatedProblemCluster extends Cluster    {
                  @Deprecated
   @Override
   public List<Item> getItems()
   {
        TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
    return super.getItems();
       }

                        private List<DvCodedText> category;

            @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0040|at0041|at0042|at0043"), notNull=@NotNull), notNull=@NotNull))
   @TdoNode(name="Category", path="/items[at0039:ELEMENT]/value")
     public List<DvCodedText> getCategory()
    {
    if (category == null)
    {
      category = new ArrayList<>();
    }

    return category;
    }

    public void setCategory(List<DvCodedText> category)
    {
    this.category = category;
    }

                       public enum Category implements EnumTerminology.TermEnum<Category>
   {
         ADHERENCE_ISSUE("at0040", "Adherence issue", "The patient is thought not be adheriing to the medication regime."), 
         PATIENT_COUNSELLING_REQUIRED("at0041", "Patient counselling required", "The patient appears to require counselling on the use of the medication."), 
         EDUCATION_MATERIAL_REQUIRED("at0042", "Education material required", "The patient appears to require educational material related to medication use."), 
         EXPERIENCING_ADRS_OR_SES("at0043", "Experiencing ADRs or SEs", "The patient appears to be suffering adverse drug reactions or side effects.")
       ;
    Category(String code, String text, String description)
   {
   terminologyTerm = new EnumTerminology.Term<>(this, code, text, description);
   }

   private final EnumTerminology.Term<Category> terminologyTerm;

   public EnumTerminology.Term<Category> getTerm()
   {
   return terminologyTerm;
   }
   public static final EnumTerminology<Category> TERMINOLOGY = EnumTerminology.newInstance(Category.class);
     }
                 private DvCodedText outcome;

            @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0023|at0024|at0025"), notNull=@NotNull), notNull=@NotNull))
   @TdoNode(name="Outcome", path="/items[at0009:ELEMENT]/value")
     public DvCodedText getOutcome()
    {
    return outcome;
    }

    public void setOutcome(DvCodedText outcome)
    {
    this.outcome = outcome;
    }
     
                
    public Outcome getOutcomeEnum()
  {
  return outcome instanceof DvCodedText ? EnumTerminology.forEnum(Outcome.class).getEnumByCode(outcome.getDefiningCode().getCodeString()) : null;
  }

  public void setOutcomeEnum(Outcome outcome)
  {
  this.outcome = outcome == null ? null : DataValueUtils.getLocalCodedText(outcome.getTerm().getCode(), outcome.getTerm().getText());
  }
                                private DvCodedText impact;

            @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0026|at0027|at0028"), notNull=@NotNull), notNull=@NotNull))
   @TdoNode(name="Impact", path="/items[at0011:ELEMENT]/value")
     public DvCodedText getImpact()
    {
    return impact;
    }

    public void setImpact(DvCodedText impact)
    {
    this.impact = impact;
    }
     
                
    public Impact getImpactEnum()
  {
  return impact instanceof DvCodedText ? EnumTerminology.forEnum(Impact.class).getEnumByCode(impact.getDefiningCode().getCodeString()) : null;
  }

  public void setImpactEnum(Impact impact)
  {
  this.impact = impact == null ? null : DataValueUtils.getLocalCodedText(impact.getTerm().getCode(), impact.getTerm().getText());
  }
                                private DvText recommendation;

            @TdoNode(name="Recommendation", path="/items[at0010:ELEMENT]/value")
     public DvText getRecommendation()
    {
    return recommendation;
    }

    public void setRecommendation(DvText recommendation)
    {
    this.recommendation = recommendation;
    }
     
                             }
           public enum Outcome implements EnumTerminology.TermEnum<Outcome>
   {
         COST_SAVINGS_ONLY("at0023", "Cost savings only", "The recommendation is likely to lead to cost-savings only."), 
         POTENTIAL_ADR_TOXICITY_PREVENTED("at0024", "Potential ADR / toxicity prevented", "The recommendation is expected to prevent an adverse drug reaction or patient toxicity."), 
         ENHANCED_THERAPEUTIC_EFFECT("at0025", "Enhanced therapeutic effect", "The recommendation is likely to result in an enhanced therapeutic effect.")
       ;
    Outcome(String code, String text, String description)
   {
   terminologyTerm = new EnumTerminology.Term<>(this, code, text, description);
   }

   private final EnumTerminology.Term<Outcome> terminologyTerm;

   public EnumTerminology.Term<Outcome> getTerm()
   {
   return terminologyTerm;
   }
   public static final EnumTerminology<Outcome> TERMINOLOGY = EnumTerminology.newInstance(Outcome.class);
     }
           public enum Impact implements EnumTerminology.TermEnum<Impact>
   {
         POTENTIALLY_SEVERE_HIGH_CLINICAL_SIGNIFICANCE("at0026", "Potentially severe / high clinical significance", "The issue has a highly significant impact on the patient's health."), 
         IMPORTANT_MODERATE_CLINICAL_SIGNIFICANCE("at0027", "Important / moderate clinical significance", "The issue has a moderately significant impact on the patient's health."), 
         MINOR_LOW_CLINICAL_SIGNIFICANCE("at0028", "Minor / low clinical significance", "The issue has a low impact on the patient's health.")
       ;
    Impact(String code, String text, String description)
   {
   terminologyTerm = new EnumTerminology.Term<>(this, code, text, description);
   }

   private final EnumTerminology.Term<Impact> terminologyTerm;

   public EnumTerminology.Term<Impact> getTerm()
   {
   return terminologyTerm;
   }
   public static final EnumTerminology<Impact> TERMINOLOGY = EnumTerminology.newInstance(Impact.class);
     }
     }
     }
                 private MiscellaneousSection miscellaneous;

            @TdoNode(name="Miscellaneous", path="/content[openEHR-EHR-SECTION.adhoc.v1]")
     public MiscellaneousSection getMiscellaneous()
    {
    return miscellaneous;
    }

    public void setMiscellaneous(MiscellaneousSection miscellaneous)
    {
    this.miscellaneous = miscellaneous;
    }
     
                       @Archetype(name="Miscellaneous", archetypeId="openEHR-EHR-SECTION.adhoc.v1")
   public static class MiscellaneousSection extends Section    {
                  @Deprecated
   @Override
   public List<ContentItem> getItems()
   {
        TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
    return super.getItems();
       }

                        private List<InpatientMedicationSupplyAction> inpatientMedicationSupply;

            @TdoNode(name="Inpatient medication supply", path="/items[openEHR-EHR-ACTION.medication_supply_uk.v1]")
     public List<InpatientMedicationSupplyAction> getInpatientMedicationSupply()
    {
    if (inpatientMedicationSupply == null)
    {
      inpatientMedicationSupply = new ArrayList<>();
    }

    return inpatientMedicationSupply;
    }

    public void setInpatientMedicationSupply(List<InpatientMedicationSupplyAction> inpatientMedicationSupply)
    {
    this.inpatientMedicationSupply = inpatientMedicationSupply;
    }

                       @Archetype(name="Inpatient medication supply", archetypeId="openEHR-EHR-ACTION.medication_supply_uk.v1")
   public static class InpatientMedicationSupplyAction extends Action    {
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

            @TdoNode(name="Link to medication order", path="/description[at0001:ITEM_TREE]/items[at0004:ELEMENT]/value")
     public DvEhrUri getLinkToMedicationOrder()
    {
    return linkToMedicationOrder;
    }

    public void setLinkToMedicationOrder(DvEhrUri linkToMedicationOrder)
    {
    this.linkToMedicationOrder = linkToMedicationOrder;
    }
     
                                         private DvCodedText supplyCategory;

            @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0009|at0010|at0011|at0012"), notNull=@NotNull), notNull=@NotNull))
   @TdoNode(name="Supply category", path="/description[at0001:ITEM_TREE]/items[at0008:ELEMENT]/value")
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
  return supplyCategory instanceof DvCodedText ? EnumTerminology.forEnum(SupplyCategory.class).getEnumByCode(supplyCategory.getDefiningCode().getCodeString()) : null;
  }

  public void setSupplyCategoryEnum(SupplyCategory supplyCategory)
  {
  this.supplyCategory = supplyCategory == null ? null : DataValueUtils.getLocalCodedText(supplyCategory.getTerm().getCode(), supplyCategory.getTerm().getText());
  }
                          public enum SupplyCategory implements EnumTerminology.TermEnum<SupplyCategory>
   {
         WARD_STOCK("at0009", "Ward stock", "The medication is supplied from ward stock."), 
         NON_STOCK("at0010", "Non-stock", "The medication is not supplied from ward stock."), 
         PATIENT_OWN_SUPPLY("at0011", "Patient own supply", "The medication is available from the patient's own supply."), 
         ONE_STOP_DISPENSING("at0012", "One stop dispensing", "The medication is supplied to allow the patient to take home.")
       ;
    SupplyCategory(String code, String text, String description)
   {
   terminologyTerm = new EnumTerminology.Term<>(this, code, text, description);
   }

   private final EnumTerminology.Term<SupplyCategory> terminologyTerm;

   public EnumTerminology.Term<SupplyCategory> getTerm()
   {
   return terminologyTerm;
   }
   public static final EnumTerminology<SupplyCategory> TERMINOLOGY = EnumTerminology.newInstance(SupplyCategory.class);
     }
                 private DvDuration supplyDuration;

            @ValidDvDuration(@ValidDuration(min=@DurationMin("P0D"), pattern=@Pattern(regexp="P(\\d+D)?")))
   @TdoNode(name="Supply duration", path="/description[at0001:ITEM_TREE]/items[at0003:ELEMENT]/value")
     public DvDuration getSupplyDuration()
    {
    return supplyDuration;
    }

    public void setSupplyDuration(DvDuration supplyDuration)
    {
    this.supplyDuration = supplyDuration;
    }
     
                             }
                 private PatientInformationSuppliedAction patientInformationSupplied;

            @TdoNode(name="Patient Information supplied", path="/items[openEHR-EHR-ACTION.health_education.v1]")
     public PatientInformationSuppliedAction getPatientInformationSupplied()
    {
    return patientInformationSupplied;
    }

    public void setPatientInformationSupplied(PatientInformationSuppliedAction patientInformationSupplied)
    {
    this.patientInformationSupplied = patientInformationSupplied;
    }
     
                       @Archetype(name="Patient Information supplied", archetypeId="openEHR-EHR-ACTION.health_education.v1")
   public static class PatientInformationSuppliedAction extends Action    {
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
                        private DvText topic;

            @NotNull
   @TdoNode(name="Topic", path="/description[at0001:ITEM_TREE]/items[at0002:ELEMENT]/value")
     public DvText getTopic()
    {
    return topic;
    }

    public void setTopic(DvText topic)
    {
    this.topic = topic;
    }
     
                                         private DvText description1;

            @TdoNode(name="Description", path="/description[at0001:ITEM_TREE]/items[at0003:ELEMENT]/value")
     public DvText getDescription1()
    {
    return description1;
    }

    public void setDescription1(DvText description1)
    {
    this.description1 = description1;
    }
     
                             }
                 private PharmacistFollowupInstruction pharmacistFollowup;

            @TdoNode(name="Pharmacist followup", path="/items[openEHR-EHR-INSTRUCTION.request-follow_up.v1]")
     public PharmacistFollowupInstruction getPharmacistFollowup()
    {
    return pharmacistFollowup;
    }

    public void setPharmacistFollowup(PharmacistFollowupInstruction pharmacistFollowup)
    {
    this.pharmacistFollowup = pharmacistFollowup;
    }
     
                       @Archetype(name="Pharmacist followup", archetypeId="openEHR-EHR-INSTRUCTION.request-follow_up.v1")
   public static class PharmacistFollowupInstruction extends Instruction    {
                  @Deprecated
   @Override
   public List<Activity> getActivities()
   {
        TdoAccess.check("Property 'Activities' is deprecated in: " + getClass().getName());
    return super.getActivities();
       }

                        private RequestActivity request;

            @TdoNode(name="Request", path="/activities[at0001]")
     public RequestActivity getRequest()
    {
    return request;
    }

    public void setRequest(RequestActivity request)
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
                        private DvText followUpServiceRequested;

            @NotNull
   @TdoNode(name="Follow up Service Requested", path="/description[at0009:ITEM_TREE]/items[at0121.1:ELEMENT]/value")
     public DvText getFollowUpServiceRequested()
    {
    return followUpServiceRequested;
    }

    public void setFollowUpServiceRequested(DvText followUpServiceRequested)
    {
    this.followUpServiceRequested = followUpServiceRequested;
    }
     
                                         private DvText notes;

            @TdoNode(name="Notes", path="/description[at0009:ITEM_TREE]/items[at0135.1:ELEMENT]/value")
     public DvText getNotes()
    {
    return notes;
    }

    public void setNotes(DvText notes)
    {
    this.notes = notes;
    }
     
                                         private List<Cluster> specificDetails;

            @TdoNode(name="Specific details", path="/description[at0009:ITEM_TREE]/items[at0132:CLUSTER]")
     public List<Cluster> getSpecificDetails()
    {
    if (specificDetails == null)
    {
      specificDetails = new ArrayList<>();
    }

    return specificDetails;
    }

    public void setSpecificDetails(List<Cluster> specificDetails)
    {
    this.specificDetails = specificDetails;
    }

                             private DvDateTime followUpDate;

            @TdoNode(name="Follow-up date", path="/description[at0009:ITEM_TREE]/items[at0040:ELEMENT]/value")
     public DvDateTime getFollowUpDate()
    {
    return followUpDate;
    }

    public void setFollowUpDate(DvDateTime followUpDate)
    {
    this.followUpDate = followUpDate;
    }
     
                                         private List<Cluster> patientRequirements;

            @TdoNode(name="Patient requirements", path="/description[at0009:ITEM_TREE]/items[at0116:CLUSTER]")
     public List<Cluster> getPatientRequirements()
    {
    if (patientRequirements == null)
    {
      patientRequirements = new ArrayList<>();
    }

    return patientRequirements;
    }

    public void setPatientRequirements(List<Cluster> patientRequirements)
    {
    this.patientRequirements = patientRequirements;
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
                        private DvText requestorIdentifier;

            @TdoNode(name="Requestor Identifier", path="/protocol[at0008:ITEM_TREE]/items[at0010:ELEMENT]/value")
     public DvText getRequestorIdentifier()
    {
    return requestorIdentifier;
    }

    public void setRequestorIdentifier(DvText requestorIdentifier)
    {
    this.requestorIdentifier = requestorIdentifier;
    }
     
                                         private List<Cluster> requestor;

            @TdoNode(name="Requestor", path="/protocol[at0008:ITEM_TREE]/items[at0141:CLUSTER]")
     public List<Cluster> getRequestor()
    {
    if (requestor == null)
    {
      requestor = new ArrayList<>();
    }

    return requestor;
    }

    public void setRequestor(List<Cluster> requestor)
    {
    this.requestor = requestor;
    }

                             private DvText receiverIdentifier;

            @TdoNode(name="Receiver identifier", path="/protocol[at0008:ITEM_TREE]/items[at0011:ELEMENT]/value")
     public DvText getReceiverIdentifier()
    {
    return receiverIdentifier;
    }

    public void setReceiverIdentifier(DvText receiverIdentifier)
    {
    this.receiverIdentifier = receiverIdentifier;
    }
     
                                         private List<Cluster> receiver;

            @TdoNode(name="Receiver", path="/protocol[at0008:ITEM_TREE]/items[at0142:CLUSTER]")
     public List<Cluster> getReceiver()
    {
    if (receiver == null)
    {
      receiver = new ArrayList<>();
    }

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
    {
      distributionListForResponse = new ArrayList<>();
    }

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
    {
      localisation = new ArrayList<>();
    }

    return localisation;
    }

    public void setLocalisation(List<Cluster> localisation)
    {
    this.localisation = localisation;
    }

                 }
                 private PrescriberReferralInstruction prescriberReferral;

            @TdoNode(name="Prescriber referral", path="/items[openEHR-EHR-INSTRUCTION.request-referral.v1]")
     public PrescriberReferralInstruction getPrescriberReferral()
    {
    return prescriberReferral;
    }

    public void setPrescriberReferral(PrescriberReferralInstruction prescriberReferral)
    {
    this.prescriberReferral = prescriberReferral;
    }
     
                       @Archetype(name="Prescriber referral", archetypeId="openEHR-EHR-INSTRUCTION.request-referral.v1")
   public static class PrescriberReferralInstruction extends Instruction    {
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
    {
      request = new ArrayList<>();
    }

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
                        private DvText serviceRequested;

            @NotNull
   @TdoNode(name="Service requested", path="/description[at0009:ITEM_TREE]/items[at0121:ELEMENT]/value")
     public DvText getServiceRequested()
    {
    return serviceRequested;
    }

    public void setServiceRequested(DvText serviceRequested)
    {
    this.serviceRequested = serviceRequested;
    }
     
                                         private List<Cluster> specificDetails;

            @TdoNode(name="Specific details", path="/description[at0009:ITEM_TREE]/items[at0132:CLUSTER]")
     public List<Cluster> getSpecificDetails()
    {
    if (specificDetails == null)
    {
      specificDetails = new ArrayList<>();
    }

    return specificDetails;
    }

    public void setSpecificDetails(List<Cluster> specificDetails)
    {
    this.specificDetails = specificDetails;
    }

                             private DvText reasonDescription;

            @TdoNode(name="Reason description", path="/description[at0009:ITEM_TREE]/items[at0064:ELEMENT]/value")
     public DvText getReasonDescription()
    {
    return reasonDescription;
    }

    public void setReasonDescription(DvText reasonDescription)
    {
    this.reasonDescription = reasonDescription;
    }
     
                                         private List<Cluster> patientRequirements;

            @TdoNode(name="Patient requirements", path="/description[at0009:ITEM_TREE]/items[at0116:CLUSTER]")
     public List<Cluster> getPatientRequirements()
    {
    if (patientRequirements == null)
    {
      patientRequirements = new ArrayList<>();
    }

    return patientRequirements;
    }

    public void setPatientRequirements(List<Cluster> patientRequirements)
    {
    this.patientRequirements = patientRequirements;
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
                        private DvText requestorIdentifier;

            @TdoNode(name="Requestor Identifier", path="/protocol[at0008:ITEM_TREE]/items[at0010:ELEMENT]/value")
     public DvText getRequestorIdentifier()
    {
    return requestorIdentifier;
    }

    public void setRequestorIdentifier(DvText requestorIdentifier)
    {
    this.requestorIdentifier = requestorIdentifier;
    }
     
                                         private List<Cluster> requestor;

            @TdoNode(name="Requestor", path="/protocol[at0008:ITEM_TREE]/items[at0141:CLUSTER]")
     public List<Cluster> getRequestor()
    {
    if (requestor == null)
    {
      requestor = new ArrayList<>();
    }

    return requestor;
    }

    public void setRequestor(List<Cluster> requestor)
    {
    this.requestor = requestor;
    }

                             private DvText receiverIdentifier;

            @TdoNode(name="Receiver identifier", path="/protocol[at0008:ITEM_TREE]/items[at0011:ELEMENT]/value")
     public DvText getReceiverIdentifier()
    {
    return receiverIdentifier;
    }

    public void setReceiverIdentifier(DvText receiverIdentifier)
    {
    this.receiverIdentifier = receiverIdentifier;
    }
     
                                         private List<Cluster> receiver;

            @TdoNode(name="Receiver", path="/protocol[at0008:ITEM_TREE]/items[at0142:CLUSTER]")
     public List<Cluster> getReceiver()
    {
    if (receiver == null)
    {
      receiver = new ArrayList<>();
    }

    return receiver;
    }

    public void setReceiver(List<Cluster> receiver)
    {
    this.receiver = receiver;
    }

                             private DvText requestStatus;

            @TdoNode(name="Request status", path="/protocol[at0008:ITEM_TREE]/items[at0127:ELEMENT]/value")
     public DvText getRequestStatus()
    {
    return requestStatus;
    }

    public void setRequestStatus(DvText requestStatus)
    {
    this.requestStatus = requestStatus;
    }
     
                                         private List<Cluster> distributionListForResponse;

            @TdoNode(name="Distribution list for response", path="/protocol[at0008:ITEM_TREE]/items[at0128:CLUSTER]")
     public List<Cluster> getDistributionListForResponse()
    {
    if (distributionListForResponse == null)
    {
      distributionListForResponse = new ArrayList<>();
    }

    return distributionListForResponse;
    }

    public void setDistributionListForResponse(List<Cluster> distributionListForResponse)
    {
    this.distributionListForResponse = distributionListForResponse;
    }

                             private List<Cluster> contacts;

            @TdoNode(name="Contacts", path="/protocol[at0008:ITEM_TREE]/items[at0.5:CLUSTER]")
     public List<Cluster> getContacts()
    {
    if (contacts == null)
    {
      contacts = new ArrayList<>();
    }

    return contacts;
    }

    public void setContacts(List<Cluster> contacts)
    {
    this.contacts = contacts;
    }

                             private List<Cluster> localisation;

            @TdoNode(name="Localisation", path="/protocol[at0008:ITEM_TREE]/items[at0112:CLUSTER]")
     public List<Cluster> getLocalisation()
    {
    if (localisation == null)
    {
      localisation = new ArrayList<>();
    }

    return localisation;
    }

    public void setLocalisation(List<Cluster> localisation)
    {
    this.localisation = localisation;
    }

                 }
                 private PrescriberReferralResponseEvaluation prescriberReferralResponse;

            @TdoNode(name="Prescriber referral response", path="/items[openEHR-EHR-EVALUATION.recommendation_response.v1]")
     public PrescriberReferralResponseEvaluation getPrescriberReferralResponse()
    {
    return prescriberReferralResponse;
    }

    public void setPrescriberReferralResponse(PrescriberReferralResponseEvaluation prescriberReferralResponse)
    {
    this.prescriberReferralResponse = prescriberReferralResponse;
    }
     
                       @Archetype(name="Prescriber referral response", archetypeId="openEHR-EHR-EVALUATION.recommendation_response.v1")
   public static class PrescriberReferralResponseEvaluation extends Evaluation    {
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
                        private DvCodedText response;

            @ValidDvCodedText(definingCode=@ValidCodePhrase(codeString=@ValidString(pattern=@Pattern(regexp="at0006|at0007|at0008|at0009"), notNull=@NotNull), notNull=@NotNull))
   @TdoNode(name="Response", path="/data[at0001:ITEM_TREE]/items[at0002:ELEMENT]/value")
     public DvCodedText getResponse()
    {
    return response;
    }

    public void setResponse(DvCodedText response)
    {
    this.response = response;
    }
     
                
    public Response getResponseEnum()
  {
  return response instanceof DvCodedText ? EnumTerminology.forEnum(Response.class).getEnumByCode(response.getDefiningCode().getCodeString()) : null;
  }

  public void setResponseEnum(Response response)
  {
  this.response = response == null ? null : DataValueUtils.getLocalCodedText(response.getTerm().getCode(), response.getTerm().getText());
  }
                          public enum Response implements EnumTerminology.TermEnum<Response>
   {
         ACCEPTED_IN_FULL("at0006", "Accepted in full", "The recommendation has been fully accepted."), 
         PARTIALLY_ACCEPTED("at0007", "Partially accepted", "The recommendation has been partially accepted."), 
         REJECTED("at0008", "Rejected", "The recommendation has been rejected."), 
         FURTHER_DISCUSSION_REQUIRED("at0009", "Further discussion required", "Further discussion is required.")
       ;
    Response(String code, String text, String description)
   {
   terminologyTerm = new EnumTerminology.Term<>(this, code, text, description);
   }

   private final EnumTerminology.Term<Response> terminologyTerm;

   public EnumTerminology.Term<Response> getTerm()
   {
   return terminologyTerm;
   }
   public static final EnumTerminology<Response> TERMINOLOGY = EnumTerminology.newInstance(Response.class);
     }
                 private DvText comment;

            @TdoNode(name="Comment", path="/data[at0001:ITEM_TREE]/items[at0005:ELEMENT]/value")
     public DvText getComment()
    {
    return comment;
    }

    public void setComment(DvText comment)
    {
    this.comment = comment;
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
                        private DvUri originalRecommendationEntry;

            @TdoNode(name="Original recommendation entry", path="/protocol[at0003:ITEM_TREE]/items[at0004:ELEMENT]/value")
     public DvUri getOriginalRecommendationEntry()
    {
    return originalRecommendationEntry;
    }

    public void setOriginalRecommendationEntry(DvUri originalRecommendationEntry)
    {
    this.originalRecommendationEntry = originalRecommendationEntry;
    }
     
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
           public enum AdministrationDetailsSite implements EnumTerminology.TermEnum<AdministrationDetailsSite>
   {
         LEFT_UPPER_ARM("at0006", "Left upper arm", "Left upper arm"), 
         RIGHT_UPPER_ARM("at0007", "Right upper arm", "Right upper arm"), 
         THIGH_LEFT_LEG("at0008", "Thigh left leg", "Thigh left leg"), 
         THIGH_RIGHT_LEG("at0009", "Thigh right leg", "Thigh right leg"), 
         MOUTH("at0010", "Mouth", "Mouth"), 
         GLUTEUS_LEFT("at0011", "Gluteus left", "Gluteus left"), 
         GLUTEUS_RIGHT("at0012", "Gluteus right", "Gluteus right")
       ;
    AdministrationDetailsSite(String code, String text, String description)
   {
   terminologyTerm = new EnumTerminology.Term<>(this, code, text, description);
   }

   private final EnumTerminology.Term<AdministrationDetailsSite> terminologyTerm;

   public EnumTerminology.Term<AdministrationDetailsSite> getTerm()
   {
   return terminologyTerm;
   }
   public static final EnumTerminology<AdministrationDetailsSite> TERMINOLOGY = EnumTerminology.newInstance(AdministrationDetailsSite.class);
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