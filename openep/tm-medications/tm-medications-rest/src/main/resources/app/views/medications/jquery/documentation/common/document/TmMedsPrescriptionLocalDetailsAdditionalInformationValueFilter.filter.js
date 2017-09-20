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
(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.common.document')
      .filter('tmMedsPrescriptionLocalDetailsAdditionalInformationValueFilter',
          tmMedsPrescriptionLocalDetailsAdditionalInformationValueFilter);

  /**
   * Returns a filter for the display value of the additional information label - based on doNotSwitch, maxDoseExceeded
   * and illnessConditionType values from the PrescriptionLocalDetails.
   */
  function tmMedsPrescriptionLocalDetailsAdditionalInformationValueFilter(viewProxy)
  {
    return function(localDetails)
    {
      var additionalInformationArray = [];
      if (localDetails.getIllnessConditionType())
      {
        additionalInformationArray.push(viewProxy.getDictionary("IllnessConditionType."
            + localDetails.getIllnessConditionType()));
      }

      if (localDetails.isDoNotSwitch())
      {
        additionalInformationArray.push(viewProxy.getDictionary('do.not.switch'));
      }

      if (localDetails.isMaxDoseExceeded())
      {
        additionalInformationArray.push(viewProxy.getDictionary('exceed.maximum.dose'));
      }

      return additionalInformationArray.join(' - ');
    }
  }
  tmMedsPrescriptionLocalDetailsAdditionalInformationValueFilter.$inject = ['tm.angularjs.common.tmcBridge.ViewProxy'];

})();