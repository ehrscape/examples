package com.marand.thinkmed.medications.connector.impl.provider.ehr;

import javax.annotation.Nonnull;

import com.marand.maf.core.Opt;
import com.marand.maf.core.StringUtils;
import com.marand.maf.core.openehr.dao.OpenEhrDaoSupport;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.connector.data.object.AllergiesDto;
import com.marand.thinkmed.medications.connector.data.object.AllergiesStatus;
import com.marand.thinkmed.medications.connector.data.object.AllergyDto;
import com.marand.thinkmed.medications.connector.data.object.ehr.EhrAllergiesStatus;
import com.marand.thinkmed.medications.connector.impl.provider.AllergiesProvider;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvDateTime;

/**
 * @author Mitja Lapajne
 * @author Nejc Korasa
 */
public class EhrAllergiesProvider extends OpenEhrDaoSupport<String> implements AllergiesProvider
{
  @Override
  public AllergiesDto getPatientAllergies(@Nonnull final String patientId)
  {
    final String ehrId = currentSession().findEhr(StringUtils.checkNotBlank(patientId, "patientId is required"));
    if (org.apache.commons.lang3.StringUtils.isEmpty(ehrId))
    {
      final AllergiesDto allergiesDto = new AllergiesDto();
      allergiesDto.setStatus(AllergiesStatus.NO_INFORMATION);
      return allergiesDto;
    }
    currentSession().useEhr(ehrId);

    final AllergiesDto allergies = new AllergiesDto();
    queryEhrContent(buildLoadAllergiesAql(ehrId), (resultRow, hasNext) ->
    {
      final DateTime date = DataValueUtils.getDateTime((DvDateTime)resultRow[0]);
      final Opt<DvCodedText> status = Opt.of((DvCodedText)resultRow[1]);

      final Opt<DvCodedText> allergen = Opt.of((DvCodedText)resultRow[2]);
      final String comment = (String)resultRow[3];
      final String reaction = (String)resultRow[4];
      final Boolean disproved = resultRow[5] != null;
      final Boolean resolved = resultRow[6] != null;

      if (allergies.getReviewDate() == null || allergies.getReviewDate().isBefore(date))
      {
        allergies.setReviewDate(date);
      }

      status.ifPresent(dvCodedText -> allergies.setStatus(EhrAllergiesStatus.get(dvCodedText).map()));

      if (allergen.isPresent() && !disproved && !resolved)
      {
        final AllergyDto allergy = new AllergyDto(
            new NamedExternalDto(allergen.get().getDefiningCode().getCodeString(), allergen.get().getValue()),
            comment,
            reaction);

        allergies.getAllergies().add(allergy);
      }

      return null;
    });

    return allergies;
  }

  private String buildLoadAllergiesAql(final String ehrId)
  {
    final String datePath = "c/content[openEHR-EHR-EVALUATION.summary_context.v1]/data[at0001]/items[at0003]/value";
    final String statusPath = "c/content[openEHR-EHR-SECTION.ispek_dialog.v1,'Allergies']/items[openEHR-EHR-EVALUATION.clinical_synopsis-allergies_mnd.v1,'Clinical Synopses - allergies']/data[at0001]/items[at0.7]/value"; // [DV_CODED_TEXT - ENUM]
    final String allergenPath = "c/content[openEHR-EHR-SECTION.ispek_dialog.v1,'Allergies']/items[openEHR-EHR-EVALUATION.adverse_reaction-md.v1]/data[at0001]/items[at0002.1]/value"; // [DV_CODED_TEXT]
    final String allergenCommentPath = "c/content[openEHR-EHR-SECTION.ispek_dialog.v1,'Allergies']/items[openEHR-EHR-EVALUATION.adverse_reaction-md.v1]/data[at0001]/items[at0006]/value/value"; // [String]
    final String allergenReactionPath = "c/content[openEHR-EHR-SECTION.ispek_dialog.v1,'Allergies']/items[openEHR-EHR-EVALUATION.adverse_reaction-md.v1]/data[at0001]/items[at0009]/items[at0026]/items[at0012]/value"; // [String]
    final String allergenDateDisprovedPath = "c/content[openEHR-EHR-SECTION.ispek_dialog.v1,'Allergies']/items[openEHR-EHR-EVALUATION.adverse_reaction-md.v1]/data[at0001]/items[at0.81]/value";
    final String allergenDateResolvedPath = "c/content[openEHR-EHR-SECTION.ispek_dialog.v1,'Allergies']/items[openEHR-EHR-EVALUATION.adverse_reaction-md.v1]/data[at0001]/items[at0.76]/value";

    return new StringBuilder()
        .append("SELECT ")
        .append(datePath).append(", ")
        .append(statusPath).append(", ")
        .append(allergenPath).append(", ")
        .append(allergenCommentPath).append(", ")
        .append(allergenReactionPath).append(", ")
        .append(allergenDateDisprovedPath).append(", ")
        .append(allergenDateResolvedPath).append(" ")
        .append("FROM EHR[ehr_id/value='").append(ehrId).append("'] ")
        .append("CONTAINS Composition c[openEHR-EHR-COMPOSITION.summary.v1] ")
        .append("WHERE c/name/value = 'Allergy history' ")
        .append("ORDER BY ").append(datePath).append(" DESC")
        .toString();
  }
}

