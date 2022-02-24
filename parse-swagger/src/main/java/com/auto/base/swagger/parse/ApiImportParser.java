package com.auto.base.swagger.parse;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;
import java.io.InputStream;

public interface ApiImportParser<T> {
    T parse(InputStream source, ApiTestImportRequest request) throws IOException, InvalidFormatException, Exception;
}
