package org.mirowidgets.store;

import org.mirowidgets.model.Coordinates;
import org.mirowidgets.model.Dimensions;
import org.mirowidgets.model.Widget;

import java.util.List;

public interface WidgetStore {

  /**
   * A {@link List} of widgets, sorted by Z-index, ascending
   *
   * @return a {@link List<Widget>}
   */
  List<Widget> list();

  void store(Widget widget);

  /**
   * Creates and returns a new {@link Widget}, with all properties set
   *
   * @param coordinates the widget's coordinates
   * @param dimensions the widget's dimensions
   * @param zIndex the widget's Z-index
   * @return a fully initialised {@link Widget}
   */
  Widget create(Coordinates coordinates, Dimensions dimensions, int zIndex);

  /**
   * Creates and returns a new {@link Widget}, with all non-default properties set The widget's
   * Z-index will be the max highest Z-Index value in the store + 1
   *
   * @param coordinates the widget's coordinates
   * @param dimensions the widget's dimensions
   * @return a fully initialised {@link Widget}
   */
  Widget create(Coordinates coordinates, Dimensions dimensions);

  /**
   * Removes all widgets
   */
  void clear();
}
