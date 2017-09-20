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

package com.marand.thinkmed.medications.audittrail.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.ispek.ehr.common.tdo.CompositionEventContext;
import com.marand.maf.core.Pair;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition;
import com.marand.openehr.util.DataValueUtils;
import com.marand.openehr.util.OpenEhrRefUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.audittrail.TherapyAuditTrailProvider;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.change.TherapyChangeCalculator;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.TherapyActionHistoryDto;
import com.marand.thinkmed.medications.dto.TherapyActionHistoryType;
import com.marand.thinkmed.medications.dto.audittrail.TherapyAuditTrailDto;
import com.marand.thinkmed.medications.dto.change.StringTherapyChangeDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeType;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openehr.jaxb.rm.Link;
import org.openehr.jaxb.rm.ObjectVersionId;
import org.openehr.jaxb.rm.PartyIdentified;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Mitja Lapajne
 */
@RunWith(MockitoJUnitRunner.class)
public class TherapyAuditTrailProviderTest
{
  private static final Locale LOCALE = new Locale("en_GB");

  @InjectMocks
  private TherapyAuditTrailProvider therapyAuditTrailProvider = new TherapyAuditTrailProviderImpl();

  @Mock
  private TherapyChangeCalculator therapyChangeCalculator;

  @Mock
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Mock
  private MedicationsBo medicationsBo;

  @Before
  public void setMocks()
  {
    final DateTime when = new DateTime(2016, 8, 12, 10, 0);

    Mockito
        .when(medicationsOpenEhrDao.getPatientLastReferenceWeight("p1", Intervals.infiniteTo(when)))
        .thenReturn(20.0);

    //NEW THERAPY
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> newInstructionPair = buildInstructionPair(
        "uid2::bla::2",
        "Instruction",
        "PerformerModify",
        new DateTime(2016, 8, 11, 8, 0));

    //add actions
    MedicationsEhrUtils.addMedicationActionTo(
        newInstructionPair.getFirst(),
        MedicationActionEnum.ABORT,
        new NamedExternalDto("PerformerAbort", "PerformerAbort"),
        new DateTime(2016, 8, 11, 23, 0));

    Mockito
        .when(medicationsOpenEhrDao.getTherapyInstructionPair("p1", "uid2::bla::2", "Instruction"))
        .thenReturn(newInstructionPair);

    final ConstantSimpleTherapyDto newTherapy = new ConstantSimpleTherapyDto();
    newTherapy.setCompositionUid("uid2::bla::2");
    newTherapy.setCreatedTimestamp(new DateTime(2016, 8, 11, 18, 0));
    newTherapy.setStart(new DateTime(2016, 8, 11, 20, 0));
    newTherapy.setPrescriberName("PerformerModify");

    Mockito
        .when(medicationsBo.convertInstructionToTherapyDtoWithDisplayValues(
            newInstructionPair.getFirst(),
            newInstructionPair.getSecond(),
            20.0,
            null,
            when,
            true,
            LOCALE))
        .thenReturn(newTherapy);

    //OLD THERAPY
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> oldInstructionPair =
        buildInstructionPair("uid1::bla::3", "Instruction", "PerformerPrescribe", new DateTime(2016, 8, 11, 8, 0));

    //add link
    final Link updateLink = OpenEhrRefUtils.getLinkToTdoTarget(
        "update",
        EhrLinkType.UPDATE.getName(),
        oldInstructionPair.getFirst(),
        oldInstructionPair.getSecond());

    MedicationsEhrUtils.addMedicationActionTo(
        oldInstructionPair.getFirst(),
        MedicationActionEnum.COMPLETE,
        new NamedExternalDto("PerformerComplete", "PerformerComplete"),
        new DateTime(2016, 8, 11, 18, 0));

    newInstructionPair.getSecond().getLinks().add(updateLink);
    newInstructionPair.getFirst().getMedicationDetail().getMedicationInstruction().get(0).getLinks().add(updateLink);

    final ConstantSimpleTherapyDto oldTherapy = new ConstantSimpleTherapyDto();
    oldTherapy.setCompositionUid("uid1::bla::3");
    oldTherapy.setCreatedTimestamp(new DateTime(2016, 8, 11, 8, 0));
    oldTherapy.setStart(new DateTime(2016, 8, 11, 15, 0));

    Mockito
        .when(medicationsBo.getInstructionFromLink("p1", newInstructionPair.getSecond(), EhrLinkType.UPDATE, true))
        .thenReturn(oldInstructionPair);

    //mock modify existing
    final MedicationOrderComposition compositionVersion1 = buildInstructionPair(
        "uid1::bla::1",
        "Instruction",
        "PerformerPrescribe",
        new DateTime(2016, 8, 11, 8, 0)).getFirst();
    final MedicationOrderComposition compositionVersion2 = buildInstructionPair(
        "uid1::bla::2",
        "Instruction",
        "PerformerPrescribe",
        new DateTime(2016, 8, 11, 8, 0)).getFirst();

    //add actions
    MedicationsEhrUtils.addMedicationActionTo(
        compositionVersion2,
        MedicationActionEnum.MODIFY_EXISTING,
        new NamedExternalDto("PerformerModifyExisting", "PerformerModifyExisting"),
        new DateTime(2016, 8, 11, 9, 0));

    //therapy review on modify
    MedicationsEhrUtils.addMedicationActionTo(
        compositionVersion2,
        MedicationActionEnum.REVIEW,
        new NamedExternalDto("PerformerModifyExisting", "PerformerModifyExisting"),
        new DateTime(2016, 8, 11, 9, 0));

    MedicationsEhrUtils.addMedicationActionTo(
        compositionVersion2,
        MedicationActionEnum.SUSPEND,
        new NamedExternalDto("PerformerSuspend", "PerformerSuspend"),
        new DateTime(2016, 8, 11, 15, 30));

    MedicationsEhrUtils.addMedicationActionTo(
        compositionVersion2,
        MedicationActionEnum.REISSUE,
        new NamedExternalDto("PerformerReissue", "PerformerReissue"),
        new DateTime(2016, 8, 11, 16, 0));

    MedicationsEhrUtils.addMedicationActionTo(
        compositionVersion2,
        MedicationActionEnum.REVIEW,
        new NamedExternalDto("PerformerReview", "PerformerReview"),
        new DateTime(2016, 8, 11, 17, 0));

    Mockito
        .when(medicationsOpenEhrDao.loadMedicationOrderComposition("p1", "uid1::bla::1"))
        .thenReturn(compositionVersion1);

    Mockito
        .when(medicationsOpenEhrDao.loadMedicationOrderComposition("p1", "uid1::bla::2"))
        .thenReturn(compositionVersion2);

    Mockito
        .when(medicationsOpenEhrDao.loadMedicationOrderComposition("p1", "uid2::bla::1"))
        .thenReturn(newInstructionPair.getFirst());

    final List<MedicationOrderComposition> oldCompositions = new ArrayList<>();
    oldCompositions.add(oldInstructionPair.getFirst());
    oldCompositions.add(compositionVersion1);
    oldCompositions.add(compositionVersion2);

    Mockito
        .when(medicationsOpenEhrDao.getAllMedicationOrderCompositionVersions("uid1::bla::2"))
        .thenReturn(oldCompositions);

    MedicationsEhrUtils.addMedicationActionTo(
        compositionVersion2,
        MedicationActionEnum.MODIFY_EXISTING,
        new NamedExternalDto("PerformerModifyExisting", "PerformerModifyExisting"),
        new DateTime(2016, 8, 11, 9, 0));

    final ConstantSimpleTherapyDto oldTherapyVersion1 = new ConstantSimpleTherapyDto();
    oldTherapyVersion1.setCreatedTimestamp(new DateTime(2016, 8, 11, 8, 0));

    oldTherapyVersion1.setCompositionUid("uid1::bla::1");
    final ConstantSimpleTherapyDto oldTherapyVersion2 = new ConstantSimpleTherapyDto();

    oldTherapyVersion2.setPrescriberName("PerformerModifyExisting");
    oldTherapyVersion2.setStart(new DateTime(2016, 8, 11, 15, 0));

    Mockito
        .when(medicationsBo.convertInstructionToTherapyDtoWithDisplayValues(
            compositionVersion1,
            compositionVersion1.getMedicationDetail().getMedicationInstruction().get(0),
            null,
            null,
            when,
            true,
            LOCALE))
        .thenReturn(oldTherapyVersion1);
    Mockito
        .when(medicationsBo.convertInstructionToTherapyDtoWithDisplayValues(
            compositionVersion2,
            compositionVersion2.getMedicationDetail().getMedicationInstruction().get(0),
            null,
            null,
            when,
            true,
            LOCALE))
        .thenReturn(oldTherapyVersion2);

    //mock modify
    final StringTherapyChangeDto comment = new StringTherapyChangeDto(TherapyChangeType.COMMENT);
    comment.setOldValue(null);
    comment.setNewValue("comment");

    //mock pharmacy review
    final PharmacyReviewReportComposition pharmacyReview = new PharmacyReviewReportComposition();
    final PharmacyReviewReportComposition.CompositionEventContext compositionEventContext =
        new PharmacyReviewReportComposition.CompositionEventContext();
    compositionEventContext.setStartTime(DataValueUtils.getDateTime(new DateTime(2016, 8, 11, 9, 30)));
    pharmacyReview.setCompositionEventContext(compositionEventContext);
    final PartyIdentified composerPartyIdentified = new PartyIdentified();
    composerPartyIdentified.setName("PerformerPharmacistReview");
    pharmacyReview.setComposer(composerPartyIdentified);

    final List<PharmacyReviewReportComposition> pharmacyReviews = new ArrayList<>();
    pharmacyReviews.add(pharmacyReview);
    Mockito
        .when(medicationsOpenEhrDao.findPharmacistsReviewCompositions("p1", new DateTime(2016, 8, 11, 8, 0)))
        .thenReturn(pharmacyReviews);
  }

  @Test
  public void testGetTherapyAuditTrail()
  {
    final TherapyAuditTrailDto therapyAuditTrail = therapyAuditTrailProvider.getTherapyAuditTrail(
        "p1",
        "uid2::bla::2",
        "Instruction",
        null,
        LOCALE,
        new DateTime(2016, 8, 12, 10, 0));

    assertEquals("uid2::bla::2", therapyAuditTrail.getCurrentTherapy().getCompositionUid());
    assertEquals("uid1::bla::1", therapyAuditTrail.getOriginalTherapy().getCompositionUid());
    assertEquals(8, therapyAuditTrail.getActionHistoryList().size());

    final TherapyActionHistoryDto prescribeAction = therapyAuditTrail.getActionHistoryList().get(0);
    assertEquals(TherapyActionHistoryType.PRESCRIBE, prescribeAction.getTherapyActionHistoryType());
    assertEquals("PerformerPrescribe", prescribeAction.getPerformer());
    assertEquals(new DateTime(2016, 8, 11, 8, 0), prescribeAction.getActionPerformedTime());

    final TherapyActionHistoryDto modifyExistingAction = therapyAuditTrail.getActionHistoryList().get(1);
    assertEquals(TherapyActionHistoryType.MODIFY_EXISTING, modifyExistingAction.getTherapyActionHistoryType());
    assertEquals("PerformerModifyExisting", modifyExistingAction.getPerformer());
    assertEquals(new DateTime(2016, 8, 11, 9, 0), modifyExistingAction.getActionPerformedTime());

    final TherapyActionHistoryDto pharmacyReviewAction = therapyAuditTrail.getActionHistoryList().get(2);
    assertEquals(TherapyActionHistoryType.PHARMACIST_REVIEW, pharmacyReviewAction.getTherapyActionHistoryType());
    assertEquals("PerformerPharmacistReview", pharmacyReviewAction.getPerformer());
    assertEquals(new DateTime(2016, 8, 11, 9, 30), pharmacyReviewAction.getActionPerformedTime());
    assertNull(pharmacyReviewAction.getActionTakesEffectTime());

    final TherapyActionHistoryDto suspendAction = therapyAuditTrail.getActionHistoryList().get(3);
    assertEquals(TherapyActionHistoryType.SUSPEND, suspendAction.getTherapyActionHistoryType());
    assertEquals("PerformerSuspend", suspendAction.getPerformer());
    assertEquals(new DateTime(2016, 8, 11, 15, 30), suspendAction.getActionPerformedTime());
    assertNull(suspendAction.getActionTakesEffectTime());

    final TherapyActionHistoryDto reissueAction = therapyAuditTrail.getActionHistoryList().get(4);
    assertEquals(TherapyActionHistoryType.REISSUE, reissueAction.getTherapyActionHistoryType());
    assertEquals("PerformerReissue", reissueAction.getPerformer());
    assertEquals(new DateTime(2016, 8, 11, 16, 0), reissueAction.getActionPerformedTime());
    assertNull(reissueAction.getActionTakesEffectTime());

    final TherapyActionHistoryDto reviewAction = therapyAuditTrail.getActionHistoryList().get(5);
    assertEquals(TherapyActionHistoryType.DOCTOR_REVIEW, reviewAction.getTherapyActionHistoryType());
    assertEquals("PerformerReview", reviewAction.getPerformer());
    assertEquals(new DateTime(2016, 8, 11, 17, 0), reviewAction.getActionPerformedTime());
    assertNull(reviewAction.getActionTakesEffectTime());

    final TherapyActionHistoryDto modifyAction = therapyAuditTrail.getActionHistoryList().get(6);
    assertEquals(TherapyActionHistoryType.MODIFY, modifyAction.getTherapyActionHistoryType());
    assertEquals("PerformerComplete", modifyAction.getPerformer());
    assertEquals(new DateTime(2016, 8, 11, 18, 0), modifyAction.getActionPerformedTime());
    assertEquals(new DateTime(2016, 8, 11, 20, 0), modifyAction.getActionTakesEffectTime());
  }

  private Pair<MedicationOrderComposition, MedicationInstructionInstruction> buildInstructionPair(
      final String uid,
      final String ehrOrderName,
      final String composerName,
      final DateTime compositionCreateTime)
  {
    final MedicationOrderComposition composition = new MedicationOrderComposition();
    final ObjectVersionId objectVersionId = new ObjectVersionId();
    objectVersionId.setValue(uid);
    composition.setUid(objectVersionId);
    final CompositionEventContext compositionEventContext = new CompositionEventContext();
    compositionEventContext.setStartTime(DataValueUtils.getDateTime(compositionCreateTime));
    composition.setCompositionEventContext(compositionEventContext);
    final PartyIdentified composer = new PartyIdentified();
    composer.setName(composerName);
    composition.setComposer(composer);
    final MedicationDetailSection medicationDetail = new MedicationDetailSection();
    final MedicationInstructionInstruction instruction = new MedicationInstructionInstruction();
    instruction.setName(DataValueUtils.getText(ehrOrderName));
    medicationDetail.getMedicationInstruction().add(instruction);
    composition.setMedicationDetail(medicationDetail);

    return Pair.of(composition, instruction);
  }
}
