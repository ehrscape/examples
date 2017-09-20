package com.marand.thinkmed.medications.administration.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.ispek.common.Dictionary;
import com.marand.ispek.ehr.common.tdo.IspekComposition;
import com.marand.maf.core.EnumUtils;
import com.marand.maf.core.openehr.visitor.IspekTdoDataSupport;
import com.marand.maf.core.openehr.visitor.TdoPopulatingVisitor;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster.InfusionAdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster.OxygenDeliveryCluster;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster.OxygenDeliveryCluster.AmbientOxygenCluster;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster.OxygenDeliveryCluster.DeviceCluster;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationAdministrationComposition;
import com.marand.openehr.medications.tdo.MedicationAdministrationComposition.MedicationDetailSection.ClinicalInterventionAction;
import com.marand.openehr.medications.tdo.MedicationAdministrationComposition.MedicationDetailSection.ClinicalInterventionAction.InfusionBagAmountCluster;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.AdministrationResultEnum;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.ClinicalInterventionEnum;
import com.marand.thinkmed.medications.EhrTerminologyEnum;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.ParticipationTypeEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.administration.AdministrationToEhrConverter;
import com.marand.thinkmed.medications.administration.AdministrationUtils;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.business.util.TherapyIdUtils;
import com.marand.thinkmed.medications.converter.therapy.OxygenMedicationToEhrConverter;
import com.marand.thinkmed.medications.dto.CodedNameDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.OxygenStartingDevice;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.DoseAdministration;
import com.marand.thinkmed.medications.dto.administration.InfusionBagDto;
import com.marand.thinkmed.medications.dto.administration.InfusionSetChangeDto;
import com.marand.thinkmed.medications.dto.administration.OxygenAdministration;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.mapper.InstructionToAdministrationMapper;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.openehr.jaxb.rm.Action;
import org.openehr.jaxb.rm.DvDate;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.InstructionDetails;
import org.openehr.jaxb.rm.IsmTransition;
import org.openehr.jaxb.rm.LocatableRef;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Nejc Korasa
 */
public class AdministrationToEhrConverterImpl implements AdministrationToEhrConverter
{
  private AdministrationUtils administrationUtils;

  @Required
  public void setAdministrationUtils(final AdministrationUtils administrationUtils)
  {
    this.administrationUtils = administrationUtils;
  }

  @Override
  public MedicationAdministrationComposition buildMedicationAdministrationComposition(
      @Nonnull final MedicationOrderComposition composition,
      @Nonnull final AdministrationDto administration,
      @Nonnull final MedicationActionEnum actionEnum,
      @Nonnull final String composerName,
      @Nonnull final String composerId,
      final String centralCaseId,
      final String careProviderId,
      @Nonnull final DateTime when)
  {
    Preconditions.checkNotNull(composition, "composition");
    Preconditions.checkNotNull(administration, "administration");
    Preconditions.checkNotNull(actionEnum, "actionEnum");
    Preconditions.checkNotNull(composerName, "composerName");
    Preconditions.checkNotNull(composerId, "composerId");
    Preconditions.checkNotNull(when, "when");

    final MedicationInstructionInstruction instruction = composition.getMedicationDetail().getMedicationInstruction().get(0);
    final MedicationAdministrationComposition administrationComposition = new MedicationAdministrationComposition();

    InstructionToAdministrationMapper.map(instruction, administrationComposition);
    updateAdministrationComposition(administrationComposition, administration, actionEnum);
    linkActionsToInstructions(composition, administrationComposition.getMedicationDetail().getMedicationAction());

    addAdministrationContext(
        administrationComposition,
        composerName,
        composerId,
        centralCaseId,
        careProviderId,
        administration.getWitness(),
        when);

    return administrationComposition;
  }

  @Override
  public MedicationAdministrationComposition buildSetChangeAdministrationComposition(
      @Nonnull final MedicationOrderComposition composition,
      @Nonnull final InfusionSetChangeDto administration,
      @Nonnull final String composerName,
      @Nonnull final String composerId,
      final String centralCaseId,
      final String careProviderId,
      @Nonnull final Locale locale,
      @Nonnull final DateTime when)
  {
    Preconditions.checkNotNull(composition, "composition");
    Preconditions.checkNotNull(administration, "administrationDto");
    Preconditions.checkNotNull(composerName, "composerName");
    Preconditions.checkNotNull(composerId, "composerId");
    Preconditions.checkNotNull(when, "when");
    Preconditions.checkNotNull(locale, "locale");

    final MedicationAdministrationComposition administrationComposition = new MedicationAdministrationComposition();
    administrationComposition.setMedicationDetail(new MedicationAdministrationComposition.MedicationDetailSection());

    final ClinicalInterventionAction clinicalInterventionAction = new ClinicalInterventionAction();
    administrationComposition.getMedicationDetail().getClinicalIntervention().add(clinicalInterventionAction);

    final String translatedIntervention = Dictionary.getEntry(EnumUtils.getIdentifier(administration.getInfusionSetChangeEnum()), locale);

    clinicalInterventionAction.setIntervention(
        DataValueUtils.getCodedText(
            EhrTerminologyEnum.PK_NANDA.getEhrName(),
            administration.getInfusionSetChangeEnum().getCode(),
            translatedIntervention));

    setInfusionBagToClinicalIntervention(administration, clinicalInterventionAction);

    clinicalInterventionAction.setInterventionUnsuccessful(DataValueUtils.getBoolean(false));
    clinicalInterventionAction.setTime(DataValueUtils.getDateTime(administration.getAdministrationTime()));
    clinicalInterventionAction.setComments(DataValueUtils.getText(administration.getComment()));

    setClinicalInterventionTransition(clinicalInterventionAction);

    linkActionsToInstructions(composition, administrationComposition.getMedicationDetail().getClinicalIntervention());

    addAdministrationContext(
        administrationComposition,
        composerName,
        composerId,
        centralCaseId,
        careProviderId,
        null,
        when);

    return administrationComposition;
  }

  private void linkActionsToInstructions(final MedicationOrderComposition composition, final List<? extends Action> actions)
  {
    final MedicationInstructionInstruction instruction = composition.getMedicationDetail().getMedicationInstruction().get(0);
    final String compositionUid =
        TherapyIdUtils.getCompositionUidWithoutVersion(composition.getUid().getValue());
    final LocatableRef actionInstructionId =
        MedicationsEhrUtils.createInstructionLocatableRef(composition);

    for (final Action action : actions)
    {
      action.setInstructionDetails(new InstructionDetails());
      action.getInstructionDetails().setActivityId("activities[at0001] ");
      action.getInstructionDetails().setInstructionId(actionInstructionId);
      MedicationsEhrUtils.fillActionInstructionId(actionInstructionId, composition, instruction, compositionUid);
    }
  }

  private void updateAdministrationComposition(
      final MedicationAdministrationComposition administrationComposition,
      final AdministrationDto administrationDto,
      final MedicationActionEnum medicationActionEnum)
  {
    final MedicationActionAction medicationAction =
        administrationComposition.getMedicationDetail().getMedicationAction().get(0);
    medicationAction.setTime(DataValueUtils.getDateTime(administrationDto.getAdministrationTime()));

    final AdministrationResultEnum resultEnum = administrationDto.getAdministrationResult();
    if (resultEnum == AdministrationResultEnum.SELF_ADMINISTERED)
    {
      medicationAction.setSelfAdministrationTypeEnum(administrationDto.getSelfAdministrationType());
    }

    final AdministrationTypeEnum administrationType = administrationDto.getAdministrationType();
    setMedicationActionReason(
        resultEnum,
        administrationDto.getNotAdministeredReason(),
        administrationType,
        medicationAction);

    if (administrationDto.getExpiryDate() != null)
    {
      final DvDate dvExpiryDate = new DvDate();
      dvExpiryDate.setValue(ISODateTimeFormat.date().print(administrationDto.getExpiryDate()));
      medicationAction.setExpiryDate(dvExpiryDate);
    }
    if (administrationDto.getBatchId() != null)
    {
      medicationAction.setBatchID(DataValueUtils.getText(administrationDto.getBatchId()));
    }

    // for simple therapies user can select substitute medication
    if (administrationType == AdministrationTypeEnum.START || administrationType == AdministrationTypeEnum.BOLUS)
    {
      final MedicationDto substituteMedication =
          administrationDto instanceof StartAdministrationDto
          ? ((StartAdministrationDto)administrationDto).getSubstituteMedication()
          : null;

      if (substituteMedication != null)
      {
        if (medicationAction.getIngredientsAndForm() != null &&
            !medicationAction.getIngredientsAndForm().getIngredient().isEmpty())
        {
          throw new IllegalArgumentException("Substitute medication can only be set for simple therapies");
        }
        medicationAction.setMedicine(
            DataValueUtils.getLocalCodedText(String.valueOf(substituteMedication.getId()), substituteMedication.getName()));
        medicationAction.setBrandSubstituted(DataValueUtils.getBoolean(true));
      }
    }

    if (administrationDto instanceof OxygenAdministration)
    {
      fillActionForOxygenTherapy(administrationDto, medicationAction);
    }

    else if (administrationType == AdministrationTypeEnum.START
        || administrationType == AdministrationTypeEnum.BOLUS
        || administrationType == AdministrationTypeEnum.ADJUST_INFUSION)
    {
      final TherapyDoseDto therapyDose = administrationUtils.getTherapyDose(administrationDto);
      if (therapyDose != null && therapyDose.getNumerator() != null)
      {
        if (therapyDose.getTherapyDoseTypeEnum() == TherapyDoseTypeEnum.QUANTITY)
        {
          medicationAction.setStructuredDose(MedicationsEhrUtils.buildStructuredDose(
              therapyDose.getNumerator(),
              therapyDose.getNumeratorUnit(),
              therapyDose.getDenominator(),
              therapyDose.getDenominatorUnit(),
              null,
              null));
        }
        else if (therapyDose.getTherapyDoseTypeEnum() == TherapyDoseTypeEnum.RATE)
        {
          final InfusionAdministrationDetailsCluster infusionDetails = getInfusionDetails(therapyDose);
          if (!medicationAction.getAdministrationDetails().getInfusionAdministrationDetails().isEmpty())
          {
            infusionDetails.setPurposeEnum(
                medicationAction.getAdministrationDetails().getInfusionAdministrationDetails().get(0).getPurposeEnum());
          }
          medicationAction.getAdministrationDetails().getInfusionAdministrationDetails().clear();
          medicationAction.getAdministrationDetails().getInfusionAdministrationDetails().add(infusionDetails);
        }
        else if (therapyDose.getTherapyDoseTypeEnum() == TherapyDoseTypeEnum.VOLUME_SUM)
        {
          medicationAction.setStructuredDose(MedicationsEhrUtils.buildStructuredDose(
              therapyDose.getNumerator(),
              therapyDose.getNumeratorUnit()));
        }
      }
    }

    if (administrationType == AdministrationTypeEnum.START && administrationUtils.getInfusionBagDto(administrationDto) != null)
    {
      final ClinicalInterventionAction clinicalInterventionAction = new ClinicalInterventionAction();
      administrationComposition.getMedicationDetail().getClinicalIntervention().add(clinicalInterventionAction);

      setInfusionBagToClinicalIntervention(administrationDto, clinicalInterventionAction);

      clinicalInterventionAction.setTime(DataValueUtils.getDateTime(administrationDto.getAdministrationTime()));
      clinicalInterventionAction.setInterventionUnsuccessful(DataValueUtils.getBoolean(false));
      setClinicalInterventionTransition(clinicalInterventionAction);
    }

    final MedicationRouteDto route = administrationDto.getRoute();
    if (route != null)
    {
      medicationAction.getAdministrationDetails()
          .setRoute(Collections.singletonList(DataValueUtils.getLocalCodedText(
              String.valueOf(route.getId()),
              route.getName())));
    }

    if (!StringUtils.isEmpty(administrationDto.getComment()))
    {
      medicationAction.setComment(DataValueUtils.getText(administrationDto.getComment()));
    }

    final IsmTransition ismTransition = new IsmTransition();
    ismTransition.setCareflowStep(medicationActionEnum.getCareflowStep());
    ismTransition.setCurrentState(medicationActionEnum.getCurrentState());
    medicationAction.setIsmTransition(ismTransition);
  }

  private void fillActionForOxygenTherapy(final AdministrationDto administration, final MedicationActionAction action)
  {
    if (AdministrationResultEnum.ADMINISTERED.contains(administration.getAdministrationResult()))
    {
      final OxygenStartingDevice device = ((OxygenAdministration)administration).getStartingDevice();
      final TherapyDoseDto dose = ((DoseAdministration)administration).getAdministeredDose();

      if (dose == null && device == null)
      {
        throw new IllegalArgumentException("Either oxygen device or therapy dose must be set when administering oxygen!");
      }

      final OxygenDeliveryCluster oxygenDelivery = new OxygenDeliveryCluster();
      action.getAdministrationDetails().setOxygenDelivery(Collections.singletonList(oxygenDelivery));

      setOxygenFlowRate(dose, oxygenDelivery);
      setOxygenDevice(device, oxygenDelivery);
    }
  }

  private void setOxygenDevice(final OxygenStartingDevice device, final OxygenDeliveryCluster oxygenDelivery)
  {
    if (device != null)
    {
      oxygenDelivery.setRouteEnum(device.getRoute());
      if (device.getRouteType() != null)
      {
        final DeviceCluster deviceCluster = new DeviceCluster();
        deviceCluster.setDeviceName(DataValueUtils.getText(device.getRoute().getTerm().getDescription()));
        deviceCluster.setType(DataValueUtils.getText(device.getRouteType()));
        oxygenDelivery.getDevice().add(deviceCluster);
      }
    }
  }

  private void setOxygenFlowRate(final TherapyDoseDto dose, final OxygenDeliveryCluster oxygenDelivery)
  {
    if (dose != null)
    {
      final AmbientOxygenCluster ambientOxygenCluster = new AmbientOxygenCluster();
      oxygenDelivery.setAmbientOxygen(ambientOxygenCluster);

      ambientOxygenCluster.setOxygenFlowRate(OxygenMedicationToEhrConverter.getOxygenFlowRate(
          dose.getNumerator(),
          dose.getNumeratorUnit()));
    }
  }

  private void setMedicationActionReason(
      final AdministrationResultEnum administrationResultEnum,
      final CodedNameDto administrationNotGivenReason,
      final AdministrationTypeEnum administrationTypeEnum,
      final MedicationActionAction medicationAction)
  {
    final List<DvText> dvTextList = new ArrayList<>();

    dvTextList.add(DataValueUtils.getText(AdministrationTypeEnum.getFullString(administrationTypeEnum)));
    if (administrationResultEnum == AdministrationResultEnum.DEFER || administrationResultEnum == AdministrationResultEnum.NOT_GIVEN)
    {
      dvTextList.add(DataValueUtils.getLocalCodedText(
          administrationNotGivenReason.getCode(),
          administrationNotGivenReason.getName()));
    }
    medicationAction.setReason(dvTextList);
  }

  private void addAdministrationContext(
      final IspekComposition medicationOrder,
      final String composerName,
      final String composerId,
      final String centralCaseId,
      final String careProviderId,
      final NamedExternalDto administrationWitness,
      final DateTime when)
  {
    MedicationsEhrUtils.addContext(medicationOrder, centralCaseId, careProviderId, when);

    final TdoPopulatingVisitor.DataContext dataContext =
        TdoPopulatingVisitor.getSloveneContext(when)
            .withReplaceParticipation(false)
            .withCompositionComposer(IspekTdoDataSupport.getPartyIdentified(composerName, composerId));

    MedicationsEhrUtils.setContextParticipation(dataContext, administrationWitness, ParticipationTypeEnum.WITNESS);
    new TdoPopulatingVisitor().visitBean(medicationOrder, dataContext);
  }

  private InfusionAdministrationDetailsCluster getInfusionDetails(final TherapyDoseDto therapyDose)
  {
    return MedicationsEhrUtils.getInfusionDetails(
        false,
        null,
        therapyDose.getNumerator(),
        therapyDose.getNumeratorUnit(),
        therapyDose.getDenominator(),
        therapyDose.getDenominatorUnit());
  }

  private void setInfusionBagToClinicalIntervention(
      final AdministrationDto administrationDto,
      final ClinicalInterventionAction clinicalIntervention)
  {
    final InfusionBagDto infusionBagDto = administrationUtils.getInfusionBagDto(administrationDto);
    if (infusionBagDto != null)
    {
      final InfusionBagAmountCluster bagChangeDose = new InfusionBagAmountCluster();
      final Double infusionBagQuantity = infusionBagDto.getQuantity();
      final String infusionBagUnit = infusionBagDto.getUnit();

      bagChangeDose.setQuantity(DataValueUtils.getQuantity(infusionBagQuantity, ""));
      bagChangeDose.setDoseUnit(DataValueUtils.getLocalCodedText(infusionBagUnit, infusionBagUnit));

      clinicalIntervention.setInfusionBagAmount(bagChangeDose);
      clinicalIntervention.setTime(DataValueUtils.getDateTime(administrationDto.getAdministrationTime()));
      clinicalIntervention.setComments(DataValueUtils.getText(administrationDto.getComment()));
    }
  }

  private void setClinicalInterventionTransition(final ClinicalInterventionAction clinicalInterventionAction)
  {
    final IsmTransition ismTransition = new IsmTransition();
    ismTransition.setCareflowStep(ClinicalInterventionEnum.COMPLETED.getCareflowStep());
    ismTransition.setCurrentState(ClinicalInterventionEnum.COMPLETED.getCurrentState());
    clinicalInterventionAction.setIsmTransition(ismTransition);
  }
}
