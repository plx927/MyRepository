package com.panlingxiao.spring.validation.converter;

import com.panlingxiao.spring.validation.domain.Point;
import org.springframework.core.convert.converter.Converter;

/**
 * Created by panlingxiao on 2016/6/3.
 */
public class PointConverter implements Converter<String,Point> {
    @Override
    public Point convert(String source) {
        String[] splits = source.split(";");
        Point point = new Point();
        point.setX(Integer.parseInt(splits[0]));
        point.setY(Integer.parseInt(splits[1]));
        return point;
    }
}
