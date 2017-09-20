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

Class.define('app.views.medications.timeline.titration.BaseApplicationRowContainer', 'tm.jquery.Container', {
  therapyForTitration: null,
  administration: null,
  allAdministrations: null,
  medicationData: null,
  latestTherapyId: null,
  administrationType: null,
  scheduleAdditional: false,
  applyUnplanned: false,
  stopFlow: false,

  /**privates**/
  _administrationWarnings: null,
  _timePicker: null,
  _plannedDoseTimeValidator: null,
  _markAsGivenCheckBox: null,
  _administrationWarningsProvider: null,
  _warningContainer: null,
  _commentField: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this._configureValidators();
    this.buildGui();
  },

  _configureValidators: function()
  {
    this._plannedDoseTimeValidator = new app.views.medications.timeline.administration.PlannedDoseTimeValidator({
      administrations: this.getAllAdministrations()
    });

    this._administrationWarningsProvider = new app.views.medications.timeline.administration.AdministrationWarningsProvider({
      view: this.getView(),
      plannedDoseTimeValidator: this._plannedDoseTimeValidator,
      administration: this.getAdministration(),
      administrations: this.getAllAdministrations(),
      administrationType: this.getAdministrationType(),
      therapy: this.getTherapyForTitration().getTherapy(),
      infusionActive: this.isActiveContinuousInfusion()
    });
  },

  buildGui: function()
  {
    var view = this.getView();
    var plannedDoseTime = this.getAdministration() ? new Date(this.getAdministration().plannedTime) : CurrentTime.get();

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0));

    var verticalWrapperMarkerLine = new tm.jquery.Container({
      cls: "wrapper-marker-line",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "5px"),
      alignSelf: "stretch"
    });

    var verticalContentWrapper = new tm.jquery.Container({
      scrollable: 'visible',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0)
    });

    var applicationConditionsRow = new tm.jquery.Container({
      cls: "application-conditions-row",
      scrollable: 'visible',
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    var applicationPlannedTimeColumn = new tm.views.medications.common.VerticallyTitledComponent({
      cls: "planned-time-container",
      titleText: view.getDictionary("planned.time"),
      scrollable: 'visible',
      contentComponent: new tm.jquery.DateTimePicker({
        date: plannedDoseTime,
        showType: "focus",
        scrollable: 'visible',
        style: "overflow: visible;", /* remove once scrollable starts working */
        nowButton: {
          text: this.view.getDictionary("asap")
        },
        currentTimeProvider: function()
        {
          return CurrentTime.get();
        }
      }),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    this._timePicker = applicationPlannedTimeColumn.getContentComponent();

    this._timePicker.getDatePicker().getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        setCurrentDateTimeToPicker);

    this._timePicker.getTimePicker().getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        setCurrentDateTimeToPicker);

    var commentColumn = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary("commentary"),
      scrollable: 'visible',
      contentComponent: new tm.jquery.TextArea({
        width: 400,
        maxLength: 60,
        cls: 'comment-field',
        rows: 1,
        placeholder: view.getDictionary('commentary') + "...",
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      }),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    this._commentField = commentColumn.getContentComponent();

    var warningsRow = new tm.views.medications.timeline.AdministrationWarningContainer({
      view: view,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    this._warningContainer = warningsRow;

    applicationConditionsRow.add(applicationPlannedTimeColumn);
    applicationConditionsRow.add(this.buildApplicationOptionsColumn());

    var applicationDosingRow = this.buildApplicationDosingRow();
    applicationDosingRow.add(commentColumn);

    verticalContentWrapper.add(applicationConditionsRow);
    verticalContentWrapper.add(applicationDosingRow);
    if (this.isContinuousInfusion() && view.isInfusionBagEnabled())
    {
      verticalContentWrapper.add(this.buildInfusionBagRow());
    }
    verticalContentWrapper.add(warningsRow);

    this.add(verticalWrapperMarkerLine);
    this.add(verticalContentWrapper);

    function setCurrentDateTimeToPicker()
    {
      this._timePicker.setDate(CurrentTime.get());
    }
  },

  /**
   * @returns {tm.views.medications.common.VerticallyTitledComponent}
   */
  buildApplicationOptionsColumn: function()
  {
    var applicationOptionsColumn = new tm.views.medications.common.VerticallyTitledComponent({
      contentComponent: new tm.jquery.Container({
        layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
      }),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto')
    });
    return applicationOptionsColumn;
  },

  /**
   * @returns {tm.jquery.Container}
   */
  buildApplicationDosingRow: function()
  {
    var applicationDosingRow = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0)
    });
    return applicationDosingRow;
  },

  /**
   * @returns {boolean}
   */
  isActiveContinuousInfusion: function()
  {
    return this.activeContinuousInfusion === true;
  },

  /**
   * @returns {Boolean}
   */
  isMarkedGiven: function()
  {
    return this._markAsGivenCheckBox && this._markAsGivenCheckBox.isChecked();
  },

  /**
   * @returns {Boolean}
   */
  isContinuousInfusion: function()
  {
    return this.getTherapyForTitration().getTherapy().isContinuousInfusion() === true;
  },

  /**
   * @returns {Boolean}
   */
  isAdjustInfusion: function()
  {
    return this.getAdministrationType() === app.views.medications.TherapyEnums.administrationTypeEnum.ADJUST_INFUSION;
  },

  /**
   * @returns {Boolean}
   */
  isScheduleAdditional: function()
  {
    return this.scheduleAdditional === true;
  },

  /**
   * @returns {Boolean}
   */
  isApplyUnplanned: function()
  {
    return this.applyUnplanned === true;
  },

  /**
   * @returns {Boolean}
   */
  isStopFlow: function()
  {
    return this.stopFlow === true;
  },

  /**
   * @returns {Boolean}
   */
  isBolusAdministration: function()
  {
    return this.getAdministrationType() === app.views.medications.TherapyEnums.administrationTypeEnum.BOLUS;
  },

  /**
   * @param {app.views.medications.common.dto.TherapyDose} plannedDose
   * @param {Date} plannedTime
   * @param {Boolean} markGiven
   * @param {String} applicationComment
   * @param {Date|null} setUntilDate
   * @returns {tm.jquery.Promise}
   */
  applyAdministration: function(plannedDose, plannedTime, markGiven, applicationComment, setUntilDate)
  {
    var view = this.getView();
    var therapy = this.getTherapyForTitration().getTherapy();

    var administration = this.getAdministration() ? jQuery.extend(true, {}, this.getAdministration()) : {};

    if (!this.getAdministration() && (this.isApplyUnplanned() ||
        (this.isContinuousInfusion() && (this.isAdjustInfusion() || this.isBolusAdministration()))))
    {
      administration.administrationTime = plannedTime;
      administration.administeredDose = plannedDose;
      administration.additionalAdministration = true;
      administration.administrationType = this.getAdministrationType();
      administration.comment = applicationComment;

      return view.getRestApi().confirmAdministrationTask(therapy, administration, false, false, true);
    }
    else if (this.isScheduleAdditional())
    {
      administration.plannedTime = plannedTime;
      administration.plannedDose = plannedDose;

      if (markGiven)
      {
        administration.administrationTime = plannedTime;
        administration.administeredDose = plannedDose;
        administration.comment = applicationComment;
      }
      else
      {
        administration.doctorsComment = applicationComment;
      }
      return view.getRestApi().createAdministrationTask(
          therapy,
          administration,
          false,
          true);
    }
    else
    {
      administration.plannedDose = plannedDose;
      administration.plannedTime = plannedTime;

      if (markGiven)
      {
        administration.administrationTime = plannedTime;
        administration.administeredDose = plannedDose;
        administration.comment = applicationComment;
        if (this.isContinuousInfusion() && this._bagField && this._bagField.getValue())
        {
          administration.infusionBag = {quantity: this._bagField.getValue(), unit: "mL"};
        }
      }
      else
      {
        administration.doctorsComment = applicationComment;
      }
      return view.getRestApi().setAdministrationTitrationDose(
          this.getLatestTherapyId(),
          administration,
          markGiven,
          setUntilDate,
          true);
    }
  },

  /**
   * Getters & Setters
   */

  /**
   * @param {app.views.medications.timeline.administration.AdministrationWarnings|null} warnings
   * @private
   */
  setAdministrationWarnings: function(warnings)
  {
    this._administrationWarnings = warnings;
  },

  /**
   * @returns {null|app.views.medications.TherapyEnums.administrationTypeEnum}
   */
  getAdministrationType: function()
  {
    return this.administrationType;
  },

  /**
   * @returns {app.views.medications.timeline.titration.dto.TherapyForTitration}
   */
  getTherapyForTitration: function()
  {
    return this.therapyForTitration;
  },

  /**
   * @returns {Object}
   */
  getAdministration: function()
  {
    return this.administration;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {String}
   */
  getDoctorsComment: function()
  {
    return tm.jquery.Utils.isEmpty(this._commentField.getValue()) ? null : this._commentField.getValue();
  },

  /**
   * @returns {app.views.medications.common.dto.MedicationData}
   */
  getMedicationData: function()
  {
    return this.medicationData;
  },

  /**
   * @returns {Array<Object>}
   */
  getAllAdministrations: function()
  {
    return tm.jquery.Utils.isArray(this.allAdministrations) ? this.allAdministrations : [];
  },

  /**
   * @returns {String}
   */
  getLatestTherapyId: function()
  {
    return this.latestTherapyId;
  },

  /**
   * @returns {app.views.medications.timeline.administration.AdministrationWarnings|null}
   */
  getAdministrationWarnings: function()
  {
    return this._administrationWarnings;
  },

  /**
   * @returns {Date|null}
   */
  getAdministrationDateTime: function()
  {
    return this._timePicker.getDate();
  }
});
