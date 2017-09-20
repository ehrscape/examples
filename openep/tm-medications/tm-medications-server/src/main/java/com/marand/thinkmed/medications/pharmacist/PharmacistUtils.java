package com.marand.thinkmed.medications.pharmacist;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.marand.maf.core.Pair;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition;
import com.marand.openehr.util.DataValueUtils;
import com.marand.openehr.util.OpenEhrRefUtils;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.business.util.TherapyIdUtils;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewStatusEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistTherapyChangeType;
import org.joda.time.DateTime;

/**
 * @author Klavdij Lapajne
 */
public class PharmacistUtils
{
  private PharmacistUtils()
  {
  }

  public static PharmacistTherapyChangeType getPharmacistTherapyChangeType(final MedicationActionAction action)
  {
    final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);

    if (actionEnum == MedicationActionEnum.ABORT)
    {
      return PharmacistTherapyChangeType.ABORT;
    }
    if (actionEnum == MedicationActionEnum.RECOMMEND)
    {
      return PharmacistTherapyChangeType.EDIT;
    }
    if (actionEnum == MedicationActionEnum.SUSPEND)
    {
      return  PharmacistTherapyChangeType.SUSPEND;
    }
    return null;
  }

  public static MedicationActionEnum getMedicationActionChangeType(final PharmacistTherapyChangeType changeType)
  {
    if (changeType == PharmacistTherapyChangeType.ABORT)
    {
      return MedicationActionEnum.ABORT;
    }
    if (changeType == PharmacistTherapyChangeType.EDIT)
    {
      return MedicationActionEnum.RECOMMEND;
    }
    if (changeType == PharmacistTherapyChangeType.SUSPEND)
    {
      return MedicationActionEnum.SUSPEND;
    }
    return null;
  }

  public static Pair<DateTime, Set<String>> getLastReviewTimestampAndReferredBackTherapiesCompositionUIds(
      final List<PharmacyReviewReportComposition> pharmacistsReviewCompositions)
  {
    final Set<String> referredBackTherapiesCompositionUIds = new HashSet<>();
    DateTime lastReviewTimestamp = null;

    for (final PharmacyReviewReportComposition pharmacistsReview : pharmacistsReviewCompositions)
    {
      final PharmacistReviewStatusEnum pharmacistReviewStatus =
          PharmacistReviewStatusEnum.valueOf(pharmacistsReview.getCompositionEventContext().getStatus().getValue());

      final DateTime reviewTimestamp =
          DataValueUtils.getDateTime(pharmacistsReview.getCompositionEventContext().getStartTime());

      if (pharmacistReviewStatus == PharmacistReviewStatusEnum.Final
          && (lastReviewTimestamp == null || lastReviewTimestamp.isBefore(reviewTimestamp)))
      {
        lastReviewTimestamp = reviewTimestamp;
      }
      if (pharmacistsReview.getMiscellaneous() != null)
      {
        final boolean prescriberHasAlreadyResponded =
            pharmacistsReview.getMiscellaneous().getPrescriberReferralResponse() != null;

        if (pharmacistReviewStatus == PharmacistReviewStatusEnum.Final && !prescriberHasAlreadyResponded)
        {
          for (final MedicationInstructionInstruction reviewedInstruction :
              pharmacistsReview.getMiscellaneous().getMedicationInstruction())
          {
            final boolean referredBackToPrescriber = pharmacistsReview.getMiscellaneous().getPrescriberReferral() != null;
            if (referredBackToPrescriber && !reviewedInstruction.getLinks().isEmpty())
            {
              final String compositionId = OpenEhrRefUtils.parseEhrUri(
                  reviewedInstruction.getLinks().get(0).getTarget().getValue()).getCompositionId();

              referredBackTherapiesCompositionUIds.add(TherapyIdUtils.getCompositionUidWithoutVersion(compositionId));
            }
          }
        }
      }
    }
    return Pair.of(lastReviewTimestamp, referredBackTherapiesCompositionUIds);
  }
}
