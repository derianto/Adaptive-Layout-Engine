package org.dk.aledemo.layout;


/**
 * Represents the score of a calculated layout.
 *
 *
 */
public class CalculatedLayoutScore
{
  private final double _totalPenalty;

  public CalculatedLayoutScore(double totalPenalty)
  {
    _totalPenalty = totalPenalty;
  }

  public double getTotalPenalty()
  {
    return _totalPenalty;
  }
}
