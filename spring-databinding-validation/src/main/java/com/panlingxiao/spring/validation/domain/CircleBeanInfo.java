package com.panlingxiao.spring.validation.domain;

import com.panlingxiao.spring.validation.editor.PointEditor;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.SimpleBeanInfo;
import java.lang.reflect.Method;

/**
 * Created by panlingxiao on 2016/5/29.
 */
public class CircleBeanInfo extends SimpleBeanInfo{

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        final PointEditor pointEditor = new PointEditor();
        try {
            PropertyDescriptor pointDescriptor = new PropertyDescriptor("point", Circle.class,"getPoint","setPoint") {
                public PropertyEditor createPropertyEditor(Object bean) {
                    Circle circle = (Circle) bean;
                    circle.setPoint(new Point());
                    return pointEditor;
                }
            };
            return new PropertyDescriptor[]{pointDescriptor};
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }

    }
}
