/*
 * Copyright (c) 2010-2014 Marand d.o.o. (www.marand.com)
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

package com.marand.thinkmed.api.demographics.data;

/**
 * @author Bostjan Vester
 */
public interface Address<P extends PostOffice, C extends Country, M extends Municipality, N extends NutsRegion>
{
  String getAddressee();
  void setAddressee(String addressee);
  String getAddressee2();
  void setAddressee2(String addressee2);
  String getStreet();
  void setStreet(String street);
  String getStreet2();
  void setStreet2(String street2);
  P getPostOffice();
  void setPostOffice(P postOffice);
  String getForeignPostOffice();
  void setForeignPostOffice(String foreignPostOffice);
  C getCountry();
  void setCountry(C country);
  M getMunicipality();
  void setMunicipality(M municipality);
  N getNutsRegion();
  void setNutsRegion(N nutsRegion);
}
