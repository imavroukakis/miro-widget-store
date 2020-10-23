package org.mirowidgets.store;

import org.mirowidgets.model.Coordinates;
import org.mirowidgets.model.Dimensions;
import org.mirowidgets.model.Widget;

import java.util.List;
import java.util.Optional;

public interface WidgetStore {

  /**
   * A {@link List} of widgets, sorted by Z-index, ascending
   *
   * @return a {@link List<Widget>}
   */
  List<Widget> list();

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
   * Attempts to find a widget by its id
   *
   * @param id the widget's id
   * @return an Optional {@link Widget}
   */
  Optional<Widget> get(String id);

  /**
   * Updates the coordinates of a {@link Widget}. Looks up the widget by id and if found, updates
   * the coordinates and returns the widget
   *
   * @param coordinates the new coordinates
   * @param id the widget id to update
   * @return if it exists, the updated widget, otherwise {@link Optional#empty()}
   */
  Optional<Widget> update(Coordinates coordinates, String id);

  /**
   * Updates the dimensions of a {@link Widget}. Looks up the widget by id and if found, updates the
   * dimensions and returns the widget
   *
   * @param dimensions the new dimensions
   * @param id the widget id to update
   * @return if it exists, the updated widget, otherwise {@link Optional#empty()}
   */
  Optional<Widget> update(Dimensions dimensions, String id);

  /**
   * Updates the Z-Index of a {@link Widget}. Looks up the widget by id and if found, updates the
   * Z-Index and returns the widget
   *
   * @param zIndex the new Z-Index
   * @param id the widget id to update
   * @return if it exists, the updated widget, otherwise {@link Optional#empty()}
   */
  Optional<Widget> update(int zIndex, String id);

  /**
   * Updates the Z-Index,dimensions and coordinates of a {@link Widget}. Looks up the widget by id
   * and if found, updates the Z-Index,dimensions and coordinates and returns the widget
   *
   * @param dimensions the new dimensions
   * @param coordinates the new coordinates
   * @param zIndex the new Z-Index
   * @param id the widget id to update
   * @return if it exists, the updated widget, otherwise {@link Optional#empty()}
   */
  Optional<Widget> update(Dimensions dimensions, Coordinates coordinates, int zIndex, String id);

  /**
   * Removes a widget
   *
   * @param widget the {@link Widget} to remove
   */
  void remove(Widget widget);

  /** Removes all widgets */
  void clear();
}
