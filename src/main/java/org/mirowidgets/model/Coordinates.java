package org.mirowidgets.model;

import org.immutables.value.Value;
import org.mirowidgets.value.Immutables;

@Value.Immutable
@Immutables.DefaultStyle
interface CoordinatesModel {
  int getX();
  int getY();
}
