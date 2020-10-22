package org.mirowidgets.store;

public final class WidgetStores {

  private static final WidgetStore IN_MEMORY_WIDGET_STORE = new InMemoryWidgetStore();

  public static WidgetStore inMemoryStore() {
    return IN_MEMORY_WIDGET_STORE;
  }

  private WidgetStores() {
    throw new IllegalAccessError();
  }
}
