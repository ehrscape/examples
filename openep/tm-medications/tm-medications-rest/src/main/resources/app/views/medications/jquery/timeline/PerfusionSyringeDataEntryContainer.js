Class.define('app.views.medications.common.PerfusionSyringeDataEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: 'perfusion-syringe-container',
  scrollable: "vertical",

  /* public members */
  defaultHeight: 260,
  defaultWidth: 270,
  view: null,
  startProcessOnEnter: true,

  task: null, /* task DTO to preload */
  warningText: null, /* bottom warning text to display, if any */

  _startDateTimePicker: null,
  _urgencyCheckBox: null,
  _syringeCountField: null,
  _form: null,
  _printSystemLabelCheckBox: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGUI();
    this._configureForm();
  },

  _buildGUI: function()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));

    var view = this.getView();
    var taskDto = this.getTask();
    var warningText = this.getWarningText();

    var startComponent = new tm.views.medications.common.VerticallyTitledComponent({
      cls: "start-component",
      titleText: view.getDictionary('start'),
      contentComponent: new tm.jquery.DateTimePicker({
        cls: "larger-date-time-field",
        showType: "focus",
        date: tm.jquery.Utils.isEmpty(taskDto) ?  CurrentTime.get() : new Date(taskDto.dueTime)
      }),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    var urgencyCheckbox = new tm.jquery.CheckBox({
      cls: "urgency-checkbox",
      labelText: view.getDictionary("urgent"),
      labelCls: "TextData",
      checked: tm.jquery.Utils.isEmpty(taskDto) ? false : taskDto.urgent,
      labelAlign: "right",
      enabled: true,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    var rowNumberRowWrapper = new tm.jquery.Container({
      cls: "row-number-row-wrapper",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-end", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    var syringeCountLabel = new tm.jquery.Component({
      html: view.getDictionary("number.of.syringes"),
      cls: "TextData lowercase"
    });

    var syringeCountInput = new tm.jquery.NumberField({
      cls: "syringe-count-field",
      maxLength: 3,
      formatting: {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: 0},
      value: tm.jquery.Utils.isEmpty(taskDto) ? 1 : parseInt(taskDto.numberOfSyringes),
      width: 50
    });
    rowNumberRowWrapper.add(syringeCountLabel);
    rowNumberRowWrapper.add(syringeCountInput);

    this._printSystemLabelCheckBox = new tm.jquery.CheckBox({
      cls: "print-system-checkbox",
      labelText: view.getDictionary("print.system"),
      labelCls: "TextData",
      labelAlign: "right",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    this.add(startComponent);
    this.add(urgencyCheckbox);
    this.add(rowNumberRowWrapper);
    this.add(this._printSystemLabelCheckBox);

    if (!tm.jquery.Utils.isEmpty(warningText))
    {
      this.add(new tm.jquery.Component({
        html: warningText,
        cls: "TextData warning-text margin-top-5"
      }));
    }

    this._startDateTimePicker = startComponent.getContentComponent();
    this._urgencyCheckBox = urgencyCheckbox;
    this._syringeCountField = syringeCountInput;
  },

  _configureForm: function()
  {
    var self = this;

    var form = new tm.jquery.Form({
      view: this.getView(),
      showTooltips: false,
      requiredFieldValidatorErrorMessage: this.getView().getDictionary("field.value.is.required")
    });

    // reason input //
    form.addFormField(new tm.jquery.FormField({
      label: null, component: this.getStartDateTimePicker(), required: true,
      validation: {
        type: "local"
      },
      componentValueImplementationFn: function (component)
      {
        return component.getDate() ? component.getDate() : null;
      }
    }));
    form.addFormField(new tm.jquery.FormField({
      component: this.getSyringeCountField(),
      required: true,
      componentValueImplementationFn: function()
      {
        var value = self.getSyringeCountField().getValue();

        if (!value || value <= 0)
        {
          return null;
        }

        return value;
      }
    }));

    this._form = form;
  },

  getView: function()
  {
    return this.view;
  },

  getForm: function()
  {
    return this._form;
  },

  getUrgencyCheckBox: function()
  {
    return this._urgencyCheckBox;
  },

  getStartDateTimePicker: function()
  {
    return this._startDateTimePicker;
  },

  getSyringeCountField: function()
  {
    return this._syringeCountField;
  },

  getPrintSystemLabelCheckBox: function()
  {
    return this._printSystemLabelCheckBox;
  },

  getDefaultHeight: function()
  {
    return this.defaultHeight;
  },

  getDefaultWidth: function()
  {
    return this.defaultWidth;
  },

  getTask: function()
  {
    return this.task;
  },

  getWarningText: function()
  {
    return this.warningText;
  },

  processResultData: function(resultDataCallback)
  {
    var self = this;
    var form = this.getForm();
    var failResultData = new app.views.common.AppResultData({success: false, value: null});

    form.setOnValidationSuccess(function(form)
    {
      var successResultData = new app.views.common.AppResultData({
        success: true,
        value: {
          count: self.getSyringeCountField().getValue(),
          orderDate: self.getStartDateTimePicker().getDate(),
          urgent: self.getUrgencyCheckBox().isChecked(),
          printSystemLabel: self.getPrintSystemLabelCheckBox().isChecked()
        }
      });
      resultDataCallback(successResultData);
    });
    form.setOnValidationError(function(form, validationResults)
    {
      resultDataCallback(failResultData);
    });

    form.submit();
  }
});