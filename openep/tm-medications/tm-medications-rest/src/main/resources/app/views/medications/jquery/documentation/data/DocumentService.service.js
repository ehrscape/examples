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
      .service('tm.angular.medications.documentation.data.DocumentService', ['$q',
        'tm.angular.medications.documentation.data.DocumentRestApi',
        'tm.angular.medications.documentation.data.models.PrescriptionStatus',
        'tm.angular.medications.documentation.data.models.PrescriptionPackage',
        'tm.angularjs.common.tmcBridge.ViewProxy',
        'tm.angular.medications.documentation.common.hub.HubActionName',
        'tm.angular.medications.documentation.common.ehr.CompositionUidUtils',
        'ActiveUpdateData', 'ViewActionName',
        function($q, documentServiceRestApi, prescriptionStatusEnum, PrescriptionPackage, viewProxy,
                 hubActionName, compositionUidUtils, activeUpdateData, viewActionName)
        {
          /* exposing public methods and properties */
          var documentService = this;
          documentService.cancelPrescriptionTherapy = cancelPrescriptionTherapy;
          documentService.updateCachedPrescriptionTherapyStatus = updateCachedPrescriptionTherapyStatus;
          documentService.loadMoreDocuments = loadMoreDocuments;
          documentService.clearDocumentCache = clearDocumentCache;
          documentService.isMoreDocumentsExist = isMoreDocumentsExist;
          documentService.getDocumentCache = getDocumentCache;
          documentService.authorizeDocument = authorizeDocument;
          documentService.updatePrescriptionPackage = updatePrescriptionPackage;
          documentService.deleteDocument = deleteDocument;
          documentService.updateCachedDocumentPrescriptionPackage = updateCachedDocumentPrescriptionPackage;
          documentService.removePrescriptionTherapy = removePrescriptionTherapy;
          documentService.removeCachedDocumentByPrescriptionPackage = removeCachedDocumentByPrescriptionPackage;

          /** Member definitions **/
          var _documentCache = [];
          var _moreDocumentsExist = false;
          var _lastOffset = 0;
          var _stepCount = 5;

          /**
           * @returns {Array<TherapyDocument>}
           */
          function getDocumentCache()
          {
            return _documentCache;
          }

          /**
           * Loads the next set of documents. The number is defined by the local property _stepCount.
           */
          function loadMoreDocuments()
          {
            var patientId = activeUpdateData.patientId;
            viewProxy.getHubAdaptor().actionStarted(hubActionName.GET_DOCUMENTS);
            documentServiceRestApi.getDocuments(activeUpdateData.patientId, _stepCount, _lastOffset).then(
                function(documents)
                {
                  viewProxy.getHubAdaptor().actionEnded(hubActionName.GET_DOCUMENTS);

                  if (activeUpdateData.patientId !== patientId)
                  {
                    viewProxy.getLogger().info("Active patientId changed while loading data, throwing away results.");
                    return;
                  }

                  if (documents != null)
                  {
                    // throw away any response that contains records that should already be loaded
                    if (documents.getRecordOffset() < _documentCache.length)
                    {
                      viewProxy.getLogger().info("Received data with the record offset lower than the actual document count. Throwing away the response.");
                      return;
                    }

                    documents.getDocuments().forEach(function(document)
                    {
                      _documentCache.push(document);
                    });
                    _lastOffset += documents.getDocuments().length;
                    _moreDocumentsExist = documents.hasMoreRecords();
                  }
                  else
                  {
                    clearDocumentCache();
                  }
                },
                function(response)
                {
                  viewProxy.getHubAdaptor().actionFailed(hubActionName.GET_DOCUMENTS);
                  viewProxy.displayRequestErrorNotice(response);
                  viewProxy.getLogger().debug("DocumentService.getAllDocuments() exception occured:", response);
                });
          }

          /**
           * Resets the document cache.
           */
          function clearDocumentCache()
          {
            _documentCache.length = 0;
            _lastOffset = 0;
            _moreDocumentsExist = false;
          }

          /**
           * @returns {boolean}
           */
          function isMoreDocumentsExist()
          {
            return _moreDocumentsExist;
          }

          /**
           * Returns the specified number of documents, from the specified offset, if any exist.
           * @param count Number of documents to load.
           * @param offset The offset from which to start with.
           * @returns {TherapyDocuments}
           */
          function getDocuments(count, offset)
          {
            // wrap for proof of concept, add extra processing logic here or don't
            var deferred = $q.defer();
            documentServiceRestApi.getDocuments(activeUpdateData.patientId, count, offset).then(function(documents)
                {
                  deferred.resolve(documents);
                },
                function(response)
                {
                  deferred.reject(response);
                });
            return deferred.promise;
          }

          /**
           * Cancels the specified prescription therapy via an view action. When finished, updates the
           * prescription status of the specified therapy to the PrescriptionStatus.CANCELLED value.
           * @param {TherapyDocument} document
           * @param {PrescriptionTherapy} prescriptionTherapy
           */
          function cancelPrescriptionTherapy(document, prescriptionTherapy)
          {
            if (prescriptionTherapy.isCancelable())
            {
              viewProxy.sendViewAction(viewActionName.cancelPrescription, {
                compositionUid: JSON.stringify(document.getContent().getCompositionUid()),
                prescriptionPackageId: JSON.stringify(document.getContent().getPrescriptionPackageId()),
                prescriptionTherapyId: JSON.stringify(prescriptionTherapy.getPrescriptionTherapyId()),
                cancellationReason: '',
                patientId: JSON.stringify(activeUpdateData.patientId)
              });
            }
          }

          /**
           * Cancels the specified prescription therapy via an view action. When finished, updates the
           * prescription status of the specified therapy to the PrescriptionStatus.CANCELLED value.
           * @param {TherapyDocument} document
           * @param {PrescriptionTherapy} prescriptionTherapy
           */
          function removePrescriptionTherapy(document, prescriptionTherapy)
          {
            if (prescriptionTherapy.isRemovable())
            {
              var content = document.getContent();

              if (content.getPrescriptionTherapies().length > 1)
              {
                var prescriptionTherapyIndex = content.getPrescriptionTherapies().indexOf(prescriptionTherapy);

                if (prescriptionTherapyIndex > -1)
                {
                  // create a shallow copy and change the therapies in it
                  var prescriptionPackageCopy = angular.extend(new PrescriptionPackage(), document.getContent());
                  var prescriptionTherapiesCopy = content.getPrescriptionTherapies().slice();

                  prescriptionTherapiesCopy.splice(prescriptionTherapyIndex, 1);
                  prescriptionPackageCopy.setPrescriptionTherapies(prescriptionTherapiesCopy);

                  viewProxy.sendViewAction(viewActionName.outpatientPrescription, {
                    patientId: JSON.stringify(activeUpdateData.patientId),
                    prescriptionBundle: JSON.stringify(prescriptionPackageCopy),
                    saveOnly: true
                  });
                }
              }
              else
              {
                if (!content.isAuthorized())
                {
                  deleteDocument(document);
                }
              }
            }
          }

          /**
           * Remove the {@link TherapyDocument} with the specified {@link PrescriptionPackage} from the document cache,
           * if it exists.
           * @param {string} compositionUid
           */
          function removeCachedDocumentByPrescriptionPackage(compositionUid)
          {
            if (angular.isString(compositionUid))
            {
              var document = _findDocumentByContentCompositionUid(compositionUid);
              if (document)
              {
                var index = _documentCache.indexOf(document);
                if (index > -1)
                {
                  _documentCache.splice(index, 1);
                }
              }
            }
          }

          /**
           * Checks if the passed document list contains a document who's content is a {PrescriptionPackage} with the
           * specified compositionUid and {PrescriptionTherapy} with the specified prescriptionTherapyId and
           * updates it's status to whatever is specified. Used as part of the cancel prescription action.
           *
           * @param {String} compositionUid
           * @param {String} prescriptionTherapyId
           */
          function updateCachedPrescriptionTherapyStatus(compositionUid, prescriptionTherapyId, prescriptionStatusEnum)
          {
            if (!angular.isDefined(compositionUid) || !angular.isDefined(prescriptionTherapyId))
            {
              return;
            }

            var document = _findDocumentByContentCompositionUid(compositionUid);
            if (document)
            {
              var documentContent = document.getContent();
              var prescriptionTherapy = documentContent.getPrescriptionTherapyById(prescriptionTherapyId);
              if (angular.isDefined(prescriptionTherapy))
              {
                documentContent.setCompositionUid(compositionUidUtils.incrementCompositionUid(compositionUid));
                prescriptionTherapy.setPrescriptionStatus(prescriptionStatusEnum);
              }
            }
          }

          /**
           * Updates the document content based on the content's composition uid.
           * @param {Object} prescriptionPackageJson
           */
          function updateCachedDocumentPrescriptionPackage(prescriptionPackageJson)
          {
            var prescriptionPackage = PrescriptionPackage.fromJsonObject(prescriptionPackageJson);
            var document = _findDocumentByContentCompositionUid(prescriptionPackage.getCompositionUid());

            if (document)
            {
              document.setContent(prescriptionPackage);
            }
          }

          /**
           * Authorize the given document.
           * @param {TherapyDocument} document
           */
          function authorizeDocument(document)
          {
            if (angular.isDefined(document))
            {
              var content = document.getContent();
              // trigger authorization based on content type, only supporting PrescriptionPackage for now.
              if (content instanceof PrescriptionPackage)
              {
                viewProxy.sendViewAction(viewActionName.authorizeOutpatientPrescription, {
                  patientId: JSON.stringify(activeUpdateData.patientId),
                  prescriptionBundle: JSON.stringify(content),
                  saveOnly: false
                });
              }
            }
          }

          /**
           * Trigger updating the EER prescription package contents for the given document.
           * @param {TherapyDocument} document
           */
          function updatePrescriptionPackage(document)
          {
            if (angular.isDefined(document))
            {
              var content = document.getContent();
              // trigger authorization based on content type, only supporting PrescriptionPackage for now.
              if (content instanceof PrescriptionPackage)
              {
                viewProxy.sendViewAction(viewActionName.updateOutpatientPrescription, {
                  patientId: activeUpdateData.patientId,
                  prescriptionPackageId: content.getPrescriptionPackageId(),
                  compositionUid: content.getCompositionUid()
                });
              }
            }
          }

          /**
           * Delete the given document.
           * @param {TherapyDocument} document
           */
          function deleteDocument(document)
          {
            if (angular.isDefined(document))
            {
              var content = document.getContent();
              // trigger deletion based on content type, only supporting PrescriptionPackage for now.
              if (content instanceof PrescriptionPackage)
              {
                viewProxy.sendViewAction(viewActionName.deleteOutpatientPrescription, {
                  patientId: activeUpdateData.patientId,
                  compositionUid: content.getCompositionUid()
                });
              }
            }
          }

          /**
           * @param {string} compositionUid
           * @returns {TherapyDocument | null}
           * @private
           */
          function _findDocumentByContentCompositionUid(compositionUid)
          {
            if (!angular.isDefined(compositionUid))
            {
              return null;
            }

            for (var idx = 0; idx < _documentCache.length; idx++)
            {
              var documentContent = _documentCache[idx].getContent();

              if (compositionUidUtils.isCompositionUidSameOrNewer(
                      _documentCache[idx].getContent().getCompositionUid(), compositionUid))
              {
                return _documentCache[idx];
              }
            }

            return null;
          }
        }]);
})();