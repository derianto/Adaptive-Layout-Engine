package org.dk.aledemo.algorithm.dp1;


import org.dk.aledemo.algorithm.Dimension;
import org.dk.aledemo.algorithm.FittingAlgorithm;
import org.dk.aledemo.algorithm.FittingAlgorithmResult;
import java.util.List;
import org.apache.log4j.Logger;


/**
 * The core implementation of dynamic-programming-based fitting algorithm.
 *
 *
 */
public abstract class RowBasedFittingAlgorithm implements FittingAlgorithm
{
  private static final Logger log = Logger.getLogger(RowBasedFittingAlgorithm.class);

  protected static final int N_MAX = 50 + 1;
  protected static final int W_MAX = 1200 + 1;
  protected static final int H_MAX = 1200 + 1;
  protected static final int N_PER_ROW = 8 + 1; // Max number of items per row / column
  protected static final int WH_ROW_GRANULARITY = 50; // Step size for W or H during single row / column optimization
  protected static final int WH_LAYOUT_GRANULARITY = 10; // Step size for W or H during layout optimization

  protected static final int W_ROW_GRANULAR_MAX = W_MAX / WH_ROW_GRANULARITY + 1;
  protected static final int H_ROW_GRANULAR_MAX = H_MAX / WH_ROW_GRANULARITY + 1;
  protected static final int H_LAYOUT_GRANULAR_MAX = H_MAX / WH_LAYOUT_GRANULARITY + 1;

  // Helper constants for R
  protected static final int R_LENGTH = H_ROW_GRANULAR_MAX * N_MAX * N_PER_ROW * W_ROW_GRANULAR_MAX;

  // Helper constants for Rw
  protected static final int RW_LENGTH = N_MAX * N_PER_ROW * H_ROW_GRANULAR_MAX;

  // Helper constants for MRw
  protected static final int MRW_LENGTH = N_MAX * H_LAYOUT_GRANULAR_MAX;

  protected static final double LOG2 = Math.log(2.0);
  protected static final double INVERTED_LOG2 = 1.0 / LOG2;
  protected static final double MIN_ACCEPTABLE_SIZE = 150.0 * 100.0;
  protected static final double INVERTED_MIN_ACCEPTABLE_SIZE = 1.0 / MIN_ACCEPTABLE_SIZE;

  // For indicating invalid table cell value
  protected static final double INFINITY = 1.0e10;

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

  // R[h, i, j, w] = the minimum penalty for putting items no. i..j (inclusive) into a row with height h
  //                 and width w
  protected double[] _R = new double[R_LENGTH];

  // RW[i, j, h] = the minimum penalty for putting items no. i..j (inclusive) into a row with height h
  //               and width w (width is fixed at desired layout width)
  protected double[] _Rw = new double[RW_LENGTH];

  // MRw[i, h] = the minimum penalty for putting items no 0..i (inclusive) into a multirow layout of height h
  //             and width w (width is fixed at desired layout width)
  protected double[] _MRw = new double[MRW_LENGTH];

  // RFromW[h, i, j, w] = the index w' such that the optimal value R[h, i, j, w] is obtained from
  //                      R[h, i, j - 1, w'] + penalty(j, w - w', h)
  protected int[] _RFromW = new int[R_LENGTH];

  // RFromUsed[h, i, j, w] = true if the optimal value R[h, i, j, w] is obtained by selecting j-th item
  //                         false if the optimal value R[h, i, j, w] is obtained by not selecting j-th item
  protected boolean[] _RFromUsed = new boolean[R_LENGTH];

  // (MRwFromI[i, h], MRwFromH[i, h]) = the index (i', h') such that the optimal value MRw[i, h] is obtained from
  //                                    MRw[i', h'] + R[h - h', i' + 1, i, w]
  protected int[] _MRwFromI = new int[MRW_LENGTH];
  protected int[] _MRwFromH = new int[MRW_LENGTH];

  public RowBasedFittingAlgorithm()
  {
  }

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

    fillRw();
    fillMRw();
    FittingAlgorithmResult result = backtrack();
    return result;
  }

  protected abstract void fillRw();

  protected abstract void fillMRw();

  protected abstract FittingAlgorithmResult backtrack();
}
