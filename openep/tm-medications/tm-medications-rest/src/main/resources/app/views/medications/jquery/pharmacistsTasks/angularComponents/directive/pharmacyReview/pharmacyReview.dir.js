var PharmacyReviewDir =  function ()
  {
    return {
      restrict: 'E',
      templateUrl: '../ui/app/views/medications/jquery/pharmacistsTasks/angularComponents/directive/pharmacyReview/pharmacyReview.template.html',
      controller: ['TherapyPharmacistReviewStatus','TextColorClass', '$scope', function (TherapyPharmacistReviewStatus, TextColorClass, $scope) {

        var setTherapyPharmacistReviewStatusIcon = function ()
        {
          if($scope.dataItem === null)
          {
            $scope.therapyPharmacistReviewStatusIcon = 'pharmacyYellow_status_16.png';
            return;
          }
          if($scope.dataItem.therapyPharmacistReviewStatus === TherapyPharmacistReviewStatus.REVIEWED)
          {
            $scope.therapyPharmacistReviewStatusIcon = 'pharmacyGreen_status_16.png';
          }
          else if($scope.dataItem.therapyPharmacistReviewStatus === TherapyPharmacistReviewStatus.REVIEWED_REFERRED_BACK)
          {
            $scope.therapyPharmacistReviewStatusIcon = 'pharmacyRed_status_16.png';
          }
          else
          {
            $scope.therapyPharmacistReviewStatusIcon = 'pharmacyYellow_status_16.png';
          }
        }();
      }],
      replace : true,
      link: function (scope, ele, attrs)
      {
        return {
          post: function (scope, iElement, iAttrs, controller)
          {
          }
        }
      }
    };
  };
