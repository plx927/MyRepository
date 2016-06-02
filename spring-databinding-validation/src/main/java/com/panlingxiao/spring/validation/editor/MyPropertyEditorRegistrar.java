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
        registry.registerCustomEditor(Point.class,"point",new PointEditor());
    }
}
