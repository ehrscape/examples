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
Class.define('app.views.medications.TherapyDisplayProvider', 'tm.jquery.Object', {
  /** members: configs */
  view: null,

  showChangeHistory: true,
  showChangeReason: true,
  showBnf: true,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @param {Object} dto
   * @returns {app.views.medications.TherapyEnums.therapyStatusEnum|null}
   */
  getTherapyStatus: function(dto)
  {
    var therapyStatus = tm.jquery.Utils.isFunction(dto.getTherapyStatus) ? dto.getTherapyStatus() : dto.therapyStatus;
    return tm.jquery.Utils.isEmpty(dto.changeType) ? therapyStatus : dto.changeType;
  },

  getStatusIcon: function(dto)
  {
    var status = this.getTherapyStatus(dto);
    var enums = app.views.medications.TherapyEnums;
    if (status == enums.therapyStatusEnum.ABORTED)
    {
      return "icon_aborted";
    }
    if (status == enums.therapyStatusEnum.CANCELLED)
    {
      return "icon_cancelled";
    }
    if (status == enums.therapyStatusEnum.LATE)
    {
      return "icon_late";
    }
    if (status == enums.therapyStatusEnum.VERY_LATE)
    {
      return "icon_very_late";
    }
    if (status == enums.therapyStatusEnum.SUSPENDED)
    {
      if (!tm.jquery.Utils.isEmpty(dto.therapyChangeReasonEnum &&
              dto.therapyChangeReasonEnum == enums.therapyChangeReasonEnum.TEMPORARY_LEAVE))
      {
        return "icon_suspended_temporary_leave";
      }
      return "icon_suspended";
    }
    if (status == enums.pharmacistTherapyChangeType.ABORT)
    {
      return "icon_aborted";
    }
    if (status == enums.pharmacistTherapyChangeType.SUSPEND)
    {
      return "icon_suspended";
    }
    if (dto.additionalWarnings && dto.additionalWarnings.length)
    {
      return "high-alert-icon";
    }
    return null;
  },

  getStatusDescription: function(dto)
  {
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    var status = this.getTherapyStatus(dto);
    var therapy = dto.therapy;

    if (status === enums.therapyStatusEnum.ABORTED)
    {
      return view.getDictionary('stopped.therapy');
    }
    if (status === enums.therapyStatusEnum.CANCELLED)
    {
      return view.getDictionary('canceled');
    }
    if (status === enums.therapyStatusEnum.LATE)
    {
      return view.getDictionary('delay.of.confirmation');
    }
    if (status === enums.therapyStatusEnum.VERY_LATE)
    {
      return view.getDictionary('delay.of.confirmation.long');
    }
    if (status === enums.therapyStatusEnum.SUSPENDED)
    {
      if (!tm.jquery.Utils.isEmpty(dto.therapyChangeReasonEnum &&
              dto.therapyChangeReasonEnum == enums.therapyChangeReasonEnum.TEMPORARY_LEAVE))
      {
        return view.getDictionary('patient.on.temporary.leave');
      }
      return view.getDictionary('suspended.therapy');
    }
    if (status === enums.pharmacistTherapyChangeType.ABORT)
    {
      return view.getDictionary('stopped.therapy');
    }
    if (status === enums.pharmacistTherapyChangeType.SUSPEND)
    {
      return view.getDictionary('suspended.therapy');
    }
    if (dto.additionalWarnings && dto.additionalWarnings.length)
    {
      return view.getDictionary('additional.warnings');
    }
    return null;
  },

  getStatusClass: function(dto)
  {
    var enums = app.views.medications.TherapyEnums;
    var status = this.getTherapyStatus(dto);
    var therapy = dto.therapy;
    var therapyEndTime = therapy.end ? new Date(therapy.end) : null;
    var therapyEnded = therapyEndTime != null && therapyEndTime.getTime() <= CurrentTime.get().getTime();

    var containerStyles = [];

    if (dto.hasOwnProperty("active") && dto.active !== true) // DayTherapy
    {
      containerStyles.push("inactive");
    }
    if (dto.additionalWarnings && dto.additionalWarnings.length)
    {
      containerStyles.push("additional-warning")
    }
    if (status === enums.therapyStatusEnum.ABORTED
        || status === enums.therapyStatusEnum.CANCELLED)
    {
      containerStyles.push("aborted");
    }
    else if (status === enums.therapyStatusEnum.SUSPENDED)
    {
      containerStyles.push("suspended")
    }
    else if (dto.modifiedFromLastReview === true && !therapyEnded)
    {
      containerStyles.push("changed")
    }

    containerStyles.push(therapyEnded ? "ended" : "normal");

    return containerStyles.join(" ");
  },

  getPharmacistReviewIcon: function(status)
  {
    var enums = app.views.medications.TherapyEnums;
    if (status == enums.therapyPharmacistReviewStatusEnum.REVIEWED)
    {
      return null;
    }
    if (status == enums.therapyPharmacistReviewStatusEnum.NOT_REVIEWED)
    {
      return "icon_pharmacist_not_reviewed";
    }
    if (status == enums.therapyPharmacistReviewStatusEnum.REVIEWED_REFERRED_BACK)
    {
      return "icon_pharmacist_refer_back";
    }

  },

  getPharmacistReviewDescription: function(status)
  {
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    if (status === enums.therapyPharmacistReviewStatusEnum.REVIEWED)
    {
      return null;
    }
    if (status === enums.therapyPharmacistReviewStatusEnum.NOT_REVIEWED)
    {
      return view.getDictionary('pharmacist.review.waiting');
    }
    if (status === enums.therapyPharmacistReviewStatusEnum.REVIEWED_REFERRED_BACK)
    {
      return view.getDictionary('pharmacist.review.referred');
    }
  },

  getSelfAdminStatusIcon: function(selfAdminActionEnum, therapyStatus)
  {
    var enums = app.views.medications.TherapyEnums;
    var isTherapyActive = !(tm.jquery.Utils.isEmpty(therapyStatus) || therapyStatus == enums.therapyStatusEnum.ABORTED
    || therapyStatus == enums.therapyStatusEnum.CANCELLED || therapyStatus == enums.therapyStatusEnum.SUSPENDED);

    if (isTherapyActive)
    {
      if (selfAdminActionEnum == enums.selfAdministeringActionEnum.AUTOMATICALLY_CHARTED)
      {
        return "icon_self_admin_automatic";
      }
      else if (selfAdminActionEnum == enums.selfAdministeringActionEnum.CHARTED_BY_NURSE)
      {
        return "icon_self_admin_nurse";
      }
      else
      {
        return null;
      }
    }
    else
    {
      return null;
    }
  },

  getSelfAdminStatusDescription: function(selfAdminActionEnum, therapyStatus)
  {
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    var isTherapyActive = !(tm.jquery.Utils.isEmpty(therapyStatus) || therapyStatus === enums.therapyStatusEnum.ABORTED
    || therapyStatus === enums.therapyStatusEnum.CANCELLED || therapyStatus === enums.therapyStatusEnum.SUSPENDED);

    if (isTherapyActive)
    {
      if (selfAdminActionEnum === enums.selfAdministeringActionEnum.AUTOMATICALLY_CHARTED)
      {
        return view.getDictionary('automatically.charted');
      }
      else if (selfAdminActionEnum === enums.selfAdministeringActionEnum.CHARTED_BY_NURSE)
      {
        return view.getDictionary('charted.by.nurse');
      }
      else
      {
        return null;
      }
    }
    else
    {
      return null;
    }
  },

  getMedicationNameDisplay: function(medication, showGeneric, preferShortName)
  {
    var nameDisplay = "";
    var name = (preferShortName && medication.shortName) ? medication.shortName : medication.name;
    if (showGeneric && medication.genericName)
    {
      nameDisplay += "<span class='TextDataBold'>" + medication.genericName + "</span>";
      nameDisplay += "<span class='TextData'> (" + name + ")";
    }
    else
    {
      nameDisplay += "<span class='TextData'>" + name;
    }
    nameDisplay += "</span>";
    return nameDisplay;
  },

  getBigIconContainerOptions: function(dto)
  {
    var view = this.getView();
    var therapy = dto.therapy;

    var options = {
      background: {cls: this.getTherapyIcon(therapy)},
      layers: []
    };
    var statusIcon = this.getStatusIcon(dto);
    var pharmacistReviewStatusIcon = null;
    var selfAdminStatusIcon = null;

    if (view.getTherapyAuthority().isShowPharmacistReviewStatus() && !tm.jquery.Utils.isEmpty(dto.therapyPharmacistReviewStatus))
    {
      pharmacistReviewStatusIcon = this.getPharmacistReviewIcon(dto.therapyPharmacistReviewStatus);
    }

    if (!tm.jquery.Utils.isEmpty(therapy.selfAdministeringActionEnum))
    {
      selfAdminStatusIcon = this.getSelfAdminStatusIcon(therapy.selfAdministeringActionEnum, this.getTherapyStatus(dto));
      if (!tm.jquery.Utils.isEmpty(selfAdminStatusIcon))
      {
        options.layers.push({hpos: "right", vpos: "center", cls: "status-icon " + selfAdminStatusIcon});
      }
    }

    if (therapy != null && therapy.linkName)
    {
      var link = therapy.linkName;
      if (link.length <= 3)
      {
        options.layers.push({hpos: "left", vpos: "bottom", cls: "icon_link", html: link});
      }
    }
    if (dto.modifiedFromLastReview || dto.completed == false || therapy.completed == false)
    {
      options.layers.push({hpos: "left", vpos: "top", cls: "icon_changed"});
    }
    if (dto.showConsecutiveDay)
    {
      options.layers.push({hpos: "right", vpos: "top", cls: "icon_day_number", html: dto.consecutiveDay});
    }
    if (!tm.jquery.Utils.isEmpty(therapy.criticalWarnings) && therapy.criticalWarnings.length > 0)
    {
      options.layers.push({hpos: "center", vpos: "center", cls: "icon_warning"});
    }

    options.layers.push({hpos: "right", vpos: "bottom", cls: statusIcon});

    if (!tm.jquery.Utils.isEmpty(pharmacistReviewStatusIcon))
    {
      options.layers.push({hpos: "center", vpos: "bottom", cls: "status-icon " + pharmacistReviewStatusIcon});
    }

    return options;
  },

  /**
   * @param {app.views.medications.common.dto.Therapy|app.views.medications.common.dto.OxygenTherapy} therapy
   * @returns {String}
   */
  getTherapyIcon: function(therapy)
  {
    if (!tm.jquery.Utils.isEmpty(therapy) && tm.jquery.Utils.isArray(therapy.getRoutes()))
    {
      var routeType = !tm.jquery.Utils.isEmpty(therapy.getRoutes()[0]) ? therapy.getRoutes()[0].type : null;
      var therapyEnums = app.views.medications.TherapyEnums;

      if (routeType == therapyEnums.medicationRouteTypeEnum.IV)
      {
        if (therapy.isBaselineInfusion())
        {
          return "icon_baseline_infusion";
        }
        if (therapy.isContinuousInfusion())
        {
          return "icon_continuous_infusion";
        }
        if (therapy.getSpeedDisplay())
        {
          if (therapy.getSpeedDisplay() == 'BOLUS')
          {
            return "icon_bolus";
          }
          return "icon_infusion";
        }
        return "icon_injection"
      }
      if (routeType == therapyEnums.medicationRouteTypeEnum.IM)
      {
        return "icon_injection"
      }
      if (routeType == therapyEnums.medicationRouteTypeEnum.INHAL)
      {
        return "icon_inhalation"
      }
      if (therapy.getDoseForm() && therapy.getDoseForm().doseFormType == 'TBL')
      {
        return "icon_pills";
      }
    }
    return "icon_other_medication";
  },

  getBigIconContainerHtml: function(dto)
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();
    var options = this.getBigIconContainerOptions(dto);

    return appFactory.createLayersContainerHtml(options);
  },

  getShowChangeHistory: function()
  {
    return this.showChangeHistory === true;
  },

  getShowChangeReason: function()
  {
    return this.showChangeReason === true;
  },

  setShowChangeReason: function(value)
  {
    this.showChangeReason = value;
  },

  getShowBnf: function()
  {
    return this.showBnf === true;
  },

  setShowBnf: function(value)
  {
    this.showBnf = value;
  },
  setShowChangeHistory: function(value)
  {
    this.showChangeHistory = value;
  },

  getView: function()
  {
    return this.view;
  }
});