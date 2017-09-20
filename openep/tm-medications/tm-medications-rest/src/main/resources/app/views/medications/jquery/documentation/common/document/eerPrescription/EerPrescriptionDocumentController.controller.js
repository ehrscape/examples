(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.common.document')
      .controller('tm.angular.medications.documentation.common.document.EerPrescriptionDocumentController',
          EerPrescriptionDocumentController);

  function EerPrescriptionDocumentController($scope, $timeout, documentService)
  {
    var vm = this;
    /* Scope init */
    vm.showWhitePrescriptionsSection = showWhitePrescriptionsSection;
    vm.showGreenPrescriptionsSection = showGreenPrescriptionsSection;
    vm.getWhiteDocumentTypePrescriptionTherapies = getWhiteDocumentTypePrescriptionTherapies;
    vm.getGreenDocumentTypePrescriptionTherapies = getGreenDocumentTypePrescriptionTherapies;
    vm.cancelTherapy = cancelTherapy;
    vm.authorizeDocument = authorizeDocument;
    vm.updatePrescriptionPackage = updatePrescriptionPackage;
    vm.deleteDocument = deleteDocument;
    vm.isReadOnly = isReadOnly;
    vm.isMenuDebounceActive = isMenuDebounceActive;
    vm.removeTherapy = removeTherapy;
    vm.getDocument = getDocument;

    $scope.$watch('vm._document.content', function()
    {
      updatePrescriptionTherapyLists();
    });

    var _documentContent = null;
    var _menuDebounceActive = false;
    var _whiteDocumentTypePrescriptionTherapies = [];
    var _greenDocumentTypePrescriptionTherapies = [];

    function updatePrescriptionTherapyLists()
    {
      _documentContent = getDocument().getContent();

      if (angular.isObject(_documentContent))
      {
        _whiteDocumentTypePrescriptionTherapies = _documentContent.getPrescriptionTherapiesWithWhiteDocumentType();
        _greenDocumentTypePrescriptionTherapies = _documentContent.getPrescriptionTherapiesWithGreenDocumentType();
      }
      else
      {
        _whiteDocumentTypePrescriptionTherapies.length = 0;
        _greenDocumentTypePrescriptionTherapies.length = 0;
      }
    }

    /**
     * Returns true if the document contains prescriptions on a white document type.
     * @returns {boolean}
     */
    function showWhitePrescriptionsSection()
    {
      return _whiteDocumentTypePrescriptionTherapies.length > 0;
    }

    /**
     * Returns true if the document contains prescriptions on a white document type.
     * @returns {boolean}
     */
    function showGreenPrescriptionsSection()
    {
      return _greenDocumentTypePrescriptionTherapies.length > 0;
    }

    /**
     * Returns an array of prescriptions based on the white document type, if any exist.
     * @returns {Array}
     */
    function getWhiteDocumentTypePrescriptionTherapies()
    {
      return _whiteDocumentTypePrescriptionTherapies;
    }

    /**
     * Returns an array of prescriptions based on the green document type, if any exist.
     * @returns {Array}
     */
    function getGreenDocumentTypePrescriptionTherapies()
    {
      return _greenDocumentTypePrescriptionTherapies;
    }

    /**
     * Cancel a prescribed therapy on the specified document.
     * @param {TherapyDocument} document
     * @param {PrescriptionTherapy} prescriptionTherapy
     */
    function cancelTherapy(document, prescriptionTherapy)
    {
      documentService.cancelPrescriptionTherapy(document, prescriptionTherapy);
    }

    /**
     * @param {TherapyDocument} document
     */
    function authorizeDocument(document)
    {
      _debounceMenu();
      documentService.authorizeDocument(document);
    }

    /**
     * @param {TherapyDocument} document
     */
    function updatePrescriptionPackage(document)
    {
      _debounceMenu();
      documentService.updatePrescriptionPackage(document);
    }

    /**
     * @param {TherapyDocument} document
     */
    function deleteDocument(document)
    {
      _debounceMenu();
      documentService.deleteDocument(document);
    }

    /**
     * @returns {boolean}
     */
    function isMenuDebounceActive()
    {
      return _menuDebounceActive;
    }

    /**
     * Returns true if the content is meant to be read only. No editable actions should be shown in that case. Based
     * on the read-only attribute.
     * @returns {boolean}
     */
    function isReadOnly()
    {
      return vm._readOnly === true;
    }

    /**
     * @returns {TherapyDocument}
     */
    function getDocument()
    {
      return vm._document;
    }

    /**
     * Remove the desired prescription therapy from the specified document.
     * @param {TherapyDocument} document
     * @param {PrescriptionTherapy} prescriptionTherapy
     */
    function removeTherapy(document, prescriptionTherapy)
    {
      documentService.removePrescriptionTherapy(document, prescriptionTherapy);
    }

    /**
     * Temporarily disables the main menu and enables it again after 500 ms.
     * @private
     */
    function _debounceMenu()
    {
      _menuDebounceActive = true;
      $timeout(function()
      {
        _menuDebounceActive = false;
      }, 500);
    }
  }
  EerPrescriptionDocumentController.$inject = ['$scope', '$timeout', 'tm.angular.medications.documentation.data.DocumentService'];
})();