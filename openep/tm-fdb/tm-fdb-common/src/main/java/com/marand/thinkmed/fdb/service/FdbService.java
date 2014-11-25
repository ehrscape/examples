package com.marand.thinkmed.fdb.service;

import com.marand.thinkmed.fdb.dto.TherapyInfo;

/**
 * @author Mitja Lapajne
 */
public interface FdbService
{
  void scanForWarnings(TherapyInfo therapyInfo);
}
