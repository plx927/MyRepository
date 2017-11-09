package com.panlingxiao.spring.validation.converter;

import com.panlingxiao.spring.validation.annotation.MyDate;
import org.springframework.format.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by panlingxiao on 2016/6/5.
 */
public class MyDateAnnotationFormatterFactory implements AnnotationFormatterFactory<MyDate> {
    private static final Set<Class<?>> FIELD_TYPES;
    static {
        Set<Class<?>> fieldTypes = new HashSet<Class<?>>(4);
        fieldTypes.add(Date.class);
        FIELD_TYPES = Collections.unmodifiableSet(fieldTypes);
    }

    @Override
    public Set<Class<?>> getFieldTypes() {
        return FIELD_TYPES;
    }

    @Override
    public Printer<?> getPrinter(MyDate annotation, Class<?> fieldType) {
        return null;
    }

    @Override
    public Parser<?> getParser(MyDate annotation, Class<?> fieldType) {
        return new MyDateFormatter(annotation.pattern());
    }


    public static class MyDateFormatter implements org.springframework.format.Formatter<Date>{
        private String pattern ;


        public MyDateFormatter(String pattern) {
            this.pattern = pattern;
        }

        @Override
        public Date parse(String text, Locale locale) throws ParseException {
            return new SimpleDateFormat(pattern).parse(text);
        }

        @Override
        public String print(Date object, Locale locale) {
            return null;
        }
    }
}
