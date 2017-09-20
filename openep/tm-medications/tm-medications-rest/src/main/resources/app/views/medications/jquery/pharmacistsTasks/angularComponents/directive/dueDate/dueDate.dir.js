var DueDir =  function ()
  {
    return {
      restrict: 'E',
      scope: {
        date: "="
      },
      templateUrl: '../ui/app/views/medications/jquery/pharmacistsTasks/angularComponents/directive/dueDate/dueDate.template.html',
      controller:  function () {
      },
      replace : true,
      link: function (scope, ele, attrs)
      {
        if(attrs.condition)
        {
          var conditionKeys = attrs.condition.split(',');
          var key1 = conditionKeys[0];
          var key2 = conditionKeys[1];

          scope.dataItem = scope.dataItem[key1] ? scope.dataItem[key1] : scope.dataItem[key2];
        }
      }
    };
  };
