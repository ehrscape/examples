Class.define('app.views.medications.RestApi', 'tm.jquery.Object', {
  statics: {
    SERVLET_PATH_MEDICATION_DATA: '/medicationdata',
    SERVLET_PATH_FIND_MEDICATIONS: '/findmedications',
    SERVLET_PATH_FIND_MEDICATION_PRODUCTS: '/findMedicationProducts',
    SERVLET_PATH_FIND_SIMILAR_MEDICATIONS: '/findSimilarMedications',
    SERVLET_PATH_SAVE_MEDICATIONS_ORDER: '/saveMedicationsOrder',
    SERVLET_PATH_MODIFY_THERAPY: '/modifyTherapy',
    SERVLET_PATH_GET_THERAPY_CHANGE_TYPE: '/getTherapyChangeTypes',
    SERVLET_PATH_CREATE_ADMINISTRATION_TASK: '/createAdministrationTask',
    SERVLET_PATH_CONFIRM_THERAPY_ADMINISTRATION: '/confirmTherapyAdministration',
    SERVLET_PATH_GET_MEDICATION_DATA_FOR_MULTIPLE_IDS: '/medicationDataForMultipleIds',
    SERVLET_PATH_RESCHEDULE_TASKS: '/rescheduleTasks',
    SERVLET_PATH_RESCHEDULE_TASK: '/rescheduleTask',
    SERVLET_PATH_GET_THERAPY_AUDIT_TRAIL: '/getTherapyAuditTrail',
    SERVLET_PATH_SET_ADMINISTRATION_TITRATION_DOSE: '/setAdministrationTitratedDose',
    SERVLET_PATH_GET_THERAPY: '/getTherapy',
    SERVLET_PATH_GET_UNLICENSED_MEDICATION_WARNING: '/getUnlicensedMedicationWarning',
    SERVLET_PATH_SET_ADMINISTRATION_DOCTORS_COMMENT: '/setAdministrationDoctorsComment',
    SERVLET_PATH_GET_TEMPLATES: '/getTherapyTemplates',
    SERVLET_PATH_GET_MEDICATION_ID_FOR_BARCODE: '/getMedicationIdForBarcode',
    SERVLET_PATH_GET_ADMINISTRATION_TASK_FOR_BARCODE: '/getAdministrationTaskForBarcode',
    SERVLET_PATH_GET_ORIGINAL_THERAPY_ID: '/getOriginalTherapyId'
  },

  view: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * Helper with conditional execution for less code.
   * @param {Boolean} prevent
   * @private
   */
  _showLoaderMask: function(prevent)
  {
    if (!prevent)
    {
      this.getView().showLoaderMask();
    }
  },

  /**
   * Helper with conditional execution for less code.
   * @param {Boolean} prevent
   * @private
   */
  _hideLoaderMask: function(prevent)
  {
    if (!prevent)
    {
      this.getView().hideLoaderMask();
    }
  },

  /**
   * @param medicationId
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadMedicationData: function(medicationId, preventMask)
  {
    var view = this.getView();
    var self = this;
    var medicationDataUrl = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_MEDICATION_DATA;
    var params = {medicationId: medicationId};
    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);

    view.loadViewData(medicationDataUrl, params, null, function(data)
        {
          self._hideLoaderMask(preventMask);
          var medicationData = !tm.jquery.Utils.isEmpty(data) ?
              app.views.medications.common.dto.MedicationData.fromJson(data) : null;
          deferred.resolve(medicationData);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * @param {Array<String>} ids
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Deferred}
   */
  loadMedicationDataForMultipleIds: function(ids, preventMask)
  {
    var view = this.getView();
    var self = this;
    var deferred = new tm.jquery.Deferred;
    var restUrl = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_GET_MEDICATION_DATA_FOR_MULTIPLE_IDS;
    var params = {medicationIds: JSON.stringify(ids)};

    this._showLoaderMask(preventMask);

    view.loadViewData(restUrl, params, null, function(data)
        {
          self._hideLoaderMask(preventMask);

          if (!tm.jquery.Utils.isArray(data)) data = [data];
          var medicationData = data.map(function(item)
          {
            return app.views.medications.common.dto.MedicationData.fromJson(item);
          });
          deferred.resolve(medicationData);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred;
  },

  /**
   * @param {String} searchQuery
   * @param {Array<String>|null} [additionalFilters=null]
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadMedications: function(searchQuery, additionalFilters, preventMask)
  {
    var view = this.getView();
    var self = this;

    var url = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_FIND_MEDICATIONS;

    var params = {
      searchQuery: searchQuery,
      careProviderId: view.getCareProviderId()
    };

    if (additionalFilters)
    {
      params.additionalFilters = JSON.stringify(additionalFilters);
    }

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);

    view.loadViewData(url, params, null, function(data)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(data);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * @param {String} medicationId
   * @param {Array<Object>} routes
   * @param {String} routes.id
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadSimilarMedications: function(medicationId, routes, preventMask) //Similar medications have same generic and route
  {
    var view = this.getView();
    var self = this;

    var url = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_FIND_SIMILAR_MEDICATIONS;
    var routeIds = routes ? routes.map(function(route)
    {
      return route.id;
    }) : null;

    var params = {
      medicationId: medicationId,
      routeIds: routeIds
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);

    view.loadViewData(url, params, null, function(data)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(data);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * @param {string} medicationId
   * @param {Array<number>} [routes=null]
   * @param {boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadMedicationProducts: function(medicationId, routes, preventMask)
  {
    var view = this.getView();
    var self = this;

    var url = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_FIND_MEDICATION_PRODUCTS;

    var routeIds = routes ? routes.map(function(route)
    {
      return route.id;
    }) : null;

    var params = {
      medicationId: medicationId,
      routeIds: routeIds
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);

    view.loadViewData(url, params, null, function(data)
        {
          self._hideLoaderMask(preventMask);
          data = data.map(function convert(item)
          {
            return new app.views.medications.common.dto.Medication(item);
          });
          deferred.resolve(data);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * @param {Array<app.views.medications.common.dto.SaveMedicationOrder>} medicationOrders
   * @param {Object} prescriber
   * @param {Date|null} saveDateTime
   * @param {String|null} lastLinkName
   * @param {Boolean} preventMask
   * @returns {tm.jquery.Promise}
   */
  saveMedicationsOrder: function(medicationOrders, prescriber, saveDateTime, lastLinkName, preventMask)
  {
    var self = this;
    var view = this.getView();
    var viewHubNotifier = view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_SAVE_HUB;

    var url = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_SAVE_MEDICATIONS_ORDER;

    var centralCaseData = view.getCentralCaseData();

    var params = {
      patientId: view.getPatientId(),
      medicationOrders: JSON.stringify(medicationOrders),
      centralCaseId: centralCaseData ? centralCaseData.centralCaseId : null,
      hospitalizationStart: view.getHospitalizationStart(),
      careProviderId: view.getCareProviderId(),
      prescriber: JSON.stringify(prescriber),
      lastLinkName: lastLinkName,
      saveDateTime: saveDateTime ? JSON.stringify(saveDateTime) : null,
      language: view.getViewLanguage()
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    viewHubNotifier.actionStarted(hubAction);
    view.loadPostViewData(url, params, null,
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionEnded(hubAction);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionFailed(hubAction);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {Boolean} preventMask
   * @returns {tm.jquery.Promise}
   */
  loadTherapyChangeReasonTypeMap: function(preventMask)
  {
    var self = this;
    var view = this.getView();
    var url = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_GET_THERAPY_CHANGE_TYPE;

    var params = {
      language: view.getViewLanguage()
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);

    view.loadViewData(url, params, null,
        function(data)
        {
          self._hideLoaderMask(preventMask);
          if (data)
          {
            // append suspend on admission reasons to suspend reasons so they can be used under one key in the suspend dialog
            // then simply check if it belongs to one but not the other group
            var orderEnums = app.views.medications.TherapyEnums.actionReasonTypeEnum;
            if (data.hasOwnProperty(orderEnums.SUSPEND_ADMISSION) &&
                tm.jquery.Utils.isArray(data[orderEnums.SUSPEND_ADMISSION]))
            {
              if (!data.hasOwnProperty(orderEnums.SUSPEND))
              {
                data[orderEnums.SUSPEND] = [];
              }
              data[orderEnums.SUSPEND] = data[orderEnums.SUSPEND].concat(data[orderEnums.SUSPEND_ADMISSION]);
            }
          }
          else
          {
            data = {};
          }
          deferred.resolve(data);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {app.views.medications.common.dto.TherapyChangeReason} changeReason
   * @param {Object} prescriber
   * @param {Boolean} hasStarted
   * @param {Date} saveDateTime
   * @param {Boolean} preventMask
   * @returns {tm.jquery.Promise}
   */
  modifyTherapy: function(therapy, changeReason, prescriber, hasStarted, saveDateTime, preventMask)
  {
    var self = this;
    var view = this.getView();
    var viewHubNotifier = view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_SAVE_HUB;
    var url = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_MODIFY_THERAPY;

    var params = {
      patientId: view.getPatientId(),
      therapy: JSON.stringify(therapy),
      changeReason: changeReason ? JSON.stringify(changeReason) : null,
      centralCaseId: view.getCentralCaseData() ? view.getCentralCaseData().centralCaseId : null,
      careProviderId: view.getCareProviderId(),
      prescriber: JSON.stringify(prescriber),
      therapyAlreadyStarted: hasStarted === true,
      saveDateTime: saveDateTime ? JSON.stringify(saveDateTime) : null
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    viewHubNotifier.actionStarted(hubAction);
    view.loadPostViewData(url, params, null,
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionEnded(hubAction);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionFailed(hubAction);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @param {Boolean} requestSupply
   * @param {Boolean} preventMask
   * @returns {tm.jquery.Promise}
   */
  createAdministrationTask: function(therapy, administration, requestSupply, preventMask)
  {
    var self = this;
    var view = this.getView();
    var viewHubNotifier = view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_CONFIRM_ADMINISTRATION_HUB;
    var url = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_CREATE_ADMINISTRATION_TASK;

    var params = {
      therapyCompositionUid: therapy.getCompositionUid(),
      ehrOrderName: therapy.getEhrOrderName(),
      patientId: view.getPatientId(),
      administration: JSON.stringify(administration),
      requestSupply: requestSupply === true
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    viewHubNotifier.actionStarted(hubAction);
    view.loadPostViewData(url, params, null,
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionEnded(hubAction);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionFailed(hubAction);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @param {Boolean} editMode
   * @param {Boolean} requestSupply
   * @param {Boolean} preventMask
   * @returns {tm.jquery.Promise}
   */
  confirmAdministrationTask: function(therapy, administration, editMode, requestSupply, preventMask)
  {
    var self = this;
    var view = this.getView();
    var viewHubNotifier = view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_CONFIRM_ADMINISTRATION_HUB;
    var url = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_CONFIRM_THERAPY_ADMINISTRATION;

    var centralCaseData = view.getCentralCaseData();

    var params = {
      therapyCompositionUid: therapy.getCompositionUid(),
      ehrOrderName: therapy.getEhrOrderName(),
      patientId: view.getPatientId(),
      editMode: editMode,
      administration: JSON.stringify(administration),
      centralCaseId: centralCaseData ? centralCaseData.centralCaseId : null,
      careProviderId: view.getCareProviderId(),
      requestSupply: requestSupply === true
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    viewHubNotifier.actionStarted(hubAction);
    view.loadPostViewData(url, params, null,
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionEnded(hubAction);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionFailed(hubAction);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {string} taskId
   * @param {Date} selectedTimestamp
   * @param {string} therapyId
   * @param {Boolean} moveSingle
   * @param {Boolean} preventMask
   * @returns {tm.jquery.Promise}
   */
  rescheduleTasks: function(taskId, selectedTimestamp, therapyId, moveSingle, preventMask)
  {
    var self = this;
    var deferred = tm.jquery.Deferred.create();
    var view = this.getView();
    var viewHubNotifier = this.view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_MOVE_ADMINISTRATION_HUB;
    viewHubNotifier.actionStarted(hubAction);
    var params = {
      patientId: view.getPatientId(),
      taskId: taskId,
      newTime: JSON.stringify(selectedTimestamp),
      therapyId: therapyId
    };

    var url = this.view.getViewModuleUrl() + (moveSingle === true ?
            app.views.medications.RestApi.SERVLET_PATH_RESCHEDULE_TASK :
            app.views.medications.RestApi.SERVLET_PATH_RESCHEDULE_TASKS);

    this._showLoaderMask(preventMask);

    this.view.loadPostViewData(url, params, null,
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionEnded(hubAction);
          deferred.resolve()
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionFailed(hubAction);
          deferred.reject()
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {boolean} [preventMask=false]
   * @returns {tm.jquery.Deferred}
   */
  loadAuditTrailData: function(therapy, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var auditTrailUrl =
        view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_GET_THERAPY_AUDIT_TRAIL;
    var params = {
      patientId: view.getPatientId(),
      compositionId: therapy.getCompositionUid(),
      ehrOrderName: therapy.getEhrOrderName(),
      language: view.getViewLanguage()
    };
    if (view.getPatientHeightInCm())
    {
      params.patientHeight = view.getPatientHeightInCm();
    }
    this._showLoaderMask(preventMask);
    view.loadViewData(auditTrailUrl, params, null, function(auditTrailData)
        {
          self._hideLoaderMask(preventMask);
          var therapyAuditTrail = app.views.medications.common.dto.TherapyAuditTrail.fromJson(auditTrailData);
          deferred.resolve(therapyAuditTrail);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });
    return deferred.promise();
  },

  /**
   * @param {string} latestTherapyId
   * @param {object} administration
   * @param {boolean} markGiven
   * @param {date|null} until
   * @param {boolean} [preventMask=false]
   * @returns {tm.jquery.Deferred}
   */
  setAdministrationTitrationDose: function(latestTherapyId, administration, markGiven, until, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();

    var setTitratedDoseUrl = view.getViewModuleUrl()
        + app.views.medications.RestApi.SERVLET_PATH_SET_ADMINISTRATION_TITRATION_DOSE;

    var params = {
      patientId: view.getPatientId(),
      latestTherapyId: latestTherapyId,
      taskId: administration.taskId,
      administration: JSON.stringify(administration),
      confirmAdministration: markGiven,
      until: until ? JSON.stringify(until) : null,
      centralCaseId: view.getCentralCaseData() && view.getCentralCaseData().centralCaseId ?
          view.getCentralCaseData().centralCaseId : null,
      careProviderId: view.getCareProviderId()
    };

    this._showLoaderMask(preventMask);
    view.loadPostViewData(setTitratedDoseUrl, params, null,
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {String} id
   * @param {boolean} [preventMask=false]
   * @returns {tm.jquery.Deferred}
   */
  loadTherapy: function(id, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();

    var getTherapyUrl = view.getViewModuleUrl()
        + app.views.medications.RestApi.SERVLET_PATH_GET_THERAPY;

    var params = {
      patientId: view.getPatientId(),
      therapyId: id
    };

    this._showLoaderMask(preventMask);
    view.loadViewData(getTherapyUrl, params, null, function(data)
        {
          self._hideLoaderMask(preventMask);
          var therapy = !tm.jquery.Utils.isEmpty(data) ?
              app.views.medications.common.TherapyJsonConverter.convert(data) : null;
          deferred.resolve(therapy);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * @param {String} [templateMode=null] Additional filter for the type of templates you want to get ('OUTPATIENT').
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Deferred}
   */
  loadOrderingTemplates: function(templateMode, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var getTemplatesUrl =
        view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_GET_TEMPLATES;

    var params = {
      patientId: view.getPatientId(),
      templateMode: templateMode,
      careProviderId: view.getCareProviderId(),
      referenceWeight: view.getReferenceWeight(),
      patientHeight: view.getPatientHeightInCm()
    };

    this._showLoaderMask(preventMask);

    view.loadViewData(getTemplatesUrl, params, null,
        function onSuccess(data)
        {
          self._hideLoaderMask(preventMask);

          data.organizationTemplates.forEach(function(template)
          {
            template.templateElements = template.templateElements.map(function(element)
            {
              return app.views.medications.common.dto.TherapyTemplateElement.fromJson(element);
            });
          });
          data.patientTemplates.forEach(function(template)
          {
            template.templateElements = template.templateElements.map(function(element)
            {
              return app.views.medications.common.dto.TherapyTemplateElement.fromJson(element);
            });
          });
          data.userTemplates.forEach(function(template)
          {
            template.templateElements = template.templateElements.map(function(element)
            {
              return app.views.medications.common.dto.TherapyTemplateElement.fromJson(element);
            });
          });

          deferred.resolve(data);
        },
        function onFailure()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * @returns {tm.jquery.Deferred}
   * @param {boolean} [preventMask=false]
   */
  loadUnlicensedMedicationWarning: function(preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();

    var getTherapyUrl = view.getViewModuleUrl()
        + app.views.medications.RestApi.SERVLET_PATH_GET_UNLICENSED_MEDICATION_WARNING;

    var params = {language: view.getViewLanguage()};

    this._showLoaderMask(preventMask);
    view.loadViewData(getTherapyUrl, params, null, function(data)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(data);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * @param {string} taskId
   * @param {string} doctorsComment
   * @param {boolean} [preventMask=false]
   */
  setAdministrationDoctorsComment: function(taskId, doctorsComment, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var setAdministrationDoctorsCommentUrl = view.getViewModuleUrl()
        + app.views.medications.RestApi.SERVLET_PATH_SET_ADMINISTRATION_DOCTORS_COMMENT;

    var params = {
      taskId: taskId,
      doctorsComment: doctorsComment
    };
    this._showLoaderMask(preventMask);
    view.sendPostRequest(setAdministrationDoctorsCommentUrl, params, function()
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });
    return deferred.promise();
  },

  /**
   * @param {String} barcode
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Deferred}
   */
  getMedicationIdForBarcode: function(barcode, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var getMedicationIdForBarcodeUrl = view.getViewModuleUrl()
        + app.views.medications.RestApi.SERVLET_PATH_GET_MEDICATION_ID_FOR_BARCODE;

    var params = {
      barcode: barcode
    };
    this._showLoaderMask(preventMask);
    view.loadViewData(getMedicationIdForBarcodeUrl, params, null, function(medicationId)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(medicationId);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });
    return deferred.promise();
  },

  /**
   * @param {String} medicationBarcode
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Deferred}
   */
  getAdministrationTaskForBarcode: function(medicationBarcode, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var getAdministrationTaskForBarcodeUrl = view.getViewModuleUrl()
        + app.views.medications.RestApi.SERVLET_PATH_GET_ADMINISTRATION_TASK_FOR_BARCODE;

    var params = {
      patientId: view.getPatientId(),
      medicationBarcode: medicationBarcode
    };
    this._showLoaderMask(preventMask);
    view.loadViewData(getAdministrationTaskForBarcodeUrl, params, null, function(barcodeTaskSearch)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(new app.views.medications.common.dto.BarcodeTaskSearch(barcodeTaskSearch));
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });
    return deferred.promise();
  },

  /**
   * @param {String} therapyId
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Deferred}
   */
  getOriginalTherapyId: function(therapyId, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var getOriginalTherapyIdUrl = view.getViewModuleUrl() +
        app.views.medications.RestApi.SERVLET_PATH_GET_ORIGINAL_THERAPY_ID;

    var params = {
      patientId: view.getPatientId(),
      therapyId: therapyId
    };
    this._showLoaderMask(preventMask);
    view.loadViewData(getOriginalTherapyIdUrl, params, null,
        function(originalTherapyId)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(originalTherapyId);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });
    return deferred.promise();
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});