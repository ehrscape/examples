(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.common.document')
      .controller('tm.angular.medications.documentation.common.document.ExternalEerPrescriptionDocumentSectionController',
          ExternalEerPrescriptionDocumentSectionController);

  function ExternalEerPrescriptionDocumentSectionController()
  {

    var vm = this;

    vm.getSectionTitle = getSectionTitle;
    vm.getSectionTherapies = getSectionTherapies;
    vm.getPrescriptionDate = getPrescriptionDate;

    /**
     * @returns {string}
     */
    function getSectionTitle()
    {
      return vm._sectionTitle;
    }

    /**
     * @returns {Array.<PrescriptionTherapy>}
     */
    function getSectionTherapies()
    {
      return vm._sectionTherapies ? vm._sectionTherapies : [];
    }

    /**
     * @returns {String}
     */
    function getPrescriptionDate()
    {
      return vm._prescriptionDate;
    }
  }

  ExternalEerPrescriptionDocumentSectionController.$inject = [];
})();