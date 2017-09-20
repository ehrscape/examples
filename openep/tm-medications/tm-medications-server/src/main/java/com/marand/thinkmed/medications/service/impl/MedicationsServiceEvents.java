package com.marand.thinkmed.medications.service.impl;

import com.marand.maf.core.eventbus.Event;

/**
 * @author Bostjan Vester
 */
public interface MedicationsServiceEvents
{
  class PrescribeTherapy extends Event
  {
  }

  class ModifyTherapy extends Event
  {
  }

  class SuspendTherapy extends Event
  {
  }

  class AbortTherapy extends Event
  {
  }

  class ReissueTherapy extends Event
  {
  }

  class ConfirmAdministration extends Event
  {
  }

  class CreateAdministration extends Event
  {
  }

  class DeleteAdministration extends Event
  {
  }

  class RescheduleTasks extends Event
  {
  }

  class RescheduleTask extends Event
  {
  }

  class ReviewPharmacistReview extends Event
  {
  }

  class SavePharmacistReview extends Event
  {
  }
}
