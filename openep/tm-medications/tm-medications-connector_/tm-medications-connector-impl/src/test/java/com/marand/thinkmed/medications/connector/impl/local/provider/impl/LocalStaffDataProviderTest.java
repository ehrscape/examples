package com.marand.thinkmed.medications.connector.impl.local.provider.impl;

import java.util.List;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.connector.impl.provider.StaffDataProvider;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Bostjan Vester
 */
@RunWith(SpringProxiedJUnit4ClassRunner.class)
@ContextConfiguration({"/com/marand/thinkmed/medications/connector/impl/local/provider/impl/LocalStaffDataProviderTest-context.xml"})
@DbUnitConfiguration(databaseConnection = {"dbUnitDatabaseConnection"})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionDbUnitTestExecutionListener.class})
@DatabaseSetup("LocalStaffDataProviderTest.xml")
@Transactional
public class LocalStaffDataProviderTest
{
  @Autowired
  private StaffDataProvider staffDataProvider;

  @Test
  public void getMedicalStaff()
  {
    final List<NamedExternalDto> medicalStaff = staffDataProvider.getMedicalStaff();
    Assert.assertNotNull(medicalStaff);
    Assert.assertEquals(3, medicalStaff.size());
  }

  @Test
  public void getExistingValidUsersName()
  {
    final NamedExternalDto usersName = staffDataProvider.getUsersName("1", new DateTime());
    Assert.assertNotNull(usersName);
    Assert.assertEquals("1", usersName.getId());
    Assert.assertEquals("User1", usersName.getName());
  }

  @Test
  public void getExistingNonvalidUsersName()
  {
    final NamedExternalDto usersName = staffDataProvider.getUsersName("999", new DateTime());
    Assert.assertNull(usersName);
  }

  @Test
  public void getNonExistingUsersName()
  {
    final NamedExternalDto usersName = staffDataProvider.getUsersName("1000", new DateTime());
    Assert.assertNull(usersName);
  }

  @Test
  public void getExistingUserCareProviders()
  {
    final List<NamedExternalDto> userCareProviders = staffDataProvider.getUserCareProviders("1");
    Assert.assertNotNull(userCareProviders);
    Assert.assertEquals(3, userCareProviders.size());
  }

  @Test
  public void getNonExistingUserCareProviders()
  {
    final List<NamedExternalDto> userCareProviders = staffDataProvider.getUserCareProviders("3");
    Assert.assertNotNull(userCareProviders);
    Assert.assertTrue(userCareProviders.isEmpty());
  }
}
