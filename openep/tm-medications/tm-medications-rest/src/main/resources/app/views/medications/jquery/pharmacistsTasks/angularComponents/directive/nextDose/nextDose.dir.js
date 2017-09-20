var NextDoseDir =  function ()
  {
    return {
      restrict: 'E',
      templateUrl: '../ui/app/views/medications/jquery/pharmacistsTasks/angularComponents/directive/nextDose/nextDose.template.html',
      controller:  function () {},
      replace : true,
      link: function (scope, ele, attrs)
      {
        var today = new Date();
        scope.dateIsYesterday = {};
        today.setDate(today.getDate());
        today.setHours(0, 0, 0, 0);

        var whichDay = new Date(scope.dataItem);
        whichDay.setHours(0, 0, 0, 0);

        if (whichDay.getTime() < today.getTime())
        {
          scope.dateIsYesterday = { color : 'red'};
        }
      }
    };
  };

