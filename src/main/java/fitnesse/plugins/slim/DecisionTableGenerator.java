package fitnesse.plugins.slim;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import util.GracefulNamer;
import fitnesse.slimTables.DecisionTable;

@SuppressWarnings("unchecked")
public class DecisionTableGenerator {
  private static class SetMethod {
    private final String name;

    public SetMethod(String name) {
      this.name = name;
    }

    public static SetMethod withName(String name) {
      return new SetMethod(name);
    }

    public void add(List lines) {
      lines.add("  public void " + name + " (String " + StringUtils.uncapitalize(name.substring(3))
          + ") {");
      lines.add("  ");
      lines.add("  }");
    }
  }

  private final String basePath;

  public DecisionTableGenerator(String basePath) {
    this.basePath = basePath;
    new File(basePath).mkdirs();
  }

  public void generateFor(DecisionTable table) {
    try {
      List instructions = getSlimInstructions(table);
      String name = tableName(instructions);
      File file = new File(basePath, name + ".java");
      List row1 = (List) instructions.get(1);
      List<List<String>> row4 = (List<List<String>>) row1.get(4);
      List<String> cells = row4.get(0);
      Set<String> testMethods = new HashSet<String>();
      for (String cell : cells) {
        if(cell.endsWith("?")) {
          String methodName = GracefulNamer.regrace(cell.replaceAll("\\?", ""));
          testMethods.add(methodName);
        }
      }
      List<String> lines = new ArrayList<String>();
      lines.add("public class " + name + " {");
      blankLine(lines);
      blankLine(lines);
      addSetters(instructions, testMethods, lines);
      blankLine(lines);
      for (String testMethod : testMethods) {
        lines.add("  public Object " + testMethod + "() {");
        lines.add("    throw new RuntimeException(\"not implemented yet\");");
        lines.add("  }");
      }
      lines.add("}");
      FileUtils.writeLines(file, lines);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void blankLine(List<String> lines) {
    lines.add("");
  }

  private void addSetters(List instructions, Set<String> testMethods,
      List<String> lines) {
    for (int x = 2; x < instructions.size(); x++) {
      List row = (List) instructions.get(x);
      String methodName = getMethodName(row);
      if (!"reset".equals(methodName) && !"execute".equals(methodName) && !testMethods.contains(methodName)) {
        SetMethod.withName(methodName).add(lines);
      }
    }
  }

  private String tableName(List instructions) {
    String name = (String) ((List) instructions.get(0)).get(3);
    return name;
  }

  private String getMethodName(List row) {
    String methodName = (String) row.get(3);
    return methodName;
  }

  private ArrayList getSlimInstructions(DecisionTable table) {
    ArrayList instructions = new ArrayList();
    table.appendInstructions(instructions);
    return instructions;
  }

}
