package com.marand.thinkmed.medications.connector.impl.provider;

import java.util.List;

import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import org.joda.time.DateTime;

/**
 * @author Bostjan Vester
 */
public interface StaffDataProvider
{
  List<NamedExternalDto> getMedicalStaff();
  NamedExternalDto getUsersName(String userId, DateTime when);
  List<NamedExternalDto> getUserCareProviders(final String userId);
}
