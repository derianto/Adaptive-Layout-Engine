package org.dk.aledemo.algorithm;


import org.dk.aledemo.algorithm.naive1.RowBasedFittingAlgorithmWithDeletion;
import org.dk.aledemo.algorithm.naive1.RowBasedFittingAlgorithmWithoutDeletion;
import org.dk.aledemo.article.ArticleDataAccessor;
import org.dk.aledemo.article.ImageInfo;
import org.dk.aledemo.layout.CalculatedLayout;
import org.dk.aledemo.layout.CalculatedLayoutScore;
import org.dk.aledemo.layout.ImagePlacement;
import org.dk.aledemo.layout.LayoutParameters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The default implementation of layout engine that understand all the parameters in LayoutParameters.
 *
 *
 */
public class LayoutEngineImpl implements LayoutEngine
{
  private final ArticleDataAccessor _articleDataAccessor;
  private final ThreadLocal<RowBasedFittingAlgorithmWithDeletion> _rowBasedFittingAlgorithmWithDeletion =
      new ThreadLocal<RowBasedFittingAlgorithmWithDeletion>()
      {
        @Override
        protected RowBasedFittingAlgorithmWithDeletion initialValue()
        {
          return new RowBasedFittingAlgorithmWithDeletion();
        }
      };
  private final ThreadLocal<RowBasedFittingAlgorithmWithoutDeletion> _rowBasedFittingAlgorithmWithoutDeletion =
      new ThreadLocal<RowBasedFittingAlgorithmWithoutDeletion>()
      {
        @Override
        protected RowBasedFittingAlgorithmWithoutDeletion initialValue()
        {
          return new RowBasedFittingAlgorithmWithoutDeletion();
        }
      };
  private final ThreadLocal<ColumnBasedFittingAlgorithm> _columnBasedFittingAlgorithmWithDeletion =
      new ThreadLocal<ColumnBasedFittingAlgorithm>()
      {
        @Override
        protected ColumnBasedFittingAlgorithm initialValue()
        {
          return new ColumnBasedFittingAlgorithm(new RowBasedFittingAlgorithmWithDeletion());
        }
      };
  private final ThreadLocal<ColumnBasedFittingAlgorithm> _columnBasedFittingAlgorithmWithoutDeletion =
      new ThreadLocal<ColumnBasedFittingAlgorithm>()
      {
        @Override
        protected ColumnBasedFittingAlgorithm initialValue()
        {
          return new ColumnBasedFittingAlgorithm(new RowBasedFittingAlgorithmWithoutDeletion());
        }
      };

  public LayoutEngineImpl(ArticleDataAccessor articleDataAccessor)
  {
    _articleDataAccessor = articleDataAccessor;
  }

  public CalculatedLayout calculateLayout(LayoutParameters layoutParameters)
  {
    FittingAlgorithm rowBasedFittingAlgorithm;
    FittingAlgorithm columnBasedFittingAlgorithm;

    if (layoutParameters.getArticleUse() == LayoutParameters.ArticleUse.ALL)
    {
      rowBasedFittingAlgorithm = _rowBasedFittingAlgorithmWithoutDeletion.get();
      columnBasedFittingAlgorithm = _columnBasedFittingAlgorithmWithoutDeletion.get();
    }
    else if (layoutParameters.getArticleUse() == LayoutParameters.ArticleUse.SOME)
    {
      rowBasedFittingAlgorithm = _rowBasedFittingAlgorithmWithDeletion.get();
      columnBasedFittingAlgorithm = _columnBasedFittingAlgorithmWithDeletion.get();
    }
    else
    {
      throw new IllegalArgumentException("LayoutParameters.ArticleUse value is not recognized");
    }

    boolean isOptimized = layoutParameters.getArticleOrdering() == LayoutParameters.ArticleOrdering.FIXED_OPTIMIZED ||
                          layoutParameters.getArticleOrdering() == LayoutParameters.ArticleOrdering.FLEXIBLE_OPTIMIZED;
    boolean isFlexible = layoutParameters.getArticleOrdering() == LayoutParameters.ArticleOrdering.FLEXIBLE ||
                         layoutParameters.getArticleOrdering() == LayoutParameters.ArticleOrdering.FLEXIBLE_OPTIMIZED;

    Map<Long, Integer> rowBasedImageIndexMap;
    Map<Long, Integer> columnBasedImageIndexMap;

    FittingAlgorithmResult rowBasedResult;
    FittingAlgorithmResult columnBasedResult;

    CalculatedLayout calculatedLayout;

    // Get mapping from article IDs to zero-based index
    if (isOptimized)
    {
      rowBasedImageIndexMap = getSortedByHeightDesc(layoutParameters.getArticleIds());
      columnBasedImageIndexMap = getSortedByWidthDesc(layoutParameters.getArticleIds());
    }
    else
    {
      rowBasedImageIndexMap = getOriginalOrder(layoutParameters.getArticleIds());
      columnBasedImageIndexMap = rowBasedImageIndexMap;
    }

    // Get raw result (using zero-based index)
    rowBasedResult = runAlgorithm(rowBasedFittingAlgorithm, layoutParameters, rowBasedImageIndexMap);
    columnBasedResult = runAlgorithm(columnBasedFittingAlgorithm, layoutParameters, columnBasedImageIndexMap);

    // Get calculated layout
    if (rowBasedResult.getTotalPenalty() <= columnBasedResult.getTotalPenalty())
    {
      Map<Long, ImagePlacement> imagePlacements = getImagePlacements(
          rowBasedResult.getFittingResult(), rowBasedImageIndexMap,
          isFlexible, isFlexible, layoutParameters.getHorizontalPadding(), layoutParameters.getVerticalPadding(), true);
      CalculatedLayoutScore calculatedLayoutScore = new CalculatedLayoutScore(rowBasedResult.getTotalPenalty());
      calculatedLayout = new CalculatedLayout(imagePlacements, calculatedLayoutScore);
    }
    else
    {
      Map<Long, ImagePlacement> imagePlacements = getImagePlacements(
          columnBasedResult.getFittingResult(), columnBasedImageIndexMap,
          isFlexible, isFlexible, layoutParameters.getHorizontalPadding(), layoutParameters.getVerticalPadding(), false);
      CalculatedLayoutScore calculatedLayoutScore = new CalculatedLayoutScore(columnBasedResult.getTotalPenalty());
      calculatedLayout = new CalculatedLayout(imagePlacements, calculatedLayoutScore);
    }

    return calculatedLayout;
  }

  /**
   * Returns a map from articleID to 0-based sequential index.
   * @param articleIds
   * @return
   */
  private Map<Long, Integer> getOriginalOrder(List<Long> articleIds)
  {
    Map<Long, Integer> map = new HashMap<Long, Integer>();
    int index = 0;
    for (Long articleId: articleIds)
    {
      map.put(articleId, index++);
    }
    return map;
  }

  /**
   * Returns a map from articleID to 0-based sequential index.  The images are ordered by height, descending.
   * @param articleIds
   * @return
   */
  private Map<Long, Integer> getSortedByHeightDesc(List<Long> articleIds)
  {
    List<Long> sortedArticleIds = new ArrayList<Long>(articleIds);
    final Map<Long, Integer> imageHeights = new HashMap<Long, Integer>();
    for (Long articleId: articleIds)
    {
      imageHeights.put(articleId, _articleDataAccessor.getArticle(articleId).getPrimaryImage().getHeight());
    }
    Collections.sort(sortedArticleIds, new Comparator<Long>()
    {
      public int compare(Long id1, Long id2)
      {
        if (imageHeights.get(id1) > imageHeights.get(id2))
        {
          return -1;
        }
        if (imageHeights.get(id1) < imageHeights.get(id2))
        {
          return 1;
        }
        return 0;
      }
    });

    Map<Long, Integer> map = new HashMap<Long, Integer>();
    int index = 0;
    for (Long articleId: sortedArticleIds)
    {
      map.put(articleId, index++);
    }
    return map;
  }

  /**
   * Returns a map from articleID to 0-based sequential index.  The images are ordered by width, descending.
   * @param articleIds
   * @return
   */
  private Map<Long, Integer> getSortedByWidthDesc(List<Long> articleIds)
  {
    List<Long> sortedArticleIds = new ArrayList<Long>(articleIds);
    final Map<Long, Integer> imageWidths = new HashMap<Long, Integer>();
    for (Long articleId: articleIds)
    {
      imageWidths.put(articleId, _articleDataAccessor.getArticle(articleId).getPrimaryImage().getWidth());
    }
    Collections.sort(sortedArticleIds, new Comparator<Long>()
    {
      public int compare(Long id1, Long id2)
      {
        if (imageWidths.get(id1) > imageWidths.get(id2))
        {
          return -1;
        }
        if (imageWidths.get(id1) < imageWidths.get(id2))
        {
          return 1;
        }
        return 0;
      }
    });

    Map<Long, Integer> map = new HashMap<Long, Integer>();
    int index = 0;
    for (Long articleId: sortedArticleIds)
    {
      map.put(articleId, index++);
    }
    return map;
  }

  /**
   * Returns an image placement given raw image placements.  Can optionally specify to shuffle in row per row and
   * shuffle row order.
   * @param rawImagePlacements
   * @param articleIdToIndexMap
   * @param shuffleInRow
   * @param shuffleRows
   * @param hPadding
   * @param vPadding
   * @param rowBased
   * @return
   */
  private Map<Long, ImagePlacement> getImagePlacements(List<List<Dimension>> rawImagePlacements,
                                                       Map<Long, Integer> articleIdToIndexMap,
                                                       boolean shuffleInRow, boolean shuffleRows,
                                                       int hPadding, int vPadding, boolean rowBased)
  {
    Map<Long, ImagePlacement> imagePlacements = new HashMap<Long, ImagePlacement>();
    List<Long> articleIds = new ArrayList<Long>();
    Map<Long, Dimension> dimensionByArticleId = new HashMap<Long, Dimension>();
    List<List<Long>> articleIdRows = new ArrayList<List<Long>>();

    // Get mapping from zero-based index to articleIds
    for (int i = 0; i < articleIdToIndexMap.size(); i++)
    {
      articleIds.add(null);
    }
    for (Long articleId: articleIdToIndexMap.keySet())
    {
      articleIds.set(articleIdToIndexMap.get(articleId), articleId);
    }

    // Get dimensionByArticleId and articleIdRows
    int index = 0;
    for (List<Dimension> dimensions: rawImagePlacements)
    {
      List<Long> articleIdRow = new ArrayList<Long>();
      for (Dimension dimension: dimensions)
      {
        dimensionByArticleId.put(articleIds.get(index), dimension);
        articleIdRow.add(articleIds.get(index));
        index++;
      }
      articleIdRows.add(articleIdRow);
    }

    // Do shuffling if necessary
    if (shuffleInRow)
    {
      for (List<Long> articleIdRow: articleIdRows)
      {
        Collections.shuffle(articleIdRow);
      }
    }
    if (shuffleRows)
    {
      Collections.shuffle(articleIdRows);
    }

    if (rowBased)
    {
      // Calculate image placements
      int currY = 0;
      for (List<Long> articleIdRow: articleIdRows)
      {
        int currX = 0;
        for (Long articleId: articleIdRow)
        {
          Dimension dimension = dimensionByArticleId.get(articleId);
          imagePlacements.put(articleId, new ImagePlacement(currX, currY, dimension.getWidth(), dimension.getHeight()));

          currX += dimension.getWidth() + hPadding;
        }
        currY += dimensionByArticleId.get(articleIdRow.get(0)).getHeight() + vPadding;
      }
    }
    else
    {
      // Calculate image placements
      int currX = 0;
      for (List<Long> articleIdRow: articleIdRows)
      {
        int currY = 0;
        for (Long articleId: articleIdRow)
        {
          Dimension dimension = dimensionByArticleId.get(articleId);
          imagePlacements.put(articleId, new ImagePlacement(currX, currY, dimension.getWidth(), dimension.getHeight()));

          currY += dimension.getHeight() + vPadding;
        }
        currX += dimensionByArticleId.get(articleIdRow.get(0)).getWidth() + hPadding;
      }
    }

    return imagePlacements;
  }

  /**
   * Run the specified fittingAlgorithm given the layout parameters and the ID mapping.
   * @param fittingAlgorithm
   * @param layoutParameters
   * @param articleIdToIndexMap
   * @return
   */
  private FittingAlgorithmResult runAlgorithm(FittingAlgorithm fittingAlgorithm, LayoutParameters layoutParameters,
                                              Map<Long, Integer> articleIdToIndexMap)
  {
    List<Dimension> dimensions = new ArrayList<Dimension>();
    for (int i = 0; i < articleIdToIndexMap.size(); i++)
    {
      dimensions.add(null);
    }

    for (Long articleId: articleIdToIndexMap.keySet())
    {
      int index = articleIdToIndexMap.get(articleId);
      ImageInfo imageInfo = _articleDataAccessor.getArticle(articleId).getPrimaryImage();
      dimensions.set(index, new Dimension(imageInfo.getWidth(), imageInfo.getHeight()));
    }

    FittingAlgorithmResult result = fittingAlgorithm.calculate(
        dimensions, layoutParameters.getHorizontalPadding(), layoutParameters.getVerticalPadding(),
        layoutParameters.getContainerWidth(), layoutParameters.getContainerHeight(),
        layoutParameters.getPenaltyForSmallElement(), layoutParameters.getPenaltyForHighAspectRatio(),
        layoutParameters.getPenaltyForAspectRatioChange(), layoutParameters.getPenaltyForUpscaling());

    return result;
  }
}
