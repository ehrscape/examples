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

Class.define('tm.views.medications.timeline.RescheduleTasksContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls:'reschedule-tasks-container',
  scrollable: 'vertical',
  /** configs */
  view: null,
  administration: null,
  administrations: null,
  therapy: null,

  /** privates: components */
  administrationDateTimeCard: null,
  administrationDateField: null,
  administrationTimeField: null,
  moveAllRadioButtonGroup: null,
  moveAllRadioButton: null,
  moveSingleRadioButton: null,
  validationForm: null,
  _warningContainer: null,
  _plannedDoseTimeValidator: null,
  _enableDialogConfirmationFunction: null,
  resultCallback: null,
  _administrationWarningsProvider: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"));
    this._buildComponents();
    this._buildGui();
  },

  _buildComponents: function()
  {
    var self = this;

    this.administrationDateTimeCard = new tm.jquery.Container({layout: new tm.jquery.HFlexboxLayout({gap: 5})});
    this.administrationDateField = new tm.jquery.DatePicker({
      currentTimeProvider: function()
      {
        return CurrentTime.get();
      }
    });
    this.administrationTimeField = new tm.jquery.TimePicker({
      currentTimeProvider: function()
      {
        return CurrentTime.get();
      }
    });
    this.administrationDateField.getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        function()
        {
          self.administrationDateField.setDate(CurrentTime.get(), true);
          self.administrationTimeField.setTime(CurrentTime.get());
        });
    this.administrationDateField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._checkAllowedTherapyAdministration();
    });
    this.administrationTimeField.getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        function()
        {
          self.administrationDateField.setDate(CurrentTime.get(), true);
          self.administrationTimeField.setTime(CurrentTime.get());
        });

    this.administrationTimeField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._checkAllowedTherapyAdministration();
    });
    var administrationTime = this.administration ? new Date(this.administration.plannedTime) : CurrentTime.get();
    this.administrationDateField.setDate(administrationTime, true);
    this.administrationTimeField.setTime(administrationTime, true);

    this.moveSingleRadioButton = new tm.jquery.RadioButton({labelText: this.view.getDictionary('single.f'), data: true, labelAlign: "right", checked: true});
    this.moveAllRadioButton = new tm.jquery.RadioButton({labelText: this.view.getDictionary('all.f'), data: false, labelAlign: "right"});
    this.moveAllRadioButtonGroup = new tm.jquery.RadioButtonGroup();
    this.moveAllRadioButtonGroup.add(this.moveSingleRadioButton);
    this.moveAllRadioButtonGroup.add(this.moveAllRadioButton);

    this._warningContainer = new tm.views.medications.timeline.AdministrationWarningContainer({
      view: this.view,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    this._plannedDoseTimeValidator = new app.views.medications.timeline.administration.PlannedDoseTimeValidator({
      administrations: this.administrations
    });
    this._administrationWarningsProvider = new app.views.medications.timeline.administration.AdministrationWarningsProvider({
      view: this.view,
      plannedDoseTimeValidator: this._plannedDoseTimeValidator,
      administration: this.administration,
      administrations: this.administrations,
      administrationType: this.administrationType,
      therapy: this.therapy,
      infusionActive: this.infusionActive,
      therapyReviewedUntil: this.therapyReviewedUntil
    });
  },

  _buildGui: function()
  {
    var mainContainer = new tm.jquery.Container({
      layout: new tm.jquery.VFlexboxLayout(),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    if (this.isDosingFrequencyBetweenDoses())
    {
      mainContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('move'), 0));
      mainContainer.add(this.view.getAppFactory().createHRadioButtonGroupContainer(this.moveAllRadioButtonGroup));
    }
    mainContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('reschedule.to'), 0));
    mainContainer.add(this.administrationDateTimeCard);
    this.administrationDateTimeCard.add(this.administrationDateField);
    this.administrationDateTimeCard.add(this.administrationTimeField);

    this.administrationDateField.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      component.getInputElement().focus();
    });

    this.add(mainContainer);
    this.add(this._warningContainer);
  },

  _rescheduleTasks: function()
  {
    var self = this;
    this.getView().getRestApi().rescheduleTasks(
        this.administration.taskId,
        this._getSelectedTimestamp(),
        this.getTherapy().getTherapyId(),
        this.moveAllRadioButtonGroup.getActiveRadioButton().data,
        true).then(
        function()
        {
          var resultData = new app.views.common.AppResultData({success: true});
          self.resultCallback(resultData);
        },
        function()
        {
          var resultData = new app.views.common.AppResultData({success: false});
          self.resultCallback(resultData);
        });
  },

  /**
   * Check if selected time is between previous and next planned administration
   * @private
   */
  _checkAllowedTherapyAdministration: function()
  {
    var warnings = this._administrationWarningsProvider.getRestrictiveAdministrationWarnings(
        this._getSelectedTimestamp(),
        true,
        false);
    this._warningContainer.setRestrictiveWarnings(warnings);
    if (this._enableDialogConfirmationFunction)
    {
      this._enableDialogConfirmationFunction(!warnings.hasRestrictiveWarnings());
    }
  },

  /**
   * @returns {Date}
   * @private
   */
  _getSelectedTimestamp: function()
  {
    var administrationDate = this.administrationDateField.getDate();
    var administrationTime = this.administrationTimeField.getTime();
    return new Date(
        administrationDate.getFullYear(),
        administrationDate.getMonth(),
        administrationDate.getDate(),
        administrationTime.getHours(),
        administrationTime.getMinutes(),
        0, 0);
  },

  /**
   * Checks if selected timestamp is more than 10% off planned timestamp
   * @private
   */
  _checkWarnSelectedTimeDifference: function()
  {
    var self = this;
    var utils = tm.views.medications.MedicationUtils;
    if (this._plannedDoseTimeValidator.assertTimeDifferenceTooBig(
            this._getSelectedTimestamp(),
            new Date(this.administration.plannedTime),
            0.1,
            this.getTherapy().getDosingFrequency().value))
    {
      utils.openConfirmationWithWarningDialog(this.view,
          tm.jquery.Utils.formatMessage(
              this.view.getDictionary("selected.timestamp.difference.warning"), this.getTherapy().getDosingFrequency().value),
          300,
          150).then(function(confirm)
          {
            if (confirm)
            {
              self._rescheduleTasks();
            }
            else if (self.resultCallback)
            {
              var resultData = new app.views.common.AppResultData({success: false});
              self.resultCallback(resultData);
            }
          },
          function()
          {
            if (self.resultCallback)
            {
              var resultData = new app.views.common.AppResultData({success: false});
              self.resultCallback(resultData);
            }
          })
    }
    else
    {
      this._rescheduleTasks();
    }
  },
  
  /**
   * @param {function} enableFunction
   */
  setEnableDialogConfirmationFunction: function(enableFunction)
  {
    this._enableDialogConfirmationFunction = enableFunction;
  },

  /**
   * @returns {app.views.medications.common.dto.Therapy}
   */
  getTherapy: function()
  {
    return this.therapy;
  },

  /**
   * @returns {Boolean}
   */
  isDosingFrequencyBetweenDoses: function()
  {
    var enums = app.views.medications.TherapyEnums;

    return this.getTherapy().getDosingFrequency() && 
        this.getTherapy().getDosingFrequency().type === enums.dosingFrequencyTypeEnum.BETWEEN_DOSES;
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    this.resultCallback = resultDataCallback;
    if (this.isDosingFrequencyBetweenDoses() && this.moveAllRadioButtonGroup.getActiveRadioButton().data)
    {
      this._checkWarnSelectedTimeDifference();
    }
    else
    {
      this._rescheduleTasks();
    }
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});