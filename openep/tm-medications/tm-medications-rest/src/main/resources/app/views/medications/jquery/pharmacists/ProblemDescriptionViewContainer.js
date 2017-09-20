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
Class.define('tm.views.medications.pharmacists.ProblemDescriptionViewContainer', 'tm.jquery.Container', {
  cls: "problem-desc-container",

  reviewContainerContentCard: null,
  problemDescription: null,
  titleText: null,

  recommendationField: null,
  categoryField: null,
  impactField: null,
  outcomeField: null,

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
    this._buildGUI();
  },

  ///
  /// private methods
  ///
  _buildGUI: function ()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0));
    var view = this.getReviewContainerContentCard().getReviewContainer().getView();
    var data = this.getProblemDescription();

    var recommendationData = tm.jquery.Utils.isEmpty(data) ? null : data.getRecommendation();
    var categoriesData = !tm.jquery.Utils.isEmpty(data) && !tm.jquery.Utils.isEmpty(data.getCategories()) ?
      data.getCategories().map(function(item){ return item.name }).join(", ") : null;
    var outcomeData = !tm.jquery.Utils.isEmpty(data) && !tm.jquery.Utils.isEmpty(data.getOutcome()) ?
        data.getOutcome().name : null;
    var impactData = !tm.jquery.Utils.isEmpty(data) && !tm.jquery.Utils.isEmpty(data.getImpact()) ?
        data.getImpact().name : null;

    var recommendationContainer = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: this.getTitleText(),
      contentComponent: new tm.jquery.Component({
        cls: "TextData",
        style: "white-space: pre-wrap;",
        html: recommendationData,
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      }),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    this.recommendationField = recommendationContainer.getContentComponent();

    var rightColumn = new tm.jquery.Container({
      cls: "right-column",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "250px")
    });

    var categoryContainer = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary("drug.related.problem"),
      contentComponent: new tm.jquery.Component({
        cls: "TextData",
        html: categoriesData,
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      })
    });
    var impactContainer = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary("clinical.significance"),
      contentComponent: new tm.jquery.Component({
        cls: "TextData",
        html: impactData,
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      })
    });
    var outcomeContainer = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary("expected.outcome"),
      contentComponent: new tm.jquery.Component({
        cls: "TextData",
        html: outcomeData,
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      })
    });

    rightColumn.add(categoryContainer);
    rightColumn.add(impactContainer);
    rightColumn.add(outcomeContainer);

    this.add(recommendationContainer);
    this.add(rightColumn);

    this.recommendationField = recommendationContainer.getContentComponent();
    this.categoryField = categoryContainer.getContentComponent();
    this.outcomeField = outcomeContainer.getContentComponent();
    this.impactField = impactContainer.getContentComponent();
  },

  getProblemDescription: function ()
  {
    return this.problemDescription;
  },

  getTitleText: function ()
  {
    return this.titleText;
  },

  getReviewContainerContentCard: function ()
  {
    return this.reviewContainerContentCard;
  },

  getRecommendationField: function()
  {
    return this.recommendationField;
  },

  getImpactField: function()
  {
    return this.impactField;
  },

  getOutcomeField: function()
  {
    return this.outcomeField;
  },

  getCategoryField: function()
  {
    return this.categoryField;
  },

  problemDescriptionValuesPresent: function (data)
  {
    data = arguments.length > 0 ? data : this.getProblemDescription();

    return !tm.jquery.Utils.isEmpty(data) &&  (!tm.jquery.Utils.isEmpty(data.getOutcome())
        || !tm.jquery.Utils.isEmpty(data.getImpact())
        || (!tm.jquery.Utils.isEmpty(data.getCategories()) && data.getCategories().length > 0));
  },

  setProblemDescription: function(value)
  {
    this.problemDescription = value;
  }
});