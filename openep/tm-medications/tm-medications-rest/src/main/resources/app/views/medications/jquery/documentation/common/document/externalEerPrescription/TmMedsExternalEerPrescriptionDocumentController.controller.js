(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.common.document')
      .controller('tm.angular.medications.documentation.common.document.ExternalEerPrescriptionDocumentController',
          ExternalEerPrescriptionDocumentController);

  function ExternalEerPrescriptionDocumentController($scope)
  {
    var vm = this;

    vm.getDocument = getDocument;
    vm.showWhitePrescriptionsSection = showWhitePrescriptionsSection;
    vm.showGreenPrescriptionsSection = showGreenPrescriptionsSection;
    vm.getWhiteDocumentTypePrescriptionTherapies = getWhiteDocumentTypePrescriptionTherapies;
    vm.getGreenDocumentTypePrescriptionTherapies = getGreenDocumentTypePrescriptionTherapies;

    $scope.$watch('vm._document.content', function()
    {
      updatePrescriptionTherapyLists();
    });

    var _documentContent = null;
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
     * @returns {TherapyDocument}
     */
    function getDocument()
    {
      return vm._document;
    }
  }

  ExternalEerPrescriptionDocumentController.$inject = ['$scope'];
})();