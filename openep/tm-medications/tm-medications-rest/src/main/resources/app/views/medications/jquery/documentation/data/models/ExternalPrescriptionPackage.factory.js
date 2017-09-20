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
      .factory('tm.angular.medications.documentation.data.models.ExternalPrescriptionPackage',
          externalPrescriptionPackageFactory);

  function externalPrescriptionPackageFactory(ExternalPrescriptionTherapy, prescriptionDocumentTypeEnum)
  {
    /**
     * Constructs a new ExternalPrescriptionPackage object.
     * @param {String} prescriptionPackageId
     * @param {Date} prescriptionDate
     * @param {String} externalPrescriptionTherapies
     * @constructor
     */
    function ExternalPrescriptionPackage(prescriptionPackageId, prescriptionDate, externalPrescriptionTherapies)
    {
      this.prescriptionPackageId = prescriptionPackageId;
      this.externalPrescriptionTherapies = externalPrescriptionTherapies ? externalPrescriptionTherapies : [];
      this.prescriptionDate = prescriptionDate;
    }

    ExternalPrescriptionPackage.fromJsonObject = fromJsonObject;
    ExternalPrescriptionPackage.prototype = {
      isAuthorized: isAuthorized,
      getPrescriptionTherapiesByDocumentType: getPrescriptionTherapiesByDocumentType,
      getPrescriptionTherapiesWithWhiteDocumentType: getPrescriptionTherapiesWithWhiteDocumentType,
      getPrescriptionTherapiesWithGreenDocumentType: getPrescriptionTherapiesWithGreenDocumentType,
      getCompositionUid: getCompositionUid,
      getPrescriptionPackageId: getPrescriptionPackageId,
      getExternalPrescriptionTherapies: getExternalPrescriptionTherapies,
      getPrescriptionDate: getPrescriptionDate,
      setPrescriptionDate: setPrescriptionDate
    };

    return ExternalPrescriptionPackage;

    /**
     * *  External prescriptions are  authorized. Returns true because of header component.
     * @returns {boolean}
     */

    function isAuthorized()
    {
      return true;
    }

    /**
     * Returns an array of PrescriptionTherapy objects. If none exists, returns an empty array.
     * @returns {Array}
     */

    function getPrescriptionTherapiesByDocumentType(documentType)
    {
      return this.externalPrescriptionTherapies.filter(function(externalPrescriptionTherapy)
      {
        return externalPrescriptionTherapy.getPrescriptionDocumentType() === documentType;
      });
    }

    /**
     * Returns an array of PrescriptionTherapy objects with the white document type prescription.
     * If none exists, returns an empty array.
     * @returns {Array}
     */
    function getPrescriptionTherapiesWithWhiteDocumentType()
    {
      return this.getPrescriptionTherapiesByDocumentType(prescriptionDocumentTypeEnum.WHITE);
    }

    /**
     * Returns an array of PrescriptionTherapy objects with the green document type prescription.
     * If none exists, returns an empty array.
     * @returns {Array}
     */
    function getPrescriptionTherapiesWithGreenDocumentType()
    {
      return this.getPrescriptionTherapiesByDocumentType(prescriptionDocumentTypeEnum.GREEN);
    }

    /**
     * @returns {String}
     */
    function getCompositionUid()
    {
      return this.compositionUid;
    }

    /**
     * @returns {String}
     */
    function getPrescriptionPackageId()
    {
      return this.prescriptionPackageId;
    }

    /**
     * @returns {Array|*}
     */
    function getExternalPrescriptionTherapies()
    {
      return this.externalPrescriptionTherapies;
    }

    /**
     * @returns {Date}
     */
    function getPrescriptionDate()
    {
      return this.prescriptionDate;
    }

    /**
     * @param {Date} value
     */
    function setPrescriptionDate(value)
    {
      this.prescriptionDate = value;
    }

    /**
     * Helper method to return a new instance of {@link ExternalPrescriptionPackage} based on a JSON object.
     * @param {Object} jsonObject
     */
    function fromJsonObject(jsonObject)
    {
      var externalPrescriptionPackage = new ExternalPrescriptionPackage();
      if (jsonObject)
      {
        angular.extend(externalPrescriptionPackage, jsonObject);
        if (angular.isDefined(jsonObject.externalPrescriptionTherapies) && angular.isArray(jsonObject.externalPrescriptionTherapies))
        {
          externalPrescriptionPackage.externalPrescriptionTherapies = jsonObject.externalPrescriptionTherapies.map(function(therapiesObject)
          {
            return ExternalPrescriptionTherapy.fromJsonObject(therapiesObject);
          });
        }
        if (jsonObject.prescriptionDate)
        {
          externalPrescriptionPackage.setPrescriptionDate(new Date(jsonObject.prescriptionDate));
        }
      }
      return externalPrescriptionPackage;
    }
  }

  externalPrescriptionPackageFactory.$inject = ['tm.angular.medications.documentation.data.models.ExternalPrescriptionTherapy', 'tm.angular.medications.documentation.data.models.PrescriptionDocumentTypeEnum'];
})();