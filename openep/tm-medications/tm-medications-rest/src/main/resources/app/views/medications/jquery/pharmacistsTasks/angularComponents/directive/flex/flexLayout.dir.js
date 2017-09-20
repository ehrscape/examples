/*
 * Copyright (c) 2010-2016 Marand d.o.o. (www.marand.com)
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
var FlexLayoutDir = function(FlexLayoutService)
{
  return {
    restrict: 'A',
    controller: ['$scope', function($scope)
    {
    }],
    link: function(scope, ele, attrs)
    {
      // based on tm.jquery.AbstractFlexboxLayout
      var layoutCls = "";

      var flexLayout = FlexLayoutService.getFirstValidValue(attrs.flexLayout, 'row',
          ['row', 'column', 'row-reverse', 'column-reverse']);

      var flexWrap = FlexLayoutService.getFirstValidValue(attrs.flexWrap, 'nowrap', ['nowrap', 'wrap', 'wrap-reverse']);

      var justifyContent = FlexLayoutService.getFirstValidValue(attrs.justifyContent, 'flex-start',
          ['flex-start', 'flex-end', 'center', 'space-between', 'space-around']);

      var alignItems = FlexLayoutService.getFirstValidValue(attrs.alignItems, 'center',
          ['flex-start', 'flex-end', 'center', 'baseline', 'stretch']);

      var layoutCls = "tm-flexboxlayout" + " ";
      // flex-flow //
      layoutCls += "direction-" + FlexLayoutService.getNameForFlexDirection(flexLayout) + "-" + flexWrap + " ";
      // justify-content //
      layoutCls += "justify-content-" + justifyContent + " ";
      // align-items //
      layoutCls += "align-items-" + alignItems + " ";
      // align-content (hardcoded) //
      layoutCls += "align-content-center";
      ele.addClass(layoutCls);
    }
  }
};
