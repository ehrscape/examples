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
      .factory('tm.angular.medications.documentation.data.models.MentalHealthDocumentContent',
          mentalHealthDocumentContentFactory);

  function mentalHealthDocumentContentFactory(MentalHealthMedication, MentalHealthTemplate)
  {
    /**
     * Constructs a new MentalHealthDocumentContent object.
     * @param {String} compositionUid
     * @constructor
     */
    function MentalHealthDocumentContent(compositionUid)
    {
      this.compositionUid = compositionUid;
    }

    MentalHealthDocumentContent.fromJsonObject = fromJsonObject;
    MentalHealthDocumentContent.prototype = {
      isAuthorized: isAuthorized,
      getMentalHealthDocumentType: getMentalHealthDocumentType,
      getMentalHealthMedications: getMentalHealthMedications,
      getMentalHealthTemplates: getMentalHealthTemplates,
      getCompositionUid: getCompositionUid,
      getBnfMaximum: getBnfMaximum
    };
    return MentalHealthDocumentContent;

    /**
     * Returns true because of header component.
     * @returns {boolean}
     */
    function isAuthorized()
    {
      return true;
    }

    /**
     * @returns {app.views.medications.TherapyEnums.mentalHealthDocumentType|{T2, T3}}
     */
    function getMentalHealthDocumentType()
    {
      return this.mentalHealthDocumentType;
    }

    /**
     * @returns {Array}
     */

    function getMentalHealthMedications()
    {
      return this.mentalHealthMedicationDtoList;
    }

    /**
     * @returns {Array}
     */
    function getMentalHealthTemplates()
    {
      return this.mentalHealthTemplateDtoList;
    }

    /**
     * @returns {String}
     */
    function getCompositionUid()
    {
      return this.compositionUid;
    }

    /**
     *
     * @returns {Number}
     */
    function getBnfMaximum()
    {
      return this.bnfMaximum;
    }

    /**
     * Helper method to convert a json object to a {@link MentalHealthDocumentContent} instance.
     * @param jsonObject
     */
    function fromJsonObject(jsonObject)
    {
      var mentalHealthDocumentContent = new MentalHealthDocumentContent(jsonObject.compositionUid);
      if (jsonObject)
      {
        angular.extend(mentalHealthDocumentContent, jsonObject);

        if (angular.isDefined(jsonObject.mentalHealthMedicationDtoList) && angular.isArray(jsonObject.mentalHealthMedicationDtoList))
        {
          mentalHealthDocumentContent.mentalHealthMedicationDtoList = jsonObject.mentalHealthMedicationDtoList.map(function(medicationObject)
          {
            return MentalHealthMedication.fromJsonObject(medicationObject);
          });
        }
        if (angular.isDefined(jsonObject.mentalHealthTemplateDtoList) && angular.isArray(jsonObject.mentalHealthTemplateDtoList))
        {
          mentalHealthDocumentContent.mentalHealthTemplateDtoList = jsonObject.mentalHealthTemplateDtoList.map(function(medicationObject)
          {
            return MentalHealthTemplate.fromJsonObject(medicationObject);
          });
        }
      }
      return mentalHealthDocumentContent;
    }
  }
  mentalHealthDocumentContentFactory.$inject = ['tm.angular.medications.documentation.data.models.MentalHealthMedication', 'tm.angular.medications.documentation.data.models.MentalHealthTemplate'];
})();