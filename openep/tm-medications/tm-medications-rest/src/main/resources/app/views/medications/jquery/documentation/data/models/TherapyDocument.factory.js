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
      .factory('tm.angular.medications.documentation.data.models.TherapyDocument',
          ['tm.angular.medications.documentation.data.models.PrescriptionPackage',
            'tm.angular.medications.documentation.data.models.ExternalPrescriptionPackage',
            'tm.angular.medications.documentation.data.models.MentalHealthDocumentContent',
            'tm.angular.medications.documentation.data.models.TherapyDocumentType',
            function(PrescriptionPackage, ExternalPrescriptionPackage, MentalHealthDocumentContent, TherapyDocumentTypeEnum)
            {
              /** Constructor */
              function TherapyDocument(documentType, createTimestamp, creator, careProvider, content)
              {
                this.documentType = documentType;
                this.createTimestamp = createTimestamp;
                this.creator = creator;
                this.careProvider = careProvider;
                if (!angular.isObject(content))
                {
                  this.content = new PrescriptionPackage();
                }
                else
                {
                  this.content = content;
                }
              }

              TherapyDocument.fromJsonObject = function(object)
              {
                var therapyDocument = new TherapyDocument();
                if (angular.isDefined(object))
                {
                  angular.extend(therapyDocument, object);
                  if (angular.isDefined(object.content))
                  {
                    if (therapyDocument.getDocumentType() === TherapyDocumentTypeEnum.EXTERNAL_EER_PRESCRIPTION)
                    {
                      therapyDocument.content = ExternalPrescriptionPackage.fromJsonObject(object.content);
                    }
                    else if (therapyDocument.getDocumentType() === TherapyDocumentTypeEnum.T2 ||
                        therapyDocument.getDocumentType() === TherapyDocumentTypeEnum.T3)
                    {
                      therapyDocument.content = MentalHealthDocumentContent.fromJsonObject(object.content);
                    }
                    else
                    {
                      // fallback or default
                      therapyDocument.content = PrescriptionPackage.fromJsonObject(object.content);
                    }
                  }
                }
                return therapyDocument;
              };

              /** Public methods */

              /**
               * Returns the therapy document type.
               * @returns {tm.angular.medications.documentation.data.models.TherapyDocumentType}
               */
              TherapyDocument.prototype.getDocumentType = function()
              {
                return this.documentType;
              };

              TherapyDocument.prototype.getCreatedTimestamp = function()
              {
                return this.createTimestamp;
              };

              TherapyDocument.prototype.getCreator = function()
              {
                return this.creator;
              };

              TherapyDocument.prototype.getCareProvider = function()
              {
                return this.careProvider;
              };

              TherapyDocument.prototype.getContent = function()
              {
                return this.content;
              };

              TherapyDocument.prototype.setContent = function(value)
              {
                this.content = value;
              };

              return TherapyDocument;
            }]);
})();