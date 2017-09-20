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
var SupplyStatusDir = function ()
{
  return {
    restrict: 'E',
    templateUrl: '../ui/app/views/medications/jquery/pharmacistsTasks/angularComponents/directive/supplyStatus/supplyStatus.template.html',
    controller: ['$scope', 'SupplyRequestStatus', 'TextColorClass', 'tm.angularjs.common.tmcBridge.ViewProxy',
      function ($scope, SupplyRequestStatus, TextColorClass, viewProxy)
    {
      $scope.classes = function ()
      {
        var claz = TextColorClass.GREEN;

        if ($scope.dataItem !== SupplyRequestStatus.VERIFIED)
        {
          claz = TextColorClass.RED;
        }
        return claz;
      }

      var setTransformedLabel = function ()
      {
        if ($scope.dataItem === SupplyRequestStatus.VERIFIED)
        {
          $scope.dataItemTransformed = viewProxy.getDictionary('ClinicalNoteStatus.CONFIRMED');
        }
        else if ($scope.dataItem === SupplyRequestStatus.UNVERIFIED)
        {
          $scope.dataItemTransformed = viewProxy.getDictionary('unconfirmed');
        }
      }();
    }],
    replace: true,
    link: function (scope, ele, attrs)
    {
    }
  }
};
