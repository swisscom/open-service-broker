package com.swisscom.cloud.sb.broker.converter

import groovy.transform.CompileStatic

@CompileStatic
public interface Converter<Source, Prototype> {
    Prototype convert(final Source source);

    List<Prototype> convertAll(final Collection<Source> source);
}
