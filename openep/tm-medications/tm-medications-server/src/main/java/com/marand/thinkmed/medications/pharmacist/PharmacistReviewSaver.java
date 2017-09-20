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

package com.marand.thinkmed.medications.pharmacist;

import java.util.List;
import java.util.Locale;

import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.ReviewPharmacistReviewAction;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public interface PharmacistReviewSaver
{
  String savePharmacistReview(
      String patientId,
      PharmacistReviewDto pharmacistReview,
      Boolean authorize,
      Locale locale);

  void reviewPharmacistReview(
      String patientId,
      String pharmacistReviewUid,
      ReviewPharmacistReviewAction reviewAction,
      List<String> deniedReviews,
      DateTime when,
      Locale locale);

  void authorizePatientPharmacistReviews(
      String patientId,
      List<String> pharmacistReviewUids,
      Locale locale,
      DateTime when);
}
