package com.ru.ruchurch.ui;

public interface UiInformableCallback<T> extends UiCallback<T> {
    void inform(String text);
}
