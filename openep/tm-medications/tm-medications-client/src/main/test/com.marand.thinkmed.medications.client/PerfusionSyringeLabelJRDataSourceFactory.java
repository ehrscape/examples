package com.marand.thinkmed.medications.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.marand.thinkmed.medications.dto.pharmacist.perfusionSyringe.PerfusionSyringeLabelDto;

/**
 * @author Primoz Prislan
 */
public class PerfusionSyringeLabelJRDataSourceFactory
{
  public static Collection<PerfusionSyringeLabelDto> createPerfusionSyringeLabel()
  {
    System.out.println("PerfusionSyringeLabelJRDataSourceFactory v1.4");
    final List<PerfusionSyringeLabelDto> records = new ArrayList<>();

    final PerfusionSyringeLabelDto record1 = new PerfusionSyringeLabelDto();
    record1.setBarCode("demoBarcodeString1");
    record1.setTherapyDisplayValue(
        "Ara-cell 50mg/ konc. za razt. za inf. \u2014 <b>100mg</b> \u25CF Depocyte 10mg/ml susp. za inj — <b>20mg/ml</b>");
    record1.setPatientName("Tanhofer Benček");
    record1.setPatientBirthDate("12.4.1975");
    record1.setPreparationStartedTime("18.12.2015 10:30");
    record1.setPatientCareProvider("KOOKIT EIT");
    record1.setPatientRoomAndBed("S15P01");
    record1.setPreparedBy("PRIPRAVIL: Dragica Dremelj");
    record1.setPrescribedBy("PREDPISAL: Hamp Ayako");
    records.add(record1);

    final PerfusionSyringeLabelDto record2 = new PerfusionSyringeLabelDto();
    record2.setBarCode("demoBarcodeString2");
    record2.setTherapyDisplayValue(
        "Morphine sulfate 10mg/10ml solution for injection pre-filled syringes — <b>10ml</b> ●"
            + " Dopamine 200mg/5ml concentrate for solution for infusion ampoules — <b>200ml</b> ●"
            + " Potassium chloride 15% solution for infusion — <b>20ml</b> ● SKUPNI VOLUMEN: <b>230ml</b>");
    record2.setPatientName("Tomaž Benček z imenom kar dolgim");
    record2.setPatientBirthDate("12.4.1975");
    record2.setPreparationStartedTime("18.12.2015 10:30");
    record2.setPatientCareProvider("KOOKIT EIT");
    record2.setPatientRoomAndBed("N1A-IZO1_EIT/POST1");
    record2.setPreparedBy("PRIPRAVIL: Dragica Dremelj");
    record2.setPrescribedBy("PREDPISAL: Hamp Ayako");
    records.add(record2);

    final PerfusionSyringeLabelDto record3 = new PerfusionSyringeLabelDto();
    record3.setBarCode("demoBarcodeString3");
    record3.setTherapyDisplayValue(
        "Insulin soluble human 100units/ml solution for injection — <b>10ml</b> ●"
            + " Glucose 10% infusion 500ml bags — <b>500ml</b> ●"
            + " Potassium chloride 15% solution for infusion — <b>120ml</b> ●"
            + " Sodium chloride (Nacl) 15% solution for infusion — <b>200ml</b> ● SKUPNI VOLUMEN: <b>230ml</b>");
    record3.setPatientName("Tomaž Benček z dolgim imenom");
    record3.setPatientBirthDate("12.4.1975");
    record3.setPreparationStartedTime("18.12.2015 10:30");
    record3.setPatientCareProvider("HEMATO ONKO HOSPITAL");
    record3.setPatientRoomAndBed("S15P11");
    record3.setPreparedBy("PRIPRAVIL: Dragica Dremelj");
    record3.setPrescribedBy("PREDPISAL: Hamp Ayako");
    records.add(record3);

    final PerfusionSyringeLabelDto record4 = new PerfusionSyringeLabelDto();
    record4.setBarCode("demoBarcodeString4");
    record4.setTherapyDisplayValue(
        "1/4 RINGER v 5% GLUKOZI — <b>485ml</b> ●"
            + " KALIJEV KLORID 1M konc. za razt. za inf. — <b>10mmol</b> ●"
            + " NATRIJEV KLORID (Nacl) 1M — <b>5mmol</b> ● SKUPNI VOLUMEN: <b>500ml + heparin 1 IE/ml</b>\"");
    record4.setPatientName("Tomaž Benček z dolgim imenom");
    record4.setPatientBirthDate("12.4.1975");
    record4.setPreparationStartedTime("18.12.2015 10:30");
    record4.setPatientCareProvider("HEMATO ONKO HOSPITAL");
    record4.setPatientRoomAndBed("S15P11");
    record4.setPreparedBy("PRIPRAVIL: Dragica Dremelj");
    record4.setPrescribedBy("PREDPISAL: Hamp Ayako");
    records.add(record4);

    return records;
  }
}