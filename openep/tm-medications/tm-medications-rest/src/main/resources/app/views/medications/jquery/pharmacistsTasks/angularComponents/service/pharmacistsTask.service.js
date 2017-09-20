/**
 * @author Matej Poklukar
 */

var PharmacistsTaskService = function ($q, $http, RequestTransformer, $uibPosition)
{

  var pharmacistsTaskService = this;
  pharmacistsTaskService.getElementTopLeft = getElementTopLeft;
  pharmacistsTaskService.getViewOffset = getViewOffset;
  pharmacistsTaskService.calculateTopResupplyFormTopPosition = calculateTopResupplyFormTopPosition;
  pharmacistsTaskService.careproviderSelectorDataProvider = careproviderSelectorDataProvider;

   /**
   * Calculates top left position of the element
   * @param  $event
   * @param  leftOffset
   * @returns {object} with top and left property
   */
  function getElementTopLeft($event, leftOffset, extraTopMargin)
  {
    var elementPosition = $uibPosition.position($event.target);
    var posLeft = elementPosition.left - leftOffset;
    var posTop = elementPosition.top + elementPosition.height + extraTopMargin;
    return {
      top: posTop + 'px',
      left: posLeft + 'px'
    };
  }

  /**
   * Calculates distance to viewport parameters
   * @param  element
   * @returns {object} with width, height, top, left properties
   */
  function getViewOffset(element)
  {
    return $uibPosition.offset(element);
  }

  /**
   * Calculates distance to viewport parameters
   * @param  thinkGridController controller element
   * @param  viewOffset calculated
   * @param  calculatedResupplyFormPosition
   * @returns {object} with width, height, top, left properties
   */
  function calculateTopResupplyFormTopPosition(targetElement, calculatedResupplyFormPosition)
  {
    delete calculatedResupplyFormPosition['left'];
    var targetElementOffset = this.getViewOffset(targetElement); //get target offset inside view
    var thinkGridController = document.querySelectorAll('[data-ng-controller="ThinkGridCtrl"]')[0];
    var controllerOffset= this.getViewOffset(thinkGridController); //get controller offset inside view
    var targetTop = targetElementOffset.top - controllerOffset.top; //subtract actuall target top inside controller
    var overflow =  targetTop + 210 + 20; //calculate bottom offset
    var controllerHeight = thinkGridController.clientHeight;

    if(overflow > controllerHeight)
    {
      calculatedResupplyFormPosition.top = controllerHeight-overflow + parseInt(calculatedResupplyFormPosition.top.substring(0, calculatedResupplyFormPosition.top.indexOf('px'))) + 'px'
    }
    else
    {
      // no special positioning - delete top property
      delete calculatedResupplyFormPosition['top'];
    }
    return calculatedResupplyFormPosition;
  }

  function careproviderSelectorDataProvider ()
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
PharmacistsTaskService.$inject = ['$q', '$http', 'tm.angularjs.common.rest.RequestTransformerService', '$uibPosition'];
