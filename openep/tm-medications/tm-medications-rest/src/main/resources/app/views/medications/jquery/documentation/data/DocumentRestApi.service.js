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
  angular.module('tm.angular.medications.documentation.data')
      .service('tm.angular.medications.documentation.data.DocumentRestApi', ['$q', '$resource',
        'tm.angular.medications.documentation.data.models.TherapyDocuments',
        'tm.angular.medications.documentation.data.models.TherapyDocument',
        function($q, $resource, TherapyDocuments, TherapyDocument)
        {
          /* exposing public methods and properties */
          var restApi = this;
          restApi.getDocuments = getDocuments;

          /**
           * @returns {$resource}
           */
          function getDocumentResource()
          {
            return $resource('medications/getTherapyDocuments/?patientId=:patientId&recordCount=:recordCount&recordOffset=:recordOffset',
                {patientId: '@careProviderIds', recordCount: '@recordCount', recordOffset: '@recordOffset'},
                {
                  get: {method: 'GET', isArray: false}
                }
            );
          }

          /**
           * Retrieves the specified number of documents, with the given offset, for the specified patientId.
           * @param patientId
           * @param recordCount
           * @param recordOffset
           * @returns {TherapyDocuments}
           */
          function getDocuments(patientId, recordCount, recordOffset)
          {
            var deferred = $q.defer();
            getDocumentResource().get({
                  patientId: patientId,
                  recordCount: recordCount,
                  recordOffset: recordOffset
                },
                function(response)
                {
                  // fake transform from json
                  var documentResponse = null;
                  if (angular.isDefined(response))
                  {
                    documentResponse = new TherapyDocuments(patientId, recordCount,
                        recordOffset, response.moreRecordsExist, response.documents.map(function(object)
                        {
                          return TherapyDocument.fromJsonObject(object);
                        }));
                  }
                  deferred.resolve(documentResponse);
                }, function(response)
                {
                  deferred.reject(response);
                });
            return deferred.promise;
          }
        }]);
})();