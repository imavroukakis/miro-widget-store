package org.mirowidgets;

import org.mirowidgets.model.Coordinates;
import org.mirowidgets.model.Dimensions;
import org.mirowidgets.store.WidgetStore;
import org.mirowidgets.store.WidgetStores;

public class RunHelper {

  public static void main(String[] args) {
    final WidgetStore widgetStore = WidgetStores.inMemoryStore();
    for (int i = 0; i < 1000; i++) {
      widgetStore.create(
          Coordinates.builder().setY(0).setX(0).build(),
          Dimensions.builder().setWidth(1).setHeight(1).build(),
          i);
    }
    int size = widgetStore.list().size();
    System.out.println(size + " widgets created");
    for (int i = 0; i < 12000; i++) {
      widgetStore.create(
          Coordinates.builder().setY(0).setX(0).build(),
          Dimensions.builder().setWidth(1).setHeight(1).build(),
          3);
    }
    size = widgetStore.list().size();
    System.out.println(size + " widgets available");
  }
}
