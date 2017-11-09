package com.panlingxiao.spring.validation.domain;

import java.beans.PropertyEditorSupport;

/**
 * Created by panlingxiao on 2016/5/29.
 */
public class FooEditor extends PropertyEditorSupport {
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        String[] splits = text.split(";");
        Foo foo = new Foo();
        foo.setX(Integer.parseInt(splits[0]));
        foo.setY(Integer.parseInt(splits[1]));
        setValue(foo);
    }
}
