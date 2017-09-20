/*
 * Copyright (c) 2010-2014 Marand d.o.o. (www.marand.com)
 *
 * This file is part of Think!Med Clinical Medication Management.
 *
 * Think!Med Clinical Medication Management is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Think!Med Clinical Medication Management is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Think!Med Clinical Medication Management.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.marand.thinkmed.medications.dao.openehr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.maf.core.openehr.dao.OpenEhrDaoSupport;
import com.marand.maf.core.resultrow.ResultRowProcessor;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.medications.tdo.EPrescriptionSloveniaComposition;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationAdministrationComposition;
import com.marand.openehr.medications.tdo.MedicationConsentFormComposition;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity;
import com.marand.openehr.medications.tdo.MedicationOnAdmissionComposition;
import com.marand.openehr.medications.tdo.MedicationOnDischargeComposition;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.medications.tdo.MedicationReferenceWeightComposition;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition;
import com.marand.openehr.rm.RmObject;
import com.marand.openehr.rm.RmPath;
import com.marand.openehr.rm.TdoPathable;
import com.marand.openehr.tdo.conversion.RmoToTdoConverter;
import com.marand.openehr.tdo.conversion.TdoToRmoConverter;
import com.marand.openehr.util.DataValueUtils;
import com.marand.openehr.util.OpenEhrRefUtils;
import com.marand.thinkehr.query.builder.EhrResult;
import com.marand.thinkehr.query.builder.EhrResultRow;
import com.marand.thinkehr.query.builder.QueryBuilder;
import com.marand.thinkehr.query.service.QueryService;
import com.marand.thinkehr.session.EhrSessionManager;
import com.marand.thinkehr.session.EhrSessioned;
import com.marand.thinkehr.web.WebTemplate;
import com.marand.thinkehr.web.build.input.CodedValueWithDescription;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.SelfAdministeringActionEnum;
import com.marand.thinkmed.medications.TherapyCommentEnum;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.business.util.TherapyIdUtils;
import com.marand.thinkmed.medications.charting.NormalInfusionAutomaticChartingDto;
import com.marand.thinkmed.medications.charting.SelfAdminAutomaticChartingDto;
import com.marand.thinkmed.medications.charting.TherapyAutomaticChartingDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.openehr.jaxb.rm.Action;
import org.openehr.jaxb.rm.Composition;
import org.openehr.jaxb.rm.DvDateTime;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.Link;
import org.openehr.jaxb.rm.LocatableRef;
import org.openehr.jaxb.rm.ObjectVersionId;
import org.springframework.beans.factory.annotation.Autowired;

import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.InfusionAdministrationDetailsPurpose;

/**
 * @author Bostjan Vester
 */
public class MedicationsOpenEhrDao extends OpenEhrDaoSupport<String>
{
  private EhrSessionManager ehrSessionManager;
  private QueryService ehrQueryService;

  @Autowired
  public void setEhrSessionManager(final EhrSessionManager ehrSessionManager)
  {
    this.ehrSessionManager = ehrSessionManager;
  }

  @Autowired
  public void setEhrQueryService(final QueryService ehrQueryService)
  {
    this.ehrQueryService = ehrQueryService;
  }

  public List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> findMedicationInstructions(
      final String patientId, final Interval searchInterval, final String centralCaseId)
  {
    Preconditions.checkArgument(BooleanUtils.xor(new Boolean[]{centralCaseId != null, searchInterval != null}));

    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      sb
          .append("SELECT c, i/name/value FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]")
          .append(" CONTAINS Instruction i[openEHR-EHR-INSTRUCTION.medication.v1]")
          .append(" WHERE c/name/value = 'Medication order'");
      if (searchInterval != null)
      {
        appendMedicationTimingIntervalCriterion(sb, searchInterval);
      }
      if (centralCaseId != null)
      {
        sb
            .append(" AND ")
            .append(
                "c/context/other_context[at0001]/items[openEHR-EHR-CLUSTER.composition_context_detail.v1]/items[at0001]/value = '"
                    + centralCaseId + "'"
            );
      }
      sb.append(" ORDER BY c/context/start_time");

      return queryEhrContent(
          sb.toString(),
          (resultRow, hasNext) -> {
            final MedicationOrderComposition composition =
                RmoToTdoConverter.convert(MedicationOrderComposition.class, (RmObject)resultRow[0]);
            final MedicationInstructionInstruction instruction =
                MedicationsEhrUtils.getMedicationInstructionByEhrName(composition, (String)resultRow[1]);
            return Pair.of(composition, instruction);
          }
      );
    }
    return Lists.newArrayList();
  }

  private void appendMedicationTimingIntervalCriterion(final StringBuilder stringBuilder, final Interval searchInterval)
  {
    //(medication_start <= start && (medication_end > start || medication_end == null)) || (medication_start >= start && medication_start < end)
    stringBuilder
        .append(" AND (")
        .append("i/activities[at0001]/description[at0002]/items[at0010]/items[at0012]/value <= ")
        .append(getAqlDateTimeQuoted(searchInterval.getStart()))
        .append("AND (i/activities[at0001]/description[at0002]/items[at0010]/items[at0013]/value > ")
        .append(getAqlDateTimeQuoted(searchInterval.getStart()))
        .append("OR NOT EXISTS i/activities[at0001]/description[at0002]/items[at0010]/items[at0013]/value)")
        .append("OR (i/activities[at0001]/description[at0002]/items[at0010]/items[at0012]/value >= ")
        .append(getAqlDateTimeQuoted(searchInterval.getStart()))
        .append(" AND i/activities[at0001]/description[at0002]/items[at0010]/items[at0012]/value < ")
        .append(getAqlDateTimeQuoted(searchInterval.getEnd()))
        .append(')')
        .append(')');
  }

  public MedicationOrderComposition saveNewMedicationOrderComposition(
      final String patientId,
      final MedicationOrderComposition composition)
  {
    final String ehrId = getOrCreatePatientEhrId(patientId);
    currentSession().useEhr(ehrId);
    final Composition rmoComposition = TdoToRmoConverter.convertToCopy(composition);
    final String uid = currentSession().createComposition(rmoComposition);
    final MedicationOrderComposition tdoComposition =
        RmoToTdoConverter.convert(MedicationOrderComposition.class, rmoComposition);

    linkActionsToInstructions(tdoComposition, uid);
    currentSession().modifyComposition(uid, TdoToRmoConverter.convertToCopy(tdoComposition));
    tdoComposition.setUid(OpenEhrRefUtils.getObjectVersionId(uid));
    return tdoComposition;
  }

  public String modifyMedicationOrderComposition(final String patientId, final MedicationOrderComposition composition)
  {
    final String ehrId = getOrCreatePatientEhrId(patientId);
    currentSession().useEhr(ehrId);
    linkActionsToInstructions(composition, composition.getUid().getValue());
    return currentSession().modifyComposition(
        composition.getUid().getValue(),
        TdoToRmoConverter.convertToCopy(composition));
  }

  private void linkActionsToInstructions(final MedicationOrderComposition composition, final String compositionUid)
  {
    final MedicationInstructionInstruction instruction = composition.getMedicationDetail().getMedicationInstruction().get(0);
    for (final MedicationActionAction action : composition.getMedicationDetail().getMedicationAction())
    {
      final LocatableRef actionInstructionId = action.getInstructionDetails().getInstructionId();
      if (actionInstructionId.getPath() == null)
      {
        MedicationsEhrUtils.fillActionInstructionId(actionInstructionId, composition, instruction, compositionUid);
      }
    }
  }

  public Map<String, List<MedicationAdministrationComposition>> getTherapiesAdministrations(
      @Nonnull final String patientId,
      @Nonnull final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs,
      final Interval searchInterval)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final Map<String, List<MedicationAdministrationComposition>> administrationsMap = new HashMap<>();

      final Map<Pair<String, String>, String> locatableRefs = extractLocatableRefs(instructionPairs);
      final List<String> compositionUids = extractCompositionUids(instructionPairs);

      if (!compositionUids.isEmpty())
      {
        addTherapyAdministrations(ehrId, administrationsMap, locatableRefs, compositionUids, searchInterval, false);
        addTherapyAdministrations(ehrId, administrationsMap, locatableRefs, compositionUids, searchInterval, true);
      }
      return administrationsMap;
    }
    return new HashMap<>();
  }

  private void addTherapyAdministrations(
      @Nonnull final String ehrId,
      @Nonnull final Map<String, List<MedicationAdministrationComposition>> administrationsMap,
      @Nonnull final Map<Pair<String, String>, String> locatableRefs,
      @Nonnull final List<String> compositionUids,
      final Interval searchInterval,
      final boolean clinicalInterventions)
  {
    final StringBuilder sb = new StringBuilder();
    sb
        .append("SELECT c FROM EHR[ehr_id/value='").append(ehrId).append("']")
        .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]");
    if (clinicalInterventions)
    {
      sb.append(" CONTAINS Action a[openEHR-EHR-ACTION.procedure-zn.v1]");
    }
    else
    {
      sb.append(" CONTAINS Action a[openEHR-EHR-ACTION.medication.v1]");
    }
    sb.append(" WHERE c/name/value = 'Medication Administration'")
        .append(" AND a/instruction_details/instruction_id/id/value matches {" + getAqlQuoted(compositionUids) + '}');
    if (searchInterval != null)
    {
      sb.append(" AND a/time >= ").append(getAqlDateTimeQuoted(searchInterval.getStart()))
          .append(" AND a/time <= ").append(getAqlDateTimeQuoted(searchInterval.getEnd()));
    }

    queryEhrContent(
        sb.toString(),
        (ResultRowProcessor<Object[], Void>)(resultRow, hasNext) -> {
          final MedicationAdministrationComposition administration =
              RmoToTdoConverter.convert(MedicationAdministrationComposition.class, (RmObject)resultRow[0]);

          final Action action;
          if (!administration.getMedicationDetail().getMedicationAction().isEmpty())
          {
            action = administration.getMedicationDetail().getMedicationAction().get(0);
          }
          else if (!administration.getMedicationDetail().getClinicalIntervention().isEmpty())
          {
            action = administration.getMedicationDetail().getClinicalIntervention().get(0);
          }
          else
          {
            throw new IllegalArgumentException(
                "MedicationAdministrationComposition must have MedicationActions or ClinicalIntervention");
          }
          final LocatableRef locatableRef = action.getInstructionDetails().getInstructionId();
          final Pair<String, String> instructionId = Pair.of(locatableRef.getId().getValue(), locatableRef.getPath());
          if (locatableRefs.containsKey(instructionId))
          {
            final String therapyId = locatableRefs.get(instructionId);
            if (!administrationsMap.containsKey(therapyId))
            {
              administrationsMap.put(therapyId, new ArrayList<>());
            }
            administrationsMap.get(therapyId).add(administration);
          }
          return null;
        }
    );
  }

  private List<String> extractCompositionUids(final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs)
  {
    final List<String> compositionUids = new ArrayList<>();

    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair : instructionPairs)
    {
      compositionUids.add(
          TherapyIdUtils.getCompositionUidWithoutVersion(instructionPair.getFirst().getUid().getValue()));
    }
    return compositionUids;
  }

  private Map<Pair<String, String>, String> extractLocatableRefs(final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs)
  {
    final Map<Pair<String, String>, String> locatableRefs = new HashMap<>();

    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair : instructionPairs)
    {
      final String therapyId = TherapyIdUtils.createTherapyId(instructionPair.getFirst(), instructionPair.getSecond());
      //final LocatableRef instructionLocatableRef =
      //    MedicationsEhrUtils.createInstructionLocatableRef(instructionPair.getFirst(), instructionPair.getSecond());

      final MedicationInstructionInstruction therapyInstruction = instructionPair.getSecond();
      final MedicationOrderComposition therapyComposition = instructionPair.getFirst();

      final LocatableRef instructionLocatableRef =
          MedicationsEhrUtils.createInstructionLocatableRef(therapyComposition);
      final RmPath rmPath = TdoPathable.pathOfItem(therapyComposition, therapyInstruction);
      instructionLocatableRef.setPath(rmPath.getCanonicalString());
      final ObjectVersionId objectVersionId = new ObjectVersionId();
      final String compositionUid =
          TherapyIdUtils.getCompositionUidWithoutVersion(therapyComposition.getUid().getValue());
      objectVersionId.setValue(compositionUid);
      instructionLocatableRef.setId(objectVersionId);

      final Pair<String, String> pair =
          Pair.of(instructionLocatableRef.getId().getValue(), instructionLocatableRef.getPath());
      locatableRefs.put(pair, therapyId);
    }
    return locatableRefs;
  }

  public Opt<DateTime> getAdministrationTime(@Nonnull final String patientId, @Nonnull final String compositionUId)
  {
    Preconditions.checkNotNull(patientId, "patientId");
    Preconditions.checkNotNull(compositionUId, "compositionUId");

    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final String aql = "SELECT " +
          "a/time \n" + // action
          "FROM EHR[ehr_id/value='" + ehrId + "'] \n" +
          "CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]\n" +
          "CONTAINS Action a[openEHR-EHR-ACTION.medication.v1]\n" +
          "WHERE c/name/value = 'Medication Administration'\n" +
          "AND c/uid/value = '" + compositionUId + "'";

      return Opt.of(
          queryEhrContent(aql, (resultRow, hasNext) -> (DvDateTime)resultRow[0])
              .stream()
              .filter(Objects::nonNull)
              .findAny()
              .map(DataValueUtils::getDateTime)
              .orElse(null));
    }
    return Opt.none();
  }

  @EhrSessioned
  public MedicationOrderComposition loadMedicationOrderComposition(final String patientId, final String compositionUid)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final Composition composition = currentSession().getComposition(compositionUid);
      if (composition == null)
      {
        throw new CompositionNotFoundException(compositionUid);
      }
      return RmoToTdoConverter.convert(MedicationOrderComposition.class, composition);
    }
    return null;
  }

  public List<Interval> getPatientBaselineInfusionIntervals(final String patientId, final Interval searchInterval)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      sb
          .append(
              "SELECT i/activities[at0001]/description[at0002]/items[at0010]/items[at0012]/value, " +
                  "i/activities[at0001]/description[at0002]/items[at0010]/items[at0013]/value"
          )
          .append(" FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]")
          .append(" CONTAINS Instruction i[openEHR-EHR-INSTRUCTION.medication.v1]")
          .append(" CONTAINS Cluster cl[openEHR-EHR-CLUSTER.infusion_details.v1]")
          .append(" WHERE c/name/value = 'Medication order'")
          .append(" AND cl/items[at0007]/value/defining_code/code_string='")
          .append(InfusionAdministrationDetailsPurpose.BASELINE_ELECTROLYTE_INFUSION.getTerm().getCode()).append('\'');
      appendMedicationTimingIntervalCriterion(sb, searchInterval);

      return queryEhrContent(
          sb.toString(),
          (resultRow, hasNext) -> {
            final DvDateTime dvTherapyStart = (DvDateTime)resultRow[0];
            final DvDateTime dvTherapyEnd = (DvDateTime)resultRow[1];
            if (dvTherapyEnd != null)
            {
              return new Interval(DataValueUtils.getDateTime(dvTherapyStart), DataValueUtils.getDateTime(dvTherapyEnd));
            }
            return Intervals.infiniteFrom(DataValueUtils.getDateTime(dvTherapyStart));
          }
      );
    }
    return new ArrayList<>();
  }

  public DateTime getTherapyInstructionStart(@Nonnull final String patientId, @Nonnull final String compositionUid)
  {
    com.marand.maf.core.StringUtils.checkNotBlank(patientId, "patientId");
    com.marand.maf.core.StringUtils.checkNotBlank(compositionUid, "compositionUid");

    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      sb
          .append(" SELECT i/activities[at0001]/description[at0002]/items[at0010]/items[at0012]/value")
          .append(" FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]")
          .append(" CONTAINS Instruction i[openEHR-EHR-INSTRUCTION.medication.v1]")
          .append(" WHERE c/name/value = 'Medication order'")
          .append(" AND c/uid/value like '").append(compositionUid).append("*'");

      return queryEhrContent(sb.toString(), (resultRow, hasNext) -> DataValueUtils.getDateTime((DvDateTime)resultRow[0]))
          .stream()
          .filter(Objects::nonNull)
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("Therapy start not found for " + compositionUid));
    }
    throw new IllegalArgumentException("Patient not in ehr " + patientId);
  }

  public Double getPatientLastReferenceWeight(final String patientId, final Interval searchInterval)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      sb
          .append("SELECT o/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value")
          .append(" FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Observation o[openEHR-EHR-OBSERVATION.body_weight.v1]")
          .append(" WHERE o/name/value = 'Medication reference body weight'")
          .append(" AND o/data[at0002]/events[at0003]/time >= ")
          .append(getAqlDateTimeQuoted(searchInterval.getStart()))
          .append(" AND o/data[at0002]/events[at0003]/time <= ")
          .append(getAqlDateTimeQuoted(searchInterval.getEnd()))
          .append(" ORDER BY o/data[at0002]/events[at0003]/time DESC")
          .append(" FETCH 1");

      final List<Double> weights = query(
          sb.toString(), (resultRow, hasNext) -> {
            final DvQuantity weight = (DvQuantity)resultRow[0];
            return weight.getMagnitude();
          }
      );
      return weights.isEmpty() ? null : weights.get(0);
    }
    return null;
  }

  public void savePatientReferenceWeight(final String patientId, final MedicationReferenceWeightComposition comp)
  {
    saveSubjectComposition(patientId, comp, null);
  }

  public Pair<MedicationOrderComposition, MedicationInstructionInstruction> getTherapyInstructionPair(
      final String patientId, final String compositionUid, final String ehrOrderName)
  {
    final MedicationOrderComposition composition = loadMedicationOrderComposition(patientId, compositionUid);
    final MedicationInstructionInstruction instruction =
        MedicationsEhrUtils.getMedicationInstructionByEhrName(composition, ehrOrderName);
    return Pair.of(composition, instruction);
  }

  public String saveMedicationAdministrationComposition(
      final String patientId,
      final MedicationAdministrationComposition composition,
      final String uid)
  {
    if (uid != null)
    {
      final String latestCompositionUid = getLatestCompositionUid(patientId, uid);
      return updateSubjectComposition(patientId, composition, latestCompositionUid != null ? latestCompositionUid : uid);
    }

    return saveSubjectComposition(patientId, composition, null);
  }

  public void deleteComposition(final String patientId, final String compositionUid)
  {
    deleteSubjectComposition(patientId, compositionUid);
  }

  public void deleteTherapyAdministration(
      final String patientId, final String administrationCompositionUid, final String comment)
  {
    deleteSubjectComposition(patientId, administrationCompositionUid, comment);
  }

  public void deletePharmacistReview(final String patientId, final String pharmacistReviewUid)
  {
    deleteSubjectComposition(patientId, pharmacistReviewUid);
  }

  private String getLatestCompositionUid(final String patientId, final String compositionUid)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final String uidWithoutVersion = TherapyIdUtils.getCompositionUidWithoutVersion(compositionUid);

      final StringBuilder sb = new StringBuilder();
      sb
          .append("SELECT v/uid/value")
          .append(" FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS VERSIONED_OBJECT vo")
          .append(" CONTAINS VERSION v[all_versions]")
          .append(" WHERE vo/uid/value = '" + uidWithoutVersion + "'");

      final List<Long> versions = query(
          sb.toString(), (resultRow, hasNext) -> {
            return TherapyIdUtils.getCompositionVersion((String)resultRow[0]);
          }
      );
      if (!versions.isEmpty())
      {
        Collections.sort(versions);
        final Long latestVersion = versions.get(versions.size() - 1);
        return TherapyIdUtils.buildCompositionUid(compositionUid, latestVersion);
      }
    }
    return null;
  }

  public List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> getLinkedTherapies(
      final String patientId,
      final String compositionUid,
      final String instructionName,
      final EhrLinkType linkType)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      final String compositionUidWithoutVersion = TherapyIdUtils.getCompositionUidWithoutVersion(compositionUid);

      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      sb
          .append("SELECT c, i/name/value, i/links/target/value FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]")
          .append(" CONTAINS Instruction i[openEHR-EHR-INSTRUCTION.medication.v1]")
          .append(" WHERE c/name/value = 'Medication order'")
          .append(" AND i/links/target/value like '*" + compositionUidWithoutVersion + "*'")
          .append(" AND i/links/type/value = '" + linkType.getName() + "'");

      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> linkedTherapies = new ArrayList<>();
      queryEhrContent(
          sb.toString(),
          (ResultRowProcessor<Object[], Void>)(resultRow, hasNext) -> {
            final MedicationOrderComposition linkedComposition =
                RmoToTdoConverter.convert(MedicationOrderComposition.class, (RmObject)resultRow[0]);

            final MedicationInstructionInstruction instruction =
                MedicationsEhrUtils.getMedicationInstructionByEhrName(linkedComposition, (String)resultRow[1]);

            final String link = (String)resultRow[2];
            if (link.contains("'" + instructionName + "'"))
            {
              linkedTherapies.add(Pair.of(linkedComposition, instruction));
            }
            return null;
          }
      );

      return linkedTherapies;
    }

    return null;
  }

  public Map<String, Pair<TherapyStatusEnum, TherapyChangeReasonDto>> getLastChangeReasonsForCompositionsFromAdmission(
      final String patientId,
      final boolean onlyAbortedOrSuspended)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      final Set<String> compositionIdsWithAdmissionLink = getOrderCompositionIdsWithAdmissionLinks(patientId);
      if (compositionIdsWithAdmissionLink != null)
      {
        currentSession().useEhr(ehrId);
        final StringBuilder query = getEhrQueryForChangeReasons(
            ehrId,
            compositionIdsWithAdmissionLink,
            onlyAbortedOrSuspended ? ChangeReasonType.SUSPENDED_OR_ABORTED : ChangeReasonType.ALL);

        final Map<String, MedicationActionAction> linkedToAdmissionActionMap = new HashMap<>();
        queryEhrContent(
            query.toString(),
            (ResultRowProcessor<Object[], Void>)(resultRow, hasNext) -> {
              final MedicationOrderComposition orderComposition =
                  RmoToTdoConverter.convert(MedicationOrderComposition.class, (RmObject)resultRow[0]);

              final String linkedAdmissionId = getLinkedAdmissionCompositionIdFromOrderComposition(orderComposition);
              if (linkedAdmissionId != null)
              {
                final MedicationActionAction latestMatchingAction = MedicationsEhrUtils.getLatestActionWithChangeReason(
                    orderComposition.getMedicationDetail().getMedicationAction(), onlyAbortedOrSuspended);

                final DateTime actionDate = DataValueUtils.getDateTime(latestMatchingAction.getTime());

                if (linkedToAdmissionActionMap.containsKey(linkedAdmissionId))
                {
                  if (DataValueUtils.getDateTime(linkedToAdmissionActionMap.get(linkedAdmissionId).getTime()).isBefore(
                      actionDate))
                  {
                    linkedToAdmissionActionMap.put(linkedAdmissionId, latestMatchingAction);
                  }
                }
                else
                {
                  linkedToAdmissionActionMap.put(linkedAdmissionId, latestMatchingAction);
                }
              }
              return null;
            }
        );

        final Map<String, Pair<TherapyStatusEnum, TherapyChangeReasonDto>> linkedToAdmissionReasonAndStatusMap = new HashMap<>();
        for (final Map.Entry<String, MedicationActionAction> mapEntry : linkedToAdmissionActionMap.entrySet())
        {
          final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(mapEntry.getValue());
          TherapyStatusEnum statusEnum = null;
          if (actionEnum == MedicationActionEnum.ABORT)
          {
            statusEnum = TherapyStatusEnum.ABORTED;
          }
          else if (actionEnum == MedicationActionEnum.CANCEL)
          {
            statusEnum = TherapyStatusEnum.CANCELLED;
          }
          else if (actionEnum == MedicationActionEnum.SUSPEND)
          {
            statusEnum = TherapyStatusEnum.SUSPENDED;
          }
          else if (actionEnum == MedicationActionEnum.MODIFY_EXISTING || actionEnum == MedicationActionEnum.COMPLETE)
          {
            statusEnum = TherapyStatusEnum.NORMAL;
          }

          linkedToAdmissionReasonAndStatusMap.put(
              mapEntry.getKey(),
              Pair.of(statusEnum, MedicationsEhrUtils.getTherapyChangeReasonDtoFromAction(mapEntry.getValue())));
        }
        return linkedToAdmissionReasonAndStatusMap;
      }
    }
    return new HashMap<>();
  }

  private String getLinkedAdmissionCompositionIdFromOrderComposition(final MedicationOrderComposition orderComposition)
  {
    final MedicationInstructionInstruction instruction = orderComposition.getMedicationDetail()
        .getMedicationInstruction()
        .get(0);

    final List<Link> admissionLinks = MedicationsEhrUtils.getLinksOfType(instruction, EhrLinkType.MEDICATION_ON_ADMISSION);
    if (!admissionLinks.isEmpty())
    {
      final String targetCompositionId = MedicationsEhrUtils.getTargetCompositionIdFromLink(admissionLinks.get(0));
      return TherapyIdUtils.getCompositionUidWithoutVersion(targetCompositionId);
    }
    return null;
  }

  public Map<String, Pair<DateTime, TherapyChangeReasonDto>> getLastEditChangeReasonsForCompositionsFromAdmission(
      final String patientId)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      final Set<String> compositionIdsWithAdmissionLink = getOrderCompositionIdsWithAdmissionLinks(patientId);
      if (compositionIdsWithAdmissionLink != null)
      {
        currentSession().useEhr(ehrId);
        final StringBuilder query = getEhrQueryForChangeReasons(
            ehrId,
            compositionIdsWithAdmissionLink,
            ChangeReasonType.MODIFIED_ONLY);

        final Map<String, Pair<DateTime, TherapyChangeReasonDto>> resultReasonsMap = new HashMap<>();

        queryEhrContent(
            query.toString(),
            (ResultRowProcessor<Object[], Void>)(resultRow, hasNext) -> {
              final MedicationOrderComposition orderComposition =
                  RmoToTdoConverter.convert(MedicationOrderComposition.class, (RmObject)resultRow[0]);

              final String linkedAdmissionId = getLinkedAdmissionCompositionIdFromOrderComposition(orderComposition);

              final MedicationActionAction latestModifyAction = MedicationsEhrUtils.getLatestModifyAction(
                  orderComposition.getMedicationDetail().getMedicationAction());

              final TherapyChangeReasonDto changeReasonDto = MedicationsEhrUtils.getTherapyChangeReasonDtoFromAction(
                  latestModifyAction);

              final DateTime actionDate = DataValueUtils.getDateTime(latestModifyAction.getTime());

              if (resultReasonsMap.containsKey(linkedAdmissionId))
              {
                if (resultReasonsMap.get(linkedAdmissionId).getFirst().isBefore(actionDate))
                {
                  resultReasonsMap.put(linkedAdmissionId, Pair.of(actionDate, changeReasonDto));
                }
              }
              else
              {
                resultReasonsMap.put(linkedAdmissionId, Pair.of(actionDate, changeReasonDto));
              }
              return null;
            }
        );

        return resultReasonsMap;
      }
    }
    return new HashMap<>();
  }

  private StringBuilder getEhrQueryForChangeReasons(
      final String ehrId,
      final Set<String> compositionIdsWithAdmissionLink,
      final ChangeReasonType changeReasonType)
  {
    final StringBuilder sb = new StringBuilder();
    sb
        .append("SELECT c FROM EHR[ehr_id/value='").append(ehrId).append("']")
        .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]")
        .append(" CONTAINS Action a[openEHR-EHR-ACTION.medication.v1]")
        .append(" WHERE c/name/value = 'Medication order'");

    if (changeReasonType == ChangeReasonType.ALL)
    {
      sb
          .append(" AND (a/ism_transition/careflow_step/defining_code/code_string = 'at0041'")
          .append(" OR a/ism_transition/careflow_step/defining_code/code_string = 'at0039')")
          .append(" OR (a/ism_transition/careflow_step/defining_code/code_string = 'at0015'")
          .append(" OR a/ism_transition/careflow_step/defining_code/code_string = 'at0012'")
          .append(" OR a/ism_transition/careflow_step/defining_code/code_string = 'at0010'")
          .append(" OR a/ism_transition/careflow_step/defining_code/code_string = 'at0009')");
    }
    else if (changeReasonType == ChangeReasonType.MODIFIED_ONLY)
    {
      sb
          .append(" AND (a/ism_transition/careflow_step/defining_code/code_string = 'at0041'")
          .append(" OR a/ism_transition/careflow_step/defining_code/code_string = 'at0039')");
    }
    else
    {
      sb
          .append(" AND (a/ism_transition/careflow_step/defining_code/code_string = 'at0015'")
          .append(" OR a/ism_transition/careflow_step/defining_code/code_string = 'at0012'")
          .append(" OR a/ism_transition/careflow_step/defining_code/code_string = 'at0010'")
          .append(" OR a/ism_transition/careflow_step/defining_code/code_string = 'at0009')");
    }
    sb
        .append(" AND c/uid/value matches {" + getAqlQuoted(compositionIdsWithAdmissionLink) + '}');

    return sb;
  }

  private Set<String> getOrderCompositionIdsWithAdmissionLinks(final String patientId)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      final EhrLinkType linkType = EhrLinkType.MEDICATION_ON_ADMISSION;
      sb
          .append("SELECT c/uid/value FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]")
          .append(" CONTAINS Instruction i[openEHR-EHR-INSTRUCTION.medication.v1]")
          .append(" WHERE c/name/value = 'Medication order'")
          .append(" AND i/links/type/value = '" + linkType.getName() + "'");

      final Set<String> compositions = new HashSet<>();
      queryEhrContent(
          sb.toString(),
          (ResultRowProcessor<Object[], Void>)(resultRow, hasNext) -> {
            compositions.add((String)resultRow[0]);
            return null;
          }
      );
      return compositions.isEmpty() ? null : compositions;
    }
    return null;
  }

  public String saveComposition(
      final String patientId, final Composition composition, final String uid)
  {
    if (uid != null)
    {
      return updateSubjectComposition(patientId, composition, uid);
    }
    return saveSubjectComposition(patientId, composition, null);
  }

  public Map<String, List<NamedExternalDto>> getTemplateTerms(
      final String templateId, final Set<String> pathIds, final Locale locale)
  {
    final Map<String, List<NamedExternalDto>> localisationMap = new HashMap<>();

    final WebTemplate webTemplate = getWebTemplate(templateId);

    for (final String id : pathIds)
    {
      final List<CodedValueWithDescription> codesWithDescriptions =
          webTemplate.getCodesWithDescriptions(id, locale.getLanguage());
      final List<NamedExternalDto> namedIdentities = new ArrayList<>();
      for (final CodedValueWithDescription codeWithDescription : codesWithDescriptions)
      {
        namedIdentities.add(new NamedExternalDto(codeWithDescription.getValue(), codeWithDescription.getDescription()));
      }

      localisationMap.put(id, namedIdentities);
    }

    return localisationMap;
  }

  public List<PharmacyReviewReportComposition> findPharmacistsReviewCompositions(
      final String patientId,
      final DateTime fromDate)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      sb
          .append("SELECT c FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.report.v1]")
          .append(" WHERE c/name/value = 'Pharmacy review report'")
          .append(" AND c/context/start_time > " + getAqlDateTimeQuoted(fromDate))
          .append(" ORDER BY c/context/start_time DESC");

      return queryEhrContent(sb.toString(), PharmacyReviewReportComposition.class);
    }
    return Lists.newArrayList();
  }

  public PharmacyReviewReportComposition loadPharmacistsReviewComposition(
      final String patientId,
      final String compositionUid)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final Composition composition = currentSession().getComposition(compositionUid);
      return RmoToTdoConverter.convert(PharmacyReviewReportComposition.class, composition);
    }
    return null;
  }

  public List<MedicationOnAdmissionComposition> findMedicationOnAdmissionCompositions(
      final String patientId,
      final DateTime fromDate)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      sb
          .append("SELECT c FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]")
          .append(" WHERE c/name/value = 'Medication on admission'")
          .append(" AND c/context/start_time > " + getAqlDateTimeQuoted(fromDate))
          .append(" ORDER BY c/context/start_time DESC");

      return queryEhrContent(sb.toString(), MedicationOnAdmissionComposition.class);
    }
    return Lists.newArrayList();
  }

  public MedicationOnAdmissionComposition loadMedicationOnAdmissionComposition(
      final String patientId,
      final String compositionUid)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final Composition composition = currentSession().getComposition(compositionUid);
      return RmoToTdoConverter.convert(MedicationOnAdmissionComposition.class, composition);
    }
    return null;
  }

  public List<MedicationOnDischargeComposition> findMedicationOnDischargeCompositions(
      final String patientId, final Interval interval)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      sb
          .append("SELECT c FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]")
          .append(" WHERE c/name/value = 'Medication on discharge'")
          .append(" AND c/context/start_time > " + getAqlDateTimeQuoted(interval.getStart()))
          .append(" AND c/context/start_time < " + getAqlDateTimeQuoted(interval.getEnd()))
          .append(" ORDER BY c/context/start_time DESC");

      return queryEhrContent(sb.toString(), MedicationOnDischargeComposition.class);
    }
    return Lists.newArrayList();
  }

  public MedicationOnDischargeComposition loadMedicationOnDischargeComposition(
      final String patientId,
      final String compositionUid)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final Composition composition = currentSession().getComposition(compositionUid);
      return RmoToTdoConverter.convert(MedicationOnDischargeComposition.class, composition);
    }
    return null;
  }

  public Opt<MedicationConsentFormComposition> findLatestConsentFormComposition(@Nonnull final String patientId)
  {
    Preconditions.checkNotNull(patientId, "patientId must not be null!");

    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);

      final List<MedicationConsentFormComposition> result = queryEhrContent(
          getConsentFormQuery(ehrId, null, 1),
          MedicationConsentFormComposition.class);

      return result.isEmpty() ? Opt.none() : Opt.of(result.get(0));
    }

    return Opt.none();
  }

  public Collection<MedicationConsentFormComposition> findMedicationConsentFormCompositions(
      @Nonnull final String patientId,
      final Interval interval,
      final Integer fetchCount)
  {
    Preconditions.checkNotNull(patientId, "patientId must not be null!");

    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      return queryEhrContent(getConsentFormQuery(ehrId, interval, fetchCount), MedicationConsentFormComposition.class);
    }

    return Collections.emptyList();
  }

  private String getConsentFormQuery(final String ehrId, final Interval interval, final Integer fetchCount)
  {
    final StringBuilder query = new StringBuilder()
        .append("SELECT c FROM EHR[ehr_id/value='").append(ehrId).append("']")
        .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]")
        .append(" WHERE c/name/value = 'Medication consent form'");

    if (interval != null)
    {
      query
          .append(" AND c/context/start_time > " + getAqlDateTimeQuoted(interval.getStart()))
          .append(" AND c/context/start_time < " + getAqlDateTimeQuoted(interval.getEnd()));
    }

    query.append(" ORDER BY c/context/start_time DESC");

    if (fetchCount != null)
    {
      query.append(" FETCH " + fetchCount);
    }

    return query.toString();
  }

  public MedicationConsentFormComposition loadConsentFormComposition(
      @Nonnull final String patientId,
      @Nonnull final String compositionUid)
  {
    Preconditions.checkNotNull(patientId, "patientId must not be null!");
    Preconditions.checkNotNull(compositionUid, "compositionUid must not be null!");

    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final Composition composition = currentSession().getComposition(compositionUid);
      if (composition == null)
      {
        throw new CompositionNotFoundException(compositionUid);
      }
      return RmoToTdoConverter.convert(MedicationConsentFormComposition.class, composition);
    }
    throw new CompositionNotFoundException(compositionUid);
  }

  public EPrescriptionSloveniaComposition loadEPrescriptionSloveniaComposition(
      final String patientId,
      final String compositionUid)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final Composition composition = currentSession().getComposition(compositionUid);
      return RmoToTdoConverter.convert(EPrescriptionSloveniaComposition.class, composition);
    }
    return null;
  }

  public List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> getTherapyInstructionPairs(
      final Set<String> compositionUids)
  {
    if (!compositionUids.isEmpty())
    {
      final StringBuilder aqlQuerry = new StringBuilder();
      aqlQuerry
          .append("SELECT c FROM EHR e")
          .append(" CONTAINS VERSIONED_OBJECT vo")
          .append(" CONTAINS VERSION v")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]")
          .append(" WHERE c/name/value = 'Medication order'")
          .append(" AND vo/uid/value matches {'");

      aqlQuerry.append(StringUtils.join(compositionUids, "', '")).append("'}");

      return query(
          aqlQuerry.toString(),
          (resultRow, hasNext) -> {
            final MedicationOrderComposition medicationOrderComposition =
                RmoToTdoConverter.convert(MedicationOrderComposition.class, (RmObject)resultRow[0]);
            // we only have one instruction per composition
            return Pair.of(
                medicationOrderComposition,
                medicationOrderComposition.getMedicationDetail().getMedicationInstruction().get(0));
          }
      );
    }
    return Collections.emptyList();
  }

  public List<MedicationOrderComposition> getAllMedicationOrderCompositionVersions(@Nonnull final String compositionUid)
  {
    Preconditions.checkNotNull(compositionUid, "compositionUid is required");
    final List<Composition> compositions =
        currentSession().getAllCompositionVersions(TherapyIdUtils.getCompositionUidWithoutVersion(compositionUid));
    return compositions.stream()
        .map(c -> convertToTdo(c, MedicationOrderComposition.class))
        .sorted((c1, c2) -> c1.getUid().getValue().compareTo(c2.getUid().getValue()))
        .collect(Collectors.toList());
  }

  public Map<String, MedicationOrderComposition> getLatestCompositionsForOriginalCompositionUids(
      final Set<String> originalCompositionUids,
      final Set<String> patientIds,
      final int searchIntervalInWeeks,
      final DateTime when)
  {
    final Map<String, MedicationOrderComposition> originalUidWithLatestCompositionMap = new HashMap<>();

    if (originalCompositionUids.isEmpty() || patientIds.isEmpty())
    {
      return originalUidWithLatestCompositionMap;
    }

    final Map<String, String> ehrIdsMap = getEhrIds(patientIds);
    final Set<String> ehrIds = new HashSet<>(ehrIdsMap.values());

    final StringBuilder sb = new StringBuilder();
    sb
        .append("SELECT c/uid/value, i/links/target/value, i/links/type/value, c FROM EHR e")
        .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]")
        .append(" CONTAINS Instruction i[openEHR-EHR-INSTRUCTION.medication.v1]")
        .append(" WHERE c/name/value = 'Medication order'");

    appendMedicationTimingIntervalCriterion(sb, Intervals.infiniteFrom(when.minusWeeks(searchIntervalInWeeks).withTimeAtStartOfDay()));
    //TODO NEJC change to correct time - to load correct therapies

    sb
        .append(" AND e/ehr_id/value matches {" + getAqlQuoted(ehrIds) + '}')
        .append(" ORDER BY c/context/start_time DESC");

    query(
        sb.toString(),
        (ResultRowProcessor<Object[], Void>)(resultRow, hasNext) -> {
          final String compositionId = (String)resultRow[0];
          final String linkTarget = (String)resultRow[1];
          final String linkType = (String)resultRow[2];
          final RmObject orderCompositionObject = (RmObject)resultRow[3];

          final String compositionUidWithoutVersion = TherapyIdUtils.getCompositionUidWithoutVersion(compositionId);
          if (originalCompositionUids.contains(compositionUidWithoutVersion) && !originalUidWithLatestCompositionMap.containsKey(
              compositionUidWithoutVersion))
          {
            originalUidWithLatestCompositionMap.put(
                compositionUidWithoutVersion,
                RmoToTdoConverter.convert(MedicationOrderComposition.class, orderCompositionObject));
          }
          else if (linkTarget != null && linkType.equals(EhrLinkType.ORIGIN.getName()))
          {
            final OpenEhrRefUtils.EhrUriComponents ehrUri = OpenEhrRefUtils.parseEhrUri(linkTarget);
            final String linkedCompositionUidWithoutVersion =
                TherapyIdUtils.getCompositionUidWithoutVersion(ehrUri.getCompositionId());

            if (originalCompositionUids.contains(linkedCompositionUidWithoutVersion) &&
                !originalUidWithLatestCompositionMap.containsKey(linkedCompositionUidWithoutVersion))
            {
              originalUidWithLatestCompositionMap.put(
                  linkedCompositionUidWithoutVersion,
                  RmoToTdoConverter.convert(MedicationOrderComposition.class, orderCompositionObject));
            }
          }
          return null;
        }
    );
    return originalUidWithLatestCompositionMap;
  }

  @EhrSessioned
  public Collection<TherapyAutomaticChartingDto> getAutoChartingTherapyDtos(final DateTime when)
  {
    final StringBuilder sb = new StringBuilder();
    sb
        .append("SELECT c/uid/value, " +
                    "i/name/value, " +
                    "e/ehr_status/subject/external_ref/id/value, " +
                    "i/activities/description/items/value/formalism")

        .append(" FROM EHR e")
        .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]")
        .append(" CONTAINS Instruction i[openEHR-EHR-INSTRUCTION.medication.v1]")
        .append(" WHERE c/name/value = 'Medication order'");

    appendMedicationTimingIntervalCriterion(sb, Intervals.infiniteFrom(when.minusDays(1).withTimeAtStartOfDay()));

    //TODO NEJC SAVE DIFFERENTLY when new templates!
    sb
        .append(" AND i/activities/description/items/name = 'Parsable dose description'")
        .append(" AND i/activities/description/items/value/value = '"
                    + SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED.name() + "'");

    final List<TherapyAutomaticChartingDto> autoChartingTherapies = new ArrayList<>();
    try (EhrResult ehrResult = new QueryBuilder(ehrQueryService, ehrSessionManager.getSessionId())
        .poll(100L, 1000L * 30L)
        .withAql(sb.toString())
        .execute())
    {
      while (ehrResult.isActive())
      {
        if (ehrResult.hasNext(100L))
        {
          final EhrResultRow resultRow = ehrResult.next();

          final String compositionUid = (String)resultRow.get(0);
          final String instructionName = (String)resultRow.get(1);
          final String patientId = (String)resultRow.get(2);
          final String jsonDateTime = (String)resultRow.get(3);

          final SelfAdminAutomaticChartingDto dto = new SelfAdminAutomaticChartingDto(
              compositionUid,
              instructionName,
              patientId,
              JsonUtil.fromJson(jsonDateTime, DateTime.class),
              SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED);

          autoChartingTherapies.add(dto);
        }
      }
    }

    final List<TherapyAutomaticChartingDto> autoChartingNormalInfusions = getAutoChartingTherapiesWithRate(when);
    return joinAutoChartingDtos(autoChartingTherapies, autoChartingNormalInfusions);
  }

  private Collection<TherapyAutomaticChartingDto> joinAutoChartingDtos(
      final List<TherapyAutomaticChartingDto> collection1,
      final List<TherapyAutomaticChartingDto> collection2)
  {
    final Set<String> collection1Ids = collection1
        .stream()
        .map(TherapyAutomaticChartingDto::getCompositionUid)
        .collect(Collectors.toSet());

    final List<TherapyAutomaticChartingDto> result = new ArrayList<>(collection1);
    result.addAll(collection2.stream()
                      .filter(a -> !collection1Ids.contains(a.getCompositionUid()))
                      .collect(Collectors.toList()));
    return result;
  }

  private List<TherapyAutomaticChartingDto> getAutoChartingTherapiesWithRate(final DateTime when)
  {
    final StringBuilder sb = new StringBuilder();
    sb
        .append("SELECT " +
                    "c/uid/value, " +
                    "i/name/value, " +
                    "e/ehr_status/subject/external_ref/id/value")

        .append(" FROM EHR e")
        .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]")
        .append(" CONTAINS Instruction i[openEHR-EHR-INSTRUCTION.medication.v1]")
        .append(" CONTAINS Cluster a[openEHR-EHR-CLUSTER.medication_admin.v1]")
        .append(" CONTAINS Cluster d[openEHR-EHR-CLUSTER.infusion_details.v1]")
        .append(" WHERE c/name/value = 'Medication order'")
        .append(" AND EXISTS d/items[at0001]/value") // has rate
        .append(" AND d/items[at0001]/value/value != 'BOLUS'") // is not BOLUS
        .append(" AND NOT EXISTS a/items[at0003]/value"); // delivery method is empty

    appendMedicationTimingIntervalCriterion(sb, Intervals.infiniteFrom(when.minusDays(1).withTimeAtStartOfDay()));

    final List<TherapyAutomaticChartingDto> autoChartingNormalInfusions = new ArrayList<>();
    try (EhrResult ehrResult = new QueryBuilder(ehrQueryService, ehrSessionManager.getSessionId())
        .poll(100L, 1000L * 30L)
        .withAql(sb.toString())
        .execute())
    {
      while (ehrResult.isActive())
      {
        if (ehrResult.hasNext(100L))
        {
          final EhrResultRow resultRow = ehrResult.next();

          final NormalInfusionAutomaticChartingDto dto = new NormalInfusionAutomaticChartingDto(
              (String)resultRow.get(0),
              (String)resultRow.get(1),
              (String)resultRow.get(2));

          autoChartingNormalInfusions.add(dto);
        }
      }
    }

    return autoChartingNormalInfusions;
  }

  @EhrSessioned
  public Map<Pair<MedicationOrderComposition, MedicationInstructionInstruction>, String> getActiveInstructionPairsWithPatientIds(
      final DateTime when)
  {
    final StringBuilder sb = new StringBuilder();
    sb
        .append("SELECT c, i/name/value, e/ehr_status/subject/external_ref/id/value FROM EHR e")
        .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]")
        .append(" CONTAINS Instruction i[openEHR-EHR-INSTRUCTION.medication.v1]")
        .append(" WHERE c/name/value = 'Medication order'")
        .append(" AND c/context/start_time > ")
        .append(getAqlDateTimeQuoted(when.minusMonths(6)));

    appendMedicationTimingIntervalCriterion(sb, Intervals.infiniteFrom(when));

    final Map<Pair<MedicationOrderComposition, MedicationInstructionInstruction>, String> instructionPairWithPatientIds = new HashMap<>();

    try (EhrResult ehrResult = new QueryBuilder(ehrQueryService, ehrSessionManager.getSessionId())
        .poll(100L, 1000L * 30L)
        .withAql(sb.toString())
        .execute())
    {
      while (ehrResult.isActive())
      {
        if (ehrResult.hasNext(100L))
        {
          final EhrResultRow resultRow = ehrResult.next();
          final MedicationOrderComposition composition =
              RmoToTdoConverter.convert(MedicationOrderComposition.class, (RmObject)resultRow.get(0));
          final MedicationInstructionInstruction instruction =
              MedicationsEhrUtils.getMedicationInstructionByEhrName(composition, (String)resultRow.get(1));
          final String patientId = (String)resultRow.get(2);

          instructionPairWithPatientIds.put(Pair.of(composition, instruction), patientId);
        }
      }
    }

    return instructionPairWithPatientIds;
  }

  public Integer getPatientsCurrentBnfMaximumSum(final DateTime when, final String patientId)
  {
    Preconditions.checkNotNull(patientId, "patient id cannot be null");

    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      // TODO nejc change query when new templates are implemented. Currently location where BNF max data is stored is not correct.
      sb
          .append("SELECT i/protocol/items/value FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]")
          .append(" CONTAINS Instruction i[openEHR-EHR-INSTRUCTION.medication.v1]")
          .append(" WHERE c/name/value = 'Medication order'")
          .append("AND i/protocol/items/name = 'Concession benefit'");

      appendMedicationTimingIntervalCriterion(sb, Intervals.infiniteFrom(when));

      final List<Integer> bnfMaximums = new ArrayList<>();
      query(
          sb.toString(),
          (ResultRowProcessor<Object[], Void>)(resultRow, hasNext) -> {
            final Integer therapyBnfMaximum = Integer.valueOf(((DvText)resultRow[0]).getValue());
            bnfMaximums.add(therapyBnfMaximum);
            return null;
          }
      );

      if (bnfMaximums.isEmpty())
      {
        return 0;
      }
      else
      {
        int bnfMaximumSum = 0;
        for (final Integer bnfMaximum : bnfMaximums)
        {
          bnfMaximumSum += bnfMaximum;
        }
        return bnfMaximumSum;
      }
    }
    return null;
  }

  public List<EPrescriptionSloveniaComposition> findEERPrescriptions(
      final String patientId, final Integer numberOfResults)
  {
    Preconditions.checkNotNull(patientId, "patient id cannot be null");

    final String ehrId = currentSession().findEhr(patientId);
    if (StringUtils.isEmpty(ehrId))
    {
      return Collections.emptyList();
    }
    currentSession().useEhr(ehrId);
    final StringBuilder sb = new StringBuilder();
    sb
        .append("SELECT c FROM EHR[ehr_id/value='").append(ehrId).append("']")
        .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]")
        .append(" WHERE c/name/value = 'ePrescription (Slovenia)'")
        .append(" ORDER BY c/context/start_time desc")
        .append(" FETCH " + numberOfResults);

    return queryEhrContent(sb.toString(), EPrescriptionSloveniaComposition.class);
  }

  public void appendWarningsToTherapy(
      @Nonnull final String patientId,
      @Nonnull final String therapyId,
      @Nonnull final Collection<String> warnings)
  {
    com.marand.maf.core.StringUtils.checkNotBlank(patientId, "patientId must be defined");
    com.marand.maf.core.StringUtils.checkNotBlank(therapyId, "therapyId must be defined");
    Preconditions.checkNotNull(warnings, "warnings must not be null");

    final Pair<String, String> therapyIdPair = TherapyIdUtils.parseTherapyId(therapyId);
    final MedicationOrderComposition composition = loadMedicationOrderComposition(patientId, therapyIdPair.getFirst());
    final OrderActivity orderActivity = MedicationsEhrUtils.getMedicationInstructionByEhrName(
        composition,
        therapyIdPair.getSecond()).getOrder().get(0);

    final String prefix = TherapyCommentEnum.getFullString(TherapyCommentEnum.WARNING) + " ";
    warnings.forEach(w -> orderActivity.getComment().add(DataValueUtils.getText(prefix + w)));

    updateSubjectComposition(patientId, composition, composition.getUid().getValue());
  }

  private enum ChangeReasonType
  {
    MODIFIED_ONLY, SUSPENDED_OR_ABORTED, ALL
  }
}
