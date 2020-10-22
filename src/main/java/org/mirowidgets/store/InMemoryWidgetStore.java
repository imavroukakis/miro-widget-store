package org.mirowidgets.store;

import org.mirowidgets.model.Coordinates;
import org.mirowidgets.model.Dimensions;
import org.mirowidgets.model.Widget;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

class InMemoryWidgetStore implements WidgetStore {
  private final Map<Integer, Widget> zIndexToWidget = new HashMap<>(1000);
  private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();
  Lock writeLock = LOCK.writeLock();
  Lock readLock = LOCK.readLock();

  @Override
  public List<Widget> list() {
    try {
      readLock.lock();
      return zIndexToWidget.values().stream()
          .sorted(Comparator.comparingInt(Widget::getZ))
          .collect(Collectors.toUnmodifiableList());
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public void store(Widget widget) {
    try {
      writeLock.lock();
      zIndexToWidget.putAll(positionWidget(widget));
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public Widget create(Coordinates coordinates, Dimensions dimensions, int zIndex) {

    Widget widget =
        Widget.builder().setCoordinates(coordinates).setZ(zIndex).setDimensions(dimensions).build();

    try {
      writeLock.lock();
      zIndexToWidget.putAll(positionWidget(widget));
    } finally {
      writeLock.unlock();
    }
    return widget;
  }

  @Override
  public Widget create(Coordinates coordinates, Dimensions dimensions) {
    OptionalInt maybeMax;
    try {
      readLock.lock();
      maybeMax = zIndexToWidget.keySet().stream().mapToInt(v -> v).max();
    } finally {
      readLock.unlock();
    }
    int max = maybeMax.orElse(-1);
    if (max == Integer.MAX_VALUE) {
      throw new IllegalStateException();
    }
    Widget widget =
        Widget.builder()
            .setCoordinates(coordinates)
            .setDimensions(dimensions)
            .setZ(max + 1)
            .build();

    try {
      writeLock.lock();
      zIndexToWidget.putAll(positionWidget(widget));
    } finally {
      writeLock.unlock();
    }
    return widget;
  }

  @Override
  public void clear() {
    try {
      writeLock.lock();
      zIndexToWidget.clear();
    } finally {
      writeLock.unlock();
    }
  }

  private Map<Integer, Widget> positionWidget(Widget widget) {
    Map<Integer, Widget> widgets = new HashMap<>();
    widgets.put(widget.getZ(), widget);
    int zIndex = widget.getZ();
    while (zIndexToWidget.containsKey(zIndex)) {
      Widget shiftableWidget = zIndexToWidget.get(zIndex).withZ(zIndex + 1);
      zIndex = shiftableWidget.getZ();
      widgets.put(zIndex, shiftableWidget);
    }
    return widgets;
  }
}
