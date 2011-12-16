package org.dk.aledemo.article;


import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;


/**
 * An article data accessor from a specified file.  Only capable of reading from the file once during its lifetime.
 *
 *
 */
public class FileArticleDataAccessor implements ArticleDataAccessor
{
  private static final Logger log = Logger.getLogger(FileArticleDataAccessor.class);

  private Gson _gson;
  private Map<Long, ArticleInfo> _articles;

  public FileArticleDataAccessor(String filename, Gson gson)
  {
    _gson = gson;
    try
    {
      loadFromFile(filename);
    }
    catch (IOException e)
    {
      throw new IllegalArgumentException("The filename " + filename + " caused loadFromFile to fail", e);
    }
  }

  private void loadFromFile(String filename)
      throws IOException
  {
    String stringContent;
    InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
    // Doesn't work
    //InputStream stream = ClassLoader.getSystemResourceAsStream(filename);
    // Doesn't work
    //FileInputStream stream = new FileInputStream(new File(filename));
    if (stream == null)
    {
      throw new IllegalArgumentException("Resource named " + filename + " is not found");
    }
    try
    {
      stringContent = IOUtils.toString(stream, "UTF-8");
    }
    finally
    {
      stream.close();
    }

    // Using Map.class doesn't work
    Type articleMapType = new TypeToken<Map<Long, ArticleInfo>>() {}.getType();
    _articles = _gson.fromJson(stringContent, articleMapType);
    log.info("------------------------- LISTING ARTICLES");
    for (long id: _articles.keySet())
    {
      log.info("  [" + id + "] " + _articles.get(id));
    }
    log.info("------------------------- END OF LISTING ARTICLES");
  }

  public List<Long> getArticleIds()
  {
    List<Long> ids = new ArrayList<Long>(_articles.keySet());
    Collections.shuffle(ids);
    return ids;
  }

  public ArticleInfo getArticle(long articleId)
  {
    return _articles.get(articleId);
  }

  public Map<Long, ArticleInfo> getArticles()
  {
    return _articles;
  }
}
