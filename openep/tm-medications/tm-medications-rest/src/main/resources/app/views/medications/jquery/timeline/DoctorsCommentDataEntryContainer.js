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
Class.define('tm.views.medications.timeline.DoctorsCommentDataEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: 'doctors-comment-container',
  scrollable: 'vertical',
  view: null,
  administration: null,
  therapy: null,

  /** components */

  _commentField: null,
  _therapyDescriptionContainer: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    var self = this;

    this._buildGui();
    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      setTimeout(function()
      {
        self._commentField.focus()
      }, 0);
    });
  },

  _buildGui: function()
  {
    var view = this.getView();
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"));

    this._therapyDescriptionContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create('flex-start', 'flex-start'),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      alignSelf: 'stretch',
      html: this.getTherapy().getFormattedTherapyDisplay(),
      cls: 'TherapyDescription'
    });

    if (this.getAdministration().plannedTime)
    {
      this._therapyDescriptionContainer.setHtml(this._therapyDescriptionContainer.getHtml() +
          tm.views.medications.MedicationTimingUtils.getFormattedAdministrationPlannedTime(view, this.getAdministration()));
    }

    var commentLabel = new tm.jquery.Container({
      cls: 'TextLabel',
      html: view.getDictionary('doctors.comment')
    });
    this._commentField = new tm.jquery.TextField({
      width: 400
    });
    this.add(this._therapyDescriptionContainer);
    this.add(commentLabel);
    this.add(this._commentField);
  },

  /**
   * @returns {Object}
   */
  getAdministration: function()
  {
    return this.administration;
  },

  /**
   * @returns {app.views.medications.common.dto.Therapy}
   */
  getTherapy: function()
  {
    return this.therapy;
  },

  /**
   * @returns {tm.jquery.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },
  processResultData: function(resultDataCallback)
  {
    var self = this;
    var view = this.getView();
    var failResultData = new app.views.common.AppResultData({success: false});
    var validationForm = new tm.jquery.Form({
      onValidationSuccess: onValidationSuccessFn,
      onValidationError: function()
      {
        resultDataCallback(failResultData);
      },
      requiredFieldValidatorErrorMessage: this.getView().getDictionary("field.value.is.required")
    });
    validationForm.addFormField(new tm.jquery.FormField({
      component: self._commentField,
      required: true
    }));
    validationForm.submit();

    function onValidationSuccessFn()
    {
      view.getRestApi().setAdministrationDoctorsComment(
          self.getAdministration().taskId,
          self._commentField.getValue(),
          false).then(
          function onSuccess()
          {
            resultDataCallback(new app.views.common.AppResultData({success: true}));
          },
          function onFailure()
          {
            resultDataCallback(failResultData);
          }
      );
    }
  }
});
