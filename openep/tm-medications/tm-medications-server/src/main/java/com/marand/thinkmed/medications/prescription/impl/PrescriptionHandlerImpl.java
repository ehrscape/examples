package com.marand.thinkmed.medications.prescription.impl;

import java.util.ArrayList;
import java.util.List;

import com.marand.maf.core.service.ServiceMethod;
import com.marand.maf.core.service.auditing.Auditing;
import com.marand.maf.core.service.auditing.Level;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.thinkehr.session.EhrSessioned;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.converter.therapy.MedicationConverterSelector;
import com.marand.thinkmed.medications.converter.therapy.MedicationToEhrConverter;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.prescription.PrescriptionPackageDto;
import com.marand.thinkmed.medications.dto.prescription.PrescriptionTherapyDto;
import com.marand.thinkmed.medications.prescription.PrescriptionHandler;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.Composition;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Mitja Lapajne
 */
public abstract class PrescriptionHandlerImpl implements PrescriptionHandler
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  protected MedicationsOpenEhrDao getEhrMedicationsDao()
  {
    return medicationsOpenEhrDao;
  }

  @Required
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Override
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public String savePrescription(
      final String patientId,
      final PrescriptionPackageDto prescriptionPackageDto,
      final DateTime when)
  {
    final List<MedicationInstructionInstruction> instructions = new ArrayList<>();
    for (final PrescriptionTherapyDto prescriptionDto : prescriptionPackageDto.getPrescriptionTherapies())
    {
      final TherapyDto therapy = prescriptionDto.getTherapy();
      final MedicationToEhrConverter<?> therapyConverter = MedicationConverterSelector.getConverter(therapy);
      final MedicationInstructionInstruction instruction = therapyConverter.createInstructionFromTherapy(therapy);
      fillAuthorisationData(instruction, prescriptionDto, prescriptionPackageDto);
      instructions.add(instruction);
    }
    final Composition composition = createPrescriptionComposition(instructions, prescriptionPackageDto, when);
    MedicationsEhrUtils.visitComposition(composition, prescriptionPackageDto.getComposer(), when);
    return medicationsOpenEhrDao.saveComposition(patientId, composition, prescriptionPackageDto.getCompositionUid());
  }

  protected abstract Composition createPrescriptionComposition(
      List<MedicationInstructionInstruction> instructions,
      final PrescriptionPackageDto prescriptionPackageDto,
      final DateTime when);

  protected abstract void fillAuthorisationData(
      MedicationInstructionInstruction instruction,
      PrescriptionTherapyDto prescriptionDto,
      PrescriptionPackageDto prescriptionPackage);
}
