package com.example.queuemanagementsystem.mapper;

public interface EntityMapper<D, E> {
    D toDto(E entity);
}
