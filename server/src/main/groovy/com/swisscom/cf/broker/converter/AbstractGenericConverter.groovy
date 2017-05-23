package com.swisscom.cf.broker.converter

import java.lang.reflect.ParameterizedType

public abstract class AbstractGenericConverter<S, T> implements Converter<S, T> {

    @Override
    public T convert(S source) {
        final T prototype = instantiatePrototype(source);
        if (prototype != null) {
            convert(source, prototype);
        }
        return prototype;
    }

    protected abstract void convert(final S source, final T prototype);

    @Override
    public List<T> convertAll(Collection<S> source) {
        // reverted from java8 as it did not work in web-context. Tests were passing though.
        final List<T> list = new ArrayList<>(source.size());
        for (S s : source) {
            final T converted = convert(s);
            if (converted != null) {
                list.add(converted);
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    protected T instantiatePrototype(@SuppressWarnings("unused") S source) {
        if (source == null) {
            return null;
        }
        try {
            return (T) ((Class) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[1]).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
