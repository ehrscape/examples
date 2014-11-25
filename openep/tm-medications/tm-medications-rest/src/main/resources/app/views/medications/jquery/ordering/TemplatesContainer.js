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

Class.define('app.views.medications.ordering.TemplatesContainer', 'tm.jquery.Container', {
  cls: "templates-container",
  scrollable: 'vertical',

  /** configs */
  view: null,
  medicationSelectedEvent: null,
  addTherapiesToBasketFunction: null,
  editTherapyFunction: null,
  /** privates*/
  displayProvider: null,
  templates: null,
  /** privates: components */
  searchField: null,
  patientTemplatesPanel: null,
  userTemplatesPanel: null,
  orgTemplatesPanel: null,

  /** constructor */
  Constructor: function(config)
  {
    var self = this;
    this.callSuper(config);
    var appFactory = this.view.getAppFactory();
    this.setLayout(appFactory.createDefaultVFlexboxLayout("start", "stretch"));
    this.displayProvider = new app.views.medications.TherapyDisplayProvider({view: config.view});
    this._buildComponents();
    this._buildGui();
    this._loadTemplates();

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      setTimeout(function()
      {
        self.searchField.focus();
      }, 0);
    });
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    //this.searchField = tm.views.medications.MedicationUtils.createMedicationsSearchField(this.view, 646, 'basic', null, true);
    //this.searchField.setMargin('10 0 0 10');
    //this.searchField.setSource(this.view.getMedications());
    //this.searchField.on(tm.jquery.ComponentEvent.EVENT_TYPE_SELECT, function(component)
    //{
    //  var selection = component.getSelection();
    //  if (selection)
    //  {
    //    self._readMedicationData(selection.id)
    //  }
    //});

    this.searchField = new app.views.medications.MedicationSearchField({
      view: this.view,
      width: 720,
      padding: 10,
      medicationSelectedFunction: function(medicationId)
      {
        if (self.templates && self.templates.patientTemplates && self.templates.patientTemplates.length > 0)
        {
          var patientTemplates = self.templates.patientTemplates;
          var templatesContainMedication = tm.views.medications.MedicationUtils.assertTemplatesContainMedication(medicationId, patientTemplates);
          if (!templatesContainMedication)
          {
            var message = self.view.getDictionary('medication.not.in.patient.templates');
            self.view.getAppFactory().createConfirmSystemDialog(message, function(params)
            {
              if (params == true)
              {
                self._readMedicationData(medicationId);
              }
              else
              {
                self.searchField.clear();
              }
            }, 320, 160).show();
          }
          else
          {
            self._readMedicationData(medicationId);
          }
        }
        else
        {
          self._readMedicationData(medicationId);
        }
      }});

    this.patientTemplatesPanel = new tm.jquery.Panel({collapsed: true, showHeader: true, showFooter: false});
    var patientTemplatesHeader = this.patientTemplatesPanel.getHeader();
    patientTemplatesHeader.setCls('template-group-panel-header text-unselectable');
    patientTemplatesHeader.setLayout(appFactory.createDefaultHFlexboxLayout("start", "stretch"));
    var patientTemplatesTitlePane = new tm.jquery.Container({
      padding: "7 0 0 0",
      layout: appFactory.createDefaultHFlexboxLayout('start', 'center'), flex: 1,
      html: '<span class="TextHeading3">' + this.view.getDictionary('patient.templates') + '</span>'
    });
    patientTemplatesHeader.add(patientTemplatesTitlePane);
    this.patientTemplatesPanel.bindToggleEvent([patientTemplatesTitlePane]);
    var patientTemplatesContent = this.patientTemplatesPanel.getContent();
    patientTemplatesContent.setLayout(appFactory.createDefaultVFlexboxLayout("start", "stretch"));
    patientTemplatesContent.setScrollable('visible');

    this.userTemplatesPanel = new tm.jquery.Panel({collapsed: true, showHeader: true, showFooter: false});
    var userTemplatesHeader = this.userTemplatesPanel.getHeader();
    userTemplatesHeader.setCls('template-group-panel-header text-unselectable');
    userTemplatesHeader.setLayout(appFactory.createDefaultHFlexboxLayout("start", "stretch"));
    var userTemplatesTitlePane = new tm.jquery.Container({
      padding: "7 0 0 0",
      layout: appFactory.createDefaultHFlexboxLayout('start', 'center'), flex: 1,
      html: '<span class="TextHeading3">'+this.view.getDictionary('my.templates')+'</span>'
    });
    //userTemplatesTitlePane.add(new tm.jquery.Container({cls: 'TextHeading3', html:  this.view.getDictionary('my.templates'), flex: 1}));
    userTemplatesHeader.add(userTemplatesTitlePane);
    this.userTemplatesPanel.bindToggleEvent([userTemplatesTitlePane]);
    var userTemplatesContent = this.userTemplatesPanel.getContent();
    userTemplatesContent.setLayout(appFactory.createDefaultVFlexboxLayout("start", "stretch"));
    userTemplatesContent.setScrollable('visible');

    this.orgTemplatesPanel = new tm.jquery.Panel({collapsed: true, showHeader: true, showFooter: false});
    var orgTemplatesHeader = this.orgTemplatesPanel.getHeader();
    orgTemplatesHeader.setCls('template-group-panel-header text-unselectable');
    orgTemplatesHeader.setLayout(appFactory.createDefaultHFlexboxLayout("start", "stretch"));
    var orgTemplatesTitle = this.view.getDictionary('organizational.templates.long');
    if (this.view.getOrganizationalEntityName())
    {
      orgTemplatesTitle += ' (' + this.view.getOrganizationalEntityName() + ')';
    }
    var orgTemplatesTitlePane = new tm.jquery.Container({
      padding: "7 0 0 0",
      layout: appFactory.createDefaultHFlexboxLayout('start', 'center'), flex: 1,
      html: '<span class="TextHeading3">'+orgTemplatesTitle+'</span>'
    });
    //orgTemplatesTitlePane.add(new tm.jquery.Container({cls: 'TextHeading3', html: orgTemplatesTitle, flex: 1}));
    orgTemplatesHeader.add(orgTemplatesTitlePane);
    this.orgTemplatesPanel.bindToggleEvent([orgTemplatesTitlePane]);
    var orgTemplatesContent = this.orgTemplatesPanel.getContent();
    orgTemplatesContent.setLayout(appFactory.createDefaultVFlexboxLayout("start", "stretch"));
    orgTemplatesContent.setScrollable('visible');
  },

  _buildGui: function()
  {
    var appFactory = this.view.getAppFactory();
    var searchFieldContainer =
        new tm.jquery.Container({height: 40, layout: appFactory.createDefaultHFlexboxLayout('start', 'center')});
    searchFieldContainer.add(this.searchField);

    this.add(searchFieldContainer);
    this.add(new tm.jquery.Spacer({type: 'vertical', size: 5}));
    this.add(this.orgTemplatesPanel);
    this.add(this.userTemplatesPanel);
    this.add(this.patientTemplatesPanel);
  },

  _buildTemplateRow: function(template, lastRow)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var templateRow = new tm.jquery.Panel({collapsed: true, showHeader: true, showFooter: false, padding: '0 0 0 15'});

    var header = templateRow.getHeader();
    header.setCls('template-panel-header text-unselectable');
    header.setLayout(appFactory.createDefaultHFlexboxLayout('start', 'stretch'));
    var headerContainer = new tm.jquery.Container({
      cls: lastRow ? '' : 'template-bottom-border',
      layout: appFactory.createDefaultHFlexboxLayout('start', 'center'),
      flex: 1,
      padding: "7 0 0 0",
      html: '<span class="TextDataBold">'+template.name+'</span>'
    });
    header.add(headerContainer);
    headerContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_DOUBLE_CLICK, function()
    {
      self._addTherapiesToBasket(template.templateElements);
    });

    var popupMenu = this._createTemplateActionsMenu(template, header, false);
    var menuButton = this._buildAddToBasketButton(template, template.templateElements, header, popupMenu);
    var menuButtonHeaderContainer = new tm.jquery.Container({
      scrollable: 'visible', layout: appFactory.createDefaultVFlexboxLayout('start', 'center')});
    menuButtonHeaderContainer.add(menuButton);

    header.add(menuButtonHeaderContainer);

    var contextMenu = this._createTemplateActionsMenu(template, header, true);
    headerContainer.setContextMenu(contextMenu);

    var content = templateRow.getContent();
    content.setScrollable('visible');
    content.setLayout(appFactory.createDefaultVFlexboxLayout('start', 'stretch'));
    content.add(new tm.jquery.List({
      padding: '0 0 0 20',
      scrollable: 'visible',
      dataSource: template.templateElements,
      itemTpl: function(index, templateElement)
      {
        return self._buildTemplateElementRow(template, templateElement);
      },
      selectable: false
    }));
    return templateRow;
  },

  _buildTemplateElementRow: function(template, templateElement)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var row = new tm.jquery.Container({flex: 1, scrollable: 'visible', layout: appFactory.createDefaultHFlexboxLayout('start', 'start')});
    var therapyContainer = new tm.jquery.Container({
      flex: 1,
      html: templateElement.therapy.formattedTherapyDisplay,
      cls: 'TherapyDescription'
    });
    row.add(new tm.jquery.Container({
      margin: '2 0 0 0',
      height: 38,
      width: 20,
      cls: templateElement.completed == false ? 'incomplete-therapy-icon' : ""
    }));

    row.add(therapyContainer);

    var popupMenu = this._createTemplateElementActionsMenu(template, templateElement, row, false);
    var menuButton = this._buildAddToBasketButton(template, [templateElement], row, popupMenu);
    row.add(menuButton);

    var contextMenu = this._createTemplateElementActionsMenu(template, templateElement, row, true);
    row.setContextMenu(contextMenu);

    row.on(tm.jquery.ComponentEvent.EVENT_TYPE_MOUSE_OVER, function()
    {
      menuButton.show();
    });
    row.on(tm.jquery.ComponentEvent.EVENT_TYPE_MOUSE_LEAVE, function()
    {
      menuButton.hide();
    });
    row.on(tm.jquery.ComponentEvent.EVENT_TYPE_DOUBLE_CLICK, function()
    {
      self._addTherapiesToBasket([templateElement]);
    });
    return row;
  },

  _buildAddToBasketButton: function(template, templateElements, parentComponent, popupMenu)
  {
    var self = this;
    var actionsButton = new tm.jquery.SplitButton({
      iconCls: "icon-add-to",
      hidden: true,
      margin: '3 10 0 0',
      popupMenu: popupMenu,
      handler: function()
      {
        self._addTherapiesToBasket(templateElements);
      }
    });
    parentComponent.on(tm.jquery.ComponentEvent.EVENT_TYPE_MOUSE_OVER, function()
    {
      actionsButton.show();
    });
    parentComponent.on(tm.jquery.ComponentEvent.EVENT_TYPE_MOUSE_LEAVE, function()
    {
      actionsButton.hide();
      actionsButton.closePopupMenu();
    });
    return actionsButton;
  },

  _createTemplateActionsMenu: function(template, row, createContextMenu)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var menu;
    if (createContextMenu)
    {
      menu = appFactory.createContextMenu();
      menu.setRenderToElement(this.getDom());
    }
    else
    {
      menu = appFactory.createPopupMenu();
      menu.setAlignment('right');
    }
    var menuItemAdd = new tm.jquery.MenuItem({
          text: this.view.getDictionary("do.order"),
          iconCls: 'icon-add-to',
          handler: function()
          {
            self._addTherapiesToBasket(template.templateElements);
          }}
    );
    menu.addMenuItem(menuItemAdd);
    var menuItemDelete = new tm.jquery.MenuItem({
          text: this.view.getDictionary('delete.template'),
          iconCls: 'icon-delete',
          handler: function()
          {
            self._deleteTemplate(template.id);
          }}
    );
    menu.addMenuItem(menuItemDelete);
    menu.on(tm.jquery.ComponentEvent.EVENT_TYPE_SHOW, function()
    {
      menu.popUpIsOpen = true;
      row.setCls('template-panel-header-selected text-unselectable');
    });
    menu.on(tm.jquery.ComponentEvent.EVENT_TYPE_HIDE, function()
    {
      if (menu.popUpIsOpen === true)
      {
        menu.popUpIsOpen = false;
        row.setCls('template-panel-header text-unselectable');
      }
    });
    return menu;
  },

  _createTemplateElementActionsMenu: function(template, templateElement, row, createContextMenu)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var menu;
    if (createContextMenu)
    {
      menu = appFactory.createContextMenu();
      menu.setRenderToElement(this.getDom());
    }
    else
    {
      menu = appFactory.createPopupMenu();
      menu.setAlignment('right');
    }
    menu.setAlignment('right');
    var menuItemAdd = new tm.jquery.MenuItem({
          text: this.view.getDictionary("do.order"),
          iconCls: 'icon-add-to',
          handler: function()
          {
            self._addTherapiesToBasket([templateElement]);
          }}
    );
    menu.addMenuItem(menuItemAdd);
    var menuItemEditAndAdd = new tm.jquery.MenuItem({
          text: this.view.getDictionary('edit.and.add'),
          iconCls: 'icon-edit',
          handler: function()
          {
            self.editTherapyFunction(templateElement.therapy);
          }}
    );
    menu.addMenuItem(menuItemEditAndAdd);
    if (template.templateElements.length > 1)
    {
      var menuItemRemove = new tm.jquery.MenuItem({
            text: this.view.getDictionary('remove.from.template'),
            iconCls: 'icon-delete',
            handler: function()
            {
              self._removeElementFromTemplate(template, templateElement, row);
            }}
      );
      menu.addMenuItem(menuItemRemove);
    }
    menu.on(tm.jquery.ComponentEvent.EVENT_TYPE_SHOW, function()
    {
      menu.popUpIsOpen = true;
      row.setCls('selected-therapy');
    });
    menu.on(tm.jquery.ComponentEvent.EVENT_TYPE_HIDE, function()
    {
      if (menu.popUpIsOpen === true)
      {
        menu.popUpIsOpen = false;
        row.setCls(null);
      }
    });
    return menu;
  },

  _loadTemplates: function()
  {
    var self = this;
    var medicationDataUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_TEMPLATES;
    var params = {
      organizationalEntityId: this.view.getOrganizationalEntityId(),
      userId: this.view.getUserId(),
      patientId: this.view.getPatientId(),
      referenceWeight: this.view.getReferenceWeight(),
      patientHeight: this.view.getPatientData().heightInCm
    };
    this.view.loadViewData(medicationDataUrl, params, null, function(templates)
    {
      self.templates = templates;
      self._displayTemplates(templates);
    });
  },
  _displayTemplates: function(templates)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var patientTemplatesContent = this.patientTemplatesPanel.getContent();
    patientTemplatesContent.removeAll(true);
    var userTemplatesContent = this.userTemplatesPanel.getContent();
    userTemplatesContent.removeAll(true);
    var orgTemplatesContent = this.orgTemplatesPanel.getContent();
    orgTemplatesContent.removeAll(true);

    for (var h = 0; h < templates.patientTemplates.length; h++)
    {
      var lastPatientRow = h == templates.patientTemplates.length - 1;
      var patientTemplate = templates.patientTemplates[h];
      var patientTemplateRow = this._buildTemplateRow(patientTemplate, lastPatientRow);
      patientTemplatesContent.add(patientTemplateRow);
    }

    for (var i = 0; i < templates.userTemplates.length; i++)
    {
      var lastUserRow = i == templates.userTemplates.length - 1;
      var userTemplate = templates.userTemplates[i];
      var userTemplateRow = this._buildTemplateRow(userTemplate, lastUserRow);
      userTemplatesContent.add(userTemplateRow);
    }

    for (var j = 0; j < templates.organizationTemplates.length; j++)
    {
      var lastOrgRow = j == templates.organizationTemplates.length - 1;
      var orgTemplate = templates.organizationTemplates[j];
      var orgTemplateRow = this._buildTemplateRow(orgTemplate, lastOrgRow);
      orgTemplatesContent.add(orgTemplateRow);
    }

    patientTemplatesContent.repaint();
    userTemplatesContent.repaint();
    orgTemplatesContent.repaint();

    var hasPatientTemplates = templates.patientTemplates != null && templates.patientTemplates.length > 0;
    if (hasPatientTemplates)
    {
      appFactory.createConditionTask(
          function()
          {
            self.patientTemplatesPanel.expand();
          },
          function()
          {
            return self.patientTemplatesPanel.isRendered();
          },
          500, 100
      );
    }
    else
    {
      appFactory.createConditionTask(
          function()
          {
            self.orgTemplatesPanel.expand();
          },
          function()
          {
            return self.orgTemplatesPanel.isRendered();
          },
          500, 100
      );
    }
  },

  _deleteTemplate: function(templateId)
  {
    var self = this;
    var deleteUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_DELETE_TEMPLATE;
    var params = {templateId: templateId};

    this.view.loadPostViewData(deleteUrl, params, null,
        function()
        {
          self._loadTemplates();
        });
  },

  _removeElementFromTemplate: function(template, templateElement, row)
  {
    template.templateElements.remove(templateElement);
    var saveUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_SAVE_TEMPLATE;
    var params = {template: JSON.stringify(template)};

    this.view.loadPostViewData(saveUrl, params, null,
        function(newTherapyId)
        {
          template.id = newTherapyId;
          row.hide();
        });
  },

  _addTherapiesToBasket: function(templateElements)
  {
    var therapies = [];
    for (var i = 0; i < templateElements.length; i++)
    {
      var therapy = templateElements[i].therapy;
      therapy.completed = templateElements[i].completed;
      therapies.push(therapy);
    }
    this.addTherapiesToBasketFunction(therapies);
  },

  _readMedicationData: function(medicationId)
  {
    var self = this;
    var medicationDataUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_MEDICATION_DATA;
    var params = {medicationId: medicationId};
    this.view.loadViewData(medicationDataUrl, params, null, function(data)
    {
      self.medicationSelectedEvent(data);
    });
  },

  /** public methods */
  clear: function()
  {
    this.searchField.clear();
    this.searchField.focus();
  },

  reloadTemplates: function()
  {
    this._loadTemplates();
  },

  getTemplates: function()
  {
    return this.templates;
  }
});

