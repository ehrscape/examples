Class.define('app.views.medications.therapy.ShowExternalPrescriptionsDataEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: 'show-external-prescriptions-container',

  /* public members */
  defaultHeight: 150,
  defaultWidth: 240,
  view: null,
  startProcessOnEnter: true,

  _startDateField: null,
  _endDateField: null,
  _validationForm: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGUI();
    this._configureForm();
  },

  _buildGUI: function()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create("center", "center", 0));

    var startDate = CurrentTime.get();
    startDate.setDate(startDate.getDate() - 7);

    var dateFromField = new tm.jquery.DatePicker({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "100px"),
      viewMode: "overlay",
      showType: "focus",
      date: startDate
    });

    var spacerElement = new tm.jquery.Component({
      cls: 'TextData field-divider',
      html: '-'
    });

    var dateToField = new tm.jquery.DatePicker({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "100px"),
      viewMode: "overlay",
      showType: "focus",
      date: CurrentTime.get()
    });

    this.add(dateFromField);
    this.add(spacerElement);
    this.add(dateToField);

    this._startDateField = dateFromField;
    this._endDateField = dateToField;
  },

  _configureForm: function()
  {
    var self = this;
    var view = this.getView();

    var form = new tm.jquery.Form({
      view: view,
      showTooltips: false,
      requiredFieldValidatorErrorMessage: view.getDictionary("field.value.is.required")
    });

    form.addFormField(new tm.jquery.FormField({
      label: null, component: this.getStartDateField(), required: true,
      validation: {
        type: "local",
        validators: [
          new tm.jquery.Validator({
            errorMessage: view.getDictionary('starting.date.should.not.come.before.end.date'),
            isValid: function(value)
            {
              var endDate = self.getEndDateField().getDate();
              return value <= endDate;
            }
          })
        ]
      },
      componentValueImplementationFn: function(component)
      {
        return component.getDate() ? component.getDate() : null;
      }
    }));
    form.addFormField(new tm.jquery.FormField({
      label: null, component: this.getEndDateField(), required: true,
      validation: {
        type: "local"
      },
      componentValueImplementationFn: function(component)
      {
        return component.getDate() ? component.getDate() : null;
      }
    }));

    this._validationForm = form;
  },

  processResultData: function(resultDataCallback)
  {
    var self = this;
    var form = this._validationForm;
    var failResultData = new app.views.common.AppResultData({success: false, value: null});

    form.setOnValidationSuccess(function()
    {
      var successResultData = new app.views.common.AppResultData({
        success: true,
        value: {
          startDate: self.getStartDateField().getDate(),
          endDate: self.getEndDateField().getDate()
        }
      });
      resultDataCallback(successResultData);
    });
    form.setOnValidationError(function(form, validationResults)
    {
      resultDataCallback(failResultData);
    });

    form.submit();
  },

  /**
   * @returns {number}
   */
  getDefaultHeight: function()
  {
    return this.defaultHeight;
  },

  /**
   * @returns {number}
   */
  getDefaultWidth: function()
  {
    return this.defaultWidth;
  },

  /**
   * @returns {tm.jquery.DatePicker}
   */
  getStartDateField: function()
  {
    return this._startDateField;
  },

  /**
   * @returns {tm.jquery.DatePicker}
   */
  getEndDateField: function()
  {
    return this._endDateField;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});