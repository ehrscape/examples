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
Class.define('app.views.medications.therapy.PharmacistReviewPaneReviewHeader', 'tm.jquery.Container', {
  cls: 'header-container',

  reviewContainer: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui(false);
  },

  ///
  /// private methods
  ///
  _buildGui: function ()
  {
    var self = this;
    var view = this.getReviewContainer().getView();
    var appFactory = view.getAppFactory();
    var data = this.getReviewContainer().getReviewData();
    var statusEnum = app.views.medications.TherapyEnums.pharmacistReviewStatusEnum;
    var statusCls = data.getPharmacistReviewStatus() == statusEnum.REVIEWED ? 'status-icon done' : 'status-icon inprogress';

    var layerContainerOptions = {
      background: {
        cls: 'document-icon'
      },
      layers: []
    };
    layerContainerOptions.layers.push({
      hpos: 'right', vpos: 'bottom', cls: statusCls,
      title: data.getPharmacistReviewStatus() == statusEnum.REVIEWED ?
          view.getDictionary('Status.DONE') : view.getDictionary('Status.IN_PROGRESS')
    });

    var statusIcon = new tm.jquery.Image({
      html: appFactory.createLayersContainerHtml(layerContainerOptions),
      width: 48,
      height: 48
    });

    var titleContainer = new tm.jquery.Container({
      cls: 'title-container',
      layout: tm.jquery.VFlexboxLayout.create('flex-start', 'stretch', 0),
      html: '<div class="PortletHeading1">' + view.getDictionary('pharmacists.review') + '</div>' +
          '<div class="TextData">' + view.getDisplayableValue(new Date(data.getCreateTimestamp()), 'short.date.time')
          + (tm.jquery.Utils.isEmpty(data.getComposer()) ? '' : ', '.concat(data.getComposer().name)) + '</div>',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
      alignSelf: 'stretch'
    });

    this.setLayout(tm.jquery.HFlexboxLayout.create('flex-start', 'center', 0));
    this.add(statusIcon);
    this.add(titleContainer);

  },

  getReviewContainer: function()
  {
    return this.reviewContainer;
  },

  refresh: function()
  {
    if (this.isRendered())
    {
      this.removeAll();
      this._buildGui();
      this.repaint();
    }
  }
});