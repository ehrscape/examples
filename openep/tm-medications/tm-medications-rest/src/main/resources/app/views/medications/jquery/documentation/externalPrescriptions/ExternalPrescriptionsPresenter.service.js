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
  angular.module('tm.angular.medications.documentation.externalPrescriptions')
      .service('tm.angular.medications.documentation.externalPrescriptions.ExternalPrescriptionsPresenter',
          ExternalPrescriptionsPresenter);

  /**
   * @constructor
   */
  function ExternalPrescriptionsPresenter(modalService, TherapyDocument)
  {
    this.showExternalPrescriptions = showExternalPrescriptions;
    this.handleViewActionCallback = handleViewActionCallback;

    /**
     * @param {Object} eventData
     */
    function handleViewActionCallback(eventData)
    {
      if (!eventData || !eventData.therapyDocuments) return;

      var therapyDocuments = eventData.therapyDocuments.map(_jsonToTherapyDocumentMapper);
      showExternalPrescriptions(therapyDocuments);
    }
    
    /***
      * @param {Array|TherapyDocument} documents
     */
    function showExternalPrescriptions(documents)
    {
      var modalInstance = modalService.openModal({
        templateUrl: '../ui/app/views/medications/jquery/documentation/externalPrescriptions/TmMedsExternalPrescriptionsDialog.template.html',
        backdrop: true,
        controllerAs: 'vm',
        controller: 'tm.angular.medications.documentation.externalPrescriptions.PrescriptionsDialogController',
        resolve: {
          documents: function getDocuments()
          {
            return documents;
          }
        }
      });

      modalInstance.result.then(function()
      {
        console.log('Modal closed.');
      });
    }

    function _jsonToTherapyDocumentMapper(jsonObject)
    {
      return TherapyDocument.fromJsonObject(jsonObject);
    }
  }

  ExternalPrescriptionsPresenter.$inject = ['tm.angularjs.common.modal.ModalService', 'tm.angular.medications.documentation.data.models.TherapyDocument'];
})();