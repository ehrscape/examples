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

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.Pair;
import com.marand.maf.core.time.CurrentTime;
import com.marand.maf.core.time.Intervals;
import com.marand.thinkmed.api.core.data.NamedIdentity;
import com.marand.thinkmed.api.organization.data.KnownClinic;
import com.marand.thinkmed.html.AppHtmlConst;
import com.marand.thinkmed.html.AppHtmlViewConfig;
import com.marand.thinkmed.html.HtmlViewServlet;
import com.marand.thinkmed.html.Parameter;
import com.marand.thinkmed.html.components.pdf.PdfViewerRequestData;
import com.marand.thinkmed.html.externals.CHAPLinksTimelineHtmlExternal;
import com.marand.thinkmed.html.framework.TmJQueryHtmlFramework;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.TherapyJsonAdapter;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.dto.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.TherapyCardInfoDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.TherapyFlowDto;
import com.marand.thinkmed.medications.dto.TherapyReloadAfterActionDto;
import com.marand.thinkmed.medications.dto.TherapyTemplateDto;
import com.marand.thinkmed.medications.dto.TherapyTemplatesDto;
import com.marand.thinkmed.medications.dto.TherapyViewDto;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.InfusionSetChangeDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StopAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.TherapyTimelineRowDto;
import com.marand.thinkmed.medications.service.MedicationsService;
import com.marand.thinkmed.medicationsexternal.dto.MedicationForWarningsSearchDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationsWarningDto;
import org.jboss.resteasy.util.Base64;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

/**
 * @author Bostjan Vester
 * @author Mitja Lapajne
 */
@Controller
@Path("/medications")
public class MedicationsRestServlet extends HtmlViewServlet
{
  private static final String MODULE_DIR_PATH = AppHtmlConst.DEFAULT_APP_VIEWS_SERVLET_PATH + "/medications";

  private static final String MODULE_ORDERING_DIR_PATH = MODULE_DIR_PATH + "/jquery/ordering";
  private static final String MODULE_THERAPY_DIR_PATH = MODULE_DIR_PATH + "/jquery/therapy";
  private static final String MODULE_TIMELINE_DIR_PATH = MODULE_DIR_PATH + "/jquery/timeline";

  @Autowired
  private MedicationsService service;

  private Class<? extends NamedIdentity> prescriberClass;

  public void setService(final MedicationsService service)
  {
    this.service = service;
  }

  public void setPrescriberClass(final Class<? extends NamedIdentity> prescriberClass)
  {
    this.prescriberClass = prescriberClass;
  }

  /**
   * ****************** THERAPY VIEW * *******************
   */

  @GET
  @Path("therapyView")
  public Response therapyView(
      @QueryParam("data") final Parameter.String data,
      @QueryParam("language") final String language,
      @QueryParam("theme") final String theme)
  {
    return buildJsonResponse(buildViewConfig("therapy", theme, language).toJson());
  }

  @Override
  protected AppHtmlViewConfig getViewConfig(final String view, final String theme, final String language)
  {
    final AppHtmlViewConfig config =
        new AppHtmlViewConfig("tm.views.medications.TherapyView", new TmJQueryHtmlFramework(), theme, language);

    // externals //
    //config.addExternal(CHAPLinksTimelineHtmlExternal.VERSION_2_6_1);
    config.addExternal(CHAPLinksTimelineHtmlExternal.VERSION_2_9_0);

    // style sheets //
    config.addStyleSheetDependency(MODULE_THERAPY_DIR_PATH + "/TherapyView.css");

    // java scripts //
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/TherapyDisplayProvider.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/TherapyDetailsCardContainer.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/MedicationUtils.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/MedicationTimingUtils.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/ValueLabel.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/TherapyEnums.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/TherapyActions.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/MedicationSearchField.js");

    //view
    config.addJavaScriptDependency(MODULE_THERAPY_DIR_PATH + "/TherapyView.js");

    //therapyFlow
    config.addJavaScriptDependency(MODULE_THERAPY_DIR_PATH + "/TherapyFlowGrid.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DIR_PATH + "/TherapyOverviewHeader.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DIR_PATH + "/TherapyFlowTodayButtons.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DIR_PATH + "/ReferenceWeightPane.js");

    //ordering complex
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/ComplexTherapyContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/HeparinPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/InfusionRatePane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/VolumeSumPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/ComplexTherapyMedicationPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/ComplexVariableRatePane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/InfusionRateFormulaUnitPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/ComplexTherapyEditContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/UniversalComplexTherapyContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/InfusionRateTypePane.js");

    //ordering simple
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/simple/SimpleTherapyContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/simple/SimpleVariableDosePane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/simple/SimpleVariableDoseDaysPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/simple/SimpleTherapyEditContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/simple/UniversalSimpleTherapyContainer.js");

    //ordering
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/TherapyDaysContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/MedicationDetailsCardPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/RoutesPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/MedicationsTitleHeader.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/TemplatesContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/MedicationsOrderingContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/BasketContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/OrderingContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/WarningsContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/DosingFrequencyPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/DosePane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/UniversalDosePane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/TherapyIntervalPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/CalculatedDosagePane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/TherapySaveDatePane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/EditPastDaysOfTherapyContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/SaveTemplatePane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/LinkTherapyPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/TherapyApplicationConditionPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/WarningOverrideReasonPane.js");

    //timeline
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/TherapyTimeline.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/TherapyTimelineContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/TherapyTimelineUtils.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/TherapyAdministrationContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/RescheduleTasksContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/TherapyAdministrationCard.js");

    // resources - dictionaries //

    // resources - paths //
    return config;
  }

  @GET
  @Produces({MediaType.TEXT_PLAIN})
  @Path("getTherapyViewData")
  public Response getTherapyViewData(
      @QueryParam("patientId") final Parameter.Long patientId,
      @QueryParam("knownOrganizationalEntityName") final Parameter.String knownDepartmentName)
  {
    final KnownClinic department = KnownClinic.Utils.fromName(
        knownDepartmentName.getValue());
    final TherapyViewDto therapyViewDto = service.getTherapyViewDto(patientId.getValue(), department);
    return buildJsonResponse(JsonUtil.toJson(therapyViewDto));
  }

  @GET
  @Produces({MediaType.TEXT_PLAIN})
  @Path("therapyflowdata")
  public Response getTherapyFlowData(
      @QueryParam("patientId") final Parameter.Long patientId,
      @QueryParam("centralCaseId") final Parameter.Long centralCaseId,
      @QueryParam("patientHeight") final Parameter.Double patientHeight,
      @QueryParam("startDate") final Parameter.String startDate,
      @QueryParam("dayCount") final Parameter.Integer dayCount,
      @QueryParam("todayIndex") final Parameter.Integer todayIndex,
      @QueryParam("roundsInterval") final Parameter.String roundsInterval,
      @QueryParam("therapySortTypeEnum") final Parameter.String therapySortTypeString,
      @QueryParam("knownOrganizationalEntity") final Parameter.String knownOrganizationalEntity,
      @QueryParam("language") final Parameter.String language)
  {
    final DateTime startDateAtMidnight = new DateTime(Long.parseLong(startDate.getValue())).withTimeAtStartOfDay();
    final RoundsIntervalDto roundsIntervalDto = JsonUtil.fromJson(roundsInterval.getValue(), RoundsIntervalDto.class);
    final TherapySortTypeEnum therapySortTypeEnum = TherapySortTypeEnum.valueOf(therapySortTypeString.getValue());
    final TherapyFlowDto therapyFlowDto =
        service.getTherapyFlow(
            patientId.getValue(),
            centralCaseId.getValue() != null ? centralCaseId.getValue() : 0,
            patientHeight.getValue(),
            startDateAtMidnight,
            dayCount.getValue(),
            todayIndex.getValue(),
            roundsIntervalDto,
            therapySortTypeEnum,
            KnownClinic.Utils.fromName(knownOrganizationalEntity.getValue()),
            new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(therapyFlowDto));
  }

  @GET
  @Produces({MediaType.TEXT_PLAIN})
  @Path("reloadSingleTherapyAfterAction")
  public Response reloadSingleTherapyAfterAction(
      @QueryParam("patientId") final Parameter.Long patientId,
      @QueryParam("compositionUid") final Parameter.String compositionUid,
      @QueryParam("ehrOrderName") final Parameter.String ehrOrderName,
      @QueryParam("roundsInterval") final Parameter.String roundsInterval)
  {
    final RoundsIntervalDto roundsIntervalDto = JsonUtil.fromJson(roundsInterval.getValue(), RoundsIntervalDto.class);

    final TherapyReloadAfterActionDto reloadDto =
        service.reloadSingleTherapyAfterAction(
            patientId.getValue(),
            compositionUid.getValue(),
            ehrOrderName.getValue(),
            roundsIntervalDto);

    return buildJsonResponse(JsonUtil.toJson(reloadDto));
  }

  @GET
  @Produces({MediaType.TEXT_PLAIN})
  @Path("patientdata")
  public Response getPatientData(
      @QueryParam("patientId") final Parameter.Long patientId,
      @QueryParam("episodeId") final Parameter.Long episodeId)
  {
    final Long episodeIdValue = episodeId != null ? episodeId.getValue() : null;
    final PatientDataForMedicationsDto patientData = service.getPatientData(patientId.getValue(), episodeIdValue);
    return buildJsonResponse(JsonUtil.toJson(patientData));
  }

  @GET
  @Produces({MediaType.TEXT_PLAIN})
  @Path("getPatientBaselineInfusionIntervals")
  public Response getPatientBaselineInfusionIntervals(
      @QueryParam("patientId") final Parameter.Long patientId)
  {
    final List<Interval> patientBaselineInfusionIntervals =
        service.getPatientBaselineInfusionIntervals(patientId.getValue());
    return buildJsonResponse(
        JsonUtil.toJson(null, patientBaselineInfusionIntervals, null, Lists.newArrayList(INTERVAL_SERIALIZER)));
  }

  @GET
  @Produces({MediaType.TEXT_PLAIN})
  @Path("findmedications")
  public Response findMedications(
      @QueryParam("searchString") final String searchString)
  {
    return buildJsonResponse(JsonUtil.toJson(service.findMedications(searchString)));
  }

  @GET
  @Produces({MediaType.TEXT_PLAIN})
  @Path("medicationdata")
  public Response getMedicationData(
      @QueryParam("medicationId") final long medicationId)
  {
    return buildJsonResponse(JsonUtil.toJson(service.getMedicationData(medicationId)));
  }

  @GET
  @Produces({MediaType.TEXT_PLAIN})
  @Path("doserangechecks")
  public Response findDoseRangeChecks(
      @QueryParam("medicationId") final Long medicationId)
  {
    return buildJsonResponse(JsonUtil.toJson(service.findDoseRangeChecks(medicationId)));
  }

  @POST
  @Produces({MediaType.TEXT_PLAIN})
  @Path("tagTherapyForPrescription")
  public Response tagTherapyForPrescription(
      @FormParam("patientId") final Parameter.Long patientId,
      @FormParam("centralCaseId") final Parameter.Long centralCaseId,
      @FormParam("compositionId") final Parameter.String compositionId,
      @FormParam("ehrOrderName") final Parameter.String ehrOrderName)
  {
    service.tagTherapyForPrescription(
        patientId.getValue(),
        compositionId.getValue(),
        centralCaseId.getValue(),
        ehrOrderName.getValue());
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.TEXT_PLAIN})
  @Path("untagTherapyForPrescription")
  public Response untagTherapyForPrescription(
      @FormParam("patientId") final Parameter.Long patientId,
      @FormParam("centralCaseId") final Parameter.Long centralCaseId,
      @FormParam("compositionId") final Parameter.String compositionId,
      @FormParam("ehrOrderName") final Parameter.String ehrOrderName)
  {
    service.untagTherapyForPrescription(
        patientId.getValue(),
        compositionId.getValue(),
        centralCaseId.getValue(),
        ehrOrderName.getValue());
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.TEXT_PLAIN})
  @Path("findMedicationWarnings")
  public Response findMedicationWarnings(
      @FormParam("patientAgeInDays") final Parameter.Long patientAgeInDays,
      @FormParam("patientWeightInKg") final Parameter.Double patientWeightInKg,
      @FormParam("patientAllergies") final Parameter.String patientAllergies,
      @FormParam("gabInWeeks") final Parameter.Integer gabInWeeks,
      @FormParam("bsaInM2") final Parameter.Double bsaInM2,
      @FormParam("isFemale") final Parameter.Boolean isFemale,
      @FormParam("diseaseTypeCodes") final Parameter.String diseaseTypeCodes,
      @FormParam("patientMedications") final Parameter.String patientMedications)
  {
    final MedicationForWarningsSearchDto[] medicationsArray = JsonUtil.fromJson(
        patientMedications.getValue(),
        MedicationForWarningsSearchDto[].class,
        Lists.newArrayList(INTERVAL_DESERIALIZER));
    final String[] patientAllergiesArray = JsonUtil.fromJson(patientAllergies.getValue(), String[].class);
    final String[] diseaseTypeCodesArray = JsonUtil.fromJson(diseaseTypeCodes.getValue(), String[].class);;     //todo
    final List<MedicationForWarningsSearchDto> medications = Arrays.asList(medicationsArray);
    final List<String> patientAllergiesList = Arrays.asList(patientAllergiesArray);
    final List<String> diseaseTypeCodesList = Arrays.asList(diseaseTypeCodesArray);

    final List<MedicationsWarningDto> warnings =
        service.findMedicationWarnings(
            patientAgeInDays.getValue(),
            patientWeightInKg.getValue() > 0.0 ? patientWeightInKg.getValue() : null,
            gabInWeeks.getValue() > 0 ? gabInWeeks.getValue() : null,
            bsaInM2.getValue() > 0.0 ? bsaInM2.getValue() : null,
            isFemale.getValue(),
            diseaseTypeCodesList,
            patientAllergiesList,
            medications);
    return buildJsonResponse(JsonUtil.toJson(warnings));
  }

  @GET
  @Produces({MediaType.TEXT_PLAIN})
  @Path("getTherapiesForWarnings")
  public Response getTherapiesForWarnings(
      @QueryParam("patientId") final Parameter.Long patientId)
  {
    final List<MedicationForWarningsSearchDto> therapies = service.getTherapiesForWarningsSearch(patientId.getValue());
    return buildJsonResponse(JsonUtil.toJson(null, therapies, null, Lists.newArrayList(INTERVAL_SERIALIZER)));
  }

  @GET
  @Produces({MediaType.TEXT_PLAIN})
  @Path("getMedicationDocument")
  public Response getMedicationDocument(
      @QueryParam("data") final Parameter.String data,
      @QueryParam("reference") final Parameter.String reference)
  {
    try
    {
      final ContentRequestData contentRequestData = JsonUtil.fromJson(data.getValue(), ContentRequestData.class);
      //final String reference = "9b633e02-1810-4775-a67f-c0f8d5f737bb";

      SecurityContextHolder.getContext().setAuthentication(
          new UsernamePasswordAuthenticationToken(
              new String(Base64.decode(contentRequestData.getDau()), "utf-8"),
              new String(Base64.decode(contentRequestData.getDap()), "utf-8")
          ));
      final byte[] document = service.getMedicationDocument(reference.getValue());
      return buildPdfResponse(document);
    }
    catch (Throwable th)
    {
      return null;
    }
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("patientmedicationordercardinfodata")
  public Response getPatientMedicationOrderCardInfoData(
      @QueryParam("patientId") final Parameter.Long patientId,
      @QueryParam("patientHeight") final Parameter.Double patientHeight,
      @QueryParam("compositionId") final Parameter.String compositionId,
      @QueryParam("ehrOrderName") final Parameter.String ehrOrderName,
      @QueryParam("similarTherapiesInterval") final Parameter.String similarTherapiesInterval,
      @QueryParam("language") final Parameter.String language)
  {
    final TherapyCardInfoDto medicationOrderCardInfoData =
        service.getTherapyCardInfoData(
            patientId.getValue(),
            patientHeight.getValue(),
            compositionId.getValue(),
            ehrOrderName.getValue(),
            JsonUtil.fromJson(
                similarTherapiesInterval.getValue(),
                Interval.class,
                Lists.newArrayList(INTERVAL_DESERIALIZER)),
            new Locale(language.getValue()));

    return buildJsonResponse(JsonUtil.toJson(medicationOrderCardInfoData));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getTherapyFormattedDisplay")
  public Response getTherapyFormattedDisplay(
      @QueryParam("patientId") final Parameter.Long patientId,
      @QueryParam("therapyId") final Parameter.String therapyId,
      @QueryParam("language") final Parameter.String language)
  {
    final String therapyFormattedDisplay =
        service.getTherapyFormattedDisplay(
            patientId.getValue(),
            therapyId.getValue(),
            new Locale(language.getValue()));

    return buildJsonResponse(JsonUtil.toJson(therapyFormattedDisplay));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("fillTherapyDisplayValues")
  public Response fillTherapyDisplayValues(
      @QueryParam("therapy") final Parameter.String therapy,
      @QueryParam("language") final Parameter.String language)
  {
    final TherapyDto therapyDto =
        JsonUtil.fromJson(
            therapy.getValue(),
            TherapyDto.class,
            Lists.newArrayList(TherapyJsonAdapter.INSTANCE));

    final TherapyDto therapyWithDisplayValues =
        service.fillTherapyDisplayValues(therapyDto, new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(therapyWithDisplayValues));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("modifyTherapy")
  public Response modifyTherapy(
      @FormParam("patientId") final Parameter.Long patientId,
      @FormParam("therapy") final Parameter.String therapy,
      @FormParam("centralCaseId") final Parameter.Long centralCaseId,
      @FormParam("careProviderId") final Parameter.Long careProviderId,
      @FormParam("sessionId") final Parameter.Long sessionId,
      @FormParam("knownOrganizationalEntity") final Parameter.String knownOrganizationalEntity,
      @FormParam("prescriber") final Parameter.String prescriberJson,
      @FormParam("saveDateTime") final Parameter.String saveDateTimeJson,
      @FormParam("language") final Parameter.String language)
  {
    final TherapyDto therapyDto =
        JsonUtil.fromJson(
            therapy.getValue(),
            TherapyDto.class,
            Lists.newArrayList(TherapyJsonAdapter.INSTANCE));
    final DateTime saveDateTime =
        saveDateTimeJson != null ? JsonUtil.fromJson(saveDateTimeJson.getValue(), DateTime.class) : null;
    final NamedIdentity prescriber = JsonUtil.fromJson(
        prescriberJson.getValue(),
        prescriberClass);

    service.modifyTherapy(
        patientId.getValue(),
        therapyDto,
        centralCaseId.getValue(),
        careProviderId.getValue(),
        sessionId.getValue(),
        KnownClinic.Utils.fromName(knownOrganizationalEntity.getValue()),
        prescriber,
        saveDateTime,
        new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("reviewTherapy")
  public Response reviewTherapy(
      @FormParam("patientId") final Parameter.Long patientId,
      @FormParam("compositionUid") final Parameter.String compositionUid,
      @FormParam("ehrOrderName") final Parameter.String ehrOrderName,
      @FormParam("sessionId") final Parameter.Long sessionId,
      @FormParam("knownOrganizationalEntity") final Parameter.String department)
  {
    service.reviewTherapy(
        patientId.getValue(),
        compositionUid.getValue(),
        ehrOrderName.getValue(),
        sessionId.getValue(),
        KnownClinic.Utils.fromName(department.getValue()));
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("suspendTherapy")
  public Response suspendTherapy(
      @FormParam("patientId") final Parameter.Long patientId,
      @FormParam("compositionUid") final Parameter.String compositionUid,
      @FormParam("ehrOrderName") final Parameter.String ehrOrderName)
  {
    service.suspendTherapy(patientId.getValue(), compositionUid.getValue(), ehrOrderName.getValue());
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("suspendAllTherapies")
  public Response suspendAllTherapies(
      @FormParam("patientId") final Parameter.Long patientId)
  {
    service.suspendAllTherapies(patientId.getValue(), CurrentTime.get());
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("reissueTherapy")
  public Response reissueTherapy(
      @FormParam("patientId") final Parameter.Long patientId,
      @FormParam("compositionUid") final Parameter.String compositionUid,
      @FormParam("ehrOrderName") final Parameter.String ehrOrderName,
      @FormParam("sessionId") final Parameter.Long sessionId,
      @FormParam("knownOrganizationalEntity") final Parameter.String department)
  {
    service.reissueTherapy(
        patientId.getValue(),
        compositionUid.getValue(),
        ehrOrderName.getValue(),
        sessionId.getValue(),
        KnownClinic.Utils.fromName(department.getValue()));
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("abortTherapy")
  public Response abortTherapy(
      @FormParam("patientId") final Parameter.Long patientId,
      @FormParam("compositionUid") final Parameter.String compositionUid,
      @FormParam("ehrOrderName") final Parameter.String ehrOrderName)
  {
    service.abortTherapy(patientId.getValue(), compositionUid.getValue(), ehrOrderName.getValue());
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("saveMedicationsOrder")
  public Response saveMedicationsOrder(
      @FormParam("patientId") final Parameter.Long patientId,
      @FormParam("therapies") final Parameter.String therapiesJson,
      @FormParam("centralCaseId") final Parameter.Long centralCaseId,
      @FormParam("careProviderId") final Parameter.Long careProviderId,
      @FormParam("sessionId") final Parameter.Long sessionId,
      @FormParam("knownOrganizationalEntity") final Parameter.String department,
      @FormParam("prescriber") final Parameter.String prescriberJson,
      @FormParam("saveDateTime") final Parameter.String saveDateTimeJson,
      @FormParam("language") final Parameter.String language)
  {
    final TherapyDto[] therapies =
        JsonUtil.fromJson(
            therapiesJson.getValue(), TherapyDto[].class, Lists.newArrayList(TherapyJsonAdapter.INSTANCE));
    final DateTime saveDateTime =
        saveDateTimeJson != null ? JsonUtil.fromJson(saveDateTimeJson.getValue(), DateTime.class) : null;
    final NamedIdentity prescriber =
        JsonUtil.fromJson(prescriberJson.getValue(), prescriberClass);

    service.saveNewMedicationOrder(
        patientId.getValue(),
        Arrays.asList(therapies),
        centralCaseId.getValue(),
        careProviderId.getValue(),
        sessionId.getValue(),
        KnownClinic.Utils.fromName(department.getValue()),
        prescriber,
        saveDateTime,
        new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("saveReferenceWeight")
  public Response saveReferenceWeight(
      @FormParam("patientId") final Parameter.Long patientId,
      @FormParam("weight") final Parameter.Double weight)
  {
    service.savePatientReferenceWeight(patientId.getValue(), weight.getValue());
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("findSimilarMedications")
  public Response findSimilarMedications(
      @QueryParam("medicationId") final Parameter.Long medicationId,
      @QueryParam("routeCode") final Parameter.String routeCode)
  {
    return buildJsonResponse(JsonUtil.toJson(service.findSimilarMedications(medicationId.getValue(), routeCode.getValue())));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getMedicationProducts")
  public Response getMedicationProducts(
      @QueryParam("medicationId") final Parameter.Long medicationId,
      @QueryParam("routeCode") final Parameter.String routeCode)
  {
    return buildJsonResponse(JsonUtil.toJson(service.getMedicationProducts(medicationId.getValue(), routeCode.getValue())));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getRoutes")
  public Response getRoutes()
  {
    return buildJsonResponse(JsonUtil.toJson(service.getRoutes()));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getDoseForms")
  public Response getDoseForms()
  {
    return buildJsonResponse(JsonUtil.toJson(service.getDoseForms()));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getMedicationBasicUnits")
  public Response getMedicationBasicUnits()
  {
    return buildJsonResponse(JsonUtil.toJson(service.getMedicationBasicUnits()));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("saveConsecutiveDays")
  public Response saveConsecutiveDays(
      @FormParam("patientId") final Parameter.Long patientId,
      @FormParam("compositionUid") final Parameter.String compositionUid,
      @FormParam("ehrOrderName") final Parameter.String ehrOrderName,
      @FormParam("pastDaysOfTherapy") final Parameter.Integer pastDaysOfTherapy)
  {
    service.saveConsecutiveDays(
        patientId.getValue(),
        compositionUid.getValue(),
        ehrOrderName.getValue(),
        pastDaysOfTherapy.getValue());
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getLastTherapiesForPreviousHospitalization")
  public Response getLastTherapiesForPreviousHospitalization(
      @QueryParam("patientId") final Parameter.Long patientId,
      @QueryParam("patientHeight") final Parameter.Double patientHeight,
      @QueryParam("language") final Parameter.String language)
  {
    final List<TherapyDto> therapyDtoList =
        service.getLastTherapiesForPreviousHospitalization(patientId.getValue(), patientHeight.getValue(), new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(therapyDtoList));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getTherapyTemplates")
  public Response getTherapyTemplates(
      @QueryParam("organizationalEntityId") final Parameter.Long departmentId,
      @QueryParam("userId") final Parameter.Long userId,
      @QueryParam("patientId") final Parameter.Long patientId,
      @QueryParam("referenceWeight") final Parameter.Double referenceWeight,
      @QueryParam("patientHeight") final Parameter.Double patientHeight,
      @QueryParam("language") final Parameter.String language)
  {
    final TherapyTemplatesDto therapyTemplates = service.getTherapyTemplates(
        userId.getValue(),
        departmentId.getValue(),
        patientId.getValue(),
        referenceWeight.getValue(),
        patientHeight.getValue(),
        new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(therapyTemplates));

    //TherapyTemplatesDto therapyTemplatesDto = new TherapyTemplatesDto();
    //return buildJsonResponse(JsonUtil.toJson(therapyTemplatesDto));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("saveTherapyTemplate")
  public Response saveTherapyTemplate(
      @FormParam("template") final Parameter.String template)
  {
    final TherapyTemplateDto templateDto =
        JsonUtil.fromJson(template.getValue(), TherapyTemplateDto.class, Lists.newArrayList(TherapyJsonAdapter.INSTANCE));
    final long newTemplateId = service.saveTherapyTemplate(templateDto);
    return buildJsonResponse(JsonUtil.toJson(newTemplateId));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("deleteTherapyTemplate")
  public Response deleteTherapyTemplate(
      @FormParam("templateId") final Parameter.Long templateId)
  {
    service.deleteTherapyTemplate(templateId.getValue());
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getTherapyTimelineData")
  public Response getTherapyTimelineData(
      @QueryParam("patientId") final Parameter.Long patientId,
      @QueryParam("centralCaseId") final Parameter.Long centralCaseId,
      @QueryParam("knownOrganizationalEntityName") final Parameter.String knownOrganizationalEntityName,
      @QueryParam("timelineInterval") final Parameter.String timelineInterval,
      @QueryParam("roundsInterval") final Parameter.String roundsInterval,
      @QueryParam("therapySortTypeEnum") final Parameter.String therapySortTypeString,
      @QueryParam("language") final Parameter.String language)
  {
    final KnownClinic knownClinic = KnownClinic.Utils.fromName(
        knownOrganizationalEntityName.getValue());
    final RoundsIntervalDto roundsIntervalDto = JsonUtil.fromJson(roundsInterval.getValue(), RoundsIntervalDto.class);
    final Interval searchInterval =
        JsonUtil.fromJson(
            timelineInterval.getValue(), Interval.class, Lists.newArrayList(INTERVAL_DESERIALIZER));
    final TherapySortTypeEnum therapySortTypeEnum = TherapySortTypeEnum.valueOf(therapySortTypeString.getValue());
    final List<TherapyTimelineRowDto> timelineRows =
        service.getTherapyTimelineData(
            patientId.getValue(),
            centralCaseId.getValue() != null ? centralCaseId.getValue() : 0,
            searchInterval,
            roundsIntervalDto,
            therapySortTypeEnum,
            knownClinic,
            new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(timelineRows));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("confirmTherapyAdministration")
  public Response confirmTherapyAdministration(
      @FormParam("therapyCompositionUid") final Parameter.String therapyCompositionUid,
      @FormParam("ehrOrderName") final Parameter.String ehrOrderName,
      @FormParam("patientId") final Parameter.Long patientId,
      @FormParam("administrationSuccessful") final Parameter.Boolean administrationSuccessful,
      @FormParam("editMode") final Parameter.Boolean editMode,
      @FormParam("administration") final Parameter.String administration,
      @FormParam("centralCaseId") final Parameter.Long centralCaseId,
      @FormParam("knownOrganizationalEntity") final Parameter.String department,
      @FormParam("careProviderId") final Parameter.Long careProviderId,
      @FormParam("sessionId") final Parameter.Long sessionId,
      @FormParam("language") final Parameter.String language)
  {
    final AdministrationDto therapyAdministrationDto =
        JsonUtil.fromJson(
            administration.getValue(),
            AdministrationDto.class,
            Lists.newArrayList(ADMINISTRATION_DESERIALIZER));

    service.confirmTherapyAdministration(
        therapyCompositionUid.getValue(),
        ehrOrderName.getValue(),
        patientId.getValue(),
        therapyAdministrationDto,
        (editMode == null || editMode.getValue() == null) ? false : editMode.getValue(),
        administrationSuccessful.getValue(),
        centralCaseId.getValue(),
        department.getValue(),
        careProviderId.getValue(),
        sessionId.getValue(),
        new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("createAdministrationTask")
  public Response createAdministrationTask(
      @FormParam("therapyCompositionUid") final Parameter.String therapyCompositionUid,
      @FormParam("ehrOrderName") final Parameter.String ehrOrderName,
      @FormParam("patientId") final Parameter.Long patientId,
      @FormParam("administration") final Parameter.String administration)
  {
    final StartAdministrationDto therapyAdministrationDto =
        JsonUtil.fromJson(
            administration.getValue(),
            StartAdministrationDto.class);

    service.createAdditionalAdministrationTask(
        therapyCompositionUid.getValue(),
        ehrOrderName.getValue(),
        patientId.getValue(),
        therapyAdministrationDto);

    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("rescheduleTasks")
  public Response rescheduleTasks(
      @FormParam("taskId") final Parameter.String taskId,
      @FormParam("fromTime") final Parameter.String fromTimeJson,
      @FormParam("dueTime") final Parameter.String dueTimeJson,
      @FormParam("rescheduleSingleTask") final Parameter.Boolean rescheduleSingleTask,
      @FormParam("therapyId") final Parameter.String therapyId)
  {
    final DateTime dueTime = JsonUtil.fromJson(dueTimeJson.getValue(), DateTime.class);
    final DateTime fromTime = JsonUtil.fromJson(fromTimeJson.getValue(), DateTime.class);
    final long moveTimeInMillis = dueTime.getMillis() - fromTime.getMillis();
    service.rescheduleTasks(
        taskId.getValue(),
        fromTime,
        moveTimeInMillis,
        rescheduleSingleTask.getValue(),
        therapyId.getValue());
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("deleteTask")
  public Response deleteTask(
      @FormParam("taskId") final Parameter.String taskId,
      @FormParam("comment") final Parameter.String comment)
  {
    service.deleteTask(taskId.getValue(), comment.getValue());
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("deleteAdministration")
  public Response deleteAdministration(
      @FormParam("patientId") final Parameter.Long patientId,
      @FormParam("administrationId") final Parameter.String administrationId,
      @FormParam("taskId") final Parameter.String taskId,
      @FormParam("comment") final Parameter.String comment)
  {
    service.deleteAdministration(patientId.getValue(), administrationId.getValue(), taskId.getValue(), comment.getValue());
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getCareProfessionals")
  public Response getCareProfessionals()
  {
    final List<? extends NamedIdentity> careProfessionalList = service.getCareProfessionals();
    return buildJsonResponse(JsonUtil.toJson(careProfessionalList));
  }

  private static final JsonUtil.TypeAdapterPair PAIR_SERIALIZER = new JsonUtil.TypeAdapterPair(
      Pair.class, new JsonSerializer<Pair>()
  {
    @Override
    public JsonElement serialize(final Pair src, final Type typeOfSrc, final JsonSerializationContext context)
    {
      final JsonObject result = new JsonObject();
      result.add("first", context.serialize(src.getFirst()));
      result.add("second", context.serialize(src.getSecond()));
      return result;
    }
  });

  private static final JsonUtil.TypeAdapterPair INTERVAL_SERIALIZER = new JsonUtil.TypeAdapterPair(
      Interval.class, new JsonSerializer<Interval>()
  {
    @Override
    public JsonElement serialize(final Interval src, final Type typeOfSrc, final JsonSerializationContext context)
    {
      final JsonObject result = new JsonObject();
      result.add("startMillis", context.serialize(src.getStartMillis()));
      result.add("endMillis", context.serialize(src.getEndMillis()));
      return result;
    }
  });

  private static final JsonUtil.TypeAdapterPair INTERVAL_DESERIALIZER = new JsonUtil.TypeAdapterPair(
      Interval.class, new JsonDeserializer<Interval>()
  {
    @Override
    public Interval deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
        throws JsonParseException
    {
      final Long startMillis = context.deserialize(json.getAsJsonObject().get("startMillis"), Long.class);
      final Long endMillis = context.deserialize(json.getAsJsonObject().get("endMillis"), Long.class);
      if (startMillis == null && endMillis == null)
      {
        return Intervals.INFINITE;
      }
      if (startMillis == null)
      {
        return Intervals.infiniteTo(new DateTime(endMillis));
      }
      if (endMillis == null)
      {
        return Intervals.infiniteFrom(new DateTime(startMillis));
      }
      return new Interval(startMillis, endMillis);
    }
  });

  private static final JsonUtil.TypeAdapterPair ADMINISTRATION_DESERIALIZER = new JsonUtil.TypeAdapterPair(
      AdministrationDto.class, new JsonDeserializer<AdministrationDto>()
  {
    @Override
    public AdministrationDto deserialize(
        final JsonElement json,
        final Type typeOfT,
        final JsonDeserializationContext context)
        throws JsonParseException
    {
      final AdministrationTypeEnum administrationTypeEnum =
          context.deserialize(json.getAsJsonObject().get("administrationType"), AdministrationTypeEnum.class);
      if (administrationTypeEnum == AdministrationTypeEnum.START)
      {
        return context.deserialize(json, StartAdministrationDto.class);
      }
      if (administrationTypeEnum == AdministrationTypeEnum.ADJUST_INFUSION)
      {
        return context.deserialize(json, AdjustInfusionAdministrationDto.class);
      }
      if (administrationTypeEnum == AdministrationTypeEnum.STOP)
      {
        return context.deserialize(json, StopAdministrationDto.class);
      }
      if (administrationTypeEnum == AdministrationTypeEnum.INFUSION_SET_CHANGE)
      {
        return context.deserialize(json, InfusionSetChangeDto.class);
      }
      return null;
    }
  });

  private static class ContentRequestData extends PdfViewerRequestData
  {
    private String reference;
    private Boolean print;

    public String getReference()
    {
      return reference;
    }

    public Boolean isPrint()
    {
      return print;
    }
  }
}
