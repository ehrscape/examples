/**
 * @author Matej Poklukar
 */
(function()
{
  'use strict';
  angular.module('tm.angularjs.gui.modules.pharmacistsTask', ['tm.angularjs', 'ngResource', 'ngSanitize', 'data-table', 'ui.bootstrap.datepicker'])
      .controller('ThinkGridCtrl', ThinkGridCtrl)
      .controller('SyringesListController', SyringesListController)
      .directive('resupplyForm', ResupplyFormDir)
      .directive('dueDir', DueDir)
      .directive('nextDose', NextDoseDir)
      .directive('therapyType', TherapyTypeDir)
      .directive('therapyModificationType', TherapyModificationTypeDir)
      .directive('therapyAction', TherapyActionDir)
      .directive('moreDropDownMenu', MoreDropDownMenuDir)
      .directive('openCloseFilter', OpenCloseFilterDir)
      .directive('dispenseRequested', DispenseRequestedDir)
      .directive('supplyStatus', SupplyStatusDir)
      .directive('pharmacyReview', PharmacyReviewDir)
      .directive('taskPrinted', TaskPrintedDir)
      .directive('reminderNote', ReminderNoteDir)
      .directive('dateTimeUser', DateTimeUserDir)
      .directive('dateTime', DateTimeDir)
      .directive('careProvider', CareProviderDir)
      .directive('therapyTypeCell', TherapyTypeCellDir)
      .directive('syringesList', SyringesListDir)
      .directive('syringesListRow', ['TasksOperationService', 'tm.angularjs.common.tmcBridge.ViewProxy', 'TaskTypeEnum', SyringesListRowDir])
      .directive('syringesFilterMenu', SyringesFilterMenuDir)
      .directive('syringeOverview', SyringeOverviewDir)
      .directive('syringeProgress', SyringeProgressDir)
      .filter('TaskTypeFilter', TaskTypeFilter)
      .filter('RequesterRoleFilter', RequesterRoleFilter)
      .filter('IsTodayStringFilter', IsTodayStringFilter)
      .filter('GetTimeFromDateTime', GetTimeFromDateTime)
      .filter('SupplyTypeEnumFilter', SupplyTypeEnumFilter)
      .filter('SyringeProgressTaskTypeFilter', ['TaskTypeEnum', SyringeProgressTaskTypeFilter])
      .service('TasksService', TasksService)
      .service('TasksOperationService', TasksOperationService)
      .service('ThinkGridService', ThinkGridService)
      .service('PharmacistsTaskService', PharmacistsTaskService)
      .factory('ReviewTasksResource', ReviewTasksResource)
      .factory('ResupplyTasksResource', ResupplyTasksResource)
      .factory('DispenseTasksResource', DispenseTasksResource)
      .factory('PerfusionSyringeTasksResource', PerfusionSyringeTasksResource)
      .factory('FinishedPerfusionSyringeTasksResource', FinishedPerfusionSyringeTasksResource)
      .constant('ServerSideDateFormat', 'YYYY-MM-DDTHH:mm:ss.SSSZ')
      .constant('TaskType', {
        'SUPPLY_REVIEW': 'SUPPLY_REVIEW',
        'SUPPLY_REMINDER': 'SUPPLY_REMINDER',
        'DISPENSE_MEDICATION': 'DISPENSE_MEDICATION'
      })
      .constant('SupplyRequestStatus', {
        'VERIFIED': 'VERIFIED',
        'UNVERIFIED': 'UNVERIFIED'
      })
      .constant('SupplyRequestStatusTransformed', {
        'CONFIRMED': 'CONFIRMED',
        'UNCONFIRMED': 'UNCONFIRMED'
      })
      .constant('PharmacistGridType', {
        'SUPPLY': 'SUPPLY',
        'DISPENSE': 'DISPENSE',
        'REVIEW': 'REVIEW',
        'PERFUSIONSYRINGES': 'PERFUSIONSYRINGES'
      })
      .constant('TherapyPharmacistReviewStatus', {
        'REVIEWED': 'REVIEWED',
        'REVIEWED_REFERRED_BACK': 'REVIEWED_REFERRED_BACK',
        'NOT_REVIEWED': 'NOT_REVIEWED'
      })
      .constant('TaskTypeEnum', {
        ADMINISTRATION_TASK: 'ADMINISTRATION_TASK',
        PHARMACIST_REVIEW: 'PHARMACIST_REVIEW',
        PHARMACIST_REMINDER: 'PHARMACIST_REMINDER',
        SUPPLY_REMINDER: 'SUPPLY_REMINDER',
        SUPPLY_REVIEW: 'SUPPLY_REVIEW',
        DISPENSE_MEDICATION: 'DISPENSE_MEDICATION',
        DOCTOR_REVIEW: 'DOCTOR_REVIEW',
        SWITCH_TO_ORAL: 'SWITCH_TO_ORAL',
        PERFUSION_SYRINGE_START: 'PERFUSION_SYRINGE_START',
        PERFUSION_SYRINGE_COMPLETE: 'PERFUSION_SYRINGE_COMPLETE',
        PERFUSION_SYRINGE_DISPENSE: 'PERFUSION_SYRINGE_DISPENSE'
      })
      .constant('PerfusionSyringeLabelTypeEnum', {
        SYSTEM: "SYSTEM",
        MEDICATION: "MEDICATION"
      })
      .constant('TextColorClass', {
        'GREEN': 'textColorGreen',
        'YELLOW': 'textColorYellow',
        'RED': 'textColorRed'
      });

})();

