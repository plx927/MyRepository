package com.panlingxiao.spring.validation.editor;

import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by panlingxiao on 2016/6/1.
 */
public class MyDateEditor extends PropertyEditorSupport {

    private String pattern;
    private DateFormat dateFormat;

    public MyDateEditor(String pattern) {
        dateFormat = new SimpleDateFormat(pattern);
        System.out.println("MyDateEditor createted!");
    }



    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null) {
            setValue(text);
        } else {
            try {
                Date date = dateFormat.parse(text);
                setValue(date);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
