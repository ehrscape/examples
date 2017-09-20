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
Class.define('tm.views.medications.pharmacists.DailyReviewsContainer', 'tm.jquery.Container', {
  statics: {
    EVENT_TYPE_THERAPY_SELECTION_CHANGE: new tm.jquery.event.EventType({
      name: 'reviewsContainerTherapySelectionChange', delegateName: null}),
    EVENT_TYPE_CONTENT_CHANGE: new tm.jquery.event.EventType({
      name: 'dailyReviewContainerContentChange', delegateName: null}),
    EVENT_TYPE_CONTENT_AUTHORIZED: new tm.jquery.event.EventType({
      name: 'dailyReviewContainerContentAuthorized', delegateName: null})
  },
  cls: "daily-reviews-container",

  /* public members */
  view: null,
  active: null,
  contentDate: null,
  content: null,

  showPlaceHolder: null,
  showSupply: null,

  /* private members */
  _headerContainer: null,
  _contentContainer: null,

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);

    this.active = this.getConfigValue("active", false);
    this.contentDate = this.getConfigValue("contentDate", CurrentTime.get());
    this.content = this.getConfigValue("content", []);
    this.showPlaceHolder = this.getConfigValue("showPlaceHolder", false);

    this.getContentDate().setHours(0, 0, 0, 0);
    /* for compare purposes make sure it's time equals 0 */

    this.registerEventTypes('tm.views.medications.pharmacists.DailyReviewsContainer', [
      { eventType: tm.views.medications.pharmacists.DailyReviewsContainer.EVENT_TYPE_THERAPY_SELECTION_CHANGE }
    ]);
    this.registerEventTypes('tm.views.medications.pharmacists.DailyReviewsContainer', [
      { eventType: tm.views.medications.pharmacists.DailyReviewsContainer.EVENT_TYPE_CONTENT_CHANGE }
    ]);
    this.registerEventTypes('tm.views.medications.pharmacists.DailyReviewsContainer', [
      { eventType: tm.views.medications.pharmacists.DailyReviewsContainer.EVENT_TYPE_CONTENT_AUTHORIZED }
    ]);

    this._toggleActiveCls();
    this._buildGui();
    this._buildReviews();
  },

  ///
  /// private methods
  ///
  _buildGui: function ()
  {
    var self = this;

    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));

    var header = new tm.views.medications.pharmacists.DailyReviewsContainerHeader({
      dailyContainer: this,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      doneButtonClickCallback: function ()
      {
        self.onDoneButtonClick();
      }
    });

    var content = new tm.jquery.Container({
      cls: "content",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    this.add(header);
    this.add(content);

    this._headerContainer = header;
    this._contentContainer = content;
  },

  _buildReviews: function ()
  {
    this.getView().getLocalLogger().debug("Called _buildReviews.");

    var contentContainer = this.getContentContainer();
    var self = this;
    var hasDrafts = false;

    this.getContent().forEach(function (reviewData)
    {
      self.addReview(reviewData, false);

      if (reviewData.isDraft())
      {
        hasDrafts = true;
      }
    });

    // don't show the placeholder container if there's drafts present - will be added if the drafts are deleted
    if (this.showPlaceHolder && !hasDrafts)
    {
      this._showConfirmAllTherapiesPlaceholderContainer(false);
    }

    this.getHeader().setDoneButtonEnabled(hasDrafts);

    if (contentContainer.isRendered())
    {
      this.getView().getLocalLogger().debug("Content container getting repainted.");
      contentContainer.repaint();
      this.getView().getLocalLogger().debug("Content container repaint finished.");
    }
  },

  _showConfirmAllTherapiesPlaceholderContainer: function (render)
  {
    var self = this;
    var contentContainer = this.getContentContainer();

    var placeholderContainer = new tm.views.medications.pharmacists.ConfirmAllTherapiesPlaceholderContainer({
      view: this.getView(),
      confirmAllTherapiesCallback: function ()
      {
        self.onConfirmAllTherapies(placeholderContainer);
      }
    });

    contentContainer.add(placeholderContainer);
    this._sortContentComponents();

    if (render == true)
    {
      // inject the HTML to the top, since refresh() on all the content might take too long to render!
      placeholderContainer.doRender();
      var $contentContainer = jQuery(contentContainer.getDom());
      $contentContainer.prepend(placeholderContainer.getDom());
    }

    return placeholderContainer;
  },

  _toggleActiveCls: function ()
  {
    var active = this.isActive();

    if (active === true && !this.getCls().contains("item-active"))
    {
      this.setCls(this.getCls() + " item-active");
    }
    else if (active === false && this.getCls().contains("item-active"))
    {
      this.setCls(this.getCls().replace(" item-active", ""));
    }
  },

  _sortContentComponents: function ()
  {
    this.getContentContainer().getComponents().sort(function (a, b)
    {
      return b.getReviewData().createTimestamp - a.getReviewData().createTimestamp;
    });
  },

  ///
  // getters, setters, public
  //
  getHeader: function ()
  {
    return this._headerContainer;
  },

  isActive: function ()
  {
    return this.active;
  },

  setActive: function (value)
  {
    this.active = value;
    this._toggleActiveCls();
    this.getHeader().refresh();
  },

  getContentDate: function ()
  {
    return this.contentDate;
  },

  getContent: function ()
  {
    return this.content;
  },

  getContentContainer: function ()
  {
    return this._contentContainer;
  },

  getView: function ()
  {
    return this.view;
  },

  getShowPlaceHolder: function ()
  {
    return this.showPlaceHolder;
  },

  setShowPlaceHolder: function (value)
  {
    this.showPlaceHolder = value;
  },

  getReviewContainerInEdit: function ()
  {
    var reviews = this.getContentContainer().getComponents();

    for (var idx = 0; idx < reviews.length; idx++)
    {
      var reviewContainer = reviews[idx];
      if (reviewContainer instanceof tm.views.medications.pharmacists.ReviewContainer && reviewContainer.isInEditMode())
      {
        return reviewContainer;
      }
    }

    return null;
  },

  getDraftReviewsTherapyIds: function ()
  {
    var therapyIds = [];

    this.getContentContainer().getComponents().forEach(function (reviewContainer)
    {
      if (reviewContainer instanceof tm.views.medications.pharmacists.ReviewContainer &&
          reviewContainer.getReviewData().isDraft())
      {
        reviewContainer.getActiveTherapies().forEach(function (therapy)
        {
          if (!tm.jquery.Utils.isEmpty(therapy.therapy) && !tm.jquery.Utils.isEmpty(therapy.therapy.compositionUid))
          {
            therapyIds.push(therapy.therapy.compositionUid);
          }
        });
      }
    });

    return therapyIds;
  },

  addReview: function (reviewData, reorder, editMode)
  {
    var self = this;
    var contentContainer = this.getContentContainer();
    var reviewDate = new Date(reviewData.createTimestamp);
    reviewDate.setHours(0, 0, 0, 0);
    var today = CurrentTime.get();
    today.setHours(0, 0, 0, 0);

    // if we're showing a placeholder to confirm all therapies but adding a new review in edit mode,
    // we should remove the placeholder for the time being
    if (this.getShowPlaceHolder() && editMode == true)
    {
      var containers = contentContainer.getComponents();
      for (var idx = 0; idx < containers.length; idx++)
      {
        if (containers[idx] instanceof tm.views.medications.pharmacists.ConfirmAllTherapiesPlaceholderContainer)
        {
          contentContainer.remove(containers[idx]);
          break;
        }
      }
    }

    var reviewContainer = new tm.views.medications.pharmacists.ReviewContainer({
      view: this.getView(),
      reviewData: reviewData,
      editable: reviewData.isDraft() && reviewDate.getTime() == today.getTime(),
      startInEditMode: editMode,
      showReminders: true,
      showSupply: this.showSupply,
      deleteReviewEventCallback: function (container)
      {
        self.onDeleteReviewEventCallback(container);
      }
    });
    reviewContainer.on(tm.views.medications.pharmacists.ReviewContainer.EVENT_TYPE_MODE_SWITCH,
        function (component, componentEvent)
        {
          self.onReviewContainerModeSwitch(component, componentEvent.getEventData().switchedToEdit);
        });
    reviewContainer.on(tm.views.medications.pharmacists.ReviewContainer.EVENT_TYPE_THERAPY_REMOVED,
        function (component, componentEvent)
        {
          self.onReviewContainerTherapyRemoved(componentEvent.getEventData().therapyId);
        });

    contentContainer.add(reviewContainer);

    /* sort to maintain the order */
    if (reorder !== false)
    {
      this._sortContentComponents();
    }

    if (editMode === true)
    {
      reviewContainer.highlight();
      this.onReviewContainerModeSwitch(reviewContainer, true);
    }

    if (contentContainer.isRendered())
    {
      // repaint is too slow when there's more reviews present, bypassing the framework for performance
      reviewContainer.doRender();
      var $contentContainer = jQuery(contentContainer.getDom());
      $contentContainer.prepend(reviewContainer.getDom());
    }
  },

  onDeleteReviewEventCallback: function (container)
  {
    var self = this;
    var contentContainer = this.getContentContainer();

    if (!tm.jquery.Utils.isEmpty(container.getReviewData().getCompositionUid()))
    {
      this.getView().showLoaderMask();
      var url = this.getView().getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_DELETE_PHARMACIST_REVIEW;

      var params = {
        patientId: this.getView().getPatientId(),
        pharmacistReviewUid: container.getReviewData().getCompositionUid()
      };

      this.getView().sendPostRequest(url, params, function ()
          {
            self.getView().hideLoaderMask();
            contentContainer.remove(container);
            self.getHeader().setDoneButtonEnabled(self.containsDraftReviews());

            if (!self.containsDraftReviews() && self.getShowPlaceHolder())
            {
              self._showConfirmAllTherapiesPlaceholderContainer(true);
            }

            self.fireEvent(new tm.jquery.ComponentEvent({
              eventType: tm.views.medications.pharmacists.DailyReviewsContainer.EVENT_TYPE_CONTENT_CHANGE
            }), null);

          }, function ()
          {
            self.getView().hideLoaderMask();
          },
          app.views.common.AppNotifierDisplayType.HTML);
    }
    else
    {
      contentContainer.remove(container);

      this.fireEvent(new tm.jquery.ComponentEvent({
        eventType: tm.views.medications.pharmacists.DailyReviewsContainer.EVENT_TYPE_THERAPY_SELECTION_CHANGE,
        eventData: {
          action: 'set',
          therapyIds: []
        }
      }), null);

      this.fireEvent(new tm.jquery.ComponentEvent({
        eventType: tm.views.medications.pharmacists.DailyReviewsContainer.EVENT_TYPE_CONTENT_CHANGE
      }), null);

      // enable editing for draft reviews
      var hasDrafts = false;
      contentContainer.getComponents().forEach(function (reviewContainer)
      {
        if (reviewContainer instanceof tm.views.medications.pharmacists.ReviewContainer)
        {
          if (reviewContainer.getReviewData().isDraft())
          {
            hasDrafts = true;
            reviewContainer.setEditable(true);
          }
        }
      });

      // if user canceled edit, check if we need to add a new placeholder container to confirm all therapies
      if (!hasDrafts && self.getShowPlaceHolder())
      {
        this._showConfirmAllTherapiesPlaceholderContainer(true);
      }

      self.getHeader().setDoneButtonEnabled(hasDrafts);
    }
  },

  onConfirmAllTherapies: function (placeholderContainer)
  {
    var self = this;
    var reviewData = new tm.views.medications.pharmacists.dto.PharmacistMedicationReview({
      createTimestamp: CurrentTime.get(),
      composer: this.getView().getCurrentUserAsCareProfessional(),
      relatedTherapies: [],
      referBackToPrescriber: false,
      noProblem: true
    });

    this.getView().showLoaderMask();
    var url = this.getView().getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_SAVE_PHARMACIST_REVIEW;

    var params = {
      patientId: this.getView().getPatientId(),
      pharmacistReview: JSON.stringify(reviewData),
      authorize: true,
      language: this.getView().getViewLanguage()
    };

    this.getView().loadPostViewData(url, params, null, function (compositionUid)
    {
      self.getView().hideLoaderMask();
      reviewData.setCompositionUid(compositionUid);
      reviewData.markAuthorized();
      self.getContentContainer().remove(placeholderContainer);
      self.addReview(reviewData, true, false);
      self.setShowPlaceHolder(false);

      self.fireEvent(new tm.jquery.ComponentEvent({
        eventType: tm.views.medications.pharmacists.DailyReviewsContainer.EVENT_TYPE_CONTENT_AUTHORIZED
      }), null);
    });
  },

  containsDraftReviews: function ()
  {
    var reviewContainers = this.getContentContainer().getComponents();
    for (var idx = 0; idx < reviewContainers.length; idx++)
    {
      if (reviewContainers[idx] instanceof tm.views.medications.pharmacists.ReviewContainer
          && reviewContainers[idx].getReviewData().isDraft())
      {
        return true;
      }

    }
    return false;
  },

  onReviewContainerModeSwitch: function (container, toEditMode)
  {
    this.getContentContainer().getComponents().forEach(function (reviewContainer)
    {
      if (reviewContainer instanceof tm.views.medications.pharmacists.ReviewContainer && reviewContainer != container)
      {
        reviewContainer.setEditable(reviewContainer.getReviewData().isDraft() && !toEditMode);
      }
    });

    var therapyIds = [];

    if (toEditMode === true)
    {
      container.getReviewData().getRelatedTherapies().forEach(function (relatedTherapy)
      {
        if (!tm.jquery.Utils.isEmpty(relatedTherapy.therapy)) therapyIds.push(relatedTherapy.therapy.compositionUid);
      });

      this.getHeader().setDoneButtonEnabled(false);
    }
    else
    {
      this.getHeader().setDoneButtonEnabled(true);
    }

    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: tm.views.medications.pharmacists.DailyReviewsContainer.EVENT_TYPE_THERAPY_SELECTION_CHANGE,
      eventData: {
        action: 'set',
        therapyIds: therapyIds
      }
    }), null);
  },

  onReviewContainerTherapyRemoved: function (therapyId)
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: tm.views.medications.pharmacists.DailyReviewsContainer.EVENT_TYPE_THERAPY_SELECTION_CHANGE,
      eventData: {
        action: 'remove',
        therapyIds: [therapyId]
      }
    }), null);
  },

  onDoneButtonClick: function ()
  {
    var self = this;
    var openReviewContainers = [];

    this.getContentContainer().getComponents().forEach(function (reviewContainer)
    {
      if (reviewContainer instanceof tm.views.medications.pharmacists.ReviewContainer
          && reviewContainer.getReviewData().isDraft()
          && !tm.jquery.Utils.isEmpty(reviewContainer.getReviewData().getCompositionUid()))
      {
        openReviewContainers.push(reviewContainer);
      }
    });

    if (openReviewContainers.length > 0)
    {
      this.getHeader().setDoneButtonEnabled(false);
      this.getView().showLoaderMask();
      var url = this.getView().getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_AUTHORIZE_PHARMACIST_REVIEWS;

      var params = {
        patientId: this.getView().getPatientId(),
        pharmacistReviewUids: JSON.stringify(openReviewContainers.map(function (container)
        {
          return container.getReviewData().getCompositionUid();
        }))
      };

      this.getView().sendPostRequest(url, params, function ()
      {
        self.getView().hideLoaderMask();
        self.setShowPlaceHolder(false);
        openReviewContainers.forEach(function (container)
        {
          container.setEditable(false);
          container.authorize();
        });

        self.fireEvent(new tm.jquery.ComponentEvent({
          eventType: tm.views.medications.pharmacists.DailyReviewsContainer.EVENT_TYPE_CONTENT_AUTHORIZED
        }), null);
      }, function ()
      {
        self.getHeader().setDoneButtonEnabled(true);
        self.getView().hideLoaderMask();
      }, app.views.common.AppNotifierDisplayType.HTML);
    }
  }
});
