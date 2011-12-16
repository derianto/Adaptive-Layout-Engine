package org.dk.aledemo.servlet;


import com.google.gson.Gson;
import org.dk.aledemo.algorithm.LayoutEngine;
import org.dk.aledemo.article.ArticleDataAccessor;
import org.dk.aledemo.layout.CalculatedLayout;
import org.dk.aledemo.layout.CalculatedLayoutScore;
import org.dk.aledemo.layout.ImagePlacement;
import org.dk.aledemo.layout.LayoutParameters;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;


/**
 * A servlet that does layouting of articles.
 *
 *
 */
public class LayoutEngineServlet extends HttpServlet
{
  private static final Logger log = Logger.getLogger(LayoutEngineServlet.class);

  private ServletContext _servletContext;
  private Gson _gson;
  private ArticleDataAccessor _articleDataAccessor;
  private LayoutEngine _layoutEngine;

  @Override
  public void init(ServletConfig config)
      throws ServletException
  {
    log.info("at LayoutEngineServlet.init()");
    super.init(config);

    _servletContext = config.getServletContext();
    _articleDataAccessor = (ArticleDataAccessor)_servletContext.getAttribute(AleDemoServletContextListener.ARTICLE_DATA_ACCESSOR);
    _layoutEngine = (LayoutEngine)_servletContext.getAttribute(AleDemoServletContextListener.LAYOUT_ENGINE);
    _gson = (Gson)_servletContext.getAttribute(AleDemoServletContextListener.GSON);
  }

  /**
   * The input and output formats are shown by this example:
   *
   * Example input (GSON-serialized LayoutParameters):

     {
       "articleIds": ["409","104","312","504","105"],
       "articleOrdering": "FIXED",
       "articleUse": "ALL",
       "containerWidth": 700,
       "containerHeight": 500,
       "horizontalPadding": 2,
       "verticalPadding": 2,
       "penaltyForSmallElement": 1.0,
       "penaltyForHighAspectRatio": 1.0,
       "penaltyForAspectRatioChange": 1.0,
       "penaltyForUpscaling": 1.0,
       "maxProcessingMsec": 5
     }

   * Example output (GSON-serialized CalculatedLayout):

     {
       "imagePlacements": {
         "409": {
           "absoluteLeft": 100,
           "absoluteTop": 150,
           "croppedWidth": 120,
           "croppedHeight": 110
         },
         "504": {
           "absoluteLeft": 222,
           "absoluteTop": 222,
           "croppedWidth": 70,
           "croppedHeight": 100
         },
         "312": {
           "absoluteLeft": 222,
           "absoluteTop": 150,
           "croppedWidth": 70,
           "croppedHeight": 70
         },
         "104": {
           "absoluteLeft": 100,
           "absoluteTop": 262,
           "croppedWidth": 120,
           "croppedHeight": 60
         },
         "105": {
           "absoluteLeft": 294,
           "absoluteTop": 150,
           "croppedWidth": 150,
           "croppedHeight": 172
         }
       },
       "score": {
         "totalPenalty": 12.5
       }
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
    // Parse input
    String postBody = IOUtils.toString(request.getInputStream(), "UTF-8");
    LayoutParameters layoutParameters = _gson.fromJson(postBody, LayoutParameters.class);

    CalculatedLayout calculatedLayout = _layoutEngine.calculateLayout(layoutParameters);

    // TODO HIGH TEST
    /*Map<Long, ImagePlacement> imagePlacements = new HashMap<Long, ImagePlacement>();
    int numSelectedArticles = layoutParameters.getArticleIds().size();
    if (numSelectedArticles == 0)
    {
      sendBadRequest("There must be at least one selected article", response);
      return;
    }
    imagePlacements.put(layoutParameters.getArticleIds().get(0 % numSelectedArticles),
                        new ImagePlacement(100, 150, 120, 110));
    imagePlacements.put(layoutParameters.getArticleIds().get(1 % numSelectedArticles),
                        new ImagePlacement(100, 260 + layoutParameters.getVerticalPadding(), 120, 60));
    imagePlacements.put(layoutParameters.getArticleIds().get(2 % numSelectedArticles),
                        new ImagePlacement(220 + layoutParameters.getHorizontalPadding(), 150, 70, 70));
    imagePlacements.put(layoutParameters.getArticleIds().get(3 % numSelectedArticles),
                        new ImagePlacement(220 + layoutParameters.getHorizontalPadding(), 220 + layoutParameters.getVerticalPadding(), 70, 100));
    imagePlacements.put(layoutParameters.getArticleIds().get(4 % numSelectedArticles),
                        new ImagePlacement(290 + 2 * layoutParameters.getHorizontalPadding(), 150, 150, 170 + layoutParameters.getVerticalPadding()));

    CalculatedLayoutScore calculatedLayoutScore = new CalculatedLayoutScore(22.5);
    CalculatedLayout calculatedLayout = new CalculatedLayout(imagePlacements, calculatedLayoutScore);*/

    // Format output
    response.setStatus(200);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    String json = _gson.toJson(calculatedLayout);
    response.getWriter().write(json);
  }

  private void sendBadRequest(String message, HttpServletResponse response)
      throws IOException
  {
    response.setStatus(400);
    response.setContentType("text/plain");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write(message);
  }
}
