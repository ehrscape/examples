(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.common.document')
      .filter('tmMedsEnumTranslationFilter', tmMedsEnumTranslationFilter);

  function tmMedsEnumTranslationFilter(viewProxy)
  {
    return function(value, enumPrefix)
    {
      return viewProxy.getDictionary(enumPrefix + "." + value);
    }
  }
  tmMedsEnumTranslationFilter.$inject = ['tm.angularjs.common.tmcBridge.ViewProxy'];
  
})();