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

Class.define('app.views.medications.ordering.ApplicationPreconditionPane', 'app.views.common.containers.AppDataEntryContainer', {

  cls: 'therapy-preconditions-pane',
  /** configs */
  view: null,
  applicationPrecondition: null,  //optional

  /** components **/
  conditionsButtonGroup: null,

  /** privates*/

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    this._buildComponents();
    this._buildGui();
    if (this.applicationPrecondition)
    {
      this._setCondition(this.applicationPrecondition);
    }
  },

  /** private methods */
  _buildComponents: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var noSelectionButton = new tm.jquery.RadioButton({
      cls: "no-precondition-button",
      labelText: this.view.getDictionary('no.medication.start.criterion'),
      data: null,
      labelAlign: 'right',
      checked: true
    });
    var beforeMealButton = new tm.jquery.RadioButton({
      cls: "before-meal-button",
      labelText: this.view.getDictionary('before.meal'),
      data: enums.medicationAdditionalInstructionEnum.BEFORE_MEAL,
      labelAlign: 'right'
    });
    var afterMealButton = new tm.jquery.RadioButton({
      cls: "after-meal-button",
      labelText: this.view.getDictionary('after.meal'),
      data: enums.medicationAdditionalInstructionEnum.AFTER_MEAL,
      labelAlign: 'right'
    });
    this.conditionsButtonGroup = new tm.jquery.RadioButtonGroup({groupName: 'vertical-radio-button-group', cls: 'conditions-button-group'});
    this.conditionsButtonGroup.add(noSelectionButton);
    this.conditionsButtonGroup.add(beforeMealButton);
    this.conditionsButtonGroup.add(afterMealButton);
  },
  _buildGui: function()
  {
    var appFactory = this.view.getAppFactory();
    this.add(appFactory.createVRadioButtonGroupContainer(this.conditionsButtonGroup, 0));
  },
  _setCondition: function(applicationPrecondition)
  {
    for (var i = 0; i < this.conditionsButtonGroup.getRadioButtons().length; i++)
    {
      var button = this.conditionsButtonGroup.getRadioButtons()[i];
      if (applicationPrecondition == button.data)
      {
        this.conditionsButtonGroup.setActiveRadioButton(button);
      }
    }
  },

  /** public methods */

  processResultData: function(resultDataCallback)
  {
    var selectedButton = this.conditionsButtonGroup.getActiveRadioButton();
    resultDataCallback(new app.views.common.AppResultData({success: true, selection: selectedButton.data}));
  }
});
