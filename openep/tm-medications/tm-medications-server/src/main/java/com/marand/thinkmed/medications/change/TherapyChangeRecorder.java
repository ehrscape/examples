package com.marand.thinkmed.medications.change;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.marand.maf.core.eventbus.Event;
import com.marand.maf.core.eventbus.EventDispatcher;
import com.marand.maf.core.server.entity.dao.EntityDao;
import com.marand.maf.core.service.RequestContextHolder;
import com.marand.thinkmed.medications.model.TherapyChanged;
import com.marand.thinkmed.medications.model.impl.TherapyChangedImpl;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Mitja Lapajne
 */
public class TherapyChangeRecorder implements InitializingBean
{
  private EventDispatcher eventDispatcher;
  private EntityDao entityDao;

  @Required
  public void setEventDispatcher(final EventDispatcher eventDispatcher)
  {
    this.eventDispatcher = eventDispatcher;
  }

  @Required
  public void setEntityDao(final EntityDao entityDao)
  {
    this.entityDao = entityDao;
  }

  @Override
  public void afterPropertiesSet() throws Exception
  {
    eventDispatcher.register(this);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final MedicationsServiceEvents.PrescribeTherapy event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      handleChange(patientId);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final MedicationsServiceEvents.ModifyTherapy event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      handleChange(patientId);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final MedicationsServiceEvents.SuspendTherapy event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      handleChange(patientId);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final MedicationsServiceEvents.AbortTherapy event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      handleChange(patientId);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final MedicationsServiceEvents.ReissueTherapy event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      handleChange(patientId);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final MedicationsServiceEvents.ConfirmAdministration event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[2];
      handleChange(patientId);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final MedicationsServiceEvents.DeleteAdministration event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      handleChange(patientId);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final MedicationsServiceEvents.ReviewPharmacistReview event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      handleChange(patientId);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final MedicationsServiceEvents.SavePharmacistReview event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      handleChange(patientId);
    }
  }

  private void handleChange(final String patientId)
  {
    final TherapyChanged therapyChanged = new TherapyChangedImpl();
    therapyChanged.setChangeTime(RequestContextHolder.getContext().getRequestTimestamp());
    therapyChanged.setPatientId(patientId);
    entityDao.saveEntity(therapyChanged);
  }
}
