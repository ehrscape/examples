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
Class.define('app.views.medications.common.dto.AdditionalWarnings', 'tm.jquery.Object', {
  statics: {
    fromJson: function (jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var config = jQuery.extend(true, {}, jsonObject);

      config.warnings.forEach(function(warning)
      {
        warning.therapy = app.views.medications.common.dto.Therapy.fromJson(warning.therapy);
      });
      return new app.views.medications.common.dto.AdditionalWarnings(config);
    }
  },
  taskIds: null,


  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @returns {array}
   */
  getTaskIds: function()
  {
    return this.taskIds;
  },

  /**
   * @returns {array}
   */
  getWarnings: function()
  {
    return this.warnings;
  },

  /**
   * @returns {boolean}
   */
  hasAdditionalWarnings: function()
  {
    return this.warnings.length > 0;
  },

  /**
   * @returns {boolean}
   */
  hasTaskIds: function()
  {
    return this.taskIds.length > 0;
  }
});