package com.marand.thinkmed.medications.connector.impl.provider.ehr;

import java.util.List;
import javax.annotation.Nonnull;

import com.marand.maf.core.Opt;
import com.marand.maf.core.StringUtils;
import com.marand.maf.core.openehr.dao.OpenEhrDaoSupport;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.connector.data.object.ObservationDto;
import com.marand.thinkmed.medications.connector.impl.provider.HeightProvider;
import org.openehr.jaxb.rm.DvDateTime;
import org.openehr.jaxb.rm.DvQuantity;

/**
 * @author Mitja Lapajne
 */
public class EhrHeightProvider extends OpenEhrDaoSupport<String> implements HeightProvider
{

  @Override
  public Opt<ObservationDto> getPatientHeight(@Nonnull final String patientId)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");

    final String ehrId = currentSession().findEhr(patientId);

    if (org.apache.commons.lang3.StringUtils.isEmpty(ehrId))
    {
      return Opt.none();
    }
    currentSession().useEhr(ehrId);

    final String aqlString =
        "SELECT o/data[at0001]/events[at0002]/time, o/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value " +
            "FROM EHR[ehr_id/value='" + ehrId + "'] " +
            "CONTAINS Observation o[openEHR-EHR-OBSERVATION.weight.v1] " +
            "WHERE o/name/value = 'Weight' " +
            "ORDER BY o/data[at0001]/events[at0002]/time DESC " +
            "FETCH 1";

    final List<ObservationDto> observations = queryEhrContent(aqlString, (resultRow, hasNext) ->
    {
      return new ObservationDto(
          DataValueUtils.getDateTime((DvDateTime)resultRow[0]),
          ((DvQuantity)resultRow[1]).getMagnitude());
    });

    return observations.isEmpty() ? Opt.none() : Opt.of(observations.get(0));
  }
}
