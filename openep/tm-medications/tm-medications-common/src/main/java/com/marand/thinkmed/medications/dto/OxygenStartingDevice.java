package com.marand.thinkmed.medications.dto;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static com.marand.openehr.medications.tdo.AdministrationDetailsCluster.OxygenDeliveryCluster;

/**
 * @author Nejc Korasa
 */
public class OxygenStartingDevice extends DataTransferObject implements JsonSerializable
{
  private OxygenDeliveryCluster.Route route;
  private String routeType;

  public OxygenStartingDevice(final OxygenDeliveryCluster.Route route)
  {
    this.route = route;
  }

  public OxygenDeliveryCluster.Route getRoute()
  {
    return route;
  }

  public void setRoute(final OxygenDeliveryCluster.Route route)
  {
    this.route = route;
  }

  public String getRouteType()
  {
    return routeType;
  }

  public void setRouteType(final String routeType)
  {
    this.routeType = routeType;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("route", route).append("routeType", routeType);
  }
}
