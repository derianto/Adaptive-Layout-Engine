package org.dk.aledemo.article;


/**
 * Represents a scraped image.
 *
 *
 */
public class ImageInfo
{
  private final String _imageUrl;
  private final int _width;
  private final int _height;

  public ImageInfo(String imageUrl, int width, int height)
  {
    _imageUrl = imageUrl;
    _width = width;
    _height = height;
  }

  public String getImageUrl()
  {
    return _imageUrl;
  }

  public int getWidth()
  {
    return _width;
  }

  public int getHeight()
  {
    return _height;
  }

  @Override
  public String toString()
  {
    return "ImageInfo(imageUrl= " + _imageUrl + ", width = " + _width + ", height = " + _height + ")";
  }
}
