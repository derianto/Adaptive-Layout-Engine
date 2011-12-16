package org.dk.aledemo.algorithm.dp1;


import org.dk.aledemo.algorithm.FittingAlgorithmResult;
import java.util.Arrays;
import org.apache.log4j.Logger;


/**
 * TODO HIGH Comment
 *
 *
 */
public class RowBasedFittingAlgorithmWithoutDeletion extends RowBasedFittingAlgorithm
{
  private static final Logger log = Logger.getLogger(RowBasedFittingAlgorithmWithoutDeletion.class);

  public RowBasedFittingAlgorithmWithoutDeletion()
  {
  }

  protected void fillRw()
  {
    log.info("============================== fill R, Rw ===============================");

    Arrays.fill(_R, INFINITY);
    Arrays.fill(_Rw, INFINITY);

    //protected static final int R_LENGTH = H_ROW_GRANULAR_MAX * N_MAX * N_PER_ROW * W_ROW_GRANULAR_MAX;

    for (int h = WH_ROW_GRANULARITY; h < _H + WH_ROW_GRANULARITY; h += WH_ROW_GRANULARITY)
    {
      int rIndexH = (h / WH_ROW_GRANULARITY) * N_MAX * N_PER_ROW * W_ROW_GRANULAR_MAX;
      log.info("---------------------------------------------------- h = " + h);

      for (int i = 0; i < N_MAX - 1; i++) // i < N_MAX - 1 is intentional
      {
        int rIndexI = rIndexH + i * N_PER_ROW * W_ROW_GRANULAR_MAX;

        log.info("-------------------------------------- i = " + i);
        {
          int j = i;
          int rIndexJ = rIndexI + (j - i) * W_ROW_GRANULAR_MAX;

          for (int w = WH_ROW_GRANULARITY; w < _W + WH_ROW_GRANULARITY; w += WH_ROW_GRANULARITY)
          {
            int rIndex = rIndexJ + (w / WH_ROW_GRANULARITY);

            // Case: R[h, i, j, w], j == i

            _R[rIndex] = penalty(i, w, h);
            _RFromW[rIndex] = -1; // undefined

            log.info("R[h = " + h + ", item no " + i + ".." + j + ", w = " + w + "] = " + _R[rIndex] + " from wp = " + _RFromW[rIndex]);
          }

          // Fill Rw[i, j, h]
          int rwIndex = (i * N_PER_ROW + j) * H_ROW_GRANULAR_MAX + (h / WH_ROW_GRANULARITY);
          int wOffset = _W / WH_ROW_GRANULARITY;
          int wModulo = _W % WH_ROW_GRANULARITY;
          _Rw[rwIndex] = (_R[rIndexJ + wOffset] * (WH_ROW_GRANULARITY - wModulo) +
                          _R[rIndexJ + wOffset + 1] * wModulo)
                         / WH_ROW_GRANULARITY;
          log.info("Rw[item no " + i + ".." + j + ", h = " + h + "] = " + _Rw[rwIndex]);
        }

        for (int j = i + 1; j < i + N_PER_ROW; j++) // j < i + N_PER_ROW is intentional
        {
          int rIndexJ = rIndexI + (j - i) * W_ROW_GRANULAR_MAX;
          int rIndexJMin1 = rIndexI + (j - i - 1) * W_ROW_GRANULAR_MAX;

          for (int w = WH_ROW_GRANULARITY; w < _W + WH_ROW_GRANULARITY; w += WH_ROW_GRANULARITY)
          {
            int rIndex = rIndexJ + (w / WH_ROW_GRANULARITY);

            // Case: R[h, i, j, w], j != i

            // Ternary search wp to determine best R value
            int wp0 = 1; // Low points
            int wp3 = w - _hPad - 1; // High points
            int wp1, wp2;
            int wpOffset;
            int wpModulo;
            double Rwp1, Rwp2;

            while (wp3 >= wp0 + 3)
            {
              wp1 = wp0 + (wp3 - wp0) / 3;
              wp2 = wp3 - (wp3 - wp0) / 3;

              // TODO LOW: can be wrong on boundary cases, e.g. at minimal width, etc.
              // Interpolate table call values for simulating the following expression with non-integer index
              //Rwp1 = _R[rIndexJMin1 + ((double)wp1 / WH_ROW_GRANULARITY)] + penalty(j, w - wp1 - _hPad, h);
              wpOffset = wp1 / WH_ROW_GRANULARITY;
              wpModulo = wp1 % WH_ROW_GRANULARITY;
              Rwp1 = (_R[rIndexJMin1 + wpOffset] * (WH_ROW_GRANULARITY - wpModulo) +
                      _R[rIndexJMin1 + wpOffset + 1] * wpModulo)
                   / WH_ROW_GRANULARITY
                   + penalty(j, w - wp1 - _hPad, h);

              // Interpolate table call values for simulating the following expression with non-integer index
              //Rwp2 = _R[rIndexJMin1 + ((double)wp2 / WH_ROW_GRANULARITY)] + penalty(j, w - wp2 - _hPad, h);
              wpOffset = wp2 / WH_ROW_GRANULARITY;
              wpModulo = wp2 % WH_ROW_GRANULARITY;
              Rwp2 = (_R[rIndexJMin1 + wpOffset] * (WH_ROW_GRANULARITY - wpModulo) +
                      _R[rIndexJMin1 + wpOffset + 1] * wpModulo)
                   / WH_ROW_GRANULARITY
                   + penalty(j, w - wp2 - _hPad, h);

              if (Rwp1 <= Rwp2)
              {
                wp3 = wp2;
              }
              else
              {
                wp0 = wp1;
              }
            }

            _R[rIndex] = INFINITY;
            _RFromW[rIndex] = -1; // undefined
            for (int wp = wp0; wp <= wp3; wp++)
            {
              // Interpolate table call values for simulating the following expression with non-integer index
              //Rwp = _R[rIndexJMin1 + ((double)wp / WH_ROW_GRANULARITY)] + penalty(j, w - wp - _hPad, h);
              wpOffset = wp / WH_ROW_GRANULARITY;
              wpModulo = wp % WH_ROW_GRANULARITY;
              double Rwp = (_R[rIndexJMin1 + wpOffset] * (WH_ROW_GRANULARITY - wpModulo) +
                      _R[rIndexJMin1 + wpOffset + 1] * wpModulo)
                   / WH_ROW_GRANULARITY
                   + penalty(j, w - wp - _hPad, h);

              if (Rwp < _R[rIndex])
              {
                _R[rIndex] = Rwp;
                _RFromW[rIndex] = wp;
              }
            }

            log.info("R[h = " + h + ", item no " + i + ".." + j + ", w = " + w + "] = " + _R[rIndex] + " from wp = " + _RFromW[rIndex]);
          }

          // Fill Rw[i, j, h]
          int rwIndex = (i * N_PER_ROW + j) * H_ROW_GRANULAR_MAX + (h / WH_ROW_GRANULARITY);
          int wOffset = _W / WH_ROW_GRANULARITY;
          int wModulo = _W % WH_ROW_GRANULARITY;
          _Rw[rwIndex] = (_R[rIndexJ + wOffset] * (WH_ROW_GRANULARITY - wModulo) +
                          _R[rIndexJ + wOffset + 1] * wModulo)
                         / WH_ROW_GRANULARITY;
          log.info("Rw[item no " + i + ".." + j + ", h = " + h + "] = " + _Rw[rwIndex]);
        }
      }
    }
  }

  protected void fillMRw()
  {
    log.info("============================== fill MRw ===============================");
    Arrays.fill(_MRw, INFINITY);

    {
      int i = 0;
      log.info("---------------------------------------------------- i = " + i);
      int mrIndexI = i * H_LAYOUT_GRANULAR_MAX;

      for (int h = WH_LAYOUT_GRANULARITY; h < _H + WH_LAYOUT_GRANULARITY; h++)
      {
        int mrIndex = mrIndexI + h / WH_LAYOUT_GRANULARITY;

        // Interpolate
        //_MRw[mrIndexH] = _Rw[((double)h / WH_ROW_GRANULARITY) * N_MAX * N_PER_ROW + i];
        int hOffset = h / WH_ROW_GRANULARITY;
        int hModulo = h % WH_ROW_GRANULARITY;
        _MRw[mrIndex] = (_Rw[(hOffset) * N_MAX * N_PER_ROW + i]     * (WH_ROW_GRANULARITY - hModulo) +
                         _Rw[(hOffset + 1) * N_MAX * N_PER_ROW + i] * hModulo)
                       / WH_ROW_GRANULARITY;
        _MRwFromH[mrIndex] = 0;
        _MRwFromI[mrIndex] = -1; // undefined
      }
    }

    for (int i = 1; i < N_MAX; i++)
    {
      log.info("---------------------------------------------------- i = " + i);
      int mrIndexI = i * H_LAYOUT_GRANULAR_MAX;

      for (int h = WH_LAYOUT_GRANULARITY; h < _H + WH_LAYOUT_GRANULARITY; h += WH_LAYOUT_GRANULARITY)
      {
        int mrIndex = mrIndexI + h / WH_LAYOUT_GRANULARITY;

        double bestValue = INFINITY;
        double currentValue;
        _MRw[mrIndex] = INFINITY;
        _MRwFromH[mrIndex] = -1; // undefined
        _MRwFromI[mrIndex] = -1; // undefined

        // Limit to at most (N_PER_ROW - 1) items in a row
        for (int j = Math.max(0, i - (N_PER_ROW - 1)); j < i; j++)
        {
          int mrIndexJ = j * H_LAYOUT_GRANULAR_MAX;
          int rwIndexJI = ((j + 1) * N_PER_ROW + i) * H_ROW_GRANULAR_MAX;
          int optimalHp;

          for (int hp = WH_LAYOUT_GRANULARITY; hp < h - _vPad; hp += WH_LAYOUT_GRANULARITY)
          {
            int mrIndexJHp = mrIndexJ + hp / WH_LAYOUT_GRANULARITY;
            int hpOffset = (h - hp - _vPad) / WH_ROW_GRANULARITY;
            int hpModulo = (h - hp - _vPad) % WH_ROW_GRANULARITY;
            //int rwIndexHpJI = rwIndexJI + ((h - hp - _vPad) / WH_ROW_GRANULARITY) * N_MAX * N_PER_ROW;

            // TODO HIGH not implemented
            // Interpolate
            //currentValue = _MRw[] + _Rw[]
          }

          // After finding the most optimal hp for this j, look around the smaller-granularity neighbor of hp
          // to find the most optimal hp
          // TODO HIGH algo flaw
          // Does this even make sense though?  I don't think so b/c you don't have granular enough information
          // at the Rw level and at the earlier MRw cells.
        }

        _MRw[mrIndex] = bestValue;
        log.info("MRw[i = " + i + ", h = " + h + "] = " + _MRw[mrIndex]);
      }
    }
  }

  protected FittingAlgorithmResult backtrack()
  {
    // TODO HIGH
    return null;
  }

}
