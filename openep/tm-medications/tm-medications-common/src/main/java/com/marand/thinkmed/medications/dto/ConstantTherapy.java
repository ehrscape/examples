package com.marand.thinkmed.medications.dto;

import java.util.List;

import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.thinkmed.medications.TitrationType;

/**
 * @author Mitja Lapajne
 */
public interface ConstantTherapy
{
  TitrationType getTitration();

  void setTitration(TitrationType titration);

  List<HourMinuteDto> getDoseTimes();

  void setDoseTimes(List<HourMinuteDto> doseTimes);
}
