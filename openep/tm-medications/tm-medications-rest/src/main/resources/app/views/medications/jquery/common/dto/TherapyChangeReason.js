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
Class.define('app.views.medications.common.dto.TherapyChangeReason', 'tm.jquery.Object', {
  statics: {
    fromJson: function (jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;
      if (tm.jquery.Utils.isEmpty(jsonObject.changeReason)) return null; /* if there's no enum there's no change..*/

      return new app.views.medications.common.dto.TherapyChangeReason(jsonObject);
    }
  },

  changeReason: null, /* named identity {code, name} */
  comment: null, /* string */


  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
  },

  setComment: function (value)
  {
    this.comment = value;
  },
  setReason: function (value)
  {
    this.changeReason = value;
  },

  getComment: function ()
  {
    return this.comment;
  },
  getReason: function ()
  {
    return this.changeReason;
  }
});