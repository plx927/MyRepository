package com.panlingxiao.spring.validation.converter;

import org.springframework.beans.TypeMismatchException;
import org.springframework.core.convert.converter.Converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by panlingxiao on 2016/5/31.
 */
public class MyCustomStringToDateConverter implements Converter<String,Date>{

    @Override
    public Date convert(String source) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            return sdf.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
