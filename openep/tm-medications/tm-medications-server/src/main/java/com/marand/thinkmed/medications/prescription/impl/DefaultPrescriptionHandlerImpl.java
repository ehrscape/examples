package com.marand.thinkmed.medications.prescription.impl;

import java.util.List;
import javax.annotation.Nullable;

import com.marand.openehr.medications.tdo.MedicationAuthorisationSloveniaCluster.PrescriptionStatus;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.thinkmed.medications.dto.PrescriptionDto;
import com.marand.thinkmed.medications.dto.prescription.PrescriptionPackageDto;
import com.marand.thinkmed.medications.dto.prescription.PrescriptionTherapyDto;
import org.apache.commons.lang3.NotImplementedException;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.Composition;

/**
 * @author Mitja Lapajne
 */
public class DefaultPrescriptionHandlerImpl extends PrescriptionHandlerImpl
{
  @Override
  public void updatePrescriptionStatus(
      final String patientId,
      final String compositionUid,
      @Nullable final String prescriptionTherapyId,
      final PrescriptionStatus status,
      final DateTime when)
  {
    throw new NotImplementedException("Implement local PrescriptionHandler");
  }

  @Override
  public String updatePrescriptionPackage(
      final String patientId,
      final String compositionUid,
      final List<PrescriptionDto> prescriptionDtoList,
      final DateTime when)
  {
    throw new NotImplementedException("Implement local PrescriptionHandler");
  }

  @Override
  protected Composition createPrescriptionComposition(
      final List<MedicationInstructionInstruction> instructions,
      final PrescriptionPackageDto prescriptionPackageDto,
      final DateTime when)
  {
    throw new NotImplementedException("Implement local PrescriptionHandler");
  }

  @Override
  protected void fillAuthorisationData(
      final MedicationInstructionInstruction instruction,
      final PrescriptionTherapyDto prescriptionDto,
      final PrescriptionPackageDto prescriptionPackage)
  {
    throw new NotImplementedException("Implement local PrescriptionHandler");
  }
}
