/*
 * Copyright (c) 2010-2016 Marand d.o.o. (www.marand.com)
 *
 * This file is part of Think!Med Clinical Medication Management.
 *
 * Think!Med Clinical Medication Management is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Think!Med Clinical Medication Management is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Think!Med Clinical Medication Management.  If not, see <http://www.gnu.org/licenses/>.
 */
var ThinkGridCtrl = function ($scope, $rootScope, $timeout, $q, $http, TasksService, ActiveInitData, ActiveUpdateData,
                              ThinkGridService, SaveGridTypeToContext, SavePerfusionSyringeFilterStateToContext,
                              PharmacistGridType, SaveCareProviderIdsToContext, viewProxy, PharmacistsTaskService)
{
  $scope.saveGridTypeToContext = function (type)
  {
    SaveGridTypeToContext(type);
    ActiveInitData.selectedGrid = type;
    $scope.maskData.selectedGrid = type;
  };

  /*************************************************************************************************************************/
  $scope.createSupplyGrid = function ()
  {
    $scope.maskData.supply.options = ThinkGridService.getSupplyGridDefinition();
  };

  $scope.showSupplyGrid = function ()
  {
    $scope.clearDataImpl();
    $scope.saveGridTypeToContext(PharmacistGridType.SUPPLY);
    $scope.refreshSupplyGrid();
  };

  $scope.refreshSupplyGrid = function ()
  {
    viewProxy.showLoaderMask();
    delete $scope.maskData.supply['data']; // clears the no data message in the grid
    
    TasksService.getSupplyTasks(
        $scope.isCareProviderFilterEnabled() ? ActiveInitData.careProviderIds : null,
        $scope.isCareProviderFilterEnabled() ? null : ActiveUpdateData.patientIds,
        ActiveInitData.openClosedFilter === 'close',
        true)
        .then(
            function(data)
            {
              //if we click on different lists quickly we have to dismiss redundant data
              if (ActiveInitData.selectedGrid !== PharmacistGridType.SUPPLY)
              {
                return;
              }
              $scope.maskData.supply.data = data;
              if (ActiveInitData.openClosedFilter === 'open')
              {
                $scope.maskData.supply.options.columns[4].hide = false;
                $scope.maskData.supply.options.columns[5].hide = false;
                $scope.maskData.supply.options.columns[6].hide = true;
              }
              else
              {
                $scope.maskData.supply.options.columns[4].hide = true;
                $scope.maskData.supply.options.columns[5].hide = true;
                $scope.maskData.supply.options.columns[6].hide = false;
              }
            },
            function(response)
            {
              viewProxy.displayRequestErrorNotice(response);
            })
        .finally(hideLoaderMask);
  };

  /*************************************************************************************************************************/
  $scope.createDispenseGrid = function ()
  {
    $scope.maskData.dispense.options = ThinkGridService.getDispenseGridDefinition();
  };

  $scope.showDispenseGrid = function ()
  {
    $scope.clearDataImpl();
    $scope.saveGridTypeToContext(PharmacistGridType.DISPENSE);
    $scope.refreshDispenseGrid();
  };

  $scope.refreshDispenseGrid = function ()
  {
    viewProxy.showLoaderMask();
    delete $scope.maskData.dispense['data']; // clears the no data message in the grid
    
    TasksService.getDispenseTasks(
        $scope.isCareProviderFilterEnabled() ? ActiveInitData.careProviderIds : null,
        $scope.isCareProviderFilterEnabled() ? null : ActiveUpdateData.patientIds,
        ActiveInitData.openClosedFilter === 'close',
        true)
        .then(
            function(data)
            {
              //if we click on different lists quickly we have to dismiss redundant data
              if (ActiveInitData.selectedGrid !== PharmacistGridType.DISPENSE)
              {
                return;
              }
              $scope.maskData.dispense.data = data;
              if (ActiveInitData.openClosedFilter === 'close')
              {
                $scope.maskData.dispense.options.columns[5].hide = false;
                $scope.maskData.dispense.options.columns[7].hide = true;
              }
              else
              {
                $scope.maskData.dispense.options.columns[5].hide = true;
                $scope.maskData.dispense.options.columns[7].hide = false;
              }
            },
            function(response)
            {
              viewProxy.displayRequestErrorNotice(response);
            })
        .finally(hideLoaderMask);
  };
  /*************************************************************************************************************************/
  $scope.createReviewGrid = function ()
  {
    $scope.maskData.review.options = ThinkGridService.getReviewGridDefinition();
  };

  $scope.showReviewGrid = function () {
    $scope.clearDataImpl();
    $scope.saveGridTypeToContext(PharmacistGridType.REVIEW);
    $scope.refreshReviewGrid();
  };

  $scope.refreshReviewGrid = function()
  {
    viewProxy.showLoaderMask();
    delete $scope.maskData.review['data']; // clears the no data message in the grid

    TasksService.getReviewTasks(
        $scope.isCareProviderFilterEnabled() ? ActiveInitData.careProviderIds : null,
        $scope.isCareProviderFilterEnabled() ? null : ActiveUpdateData.patientIds)
        .then(
            function(data)
            {
              //if we click on different lists quickly we have to dismiss redundant data
              if (ActiveInitData.selectedGrid !== PharmacistGridType.REVIEW)
              {
                return;
              }
              $scope.maskData.review.data = data;
            },
            function(response)
            {
              viewProxy.displayRequestErrorNotice(response);
            })
        .finally(hideLoaderMask);
  };
  /*************************************************************************************************************************/

  $scope.showPerfusionSyringesGrid = function () {
    $scope.clearDataImpl();
    $scope.saveGridTypeToContext(PharmacistGridType.PERFUSIONSYRINGES);
    $scope.refreshPerfusionSyringesGrid();
  };

  $scope.refreshPerfusionSyringesGrid = function ()
  {
    viewProxy.showLoaderMask();
    delete $scope.maskData.perfusionSyringes['data']; // clears the no data message in the grid

    // checking if the perfusionSyringeFilter value was loaded from the context, in that case
    // we have to deserialize the closedTasksDate
    var closedTasksDate = ActiveInitData.perfusionSyringeFilter.closedTasksDate;
    if (closedTasksDate && !angular.isDate(closedTasksDate))
    {
      ActiveInitData.perfusionSyringeFilter.closedTasksDate = new Date(closedTasksDate);
    }

    if (ActiveInitData.perfusionSyringeFilter.showDispensed !== true)
    {
      var taskTypes = ['PERFUSION_SYRINGE_START'];
      if (ActiveInitData.perfusionSyringeFilter.showInProgress === true) taskTypes.push('PERFUSION_SYRINGE_COMPLETE');
      if (ActiveInitData.perfusionSyringeFilter.showCompleted === true) taskTypes.push('PERFUSION_SYRINGE_DISPENSE');

      TasksService.getPerfusionSyringeTasks(
          $scope.isCareProviderFilterEnabled() ? ActiveInitData.careProviderIds : null,
          $scope.isCareProviderFilterEnabled() ? null : ActiveUpdateData.patientIds,
          taskTypes)
          .then(function(data)
              {
                //if we click on different lists quickly we have to dismiss redundant data
                if (ActiveInitData.selectedGrid !== PharmacistGridType.PERFUSIONSYRINGES)
                {
                  return;
                }
                $scope.maskData.perfusionSyringes.data = data;
              },
              function(data)
              {
                viewProxy.displayRequestErrorNotice(data);
              })
          .finally(hideLoaderMask);
    }
    else
    {
      TasksService.getFinishedPerfusionSyringeTasks(
          $scope.isCareProviderFilterEnabled() ? ActiveInitData.careProviderIds : null,
          $scope.isCareProviderFilterEnabled() ? null : ActiveUpdateData.patientIds,
          ActiveInitData.perfusionSyringeFilter.closedTasksDate)
          .then(function(data)
              {
                //if we click on different lists quickly we have to dismiss redundant data
                if (ActiveInitData.selectedGrid !== PharmacistGridType.PERFUSIONSYRINGES)
                {
                  return;
                }
                $scope.maskData.perfusionSyringes.data = data;
              },
              function(data)
              {
                viewProxy.displayRequestErrorNotice(data);
              })
          .finally(hideLoaderMask);
    }
  };
  /*************************************************************************************************************************/

  $rootScope.$on('removeSupplyTask', function (event, param1)
  {
    var index = -1;
    for (var i = 0; i < $scope.maskData.supply.data.length; i++)
    {
      if ($scope.maskData.supply.data[i].id === param1)
      {
        index = i;
        break;
      }
    }
    if (index !== -1)
    {
      $scope.maskData.supply.data.splice(index, 1);
    }
  });


  $scope.showForSupplyAndDispence = function() {
      return ((ActiveInitData.selectedGrid === PharmacistGridType.SUPPLY) || (ActiveInitData.selectedGrid === PharmacistGridType.DISPENSE))
  };
  
  $scope.isCareProviderFilterEnabled = function()
  {
    return ActiveInitData.careProviderFilterEnabled === true;
  }

  $scope.updateDataImpl = function (updateData)
  {
    if (!$scope.isCareProviderFilterEnabled())
    {
      ActiveUpdateData.patientIds = angular.isArray(updateData.patientIds) ? updateData.patientIds : [];
    }
    $scope.showGridBasedOnContextType();
  };

  $scope.setInitialFilterValue = function ()
  {
    ActiveInitData.openClosedFilter = 'open';
  };

  $scope.clearDataImpl = function ()
  {
    $scope.maskData.supply.data = [];
    $scope.maskData.dispense.data = [];
    $scope.maskData.review.data = [];
    $scope.maskData.perfusionSyringes.data = [];
  };



  $scope.$on('refreshData', function ()
  {
    $scope.showGridBasedOnContextType(true);
  });

  $scope.$on('clearData', function ()
  {
    //catch clear data event from view
    //set initial values (now just filter)
    $scope.setInitialFilterValue();
    //emit clear data on root scope so directives can catch it
    $rootScope.$emit('clearData');
    $scope.clearDataImpl();
  });

  $scope.$on('updateData', function (event, updateData)
  {
    $scope.updateDataImpl(updateData);
  });

  /**
   * @param {Boolean} [refreshOnly=false]
   */
  $scope.showGridBasedOnContextType = function (refreshOnly)
  {
    if (ActiveInitData.selectedGrid === PharmacistGridType.SUPPLY && isPharmacySupplyReviewTasklistEnabled())
    {
      refreshOnly ? $scope.refreshSupplyGrid() : $scope.showSupplyGrid();
    }
    else if (ActiveInitData.selectedGrid === PharmacistGridType.DISPENSE && isPharmacyDispenseTasklistEnabled())
    {
      refreshOnly ? $scope.refreshDispenseGrid() : $scope.showDispenseGrid();
    }
    else if (ActiveInitData.selectedGrid === PharmacistGridType.REVIEW && isPharmacyPharmacistReviewTasklistEnabled())
    {
      refreshOnly ? $scope.refreshReviewGrid() : $scope.showReviewGrid();
    }
    else if (ActiveInitData.selectedGrid === PharmacistGridType.PERFUSIONSYRINGES &&
        isMedicationOnPreparationTasklistEnabled())
    {
      refreshOnly ? $scope.refreshPerfusionSyringesGrid() : $scope.showPerfusionSyringesGrid();
    }
    else if (!refreshOnly)
    {
      // find first available, if any
      activateFirstAvailableList();
    }
  };

  $scope.init = function ()
  {
    $scope.maskData = {
      supply: {},
      dispense: {},
      review : {},
      perfusionSyringes: {},
      selectedGrid : null
    };
    // set initial filter values for perfusion syringe
    if (!angular.isDefined(ActiveInitData.perfusionSyringeFilter))
    {
      ActiveInitData.perfusionSyringeFilter = {
        showInProgress: true,
        showCompleted: false,
        showDispensed: false,
        closedTasksDate: new Date()
      }
    }
    $scope.createSupplyGrid();
    $scope.createDispenseGrid();
    $scope.createReviewGrid();

    //directives callbacks
    $scope.confirm = function (selectedIds, zeroSelectedCareproviders) {
      //ce ni izbran noben potem dobimo nazaj VSE idje
      //vendar jih ne shranimo v context - saj bi v tem primeru pri naslednjem odprtju imel vse izbrane
      if(zeroSelectedCareproviders === true)
      {
        SaveCareProviderIdsToContext([]);
      }
      else
      {
        SaveCareProviderIdsToContext(selectedIds);
      }
      ActiveInitData.careProviderIds = selectedIds;
      //ce je null potem pomeni da se ni prisel update data
      if($scope.maskData.selectedGrid !== null)
      {
        $scope.clearDataImpl();
        $timeout(function () {
          $scope.showGridBasedOnContextType(true);
        },1);
      }
    };

    $scope.careproviderSelectorDataProvider = PharmacistsTaskService.careproviderSelectorDataProvider;

    $scope.cancel = function () {
    };

    $scope.getContextCareproviders = function () {
      return ActiveInitData.careProviderIds;
    };

    $scope.getContextFilters = function () {
      return ActiveInitData.openClosedFilter;
    };

    $scope.applyOpenCloseFilter = function (filterValue) {
      ActiveInitData.openClosedFilter = filterValue;
      $scope.clearDataImpl();
      $timeout(function () {
        $scope.showGridBasedOnContextType(true);
      },1);
    };


  };
  $scope.init();

  function hideLoaderMask()
  {
    viewProxy.hideLoaderMask();
  }

  /**
   * @returns {boolean}
   */
  function isPharmacySupplyReviewTasklistEnabled()
  {
    return ActiveInitData.permissions.supplyList === true;
  }
  /**
   * @returns {boolean}
   */
  function isPharmacyDispenseTasklistEnabled()
  {
    return ActiveInitData.permissions.dispenseList === true;
  }
  /**
   * @returns {boolean}
   */
  function isPharmacyPharmacistReviewTasklistEnabled()
  {
    return ActiveInitData.permissions.reviewList === true;
  }
  /**
   * @returns {boolean}
   */
  function isMedicationOnPreparationTasklistEnabled()
  {
    return ActiveInitData.permissions.preparationList === true;
  }

  function activateFirstAvailableList()
  {
    // find first available, if any
    if (isPharmacySupplyReviewTasklistEnabled())
    {
      ActiveInitData.selectedGrid = PharmacistGridType.SUPPLY;
      $scope.showSupplyGrid();
    }
    else if (isPharmacyDispenseTasklistEnabled())
    {
      ActiveInitData.selectedGrid = PharmacistGridType.DISPENSE;
      $scope.showDispenseGrid();
    }
    else if (isPharmacyPharmacistReviewTasklistEnabled())
    {
      ActiveInitData.selectedGrid = PharmacistGridType.REVIEW;
      $scope.showReviewGrid();
    }
    else if (isMedicationOnPreparationTasklistEnabled())
    {
      ActiveInitData.selectedGrid = PharmacistGridType.PERFUSIONSYRINGES;
      $scope.showPerfusionSyringesGrid();
    }
  }
};
ThinkGridCtrl.$inject = ['$scope', '$rootScope', '$timeout', '$q', '$http', 'TasksService', 'ActiveInitData',
  'ActiveUpdateData', 'ThinkGridService', 'SaveGridTypeToContext', 'SavePerfusionSyringeFilterStateToContext',
  'PharmacistGridType', 'SaveCareProviderIdsToContext', 'tm.angularjs.common.tmcBridge.ViewProxy', 'PharmacistsTaskService'];
