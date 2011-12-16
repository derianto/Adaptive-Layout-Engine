package org.dk.aledemo.layout;


/**
 * Represents the placement of an image.
 *
 *
 */
public class ImagePlacement
{
  private final int _absoluteLeft;
  private final int _absoluteTop;
  private final int _croppedWidth;
  private final int _croppedHeight;

  public ImagePlacement(int absoluteLeft, int absoluteTop, int croppedWidth, int croppedHeight)
  {
    _absoluteLeft = absoluteLeft;
    _absoluteTop = absoluteTop;
    _croppedWidth = croppedWidth;
    _croppedHeight = croppedHeight;
  }

  public int getAbsoluteLeft()
  {
    return _absoluteLeft;
  }

  public int getAbsoluteTop()
  {
    return _absoluteTop;
  }

  public int getCroppedWidth()
  {
    return _croppedWidth;
  }

  public int getCroppedHeight()
  {
    return _croppedHeight;
  }
}
