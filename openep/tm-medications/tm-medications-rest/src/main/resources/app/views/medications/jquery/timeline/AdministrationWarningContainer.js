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
Class.define('tm.views.medications.timeline.AdministrationWarningContainer', 'tm.jquery.Container', {
  cls: 'administration-warning-container',
  view: null,
  _warningsContentContainer: null,
  _hasRestrictiveWarnings: false,
  /** privates: components */
  _ingredientRuleWarningContainer: null,
  _loadingWarningsImg: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-end", "center", 0));
    this._buildGui();
  },

  _buildGui: function()
  {
    this._warningsContentContainer = new tm.jquery.Component({
      cls: "TextDataLight administrations-warning",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    
    this._ingredientRuleWarningContainer = new tm.jquery.Component({
      cls: "TextDataLight ingredient-rule-warning",
      hidden: true
    });

    this._loadingWarningsImg = new tm.jquery.Container({
      cls: 'loader',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),

      hidden: true
    });
    this.add(this._warningsContentContainer);
    this.add(this._ingredientRuleWarningContainer);
    this.add(this._loadingWarningsImg);
  },
  
  handleIngredientWarning: function(ingredientWarning)
  {
    this._ingredientRuleWarningContainer.setHtml(ingredientWarning);

    if (!tm.jquery.Utils.isEmpty(ingredientWarning))
    {
      this._ingredientRuleWarningContainer.isRendered()
          ? this._ingredientRuleWarningContainer.show()
          : this._ingredientRuleWarningContainer.setHidden(false);
    }
    else
    {
      this._ingredientRuleWarningContainer.isRendered()
          ? this._ingredientRuleWarningContainer.hide()
          : this._ingredientRuleWarningContainer.setHidden(true);
    }
  },

  showLoadingIcon: function(value)
  {
    if (value === true)
    {
      this._loadingWarningsImg.isRendered()
          ? this._loadingWarningsImg.show()
          : this._loadingWarningsImg.setHidden(false);
    }
    else
    {
      this._loadingWarningsImg.isRendered()
          ? this._loadingWarningsImg.hide()
          : this._loadingWarningsImg.setHidden(true);
    }
  },

  /**
   * @param {app.views.medications.timeline.administration.AdministrationWarnings} warnings
   */
  setRestrictiveWarnings: function(warnings)
  {
    var warningsHtml = "";

    if (warnings.getJumpWarning())
    {
      addBreakLine();
      warningsHtml += warnings.getJumpWarning();
    }
    if (warnings.getInfusionInactiveWarning())
    {
      addBreakLine();
      warningsHtml += warnings.getInfusionInactiveWarning();
    }
    if (warnings.getAdministrationInFutureWarning())
    {
      addBreakLine();
      warningsHtml += warnings.getAdministrationInFutureWarning();
    }
    if (warnings.getMaxAdministrationsWarning())
    {
      addBreakLine();
      warningsHtml += warnings.getMaxAdministrationsWarning();
    }
    if (warnings.getTherapyNotReviewedWarning())
    {
      addBreakLine();
      warningsHtml += warnings.getTherapyNotReviewedWarning();
    }

    this._warningsContentContainer.setHtml(warningsHtml);

    function addBreakLine()
    {
      if (warningsHtml)
      {
        warningsHtml += "<br>";
      }
    }
  }
});
