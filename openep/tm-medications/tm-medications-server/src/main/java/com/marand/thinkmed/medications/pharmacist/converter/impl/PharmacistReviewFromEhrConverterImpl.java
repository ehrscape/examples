package com.marand.thinkmed.medications.pharmacist.converter.impl;

import java.util.List;
import java.util.Locale;

import com.marand.maf.core.Pair;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition.PharmacistMedicationReviewEvaluation.MedicationItemAssessmentCluster;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition.PharmacistMedicationReviewEvaluation.MedicationItemAssessmentCluster.DrugRelatedProblemCluster;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition.PharmacistMedicationReviewEvaluation.MedicationItemAssessmentCluster.PatientRelatedProblemCluster;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition.PharmacistMedicationReviewEvaluation.MedicationItemAssessmentCluster.PharmacokineticIssueCluster;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.change.TherapyChangeCalculator;
import com.marand.thinkmed.medications.converter.therapy.MedicationConverterSelector;
import com.marand.thinkmed.medications.converter.therapy.MedicationFromEhrConverter;
import com.marand.thinkmed.medications.dto.change.TherapyChangeDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewStatusEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewTherapyDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistTherapyChangeType;
import com.marand.thinkmed.medications.dto.pharmacist.review.TherapyProblemDescriptionDto;
import com.marand.thinkmed.medications.pharmacist.PharmacistUtils;
import com.marand.thinkmed.medications.pharmacist.converter.PharmacistReviewFromEhrConverter;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.PartyIdentified;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Mitja Lapajne
 */
public class PharmacistReviewFromEhrConverterImpl implements PharmacistReviewFromEhrConverter
{
  private MedicationFromEhrConverter.MedicationDataProvider medicationDataProvider;
  private TherapyDisplayProvider therapyDisplayProvider;
  private MedicationsBo medicationsBo;
  private TherapyChangeCalculator therapyChangeCalculator;

  @Required
  public void setMedicationDataProvider(final MedicationFromEhrConverter.MedicationDataProvider medicationDataProvider)
  {
    this.medicationDataProvider = medicationDataProvider;
  }

  @Required
  public void setTherapyDisplayProvider(final TherapyDisplayProvider therapyDisplayProvider)
  {
    this.therapyDisplayProvider = therapyDisplayProvider;
  }

  @Required
  public void setTherapyChangeCalculator(final TherapyChangeCalculator therapyChangeCalculator)
  {
    this.therapyChangeCalculator = therapyChangeCalculator;
  }

  @Required
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Override
  public PharmacistReviewDto convert(
      final String patientId,
      final PharmacyReviewReportComposition composition,
      final DateTime when,
      final Locale locale)
  {
    final PharmacistReviewDto dto = new PharmacistReviewDto();
    dto.setCompositionUid(composition.getUid().getValue());

    final NamedExternalDto composer = extractComposer(composition);
    dto.setComposer(composer);

    dto.setCreateTimestamp(DataValueUtils.getDateTime(composition.getCompositionEventContext().getStartTime()));
    dto.setPharmacistReviewStatus(
        PharmacistReviewStatusEnum.valueOf(composition.getCompositionEventContext().getStatus().getValue()));

    if (composition.getPharmacistMedicationReview() != null)
    {
      final MedicationItemAssessmentCluster assessment =
          composition.getPharmacistMedicationReview().getMedicationItemAssessment().get(0);

      dto.setNoProblem(assessment.getNoProblemIdentified() != null && assessment.getNoProblemIdentified().isValue());
      if (assessment.getOverallRecommendation() != null)
      {
        dto.setOverallRecommendation(assessment.getOverallRecommendation().getValue());
      }

      final DrugRelatedProblemCluster drugRelatedProblem = assessment.getDrugRelatedProblem();
      if (drugRelatedProblem != null)
      {
        final TherapyProblemDescriptionDto drugRelatedProblemDto =
            convertTherapyProblemDescription(
                drugRelatedProblem.getCategory(),
                drugRelatedProblem.getOutcome(),
                drugRelatedProblem.getImpact(),
                drugRelatedProblem.getRecommendation());
        dto.setDrugRelatedProblem(drugRelatedProblemDto);
      }

      final List<PharmacokineticIssueCluster> pharmacokineticIssues = assessment.getPharmacokineticIssue();
      if (!pharmacokineticIssues.isEmpty())
      {
        final TherapyProblemDescriptionDto pharmacokineticIssueDto =
            convertTherapyProblemDescription(
                pharmacokineticIssues.get(0).getCategory(),
                pharmacokineticIssues.get(0).getOutcome(),
                pharmacokineticIssues.get(0).getImpact(),
                pharmacokineticIssues.get(0).getRecommendation());
        dto.setPharmacokineticIssue(pharmacokineticIssueDto);
      }

      final List<PatientRelatedProblemCluster> patientRelatedProblems = assessment.getPatientRelatedProblem();
      if (!patientRelatedProblems.isEmpty())
      {
        final TherapyProblemDescriptionDto patientRelatedProblemDto =
            convertTherapyProblemDescription(
                patientRelatedProblems.get(0).getCategory(),
                patientRelatedProblems.get(0).getOutcome(),
                patientRelatedProblems.get(0).getImpact(),
                patientRelatedProblems.get(0).getRecommendation());
        dto.setPatientRelatedProblem(patientRelatedProblemDto);
      }
    }

    if (composition.getMiscellaneous() != null)
    {
      final boolean referBackToPrescriber = composition.getMiscellaneous().getPrescriberReferral() != null;
      dto.setReferBackToPrescriber(referBackToPrescriber);

      if (!composition.getMiscellaneous().getMedicationInstruction().isEmpty())
      {
        final MedicationInstructionInstruction instruction =
            composition.getMiscellaneous().getMedicationInstruction().get(0);

        final Pair<MedicationOrderComposition, MedicationInstructionInstruction> originalTherapy =
            medicationsBo.getInstructionFromLink(patientId, instruction.getLinks().get(0), false);

        final MedicationFromEhrConverter<?> originalTherapyConverter =
            MedicationConverterSelector.getConverter(originalTherapy.getSecond());

        final TherapyDto originalTherapyDto =
            originalTherapyConverter.createTherapyFromInstruction(
                originalTherapy.getSecond(),
                originalTherapy.getFirst().getUid().getValue(),
                originalTherapy.getSecond().getName().getValue(),
                DataValueUtils.getDateTime(composition.getCompositionEventContext().getStartTime()),
                when,
                medicationDataProvider);
        therapyDisplayProvider.fillDisplayValues(originalTherapyDto, true, false, locale);

        final MedicationFromEhrConverter<?> converter = MedicationConverterSelector.getConverter(instruction);

        final TherapyDto therapyDto =
            converter.createTherapyFromInstruction(
                instruction,
                originalTherapy.getFirst().getUid().getValue(),
                originalTherapy.getSecond().getName().getValue(),
                DataValueUtils.getDateTime(composition.getCompositionEventContext().getStartTime()),
                when,
                medicationDataProvider);
        therapyDisplayProvider.fillDisplayValues(therapyDto, true, false, locale);

        final PharmacistReviewTherapyDto pharmacistReviewTherapyDto = new PharmacistReviewTherapyDto();
        pharmacistReviewTherapyDto.setTherapy(therapyDto);

        if (!composition.getMiscellaneous().getMedicationAction().isEmpty())
        {
          final MedicationActionAction medicationAction = composition.getMiscellaneous().getMedicationAction().get(0);

          final PharmacistTherapyChangeType pharmacistTherapyChangeType =
              PharmacistUtils.getPharmacistTherapyChangeType(medicationAction);
          pharmacistReviewTherapyDto.setChangeType(pharmacistTherapyChangeType);
        }

        if (pharmacistReviewTherapyDto.getChangeType() == PharmacistTherapyChangeType.EDIT)
        {
          final List<TherapyChangeDto<?, ?>> changes =
              therapyChangeCalculator.calculateTherapyChanges(originalTherapyDto, therapyDto, false, locale);
          pharmacistReviewTherapyDto.setChanges(changes);
        }

        dto.getRelatedTherapies().add(pharmacistReviewTherapyDto);
      }
    }
    return dto;
  }

  private TherapyProblemDescriptionDto convertTherapyProblemDescription(
      final List<DvCodedText> categories,
      final DvCodedText outcome,
      final DvCodedText impact,
      final DvText recommendation)
  {
    final TherapyProblemDescriptionDto therapyProblemDescription = new TherapyProblemDescriptionDto();

    for (final DvCodedText category : categories)
    {
      final NamedExternalDto categoryNamedIdentity = convertCodedText(category);
      therapyProblemDescription.getCategories().add(categoryNamedIdentity);
    }
    final NamedExternalDto outcomeDto = convertCodedText(outcome);
    therapyProblemDescription.setOutcome(outcomeDto);
    final NamedExternalDto impactDto = convertCodedText(impact);
    therapyProblemDescription.setImpact(impactDto);
    therapyProblemDescription.setRecommendation(recommendation.getValue());
    return therapyProblemDescription;
  }

  private NamedExternalDto convertCodedText(final DvCodedText codedText)
  {
    return codedText != null ?
           new NamedExternalDto(codedText.getDefiningCode().getCodeString(), codedText.getValue()) :
           null;
  }

  @Override
  public NamedExternalDto extractComposer(final PharmacyReviewReportComposition composition)
  {
    final String composerName = ((PartyIdentified)composition.getComposer()).getName();
    final String composerId = composition.getComposer().getExternalRef().getId().getValue();
    return new NamedExternalDto(composerId, composerName);
  }
}
