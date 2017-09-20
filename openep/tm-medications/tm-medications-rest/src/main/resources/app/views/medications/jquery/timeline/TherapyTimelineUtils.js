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
        /* limits the redraw, only triggered after 50px move */
        overrideTimelinePanMove: function (timeline)
        {
          timeline.removeAllListeners('panmove');
          timeline.on('panmove', function (event)
          {
            var self = this;
            this.range.props.touch.dragging = true;

            var direction = this.range.options.direction;
            var delta = direction == 'horizontal' ? event.deltaX : event.deltaY;
            delta -= this.range.deltaDifference;

            // we ignore moving for each 25px to reduce the amount of redraws, but since fast swipes can cause
            // an overload in redraws, we also add a small timer delay, to reduce the amount of redraws when
            // velocity is high .. could probably use hammer.js's velocity in the px calculation?
            if (Math.abs(delta - this.range.previousDelta) > 25)
            {
              clearTimeout(timeline._moveTimer);
              timeline._moveTimer = setTimeout(function(){
                self.range._onDrag.call(self.range, event);
              }, 5);
            }
          });
        },
        /* zoom only works when using the ALT key */
        overrideTimelineOnMouseWheel: function (timeline)
        {
          timeline.removeAllListeners('mousewheel');
          timeline.on('mousewheel', function(event)
          {
            if (event.altKey === true)
            {
              this.range._onMouseWheel.call(this.range, event);
            }
          });
        },

        //row height is always header height
        overrideTimelineReflowItems: function (timeline)
        {
          timeline.reflowItems = function ()
          {
            var groups = this.groups;
            if (groups)
            {
              groups.forEach(function (group)
              {
                group.itemsHeight = group.labelHeight;
              });
            }
            return false;
          };
        }
      }
    }
);
