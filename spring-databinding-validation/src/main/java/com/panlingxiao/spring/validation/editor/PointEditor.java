package com.panlingxiao.spring.validation.editor;


import com.panlingxiao.spring.validation.domain.Point;

        import java.beans.PropertyEditorSupport;

/**
 * 自定义PropertyEditor,完成String到Point的转换
 */
public class PointEditor extends PropertyEditorSupport {

    public PointEditor(){
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        String[] splits = text.split(";");
        Point point = new Point();
        point.setX(Integer.parseInt(splits[0]));
        point.setY(Integer.parseInt(splits[1]));
        /*
         *需要将装换后的结果设置到Editor的value属性中，因为外部会通过getValue获取到转换的结果。
         */
        setValue(point);
    }
}