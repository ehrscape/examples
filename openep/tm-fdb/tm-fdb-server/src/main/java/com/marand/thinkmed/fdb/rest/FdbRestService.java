package com.marand.thinkmed.fdb.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Mitja Lapajne
 */
public interface FdbRestService
{
  @POST
  @Path("json")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  String scanForWarnings(String input);
}
