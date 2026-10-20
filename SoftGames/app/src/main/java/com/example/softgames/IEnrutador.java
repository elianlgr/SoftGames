package com.example.softgames;

public interface IEnrutador<E extends IEvento> {
    void manejar(E evento);
}
