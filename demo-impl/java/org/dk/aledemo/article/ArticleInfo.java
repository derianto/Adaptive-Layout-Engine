package org.dk.aledemo.article;


/**
 * Represents the scraping result of an article.
 *
 *
 */
public class ArticleInfo
{
  private final String _title;
  private final ImageInfo _primaryImage;

  public ArticleInfo(String title, ImageInfo primaryImage)
  {
    _title = title;
    _primaryImage = primaryImage;
  }

  public String getTitle()
  {
    return _title;
  }

  public ImageInfo getPrimaryImage()
  {
    return _primaryImage;
  }

  @Override
  public String toString()
  {
    return "ArticleInfo(title = " + _title + ", primaryImage = " + _primaryImage + ")";
  }
}
