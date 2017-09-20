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

Class.define('app.views.medications.ordering.TherapyNextAdministrationLabelPane', 'tm.jquery.Container', {
  cls: "therapy-next-administration-label-pane TextData",
  scrollable: 'visible',
  padding: "5 0 0 0",
  height: 22,

  /** configs */
  view: null,

  /** privates */
  previousTime: null,
  nextTime: null,
  editMode: null,

  /** privates: components */


  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center", 5));
    this.setHtml('');
  },

  /** private methods */

  _showAdministrationLabel: function()
  {
    if (this.editMode)
    {
      this._showAdministrationLabelForEdit();
    }
    else
    {
      this._showAdministrationLabelForNewPrescription();
    }
  },

  _showAdministrationLabelForNewPrescription: function()
  {
    var text = "";
    var now = CurrentTime.get();
    if (this.nextTime)
    {
      var formattedDate = app.views.common.AppHelpers.getFormattedDate(this.view, this.nextTime);
      if (this.nextTime.isToday() || this.nextTime.isTomorrow() || this.nextTime.isYesterday())
      {
        formattedDate = formattedDate.toUpperCase();
      }
      formattedDate += " " + this.view.getDictionary("time.at") + " " + this.view.getDisplayableValue(new Date(this.nextTime), "short.time");
      if (this.nextTime.isTomorrow() || now.setDate(now.getDate() + 1) < this.nextTime)
      {
        formattedDate = "<span class='next-administration-warning-label'>" + formattedDate.bold() + "</span>"
      }
      text += this.view.getDictionary('first.administration') + ": " + formattedDate + " - ";

      var hoursFromNow = (this.nextTime.getTime() - CurrentTime.get().getTime()) / 1000 / 60 / 60;
      if (hoursFromNow == 0)
      {
        text += this.view.getDictionary('now').toLowerCase();
      }
      else if (hoursFromNow < 1 && hoursFromNow > 0)
      {
        text += this.view.getDictionary('in.next.hour').toLowerCase();
      }
      else if (hoursFromNow > -1 && hoursFromNow < 0)
      {
        text += this.view.getDictionary('less.than.an.hour.ago').toLowerCase();
      }
      else if (hoursFromNow < 0)
      {
        text += "<span class='next-administration-warning-label'><b>" + Math.floor(Math.abs(hoursFromNow)) + "h " + this.view.getDictionary('ago').toLowerCase() + "</b></span>";
      }
      else
      {
        if (hoursFromNow >= 6)
        {
          text += "<span class='next-administration-warning-label'><b>" + tm.jquery.Utils.formatMessage(this.view.getDictionary('hours.from.now').toLowerCase(), Math.floor(hoursFromNow)) + "</b></span>";
        }
        else
        {
          text += tm.jquery.Utils.formatMessage(this.view.getDictionary('hours.from.now').toLowerCase(), Math.floor(hoursFromNow));
        }
      }
    }
    this.setHtml(text);
  },

  _showAdministrationLabelForEdit: function()
  {
    var now = CurrentTime.get();
    var text = "";
    if (this.previousTime || this.nextTime)
    {
      text += this.view.getDictionary('administrations') + ": ";
    }
    if (this.previousTime)
    {
      var formattedDate = app.views.common.AppHelpers.getFormattedDate(this.view, this.previousTime);
      if (this.previousTime.isToday() || this.previousTime.isTomorrow() || this.previousTime.isYesterday())
      {
        formattedDate = formattedDate.toUpperCase();
      }
      formattedDate += " " + this.view.getDictionary("time.at") + " " + this.view.getDisplayableValue(new Date(this.previousTime), "short.time");
      text += this.view.getDictionary('previous.f').toLowerCase() + " " + formattedDate + ", ";
    }
    if (this.nextTime)
    {
      var formattedDate = app.views.common.AppHelpers.getFormattedDate(this.view, this.nextTime);
      if (this.nextTime.isToday() || this.nextTime.isTomorrow() || this.nextTime.isYesterday())
      {
        formattedDate = formattedDate.toUpperCase();
      }
      formattedDate += " " + this.view.getDictionary("time.at") + " " + this.view.getDisplayableValue(new Date(this.nextTime), "short.time");
      if (this.nextTime.isTomorrow() || now.setDate(now.getDate() + 1) < this.nextTime)
      {
        formattedDate = "<span class='next-administration-warning-label'>" + formattedDate.bold() + "</span>"
      }
      text += this.view.getDictionary('next.f').toLowerCase() + " " + formattedDate;
    }
    if (this.previousTime && this.nextTime)
    {
      text += " - ";
      var hoursFromNow = (this.nextTime.getTime() - this.previousTime.getTime()) / 1000 / 60 / 60;
      if (hoursFromNow < 1 && hoursFromNow > 0)
      {
        text += this.view.getDictionary('in.next.hour').toLowerCase();
      }
      else
      {
        if (hoursFromNow >= 6)
        {
          text += "<span class='next-administration-warning-label'><b>" + Math.floor(hoursFromNow) + "h" + " " + this.view.getDictionary('after.previous').toLowerCase() + "</b></span>";
        }
        else
        {
          text += Math.floor(hoursFromNow) + "h" + " " + this.view.getDictionary('after.previous').toLowerCase();
        }
      }
    }
    this.setHtml(text);
  },

  _loadPreviousAdministrationTime: function(oldTherapyCompositionUid, oldTherapyOrderName)
  {
    var self = this;
    var url = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_FIND_PREVIOUS_TASK_FOR_THERAPY;

    var params = {
      patientId: this.view.getPatientId(),
      compositionUid: oldTherapyCompositionUid,
      ehrOrderName: oldTherapyOrderName
    };

    this.view.loadViewData(url, params, null, function(previousTime)
    {
      self.previousTime = previousTime ? new Date(previousTime) : null;
      self._showAdministrationLabel();
    });
  },

  /** public methods */
  clear: function()
  {
    this.setHtml("");
  },

  setNextAdministration: function(nextAdministration)
  {
    this.nextTime = nextAdministration;
    this._showAdministrationLabel();
  },

  setOldTherapyId: function(oldTherapyCompositionUid, oldTherapyOrderName, isContinuousInfusion)
  {
    var oldTherapyDataIsSet = oldTherapyCompositionUid && oldTherapyOrderName;
    this.editMode = true;
    if (oldTherapyDataIsSet && !isContinuousInfusion)
    {
      this._loadPreviousAdministrationTime(oldTherapyCompositionUid, oldTherapyOrderName);
    }
    else if (oldTherapyDataIsSet && isContinuousInfusion)
    {
      this._showAdministrationLabel();
    }
  }
});

