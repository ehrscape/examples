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
Class.define('app.views.medications.ordering.oxygen.OxygenSaturationInputContainer', 'tm.jquery.Container', {
  cls: 'oxygen-saturation-container',
  scrollable: 'visible',

  view: null,
  saturation: null,

  _customInputContainer: null,
  _minField: null,
  _maxField: null,
  _predefinedValuesButtonGroup: null,
  _toggleInputTypeButton: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  _buildGui: function()
  {
    var self = this;
    var view = this.getView();
    
    this.setLayout(tm.jquery.HFlexboxLayout.create('flex-start', 'flex-start', 0));

    var customInputComponentsContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create('flex-start', 'center', 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      hidden: true
    });

    var minField = new tm.jquery.NumberField({
      cls: 'range-start',
      width: 40,
      formatting: {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: 0}
    });

    var maxField = new tm.jquery.NumberField({
      cls: 'range-stop',
      width: 40,
      formatting: {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: 0}
    });

    customInputComponentsContainer.add(minField);
    customInputComponentsContainer.add(new tm.jquery.Component({
      cls: 'range-unit',
      html: '%',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    }));
    customInputComponentsContainer.add(new tm.jquery.Component({
      cls: 'range-dash',
      html: '-',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    }));
    customInputComponentsContainer.add(maxField);
    customInputComponentsContainer.add(new tm.jquery.Component({
      cls: 'range-unit',
      html: '%',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    }));

    var commonLowValueButton = new tm.jquery.Button({
      text: '88-92%',
      type: 'filter',
      data: app.views.medications.common.dto.Range.createStrict(
          0.88,
          0.92
      ),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });
    var commonHighValueButton = new tm.jquery.Button({
      text: '94-98%',
      type: 'filter',
      data: app.views.medications.common.dto.Range.createStrict(
          0.94,
          0.98
      ),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    var predefinedValuesButtonGroup = new tm.jquery.ButtonGroup({
      orientation: 'horizontal',
      type: 'radio',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      buttons: [commonLowValueButton, commonHighValueButton]
    });
    predefinedValuesButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component, componentEvent)
    {
      var newSelection = componentEvent.eventData && componentEvent.eventData.newSelectedButton ?
          componentEvent.eventData.newSelectedButton.data : null;
      self._onPredefinedValuesSelectionChanged(newSelection);
    });

    var toggleInputTypeButton = new tm.jquery.Button({
      cls: 'custom-input-btn',
      type: 'link',
      text: view.getDictionary('custom'),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      handler: function()
      {
        self._handleSaturationInputTypeChange();
      }
    });

    this._customInputContainer = customInputComponentsContainer;
    this._minField = minField;
    this._maxField = maxField;
    this._predefinedValuesButtonGroup = predefinedValuesButtonGroup;
    this._toggleInputTypeButton = toggleInputTypeButton;

    this.add(customInputComponentsContainer);
    this.add(predefinedValuesButtonGroup);
    this.add(toggleInputTypeButton);
  },

  _showCustomInputFields: function()
  {
    this._toggleInputTypeButton.setText(this.getView().getDictionary('default'));
    this.isRendered() ? this._predefinedValuesButtonGroup.hide() : this._predefinedValuesButtonGroup.setHidden(true);
    this._clearAllFieldValues();
    this.isRendered() ? this._customInputContainer.show() : this._customInputContainer.setHidden(false);
  },

  _showPredefinedInputFields: function()
  {
    this._toggleInputTypeButton.setText(this.getView().getDictionary('custom'));
    this.isRendered() ? this._customInputContainer.hide() : this._customInputContainer.setHidden(true);
    this._clearAllFieldValues();
    this.isRendered() ? this._predefinedValuesButtonGroup.show() : this._predefinedValuesButtonGroup.setHidden(false);
  },

  _handleSaturationInputTypeChange: function()
  {
    if (this._customInputContainer.isHidden())
    {
      this._showCustomInputFields();
    }
    else
    {
      this._showPredefinedInputFields();
    }
  },

  /**
   * @param {app.views.medications.common.dto.Range|null} selection
   * @private
   */
  _onPredefinedValuesSelectionChanged: function(selection)
  {
    if (selection)
    {
      this._minField.setValue(selection.getMin() * 100, true);
      this._maxField.setValue(selection.getMax() * 100, true);
    }
    else
    {
      this._clearCustomInputFields();
    }
  },

  _clearCustomInputFields: function()
  {
    this._minField.setValue(null, true);
    this._maxField.setValue(null, true);
  },

  _clearAllFieldValues: function()
  {
    this._predefinedValuesButtonGroup.setSelection([], true);
    this._clearCustomInputFields();
  },

  /**
   * @returns {app.views.medications.common.dto.Range|null}
   * @private
   */
  _findPreselectedButtonByValue: function(saturation)
  {
    var buttons = this._predefinedValuesButtonGroup.getButtons();
    for (var idx = 0; idx < buttons.length; idx++)
    {
      // intentionally using string compare, since toString is implemented in the Range object
      if (buttons[idx].data && buttons[idx].data.equals(saturation))
      {
        return buttons[idx];
      }
    }
    return null;
  },

  clear: function()
  {
    this._showPredefinedInputFields();
  },

  /**
   * @returns {app.views.medications.common.dto.Range}
   */
  getSaturation: function()
  {
    // round the values first due to how tm.jquery.NumberField works in case of decimal inputs
    return app.views.medications.common.dto.Range.createStrict(
        Math.round(this._minField.getValue())/100,
        Math.round(this._maxField.getValue())/100
    );
  },

  /**
   * @param {app.views.medications.common.dto.Range} saturation
   */
  setSaturation: function(saturation)
  {
    if (saturation)
    {
      var predefinedButton = this._findPreselectedButtonByValue(saturation);
      if (predefinedButton)
      {
        this._showPredefinedInputFields();
        this._predefinedValuesButtonGroup.setSelection([predefinedButton], true);
      }
      else
      {
        this._showCustomInputFields();
      }
      this._minField.setValue(saturation.getMin() * 100);
      this._maxField.setValue(saturation.getMax() * 100);
    }
    else
    {
      this._showPredefinedInputFields();
    }
  },

  /**
   * @returns {Array<tm.jquery.FormField>}
   */
  getFormFieldValidators: function()
  {
    var view = this.getView();
    var self = this;
    var validators = [];

    if (this._customInputContainer.isHidden())
    {
      validators.push(new tm.jquery.FormField({
        name: 'predefinedValuesSelection',
        component: this._predefinedValuesButtonGroup,
        required: true,
        componentValueImplementationFn: function(component)
        {
          var selection = component.getSelection();
          return selection.length > 0 ? selection[0] : null;
        }
      }));
    }
    else
    {
      validators.push(new tm.jquery.FormField({
        name: 'minSaturationField',
        component: this._minField,
        required: true,
        validation: {
          type: 'local',
          validators: [
            new tm.jquery.Validator({
              errorMessage: tm.jquery.Utils.formatMessage(
                  view.getDictionary("value.must.be.greater.than.and.less.than"),
                  0,
                  101),
              isValid: function(value)
              {
                return parseFloat(value) > 0 && parseFloat(value) <= 100;
              }
            }),
            new tm.jquery.Validator({
              errorMessage: view.getDictionary("minimum.saturation.must.be.less.than.maximum"),
              isValid: function(value)
              {
                return parseFloat(value) < parseFloat(self._maxField.getValue());
              }
            })
          ]
        }
      }));
      validators.push(new tm.jquery.FormField({
        name: 'maxSaturationField',
        component: this._maxField,
        required: true,
        validation: {
          type: 'local',
          validators: [
            new tm.jquery.Validator({
              errorMessage: tm.jquery.Utils.formatMessage(
                  view.getDictionary("value.must.be.greater.than.and.less.than"),
                  0,
                  101),
              isValid: function(value)
              {
                return parseFloat(value) > 0 && parseFloat(value) <= 100;
              }
            }),
            new tm.jquery.Validator({
              errorMessage: view.getDictionary("maximum.saturation.must.be.greater.than.minimum"),
              isValid: function(value)
              {
                return parseFloat(value) > parseFloat(self._minField.getValue());
              }
            })

          ]
        }
      }));
    }
    return validators;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});