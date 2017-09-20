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
      .directive('tmMedsEerPrescriptionDocument', tmMedsEerPrescriptionDocument);

  function tmMedsEerPrescriptionDocument()
  {
    return {
      restrict: 'E',
      scope: {
        document: '=',
        readOnly: '='
      },
      controller: 'tm.angular.medications.documentation.common.document.EerPrescriptionDocumentController',
      controllerAs: 'vm',
      bindToController: {
        _document: '=document',
        _readOnly: '=readOnly'
      },
      replace: true,
      templateUrl: '../ui/app/views/medications/jquery/documentation/common/document/eerPrescription/TmMedsEerPrescriptionDocument.template.html'
    };
  }
})();