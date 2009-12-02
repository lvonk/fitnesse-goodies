package fitnesse.plugins;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import fitnesse.plugins.slim.SlimTableFinder;
import fitnesse.slimTables.DecisionTable;
import fitnesse.slimTables.ImportTable;

public class SlimTableFinderTest {

  private SlimTableFinder tableFinder;

  @Before
  public final void setUp() {
    tableFinder = new SlimTableFinder();
  }

  @Test
  public void testHowFitNessePagesWork() throws Exception {
    ImportTable actual = tableFinder.findTable("SimpleSuite", "Import",
        ImportTable.class);
    assertNotNull(actual);
  }

  @Test
  public void testWithSetUp() throws Exception {
    DecisionTable actual = tableFinder.findTable("SetUpSuite.MyTest",
        "my fixture", DecisionTable.class);
    assertNotNull(actual);
  }

}
