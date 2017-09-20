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
var NurseTaskListDir = function ()
{
  var resizeTimer, nurseTaskListElement, headerElement, headerRowElement;
  var resizeTimerMillis = 500; // update the transition length in CSS if you change it, has to be smaller!
  return {
    restrict: 'E',
    templateUrl: '../ui/app/views/medications/jquery/nurseTasks/angularComponents/directive/nurseTaskList/nurseTaskList.template.html',
    controller: ['$scope', '$timeout', function ($scope, $timeout)
    {
      nurseTaskListElement = angular.element(document.querySelector('.nurse-task-list'));
      // set the initial height of the list to match the parent, since the parent doesn't use flex
      nurseTaskListElement.height(angular.element(document.querySelector('.v-nurse-task-list-view')).height() - 45);

      function resizeHeaderRow(){
        var firstRowWidth = nurseTaskListElement.find(".content .row").first().width();
        var headerRowWidth = headerRowElement.width();
        if (firstRowWidth && firstRowWidth != headerRowWidth)
        {
          var diff = headerRowWidth - firstRowWidth;
          diff = isNaN(diff) || diff < 0 ? 0 : diff;
          headerElement.css('padding-right', diff + 'px');
        }
        resizeTimer = $timeout(resizeHeaderRow, resizeTimerMillis);
      };

      if (nurseTaskListElement.length > 0) {
        headerElement = nurseTaskListElement.find('.header');
        headerRowElement = headerElement.find(".row").first();
        resizeTimer = $timeout(resizeHeaderRow, resizeTimerMillis);
      }

      $scope.$on('$destroy', function(){
        $timeout.cancel(resizeTimer);
        headerRowElement = null;
        headerElement = null;
        nurseTaskListElement = null;
        resizeTimer = null;
      });
    }],
    replace: true,
    scope: true,
    link: function (scope, ele, attrs)
    {
    }
  }
};