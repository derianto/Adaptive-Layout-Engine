package org.dk.aledemo.algorithm;


import java.util.List;


/**
 * Represents a row-based or column-based fitting algorithm.
 *
 *
 */
public interface FittingAlgorithm
{
  /**
   * Note that the item index is normalized to 0 .. N - 1.
   *
   * @param dimensions  image dimensions
   * @param w     desired layout width
   * @param h     desired layout height
   * @param hPad  desired horizontal / column padding
   * @param vPad  desired vertical / row padding
   * @param Ks    desired multiplier for small size penalty
   * @param Kar   desired multiplier for high aspect ratio penalty
   * @param Karc  desired multiplier for aspect ratio change penalty
   * @param Ku    desired multiplier for upscaling penalty
   * @return      Note: The image is in the original order, ordered from first to last index per row, from the first
   *              row to the last row.
   */
  FittingAlgorithmResult calculate(List<Dimension> dimensions, int hPad, int vPad, int w, int h, double Ks,
                                   double Kar, double Karc, double Ku);
}
