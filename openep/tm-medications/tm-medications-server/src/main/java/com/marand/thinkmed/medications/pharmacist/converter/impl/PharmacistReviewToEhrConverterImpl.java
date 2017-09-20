package com.marand.thinkmed.medications.pharmacist.converter.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;
import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.ispek.ehr.common.IspekEhrUtils;
import com.marand.maf.core.openehr.visitor.IspekTdoDataSupport;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition.CompositionEventContext;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition.MiscellaneousSection;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition.MiscellaneousSection.PrescriberReferralInstruction;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition.MiscellaneousSection.PrescriberReferralResponseEvaluation;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition.PharmacistMedicationReviewEvaluation;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition.PharmacistMedicationReviewEvaluation.MedicationItemAssessmentCluster;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition.PharmacistMedicationReviewEvaluation.MedicationItemAssessmentCluster.DrugRelatedProblemCluster;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition.PharmacistMedicationReviewEvaluation.MedicationItemAssessmentCluster.PatientRelatedProblemCluster;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition.PharmacistMedicationReviewEvaluation.MedicationItemAssessmentCluster.PharmacokineticIssueCluster;
import com.marand.openehr.util.DataValueUtils;
import com.marand.openehr.util.OpenEhrRefUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.converter.therapy.MedicationConverterSelector;
import com.marand.thinkmed.medications.converter.therapy.MedicationToEhrConverter;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewStatusEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewTherapyDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistTherapyChangeType;
import com.marand.thinkmed.medications.dto.pharmacist.review.ReviewPharmacistReviewAction;
import com.marand.thinkmed.medications.dto.pharmacist.review.TherapyProblemDescriptionDto;
import com.marand.thinkmed.medications.pharmacist.PharmacistUtils;
import com.marand.thinkmed.medications.pharmacist.converter.PharmacistReviewToEhrConverter;
import com.marand.thinkmed.medications.pharmacist.converter.TherapyProblemDescriptionConvertDto;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvParsable;
import org.openehr.jaxb.rm.Link;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Mitja Lapajne
 */
public class PharmacistReviewToEhrConverterImpl implements PharmacistReviewToEhrConverter
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Required
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Override
  public PharmacyReviewReportComposition convert(final String patientId, final PharmacistReviewDto dto, final DateTime when)
  {
    final PharmacyReviewReportComposition composition = new PharmacyReviewReportComposition();
    if (dto.getComposer() != null)
    {
      composition.setComposer(IspekTdoDataSupport.getPartyIdentified(dto.getComposer().getName(), dto.getComposer().getId()));
    }
    final PharmacistMedicationReviewEvaluation pharmacistMedicationReview = new PharmacistMedicationReviewEvaluation();
    composition.setPharmacistMedicationReview(pharmacistMedicationReview);

    final MedicationItemAssessmentCluster assessmentCluster = new MedicationItemAssessmentCluster();
    pharmacistMedicationReview.getMedicationItemAssessment().add(assessmentCluster);

    final DrugRelatedProblemCluster drugRelatedProblem =
        convertDrugRelatedProblem(dto.getDrugRelatedProblem());
    assessmentCluster.setDrugRelatedProblem(drugRelatedProblem);

    final List<PharmacokineticIssueCluster> pharmacokineticIssues =
        convertPharmacokineticIssues(dto.getPharmacokineticIssue());
    assessmentCluster.setPharmacokineticIssue(pharmacokineticIssues);

    final List<PatientRelatedProblemCluster> patientRelatedProblem =
        convertPatientRelatedProblem(dto.getPatientRelatedProblem());
    assessmentCluster.setPatientRelatedProblem(patientRelatedProblem);

    assessmentCluster.setNoProblemIdentified(dto.isNoProblem() ? DataValueUtils.getBoolean(dto.isNoProblem()) : null);

    if (dto.getOverallRecommendation() != null)
    {
      assessmentCluster.setOverallRecommendation(DataValueUtils.getText(dto.getOverallRecommendation()));
    }

    composition.setMiscellaneous(new MiscellaneousSection());

    final List<MedicationInstructionInstruction> instructions =
        convertRelatedMedicationInstructionsAndActions(patientId, composition, dto.getRelatedTherapies(), when);
    composition.getMiscellaneous().setMedicationInstruction(instructions);

    final CompositionEventContext compositionEventContext =
        IspekTdoDataSupport.getEventContext(CompositionEventContext.class, when);
    composition.setCompositionEventContext(compositionEventContext);
    compositionEventContext.setStatus(DataValueUtils.getText(PharmacistReviewStatusEnum.Draft.name()));
    compositionEventContext.setStartTime(DataValueUtils.getDateTime(when));

    if (dto.isReferBackToPrescriber())
    {
      final PrescriberReferralInstruction prescriberReferralInstruction = new PrescriberReferralInstruction();
      final PrescriberReferralInstruction.RequestActivity requestActivity = new PrescriberReferralInstruction.RequestActivity();

      final DvParsable timing = new DvParsable();
      timing.setValue(ISOPeriodFormat.standard().print(new Period(0, 0, 0, 1, 0, 0, 0, 0)));
      timing.setFormalism(IspekEhrUtils.TIMING_FORMALISM);

      requestActivity.setTiming(timing);
      requestActivity.setActionArchetypeId("[openEHR-EHR-ACTION.medication.v1]");
      requestActivity.setReasonDescription(DataValueUtils.getText("Pharmacist referred the review to prescriber"));
      requestActivity.setServiceRequested(DataValueUtils.getText("Review pharmacist report"));

      prescriberReferralInstruction.getRequest().add(requestActivity);
      prescriberReferralInstruction.setNarrative(DataValueUtils.getText("Pharmacist referred the review to prescriber"));

      composition.getMiscellaneous().setPrescriberReferral(prescriberReferralInstruction);
    }
    else
    {
      composition.getMiscellaneous().setPrescriberReferral(null);
    }

    MedicationsEhrUtils.visitComposition(composition, dto.getComposer(), when);
    return composition;
  }

  private DrugRelatedProblemCluster convertDrugRelatedProblem(final TherapyProblemDescriptionDto dto)
  {
    if (dto != null)
    {
      final DrugRelatedProblemCluster drugRelatedProblem = new DrugRelatedProblemCluster();
      final TherapyProblemDescriptionConvertDto convertDto = convertTherapyProblemDescription(dto);
      drugRelatedProblem.setCategory(convertDto.getCategories());
      drugRelatedProblem.setOutcome(convertDto.getOutcome());
      drugRelatedProblem.setImpact(convertDto.getImpact());
      drugRelatedProblem.setRecommendation(convertDto.getRecommendation());
      return drugRelatedProblem;
    }
    return null;
  }

  private List<PharmacokineticIssueCluster> convertPharmacokineticIssues(final TherapyProblemDescriptionDto dto)
  {
    if (dto != null)
    {
      final PharmacokineticIssueCluster pharmacokineticIssue = new PharmacokineticIssueCluster();
      final TherapyProblemDescriptionConvertDto convertDto = convertTherapyProblemDescription(dto);
      pharmacokineticIssue.setCategory(convertDto.getCategories());
      pharmacokineticIssue.setOutcome(convertDto.getOutcome());
      pharmacokineticIssue.setImpact(convertDto.getImpact());
      pharmacokineticIssue.setRecommendation(convertDto.getRecommendation());
      return Collections.singletonList(pharmacokineticIssue);
    }
    return null;
  }

  private List<PatientRelatedProblemCluster> convertPatientRelatedProblem(final TherapyProblemDescriptionDto dto)
  {
    if (dto != null)
    {
      final PatientRelatedProblemCluster patientRelatedProblem = new PatientRelatedProblemCluster();
      final TherapyProblemDescriptionConvertDto convertDto = convertTherapyProblemDescription(dto);
      patientRelatedProblem.setCategory(convertDto.getCategories());
      patientRelatedProblem.setOutcome(convertDto.getOutcome());
      patientRelatedProblem.setImpact(convertDto.getImpact());
      patientRelatedProblem.setRecommendation(convertDto.getRecommendation());
      return Collections.singletonList(patientRelatedProblem);
    }
    return null;
  }

  private TherapyProblemDescriptionConvertDto convertTherapyProblemDescription(final TherapyProblemDescriptionDto dto)
  {
    final TherapyProblemDescriptionConvertDto convertDto = new TherapyProblemDescriptionConvertDto();
    for (final NamedExternalDto category : dto.getCategories())
    {
      convertDto.getCategories().add(convertNamedIdentity(category));
    }
    final DvCodedText outcome = convertNamedIdentity(dto.getOutcome());
    convertDto.setOutcome(outcome);

    final DvCodedText impact = convertNamedIdentity(dto.getImpact());
    convertDto.setImpact(impact);

    convertDto.setRecommendation(DataValueUtils.getText(dto.getRecommendation()));
    return convertDto;
  }

  private DvCodedText convertNamedIdentity(final NamedExternalDto dto)
  {
    return dto != null ? DataValueUtils.getLocalCodedText(dto.getId(), dto.getName()) : null;
  }

  private List<MedicationInstructionInstruction> convertRelatedMedicationInstructionsAndActions(
      final String patientId,
      final PharmacyReviewReportComposition composition,
      final List<PharmacistReviewTherapyDto> relatedTherapies,
      final DateTime when)
  {
    final List<MedicationInstructionInstruction> instructions = new ArrayList<>();

    Preconditions.checkArgument(relatedTherapies.size() < 2, "Only 1 or 0 relatedTherapies allowed!");
    if (!relatedTherapies.isEmpty())
    {
      final PharmacistReviewTherapyDto relatedTherapy = relatedTherapies.get(0);
      final TherapyDto therapy = relatedTherapy.getTherapy();
      final MedicationToEhrConverter<?> therapyConverter = MedicationConverterSelector.getConverter(therapy);
      final MedicationInstructionInstruction instruction = therapyConverter.createInstructionFromTherapy(therapy);
      if (relatedTherapy.getChangeType() != null && relatedTherapy.getChangeType() != PharmacistTherapyChangeType.NONE)
      {
        final MedicationActionEnum actionEnum = PharmacistUtils.getMedicationActionChangeType(relatedTherapy.getChangeType());

        if (actionEnum != null)
        {
          MedicationsEhrUtils.addMedicationActionTo(composition, actionEnum, when);
        }
      }

      instruction.getLinks().clear();
      final Link linkToTherapy =
          getLinkToTherapyInstruction(patientId, therapy.getCompositionUid(), therapy.getEhrOrderName());
      instruction.getLinks().add(linkToTherapy);

      instructions.add(instruction);
    }
    return instructions;
  }

  private Link getLinkToTherapyInstruction(final String patientId, final String compositionUid, final String ehrName)
  {
    final MedicationOrderComposition orderComposition =
        medicationsOpenEhrDao.loadMedicationOrderComposition(patientId, compositionUid);
    final MedicationInstructionInstruction orderInstruction =
        MedicationsEhrUtils.getMedicationInstructionByEhrName(orderComposition, ehrName);

    return OpenEhrRefUtils.getLinkToTdoTarget("pharmacy", EhrLinkType.REVIEWED.getName(), orderComposition, orderInstruction);
  }

  @Override
  public void setPrescriberReferralResponse(
      final PharmacyReviewReportComposition composition,
      final ReviewPharmacistReviewAction reviewAction,
      final DateTime when)
  {
    final PrescriberReferralResponseEvaluation prescriberReferralResponse = new PrescriberReferralResponseEvaluation();

    if (reviewAction == ReviewPharmacistReviewAction.MODIFIED || reviewAction == ReviewPharmacistReviewAction.ABORTED)
    {
      prescriberReferralResponse.setResponseEnum(PrescriberReferralResponseEvaluation.Response.PARTIALLY_ACCEPTED);
    }
    else if (reviewAction == ReviewPharmacistReviewAction.ACCEPTED)
    {
      prescriberReferralResponse.setResponseEnum(PrescriberReferralResponseEvaluation.Response.ACCEPTED_IN_FULL);
    }
    else
    {
      prescriberReferralResponse.setResponseEnum(PrescriberReferralResponseEvaluation.Response.REJECTED);
    }
    if (composition.getMiscellaneous() == null)
    {
      composition.setMiscellaneous(new MiscellaneousSection());
    }
    composition.getMiscellaneous().setPrescriberReferralResponse(prescriberReferralResponse);
    MedicationsEhrUtils.visitComposition(composition, composition.getComposer(), when);
  }
}
