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
Class.define('tm.views.medications.pharmacists.ReviewContainerEditContentCard', 'tm.views.medications.pharmacists.ReviewContainerViewContentCard', {
  cls: "edit-card",

  therapyRemovedCallback: null,

  reminderComponent: null,
  daysSupplyComponent: null,
  supplyContainer: null,
  showSupply: null,

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
    this._setKeyboardShortcuts(this);
  },

  /* override */
  _buildGUI: function ()
  {
    var self = this;
    var view = this.getReviewContainer().getView();
    var data = this.getReviewContainer().getReviewData();
    var valueKeyEnums = app.views.medications.TherapyEnums.therapyProblemDescriptionEnum;

    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));

    var therapiesListContainer = new tm.jquery.Container({
      cls: "therapy-list-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0)
    });

    var basicDataRow = new tm.jquery.Container({
      cls: "basic-data-row",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0)
    });
    basicDataRow.layout.setFlexFlow(tm.jquery.flexbox.FlexFlow.create("row", "wrap"));

    var referBackButtonYes = new tm.jquery.RadioButton({
      labelText: view.getDictionary("yes"),
      labelAlign: "right",
      labelCls: "TextData",
      value: true,
      checked: data.getReferBackToPrescriber() === true
    });
    var referBackButtonNo = new tm.jquery.RadioButton({
      labelText: view.getDictionary("no"),
      labelAlign: "right",
      labelCls: "TextData",
      value: false,
      checked: data.getReferBackToPrescriber() === false
    });
    var referBackRadioButtonGroup = new tm.jquery.RadioButtonGroup({
      radioButtons: [referBackButtonYes, referBackButtonNo]
    });
    var referBackContainer = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary("refer.back.to.prescriber"),
      contentComponent: view.getAppFactory().createHRadioButtonGroupContainer(referBackRadioButtonGroup)
    });
    referBackContainer.getContentComponent().setCls("radiobutton-group-container");
    basicDataRow.add(referBackContainer);

    var reminderComponent = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary("reminder"),
      contentComponent: new tm.jquery.DatePicker({
        minDate: CurrentTime.get(),
        viewMode: "overlay",
        date: tm.jquery.Utils.isEmpty(data.getReminderDate()) ? null : new Date(data.getReminderDate().getTime()),
        currentTimeProvider: function()
        {
          return CurrentTime.get();
        }
      })
    });
    reminderComponent.getContentComponent().getField().setFlex(tm.jquery.flexbox.item.Flex.create(1, 1, "auto"));
    this._setKeyboardShortcuts(reminderComponent.getContentComponent().getField());
    reminderComponent.getContentComponent().on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component){
         self.getReminderNoteComponent().setEnabled(!tm.jquery.Utils.isEmpty(component.getDate()));
    });
    basicDataRow.add(reminderComponent);

    var reminderNoteContainer = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary("reminder.note"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      contentComponent: new tm.jquery.TextField({
        value: tm.jquery.Utils.isEmpty(data) ? null : data.getReminderNote(),
        enabled: !tm.jquery.Utils.isEmpty(data.getReminderDate()),
        width: 300
      })
    });
    this._setKeyboardShortcuts(reminderNoteContainer.getContentComponent());
    basicDataRow.add(reminderNoteContainer);

    var drugRelatedProblemDescContainer = new tm.views.medications.pharmacists.ProblemDescriptionEditContainer({
      titleText: view.getDictionary("pharmacotherapeutic.drug.related"),
      problemDescription: this.getReviewContainer().getReviewData().getDrugRelatedProblem(),
      reviewContainerContentCard: this,
      valueKeys: {
        categories: valueKeyEnums.DRUG_RELATED_PROBLEM_CATEGORY,
        outcome: valueKeyEnums.DRUG_RELATED_PROBLEM_OUTCOME,
        impact: valueKeyEnums.DRUG_RELATED_PROBLEM_IMPACT
      }
    });
    this._setKeyboardShortcuts(drugRelatedProblemDescContainer.getRecommendationField());

    var pharmacokineticIissueDescContainer = new tm.views.medications.pharmacists.ProblemDescriptionEditContainer({
      titleText: view.getDictionary("pharmacokinetic.consultation"),
      problemDescription: this.getReviewContainer().getReviewData().getPharmacokineticIssue(),
      reviewContainerContentCard: this,
      valueKeys: {
        categories: valueKeyEnums.PHARMACOKINETIC_ISSUE_CATEGORY,
        outcome: valueKeyEnums.PHARMACOKINETIC_ISSUE_OUTCOME,
        impact: valueKeyEnums.PHARMACOKINETIC_ISSUE_IMPACT
      }
    });
    this._setKeyboardShortcuts(pharmacokineticIissueDescContainer.getRecommendationField());

    var patientRelatedDescContainer = new tm.views.medications.pharmacists.ProblemDescriptionEditContainer({
      titleText: view.getDictionary("patient.related"),
      problemDescription: this.getReviewContainer().getReviewData().getPatientRelatedProblem(),
      reviewContainerContentCard: this,
      valueKeys: {
        categories: valueKeyEnums.PATIENT_RELATED_PROBLEM_CATEGORY,
        outcome: valueKeyEnums.PATIENT_RELATED_PROBLEM_OUTCOME,
        impact: valueKeyEnums.PATIENT_RELATED_PROBLEM_IMPACT
      }
    });
    this._setKeyboardShortcuts(patientRelatedDescContainer.getRecommendationField());

    var overallRecommendationContainer = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary("overall.recommendation"),
      //contentComponent: new tm.views.medications.pharmacists.ResizingTextArea({
      contentComponent: new tm.jquery.TextArea({
        rows: 1,
        autoHeight: true,
        value: tm.jquery.Utils.isEmpty(data) ? null : data.getOverallRecommendation(),
        placeholder: "..."
      })
    });
    overallRecommendationContainer.getContentComponent().on(tm.jquery.ComponentEvent.EVENT_TYPE_FOCUS_GAINED, function (component)
    {
      component.getDom().style.minHeight = "122px";
    });
    this._setKeyboardShortcuts(overallRecommendationContainer.getContentComponent());

    this.add(therapiesListContainer);
    if (this.showSupply)
    {
      this.supplyContainer = new tm.views.medications.common.VerticallyTitledComponent({
        cls: "vertically-titled-component suppy-row",
        titleText: view.getDictionary("supply") + " (" + view.getDictionary("in.days") + ")"
      });
      this.supplyContainer.getContentComponent().setStyle("min-height: 30px;");
      this.add(this.supplyContainer);
    }
    this.add(basicDataRow);
    this.add(drugRelatedProblemDescContainer);
    this.add(pharmacokineticIissueDescContainer);
    this.add(patientRelatedDescContainer);
    this.add(overallRecommendationContainer);

    this.therapiesListContainer = therapiesListContainer;
    this.referBackComponent = referBackRadioButtonGroup;
    this.reminderComponent = reminderComponent.getContentComponent();
    this.reminderNoteComponent = reminderNoteContainer.getContentComponent();
    this.drugRelatedProblemDescContainer = drugRelatedProblemDescContainer;
    this.pharmacokineticIissueDescContainer = pharmacokineticIissueDescContainer;
    this.patientRelatedDescContainer = patientRelatedDescContainer;
    this.overallRecommendationComponent = overallRecommendationContainer.getContentComponent();
  },

  _setKeyboardShortcuts: function(container)
  {
    var self = this;

    container.onKey(new tm.jquery.event.KeyStroke({key: "esc", altKey: false, ctrlKey: false, shiftKey: false}),
        function ()
        {
          // trigger ESC on a minor timeout due to issues with DatePicker
          setTimeout(function(){
            self.getReviewContainer().onCancelEdit();
          }, 10);
        });

    container.onKey(new tm.jquery.event.KeyStroke({key: "return", altKey: false, ctrlKey: true, shiftKey: false}),
        function ()
        {
          self.getReviewContainer().onConfirmReview(self.getEditorData());
        });
  },

  _loadDataAndPopulateSupplyContainer: function()
  {
    var self = this;
    var view = this.getReviewContainer().getView();

    var therapiesListContainer = this.getTherapiesListContainer();
    var presentTherapies = therapiesListContainer.getComponents();

    var therapyDay = presentTherapies[0].getData(); // [TherapyDayDto.java]
    var therapy = therapyDay.therapy;

    var patientId = view.getPatientId();
    var params = {
      patientId: patientId,
      therapyCompositionUid: therapy.compositionUid
    };
    var getSupplyTaskUrl =
        view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_SUPPLY_DATA_FOR_PHARMACIST_REVIEW;
    view.loadViewData(getSupplyTaskUrl, params, null, function(supplyData)
    {
      self._createSupplyContainerContent(supplyData);
    });
  },

  _createSupplyContainerContent: function(supplyData)
  {
    var view = this.getReviewContainer().getView();
    var data = this.getReviewContainer().getReviewData();
    var medicationSupplyEnums = app.views.medications.TherapyEnums.medicationSupplyTypeEnum;

    if (this.showSupply)
    {
      this.supplyContainer.getContentComponent().removeAll();
      this.supplyContainer.getContentComponent().layout.setFlexFlow(tm.jquery.flexbox.FlexFlow.create("row", "wrap"));

      if (!supplyData.processExists)
      {
        var supplyPatientsStock = new tm.jquery.RadioButton({
          labelText: view.getDictionary("MedicationSupplyTypeEnum.PATIENTS_OWN"),
          labelAlign: "right",
          labelCls: "TextData",
          uncheckable: true,
          value: medicationSupplyEnums.PATIENTS_OWN,
          checked: data.getMedicationSupplyType() === medicationSupplyEnums.PATIENTS_OWN
        });
        var supplyNonStock = new tm.jquery.RadioButton({
          labelText: view.getDictionary("MedicationSupplyTypeEnum.NON_WARD_STOCK"),
          labelAlign: "right",
          labelCls: "TextData",
          uncheckable: true,
          value: medicationSupplyEnums.NON_WARD_STOCK,
          checked: data.getMedicationSupplyType() === medicationSupplyEnums.NON_WARD_STOCK
        });
        var supplyOneStopSupply = new tm.jquery.RadioButton({
          labelText: view.getDictionary("MedicationSupplyTypeEnum.ONE_STOP_DISPENSING"),
          labelAlign: "right",
          labelCls: "TextData",
          uncheckable: true,
          value: medicationSupplyEnums.ONE_STOP_DISPENSING,
          checked: data.getMedicationSupplyType() === medicationSupplyEnums.ONE_STOP_DISPENSING
        });
        var supplyRadioButtonGroup = new tm.jquery.RadioButtonGroup({
          radioButtons: [supplyPatientsStock, supplyNonStock, supplyOneStopSupply]
        });

        var supplyDaysContainer = new tm.jquery.Container({
          cls: "supply-days-container",
          layout: tm.jquery.HFlexboxLayout.create("flex-center", "center", 0),
          flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
        });
        var supplyDaysField = new tm.jquery.NumberField({
          width: 40,
          maxLength: 3,
          formatting: {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: 0},
          value: data.getDaysSupply(),
          flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
        });

        supplyDaysContainer.add(new tm.jquery.Label({
          cls: "TextData",
          html: view.getDictionary("for"),
          flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
        }));
        supplyDaysContainer.add(supplyDaysField);
        supplyDaysContainer.add(new tm.jquery.Label({
          cls: "TextData",
          html: view.getDictionary("days"),
          flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
        }));

        this.medicationSupplyTypeComponent = supplyRadioButtonGroup;
        this.daysSupplyComponent = supplyDaysField;

        this.supplyContainer.getContentComponent().add(view.getAppFactory().createHRadioButtonGroupContainer(supplyRadioButtonGroup));
        this.supplyContainer.getContentComponent().add(supplyDaysContainer);
      }
      else
      {
        var supplyText = view.getDictionary("process.exists.with.no.reminder.task");
        if (supplyData.taskId && !supplyData.isDismissed)
        {
          supplyText = view.getDictionary("reminder") + ": " +
              view.getDictionary("MedicationSupplyTypeEnum." + supplyData.supplyTypeEnum) +
              ", " + supplyData.daysSupply + (data.getDaysSupply() == 1 ? view.getDictionary("day") : view.getDictionary("days"));

        }
        var supplyTextLabel = new tm.jquery.Label({
          cls: "TextData",
          html: supplyText,
          flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
        });

        this.supplyContainer.getContentComponent().add(supplyTextLabel);
      }
      var supplyContainerLayout = tm.jquery.HFlexboxLayout.create("flex-start", "center");
      supplyContainerLayout.setFlexFlow(tm.jquery.flexbox.FlexFlow.create("row", "wrap"));
      this.supplyContainer.getContentComponent().setLayout(supplyContainerLayout);
      this.supplyContainer.repaint();
      //this.supplyContainer.show();
    }
  },

  _removeTherapyContainer: function(therapyContainer, blockEvent)
  {
    this.getTherapiesListContainer().remove(therapyContainer);

    if (!tm.jquery.Utils.isEmpty(this.therapyRemovedCallback) && blockEvent != true)
    {
      this.therapyRemovedCallback(therapyContainer.getTherapyId());
    }
  },

  _suspendTherapy: function (therapyContainer)
  {
    therapyContainer.getData().changeType = app.views.medications.TherapyEnums.pharmacistTherapyChangeType.SUSPEND;
    therapyContainer.refresh();
  },

  _abortTherapy: function (therapyContainer)
  {
    /* extend over the original, keeping whatever is missing */
    therapyContainer.getData().changeType = app.views.medications.TherapyEnums.pharmacistTherapyChangeType.ABORT;
    therapyContainer.refresh();
  },

  _revertAbortOrSuspendTherapy: function (therapyContainer)
  {
    therapyContainer.getData().changeType = app.views.medications.TherapyEnums.pharmacistTherapyChangeType.NONE;
    therapyContainer.refresh();
  },

  _editTherapy: function (therapyContainer)
  {
    var self = this;
    var view = this.getReviewContainer().getView();
    var therapyDayDto = therapyContainer.getData();
    var therapy = therapyDayDto.therapy;

    view.showEditTherapyDialog(therapy, false, therapyDayDto.modified, function (result, editContainer)
    {
      self._therapyEditDialogResultCallback(result, editContainer, therapyContainer);
    });
  },

  _therapyEditDialogResultCallback: function (result, editContainer, therapyContainer)
  {
    this._loadTherapyDisplayData(therapyContainer.getData().therapy, result, function (displayValue)
    {
      displayValue.therapy.setCompositionUid(therapyContainer.getData().therapy.getCompositionUid());
      displayValue.therapy.setComposerName(therapyContainer.getData().therapy.getComposerName());
      displayValue.therapy.setEhrOrderName(therapyContainer.getData().therapy.getEhrOrderName());

      therapyContainer.setData(displayValue);
      therapyContainer.getData().changeType = app.views.medications.TherapyEnums.pharmacistTherapyChangeType.EDIT;
      therapyContainer.refresh();
    });
  },

  _loadTherapyDisplayData: function (originalTherapy, changedTherapy, callback)
  {
    var fillPharmacistReviewTherapyOnEdit = this.getReviewContainer().getView().getViewModuleUrl()
        + tm.views.medications.TherapyView.SERVLET_PATH_LOAD_FILL_PHARMACISTS_THERAPY_EDIT;

    var params = {
      originalTherapy: JSON.stringify(originalTherapy),
      changedTherapy: JSON.stringify(changedTherapy)
    };

    this.getReviewContainer().getView().loadPostViewData(fillPharmacistReviewTherapyOnEdit, params, null, function (therapyWithDisplayValues)
    {
      therapyWithDisplayValues.therapy.completed = originalTherapy.completed;
      therapyWithDisplayValues.therapy = app.views.medications.common.TherapyJsonConverter.convert(therapyWithDisplayValues.therapy);

      callback(therapyWithDisplayValues);
    });
  },

  _rtrim: function(value)
  {
    return value.replace(/((\s*\S+)*)\s*/, "$1");
  },

  /*
   Override, returning an edit version of the therapy container.
   */
  buildTherapyContainer: function (view, data)
  {
    var self = this;

    var therapyContainer = app.views.medications.pharmacists.TherapyContainer.forEditReviewContentCard({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      view: view,
      data: data
    });
    therapyContainer.getToolbar().setRemoveTherapyEventCallback(function (container)
    {
      self._removeTherapyContainer(container);
    });
    therapyContainer.getToolbar().setEditTherapyEventCallback(function (container)
    {
      self._editTherapy(container);
    });
    therapyContainer.getToolbar().setAbortTherapyEventCallback(function (container)
    {
      self._abortTherapy(container);
    });
    therapyContainer.getToolbar().setRevertAbortOrSuspendEventCallback(function (container)
    {
      self._revertAbortOrSuspendTherapy(container);
    });
    therapyContainer.getToolbar().setSuspendTherapyEventCallback(function (container)
    {
      self._suspendTherapy(container);
    });
    return therapyContainer;
  },

  setTherapyRemovedCallback: function (value)
  {
    this.therapyRemovedCallback = value;
  },

  getReminderComponent: function ()
  {
    return this.reminderComponent;
  },

  getReminderNoteComponent: function()
  {
    return this.reminderNoteComponent;
  },

  getDaysSupplyComponent: function()
  {
    return this.daysSupplyComponent;
  },

  addTherapy: function (therapyData)
  {
    var view = this.getReviewContainer().getView();
    var therapiesListContainer = this.getTherapiesListContainer();
    var presentTherapies = therapiesListContainer.getComponents();
    var therapyCompId = tm.jquery.Utils.isEmpty(therapyData.therapy) ? null : therapyData.therapy.compositionUid;

    /* we only allow 1 therapy to be present on a review, but it use to be multiple, so trying to
     * keep some multi-therapy support still present in case of we switch back again */
    if (presentTherapies.length >= 1)
    {
      // check if it's already in the list
      for (var idx = 0; idx < presentTherapies.length; idx++)
      {
        if (presentTherapies[idx].getTherapyId() === therapyCompId) return;
      }

      // remove one by one because of the required callbacks
      while (presentTherapies.length > 0)
      {
        this._removeTherapyContainer(presentTherapies[0])
      }
    }

    var therapyContainer = this.buildTherapyContainer(view, therapyData);
    therapyContainer.highlight();

    therapiesListContainer.add(therapyContainer);
    this._loadDataAndPopulateSupplyContainer();
    therapiesListContainer.repaint();
  },

  getEditorData: function()
  {
    var oldData = this.getReviewContainer().getReviewData();

    var editorData = new tm.views.medications.pharmacists.dto.PharmacistMedicationReview({
      compositionUid: oldData.compositionUid,
      composer: oldData.composer,
      createTimestamp: oldData.createTimestamp
    });

    if (!tm.jquery.Utils.isEmpty(this.getReferBackComponent().getActiveRadioButton()))
    {
      editorData.setReferBackToPrescriber(this.getReferBackComponent().getActiveRadioButton().value);
    }

    if (this.showSupply && this.getMedicationSupplyTypeComponent() && !tm.jquery.Utils.isEmpty(this.getMedicationSupplyTypeComponent().getActiveRadioButton()))
    {
      editorData.setMedicationSupplyType(this.getMedicationSupplyTypeComponent().getActiveRadioButton().value);

      editorData.setDaysSupply(!tm.jquery.Utils.isEmpty(this.getDaysSupplyComponent().getValue()) ?
          this.getDaysSupplyComponent().getValue() : null);
    }

    if (this.getDrugRelatedProblemDescContainer().hasValues())
    {
      editorData.setDrugRelatedProblem(new tm.views.medications.pharmacists.dto.TherapyProblemDescription());
      this.getDrugRelatedProblemDescContainer().toTherapyProblemDescription(editorData.getDrugRelatedProblem());
    }

    if (this.getPharmacokineticIissueDescContainer().hasValues())
    {
      editorData.setPharmacokineticIssue(new tm.views.medications.pharmacists.dto.TherapyProblemDescription());
      this.getPharmacokineticIissueDescContainer().toTherapyProblemDescription(editorData.getPharmacokineticIssue());
    }

    if (this.getPatientRelatedDescContainer().hasValues())
    {
      editorData.setPatientRelatedProblem(new tm.views.medications.pharmacists.dto.TherapyProblemDescription());
      this.getPatientRelatedDescContainer().toTherapyProblemDescription(editorData.getPatientRelatedProblem());
    }

    if (!tm.jquery.Utils.isEmpty(this.getOverallRecommendationComponent().getValue()))
    {
      editorData.setOverallRecommendation(this._rtrim(this.getOverallRecommendationComponent().getValue()));
    }

    editorData.setNoProblem(false);
    editorData.setReminderDate(this.getReminderComponent().getDate());
    editorData.setReminderNote(this.getReminderNoteComponent().getValue());

    var therapiesListContainer = this.getTherapiesListContainer();
    therapiesListContainer.getComponents().forEach(function (container)
    {
      editorData.getRelatedTherapies().push(container.getData());
    });

    return editorData;
  },

  refresh: function()
  {
    // we need to make sure the problem description coded values are loaded, otherwise abort going into edit
    if (this.isRendered())
    {
      var view = this.getReviewContainer().getView();
      var appFactory = view.getAppFactory();
      var self = this;

      view.showLoaderMask();

      appFactory.createConditionTask(function()
          {
            view.hideLoaderMask();
            self.removeAll();
            self.medicationSupplyTypeComponent = null;
            self.daysSupplyComponent = null;
            self._buildGUI();
            self._populateReviewDataContainers();
            self.repaint();
          }, function()
          {
            return !tm.jquery.Utils.isEmpty(view.getProblemDescriptionNamedIdentitiesMap());
          },
          function()
          {
            self.getReviewContainer().switchMode(false);
            view.hideLoaderMask();
          }, 50, 100);
    }
  },

  /* Override */
  _populateReviewDataContainers: function()
  {
    this._populateTherapiesListContainer();
    this._loadDataAndPopulateSupplyContainer();
  }
});