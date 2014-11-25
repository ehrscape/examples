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

  /** configs */
  view: null,
  administration: null,
  therapyId: null,
  therapy: null,

  /** privates: components */
  administrationDateTimeCard: null,
  administrationDateField: null,
  administrationTimeField: null,
  moveAllRadioButtonGroup: null,
  moveAllRadioButton: null,
  moveSingleRadioButton: null,
  validationForm: null,
  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.setLayout(new tm.jquery.HFlexboxLayout({gap: 5}));
    this._buildComponents();
    this._buildGui();
  },

  _buildComponents: function()
  {
    var self = this;

    this.administrationDateTimeCard = new tm.jquery.Container({layout: new tm.jquery.HFlexboxLayout({gap: 5})});
    this.administrationDateField = new tm.jquery.DatePicker();
    this.administrationTimeField = new tm.jquery.TimePicker();
    this.administrationDateField.getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        function()
        {
          self.administrationDateField.setDate(new Date());
          self.administrationTimeField.setTime(new Date());
        });
    this.administrationTimeField.getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        function()
        {
          self.administrationDateField.setDate(new Date());
          self.administrationTimeField.setTime(new Date());
        });
    var administrationTime = this.administration ? new Date(this.administration.plannedTime) : new Date();
    this.administrationDateField.setDate(administrationTime);
    this.administrationTimeField.setTime(administrationTime);

    this.moveSingleRadioButton = new tm.jquery.RadioButton({labelText: this.view.getDictionary('single.f'), data: true, labelAlign: "right", checked: true});
    this.moveAllRadioButton = new tm.jquery.RadioButton({labelText: this.view.getDictionary('all.f'), data: false, labelAlign: "right"});
    this.moveAllRadioButtonGroup = new tm.jquery.RadioButtonGroup();
    this.moveAllRadioButtonGroup.add(this.moveSingleRadioButton);
    this.moveAllRadioButtonGroup.add(this.moveAllRadioButton);
  },

  _buildGui: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var mainContainer = new tm.jquery.Container({layout: new tm.jquery.VFlexboxLayout(), margin: 10});

    if(this.therapy.dosingFrequency && this.therapy.dosingFrequency.type == enums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
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
  },

  _rescheduleTasks: function()
  {
    var self = this;

    var administrationDate = this.administrationDateField.getDate();
    var administrationTime = this.administrationTimeField.getTime();
    var dueTime = new Date(
        administrationDate.getFullYear(),
        administrationDate.getMonth(),
        administrationDate.getDate(),
        administrationTime.getHours(),
        administrationTime.getMinutes(),
        0, 0);

    var params = {
      taskId: this.administration.taskId,
      fromTime: JSON.stringify(this.administration.plannedTime),
      dueTime: JSON.stringify(dueTime),
      rescheduleSingleTask: this.moveAllRadioButtonGroup.getActiveRadioButton().data,
      therapyId: this.therapyId
    };

    var administrationUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_RESCHEDULE_TASKS;
    this.view.loadPostViewData(administrationUrl, params, null,
        function()
        {
          var resultData = new app.views.common.AppResultData({success: true});
          self.resultCallback(resultData);
        },
        function()
        {
          var resultData = new app.views.common.AppResultData({success: false});
          self.resultCallback(resultData);
        },
        true);
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    this.resultCallback = resultDataCallback;
    this._rescheduleTasks();
  }
});