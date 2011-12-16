package org.dk.aledemo.servlet;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;



/**
 * A servlet that renders the main page.
 *
 *
 */
public class MainPageServlet extends HttpServlet
{
  private static final Logger log = Logger.getLogger(MainPageServlet.class);

  private VelocityEngine _velocityEngine;

  @Override
  public void init()
  {
    // Copied from example in nus_trunk
    // TODO MED Use file loader, nocache for dev (see tap/war for example of dev/prod build switch)
    _velocityEngine = new VelocityEngine();
    _velocityEngine.setProperty("resource.loader", "class");
    _velocityEngine.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    _velocityEngine.setProperty("file.resource.loader.cache", true);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    log.info("on doGet()");

    Map<String, Object> model = new HashMap<String, Object>();
    model.put("title", "Title 1");

    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");
    try
    {
      _velocityEngine.mergeTemplate("mainpage.html", new VelocityContext(model), response.getWriter());
    }
    catch (Exception e)
    {
      throw new ServletException(e);
    }
  }
}
