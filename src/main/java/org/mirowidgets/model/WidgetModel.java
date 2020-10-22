package org.mirowidgets.model;

import com.google.common.base.Preconditions;
import de.huxhorn.sulky.ulid.ULID;
import org.immutables.value.Value;
import org.mirowidgets.value.Immutables;

import java.time.LocalDateTime;

@Value.Immutable
@Immutables.DefaultStyle
abstract class WidgetModel {
  private static final ULID ULID_INSTANCE = new ULID();

  abstract Coordinates getCoordinates();

  @Value.Default
  int getZ() {
    return Integer.MAX_VALUE;
  }

  abstract Dimensions getDimensions();

  @Value.Default
  LocalDateTime getLastModified() {
    return LocalDateTime.now();
  }

  @Value.Default
  String getId() {
    return ULID_INSTANCE.nextULID();
  }

  @Value.Check
  protected void check() {
    Preconditions.checkState(getDimensions().getWidth() > 0, "width must be greater than zero");
    Preconditions.checkState(getDimensions().getHeight() > 0, "height must be greater than zero");
  }
}
