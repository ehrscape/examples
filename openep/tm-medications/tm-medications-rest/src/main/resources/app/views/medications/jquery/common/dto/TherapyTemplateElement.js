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
Class.define('app.views.medications.common.dto.TherapyTemplateElement', 'tm.jquery.Object', {
  statics: {
    fromJson: function (jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      return new app.views.medications.common.dto.TherapyTemplateElement({
        therapy: app.views.medications.common.TherapyJsonConverter.convert(jsonObject.therapy),
        completed: jsonObject.completed
      });
    }
  },

  therapy: null, /* app.views.medications.common.dto.Therapy */
  completed: null,

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
  },

  getTherapy: function()
  {
    return this.therapy;
  },
  isCompleted: function()
  {
    return this.completed === true;
  },

  setTherapy: function(value)
  {
    this.therapy = value;
  },
  setCompleted: function(value)
  {
    this.completed = value;
  }
});