package com.panlingxiao.spring.validation.editor;

import com.panlingxiao.spring.validation.domain.Point;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;

/**
 * Created by panlingxiao on 2016/6/2.
 */
public class MyPropertyEditorRegistrar implements PropertyEditorRegistrar {
    @Override
    public void registerCustomEditors(PropertyEditorRegistry registry) {
        //将自己所定义的PropertyEditor注册到PropertyEditorRegistry中
        registry.registerCustomEditor(Point.class,new PointEditor());
    }
}
