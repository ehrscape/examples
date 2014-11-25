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
  /** privates */
  resultCallback: null,
  medications: null,
  /** privates: components */
  simpleTherapyContainer: null,
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

    var appFactory = this.view.getAppFactory();
    appFactory.createConditionTask(
        function()
        {
          self._presentValue();
        },
        function()
        {
          return self.isRendered(true);
        },
        50, 1000
    );
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;

    var editingStartDateTime = new Date();
    editingStartDateTime.setSeconds(0);
    editingStartDateTime.setMilliseconds(0);

    this.simpleTherapyContainer = new app.views.medications.ordering.SimpleTherapyContainer({
      view: this.view,
      editMode: !this.copyTherapy,
      copyMode: this.copyTherapy,
      isPastMode: this.isPastMode,
      getTherapyStartNotBeforeDateFunction:  function()
      {
        return self.isPastMode == true ? null : self.saveDateTimePane.isHidden() ? editingStartDateTime : self.saveDateTimePane.getSaveDateTime();
      },
      confirmTherapyEvent: function(result)
      {
        if (result == "VALIDATION_FAILED")
        {
          self.resultCallback(new app.views.common.AppResultData({success: false}));
        }
        else if (!self.isPastMode && !self.performerContainer.getPerformer())
        {
          var warningSystemDialog = self.view.getAppFactory().createWarningSystemDialog(self.view.getDictionary("prescriber.not.defined.warning"));
          warningSystemDialog.setWidth(320);
          warningSystemDialog.setHeight(122);
          warningSystemDialog.show();
          self.resultCallback(new app.views.common.AppResultData({success: false}));
        }
        else
        {
          self._saveTherapy(result);
        }
      },
      medicationsProviderFunction: function()
      {
        return self.medications;
      },
      saveDateTimePaneEvent: function()
      {
        self.saveDateTimePane.setHeight(34);
        self.saveDateTimePane.setPadding('4 0 0 0');
        self.saveDateTimePane.show();
        self.saveDateTimePane.repaint();
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
    this.add(this.simpleTherapyContainer);
    this.add(new tm.jquery.Container({flex: 1}));
    this.add(this.performerContainer);
    this.add(this.saveDateTimePane);
  },

  _loadSimilarMedications: function(medicationId, routeCode) //Similar medications have same generic and route
  {
    var self = this;
    var medicationsUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_FIND_SIMILAR_MEDICATIONS;
    var params = {
      medicationId: medicationId,
      routeCode: routeCode
    };
    this.view.loadViewData(medicationsUrl, params, null, function(data)
    {
      self.medications.length = 0;
      $.merge(self.medications, data);
    });
  },

  _presentValue: function()
  {
    this._loadSimilarMedications(this.therapy.medication.id, this.therapy.route.code);
    this.simpleTherapyContainer.setSimpleTherapy(this.therapy, this.isPastMode == true);
  },

  _saveTherapy: function(newTherapy)
  {
    var self = this;
    var saveUrl;
    var params;
    var saveDateTime = this.saveDateTimePane.isHidden() ? null : this.saveDateTimePane.getSaveDateTime();
    var centralCaseData = this.view.getCentralCaseData();
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
    this.simpleTherapyContainer.validateAndConfirmOrder();
  }
});

