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
Class.define('tm.views.medications.pharmacists.ResizingTextArea', 'tm.jquery.Field', {
  componentCls: 'tm-component tm-field tm-textfield tm-resizingtextarea',

  /** members: configs */
  value: null,
  maxLength: null,
  placeholder: "",
  editable: true,
  minHeight: null,

  _pasteMode: null,
  _pasteTimer: null,
  _delayTimer: null,

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
  },

  /**
   * @Override
   */
  createDom: function ()
  {
    var self = this;

    var div = document.createElement(this.getComponentElementTagName());

    if (tm.jquery.Utils.isEmpty(this.minHeight) == false) div.style.minHeight = this.minHeight + "px";

    var textarea = document.createElement("textarea");
    textarea.setAttribute("class", (this.isEditable() ?
        "tm-component tm-field tm-textarea" : "tm-component tm-field tm-textarea uneditable-input"));
    textarea.setAttribute("placeholder", this.getPlaceholder());
    textarea.setAttribute("spellcheck", false);
    textarea.innerHTML = this.getDisplayValue(this.value);

    if (tm.jquery.Utils.isEmpty(this.maxLength) == false) textarea.setAttribute("maxlength", this.maxLength);
    if (this.isEnabled() === false) textarea.setAttribute("disabled", "true");
    if (this.isEditable() === false) textarea.setAttribute("readonly", "true");

    var pre = document.createElement("pre");
    pre.setAttribute("class", "tm-component");
    pre.innerHTML = this.getSizingValue(this.value);

    div.appendChild(textarea);
    div.appendChild(pre);
    div.componentInstance = this;

    // required events
    $(textarea).on('paste', function ()
    {
      self._pasteMode = true;
      clearTimeout(self._pasteTimer);
      self._pasteTimer = setTimeout(function ()
      {
        self._pasteMode = false;
      }, 50);
    });

    $(textarea).on('input propertychange', function ()
    {
      if (self._pasteMode == true)
      {
        $(self.getDom()).children('pre').text(self.getSizingValue($(self.getDom()).children('textarea').val()));
      }
      else
      {
        clearTimeout(self._delayTimer);
        self._delayTimer = setTimeout(function ()
        {
          $(self.getDom()).children('pre').text(self.getSizingValue($(self.getDom()).children('textarea').val()));
        }, 100);
      }
    });

    return div;
  },

  /**
   * @Override applying attributes to the input element instead of
   */
  doRender: function()
  {
    this.callSuper();
    this.applyTabIndex();
  },

  getSizingValue: function(value)
  {
    // if there's an actual value, return an extra line to counter the effect of the delay timer, otherwise append only one newline
    return this.getDisplayValue(value) + (tm.jquery.Utils.isEmpty(value) ? '\n' : '\n\n');
  },

  /**
   * Getters & Setters
   */
  getEventDelegateElementProviderFn: function (eventType)
  {
    var self = this;
    return function ()
    {
      return self.dom == null ? null : self.getInputElement();
    };
  },

  getInputElement: function ()
  {
    return $(this.getDom()).children("textarea")[0];
  },

  getValue: function ()
  {
    this.value = $(this.getInputElement()).val();
    return this.value;
  },

  getMinHeight: function ()
  {
    return this.minHeight;
  },

  setMinHeight: function (value)
  {
    if (this.isRendered())
    {
      this.applyMinHeight(value);
    }
    this.minHeight = value;
  },

  /**
   * @Apply methods
   */
  applyValue: function (value, preventEvent)
  {
    $(this.getInputElement()).val(this.getDisplayValue(value));
    $(this.getDom()).children('pre').text(this.getSizingValue(value));
    if (preventEvent !== true)$(this.getInputElement()).trigger("change");

    if (this.clearable === true)
    {
      $(this.getInputElement()).addClass("clearable" + (tm.jquery.Utils.isEmpty(value, false) ? "" : " x"));
    }
  },

  applyPlaceholder: function (placeholder)
  {
    $(this.getInputElement()).attr("placeholder", placeholder);
  },

  applyEditable: function (editable)
  {
    $(this.getInputElement()).removeClass("uneditable-input");
    if (editable === false) $(this.getInputElement()).addClass("uneditable-input");
  },

  applyEnabled: function (enabled)
  {
    $(this.getInputElement()).removeAttr("disabled");
    if (enabled === false) $(this.getInputElement()).attr("disabled", true);
  },

  applyTabIndex: function (tabIndex)
  {
    $(this.getDom()).removeAttr("tabIndex");
    $(this.getInputElement()).removeAttr("tabIndex");
    if (tabIndex !== null) $(this.getInputElement()).attr("tabIndex", tabIndex);
  },

  applyMinHeight: function (value)
  {
    $(this.getDom()).css({'min-height': tm.jquery.Utils.isEmpty(value) ? "" : (value + "px")});
  },

  applyMaxLength: function(maxLength)
  {
    $(this.getInputElement()).attr("maxlength", maxLength);
  },


  focus: function()
  {
    $(this.getInputElement()).focus();
  },
});