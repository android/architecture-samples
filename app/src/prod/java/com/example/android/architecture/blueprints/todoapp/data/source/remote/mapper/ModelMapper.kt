package com.example.android.architecture.blueprints.todoapp.data.source.remote.mapper

interface ModelMapper<M, E> {

    fun mapFromModel(model: M): E
    fun mapToModel(model: E): M

}