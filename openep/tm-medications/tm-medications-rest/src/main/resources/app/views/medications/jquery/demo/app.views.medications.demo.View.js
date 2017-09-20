Class.define('app.views.medications.demo.View', 'app.views.common.AppView', {
  cls: "v-demo-portal-therapy-view",

  _therapyViewContainer: null,

  Constructor: function()
  {
    this.callSuper();

    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));

    this._buildGUI();

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component, componentEvent, elementEvent)
    {
      component._createTherapyView();
    });
  },

  _buildGUI: function()
  {
    var headerContainer = this._createHeaderContainer();
    this._therapyViewContainer = new tm.jquery.Container({flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")});

    this.add(headerContainer);
    this.add(this._therapyViewContainer);
  },

  _createHeaderContainer: function()
  {
    var appFactory = this.getAppFactory();

    var tooltip = appFactory.createDefaultPopoverTooltip("Allergies", null, '<div class="allergies-tooltip"></div>', 215, 85);
    tooltip.setPlacement("bottom");
    tooltip.setTrigger("click");
    tooltip.setDefaultAutoPlacements(["bottom", "leftBottom"]);

    var leftHeaderContainer = new tm.jquery.Container({html: '<div class="header-left" style="width:567px;height:64px;max-width:567px;"></div>'});
    var rightHeaderContainer = new tm.jquery.Container({
      margin: "0 10 0 0", padding: "0 0 0 25",
      html: '<div class="header-right" style="width:191px;height:64px;max-width:191px;cursor:pointer;"></div>'
    });
    rightHeaderContainer.setTooltip(tooltip);

    var container = new tm.jquery.Container({
      height: 64,
      style: "border-bottom: 1px solid #ccc;",
      layout: tm.jquery.HFlexboxLayout.create("space-between", "center")
    });

    container.add(leftHeaderContainer);
    container.add(rightHeaderContainer);

    return container;
  },
  _createTherapyView: function()
  {
    var viewRenderElementId = this._therapyViewContainer.getId();

    var viewActionListener = function(action)
    {
    };

    // configs & settings //
    var viewConfig = {
      header: {
        url: "",
        context: "rest",
        controller: "medications"
      },
      view: "demoTherapyView",
      language: "en",
      theme: "fresh"
    };

    var viewSettings = {
      viewConfig: viewConfig,
      viewDefaults: this.getViewDefaults(),
      viewRenderElementId: viewRenderElementId,
      viewActionListener: viewActionListener,
      viewSecurity: this.getViewSecurity()
    };

    // create view //
    ViewManager.getInstance().createView(viewSettings, function(view)
    {
      console.log("Therapy view init data: ", view.getViewInitData());
    });
  },
  _getQueryVariable: function(variable)
  {
    var query = window.location.search.substring(1);
    var vars = query.split('&');
    for (var i = 0; i < vars.length; i++)
    {
      var pair = vars[i].split('=');
      if (decodeURIComponent(pair[0]) == variable)
      {
        return decodeURIComponent(pair[1]);
      }
    }
    console.log('Query variable %s not found', variable);
    return null;
  }
});