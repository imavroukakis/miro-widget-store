package org.mirowidgets;

import org.mirowidgets.model.Coordinates;
import org.mirowidgets.model.Dimensions;
import org.mirowidgets.model.Widget;
import org.mirowidgets.store.WidgetStore;
import org.mirowidgets.store.WidgetStores;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RunHelper {

  public static void main(String[] args) {
    final WidgetStore widgetStore = WidgetStores.inMemoryStore();
    IntStream.range(1, 1_000)
        .boxed()
        .map(
            num ->
                Widget.builder()
                    .setCoordinates(Coordinates.builder().setY(0).setX(0).build())
                    .setDimensions(Dimensions.builder().setWidth(1).setHeight(1).build())
                    .setZIndex(num)
                    .build())
        .collect(Collectors.toUnmodifiableList())
        .forEach(widgetStore::store);

    System.out.println("load complete");

    for (int i = 0; i < 12000; i++) {
      widgetStore.create(
          Coordinates.builder().setY(0).setX(0).build(),
          Dimensions.builder().setWidth(1).setHeight(1).build(),
          3);
    }
  }
}
