var TherapyModificationTypeDir =  function ()
  {
    return {
      restrict: 'E',
      templateUrl: '../ui/app/views/medications/jquery/pharmacistsTasks/angularComponents/directive/therapyModificationType/therapyModificationType.template.html',
      controller:  ['tm.angularjs.common.tmcBridge.ViewProxy', '$scope', function (viewProxy, $scope) {

        if($scope.dataItem === 'NEW_ADMISSION_PRESCRIPTION')
        {
          $scope.therapyModificationTypeTranslation = viewProxy.getDictionary('new.admission.prescription');
        }
        else if($scope.dataItem === 'ADDITION_TO_EXISTING_PRESCRIPTION')
        {
          $scope.therapyModificationTypeTranslation = viewProxy.getDictionary('addition.to.existing.prescription');
        }
      }],
      replace : true,
      link: function (scope, ele, attrs)
      {

      }
    };
  };