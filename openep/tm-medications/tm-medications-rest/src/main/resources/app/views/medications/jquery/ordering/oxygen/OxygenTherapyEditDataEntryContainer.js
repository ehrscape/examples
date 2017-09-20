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
Class.define('app.views.medications.ordering.oxygen.OxygenTherapyEditDataEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "edit-oxygen-therapy-dialog",
  scrollable: 'vertical',

  therapy: null,
  saveTherapyFunction: null,
  pastMode: false, /* required by DRP */
  copyMode: false, /* set true if we're copying instead of editing the therapy */
  therapyAlreadyStarted: false,
  modifiedInThePast: false,

  _oxygenTherapyContainer: null,
  _renderConditionTask: null,
  _restApi: null,
  _performerContainer: null, /* required by DRP */
  _saveDateTimePane: null, /* requred by DRP */

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    var self = this;
    var appFactory = this.getView().getAppFactory();

    this._buildGUI();

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      self._abortRenderConditionTask();
      self._renderConditionTask = appFactory.createConditionTask(
          function()
          {
            self._presentData();
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

  _buildGUI: function()
  {
    var view = this.getView();
    var self = this;
    
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));

    this._oxygenTherapyContainer = new app.views.medications.ordering.OxygenTherapyContainer({
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      view: view,
      orderMode: false,
      pastMode: this.isPastMode(),
      changeReasonRequired: this.getTherapy().isLinkedToAdmission()
    });
    this._oxygenTherapyContainer.on(
        app.views.medications.ordering.OxygenTherapyContainer.EVENT_TYPE_ENABLE_SAVE_TIMESTAMP_CHANGE,
        function()
        {
          self._saveDateTimePane.setHeight(34);
          self._saveDateTimePane.setPadding('4 0 0 0');
          self._saveDateTimePane.show();
        }
    );

    this.add(this._oxygenTherapyContainer);
    
    if (this.isPastMode())
    {
      var careProfessionals = view.getCareProfessionals();
      var currentUserAsCareProfessionalName = view.getCurrentUserAsCareProfessional() ?
          view.getCurrentUserAsCareProfessional().name : null;

      this._performerContainer =
          tm.views.medications.MedicationUtils.createPerformerContainer(
              view,
              careProfessionals,
              currentUserAsCareProfessionalName);

      this.add(this._performerContainer);
    }

    this._saveDateTimePane = new app.views.medications.ordering.TherapySaveDatePane({
      hidden: true
    });
    this.add(this._saveDateTimePane);
  },

  _presentData: function()
  {
    var self = this;
    var medicationId = this.getTherapy().getMedication().getId();
    var showPreviousAdministrations = !this.isCopyMode() && (this.isTherapyAlreadyStarted() || this.isModifiedInThePast());

    this.getView().getRestApi().loadMedicationData(medicationId)
        .then(function onMedicationLoad(medicationData)
        {
          if (self.isRendered())
          {
            self._oxygenTherapyContainer.setMedicationData(medicationData);
            self._oxygenTherapyContainer.setValues(self.getTherapy(), showPreviousAdministrations, self.isCopyMode());
          }
        });
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
   * @param {app.views.medications.common.dto.OxygenTherapy|app.views.medications.common.dto.Therapy} therapy
   * @param {app.views.medications.common.dto.TherapyChangeReason} changeReason
   * @param {Object} prescriber
   * @param {Date} saveDateTime
   * @returns {tm.jquery.Promise}
   * @private
   */
  _saveTherapy: function (therapy, changeReason, prescriber, saveDateTime)
  {
    var self = this;

    if (this.isCopyMode())
    {
      var medicationOrder =  [
        new app.views.medications.common.dto.SaveMedicationOrder({
          therapy: therapy,
          actionEnum: app.views.medications.TherapyEnums.medicationOrderActionEnum.PRESCRIBE
        })
      ];

      return this.getView().getRestApi().saveMedicationsOrder(medicationOrder, prescriber, saveDateTime, null, false);
    }
    else
    {
      therapy.setCompositionUid(this.getTherapy().getCompositionUid());
      therapy.setEhrOrderName(this.getTherapy().getEhrOrderName());
      
      return this.getView().getRestApi().modifyTherapy(
          therapy, 
          changeReason, 
          prescriber, 
          this.isTherapyAlreadyStarted(), 
          saveDateTime, 
          false);
    }
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
  isTherapyAlreadyStarted: function()
  {
    return this.therapyAlreadyStarted === true;
  },

  /**
   * @returns {boolean}
   */
  isCopyMode: function()
  {
    return this.copyMode === true;
  },

  /**
   * @returns {boolean}
   */
  isModifiedInThePast: function()
  {
    return this.modifiedInThePast === true;
  },

  destroy: function()
  {
    this._abortRenderConditionTask();
    this.callSuper();
  },

  processResultData: function(resultDataCallback)
  {
    var self = this;

    if (!this._saveDateTimePane.isHidden())
    {
      this._oxygenTherapyContainer.setEditStartTimestamp(this._saveDateTimePane.getSaveDateTime());
    }

    this._oxygenTherapyContainer.setConfirmTherapyCallback(function onProcessResult(result, changeReason)
    {
      if (result == "VALIDATION_FAILED")
      {
        resultDataCallback(new app.views.common.AppResultData({success: false}));
      }
      else
      {
        var performer = self._performerContainer != null ?
            self._performerContainer.getPerformer() : self.getView().getCurrentUserAsCareProfessional();

        if (self.saveTherapyFunction)
        {
          self.saveTherapyFunction(result, performer);
          resultDataCallback(new app.views.common.AppResultData({success: true}));
        }
        else
        {
          self._saveTherapy(
              result,
              changeReason,
              performer,
              self._saveDateTimePane.isHidden() ? null : self._saveDateTimePane.getSaveDateTime())
              .then(
                  function onSuccess()
                  {
                    resultDataCallback(new app.views.common.AppResultData({success: true}));
                  },
                  function onFail()
                  {
                    resultDataCallback(new app.views.common.AppResultData({success: false}));
                  }
              );
        }
      }
    });

    this._oxygenTherapyContainer.validateAndConfirmOrder();
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {app.views.medications.common.dto.OxygenTherapy}
   */
  getTherapy: function()
  {
    return this.therapy;
  }
});
