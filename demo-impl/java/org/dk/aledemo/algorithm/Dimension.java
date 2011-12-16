package org.dk.aledemo.algorithm;


/**
 * Represents a width-height tuple.
 *
 *
 */
public class Dimension
{
  private final int _width;
  private final int _height;

  public Dimension(int width, int height)
  {
    _width = width;
    _height = height;
  }

  public int getWidth()
  {
    return _width;
  }

  public int getHeight()
  {
    return _height;
  }
}
