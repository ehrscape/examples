package com.marand.thinkmed.fdb.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import com.marand.maf.core.Pair;
import com.marand.maf.core.time.Intervals;
import com.marand.thinkmed.fdb.dif.FdbDataManagerFactory;
import com.marand.thinkmed.fdb.dto.DrugDosingInfo;
import com.marand.thinkmed.fdb.dto.TherapyInfo;
import com.marand.thinkmed.fdb.service.DifService;
import com.marand.thinkmed.medicationsexternal.dto.DoseRangeCheckDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationForWarningsSearchDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationsWarningDto;
import com.marand.thinkmed.medicationsexternal.dto.UnitValueDto;
import com.marand.thinkmed.medicationsexternal.plugin.MedicationExternalDataPlugin;
import firstdatabank.database.FDBException;
import firstdatabank.dif.DDCMScreenResult;
import firstdatabank.dif.DDCMScreenResults;
import firstdatabank.dif.DDIMScreenResult;
import firstdatabank.dif.DDIMScreenResults;
import firstdatabank.dif.DOSEScreenResult;
import firstdatabank.dif.DOSEScreenResults;
import firstdatabank.dif.DRCLookupResult;
import firstdatabank.dif.DRCLookupResults;
import firstdatabank.dif.DTScreenDrugItem;
import firstdatabank.dif.DTScreenDrugItems;
import firstdatabank.dif.DTScreenResult;
import firstdatabank.dif.DTScreenResults;
import firstdatabank.dif.DispensableDrug;
import firstdatabank.dif.FDBCode;
import firstdatabank.dif.FDBDDCMSeverityFilter;
import firstdatabank.dif.FDBDOSEStatus;
import firstdatabank.dif.FDBMessageSeverity;
import firstdatabank.dif.FDBMonographFormat;
import firstdatabank.dif.FDBMonographSource;
import firstdatabank.dif.FDBMonographType;
import firstdatabank.dif.LACTScreenResult;
import firstdatabank.dif.LACTScreenResults;
import firstdatabank.dif.Monograph;
import firstdatabank.dif.NeoDoseScreenResult;
import firstdatabank.dif.NeoDoseScreenResults;
import firstdatabank.dif.PEDIScreenResult;
import firstdatabank.dif.PEDIScreenResults;
import firstdatabank.dif.PREGScreenResult;
import firstdatabank.dif.PREGScreenResults;
import firstdatabank.dif.SIDEScreenResult;
import firstdatabank.dif.SIDEScreenResults;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Bostjan Vester
 * @author Mitja Lapajne
 */
public class FdbDataProviderPlugin implements MedicationExternalDataPlugin
{
  private DifService difService;

  public void setDifService(final DifService difService)
  {
    this.difService = difService;
  }

  @Override
  public void reloadCache()
  {
  }

  @Override
  public List<DoseRangeCheckDto> findDoseRangeChecks(final String externalId)
  {
    try
    {
      final List<DoseRangeCheckDto> result = new ArrayList<DoseRangeCheckDto>();
      final long fdbGcnSeqNo = Long.parseLong(externalId);
      final DispensableDrug dispensableDrug = difService.getDispensableDrugByGcnSeqNo(fdbGcnSeqNo);
      final DRCLookupResults lookupResults = dispensableDrug.getDRCDosing("", "", "", 0L);
      for (int i = 0; i < lookupResults.count(); i++)
      {
        final DRCLookupResult lookupResult = lookupResults.item(i);
        final DoseRangeCheckDto doseRangeCheck = new DoseRangeCheckDto();
        doseRangeCheck.setAgeFrom(lookupResult.getAgeInDaysLow());
        doseRangeCheck.setAgeTo(lookupResult.getAgeInDaysHigh());
        doseRangeCheck.setRoute(difService.getDoseRoute(lookupResult.getDoseRouteID()).getDescription());
        doseRangeCheck.setDoseLow(
            new UnitValueDto<Double, String>(
                lookupResult.getDoseLow(),
                translateUnit(lookupResult.getDoseLowUnit())));
        doseRangeCheck.setDoseHigh(
            new UnitValueDto<Double, String>(
                lookupResult.getDoseHigh(),
                translateUnit(lookupResult.getDoseHighUnit())));
        doseRangeCheck.setMaxDailyDose(
            new UnitValueDto<Double, String>(
                lookupResult.getMaxDailyDose(),
                translateUnit(lookupResult.getMaxDailyDoseUnit())));
        doseRangeCheck.setIndicationDescription(lookupResult.getIndicationDescription());
        result.add(doseRangeCheck);
      }
      return result;
    }
    catch (final FDBException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private String translateUnit(final String unit)
  {
    final StringBuffer sb = new StringBuffer();

    for (final String part : unit.split(" "))
    {
      sb.append(translateUnitPart(part));
    }

    return sb.toString();
  }

  private String translateUnitPart(final String unitPart)
  {
    if ("milligram".equals(unitPart))
    {
      return "mg";
    }
    if ("kilogram".equals(unitPart))
    {
      return "kg";
    }
    if ("microgram".equals(unitPart))
    {
      return "μg";
    }
    if ("gram".equals(unitPart))
    {
      return "g";
    }
    if ("hour".equals(unitPart))
    {
      return "h";
    }
    if ("per".equals(unitPart))
    {
      return "/";
    }
    if ("day".equals(unitPart))
    {
      return "d";
    }
    return unitPart;
  }

  @Override
  public List<MedicationsWarningDto> findMedicationWarnings(
      final long patientAgeInDays,
      final Double patientWeightInKg,
      final Integer gabInWeeks,
      final Double bsaInM2,
      final boolean isFemale,
      final List<String> diseaseTypeCodes,   //todo this codes are now icd10 ... fdb expects icd9
      final List<String> allergiesExternalValues,
      final List<MedicationForWarningsSearchDto> medicationSummaries)
  {
    final List<MedicationsWarningDto> warnings = new ArrayList<>();
    try
    {
      final MedicationWarningRequest medicationWarningRequest = new MedicationWarningRequest(
          patientAgeInDays,
          patientWeightInKg,
          gabInWeeks,
          bsaInM2,
          diseaseTypeCodes,
          medicationSummaries);

      final MedicationsWarningDto drugInteractionsWarning = findDrugToDrugWarnings(medicationWarningRequest);
      if (drugInteractionsWarning != null)
      {
        warnings.add(drugInteractionsWarning);
      }

      if (medicationWarningRequest.getTherapyInfo().getWeightInKg() != null)
      {
        final MedicationsWarningDto dosingWarning = findDosingWarnings(medicationWarningRequest);
        if (dosingWarning != null)
        {
          warnings.add(dosingWarning);
        }
      }

      final MedicationsWarningDto sideEffectsWarnings = findSideEffectsWarnings(medicationWarningRequest);
      if (sideEffectsWarnings != null)
      {
        warnings.add(sideEffectsWarnings);
      }

      final MedicationsWarningDto duplicateTherapyWarning = findDuplicateTherapyWarnings(medicationWarningRequest);
      if (duplicateTherapyWarning != null)
      {
        warnings.add(duplicateTherapyWarning);
      }

      final MedicationsWarningDto pediatricWarning = findPediatricWarnings(medicationWarningRequest);
      if (pediatricWarning != null)
      {
        warnings.add(pediatricWarning);
      }

      if (medicationWarningRequest.getTherapyInfo().getGabInWeeks() != null &&
          medicationWarningRequest.getTherapyInfo().getWeightInKg() != null)
      {
        final MedicationsWarningDto neoDoseWarning = findNeoDoseWarnings(medicationWarningRequest);
        if (neoDoseWarning != null)
        {
          warnings.add(neoDoseWarning);
        }
      }

      final MedicationsWarningDto contraindicationsWarning = findContraindicationsWarnings(medicationWarningRequest);
      if (contraindicationsWarning != null)
      {
        warnings.add(contraindicationsWarning);
      }

      final MedicationsWarningDto pregnancyWarning = findPregnancyWarnings(medicationWarningRequest);
      if (pregnancyWarning != null)
      {
        warnings.add(pregnancyWarning);
      }

      final MedicationsWarningDto lactationWarning = findLactationWarnings(medicationWarningRequest);
      if (lactationWarning != null)
      {
        warnings.add(lactationWarning);
      }
    }
    catch (final Exception ex)
    {
      warnings.add(createWarning("UNABLE TO PERFORM WARNING SCAN: " + ex.getMessage()));
    }
    return warnings;
  }

  private MedicationsWarningDto findDrugToDrugWarnings(final MedicationWarningRequest request) throws FDBException
  {
    final DDIMScreenResults ddimScreenResults = difService.ddimScreen(request.getTherapyInfo());
    if (ddimScreenResults.count() > 0)
    {
      final MedicationsWarningDto drugInteractionsWarning = new MedicationsWarningDto();
      drugInteractionsWarning.setDescription("Drug Interactions");
      for (int i = 0; i < ddimScreenResults.count(); i++)
      {
        final DDIMScreenResult screenResult = ddimScreenResults.item(i);
        addWarning(drugInteractionsWarning, screenResult, request);
      }
      return drugInteractionsWarning;
    }
    return null;
  }

  private void addWarning(
      final MedicationsWarningDto parent,
      final DDIMScreenResult screenResult,
      final MedicationWarningRequest request) throws FDBException
  {
    final MedicationsWarningDto mainWarning = createWarning(screenResult.getInteractionDescription());
    final MedicationsWarningDto.Severity severity = getSeverityFromSeverityLevelCode(screenResult.getSeverityLevelCode());
    mainWarning.setSeverity(severity);
    parent.addDetail(mainWarning);
    setParentSeverity(parent, severity);

    mainWarning.addDetail(createDrugNameWarning(screenResult.getDrug1Index(), request));
    mainWarning.addDetail(createDrugNameWarning(screenResult.getDrug2Index(), request));

    if (StringUtils.isNotEmpty(screenResult.getClinicalEffectCode1()))
    {
      final FDBCode fdbCode = new FDBCode(FdbDataManagerFactory.getManager());
      fdbCode.load(15, screenResult.getClinicalEffectCode1());
      mainWarning.addDetail(createWarning("Clinical Effect Code 1: " + fdbCode.getDescription()));
    }
    if (StringUtils.isNotEmpty(screenResult.getClinicalEffectCode2()))
    {
      final FDBCode fdbCode = new FDBCode(FdbDataManagerFactory.getManager());
      fdbCode.load(15, screenResult.getClinicalEffectCode2());
      mainWarning.addDetail(createWarning("Clinical Effect Code 2: " + fdbCode.getDescription()));
    }
    if (screenResult.getMonographID() > 0L)
    {
      final Monograph monograph = new Monograph(FdbDataManagerFactory.getManager());
      monograph.load(FDBMonographType.fdbMTDDIM, "FDB-P", screenResult.getMonographID(), FDBMonographSource.fdbMSFDBOnly);
      final String monographHtml = monograph.getFormattedText(FDBMonographFormat.fdbMFHTML);
      mainWarning.addDetail(createWarning("Monograph ", monographHtml));
    }
  }

  private MedicationsWarningDto findPregnancyWarnings(final MedicationWarningRequest request) throws FDBException
  {
    final PREGScreenResults pregScreenResults = difService.pregScreen(request.getTherapyInfo());
    if (pregScreenResults.count() > 0)
    {
      final MedicationsWarningDto duplicateIngredientWarning = new MedicationsWarningDto();
      duplicateIngredientWarning.setDescription("Pregnancy");
      for (int i = 0; i < pregScreenResults.count(); i++)
      {
        final PREGScreenResult screenResult = pregScreenResults.item(i);
        addWarning(duplicateIngredientWarning, screenResult, request);
      }
      return duplicateIngredientWarning;
    }
    return null;
  }

  private void addWarning(
      final MedicationsWarningDto parent,
      final PREGScreenResult screenResult,
      final MedicationWarningRequest request)
  {
    final MedicationsWarningDto mainWarning = createDrugNameWarning(screenResult.getDrugIndex(), request);
    parent.addDetail(mainWarning);

    final MedicationsWarningDto.Severity severity =
        getSeverityFromSignificanceLevelCode(screenResult.getSignificanceLevelCode());
    mainWarning.setSeverity(severity);
    setParentSeverity(parent, severity);

    if (StringUtils.isNotEmpty(screenResult.getScreenMessage()))
    {
      mainWarning.addDetail(createWarning("Description: " + screenResult.getScreenMessage()));
    }
    if (StringUtils.isNotEmpty(screenResult.getAddComment()))
    {
      mainWarning.addDetail(createWarning("Additional Comment: " + screenResult.getAddComment()));
    }
    if (StringUtils.isNotEmpty(screenResult.getSignificanceLevelCodeFullDescription()))
    {
      mainWarning.addDetail(createWarning("Significance Level: " + screenResult.getSignificanceLevelCodeFullDescription()));
    }
  }

  private MedicationsWarningDto findLactationWarnings(final MedicationWarningRequest request) throws FDBException
  {
    final LACTScreenResults lactScreenResults = difService.lactScreen(request.getTherapyInfo());
    if (lactScreenResults.count() > 0)
    {
      final MedicationsWarningDto lactationWarning = new MedicationsWarningDto();
      lactationWarning.setDescription("Lactation");
      for (int i = 0; i < lactScreenResults.count(); i++)
      {
        final LACTScreenResult screenResult = lactScreenResults.item(i);
        addWarning(lactationWarning, screenResult, request);
      }
      return lactationWarning;
    }
    return null;
  }

  private void addWarning(
      final MedicationsWarningDto parent,
      final LACTScreenResult screenResult,
      final MedicationWarningRequest request) throws FDBException
  {
    final MedicationsWarningDto mainWarning = createDrugNameWarning(screenResult.getDrugIndex(), request);
    parent.addDetail(mainWarning);

    final MedicationsWarningDto.Severity severity = getSeverityFromSeverityLevelCode(screenResult.getSeverityLevelCode());
    mainWarning.setSeverity(severity);

    setParentSeverity(parent, severity);

    if (StringUtils.isNotEmpty(screenResult.getScreenMessage()))
    {
      mainWarning.addDetail(createWarning("Description: " + screenResult.getScreenMessage()));
    }
    if (StringUtils.isNotEmpty(screenResult.getAddComment()))
    {
      mainWarning.addDetail(createWarning("Additional Comment: " + screenResult.getAddComment()));
    }
    if (StringUtils.isNotEmpty(screenResult.getExcretionEffectCode()))
    {
      final FDBCode excretionEffect = new FDBCode(FdbDataManagerFactory.getManager());
      excretionEffect.load(90, screenResult.getExcretionEffectCode());
      mainWarning.addDetail(createWarning("Excretion Effect: " + excretionEffect.getDescription()));
    }
    if (StringUtils.isNotEmpty(screenResult.getLactationEffectCode()))
    {
      final FDBCode lactationEffect = new FDBCode(FdbDataManagerFactory.getManager());
      lactationEffect.load(91, screenResult.getExcretionEffectCode());
      mainWarning.addDetail(createWarning("Lactation Effect: " + lactationEffect.getDescription()));
    }
    if (StringUtils.isNotEmpty(screenResult.getSeverityLevelCode()))
    {
      final FDBCode severityCode = new FDBCode(FdbDataManagerFactory.getManager());
      severityCode.load(91, screenResult.getSeverityLevelCode());
      mainWarning.addDetail(createWarning("Severity level: " + severityCode.getDescription()));
    }
  }

  private MedicationsWarningDto findDosingWarnings(final MedicationWarningRequest request) throws FDBException
  {
    final DOSEScreenResults doseScreenResults = difService.doseScreen(request.getTherapyInfo());
    final MedicationsWarningDto dosingWarning = createWarning("Dosing");
    boolean warningExists = false;
    if (doseScreenResults.count() > 0)
    {
      dosingWarning.setSeverity(MedicationsWarningDto.Severity.HIGH);
      for (int i = 0; i < doseScreenResults.count(); i++)
      {
        final DOSEScreenResult screenResult = doseScreenResults.item(i);
        final boolean isWarning = addWarning(dosingWarning, screenResult, request);
        if (isWarning)
        {
          warningExists = true;
        }
      }
    }
    if (doseScreenResults.getMessages().count() > 0)
    {
      for (int i = 0; i < doseScreenResults.getMessages().count(); i++)
      {
        if (doseScreenResults.getMessages().item(i).getSeverity() != FDBMessageSeverity.fdbMSSInformation)
        {
          final String messageText = doseScreenResults.getMessages().item(i).getDrugDescription() + " - " +
              doseScreenResults.getMessages().item(i).getMessageText();
          dosingWarning.addDetail(createWarning(messageText));
          warningExists = true;
        }
      }
    }
    if (warningExists)
    {
      return dosingWarning;
    }
    return null;
  }

  private boolean addWarning(
      final MedicationsWarningDto parent,
      final DOSEScreenResult screenResult,
      final MedicationWarningRequest request)
  {
    final MedicationsWarningDto mainWarning = createDrugNameWarning(screenResult.getDrugIndex(), request);
    parent.addDetail(mainWarning);

    boolean warningExists = false;

    if (screenResult.getDailyDoseStatus() != FDBDOSEStatus.fdbDSTUnableToCheck
        && screenResult.getDailyDoseStatus() != FDBDOSEStatus.fdbDSTPassed)
    {
      mainWarning.addDetail(createWarning("Daily Dose: " + screenResult.getDailyDoseMessage()));
      warningExists = true;
    }
    if (screenResult.getDurationStatus() != FDBDOSEStatus.fdbDSTUnableToCheck
        && screenResult.getDurationStatus() != FDBDOSEStatus.fdbDSTPassed)
    {
      mainWarning.addDetail(createWarning("Duration: " + screenResult.getDurationMessage()));
      warningExists = true;
    }
    if (screenResult.getFrequencyStatus() != FDBDOSEStatus.fdbDSTUnableToCheck
        && screenResult.getFrequencyStatus() != FDBDOSEStatus.fdbDSTPassed)
    {
      mainWarning.addDetail(createWarning("Frequency: " + screenResult.getFrequencyMessage()));
      warningExists = true;
    }
    if (screenResult.getMaxDailyDoseStatus() != FDBDOSEStatus.fdbDSTUnableToCheck
        && screenResult.getMaxDailyDoseStatus() != FDBDOSEStatus.fdbDSTPassed)
    {
      mainWarning.addDetail(createWarning("Max Daily Dose: " + screenResult.getMaxDailyDoseMessage()));
      warningExists = true;
    }
    if (screenResult.getMaxLifetimeDoseStatus() != FDBDOSEStatus.fdbDSTUnableToCheck
        && screenResult.getMaxLifetimeDoseStatus() != FDBDOSEStatus.fdbDSTPassed)
    {
      mainWarning.addDetail(createWarning("Max Lifetime Dose: " + screenResult.getMaxLifetimeDoseMessage()));
      warningExists = true;
    }
    if (screenResult.getRangeDoseStatus() != FDBDOSEStatus.fdbDSTUnableToCheck
        && screenResult.getRangeDoseStatus() != FDBDOSEStatus.fdbDSTPassed)
    {
      mainWarning.addDetail(createWarning("Range Dose: " + screenResult.getRangeDoseMessage()));
      warningExists = true;
    }
    if (screenResult.getSingleDoseStatus() != FDBDOSEStatus.fdbDSTUnableToCheck
        && screenResult.getSingleDoseStatus() != FDBDOSEStatus.fdbDSTPassed)
    {
      mainWarning.addDetail(createWarning("Single Dose: " + screenResult.getSingleDoseMessage()));
      warningExists = true;
    }
    return warningExists;
  }

  private MedicationsWarningDto findDuplicateTherapyWarnings(final MedicationWarningRequest request) throws FDBException
  {
    final DTScreenResults dtScreenResults = difService.dtScreen(request.getTherapyInfo());
    if (dtScreenResults.count() > 0)
    {
      final MedicationsWarningDto duplicateTherapyWarning = new MedicationsWarningDto();
      duplicateTherapyWarning.setDescription("Duplicate Therapy");
      duplicateTherapyWarning.setSeverity(MedicationsWarningDto.Severity.HIGH);
      for (int i = 0; i < dtScreenResults.count(); i++)
      {
        final DTScreenResult screenResult = dtScreenResults.item(i);
        addWarning(duplicateTherapyWarning, screenResult, request);
      }
      return duplicateTherapyWarning;
    }
    return null;
  }

  private void addWarning(
      final MedicationsWarningDto parent,
      final DTScreenResult screenResult,
      final MedicationWarningRequest request)
  {
    final MedicationsWarningDto mainWarning = createWarning(screenResult.getClassDescription());
    parent.addDetail(mainWarning);
    final DTScreenDrugItems drugItems = screenResult.getDrugItems();
    for (int i = 0; i < drugItems.count(); i++)
    {
      final DTScreenDrugItem drugItem = drugItems.item(i);
      mainWarning.addDetail(createDrugNameWarning(drugItem.getDrugIndex(), request));
    }
    mainWarning.addDetail(createWarning(screenResult.getScreenMessage()));
  }

  private MedicationsWarningDto findContraindicationsWarnings(final MedicationWarningRequest request) throws FDBException
  {
    final List<Pair<FDBDDCMSeverityFilter, DDCMScreenResults>> ddcmScreenResults =
        difService.ddcmScreen(request.getTherapyInfo());
    if (!ddcmScreenResults.isEmpty())
    {
      final MedicationsWarningDto contraindicationsWarning = new MedicationsWarningDto();
      contraindicationsWarning.setDescription("Contraindications");
      for (final Pair<FDBDDCMSeverityFilter, DDCMScreenResults> result : ddcmScreenResults)
      {
        final DDCMScreenResults currentResults = result.getSecond();
        for (int i = 0; i < currentResults.count(); i++)
        {
          final DDCMScreenResult screenResult = currentResults.item(i);
          addWarning(contraindicationsWarning, screenResult, translate(result.getFirst()), request);
        }
      }
      return contraindicationsWarning;
    }
    return null;
  }

  private void addWarning(
      final MedicationsWarningDto parent,
      final DDCMScreenResult screenResult,
      final MedicationsWarningDto.Severity severity,
      final MedicationWarningRequest request)
  {
    final MedicationsWarningDto mainWarning = createDrugNameWarning(screenResult.getDrugIndex(), request);
    parent.addDetail(mainWarning);
    mainWarning.setSeverity(severity);
    setParentSeverity(parent, severity);

    mainWarning.addDetail(createWarning(screenResult.getScreenMessage()));
  }

  private MedicationsWarningDto findSideEffectsWarnings(final MedicationWarningRequest request) throws FDBException
  {
    final SIDEScreenResults sideScreenResults = difService.sideScreen(request.getTherapyInfo());
    if (sideScreenResults.count() > 0)
    {
      final MedicationsWarningDto dosingWarning = new MedicationsWarningDto();
      dosingWarning.setDescription("Side Effects");
      for (int i = 0; i < sideScreenResults.count(); i++)
      {
        final SIDEScreenResult screenResult = sideScreenResults.item(i);
        addWarning(dosingWarning, screenResult, request);
      }
      return dosingWarning;
    }
    return null;
  }

  private void addWarning(
      final MedicationsWarningDto parent,
      final SIDEScreenResult screenResult,
      final MedicationWarningRequest request)
  {
    final MedicationsWarningDto mainWarning = createDrugNameWarning(screenResult.getDrugIndex(), request);
    parent.addDetail(mainWarning);
    final MedicationsWarningDto.Severity severity = getSeverityFromSeverityLevelCode(screenResult.getSeverityLevelCode());
    mainWarning.setSeverity(severity);
    setParentSeverity(parent, severity);

    mainWarning.addDetail(createWarning(screenResult.getScreenMessage()));
  }

  private MedicationsWarningDto findPediatricWarnings(final MedicationWarningRequest request) throws FDBException
  {
    final PEDIScreenResults pediScreenResults = difService.pediScreen(request.getTherapyInfo());
    if (pediScreenResults.count() > 0)
    {
      final MedicationsWarningDto dosingWarning = new MedicationsWarningDto();
      dosingWarning.setDescription("Pediatric Precautions");
      for (int i = 0; i < pediScreenResults.count(); i++)
      {
        final PEDIScreenResult screenResult = pediScreenResults.item(i);
        addWarning(dosingWarning, screenResult, request);
      }
      return dosingWarning;
    }
    return null;
  }

  private void addWarning(
      final MedicationsWarningDto parent,
      final PEDIScreenResult screenResult,
      final MedicationWarningRequest request)
  {
    final MedicationsWarningDto mainWarning = createDrugNameWarning(screenResult.getDrugIndex(), request);
    parent.addDetail(mainWarning);
    final MedicationsWarningDto.Severity severity = getSeverityFromSeverityLevelCode(screenResult.getSeverityLevelCode());
    mainWarning.setSeverity(severity);
    setParentSeverity(parent, severity);

    mainWarning.addDetail(createWarning(screenResult.getScreenMessage()));
    mainWarning.addDetail(createWarning(screenResult.getAddComment()));
  }

  private MedicationsWarningDto findNeoDoseWarnings(final MedicationWarningRequest request) throws FDBException
  {
    final NeoDoseScreenResults neoDoseScreenResults = difService.neoDoseScreen(request.getTherapyInfo());
    final MedicationsWarningDto dosingWarning = createWarning("Neonatal and Infant Dosage");
    boolean warningExists = false;
    if (neoDoseScreenResults.count() > 0)
    {
      dosingWarning.setSeverity(MedicationsWarningDto.Severity.HIGH);
      for (int i = 0; i < neoDoseScreenResults.count(); i++)
      {
        final NeoDoseScreenResult screenResult = neoDoseScreenResults.item(i);
        final boolean isWarning = addWarning(dosingWarning, screenResult, request);
        if (isWarning)
        {
          warningExists = true;
        }
      }
    }
    if (neoDoseScreenResults.getMessages().count() > 0)
    {
      for (int i = 0; i < neoDoseScreenResults.getMessages().count(); i++)
      {
        if (neoDoseScreenResults.getMessages().item(i).getSeverity() != FDBMessageSeverity.fdbMSSInformation)
        {
          final String messageText = neoDoseScreenResults.getMessages().item(i).getDrugDescription() + " - " +
              neoDoseScreenResults.getMessages().item(i).getMessageText();
          dosingWarning.addDetail(createWarning(messageText));
          warningExists = true;
        }
      }
    }
    if (warningExists)
    {
      return dosingWarning;
    }
    return null;
  }

  private boolean addWarning(
      final MedicationsWarningDto parent,
      final NeoDoseScreenResult screenResult,
      final MedicationWarningRequest request)
  {
    final MedicationsWarningDto mainWarning = createDrugNameWarning(screenResult.getDrugIndex(), request);
    parent.addDetail(mainWarning);

    boolean warningExists = false;

    if (screenResult.getDailyDoseStatus() != FDBDOSEStatus.fdbDSTUnableToCheck
        && screenResult.getDailyDoseStatus() != FDBDOSEStatus.fdbDSTPassed)
    {
      mainWarning.addDetail(createWarning("Daily Dose: " + screenResult.getDailyDoseMessage()));
      warningExists = true;
    }
    if (screenResult.getDurationStatus() != FDBDOSEStatus.fdbDSTUnableToCheck
        && screenResult.getDurationStatus() != FDBDOSEStatus.fdbDSTPassed)
    {
      mainWarning.addDetail(createWarning("Duration: " + screenResult.getDurationMessage()));
      warningExists = true;
    }
    if (screenResult.getFrequencyStatus() != FDBDOSEStatus.fdbDSTUnableToCheck
        && screenResult.getFrequencyStatus() != FDBDOSEStatus.fdbDSTPassed)
    {
      mainWarning.addDetail(createWarning("Frequency: " + screenResult.getFrequencyMessage()));
      warningExists = true;
    }
    if (screenResult.getMaxDailyDoseStatus() != FDBDOSEStatus.fdbDSTUnableToCheck
        && screenResult.getMaxDailyDoseStatus() != FDBDOSEStatus.fdbDSTPassed)
    {
      mainWarning.addDetail(createWarning("Max Daily Dose: " + screenResult.getMaxDailyDoseMessage()));
      warningExists = true;
    }
    if (screenResult.getMaxLifetimeDoseStatus() != FDBDOSEStatus.fdbDSTUnableToCheck
        && screenResult.getMaxLifetimeDoseStatus() != FDBDOSEStatus.fdbDSTPassed)
    {
      mainWarning.addDetail(createWarning("Max Lifetime Dose: " + screenResult.getMaxLifetimeDoseMessage()));
      warningExists = true;
    }
    if (screenResult.getRangeDoseStatus() != FDBDOSEStatus.fdbDSTUnableToCheck
        && screenResult.getRangeDoseStatus() != FDBDOSEStatus.fdbDSTPassed)
    {
      mainWarning.addDetail(createWarning("Range Dose: " + screenResult.getRangeDoseMessage()));
      warningExists = true;
    }
    if (screenResult.getSingleDoseStatus() != FDBDOSEStatus.fdbDSTUnableToCheck
        && screenResult.getSingleDoseStatus() != FDBDOSEStatus.fdbDSTPassed)
    {
      mainWarning.addDetail(createWarning("Single Dose: " + screenResult.getSingleDoseMessage()));
      warningExists = true;
    }
    return warningExists;
  }

  private void setParentSeverity(final MedicationsWarningDto parent, final MedicationsWarningDto.Severity severity)
  {
    final MedicationsWarningDto.Severity parentSeverity = parent.getSeverity();
    if (parentSeverity == null || (severity != null) && (severity.compareTo(parentSeverity) > 0))
    {
      parent.setSeverity(severity);
    }
  }

  private MedicationsWarningDto.Severity translate(final FDBDDCMSeverityFilter fdbSeverity)
  {
    if (fdbSeverity == null)
    {
      return null;
    }
    if (fdbSeverity == FDBDDCMSeverityFilter.fdbDDCMSFContraindicated)
    {
      return MedicationsWarningDto.Severity.HIGH;
    }
    if (fdbSeverity == FDBDDCMSeverityFilter.fdbDDCMSFExtremeCaution)
    {
      return MedicationsWarningDto.Severity.MEDIUM;
    }
    if (fdbSeverity == FDBDDCMSeverityFilter.fdbDDCMSFWarning)
    {
      return MedicationsWarningDto.Severity.LOW;
    }
    throw new IllegalArgumentException("Unknown FDB severity: " + fdbSeverity);
  }

  private MedicationsWarningDto createDrugNameWarning(
      final int drugIndex,
      final MedicationWarningRequest request)
  {
    final MedicationForWarningsSearchDto drugSummary = request.getSummary(drugIndex);
    return createWarning(drugSummary.getDescription());
  }

  private MedicationsWarningDto createWarning(final String description)
  {
    return createWarning(description, null);
  }

  private MedicationsWarningDto createWarning(final String description, final String longDescription)
  {
    final MedicationsWarningDto drugWarning = new MedicationsWarningDto();
    drugWarning.setDescription(description);
    drugWarning.setLongDescription(longDescription);
    return drugWarning;
  }

  private MedicationsWarningDto.Severity getSeverityFromSeverityLevelCode(final String severityLevelCode)
  {
    if ("1".equals(severityLevelCode))
    {
      return MedicationsWarningDto.Severity.HIGH;
    }
    if ("2".equals(severityLevelCode))
    {
      return MedicationsWarningDto.Severity.MEDIUM;
    }
    if ("3".equals(severityLevelCode))
    {
      return MedicationsWarningDto.Severity.LOW;
    }
    return null;
  }

  private MedicationsWarningDto.Severity getSeverityFromSignificanceLevelCode(final String significanceLevelCode)
  {
    if ("1".equals(significanceLevelCode) || "X".equals(significanceLevelCode) || "D".equals(significanceLevelCode))
    {
      return MedicationsWarningDto.Severity.HIGH;
    }
    if ("2".equals(significanceLevelCode) || "C".equals(significanceLevelCode))
    {
      return MedicationsWarningDto.Severity.MEDIUM;
    }
    if ("2".equals(significanceLevelCode) || "B".equals(significanceLevelCode) || "A".equals(significanceLevelCode))
    {
      return MedicationsWarningDto.Severity.MEDIUM;
    }
    return null;
  }

  @Override
  public boolean isWarningsProvider()
  {
    return true;
  }

  @Override
  public boolean isMedicationOverviewProvider()
  {
    return true;
  }

  @Override
  public boolean isDoseRangeChecksProvider()
  {
    return true;
  }

  private class MedicationWarningRequest
  {
    private final TherapyInfo therapyInfo;
    private final Map<DispensableDrug, MedicationForWarningsSearchDto> mappedDrugs;

    private MedicationWarningRequest(
        final long patientAgeInDays,
        @Nullable final Double patientWeightInKg,
        @Nullable final Integer gabInWeeks,
        @Nullable final Double bsaInM2,
        final List<String> diseaseTypeCodes,
        final List<MedicationForWarningsSearchDto> medicationSummaries) throws FDBException
    {
      mappedDrugs = mapToFdb(medicationSummaries);
      therapyInfo = createTherapyInfo(
          patientAgeInDays,
          patientWeightInKg,
          gabInWeeks,
          bsaInM2,
          diseaseTypeCodes,
          mappedDrugs);
    }

    private TherapyInfo createTherapyInfo(
        final long patientAgeInDays,
        @Nullable final Double patientWeightInKg,
        @Nullable final Integer gabInWeeks,
        @Nullable final Double bsaInM2,
        final List<String> diseaseTypeCodes,
        final Map<DispensableDrug, MedicationForWarningsSearchDto> drugs)
    {
      final TherapyInfo info = new TherapyInfo();

      info.setAgeInDays(patientAgeInDays);
      info.setWeightInKg(patientWeightInKg);
      info.setIcd9Codes(diseaseTypeCodes);
      info.setGabInWeeks(gabInWeeks);
      info.setBsaInM2(bsaInM2);
      //TODO ISPEK-11438
      //info.getIcd9Codes().add("345.80");  //epilepsy

      for (final DispensableDrug dispensableDrug : drugs.keySet())
      {
        final MedicationForWarningsSearchDto medicationSummary = drugs.get(dispensableDrug);
        final DrugDosingInfo dosingInfo = new DrugDosingInfo();
        dosingInfo.setDispensableDrug(dispensableDrug);
        dosingInfo.setRoute(medicationSummary.getRouteCode());
        if (medicationSummary.getDoseAmount() != null)
        {
          dosingInfo.setSingleDoseAmount(medicationSummary.getDoseAmount());
          dosingInfo.setDoseUnit(medicationSummary.getDoseUnit());
        }

        final String fdbFrequency = getFdbFrequency(medicationSummary.getFrequency(), medicationSummary.getFrequencyUnit());
        dosingInfo.setFrequency(fdbFrequency);
        dosingInfo.setDuration(Intervals.durationInDays(medicationSummary.getEffective()));
        dosingInfo.setOnlyOnce(medicationSummary.isOnlyOnce());
        dosingInfo.setProspective(medicationSummary.isProspective());
        info.addDosingInfo(dosingInfo);
      }

      return info;
    }

    private String getFdbFrequency(final int frequency, final String frequencyUnit)
    {
      if (!"/d".equals(frequencyUnit))
      {
        throw new IllegalArgumentException("Only handling 'per day (/d)' frequencies for now!");
      }
      if (frequency == 0)
      {
        throw new NullPointerException("Frequency value is required!");
      }
      return "X" + frequency + 'D';
    }

    private Map<DispensableDrug, MedicationForWarningsSearchDto> mapToFdb(final List<MedicationForWarningsSearchDto> medicationSummaries)
        throws FDBException
    {
      final Map<DispensableDrug, MedicationForWarningsSearchDto> map =
          new HashMap<DispensableDrug, MedicationForWarningsSearchDto>(medicationSummaries.size());

      for (final MedicationForWarningsSearchDto medicationSummary : medicationSummaries)
      {
        final long fdbGcnSeqNo = new Long(medicationSummary.getExternalId());
        map.put(difService.getDispensableDrugByGcnSeqNo(fdbGcnSeqNo), medicationSummary);
      }

      return map;
    }

    public TherapyInfo getTherapyInfo()
    {
      return therapyInfo;
    }

    public MedicationForWarningsSearchDto getSummary(final int dispensableDrugIndex)
    {
      final DispensableDrug dispensableDrug = therapyInfo.getDispensableDrug(dispensableDrugIndex);
      return mappedDrugs.get(dispensableDrug);
    }
  }
}
