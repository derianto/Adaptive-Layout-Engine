package org.dk.aledemo.algorithm.dp1;


import org.dk.aledemo.algorithm.FittingAlgorithmResult;
import org.apache.log4j.Logger;


/**
 * TODO HIGH Comment
 *
 *
 */
public class RowBasedFittingAlgorithmWithDeletion extends RowBasedFittingAlgorithm
{
  private static final Logger log = Logger.getLogger(RowBasedFittingAlgorithmWithDeletion.class);

  public RowBasedFittingAlgorithmWithDeletion()
  {
  }

  @Override
  protected FittingAlgorithmResult backtrack()
  {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  protected void fillMRw()
  {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  protected void fillRw()
  {
    throw new UnsupportedOperationException("Not implemented");
  }
}
