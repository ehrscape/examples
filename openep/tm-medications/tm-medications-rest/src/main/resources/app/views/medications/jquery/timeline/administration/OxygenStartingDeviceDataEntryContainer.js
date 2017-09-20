/*
 * Copyright (c) 2010-2017 Marand d.o.o. (www.marand.com)
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
Class.define('app.views.medications.timeline.administration.OxygenStartingDeviceDataEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: 'oxygen-starting-device-dialog',
  scrollable: 'vertical',

  therapy: null,
  currentStartingDevice: null,
  currentFlowRate: null,
  administration: null,

  _oxygenRouteContainer: null,
  _changeDatePicker: null,
  _changeTimePicker: null,
  _commentField: null,
  _form: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
    this._configureForm();
  },

  _buildGui: function()
  {
    var view = this.getView();

    this.setLayout(tm.jquery.VFlexboxLayout.create("start", "stretch", 0));

    var therapyDescriptionContainer = new tm.jquery.Container({
      layout: new tm.jquery.HFlexboxLayout(),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      html: this.getTherapy().getFormattedTherapyDisplay(),
      cls: 'TherapyDescription'
    });

    var oxygenRouteRowContainer = new tm.jquery.Container({
      cls: 'oxygen-route-row-container',
      scrollable: "visible",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    var oxygenRouteLabel = new tm.jquery.Component({
      cls: 'TextLabel route-label',
      html: view.getDictionary('device'),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    var oxygenRouteContainer = new app.views.medications.ordering.oxygen.OxygenRouteContainer({
      view: view,
      allowDeviceDeselect: false,
      cls: "oxygen-route-container",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      startingDevice: this.getCurrentStartingDevice()
    });

    var changeDateTimeContainer = new tm.jquery.Container({
      cls: 'change-time-container',
      scrollable: 'visible',
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      height: 48
    });

    var changeDateTimeLabel = new tm.jquery.Container({
      cls: 'TextLabel change-time-label',
      html: view.getDictionary('change.time'),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    var changeDateField = new tm.jquery.DatePicker({
      showType: "focus",
      width: 100,
      date: this._getInitialTimestamp(),
      currentTimeProvider: function()
      {
        return CurrentTime.get();
      }
    });
    changeDateField.setFlex(new tm.jquery.flexbox.item.Flex.create(1, 1, "auto"));
    var changeTimeField = new tm.jquery.TimePicker({
      showType: "focus",
      width: 60,
      time: this._getInitialTimestamp(),
      nowButton: {
        text: view.getDictionary("asap")
      },
      currentTimeProvider: function()
      {
        return CurrentTime.get();
      }
    });

    var commentField = new tm.jquery.TextArea({
      width: 438,
      cls: 'comment-field',
      rows: 4,
      placeholder: view.getDictionary('commentary') + "..."
    });

    var commentLabelContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create('flex-start', 'center', 0),
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto")
    });
    var commentContainer = new tm.jquery.Container({
      cls: 'comment-container',
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    commentLabelContainer.add(tm.views.medications.MedicationUtils.crateLabel(
        'TextLabel',
        view.getDictionary('commentary')
    ));
    commentContainer.add(commentLabelContainer);
    commentContainer.add(commentField);

    changeDateTimeContainer.add(changeDateTimeLabel);
    changeDateTimeContainer.add(changeDateField);
    changeDateTimeContainer.add(changeTimeField);

    oxygenRouteRowContainer.add(oxygenRouteLabel);
    oxygenRouteRowContainer.add(oxygenRouteContainer);

    this.add(therapyDescriptionContainer);
    this.add(oxygenRouteRowContainer);
    this.add(changeDateTimeContainer);
    this.add(commentContainer);

    this._oxygenRouteContainer = oxygenRouteContainer;
    this._changeDatePicker = changeDateField;
    this._changeTimePicker = changeTimeField;
    this._commentField = commentField;
  },

  _configureForm: function ()
  {
    var self = this;

    var form = new tm.jquery.Form({
      view: this.getView(),
      showTooltips: false,
      requiredFieldValidatorErrorMessage: this.getView().getDictionary("field.value.is.required")
    });

    // reason input //
    form.addFormField(new tm.jquery.FormField({
      name: "oxygenStartingDevice", label: null, component: this._oxygenRouteContainer, required: true,
      validation: {
        type: "local"
      },
      componentValueImplementationFn: function (component)
      {
        return component.getStartingDevice();
      },
      getComponentValidationMarkElement: function (component)
      {
        return component.getInputElement();
      }
    }));

    form.addFormField(new tm.jquery.FormField({
      name: "changeDate", label: null, component: this._changeDatePicker, required: true,
      validation: {
        type: "local"
      },
      componentValueImplementationFn: function (component)
      {
        return component.getDate();
      }
    }));

    form.addFormField(new tm.jquery.FormField({
      name: "changeTime", label: null, component: this._changeTimePicker, required: true,
      validation: {
        type: "local"
      },
      componentValueImplementationFn: function (component)
      {
        return component.getTime();
      }
    }));

    this._form = form;
  },

  /**
   * @returns {Date}
   * @private
   */
  _getSelectedTimestamp: function()
  {
    var administrationDate = this._changeDatePicker.getDate();
    var administrationTime = this._changeTimePicker.getTime();
    
    return new Date(
        administrationDate.getFullYear(),
        administrationDate.getMonth(),
        administrationDate.getDate(),
        administrationTime.getHours(),
        administrationTime.getMinutes(),
        0, 0);
  },

  /**
   * @returns {Date}
   * @private
   */
  _getInitialTimestamp: function()
  {
    return this.getAdministration() && this.getAdministration().administrationTime ?
        new Date(this.getAdministration().administrationTime) :
        CurrentTime.get();
  },

  processResultData: function(resultDataCallback)
  {
    var self = this;
    var failResultData = new app.views.common.AppResultData({success: false, value: null});
    var enums = app.views.medications.TherapyEnums;
    var administeredDose = {
      therapyDoseTypeEnum: enums.therapyDoseTypeEnum.RATE,
      numerator: this.getCurrentFlowRate(),
      numeratorUnit: this.getTherapy().getFlowRateUnit()
    };
    this._form.setOnValidationSuccess(function (form)
    {
      var administration = self.getAdministration() || {};

      administration.additionalAdministration = true;
      administration.administrationType = enums.administrationTypeEnum.ADJUST_INFUSION;
      administration.adjustAdministrationSubtype = enums.adjustAdministrationSubtype.OXYGEN;
      administration.comment = self._commentField.getValue();
      administration.administrationTime = self._getSelectedTimestamp();
      administration.startingDevice = self._oxygenRouteContainer.getStartingDevice();
      administration.administrationResult = enums.administrationResultEnum.GIVEN;
      administration.administeredDose = administeredDose;

      self.getView().getRestApi().confirmAdministrationTask(
          self.getTherapy(),
          administration,
          !tm.jquery.Utils.isEmpty(self.getAdministration()),
          false,
          false).then(
          function onSuccess()
          {
            resultDataCallback(new app.views.common.AppResultData({
              success: true,
              value: null
            }));
          },
          function onFailure()
          {
            resultDataCallback(failResultData);
          }
      );
    });
    this._form.setOnValidationError(function ()
    {
      resultDataCallback(failResultData);
    });

    this._form.submit();
  },



  /**
   * @returns {app.views.medications.common.dto.Therapy}
   */
  getTherapy: function()
  {
    return this.therapy;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {app.views.medications.common.dto.OxygenStartingDevice|null}
   */
  getCurrentStartingDevice: function()
  {
    return this.currentStartingDevice;
  },

  /**
   * @returns {Number|null}
   */
  getCurrentFlowRate: function()
  {
    return this.currentFlowRate;
  },

  /**
   * @returns {Object|null}
   */
  getAdministration: function()
  {
    return this.administration;
  }
});
