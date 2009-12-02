package fitnesse.plugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import fitnesse.plugins.slim.DecisionTableGenerator;
import fitnesse.plugins.slim.SlimTableFinder;
import fitnesse.slimTables.DecisionTable;

public class DecisionTableGeneratorTest {
  private SlimTableFinder tableFinder;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private String path;
  private DecisionTableGenerator generator;
  private ClassLoader original;

  @Before
  public final void setup() {
    tableFinder = new SlimTableFinder();
    original = Thread.currentThread().getContextClassLoader();
  }

  @After
  public void after() {
    Thread.currentThread().setContextClassLoader(original);
  }

  @Test
  public void shouldGenerateClassNameBasedOnDecisionTableWithNormalName()
      throws Exception {
    path = temporaryFolder.newFolder("DecisionTableGeneratorTest")
        .getAbsolutePath();
    DecisionTable table = tableFinder.findTable("SetUpSuite.MyTest", "",
        DecisionTable.class);

    generator = new DecisionTableGenerator(path);
    generator.generateFor(table);

    File fixtureFile = new File(path, "MyFixture.java");
    assertTrue(fixtureFile.exists());
    compile(fixtureFile);
    Class<?> clazz = loadClass();
    assertNotNull(clazz);
    String fileAsString = FileUtils.readFileToString(fixtureFile);
    assertTrue(fileAsString, methodExists(clazz, "setFirstName", String.class));
    assertTrue(fileAsString, methodExists(clazz, "setLastName", String.class));
    assertTrue(fileAsString, methodExists(clazz, "valid", null));

    int numberOfMethods = clazz.getDeclaredMethods().length;
    assertEquals(3, numberOfMethods);
  }

  private boolean methodExists(Class<?> clazz, String methodName,
      Class<?> paramType) {
    try {
      if (paramType != null) {
        clazz.getDeclaredMethod(methodName, paramType);
      } else {
        clazz.getDeclaredMethod(methodName);
      }
      return true;
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      return false;
    }
  }

  private Class<?> loadClass() throws MalformedURLException,
      ClassNotFoundException {
    URLClassLoader newClassLoader = new URLClassLoader(new URL[] { new File(
        path).toURI().toURL() }, original);
    Thread.currentThread().setContextClassLoader(newClassLoader);
    Class<?> clazz = newClassLoader.loadClass("MyFixture");
    return clazz;
  }

  private void compile(File fixtureFile) {
    JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = javaCompiler.getStandardFileManager(
        null, null, Charset.forName("UTF-8"));
    Iterable<? extends JavaFileObject> compilationUnits = fileManager
        .getJavaFileObjects(fixtureFile);
    Boolean success = javaCompiler.getTask(null, fileManager, null, null, null,
        compilationUnits).call();
    assertTrue(success);
  }

  @Test
  public void shouldGenerateClassNameBasedOnDecisionTableWithGracefulName() {
    path = temporaryFolder.newFolder("DecisionTableGeneratorTest")
        .getAbsolutePath();
    DecisionTable table = tableFinder.findTable("SetUpSuite.AnotherTest", "",
        DecisionTable.class);

    generator = new DecisionTableGenerator(path);
    generator.generateFor(table);
    assertTrue(new File(path, "AnotherFixture.java").exists());
  }
}
