package com.panlingxiao.spring.validation.editor;


import com.panlingxiao.spring.validation.domain.Point;

import java.beans.PropertyEditorSupport;

public class PointEditor extends PropertyEditorSupport {
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        String[] splits = text.split(";");
        Point point = new Point();
        point.setX(Integer.parseInt(splits[0]));
        point.setY(Integer.parseInt(splits[1]));
        setValue(point);
    }
}