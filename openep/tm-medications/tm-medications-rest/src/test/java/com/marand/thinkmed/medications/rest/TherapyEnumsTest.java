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

package com.marand.thinkmed.medications.rest;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.marand.maf.core.JsonUtil;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationAuthorisationSloveniaCluster.IllnessConditionType;
import com.marand.openehr.medications.tdo.MedicationAuthorisationSloveniaCluster.Payer;
import com.marand.openehr.medications.tdo.MedicationAuthorisationSloveniaCluster.PrescriptionDocumentType;
import com.marand.thinkmed.medications.ActionReasonType;
import com.marand.thinkmed.medications.AdministrationResultEnum;
import com.marand.thinkmed.medications.AdministrationStatusEnum;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.InfusionSetChangeEnum;
import com.marand.thinkmed.medications.MedicationAdditionalInstructionEnum;
import com.marand.thinkmed.medications.MedicationFinderFilterEnum;
import com.marand.thinkmed.medications.MedicationOrderActionEnum;
import com.marand.thinkmed.medications.MedicationOrderFormType;
import com.marand.thinkmed.medications.MedicationRouteTypeEnum;
import com.marand.thinkmed.medications.MedicationStartCriterionEnum;
import com.marand.thinkmed.medications.MedicationTypeEnum;
import com.marand.thinkmed.medications.PharmacistReviewTaskStatusEnum;
import com.marand.thinkmed.medications.PrescriptionChangeTypeEnum;
import com.marand.thinkmed.medications.SelfAdministeringActionEnum;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.TherapyTagEnum;
import com.marand.thinkmed.medications.TherapyTemplateModeEnum;
import com.marand.thinkmed.medications.TherapyTemplateTypeEnum;
import com.marand.thinkmed.medications.TitrationType;
import com.marand.thinkmed.medications.dto.BnfMaximumUnitType;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonEnum;
import com.marand.thinkmed.medications.dto.administration.AdjustAdministrationSubtype;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationSubtype;
import com.marand.thinkmed.medications.dto.change.TherapyChangeType;
import com.marand.thinkmed.medications.dto.pharmacist.review.MedicationSupplyTypeEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.TherapyPharmacistReviewStatusEnum;
import com.marand.thinkmed.medications.dto.reconsiliation.ReconciliationRowGroupEnum;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsType;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import com.marand.thinkmed.medicationsexternal.WarningSeverity;
import com.marand.thinkmed.medicationsexternal.WarningType;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.jaxb.json.JsonParsing;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Mitja Lapajne
 */
public class TherapyEnumsTest
{
  private static final Pattern NEW_LINE_PATTERN = Pattern.compile("\\r?\\n");
  private static final Pattern COLON_PATTERN = Pattern.compile(":");

  @Test
  public void testEnums() throws Exception
  {
    final InputStream inputStream = JsonParsing.class.getResourceAsStream("/app/views/medications/jquery/TherapyEnums.js");
    final String enumsString = IOUtils.toString(inputStream);
    assertEnum("templateTypeEnum", enumsString, TherapyTemplateTypeEnum.values());
    assertEnum("medicationRouteTypeEnum", enumsString, MedicationRouteTypeEnum.values());
    assertEnum("dosingFrequencyTypeEnum", enumsString, DosingFrequencyTypeEnum.values());
    assertEnum("medicationOrderFormType", enumsString, MedicationOrderFormType.values());
    assertEnum("therapyStatusEnum", enumsString, TherapyStatusEnum.values());
    assertEnum("administrationTypeEnum", enumsString, AdministrationTypeEnum.values());
    assertEnum("infusionSetChangeEnum", enumsString, InfusionSetChangeEnum.values());
    assertEnum("therapySortTypeEnum", enumsString, TherapySortTypeEnum.values());
    assertEnum("medicationStartCriterionEnum", enumsString, MedicationStartCriterionEnum.values());
    assertEnum("therapyTag", enumsString, TherapyTagEnum.values());
    assertEnum("warningSeverityEnum", enumsString, WarningSeverity.values());
    assertEnum("medicationAdditionalInstructionEnum", enumsString, MedicationAdditionalInstructionEnum.values());
    assertEnum("prescriptionChangeTypeEnum", enumsString, PrescriptionChangeTypeEnum.values());
    assertEnum("pharmacistReviewTaskStatusEnum", enumsString, PharmacistReviewTaskStatusEnum.values());
    assertEnum("taskTypeEnum", enumsString, TaskTypeEnum.values());
    assertEnum("therapyPharmacistReviewStatusEnum", enumsString, TherapyPharmacistReviewStatusEnum.values());
    assertEnum("medicationSupplyTypeEnum", enumsString, MedicationSupplyTypeEnum.values());
    assertEnum("administrationResultEnum", enumsString, AdministrationResultEnum.values());
    assertEnum("selfAdministrationTypeEnum", enumsString, MedicationActionAction.SelfAdministrationType.values());
    assertEnum("medicationOrderActionEnum", enumsString, MedicationOrderActionEnum.values());
    assertEnum("reconciliationRowGroupEnum", enumsString, ReconciliationRowGroupEnum.values());
    assertEnum("actionReasonTypeEnum", enumsString, ActionReasonType.values());
    assertEnum("bnfMaximumUnitType", enumsString, BnfMaximumUnitType.values());
    assertEnum("prescriptionDocumentType", enumsString, PrescriptionDocumentType.values());
    assertEnum("illnessConditionType", enumsString, IllnessConditionType.values());
    assertEnum("selfAdministeringActionEnum", enumsString, SelfAdministeringActionEnum.values());
    assertEnum("administrationStatusEnum", enumsString, AdministrationStatusEnum.values());
    assertEnum("medicationFinderFilterEnum", enumsString, MedicationFinderFilterEnum.values());
    assertEnum("therapyTemplateModeEnum", enumsString, TherapyTemplateModeEnum.values());
    assertEnum("therapyDoseTypeEnum", enumsString, TherapyDoseTypeEnum.values());
    assertEnum("warningType", enumsString, WarningType.values());
    assertEnum("medicationTypeEnum", enumsString, MedicationTypeEnum.values());
    assertEnum("medicationAuthorisationSloveniaClusterPayer", enumsString, Payer.values());
    assertEnum("medicationRuleEnum", enumsString, MedicationRuleEnum.values());
    assertEnum("therapyChangeReasonEnum", enumsString, TherapyChangeReasonEnum.values());
    assertEnum("therapyTitrationTypeEnum", enumsString, TitrationType.values());
    assertEnum("flowRateMode", enumsString, AdministrationDetailsCluster.OxygenDeliveryCluster.FlowRateMode.values());
    assertEnum("oxygenDeliveryClusterRoute", enumsString, AdministrationDetailsCluster.OxygenDeliveryCluster.Route.values());
    assertEnum("adjustAdministrationSubtype", enumsString, AdjustAdministrationSubtype.values());
    assertEnum("startAdministrationSubtype", enumsString, StartAdministrationSubtype.values());
    assertEnum("therapyChangeTypeEnum", enumsString, TherapyChangeType.values());
    assertEnum("additionalWarningsTypeEnum", enumsString, AdditionalWarningsType.values());
  }

  private void assertEnum(final String enumName, final String enumsString, final Object[] enumValues)
  {
    final int enumIndex = enumsString.indexOf(enumName);
    final int start = enumsString.indexOf('{', enumIndex);
    final int end = enumsString.indexOf('}', enumIndex) + 1;
    final String enumString = enumsString.substring(start, end);

    final List<Object> enums = new ArrayList<>();
    final String[] lines = NEW_LINE_PATTERN.split(enumString);
    for (final String line : lines)
    {
      final String[] strings = COLON_PATTERN.split(line);
      if (strings.length > 1)
      {
        enums.add(JsonUtil.fromJson(strings[0], enumValues[0].getClass()));
      }
    }

    Assert.assertArrayEquals(enums.toArray(), enumValues);
  }
}
