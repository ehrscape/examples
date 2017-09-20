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
Class.define('tm.views.medications.timeline.TherapyTimelineTooltip', 'tm.jquery.Tooltip', {
  type: "tooltip",
  appendTo: "body",
  placement: "bottom",
  delay: {
    show: 1000,
    hide: 1000
  },

  /** constructor */
  Constructor: function()
  {
    this.callSuper();
    var self = this;

    this.setPlacement(function(tooltipElement, componentElement)
    {
      // return 0 if either of the elements are missing due to TMC jQuery framework test calls
      return componentElement && tooltipElement ? self.getPlacementAccordingToScreenPosition(componentElement, tooltipElement) : 0;
    });
  },

  getPlacementAccordingToScreenPosition: function(element, tooltip)
  {
    var $tooltipClone = $(tooltip).clone(false).css('visibility', 'hidden').appendTo(document.body);
    var tooltipHeight = $tooltipClone.outerHeight();
    var windowHeight = $(document.body).height();
    var $anchorElement = $(element);

    $tooltipClone.remove();

    return windowHeight > $anchorElement.offset().top + $anchorElement.height() + tooltipHeight ? "bottom" : "top";
  }
});