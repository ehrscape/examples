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

var ThinkGridService = function (viewProxy, OpenPatientClickHandler, ActiveInitData)
{
  this.getCommonDefinition = function ()
  {
    return {
      openPatientDoubleClickHandler : function(rowData, defaultViewType) {
        if (rowData.changeType && ['MEDS_ON_ADMISSION', 'MEDS_ON_DISCHARGE'].contains(rowData.changeType))
        {
          defaultViewType = 'SUMMARY';
        }
        OpenPatientClickHandler(rowData.patientDisplayDto.id, defaultViewType);
      },
      rowHeight: 'auto',
      footerHeight: false,
      scrollbarV: false,
      emptyMessage: viewProxy.getDictionary('no.data.plural'),
      loadingMessage: "", // clearing so it's not shown, since we're using the loader mask on the view
      headerHeight: 50,
      skipRowStyle : true
    };
  };

  this.getSupplyGridDefinition = function ()
  {
    var widthsDef = {
      'tablet' : [200,250,220,100,70,70,70],
      'desktop' : [350,400,220,100,70,70,70]
    };
    var widths = ActiveInitData.isTablet ? widthsDef['tablet'] : widthsDef['desktop'];

    var definition = this.getCommonDefinition();
    definition.therapyViewType = 'TIMELINE';
    var headerHeight = 50;

    definition.columns = [
      {
        name: viewProxy.getDictionary('patient'),
        prop: 'patientDisplayDto',
        width: widths[0],
        frozenLeft: false,
        sort: false,
        template: '<tm-patient-banner dto="dataItem"></tm-patient-banner>'
      },
      {
        name: viewProxy.getDictionary('therapy'),
        prop: 'therapyDayDto',
        sort: false,
        template: '<tm-therapy-description dto="dataItem"></tm-therapy-description>',
        height: headerHeight,
        width: widths[1],
        style : !ActiveInitData.isTablet ? {} : {
          'margin-right': '10px',
          'margin-left': '10px'
        }        
      },
      {
        sort: false,
        prop: 'supplyTypeEnum',
        template: '<therapy-type></therapy-type>',
        name: viewProxy.getDictionary('type'),
        width: widths[2],
        height: headerHeight
      },
      {
        name: viewProxy.getDictionary('due'),
        prop: 'dueDate',
        sort: false,
        width: widths[3],
        template: '<due-dir date="dataItem"></due-dir>',
        height: headerHeight
      },
      {
        sortable: false,
        prop: '/',
        width: widths[4],
        name: '',
        template: '<therapy-action dataItem="dataItem" index="$index"></therapy-action>',
        height: headerHeight
      },
      {
        sortable: false,
        style: {
          overflow: 'visible'
        },
        prop: '/',
        template: '<more-drop-down-menu task="task" index="$index"></more-drop-down-menu>',
        name: '',
        width: widths[5],
        height: headerHeight
      },
      {
        name: viewProxy.getDictionary('BillingSummaryEnum.CLOSED'),
        prop: 'closedDateTime',
        sort: false,
        width: widths[6],
        template: '<due-dir date="dataItem"></due-dir>',
        height: headerHeight,
        hide : true
      }

    ];
    return definition;
  };

  this.getDispenseGridDefinition = function ()
  {
    var widthsDef = {
      'tablet' : [200,250,100,120,150,100,60,60],
      'desktop' : [350,400,100,120,150,100,70,70]
    };
    var widths = ActiveInitData.isTablet ? widthsDef['tablet'] : widthsDef['desktop'];
    var definition = this.getCommonDefinition();
    definition.therapyViewType = 'TIMELINE';
    var headerHeight = 50;
    definition.columns = [
      {
        name: viewProxy.getDictionary('patient'),
        prop: 'patientDisplayDto',
        width: widths[0],
        frozenLeft: false,
        sort: false,
        template: '<tm-patient-banner dto="dataItem"></tm-patient-banner>'
      },
      {
        name: viewProxy.getDictionary('therapy'),
        prop: 'therapyDayDto',
        sort: false,
        template: '<tm-therapy-description dto="dataItem"></tm-therapy-description>',
        width: widths[1],
        height: headerHeight,
        style : !ActiveInitData.isTablet ? {} : {
          'margin-right': '10px',
          'margin-left': '10px'
        }
      },
      {
        name: viewProxy.getDictionary('pharmacists.review'),
        prop: 'therapyDayDto',
        sort: false,
        template: '<pharmacy-review></pharmacy-review>',
        width: widths[2],
        style: {
          'text-align': 'center'
        },
        height: headerHeight
      },
      {
        name: viewProxy.getDictionary('supply.status'),
        sort: false,
        prop: 'supplyRequestStatus',
        template: '<supply-status></supply-status>',
        width: widths[3],
        height: headerHeight
      },
      {
        name: viewProxy.getDictionary('dispense.requested'),
        sort: false,
        prop: 'createdDateTime',
        template: '<dispense-requested></dispense-requested>',
        width: widths[4],
        height: headerHeight
      },
      {
        name: viewProxy.getDictionary('BillingSummaryEnum.CLOSED'),
        prop: 'closedDateTime',
        sort: false,
        width: widths[5],
        template: '<due-dir date="dataItem"></due-dir>',
        height: headerHeight,
        hide : true
      },
      {
        sortable: false,
        prop: '/',
        name: '',
        width: widths[6],
        template: '<task-printed></task-printed>',
        height: headerHeight
      },
      {
        sortable: false,
        prop: '/',
        name: '',
        width: widths[7],
        template: '<therapy-action dataItem="dataItem" index="$index"></therapy-action>',
        height: headerHeight
      }
    ];
    return definition;
  };
  this.getReviewGridDefinition = function ()
  {
    var widthsDef = {
      'tablet' : [200,100,100,100,150,100,250],
      'desktop' : [300,180,100,100,200,100,300]
    };
    var widths = ActiveInitData.isTablet ? widthsDef['tablet'] : widthsDef['desktop'];

    var definition = this.getCommonDefinition();
    definition.therapyViewType = 'PHARMACIST';
    var headerHeight = 50;
    definition.columns = [
      {
        name: viewProxy.getDictionary('patient'),
        prop: 'patientDisplayDto',
        width: widths[0],
        frozenLeft: true,
        sort: false,
        template: '<tm-patient-banner dto="dataItem"></tm-patient-banner>'
      },
      {
        name: viewProxy.getDictionary('organizational.entity'),
        prop: 'careProviderName',
        sort: false,
        width: widths[1],
        template: '<care-provider></care-provider>',
        height: headerHeight
      },
      {
        name: viewProxy.getDictionary('next.dose'),
        prop: 'firstAdministrationTimestamp',
        sort: false,
        width: widths[2],
        template: '<next-dose></next-dose>',
        height: headerHeight
      },
      {
        name: viewProxy.getDictionary('last.change'),
        width: widths[3],
        prop: 'lastEditTimestamp',
        sort: false,
        template: '<date-time-user></date-time-user>',
        height: headerHeight
      },
      {
        name: viewProxy.getDictionary('therapy.modification.type'),
        width: widths[4],
        prop: 'changeType',
        sort: false,
        template: '<therapy-modification-type></therapy-modification-type>',
        height: headerHeight
      },
      {
        name: viewProxy.getDictionary('reminder.date'),
        prop: 'reminderDate',
        sort: false,
        width: widths[5],
        template: '<due-dir date="dataItem"></due-dir>',
        height: headerHeight
      },
      {
        name: viewProxy.getDictionary('reminder.note'),
        prop: 'reminderNote',
        sort: false,
        width: widths[6],
        template: '<reminder-note></reminder-note>',
        height: headerHeight
      }
    ];
    return definition;
  };
  this.getPerfusionSyringesGridDefinition = function ()
  {
    var definition = this.getCommonDefinition();
    definition.therapyViewType = 'PHARMACIST';
    var headerHeight = 50;
    definition.columns = [
      {
        name: viewProxy.getDictionary('due'),
        prop: 'dueDate',
        sort: true,
        template: '<due-dir date="dataItem"></due-dir>',
        frozenLeft: true,
        height: headerHeight
      },
      {
        name: viewProxy.getDictionary('patient'),
        prop: 'patientDisplayDto',
        minWidth: 300,
        template: '<tm-patient-banner dto="dataItem"></tm-patient-banner>',
        height: headerHeight
      },
      {
        name: viewProxy.getDictionary('therapy'),
        prop: 'tasksList[0]/therapyDayDto',
        sort: false,
        template: '<tm-therapy-description dto="dataItem"></tm-therapy-description>',
        minWidth: 450,
        height: headerHeight
      },
      {
        name: viewProxy.getDictionary('progress'),
        sort: false,
        prop: '/',
        template: '<syringe-progress></syringe-progress>',
        minWidth: 100,
        height: headerHeight
      },
      {
        sort: false,
        prop: '/',
        template: '<syringe-overview></syringe-overview>',
        name: viewProxy.getDictionary('syringes'),
        minWidth: 200,
        height: headerHeight
      }
    ];
    return definition;
  };

};
ThinkGridService.$inject = ['tm.angularjs.common.tmcBridge.ViewProxy', 'OpenPatientClickHandler', 'ActiveInitData'];
