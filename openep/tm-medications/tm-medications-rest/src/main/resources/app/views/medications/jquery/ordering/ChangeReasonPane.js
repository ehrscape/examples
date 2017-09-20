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

Class.define('app.views.medications.ordering.ChangeReasonPane', 'tm.jquery.Container', {
  cls: "change-reason-pane",
  scrollable: "visible",
  /** configs */
  view: null,
  /** privates: components */
  commentField: null,
  reasonCombo: null,

  reasonOptions: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.reasonOptions = tm.jquery.Utils.isEmpty(this.reasonOptions) ? [] : this.reasonOptions;
    this._buildGui();
  },

  /** private methods */
  _buildGui: function()
  {
    var self = this;
    var view = this.getView();

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 5));

    var commentComponent = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary('change.commentary'),
      contentComponent: new tm.jquery.TextField({
        cls: "comment-field",
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "100%")
      }),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "50%")
    });

    var reasonComponent = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary('change.reason'),
      scrollable: "visible",
      contentComponent: new tm.jquery.SelectBox({
        cls: "indication-combo",
        placeholder: view.getDictionary("select"),
        options: this._buildReasonOptions(this.reasonOptions),
        liveSearch: false,
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
        dropdownWidth: "stretch",
        defaultValueCompareToFunction: function(value1, value2)
        {
          return (tm.jquery.Utils.isEmpty(value1) ? null : value1.code)
              === (tm.jquery.Utils.isEmpty(value2) ? null : value2.code);
        },
        defaultTextProvider: function(selectBox, index, option)
        {
          return option.getValue().name;
        }
      }),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "50%")
    });
    var selectBox = reasonComponent.getContentComponent();

    this.add(reasonComponent);
    this.add(commentComponent);

    this.reasonCombo = selectBox;
    this.commentField = commentComponent.getContentComponent();
  },

  _buildReasonOptions: function(values)
  {
    values = tm.jquery.Utils.isEmpty(values) ? [] : values;

    return values.map(function(element){
      return tm.jquery.SelectBox.createOption(element, null);
    });
  },

  /** public methods */
  getView: function()
  {
    return this.view;
  },
  getComment: function()
  {
    return this.commentField.getValue() ? this.commentField.getValue() : null;
  },

  setComment: function(comment)
  {
    this.commentField.setValue(comment);
  },

  setReasonOptions: function(values)
  {
    // since values is usually a property of the main view, this instance check will suffice to skip
    // unnecessary reloading of the dom
    if (this.reasonOptions != values)
    {
      this.reasonOptions = values;
      this.reasonCombo.removeAllOptions();
      this.reasonCombo.addOptions(this._buildReasonOptions(this.reasonOptions));
    }
  },

  getReasonValue: function ()
  {
    return this.reasonCombo.getSelections().length > 0 ? this.reasonCombo.getSelections()[0] : null;
  },

  clear: function ()
  {
    this.commentField.setValue(null);
    this.reasonCombo.setSelections([], true);
  },

  getChangeReasonValidations: function ()
  {
    var self = this;
    var formFields = [];
    formFields.push(new tm.jquery.FormField({
      name: "reasonSelectBox", label: null, component: this.reasonCombo, required: true,
      validation: {
        type: "local"
      },
      componentValueImplementationFn: function (component)
      {
        return component.hasSelections() ? component.getSelections() : null;
      },
      getComponentValidationMarkElement: function (component)
      {
        return component.getButtonElement();
      }
    }));
    formFields.push(new tm.jquery.FormField({
      name: "reasonComment",
      component: self.commentField,
      required: true,
      componentValueImplementationFn: function (component)
      {
        return component.getValue();
      }
    }));
    return formFields;
  },

  requestFocus: function()
  {
    this.commentField.getInputElement().focus();
  },

  clear: function()
  {
    this.reasonCombo.setSelections([]);
    this.commentField.setValue(null);
  }
});

