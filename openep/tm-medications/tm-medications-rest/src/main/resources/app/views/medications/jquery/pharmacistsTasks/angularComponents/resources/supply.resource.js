/**
 * Created by matejp on 26.8.2015.
 */

var ResupplyTasksResource = function ($resource)
{
  return $resource('medications/getPharmacistResupplyTasks/?careProviderIds=:careProviderIds&patientIds=:patientIds',
      {careProviderIds: '@careProviderIds', patientIds: '@patientIds'},
      {
        get: {method: 'GET', isArray: true}
      }
  );
};

ResupplyTasksResource.$inject = ['$resource'];

var DispenseTasksResource = function ($resource)
{
  return $resource('medications/getPharmacistDispenseMedicationTasks/?careProviderIds=:careProviderIds&patientIds=:patientIds',
      {careProviderIds: '@careProviderIds', patientIds: '@patientIds'},
      {
        get: {method: 'GET', isArray: true}
      }
  );
};

DispenseTasksResource.$inject = ['$resource'];

var ReviewTasksResource = function ($resource)
{
  return $resource('medications/getPharmacistReviewTasks/?careProviderIds=:careProviderIds&patientIds=:patientIds',
      {careProviderIds: '@careProviderIds', patientIds: '@patientIds'},
      {
        get: {method: 'GET', isArray: true}
      }
  );
};

ReviewTasksResource.$inject = ['$resource'];

var PerfusionSyringeTasksResource = function($resource)
{
  return $resource('medications/findPerfusionSyringePreparationRequests/?careProviderIds=:careProviderIds&patientIds=:patientIds&taskTypes=:taskTypes',
      {careProviderIds: '@careProviderIds', patientIds: '@patientIds', taskTypes: '@taskTypes'},
      {
        get: {method: 'GET', isArray: true}
      }
  );
};
PerfusionSyringeTasksResource.$inject = ['$resource'];

var FinishedPerfusionSyringeTasksResource = function($resource)
{
  return $resource('medications/findFinishedPerfusionSyringePreparationRequests/?careProviderIds=:careProviderIds&patientIds=:patientIds&date=:date',
      {careProviderIds: '@careProviderIds', patientIds: '@patientIds', date: '@date'},
      {
        get: {method: 'GET', isArray: true}
      }
  );
};
FinishedPerfusionSyringeTasksResource.$inject = ['$resource'];
