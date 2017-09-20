package com.marand.thinkmed.medications.administration.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster.InfusionAdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster.OxygenDeliveryCluster;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster.OxygenDeliveryCluster.AmbientOxygenCluster;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationAdministrationComposition;
import com.marand.openehr.medications.tdo.MedicationAdministrationComposition.MedicationDetailSection.ClinicalInterventionAction;
import com.marand.openehr.medications.tdo.MedicationAdministrationComposition.MedicationDetailSection.ClinicalInterventionAction.InfusionBagAmountCluster;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.AdministrationResultEnum;
import com.marand.thinkmed.medications.AdministrationStatusEnum;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.InfusionSetChangeEnum;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.MedicationJobPerformerEnum;
import com.marand.thinkmed.medications.ParticipationTypeEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.administration.AdministrationFromEhrConverter;
import com.marand.thinkmed.medications.administration.AdministrationUtils;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.converter.therapy.MedicationFromEhrConverter;
import com.marand.thinkmed.medications.converter.therapy.OxygenMedicationFromEhrConverter;
import com.marand.thinkmed.medications.dto.CodedNameDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.OxygenStartingDevice;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdjustOxygenAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.BolusAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.DoseAdministration;
import com.marand.thinkmed.medications.dto.administration.InfusionBagAdministration;
import com.marand.thinkmed.medications.dto.administration.InfusionBagDto;
import com.marand.thinkmed.medications.dto.administration.InfusionSetChangeDto;
import com.marand.thinkmed.medications.dto.administration.OxygenAdministration;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StartOxygenAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StopAdministrationDto;
import org.joda.time.format.ISODateTimeFormat;
import org.openehr.jaxb.rm.Action;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvDate;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.Participation;
import org.openehr.jaxb.rm.PartyIdentified;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Nejc Korasa
 */
public class AdministrationFromEhrConverterImpl implements AdministrationFromEhrConverter
{
  private MedicationFromEhrConverter.MedicationDataProvider medicationDataProvider;
  private AdministrationUtils administrationUtils;

  @Autowired
  public void setMedicationDataProvider(final MedicationFromEhrConverter.MedicationDataProvider medicationDataProvider)
  {
    this.medicationDataProvider = medicationDataProvider;
  }

  @Autowired
  public void setAdministrationUtils(final AdministrationUtils administrationUtils)
  {
    this.administrationUtils = administrationUtils;
  }

  @Override
  public List<AdministrationDto> convertToAdministrationDtos(
      @Nonnull final Map<String, List<MedicationAdministrationComposition>> administrations,
      @Nonnull final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs)
  {
    Preconditions.checkNotNull(administrations, "administrations must not be null");
    Preconditions.checkNotNull(instructionPairs, "instructionPairs must not be null");

    final List<AdministrationDto> administrationDtos = new ArrayList<>();
    for (final String therapyId : administrations.keySet())
    {
      final List<MedicationAdministrationComposition> administrationsList = administrations.get(therapyId);
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair =
          MedicationsEhrUtils.findMedicationInstructionPairByTherapyId(therapyId, instructionPairs);

      administrationDtos.addAll(
          administrationsList
              .stream()
              .map(administrationComp -> convertToAdministrationDto(administrationComp, instructionPair, therapyId))
              .collect(Collectors.toList()));
    }

    return administrationDtos;
  }

  @Override
  public AdministrationDto convertToAdministrationDto(
      @Nonnull final MedicationAdministrationComposition administrationComp,
      @Nonnull final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair,
      @Nonnull final String therapyId)
  {
    Preconditions.checkNotNull(administrationComp, "administrationComp must not be null");
    Preconditions.checkNotNull(instructionPair, "instructionPair must not be null");
    Preconditions.checkNotNull(therapyId, "therapyId must not be null");

    final Action action;
    final AdministrationDto administrationDto;
    if (!administrationComp.getMedicationDetail().getMedicationAction().isEmpty())
    {
      action = administrationComp.getMedicationDetail().getMedicationAction().get(0);
      administrationDto = buildAdministrationFromMedicationAction(
          (MedicationActionAction)action,
          instructionPair.getSecond());

      if (!administrationComp.getMedicationDetail().getClinicalIntervention().isEmpty())
      {
        setInfusionBagDataToAdministrationDto(
            administrationComp.getMedicationDetail().getClinicalIntervention().get(0),
            administrationDto);
      }
    }
    else if (!administrationComp.getMedicationDetail().getClinicalIntervention().isEmpty())
    {
      action = administrationComp.getMedicationDetail().getClinicalIntervention().get(0);
      administrationDto = buildAdministrationFromClinicalIntervention((ClinicalInterventionAction)action);
    }
    else
    {
      throw new IllegalArgumentException(
          "MedicationAdministrationComposition must have MedicationActions or ClinicalInterventions");
    }

    administrationDto.setAdministrationTime(DataValueUtils.getDateTime(action.getTime()));

    final String composerName =
        administrationComp.getComposer() instanceof PartyIdentified
        ? ((PartyIdentified)administrationComp.getComposer()).getName()
        : "";
    if (!MedicationJobPerformerEnum.AUTOMATIC_CHARTING_PERFORMER.getCode().equals(composerName))
    {
      administrationDto.setComposerName(composerName);
    }

    administrationDto.setAdministrationId(administrationComp.getUid().getValue());
    administrationDto.setTherapyId(therapyId);

    return administrationDto;
  }

  private InfusionSetChangeDto buildAdministrationFromClinicalIntervention(final ClinicalInterventionAction action)
  {
    final InfusionSetChangeDto administration = new InfusionSetChangeDto();
    administration.setAdministrationStatus(AdministrationStatusEnum.COMPLETED);
    administration.setInfusionSetChangeEnum(
        InfusionSetChangeEnum.getEnumByCode(action.getIntervention().getDefiningCode().getCodeString()));

    if (action.getComments() != null)
    {
      administration.setComment(action.getComments().getValue());
    }

    setInfusionBagDataToAdministrationDto(action, administration);
    return administration;
  }

  private void setInfusionBagDataToAdministrationDto(
      final ClinicalInterventionAction action,
      final AdministrationDto administration)
  {
    final InfusionBagAmountCluster bagChangeDose = action.getInfusionBagAmount();

    if (bagChangeDose != null)
    {
      final InfusionBagDto infusionBagDto = new InfusionBagDto(
          bagChangeDose.getQuantity().getMagnitude(),
          bagChangeDose.getDoseUnit().getDefiningCode().getCodeString());

      Preconditions.checkArgument(
          administration instanceof InfusionBagAdministration,
          "Not supported administration data object");

      ((InfusionBagAdministration)administration).setInfusionBag(infusionBagDto);
    }
  }

  private AdministrationDto buildAdministrationFromMedicationAction(
      final MedicationActionAction action,
      final MedicationInstructionInstruction medicationInstruction)
  {
    final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);

    Preconditions.checkArgument(
        actionEnum == MedicationActionEnum.ADMINISTER
            || actionEnum == MedicationActionEnum.WITHHOLD
            || actionEnum == MedicationActionEnum.DEFER);

    final AdministrationDto administration;

    final Pair<AdministrationTypeEnum, CodedNameDto> reasonValues = getReasonValuesFromMedicationAction(action);
    final AdministrationTypeEnum administrationTypeEnum = reasonValues != null ? reasonValues.getFirst() : null;
    final CodedNameDto notAdministeredReason = reasonValues != null ? reasonValues.getSecond() : null;

    final AdministrationDetailsCluster administrationDetails = action.getAdministrationDetails();
    final InfusionAdministrationDetailsCluster infusionAdministrationDetails =
        administrationDetails.getInfusionAdministrationDetails().isEmpty()
        ? null
        : administrationDetails.getInfusionAdministrationDetails().get(0);

    if (isOxygenTherapy(medicationInstruction))
    {
      administration = buildAdministrationDtoForOxygenTherapy(administrationTypeEnum, administrationDetails);
    }
    else if (administrationTypeEnum == AdministrationTypeEnum.START || administrationTypeEnum == AdministrationTypeEnum.BOLUS)
    {
      final boolean hasRate = isRateAdministration(administrationTypeEnum, administrationDetails);
      final boolean hasQuantity = action.getStructuredDose() != null;

      TherapyDoseDto administeredDose = null;
      MedicationDto substituteMedication = null;

      if (hasRate) // RATE
      {
        administeredDose = MedicationsEhrUtils.buildTherapyDoseDtoForRate(infusionAdministrationDetails);
      }
      else if (hasQuantity) // QUANTITY, VOLUME_SUM
      {
        final boolean multipleIngredients = Opt
            .resolve(() -> action.getIngredientsAndForm().getIngredient())
            .map(i -> i.size() > 1)
            .orElse(false);

        administeredDose = MedicationsEhrUtils.buildTherapyDoseDto(action.getStructuredDose(), multipleIngredients);

        if (MedicationsEhrUtils.isSimpleInstruction(medicationInstruction))
        {
          final DvText therapyMedicine = medicationInstruction.getOrder().get(0).getMedicine();
          if (therapyMedicine != null && therapyMedicine instanceof DvCodedText)
          {
            if (action.getMedicine() != null && action.getMedicine() instanceof DvCodedText)
            {
              final Long therapyMedicationId =
                  Long.parseLong(((DvCodedText)therapyMedicine).getDefiningCode().getCodeString());
              final Long administrationMedicationId =
                  Long.parseLong(((DvCodedText)action.getMedicine()).getDefiningCode().getCodeString());
              if (!therapyMedicationId.equals(administrationMedicationId))
              {
                substituteMedication = medicationDataProvider.getMedication(administrationMedicationId);
              }
            }
          }
        }
      }
      if (administrationTypeEnum == AdministrationTypeEnum.START)
      {
        administration = new StartAdministrationDto();
        ((StartAdministrationDto)administration).setAdministeredDose(administeredDose);
        ((StartAdministrationDto)administration).setSubstituteMedication(substituteMedication);
      }
      else
      {
        administration = new BolusAdministrationDto();
        ((BolusAdministrationDto)administration).setAdministeredDose(administeredDose);
      }
    }
    else if (administrationTypeEnum == AdministrationTypeEnum.ADJUST_INFUSION) // RATE
    {
      administration = new AdjustInfusionAdministrationDto();
      if (infusionAdministrationDetails != null)
      {
        ((AdjustInfusionAdministrationDto)administration).setAdministeredDose(
            MedicationsEhrUtils.buildTherapyDoseDtoForRate(infusionAdministrationDetails));
      }
    }
    else if (administrationTypeEnum == AdministrationTypeEnum.STOP)
    {
      administration = new StopAdministrationDto();
    }
    else
    {
      throw new IllegalArgumentException("Administration reason not supported");
    }

    administration.setNotAdministeredReason(notAdministeredReason);
    administration.setSelfAdministrationType(action.getSelfAdministrationTypeEnum());

    final AdministrationResultEnum administrationResultEnum = administrationUtils.getAdministrationResult(action);
    administration.setAdministrationResult(administrationResultEnum);

    final DvText batchID = action.getBatchID();
    final DvDate expiryDate = action.getExpiryDate();
    if (batchID != null)
    {
      administration.setBatchId(batchID.getValue());
    }
    if (expiryDate != null)
    {
      administration.setExpiryDate(ISODateTimeFormat.date().parseDateTime(expiryDate.getValue()));
    }

    final DvText witnessCode = DataValueUtils.getText(ParticipationTypeEnum.WITNESS.getCode());
    action.getOtherParticipations()
        .stream()
        .filter(participation -> participation.getFunction().equals(witnessCode))
        .forEach(participation -> setAdministrationWitness(administration, participation));

    final AdministrationStatusEnum administrationStatus =
        actionEnum == MedicationActionEnum.ADMINISTER
        ? AdministrationStatusEnum.COMPLETED
        : AdministrationStatusEnum.FAILED;

    administration.setAdministrationStatus(administrationStatus);
    if (action.getComment() != null)
    {
      administration.setComment(action.getComment().getValue());
    }

    if (administration.getAdministrationResult() == AdministrationResultEnum.GIVEN)
    {
      if (!action.getAdministrationDetails().getRoute().isEmpty())
      {
        final MedicationRouteDto routeDto = new MedicationRouteDto();
        final DvCodedText routeDvCodedText = action.getAdministrationDetails().getRoute().get(0);
        routeDto.setId(Long.valueOf(routeDvCodedText.getDefiningCode().getCodeString()));
        routeDto.setName(routeDvCodedText.getValue());

        administration.setRoute(routeDto);
      }
    }

    return administration;
  }

  private AdministrationDto buildAdministrationDtoForOxygenTherapy(
      final AdministrationTypeEnum administrationType,
      final AdministrationDetailsCluster administrationDetails)
  {
    final AdministrationDto administration;
    if (administrationType == AdministrationTypeEnum.START)
    {
      administration = new StartOxygenAdministrationDto();
    }
    else if (administrationType == AdministrationTypeEnum.ADJUST_INFUSION)
    {
      administration = new AdjustOxygenAdministrationDto();
    }
    else if (administrationType == AdministrationTypeEnum.STOP)
    {
      return new StopAdministrationDto();
    }
    else
    {
      throw new IllegalArgumentException("This administration type is not supported for oxygen therapy");
    }

    if (administrationDetails.getOxygenDelivery().size() != 1)
    {
      throw new IllegalArgumentException(
          "Expected exactly one oxygen delivery cluster, got " + administrationDetails.getOxygenDelivery().size() + '!');
    }

    final Opt<OxygenDeliveryCluster> oxygenDelivery = Opt.of(administrationDetails.getOxygenDelivery().get(0));

    ((DoseAdministration)administration).setAdministeredDose(buildDoseFromOxygenDelivery(oxygenDelivery));

    oxygenDelivery
        .map(OxygenDeliveryCluster::getRouteEnum)
        .ifPresent(route -> ((OxygenAdministration)administration).setStartingDevice(new OxygenStartingDevice(route)));

    if (oxygenDelivery.isPresent() && oxygenDelivery.get().getDevice().size() == 1)
    {
      Opt.resolve(() -> oxygenDelivery.get().getDevice().get(0).getType())
          .map(DvText::getValue)
          .map(String::valueOf)
          .ifPresent(i -> ((OxygenAdministration)administration).getStartingDevice().setRouteType(i));
    }

    return administration;
  }

  private TherapyDoseDto buildDoseFromOxygenDelivery(final Opt<OxygenDeliveryCluster> oxygenDelivery)
  {
    final TherapyDoseDto dose = new TherapyDoseDto();

    final Opt<Double> rate = oxygenDelivery
        .map(OxygenDeliveryCluster::getAmbientOxygen)
        .map(AmbientOxygenCluster::getOxygenFlowRate)
        .map(DvQuantity::getMagnitude);

    final Opt<String> unit = oxygenDelivery
        .map(OxygenDeliveryCluster::getAmbientOxygen)
        .map(AmbientOxygenCluster::getOxygenFlowRate)
        .map(DvQuantity::getUnits);

    if (rate.isPresent() && unit.isPresent())
    {
      final Pair<Double, String> oxygenFlowRateWithUnit = OxygenMedicationFromEhrConverter.getOxygenFlowRateWithUnit(
          rate.get(),
          unit.get());

      dose.setNumerator(oxygenFlowRateWithUnit.getFirst());
      dose.setNumeratorUnit(oxygenFlowRateWithUnit.getSecond());
      dose.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    }

    return dose;
  }

  private boolean isOxygenTherapy(final MedicationInstructionInstruction instruction)
  {
    return instruction.getOrder().get(0).getAdministrationDetails().getOxygenDelivery().size() == 1;
  }

  private void setAdministrationWitness(final AdministrationDto administration, final Participation participation)
  {
    final PartyIdentified partyIdentified = (PartyIdentified)participation.getPerformer();
    final String witnessId = partyIdentified.getIdentifiers().get(0).getId();
    final String witnessName = partyIdentified.getName();
    final NamedExternalDto witnessIdentityDto = new NamedExternalDto(witnessId, witnessName);

    administration.setWitness(witnessIdentityDto);
  }

  private boolean isRateAdministration(final AdministrationTypeEnum type, final AdministrationDetailsCluster details)
  {
    final boolean hasDoseAdministrationRate = details != null
        && !details.getInfusionAdministrationDetails().isEmpty()
        && details.getInfusionAdministrationDetails().get(0).getDoseAdministrationRate() instanceof DvQuantity;

    return hasDoseAdministrationRate && type != AdministrationTypeEnum.BOLUS;
  }

  private Pair<AdministrationTypeEnum, CodedNameDto> getReasonValuesFromMedicationAction(final MedicationActionAction action)
  {
    final List<DvText> reason = action.getReason();
    if (reason.isEmpty())
    {
      return null;
    }

    AdministrationTypeEnum administrationTypeEnum = null;
    final CodedNameDto notAdministeredReason = new CodedNameDto(null, null);

    for (final DvText dvText : reason)
    {
      if (AdministrationTypeEnum.getByFullString(dvText.getValue()) != null)
      {
        administrationTypeEnum = AdministrationTypeEnum.getByFullString(dvText.getValue());
      }
      else
      {
        if (dvText instanceof DvCodedText)
        {
          final DvCodedText codedReason = (DvCodedText)dvText;
          notAdministeredReason.setCode(codedReason.getDefiningCode().getCodeString());
          notAdministeredReason.setName(codedReason.getValue());
        }
      }
    }
    return Pair.of(administrationTypeEnum, notAdministeredReason);
  }
}
