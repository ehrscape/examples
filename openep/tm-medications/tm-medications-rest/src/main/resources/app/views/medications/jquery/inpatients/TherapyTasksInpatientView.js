/*
 * Copyright (c) 2010-2014 Marand d.o.o. (www.marand.com)
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

Class.define('app.views.medications.inpatient.TherapyTasksInpatientView', 'app.views.common.AppView', {
  cls: "v-therapy-tasks-inpatient-view",
  flex: 1,

  /** privates */
  _activeUpdateData: null,

  /** privates: components */
  _therapiesList: null,

  /** statics */
  statics: {
    SERVLET_PATH_GET_THERAPIES_DESCRIPTIONS_MAP: '/getTherapiesFormattedDescriptionsMap'
  },

  /** constructor */
  Constructor: function(config)
  {
    config = tm.jquery.Utils.applyIf({
      layout: new tm.jquery.VFlexboxLayout.create("flex-start", "stretch")
    }, config);
    this.callSuper(config);

    this._buildGui();
  },

  onViewCommand: function(command)
  {
    if (command.hasOwnProperty('update'))
    {
      this._activeUpdateData = command.update.data;
      this._presentData();
    }
    else if (command.hasOwnProperty('clear'))
    {
      this._therapiesList.clearListData();
    }
    else if (command.hasOwnProperty('refresh'))
    {
      this._presentData();
    }
  },
  _buildGui: function()
  {
    var self = this;
    this._therapiesList = new tm.jquery.List({
      autoLoad: true,
      dataSource: [],
      flex: 1,
      itemTpl: function(index, item)
      {
        return self._buildRow(item);
      },
      paging: false,
      selectable: false,
      multiSelect: false,
      allowSingleDeselect: false
    });
    this.add(this._therapiesList);
  },

  _presentData: function()
  {
    var self = this;
    var tasks = this._activeUpdateData.data;

    var therapiesIds = [];
    tasks.forEach(function(task)
    {
      therapiesIds.push(task.therapyId);
    });

    var params = {
      patientId: this._activeUpdateData.patientId,
      therapiesIds: JSON.stringify(therapiesIds)
    };
    var url =
        this.getViewModuleUrl() +
        app.views.medications.inpatient.TherapyTasksInpatientView.SERVLET_PATH_GET_THERAPIES_DESCRIPTIONS_MAP;

    this.loadViewData(url, params, null, function(therapiesMap)
    {
      if (tasks == self._activeUpdateData.data)
      {
        //key is therapyId and value is therapy formatted description
        var listData = [];
        tasks.forEach(function(task)
        {
          var therapyDescription = therapiesMap[task.therapyId];
          listData.push({
            therapyDescription: therapyDescription,
            plannedDose: task.plannedDose,
            plannedAdministrationTime: task.plannedAdministrationTime
          });
        });
        self._therapiesList.setListData(listData);
      }
    });
  },

  _buildRow: function(rowData)
  {
    var rowContainer = new tm.jquery.Container({height: 54});
    rowContainer.setLayout(tm.jquery.HFlexboxLayout.create("start", "stretch", 10));
    rowContainer.setHeight('auto');

    var html = "";
    html += '<div>';
    html += rowData.therapyDescription;
    html += '<br>';
    html += "<span class='TextLabel'>" + this.getDictionary('administration') + " </span>";
    if (rowData.plannedDose)
    {
      html += "<span class='TextData'>" + rowData.plannedDose + " </span>";
    }
    html += "<span class='TextLabel'>" + this.getDictionary('time.at') + " </span>";
    html += "<span class='TextData'>" + this.getDisplayableValue(new Date(rowData.plannedAdministrationTime), "short.date.time") + "</span>";
    html += '</div>';

    var contentContainer = new tm.jquery.Container({flex: 1, html: html});

    var iconContainer = new tm.jquery.Container({
      width: 48, height: 48,
      cls: "icon-pills"
    });

    rowContainer.add(iconContainer);
    rowContainer.add(contentContainer);

    return rowContainer;
  },

  onViewInit: function()
  {
    this.callSuper();

    if (this.isDevelopmentMode())
    {
      //var updateDataCommand = {"update": {"data": {"patientId": 8555303, "tasks": [{"taskId":"20108573","therapyId":"55cedee4-79de-4e91-9456-68601e3cda4f|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448280000000},{"taskId":"20190127","therapyId":"89bd5616-0228-463e-b0d2-edc8431f5977|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448305200000},{"taskId":"20108589","therapyId":"55cedee4-79de-4e91-9456-68601e3cda4f|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448305200000},{"taskId":"20190143","therapyId":"89bd5616-0228-463e-b0d2-edc8431f5977|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448348400000},{"taskId":"20150847","therapyId":"ba5dedd1-98fa-4b17-8dea-c7311b6776fe|Medication instruction","plannedDose":"2.5 mg","plannedAdministrationTime":1448348400000},{"taskId":"20144239","therapyId":"21d6d12e-2db1-4856-a29e-8e8bff8767e5|Medication instruction","plannedDose":"20 mg","plannedAdministrationTime":1448348400000},{"taskId":"20108605","therapyId":"55cedee4-79de-4e91-9456-68601e3cda4f|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448348400000},{"taskId":"20108621","therapyId":"55cedee4-79de-4e91-9456-68601e3cda4f|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448366400000},{"taskId":"20190159","therapyId":"89bd5616-0228-463e-b0d2-edc8431f5977|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448391600000},{"taskId":"20108637","therapyId":"55cedee4-79de-4e91-9456-68601e3cda4f|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448391600000},{"taskId":"20190175","therapyId":"89bd5616-0228-463e-b0d2-edc8431f5977|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448434800000},{"taskId":"20150863","therapyId":"ba5dedd1-98fa-4b17-8dea-c7311b6776fe|Medication instruction","plannedDose":"2.5 mg","plannedAdministrationTime":1448434800000},{"taskId":"20144255","therapyId":"21d6d12e-2db1-4856-a29e-8e8bff8767e5|Medication instruction","plannedDose":"20 mg","plannedAdministrationTime":1448434800000},{"taskId":"20108653","therapyId":"55cedee4-79de-4e91-9456-68601e3cda4f|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448434800000},{"taskId":"20108669","therapyId":"55cedee4-79de-4e91-9456-68601e3cda4f|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448452800000},{"taskId":"20190191","therapyId":"89bd5616-0228-463e-b0d2-edc8431f5977|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448478000000},{"taskId":"20108685","therapyId":"55cedee4-79de-4e91-9456-68601e3cda4f|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448478000000},{"taskId":"20281581","therapyId":"89bd5616-0228-463e-b0d2-edc8431f5977|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448481600000},{"taskId":"20190207","therapyId":"89bd5616-0228-463e-b0d2-edc8431f5977|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448521200000},{"taskId":"20150879","therapyId":"ba5dedd1-98fa-4b17-8dea-c7311b6776fe|Medication instruction","plannedDose":"2.5 mg","plannedAdministrationTime":1448521200000},{"taskId":"20108701","therapyId":"55cedee4-79de-4e91-9456-68601e3cda4f|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448521200000},{"taskId":"20281597","therapyId":"89bd5616-0228-463e-b0d2-edc8431f5977|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448524800000},{"taskId":"20108717","therapyId":"55cedee4-79de-4e91-9456-68601e3cda4f|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448539200000}]}}};
      //var updateDataCommand = {"update": {"data": {"patientId": 8555303, "tasks": [{"taskId":"20186047","therapyId":"f6f5cf8d-50bf-41c4-89ac-5167c8843d67|Medication instruction","plannedDose":"18 microgram","plannedAdministrationTime":1448348400000},{"taskId":"20186063","therapyId":"f6f5cf8d-50bf-41c4-89ac-5167c8843d67|Medication instruction","plannedDose":"18 microgram","plannedAdministrationTime":1448434800000},{"taskId":"20186079","therapyId":"f6f5cf8d-50bf-41c4-89ac-5167c8843d67|Medication instruction","plannedDose":"18 microgram","plannedAdministrationTime":1448521200000}]}}};
      //var updateDataCommand = {"update": {"data": {"patientId": 8555303, "tasks": [{"taskId":"20193375","therapyId":"59787007-e96c-497d-a898-e928fa2bc88d|Medication instruction","plannedDose":"1000 gram","plannedAdministrationTime":1448273220000},{"taskId":"20203816","therapyId":"8133ad20-6691-4790-9e84-012b42214b80|Medication instruction","plannedDose":"20 mg","plannedAdministrationTime":1448276400000},{"taskId":"20147151","therapyId":"8921c0c8-8c14-4efe-b7e4-4f114ff8f791|Medication instruction","plannedDose":"20 mg","plannedAdministrationTime":1448276400000},{"taskId":"20186623","therapyId":"e09fc035-0f74-431b-a275-ba27e3b8c837|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448280000000},{"taskId":"20141311","therapyId":"51e98c07-9245-487f-aa7a-bda0620616d5|Medication instruction","plannedDose":"300 mg","plannedAdministrationTime":1448280000000},{"taskId":"20106301","therapyId":"159b6982-1b3a-43d1-83b2-d83643f944f3|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448280000000},{"taskId":"20193391","therapyId":"59787007-e96c-497d-a898-e928fa2bc88d|Medication instruction","plannedDose":"1000 gram","plannedAdministrationTime":1448294820000},{"taskId":"20180335","therapyId":"3b1e672b-1e4a-4a5d-9a2a-29f16714d11d|Medication instruction","plannedDose":"200 microgram","plannedAdministrationTime":1448298000000},{"taskId":"20186639","therapyId":"e09fc035-0f74-431b-a275-ba27e3b8c837|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448301600000},{"taskId":"20106317","therapyId":"159b6982-1b3a-43d1-83b2-d83643f944f3|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448301600000},{"taskId":"20141327","therapyId":"51e98c07-9245-487f-aa7a-bda0620616d5|Medication instruction","plannedDose":"300 mg","plannedAdministrationTime":1448305200000},{"taskId":"20123142","therapyId":"3c04030c-3154-4da0-a4b1-01b8fc0dbf1d|Medication instruction","plannedDose":"10 mg","plannedAdministrationTime":1448305200000},{"taskId":"20200799","therapyId":"dcb7db18-df1d-4cfa-9821-4649e2939b67|Medication instruction","plannedDose":"1200 mg","plannedAdministrationTime":1448308800000},{"taskId":"20198735","therapyId":"65504689-6218-4a95-8669-51e19556e3e1|Medication instruction","plannedDose":"4 mg","plannedAdministrationTime":1448308800000},{"taskId":"20111974","therapyId":"99e44ff5-847b-4ace-8b13-12f13dbe49ab|Medication instruction","plannedDose":"40 mg","plannedAdministrationTime":1448308800000},{"taskId":"20107373","therapyId":"50612975-d58c-4063-a23f-b2295b364eca|Medication instruction","plannedDose":"50 drop","plannedAdministrationTime":1448308800000},{"taskId":"20193407","therapyId":"59787007-e96c-497d-a898-e928fa2bc88d|Medication instruction","plannedDose":"1000 gram","plannedAdministrationTime":1448316420000},{"taskId":"20186655","therapyId":"e09fc035-0f74-431b-a275-ba27e3b8c837|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448323200000},{"taskId":"20106333","therapyId":"159b6982-1b3a-43d1-83b2-d83643f944f3|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448323200000},{"taskId":"20193423","therapyId":"59787007-e96c-497d-a898-e928fa2bc88d|Medication instruction","plannedDose":"1000 gram","plannedAdministrationTime":1448338020000},{"taskId":"20186671","therapyId":"e09fc035-0f74-431b-a275-ba27e3b8c837|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448344800000},{"taskId":"20106349","therapyId":"159b6982-1b3a-43d1-83b2-d83643f944f3|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448344800000},{"taskId":"20203832","therapyId":"8133ad20-6691-4790-9e84-012b42214b80|Medication instruction","plannedDose":"40 mg","plannedAdministrationTime":1448348400000},{"taskId":"20180351","therapyId":"3b1e672b-1e4a-4a5d-9a2a-29f16714d11d|Medication instruction","plannedDose":"200 microgram","plannedAdministrationTime":1448348400000},{"taskId":"20147167","therapyId":"8921c0c8-8c14-4efe-b7e4-4f114ff8f791|Medication instruction","plannedDose":"40 mg","plannedAdministrationTime":1448348400000},{"taskId":"20141343","therapyId":"51e98c07-9245-487f-aa7a-bda0620616d5|Medication instruction","plannedDose":"300 mg","plannedAdministrationTime":1448348400000},{"taskId":"20130550","therapyId":"3fe5f864-d6b4-4004-996b-a8218a5cdc56|Medication instruction","plannedDose":"2.5 mg","plannedAdministrationTime":1448348400000},{"taskId":"20123158","therapyId":"3c04030c-3154-4da0-a4b1-01b8fc0dbf1d|Medication instruction","plannedDose":"10 mg","plannedAdministrationTime":1448348400000},{"taskId":"20119766","therapyId":"0865050f-eca6-4e7a-afa3-060016023541|Medication instruction","plannedDose":"3 mg","plannedAdministrationTime":1448348400000},{"taskId":"20107501","therapyId":"735a27d2-0f65-4a8b-a14c-c35701364ad8|Medication instruction","plannedDose":"2.5 mg","plannedAdministrationTime":1448348400000},{"taskId":"20091757","therapyId":"cb172eac-2df5-4105-a203-a20e3bd08f95|Medication instruction","plannedDose":"5 microgram","plannedAdministrationTime":1448348400000},{"taskId":"20089477","therapyId":"08c231c9-b921-4167-9f54-40096d72ca32|Medication instruction","plannedDose":"125 microgram","plannedAdministrationTime":1448348400000},{"taskId":"20193439","therapyId":"59787007-e96c-497d-a898-e928fa2bc88d|Medication instruction","plannedDose":"1000 gram","plannedAdministrationTime":1448359620000},{"taskId":"20203848","therapyId":"8133ad20-6691-4790-9e84-012b42214b80|Medication instruction","plannedDose":"20 mg","plannedAdministrationTime":1448362800000},{"taskId":"20147183","therapyId":"8921c0c8-8c14-4efe-b7e4-4f114ff8f791|Medication instruction","plannedDose":"20 mg","plannedAdministrationTime":1448362800000},{"taskId":"20186687","therapyId":"e09fc035-0f74-431b-a275-ba27e3b8c837|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448366400000},{"taskId":"20141359","therapyId":"51e98c07-9245-487f-aa7a-bda0620616d5|Medication instruction","plannedDose":"300 mg","plannedAdministrationTime":1448366400000},{"taskId":"20106365","therapyId":"159b6982-1b3a-43d1-83b2-d83643f944f3|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448366400000},{"taskId":"20193455","therapyId":"59787007-e96c-497d-a898-e928fa2bc88d|Medication instruction","plannedDose":"1000 gram","plannedAdministrationTime":1448381220000},{"taskId":"20180367","therapyId":"3b1e672b-1e4a-4a5d-9a2a-29f16714d11d|Medication instruction","plannedDose":"200 microgram","plannedAdministrationTime":1448384400000},{"taskId":"20186703","therapyId":"e09fc035-0f74-431b-a275-ba27e3b8c837|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448388000000},{"taskId":"20106381","therapyId":"159b6982-1b3a-43d1-83b2-d83643f944f3|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448388000000},{"taskId":"20141375","therapyId":"51e98c07-9245-487f-aa7a-bda0620616d5|Medication instruction","plannedDose":"300 mg","plannedAdministrationTime":1448391600000},{"taskId":"20123174","therapyId":"3c04030c-3154-4da0-a4b1-01b8fc0dbf1d|Medication instruction","plannedDose":"10 mg","plannedAdministrationTime":1448391600000},{"taskId":"20200815","therapyId":"dcb7db18-df1d-4cfa-9821-4649e2939b67|Medication instruction","plannedDose":"1200 mg","plannedAdministrationTime":1448395200000},{"taskId":"20198751","therapyId":"65504689-6218-4a95-8669-51e19556e3e1|Medication instruction","plannedDose":"4 mg","plannedAdministrationTime":1448395200000},{"taskId":"20111990","therapyId":"99e44ff5-847b-4ace-8b13-12f13dbe49ab|Medication instruction","plannedDose":"40 mg","plannedAdministrationTime":1448395200000},{"taskId":"20107389","therapyId":"50612975-d58c-4063-a23f-b2295b364eca|Medication instruction","plannedDose":"50 drop","plannedAdministrationTime":1448395200000},{"taskId":"20193471","therapyId":"59787007-e96c-497d-a898-e928fa2bc88d|Medication instruction","plannedDose":"1000 gram","plannedAdministrationTime":1448402820000},{"taskId":"20186719","therapyId":"e09fc035-0f74-431b-a275-ba27e3b8c837|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448409600000},{"taskId":"20106397","therapyId":"159b6982-1b3a-43d1-83b2-d83643f944f3|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448409600000},{"taskId":"20193487","therapyId":"59787007-e96c-497d-a898-e928fa2bc88d|Medication instruction","plannedDose":"1000 gram","plannedAdministrationTime":1448424420000},{"taskId":"20186735","therapyId":"e09fc035-0f74-431b-a275-ba27e3b8c837|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448431200000},{"taskId":"20106413","therapyId":"159b6982-1b3a-43d1-83b2-d83643f944f3|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448431200000},{"taskId":"20203864","therapyId":"8133ad20-6691-4790-9e84-012b42214b80|Medication instruction","plannedDose":"40 mg","plannedAdministrationTime":1448434800000},{"taskId":"20180383","therapyId":"3b1e672b-1e4a-4a5d-9a2a-29f16714d11d|Medication instruction","plannedDose":"200 microgram","plannedAdministrationTime":1448434800000},{"taskId":"20147199","therapyId":"8921c0c8-8c14-4efe-b7e4-4f114ff8f791|Medication instruction","plannedDose":"40 mg","plannedAdministrationTime":1448434800000},{"taskId":"20141391","therapyId":"51e98c07-9245-487f-aa7a-bda0620616d5|Medication instruction","plannedDose":"300 mg","plannedAdministrationTime":1448434800000},{"taskId":"20130566","therapyId":"3fe5f864-d6b4-4004-996b-a8218a5cdc56|Medication instruction","plannedDose":"2.5 mg","plannedAdministrationTime":1448434800000},{"taskId":"20123190","therapyId":"3c04030c-3154-4da0-a4b1-01b8fc0dbf1d|Medication instruction","plannedDose":"10 mg","plannedAdministrationTime":1448434800000},{"taskId":"20119782","therapyId":"0865050f-eca6-4e7a-afa3-060016023541|Medication instruction","plannedDose":"3 mg","plannedAdministrationTime":1448434800000},{"taskId":"20107517","therapyId":"735a27d2-0f65-4a8b-a14c-c35701364ad8|Medication instruction","plannedDose":"2.5 mg","plannedAdministrationTime":1448434800000},{"taskId":"20091773","therapyId":"cb172eac-2df5-4105-a203-a20e3bd08f95|Medication instruction","plannedDose":"5 microgram","plannedAdministrationTime":1448434800000},{"taskId":"20089493","therapyId":"08c231c9-b921-4167-9f54-40096d72ca32|Medication instruction","plannedDose":"125 microgram","plannedAdministrationTime":1448434800000},{"taskId":"20193503","therapyId":"59787007-e96c-497d-a898-e928fa2bc88d|Medication instruction","plannedDose":"1000 gram","plannedAdministrationTime":1448446020000},{"taskId":"20203880","therapyId":"8133ad20-6691-4790-9e84-012b42214b80|Medication instruction","plannedDose":"20 mg","plannedAdministrationTime":1448449200000},{"taskId":"20147215","therapyId":"8921c0c8-8c14-4efe-b7e4-4f114ff8f791|Medication instruction","plannedDose":"20 mg","plannedAdministrationTime":1448449200000},{"taskId":"20186751","therapyId":"e09fc035-0f74-431b-a275-ba27e3b8c837|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448452800000},{"taskId":"20141407","therapyId":"51e98c07-9245-487f-aa7a-bda0620616d5|Medication instruction","plannedDose":"300 mg","plannedAdministrationTime":1448452800000},{"taskId":"20106429","therapyId":"159b6982-1b3a-43d1-83b2-d83643f944f3|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448452800000},{"taskId":"20193519","therapyId":"59787007-e96c-497d-a898-e928fa2bc88d|Medication instruction","plannedDose":"1000 gram","plannedAdministrationTime":1448467620000},{"taskId":"20180399","therapyId":"3b1e672b-1e4a-4a5d-9a2a-29f16714d11d|Medication instruction","plannedDose":"200 microgram","plannedAdministrationTime":1448470800000},{"taskId":"20186767","therapyId":"e09fc035-0f74-431b-a275-ba27e3b8c837|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448474400000},{"taskId":"20106445","therapyId":"159b6982-1b3a-43d1-83b2-d83643f944f3|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448474400000},{"taskId":"20141423","therapyId":"51e98c07-9245-487f-aa7a-bda0620616d5|Medication instruction","plannedDose":"300 mg","plannedAdministrationTime":1448478000000},{"taskId":"20123206","therapyId":"3c04030c-3154-4da0-a4b1-01b8fc0dbf1d|Medication instruction","plannedDose":"10 mg","plannedAdministrationTime":1448478000000},{"taskId":"20200831","therapyId":"dcb7db18-df1d-4cfa-9821-4649e2939b67|Medication instruction","plannedDose":"1200 mg","plannedAdministrationTime":1448481600000},{"taskId":"20198767","therapyId":"65504689-6218-4a95-8669-51e19556e3e1|Medication instruction","plannedDose":"4 mg","plannedAdministrationTime":1448481600000},{"taskId":"20112006","therapyId":"99e44ff5-847b-4ace-8b13-12f13dbe49ab|Medication instruction","plannedDose":"40 mg","plannedAdministrationTime":1448481600000},{"taskId":"20107405","therapyId":"50612975-d58c-4063-a23f-b2295b364eca|Medication instruction","plannedDose":"50 drop","plannedAdministrationTime":1448481600000},{"taskId":"20193535","therapyId":"59787007-e96c-497d-a898-e928fa2bc88d|Medication instruction","plannedDose":"1000 gram","plannedAdministrationTime":1448489220000},{"taskId":"20186783","therapyId":"e09fc035-0f74-431b-a275-ba27e3b8c837|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448496000000},{"taskId":"20106461","therapyId":"159b6982-1b3a-43d1-83b2-d83643f944f3|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448496000000},{"taskId":"20193551","therapyId":"59787007-e96c-497d-a898-e928fa2bc88d|Medication instruction","plannedDose":"1000 gram","plannedAdministrationTime":1448510820000},{"taskId":"20186799","therapyId":"e09fc035-0f74-431b-a275-ba27e3b8c837|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448517600000},{"taskId":"20106477","therapyId":"159b6982-1b3a-43d1-83b2-d83643f944f3|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448517600000},{"taskId":"20205064","therapyId":"cb172eac-2df5-4105-a203-a20e3bd08f95|Medication instruction","plannedDose":"5 microgram","plannedAdministrationTime":1448521200000},{"taskId":"20203896","therapyId":"8133ad20-6691-4790-9e84-012b42214b80|Medication instruction","plannedDose":"40 mg","plannedAdministrationTime":1448521200000},{"taskId":"20180415","therapyId":"3b1e672b-1e4a-4a5d-9a2a-29f16714d11d|Medication instruction","plannedDose":"200 microgram","plannedAdministrationTime":1448521200000},{"taskId":"20147231","therapyId":"8921c0c8-8c14-4efe-b7e4-4f114ff8f791|Medication instruction","plannedDose":"40 mg","plannedAdministrationTime":1448521200000},{"taskId":"20141439","therapyId":"51e98c07-9245-487f-aa7a-bda0620616d5|Medication instruction","plannedDose":"300 mg","plannedAdministrationTime":1448521200000},{"taskId":"20130582","therapyId":"3fe5f864-d6b4-4004-996b-a8218a5cdc56|Medication instruction","plannedDose":"2.5 mg","plannedAdministrationTime":1448521200000},{"taskId":"20123222","therapyId":"3c04030c-3154-4da0-a4b1-01b8fc0dbf1d|Medication instruction","plannedDose":"10 mg","plannedAdministrationTime":1448521200000},{"taskId":"20119798","therapyId":"0865050f-eca6-4e7a-afa3-060016023541|Medication instruction","plannedDose":"3 mg","plannedAdministrationTime":1448521200000},{"taskId":"20107533","therapyId":"735a27d2-0f65-4a8b-a14c-c35701364ad8|Medication instruction","plannedDose":"2.5 mg","plannedAdministrationTime":1448521200000},{"taskId":"20089509","therapyId":"08c231c9-b921-4167-9f54-40096d72ca32|Medication instruction","plannedDose":"125 microgram","plannedAdministrationTime":1448521200000},{"taskId":"20193567","therapyId":"59787007-e96c-497d-a898-e928fa2bc88d|Medication instruction","plannedDose":"1000 gram","plannedAdministrationTime":1448532420000},{"taskId":"20203912","therapyId":"8133ad20-6691-4790-9e84-012b42214b80|Medication instruction","plannedDose":"20 mg","plannedAdministrationTime":1448535600000},{"taskId":"20147247","therapyId":"8921c0c8-8c14-4efe-b7e4-4f114ff8f791|Medication instruction","plannedDose":"20 mg","plannedAdministrationTime":1448535600000},{"taskId":"20186815","therapyId":"e09fc035-0f74-431b-a275-ba27e3b8c837|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448539200000},{"taskId":"20141455","therapyId":"51e98c07-9245-487f-aa7a-bda0620616d5|Medication instruction","plannedDose":"300 mg","plannedAdministrationTime":1448539200000},{"taskId":"20106493","therapyId":"159b6982-1b3a-43d1-83b2-d83643f944f3|Medication instruction","plannedDose":"500 mg","plannedAdministrationTime":1448539200000}]}}};
      var updateDataCommand = {
        "update": {
          "data": {
            "patientId": 397777307,
            "tasks": [{
              "taskId": "20188431",
              "therapyId": "fd9dd0ff-2386-411a-b00f-ed9d9b9d42a1|Medication instruction",
              "plannedDose": "100 mg",
              "plannedAdministrationTime": 1448305200000
            }, {
              "taskId": "20188447",
              "therapyId": "fd9dd0ff-2386-411a-b00f-ed9d9b9d42a1|Medication instruction",
              "plannedDose": "100 mg",
              "plannedAdministrationTime": 1448348400000
            }, {
              "taskId": "20188463",
              "therapyId": "fd9dd0ff-2386-411a-b00f-ed9d9b9d42a1|Medication instruction",
              "plannedDose": "100 mg",
              "plannedAdministrationTime": 1448391600000
            }, {
              "taskId": "20188479",
              "therapyId": "fd9dd0ff-2386-411a-b00f-ed9d9b9d42a1|Medication instruction",
              "plannedDose": "100 mg",
              "plannedAdministrationTime": 1448434800000
            }, {
              "taskId": "20188495",
              "therapyId": "fd9dd0ff-2386-411a-b00f-ed9d9b9d42a1|Medication instruction",
              "plannedDose": "100 mg",
              "plannedAdministrationTime": 1448478000000
            }, {
              "taskId": "20188511",
              "therapyId": "fd9dd0ff-2386-411a-b00f-ed9d9b9d42a1|Medication instruction",
              "plannedDose": "100 mg",
              "plannedAdministrationTime": 1448521200000
            }]
          }
        }
      };

      var self = this;
      setTimeout(function()
      {
        self.onViewCommand(updateDataCommand);
      }, 0);
    }
  }
});