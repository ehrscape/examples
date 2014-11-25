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

Class.define('app.views.medications.MedicationSearchField', 'tm.jquery.Container', {
  /** configs */
  view: null,
  medicationSelectedFunction: null,
  enabled: true,
  preselectedMedication: null, //optional
  /** privates */
  treeTimer: null,
  medicationField: null,

  /** constructor */
  Constructor: function(config)
  {
    var self = this;
    this.callSuper(config);
    var appFactory = this.view.getAppFactory();

    this.setLayout(appFactory.createDefaultHFlexboxLayout("start", "stretch"));

    this.medicationField = new tm.jquery.TextField({
      flex: 1,
      enabled: this.enabled,
      value: this.preselectedMedication ? this.preselectedMedication.name : null
    });
    this.add(this.medicationField);
    var treeDialog = new tm.jquery.Container({
      flex: 1,
      height: 30,
      padding: 0,
      cls: "medications-tree-dialog"
    });
    var tree = new tm.jquery.Container({
      flex: 1,
      height: 30,
      cls: "medications-tree",
      scrollable: "vertical"
    });
    treeDialog.add(tree);
    this.add(treeDialog);

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      self.initSelector(
          tree.id,
          treeDialog.id,
          self.medicationField.id,
          self.id);
    });
  },

  /**
   * @author robertk
   * @author Sandi Krese
   *    refactored, tree parametrized and more reusable
   * @author Mitja Lapajne
   */

  initSelector: function(treeIdParam, dialogIdParam, fieldIdParam, containerParam)
  {
    var self = this;
    var ignoredKeys = [9, 16, 17, 18, 19, 20, 33, 34, 35, 36, 37, 38, 39, 45, 144, 145];

    var treeId = $('#' + containerParam + ' #' + treeIdParam);
    var dialogId = $('#' + containerParam + ' #' + dialogIdParam);
    var fieldId = '#' + containerParam + ' #' + fieldIdParam;
    var container = '#' + containerParam;
    var valueFieldId = fieldId;
    var codeField = $(fieldId + '\\|code');

    treeId.dynatree({
      clickFolderMode: 1,
      onActivate: function(node)
      {
        if (!node.data.unselectable)
        {
          dialogId.dialog('close');
          self.medicationField.setValue(node.data.medication.name);
          self.medicationSelectedFunction(node.data.medication.id)
        }
      }
    });

    dialogId.dialog({
      width: 672,
      height: 400,
      closeOnEscape: false,
      position: {
        my: 'left top',
        at: 'left bottom',
        of: valueFieldId
      },
      appendTo: container + ' .modal-body',
      autoOpen: false,
      resizable: false,
      open: function()
      {
        $(document).off('click', resolveCloseDialog);
        $(document).on('click', resolveCloseDialog);
      },
      close: function()
      {
        $(document).off('click', resolveCloseDialog);
      }
    }).keyup(function(e)
    {
      if (e.keyCode === $.ui.keyCode.ESCAPE)
      {
        if ($(dialogId).dialog("isOpen"))
        {
          e.stopPropagation();
          self.medicationField.setValue(null);
          self.medicationField.focus();
          $(dialogId).dialog("close");
          return false;
        }
      }
    });

    var dialog = $('.ui-dialog .medications-tree-dialog').parent();
    dialog.css('z-index', '9999999');
    dialog.find('.ui-dialog-titlebar').hide();

    $(valueFieldId).keyup(function(e)
    {
      var text = $(this).val();
      if (text == null || text.length < 4)
      {
        var rootNode = $(treeId).dynatree('getRoot');
        rootNode.removeChildren();
        $(codeField).val('');
        $(dialogId).dialog("close");
        return true;
      }

      if (ignoredKeys.indexOf(e.keyCode) != -1)
      {
        return true;
      }

      if (e.keyCode == 13)
      { //ENTER
        var node = treeId.dynatree('getRoot');
        if (node.data.unselectable)
        {
          e.stopPropagation();
        }
        else if (dialogId.dialog('isOpen') && !node.data.unselectable)
        {
          if (node.isSelected())
          {
            node.activate();
          }
          return false;
        }
      }
      if (e.keyCode == 27)
      { //ESC
        //handle ESC key only if popup is open
        if (dialogId.dialog('isOpen'))
        {
          e.stopPropagation();
          dialogId.dialog("close");
          self.medicationField.setValue(null);
          self.medicationField.focus();
          return false;
        }
      }
      if (e.keyCode == 40)
      { //Down Arrow
        if (dialogId.dialog('isOpen'))
        {
          var node = treeId.dynatree('getRoot');
          while (node.hasChildren() && node.isExpanded())
          {
            node = node.getChildren()[0];
          }
          node.focus();
          return false;
        }
      }
      self.textChanged($(this), treeId, dialogId);
    });

    var resolveCloseDialog = function(e)
    {
      var posX = e.clientX;
      var posY = e.clientY;
      var dialogOffs = $(dialogId).offset();
      var dialogWidth = $(dialogId).outerWidth();
      var dialogHeight = $(dialogId).outerHeight();

      if ($(dialogId).length > 0)
      {
        if ($(dialogId).dialog('isOpen'))
        {
          if (posX < dialogOffs.left || posX > (dialogOffs.left + dialogWidth) || posY < dialogOffs.top || posY > (dialogOffs.top + dialogHeight))
          {
            $(dialogId).dialog('close');
          }
        }
      }
    };
  },

  rebuildTree: function(element, treeId, dialogId)
  {
    var searchString = $(element).val();
    this._rebuildTree(searchString, element, treeId, dialogId);
    return false;
  },

  _rebuildTree: function(searchString, element, treeId, dialogId)
  {
    var params =
    {
      searchString: searchString
    };
    var url =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_FIND_MEDICATIONS;

    this.view.loadViewData(url, params, null,
        function(data)
        {
          if (searchString == $(element).val())
          {
            var rootNode = treeId.dynatree('getRoot');
            rootNode.removeChildren();
            rootNode.addChild(data);
            dialogId.dialog('open');
            if (!$(element).is('button'))
            {
              $(element).focus();
            }
          }
        });
  },

  textChanged: function(text, treeId, dialogId)
  {
    var self = this;
    clearTimeout(this.treeTimer);
    this.treeTimer = setTimeout(function()
    {
      if ($(text).val().length > 2)
      {
        self.rebuildTree(text, treeId, dialogId);
      }
      return false;
    }, 500)
  },

  focus: function()
  {
    this.medicationField.focus();
  },

  clear: function()
  {
    this.medicationField.setValue(null);
  },

  getTextField: function()
  {
    return this.medicationField;
  },

  setMedication: function(medication)
  {
    this.medicationField.setValue(medication ? medication.name : null);
  }
});

