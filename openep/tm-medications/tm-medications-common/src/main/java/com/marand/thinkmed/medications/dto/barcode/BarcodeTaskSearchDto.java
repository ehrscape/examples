package com.marand.thinkmed.medications.dto.barcode;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class BarcodeTaskSearchDto extends DataTransferObject implements JsonSerializable
{
  private final BarcodeSearchResult barcodeSearchResult;
  private final String taskId;
  private final Long medicationId;

  public BarcodeTaskSearchDto(final BarcodeSearchResult barcodeSearchResult)
  {
    this.barcodeSearchResult = barcodeSearchResult;
    medicationId = null;
    taskId = null;
  }

  public BarcodeTaskSearchDto(
      final BarcodeSearchResult barcodeSearchResult,
      final String taskId,
      final Long medicationId)
  {
    this.barcodeSearchResult = barcodeSearchResult;
    this.taskId = taskId;
    this.medicationId = medicationId;
  }

  public BarcodeSearchResult getBarcodeSearchResult()
  {
    return barcodeSearchResult;
  }

  public String getTaskId()
  {
    return taskId;
  }

  public Long getMedicationId()
  {
    return medicationId;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("barcodeScanResult", barcodeSearchResult)
        .append("taskId", taskId)
        .append("medicationId", medicationId);
  }

  public enum BarcodeSearchResult
  {
    TASK_FOUND, //success - exactly one due task found for given barcode
    NO_MEDICATION, //failed - no medication found for given barcode
    NO_TASK, //failed - no due task found for given barcode
    MULTIPLE_TASKS //failed - multiple due tasks found for given barcode
  }
}
