Class.define('app.views.medications.common.testing.RenderCoordinator', 'tm.jquery.Object', {

  attributeName: null,
  view: null,
  component: null,
  manualMode: false,

  _renderConditionTask: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    // only coordinate in test mode
    if (this.getView().isTestMode() && !this.isManualMode())
    {
      this._attachEvents();
    }
  },

  _attachEvents: function()
  {
    var self = this;
    this.getComponent().on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      self._onComponentRender();
    });
    this.getComponent().on(tm.jquery.ComponentEvent.EVENT_TYPE_DESTROY, function()
    {
      self._onComponentDestroy();
    });
  },

  _onComponentRender: function()
  {
    this._abortRenderConditionTask();

    var appFactory = this.getView().getAppFactory();
    var self = this;
    var component = self.getComponent();

    this._renderConditionTask = appFactory.createConditionTask(
        function()
        {
          self.insertCoordinator(component);
        },
        function()
        {
          return component.isRendered(true);
        },
        50, 100
    );
  },

  _onComponentDestroy: function()
  {
    this._abortRenderConditionTask();
  },

  _abortRenderConditionTask: function()
  {
    if (!tm.jquery.Utils.isEmpty(this._renderConditionTask))
    {
      this._renderConditionTask.abort();
      this._renderConditionTask = null;
    }
  },

  /**
   * @param {tm.jquery.Component} [component] Optional - should be left blank when calling from outside.
   */
  insertCoordinator: function(component)
  {
    if (this.getView().isTestMode())
    {
      component = component ? component : this.getComponent();
      var coordinator = new tm.jquery.Component();
      coordinator.addTestAttribute(this.getAttributeName());
      coordinator.doRender();
      $(component.getDom()).append(coordinator.getDom());
    }
  },

  /**
   * Manually remove the coordinator. Useful in scenarios when the content of the bound component is not controlled
   * by the framework and no render event fires.
   * @param {tm.jquery.Component} [component] Optional - should be left blank when calling from outside.
   */
  removeCoordinator: function(component)
  {
    if (this.getView().isTestMode())
    {
      component = component ? component : this.getComponent();
      $(component.getDom())
          .find(tm.jquery.Utils.formatMessage("[{0}='{1}']", tm.jquery.Component.ATTR_TM_TEST, this.getAttributeName()))
          .remove();
    }
  },

  /**
   * @returns {tm.jquery.Component}
   */
  getComponent: function()
  {
    return this.component;
  },

  /**
   * @returns {String}
   */
  getAttributeName: function()
  {
    return this.attributeName;
  },

  /**
   * If true, the attachment of the coordinator element should be done manually and no events will be bound.
   * @returns {boolean}
   */
  isManualMode: function()
  {
    return this.manualMode === true;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});