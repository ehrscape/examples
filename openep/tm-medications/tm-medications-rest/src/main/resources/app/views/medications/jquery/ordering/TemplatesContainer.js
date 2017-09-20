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
  scrollable: "visible",
  /** configs */
  view: null,
  medicationSelectedEvent: null,
  addTherapiesToBasketFunction: null,
  editTherapyFunction: null,
  templateMode: null,
  /** privates*/
  templates: null,
  /** privates: components */
  patientTemplatesPanel: null,
  userTemplatesPanel: null,
  orgTemplatesPanel: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    this._buildComponents();
    this._buildGui();
    this._loadTemplates();
  },

  /** private methods */
  _buildComponents: function()
  {
    var view = this.getView();

    this.patientTemplatesPanel = new tm.jquery.Panel({
      cls: "patient-templates-pane",
      collapsed: true,
      showHeader: true,
      showFooter: false,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    var patientTemplatesHeader = this.patientTemplatesPanel.getHeader();
    patientTemplatesHeader.setCls('template-group-panel-header');
    patientTemplatesHeader.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center", 0));
    var patientTemplatesTitlePane = new tm.jquery.Container({
      cursor: "pointer",
      cls: "patient-templates-title-pane-header TextDataBold text-unselectable ellipsis",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      html: view.getDictionary('patient.templates')
    });
    patientTemplatesHeader.add(patientTemplatesTitlePane);
    this.patientTemplatesPanel.bindToggleEvent([patientTemplatesTitlePane]);
    var patientTemplatesContent = this.patientTemplatesPanel.getContent();
    patientTemplatesContent.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    patientTemplatesContent.setScrollable('visible');

    this.userTemplatesPanel = new tm.jquery.Panel({
      cls: "user-templates-pane",
      collapsed: true,
      showHeader: true,
      showFooter: false,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    var userTemplatesHeader = this.userTemplatesPanel.getHeader();
    userTemplatesHeader.setCls('template-group-panel-header');
    userTemplatesHeader.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center", 0));
    var userTemplatesTitlePane = new tm.jquery.Container({
      cls: "user-templates-title-pane TextDataBold text-unselectable ellipsis",
      cursor: "pointer",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      html: view.getDictionary('my.templates')
    });
    userTemplatesHeader.add(userTemplatesTitlePane);
    this.userTemplatesPanel.bindToggleEvent([userTemplatesTitlePane]);
    var userTemplatesContent = this.userTemplatesPanel.getContent();
    userTemplatesContent.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    userTemplatesContent.setScrollable('visible');

    this.orgTemplatesPanel = new tm.jquery.Panel({
      cls: "org-templates-pane",
      collapsed: true,
      showHeader: true,
      showFooter: false,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    var orgTemplatesHeader = this.orgTemplatesPanel.getHeader();
    orgTemplatesHeader.setCls('template-group-panel-header');
    orgTemplatesHeader.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center", 0));
    var orgTemplatesTitle = this.view.getDictionary('organizational.templates.long');
    if (view.getCareProviderName())
    {
      orgTemplatesTitle += ' (' + this.view.getCareProviderName() + ')';
    }
    var orgTemplatesTitlePane = new tm.jquery.Container({
      cls: "org-templates-title-pane TextDataBold text-unselectable ellipsis",
      cursor: "pointer",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      html: orgTemplatesTitle
    });
    orgTemplatesHeader.add(orgTemplatesTitlePane);
    this.orgTemplatesPanel.bindToggleEvent([orgTemplatesTitlePane]);
    var orgTemplatesContent = this.orgTemplatesPanel.getContent();
    orgTemplatesContent.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    orgTemplatesContent.setScrollable('visible');
  },

  _buildGui: function()
  {
    this.add(this.orgTemplatesPanel);
    this.add(this.userTemplatesPanel);
    this.add(this.patientTemplatesPanel);
  },

  _buildTemplateRow: function(template, changeActionAllowed)
  {
    var self = this;
    var view = this.getView();

    var templatePanel = new app.views.medications.common.TemplateTherapyGroupPanel({
      groupTitle: template.name,
      view: view,
      contentData: template.templateElements,
      changeActionAllowed: changeActionAllowed,
      attachElementToolbar: function(elementContainer){
        self._attachGroupPanelElementToolbar(elementContainer, template, changeActionAllowed);
      },
      addToBasketEventCallback: function(content){
        self._addTherapiesToBasket(content);
      },
      deleteTemplateEventCallback: function(){
        self._deleteTemplate(template);
      },
      collapsed: true
    });
    return templatePanel;
  },

  _attachGroupPanelElementToolbar: function(elementContainer, template, changeActionAllowed){
    var self = this;
    var toolbar = new app.views.medications.common.TemplateTherapyContainerToolbar({
      therapyContainer: elementContainer,
      changeActionAllowed: changeActionAllowed,
      addToBasketEventCallback: function(therapyContainer){
        self._addTherapiesToBasket([therapyContainer.getData()]);
      },
      addToBasketWithEditEventCallback: function(therapyContainer){
        therapyContainer.getData().therapy.rescheduleTherapyTimings(false);
        self.editTherapyFunction(therapyContainer);
      },
      removeFromTemplateEventCallback: function(therapyContainer){
        self._removeElementFromTemplate(template, therapyContainer.getData(), function(){
          therapyContainer.getParent().remove(therapyContainer);
        });
      }
    });
    elementContainer.setToolbar(toolbar);
  },

  _loadTemplates: function()
  {
    var self = this;

    this.getView().getRestApi().loadOrderingTemplates(this.templateMode, true).then(
        function onLoad(templates)
        {
          self.templates = templates;
          self._displayTemplates(templates);
        }
    );
  },

  _displayTemplates: function(templates)
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();
    var userTemplatesContent = this.userTemplatesPanel.getContent();
    userTemplatesContent.removeAll(true);
    var orgTemplatesContent = this.orgTemplatesPanel.getContent();
    orgTemplatesContent.removeAll(true);
    var patientTemplatesContent = this.patientTemplatesPanel.getContent();
    patientTemplatesContent.removeAll(true);

    for (var i = 0; i < templates.userTemplates.length; i++)
    {
      var userTemplate = templates.userTemplates[i];
      var userTemplateRow = this._buildTemplateRow(userTemplate);
      userTemplatesContent.add(userTemplateRow);
    }

    for (var j = 0; j < templates.organizationTemplates.length; j++)
    {
      var orgTemplate = templates.organizationTemplates[j];
      var orgTemplateRow =
          this._buildTemplateRow(orgTemplate, view.getTherapyAuthority().isManageOrganizationalTemplatesAllowed());
      orgTemplatesContent.add(orgTemplateRow);
    }

    for (var h = 0; h < templates.patientTemplates.length; h++)
    {
      var patientTemplate = templates.patientTemplates[h];
      var patientTemplateRow =
          this._buildTemplateRow(patientTemplate, view.getTherapyAuthority().isManagePatientTemplatesAllowed());
      patientTemplatesContent.add(patientTemplateRow);
    }

    userTemplatesContent.repaint();
    orgTemplatesContent.repaint();
    patientTemplatesContent.repaint();

    appFactory.createConditionTask(
        function()
        {
          self.orgTemplatesPanel.expand();
        },
        function()
        {
          return self.orgTemplatesPanel.isRendered(true);
        },
        500, 100
    );
  },

  _deleteTemplate: function(template)
  {
    var self = this;
    var view = this.getView();
    var deleteUrl = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_DELETE_TEMPLATE;
    var params = {templateId: template.id};

    var message = view.getDictionary("template.delete.confirmation.message");
    message = tm.jquery.Utils.isEmpty(message) ? "" : message.replace("{0}", template.name);

    var confirmDialog = view.getAppFactory().createConfirmSystemDialog(message,
        function(confirmed)
        {
          if (confirmed == true)
          {
            view.loadPostViewData(deleteUrl, params, null,
                function()
                {
                  self._loadTemplates();
                });
          }
        }
    );
    confirmDialog.setWidth(450);
    confirmDialog.setHeight(142);
    confirmDialog.show();
  },

  _removeElementFromTemplate: function(template, templateElement, callback)
  {
    var view = this.getView();

    template.templateElements.remove(templateElement);
    var saveUrl = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_SAVE_TEMPLATE;
    var params = {
      template: JSON.stringify(template),
      templateMode: this.templateMode
    };

    view.loadPostViewData(saveUrl, params, null,
        function(newTherapyId)
        {
          template.id = newTherapyId;
          if (callback) callback();
        });
  },

  _addTherapiesToBasket: function(templateElements)
  {
    var therapies = [];
    for (var i = 0; i < templateElements.length; i++)
    {
      var therapy = jQuery.extend(true, {}, templateElements[i].therapy);  // has to be deep copy or else references to child DTOs will be kept!
      therapy.completed = templateElements[i].completed;
      therapies.push(therapy);
    }
    this.addTherapiesToBasketFunction(therapies);
  },

  reloadTemplates: function()
  {
    this._loadTemplates();
  },

  getTemplates: function()
  {
    return this.templates;
  },

  /**
   *
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});

