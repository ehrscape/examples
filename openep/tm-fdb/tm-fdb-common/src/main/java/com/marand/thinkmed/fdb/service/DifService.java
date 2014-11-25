package com.marand.thinkmed.fdb.service;

import java.util.List;

import com.marand.maf.core.Pair;
import com.marand.thinkmed.fdb.dto.TherapyInfo;
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
import firstdatabank.dif.FDBDDCMSeverityFilter;
import firstdatabank.dif.Ingredients;
import firstdatabank.dif.LACTScreenResults;
import firstdatabank.dif.NeoDoseScreenResults;
import firstdatabank.dif.PEDIScreenResults;
import firstdatabank.dif.PREGScreenResults;
import firstdatabank.dif.SIDEScreenResults;

public interface DifService
{
  DoseRoutes getMedicationRoutes(Long id) throws FDBException;

  DispensableDrugs findDispensableDrugs(String searchString) throws FDBException;

  Ingredients findIngredients(String searchString) throws FDBException;

  DispensableDrug getDispensableDrugByGcnSeqNo(long fdbGcnSeqNo) throws FDBException;

  DoseForm getDoseForm(DispensableDrug drug) throws FDBException;

  DoseRoute getDoseRoute(String doseRouteID) throws FDBException;

  DDIMScreenResults ddimScreen(TherapyInfo therapyInfo) throws FDBException;

  PREGScreenResults pregScreen(TherapyInfo therapyInfo) throws FDBException;

  LACTScreenResults lactScreen(TherapyInfo therapyInfo) throws FDBException;

  DOSEScreenResults doseScreen(TherapyInfo therapyInfo) throws FDBException;

  DTScreenResults dtScreen(TherapyInfo therapyInfo) throws FDBException;

  List<Pair<FDBDDCMSeverityFilter, DDCMScreenResults>> ddcmScreen(TherapyInfo therapyInfo) throws FDBException;

  SIDEScreenResults sideScreen(TherapyInfo therapyInfo) throws FDBException;

  PEDIScreenResults pediScreen(TherapyInfo therapyInfo) throws FDBException;

  NeoDoseScreenResults neoDoseScreen(TherapyInfo therapyInfo) throws FDBException;;
}
