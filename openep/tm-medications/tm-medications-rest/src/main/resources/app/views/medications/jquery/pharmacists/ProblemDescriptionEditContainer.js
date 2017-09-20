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
Class.define('tm.views.medications.pharmacists.ProblemDescriptionEditContainer', 'tm.views.medications.pharmacists.ProblemDescriptionViewContainer', {
  cls: "problem-desc-container",

  placeholderContainer: null,
  selectBoxValueKeys: null,

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);

    this.valueKeys = this.getConfigValue("valueKeys", { categories: null, outcome: null, impact: null });
  },

  ///
  /// override
  ///
  _buildGUI: function ()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0));
    var self = this;
    var view = this.getReviewContainerContentCard().getReviewContainer().getView();
    var data = this.getProblemDescription();
    var hasValues = this.problemDescriptionValuesPresent(data);

    var recommendationData = tm.jquery.Utils.isEmpty(data) ? null : data.getRecommendation();
    var categoriesData = !tm.jquery.Utils.isEmpty(data) && !tm.jquery.Utils.isEmpty(data.getCategories()) ?
        data.getCategories() : [];
    var outcomeData = !tm.jquery.Utils.isEmpty(data) && !tm.jquery.Utils.isEmpty(data.getOutcome()) ?
        data.getOutcome() : [];
    var impactData = !tm.jquery.Utils.isEmpty(data) && !tm.jquery.Utils.isEmpty(data.getImpact()) ?
        data.getImpact() : [];

    var recommendationContainer = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: this.getTitleText(),
      contentComponent: new tm.jquery.TextArea({
      //contentComponent: new tm.views.medications.pharmacists.ResizingTextArea({
        rows: 1,
        autoHeight: true,
        value: recommendationData,
        placeholder: "...",
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      }),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    if (!hasValues)
    {
      recommendationContainer.getContentComponent().on(tm.jquery.ComponentEvent.EVENT_TYPE_FOCUS_GAINED, function ()
      {
        self.expand();
      });
    }

    var rightColumn = new tm.jquery.Container({
      cls: "right-column",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "250px")
    });

    var categoryContainer = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary("drug.related.problem"),
      contentComponent: this._createSelectBox(this.getValueKeys().categories, categoriesData, true),
      hidden: !hasValues
    });
    var impactContainer = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary("clinical.significance"),
      contentComponent: this._createSelectBox(this.getValueKeys().impact, impactData, false),
      hidden: !hasValues
    });
    var outcomeContainer = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary("expected.outcome"),
      contentComponent: this._createSelectBox(this.getValueKeys().outcome, outcomeData, false),
      hidden: !hasValues
    });

    var placeholderContainer = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: "...",
      hidden: hasValues,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    rightColumn.add(categoryContainer);
    rightColumn.add(impactContainer);
    rightColumn.add(outcomeContainer);
    rightColumn.add(placeholderContainer);

    this.add(recommendationContainer);
    this.add(rightColumn);

    this.recommendationField = recommendationContainer.getContentComponent();
    this.categoryField = categoryContainer.getContentComponent();
    this.outcomeField = outcomeContainer.getContentComponent();
    this.impactField = impactContainer.getContentComponent();
    this.placeholderContainer = placeholderContainer;
  },

  _createSelectBox: function (valuesKey, selections, multiValue)
  {
    selections = !tm.jquery.Utils.isEmpty(selections) && tm.jquery.Utils.isArray(selections) ? selections : [ selections ];

    var options = this.getSelectBoxValues(valuesKey).map(function(item){
      return tm.jquery.SelectBox.createOption(item, null);
    });
    var appFactory = this.getReviewContainerContentCard().getReviewContainer().getView().getAppFactory();

    var selectBox = new tm.jquery.SelectBox({
      appendTo: function ()
      {
        return appFactory.getDefaultRenderToElement();
      },
      //dropdownWidth: 250,
      dropdownHeight: 5,
      dropdownAlignment: "right",

      options: options,
      selections: selections,

      multiple: multiValue,
      allowSingleDeselect: !multiValue,
      defaultTextProvider: this._selectBoxTextProvider,
      defaultValueCompareToFunction: this._selectBoxValueCompare
    });
    selectBox.onKey(new tm.jquery.event.KeyStroke({key: "esc", altKey: false, ctrlKey: false, shiftKey: false}),
        function (component, componentEvent, elementEvent)
        {
          elementEvent.stopPropagation();
        });


    return selectBox;
  },

  _selectBoxTextProvider: function (selectBox, index, option)
  {
    var item = option.getValue();
    return tm.jquery.Utils.isEmpty(item) ? null : item.name;
  },

  _selectBoxValueCompare: function (value1, value2)
  {
    return (tm.jquery.Utils.isEmpty(value1) ? null : value1.id)
        === (tm.jquery.Utils.isEmpty(value2) ? null : value2.id);
  },

  _animate: function (object, animation, speed)
  {
    var $animObject = $(object.getDom());
    $animObject.css('-webkit-animation-duration', speed / 1000 + 's');
    $animObject.css('animation-duration', speed / 1000 + 's');

    if (!$animObject.hasClass("animated")) $animObject.addClass("animated");
    $animObject.removeClass(animation).addClass(animation);
    var wait = setTimeout(function ()
        {
          $animObject.removeClass("animated");
          $animObject.removeClass(animation);
        },
        speed
    );
  },

  getPlaceholderContainer: function ()
  {
    return this.placeholderContainer;
  },

  getRecommendationField: function()
  {
    return this.recommendationField;
  },

  getSelectBoxValues: function (key)
  {
    var view = this.getReviewContainerContentCard().getReviewContainer().getView();
    var identitiesMap = view.getProblemDescriptionNamedIdentitiesMap();

    if (!tm.jquery.Utils.isEmpty(identitiesMap))
    {
      return identitiesMap.hasOwnProperty(key) && tm.jquery.Utils.isArray(identitiesMap[key]) ?
          identitiesMap[key] : [];
    }

    return [];
  },

  getValueKeys: function()
  {
    return this.valueKeys;
  },

  expand: function ()
  {
    if (this.getPlaceholderContainer().isHidden()) return;

    this.getPlaceholderContainer().hide();
    this._animate(this.getCategoryField().getParent(), 'fadeIn', 500);
    this.getCategoryField().getParent().show();
    this._animate(this.getImpactField().getParent(), 'fadeIn', 500);
    this.getImpactField().getParent().show();
    this._animate(this.getOutcomeField().getParent(), 'fadeIn', 500);
    this.getOutcomeField().getParent().show();
  },

  collapse: function()
  {
    if (!this.getPlaceholderContainer().isHidden()) return;

    this.getCategoryField().getParent().hide();
    this.getImpactField().getParent().hide();
    this.getOutcomeField().getParent().hide();
    this.getPlaceholderContainer().show();
  },

  hasValues: function()
  {
    return !tm.jquery.Utils.isEmpty(this.getRecommendationField().getValue())
        || this.getCategoryField().getSelections().length > 0
        || this.getImpactField().getSelections().length > 0
        || this.getOutcomeField().getSelections().length > 0;
  },

  toTherapyProblemDescription: function(therapyProblemDescription)
  {
    if (tm.jquery.Utils.isEmpty(therapyProblemDescription)
        || !(therapyProblemDescription instanceof tm.views.medications.pharmacists.dto.TherapyProblemDescription)) return;

    therapyProblemDescription.setRecommendation(this.getRecommendationField().getValue());
    therapyProblemDescription.setCategories(this.getCategoryField().getSelections());
    therapyProblemDescription.setImpact(this.getImpactField().getSelections().length > 0 ?
        this.getImpactField().getSelections()[0] : null);
    therapyProblemDescription.setOutcome(this.getOutcomeField().getSelections().length > 0 ?
        this.getOutcomeField().getSelections()[0] : null);
  },

  problemDescriptionValuesPresent: function (data)
  {
    data = arguments.length > 0 ? data : this.getProblemDescription();

    return !tm.jquery.Utils.isEmpty(data) && (!tm.jquery.Utils.isEmpty(data.getOutcome())
        || !tm.jquery.Utils.isEmpty(data.getImpact()) || !tm.jquery.Utils.isEmpty(data.getCategories()));
  }
});