package org.dk.aledemo.layout;


import java.util.List;


/**
 * Represents parameters of a layout.
 *
 *
 */
public class LayoutParameters
{
  public static enum ArticleOrdering
  {
    FIXED,
    FLEXIBLE,
    FIXED_OPTIMIZED,
    FLEXIBLE_OPTIMIZED
  }

  public static enum ArticleUse
  {
    ALL,
    SOME // Some articles may not be used in the resulting layout
  }

  private final List<Long> _articleIds;
  private final ArticleOrdering _articleOrdering;
  private final ArticleUse _articleUse;
  private final int _containerWidth;
  private final int _containerHeight; // Inter-element
  private final int _horizontalPadding; // Inter-element
  private final int _verticalPadding;
  private final double _penaltyForSmallElement;
  private final double _penaltyForHighAspectRatio;
  private final double _penaltyForAspectRatioChange;
  private final double _penaltyForUpscaling;
  private final long _maxProcessingMsec; // For tweaking tradeoff between latency and CPU vs. quality

  public LayoutParameters(List<Long> articleIds, ArticleOrdering articleOrdering, ArticleUse articleUse,
                          int containerWidth, int containerHeight, int horizontalPadding, int verticalPadding,
                          double penaltyForSmallElement, double penaltyForHighAspectRatio,
                          double penaltyForAspectRatioChange, double penaltyForUpscaling, long maxProcessingMsec)
  {
    _articleIds = articleIds;
    _articleOrdering = articleOrdering;
    _articleUse = articleUse;
    _containerWidth = containerWidth;
    _containerHeight = containerHeight;
    _horizontalPadding = horizontalPadding;
    _verticalPadding = verticalPadding;
    _penaltyForSmallElement = penaltyForSmallElement;
    _penaltyForHighAspectRatio = penaltyForHighAspectRatio;
    _penaltyForAspectRatioChange = penaltyForAspectRatioChange;
    _penaltyForUpscaling = penaltyForUpscaling;
    _maxProcessingMsec = maxProcessingMsec;
  }

  public List<Long> getArticleIds()
  {
    return _articleIds;
  }

  public ArticleOrdering getArticleOrdering()
  {
    return _articleOrdering;
  }

  public ArticleUse getArticleUse()
  {
    return _articleUse;
  }

  public int getContainerWidth()
  {
    return _containerWidth;
  }

  public int getContainerHeight()
  {
    return _containerHeight;
  }

  public int getHorizontalPadding()
  {
    return _horizontalPadding;
  }

  public int getVerticalPadding()
  {
    return _verticalPadding;
  }

  public double getPenaltyForSmallElement()
  {
    return _penaltyForSmallElement;
  }

  public double getPenaltyForHighAspectRatio()
  {
    return _penaltyForHighAspectRatio;
  }

  public double getPenaltyForAspectRatioChange()
  {
    return _penaltyForAspectRatioChange;
  }

  public double getPenaltyForUpscaling()
  {
    return _penaltyForUpscaling;
  }

  public long getMaxProcessingMsec()
  {
    return _maxProcessingMsec;
  }
}
