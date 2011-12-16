package org.dk.aledemo.algorithm;


import org.dk.aledemo.layout.CalculatedLayout;
import org.dk.aledemo.layout.LayoutParameters;


/**
 * Describes a class that can calculate layout.
 *
 *
 */
public interface LayoutEngine
{
  CalculatedLayout calculateLayout(LayoutParameters layoutParameters);
}
