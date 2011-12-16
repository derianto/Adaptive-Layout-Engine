package org.dk.aledemo.algorithm;


import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;


/**
 * A column-based fitting algorithm that simply delegates to the inner row-based fitting algorithm, transforming
 * the input data and output data in a way that can operate with the row-based fitting algorithm.
 *
 *
 */
public class ColumnBasedFittingAlgorithm implements FittingAlgorithm
{
  private static final Logger log = Logger.getLogger(ColumnBasedFittingAlgorithm.class);

  private final FittingAlgorithm _rowBasedFittingAlgorithm;

  public ColumnBasedFittingAlgorithm(FittingAlgorithm rowBasedFittingAlgorithm)
  {
    _rowBasedFittingAlgorithm = rowBasedFittingAlgorithm;
  }

  public FittingAlgorithmResult calculate(List<Dimension> dimensions, int hPad, int vPad, int w, int h, double Ks,
                                          double Kar, double Karc, double Ku)
  {
    log.info("\n\n============================= ColumnBasedFittingAlgorithm.calculate =============================\n\n");
    List<Dimension> flippedDimensions = flipXYAxis(dimensions);
    FittingAlgorithmResult originalResult = _rowBasedFittingAlgorithm.calculate(flippedDimensions, vPad, hPad, h, w,
                                                                                Ks, Kar, Karc, Ku);
    return flipXYAxis(originalResult);
  }

  /**
   * Returns a list with X and Y axis flipped for every item.
   * @param dimensions
   */
  private List<Dimension> flipXYAxis(List<Dimension> dimensions)
  {
    List<Dimension> result = new ArrayList<Dimension>();
    for (Dimension dimension: dimensions)
    {
      result.add(new Dimension(dimension.getHeight(), dimension.getWidth()));
    }
    return result;
  }

  /**
   * Returns a FittingAlgorithmResult with X and Y axis flipped for every item.
   * @param fittingAlgorithmResult
   * @return
   */
  private FittingAlgorithmResult flipXYAxis(FittingAlgorithmResult fittingAlgorithmResult)
  {
    List<List<Dimension>> newFittingResult = new ArrayList<List<Dimension>>();
    for (List<Dimension> dimensions: fittingAlgorithmResult.getFittingResult())
    {
      List<Dimension> newDimensions = new ArrayList<Dimension>();
      for (Dimension dimension: dimensions)
      {
        newDimensions.add(new Dimension(dimension.getHeight(), dimension.getWidth()));
      }
      newFittingResult.add(newDimensions);
    }
    return new FittingAlgorithmResult(newFittingResult, fittingAlgorithmResult.getTotalPenalty());
  }
}
