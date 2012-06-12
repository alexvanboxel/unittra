package org.unittra.factory;

public interface ObjectFactory {
    <T> T instanceOf(Class<T> c);
}
