/*
 * Copyright (c) 2010-2015 Marand d.o.o. (www.marand.com)
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
Class.define('app.views.medications.TherapyDataSorter', 'tm.jquery.Object', {
  view: null,

  /** constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
  },

  group: function (type, therapyData)
  {
    if (type == 'atcGroup')
    {
      return this.groupByAtc(therapyData).sort(this._sortByKeyMethod);
    }
    else if (type == 'routes')
    {
      return this.groupByRoute(therapyData).sort(this._sortByKeyMethod);
    }
    else if (type == 'customGroup')
    {
      return this.groupByCustomGroup(therapyData).sort(this._sortByKeyMethod);
    }

    return null;
  },

  groupByRoute: function (therapyData)
  {
    var multipleRoutesDisplayName = this.getView().getDictionary("multiple.routes");
    var routeGroups = [];
    for (var i = 0; i < therapyData.length; i++)
    {
      var therapyDto = therapyData[i].therapy;
      var routes = therapyDto.getRoutes();
      if (tm.jquery.Utils.isArray(routes))
      {
        var route = routes.length === 1 ? routes[0].name : multipleRoutesDisplayName;
        var group = this.getGroupByKey(routeGroups, route);

        if (group == null)
        {
          group = {key: route, elements: []};
          routeGroups.push(group);
        }
        group.elements.push(therapyData[i]);
      }
    }
    return routeGroups;
  },

  groupByAtc: function(therapyData)
  {
    var atcGroups = [];
    for (var i = 0; i < therapyData.length; i++)
    {
      var therapyDataObject = therapyData[i];
      var atc = therapyDataObject.atcGroupName ?
          therapyDataObject.atcGroupName + ' (' + therapyDataObject.atcGroupCode + ')' :
          this.getView().getDictionary("without.atc");
      var group = this.getGroupByKey(atcGroups, atc);
      if (group == null)
      {
        group = {key: atc, elements: []};
        atcGroups.push(group);
      }
      group.elements.push(therapyDataObject);
    }

    return atcGroups;
  },

  groupByCustomGroup: function (therapyData)
  {
    var customGroups = [];
    for (var i = 0; i < therapyData.length; i++)
    {
      var therapyDataObject = therapyData[i];
      var customGroup = tm.views.medications.MedicationUtils.getTherapyCustomGroupDisplayName(
          therapyDataObject, this.getView());
      var group = this.getGroupByKey(customGroups, customGroup);
      if (group == null)
      {
        group = {key: customGroup, elements: []};
        customGroups.push(group);
      }
      group.elements.push(therapyDataObject);
    }

    return customGroups;
  },

  sortGroupsByKey: function (groups)
  {
    groups.sort(this._sortByKeyMethod);
  },

  getGroupByKey: function (groups, key)
  {
    for (var i = 0; i < groups.length; i++)
    {
      if (groups[i].key == key)
      {
        return groups[i]
      }
    }
    return null;
  },

  /*
   * For override, since therapy flow view and timeline use different DTO objects for data.
   * This is the timeline data compatible implementation. */
  getTherapyDtoFromDataObject: function(object){
    return object.therapy;
  },

  getView: function ()
  {
    return this.view;
  },

  _sortByKeyMethod: function (group1, group2)
  {
    var key1 = group1.key.toLowerCase();
    var key2 = group2.key.toLowerCase();
    if (key1 < key2)
    {
      return -1;
    }
    if (key1 > key2)
    {
      return 1;
    }
    return 0;
  }
})
;