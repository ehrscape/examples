/*
 * Copyright (c) 2010-2017 Marand d.o.o. (www.marand.com)
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
Class.define('app.views.medications.ordering.OxygenTherapyContainer', 'tm.jquery.Container', {
  statics: {
    EVENT_TYPE_CANCEL_EDIT: new tm.jquery.event.EventType({
      name: 'oxygenTherapyContainerCancelEdit', delegateName: null
    }),
    EVENT_TYPE_ENABLE_SAVE_TIMESTAMP_CHANGE: new tm.jquery.event.EventType({
      name: 'oxygenTherapyContainerEnableSaveTimestampChange', delegateName: null
    }) 
  },
  scrollable: 'vertical',
  cls: 'oxygen-therapy-container',

  view: null,
  medicationData: null,
  orderMode: true,
  withStartEndTime: true,
  withSaturation: true,
  withSupply: false, /* denotes withStartEndTime */
  pastMode: false,
  changeReasonRequired: false,
  showSource: false,

  confirmTherapyCallback: null,
  saveToTemplateCallback: null,

  _medicationField: null,
  _medicationInfoContainer: null,
  _therapyIntervalPane: null,
  _saturationInputContainer: null,
  _routeRowContainer: null,
  _startingFlowRateField: null,
  _supplyContainer: null,
  _highFlowCheckBox: null,
  _humidificationCheckBox: null,
  _commentIndicationPane: null,
  _validationForm: null,
  _addToBasketButton: null,
  _administrationPreviewTimeline: null,
  _therapyNextAdministrationLabel: null,
  _previewRefreshTimer: null,
  _changeReasonPane: null,
  _medicationSourceSelectBox: null,
  _editStartTimestamp: null,
  _flowRateValidator: null,

  _linkedToAdmission: false,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this.registerEventTypes('app.views.medications.ordering.OxygenTherapyContainer', [
      {eventType: app.views.medications.ordering.OxygenTherapyContainer.EVENT_TYPE_CANCEL_EDIT},
      {eventType: app.views.medications.ordering.OxygenTherapyContainer.EVENT_TYPE_ENABLE_SAVE_TIMESTAMP_CHANGE}
    ]);

    if (!this.isOrderMode())
    {
      this._editStartTimestamp = CurrentTime.get();
      this._editStartTimestamp.setSeconds(0);
      this._editStartTimestamp.setMilliseconds(0);
    }

    if (this.isWithSupply())
    {
      this.withStartEndTime = false;
    }
    
    this._flowRateValidator = new app.views.medications.ordering.oxygen.OxygenFlowRateValidator({
      view: this.getView()
    });
    
    this._buildGui();
    this._buildValidationForm();
  },

  _buildGui: function()
  {
    var view = this.getView();
    var self = this;
    /* optional components */
    var saturationRowComponent = null;
    var administrationPreviewTimeline = null;
    var medicationSourceRowComponent = null;
    var supplyRowContainer = null;
    
    this.setLayout(tm.jquery.VFlexboxLayout.create('flex-start', 'stretch'));

    this.add(this._buildMedicationRowComponent());

    var doseRowContainer = new tm.jquery.Container({
      cls: 'dose-row-container',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      layout: new tm.jquery.HFlexboxLayout({align: 'right'}),
      scrollable: 'visible'
    });
    this.add(doseRowContainer);

    if (this.isOrderMode())
    {
      var cancelEditButton = new tm.jquery.Container({
        cls: 'remove-icon clear-button',
        width: 30,
        height: 30
      });
      cancelEditButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
      {
        self._fireCancelEditEvent();
      });
      doseRowContainer.add(cancelEditButton);
    }

    if (this.isWithSaturation())
    {
      saturationRowComponent = this._buildSaturationRowComponent();
    }
    
    var therapyIntervalRowComponent = this._buildTherapyIntervalRowComponent();
    var routeRowComponent = this._buildOxygenRouteRowComponent();

    var startingFlowRateRowComponent = new tm.views.medications.common.VerticallyTitledComponent({
      scrollable: 'visible',
      titleText: view.getDictionary('rate'),
      contentComponent: new tm.jquery.Container({
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
        layout: tm.jquery.HFlexboxLayout.create('flex-start', 'center')
      }),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    var startingFlowRate = new tm.jquery.NumberField({
      width: 70,
      cls: 'start-flow-rate-input',
      formatting: {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: 2}
    });
    startingFlowRateRowComponent.getContentComponent().add(startingFlowRate);
    startingFlowRateRowComponent.getContentComponent().add(new tm.jquery.Component({
      cls: 'start-flow-rate-label',
      html: 'L/min'
    }));

    if (this.isWithSupply())
    {
      supplyRowContainer = new app.views.medications.ordering.TherapySupplyContainer({
        view: view
      });
    }

    var additionalInformationRow = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary('additional.information'),
      contentComponent: new tm.jquery.Container({
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
        layout: tm.jquery.VFlexboxLayout.create('flex-start', 'stretch')
      }),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    var highFlowCheckBox = new tm.jquery.CheckBox({
      cls: 'high-flow-checkbox',
      labelText: view.getDictionary('high.flow.oxygen.therapy'),
      labelCls: 'TextData',
      checked: false,
      labelAlign: 'right',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      nowrap: true
    });

    var humidificationCheckBox = new tm.jquery.CheckBox({
      cls: 'humidification-checkBox',
      labelText: view.getDictionary('humidification'),
      labelCls: 'TextData',
      checked: false,
      labelAlign: 'right',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      nowrap: true
    });

    additionalInformationRow.getContentComponent().add(highFlowCheckBox);
    additionalInformationRow.getContentComponent().add(humidificationCheckBox);

    if (this.isWithStartEndTime())
    {
      administrationPreviewTimeline = new app.views.medications.ordering.AdministrationPreviewTimeline({
        view: view
      });
    }

    var commentIndicationPane = new app.views.medications.ordering.CommentIndicationPane({
      view: view,
      saveDateTimePaneEvent: function()
      {
        self._fireEnableSaveTimestampChange();
      }
    });

    var therapyNextAdministrationLabel = new app.views.medications.ordering.TherapyNextAdministrationLabelPane({
      hidden: !this.isWithStartEndTime(),
      view: view
    });

    var changeReasonPane = new app.views.medications.ordering.ChangeReasonPane({
      view: view,
      hidden: !this.isChangeReasonRequired()
    });

    if (this.isShowSource())
    {
      medicationSourceRowComponent = this._buildMedicationSourceRowComponent();
    }

    if (this.isOrderMode())
    {
      var navigationContainer = this._buildNavigationContainer();
    }

    if (saturationRowComponent)
    {
      this.add(saturationRowComponent);
    }
    this.add(startingFlowRateRowComponent);
    this.add(routeRowComponent);
    this.add(additionalInformationRow);
    this.add(therapyIntervalRowComponent);
    if (supplyRowContainer)
    {
      this.add(supplyRowContainer);
    }
    this.add(therapyNextAdministrationLabel);
    if (administrationPreviewTimeline)
    {
      this.add(administrationPreviewTimeline);
    }
    this.add(commentIndicationPane);
    this.add(changeReasonPane);
    if (medicationSourceRowComponent)
    {
      this.add(medicationSourceRowComponent);
    }
    if (navigationContainer)
    {
      this.add(navigationContainer);
    }

    this._therapyIntervalPane = therapyIntervalRowComponent;
    this._saturationInputContainer = saturationRowComponent ? saturationRowComponent.getContentComponent() : null;
    this._routeRowContainer = routeRowComponent.getContentComponent();
    this._startingFlowRateField = startingFlowRate;
    this._supplyContainer = supplyRowContainer;
    this._highFlowCheckBox = highFlowCheckBox;
    this._humidificationCheckBox = humidificationCheckBox;
    this._administrationPreviewTimeline = administrationPreviewTimeline;
    this._commentIndicationPane = commentIndicationPane;
    this._therapyNextAdministrationLabel = therapyNextAdministrationLabel;
    this._changeReasonPane = changeReasonPane;
    this._medicationSourceSelectBox = medicationSourceRowComponent ?
        medicationSourceRowComponent.getContentComponent() :
        null;
  },

  _buildValidationForm: function()
  {
    var self = this;

    this._validationForm = new tm.jquery.Form({
      onValidationSuccess: function()
      {
        self._confirmOrder();
      },
      onValidationError: function()
      {
        if (self.confirmTherapyCallback)
        {
          self.confirmTherapyCallback('VALIDATION_FAILED');
        }
        if (self._addToBasketButton)
        {
          self._addToBasketButton.setEnabled(true);
        }
      },
      requiredFieldValidatorErrorMessage: this.getView().getDictionary('field.value.is.required')
    });
  },

  /**
   * @returns {tm.jquery.Container}
   * @private
   */
  _buildMedicationRowComponent: function()
  {
    var view = this.getView();
    var self = this;
    var appFactory = view.getAppFactory();

    var medicationRowContainer = new tm.jquery.Container({
      scrollable: 'visible',
      cls: 'medication-container',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      layout: tm.jquery.HFlexboxLayout.create('flex-start', 'flex-start')
    });

    var medicationDetailsPane = new app.views.medications.therapy.MedicationDetailsCardPane({view: view});
    var detailsCardTooltip = appFactory.createDefaultPopoverTooltip(
        view.getDictionary('medication'),
        null,
        medicationDetailsPane
    );
    detailsCardTooltip.onShow = function()
    {
      medicationDetailsPane.setMedicationData(self.getMedicationData());
    };

    this._medicationField = new app.views.medications.common.MedicationSearchField({
      view: view,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
      enabled: false,
      dropdownWidth: "stretch",
      dropdownAppendTo: view.getAppFactory().getDefaultRenderToElement() /* due to the dialog use */
    });

    this._medicationField.on(tm.jquery.ComponentEvent.EVENT_TYPE_SELECT, function(component)
    {
      self._onMedicationFieldSelection(component.getSelectionMedication());
    });

    medicationRowContainer.add(this._medicationField);

    this._medicationInfoContainer = new tm.jquery.Container({
      cls: 'info-icon pointer-cursor medication-info',
      width: 25,
      height: 30,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      tooltip: detailsCardTooltip
    });
    medicationRowContainer.add(this._medicationInfoContainer);

    return medicationRowContainer;
  },

  /**
   * @returns {tm.views.medications.common.VerticallyTitledComponent}
   * @private
   */
  _buildSaturationRowComponent: function()
  {
    var view = this.getView();

    var saturationInputContainer = new app.views.medications.ordering.oxygen.OxygenSaturationInputContainer({
      view: view
    });

    return new tm.views.medications.common.VerticallyTitledComponent({
      scrollable: 'visible',
      titleText: view.getDictionary('target.saturation'),
      contentComponent: saturationInputContainer,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });
  },

  /**
   * @returns {tm.views.medications.common.VerticallyTitledComponent}
   * @private
   */
  _buildOxygenRouteRowComponent: function()
  {
    var view = this.getView();

    var oxygenRouteContainer = new app.views.medications.ordering.oxygen.OxygenRouteContainer({
      view: view
    });

    return new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary('device'),
      scrollable: 'visible',
      contentComponent: oxygenRouteContainer,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });
  },

  /**
   * @returns {app.views.medications.ordering.TherapyIntervalPane}
   * @private
   */
  _buildTherapyIntervalRowComponent: function()
  {
    var self = this;
    var intervalPane = new app.views.medications.ordering.TherapyIntervalPane({
      view: this.getView(),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      restrictedStartHourSelection: false,
      presetCurrentTime: true,
      byDoctorsOrderAvailable: false,
      maxDailyFrequencyAvailable: false,
      withStartEndTime: this.isWithStartEndTime(),
      isPastMode: this.isPastMode(),
      hidden: this.isWithSupply(),
      getDosingPatternFunction: function()
      {
        return [];
      }
    });
    intervalPane.on(app.views.medications.ordering.TherapyIntervalPane.EVENT_TYPE_INTERVAL_CHANGE,
        function(component, componentEvent)
        {
          var eventData = componentEvent.eventData;
          self._onTherapyIntervalChange(eventData.start);
        });
    return intervalPane;
  },

  /**
   * @returns {tm.views.medications.common.VerticallyTitledComponent}
   * @private
   */
  _buildMedicationSourceRowComponent: function()
  {
    var view = this.getView();

    return new tm.views.medications.common.VerticallyTitledComponent({
      scrollable: 'visible',
      titleText: view.getDictionary('source'),
      contentComponent: new tm.jquery.SelectBox({
        dropdownHeight: 5,
        dropdownWidth: "stretch",
        options: tm.views.medications.MedicationUtils.createTherapySourceSelectBoxOptions(view),
        selections: [],
        multiple: false,
        allowSingleDeselect: true,
        placeholder: '...',
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "100%"),
        appendTo: function()
        {
          return view.getAppFactory().getDefaultRenderToElement();
        }
      }),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });
  },

  /**
   * @returns {tm.jquery.Container}
   * @private
   */
  _buildNavigationContainer: function()
  {
    var self = this;
    var view = this.getView();

    var navigationContainer = new tm.jquery.Container({
      cls: 'navigation-container',
      layout: tm.jquery.HFlexboxLayout.create('flex-start', 'flex-start', 20),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    var cancelEditButton = new tm.jquery.Button({
      cls: 'cancel-edit-button',
      text: view.getDictionary('empty.form'),
      type: 'link',
      handler: function()
      {
        self._fireCancelEditEvent();
      }
    });

    var saveToTemplateButton = new tm.jquery.Button({
      cls: "save-to-template-button",
      text: view.getDictionary('add.to.template'),
      type: "link",
      handler: function()
      {
        self._onAddTherapyToTemplate();
      }
    });

    var addToBasketButton = new tm.jquery.Button({
      cls: 'add-to-basket-button',
      text: view.getDictionary('add'),
      handler: function(component)
      {
        component.setEnabled(false);
        self.validateAndConfirmOrder();
      }
    });

    navigationContainer.add(cancelEditButton);
    navigationContainer.add(saveToTemplateButton);
    navigationContainer.add(addToBasketButton);

    this._addToBasketButton = addToBasketButton;

    return navigationContainer;
  },

  _fireCancelEditEvent: function()
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.ordering.OxygenTherapyContainer.EVENT_TYPE_CANCEL_EDIT
    }), null);
  },

  _fireEnableSaveTimestampChange: function()
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.ordering.OxygenTherapyContainer.EVENT_TYPE_ENABLE_SAVE_TIMESTAMP_CHANGE
    }), null);
  },

  _setupValidation: function()
  {
    this._validationForm.reset();

    if (this.isWithSaturation())
    {
      this._addValidationFormFields(this._saturationInputContainer.getFormFieldValidators());
    }
    if (this.isWithStartEndTime())
    {
      var therapyStartNotBefore = !this.isOrderMode() && !this.isPastMode() ? this._editStartTimestamp : null;
      this._addValidationFormFields(this._therapyIntervalPane.getTherapyIntervalPaneValidations(therapyStartNotBefore));
    }
    if (this.isWithSupply())
    {
      this._addValidationFormFields(this._supplyContainer.getFormValidations());
    }
    this._addValidationFormFields(this._commentIndicationPane.getIndicationValidations());
    if (this.isChangeReasonRequired())
    {
      this._addValidationFormFields(this._changeReasonPane.getChangeReasonValidations());
    }
    if (!this.isWithSupply() && this._therapyIntervalPane.getWhenNeeded())
    {
      this._addValidationFormFields(this._commentIndicationPane.getIndicationFieldValidation());
    }

    this._addValidationFormFields(this._flowRateValidator.getAsFormFieldValidators(this._startingFlowRateField));
  },

  /**
   * @param {Array<tm.jquery.FormField>} formFields
   * @private
   */
  _addValidationFormFields: function(formFields)
  {
    for (var i = 0; i < formFields.length; i++)
    {
      this._validationForm.addFormField(formFields[i]);
    }
  },

  /**
   * @returns {app.views.medications.common.dto.OxygenTherapy}
   * @private
   */
  _buildTherapy: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var saturation = this.isWithSaturation() ? this._saturationInputContainer.getSaturation() : null;
    var medicationData = this.getMedicationData();

    return new app.views.medications.common.dto.OxygenTherapy({
      medication: medicationData ? medicationData.getMedication() : null,
      variable: false,
      start: this._therapyIntervalPane.getStart(),
      end: this._therapyIntervalPane.getEnd(),
      whenNeeded: this.isWithSupply() ? this._supplyContainer.getWhenNeeded() : this._therapyIntervalPane.getWhenNeeded(),
      reviewReminderDays: this._therapyIntervalPane.getReviewReminderDays(),
      flowRate: this._startingFlowRateField.getValue(),
      flowRateMode: this._highFlowCheckBox.isChecked() ? enums.flowRateMode.HIGH_FLOW : enums.flowRateMode.LOW_FLOW,
      startingDevice: this._routeRowContainer.getStartingDevice(),
      minTargetSaturation: saturation ? saturation.getMin() : null,
      maxTargetSaturation: saturation ? saturation.getMax() : null,
      humidification: this._humidificationCheckBox.isChecked(),
      comment: this._commentIndicationPane.getComment(),
      clinicalIndication: this._commentIndicationPane.getIndication(),
      prescriptionSupply: this.isWithSupply() ? this._supplyContainer.getSupply() : null,
      linkedToAdmission: this._linkedToAdmission
    });
  },

  _confirmOrder: function()
  {
    var view = this.getView();
    var oxygenTherapy = this._buildTherapy();
    var changeReason = this.isChangeReasonRequired() ?
        new app.views.medications.common.dto.TherapyChangeReason({
          changeReason: this._changeReasonPane.getReasonValue(),
          comment: this._changeReasonPane.getComment()
        })
        : null;

    var confirmSuccess = this.confirmTherapyCallback(oxygenTherapy, changeReason);
    if (!confirmSuccess)
    {
      if (this._addToBasketButton)
      {
        this._addToBasketButton.setEnabled(true);
      }
    }
    else if (oxygenTherapy.getLinkName())
    {
      view.setPatientLastLinkNamePrefix(oxygenTherapy.getLinkName().substring(0, 1));
    }

    if (view.getViewMode() == 'ORDERING_PAST')
    {
      view.setPresetDate(oxygenTherapy.getStart());
    }
  },

  _onAddTherapyToTemplate: function()
  {
    if (this.saveToTemplateCallback)
    {
      var therapy = this._buildTherapy();
      this._setupValidation();
      var invalidTherapy = this._validationForm.hasFormErrors();
      this.saveToTemplateCallback(therapy, invalidTherapy);
    }
  },

  /**
   * @param {Date|null} startTime
   * @private
   */
  _onTherapyIntervalChange: function(startTime)
  {
    this._therapyNextAdministrationLabel.setNextAdministration(startTime);
    if (this.isWithStartEndTime())
    {
      this.refreshAdministrationPreview();
    }
  },

  /**
   * @param {app.views.medications.common.dto.Medication|null} selection
   * @private
   */
  _onMedicationFieldSelection: function(selection)
  {
    var self = this;

    if (selection && (!this.getMedicationData() || this.getMedicationData().getMedication().getId() !== selection.getId()))
    {
      this.getView().getRestApi().loadMedicationData(selection.getId()).then(function setData(medicationData)
      {
          self.setMedicationData(medicationData);
      });
    }
  },

  _configureChangeReasonEditPane: function()
  {
    var pane = this._changeReasonPane;
    var view = this.view;
    var editEnum = app.views.medications.TherapyEnums.pharmacistTherapyChangeType.EDIT;

    if (pane && this.isChangeReasonRequired())
    {
      if (tm.jquery.Utils.isEmpty(view.getTherapyChangeReasonTypeMap()))
      {
        view.loadTherapyChangeReasonTypeMap(function(data)
        {
          pane.setReasonOptions(data[editEnum]);
        });
      }
      else
      {
        pane.setReasonOptions(view.getTherapyChangeReasonTypeMap()[editEnum]);
      }
      pane.isRendered() ? pane.show() : pane.setHidden(false);
    }
    else
    {
      pane.isRendered() ?  pane.hide() : pane.setHidden(true);
    }
  },

  clear: function()
  {
    this.medicationData = null;
    this._medicationField.setSelection(null, true);
    if (this.isWithSaturation())
    {
      this._saturationInputContainer.clear();
    }
    this._therapyIntervalPane.clear(true);
    this._routeRowContainer.clear();
    this._humidificationCheckBox.setChecked(false, true);
    this._highFlowCheckBox.setChecked(false, true);
    this._startingFlowRateField.setValue(null, true);
    this._commentIndicationPane.clear();
    this._changeReasonPane.clear();
    if (this._medicationSourceSelectBox)
    {
      this._medicationSourceSelectBox.setSelections([]);
    }
    this._therapyNextAdministrationLabel.clear();
    this._validationForm.reset();
    if (this._addToBasketButton)
    {
      this._addToBasketButton.setEnabled(true);
    }
    if (this.isWithStartEndTime())
    {
      this._administrationPreviewTimeline.clear();
    }
    if (this.isWithSupply())
    {
      this._supplyContainer.clear();
    }
    this._linkedToAdmission = false;
  },

  /**
   * @param {Date} startDate
   */
  setTherapyStart: function(startDate)
  {
    this._therapyIntervalPane.setStart(startDate, true);
    this._therapyNextAdministrationLabel.setNextAdministration(startDate);
    if (this.isWithStartEndTime())
    {
      this.refreshAdministrationPreview();
    }
  },

  /**
   * @param {Function} callback
   */
  setConfirmTherapyCallback: function(callback)
  {
    this.confirmTherapyCallback = callback;  
  },

  validateAndConfirmOrder: function()
  {
    this._setupValidation();
    this._validationForm.submit();
  },

  refreshAdministrationPreview: function()
  {
    var self = this;
    /* add a small delay so we don't call it too often*/
    clearTimeout(this._previewRefreshTimer);

    this._previewRefreshTimer = setTimeout(function()
    {
      if (self.isRendered())
      {
        self._administrationPreviewTimeline._refreshAdministrationPreviewImpl(
            self._therapyIntervalPane.getStart(),
            self._buildTherapy());
      }
    }, 150);
  },

  /**
   * @returns {boolean}
   */
  isChangeReasonRequired: function()
  {
    return this.changeReasonRequired === true;
  },

  /**
   * Override.
   */
  destroy: function()
  {
    clearTimeout(this._previewRefreshTimer);
    this.callSuper();
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   */
  setMedicationData: function(medicationData)
  {
    this.medicationData = medicationData;
    this._medicationField.setSelection(medicationData.getMedication());
  },

  /**
   * @param {app.views.medications.common.dto.OxygenTherapy|app.views.medications.common.dto.Therapy} oxygenTherapy
   * @param {Boolean} showPreviousAdministrations
   * @param {Boolean} skipEndTime
   */
  setValues: function(oxygenTherapy, showPreviousAdministrations, skipEndTime)
  {
    var therapyHasAlreadyStarted = this._editStartTimestamp && (oxygenTherapy.getStart() < this._editStartTimestamp);

    var startTime = !therapyHasAlreadyStarted || this.isPastMode() ?
        oxygenTherapy.getStart() :
        tm.views.medications.MedicationTimingUtils.getTimestampRoundedUp(CurrentTime.get(), 5);

    this._therapyIntervalPane.setStart(startTime, true);
    if (!skipEndTime)
    {
      this._therapyIntervalPane.setEnd(oxygenTherapy.getEnd());
    }
    this._therapyIntervalPane.setReviewReminderDays(oxygenTherapy.getReviewReminderDays());
    this._therapyIntervalPane.setWhenNeeded(oxygenTherapy.getWhenNeeded());
    this._startingFlowRateField.setValue(oxygenTherapy.getFlowRate(), true);
    this._highFlowCheckBox.setChecked(oxygenTherapy.isHighFlowOxygen());
    this._humidificationCheckBox.setChecked(oxygenTherapy.isHumidification());
    if (this.isWithSaturation())
    {
      this._saturationInputContainer.setSaturation(
              app.views.medications.common.dto.Range.createStrict(
                  oxygenTherapy.getMinTargetSaturation(),
                  oxygenTherapy.getMaxTargetSaturation()
              )
      );
    }
    this._routeRowContainer.setStartingDevice(oxygenTherapy.getStartingDevice());
    this._commentIndicationPane.setComment(oxygenTherapy.getComment());
    this._commentIndicationPane.setIndication(oxygenTherapy.getClinicalIndication());
    if (this.isWithStartEndTime())
    {
      this.refreshAdministrationPreview();
    }
    if (this.isWithSupply())
    {
      this._supplyContainer.setWhenNeeded(oxygenTherapy.getWhenNeeded());
      this._supplyContainer.setSupply(oxygenTherapy.getPrescriptionSupply());
    }
    if (!this.isOrderMode())
    {
      this._medicationField.setLimitBySimilar(oxygenTherapy.getMedication(), oxygenTherapy.getRoutes());
      if (showPreviousAdministrations)
      {
        this._therapyNextAdministrationLabel.setOldTherapyId(
            oxygenTherapy.getCompositionUid(),
            oxygenTherapy.getEhrOrderName(),
            true);
      }
    }
    this._linkedToAdmission = oxygenTherapy.isLinkedToAdmission();
  },

  /**
   * @param {Boolean} value
   */
  setChangeReasonRequired: function(value)
  {
    this.changeReasonRequired = value;
    this._configureChangeReasonEditPane();
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {boolean}
   */
  isOrderMode: function()
  {
    return this.orderMode === true;
  },

  /**
   * @returns {boolean}
   */
  isWithStartEndTime: function()
  {
    return this.withStartEndTime === true;
  },

  /**
   * @returns {boolean}
   */
  isWithSaturation: function()
  {
    return this.withSaturation === true;
  },

  /**
   * @returns {app.views.medications.common.dto.MedicationData|null}
   */
  getMedicationData: function()
  {
    return this.medicationData;
  },

  /**
   * @returns {boolean}
   */
  isPastMode: function()
  {
    return this.pastMode === true;
  },

  /**
   * @returns {boolean}
   */
  isShowSource: function()
  {
    return this.showSource === true;
  },

  /**
   * @returns {boolean}
   */
  isWithSupply: function()
  {
    return this.withSupply === true;
  },

  /**
   * @param {Date} value
   */
  setEditStartTimestamp: function(value)
  {
    this._editStartTimestamp = value;
  }
});