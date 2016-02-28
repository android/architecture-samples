package com.example.android.architecture.blueprints.todoapp.util;

public interface BasePresenter {

    void result(int requestCode, int resultCode);

    void resume();

    void pause();

}
