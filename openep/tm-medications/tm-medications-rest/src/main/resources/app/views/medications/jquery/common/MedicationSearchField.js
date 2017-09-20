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
Class.define('app.views.medications.common.MedicationSearchField', 'tm.jquery.TreeTypeaheadField', {
  cls: "medication-search-field",
  view: null,

  additionalFilter: null,

  limitSimilarMedication: null,
  limitSimilarRoutes: null,

  /* tm.jquery.TreeTypeaheadField property overrides */
  minLength: 3,
  treeWideMode: true,
  treeShowIcons: false,
  mode: 'advanced',
  clearable: false,

  /* private members */
  _showNonFormularyMedicationButton: null,
  _inputBorderCls: 'input-append',

  Constructor: function(config)
  {
    this.callSuper();

    // Make sure the toggle button is always created, so we can switch modes dynamically.
    // It's either this way or altering the config before calling callSuper.
    if (!this._toggleButton)
    {
      this._toggleButton = new tm.jquery.ToggleButton({
        cls: 'toggle-button',
        html: '<span class="caret"></span>',
        hidden: !this.isLimitSimilarMedication()
      });
      this.setAppend(this._toggleButton);
    }
    else
    {
      this._toggleButton.setHidden(!this.isLimitSimilarMedication());
    }

    // support setting medication as the selection directly. 
    this.selection = this.selection && this.selection instanceof app.views.medications.common.dto.Medication ?
        this._convertMedicationToTreeNode(this.selection) :
        this.selection;


    this.footer = this._buildFooter();
    this.dataLoader = this._buildDataLoader();
  },

  _buildFooter: function()
  {
    var view = this.getView();
    var self = this;
    if (view.isFormularyFilterEnabled() && view.getTherapyAuthority().isNonFormularyMedicationSearchAllowed())
    {
      this._showNonFormularyMedicationButton = new tm.jquery.ToggleButton({
        cls: "toggle-formulary-button",
        type: 'link',
        text: view.getDictionary("show.non.formulary"),
        pressed: false,
        handler: function(component, componentEvent, elementEvent)
        {
          elementEvent.stopPropagation();
          self._refreshDropdownContent();
          component.setText(view.getDictionary(component.isPressed() ? "hide.non.formulary" : "show.non.formulary"));
        }
      });

      var dropdownFooterContainer = new tm.jquery.Container({
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        layout: tm.jquery.HFlexboxLayout.create("flex-start", "center")
      });
      dropdownFooterContainer.add(this._showNonFormularyMedicationButton);

      return dropdownFooterContainer;
    }

    return null;
  },

  _buildDataLoader: function()
  {
    var self = this;
    var view = this.getView();

    return function(component, requestParams, processCallback)
    {
      if (!self.isLimitSimilarMedication())
      {
        view.getRestApi().loadMedications(
            requestParams.searchQuery, this._buildAdditionalRequestParams(), true).then(
            function onResult(data)
            {
              processCallback(data, requestParams);
            });
      }
      else
      {
        view.getRestApi().loadSimilarMedications(
            self.getLimitSimilarMedication().getId(),
            this.getLimitSimilarRoutes(),
            true).then(
            function onResult(data)
            {
              processCallback(data, requestParams);
            });
      }
    }
  },

  /**
   * @returns {Array<String>}
   * @private
   */
  _buildAdditionalRequestParams: function()
  {
    var filterEnums = app.views.medications.TherapyEnums.medicationFinderFilterEnum;
    var filterList = [];
    var view = this.getView();

    if (!tm.jquery.Utils.isEmpty(this.additionalFilter))
    {
      filterList.push(this.additionalFilter);
    }

    if (view.isFormularyFilterEnabled())
    {
      if (!view.getTherapyAuthority().isNonFormularyMedicationSearchAllowed()
          || (this._showNonFormularyMedicationButton && !this._showNonFormularyMedicationButton.isPressed()))
      {
        filterList.push(filterEnums.FORMULARY);
      }
    }

    return filterList;
  },

  _doTreeRefresh: function()
  {
    if (tm.jquery.Utils.isEmpty(this._activeSearchQuery) || this._activeSearchQuery != this.getValue())
    {
      if (tm.jquery.Utils.isEmpty(this._$treeContainerDialog) == false)
      {
        this._tree.restore();
      }
    }
  },

  /**
   *
   * @param {app.views.medications.common.dto.Medication} medication
   * @private
   */
  _convertMedicationToTreeNode: function(medication)
  {
    return medication ? new tm.jquery.tree.Node({
      key: medication.getId() ? medication.getId() : ('UNCODED' + medication.hash()),
      title: medication.getDisplayName(),
      data: medication
    }) : medication;
  },

  _refreshDropdownContent: function()
  {
    this._activeSearchQuery = null;
    this._doTreeRefresh();
  },

  /**
   * Override, remove the added input-append class from the root div if the toggle button is not visible to prevent
   * sharp borders instead of round ones.
   * @returns {Element}
   */
  createDom: function()
  {
    var div = this.callSuper();

    if (!this.isLimitSimilarMedication())
    {
      $(div).removeClass(this._inputBorderCls);
    }

    return div;
  },

  /**
   * @param {app.views.medications.common.dto.Medication|null} medication
   * @param {Array<String>} [routes=null]
   */
  setLimitBySimilar: function(medication, routes)
  {
    this.limitSimilarMedication = medication;
    this.limitSimilarRoutes = medication ? routes : null;
    this.dataLoader = this._buildDataLoader();
    this._applyToggleButtonVisibility(medication ? true : false);
    this.setEnabled(this.isEnabled()); // prevent free input
  },

  /**
   * Override to support setting Medication as the selection directly. Be advised that
   * getSelection will return tm.jquery.tree.Node where the data is a simple json due to the 
   * FancyTreePlugin implementation.
   * @param selection (app.views.medications.common.dto.Medication|tm.jquery.tree.Node)
   * @param preventEvent (optional)
   * @param preventValueChange (optional)
   */
  setSelection: function(selection, preventEvent, preventValueChange)
  {
    if (selection instanceof app.views.medications.common.dto.Medication)
    {
      selection = this._convertMedicationToTreeNode(selection);
    }
    
    this.callSuper(selection, preventEvent, preventValueChange);
  },

  /**
   * Helper method to get the selected medication from the selected node, if any.
   * @returns {app.views.medications.common.dto.Medication|null}
   */
  getSelectionMedication: function()
  {
    var selection = this.getSelection();
    if (selection)
    {
      // don't use instanceof, due to json cloning from TreeTypeAhead breaking instanceof, and
      // using the broken object to create a new object seems to break the new object properties
      if (selection.getData() && !JS.isType(selection.getData(), app.views.medications.common.dto.Medication))
      {
        return new app.views.medications.common.dto.Medication(selection.getData());
      }
      return selection.getData();
    }
    return null;
  },

  /**
   * @returns {app.views.medications.common.dto.Medication|null}
   */
  getLimitSimilarMedication: function()
  {
    return this.limitSimilarMedication;
  },

  /**
   * @returns {boolean}
   */
  isLimitSimilarMedication: function()
  {
    return !tm.jquery.Utils.isEmpty(this.getLimitSimilarMedication());
  },

  /**
   * @returns {Array<Object>}
   */
  getLimitSimilarRoutes: function()
  {
    return this.limitSimilarRoutes;
  },

  /**
   * @returns {string|null}
   */
  getAdditionalFilter: function()
  {
    return this.additionalFilter;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @param {boolean} value
   */
  _applyToggleButtonVisibility: function(value)
  {
    if (this.isRendered())
    {
      value ? this._toggleButton.show() : this._toggleButton.hide();
    }
    else
    {
      this._toggleButton.setHidden(!value);
    }

    if (this.dom)
    {
      var $dom = $(this.dom);
      if (value)
      {
        if (!$dom.hasClass(this._inputBorderCls))
        {
          $dom.addClass(this._inputBorderCls);
        }
      }
      else
      {
        $dom.removeClass(this._inputBorderCls)
      }
    }
  },

  abortUnlicensedMedicationSelection: function()
  {
    var self = this;

    this.getView().getRestApi().loadUnlicensedMedicationWarning().then(function(warning)
    {
      self._warnAndAbortUnlicensedMedicationSelection(warning);
    })
  },

  /**
   * Override the logic and keep the toggle button enabled if the similar medication limit is set. Also applies
   * the disabled status to the input component to prevent free text searches in such a mode, regardless if
   * the component is set enabled.
   * @param enabled
   */
  applyEnabled: function(enabled)
  {
    this.callSuper(enabled && !this.isLimitSimilarMedication());

    if (this.getAppend() instanceof tm.jquery.Button)
    {
      this.getAppend().setEnabled(this.isLimitSimilarMedication() || enabled);
    }
  },

  /**
   * @param {String} warning
   * @private
   */
  _warnAndAbortUnlicensedMedicationSelection: function(warning)
  {
    var self = this;
    var dialog = this.getView().getAppFactory().createWarningSystemDialog(warning, 600, 280);

    dialog.on(tm.jquery.ComponentEvent.EVENT_TYPE_DIALOG_HIDE, function()
    {
      self.setValue(null, true);
      self.focus();
    });

    dialog.show();
  }
});