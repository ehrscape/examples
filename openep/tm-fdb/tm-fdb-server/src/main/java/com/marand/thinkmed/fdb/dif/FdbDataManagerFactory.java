package com.marand.thinkmed.fdb.dif;

import java.util.Properties;

import firstdatabank.database.FDBDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bostjan Vester
 */
public final class FdbDataManagerFactory
{
  private static final Logger LOG = LoggerFactory.getLogger(FdbDataManagerFactory.class);

  private static final String DRIVER_KEY="fdb.driver";
  private static final String URL_KEY="fdb.url";
  private static final String USER_KEY="fdb.user";
  private static final String PASSWORD_KEY="fdb.password";
  private static final String SHOW_DEBUG_INFO_KEY="fdb.showDebugInfo";
  private static final String POOL_SIZE_KEY="fdb.poolSize";
  private static final String LOAD_LIMIT_KEY="fdb.loadLimit";

  private static FDBDataManager manager = null;

  static
  {
    final Properties prop = new Properties();
    try
    {
      prop.load(FdbDataManagerFactory.class.getResourceAsStream("/fdb.properties"));
      final String driver = prop.getProperty(DRIVER_KEY);
      final String url = prop.getProperty(URL_KEY);
      final String username = prop.getProperty(USER_KEY);
      final String password = prop.getProperty(PASSWORD_KEY);
      final boolean debug = "true".equals(prop.getProperty(SHOW_DEBUG_INFO_KEY, "false"));
      final int poolSize = intFromString(prop.getProperty(POOL_SIZE_KEY), 30);
      final int loadLimit = intFromString(prop.getProperty(LOAD_LIMIT_KEY), 200);
      manager = new FDBDataManager(
          driver,
          url,
          username,
          password,
          debug,
          true,
          poolSize,
          loadLimit,
          null
      );
    }
    catch (Throwable t) // manager == null
    {
      LOG.warn("Unable to load FDB properties! FDB will not be available!", t);
    }
  }

  private static int intFromString(final String value, final int defaultValue)
  {
    try
    {
      return new Integer(value);
    }
    catch (NumberFormatException ex)
    {
      LOG.warn("Unable to convert [" + value + "] to int! Using default [" + defaultValue + "] instead!", ex);
      return defaultValue;
    }
  }

  private FdbDataManagerFactory()
  {
  }

  public static FDBDataManager getManager()
  {
    return manager;
  }

  /*
  public static void main(String[] args)
  {
    Navigation n = new Navigation(getManager());
    try
    {
      DoseForms dfs = n.doseFormsLoadAll();
      System.out.println("FORMS");
      for (int i = 0; i < dfs.count(); i++)
      {
        DoseForm df = dfs.item(i);
        System.out.println(df.getID() + "," + df.getDescription());
      }
      System.out.println("ROUTES");
      DoseRoutes drs = n.doseRoutesLoadAll();
      for (int i = 0; i < drs.count(); i++)
      {
        DoseRoute dr = drs.item(i);
        System.out.println(dr.getID() + "," + dr.getDescription());
      }
    }
    catch (FDBSQLException e)
    {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }
  */
}
