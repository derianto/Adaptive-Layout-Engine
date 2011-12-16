package org.dk.aledemo.algorithm.naive1;


import org.dk.aledemo.algorithm.FittingAlgorithmResult;
import java.util.Arrays;


/**
 * TODO HIGH Comment
 *
 *
 */
public class RowBasedFittingAlgorithmWithoutDeletion extends RowBasedFittingAlgorithm
{
  @Override
  public FittingAlgorithmResult calculate()
  {
    boolean[] used = new boolean[_N];
    Arrays.fill(used, true);
    FittingAlgorithmResult bestResult = null;

    for (int numRows = 1; numRows <= Math.min(8, _N); numRows++)
    {
      FittingAlgorithmResult currResult = calculate(used, numRows);
      if (bestResult == null || currResult.getTotalPenalty() < bestResult.getTotalPenalty())
      {
        bestResult = currResult;
      }
    }
    return bestResult;
  }
}
