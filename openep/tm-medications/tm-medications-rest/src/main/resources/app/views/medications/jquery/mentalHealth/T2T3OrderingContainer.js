/*
 * Copyright (c) 2010-2016 Marand d.o.o. (www.marand.com)
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
Class.define('app.views.medications.mentalHealth.T2T3OrderingContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "medications-ordering-container",
  padding: 0,

  view: null,
  patientId: null,

  orderingContainer: null,
  basketContainer: null,

  validationForm: null,

  defaultWidth: null,
  defaultHeight: null,
  reportType: null,
  
  _basketTherapyDisplayProvider: null,
  _orderTherapyDisplayProvider: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    
    this._basketTherapyDisplayProvider = this._createBasketContainerDisplayProvider();
    this._orderTherapyDisplayProvider = this._createOrderingContainerDisplayProvider();

    this._buildGui();
    this._configureValidationForm();
  },

  _buildGui: function()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    
    this.orderingContainer = this.buildOrderingContainer();
    this.basketContainer = this.buildBasketContainer();

    var mainContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    mainContainer.add(this.orderingContainer);
    var eastContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    eastContainer.add(this.basketContainer);
    mainContainer.add(eastContainer);
    this.add(mainContainer);
  },

  _configureValidationForm: function()
  {
    var self = this;
    var form = new tm.jquery.Form({
      view: self.getView(),
      requiredFieldValidatorErrorMessage: self.getView().getDictionary("value.bnf.between.interval.warning")
    });

    var bnfMaximumContainer = self.getBasketContainer().bnfMaximumContainer;

    form.addFormField(new tm.jquery.FormField({
      component: bnfMaximumContainer.bnfMaximumTextField,
      required: true,
      label: bnfMaximumContainer.bnfMaximumLabel,
      componentValueImplementationFn: function()
      {
        var value = bnfMaximumContainer.getResult();
        var isNumeric = tm.jquery.Utils.isNumeric(value);
        var isEmpty = tm.jquery.Utils.isEmpty(value) || 0 === value.length;

        if (isEmpty || (isNumeric && value >= 100 && value <= 200))
        {
          return true;
        }
        return null;
      }
    }));

    this.validationForm = form;
  },

  _addToBasket: function(data, options)
  {
    this.getBasketContainer().addTherapy(data, options);
  },

  _handleTherapiesRemovedEvent: function(dataRemoved)
  {
    var groupedMentalHealthDrugs = [];
    dataRemoved.forEach(function(data)
    {
      if (!tm.jquery.Utils.isEmpty(data.group))
      {
        groupedMentalHealthDrugs.push(data);
      }
    });

    this.getOrderingContainer().handleBasketTherapiesRemoved(groupedMentalHealthDrugs);
  },

  _onEditMedicationRoute: function(therapy, routes, callback)
  {
    this.selectMedicationRoute(therapy, routes, callback);
  },

  _createOrderingContainerDisplayProvider: function()
  {
    var self = this;
    return new app.views.medications.TherapyDisplayProvider({ //default display provider for T2T3 form Ordering container
      view: this.getView(),
      getBigIconContainerOptions: function(dto)
      {
        var therapy = dto.therapy;
        var statusIcon = this.getStatusIcon(dto);
        var background = self._createDefaultBigIconBackground(dto.group, therapy, this);

        var options = {
          background: background,
          layers: []
        };

        options.layers.push({hpos: "right", vpos: "bottom", cls: statusIcon});
        return options;
      }
    });
  },

  _createBasketContainerDisplayProvider: function()
  {
    var self = this;
    return new app.views.medications.TherapyDisplayProvider({ //default display provider for T2T3 form Basket container
      view: this.getView(),
      getStatusIcon: function() // not showing different statuses in basket container
      {
        return null
      },
      getStatusClass: function() // not showing different statuses in basket container
      {
        return "normal"
      },
      getBigIconContainerOptions: function(dto)
      {
        var therapy = dto.therapy;
        var background = self._createDefaultBigIconBackground(dto.group, therapy, this);

        return {
          background: background,
          layers: []
        };
      }
    });
  },

  /**
   * @param {Object} group
   * @param {Object} mentalHealthDto
   * @param {app.views.medications.TherapyDisplayProvider} displayProvider
   * @returns {*}
   * @private
   */
  _createDefaultBigIconBackground: function(group, mentalHealthDto, displayProvider) // MentalHealthTemplateDto.java / MentalHealthMedicationDto.java
  {
    var background;
    if (group == app.views.medications.TherapyEnums.mentalHealthGroupEnum.TEMPLATES) // MentalHealthTemplateDto.java
    {
      background = {cls: this._getMentalHealthTemplateIcon(mentalHealthDto)};
    }
    else // MentalHealthMedicationDto.java
    {
      var therapyDto = new app.views.medications.common.dto.Therapy();
      therapyDto.setRoutes([mentalHealthDto.mentalHealthMedicationDto.route]);

      background = {cls: displayProvider.getTherapyIcon(therapyDto)};
    }
    return background;
  },

  _getMentalHealthTemplateIcon: function(template) // Mental health template
  {
    var route = template.route;
    if (!tm.jquery.Utils.isEmpty(route) && route.code == 31) // Oral
    {
      return "mental-health-list-oral-icon";
    }
    else
    {
      return "mental-health-list-all-icon";
    }
  },

  _handleMedicationSelected: function(medicationData)
  {
    var self = this;
    if (!tm.jquery.Utils.isEmpty(medicationData))
    {
      var medicationName = medicationData.medication.shortName;
      var genericName = medicationData.medication.genericName;
      var medicationId = medicationData.medication.id;

      var mentalHealthMedicationDto =
      {
        id: medicationId,
        name: medicationName
      };

      var therapy = // therapy -> MentalHealthTherapyDto.java
      {
        mentalHealthMedicationDto: mentalHealthMedicationDto,
        genericName: genericName
      };

      var routes = medicationData.routes;
      if (routes.length > 1) // open route selection container
      {
        this.selectMedicationRoute(therapy, routes, function(result)
        {
          if (!tm.jquery.Utils.isEmpty(result) && !tm.jquery.Utils.isEmpty(result.id))
          {
            therapy.mentalHealthMedicationDto.route = result;
          }

          self.fillMentalHealthDisplayValue(therapy, true);
          self._addToBasket(
              {
                group: app.views.medications.TherapyEnums.mentalHealthGroupEnum.NEW_MEDICATION,
                therapy: therapy
              }
          );
        });
      }
      else
      {
        therapy.mentalHealthMedicationDto.route = routes[0];

        self.fillMentalHealthDisplayValue(therapy, true);
        self._addToBasket(
            {
              group: app.views.medications.TherapyEnums.mentalHealthGroupEnum.NEW_MEDICATION,
              therapy: therapy
            }
        );
      }
    }
  },

  getDefaultWidth: function()
  {
    return tm.jquery.Utils.isEmpty(this.defaultWidth) ? $(window).width() - 50 : this.defaultWidth;
  },

  getDefaultHeight: function()
  {
    return tm.jquery.Utils.isEmpty(this.defaultHeight) ? $(window).height() - 10 : this.defaultHeight;
  },

  buildOrderingContainer: function()
  {
    var view = this.getView();
    var self = this;

    return new app.views.medications.mentalHealth.T2T3TherapySelectionColumn({
      view: view,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "720px"),
      mainContainer: this,
      displayProvider: this._orderTherapyDisplayProvider,
      addTherapiesToBasketFunction: function(data)
      {
        data.forEach(function(item)
        {
          self._addToBasket(item);
        });
      },
      onMedicationSelected: function(medicationData)
      {
        self._handleMedicationSelected(medicationData);
      },
      saveTherapyToTemplateFunction: function(therapy, invalidTherapy)
      {
        self._openSaveTemplateDialog([therapy], true, invalidTherapy);
      },
      getBasketTherapiesFunction: function()
      {
        return self.getBasketContainer().getListData();
      },
      fillMentalHealthDisplayValueFunction: function(therapy, displayRoute)
      {
        self.fillMentalHealthDisplayValue(therapy, displayRoute);
      },
      fillMentalHealthTemplateDisplayValueFunction: function(template)
      {
        self.fillMentalHealthTemplateDisplayValue(template);
      }
    });
  },

  buildBasketContainer: function()
  {
    var self = this;
    var view = this.getView();

    var headerTitle = this.reportType == app.views.medications.TherapyEnums.mentalHealthDocumentType.T2
        ? view.getDictionary("t2.prescription")
        : view.getDictionary("t3.prescription");

    return new app.views.medications.mentalHealth.T2T3BasketContainer({
      view: view,
      headerTitle: headerTitle,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      displayProvider: this._basketTherapyDisplayProvider,
      therapiesRemovedEvent: function(removedElementsData)
      {
        self._handleTherapiesRemovedEvent(removedElementsData);
      },
      editMedicationRouteFunction: function(therapy, routes, callback)
      {
        self._onEditMedicationRoute(therapy, routes, callback);
      },
      fillMentalHealthDisplayValueFunction: function(therapy, displayRoute)
      {
        self.fillMentalHealthDisplayValue(therapy, displayRoute);
      }
    });
  },

  saveOrder: function(content, successCallback, failureCallback)
  {
    var self = this;
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;

    view.showLoaderMask();
    var mentalHealthDrugs = [];
    var mentalHealthReports = [];
    var bnfMaximum = this.basketContainer.bnfMaximumContainer.getResult();
    var mentalHealthDocumentType = self.reportType;

    for (var i = 0; i < content.length; i++)
    {
      var group = content[i].group;
      if (group == enums.mentalHealthGroupEnum.INPATIENT_ABORTED
          || group == enums.mentalHealthGroupEnum.INPATIENT_ACTIVE
          || group == enums.mentalHealthGroupEnum.NEW_MEDICATION)
      {
        mentalHealthDrugs.push(content[i].therapy.mentalHealthMedicationDto);
      }
      else
      {
        mentalHealthReports.push(content[i].therapy);
      }
    }

    var saveMentalHealthDocumentUrl =
        view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_SAVE_MENTAL_HEALTH_DOCUMENT;

    var mentalHealthDocument = // MentalHealthDocumentDto.java
    {
      patientId: view.getPatientId(),
      mentalHealthDocumentType: mentalHealthDocumentType,
      bnfMaximum: bnfMaximum.length === 0 ? null : bnfMaximum,
      mentalHealthMedicationDtoList: mentalHealthDrugs,
      mentalHealthTemplateDtoList: mentalHealthReports
    };

    var params = {
      mentalHealthDocument: JSON.stringify(mentalHealthDocument),
      careProvider: JSON.stringify(view.getCareProvider()),
      language: view.getViewLanguage()
    };

    view.loadPostViewData(saveMentalHealthDocumentUrl, params, null,
        function()
        {
          view.refreshTherapies(true);
          view.hideLoaderMask();
          successCallback()
        },
        function()
        {
          view.hideLoaderMask();
          failureCallback();
        },
        true);
  },

  processResultData: function(resultDataCallback)
  {
    var self = this;
    var form = this.getValidationForm();
    var failResultData = new app.views.common.AppResultData({success: false});
    var successResultData = new app.views.common.AppResultData({success: true});

    form.setOnValidationSuccess(function()
    {
      var content = self.getBasketContainer().getContent();
      if (content != null && content.length > 0)
      {
        self.saveOrder(content, function()
        {
          resultDataCallback(successResultData);
        },
        function()
        {
          resultDataCallback(failResultData);
        });
      }
      else
      {
        resultDataCallback(failResultData);
      }
    });

    form.setOnValidationError(function()
    {
      resultDataCallback(failResultData);
    });

    form.submit();
  },

  selectMedicationRoute: function(therapy, routes, resultCallback)
  {
    var self = this;

    var routeCount = 0;
    var charLength = 0;
    var linesNumber = 1;

    routes.forEach(function(item)
    {
      charLength += item.name.length;
      routeCount ++;

      if (routeCount > 5 || charLength > 50)
      {
        linesNumber ++;
        charLength = 0;
        routeCount = 0;
      }

      item.lineNumber = linesNumber;
    });

    var title = self.getView().getDictionary("route.selection");
    var height = linesNumber * 50 + 40 + 30 + 20; // header height + medication name label height + margin (10 + 10)
    var width = 500;

    if (linesNumber == 1)
    {
      var buttonsLength = charLength * 9 + routeCount * 40 + 20;
      var tittleLength = title.length * 9 + 80;

      width = Math.max(buttonsLength, tittleLength);
    }

    var therapyDisplayLength = (therapy.mentalHealthMedicationDto.name.length + therapy.genericName.length)*9 + 20;
    if (therapyDisplayLength > width)
    {
      width = therapyDisplayLength;
    }

    var view = this.getView();
    var dialog = view.getAppFactory().createDefaultDialog(
        title,
        null,
        new app.views.medications.mentalHealth.RouteSelectionContainer({
          view: view,
          routes: routes,
          linesNumber: linesNumber,
          medicationFormattedDisplay: self.getTherapyFormattedDisplay(therapy, false),
          scrollable: 'vertical',
          resultCallback: function(data)
          {
            resultCallback(data);
            dialog.hide();
          }
        }),
        null,
        width, height
    );

    dialog.header.setCls("therapy-admin-header"); // new iOS look
    dialog.show();
  },

  fillMentalHealthDisplayValue: function(therapy, displayRoute) // MentalHealthTherapyDto.java
  {
    therapy.formattedTherapyDisplay = this.getTherapyFormattedDisplay(therapy, displayRoute);
  },

  getTherapyFormattedDisplay: function(therapy, displayRoute) // therapy -> MentalHealthTherapyDto.java
  {
    var genericName = therapy.genericName;
    var medicationName = therapy.mentalHealthMedicationDto.name;
    var routeDisplayValue = this.getView().getDictionary("route.short").toUpperCase();
    var route = tm.jquery.Utils.isEmpty(therapy.mentalHealthMedicationDto.route)
        ? null
        : therapy.mentalHealthMedicationDto.route.name;

    var html = "";
    html += '<span class="GenericName TextDataBold">' + genericName + ' </span>';
    html += '<span class="MedicationName TextData">' + medicationName + ' </span>';

    if (!tm.jquery.Utils.isEmpty(route) && displayRoute)
    {
      html += '<br />';
      html += '<span class="TextLabel">' + routeDisplayValue + ' </span>';
      html += '<span class="TextData">' + route + ' </span><br />';
    }
    return html;
  },

  fillMentalHealthTemplateDisplayValue: function(template) // MentalHealthTemplateDto.java
  {
    template.formattedTherapyDisplay = this.getMentalHealthTemplateFormattedDisplay(template);
  },

  getMentalHealthTemplateFormattedDisplay: function(template) // MentalHealthTemplateDto.java
  {
    var templateName = template.name;
    var routeName = tm.jquery.Utils.isEmpty(template.route) ? null : template.route.name; // route -> NamedIdentityDto inside MentalHealthTemplateDto.java
    var routeDisplayValue = this.getView().getDictionary("route.short").toUpperCase();

    var html = "";
    html += '<span class="GenericName TextDataBold">' + templateName + ' </span>';

    if (!tm.jquery.Utils.isEmpty(routeName))
    {
      html += '<br />';
      html += '<span class="TextLabel">' + routeDisplayValue + ' </span>';
      html += '<span class="TextData">' + routeName + ' </span><br />';
    }
    else
    {
      html += '<br />';
      html += '<span class="TextLabel">' + routeDisplayValue + ' </span>';
      html += '<span class="TextData">' + this.getView().getDictionary("all") + ' </span><br />';
    }
    return html;
  },

  getView: function()
  {
    return this.view;
  },

  getValidationForm: function()
  {
    return this.validationForm;
  },

  getBasketContainer: function()
  {
    return this.basketContainer;
  },

  getOrderingContainer: function()
  {
    return this.orderingContainer;
  }
});
