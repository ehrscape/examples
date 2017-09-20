/*
 * Copyright (c) 2010-2015 Marand d.o.o. (www.marand.com)
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
Class.define('app.views.medications.reconciliation.InpatientMedReconciliationContainer', 'app.views.medications.reconciliation.MedicineReconciliationContainer', {
  suspendedTherapiesPanel: null,
  sourceTherapiesPanel: null,

  sourceTherapiesPanelTitle: "Medication on admission",
  oxygenSaturationSupported: true,

  Constructor: function (config)
  {
    // override before buildGUI sets the instance to parent components
    this.basketTherapyDisplayProvider = new app.views.medications.common.SaveMedicationOrderTherapyDisplayProvider({
      view: config.view
    });

    this.callSuper(config);
  },

  /* Override values for column titles */
  getLeftColumnTitle: function()
  {
    return null;
  },

  /* Override values for column titles */
  getRightColumnTitle: function()
  {
    return this.getView().getDictionary("inpatient.prescription");
  },

  attachTherapyGroupPanels: function(container){
    var self = this;
    var view = this.getView();
    var sourceTherapiesPanel = new app.views.medications.common.TherapyGroupPanel({
      groupTitle: this.getSourceTherapiesPanelTitle(),
      view: this.getView(),
      expanded: true,
      contentData: [],
      attachElementToolbar: function(container){
        self._attachGroupPanelElementToolbar(container, true, true);
      }
    });
    container.add(sourceTherapiesPanel);
    this.sourceTherapiesPanel = sourceTherapiesPanel;

    var suspendedTherapiesPanel = new app.views.medications.common.TherapyGroupPanel({
      groupTitle:  this.getView().getDictionary("suspended") + " / " + this.getView().getDictionary("stop.past"),
      view: view,
      contentData: [],
      expanded: true,
      attachElementToolbar: function(container){
        self._evaluateTherapyAttachGroupPanelElementToolbar(container);
      },
      displayProvider: new app.views.medications.common.SaveMedicationOrderTherapyDisplayProvider({
        showChangeReason: true,
        view: view
      })
    });
    container.add(suspendedTherapiesPanel);

    this.suspendedTherapiesPanel = suspendedTherapiesPanel;
  },

  // override, addin_attachGroupPanelElementToolbarg an extended toolbar to the group panels
  _attachGroupPanelElementToolbar: function(elementContainer, withSuspend, withCancel) {
    var self = this;
    var toolbar = new app.views.medications.reconciliation.TherapyContainerPanelToolbar({
      therapyContainer: elementContainer,
      withSuspend: withSuspend === true,
      withCancel: withCancel === true,
      addToBasketEventCallback: function(therapyContainer){
        self.getTherapySelectionContainer().setSourceTherapyContainer(therapyContainer);
        self.onAddTherapyToBasket(therapyContainer);
      },
      addToBasketWithEditEventCallback: function(therapyContainer){
        self.getTherapySelectionContainer().setSourceTherapyContainer(therapyContainer);
        self.onEditTherapy(therapyContainer);
      },
      cancelEventCallback: function(therapyContainer) {
        self.onAbortTherapy(therapyContainer, false);
      },
      suspendEventCallback: function(therapyContainer){
        self.onSuspendTherapy(therapyContainer, true);
      }
    });
    elementContainer.setToolbar(toolbar);
  },

  _evaluateTherapyAttachGroupPanelElementToolbar: function(therapyContainer) {
    var enums = app.views.medications.TherapyEnums;
    var data = therapyContainer.getData();

    if (data.isReadOnly()) return;

    var therapyStatus = data.getTherapyStatus === 'function' ? data.getTherapyStatus() : data.therapyStatus;

    this._attachGroupPanelElementToolbar(therapyContainer,
        therapyStatus !== enums.therapyStatusEnum.SUSPENDED,
        therapyStatus !== enums.therapyStatusEnum.ABORTED
    );
  },

  _findSourceTherapyContainerBySourceId: function(sourceId){
    if (sourceId)
    {
      var sourceContent = this.getSourceTherapiesPanel().getContent().getComponents();
      for (var idx = 0; idx < sourceContent.length; idx++) {
        if (sourceContent[idx].getData().getTherapy().getCompositionUid() === sourceId)
        {
          return sourceContent[idx];
        }
      }
    }
    return null;
  },

  _suspendTherapy: function(therapyContainer, prescribe)
  {
    var view = this.getView();
    var actionEnums = app.views.medications.TherapyEnums.medicationOrderActionEnum;
    var actionTypeEnum = prescribe === true ? actionEnums.SUSPEND : actionEnums.SUSPEND_ADMISSION;
    var changeReasonMap = view.getTherapyChangeReasonTypeMap();
    var changeReasonDto = null;

    this._suspendOrAbortTherapy(therapyContainer, actionTypeEnum,
        tm.views.medications.MedicationUtils.getFirstOrNullTherapyChangeReason(changeReasonMap, actionTypeEnum));
  },

  _abortTherapy: function(therapyContainer, changeReasonDto)
  {
    this._suspendOrAbortTherapy(therapyContainer,
        app.views.medications.TherapyEnums.medicationOrderActionEnum.ABORT,
        changeReasonDto);
  },

  _suspendOrAbortTherapy: function(therapyContainer, actionType, changeReasonDto)
  {
    changeReasonDto = tm.jquery.Utils.isEmpty(changeReasonDto) ? null : changeReasonDto;

    var actionEnums = app.views.medications.TherapyEnums.medicationOrderActionEnum;
    var isAbort = actionType === actionEnums.ABORT;
    var groupPanel = therapyContainer.getGroupPanel();

    if (groupPanel === this.getSuspendedTherapiesPanel())
    {
      if (actionType !== actionEnums.SUSPEND)
      {
        therapyContainer.getData().setActionEnum(actionType);
        therapyContainer.getData().setChangeReasonDto(changeReasonDto);
        therapyContainer.getToolbar().withSuspend = isAbort;
        therapyContainer.getToolbar().withCancel = !isAbort;
        therapyContainer.refresh();
      }
      else
      {
        this.addToBasket(therapyContainer.getData().getTherapy(), null, therapyContainer, actionType);
        groupPanel.remove(therapyContainer);
      }
    }
    else
    {
      var suspendOrder = new app.views.medications.common.dto.SaveMedicationOrder({
        therapy: therapyContainer.getData().getTherapy(),
        sourceId: therapyContainer.getData().getTherapy().getCompositionUid(),
        actionEnum: actionType,
        changeReasonDto: changeReasonDto
      });
      suspendOrder.getTherapy().rescheduleTherapyTimings(false);

      if (actionType !== actionEnums.SUSPEND)
      {
        this.getSuspendedTherapiesPanel().addElement(suspendOrder);
      }
      else
      {
        this.addToBasket(therapyContainer.getData().getTherapy(), null, therapyContainer, actionType);
      }
      this.markSourceTherapyContainer(therapyContainer);
    }
  },

  getSuspendedTherapiesPanel: function(){
    return this.suspendedTherapiesPanel;
  },

  getSourceTherapiesPanel: function(){
    return this.sourceTherapiesPanel;
  },

  onSuspendTherapy: function(therapyContainer){
    var view = this.getView();
    var self = this;

    var suspendDialog = app.views.medications.reconciliation.SuspendAdmissionTherapyContainer.asDialog(view,
      function(resultData){
        if (resultData != null && resultData.isSuccess())
        {
          var prescribe = resultData.getValue().prescribe;
          self._suspendTherapy(therapyContainer, prescribe === true);
        }
    });
    suspendDialog.show();
  },

  onAbortTherapy: function(therapyContainer, suspend){
    var changeTypeEnums = app.views.medications.TherapyEnums.actionReasonTypeEnum;
    var changeTypeKey = changeTypeEnums.ABORT;
    var self = this;
    var view = this.getView();

    var changeReasonEntryContainer = new app.views.medications.common.ChangeReasonDataEntryContainer({
      titleIcon: "warningYellow_status_48.png",
      titleText: view.getDictionary(suspend === true ?
          "therapy.needs.reason.for.suspending" : "therapy.needs.reason.for.stopping"),
      view: this.getView(),
      changeReasonTypeKey: changeTypeKey
    });

    var suspendConfirmationDialog = this.getView().getAppFactory().createDataEntryDialog(
        this.getView().getDictionary("warning"),
        null,
        changeReasonEntryContainer, function(resultData)
        {
          if (resultData != null && resultData.isSuccess())
          {
            self._abortTherapy(therapyContainer, resultData.value);
          }
        },
        changeReasonEntryContainer.defaultWidth,
        changeReasonEntryContainer.defaultHeight
    );
    suspendConfirmationDialog.show();
  },

  onAddTherapyToBasket: function(therapyContainer)
  {
    therapyContainer.getToolbar().setEnabled(false, true);

    var self = this;
    // if this is a suspended or canceled therapy, we should reactivate it
    var data = therapyContainer.getData();
    var therapy =  therapyContainer.getData().getTherapy();
    var therapyStatus = data.getTherapyStatus === 'function' ? data.getTherapyStatus() : data.therapyStatus;
    var statusEnums = app.views.medications.TherapyEnums.therapyStatusEnum;

    if (therapyStatus === statusEnums.SUSPENDED || therapyStatus === statusEnums.ABORTED)
    {
      data.therapyStatus = statusEnums.NORMAL;
      var admissionPanelElement = this.getSourceTherapiesPanel().getElementContainerByData(therapyContainer.getData());
      this.getTherapySelectionContainer().setSourceTherapyContainer(admissionPanelElement); // reroute to the original container
      this.getSuspendedTherapiesPanel().remove(therapyContainer);
    }
    therapy.rescheduleTherapyTimings(this.getView().getPresetDate(), false);
    if (this._isPrescriptionValid(therapy))
    {
      this._getTherapyDto(therapy, function (therapyDto)
      {
        if (therapyDto.isBaselineInfusion())
        {
          self.baselineInfusionIntervals.push({
            startMillis: therapyDto.getStart().getTime(),
            endMillis: therapyDto.getEnd() ? therapyDto.getEnd().getTime() : null
          })
        }

        // since we don't enter oxygen saturation at admission, prevent the therapy to be ordered if the saturation is
        // missing
        if (therapyDto.isOrderTypeOxygen() &&
            (!therapyDto.getMaxTargetSaturation() || !therapyDto.getMinTargetSaturation()))
        {
          therapyDto.setCompleted(false);
        }

        self.addToBasket(therapyDto, null, therapyContainer);
        therapyContainer.getToolbar().setEnabled(true, true);
      });
    }
    else
    {
      therapyContainer.getToolbar().setEnabled(true, true);
    }
  },

  /* Override, custom object in the basket. */
  addToBasket: function (therapy, changeReason, sourceContainer, action)
  {
    var actionEnum = action ? action : app.views.medications.TherapyEnums.medicationOrderActionEnum.PRESCRIBE;
    var sourceData = null;
    // in case of add with edit, no source container is passed over, check if there is one
    if (!sourceContainer) {
      sourceContainer = this.getTherapySelectionContainer().getSourceTherapyContainer();
      // if the source container was not defined, but we have a source container, edit was conducted
      if (sourceContainer)
      {
        actionEnum = app.views.medications.TherapyEnums.medicationOrderActionEnum.EDIT;
      }
    }

    if (sourceContainer)
    {
      sourceData = sourceContainer.getData();
    }

    if (sourceData instanceof app.views.medications.common.dto.SaveMedicationOrder)
    {
      // we're reading a basket item, reroute the source container if there is one
      sourceContainer = this._findSourceTherapyContainerBySourceId(sourceContainer.getData().getSourceId());
      this.getTherapySelectionContainer().setSourceTherapyContainer(sourceContainer);
      sourceData = sourceContainer ? sourceContainer.getData() : null;
    }

    var medicationOrder = new app.views.medications.common.dto.SaveMedicationOrder({
      therapy: therapy,
      actionEnum: actionEnum,
      changeReasonDto: changeReason ? changeReason : null
    });

    if (sourceContainer && sourceData instanceof app.views.medications.reconciliation.dto.MedicationOnAdmission)
    {
      medicationOrder.setSourceId(sourceData.getTherapy().getCompositionUid());
      this.markSourceTherapyContainer(sourceContainer);
    }

    this.getBasketContainer().addTherapy(medicationOrder);
  },

  /* override */
  unmarkSourceTherapyContainer: function(container) {
    container.show();
  },

  /* override */
  markSourceTherapyContainer: function(container) {
    container.hide();
  },

  /***
   * Override, check if we edited any of the existing therapies and remove them from the suspended/canceled
   * panel if required.
   */
  onTherapySelectionColumnConfirmEdit: function(therapy) {
    // check if we edited one of the therapies (as oppose to adding a completly new one)
    var sourceContainer = this.getTherapySelectionContainer().getSourceTherapyContainer();
    if (sourceContainer != null && sourceContainer.getParent() === this.getSuspendedTherapiesPanel().getContent()) {
      this.getSuspendedTherapiesPanel().remove(sourceContainer);
      var admissionContainer = this.getSourceTherapiesPanel().getElementContainerByData(sourceContainer.getData());
      this.getTherapySelectionContainer().setSourceTherapyContainer(admissionContainer); // reroute to the original container
    }
    return this.callSuper();
  },

  /* override to implement change reason required rules*/
  onEditTherapy: function (therapyContainer, callback)
  {
    var data = therapyContainer.getData();

    this.getTherapySelectionContainer().editTherapy(therapyContainer,
        data instanceof app.views.medications.reconciliation.dto.MedicationOnAdmission ||
        (data instanceof app.views.medications.common.dto.SaveMedicationOrder && !tm.jquery.Utils.isEmpty(data.getSourceId())),
        callback);
  },

  /* override, unmark if it's from medication on admission */
  onTherapyRemovedFromBasket: function (elementData)
  {
    this.callSuper();
    var self = this;
    if (!tm.jquery.Utils.isEmpty(elementData))
    {
      var sourceId = elementData.getSourceId();
      var sourceContainer = this._findSourceTherapyContainerBySourceId(sourceId);

      if (sourceContainer)
      {
        this.unmarkSourceTherapyContainer(sourceContainer);
      }
    }
    if (this.isWarningsEnabled())
    {
      this._updateCalculatedBnfPercentage();
    }
  },


  getSourceTherapiesPanelTitle: function()
  {
    return this.sourceTherapiesPanelTitle;
  }
});