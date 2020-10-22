package org.mirowidgets;

import de.huxhorn.sulky.ulid.ULID;
import org.junit.Before;
import org.junit.Test;
import org.mirowidgets.model.Coordinates;
import org.mirowidgets.model.Dimensions;
import org.mirowidgets.model.Widget;
import org.mirowidgets.store.WidgetStore;
import org.mirowidgets.store.WidgetStores;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class WidgetTest {

  private static final WidgetStore WIDGET_STORE = WidgetStores.inMemoryStore();

  @Before
  public void clearStore() {
    WIDGET_STORE.clear();
  }

  @Test
  public void new_widget_has_valid_id_automatically_generated() {

    // given
    Widget widget =
        WIDGET_STORE.create(
            Coordinates.builder().setX(0).setY(0).build(),
            Dimensions.builder().setHeight(1).setWidth(1).build(),
            0);

    // then
    assertThat(widget.getId()).isNotBlank();
    assertThatCode(() -> ULID.parseULID(widget.getId())).doesNotThrowAnyException();
  }

  @Test
  public void new_widget_has_last_modified_date_automatically_generated() {

    // given
    Widget widget =
        WIDGET_STORE.create(
            Coordinates.builder().setX(0).setY(0).build(),
            Dimensions.builder().setHeight(1).setWidth(1).build(),
            0);

    // then
    assertThat(widget.getLastModified()).isBefore(LocalDateTime.now());
  }

  @Test
  public void widget_store_list_returns_smallest_to_largest_by_z_index() {

    // given
    WIDGET_STORE.create( // TODO maybe chain?
        Coordinates.builder().setX(0).setY(0).build(),
        Dimensions.builder().setHeight(1).setWidth(1).build(),
        0);
    WIDGET_STORE.create(
        Coordinates.builder().setX(0).setY(0).build(),
        Dimensions.builder().setHeight(1).setWidth(1).build(),
        10);
    WIDGET_STORE.create(
        Coordinates.builder().setX(0).setY(0).build(),
        Dimensions.builder().setHeight(1).setWidth(1).build(),
        3);

    // when
    List<Widget> widgets = WIDGET_STORE.list();

    // then
    assertThat(widgets).isSortedAccordingTo(Comparator.comparingInt(Widget::getZ));
  }

  @Test
  public void new_widget_with_duplicate_zIndex_displaces_others_when_stored() {

    // given
    WIDGET_STORE.create( // TODO maybe chain?
        Coordinates.builder().setX(0).setY(0).build(),
        Dimensions.builder().setHeight(1).setWidth(1).build(),
        1);
    WIDGET_STORE.create(
        Coordinates.builder().setX(0).setY(0).build(),
        Dimensions.builder().setHeight(1).setWidth(1).build(),
        2);
    WIDGET_STORE.create(
        Coordinates.builder().setX(0).setY(0).build(),
        Dimensions.builder().setHeight(1).setWidth(1).build(),
        3);
    WIDGET_STORE.create(
        Coordinates.builder().setX(0).setY(0).build(),
        Dimensions.builder().setHeight(1).setWidth(1).build(),
        10);

    // when
    Widget widget =
        WIDGET_STORE.create(
            Coordinates.builder().setX(0).setY(0).build(),
            Dimensions.builder().setHeight(1).setWidth(1).build(),
            2);

    // then
    List<Widget> widgets = WIDGET_STORE.list();
    assertThat(widgets).size().isEqualTo(5);
    assertThat(widgets).last().extracting("z").containsOnly(10);
    assertThat(widgets).extracting("z").containsExactly(1, 2, 3, 4, 10);
    assertThat(widgets.get(1)).isEqualTo(widget);
  }

  @Test
  public void new_widget_without_zIndex_is_assigned_max_zIndex_when_stored() {

    // given
    WIDGET_STORE.create( // TODO maybe chain?
        Coordinates.builder().setX(0).setY(0).build(),
        Dimensions.builder().setHeight(1).setWidth(1).build(),
        1);
    WIDGET_STORE.create(
        Coordinates.builder().setX(0).setY(0).build(),
        Dimensions.builder().setHeight(1).setWidth(1).build(),
        2);
    WIDGET_STORE.create(
        Coordinates.builder().setX(0).setY(0).build(),
        Dimensions.builder().setHeight(1).setWidth(1).build(),
        3);

    // when
    Widget widget =
        WIDGET_STORE.create(
            Coordinates.builder().setX(0).setY(0).build(),
            Dimensions.builder().setHeight(1).setWidth(1).build());

    // then
    List<Widget> widgets = WIDGET_STORE.list();
    assertThat(widgets).size().isEqualTo(4);
    assertThat(widgets).last().extracting("z").containsOnly(4);
    assertThat(widgets).extracting("z").containsExactly(1, 2, 3, 4);
    assertThat(widgets.get(3)).isEqualTo(widget);
  }

  @Test
  public void new_widget_with_MAX_VALUE_zIndex_cannot_be_stored() {

    // given
    WIDGET_STORE.create(
        Coordinates.builder().setX(0).setY(0).build(),
        Dimensions.builder().setHeight(1).setWidth(1).build(),
        Integer.MAX_VALUE);

    // then
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(
            () ->
                WIDGET_STORE.create(
                    Coordinates.builder().setX(0).setY(0).build(),
                    Dimensions.builder().setHeight(1).setWidth(1).build()));
  }
}
