/*
 * Copyright (c) 2010-2017 Marand d.o.o. (www.marand.com)
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
Class.define('app.views.medications.timeline.administration.MedicationBarcodeContainer', 'tm.jquery.Container', {
  statics: {
    EVENT_TYPE_MEDICATION_BARCODE_SCANNED: new tm.jquery.event.EventType({
      name: 'medicationBarcodeScanned', delegateName: null
    })
  },
  view: null,
  therapy: null,
  barcode: null,
  medicationProducts: null,
  _medicationBarcodeField: null,
  _barcodeCorrectImg: null,
  _barcodeDifferentMassImg: null,
  _barcodeErrorImg: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this.registerEventTypes('app.views.medications.timeline.administration.MedicationBarcodeContainer', [
      {
        eventType: app.views.medications.timeline.administration.MedicationBarcodeContainer.EVENT_TYPE_MEDICATION_BARCODE_SCANNED
      }
    ]);
    this._buildGui();
  },

  _buildGui: function()
  {
    var self = this;
    var view = this.getView();
    this.setLayout(tm.jquery.HFlexboxLayout.create("center", "center"));
    this.setFlex(new tm.jquery.flexbox.item.Flex.create(1, 1, "auto"));

    this._medicationBarcodeField = new tm.jquery.TextField({
      cls: 'medication-barcode-field',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      placeholder: view.getDictionary("barcode"),
      width: 120
    });
    this._barcodeCorrectImg = new tm.jquery.Container({
      cls: "CheckLabel",
      width: 16,
      height: 16,
      alignSelf: "center",
      hidden: !this.barcode
    });
    this._barcodeDifferentMassImg = new tm.jquery.Container({
      cls: "PartialCheckLabel",
      width: 16,
      height: 16,
      alignSelf: "center",
      hidden: true
    });
    this._barcodeDifferentMassImg.setTooltip(tm.views.medications.MedicationUtils.createTooltip(
        view.getDictionary("scanned.medication.strength.differs"),
        null,
        view));
    this._barcodeErrorImg = new tm.jquery.Container({
      cls: "CrossLabel",
      width: 16,
      height: 16,
      alignSelf: "center",
      hidden: true
    });
    this._medicationBarcodeField.onKey(new tm.jquery.event.KeyStroke({key: "return"}), function()
    {
      if (self._medicationBarcodeField.getValue())
      {
        self._handleBarcodeScanned(self._medicationBarcodeField.getValue());
      }
    });
    this._medicationBarcodeField.on(tm.jquery.ComponentEvent.EVENT_TYPE_FOCUS_GAINED, function()
    {
      self.clear(false);
    });

    if (this.barcode)
    {
      this._medicationBarcodeField.setValue(this.barcode, true);
    }
    this.add(this._medicationBarcodeField);
    this.add(this._barcodeCorrectImg);
    this.add(this._barcodeDifferentMassImg);
    this.add(this._barcodeErrorImg);
  },
  /**
   * @param {String} scannedCode
   * @private
   */
  _handleBarcodeScanned: function(scannedCode)
  {
    var self = this;
    var view = this.getView();
    this.hideBarcodeStatusIcons();

    if (scannedCode === this.getTherapy().getTherapyId())
    {
      this._showBarcodeCorrectImg();
    }
    else
    {
      view.getRestApi().getOriginalTherapyId(this.getTherapy().getTherapyId()).then(function(originalTherapyId) {
        if (scannedCode === originalTherapyId)
        {
          self._showBarcodeCorrectImg();
        }
        else
        {
          view.getRestApi().getMedicationIdForBarcode(scannedCode).then(function(medicationId)
          {
            self._onMedicationIdLoaded(medicationId);
          });
        }
      })
    }
  },

  /**
   * @param {Number} medicationId
   * @private
   */
  _onMedicationIdLoaded: function(medicationId)
  {
    var view = this.getView();
    var medicationWithIdFound = false;

    if (medicationId)
    {
      if (!this.getTherapy().isOrderTypeSimple())
      {
        if (this.getTherapy().getIngredientsList() && this.getTherapy().getIngredientsList().length === 1)
        {
          if (this.getTherapy().getIngredientsList()[0].medication.getId() === medicationId)
          {
            medicationWithIdFound = true;
          }
        }
      }
      else
      {
        for (var i = 0; i < this.medicationProducts.length; i++)
        {
          if (this.medicationProducts[i].id === medicationId)
          {
            medicationWithIdFound = true;
            break;
          }
        }
      }
      medicationWithIdFound ? this._showBarcodeCorrectImg() :
          this._handleBarcodeError(view.getDictionary("scanned.medication.incorrect"));
    }
    else
    {
      this._handleBarcodeError(view.getDictionary("scanned.code.not.in.database"));
    }
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.timeline.administration.MedicationBarcodeContainer.EVENT_TYPE_MEDICATION_BARCODE_SCANNED,
      eventData: {
        medicationId: medicationId,
        medicationIdFound: medicationWithIdFound
      }
    }), null);
  },

  /**
   * @param {String} errorMessage
   * @private
   */
  _handleBarcodeError: function(errorMessage)
  {
    this._showBarcodeErrorImg();
    this.getView().getAppFactory().createWarningSystemDialog(errorMessage, 500, 160).show();
  },

  _showBarcodeCorrectImg: function()
  {
    this._barcodeCorrectImg.show();
  },

  _showBarcodeErrorImg: function()
  {
    this._barcodeErrorImg.show();
  },

  showBarcodeDifferentMassImg: function()
  {
    this._barcodeDifferentMassImg.show();
  },

  hideBarcodeStatusIcons: function()
  {
    this._barcodeErrorImg.hide();
    this._barcodeCorrectImg.hide();
    this._barcodeDifferentMassImg.hide();
  },

  requestFocusToBarcodeField: function()
  {
    this._medicationBarcodeField.focus();
  },

  /**
   * @param {Boolean} hideIcons
   */
  clear: function(hideIcons)
  {
    if (this.getView().getTherapyAuthority().isMedicationIdentifierScanningAllowed())
    {
      this._medicationBarcodeField.setValue(null, true);
      if (hideIcons !== false)
      {
        this.hideBarcodeStatusIcons();
      }
    }
  },

  /**
   * @returns {Boolean}
   */
  isScannedCodeCorrect: function()
  {
    return this._barcodeErrorImg.isHidden();
  },

  /**
   * @returns {String|null}
   */
  getBarcode: function()
  {
    return this._medicationBarcodeField.getValue();
  },

  /**
   * @returns {app.views.medications.common.dto.Therapy}
   */
  getTherapy: function()
  {
    return this.therapy;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});