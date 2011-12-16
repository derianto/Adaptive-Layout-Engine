package org.dk.aledemo.servlet;


import com.google.gson.Gson;
import org.dk.aledemo.article.ArticleDataAccessor;
import org.dk.aledemo.article.ArticleInfo;
import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;


/**
 * A servlet that simulates the scraper providing a list of articles to maybe display.
 *
 *
 */
public class ArticleProviderServlet extends HttpServlet
{
  private static final Logger log = Logger.getLogger(ArticleProviderServlet.class);

  private ServletContext _servletContext;
  private Gson _gson;
  private ArticleDataAccessor _articleDataAccessor;

  @Override
  public void init(ServletConfig config)
      throws ServletException
  {
    log.info("at ArticleProviderServlet.init()");
    super.init(config);

    _servletContext = config.getServletContext();
    _articleDataAccessor = (ArticleDataAccessor)_servletContext.getAttribute(AleDemoServletContextListener.ARTICLE_DATA_ACCESSOR);
    _gson = (Gson)_servletContext.getAttribute(AleDemoServletContextListener.GSON);
  }

  /**
   * The input and output formats are shown by this example:
   *
   * Example input: none
   *
   * Example output (GSON-serialized Map<Long, ArticleInfo>):
     {
       101: {
         title: "Loyal Dog In China Refuses To Leave Owner's Grave, Goes Week Without Food",
         primaryImage: {
           imageUrl: "http://i.huffpost.com/gen/414658/thumbs/s-LOYAL-DOG-large.jpg",
           width: 260,
           height: 190
         }
       },
       102: {
         title: "Image Expert Shapes Romney (His Hair, Anyway)",
         primaryImage: {
           imageUrl: "As Hydrofracking Decision Nears, Industry Spending Skyrockets",
           width: 190,
           height: 155
         }
       },
     }
   *
   * @param request
   * @param response
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    response.setStatus(200);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    Map<Long, ArticleInfo> longArticleInfoMap = _articleDataAccessor.getArticles();
    String json = _gson.toJson(longArticleInfoMap);
    response.getWriter().write(json);
  }
}
