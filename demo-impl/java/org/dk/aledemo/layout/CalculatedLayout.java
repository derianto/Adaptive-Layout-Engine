package org.dk.aledemo.layout;


import java.util.Map;


/**
 * Represents a calculated layout of articles.
 *
 *
 */
public class CalculatedLayout
{
  private final Map<Long, ImagePlacement> _imagePlacements;
  private final CalculatedLayoutScore _score;

  public CalculatedLayout(Map<Long, ImagePlacement> imagePlacements, CalculatedLayoutScore score)
  {
    _imagePlacements = imagePlacements;
    _score = score;
  }

  public Map<Long, ImagePlacement> getImagePlacements()
  {
    return _imagePlacements;
  }

  public CalculatedLayoutScore getScore()
  {
    return _score;
  }
}
