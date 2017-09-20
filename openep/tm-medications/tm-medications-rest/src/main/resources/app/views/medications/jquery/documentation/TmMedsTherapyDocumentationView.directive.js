(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.TherapyDocumentationApp')
      .directive('tmMedsTherapyDocumentationView', tmMedsTherapyDocumentationView);

  function tmMedsTherapyDocumentationView()
  {
    return {
      restrict: 'E',
      scope: false,
      controller: 'tm.angular.medications.documentation.TherapyDocumentationController',
      controllerAs: 'vm',
      replace: true,
      templateUrl: '../ui/app/views/medications/jquery/documentation/TmMedsTherapyDocumentationView.template.html'
    };
  }
})();