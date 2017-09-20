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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.maf.core.time.CurrentTime;
import com.marand.maf.core.time.Intervals;
import com.marand.thinkmed.api.externals.data.object.ExternalCatalogDto;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.html.AppHtmlConst;
import com.marand.thinkmed.html.AppHtmlViewConfig;
import com.marand.thinkmed.html.HtmlViewServlet;
import com.marand.thinkmed.html.Parameter;
import com.marand.thinkmed.html.externals.AngularJSHtmlExternal;
import com.marand.thinkmed.html.externals.AngularJSHtmlExternalPlugin;
import com.marand.thinkmed.html.externals.HighchartsHtmlExternal;
import com.marand.thinkmed.html.externals.MomentJsHtmlExternal;
import com.marand.thinkmed.html.externals.VisJsHtmlExternal;
import com.marand.thinkmed.html.framework.TmJQueryAngularHtmlFramework;
import com.marand.thinkmed.html.framework.TmJQueryHtmlFramework;
import com.marand.thinkmed.medications.ActionReasonType;
import com.marand.thinkmed.medications.MedicationFinderFilterEnum;
import com.marand.thinkmed.medications.SelfAdministeringActionEnum;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.TherapyJsonDeserializer;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.TherapyTemplateModeEnum;
import com.marand.thinkmed.medications.TitrationType;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.UserPersonDto;
import com.marand.thinkmed.medications.dto.CodedNameDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.SaveMedicationOrderDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.TherapyReloadAfterActionDto;
import com.marand.thinkmed.medications.dto.TherapyTemplateDto;
import com.marand.thinkmed.medications.dto.TherapyTemplatesDto;
import com.marand.thinkmed.medications.dto.TherapyViewPatientDto;
import com.marand.thinkmed.medications.dto.TitrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationPatientTaskDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionGroupDto;
import com.marand.thinkmed.medications.dto.audittrail.TherapyAuditTrailDto;
import com.marand.thinkmed.medications.dto.barcode.BarcodeTaskSearchDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeType;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeGroupDto;
import com.marand.thinkmed.medications.dto.document.TherapyDocumentsDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTemplateDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTherapyDto;
import com.marand.thinkmed.medications.dto.overview.TherapyFlowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyTimelineDto;
import com.marand.thinkmed.medications.dto.pharmacist.perfusionSyringe.PerfusionSyringePreparationDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.MedicationSupplyTypeEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewTherapyDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewsDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.ReviewPharmacistReviewAction;
import com.marand.thinkmed.medications.dto.pharmacist.review.SupplyDataForPharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.PerfusionSyringePatientTasksDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.PerfusionSyringeTaskSimpleDto;
import com.marand.thinkmed.medications.dto.reconsiliation.ReconciliationRowDto;
import com.marand.thinkmed.medications.dto.supply.MedicationSupplyCandidateDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsActionDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsType;
import com.marand.thinkmed.medications.rule.MedicationParacetamolRuleType;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import com.marand.thinkmed.medications.rule.parameters.ParacetamolRuleForAdministrationParameters;
import com.marand.thinkmed.medications.rule.parameters.ParacetamolRuleForTherapiesParameters;
import com.marand.thinkmed.medications.rule.parameters.ParacetamolRuleForTherapyParameters;
import com.marand.thinkmed.medications.rule.parameters.RuleParameters;
import com.marand.thinkmed.medications.service.MedicationsService;
import com.marand.thinkmed.medications.task.SupplyRequestStatus;
import com.marand.thinkmed.medicationsexternal.WarningSeverity;
import com.marand.thinkmed.medicationsexternal.dto.MedicationForWarningsSearchDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationWarningsDto;
import org.jboss.resteasy.annotations.GZIP;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
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
  private static final String ANGULAR_MODULE_DIR_PATH = MODULE_DIR_PATH + "/jquery/pharmacistsTasks/angularComponents";
  private static final String ANGULAR_NURSE_TASK_MODULE_DIR_PATH = MODULE_DIR_PATH + "/jquery/nurseTasks/angularComponents";

  private static final String MODULE_COMMON_DIR_PATH = MODULE_DIR_PATH + "/jquery/common";
  private static final String MODULE_ORDERING_DIR_PATH = MODULE_DIR_PATH + "/jquery/ordering";
  private static final String MODULE_THERAPY_DIR_PATH = MODULE_DIR_PATH + "/jquery/therapy";
  private static final String MODULE_TIMELINE_DIR_PATH = MODULE_DIR_PATH + "/jquery/timeline";
  private static final String MODULE_PHARMACISTS_REVIEW_DIR_PATH = MODULE_DIR_PATH + "/jquery/pharmacists";
  private static final String MODULE_OUTPATIENT_DIR_PATH = MODULE_DIR_PATH + "/jquery/outpatient";
  private static final String MODULE_MENTAL_HEALTH_DIR_PATH = MODULE_DIR_PATH + "/jquery/mentalHealth";
  private static final String MODULE_WARNING_DIR_PATH = MODULE_DIR_PATH + "/jquery/warning";
  private static final String MODULE_RECONCILIATION_DIR_PATH = MODULE_DIR_PATH + "/jquery/reconciliation";
  private static final String MODULE_THERAPY_DOCUMENTATION_DIR_PATH = MODULE_DIR_PATH + "/jquery/documentation";

  private MedicationsService service;

  @Required
  @Autowired
  public void setService(final MedicationsService service)
  {
    this.service = service;
  }

  /**
   * ****************** THERAPY VIEW * *******************
   */

  @GET
  @GZIP
  @Path("therapyView")
  public Response therapyView(
      @QueryParam("data") final Parameter.String data,
      @QueryParam("language") final String language,
      @QueryParam("theme") final String theme,
      @QueryParam("dictionary") final String dictionary,
      @HeaderParam("Request-Time") final Parameter.DateTime requestTime)
  {
    final AppHtmlViewConfig config = buildViewConfig("therapy", theme, language);
    if("all".equals(dictionary))
    {
      config.addAllDictionaries();
    }
    addTimeOffsetToConfig(config, requestTime);
    //properties
    final Map<String, Object> properties = service.getProperties();
    for (final Map.Entry<String, Object> property : properties.entrySet())
    {
      config.addProperty(property.getKey(), property.getValue());
    }

    //user authorities
    addUserDataToConfig(config);

    return buildJsonResponse(config.toJson());
  }

  @GET
  @Path("proxyTherapyView")
  public Response proxyTherapyView(
      @QueryParam("data") final Parameter.String data,
      @QueryParam("language") final String language,
      @QueryParam("theme") final String theme,
      @HeaderParam("Request-Time") final Parameter.DateTime requestTime)
  {
    final AppHtmlViewConfig config = buildViewConfig("proxyTherapyView", theme, language);
    config.addProperty("OPEN_EP_URL", "http://openep.thinkmed.marand.si:8080"); // TODO
    addTimeOffsetToConfig(config, requestTime);
    return buildJsonResponse(config.toJson());
  }

  @GET
  @GZIP
  @Path("therapyTasksInpatientView")
  public Response therapyTasksInpatientView(
      @QueryParam("data") final Parameter.String data,
      @QueryParam("language") final String language,
      @QueryParam("theme") final String theme,
      @HeaderParam("Request-Time") final Parameter.DateTime requestTime)
  {
    final AppHtmlViewConfig config = buildViewConfig("therapyTasksInpatientView", theme, language);
    addTimeOffsetToConfig(config, requestTime);
    return buildJsonResponse(config.toJson());
  }

  @GET
  @Path("demoTherapyView")
  public Response demoTherapyView(
      @QueryParam("data") final Parameter.String data,
      @QueryParam("language") final String language,
      @QueryParam("theme") final String theme,
      @HeaderParam("Request-Time") final Parameter.DateTime requestTime)
  {
    final AppHtmlViewConfig config = buildViewConfig("demoTherapyView", theme, language);
    config.addProperty("patientId", service.getDemoPatientId());
    addTimeOffsetToConfig(config, requestTime);
    return buildJsonResponse(config.toJson());
  }

  @GET
  @Produces({MediaType.TEXT_HTML})
  @Path("demoPortalTherapyView")
  public Response openepView(
      @QueryParam("data") final Parameter.String data,
      @QueryParam("theme") final Parameter.String theme,
      @QueryParam("language") final Parameter.String language,
      @QueryParam("debug") @DefaultValue("false") final Boolean debug,
      @HeaderParam("Request-Time") final Parameter.DateTime requestTime)
  {
    final AppHtmlViewConfig config = buildViewConfig("demoPortalTherapyView", theme.getValue(), language.getValue());
    addTimeOffsetToConfig(config, requestTime);
    return buildJsonResponse(config.toJson());
  }

  @GET
  @Produces({MediaType.TEXT_HTML})
  @Path("stressTestTherapyView")
  public Response stressTestTherapyView(
      @QueryParam("data") final Parameter.String data,
      @QueryParam("theme") final Parameter.String theme,
      @QueryParam("language") final Parameter.String language,
      @QueryParam("debug") @DefaultValue("false") final Boolean debug,
      @HeaderParam("Request-Time") final Parameter.DateTime requestTime)
  {
    final AppHtmlViewConfig config = buildViewConfig("stressTestTherapyView", theme.getValue(), language.getValue());
    addTimeOffsetToConfig(config, requestTime);
    return buildJsonResponse(config.toJson());
  }

  @GET
  @GZIP
  @Produces({MediaType.TEXT_HTML})
  @Path("pharmacistTasksView")
  public Response pharmacistTasksView(
      @QueryParam("data") final Parameter.String data,
      @QueryParam("theme") final Parameter.String theme,
      @QueryParam("language") final Parameter.String language,
      @QueryParam("dictionary") final String dictionary,
      @HeaderParam("Request-Time") final Parameter.DateTime requestTime)
  {
    final AppHtmlViewConfig config = buildViewConfig("pharmacistTasksView", theme.getValue(), language.getValue());
    if("all".equals(dictionary))
    {
      config.addAllDictionaries();
    }
    addTimeOffsetToConfig(config, requestTime);
    //user authorities
    addUserDataToConfig(config);
    return buildJsonResponse(config.toJson());
  }

  @GET
  @GZIP
  @Produces({MediaType.TEXT_HTML})
  @Path("nurseTasksView")
  public Response nurseTasksView(
      @QueryParam("data") final Parameter.String data,
      @QueryParam("theme") final Parameter.String theme,
      @QueryParam("language") final Parameter.String language,
      @QueryParam("dictionary") final String dictionary,
      @HeaderParam("Request-Time") final Parameter.DateTime requestTime)
  {
    final AppHtmlViewConfig config = buildViewConfig("nurseTasksView", theme.getValue(), language.getValue());
    if("all".equals(dictionary))
    {
      config.addAllDictionaries();
    }
    addTimeOffsetToConfig(config, requestTime);
    return buildJsonResponse(config.toJson());
  }

  @Override
  protected AppHtmlViewConfig getViewConfig(final String view, final String theme, final String language)
  {
    if ("therapy".equals(view))
    {
      return getTherapyViewConfig(view, theme, language);
    }
    if ("proxyTherapyView".equals(view))
    {
      return getProxyTherapyViewConfig(theme, language);
    }
    if ("demoTherapyView".equals(view))
    {
      return getTherapyViewConfig(view, theme, language);
    }
    if ("therapyTasksInpatientView".equals(view))
    {
      return getTherapyTasksInpatientViewConfig(theme, language);
    }
    if ("demoPortalTherapyView".equals(view))
    {
      return getDemoPortalTherapyViewConfig(view, theme, language);
    }
    if ("stressTestTherapyView".equals(view))
    {
      return getStressTestTherapyViewConfig(theme, language);
    }
    if ("pharmacistTasksView".equals(view))
    {
      return getPharmacistTaskLists(theme, language);
    }
    if ("nurseTasksView".equals(view))
    {
      return getNurseTaskLists(theme, language);
    }
    return null;
  }

  private void addUserDataToConfig(final AppHtmlViewConfig config)
  {
    final UserPersonDto userData = service.getUserData();
    config.addProperty("userPersonId", userData.getId());
    config.addProperty("userPersonName", userData.getName());
    final Map<String, Boolean> userAuthorities = userData.getAuthorities();
    for (final Map.Entry<String, Boolean> userAuthority : userAuthorities.entrySet())
    {
      config.addProperty(userAuthority.getKey(), userAuthority.getValue());
    }
  }

  private void addTimeOffsetToConfig(final AppHtmlViewConfig config, final Parameter.DateTime requestTime)
  {
    config.addProperty("timeOffset", calculateTimeOffset(requestTime));
  }

  private long calculateTimeOffset(final Parameter.DateTime requestTime)
  {
    return CurrentTime.get().getMillis() - requestTime.getValue().getMillis();
  }

  private AppHtmlViewConfig getTherapyViewConfig(final String view, final String theme, final String language)
  {
    final AppHtmlViewConfig config =
        buildAppHtmlViewConfig("tm.views.medications.TherapyView", new TmJQueryHtmlFramework(), theme, language);

    final AngularJSHtmlExternal angularJSHtmlExternal =
        AngularJSHtmlExternal.create(AngularJSHtmlExternal.VERSION_1_5_5.getVersion());
    angularJSHtmlExternal.addPlugin(AngularJSHtmlExternalPlugin.COMMON_1_5_5);

    // externals //
    config.addExternals(
        MomentJsHtmlExternal.VERSION_2_11_1,
        VisJsHtmlExternal.VERSION_4_3_0,
        angularJSHtmlExternal, // documentation
        HighchartsHtmlExternal.VERSION_4_1_5 // titration
    );

    config.addProperty("ISPEK_SERVER_REST_URL", getHtmlProperties().getIspekServerRestHost());

    // style sheets //
    config.addStyleSheetDependency(MODULE_THERAPY_DIR_PATH + "/TherapyView.css");
    config.addStyleSheetDependency(MODULE_WARNING_DIR_PATH + "/TherapyWarnings.css");
    config.addStyleSheetDependency(MODULE_WARNING_DIR_PATH + "/Monograph.css");

    // java scripts //
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/TherapyDisplayProvider.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/RestApi.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/MedicationUtils.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/CurrentTime.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/MedicationTimingUtils.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/MedicationRuleUtils.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/ValueLabel.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/TherapyEnums.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/TherapyActions.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/TherapyDataSorter.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/TherapyTasksRemindersContainer.js");

    // common elements
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/AdditionalWarnings.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/Therapy.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/OxygenTherapy.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/OxygenStartingDevice.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/TherapyChangeReason.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/SaveMedicationOrder.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/TherapyTemplateElement.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/MedicationData.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/Medication.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/TherapyDose.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/TherapyAuditTrail.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/TherapyActionHistory.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/TherapyChange.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/Range.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/BarcodeTaskSearch.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/auditTrail/AuditTrailContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/testing/RenderCoordinator.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/ProtocolSummaryContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/RangeField.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/TherapyContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/BaseTherapyDetailsContentContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/DosingPatternValidator.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/TherapyDetailsContentContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/TherapyDetailsLegendContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/TherapyDetailsLegendContainer.Filters.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/TherapyContainerToolbars.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/TherapyJsonConverter.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/TherapyGroupPanel.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/TemplateTherapyGroupPanel.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/VerticallyTitledComponent.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/ChangeReasonDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/SaveMedicationOrderTherapyDisplayProvider.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/MedicationSearchField.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/MedicationDetailsContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/TherapyMedicationDataLoader.js");

    //view
    config.addJavaScriptDependency(MODULE_THERAPY_DIR_PATH + "/TherapyView.js");

    //authority
    config.addJavaScriptDependency(MODULE_THERAPY_DIR_PATH + "/TherapyAuthority.js");

    //warnings
    config.addJavaScriptDependency(MODULE_WARNING_DIR_PATH + "/WarningsHelpers.js");
    config.addJavaScriptDependency(MODULE_WARNING_DIR_PATH + "/SimpleWarningsContainer.js");

    //therapyFlow
    config.addJavaScriptDependency(MODULE_THERAPY_DIR_PATH + "/TherapyFlowGrid.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DIR_PATH + "/TherapyOverviewHeader.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DIR_PATH + "/GridTherapyContainerToolbar.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DIR_PATH + "/ReferenceWeightPane.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DIR_PATH + "/PharmacistReviewPane.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DIR_PATH + "/PharmacistReviewPaneReviewHeader.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DIR_PATH + "/SelfAdministrationContainer.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DIR_PATH + "/ShowExternalPrescriptionsDataEntryContainer.js");

    //ordering complex
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/ComplexTherapyContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/HeparinPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/InfusionRatePane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/VolumeSumPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/ComplexTherapyMedicationPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/ComplexVariableRateDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/ComplexVariableRateRowContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/InfusionRateFormulaUnitPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/ComplexTherapyEditContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/UniversalComplexTherapyContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/InfusionRateTypePane.js");

    //ordering simple
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/simple/SimpleTherapyContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/simple/SimpleVariableDosePane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/simple/SimpleVariableDoseDaysPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/simple/ProtocolOptionsContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/simple/SimpleTherapyEditContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/simple/UniversalSimpleTherapyContainer.js");

    // ordering oxygen
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/oxygen/OxygenSaturationInputContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/oxygen/OxygenRouteContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/oxygen/OxygenTherapyContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/oxygen/OxygenTherapyEditDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/oxygen/OxygenFlowRateValidator.js");

    //ordering
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/TherapyDaysContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/MedicationDetailsCardPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/RoutesPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/MedicationsTitleHeader.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/SearchContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/TemplatesContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/MedicationsOrderingContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/BasketContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/OrderingContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/WarningsContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/DosingFrequencyPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/DosePane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/UniversalDosePane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/DosingPatternPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/TherapyIntervalPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/TherapyNextAdministrationLabelPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/CalculatedDosagePane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/ChangeReasonPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/BnfMaximumPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/ParacetamolLimitContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/OverdoseContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/TherapySaveDatePane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/EditPastDaysOfTherapyContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/SaveTemplatePane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/LinkTherapyPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/ApplicationPreconditionPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/WarningOverrideReasonPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/AdministrationPreviewTimeline.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/TherapyContainerToolbars.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/CommentIndicationPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/PrescriptionContentExtensionContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/TherapySupplyContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/ControlDrugsSupplyContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/ControlDrugsSupplyRowContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/HighRiskMedicationIconsContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/UniversalMedicationDataContainer.js");

    //timeline
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/TherapyTimeline.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/TherapyTimelineContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/TherapyTimelineUtils.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/TherapyAdministrationContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/RescheduleTasksContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/TherapyAdministrationDetailsContentContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/TimelineTherapyContainerToolbar.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/PerfusionSyringeDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/AdministrationWarningContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/WitnessPane.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/TherapyTimelineAdditionalWarningsDialogContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/TherapyTimelineTooltip.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/administration/OxygenStartingDeviceDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/administration/PlannedDoseTimeValidator.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/administration/AdministrationWarningsProvider.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/administration/MedicationBarcodeContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/administration/TherapyAdministrationDialogBuilder.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/dto/TherapyForTitration.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/dto/Titration.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/dto/QuantityWithTime.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/ChartHelpers.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/TherapyDoseHistoryRowContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/MeasurementResultRowContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/BaseApplicationRowContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/DoseApplicationRowContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/RateApplicationRowContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/TitrationBasedAdministrationDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/TitrationDataLoader.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/TitrationDialogBuilder.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/DoctorsCommentDataEntryContainer.js");

    // outpatient prescribing (EER)
    config.addJavaScriptDependency(MODULE_OUTPATIENT_DIR_PATH + "/EERContentExtensionContainer.js");
    config.addJavaScriptDependency(MODULE_OUTPATIENT_DIR_PATH + "/OutpatientOrderingContainer.js");

    // T2T3 prescribing
    config.addJavaScriptDependency(MODULE_MENTAL_HEALTH_DIR_PATH + "/T2T3OrderingContainer.js");
    config.addJavaScriptDependency(MODULE_MENTAL_HEALTH_DIR_PATH + "/T2T3TherapySelectionColumn.js");
    config.addJavaScriptDependency(MODULE_MENTAL_HEALTH_DIR_PATH + "/T2T3BasketContainer.js");
    config.addJavaScriptDependency(MODULE_MENTAL_HEALTH_DIR_PATH + "/T2T3BnfMaximumContainer.js");
    config.addJavaScriptDependency(MODULE_MENTAL_HEALTH_DIR_PATH + "/TherapyContainerToolbars.js");
    config.addJavaScriptDependency(MODULE_MENTAL_HEALTH_DIR_PATH + "/RouteSelectionContainer.js");
    config.addJavaScriptDependency(MODULE_MENTAL_HEALTH_DIR_PATH + "/TherapyGroupPanel.js");

    //pharmacist's review
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/dto/TherapyProblemDescription.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/dto/PharmacistMedicationReview.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/ResizingTextArea.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/ColumnContainer.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/ProblemDescriptionViewContainer.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/ProblemDescriptionEditContainer.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/ReviewView.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/TherapyContainerToolbars.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/TherapyContainer.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/ReviewContainerViewContentCard.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/ReviewContainerEditContentCard.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/ReviewContainerHeader.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/ConfirmAllTherapiesPlaceholderContainer.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/ReviewContainer.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/DailyReviewsContainer.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/DailyReviewsContainerHeader.js");

    // medicine reconciliation
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/dto/SummaryRowTherapyData.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/dto/SourceMedication.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/dto/DischargeSourceMedication.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/dto/MedicationGroup.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/dto/MedicationGroupTherapy.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/dto/MedicationOnAdmissionGroup.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/dto/MedicationOnDischargeGroup.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/dto/MedicationOnAdmission.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/dto/MedicationOnDischarge.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/SummaryRowContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/SummaryView.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/BasketContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/TherapyContainerToolbars.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/TherapySelectionColumn.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/MedicineReconciliationContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/MedicineReconciliationDialogButtons.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/AdmissionMedReconciliationContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/InpatientMedReconciliationContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/DischargeMedsReconciliationContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/AdmissionInpatientMedReconciliationEntryContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/DischargeMedReconciliationEntryContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/SuspendAdmissionTherapyContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/MedicationOnDischargeTherapyDisplayProvider.js");

    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/TherapyDocumentationApp.module.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/ehr/Ehr.module.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/ehr/CompositionUidUtils.service.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/hub/Hub.module.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/hub/HubActionName.constant.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/Document.module.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/TmMedsDocumentPresenter.directive.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/DocumentPresenterController.controller.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/eerPrescription/TmMedsEerPrescriptionDocument.directive.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/eerPrescription/EerPrescriptionDocumentController.controller.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/eerPrescription/TmMedsEerPrescriptionDocumentSection.directive.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/eerPrescription/EerPrescriptionDocumentSectionController.controller.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/externalEerPrescription/TmMedsExternalEerPrescriptionDocument.directive.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/externalEerPrescription/TmMedsExternalEerPrescriptionDocumentController.controller.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/externalEerPrescription/TmMedsExternalEerPrescriptionDocumentSection.directive.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/externalEerPrescription/TmMedsExternalEerPrescriptionDocumentSectionController.controller.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/mentalHealth/MentalHealthDocumentController.controller.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/mentalHealth/TmMedsMentalHealthDocument.directive.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/TmMedsPrescriptionLocalDetailsAdditionalInformationValueFilter.filter.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/TmMedsEnumTranslationFilter.filter.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/documentHeader/DocumentHeader.module.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/documentHeader/TmMedsNamedExternalNameFilter.filter.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/documentHeader/TmMedsDocumentHeader.directive.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/documentHeader/DocumentHeaderController.controller.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/TherapyDocumentationModels.module.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/PrescriptionDocumentType.constant.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/PrescriptionLocalDetails.factory.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/PrescriptionPackage.factory.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/ExternalPrescriptionPackage.factory.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/ExternalPrescriptionTherapy.factory.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/MentalHealthDocumentContent.factory.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/MentalHealthTemplate.factory.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/MentalHealthMedication.factory.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/TherapyDocumentType.constant.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/PrescriptionStatus.constant.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/PrescriptionTherapy.factory.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/Therapy.factory.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/TherapyDocument.factory.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/TherapyDocuments.factory.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/Data.module.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/DocumentService.service.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/DocumentRestApi.service.js");

    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/externalPrescriptions/ExternalPrescriptions.module.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/externalPrescriptions/ExternalPrescriptionsPresenter.service.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/externalPrescriptions/PrescriptionsDialogController.controller.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/indexColumn/IndexColumn.module.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/indexColumn/TmMedsIndexColumnList.directive.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/indexColumn/TmMedsIndexColumnListItem.directive.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/indexColumn/ListItemController.controller.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/multiDocumentColumn/MultiDocumentColumn.module.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/multiDocumentColumn/TmMedsMultiDocumentColumn.directive.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/singleDocumentColumn/SingleDocumentColumn.module.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/singleDocumentColumn/TmMedsSingleDocumentColumn.directive.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/TherapyDocumentationView.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/TherapyDocumentationController.controller.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/TmMedsTherapyDocumentationView.directive.js");

    // resources - dictionaries //

    // resources - paths //
    return config;
  }

  private AppHtmlViewConfig getProxyTherapyViewConfig(final String theme, final String language)
  {
    final AppHtmlViewConfig config = buildAppHtmlViewConfig(
        "app.views.medications.proxy.ProxyTherapyView",
        new TmJQueryHtmlFramework(),
        theme,
        language
    );

    // java scripts //
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/proxy/app.views.medications.proxy.ProxyTherapyView.js");

    // style sheets //
    config.addStyleSheetDependency(MODULE_DIR_PATH + "/jquery/proxy/app.views.medications.proxy.ProxyTherapyView.css");

    return config;
  }

  private AppHtmlViewConfig getTherapyTasksInpatientViewConfig(final String theme, final String language)
  {
    final AppHtmlViewConfig config = buildAppHtmlViewConfig(
        "app.views.medications.inpatient.TherapyTasksInpatientView",
        new TmJQueryHtmlFramework(),
        theme,
        language
    );

    // java scripts //
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/inpatients/TherapyTasksInpatientView.js");

    // style sheets //
    config.addStyleSheetDependency(MODULE_DIR_PATH + "/jquery/inpatients/TherapyTasksInpatientView.css");

    return config;
  }

  private AppHtmlViewConfig getDemoPortalTherapyViewConfig(final String view, final String theme, final String language)
  {
    final AppHtmlViewConfig config = buildAppHtmlViewConfig(
        "app.views.medications.demo.View",
        new TmJQueryHtmlFramework(),
        theme,
        language
    );

    config.addStyleSheetDependency(MODULE_DIR_PATH + "/jquery/demo/app.views.medications.demo.View.css");

    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/demo/app.views.medications.demo.View.js");

    return config;
  }

  private AppHtmlViewConfig getStressTestTherapyViewConfig(final String theme, final String language)
  {
    final AppHtmlViewConfig config = buildAppHtmlViewConfig(
        "app.views.medications.stresstool.View",
        new TmJQueryHtmlFramework(),
        theme,
        language
    );

    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/stresstool/app.views.medications.stresstool.View.js");

    return config;
  }

  private AppHtmlViewConfig getPharmacistTaskLists(final String theme, final String language)
  {
    final TmJQueryAngularHtmlFramework framework = new TmJQueryAngularHtmlFramework();
    final AngularJSHtmlExternal angularJSExternal = framework.getAngularJSHtmlExternal();
    angularJSExternal.addPlugin(AngularJSHtmlExternalPlugin.COMMON_1_5_5);
    angularJSExternal.addPlugin(AngularJSHtmlExternalPlugin.DATA_TABLE_0_3_12);

    final AppHtmlViewConfig config = buildAppHtmlViewConfig(
        "app.views.medications.pharmacistsTasks.View",
        framework,
        theme,
        language
    );

    config.addExternal(angularJSExternal);
    config.addExternal(MomentJsHtmlExternal.VERSION_2_11_1);

    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/filter/pharmacistTasks.filter.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/filter/syringeProgressTaskType.filter.js");

    //common directives
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/resupplyForm/resupplyForm.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/openCloseFilter/openCloseFilter.dir.js");

    //supply grid
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/dueDate/dueDate.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/nextDose/nextDose.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/therapyType/therapyType.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/therapyModificationType/therapyModificationType.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/therapyAction/therapyAction.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/reminderNote/reminderNote.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/moreDropDownMenu/moreDropDownMenu.dir.js");

    //dispense grid
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/dispenseRequested/dispenseRequested.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/pharmacyReview/pharmacyReview.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/supplyStatus/supplyStatus.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/taskPrinted/taskPrinted.dir.js");

    //review grid
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/dateTimeUser/dateTimeUser.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/dateTime/dateTime.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/careProvider/careProvider.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/therapyTypeCell/therapyTypeCell.dir.js");

    // perfusion syringe list
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/syringesList/syringesList.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/syringesList/syringesListRow.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/syringesFilterMenu/syringesFilterMenu.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/syringeProgress/syringeProgress.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/syringeOverview/syringeOverview.dir.js");

    //resources
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/resources/supply.resource.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/resources/supply.service.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/resources/taskAction.service.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/controller/thinkGrid.ctrl.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/controller/thinkGrid.service.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/controller/syringesList.ctrl.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/service/pharmacistsTask.service.js");

    config.addStyleSheetDependency(MODULE_DIR_PATH + "/jquery/pharmacistsTasks/app.views.medications.pharmacistsTasks.View.css");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/app.views.medications.pharmacistsTask.module.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/pharmacistsTasks/app.views.medications.pharmacistsTasks.View.js");

    config.addProperty("ISPEK_SERVER_REST_URL", getHtmlProperties().getIspekServerRestHost());

    return config;
  }

  private AppHtmlViewConfig getNurseTaskLists(final String theme, final String language)
  {
    final AngularJSHtmlExternal angularJSHtmlExternal =
        AngularJSHtmlExternal.create(AngularJSHtmlExternal.VERSION_1_5_5.getVersion());
    angularJSHtmlExternal.addPlugin(AngularJSHtmlExternalPlugin.COMMON_1_5_5);
    angularJSHtmlExternal.addPlugin(AngularJSHtmlExternalPlugin.DATA_TABLE_0_3_12);

    final AppHtmlViewConfig config = buildAppHtmlViewConfig(
        "app.views.medications.nurseTasks.View", new TmJQueryAngularHtmlFramework(), theme, language);
    // externals //
    config.addExternals(MomentJsHtmlExternal.VERSION_2_11_1, angularJSHtmlExternal);

    config.addJavaScriptDependency(ANGULAR_NURSE_TASK_MODULE_DIR_PATH + "/filter/nurseTask.filter.js");

    config.addJavaScriptDependency(ANGULAR_NURSE_TASK_MODULE_DIR_PATH + "/controller/nurseListGrid.ctrl.js");

    config.addJavaScriptDependency(ANGULAR_NURSE_TASK_MODULE_DIR_PATH + "/directive/nurseTaskList/nurseTaskList.dir.js");
    config.addJavaScriptDependency(ANGULAR_NURSE_TASK_MODULE_DIR_PATH + "/directive/nurseTaskList/nurseTaskListRow.dir.js");
    config.addJavaScriptDependency(ANGULAR_NURSE_TASK_MODULE_DIR_PATH + "/directive/aplicationType/aplicationType.dir.js");
    config.addJavaScriptDependency(ANGULAR_NURSE_TASK_MODULE_DIR_PATH + "/directive/applicationPrecondition/applicationPrecondition.dir.js");
    config.addJavaScriptDependency(ANGULAR_NURSE_TASK_MODULE_DIR_PATH + "/directive/roomAndBed/roomAndBed.dir.js");

    config.addJavaScriptDependency(ANGULAR_NURSE_TASK_MODULE_DIR_PATH + "/resources/administrationTasksForCareProviders.resource.js");
    config.addJavaScriptDependency(ANGULAR_NURSE_TASK_MODULE_DIR_PATH + "/resources/administrationTasksForCareProviders.service.js");
    config.addJavaScriptDependency(ANGULAR_NURSE_TASK_MODULE_DIR_PATH + "/service/nurseTask.service.js");

    config.addStyleSheetDependency(MODULE_DIR_PATH + "/jquery/nurseTasks/app.views.medications.nurseTasks.View.css");
    config.addJavaScriptDependency(ANGULAR_NURSE_TASK_MODULE_DIR_PATH + "/app.views.medications.nurseTask.module.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/nurseTasks/app.views.medications.nurseTasks.View.js");

    config.addProperty("ISPEK_SERVER_REST_URL", getHtmlProperties().getIspekServerRestHost());

    return config;
  }

  @GET
  @GZIP
  @Produces({MediaType.TEXT_PLAIN})
  @Path("getTherapyViewPatientData")
  public Response getTherapyViewPatientData(
      @QueryParam("patientId") final Parameter.String patientId)
  {
    final TherapyViewPatientDto therapyViewPatientDto = service.getTherapyViewPatientData(patientId.getValue());
    final String json = JsonUtil.toJson(null, therapyViewPatientDto, null, Lists.newArrayList(INTERVAL_SERIALIZER));
    return buildJsonResponse(json);
  }

  @GET
  @Produces({MediaType.TEXT_PLAIN})
  @Path("getTimeOffset")
  public Response getTimeOffset(@HeaderParam("Request-Time") final Parameter.DateTime requestTime)
  {
    return buildJsonResponse(JsonUtil.toJson(calculateTimeOffset(requestTime)));
  }

  @GET
  @GZIP
  @Produces({MediaType.TEXT_PLAIN})
  @Path("therapyflowdata")
  public Response getTherapyFlowData(
      @QueryParam("patientId") final Parameter.String patientId,
      @QueryParam("centralCaseId") final Parameter.String centralCaseId,
      @QueryParam("patientHeight") final Parameter.Double patientHeight,
      @QueryParam("startDate") final Parameter.String startDate,
      @QueryParam("dayCount") final Parameter.Integer dayCount,
      @QueryParam("todayIndex") final Parameter.Integer todayIndex,
      @QueryParam("roundsInterval") final Parameter.String roundsInterval,
      @QueryParam("therapySortTypeEnum") final Parameter.String therapySortTypeString,
      @QueryParam("careProviderId") final Parameter.String careProviderId,
      @QueryParam("language") final Parameter.String language)
  {
    final DateTime startDateAtMidnight = new DateTime(Long.parseLong(startDate.getValue())).withTimeAtStartOfDay();
    final RoundsIntervalDto roundsIntervalDto = JsonUtil.fromJson(roundsInterval.getValue(), RoundsIntervalDto.class);
    final TherapySortTypeEnum therapySortTypeEnum = TherapySortTypeEnum.valueOf(therapySortTypeString.getValue());
    final TherapyFlowDto therapyFlowDto =
        service.getTherapyFlow(
            patientId.getValue(),
            centralCaseId.getValue() != null ? centralCaseId.getValue() : null,
            patientHeight.getValue(),
            startDateAtMidnight,
            dayCount.getValue(),
            todayIndex.getValue(),
            roundsIntervalDto,
            therapySortTypeEnum,
            careProviderId.getValue(),
            new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(therapyFlowDto));
  }

  @GET
  @Produces({MediaType.TEXT_PLAIN})
  @Path("reloadSingleTherapyAfterAction")
  public Response reloadSingleTherapyAfterAction(
      @QueryParam("patientId") final Parameter.String patientId,
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
  @Path("getPatientData")
  public Response getPatientData(
      @QueryParam("patientId") final Parameter.String patientId)
  {
    final PatientDataForMedicationsDto patientData = service.getPatientData(patientId.getValue());
    return buildJsonResponse(JsonUtil.toJson(patientData));
  }

  @GET
  @Produces({MediaType.TEXT_PLAIN})
  @Path("getPatientBaselineInfusionIntervals")
  public Response getPatientBaselineInfusionIntervals(
      @QueryParam("patientId") final Parameter.String patientId)
  {
    final List<Interval> patientBaselineInfusionIntervals =
        service.getPatientBaselineInfusionIntervals(patientId.getValue());
    return buildJsonResponse(
        JsonUtil.toJson(null, patientBaselineInfusionIntervals, null, Lists.newArrayList(INTERVAL_SERIALIZER)));
  }

  @GET
  @GZIP
  @Produces({MediaType.TEXT_PLAIN})
  @Path("findmedications")
  public Response findMedications(
      @QueryParam("searchQuery") final String searchQuery,
      @QueryParam("careProviderId") final String careProviderId,
      @QueryParam("additionalFilters") final String additionalFilters)
  {
    final List<String> filterStrings = Arrays.asList(JsonUtil.fromJson(additionalFilters, String[].class));
    final EnumSet<MedicationFinderFilterEnum> filterEnumSet = EnumSet.noneOf(MedicationFinderFilterEnum.class);
    filterStrings.forEach(filterString -> filterEnumSet.add(MedicationFinderFilterEnum.valueOf(filterString)));

    return buildJsonResponse(JsonUtil.toJson(service.findMedications(searchQuery, careProviderId, filterEnumSet)));
  }

  @GET
  @Produces({MediaType.TEXT_PLAIN})
  @Path("medicationdata")
  public Response getMedicationData(
      @QueryParam("medicationId") final long medicationId,
      @QueryParam("careProviderId") final String careProviderId)
  {
    return buildJsonResponse(JsonUtil.toJson(service.getMedicationData(medicationId, careProviderId)));
  }

  @GET
  @Produces({MediaType.TEXT_PLAIN})
  @Path("getRemainingInfusionBagQuantity")
  public Response getRemainingInfusionBagQuantity(
      @QueryParam("patientId") final String patientId,
      @QueryParam("when") final String when,
      @QueryParam("therapyId") final String therapyId)
  {
    final DateTime time = JsonUtil.fromJson(when, DateTime.class);
    return buildJsonResponse(JsonUtil.toJson(service.getRemainingInfusionBagQuantity(time, patientId, therapyId)));
  }

  @GET
  @Produces({MediaType.TEXT_PLAIN})
  @Path("medicationDataForMultipleIds")
  public Response getMedicationDataForMultipleIds(
      @QueryParam("medicationIds") final String medicationIds)
  {
    final Long[] medicationIdsArray = JsonUtil.fromJson(medicationIds, Long[].class);
    final Set<Long> medicationIdsList = new HashSet<>(Arrays.asList(medicationIdsArray));
    return buildJsonResponse(JsonUtil.toJson(service.getMedicationDataMap(medicationIdsList).values()));
  }

  @GET
  @Produces({MediaType.TEXT_PLAIN})
  @Path("getMedicationRoutes")
  public Response getMedicationRoutes(
      @QueryParam("medicationId") final long medicationId)
  {
    return buildJsonResponse(JsonUtil.toJson(service.getMedicationRoutes(medicationId)));
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
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("centralCaseId") final Parameter.String centralCaseId,
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
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("centralCaseId") final Parameter.String centralCaseId,
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
      @FormParam("patientId") final String patientId,
      @FormParam("patientAgeInDays") final Parameter.Long patientAgeInDays,
      @FormParam("patientWeightInKg") final Parameter.Double patientWeightInKg,
      @FormParam("patientAllergies") final Parameter.String patientAllergies,
      @FormParam("patientDiseases") final Parameter.String patientDiseases,
      @FormParam("bsaInM2") final Parameter.Double bsaInM2,
      @FormParam("isFemale") final Parameter.Boolean isFemale,
      @FormParam("patientMedications") final Parameter.String patientMedications,
      @FormParam("severityFilterValues") final Parameter.String severityFilterValues)
  {
    final MedicationForWarningsSearchDto[] medicationsArray = JsonUtil.fromJson(
        patientMedications.getValue(),
        MedicationForWarningsSearchDto[].class,
        Lists.newArrayList(INTERVAL_DESERIALIZER));
    final NamedExternalDto[] patientAllergiesArray =
        JsonUtil.fromJson(patientAllergies.getValue(), NamedExternalDto[].class);
    final ExternalCatalogDto[] patientDiseasesArray = JsonUtil.fromJson(
        patientDiseases.getValue(),
        ExternalCatalogDto[].class);
    final List<MedicationForWarningsSearchDto> medications = Arrays.asList(medicationsArray);
    final List<NamedExternalDto> patientAllergiesList = Arrays.asList(patientAllergiesArray);
    final List<ExternalCatalogDto> patientDiseasesList = Arrays.asList(patientDiseasesArray);

    final String[] severityFilterValuesArray = JsonUtil.fromJson(severityFilterValues.getValue(), String[].class);
    final Set<WarningSeverity> severityFilterValuesSet = fillSeverityFilterValuesSet(severityFilterValuesArray);

    final MedicationWarningsDto warningsDto =
        service.findMedicationWarnings(
            patientId,
            patientAgeInDays.getValue(),
            patientWeightInKg.getValue() > 0.0 ? patientWeightInKg.getValue() : null,
            bsaInM2.getValue() > 0.0 ? bsaInM2.getValue() : null,
            isFemale.getValue(),
            patientDiseasesList,
            patientAllergiesList,
            medications,
            severityFilterValuesSet
        );

    return buildJsonResponse(JsonUtil.toJson(warningsDto));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getAdditionalWarnings")
  public Response getAdditionalWarnings(
      @QueryParam("patientId") final String patientId,
      @QueryParam("additionalWarningsTypes") final String additionalWarningsTypes,
      @QueryParam("patientData") final String patientData,
      @QueryParam("language") final Parameter.String language)
  {
    final PatientDataForMedicationsDto patientDataForMedications = JsonUtil.fromJson(
        patientData,
        PatientDataForMedicationsDto.class,
        Lists.newArrayList(INTERVAL_DESERIALIZER));

    final List<AdditionalWarningsType> warningsTypes = Arrays.asList(JsonUtil.fromJson(
        additionalWarningsTypes,
        AdditionalWarningsType[].class));

    return buildJsonResponse(JsonUtil.toJson(
        service.getAdditionalWarnings(
            patientId,
            warningsTypes,
            patientDataForMedications,
            new Locale(language.getValue()))));
  }

  @POST
  @Produces({MediaType.TEXT_PLAIN})
  @Path("handleAdditionalWarningsAction")
  public Response handleAdditionalWarningsAction(@FormParam("additionalWarningsActionDto") final String additionalWarningsActionDto)
  {
    service.handleAdditionalWarningsAction(JsonUtil.fromJson(
        additionalWarningsActionDto,
        AdditionalWarningsActionDto.class,
        TherapyJsonDeserializer.INSTANCE.getTypeAdapters()));

    return buildJsonResponse(JsonUtil.toJson(""));
  }

  private Set<WarningSeverity> fillSeverityFilterValuesSet(final String[] severityFilterValuesArray)
  {
    final Set<WarningSeverity> severitySet = new HashSet<>();

    for (final String severityStringValue : severityFilterValuesArray)
    {
      severitySet.add(WarningSeverity.valueOf(severityStringValue));
    }
    return severitySet;
  }

  @GET
  @Produces({MediaType.TEXT_PLAIN})
  @Path("getTherapiesForWarnings")
  public Response getTherapiesForWarnings(
      @QueryParam("patientId") final Parameter.String patientId)
  {
    final List<MedicationForWarningsSearchDto> therapies = service.getTherapiesForWarningsSearch(patientId.getValue());
    return buildJsonResponse(JsonUtil.toJson(null, therapies, null, Lists.newArrayList(INTERVAL_SERIALIZER)));
  }

  @GET
  @Produces({MediaType.TEXT_PLAIN})
  @Path("getMedicationDocument")
  public Response getMedicationDocument(@QueryParam("reference") final Parameter.String reference)
  {
    try
    {
      //final String reference = "9b633e02-1810-4775-a67f-c0f8d5f737bb";
      final byte[] document = service.getMedicationDocument(reference.getValue());
      return buildPdfResponse(document);
    }
    catch (Throwable th)
    {
      return null;
    }
  }

  @GET
  @Produces({MediaType.TEXT_PLAIN})
  @Path("getTherapyPdfReport")
  public Response getTherapyPdfReport(
      @QueryParam("patientId") final Parameter.String patientIdJson,
      @QueryParam("language") final Parameter.String language)
  {
    try
    {
      final byte[] document = service.getTherapyReport(patientIdJson.getValue(), new Locale(language.getValue()));
      return buildPdfResponse(document);
    }
    catch (Throwable th)
    {
      return null;
    }
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getTherapyAuditTrail")
  public Response getTherapyAuditTrail(
      @QueryParam("patientId") final String patientId,
      @QueryParam("patientHeight") final Double patientHeight,
      @QueryParam("compositionId") final String compositionId,
      @QueryParam("ehrOrderName") final String ehrOrderName,
      @QueryParam("language") final String language)
  {
    final TherapyAuditTrailDto therapyAuditTrail =
        service.getTherapyAuditTrail(
            patientId,
            compositionId,
            ehrOrderName,
            patientHeight,
            new Locale(language));

    return buildJsonResponse(JsonUtil.toJson(therapyAuditTrail));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getTherapyFormattedDisplay")
  public Response getTherapyFormattedDisplay(
      @QueryParam("patientId") final Parameter.String patientId,
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

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("fillTherapyDisplayValues")
  public Response fillTherapyDisplayValues(
      @FormParam("therapy") final Parameter.String therapy,
      @FormParam("language") final Parameter.String language)
  {
    final TherapyDto therapyDto =
        JsonUtil.fromJson(
            therapy.getValue(),
            TherapyDto.class,
            TherapyJsonDeserializer.INSTANCE.getTypeAdapters());

    final TherapyDto therapyWithDisplayValues =
        service.fillTherapyDisplayValues(therapyDto, new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(therapyWithDisplayValues));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("fillPharmacistReviewTherapyOnEdit")
  public Response fillPharmacistReviewTherapyOnEdit(
      @FormParam("originalTherapy") final Parameter.String originalTherapy,
      @FormParam("changedTherapy") final Parameter.String changedTherapy,
      @FormParam("language") final Parameter.String language)
  {
    final TherapyDto originalTherapyDto =
        JsonUtil.fromJson(
            originalTherapy.getValue(),
            TherapyDto.class,
            TherapyJsonDeserializer.INSTANCE.getTypeAdapters());

    final TherapyDto changedTherapyDto =
        JsonUtil.fromJson(
            changedTherapy.getValue(),
            TherapyDto.class,
            TherapyJsonDeserializer.INSTANCE.getTypeAdapters());

    final PharmacistReviewTherapyDto pharmacistReviewTherapyDto =
        service.fillPharmacistReviewTherapyOnEdit(originalTherapyDto, changedTherapyDto, new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(pharmacistReviewTherapyDto));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("modifyTherapy")
  public Response modifyTherapy(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("therapy") final Parameter.String therapy,
      @FormParam("changeReason") final Parameter.String changeReason,
      @FormParam("centralCaseId") final Parameter.String centralCaseId,
      @FormParam("careProviderId") final Parameter.String careProviderId,
      @FormParam("prescriber") final Parameter.String prescriberJson,
      @FormParam("therapyAlreadyStarted") final Parameter.Boolean therapyAlreadyStarted,
      @FormParam("saveDateTime") final Parameter.String saveDateTimeJson,
      @FormParam("language") final Parameter.String language)
  {
    final TherapyDto therapyDto =
        JsonUtil.fromJson(
            therapy.getValue(),
            TherapyDto.class,
            TherapyJsonDeserializer.INSTANCE.getTypeAdapters());

    final TherapyChangeReasonDto changeReasonDto = changeReason != null ? JsonUtil.fromJson(
        changeReason.getValue(),
        TherapyChangeReasonDto.class) : null;

    final DateTime saveDateTime =
        saveDateTimeJson != null ? JsonUtil.fromJson(saveDateTimeJson.getValue(), DateTime.class) : null;
    final NamedExternalDto prescriber = JsonUtil.fromJson(prescriberJson.getValue(), NamedExternalDto.class);

    service.modifyTherapy(
        patientId.getValue(),
        therapyDto,
        changeReasonDto,
        centralCaseId.getValue(),
        careProviderId.getValue(),
        prescriber,
        therapyAlreadyStarted.getValue(),
        saveDateTime,
        null,
        new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("reviewTherapy")
  public Response reviewTherapy(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("compositionUid") final Parameter.String compositionUid,
      @FormParam("ehrOrderName") final Parameter.String ehrOrderName)
  {
    service.reviewTherapy(
        patientId.getValue(),
        compositionUid.getValue(),
        ehrOrderName.getValue());
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("suspendTherapy")
  public Response suspendTherapy(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("compositionUid") final Parameter.String compositionUid,
      @FormParam("ehrOrderName") final Parameter.String ehrOrderName,
      @FormParam("changeReason") final Parameter.String changeReason)
  {
    service.suspendTherapy(
        patientId.getValue(),
        compositionUid.getValue(),
        ehrOrderName.getValue(),
        changeReason != null ? JsonUtil.fromJson(changeReason.getValue(), TherapyChangeReasonDto.class) : null);
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("suspendAllTherapies")
  public Response suspendAllTherapies(
      @FormParam("patientId") final Parameter.String patientId)
  {
    service.suspendAllTherapies(patientId.getValue());
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("reissueTherapy")
  public Response reissueTherapy(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("compositionUid") final Parameter.String compositionUid,
      @FormParam("ehrOrderName") final Parameter.String ehrOrderName)
  {
    service.reissueTherapy(
        patientId.getValue(),
        compositionUid.getValue(),
        ehrOrderName.getValue());
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("abortTherapy")
  public Response abortTherapy(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("compositionUid") final Parameter.String compositionUid,
      @FormParam("ehrOrderName") final Parameter.String ehrOrderName,
      @FormParam("changeReason") final Parameter.String changeReason)
  {
    service.abortTherapy(
        patientId.getValue(),
        compositionUid.getValue(),
        ehrOrderName.getValue(),
        changeReason != null ? JsonUtil.fromJson(changeReason.getValue(), TherapyChangeReasonDto.class) : null);
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("saveMedicationsOrder")
  public Response saveMedicationsOrder(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("medicationOrders") final Parameter.String medicationOrdersJson,
      @FormParam("centralCaseId") final Parameter.String centralCaseId,
      @FormParam("hospitalizationStartMillis") final Parameter.Long hospitalizationStartMillis,
      @FormParam("careProviderId") final Parameter.String careProviderId,
      @FormParam("prescriber") final Parameter.String prescriberJson,
      @FormParam("lastLinkName") final Parameter.String lastLinkName,
      @FormParam("saveDateTime") final Parameter.String saveDateTimeJson,
      @FormParam("language") final Parameter.String language)
  {
    final SaveMedicationOrderDto[] medicationOrders =
        JsonUtil.fromJson(
            medicationOrdersJson.getValue(),
            SaveMedicationOrderDto[].class,
            TherapyJsonDeserializer.INSTANCE.getTypeAdapters());

    final DateTime saveDateTime =
        saveDateTimeJson != null ? JsonUtil.fromJson(saveDateTimeJson.getValue(), DateTime.class) : null;
    final DateTime hospitalizationStart =
        hospitalizationStartMillis != null && hospitalizationStartMillis.getValue() != null ?
        new DateTime(hospitalizationStartMillis.getValue()) : null;
    final NamedExternalDto prescriber = JsonUtil.fromJson(prescriberJson.getValue(), NamedExternalDto.class);

    service.saveNewMedicationOrder(
        patientId.getValue(),
        Arrays.asList(medicationOrders),
        centralCaseId.getValue(),
        hospitalizationStart,
        careProviderId.getValue(),
        prescriber,
        lastLinkName.getValue(),
        saveDateTime,
        new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("saveMedicationsOnAdmission")
  public Response saveMedicationsOnAdmission(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("therapies") final Parameter.String therapiesJson,
      @FormParam("centralCaseId") final Parameter.String centralCaseId,
      @FormParam("careProviderId") final Parameter.String careProviderId,
      @FormParam("prescriber") final Parameter.String prescriberJson,
      @FormParam("saveDateTime") final Parameter.String saveDateTimeJson,
      @FormParam("hospitalizationStart") final Parameter.String hospitalizationStartJson,
      @FormParam("language") final Parameter.String language)
  {
    final MedicationOnAdmissionDto[] therapies =
        JsonUtil.fromJson(
            therapiesJson.getValue(),
            MedicationOnAdmissionDto[].class,
            TherapyJsonDeserializer.INSTANCE.getTypeAdapters());

    final DateTime hospitalizationStart =
        hospitalizationStartJson != null ? JsonUtil.fromJson(hospitalizationStartJson.getValue(), DateTime.class) : null;

    final DateTime saveDateTime =
        saveDateTimeJson != null ? JsonUtil.fromJson(saveDateTimeJson.getValue(), DateTime.class) : null;
    final NamedExternalDto prescriber = JsonUtil.fromJson(prescriberJson.getValue(), NamedExternalDto.class);

    final List<String> savedCompositionIds = service.saveMedicationsOnAdmission(
        patientId.getValue(),
        Arrays.asList(therapies),
        centralCaseId.getValue(),
        careProviderId.getValue(),
        prescriber,
        saveDateTime,
        hospitalizationStart,
        new Locale(language.getValue()));

    return buildJsonResponse(JsonUtil.toJson(savedCompositionIds));
  }

  @GET
  @GZIP
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getMedicationsOnAdmission")
  public Response getMedicationsOnAdmission(
      @QueryParam("patientId") final Parameter.String patientId,
      @QueryParam("hospitalizationStart") final Parameter.String hospitalizationStartJson,
      @QueryParam("language") final Parameter.String language)
  {
    final DateTime hospitalizationStart =
        hospitalizationStartJson != null ? JsonUtil.fromJson(hospitalizationStartJson.getValue(), DateTime.class) : null;

    final List<MedicationOnAdmissionDto> medicationOnAdmissionDtoList = service.getMedicationsOnAdmission(
        patientId.getValue(),
        hospitalizationStart,
        new Locale(language.getValue()));

    return buildJsonResponse(JsonUtil.toJson(medicationOnAdmissionDtoList));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("saveMedicationsOnDischarge")
  public Response saveMedicationsOnDischarge(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("therapies") final Parameter.String therapiesJson,
      @FormParam("centralCaseId") final Parameter.String centralCaseId,
      @FormParam("careProviderId") final Parameter.String careProviderId,
      @FormParam("prescriber") final Parameter.String prescriberJson,
      @FormParam("saveDateTime") final Parameter.String saveDateTimeJson,
      @FormParam("hospitalizationStart") final Parameter.String hospitalizationStartJson,
      @FormParam("language") final Parameter.String language)
  {
    final MedicationOnDischargeDto[] therapies =
        JsonUtil.fromJson(
            therapiesJson.getValue(),
            MedicationOnDischargeDto[].class,
            TherapyJsonDeserializer.INSTANCE.getTypeAdapters());

    final NamedExternalDto prescriber = JsonUtil.fromJson(prescriberJson.getValue(), NamedExternalDto.class);
    final DateTime saveDateTime =
        saveDateTimeJson != null ? JsonUtil.fromJson(saveDateTimeJson.getValue(), DateTime.class) : null;
    final DateTime hospitalizationStart =
        hospitalizationStartJson != null ? JsonUtil.fromJson(hospitalizationStartJson.getValue(), DateTime.class) : null;

    final List<String> savedCompositionIds = service.saveMedicationsOnDischarge(
        patientId.getValue(),
        Arrays.asList(therapies),
        centralCaseId.getValue(),
        careProviderId.getValue(),
        prescriber,
        saveDateTime,
        hospitalizationStart,
        new Locale(language.getValue()));

    return buildJsonResponse(JsonUtil.toJson(savedCompositionIds));
  }

  @GET
  @GZIP
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getMedicationsOnDischarge")
  public Response getMedicationsOnDischarge(
      @QueryParam("patientId") final Parameter.String patientId,
      @QueryParam("hospitalizationStart") final Parameter.String hospitalizationStartJson,
      @QueryParam("saveDateTime") final Parameter.String saveDateTimeJson,
      @QueryParam("language") final Parameter.String language)
  {
    final DateTime hospitalizationStart =
        hospitalizationStartJson != null ? JsonUtil.fromJson(hospitalizationStartJson.getValue(), DateTime.class) : null;

    final DateTime saveDateTime =
        saveDateTimeJson != null ? JsonUtil.fromJson(saveDateTimeJson.getValue(), DateTime.class) : null;

    final List<MedicationOnDischargeDto> medicationsOnDischarge = service.getMedicationsOnDischarge(
        patientId.getValue(),
        hospitalizationStart,
        saveDateTime,
        new Locale(language.getValue()));

    return buildJsonResponse(JsonUtil.toJson(medicationsOnDischarge));
  }

  @GET
  @GZIP
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getReconciliationGroups")
  public Response getReconciliationGroups(
      @QueryParam("patientId") final Parameter.String patientId,
      @QueryParam("hospitalizationStart") final Parameter.String hospitalizationStartJson,
      @QueryParam("saveDateTime") final Parameter.String saveDateTimeJson,
      @QueryParam("language") final Parameter.String language)
  {
    final DateTime hospitalizationStart =
        hospitalizationStartJson != null ? JsonUtil.fromJson(hospitalizationStartJson.getValue(), DateTime.class) : null;

    final DateTime saveDateTime =
        saveDateTimeJson != null ? JsonUtil.fromJson(saveDateTimeJson.getValue(), DateTime.class) : null;

    final List<ReconciliationRowDto> summaryRows = service.getReconciliationSummaryGroups(
        patientId.getValue(),
        hospitalizationStart,
        saveDateTime,
        new Locale(language.getValue()));

    return buildJsonResponse(JsonUtil.toJson(summaryRows));
  }

  @GET
  @GZIP
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getTherapyChangeTypes")
  public Response getTherapyChangeTypes()
  {
    final Map<ActionReasonType, List<CodedNameDto>> allActionReasons = service.getActionReasons(null);
    return buildJsonResponse(JsonUtil.toJson(allActionReasons));
  }

  @GET
  @GZIP
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getMentalHealthTemplates")
  public Response getMentalHealthTemplates()
  {
    final List<MentalHealthTemplateDto> mentalHealthTemplates = service.getMentalHealthTemplates();
    return buildJsonResponse(JsonUtil.toJson(mentalHealthTemplates));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("saveReferenceWeight")
  public Response saveReferenceWeight(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("weight") final Parameter.Double weight)
  {
    service.savePatientReferenceWeight(patientId.getValue(), weight.getValue());
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("findSimilarMedications")
  public Response findSimilarMedications(
      @QueryParam("medicationId") final Long medicationId,
      @QueryParam("routeIds") final Parameter.Longs routeIds)
  {
    //noinspection Convert2MethodRef
    return buildJsonResponse(JsonUtil.toJson(service.findSimilarMedications(
        medicationId,
        Opt.resolve(() -> routeIds.getValue()).map(Lists::newArrayList).orElseGet(Lists::newArrayList))));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("findMedicationProducts")
  public Response findMedicationProducts(
      @QueryParam("medicationId") final Long medicationId,
      @QueryParam("routeIds") final Parameter.Longs routeIds)
  {
    //noinspection Convert2MethodRef
    return buildJsonResponse(JsonUtil.toJson(service.findMedicationProducts(
        medicationId,
        Opt.resolve(() -> routeIds.getValue()).map(Lists::newArrayList).orElseGet(Lists::newArrayList))));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getRoutes")
  public Response getRoutes()
  {
    return buildJsonResponse(JsonUtil.toJson(service.getRoutes()));
  }

  @GET
  @GZIP
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
      @FormParam("patientId") final Parameter.String patientId,
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
  @GZIP
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getLastTherapiesForPreviousHospitalization")
  public Response getLastTherapiesForPreviousHospitalization(
      @QueryParam("patientId") final Parameter.String patientId,
      @QueryParam("patientHeight") final Parameter.Double patientHeight,
      @QueryParam("language") final Parameter.String language)
  {
    final List<TherapyDto> therapyDtoList =
        service.getLastTherapiesForPreviousHospitalization(
            patientId.getValue(),
            patientHeight.getValue(),
            new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(therapyDtoList));
  }

  @GET
  @GZIP
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getTherapiesOnAdmissionGroups")
  public Response getTherapiesOnAdmissionGroups(
      @QueryParam("patientId") final Parameter.String patientId,
      @QueryParam("patientHeight") final Parameter.Double patientHeight,
      @QueryParam("hospitalizationStart") final Parameter.String hospitalizationStartJson,
      @QueryParam("language") final Parameter.String language)
  {
    final DateTime hospitalizationStart = JsonUtil.fromJson(hospitalizationStartJson.getValue(), DateTime.class);

    final List<MedicationOnAdmissionGroupDto> therapyOnAdmissionGroups =
        service.getTherapiesOnAdmissionGroups(
            patientId.getValue(),
            hospitalizationStart,
            new Locale(language.getValue()));

    return buildJsonResponse(JsonUtil.toJson(therapyOnAdmissionGroups));
  }

  @GET
  @GZIP
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getTherapiesOnDischargeGroups")
  public Response getTherapiesOnDischargeGroups(
      @QueryParam("patientId") final Parameter.String patientId,
      @QueryParam("patientHeight") final Parameter.Double patientHeight,
      @QueryParam("language") final Parameter.String language,
      @QueryParam("saveDateTime") final Parameter.String dateTimeJson,
      @QueryParam("lastHospitalizationStart") final Parameter.String lastHospitalizationStartJson,
      @QueryParam("hospitalizationActive") final Parameter.Boolean hospitalizationActive)
  {
    final DateTime when = dateTimeJson != null && dateTimeJson.getValue() != null ?
                          JsonUtil.fromJson(dateTimeJson.getValue(), DateTime.class) : null;

    final DateTime hospitalizationStart =
        lastHospitalizationStartJson != null
        ? JsonUtil.fromJson(lastHospitalizationStartJson.getValue(), DateTime.class)
        : null;

    final boolean hospitalizationActiveBoolean =
        hospitalizationActive != null && hospitalizationActive.getValue() != null ? hospitalizationActive.getValue() : false;
    final Double patientHeightDouble =
        patientHeight != null && patientHeight.getValue() != null ? patientHeight.getValue() : null;
    final List<MedicationOnDischargeGroupDto> therapyOnDischargeGroups =
        service.getTherapiesOnDischargeGroups(
            patientId.getValue(),
            patientHeightDouble,
            when,
            hospitalizationStart,
            hospitalizationActiveBoolean,
            new Locale(language.getValue()));

    return buildJsonResponse(JsonUtil.toJson(therapyOnDischargeGroups));
  }

  @GET
  @GZIP
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getCurrentHospitalizationMentalHealthTherapies")
  public Response getCurrentHospitalizationMentalHealthDrugs(
      @QueryParam("patientId") final Parameter.String patientId,
      @QueryParam("hospitalizationStart") final Parameter.String hospitalizationStartJson,
      @QueryParam("language") final Parameter.String language)
  {
    final DateTime hospitalizationStart =
        hospitalizationStartJson != null
        ? JsonUtil.fromJson(hospitalizationStartJson.getValue(), DateTime.class)
        : null;

    final List<MentalHealthTherapyDto> currentHospitalizationMentalHealthTherapies =
        service.getCurrentHospitalizationMentalHealthTherapies(
            patientId.getValue(),
            hospitalizationStart,
            new Locale(language.getValue()));

    return buildJsonResponse(JsonUtil.toJson(currentHospitalizationMentalHealthTherapies));
  }

  @GET
  @GZIP
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getLinkTherapyCandidates")
  public Response getLinkTherapyCandidates(
      @QueryParam("patientId") final String patientId,
      @QueryParam("referenceWeight") final Double referenceWeight,
      @QueryParam("patientHeight") final Double patientHeight,
      @QueryParam("language") final String language)
  {
    final List<TherapyDto> linkTherapyCandidates = service.getLinkTherapyCandidates(
        patientId,
        referenceWeight,
        patientHeight,
        new Locale(language));

    return buildJsonResponse(JsonUtil.toJson(linkTherapyCandidates));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("saveMentalHealthDocument")
  public Response saveMentalHealthDocument(
      @FormParam("mentalHealthDocument") final String mentalHealthDocument,
      @FormParam("careProvider") final String careProvider)
  {
    final MentalHealthDocumentDto mentalHealthDocumentDto = JsonUtil.fromJson(
        mentalHealthDocument,
        MentalHealthDocumentDto.class);

    final NamedExternalDto careProviderDto = JsonUtil.fromJson(
        careProvider,
        NamedExternalDto.class);

    service.saveMentalHealthReport(mentalHealthDocumentDto, careProviderDto);
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @GET
  @GZIP
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getTherapyTemplates")
  public Response getTherapyTemplates(
      @QueryParam("patientId") final Parameter.String patientId,
      @QueryParam("templateMode") final Parameter.String templateMode,
      @QueryParam("careProviderId") final Parameter.String careProviderId,
      @QueryParam("referenceWeight") final Parameter.Double referenceWeight,
      @QueryParam("patientHeight") final Parameter.Double patientHeight,
      @QueryParam("language") final Parameter.String language)
  {
    final TherapyTemplateModeEnum templateModeEnum = TherapyTemplateModeEnum.valueOf(templateMode.getValue());
    final TherapyTemplatesDto therapyTemplates = service.getTherapyTemplates(
        patientId.getValue(),
        templateModeEnum,
        careProviderId.getValue(),
        referenceWeight.getValue(),
        patientHeight.getValue(),
        new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(therapyTemplates));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("saveTherapyTemplate")
  public Response saveTherapyTemplate(
      @FormParam("template") final Parameter.String template,
      @FormParam("templateMode") final Parameter.String templateMode)
  {
    final TherapyTemplateModeEnum templateModeEnum = TherapyTemplateModeEnum.valueOf(templateMode.getValue());
    final TherapyTemplateDto templateDto =
        JsonUtil.fromJson(template.getValue(), TherapyTemplateDto.class, TherapyJsonDeserializer.INSTANCE.getTypeAdapters());
    final long newTemplateId = service.saveTherapyTemplate(templateDto, templateModeEnum);
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
  @GZIP
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getTherapyTimeline")
  public Response getTherapyTimeline(
      @QueryParam("patientId") final String patientId,
      @QueryParam("patientData") final String patientData,
      @QueryParam("timelineInterval") final String timelineInterval,
      @QueryParam("roundsInterval") final String roundsInterval,
      @QueryParam("therapySortTypeEnum") final String therapySortTypeString,
      @QueryParam("hidePastTherapies") final Parameter.Boolean hidePastTherapies,
      @QueryParam("hideFutureTherapies") final Parameter.Boolean hideFutureTherapies,
      @QueryParam("language") final String language)
  {
    final PatientDataForMedicationsDto patientDataForMedications = JsonUtil.fromJson(
        patientData,
        PatientDataForMedicationsDto.class,
        Lists.newArrayList(INTERVAL_DESERIALIZER));

    final Interval searchInterval = JsonUtil.fromJson(
        timelineInterval,
        Interval.class,
        Lists.newArrayList(INTERVAL_DESERIALIZER));

    final RoundsIntervalDto roundsIntervalDto = JsonUtil.fromJson(roundsInterval, RoundsIntervalDto.class);
    final TherapySortTypeEnum sortType = TherapySortTypeEnum.valueOf(therapySortTypeString);

    final TherapyTimelineDto timeline = service.getTherapyTimeline(
        patientId,
        searchInterval,
        sortType,
        hidePastTherapies.getValue(),
        hideFutureTherapies.getValue(),
        patientDataForMedications,
        roundsIntervalDto,
        new Locale(language));

    return buildJsonResponse(JsonUtil.toJson(timeline));
  }

  @GET
  @GZIP
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getPharmacistTimeline")
  public Response getPharmacistTimeline(
      @QueryParam("patientId") final String patientId,
      @QueryParam("patientData") final String patientData,
      @QueryParam("timelineInterval") final String timelineInterval,
      @QueryParam("roundsInterval") final String roundsInterval,
      @QueryParam("therapySortTypeEnum") final String therapySortTypeString,
      @QueryParam("hidePastTherapies") final Parameter.Boolean hidePastTherapies,
      @QueryParam("language") final String language)
  {
    final PatientDataForMedicationsDto patientDataForMedications = JsonUtil.fromJson(
        patientData,
        PatientDataForMedicationsDto.class,
        Lists.newArrayList(INTERVAL_DESERIALIZER));

    final RoundsIntervalDto roundsIntervalDto = JsonUtil.fromJson(roundsInterval, RoundsIntervalDto.class);
    final TherapySortTypeEnum therapySortTypeEnum = TherapySortTypeEnum.valueOf(therapySortTypeString);
    final Interval searchInterval = JsonUtil.fromJson(
        timelineInterval,
        Interval.class,
        Lists.newArrayList(INTERVAL_DESERIALIZER));

    final TherapyTimelineDto timeline = service.getPharmacistTimeline(
        patientId,
        searchInterval,
        therapySortTypeEnum,
        hidePastTherapies.getValue(),
        patientDataForMedications,
        roundsIntervalDto,
        new Locale(language));

    return buildJsonResponse(JsonUtil.toJson(timeline));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("confirmTherapyAdministration")
  public Response confirmTherapyAdministration(
      @FormParam("therapyCompositionUid") final Parameter.String therapyCompositionUid,
      @FormParam("ehrOrderName") final Parameter.String ehrOrderName,
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("editMode") final Parameter.Boolean editMode,
      @FormParam("administration") final Parameter.String administration,
      @FormParam("centralCaseId") final Parameter.String centralCaseId,
      @FormParam("careProviderId") final Parameter.String careProviderId,
      @FormParam("language") final Parameter.String language,
      @FormParam("requestSupply") final Parameter.Boolean requestSupply)
  {
    final AdministrationDto therapyAdministrationDto =
        JsonUtil.fromJson(
            administration.getValue(),
            AdministrationDto.class,
            Lists.newArrayList(TherapyJsonDeserializer.INSTANCE.getTypeAdapters()));

    service.confirmTherapyAdministration(
        therapyCompositionUid.getValue(),
        ehrOrderName.getValue(),
        patientId.getValue(),
        therapyAdministrationDto,
        (editMode == null || editMode.getValue() == null) ? false : editMode.getValue(),
        centralCaseId.getValue(),
        careProviderId.getValue(),
        requestSupply != null && requestSupply.getValue(),
        new Locale(language.getValue()));

    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("setDoctorConfirmationResult")
  public Response setDoctorConfirmationResult(
      @FormParam("taskId") final Parameter.String taskId,
      @FormParam("result") final Parameter.Boolean result)
  {
    service.setDoctorConfirmationResult(taskId.getValue(), result.getValue());
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("setAdministrationTitratedDose")
  public Response setAdministrationTitratedDose(
      @FormParam("patientId") final String patientId,
      @FormParam("latestTherapyId") final String latestTherapyId,
      @FormParam("administration") final String administrationJson,
      @FormParam("confirmAdministration") final boolean confirmAdministration,
      @FormParam("until") final String until, //optional
      @FormParam("centralCaseId") final String centralCaseId, //optional
      @FormParam("careProviderId") final String careProviderId, //optional
      @FormParam("language") final String language)
  {
    service.setAdministrationTitratedDose(
        patientId,
        latestTherapyId,
        JsonUtil.fromJson(administrationJson, StartAdministrationDto.class),
        confirmAdministration,
        centralCaseId,
        careProviderId,
        JsonUtil.fromJson(until, DateTime.class),
        new Locale(language));

    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("createAdministrationTask")
  public Response createAdministrationTask(
      @FormParam("therapyCompositionUid") final Parameter.String therapyCompositionUid,
      @FormParam("ehrOrderName") final Parameter.String ehrOrderName,
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("administration") final Parameter.String administration,
      @FormParam("requestSupply") final Parameter.Boolean requestSupply,
      @FormParam("language") final Parameter.String language)
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
    if (requestSupply != null && requestSupply.getValue())
    {
      service.handleNurseResupplyRequest(
          patientId.getValue(),
          therapyCompositionUid.getValue(),
          ehrOrderName.getValue(),
          new Locale(language.getValue()));
    }
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("rescheduleTasks")
  public Response rescheduleTasks(
      @FormParam("patientId") final String patientId,
      @FormParam("taskId") final String taskId,
      @FormParam("newTime") final String newTime,
      @FormParam("therapyId") final String therapyId)
  {
    service.rescheduleTasks(patientId, taskId, JsonUtil.fromJson(newTime, DateTime.class), therapyId);
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("rescheduleTask")
  public Response rescheduleTask(
      @FormParam("patientId") final String patientId,
      @FormParam("taskId") final String taskId,
      @FormParam("newTime") final String newTime,
      @FormParam("therapyId") final String therapyId)
  {
    service.rescheduleTask(patientId, taskId, JsonUtil.fromJson(newTime, DateTime.class), therapyId);
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("deleteTask")
  public Response deleteTask(
      @FormParam("patientId") final String patientId,
      @FormParam("taskId") final String taskId,
      @FormParam("groupUUId") final String groupUUId,
      @FormParam("therapyId") final String therapyId,
      @FormParam("comment") final String comment)
  {
    service.deleteTask(patientId, taskId, groupUUId, therapyId, comment);
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("deleteAdministration")
  public Response deleteAdministration(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("administration") final Parameter.String administration,
      @FormParam("therapyDoseType") final Parameter.String therapyDoseType,
      @FormParam("therapyId") final Parameter.String therapyId,
      @FormParam("comment") final Parameter.String comment)
  {
    final AdministrationDto administrationDto =
        JsonUtil.fromJson(
            administration.getValue(),
            AdministrationDto.class,
            Lists.newArrayList(TherapyJsonDeserializer.INSTANCE.getTypeAdapters()));

    service.deleteAdministration(
        patientId.getValue(),
        administrationDto,
        TherapyDoseTypeEnum.valueOf(therapyDoseType.getValue()),
        therapyId.getValue(),
        comment.getValue());

    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @GET
  @GZIP
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getCareProfessionals")
  public Response getCareProfessionals()
  {
    final List<NamedExternalDto> careProfessionalList = service.getCareProfessionals();
    return buildJsonResponse(JsonUtil.toJson(careProfessionalList));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("assertPasswordForUsername")
  public Response assertPasswordForUsername(
      @QueryParam("username") final Parameter.String username,
      @QueryParam("password") final Parameter.String password
  )
  {
    final boolean assertPasswordForUsername = service.assertPasswordForUsername(username.getValue(), password.getValue());
    return buildJsonResponse(JsonUtil.toJson(assertPasswordForUsername));
  }

  @GET
  @GZIP
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getPharmacistReviews")
  public Response getPharmacistReviews(
      @QueryParam("patientId") final Parameter.String patientId,
      @QueryParam("fromDate") final Parameter.String fromDate,
      @QueryParam("language") final Parameter.String language)
  {
    final PharmacistReviewsDto reviews =
        service.getPharmacistReviews(
            patientId.getValue(),
            JsonUtil.fromJson(fromDate.getValue(), DateTime.class),
            new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(reviews));
  }

  @GET
  @GZIP
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getPharmacistReviewsForTherapy")
  public Response getPharmacistReviewsForTherapy(
      @QueryParam("patientId") final Parameter.String patientId,
      @QueryParam("therapyCompositionUid") final Parameter.String therapyCompositionUid,
      @QueryParam("language") final Parameter.String language)
  {
    final List<PharmacistReviewDto> reviews =
        service.getPharmacistReviewsForTherapy(
            patientId.getValue(),
            therapyCompositionUid.getValue(),
            new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(reviews));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("savePharmacistReview")
  public Response savePharmacistReview(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("pharmacistReview") final Parameter.String pharmacistReviewJson,
      @FormParam("authorize") final Parameter.Boolean authorize,
      @FormParam("language") final Parameter.String language)
  {
    final List<JsonUtil.TypeAdapterPair> typeAdapters = new ArrayList<>(TherapyJsonDeserializer.INSTANCE.getTypeAdapters());
    typeAdapters.add(THERAPY_CHANGE_DESERIALIZER);

    final PharmacistReviewDto pharmacistReview =
        JsonUtil.fromJson(
            pharmacistReviewJson.getValue(),
            PharmacistReviewDto.class,
            typeAdapters);

    final String compositionUid =
        service.savePharmacistReview(
            patientId.getValue(),
            pharmacistReview,
            authorize.getValue(),
            new Locale(language.getValue()));

    return buildJsonResponse(JsonUtil.toJson(compositionUid));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("reviewPharmacistReview")
  public Response reviewPharmacistReview(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("pharmacistReviewUid") final Parameter.String pharmacistReviewUid,
      @FormParam("reviewAction") final Parameter.String reviewActionString,
      @FormParam("modifiedTherapy") final Parameter.String modifiedTherapy,
      @FormParam("centralCaseId") final Parameter.String centralCaseId,
      @FormParam("careProviderId") final Parameter.String careProviderId,
      @FormParam("prescriber") final Parameter.String prescriberJson,
      @FormParam("reviewIdsToDeny") final Parameter.String reviewsToDenyJson,
      @FormParam("language") final Parameter.String language)
  {
    final TherapyDto modifiedTherapyDto =
        modifiedTherapy != null ?
        JsonUtil.fromJson(
            modifiedTherapy.getValue(),
            TherapyDto.class,
            TherapyJsonDeserializer.INSTANCE.getTypeAdapters()) : null;

    final ReviewPharmacistReviewAction reviewAction = ReviewPharmacistReviewAction.valueOf(reviewActionString.getValue());

    final NamedExternalDto prescriber =
        prescriberJson != null ? JsonUtil.fromJson(prescriberJson.getValue(), NamedExternalDto.class) : null;

    final List<String> deniedReviews =
        reviewsToDenyJson != null && reviewsToDenyJson.getValue() != null ?
        Arrays.asList(JsonUtil.fromJson(reviewsToDenyJson.getValue(), String[].class)) : null;

    service.reviewPharmacistReview(
        patientId.getValue(),
        pharmacistReviewUid.getValue(),
        reviewAction,
        modifiedTherapyDto,
        deniedReviews,
        centralCaseId != null ? centralCaseId.getValue() : null,
        careProviderId != null ? careProviderId.getValue() : null,
        prescriber,
        new Locale(language.getValue()));

    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Path("authorizePharmacistReviews")
  public Response authorizePharmacistReviews(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("pharmacistReviewUids") final Parameter.String pharmacistReviewUidsJson,
      @FormParam("language") final Parameter.String language)
  {
    final List<String> pharmacistReviewUids =
        Arrays.asList(JsonUtil.fromJson(pharmacistReviewUidsJson.getValue(), String[].class));
    service.authorizePharmacistReviews(
        patientId.getValue(),
        pharmacistReviewUids,
        new Locale(language.getValue()));
    return buildNoContentResponse();
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("deletePharmacistReview")
  public Response deletePharmacistReview(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("pharmacistReviewUid") final Parameter.String pharmacistReviewUid)
  {
    service.deletePharmacistReview(patientId.getValue(), pharmacistReviewUid.getValue());
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getSupplyDataForPharmacistReview")
  public Response getSupplyDataForPharmacistReview(
      @QueryParam("patientId") final Parameter.String patientId,
      @QueryParam("therapyCompositionUid") final Parameter.String therapyCompositionUid,
      @QueryParam("language") final Parameter.String language)
  {
    final SupplyDataForPharmacistReviewDto supplyData =
        service.getSupplyDataForPharmacistReview(patientId.getValue(), therapyCompositionUid.getValue());
    return buildJsonResponse(JsonUtil.toJson(supplyData));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getPharmacistReviewTasks")
  public Response getPharmacistReviewTasks(
      @QueryParam("careProviderIds") final Parameter.String careProviderIds,
      @QueryParam("patientIds") final Parameter.String patientIds,
      @QueryParam("language") final Parameter.String language)
  {
    final Opt<Collection<String>> careProviderIdsOpt = resolveStringArrayParameterToSetOpt(careProviderIds);
    final Opt<Collection<String>> patientIdsOpt = resolveStringArrayParameterToSetOpt(patientIds);

    return buildJsonResponse(JsonUtil.toJson(service.getPharmacistReviewTasks(careProviderIdsOpt, patientIdsOpt)));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getPharmacistResupplyTasks")
  public Response getPharmacistResupplyTasks(
      @QueryParam("careProviderIds") final Parameter.String careProviderIds,
      @QueryParam("patientIds") final Parameter.String patientIds,
      @QueryParam("includeUnverifiedDispenseTasks") final Parameter.Boolean includeUnverifiedDispenseTasks,
      @QueryParam("closedTasksOnly") final Parameter.Boolean closedTasksOnly,
      @QueryParam("taskTypes") final Parameter.String taskTypes,
      @QueryParam("language") final Parameter.String language)
  {
    final Opt<Collection<String>> careProviderIdsOpt = resolveStringArrayParameterToSetOpt(careProviderIds);
    final Opt<Collection<String>> patientIdsOpt = resolveStringArrayParameterToSetOpt(patientIds);
    final Set<TaskTypeEnum> taskTypesSet = getTaskTypes(JsonUtil.fromJson(taskTypes.getValue(), String[].class));

    final boolean closedTasksOnlyValue = closedTasksOnly.getValue();     // shows only closed tasks
    final boolean includeUnverifiedDispenseTasksValue = includeUnverifiedDispenseTasks.getValue(); // shows verified and unverified tasks

    return buildJsonResponse(
        JsonUtil.toJson(
            service.findSupplyTasks(
                careProviderIdsOpt,
                patientIdsOpt,
                taskTypesSet,
                closedTasksOnlyValue,
                includeUnverifiedDispenseTasksValue,
                new Locale(language.getValue()))));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getPharmacistSupplySimpleTask")
  public Response getPharmacistSupplySimpleTask(
      @QueryParam("taskId") final Parameter.String taskId,
      @QueryParam("language") final Parameter.String language)
  {
    return buildJsonResponse(
        JsonUtil.toJson(service.getSupplySimpleTask(taskId.getValue(), new Locale(language.getValue()))));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getPharmacistDispenseMedicationTasks")
  public Response getPharmacistDispenseMedicationTasks(
      @QueryParam("careProviderIds") final Parameter.String careProviderIds,
      @QueryParam("patientIds") final Parameter.String patientIds,
      @QueryParam("closedTasksOnly") final Parameter.Boolean closedTasksOnly,
      @QueryParam("includeUnverifiedDispenseTasks") final Parameter.Boolean includeUnverifiedDispenseTasks,
      @QueryParam("language") final Parameter.String language)
  {
    final Opt<Collection<String>> careProviderIdsOpt = resolveStringArrayParameterToSetOpt(careProviderIds);
    final Opt<Collection<String>> patientIdsOpt = resolveStringArrayParameterToSetOpt(patientIds);
    final Set<TaskTypeEnum> taskTypes = EnumSet.of(TaskTypeEnum.DISPENSE_MEDICATION);

    final boolean closedTasksOnlyValue = closedTasksOnly.getValue();     // shows only closed tasks
    final boolean includeUnverifiedDispenseTasksValue = includeUnverifiedDispenseTasks.getValue(); // shows verified and unverified tasks

    return buildJsonResponse(
        JsonUtil.toJson(
            service.findSupplyTasks(
                careProviderIdsOpt,
                patientIdsOpt,
                taskTypes,
                closedTasksOnlyValue,
                includeUnverifiedDispenseTasksValue,
                new Locale(language.getValue()))));
  }

  @POST
  @Path("confirmSupplyReminderTask")
  public Response confirmSupplyReminderTask(
      @FormParam("taskId") final Parameter.String taskId,
      @FormParam("compositionUid") final Parameter.String compositionUid,
      @FormParam("supplyType") final Parameter.String supplyType,
      @FormParam("supplyInDays") final Parameter.Integer supplyInDays,
      @FormParam("comment") final Parameter.String comment)
  {
    final MedicationSupplyTypeEnum supplyTypeEnum = MedicationSupplyTypeEnum.valueOf(supplyType.getValue());
    service.confirmSupplyReminderTask(
        taskId.getValue(),
        compositionUid.getValue(),
        supplyTypeEnum,
        supplyInDays.getValue(),
        comment.getValue());
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Path("editSupplyReminderTask")
  public Response editSupplyReminderTask(
      @FormParam("taskId") final Parameter.String taskId,
      @FormParam("supplyType") final Parameter.String supplyType,
      @FormParam("supplyInDays") final Parameter.Integer supplyInDays,
      @FormParam("comment") final Parameter.String comment)
  {
    final MedicationSupplyTypeEnum supplyTypeEnum = MedicationSupplyTypeEnum.valueOf(supplyType.getValue());
    service.editSupplyReminderTask(
        taskId.getValue(),
        supplyTypeEnum,
        supplyInDays.getValue(),
        comment.getValue());
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Path("confirmSupplyReviewTask")
  public Response confirmSupplyReviewTask(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("taskId") final Parameter.String taskId,
      @FormParam("compositionUid") final Parameter.String compositionUid,
      @FormParam("createSupplyReminder") final Parameter.Boolean createSupplyReminder,
      @FormParam("supplyType") final Parameter.String supplyType,
      @FormParam("supplyInDays") final Parameter.Integer supplyInDays,
      @FormParam("comment") final Parameter.String comment)
  {
    final MedicationSupplyTypeEnum supplyTypeEnum =
        supplyType.getValue() != null ? MedicationSupplyTypeEnum.valueOf(supplyType.getValue()) : null;
    service.confirmSupplyReviewTask(
        patientId.getValue(),
        taskId.getValue(),
        compositionUid.getValue(),
        createSupplyReminder.getValue(),
        supplyTypeEnum,
        supplyInDays.getValue(),
        comment.getValue());

    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Path("printPharmacistDispenseTask")
  public Response printPharmacistDispenseTask(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("taskId") final Parameter.String taskId,
      @FormParam("compositionUid") final Parameter.String compositionUid,
      @FormParam("requesterRole") final Parameter.String requesterRoleName,
      @FormParam("supplyRequestStatus") final Parameter.String supplyRequestStatus)
  {
    final TherapyAssigneeEnum requesterRole = TherapyAssigneeEnum.valueOf(requesterRoleName.getValue());
    final SupplyRequestStatus supplyRequestStatusEnum = SupplyRequestStatus.valueOf(supplyRequestStatus.getValue());
    service.printPharmacistDispenseTask(
        patientId.getValue(),
        taskId.getValue(),
        compositionUid.getValue(),
        requesterRole,
        supplyRequestStatusEnum);
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Path("confirmPharmacistDispenseTask")
  public Response confirmPharmacistDispenseTask(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("taskId") final Parameter.String taskId,
      @FormParam("compositionUid") final Parameter.String compositionUid,
      @FormParam("requesterRole") final Parameter.String requesterRoleName,
      @FormParam("supplyRequestStatus") final Parameter.String supplyRequestStatus)
  {
    final TherapyAssigneeEnum requesterRole = TherapyAssigneeEnum.valueOf(requesterRoleName.getValue());
    final SupplyRequestStatus supplyRequestStatusEnum = SupplyRequestStatus.valueOf(supplyRequestStatus.getValue());
    service.confirmPharmacistDispenseTask(
        patientId.getValue(),
        taskId.getValue(),
        compositionUid.getValue(),
        requesterRole,
        supplyRequestStatusEnum);
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Path("dismissPharmacistSupplyTask")
  public Response dismissPharmacistSupplyTask(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("taskId") final Parameter.String taskId)
  {
    // used to dismiss pharmacistReminder tasks - tasks don't get deleted, instead a dismissed flag is set
    service.dismissSupplyTask(patientId.getValue(), Collections.singletonList(taskId.getValue()));
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Path("dismissNurseSupplyTask")
  public Response dismissNurseSupplyTask(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("taskId") final Parameter.String taskId,
      @FormParam("language") final Parameter.String language)
  {
    //used to dismiss nurse supply task - dismisses both related (review and dispense) nurse tasks, tasks are deleted
    service.deleteNurseSupplyTask(patientId.getValue(), taskId.getValue(), new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @GET
  @Path("finishedPerfusionSyringeRequestsExistInLastHours")
  public Response finishedPerfusionSyringeRequestsExistInLastHours(
      @QueryParam("patientId") final Parameter.String patientId,
      @QueryParam("originalTherapyId") final Parameter.String originalTherapyId,
      @QueryParam("hours") final Parameter.Integer hours)
  {
    final boolean hasFinishedRequests =
        service.finishedPerfusionSyringeRequestsExistInLastHours(
            patientId.getValue(),
            originalTherapyId.getValue(),
            hours.getValue());
    return buildJsonResponse(JsonUtil.toJson(hasFinishedRequests));
  }

  @POST
  @Path("sendNurseResupplyRequest")
  public Response sendNurseResupplyRequest(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("therapy") final Parameter.String therapyJson,
      @FormParam("language") final Parameter.String language)
  {
    final TherapyDto therapyDto =
        JsonUtil.fromJson(
            therapyJson.getValue(),
            TherapyDto.class,
            TherapyJsonDeserializer.INSTANCE.getTypeAdapters());

    service.handleNurseResupplyRequest(
        patientId.getValue(),
        therapyDto.getCompositionUid(),
        therapyDto.getEhrOrderName(),
        new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getCurrentUserCareProviders")
  public Response getCurrentUserCareProviders()
  {
    return buildJsonResponse(JsonUtil.toJson(service.getCurrentUserCareProviders()));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getProblemDescriptionNamedIdentities")
  public Response getProblemDescriptionNamedIdentities(
      @QueryParam("language") final Parameter.String language)
  {
    return buildJsonResponse(JsonUtil.toJson(service.getProblemDescriptionNamedIdentities(new Locale(language.getValue()))));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("calculateTherapyAdministrationTimes")
  public Response calculateTherapyAdministrationTimes(
      @FormParam("therapy") final Parameter.String therapyJson,
      @FormParam("careProviderId") final Parameter.String careProviderId)
  {
    final TherapyDto therapyDto =
        JsonUtil.fromJson(
            therapyJson.getValue(),
            TherapyDto.class,
            TherapyJsonDeserializer.INSTANCE.getTypeAdapters());
    final List<AdministrationDto> administrations =
        service.calculateTherapyAdministrationTimes(therapyDto);
    return buildJsonResponse(JsonUtil.toJson(administrations));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("calculateNextTherapyAdministrationTime")
  public Response calculateNextTherapyAdministrationTime(
      @FormParam("therapy") final Parameter.String therapyJson,
      @FormParam("newPrescription") final Parameter.Boolean newPrescription,
      @FormParam("careProviderId") final Parameter.String careProviderId)
  {
    final TherapyDto therapyDto =
        JsonUtil.fromJson(
            therapyJson.getValue(),
            TherapyDto.class,
            TherapyJsonDeserializer.INSTANCE.getTypeAdapters());
    final DateTime nextAdministration =
        service.calculateNextTherapyAdministrationTime(therapyDto, newPrescription.getValue());
    return buildJsonResponse(nextAdministration != null ? JsonUtil.toJson(nextAdministration) : null);
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("findPreviousTaskForTherapy")
  public Response findPreviousTaskForTherapy(
      @QueryParam("patientId") final Parameter.String patientId,
      @QueryParam("compositionUid") final Parameter.String compositionUid,
      @QueryParam("ehrOrderName") final Parameter.String ehrOrderName)
  {
    final DateTime previousAdministration =
        service.findPreviousTaskForTherapy(patientId.getValue(), compositionUid.getValue(), ehrOrderName.getValue());
    return buildJsonResponse(JsonUtil.toJson(previousAdministration));
  }

  @GET
  @GZIP
  @Produces({MediaType.TEXT_HTML})
  @Path("getAdministrationTasks")
  public Response getAdministrationTasks(
      @QueryParam("careProviderIds") final Parameter.String careProviderIds,
      @QueryParam("patientIds") final Parameter.String patientIds,
      @QueryParam("language") final Parameter.String language)
  {
    final Opt<Collection<String>> careProviderIdsOpt = resolveStringArrayParameterToSetOpt(careProviderIds);
    final Opt<Collection<String>> patientIdsOpt = resolveStringArrayParameterToSetOpt(patientIds);

    final List<AdministrationPatientTaskDto> tasks =
        service.getAdministrationTasks(
            careProviderIdsOpt,
            patientIdsOpt,
            new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(tasks));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getTherapy")
  public Response getTherapy(
      @QueryParam("patientId") final Parameter.String patientId,
      @QueryParam("therapyId") final Parameter.String therapyId,
      @QueryParam("language") final Parameter.String language)
  {
    final TherapyDto therapyDto =
        service.getTherapyDto(patientId.getValue(), therapyId.getValue(), new Locale(language.getValue()));

    return buildJsonResponse(JsonUtil.toJson(therapyDto));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("applyMedicationRule")
  public Response applyMedicationRule(
      @FormParam("ruleParameters") final Parameter.String ruleParametersJson,
      @FormParam("language") final Parameter.String language)
  {
    final List<JsonUtil.TypeAdapterPair> adapters = new ArrayList<>();
    adapters.add(RULE_PARAMETERS_DESERIALIZER);
    adapters.addAll(TherapyJsonDeserializer.INSTANCE.getTypeAdapters());
    adapters.add(INTERVAL_DESERIALIZER);

    final RuleParameters ruleParameters = JsonUtil.fromJson(ruleParametersJson.getValue(), RuleParameters.class, adapters);

    final Locale locale = new Locale(language.getValue());
    return buildJsonResponse(JsonUtil.toJson(service.applyMedicationRule(ruleParameters, locale)));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getMedicationSupplyCandidates")
  public Response getMedicationSupplyCandidates(
      @QueryParam("medicationId") final Parameter.Long medicationId,
      @QueryParam("routeId") final Parameter.Long routeId)
  {
    final List<MedicationSupplyCandidateDto> supplyList =
        service.getMedicationSupplyCandidates(medicationId.getValue(), routeId.getValue());

    return buildJsonResponse(JsonUtil.toJson(supplyList));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getPerfusionSyringeTaskSimpleDto")
  public Response getPerfusionSyringeTaskSimpleDto(
      @QueryParam("taskId") final Parameter.String taskId,
      @QueryParam("language") final Parameter.String language)
  {
    final PerfusionSyringeTaskSimpleDto task = service.getPerfusionSyringeTaskSimpleDto(
        taskId.getValue(),
        new Locale(language.getValue()));

    return buildJsonResponse(JsonUtil.toJson(task));
  }

  @POST
  @Path("editPerfusionSyringeTask")
  public Response editSupplyReminderTask(
      @FormParam("taskId") final Parameter.String taskId,
      @FormParam("numberOfSyringes") final Parameter.Integer numberOfSyringes,
      @FormParam("urgent") final Parameter.Boolean urgent,
      @FormParam("dueTime") final Parameter.String dueTimeJson,
      @FormParam("printSystemLabel") final Parameter.Boolean printSystemLabel)
  {
    final DateTime dueTime = JsonUtil.fromJson(dueTimeJson.getValue(), DateTime.class);
    service.editPerfusionSyringeTask(
        taskId.getValue(),
        numberOfSyringes.getValue(),
        urgent.getValue(),
        dueTime,
        printSystemLabel.getValue());
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("findPerfusionSyringePreparationRequests")
  public Response getPerfusionSyringePreparationRequests(
      @QueryParam("careProviderIds") final Parameter.String careProviderIds,
      @QueryParam("patientIds") final Parameter.String patientIds,
      @QueryParam("taskTypes") final Parameter.String taskTypes,
      @QueryParam("language") final Parameter.String language)
  {
    final Opt<Collection<String>> careProviderIdsOpt = resolveStringArrayParameterToSetOpt(careProviderIds);
    final Opt<Collection<String>> patientIdsOpt = resolveStringArrayParameterToSetOpt(patientIds);
    final Set<TaskTypeEnum> taskTypesSet = getTaskTypes(JsonUtil.fromJson(taskTypes.getValue(), String[].class));

    final List<PerfusionSyringePatientTasksDto> tasksList =
        service.findPerfusionSyringePreparationRequests(
            careProviderIdsOpt,
            patientIdsOpt,
            taskTypesSet,
            new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(tasksList));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("findFinishedPerfusionSyringePreparationRequests")
  public Response getFinishedPerfusionSyringePreparationRequests(
      @QueryParam("careProviderIds") final Parameter.String careProviderIds,
      @QueryParam("patientIds") final Parameter.String patientIds,
      @QueryParam("date") final Parameter.String dateJson,
      @QueryParam("language") final Parameter.String language)
  {
    final Opt<Collection<String>> careProviderIdsOpt = resolveStringArrayParameterToSetOpt(careProviderIds);
    final Opt<Collection<String>> patientIdsOpt = resolveStringArrayParameterToSetOpt(patientIds);
    final DateTime date = JsonUtil.fromJson(dateJson.getValue(), DateTime.class);

    final List<PerfusionSyringePatientTasksDto> supplyList =
        service.findFinishedPerfusionSyringePreparationRequests(
            careProviderIdsOpt,
            patientIdsOpt,
            Intervals.wholeDay(date),
            new Locale(language.getValue()));

    return buildJsonResponse(JsonUtil.toJson(supplyList));
  }

  @POST
  @Path("orderPerfusionSyringePreparation")
  public Response orderPerfusionSyringePreparation(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("compositionUid") final Parameter.String compositionUid,
      @FormParam("ehrOrderName") final Parameter.String ehrOrderName,
      @FormParam("numberOfSyringes") final Parameter.Integer numberOfSyringes,
      @FormParam("urgent") final Parameter.Boolean urgent,
      @FormParam("dueTime") final Parameter.String dueTimeJson,
      @FormParam("printSystemLabel") final Parameter.Boolean printSystemLabel)
  {
    final DateTime dueTime = JsonUtil.fromJson(dueTimeJson.getValue(), DateTime.class);

    service.orderPerfusionSyringePreparation(
        patientId.getValue(),
        compositionUid.getValue(),
        ehrOrderName.getValue(),
        numberOfSyringes.getValue(),
        urgent.getValue(),
        dueTime,
        printSystemLabel.getValue());
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Path("startPerfusionSyringePreparations")
  public Response startPerfusionSyringePreparations(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("taskIds") final Parameter.String taskIds,
      @FormParam("originalTherapyIds") final Parameter.String originalTherapyIds,
      @FormParam("isUrgent") final Parameter.Boolean isUrgent,
      @FormParam("language") final Parameter.String language)
  {
    //starts preparation and prints data
    final String[] taskIdsArray = JsonUtil.fromJson(taskIds.getValue(), String[].class);
    final String[] originalTherapyIdsArray = JsonUtil.fromJson(originalTherapyIds.getValue(), String[].class);

    final Map<String, PerfusionSyringePreparationDto> originalTherapyIdAndPerfusionSyringeTaskIdMap =
        service.startPerfusionSyringePreparations(
            patientId.getValue(),
            Lists.newArrayList(taskIdsArray),
            Sets.newHashSet(originalTherapyIdsArray),
            isUrgent.getValue(),
            new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(originalTherapyIdAndPerfusionSyringeTaskIdMap));
  }

  @POST
  @Path("confirmPerfusionSyringePreparations")
  public Response confirmPerfusionSyringePreparations(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("taskId") final Parameter.String taskIds,
      @FormParam("isUrgent") final Parameter.Boolean isUrgent)
  {
    final String[] taskIdsArray = JsonUtil.fromJson(taskIds.getValue(), String[].class);
    final Map<String, String> originalTherapyIdAndPerfusionSyringeTaskIdMap = service.confirmPerfusionSyringePreparations(
        patientId.getValue(),
        Lists.newArrayList(taskIdsArray),
        isUrgent.getValue());
    return buildJsonResponse(JsonUtil.toJson(originalTherapyIdAndPerfusionSyringeTaskIdMap));
  }

  @POST
  @Path("dispensePerfusionSyringePreparations")
  public Response dispensePerfusionSyringePreparations(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("taskId") final Parameter.String taskIds,
      @FormParam("isUrgent") final Parameter.Boolean isUrgent)
  {
    final String[] taskIdsArray = JsonUtil.fromJson(taskIds.getValue(), String[].class);
    final Map<String, String> originalTherapyIdAndPerfusionSyringeTaskIdMap = service.dispensePerfusionSyringePreparations(
        patientId.getValue(),
        Lists.newArrayList(taskIdsArray),
        isUrgent.getValue());
    return buildJsonResponse(JsonUtil.toJson(originalTherapyIdAndPerfusionSyringeTaskIdMap));
  }

  @POST
  @Path("deletePerfusionSyringeRequest")
  public Response deletePerfusionSyringeTask(
      @FormParam("taskId") final Parameter.String taskId,
      @FormParam("language") final Parameter.String language)
  {
    service.deletePerfusionSyringeRequest(taskId.getValue(), new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @POST
  @Path("undoPerfusionSyringeRequestState")
  public Response undoPerfusionSyringeTask(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("taskId") final Parameter.String taskId,
      @FormParam("isUrgent") final Parameter.Boolean isUrgent)
  {
    final Map<String, String> originalTherapyIdAndPerfusionSyringeTaskIdMap = service.undoPerfusionSyringeRequestState(
        patientId.getValue(),
        taskId.getValue(),
        isUrgent.getValue());
    return buildJsonResponse(JsonUtil.toJson(originalTherapyIdAndPerfusionSyringeTaskIdMap));
  }

  @POST
  @Path("updateTherapySelfAdministeringStatus")
  public Response updateTherapySelfAdministeringStatus(
      @FormParam("patientId") final Parameter.String patientId,
      @FormParam("compositionUid") final Parameter.String compositionUid,
      @FormParam("selfAdministeringActionType") final Parameter.String selfAdministeringActionType)
  {
    service.updateTherapySelfAdministeringStatus(
        patientId.getValue(),
        compositionUid.getValue(),
        SelfAdministeringActionEnum.valueOf(selfAdministeringActionType.getValue()));

    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @GET
  @GZIP
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getTherapiesFormattedDescriptionsMap")
  public Response getTherapiesFormattedDescriptionsMap(
      @QueryParam("patientId") final Parameter.String patientId,
      @QueryParam("therapiesIds") final Parameter.String therapiesIds,
      @QueryParam("language") final Parameter.String language)
  {
    final Set<String> therapiesIdsSet = Sets.newHashSet(JsonUtil.fromJson(therapiesIds.getValue(), String[].class));
    final Map<String, String> therapiesMap =
        service.getTherapiesFormattedDescriptionsMap(
            patientId.getValue(), therapiesIdsSet, new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(therapiesMap));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getPatientsCurrentBnfMaximumSum")
  public Response getPatientsCurrentBnfMaximumSum(
      @QueryParam("patientId") final Parameter.String patientId)
  {
    final Integer bnfMaximumSum = service.getPatientsCurrentBnfMaximumSum(patientId.getValue());
    return buildJsonResponse(JsonUtil.toJson(bnfMaximumSum));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getTherapyDocuments")
  public Response getTherapyDocuments(
      @QueryParam("patientId") final Parameter.String patientId,
      @QueryParam("recordCount") final Parameter.Integer recordCount,
      @QueryParam("recordOffset") final Parameter.Integer recordOffset,
      @QueryParam("language") final Parameter.String language)
  {
    final TherapyDocumentsDto documentsDto =
        service.getTherapyDocuments(
            patientId.getValue(),
            recordCount.getValue(),
            recordOffset.getValue(),
            new Locale(language.getValue()));
    return buildJsonResponse(JsonUtil.toJson(documentsDto));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getMedicationExternalId")
  public Response getMedicationExternalId(
      @QueryParam("externalSystem") final Parameter.String externalSystem,
      @QueryParam("medicationId") final Parameter.Long medicationId
  )
  {
    final String medicationExternalId = service.getMedicationExternalId(externalSystem.getValue(), medicationId.getValue());
    return buildJsonResponse(JsonUtil.toJson(medicationExternalId));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getDataForTitration")
  public Response getDataForTitration(
      @QueryParam("patientId") final String patientId,
      @QueryParam("therapyId") final String therapyId,
      @QueryParam("titrationType") final String titrationTypeJson,
      @QueryParam("searchStart") final String searchStartJson,
      @QueryParam("searchEnd") final String searchEndJson,
      @QueryParam("language") final String language)
  {
    final TitrationDto titration =
        service.getDataForTitration(
            patientId,
            therapyId,
            TitrationType.valueOf(titrationTypeJson),
            JsonUtil.fromJson(searchStartJson, DateTime.class),
            JsonUtil.fromJson(searchEndJson, DateTime.class),
            new Locale(language));
    return buildJsonResponse(JsonUtil.toJson(titration));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("getUnlicensedMedicationWarning")
  public Response getUnlicensedMedicationWarning(
      @QueryParam("language") final String language)
  {
    final String unlicensedWarning = service.getUnlicensedMedicationWarning(new Locale(language));
    return buildJsonResponse(JsonUtil.toJson(unlicensedWarning));
  }

  @POST
  @Path("setAdministrationDoctorsComment")
  public Response setAdministrationDoctorsComment(
      @FormParam("taskId") final String taskId,
      @FormParam("doctorsComment") final String doctorsComment)
  {
    service.setAdministrationDoctorsComment(taskId, doctorsComment);
    return buildJsonResponse(JsonUtil.toJson(""));
  }

  @GET
  @Path("getMedicationIdForBarcode")
  public Response getMedicationIdForBarcode(
      @QueryParam("barcode") final String barcode)
  {
    final Long medicationId = service.getMedicationIdForBarcode(barcode);
    return buildJsonResponse(JsonUtil.toJson(medicationId));
  }

  @GET
  @Path("getAdministrationTaskForBarcode")
  public Response getAdministrationTaskForBarcode(
      @QueryParam("patientId") final String patientId,
      @QueryParam("medicationBarcode") final String medicationBarcode)
  {
    final BarcodeTaskSearchDto barcodeScan = service.getAdministrationTaskForBarcode(patientId, medicationBarcode);
    return buildJsonResponse(JsonUtil.toJson(barcodeScan));
  }

  @GET
  @Path("getOriginalTherapyId")
  public Response getOriginalTherapyId(
      @QueryParam("patientId") final String patientId,
      @QueryParam("therapyId") final String therapyId)
  {
    final String originalTherapyId = service.getOriginalTherapyId(patientId, therapyId);
    return buildJsonResponse(JsonUtil.toJson(originalTherapyId));
  }

  private static final JsonUtil.TypeAdapterPair PAIR_SERIALIZER = new JsonUtil.TypeAdapterPair(
      Pair.class, (JsonSerializer<Pair>)(src, typeOfSrc, context) ->
  {
    final JsonObject result = new JsonObject();
    result.add("first", context.serialize(src.getFirst()));
    result.add("second", context.serialize(src.getSecond()));
    return result;
  });

  private static final JsonUtil.TypeAdapterPair INTERVAL_SERIALIZER = new JsonUtil.TypeAdapterPair(
      Interval.class, (JsonSerializer<Interval>)(src, typeOfSrc, context) ->
  {
    final JsonObject result = new JsonObject();
    result.add("startMillis", context.serialize(src.getStartMillis()));
    result.add("endMillis", context.serialize(src.getEndMillis()));
    return result;
  });

  private static final JsonUtil.TypeAdapterPair INTERVAL_DESERIALIZER = new JsonUtil.TypeAdapterPair(
      Interval.class, (JsonDeserializer<Interval>)(json, typeOfT, context) ->
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
  });

  private static final JsonUtil.TypeAdapterPair RULE_PARAMETERS_DESERIALIZER = new JsonUtil.TypeAdapterPair(
      RuleParameters.class, (JsonDeserializer<RuleParameters>)(json, typeOfT, context) ->
  {
    final String medicationRuleEnum
        = context.deserialize(json.getAsJsonObject().get("medicationRuleEnum"), String.class);

    if (MedicationRuleEnum.valueOf(medicationRuleEnum) == MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE)
    {
      final String medicationParacetamolRuleType
          = context.deserialize(json.getAsJsonObject().get("medicationParacetamolRuleType"), String.class);

      if (MedicationParacetamolRuleType.valueOf(medicationParacetamolRuleType) == MedicationParacetamolRuleType.FOR_THERAPY)
      {
        return context.deserialize(json, ParacetamolRuleForTherapyParameters.class);
      }
      else if (MedicationParacetamolRuleType.valueOf(medicationParacetamolRuleType) == MedicationParacetamolRuleType.FOR_THERAPIES)
      {
        return context.deserialize(json, ParacetamolRuleForTherapiesParameters.class);
      }
      else if (MedicationParacetamolRuleType.valueOf(medicationParacetamolRuleType) == MedicationParacetamolRuleType.FOR_ADMINISTRATION)
      {
        return context.deserialize(json, ParacetamolRuleForAdministrationParameters.class);
      }
      else
      {
        return null;
      }
    }
    return null;
  });

  private static final JsonUtil.TypeAdapterPair THERAPY_CHANGE_DESERIALIZER = new JsonUtil.TypeAdapterPair(
      TherapyChangeDto.class, (JsonDeserializer<TherapyChangeDto<?, ?>>)(json, typeOfT, context) ->
  {
    final TherapyChangeType type =
        TherapyChangeType.valueOf(context.deserialize(json.getAsJsonObject().get("type"), String.class));
    return context.deserialize(json, type.getDtoClass());
  });

  private static Set<TaskTypeEnum> getTaskTypes(final String[] taskTypeNamesArray)
  {
    final Set<TaskTypeEnum> taskTypesSet = new HashSet<>();
    for (final String taskTypeName : taskTypeNamesArray)
    {
      taskTypesSet.add(TaskTypeEnum.valueOf(taskTypeName));
    }
    return taskTypesSet;
  }

  private Opt<Collection<String>> resolveStringArrayParameterToSetOpt(final Parameter.String parameter)
  {
    return Opt.resolve(() -> Sets.newHashSet(JsonUtil.fromJson(parameter.getValue(), String[].class)));
  }
}
