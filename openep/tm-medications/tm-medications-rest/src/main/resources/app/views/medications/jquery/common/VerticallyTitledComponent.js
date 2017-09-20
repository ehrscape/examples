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
Class.define('tm.views.medications.common.VerticallyTitledComponent', 'tm.jquery.Container', {
  cls: "vertically-titled-component",

  titleText: null,
  contentComponent: null,

  /** privates */
  _titleLabel: null,

  Constructor: function (config)
  {
    this.callSuper(config);
    this._buildGUI();
  },

  _buildGUI: function (config)
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));
    this._titleLabel = this._createTitleComponent();
    this.add(this._titleLabel);

    if (tm.jquery.Utils.isEmpty(this.contentComponent))
    {
      this.contentComponent = this._createContentContainer();
    }

    this.add(this.contentComponent);
  },

  getContentComponent: function ()
  {
    return this.contentComponent;
  },

  getTitleText: function ()
  {
    return tm.jquery.Utils.isEmpty(this.titleText) ? "" : this.titleText;
  },

  setTitleText: function(text)
  {
    this.titleText = text;
    this._titleLabel.setHtml(text);
  },

  _createTitleComponent: function ()
  {
    return new tm.jquery.Component({
      cls: "TextLabel title-label ellipsis",
      html: this.getTitleText()
    });
  },

  _createContentContainer: function ()
  {
    var contentContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    return contentContainer;
  }
});