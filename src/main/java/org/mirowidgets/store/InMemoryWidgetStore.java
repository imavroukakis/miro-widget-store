package org.mirowidgets.store;

import org.mirowidgets.model.Coordinates;
import org.mirowidgets.model.Dimensions;
import org.mirowidgets.model.Widget;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

class InMemoryWidgetStore implements WidgetStore {
  private final Map<Integer, Widget> zIndexToWidget = new HashMap<>(1000);
  private final Map<String, Widget> idToWidget = new HashMap<>(1000);
  private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
  private final Lock writeLock = readWriteLock.writeLock();
  private final Lock readLock = readWriteLock.readLock();

  /** {@inheritDoc} */
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

  /** {@inheritDoc} */
  @Override
  public void store(Widget widget) {
    try {
      writeLock.lock();
      positionWidget(widget);
    } finally {
      writeLock.unlock();
    }
  }

  /** {@inheritDoc} */
  @Override
  public Widget create(Coordinates coordinates, Dimensions dimensions, int zIndex) {

    Widget widget =
        Widget.builder().setCoordinates(coordinates).setZ(zIndex).setDimensions(dimensions).build();

    try {
      writeLock.lock();
      positionWidget(widget);
    } finally {
      writeLock.unlock();
    }
    return widget;
  }

  /** {@inheritDoc} */
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
      positionWidget(widget);
    } finally {
      writeLock.unlock();
    }
    return widget;
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Widget> get(String id) {
    try {
      readLock.lock();
      return Optional.ofNullable(idToWidget.get(id));
    } finally {
      readLock.unlock();
    }
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Widget> update(Coordinates coordinates, String id) {
    Optional<Widget> optionalWidget = get(id);
    if (optionalWidget.isPresent()) {
      Widget widget = optionalWidget.get();
      if (widget.getCoordinates().equals(coordinates)) {
        return optionalWidget;
      }
      Widget withCoordinates = widget.withCoordinates(coordinates);
      try {
        writeLock.lock();
        zIndexToWidget.put(withCoordinates.getZ(), withCoordinates);
        idToWidget.put(withCoordinates.getId(), withCoordinates);
      } finally {
        writeLock.unlock();
      }
      return Optional.of(withCoordinates);
    } else {
      return Optional.empty();
    }
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Widget> update(Dimensions dimensions, String id) {
    Optional<Widget> optionalWidget = get(id);
    if (optionalWidget.isPresent()) {
      Widget widget = optionalWidget.get();
      if (widget.getDimensions().equals(dimensions)) {
        return optionalWidget;
      }
      Widget withDimensions = widget.withDimensions(dimensions);
      try {
        writeLock.lock();
        zIndexToWidget.put(withDimensions.getZ(), withDimensions);
        idToWidget.put(withDimensions.getId(), withDimensions);
      } finally {
        writeLock.unlock();
      }
      return Optional.of(withDimensions);
    } else {
      return Optional.empty();
    }
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Widget> update(int zIndex, String id) {
    Optional<Widget> optionalWidget = get(id);
    if (optionalWidget.isPresent()) {
      Widget widget = optionalWidget.get();
      if (widget.getZ() == zIndex) {
        return optionalWidget;
      }
      Widget withNewZ = widget.withZ(zIndex);

      readLock.lock();
      if (!zIndexToWidget.containsKey(zIndex)) {
        readLock.unlock(); // promote to write
        writeLock.lock(); // and recheck
        try {
          if (!zIndexToWidget.containsKey(zIndex)) {
            remove(widget);
            idToWidget.put(id, withNewZ);
            zIndexToWidget.put(withNewZ.getZ(), withNewZ);
          }
        } finally {
          writeLock.unlock(); // unlock
        }
      } else {
        try {
          readLock.unlock();
          writeLock.lock();
          remove(widget);
          positionWidget(withNewZ);
        } finally {
          writeLock.unlock();
        }
      }
      return Optional.of(withNewZ);
    } else {
      return Optional.empty();
    }
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Widget> update(
      Dimensions dimensions, Coordinates coordinates, int zIndex, String id) {
    return update(dimensions, id)
        .flatMap(widget -> update(coordinates, id))
        .flatMap(widget -> update(zIndex, id));
  }

  /** {@inheritDoc} */
  @Override
  public void remove(Widget widget) {
    try {
      writeLock.lock();
      zIndexToWidget.remove(widget.getZ());
      idToWidget.remove(widget.getId());
    } finally {
      writeLock.unlock();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {
    try {
      writeLock.lock();
      zIndexToWidget.clear();
      idToWidget.clear();
    } finally {
      writeLock.unlock();
    }
  }

  private void positionWidget(Widget widget) {
    int zIndex = widget.getZ();
    if (!zIndexToWidget.containsKey(zIndex)) {
      zIndexToWidget.put(zIndex, widget);
      idToWidget.put(widget.getId(), widget);
    } else {
      idToWidget.put(widget.getId(), widget);
      Widget shiftedWidget;
      while ((shiftedWidget = zIndexToWidget.replace(zIndex, widget)) != null) {
        shiftedWidget = shiftedWidget.withZ(zIndex + 1);
        idToWidget.put(shiftedWidget.getId(), shiftedWidget);
        zIndex = shiftedWidget.getZ();
        widget = shiftedWidget;
      }
      idToWidget.put(widget.getId(), widget);
      zIndexToWidget.put(widget.getZ(), widget);
    }
  }
}
