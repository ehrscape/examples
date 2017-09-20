(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.common.document')
      .controller('tm.angular.medications.documentation.common.document.MentalHealthDocumentController',
          MentalHealthDocumentController);

  function MentalHealthDocumentController()
  {
    var vm = this;

    vm.getDocument = getDocument;

    /**
     * @returns {TherapyDocument}
     */
    function getDocument()
    {
      return vm._document;
    }
  }
})();