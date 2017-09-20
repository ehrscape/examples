(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.common.document')
      .controller('tm.angular.medications.documentation.common.document.DocumentPresenterController',
          DocumentPresenterController);

  function DocumentPresenterController()
  {
    var vm = this;
    vm.isReadOnly = isReadOnly;
    vm.getDocument = getDocument;

    function isReadOnly()
    {
      return vm._readOnly === true;
    }

    function getDocument()
    {
      return vm._document;
    }
  }
})();