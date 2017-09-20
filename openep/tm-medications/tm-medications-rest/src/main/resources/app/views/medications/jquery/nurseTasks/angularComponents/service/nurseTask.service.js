/**
 * @author Matej Poklukar
 */

var NurseTaskService = function ($q, $http, RequestTransformer)
{
  this.careproviderSelectorDataProvider = function ()
  {
    var deferred = $q.defer();
    $http({
      method: "get",
      url: 'medications/getCurrentUserCareProviders',
      transformRequest: RequestTransformer.getRequestAsFormPostTransformer()
    })
        .success(
            function (response)
            {
              for (var i = 0; i < response.length; i++)
              {
                response[i].title = response[i].name;
                delete response[i].name;
              }
              deferred.resolve(response);
            }
        ).error(
        function (response)
        {
          deferred.reject(response);
        });
    return deferred.promise;
  };
};
NurseTaskService.$inject = ['$q', '$http', 'tm.angularjs.common.rest.RequestTransformerService'];