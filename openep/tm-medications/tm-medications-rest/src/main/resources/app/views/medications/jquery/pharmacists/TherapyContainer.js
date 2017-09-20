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
Class.define('app.views.medications.pharmacists.TherapyContainer', 'app.views.medications.common.TherapyContainer', {
  statics: {
    forReviewView: function (config)
    {
      var container = new app.views.medications.pharmacists.TherapyContainer(config);
      var toolbar = new app.views.medications.pharmacists.TherapyContainerReviewViewToolbar({
        therapyContainer: container
      });
      container.setToolbar(toolbar);
      return container;
    },
    forViewReviewContentCard: function (config)
    {
      return new app.views.medications.pharmacists.TherapyContainer(config);
    },
    forEditReviewContentCard: function (config)
    {
      var container = new app.views.medications.pharmacists.TherapyContainer(config);
      var toolbar = new app.views.medications.pharmacists.TherapyContainerEditReviewContentToolbar({
        therapyContainer: container,
        alignSelf: "stretch"
      });
      container.setToolbar(toolbar);
      return container;
    },
    forFlowViewReviewPane: function (config, showToolbar)
    {
      var container = new app.views.medications.pharmacists.TherapyContainer(config);
      if (showToolbar == true)
      {
        var toolbar = new app.views.medications.pharmacists.TherapyContainerFlowViewReviewPaneToolbar({
          therapyContainer: container,
          alignSelf: "stretch",
          changeType: config.changeType
        });
        container.setToolbar(toolbar);
      }
      return container
    }
  },

  activeCls: null,
  checkedCls: null,

  /** constructor */
  Constructor: function (config)
  {
    this.activeCls = this.getConfigValue("activeCls", "item-active");
    this.checkedCls = this.getConfigValue("checkedCls", "item-checked");
    config.showIconTooltip = this.getConfigValue("showIconTooltip", true);
    this.callSuper(config);
  },

  _mark: function (cls, mark)
  {
    if ((mark === true || tm.jquery.Utils.isEmpty(mark)) && !this.getCls().contains(cls))
    {
      this.setCls(this.getCls() + " " + cls);
      // force apply due to a redraw bug - states not rendered, but already rendered, hence the CLS doesn't apply
      if (!tm.jquery.Utils.isEmpty(this.dom)) this.applyCls(this.getCls() + " " + cls);
    }
    else if (mark === false && this.getCls().contains(cls))
    {
      this.setCls(this.getCls().replace(" " + cls, ""));
      // force apply due to a redraw bug - states not rendered, but already rendered, hence the CLS doesn't apply
      if (!tm.jquery.Utils.isEmpty(this.dom))  this.applyCls(this.getCls().replace(" " + cls, ""));
    }
  },

  markActive: function (active)
  {
    this._mark(this.activeCls, active);
  },

  markChecked: function (checked)
  {
    this._mark(this.checkedCls, checked);
  }
});
