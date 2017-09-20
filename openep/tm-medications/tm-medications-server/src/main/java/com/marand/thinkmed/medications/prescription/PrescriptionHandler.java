package com.marand.thinkmed.medications.prescription;

import java.util.List;

import com.marand.openehr.medications.tdo.MedicationAuthorisationSloveniaCluster.PrescriptionStatus;
import com.marand.thinkmed.medications.dto.PrescriptionDto;
import com.marand.thinkmed.medications.dto.prescription.PrescriptionPackageDto;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public interface PrescriptionHandler
{
  String savePrescription(
      String patientId,
      PrescriptionPackageDto prescriptionPackageDto,
      DateTime when);

  void updatePrescriptionStatus(
      String patientId,
      String compositionUid,
      String prescriptionTherapyId,
      PrescriptionStatus status,
      DateTime when);

  String updatePrescriptionPackage(
      String patientId,
      String compositionUid,
      List<PrescriptionDto> prescriptionDtoList,
      DateTime when);
}
