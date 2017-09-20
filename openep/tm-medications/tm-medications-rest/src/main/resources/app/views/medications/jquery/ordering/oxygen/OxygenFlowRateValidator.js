/*
 * Copyright (c) 2010-2015 Marand d.o.o. (www.marand.com)
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
Class.define('app.views.medications.ordering.oxygen.OxygenFlowRateValidator', 'tm.jquery.Object', {
  MIN: 0,
  MAX: 100,

  view: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @returns {Array<tm.jquery.Validator>}
   */
  getValidators: function()
  {
    var self = this;
    return [
      new tm.jquery.Validator({
        errorMessage: tm.jquery.Utils.formatMessage(
            this.getView().getDictionary("value.must.be.greater.than.and.less.than"),
            this.MIN,
            this.MAX),
        isValid: function (value)
        {
          if (value)
          {
            if (!tm.jquery.Utils.isNumeric(value))
            {
              return false;
            }

            var floatValue = parseFloat(value);
            if (floatValue < self.MIN || floatValue > self.MAX)
            {
              return false;
            }
          }

          return true;
        }
      })
    ]
  },

  /**
   * Currently only supports components that implement the getValue method.
   * @param component
   * @returns {Array<tm.jquery.FormField>}
   */
  getAsFormFieldValidators: function(component)
  {
    return [new tm.jquery.FormField({
      label: null, name: "oxygenFlowRate", component: component, required: false,
      validation: {
        type: "local",
        validators: this.getValidators()
      }
    })];
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});