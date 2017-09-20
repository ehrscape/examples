package com.marand.thinkmed.medications.dto;

import java.util.Collections;
import java.util.List;

import com.marand.thinkmed.medications.MedicationOrderFormType;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static com.marand.openehr.medications.tdo.AdministrationDetailsCluster.OxygenDeliveryCluster;

/**
 * @author Nejc Korasa
 */
public class OxygenTherapyDto extends TherapyDto
{
  private MedicationDto medication;
  private Double flowRate;
  private String flowRateUnit;
  private OxygenDeliveryCluster.FlowRateMode flowRateMode;
  private OxygenStartingDevice startingDevice;
  private Double minTargetSaturation;
  private Double maxTargetSaturation;
  private boolean humidification;

  private String speedDisplay;
  private String startingDeviceDisplay;

  public OxygenTherapyDto()
  {
    super(MedicationOrderFormType.OXYGEN, false);
  }

  public MedicationDto getMedication()
  {
    return medication;
  }

  public void setMedication(final MedicationDto medication)
  {
    this.medication = medication;
  }

  public Double getFlowRate()
  {
    return flowRate;
  }

  public void setFlowRate(final Double flowRate)
  {
    this.flowRate = flowRate;
  }

  public String getFlowRateUnit()
  {
    return flowRateUnit;
  }

  public void setFlowRateUnit(final String flowRateUnit)
  {
    this.flowRateUnit = flowRateUnit;
  }

  public OxygenDeliveryCluster.FlowRateMode getFlowRateMode()
  {
    return flowRateMode;
  }

  public void setFlowRateMode(final OxygenDeliveryCluster.FlowRateMode flowRateMode)
  {
    this.flowRateMode = flowRateMode;
  }

  public OxygenStartingDevice getStartingDevice()
  {
    return startingDevice;
  }

  public void setStartingDevice(final OxygenStartingDevice startingDevice)
  {
    this.startingDevice = startingDevice;
  }

  public Double getMinTargetSaturation()
  {
    return minTargetSaturation;
  }

  public void setMinTargetSaturation(final Double minTargetSaturation)
  {
    this.minTargetSaturation = minTargetSaturation;
  }

  public Double getMaxTargetSaturation()
  {
    return maxTargetSaturation;
  }

  public void setMaxTargetSaturation(final Double maxTargetSaturation)
  {
    this.maxTargetSaturation = maxTargetSaturation;
  }

  public boolean isHumidification()
  {
    return humidification;
  }

  public void setHumidification(final boolean humidification)
  {
    this.humidification = humidification;
  }

  public String getSpeedDisplay()
  {
    return speedDisplay;
  }

  public void setSpeedDisplay(final String speedDisplay)
  {
    this.speedDisplay = speedDisplay;
  }

  public String getStartingDeviceDisplay()
  {
    return startingDeviceDisplay;
  }

  public void setStartingDeviceDisplay(final String startingDeviceDisplay)
  {
    this.startingDeviceDisplay = startingDeviceDisplay;
  }

  @Override
  public boolean isNormalInfusion()
  {
    return false;
  }

  @Override
  public List<MedicationDto> getMedications()
  {
    return Collections.singletonList(medication);
  }

  @Override
  public Long getMainMedicationId()
  {
    return medication.getId();
  }

  public String getSaturationDisplay()
  {
    final StringBuilder saturationBuilder = new StringBuilder();
    if (maxTargetSaturation == null || minTargetSaturation == null)
    {
      return saturationBuilder.toString();
    }

    //noinspection NumericCastThatLosesPrecision
    return saturationBuilder
        .append((int)Math.round(minTargetSaturation * 100))
        .append("%")
        .append(" - ")
        .append((int)Math.round(maxTargetSaturation * 100))
        .append("%")
        .toString();
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("medication", medication)
        .append("flowRate", flowRate)
        .append("flowRateUnit", flowRateUnit)
        .append("flowRateUnit", flowRateUnit)
        .append("flowRateMode", flowRateMode)
        .append("startingDevice", startingDevice)
        .append("minTargetSaturation", minTargetSaturation)
        .append("maxTargetSaturation", maxTargetSaturation)
        .append("humidification", humidification)
        .append("speedDisplay", speedDisplay)
    ;
  }
}
