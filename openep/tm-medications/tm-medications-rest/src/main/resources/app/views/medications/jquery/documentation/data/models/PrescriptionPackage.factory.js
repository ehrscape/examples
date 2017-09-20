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
      .factory('tm.angular.medications.documentation.data.models.PrescriptionPackage',
          ['tm.angular.medications.documentation.data.models.PrescriptionTherapy',
            'tm.angular.medications.documentation.data.models.PrescriptionDocumentTypeEnum',
            function(PrescriptionTherapy, prescriptionDocumentTypeEnum)
            {
              /**
               * Constructs a new PrescriptionPackage object.
               * @constructor
               */
              function PrescriptionPackage(prescriptionPackageId, compositionUid, prescriptionTherapies, lastUpdateTimestamp)
              {
                this.prescriptionPackageId = prescriptionPackageId;
                this.compositionUid = compositionUid;
                if (prescriptionTherapies)
                {
                  this.prescriptionTherapies = prescriptionTherapies;
                }
                else
                {
                  this.prescriptionTherapies = [];
                }
                this.lastUpdateTimestamp = lastUpdateTimestamp;
              }

              /**
               * Constructs a new PrescriptionPackage from a JSON object.
               * @param object The JSON object.
               * @returns {PrescriptionPackage}
               */
              PrescriptionPackage.fromJsonObject = function(object)
              {
                var prescriptionPackage = new PrescriptionPackage();
                if (object)
                {
                  angular.extend(prescriptionPackage, object);
                  if (angular.isDefined(object.prescriptionTherapies) && angular.isArray(object.prescriptionTherapies))
                  {
                    prescriptionPackage.prescriptionTherapies = object.prescriptionTherapies.map(function(prescriptionTherapiesObject)
                    {
                      return PrescriptionTherapy.fromJsonObject(prescriptionTherapiesObject);
                    });
                  }
                  if (object.lastUpdateTimestamp)
                  {
                    prescriptionPackage.setLastUpdateTimestamp(new Date(object.lastUpdateTimestamp));
                  }
                }
                return prescriptionPackage;
              };

              /**
               * Returns an array of PrescriptionTherapy objects. If none exists, returns an empty array.
               * @returns {Array}
               */
              PrescriptionPackage.prototype.getPrescriptionTherapies = function()
              {
                return this.prescriptionTherapies;
              };

              /**
               * Sets the package's prescription therapies or an empty array if no array was passed.
               * @param {Array} value
               */
              PrescriptionPackage.prototype.setPrescriptionTherapies = function(value)
              {
                this.prescriptionTherapies = angular.isArray(value) ? value : [];
              };

              PrescriptionPackage.prototype.getCompositionUid = function()
              {
                return this.compositionUid;
              };

              PrescriptionPackage.prototype.setCompositionUid = function(value)
              {
                this.compositionUid = value;
              };

              PrescriptionPackage.prototype.getPrescriptionPackageId = function()
              {
                return this.prescriptionPackageId;
              };

              PrescriptionPackage.prototype.isAuthorized = function()
              {
                return this.prescriptionPackageId ? true : false;
              };

              /**
               * Returns an array of PrescriptionTherapy objects. If none exists, returns an empty array.
               * @returns {Array}
               */
              PrescriptionPackage.prototype.getPrescriptionTherapiesByDocumentType = function(documentType)
              {
                return this.prescriptionTherapies.filter(function(prescriptionTherapy)
                {
                  return prescriptionTherapy.getTherapy().getPrescriptionLocalDetails().getPrescriptionDocumentType() === documentType;
                });
              };

              /**
               * Returns an array of PrescriptionTherapy objects with the white document type prescription.
               * If none exists, returns an empty array.
               * @returns {Array}
               */
              PrescriptionPackage.prototype.getPrescriptionTherapiesWithWhiteDocumentType = function()
              {
                return this.getPrescriptionTherapiesByDocumentType(prescriptionDocumentTypeEnum.WHITE);
              };

              /**
               * Returns an array of PrescriptionTherapy objects with the green document type prescription.
               * If none exists, returns an empty array.
               * @returns {Array}
               */
              PrescriptionPackage.prototype.getPrescriptionTherapiesWithGreenDocumentType = function()
              {
                return this.getPrescriptionTherapiesByDocumentType(prescriptionDocumentTypeEnum.GREEN);
              };

              /**
               * Finds the specified prescription therapy via the passed ID and returns it.
               * @param prescriptionTherapyId
               * @returns {PrescriptionTherapy}
               */
              PrescriptionPackage.prototype.getPrescriptionTherapyById = function(prescriptionTherapyId)
              {
                var therapies = this.getPrescriptionTherapies();
                for (var idx = 0; idx < therapies.length; idx++)
                {
                  if (therapies[idx].getPrescriptionTherapyId() === prescriptionTherapyId)
                  {
                    return therapies[idx];
                  }
                }
              };

              /**
               * @param {Date} lastUpdateTimestamp
               */
              PrescriptionPackage.prototype.setLastUpdateTimestamp = function(lastUpdateTimestamp)
              {
                this.lastUpdateTimestamp = lastUpdateTimestamp
              };

              /**
               * @returns {Date|*}
               */
              PrescriptionPackage.prototype.getLastUpdateTimestamp = function()
              {
                return this.lastUpdateTimestamp;
              };

              return PrescriptionPackage;
            }]);
})();