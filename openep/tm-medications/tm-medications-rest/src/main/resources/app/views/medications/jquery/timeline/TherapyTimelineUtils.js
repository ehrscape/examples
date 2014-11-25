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

Class.define('tm.views.medications.TherapyTimelineUtils', 'tm.jquery.Object', {

  /** statics */
  statics: {
    overrideTimelineOnMouseMove: function(timeline)
    {
      timeline.onMouseMove = function(event)
      {
        event = event || window.event;

        var params = this.eventParams,
            size = this.size,
            dom = this.dom;
        var mouseX = links.Timeline.getPageX(event);
        var diffX = mouseX - params.mouseX;

        var interval = (params.end.valueOf() - params.start.valueOf());
        var diffMillisecs = Math.round((-diffX) / size.contentWidth * interval);
        var previousLeft = params.previousLeft || 0;
        var currentLeft = parseFloat(dom.items.frame.style.left) || 0;
        var previousOffset = params.previousOffset || 0;
        var frameOffset = previousOffset + (currentLeft - previousLeft);
        var frameLeft = -diffMillisecs / interval * size.contentWidth + frameOffset;
        var move = parseFloat(dom.items.frame.style.left) || frameLeft;
        if (Math.abs(move) > 50)
        {
          links.Timeline.prototype.onMouseMove.call(timeline, event);
        }
      };
    },

    //fixes zoom step in swing webkit
    overrideTimelineOnMouseWheel: function(timeline)
    {
      timeline.onMouseWheel = function(event)
      {
        if (event.altKey === true)
        {
          if (event.wheelDelta == 4800 || event.wheelDelta == -4800) //fix for JavaFX Webkit in Swing
          {
            var zoomFactor = event.wheelDelta > 0 ? 0.2 : -0.2;
            var frameLeft = links.Timeline.getAbsoluteLeft(this.dom.content);
            var mouseX = links.Timeline.getPageX(event);
            var zoomAroundDate =
                (mouseX != undefined && frameLeft != undefined) ?
                    this.screenToTime(mouseX - frameLeft) :
                    undefined;
            this.zoom(zoomFactor, zoomAroundDate);
            this.trigger("rangechange");
            this.trigger("rangechanged");
            links.Timeline.preventDefault(event);
          }
          else
          {
            links.Timeline.prototype.onMouseWheel.call(timeline, event);
          }
        }
      };
    },

    //row height is always header height
    overrideTimelineReflowItems: function(timeline)
    {
      timeline.reflowItems = function()
      {
        var groups = this.groups;
        if (groups)
        {
          groups.forEach(function(group)
          {
            group.itemsHeight = group.labelHeight;
          });
        }
        return false;
      };
    }
  }}
);
