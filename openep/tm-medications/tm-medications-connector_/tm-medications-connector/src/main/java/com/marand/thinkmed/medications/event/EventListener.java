package com.marand.thinkmed.medications.event;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

/**
 * @author Bostjan Vester
 */
public abstract class EventListener
{
  private final List<EventHandler> eventHandlers = new ArrayList<>();

  protected final void handle(@Nonnull final Event event)
  {
    Preconditions.checkNotNull(event);

    for (final EventHandler eventHandler : eventHandlers)
    {
      event.handleWith(eventHandler);
    }
  }
}
