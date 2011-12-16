package org.dk.aledemo.servlet;


import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.dk.aledemo.algorithm.LayoutEngine;
import org.dk.aledemo.algorithm.LayoutEngineImpl;
import org.dk.aledemo.article.ArticleDataAccessor;
import org.dk.aledemo.article.FileArticleDataAccessor;
import java.lang.reflect.Field;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;


/**
 * A servlet context listener for this app, used for instantiating beans (without spring!) that will be shared
 * across different servlets.
 *
 *
 */
public class AleDemoServletContextListener implements ServletContextListener
{
  private static final Logger log = Logger.getLogger(AleDemoServletContextListener.class);

  public static final String ARTICLE_FILENAME = "articles.json";
  public static final String GSON = "gson";
  public static final String ARTICLE_DATA_ACCESSOR = "articleDataAccessor";
  public static final String LAYOUT_ENGINE = "layoutEngine";

  private ServletContext _servletContext;
  private Gson _gson;
  private ArticleDataAccessor _articleDataAccessor;
  private LayoutEngine _layoutEngine;

  public void contextInitialized(ServletContextEvent servletContextEvent)
  {
    _servletContext = servletContextEvent.getServletContext();

    try
    {
      log.info("at AleDemoServletContextListener.contextInitialized()");

      // Bean instantiation
      _gson = new GsonBuilder()
        .setPrettyPrinting()
        .setFieldNamingStrategy(new FieldNamingStrategy()
        {
          public String translateName(Field f)
          {
            String name = f.getName();
            if (name.startsWith("_"))
            {
              return name.substring(1);
            }
            else
            {
              return name;
            }
          }
        })
        .create();

      _articleDataAccessor = new FileArticleDataAccessor(ARTICLE_FILENAME, _gson);
      _layoutEngine = new LayoutEngineImpl(_articleDataAccessor);

      _servletContext.setAttribute(GSON, _gson);
      _servletContext.setAttribute(ARTICLE_DATA_ACCESSOR, _articleDataAccessor);
      _servletContext.setAttribute(LAYOUT_ENGINE, _layoutEngine);
    }
    catch (Exception e)
    {
      log.error("FATAL EXCEPTION - DEPLOYMENT FAILS", e);
      throw new IllegalStateException(e);
    }
  }

  public void contextDestroyed(ServletContextEvent servletContextEvent)
  {
    log.info("at AleDemoServletContextListener.contextDestroyed()");
  }
}
