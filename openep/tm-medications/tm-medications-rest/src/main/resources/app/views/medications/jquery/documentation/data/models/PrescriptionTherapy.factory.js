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
      .factory('tm.angular.medications.documentation.data.models.PrescriptionTherapy',
          ['tm.angular.medications.documentation.data.models.Therapy',
            'tm.angular.medications.documentation.data.models.PrescriptionStatus', function(Therapy, prescriptionStatusEnum)
          {
            /**
             * Constructs a new PrescriptionTherapy object, with the specified ID and Therapy.
             * @param prescriptionTherapyId
             * @param therapy tm.angular.medications.documentation.data.models.Therapy
             * @constructor
             */
            function PrescriptionTherapy(prescriptionTherapyId, prescriptionStatus, therapy)
            {
              this.prescriptionTherapyId = prescriptionTherapyId;
              this.prescriptionStatus = prescriptionStatus;

              if (angular.isObject(therapy))
              {
                this.therapy = therapy;
              }
              else
              {
                this.therapy = new Therapy();
              }
            }

            /**
             * Returns a new PrescriptionTherapy from an JSON object.
             * @param object The JSON object.
             * @returns {PrescriptionTherapy}
             */
            PrescriptionTherapy.fromJsonObject = function(object)
            {
              var prescriptionTherapy = new PrescriptionTherapy();
              if (angular.isDefined(object))
              {
                angular.extend(prescriptionTherapy, object);
                prescriptionTherapy.therapy = Therapy.fromJsonObject(object.therapy);
              }
              return prescriptionTherapy;
            };

            /**
             * Returns the Therapy object belonging to this prescription, or an empty object if no therapy was set.
             * @returns {*|{}}
             */
            PrescriptionTherapy.prototype.getTherapy = function()
            {
              return this.therapy || {};
            };

            /**
             * Returns the Prescription therapy ID.
             * @returns {*}
             */
            PrescriptionTherapy.prototype.getPrescriptionTherapyId = function()
            {
              return this.prescriptionTherapyId;
            };

            /**
             * Returns the prescription status.
             * @returns {tm.angular.medications.documentation.data.models.PrescriptionStatus}
             */
            PrescriptionTherapy.prototype.getPrescriptionStatus = function()
            {
              return this.prescriptionStatus;
            };

            /**
             * Sets the prescription status.
             * @param value {tm.angular.medications.documentation.data.models.PrescriptionStatus}
             * @returns {*}
             */
            PrescriptionTherapy.prototype.setPrescriptionStatus = function(value)
            {
              return this.prescriptionStatus = value;
            };

            /**
             * Can the prescription therapy be cancelled?
             * @returns {boolean}
             */
            PrescriptionTherapy.prototype.isCancelable = function()
            {
              return this.getPrescriptionStatus() &&
                  (this.getPrescriptionStatus() === prescriptionStatusEnum.PRESCRIBED
                  || this.getPrescriptionStatus() === prescriptionStatusEnum.PARTIALLY_USED);
            };

            /**
             * Can the prescription therapy be removed from the package?
             * @returns {boolean}
             */
            PrescriptionTherapy.prototype.isRemovable = function()
            {
              return !this.getPrescriptionStatus();
            };

            return PrescriptionTherapy;
          }]);
})();