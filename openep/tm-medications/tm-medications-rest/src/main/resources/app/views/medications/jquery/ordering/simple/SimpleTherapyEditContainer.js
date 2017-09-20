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

Class.define('app.views.medications.ordering.SimpleTherapyEditContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "simple-therapy-edit-container",
  scrollable: 'vertical',

  /** configs */
  view: null,
  therapy: null,
  copyTherapy: false,
  isPastMode: false,
  therapyAlreadyStarted: false,
  therapyModifiedInThePast: false,
  saveTherapyFunction: null, //optional
  medicationData: null, /** @param {Array<app.views.medications.common.dto.MedicationData>} medicationData */
  /** privates */
  resultCallback: null,
  medications: null,
  editingStartTimestamp: null,
  /** privates: components */
  simpleTherapyContainer: null,
  performerContainer: null,
  saveDateTimePane: null,

  _renderConditionTask: null,
  _testRenderCoordinator: null,

  /** constructor */
  Constructor: function(config)
  {
    var self = this;
    this.callSuper(config);
    var appFactory = this.view.getAppFactory();
    this.setLayout(tm.jquery.VFlexboxLayout.create('flex-start', "stretch", 0));

    this._buildComponents();
    this._buildGui();

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      self._abortRenderConditionTask();
      self._renderConditionTask = appFactory.createConditionTask(
          function()
          {
            self._presentValue();
            self._renderConditionTask = null;
            self._testRenderCoordinator.insertCoordinator();
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

    this.editingStartTimestamp = CurrentTime.get();
    this.editingStartTimestamp.setSeconds(0);
    this.editingStartTimestamp.setMilliseconds(0);

    this._testRenderCoordinator = new app.views.medications.common.testing.RenderCoordinator({
      attributeName: 'simple-therapy-edit-container-coordinator',
      view: this.getView(),
      component: this,
      manualMode: true
    });

    this.simpleTherapyContainer = new app.views.medications.ordering.SimpleTherapyContainer({
      view: this.view,
      editMode: true,
      copyMode: this.copyTherapy,
      isPastMode: this.isPastMode,
      therapyAlreadyStarted: this.therapyAlreadyStarted,
      changeReasonRequired: this.therapy.linkedToAdmission,
      bnfMaximumPercentage: tm.jquery.Utils.isEmpty(this.therapy.bnfMaximumPercentage) ? null :
          this.therapy.bnfMaximumPercentage,
      showBnf: !tm.jquery.Utils.isEmpty(this.therapy.bnfMaximumPercentage),
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

    this.saveDateTimePane = new app.views.medications.ordering.TherapySaveDatePane({
      hidden: true
    });
  },

  _buildGui: function()
  {
    this.add(this.simpleTherapyContainer);
    this.add(new tm.jquery.Container({flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto")}));
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
    var therapy = this.getTherapy();

    var therapyHasAlreadyStarted = therapy.getStart() < this._getEditTimestamp();
    var setTherapyStart = !therapyHasAlreadyStarted || this.isPastMode == true;
    this.simpleTherapyContainer.setSimpleTherapyData(therapy,
        this.getMedicationData(),
        setTherapyStart,
        this.therapyModifiedInThePast);
  },

  _saveTherapy: function(therapy, changeReason, prescriber, saveDateTime)
  {
    var self = this;

    if (this.copyTherapy)
    {
      var medicationOrder = [
        new app.views.medications.common.dto.SaveMedicationOrder({
          therapy: therapy,
          actionEnum: app.views.medications.TherapyEnums.medicationOrderActionEnum.PRESCRIBE
        })
      ];

      this.getView().getRestApi().saveMedicationsOrder(medicationOrder, prescriber, saveDateTime, null, true)
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
      therapy.setCompositionUid(this.getTherapy().getCompositionUid());
      therapy.setEhrOrderName(this.getTherapy().getEhrOrderName());

      this.getView().getRestApi().modifyTherapy(
          therapy,
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

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    this.resultCallback = resultDataCallback;
    this.simpleTherapyContainer.validateAndConfirmOrder();
  },
  /**
   * @Override
   */
  destroy: function()
  {
    this._abortRenderConditionTask();
    this.callSuper();
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {app.views.medications.common.dto.Therapy}
   */
  getTherapy: function()
  {
    return this.therapy;
  },

  /**
   * @returns {Array<app.views.medications.common.dto.MedicationData>}
   */
  getMedicationData: function()
  {
    return this.medicationData;
  }
});

