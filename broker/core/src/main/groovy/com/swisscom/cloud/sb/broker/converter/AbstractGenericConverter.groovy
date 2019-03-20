/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.converter

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
