Class.define('app.views.medications.common.RangeField', 'tm.jquery.TextField', {
  componentName: 'rangefield',
  componentCls: 'tm-component tm-field tm-textfield tm-numberfield tm-rangefield',

  formatting: null,

  Constructor: function()
  {
    this.callSuper();

    this.formatting = this.getConfigValue("formatting", {useGrouping: false, maximumFractionDigits: 3, round: "floor"});
  },

  /**
   * @param {*} value
   * @returns {boolean}
   * @private
   */
  _isNumber: function(value)
  {
    return tm.jquery.Utils.isNumeric(value) && !tm.jquery.Utils.isString(value);
  },

  /**
   * @param {String} value
   * @returns {app.views.medications.common.dto.Range|null}
   * @private
   */
  _parseDoseRange: function(value)
  {
    if (this._isStringDoseRange(value))
    {
      var splitedRange = value.split('-');

      if (splitedRange.length === 2)
      {
        return app.views.medications.common.dto.Range.createStrict(
            tm.jquery.NumberField.parseNumber(splitedRange[0], this.getFormatting()),
            tm.jquery.NumberField.parseNumber(splitedRange[1], this.getFormatting())
        );
      }
    }

    return null;
  },

  _parseValue: function(value)
  {
    if (!value)
    {
      return value;
    }

    if (this._isStringDoseRange(value))
    {
      return this._parseDoseRange(value);
    }

    return tm.jquery.NumberField.parseNumber(value, this.getFormatting());
  },

  /**
   * @param {String} value
   * @returns {boolean}
   * @private
   */
  _isStringDoseRange: function(value)
  {
    if (tm.jquery.Utils.isEmpty(value) || !tm.jquery.Utils.isString(value) || tm.jquery.Utils.isNumeric(value))
    {
      return false;
    }

    if (value.indexOf('-') === -1) return false;

    return true;
  },

  /**
   * @param {*} value
   * @returns {boolean}
   * @private
   */
  _isRangeOrNumber: function(value)
  {
    return value instanceof app.views.medications.common.dto.Range || this._isNumber(value);
  },

  /**
   * Based on tm.jquery.NumberField.
   * @Override
   * @info component events
   */
  attachComponentEvents: function()
  {
    var self = this;
    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component, componentEvent, elementEvent)
    {
      var eventData = componentEvent.getEventData();
      var value = tm.jquery.Utils.isEmpty(eventData) ? null : eventData.value;

      if (tm.jquery.Utils.isEmpty(eventData))
      {
        var textValue = $(component.getInputElement()).val();
        value = self._parseValue(textValue);
      }
      else
      {
        // via setValue() //
      }

      if (self._isRangeOrNumber(value))
      {
        $(component.getInputElement()).val(self.getDisplayValue(value));
      }

      component.value = self._isRangeOrNumber(value) ? value : null;
    }, true);
  },

  applyValue: function(value, preventEvent)
  {
    if (value && !this._isRangeOrNumber(value))
    {
      value = this._parseValue(value);
    }

    if (value instanceof app.views.medications.common.dto.Range)
    {
      if (value.equals(this.getValue()))
      {
        return; // prevent triggering the change event inside the applyValue of tm.jquery.Field
      }
    }

    this.callSuper(value, preventEvent);
  },

  /**
   * @Override
   */
  getDisplayValue: function(value)
  {
    if (!tm.jquery.Utils.isEmpty(value))
    {
      if (this._isNumber(value))
      {
        return tm.views.medications.MedicationUtils.safeFormatNumber(value, this.getFormatting());
      }
      else if (value instanceof app.views.medications.common.dto.Range)
      {
        return (value.getMin() ? Globalize.formatNumber(value.getMin(), this.formatting) : "") +
            ' - ' +
            (value.getMax() ? Globalize.formatNumber(value.getMax(), this.formatting) : "");
      }
    }

    return "";
  },

  getValue: function()
  {
    return this.value;
  },

  /**
   * @returns {{useGrouping: boolean, maximumFractionDigits: number, round: string}|null}
   */
  getFormatting: function()
  {
    return this.formatting;
  },

  /**
   * @param {{useGrouping: boolean, maximumFractionDigits: number, round: string}|null} formatting
   */
  setFormatting: function(formatting)
  {
    this.formatting = formatting;
    this.applyValue(this.getValue());
  }
});
