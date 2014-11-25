package com.marand.thinkmed.fdb.dif.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.marand.maf.core.Pair;
import com.marand.thinkmed.fdb.dif.FdbDataManagerFactory;
import com.marand.thinkmed.fdb.dto.DrugDosingInfo;
import com.marand.thinkmed.fdb.dto.TherapyInfo;
import com.marand.thinkmed.fdb.service.DifService;
import firstdatabank.database.FDBException;
import firstdatabank.dif.DDCMScreenResults;
import firstdatabank.dif.DDIMScreenResults;
import firstdatabank.dif.DOSEScreenResults;
import firstdatabank.dif.DTScreenResults;
import firstdatabank.dif.DispensableDrug;
import firstdatabank.dif.DispensableDrugs;
import firstdatabank.dif.DoseForm;
import firstdatabank.dif.DoseRoute;
import firstdatabank.dif.DoseRoutes;
import firstdatabank.dif.DrugSearchFilter;
import firstdatabank.dif.FDBDDCMSeverityFilter;
import firstdatabank.dif.FDBDOSESource;
import firstdatabank.dif.FDBDispensableDrugLoadType;
import firstdatabank.dif.FDBDrugType;
import firstdatabank.dif.FDBPEDISeverityFilter;
import firstdatabank.dif.FDBSIDEFreqFilter;
import firstdatabank.dif.FDBSIDESeverityFilter;
import firstdatabank.dif.FDBScreenConditionType;
import firstdatabank.dif.Ingredients;
import firstdatabank.dif.LACTScreenResults;
import firstdatabank.dif.Navigation;
import firstdatabank.dif.NeoDoseScreenResults;
import firstdatabank.dif.PEDIScreenResults;
import firstdatabank.dif.PREGScreenResults;
import firstdatabank.dif.SIDEScreenResults;
import firstdatabank.dif.ScreenCondition;
import firstdatabank.dif.ScreenConditions;
import firstdatabank.dif.ScreenDrug;
import firstdatabank.dif.ScreenDrugs;
import firstdatabank.dif.Screening;
import firstdatabank.dif.SearchFilter;

public class DifServiceImpl implements DifService
{
  @Override
  public DoseRoutes getMedicationRoutes(final Long id) throws FDBException
  {
    final DispensableDrug dispensableDrug = new DispensableDrug(FdbDataManagerFactory.getManager());
    dispensableDrug.load(id, null, "", "", "");
    return dispensableDrug.getDoseRoutes();
  }

  @Override
  public DispensableDrugs findDispensableDrugs(final String searchString) throws FDBException
  {
    final Navigation navigation = new Navigation(FdbDataManagerFactory.getManager());
    return navigation.dispensableDrugSearch(searchString, new SearchFilter(), new DrugSearchFilter());
  }

  @Override
  public Ingredients findIngredients(final String searchString) throws FDBException
  {
    final Navigation navigation = new Navigation(FdbDataManagerFactory.getManager());
    return navigation.ingredientSearch(searchString, new SearchFilter());
  }

  @Override
  public DispensableDrug getDispensableDrugByGcnSeqNo(final long fdbGcnSeqNo) throws FDBException
  {
    final DispensableDrug dispensableDrug = new DispensableDrug(FdbDataManagerFactory.getManager());
    dispensableDrug.load(fdbGcnSeqNo, FDBDispensableDrugLoadType.fdbDDLTGCNSeqNo, "", "", "");
    return dispensableDrug;
  }

  @Override
  public DoseForm getDoseForm(final DispensableDrug drug) throws FDBException
  {
    final DoseForm doseForm = new DoseForm(FdbDataManagerFactory.getManager());
    doseForm.load(drug.getDoseFormID());
    return doseForm;
  }

  @Override
  public DoseRoute getDoseRoute(final String doseRouteID) throws FDBException
  {
    final DoseRoute doseRoute = new DoseRoute(FdbDataManagerFactory.getManager());
    doseRoute.load(doseRouteID);
    return doseRoute;
  }

  private ScreenDrugs createScreenDrugs(final TherapyInfo therapyInfo) throws FDBException
  {
    final ScreenDrugs result = new ScreenDrugs(FdbDataManagerFactory.getManager());

    for (final DrugDosingInfo dosingInfo : therapyInfo.getDosingInfos())
    {
      if (dosingInfo.getSingleDoseAmount() > 0.0)
      {
        final DispensableDrug dispensableDrug = dosingInfo.getDispensableDrug();
        final ScreenDrug screenDrug = new ScreenDrug(FdbDataManagerFactory.getManager());
        screenDrug.load(String.valueOf(dispensableDrug.getID()), FDBDrugType.fdbDTGCNSeqNo);
        screenDrug.setDose(
            dosingInfo.getSingleDoseAmount(),
            dosingInfo.getDoseUnit(),
            dosingInfo.getFrequency(),
            dosingInfo.getDuration());
        screenDrug.setDoseType(dosingInfo.isOnlyOnce() ? "07" : "02");
        screenDrug.setProspective(dosingInfo.isProspective());
        //TODO ISPEK-11438
        //screenDrug.setDoseRoute(dosingInfo.getRoute());
        //screenDrug.setDoseRoute("064");
        result.addItem(screenDrug);
      }
    }

    return result;
  }

  @Override
  public DDIMScreenResults ddimScreen(final TherapyInfo therapyInfo) throws FDBException
  {
    final ScreenDrugs screenDrugs = createScreenDrugs(therapyInfo);
    final Screening screening = new Screening(FdbDataManagerFactory.getManager());
    //TODO ISPEK-11438
    //return screening.DDIMScreen(screenDrugs, true, FDBDDIMSeverityFilter.fdbDDIMSFContraindicated, false, false);
    return screening.DDIMScreen(screenDrugs, true, null, false, false);
  }

  @Override
  public PREGScreenResults pregScreen(final TherapyInfo therapyInfo) throws FDBException
  {
    final ScreenDrugs screenDrugs = createScreenDrugs(therapyInfo);
    final Screening screening = new Screening(FdbDataManagerFactory.getManager());
    //TODO ISPEK-11438
    //return screening.PREGScreen(screenDrugs, true, FDBPREGSignificanceFilter.fdbPREGSFContraindicated);
    return screening.PREGScreen(screenDrugs, true, null);
  }

  @Override
  public LACTScreenResults lactScreen(final TherapyInfo therapyInfo) throws FDBException
  {
    final ScreenDrugs screenDrugs = createScreenDrugs(therapyInfo);
    final Screening screening = new Screening(FdbDataManagerFactory.getManager());
    //TODO ISPEK-11438
    //return screening.LACTScreen(screenDrugs, true, FDBLACTSeverityFilter.fdbLACTSFContraindicated);
    return screening.LACTScreen(screenDrugs, true, null);
  }

  @Override
  public DOSEScreenResults doseScreen(final TherapyInfo therapyInfo) throws FDBException
  {
    final ScreenDrugs screenDrugs = createScreenDrugs(therapyInfo);
    final Screening screening = new Screening(FdbDataManagerFactory.getManager());
    return screening.DOSEScreen(
        screenDrugs,
        true,
        therapyInfo.getAgeInDays(),
        therapyInfo.getWeightInKg() != null ? therapyInfo.getWeightInKg() : 0.0,
        0.0,
        FDBDOSESource.fdbDSDRCAndMinMax,
        "");
  }

  @Override
  public DTScreenResults dtScreen(final TherapyInfo therapyInfo) throws FDBException
  {
    final ScreenDrugs screenDrugs = createScreenDrugs(therapyInfo);
    final Screening screening = new Screening(FdbDataManagerFactory.getManager());
    return screening.DTScreen(screenDrugs, true, true);
  }

  @Override
  public List<Pair<FDBDDCMSeverityFilter, DDCMScreenResults>> ddcmScreen(final TherapyInfo therapyInfo) throws FDBException
  {
    final ScreenDrugs screenDrugs = createScreenDrugs(therapyInfo);
    final Screening screening = new Screening(FdbDataManagerFactory.getManager());
    final ScreenConditions screenConditions = new ScreenConditions(FdbDataManagerFactory.getManager());
    for (final String icd9Code : therapyInfo.getIcd9Codes())
    {
      final ScreenCondition screenCondition = new ScreenCondition(FdbDataManagerFactory.getManager());
      screenCondition.load(icd9Code, FDBScreenConditionType.fdbSCTICD9);
      screenConditions.addItem(screenCondition);
    }

    final List<Pair<FDBDDCMSeverityFilter, DDCMScreenResults>> result =
        new ArrayList<Pair<FDBDDCMSeverityFilter, DDCMScreenResults>>();

    final DDCMScreenResults contraindicatedResults = screening.DDCMScreen(
        screenDrugs,
        screenConditions,
        true,
        FDBDDCMSeverityFilter.fdbDDCMSFContraindicated);
    if (contraindicatedResults.count() > 0)
    {
      result.add(
          Pair.of(
              FDBDDCMSeverityFilter.fdbDDCMSFContraindicated,
              contraindicatedResults));
    }

    final DDCMScreenResults extremeCautionResults = screening.DDCMScreen(
        screenDrugs,
        screenConditions,
        true,
        FDBDDCMSeverityFilter.fdbDDCMSFExtremeCaution);
    if (extremeCautionResults.count() > 0)
    {
      result.add(
          Pair.of(
              FDBDDCMSeverityFilter.fdbDDCMSFExtremeCaution,
              extremeCautionResults));
    }

    final DDCMScreenResults warningResults = screening.DDCMScreen(
        screenDrugs,
        screenConditions,
        true,
        FDBDDCMSeverityFilter.fdbDDCMSFWarning);
    if (warningResults.count() > 0)
    {
      result.add(
          Pair.of(
              FDBDDCMSeverityFilter.fdbDDCMSFWarning,
              warningResults));
    }
    return result;
  }

  @Override
  public SIDEScreenResults sideScreen(final TherapyInfo therapyInfo) throws FDBException
  {
    final ScreenDrugs screenDrugs = createScreenDrugs(therapyInfo);
    final Screening screening = new Screening(FdbDataManagerFactory.getManager());

    final ScreenConditions screenConditions = new ScreenConditions(FdbDataManagerFactory.getManager());
    for (final String icd9Code : therapyInfo.getIcd9Codes())
    {
      final ScreenCondition screenCondition = new ScreenCondition(FdbDataManagerFactory.getManager());
      screenCondition.load(icd9Code, FDBScreenConditionType.fdbSCTICD9);
      screenConditions.addItem(screenCondition);
    }

    return screening.SIDEScreen(
        screenDrugs,
        screenConditions,
        true,
        FDBSIDESeverityFilter.fdbSIDESFAll,
        FDBSIDEFreqFilter.fdbSIDEFFAll);
  }

  @Override
  public PEDIScreenResults pediScreen(final TherapyInfo therapyInfo) throws FDBException
  {
    final ScreenDrugs screenDrugs = createScreenDrugs(therapyInfo);
    final Screening screening = new Screening(FdbDataManagerFactory.getManager());

    return screening.PEDIScreen(screenDrugs, true, FDBPEDISeverityFilter.fdbPEDISFAll);
  }

  @Override
  public NeoDoseScreenResults neoDoseScreen(final TherapyInfo therapyInfo) throws FDBException
  {
    final ScreenDrugs screenDrugs = createScreenDrugs(therapyInfo);
    final Screening screening = new Screening(FdbDataManagerFactory.getManager());

    return screening.NeoDoseScreen(
        screenDrugs,
        true,
        therapyInfo.getAgeInDays(),
        therapyInfo.getWeightInKg() * 1000.0,
        therapyInfo.getBsaInM2() != null ? therapyInfo.getBsaInM2() : 0.0,
        therapyInfo.getGabInWeeks());
  }
}
