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
  therapy: null,
  copyTherapy: false,
  hideDialogFunction: null, //optional
  isPastMode: false,
  /** privates */
  resultCallback: null,
  medications: null,
  /** privates: components */
  complexTherapyContainer: null,
  performerContainer: null,
  saveDateTimePane: null,

  /** constructor */
  Constructor: function(config)
  {
    var self = this;
    this.medications = [];
    this.callSuper(config);

    this.setLayout(tm.jquery.VFlexboxLayout.create('start', "stretch", 0));
    this._buildComponents();
    this._buildGui();
    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      self._presentValue();
    });
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    var editingStartTimestamp = new Date();
    editingStartTimestamp.setSeconds(0);
    editingStartTimestamp.setMilliseconds(0);

    this.complexTherapyContainer = new app.views.medications.ordering.ComplexTherapyContainer({
      view: this.view,
      startProcessOnEnter: true,
      flex: 1,
      editMode: true,
      copyTherapy: this.copyTherapy,
      isPastMode: this.isPastMode,
      getTherapyStartNotBeforeDateFunction: function()
      {
        return self.isPastMode == true ? null : self.saveDateTimePane.isHidden() ? editingStartTimestamp : self.saveDateTimePane.getSaveDateTime();
      },
      confirmTherapyEvent: function(result)
      {
        if (result == "VALIDATION_FAILED")
        {
          self.resultCallback(new app.views.common.AppResultData({success: false}));
        }
        else if (!self.isPastMode && !self.performerContainer.getPerformer())
        {
          appFactory.createWarningSystemDialog(self.view.getDictionary("prescriber.not.defined.warning"), 320, 122).show();
          self.resultCallback(new app.views.common.AppResultData({success: false}));
        }
        else
        {
          self._saveTherapy(result);
        }
      },
      saveDateTimePaneEvent: function()
      {
        self.saveDateTimePane.setHeight(34);
        self.saveDateTimePane.setPadding('4 0 0 0');
        self.saveDateTimePane.show();
        self.saveDateTimePane.repaint();
      },
      closeDialogFunction: function()
      {
        if (self.hideDialogFunction)
        {
          self.hideDialogFunction();
        }
      }
    });

    var careProfessionals = this.view.getCareProfessionals();
    var currentUserAsCareProfessionalName = null;
    if (this.isPastMode)
    {
      currentUserAsCareProfessionalName = this.therapy.prescriberName;
    }
    else if (this.view.getCurrentUserAsCareProfessional())
    {
      currentUserAsCareProfessionalName = this.view.getCurrentUserAsCareProfessional().name;
    }
    this.performerContainer =
        tm.views.medications.MedicationUtils.createPerformerContainer(this.view, careProfessionals, currentUserAsCareProfessionalName);

    this.saveDateTimePane = new app.views.medications.ordering.TherapySaveDatePane();
    this.saveDateTimePane.hide();
  },

  _buildGui: function()
  {
    this.add(this.complexTherapyContainer);
    this.add(this.performerContainer);
    this.add(this.saveDateTimePane);
  },

  _presentValue: function()
  {
    this.complexTherapyContainer.setComplexTherapy(this.therapy, this.isPastMode == true);
  },

  _saveTherapy: function(newTherapy)
  {
    var self = this;
    var saveUrl;
    var params;
    var saveDateTime = this.saveDateTimePane.isHidden() ? null : this.saveDateTimePane.getSaveDateTime();
    var centralCaseData = self.view.getCentralCaseData(); //TherapyCentralCaseData
    if (this.copyTherapy)
    {
      saveUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_SAVE_MEDICATIONS_ORDER;
      params = {
        patientId: self.view.getPatientId(),
        therapies: JSON.stringify([newTherapy]),
        centralCaseId: centralCaseData ? centralCaseData.centralCaseId : null,
        careProviderId: centralCaseData ? centralCaseData.careProviderId : null,
        sessionId: centralCaseData && centralCaseData.sessionId ? centralCaseData.sessionId : null,
        knownOrganizationalEntity: self.view.getKnownOrganizationalEntity(),
        prescriber: JSON.stringify(this.performerContainer.getPerformer()),
        roundsInterval: JSON.stringify(self.view.getRoundsInterval()),
        saveDateTime: JSON.stringify(saveDateTime)
      };
    }
    else
    {
      newTherapy.compositionUid = this.therapy.compositionUid;
      newTherapy.ehrOrderName = this.therapy.ehrOrderName;
      saveUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_MODIFY_THERAPY;
      params = {
        patientId: this.view.getPatientId(),
        therapy: JSON.stringify(newTherapy),
        centralCaseId: centralCaseData ? centralCaseData.centralCaseId : null,
        careProviderId: centralCaseData ? centralCaseData.careProviderId : null,
        sessionId: centralCaseData && centralCaseData.sessionId ? centralCaseData.sessionId : null,
        knownOrganizationalEntity: self.view.getKnownOrganizationalEntity(),
        prescriber: JSON.stringify(self.performerContainer.getPerformer()),
        saveDateTime: JSON.stringify(saveDateTime)
      };
    }

    this.view.loadPostViewData(saveUrl, params, null,
        function()
        {
          var resultData = new app.views.common.AppResultData({success: true});
          self.resultCallback(resultData);
        },
        function()
        {
          var resultData = new app.views.common.AppResultData({success: false});
          self.resultCallback(resultData);
        },
        true);
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    this.resultCallback = resultDataCallback;
    this.complexTherapyContainer.validateAndConfirmOrder();
  }
});

