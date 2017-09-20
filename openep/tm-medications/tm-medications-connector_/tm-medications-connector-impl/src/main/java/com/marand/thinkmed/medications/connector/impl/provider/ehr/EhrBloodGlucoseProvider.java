package com.marand.thinkmed.medications.connector.impl.provider.ehr;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.StringUtils;
import com.marand.maf.core.openehr.dao.OpenEhrDaoSupport;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.connector.data.object.ObservationDto;
import com.marand.thinkmed.medications.connector.impl.provider.BloodGlucoseProvider;
import edu.emory.mathcs.backport.java.util.Collections;
import org.joda.time.Interval;
import org.openehr.jaxb.rm.DvDateTime;
import org.openehr.jaxb.rm.DvQuantity;

/**
 * @author Mitja Lapajne
 * @author Nejc Korasa
 */
public class EhrBloodGlucoseProvider extends OpenEhrDaoSupport<String> implements BloodGlucoseProvider
{
  @Override
  public List<ObservationDto> getPatientBloodGlucoseMeasurements(
      @Nonnull final String patientId,
      @Nonnull final Interval interval)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");
    Preconditions.checkNotNull(interval, "interval must not be null");

    final String ehrId = currentSession().findEhr(patientId);

    if (org.apache.commons.lang3.StringUtils.isEmpty(ehrId))
    {
      Collections.emptyList();
    }
    currentSession().useEhr(ehrId);

    return queryEhrContent(buildLoadBloodGlucoseEntriesAql(ehrId, interval), (resultRow, hasNext) ->
    {
      final DvDateTime date = (DvDateTime)resultRow[0];
      final DvQuantity glucose = (DvQuantity)resultRow[1];
      return new ObservationDto(DataValueUtils.getDateTime(date), glucose.getMagnitude());
    }).stream().filter(o -> o.getTimestamp() == null || o.getValue() == null).collect(Collectors.toList());
  }

  private String buildLoadBloodGlucoseEntriesAql(final String ehrId, final Interval interval)
  {
    final String datePath = "c/content[openEHR-EHR-SECTION.ispek_dialog.v1,'Vitals']/items[openEHR-EHR-OBSERVATION.lab_test-hba1c.v1]/protocol[at0004]/items[at0075]/value"; // [DV_DATE_TIME]
    final String glucosePath = "c/content[openEHR-EHR-SECTION.ispek_dialog.v1,'Basic body related measurements']/items[openEHR-EHR-OBSERVATION.lab_test-blood_glucose.v1]/data[at0001]/events[at0002]/data[at0003]/items[at0078.2]/value"; // [DV_QUANTITY]

    return new StringBuilder()
        .append("SELECT ")
        .append(datePath).append(", ")
        .append(glucosePath).append(" ")
        .append("FROM EHR[ehr_id/value='").append(ehrId).append("'] ")
        .append("CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1] ")
        .append("WHERE c/name/value = 'Vital functions' ")
        .append("AND ").append(datePath).append(" > ").append(getAqlDateTimeQuoted(interval.getStart())).append(" ")
        .append("AND ").append(datePath).append(" < ").append(getAqlDateTimeQuoted(interval.getEnd()))
        .toString();
  }
}

