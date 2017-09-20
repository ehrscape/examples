/*
 * Copyright (c) 2010-2014 Marand d.o.o. (www.marand.com)
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

Class.define('app.views.medications.ordering.ComplexTherapyEditContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "complex-therapy-edit-container",
  scrollable: 'vertical',

  /** configs */
  view: null,
  medicationData: null, /** @param {Array<app.views.medications.common.dto.MedicationData>} medicationData */
  therapy: null,
  copyTherapy: false,
  isPastMode: false,
  therapyAlreadyStarted: false,
  therapyModifiedInThePast: false,
  saveTherapyFunction: null, //optional
  /** privates */
  resultCallback: null,
  editingStartTimestamp: null,
  /** privates: components */
  complexTherapyContainer: null,
  performerContainer: null,
  saveDateTimePane: null,

  _renderConditionTask: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    var self = this;
    var appFactory = this.view.getAppFactory();

    this._buildComponents();
    this._buildGui();

    this.complexTherapyContainer.setMedicationDataByTherapy(this.getTherapy(), this.getMedicationData());
    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      self._abortRenderConditionTask();
      self._renderConditionTask = appFactory.createConditionTask(
          function()
          {
            self._presentValue();
            self._renderConditionTask = null;
          },
          function()
          {
            return self.isRendered(true);
          },
          20, 1000
      );
    });
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    this.editingStartTimestamp = CurrentTime.get();
    this.editingStartTimestamp.setSeconds(0);
    this.editingStartTimestamp.setMilliseconds(0);

    this.complexTherapyContainer = new app.views.medications.ordering.ComplexTherapyContainer({
      view: this.view,
      startProcessOnEnter: true,
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      editMode: true,
      changeReasonRequired: this.getTherapy().isLinkedToAdmission(),
      copyMode: this.copyTherapy,
      isPastMode: this.isPastMode,
      therapyAlreadyStarted: this.therapyAlreadyStarted,
      additionalMedicationSearchFilter: app.views.medications.TherapyEnums.medicationFinderFilterEnum.INPATIENT_PRESCRIPTION,
      getTherapyStartNotBeforeDateFunction: function()
      {
        return self.isPastMode == true ? null : self._getEditTimestamp();
      },
      confirmTherapyEvent: function(result, changeReason)
      {
        if (result == "VALIDATION_FAILED")
        {
          self.resultCallback(new app.views.common.AppResultData({success: false}));
        }
        else
        {
          var performer = self._performerContainer != null ?
              self._performerContainer.getPerformer() : self.view.getCurrentUserAsCareProfessional();
          
          if (self.saveTherapyFunction)
          {
            self.saveTherapyFunction(result, performer);
            self.resultCallback(new app.views.common.AppResultData({success: true}));
          }
          else
          {
            self._saveTherapy(result, changeReason, performer,
                self.saveDateTimePane.isHidden() ? null : self.saveDateTimePane.getSaveDateTime());
          }
        }
      },
      saveDateTimePaneEvent: function()
      {
        self.saveDateTimePane.setHeight(34);
        self.saveDateTimePane.setPadding('4 0 0 0');
        self.saveDateTimePane.show();
        self.saveDateTimePane.repaint();
      }
    });

    if (this.isPastMode === true)
    {
      var careProfessionals = this.view.getCareProfessionals();
      var currentUserAsCareProfessionalName = this.view.getCurrentUserAsCareProfessional() ? this.view.getCurrentUserAsCareProfessional().name : null;
      this._performerContainer =
          tm.views.medications.MedicationUtils.createPerformerContainer(this.view, careProfessionals, currentUserAsCareProfessionalName);
    }

    this.saveDateTimePane = new app.views.medications.ordering.TherapySaveDatePane();
    this.saveDateTimePane.hide();
  },

  _buildGui: function()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create('flex-start', "stretch", 0));

    this.add(this.complexTherapyContainer);
    if (this._performerContainer != null)
    {
      this.add(this._performerContainer);
    }
    this.add(this.saveDateTimePane);
  },

  _getEditTimestamp: function()
  {
    return this.saveDateTimePane.isHidden() ? this.editingStartTimestamp : this.saveDateTimePane.getSaveDateTime();
  },

  _presentValue: function()
  {
    var therapyHasAlreadyStarted = new Date(this.getTherapy().getStart()) < this._getEditTimestamp();
    if (therapyHasAlreadyStarted &&
        this.getTherapy().isContinuousInfusion() &&
        this.getTherapy().isVariable &&
        !this.getTherapy().isRecurringContinuousInfusion())
    {
      this.getTherapy().setVariable(false);
      if (tm.jquery.Utils.isArray(this.getTherapy().getTimedDoseElements()) &&
          this.getTherapy().getTimedDoseElements().length > 0)
      {
        this.getTherapy().setDoseElement(
            this.getTherapy().getTimedDoseElements()[this.getTherapy().getTimedDoseElements().length - 1].doseElement);
      }
      this.getTherapy().setTimedDoseElements([]);
    }

    var setTherapyStart = !therapyHasAlreadyStarted || this.isPastMode == true;

    this.complexTherapyContainer.setComplexTherapy(
        this.getTherapy(),
        setTherapyStart,
        this.therapyModifiedInThePast);
  },

  _saveTherapy: function(newTherapy, changeReason, prescriber, saveDateTime)
  {
    var self = this;

    if (this.copyTherapy)
    {
      var medicationOrder = [
        new app.views.medications.common.dto.SaveMedicationOrder({
          therapy: newTherapy,
          actionEnum: app.views.medications.TherapyEnums.medicationOrderActionEnum.PRESCRIBE
        })
      ];

      this.view.getRestApi().saveMedicationsOrder(medicationOrder, prescriber, saveDateTime, null, true)
          .then(function onSuccess()
              {
                self.resultCallback(new app.views.common.AppResultData({success: true}));
              },
              function onFailure()
              {
                self.resultCallback(new app.views.common.AppResultData({success: false}));
              });
    }
    else
    {
      newTherapy.setCompositionUid(this.getTherapy().getCompositionUid());
      newTherapy.setEhrOrderName(this.getTherapy().getEhrOrderName());

      this.view.getRestApi().modifyTherapy(
          newTherapy, 
          changeReason, 
          prescriber, 
          this.therapyAlreadyStarted, 
          saveDateTime, 
          true)
          .then(function onSuccess()
              {
                self.resultCallback(new app.views.common.AppResultData({success: true}));
              },
              function onFailure()
              {
                self.resultCallback(new app.views.common.AppResultData({success: false}));
              });
    }
  },

  _abortRenderConditionTask: function()
  {
    if (!tm.jquery.Utils.isEmpty(this._renderConditionTask))
    {
      this._renderConditionTask.abort();
      this._renderConditionTask = null;
    }
  },
  
  /**
   * @returns {Array<app.views.medications.common.dto.MedicationData>}
   */
  getMedicationData: function()
  {
    return this.medicationData;
  },

  /**
   * @returns {app.views.medications.common.dto.Therapy}
   */
  getTherapy: function()
  {
    return this.therapy;
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    this.resultCallback = resultDataCallback;
    this.complexTherapyContainer.validateAndConfirmOrder();
  },
  /**
   * @Override
   */
  destroy: function()
  {
    this.callSuper();
    this._abortRenderConditionTask();
  }
});

