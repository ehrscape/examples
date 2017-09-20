(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.TherapyDocumentationApp')
      .controller('tm.angular.medications.documentation.TherapyDocumentationController', TherapyDocumentationController);

  /**
   * Main view controller.
   * @constructor
   */
  function TherapyDocumentationController($scope, $timeout, viewProxy, signalAngularReady, documentService, activeUpdateData,
                                          activeInitData, viewActionName, externalPrescriptionsPresenter, modalService)
  {
    var vm = this;
    vm.getDocuments = getDocuments;
    vm.moreDocumentsExist = moreDocumentsExist;
    vm.showList = showList;
    vm.loadMoreDocuments = loadMoreDocuments;
    vm.setActiveDocument = setActiveDocument;
    vm.getActiveDocument = getActiveDocument;
    vm.isActiveDocument = isActiveDocument;
    vm.clearActiveDocument = clearActiveDocument;
    vm.isEditAllowed = isEditAllowed;

    $scope.$on('refreshData', _onRefreshDataEmit);
    $scope.$on('clearData', _onClearEmit);
    $scope.$on('$destroy', _clearAllData);
    $scope.$on(viewActionName.cancelPrescription, _onCancelPrescriptionEmit);
    $scope.$on(viewActionName.outpatientPrescription, _onOutpatientPrescriptionEmit);
    $scope.$on(viewActionName.deleteOutpatientPrescription, _onDeleteOutpatientPrescription);
    $scope.$on(viewActionName.getExternalOutpatientPrescriptions, _onShowExternalPrescriptions);

    viewProxy.getLogger().info("Signaling angular's scope is ready to receive commands.");
    signalAngularReady();

    /*  Private properties */
    var _activeDocument = null;

    /**
     * Defines if the complete list of documents should be shown.
     * @returns {boolean}
     */
    function showList()
    {
      return _activeDocument === null;
    }

    /**
     * Returns the active list of documents.
     * @returns {Array.<TherapyDocument>}
     */
    function getDocuments()
    {
      return documentService.getDocumentCache();
    }

    /***
     * Returns true if more documents can be loaded from the server.
     * @returns {boolean}
     */
    function moreDocumentsExist()
    {
      return documentService.isMoreDocumentsExist();
    }

    /**
     * Sets the active document which should be shown in the single document view.
     * @param {TherapyDocument} document
     */
    function setActiveDocument(document)
    {
      _activeDocument = document;
    }

    /**
     * Returns the currently active document, that should be shown in the single document view.
     * @returns {TherapyDocument}
     */
    function getActiveDocument()
    {
      return _activeDocument;
    }

    /**
     * Returns true if the passed document is also the currently active document show in the single document
     * view.
     * @param {TherapyDocument} document
     * @returns {boolean}
     */
    function isActiveDocument(document)
    {
      return _activeDocument === document;
    }

    /**
     * Based on activeInitData's editAllowed property. Determines if edit/change operations are allowed by the user.
     */
    function isEditAllowed()
    {
      return activeInitData.editAllowed === true;
    }

    /**
     * Clears the currently active document, which in effect should switch from single dcoument view to
     * multi document view.
     */
    function clearActiveDocument()
    {
      _activeDocument = null;
    }

    /**
     * Loads the next set of documents.
     */
    function loadMoreDocuments()
    {
      documentService.loadMoreDocuments();
    }

    /* Private methods */

    /**
     * Removes all the displayed data on screen.
     */
    function _clearAllData()
    {
      modalService.closeAll();
      clearActiveDocument();
      documentService.clearDocumentCache();
    }

    /**
     * Refresh view command handler.
     * @param event
     * @param updateData
     */
    function _onRefreshDataEmit(event, updateData)
    {
      viewProxy.getLogger().debug("TherapyDocumentationApp refreshData received width data.", updateData);
      _clearAllData();
      activeUpdateData.patientId = updateData.patientId;
      loadMoreDocuments();
    }

    /* Scope emit handlers. */

    function _onClearEmit()
    {
      viewProxy.getLogger().debug("TherapyDocumentationApp clearData received.");
      // safe version of $scope.$apply()
      $timeout(function()
      {
        _clearAllData();
      });
    }

    /**
     * @param {String} event
     * @param {{compositionUid: string, prescriptionTherapyId: string, cancellationStatus: PrescriptionStatus, cancellationReason: string}} eventData
     * @private
     */
    function _onCancelPrescriptionEmit(event, eventData)
    {
      viewProxy.getLogger().debug("TherapyDocumentationApp cancel prescribed therapy action callback received.", eventData);

      if (!eventData) return;

      // safe scope apply, otherwise the change isn't propagated
      $timeout(function()
      {
        documentService.updateCachedPrescriptionTherapyStatus(eventData.compositionUid,
            eventData.prescriptionTherapyId, eventData.cancellationStatus);
      });
    }

    function _onOutpatientPrescriptionEmit(event, eventData)
    {
      viewProxy.getLogger().debug("TherapyDocumentationApp outpatient prescription action callback received.");

      if (!eventData) return;

      // safe scope apply, otherwise the change isn't propagated
      $timeout(function()
      {
        documentService.updateCachedDocumentPrescriptionPackage(eventData.eerPrescriptionPackageDto);
      });
    }

    function _onDeleteOutpatientPrescription(event, eventData)
    {
      viewProxy.getLogger().debug("TherapyDocumentationApp delete outpatient prescription action callback received.");

      if (!eventData) return;

      // safe scope apply, otherwise the change isn't propagated
      $timeout(function()
      {
        documentService.removeCachedDocumentByPrescriptionPackage(eventData.compositionUid);
        setActiveDocument(null); // make sure the document isn't currently shown by the singleDocumentColumn directive
      });
    }

    function _onShowExternalPrescriptions(event, eventData)
    {
      viewProxy.getLogger().debug("TherapyDocumentationApp get external outpatient prescriptions action callback received.");
      externalPrescriptionsPresenter.handleViewActionCallback(eventData);
    }
  }
  TherapyDocumentationController.$inject = ['$scope', '$timeout',
    'tm.angularjs.common.tmcBridge.ViewProxy', 'SignalAngularReady',
    'tm.angular.medications.documentation.data.DocumentService', 'ActiveUpdateData', 'ActiveInitData',
    'ViewActionName', 'tm.angular.medications.documentation.externalPrescriptions.ExternalPrescriptionsPresenter',
    'tm.angularjs.common.modal.ModalService'];
})();