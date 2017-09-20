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
      .factory('tm.angular.medications.documentation.data.models.TherapyDocuments', function()
      {
        /** Constructor */
        function TherapyDocuments(patientId, recordCount, recordOffset, moreRecordsExist, documents)
        {
          this.patientId = patientId;
          this.recordCount = recordCount;
          this.recordOffset = recordOffset;
          this.moreRecordsExist = moreRecordsExist;

          if (angular.isDefined(documents))
          {
            this.documents = documents;
          }
          else
          {
            this.documents = [];
          }
        }

        TherapyDocuments.prototype.hasMoreRecords = function()
        {
          return this.moreRecordsExist === true;
        };

        TherapyDocuments.prototype.getDocuments = function()
        {
          return this.documents;
        };

        TherapyDocuments.prototype.getRecordOffset = function()
        {
          return this.recordOffset;
        };

        return TherapyDocuments;
      });
})();