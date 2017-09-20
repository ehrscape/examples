package com.marand.thinkmed.medications.connector.impl.provider.ehr;

import java.util.List;
import javax.annotation.Nonnull;

import com.marand.maf.core.StringUtils;
import com.marand.maf.core.openehr.dao.OpenEhrDaoSupport;
import com.marand.thinkmed.medications.connector.data.object.DiseaseDto;
import com.marand.thinkmed.medications.connector.impl.provider.DiseasesProvider;
import edu.emory.mathcs.backport.java.util.Collections;

/**
 * @author Mitja Lapajne
 */
public class EhrDiseasesProvider extends OpenEhrDaoSupport<String> implements DiseasesProvider
{

  @Override
  public List<DiseaseDto> getPatientDiseases(@Nonnull final String patientId)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");

    final String ehrId = currentSession().findEhr(patientId);

    if (org.apache.commons.lang3.StringUtils.isEmpty(ehrId))
    {
      Collections.emptyList();
    }
    currentSession().useEhr(ehrId);

    //TODO insert correct aql when ehr templates will be defined
    final String aqlString =
        "SELECT o/data[at0001]/events[at0002]/time, o/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value " +
            "FROM EHR[ehr_id/value='" + ehrId + "'] " +
            "CONTAINS Observation o[openEHR-EHR-OBSERVATION.height.v1] " +
            "WHERE o/name/value = 'Height/Length' " +
            "ORDER BY o/data[at0001]/events[at0002]/time DESC " +
            "FETCH 1";

    return queryEhrContent(aqlString, (resultRow, hasNext) ->
    {
      final DiseaseDto disease = new DiseaseDto((String)resultRow[0], (String)resultRow[1]);
      disease.setComment((String)resultRow[2]);
      return disease;
    });
  }
}

