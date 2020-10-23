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
import java.util.Optional;

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
  public void new_widget_with_invalid_width_height_throws_exception() {

    // given
    Widget widget =
        WIDGET_STORE.create(
            Coordinates.builder().setX(0).setY(0).build(),
            Dimensions.builder().setHeight(1).setWidth(1).build(),
            0);

    // then
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(
            () ->
                WIDGET_STORE.create(
                    Coordinates.builder().setX(0).setY(0).build(),
                    Dimensions.builder().setHeight(10).setWidth(0).build(),
                    0))
        .withMessage("width must be greater than zero");
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(
            () ->
                WIDGET_STORE.create(
                    Coordinates.builder().setX(0).setY(0).build(),
                    Dimensions.builder().setHeight(0).setWidth(10).build(),
                    0))
        .withMessage("height must be greater than zero");
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
    WIDGET_STORE.create(
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
    assertThat(widgets).isSortedAccordingTo(Comparator.comparingInt(Widget::getZIndex));
  }

  @Test
  public void new_widget_with_duplicate_zIndex_displaces_others_when_stored() {

    // given
    for (int i = 1; i <= 12_000; i++) {
      WIDGET_STORE.create(
          Coordinates.builder().setX(0).setY(0).build(),
          Dimensions.builder().setHeight(1).setWidth(1).build(),
          i);
    }
    // when
    Widget widget =
        WIDGET_STORE.create(
            Coordinates.builder().setX(0).setY(0).build(),
            Dimensions.builder().setHeight(1).setWidth(1).build(),
            9);

    // then
    List<Widget> widgets = WIDGET_STORE.list();
    assertThat(widgets).size().isEqualTo(12001);
    assertThat(widgets).extractingResultOf("getZIndex").containsSequence(12_000, 12_001);
    assertThat(widgets.get(8)).isEqualTo(widget);
  }

  @Test
  public void new_widget_without_zIndex_is_assigned_max_zIndex_when_stored() {

    // given
    WIDGET_STORE.create(
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
    assertThat(widgets).last().extracting("zIndex").containsOnly(4);
    assertThat(widgets).extracting("zIndex").containsExactly(1, 2, 3, 4);
    assertThat(widgets.get(3)).isEqualTo(widget);
  }

  @Test
  public void return_widget_when_looking_up_existing_id() {

    // given
    Widget widget =
        WIDGET_STORE.create(
            Coordinates.builder().setX(0).setY(0).build(),
            Dimensions.builder().setHeight(1).setWidth(1).build(),
            999);
    // when
    Optional<Widget> optionalWidget = WIDGET_STORE.get(widget.getId());

    // then
    assertThat(optionalWidget).isPresent().containsSame(widget);
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

  @Test
  public void updating_widget_coordinates_returns_widget_with_those_coordinates() {
    // given
    Widget widget =
        WIDGET_STORE.create(
            Coordinates.builder().setX(0).setY(0).build(),
            Dimensions.builder().setHeight(1).setWidth(1).build(),
            Integer.MAX_VALUE);

    // when
    Optional<Widget> optionalWidget =
        WIDGET_STORE.update(Coordinates.builder().setX(1).setY(1).build(), widget.getId());

    // then
    assertThat(optionalWidget).isPresent();
    Widget updatedWidget = optionalWidget.get();
    assertThat(updatedWidget).isNotEqualTo(widget);
    assertThat(updatedWidget.getCoordinates()).isNotEqualTo(widget.getCoordinates());
    assertThat(updatedWidget.getLastModified()).isAfter(widget.getLastModified());
    assertThat(updatedWidget.getZIndex()).isEqualTo(widget.getZIndex());
    assertThat(updatedWidget.getDimensions()).isEqualTo(widget.getDimensions());
    assertThat(updatedWidget.getId()).isEqualTo(widget.getId());
  }

  @Test
  public void updating_widget_dimensions_returns_widget_with_those_dimensions() {
    // given
    Widget widget =
        WIDGET_STORE.create(
            Coordinates.builder().setX(0).setY(0).build(),
            Dimensions.builder().setHeight(1).setWidth(1).build(),
            Integer.MAX_VALUE);

    // when
    Optional<Widget> optionalWidget =
        WIDGET_STORE.update(
            Dimensions.builder().setHeight(10).setWidth(10).build(), widget.getId());

    // then
    assertThat(optionalWidget).isPresent();
    Widget updatedWidget = optionalWidget.get();
    assertThat(updatedWidget).isNotEqualTo(widget);
    assertThat(updatedWidget.getCoordinates()).isEqualTo(widget.getCoordinates());
    assertThat(updatedWidget.getZIndex()).isEqualTo(widget.getZIndex());
    assertThat(updatedWidget.getLastModified()).isAfter(widget.getLastModified());
    assertThat(updatedWidget.getDimensions()).isNotEqualTo(widget.getDimensions());
    assertThat(updatedWidget.getId()).isEqualTo(widget.getId());
  }

  @Test
  public void updating_all_widget_data_returns_widget_with_new_data() {
    // given
    Widget widget =
        WIDGET_STORE.create(
            Coordinates.builder().setX(0).setY(0).build(),
            Dimensions.builder().setHeight(1).setWidth(1).build(),
            0);

    // when
    Optional<Widget> optionalWidget =
        WIDGET_STORE.update(
            Dimensions.builder().setHeight(10).setWidth(10).build(),
            Coordinates.builder().setX(10).setY(10).build(),
            1,
            widget.getId());

    // then
    assertThat(optionalWidget).isPresent();
    Widget updatedWidget = optionalWidget.get();
    assertThat(updatedWidget).isNotEqualTo(widget);
    assertThat(updatedWidget.getCoordinates()).isNotEqualTo(widget.getCoordinates());
    assertThat(updatedWidget.getZIndex()).isNotEqualTo(widget.getZIndex());
    assertThat(updatedWidget.getLastModified()).isAfter(widget.getLastModified());
    assertThat(updatedWidget.getDimensions()).isNotEqualTo(widget.getDimensions());
    assertThat(updatedWidget.getId()).isEqualTo(widget.getId());
    assertThat(WIDGET_STORE.list()).size().isEqualTo(1);
  }

  @Test
  public void updating_widget_zIndex_returns_widget_with_this_zIndex_and_moves_others() {
    // given
    String id =
        WIDGET_STORE
            .create(
                Coordinates.builder().setX(0).setY(0).build(),
                Dimensions.builder().setHeight(1).setWidth(1).build(),
                10)
            .getId();

    Widget widgetToUpdate =
        WIDGET_STORE.create(
            Coordinates.builder().setX(0).setY(0).build(),
            Dimensions.builder().setHeight(1).setWidth(1).build(),
            11);

    // when
    Optional<Widget> optionalUpdatedWidget = WIDGET_STORE.update(10, widgetToUpdate.getId());

    // then
    assertThat(optionalUpdatedWidget).isPresent();
    Widget updatedWidget = optionalUpdatedWidget.get();
    assertThat(updatedWidget).isNotEqualTo(widgetToUpdate);
    assertThat(updatedWidget.getCoordinates()).isEqualTo(widgetToUpdate.getCoordinates());
    assertThat(updatedWidget.getZIndex()).isNotEqualTo(widgetToUpdate.getZIndex()).isEqualTo(10);
    assertThat(updatedWidget.getLastModified()).isAfter(widgetToUpdate.getLastModified());
    assertThat(updatedWidget.getDimensions()).isEqualTo(widgetToUpdate.getDimensions());
    assertThat(updatedWidget.getId()).isEqualTo(widgetToUpdate.getId());
    Optional<Widget> displacedWidget = WIDGET_STORE.get(id);
    assertThat(displacedWidget).isPresent();
    assertThat(displacedWidget.get().getZIndex()).isEqualTo(11);
    assertThat(WIDGET_STORE.list()).size().isEqualTo(2);
  }

  @Test
  public void updating_widget_zIndex_to_an_empty_slot_returns_widget_with_this_zIndex() {
    // given
    String id =
        WIDGET_STORE
            .create(
                Coordinates.builder().setX(0).setY(0).build(),
                Dimensions.builder().setHeight(1).setWidth(1).build(),
                10)
            .getId();

    Widget newWidget =
        WIDGET_STORE.create(
            Coordinates.builder().setX(0).setY(0).build(),
            Dimensions.builder().setHeight(1).setWidth(1).build(),
            11);

    // when
    Optional<Widget> optionalUpdatedWidget = WIDGET_STORE.update(15, newWidget.getId());

    // then
    assertThat(optionalUpdatedWidget).isPresent();
    Widget updatedWidget = optionalUpdatedWidget.get();
    assertThat(updatedWidget).isNotEqualTo(newWidget);
    assertThat(updatedWidget.getCoordinates()).isEqualTo(newWidget.getCoordinates());
    assertThat(updatedWidget.getZIndex()).isNotEqualTo(newWidget.getZIndex()).isEqualTo(15);
    assertThat(updatedWidget.getLastModified()).isAfter(newWidget.getLastModified());
    assertThat(updatedWidget.getDimensions()).isEqualTo(newWidget.getDimensions());
    assertThat(updatedWidget.getId()).isEqualTo(newWidget.getId());
    assertThat(WIDGET_STORE.list()).size().isEqualTo(2);
  }

  @Test
  public void updating_widget_zIndex_to_the_same_zIndex_returns_the_same_widget() {
    // given
    Widget widget =
        WIDGET_STORE.create(
            Coordinates.builder().setX(0).setY(0).build(),
            Dimensions.builder().setHeight(1).setWidth(1).build(),
            10);

    // when
    Optional<Widget> optionalUpdatedWidget = WIDGET_STORE.update(10, widget.getId());

    // then
    assertThat(optionalUpdatedWidget).isPresent();
    Widget updatedWidget = optionalUpdatedWidget.get();
    assertThat(updatedWidget).isEqualTo(updatedWidget);
    assertThat(updatedWidget).isSameAs(widget);
    assertThat(WIDGET_STORE.list()).size().isEqualTo(1);
  }
}
