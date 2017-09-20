(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.common.document')
      .controller('tm.angular.medications.documentation.common.document.EerPrescriptionDocumentSectionController',
          EerPrescriptionDocumentSectionController);

  function EerPrescriptionDocumentSectionController()
  {
    var vm = this;
    vm.hasAdditionalInformation = hasAdditionalInformation;
    vm.isReadOnly = isReadOnly;
    vm.showPopupMenu = showPopupMenu;
    vm.getSectionTitle = getSectionTitle;
    vm.getSectionTherapies = getSectionTherapies;
    
    /**
     * Returns true if any of the following data is set: acute, do not switch, exceed max dosage.
     * @param {PrescriptionTherapy} prescriptionTherapy
     * @returns {*}
     */
    function hasAdditionalInformation(prescriptionTherapy)
    {
      var therapy = prescriptionTherapy.getTherapy();
      var localDetails = therapy.getPrescriptionLocalDetails();
      return localDetails.getIllnessConditionType() || localDetails.isMaxDoseExceeded() || localDetails.isDoNotSwitch();
    }

    /**
     * Returns true if the content is meant to be read only. No editable actions should be shown in that case. Based
     * on the read-only attribute.
     * @returns {boolean}
     */
    function isReadOnly()
    {
      return vm._readOnly === true;
    }

    /**
     * Returns true if any viable popup menu options exist, otherwise false.
     * @param {PrescriptionTherapy} prescriptionTherapy
     * @returns {boolean}
     */
    function showPopupMenu(prescriptionTherapy)
    {
      return !isReadOnly() && (prescriptionTherapy.isCancelable() || prescriptionTherapy.isRemovable());
    }

    /**
     * @returns {string}
     */
    function getSectionTitle()
    {
      return vm._sectionTitle;
    }

    /**
     * @returns {Array.<PrescriptionTherapy>}
     */
    function getSectionTherapies()
    {
      return vm._sectionTherapies ? vm._sectionTherapies : [];  
    }
  }
})();