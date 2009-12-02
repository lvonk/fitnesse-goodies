package fitnesse.plugins.slim;

import java.util.Iterator;

import org.htmlparser.util.ParserException;

import fitnesse.html.SetupTeardownIncluder;
import fitnesse.responders.run.slimResponder.MockSlimTestContext;
import fitnesse.responders.run.slimResponder.SlimTableFactory;
import fitnesse.slimTables.HtmlTableScanner;
import fitnesse.slimTables.SlimTable;
import fitnesse.slimTables.Table;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.PageCrawlerImpl;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class SlimTableFinder {
  private SlimTableFactory tableFactory;

  public SlimTableFinder() {
    tableFactory = new SlimTableFactory();
  }

  public <T extends SlimTable> T findTable(String testPath, String tableName, Class<T> tableClazz) {
    try {
      PageData data = gatherPageData(testPath);
      Iterator<Table> tables = createScanner(data).iterator();
      T foundTable = null;
      while (tables.hasNext()) {
        Table table = tables.next();
        SlimTable slimTable = tableFactory.makeSlimTable(table, "0",
            new MockSlimTestContext());
        if (slimTable.getClass().isAssignableFrom(tableClazz)) {
          foundTable = tableClazz.cast(slimTable);
          break;
        }
      }
      return foundTable;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private PageData gatherPageData(String testPath) throws Exception {
    WikiPage wikiPage = makeWikiPage(testPath);
    PageData data = wikiPage.getData();
    SetupTeardownIncluder.includeInto(data);
    return data;
  }

  private HtmlTableScanner createScanner(PageData pageData) throws Exception,
      ParserException {
    return new HtmlTableScanner(pageData.getHtml());
  }

  private WikiPage makeWikiPage(String wikiPath) throws Exception {
    FileSystemPage page = new FileSystemPage("src/test", "resources");
    PageCrawlerImpl crawlerImpl = new DefaultPageCrawler();
    WikiPagePath wikiPagePath = PathParser.parse(wikiPath);
    WikiPage wikiPage = crawlerImpl.getPage(page, wikiPagePath);
    return wikiPage;
  }
}
