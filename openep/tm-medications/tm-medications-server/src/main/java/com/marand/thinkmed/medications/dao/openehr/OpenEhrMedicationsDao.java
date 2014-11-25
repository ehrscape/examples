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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.marand.maf.core.Pair;
import com.marand.maf.core.openehr.dao.OpenEhrDaoSupport;
import com.marand.maf.core.openehr.util.InstructionTranslator;
import com.marand.maf.core.resultrow.ProcessingException;
import com.marand.maf.core.resultrow.ResultRowProcessor;
import com.marand.maf.core.server.catalog.dao.CatalogDao;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationAdministrationComposition;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.medications.tdo.MedicationReferenceWeightComposition;
import com.marand.openehr.rm.RmObject;
import com.marand.openehr.rm.RmPath;
import com.marand.openehr.rm.TdoPathable;
import com.marand.openehr.tdo.conversion.RmoToTdoConverter;
import com.marand.openehr.tdo.conversion.TdoToRmoConverter;
import com.marand.openehr.util.DataValueUtils;
import com.marand.openehr.util.OpenEhrLinkType;
import com.marand.openehr.util.OpenEhrRefUtils;
import com.marand.thinkmed.medications.business.impl.MedicationsEhrUtils;
import com.marand.thinkmed.medications.dao.EhrMedicationsDao;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.Interval;
import org.openehr.jaxb.rm.Action;
import org.openehr.jaxb.rm.Composition;
import org.openehr.jaxb.rm.DvDateTime;
import org.openehr.jaxb.rm.DvEhrUri;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.Link;
import org.openehr.jaxb.rm.LocatableRef;
import org.openehr.jaxb.rm.ObjectVersionId;
import org.springframework.util.Assert;

import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.InfusionAdministrationDetailsPurpose;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction;

/**
 * @author Bostjan Vester
 */
public class OpenEhrMedicationsDao extends OpenEhrDaoSupport<Long> implements EhrMedicationsDao
{
  private CatalogDao catalogDao;

  public void setCatalogDao(final CatalogDao catalogDao)
  {
    this.catalogDao = catalogDao;
  }

  @Override
  public void afterPropertiesSet() throws Exception
  {
    super.afterPropertiesSet();
    Assert.notNull(catalogDao, "catalogDao is required");
  }

  @Override
  public List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> findMedicationInstructions(
      final long patientId, final Interval searchInterval, final Long centralCaseId)
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
                    + Long.toString(centralCaseId) + "'"
            );
      }
      sb.append(" ORDER BY c/context/start_time");

      return queryEhrContent(
          sb.toString(),
          new ResultRowProcessor<Object[], Pair<MedicationOrderComposition, MedicationInstructionInstruction>>()
          {
            @Override
            public Pair<MedicationOrderComposition, MedicationInstructionInstruction> process(
                final Object[] resultRow, final boolean hasNext) throws ProcessingException
            {
              final MedicationOrderComposition composition =
                  RmoToTdoConverter.convert(MedicationOrderComposition.class, (RmObject)resultRow[0]);
              final MedicationInstructionInstruction instruction =
                  MedicationsEhrUtils.getMedicationInstructionByEhrName(composition, (String)resultRow[1]);
              return Pair.of(composition, instruction);
            }
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

  @Override
  public MedicationOrderComposition saveNewMedicationOrderComposition(
      final long patientId,
      final MedicationOrderComposition composition)
  {
    String ehrId = currentSession().findEhr(patientId);
    if (StringUtils.isEmpty(ehrId))
    {
      ehrId = currentSession().createSubjectEhr(patientId);
    }
    currentSession().useEhr(ehrId);
    final Composition rmoComposition = TdoToRmoConverter.convertToCopy(composition);
    final String uid = currentSession().createComposition(rmoComposition);
    final MedicationOrderComposition tdoComposition =
        RmoToTdoConverter.convert(MedicationOrderComposition.class, rmoComposition);

    fixInstructionTherapyLinks(tdoComposition, uid);
    linkActionsToInstructions(tdoComposition, uid);
    currentSession().modifyComposition(uid, (Composition)TdoToRmoConverter.convertToCopy(tdoComposition));
    tdoComposition.setUid(OpenEhrRefUtils.getObjectVersionId(uid));
    return tdoComposition;
  }

  @Override
  public String modifyMedicationOrderComposition(final long patientId, final MedicationOrderComposition composition)
  {
    String ehrId = currentSession().findEhr(patientId);
    if (StringUtils.isEmpty(ehrId))
    {
      ehrId = currentSession().createSubjectEhr(patientId);
    }
    currentSession().useEhr(ehrId);
    linkActionsToInstructions(composition, composition.getUid().getValue());
    return currentSession().modifyComposition(
        composition.getUid().getValue(),
        (Composition)TdoToRmoConverter.convertToCopy(composition));
  }

  private void fixInstructionTherapyLinks(final MedicationOrderComposition composition, final String uid)
  {
    for (final MedicationInstructionInstruction linkedInstruction :
        composition.getMedicationDetail().getMedicationInstruction())
    {
      for (final Link link : linkedInstruction.getLinks())
      {
        if (link.getType().getValue().equals(OpenEhrLinkType.ISSUE.getName()))     //todo change type after fix
        {
          final int index = Integer.parseInt(link.getTarget().getValue());
          final MedicationInstructionInstruction instruction =
              composition.getMedicationDetail().getMedicationInstruction().get(index);

          final RmPath rmPath = TdoPathable.pathOfItem(composition, instruction);
          final DvEhrUri linkEhrUri = DataValueUtils.getEhrUri(uid, rmPath);
          link.setTarget(linkEhrUri);
        }
      }
    }
  }

  private void linkActionsToInstructions(final MedicationOrderComposition composition, final String uid)
  {
    for (final MedicationActionAction action : composition.getMedicationDetail().getMedicationAction())
    {
      final LocatableRef actionInstructionId = action.getInstructionDetails().getInstructionId();
      if (NumberUtils.isDigits(actionInstructionId.getPath()))
      {
        final int instructionIndex = Integer.parseInt(actionInstructionId.getPath());
        final MedicationInstructionInstruction instruction =
            composition.getMedicationDetail().getMedicationInstruction().get(instructionIndex);
        final RmPath rmPath = TdoPathable.pathOfItem(composition, instruction);
        actionInstructionId.setPath(rmPath.getCanonicalString());
        final ObjectVersionId objectVersionId = new ObjectVersionId();
        objectVersionId.setValue(uid);
        actionInstructionId.setId(objectVersionId);
      }
    }
  }

  @Override
  public Map<String, List<MedicationAdministrationComposition>> getTherapiesAdministrations(
      final Long patientId,
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs)
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
        addTherapyAdministrations(ehrId, administrationsMap, locatableRefs, compositionUids, false);
        addTherapyAdministrations(ehrId, administrationsMap, locatableRefs, compositionUids, true);
      }
      return administrationsMap;
    }
    return new HashMap<>();
  }

  private void addTherapyAdministrations(
      final String ehrId,
      final Map<String, List<MedicationAdministrationComposition>> administrationsMap,
      final Map<Pair<String, String>, String> locatableRefs,
      final List<String> compositionUids,
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

    queryEhrContent(
        sb.toString(),
        new ResultRowProcessor<Object[], Void>()
        {
          @Override
          public Void process(final Object[] resultRow, final boolean hasNext) throws ProcessingException
          {
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
                administrationsMap.put(therapyId, new ArrayList<MedicationAdministrationComposition>());
              }
              administrationsMap.get(therapyId).add(administration);
            }
            return null;
          }
        }
    );
  }

  private List<String> extractCompositionUids(final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs)
  {
    final List<String> compositionUids = new ArrayList<>();

    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair : instructionPairs)
    {
      compositionUids.add(
          InstructionTranslator.getCompositionUidWithoutVersion(instructionPair.getFirst().getUid().getValue()));
    }
    return compositionUids;
  }

  private Map<Pair<String, String>, String> extractLocatableRefs(final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs)
  {
    final Map<Pair<String, String>, String> locatableRefs = new HashMap<>();

    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair : instructionPairs)
    {
      final String therapyId = InstructionTranslator.translate(instructionPair.getSecond(), instructionPair.getFirst());
      //final LocatableRef instructionLocatableRef =
      //    MedicationsEhrUtils.createInstructionLocatableRef(instructionPair.getFirst(), instructionPair.getSecond());

      final MedicationInstructionInstruction therapyInstruction = instructionPair.getSecond();
      final MedicationOrderComposition therapyComposition = instructionPair.getFirst();

      final LocatableRef instructionLocatableRef =
          MedicationsEhrUtils.createInstructionLocatableRef(therapyComposition, therapyInstruction);
      final RmPath rmPath = TdoPathable.pathOfItem(therapyComposition, therapyInstruction);
      instructionLocatableRef.setPath(rmPath.getCanonicalString());
      final ObjectVersionId objectVersionId = new ObjectVersionId();
      final String compositionUid =
          InstructionTranslator.getCompositionUidWithoutVersion(therapyComposition.getUid().getValue());
      objectVersionId.setValue(compositionUid);
      instructionLocatableRef.setId(objectVersionId);

      final Pair<String, String> pair =
          Pair.of(instructionLocatableRef.getId().getValue(), instructionLocatableRef.getPath());
      locatableRefs.put(pair, therapyId);
    }
    return locatableRefs;
  }

  @Override
  public MedicationOrderComposition loadMedicationOrderComposition(final long patientId, final String compositionUid)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      return RmoToTdoConverter.convert(MedicationOrderComposition.class, currentSession().getComposition(compositionUid));
    }
    return null;
  }

  @Override
  public List<Interval> getPatientBaselineInfusionIntervals(final Long patientId, final Interval searchInterval)
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
          new ResultRowProcessor<Object[], Interval>()
          {

            @Override
            public Interval process(final Object[] resultRow, final boolean hasNext) throws ProcessingException
            {
              final DvDateTime dvTherapyStart = (DvDateTime)resultRow[0];
              final DvDateTime dvTherapyEnd = (DvDateTime)resultRow[1];
              if (dvTherapyEnd != null)
              {
                return new Interval(DataValueUtils.getDateTime(dvTherapyStart), DataValueUtils.getDateTime(dvTherapyEnd));
              }
              return Intervals.infiniteFrom(DataValueUtils.getDateTime(dvTherapyStart));
            }
          }
      );
    }
    return new ArrayList<>();
  }

  @Override
  public Double getPatientLastReferenceWeight(final Long patientId, final Interval searchInterval)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      sb
          .append("SELECT o/data[at0002]/events[at0003 and name/value='Any event']/data[at0001]/items[at0004]/value")
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
          sb.toString(), new ResultRowProcessor<Object[], Double>()
          {
            @Override
            public Double process(final Object[] resultRow, final boolean hasNext) throws ProcessingException
            {
              final DvQuantity weight = (DvQuantity)resultRow[0];
              return weight.getMagnitude();
            }
          }
      );
      return weights.isEmpty() ? null : weights.get(0);
    }
    return null;
  }

  @Override
  public void savePatientReferenceWeight(final long patientId, final MedicationReferenceWeightComposition comp)
  {
    saveSubjectComposition(patientId, comp, null);
  }

  @Override
  public Pair<MedicationOrderComposition, MedicationInstructionInstruction> getTherapyInstructionPair(
      final long patientId, final String compositionUid, final String ehrOrderName)
  {
    final MedicationOrderComposition composition = loadMedicationOrderComposition(patientId, compositionUid);
    final MedicationInstructionInstruction instruction =
        MedicationsEhrUtils.getMedicationInstructionByEhrName(composition, ehrOrderName);
    return Pair.of(composition, instruction);
  }

  @Override
  public String saveMedicationAdministrationComposition(
      final long patientId,
      final MedicationAdministrationComposition composition,
      final String uid)
  {
    if (uid != null)
    {
      final String latestCompositionUid = getLatestCompositionUid(patientId, uid);
      return updateSubjectComposition(patientId, composition, latestCompositionUid);
    }

    return saveSubjectComposition(patientId, composition, null);
  }

  @Override
  public void deleteTherapy(final long patientId, final String compositionUid)
  {
    deleteSubjectComposition(patientId, compositionUid);
  }

  @Override
  public void deleteTherapyAdministration(
      final long patientId, final String administrationCompositionUid, final String comment)
  {
    deleteSubjectComposition(patientId, administrationCompositionUid, comment);
  }

  private String getLatestCompositionUid(final long patientId, final String compositionUid)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final String uidWithoutVersion = InstructionTranslator.getCompositionUidWithoutVersion(compositionUid);

      final StringBuilder sb = new StringBuilder();
      sb
          .append("SELECT v/uid/value")
          .append(" FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS VERSIONED_OBJECT vo")
          .append(" CONTAINS VERSION v[all_versions]")
          .append(" WHERE vo/uid/value = '" + uidWithoutVersion + "'");

      final List<Long> versions = query(
          sb.toString(), new ResultRowProcessor<Object[], Long>()
          {
            @Override
            public Long process(final Object[] resultRow, final boolean hasNext) throws ProcessingException
            {
              return InstructionTranslator.getCompositionVersion((String)resultRow[0]);
            }
          }
      );
      if (!versions.isEmpty())
      {
        Collections.sort(versions);
        final Long latestVersion = versions.get(versions.size() - 1);
        return InstructionTranslator.buildCompositionUid(compositionUid, latestVersion);
      }
    }
    return null;
  }
}
