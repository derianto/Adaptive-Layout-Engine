package org.dk.aledemo.algorithm;


import java.util.List;


/**
 * Represents a result of FittingAlgorithm calculation.
 *
 *
 */
public class FittingAlgorithmResult
{
  // A list of rows, where each row contains a list of images to put there and resized to the specified dimension
  private final List<List<Dimension>> _fittingResult;
  private final double _totalPenalty;

  public FittingAlgorithmResult(List<List<Dimension>> fittingResult, double totalPenalty)
  {
    _fittingResult = fittingResult;
    _totalPenalty = totalPenalty;
  }

  public List<List<Dimension>> getFittingResult()
  {
    return _fittingResult;
  }

  public double getTotalPenalty()
  {
    return _totalPenalty;
  }
}
