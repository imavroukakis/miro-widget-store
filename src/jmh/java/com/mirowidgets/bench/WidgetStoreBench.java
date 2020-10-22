package com.mirowidgets.bench;

import org.mirowidgets.model.Coordinates;
import org.mirowidgets.model.Dimensions;
import org.mirowidgets.model.Widget;
import org.mirowidgets.store.WidgetStore;
import org.mirowidgets.store.WidgetStores;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
public class WidgetStoreBench {

  @Benchmark
  @Warmup(iterations = 5)
  @Measurement(iterations = 10)
  @Threads(2)
  public void repositionWidgets(Blackhole blackhole, StateHolder stateHolder) {

    List<Widget> widgets = stateHolder.WIDGET_STORE.list();
    Widget widget =
        stateHolder.WIDGET_STORE.create(
            Coordinates.builder().setY(0).setX(0).build(),
            Dimensions.builder().setWidth(1).setHeight(1).build(),
            3);

    blackhole.consume(widgets);
    blackhole.consume(widget);
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(WidgetStoreBench.class.getSimpleName()).build();

    new Runner(opt).run();
  }

  @State(value = Scope.Benchmark)
  public static class StateHolder {

    private final WidgetStore WIDGET_STORE = WidgetStores.inMemoryStore();

    @Setup
    public void setUp() {
      List<Widget> samples =
          IntStream.range(1, 100_000)
              .boxed()
              .map(
                  num ->
                      Widget.builder()
                          .setCoordinates(Coordinates.builder().setY(0).setX(0).build())
                          .setDimensions(Dimensions.builder().setWidth(1).setHeight(1).build())
                          .setZ(num)
                          .build())
              .collect(Collectors.toUnmodifiableList());
      samples.forEach(WIDGET_STORE::store);
    }
  }
}
