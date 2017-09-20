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
Class.define('app.views.medications.common.TherapyDetailsLegendContainer', 'tm.jquery.Container', {
  cls: "legend-details-container",
  scrollable: 'vertical',

  therapy: null,
  medicationData: null,
  data: null,
  view: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildLegendContainer();
  },

  _buildLegendContainer: function()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0));

    var view = this.getView();
    var therapy = this.getTherapy();

    var contentContainer = new tm.jquery.Container({
      cls: "legend-content-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    var labelColumn = new tm.jquery.Container({
      cls: "TextLabel row-label",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      html: view.getDictionary('legend')
    });
    contentContainer.add(labelColumn);

    var descriptionColumn = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    if (!therapy.isOrderTypeOxygen() && !tm.jquery.Utils.isEmpty(therapy.getBnfMaximumPercentage()))
    {
      var bnfWarningsCls =
          tm.views.medications.warning.WarningsHelpers.getImageClsForBnfMaximumPercentage(therapy.getBnfMaximumPercentage());
      descriptionColumn.add(this._createLegendIconDescriptionRow(bnfWarningsCls, " " + view.getDictionary('BNF.maximum')));
    }
    this._addTherapyStatusLegendContent(descriptionColumn);

    if (tm.jquery.Utils.isArray(this.getData().tasks))
    {
      this._addTaskRemindersLegendContent(descriptionColumn);
    }

    this._addConflictedMedicationsLegendContent(descriptionColumn);

    contentContainer.add(descriptionColumn);
    this.add(contentContainer);
  },

  _addTherapyStatusLegendContent: function(descriptionColumn)
  {
    var view = this.getView();
    var therapy = this.getTherapy();
    var data = this.getData();
    var displayProvider = this.getDisplayProvider();

    if (displayProvider.getStatusIcon(data))
    {
      var therapyStatusIcon = displayProvider.getStatusIcon(data);
      var therapyStatusDescription = displayProvider.getStatusDescription(data);
      descriptionColumn.add(this._createLegendIconDescriptionRow(therapyStatusIcon, " " + therapyStatusDescription));
    }

    if (data.modifiedFromLastReview || data.completed === false || therapy.completed === false)
    {
      var therapyModifiedIcon = "icon_changed";
      var therapyModifiedDescription = view.getDictionary('modified');
      descriptionColumn.add(this._createLegendIconDescriptionRow(therapyModifiedIcon, " " + therapyModifiedDescription));
    }

    if (therapy !== null && therapy.getLinkName())
    {
      var link = therapy.getLinkName();
      if (link.length <= 3)
      {
        var therapyLinkedIcon = "icon_link";
        var therapyLinkedDescription = view.getDictionary('linked.infusion');
        descriptionColumn.add(this._createLegendIconDescriptionRow(therapyLinkedIcon, " " + therapyLinkedDescription, link));
      }
    }
    if (data.showConsecutiveDay)
    {
      var consecutiveDayIcon = "icon_day_number";
      var consecutiveDayDescription = view.getDictionary('consecutive.days.antibiotic');
      descriptionColumn.add(this._createLegendIconDescriptionRow(consecutiveDayIcon, " " + consecutiveDayDescription,
          data.consecutiveDay));
    }

    if (!tm.jquery.Utils.isEmpty(therapy.getCriticalWarnings()) && therapy.getCriticalWarnings().length > 0)
    {
      var criticalWarningsIcon = "icon_warning";
      var criticalWarningsDescription = view.getDictionary('critical.warnings');
      descriptionColumn.add(this._createLegendIconDescriptionRow(criticalWarningsIcon, " " + criticalWarningsDescription));
    }

    if (view.getTherapyAuthority().isShowPharmacistReviewStatus() && this.getTherapyPharmacistReviewStatus() &&
        displayProvider.getPharmacistReviewIcon(this.getTherapyPharmacistReviewStatus()))
    {
      var pharmacistReviewStatus = this.getTherapyPharmacistReviewStatus();
      var pharmacistReviewIcon = displayProvider.getPharmacistReviewIcon(pharmacistReviewStatus);
      var pharmacistReviewDescription = displayProvider.getPharmacistReviewDescription(pharmacistReviewStatus);
      descriptionColumn.add(this._createLegendIconDescriptionRow(pharmacistReviewIcon, " " + pharmacistReviewDescription));
    }

    if (displayProvider.getSelfAdminStatusIcon(therapy.getSelfAdministeringActionEnum(), this.getTherapyStatus()))
    {
      var selfAdminStatusIcon = displayProvider.getSelfAdminStatusIcon(therapy.getSelfAdministeringActionEnum(),
          this.getTherapyStatus());
      var selfAdminStatusDescription = displayProvider.getSelfAdminStatusDescription(therapy.getSelfAdministeringActionEnum(),
          this.getTherapyStatus());
      descriptionColumn.add(this._createLegendIconDescriptionRow(selfAdminStatusIcon, " " + selfAdminStatusDescription));
    }
  },

  _addTaskRemindersLegendContent: function(descriptionColumn)
  {
    var view = this.getView();
    var data = this.getData();
    var filterBy = new app.views.medications.common.TherapyDetailsLegendContainer.Filters;

    var isTaskLate = data.tasks.some(filterBy.isTaskLate);

    if (data.tasks.some(filterBy.isDoctorReviewTaskActive))
    {
      var doctorReviewIcon = isTaskLate ? "icon-doctor-review-reminder-late" : "icon-doctor-review-reminder";
      descriptionColumn.add(this._createLegendIconDescriptionRow(doctorReviewIcon, " " +
          view.getDictionary('therapy.review.reminder'), null, 24, 24));
    }

    if (data.tasks.some(filterBy.isSwitchToOralTaskActive))
    {
      var switchToOralIcon = isTaskLate ? "icon-switch-to-oral-late" : "icon-switch-to-oral";
      descriptionColumn.add(this._createLegendIconDescriptionRow(switchToOralIcon, "  " +
          view.getDictionary('switch.IV.to.oral'), null, 24, 24));
    }

    if (data.tasks.some(filterBy.isPerfusionSyringeStartTaskActive))
    {
      descriptionColumn.add(this._createLegendIconDescriptionRow("perfusion-syringe-icon start", " " +
          view.getDictionary('perfusion.syringe.preparation'), null, 24, 24));
    }

    if (data.tasks.some(filterBy.isPerfusionSyringeCompleteTaskActive))
    {
      descriptionColumn.add(this._createLegendIconDescriptionRow("perfusion-syringe-icon complete", " " +
          view.getDictionary('perfusion.syringe.task.in.progress'), null, 24, 24));
    }

    if (data.tasks.some(filterBy.isPerfusionSyringeDispenseTaskActive))
    {
      descriptionColumn.add(this._createLegendIconDescriptionRow("perfusion-syringe-icon dispense", " " +
          view.getDictionary('perfusion.syringe.task.closed'), null, 24, 24));
    }

    if (data.tasks.some(filterBy.isSupplyReminderTaskActive))
    {
      descriptionColumn.add(this._createLegendIconDescriptionRow("icon-supply-reminder", " " +
          view.getDictionary('supply.reminder'), null, 24, 24));
    }

    if (data.tasks.some(filterBy.isSupplyReviewTaskActive))
    {
      descriptionColumn.add(this._createLegendIconDescriptionRow("icon-nurse-supply", " " +
          view.getDictionary('nurse.resupply.request'), null, 24, 24));
    }
  },

  _addConflictedMedicationsLegendContent: function(descriptionColumn)
  {
    var view = this.getView();
    var filterBy = new app.views.medications.common.TherapyDetailsLegendContainer.Filters;
    var medicationDataList = this.getMedicationData();

    if (medicationDataList.some(filterBy.isMedicationControlled))
    {
      descriptionColumn.add(this._createLegendIconDescriptionRow("controlled-drug-icon", " " +
          view.getDictionary('controlled.drug')));
    }

    if (view.isFormularyFilterEnabled() && medicationDataList.some(filterBy.isMedicationNonFormulary))
    {
      descriptionColumn.add(this._createLegendIconDescriptionRow("non-formulary-icon", " " +
          view.getDictionary('non.formulary.medication')));
    }

    if (medicationDataList.some(filterBy.isMedicationBlackTriangle))
    {
      descriptionColumn.add(this._createLegendIconDescriptionRow("black-triangle-icon", " " +
          view.getDictionary('black.triangle.medication')));
    }

    if (medicationDataList.some(filterBy.isMedicationUnlicensed))
    {
      descriptionColumn.add(this._createLegendIconDescriptionRow("unlicensed-medication-icon", " " +
          view.getDictionary('unlicensed.medication')));
    }

    if (medicationDataList.some(filterBy.isMedicationHighAlert))
    {
      descriptionColumn.add(this._createLegendIconDescriptionRow("high-alert-icon", " " +
          view.getDictionary('high.alert.medication')));
    }

    if (medicationDataList.some(filterBy.isMedicationClinicalTrial))
    {
      descriptionColumn.add(this._createLegendIconDescriptionRow("trial-medicine-icon", " " +
          view.getDictionary('clinical.trial.medication')));
    }

    if (medicationDataList.some(filterBy.isMedicationExpensive))
    {
      descriptionColumn.add(this._createLegendIconDescriptionRow("expensive-drug-icon", " " +
          view.getDictionary("expensive.drug")));
    }
  },

  _createLegendIconDescriptionRow: function(legendIcon, iconDescription, html, width, height)
  {
    var descriptionRow = new tm.jquery.Container({
      cls: "description-row-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0)
    });

    var descriptionIcon = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      cls: "row-icon " + legendIcon,
      alignSelf: "center",
      html: html,
      width: width ? width : 16,
      height: height ? height : 16
    });
    descriptionRow.add(descriptionIcon);

    var descriptionValue = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      cls: "TextData legend-description",
      html: iconDescription
    });
    descriptionRow.add(descriptionValue);

    return descriptionRow;
  },

  /**
   * Getters & Setters
   */
  /**
   * @returns {app.views.medications.common.dto.Therapy}
   */
  getTherapy: function()
  {
    return this.therapy;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {app.views.medications.common.dto.MedicationData|null}
   */
  getMedicationData: function()
  {
    return this.medicationData;
  },

  getData: function()
  {
    return this.data;
  },

  /**
   * @returns {app.views.medications.TherapyDisplayProvider}
   */
  getDisplayProvider: function()
  {
    return this.displayProvider;
  },

  getTherapyPharmacistReviewStatus: function()
  {
    return this.getData().therapyPharmacistReviewStatus;
  },

  getTherapyStatus: function()
  {
    return this.getData().therapyStatus;
  }
});