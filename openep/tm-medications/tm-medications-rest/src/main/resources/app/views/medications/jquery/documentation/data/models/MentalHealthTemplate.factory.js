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
(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.data.models')
      .factory('tm.angular.medications.documentation.data.models.MentalHealthTemplate',
          mentalHealthTemplateFactory);

  function mentalHealthTemplateFactory()
  {
    /**
     * Constructs a new MentalHealthTemplate object.
     * @constructor
     */
    function MentalHealthTemplate(id)
    {
      this.id = id;
    }
    MentalHealthTemplate.fromJsonObject = fromJsonObject;
    MentalHealthTemplate.prototype = {
      getId: getId,
      getName: getName,
      getRoute: getRoute,
      getRouteName: getRouteName
    };

    return MentalHealthTemplate;

    /**
     * Helper method to convert a json object to a {@link MentalHealthTemplate} instance.
     * @param jsonObject
     */
    function fromJsonObject(jsonObject)
    {
      var mentalHealthTemplate = new MentalHealthTemplate(jsonObject.id);

      angular.extend(mentalHealthTemplate, jsonObject);

      return mentalHealthTemplate;
    }

    /**
     * @returns {string}
     */
    function getName()
    {
      return this.name;
    }

    /**
     * @returns {Object}
     */
    function getRoute()
    {
      return this.route;
    }

    /**
     * @returns {String}
     */
    function getRouteName()
    {
      return this.route.name;
    }

    /**
     * @returns {string}
     */
    function getId()
    {
      return this.id;
    }
  }
})();