package com.marand.thinkmed.medications.infusion.impl;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.marand.maf.core.eventbus.Event;
import com.marand.maf.core.eventbus.EventDispatcher;
import com.marand.maf.core.service.RequestContextHolder;
import com.marand.thinkmed.medications.business.util.TherapyIdUtils;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.infusion.InfusionBagHandler;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.CreateAdministration;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.RescheduleTask;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import static com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.ConfirmAdministration;
import static com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.DeleteAdministration;
import static com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.ModifyTherapy;
import static com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.ReissueTherapy;
import static com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.RescheduleTasks;

/**
 * @author Nejc Korasa
 */
public class InfusionBagRecalculateEventHandler implements InitializingBean
{
  private EventDispatcher eventDispatcher;
  private InfusionBagHandler infusionBagHandler;

  @Required
  public void setEventDispatcher(final EventDispatcher eventDispatcher)
  {
    this.eventDispatcher = eventDispatcher;
  }

  @Required
  public void setInfusionBagHandler(final InfusionBagHandler infusionBagHandler)
  {
    this.infusionBagHandler = infusionBagHandler;
  }

  @Override
  public void afterPropertiesSet() throws Exception
  {
    eventDispatcher.register(this);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final ModifyTherapy event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      final TherapyDto therapyDto = (TherapyDto)event.getCallArguments()[1];

      handleRecalculateInfusionBag(
          patientId,
          TherapyIdUtils.createTherapyId(therapyDto.getCompositionUid(), therapyDto.getEhrOrderName()),
          null,
          null);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final ReissueTherapy event)
  {
    //noinspection Duplicates
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      final String compositionUid = (String)event.getCallArguments()[1];
      final String ehrOrderName = (String)event.getCallArguments()[2];

      final String therapyId = TherapyIdUtils.createTherapyId(compositionUid, ehrOrderName);
      handleRecalculateInfusionBag(patientId, therapyId, null, null);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final CreateAdministration event)
  {
    //noinspection Duplicates
    if (event.getState() == Event.State.COMPLETED)
    {
      final String compositionUid = (String)event.getCallArguments()[0];
      final String ehrOrderName = (String)event.getCallArguments()[1];
      final String patientId = (String)event.getCallArguments()[2];

      final String therapyId = TherapyIdUtils.createTherapyId(compositionUid, ehrOrderName);
      handleRecalculateInfusionBag(patientId, therapyId, null, null);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final DeleteAdministration event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      final String administrationId = ((AdministrationDto)event.getCallArguments()[1]).getAdministrationId();
      final String therapyId = (String)event.getCallArguments()[3];

      handleRecalculateInfusionBag(patientId, therapyId, null, administrationId);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final ConfirmAdministration event)
  {
    //noinspection Duplicates
    if (event.getState() == Event.State.COMPLETED)
    {
      final String compositionUid = (String)event.getCallArguments()[0];
      final String ehrOrderName = (String)event.getCallArguments()[1];
      final String patientId = (String)event.getCallArguments()[2];
      final AdministrationDto administrationDto = (AdministrationDto)event.getCallArguments()[3];

      final String therapyId = TherapyIdUtils.createTherapyId(compositionUid, ehrOrderName);
      handleRecalculateInfusionBag(patientId, therapyId, administrationDto, null);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final RescheduleTasks event)
  {
    //noinspection Duplicates
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      final String therapyId = (String)event.getCallArguments()[3];

      handleRecalculateInfusionBag(patientId, therapyId, null, null);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final RescheduleTask event)
  {
    //noinspection Duplicates
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      final String therapyId = (String)event.getCallArguments()[3];

      handleRecalculateInfusionBag(patientId, therapyId, null, null);
    }
  }

  private void handleRecalculateInfusionBag(
      final String patientId,
      final String therapyId,
      final AdministrationDto administrationDto,
      final String administrationId)
  {
    infusionBagHandler.recalculateInfusionBagChange(
        patientId,
        therapyId,
        administrationDto,
        administrationId,
        RequestContextHolder.getContext().getRequestTimestamp());
  }
}
