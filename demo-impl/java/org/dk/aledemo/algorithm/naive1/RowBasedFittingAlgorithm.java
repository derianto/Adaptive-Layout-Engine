package org.dk.aledemo.algorithm.naive1;


import org.dk.aledemo.algorithm.Dimension;
import org.dk.aledemo.algorithm.FittingAlgorithm;
import org.dk.aledemo.algorithm.FittingAlgorithmResult;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;


/**
 * Heuristic dumb row-based fitting algorithm.
 *
 *
 */
public abstract class RowBasedFittingAlgorithm implements FittingAlgorithm
{
  private static final Logger log = Logger.getLogger(RowBasedFittingAlgorithm.class);

  protected static final double LOG2 = Math.log(2.0);
  protected static final double INVERTED_LOG2 = 1.0 / LOG2;
  protected static final double MIN_ACCEPTABLE_SIZE = 150.0 * 100.0;
  protected static final double INVERTED_MIN_ACCEPTABLE_SIZE = 1.0 / MIN_ACCEPTABLE_SIZE;

  // Item dimensions, with index 0 .. N - 1
  protected List<Dimension> _dimensions;
  // Number of items
  protected int _N;
  // Desired layout width
  protected int _W;
  // Desired layout height
  protected int _H;
  // Desired horizontal padding
  protected int _hPad;
  // Desired vertical padding
  protected int _vPad;

  // Scaling constant for small size penalty
  protected double _Ks;
  // Scaling constant for high aspect ratio penalty
  protected double _Kar;
  // Scaling constant for aspect ratio change penalty
  protected double _Karc;
  // Scaling constant for upscaling penalty
  protected double _Ku;

  protected double penalty(int no, double desiredW, double desiredH)
  {
    // Note: The penalty function is concave on desiredW and on desiredH, with a local neighbor of (w, h)
    // causing lowest value of penalty().
    // Second-degree derivative of penalty() over desiredW or desiredH is >= 0.
    //
    // This enables gradient descent on (desiredW, desiredH) or even on (vector of desiredW_i, desiredH)
    // to reach the optimal desiredW_i, desiredH to reach the minimum penalty for positioning a sequence of images.

    double wi = _dimensions.get(no).getWidth();
    double hi = _dimensions.get(no).getHeight();
    double ari = wi / hi;
    double desiredAr = desiredW / desiredH;

    // Small size penalty
    double ps = -_Ks * Math.log(Math.min(1.0, desiredW * desiredH * INVERTED_MIN_ACCEPTABLE_SIZE)) * INVERTED_LOG2;

    // High aspect ratio penalty
    double absLogAr = Math.abs(Math.log(desiredW / desiredH) * INVERTED_LOG2 - 0.5);
    double par = _Kar * absLogAr * absLogAr * absLogAr;

    // Aspect ratio change penalty
    double absLogArc = Math.abs(Math.log(desiredW * hi / (desiredH * wi)) * INVERTED_LOG2);
    double parc = _Karc * absLogArc * absLogArc * absLogArc;

    // Upscaling penalty
    double desiredArea;
    if (desiredAr >= ari)
    {
      desiredArea = desiredW * desiredW / desiredAr;
    }
    else
    {
      desiredArea = desiredH * desiredH * desiredAr;
    }

    double absLogUpscale = Math.log(Math.max(1.0, desiredArea / (wi * hi))) * INVERTED_LOG2;
    double pu = _Ku * absLogUpscale * absLogUpscale;

    return ps + par + parc + pu;
  }

  public FittingAlgorithmResult calculate(List<Dimension> dimensions, int hPad, int vPad, int w, int h, double Ks,
                                          double Kar, double Karc, double Ku)
  {
    log.info("\n\n============================= RowBasedFittingAlgorithm.calculate =============================\n\n");

    _dimensions = dimensions;
    _N = dimensions.size();
    _W = w;
    _H = h;
    _hPad = hPad;
    _vPad = vPad;
    _Ks = Ks;
    _Kar = Kar;
    _Karc = Karc;
    _Ku = Ku;

    return calculate();
  }

  public abstract FittingAlgorithmResult calculate();

  public FittingAlgorithmResult calculate(boolean[] used, int numRows)
  {
    // Determine number of items actually used
    // Determine number of items that are used from index 0..i
    // (Therefore, number of items used from index i+1..n-1 is nUsed - numUsed[i])
    int[] numUsed = new int[_N];
    int nUsed = 0;
    int totalW = 0;
    for (int i = 0; i < _N; i++)
    {
      if (used[i])
      {
        nUsed++;
        numUsed[i] = nUsed;
        totalW += _dimensions.get(i).getWidth();
      }
    }

    // Determine row index partitioning
    int rowNo = 1;
    int[] rowStartIndex = new int[numRows + 1];
    rowStartIndex[0] = 0;
    rowStartIndex[numRows] = _N;
    int currW = 0;
    for (int i = 0; i < _N; i++)
    {
      if (used[i])
      {
        // Imperative: must assign if there will be no more items available for subsequent rows
        // Optional: assign if the currW indicates that this likely signifies row boundary
        if (nUsed - numUsed[i] == numRows - 1 - rowNo ||
            currW > rowNo * totalW / numRows)
        {
          rowStartIndex[rowNo++] = i;
        }

        currW += _dimensions.get(i).getWidth();
      }
    }

    List<List<Dimension>> fittingResult = new ArrayList<List<Dimension>>();

    // Calculate average height per row
    double[] rowAverageHeight = new double[numRows];
    double[] rowTotalWidth = new double[numRows];
    int[] rowNumItems = new int[numRows];
    double totalAverageHeight = 0.0;
    for (int r = 0; r < numRows; r++)
    {
      double totalHeight = 0.0;

      rowTotalWidth[r] = 0;
      rowNumItems[r] = 0;

      for (int i = rowStartIndex[r]; i < rowStartIndex[r + 1]; i++)
      {
        if (used[i])
        {
          rowNumItems[r]++;
          rowTotalWidth[r] += _dimensions.get(i).getWidth();
          totalHeight += _dimensions.get(i).getHeight();
        }
      }

      rowAverageHeight[r] = totalHeight / rowNumItems[r];
      totalAverageHeight += rowAverageHeight[r];
    }

    log.info("DUMPING various row stats for numRows = " + numRows + "  totalW = " + totalW);
    currW = 0;
    for (int i = 0; i < numRows; i++)
    {
      log.info(i + " starts with index " + rowStartIndex[i] +
                   " avg height = " + rowAverageHeight[i] +
                   " total width = " + rowTotalWidth[i] +
                   " num iterms = " + rowNumItems[i]);
    }
    log.info("");

    // Determine row heights by normalizing by average height per row
    // Determine element widths per row
    // Populate fittingResult
    double remainingAvailableHeight = _H - (numRows - 1) * _vPad;
    double remainingTotalAverageHeight = totalAverageHeight;
    for (int r = 0; r < numRows; r++)
    {
      int assignedRowHeight = (int)Math.round(rowAverageHeight[r] / remainingTotalAverageHeight * remainingAvailableHeight);
      List<Dimension> rowResult = new ArrayList<Dimension>();

      double remainingAvailableWidth = _W - (rowNumItems[r] - 1) * _hPad;
      double remainingTotalWidth = rowTotalWidth[r];
      for (int i = rowStartIndex[r]; i < rowStartIndex[r + 1]; i++)
      {
        if (used[i])
        {
          int assignedItemWidth = (int)Math.round(_dimensions.get(i).getWidth() / remainingTotalWidth * remainingAvailableWidth);

          rowResult.add(new Dimension(assignedItemWidth, assignedRowHeight));

          remainingTotalWidth -= _dimensions.get(i).getWidth();
          remainingAvailableWidth -= assignedItemWidth;
        }
      }

      remainingAvailableHeight -= assignedRowHeight;
      remainingTotalAverageHeight -= rowAverageHeight[r];
      fittingResult.add(rowResult);
    }

    // Calculate score for this arrangement
    double totalPenalty = getTotalPenalty(fittingResult);
    FittingAlgorithmResult result = new FittingAlgorithmResult(fittingResult, totalPenalty);
    return result;
  }

  private double getTotalPenalty(List<List<Dimension>> fittingResult)
  {
    double totalPenalty = 0.0;
    int itemNo = 0;
    for (List<Dimension> dimensions: fittingResult)
    {
      for (Dimension dimension: dimensions)
      {
        totalPenalty += penalty(itemNo, dimension.getWidth(), dimension.getHeight());
        itemNo++;
      }
    }

    return totalPenalty;
  }
}
