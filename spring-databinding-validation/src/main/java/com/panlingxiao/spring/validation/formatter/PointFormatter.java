package com.panlingxiao.spring.validation.formatter;

import com.panlingxiao.spring.validation.domain.Point;
import org.springframework.format.Formatter;

import java.text.ParseException;
import java.util.Locale;

/**
 * Created by panlingxiao on 2016/6/4.
 */
public class PointFormatter implements Formatter<Point>{
    @Override
    public Point parse(String text, Locale locale) throws ParseException {
        Point point = new Point();
        String[] splits = text.split(";");
        point.setX(Integer.parseInt(splits[0]));
        point.setY(Integer.parseInt(splits[1]));
        return point;
    }

    @Override
    public String print(Point object, Locale locale) {
        System.out.println("point is : "+ object);
        return object.toString();
    }
}
