package com.panlingxiao.spring.bean;

import com.panlingxiao.spring.validation.domain.Boo;
import com.panlingxiao.spring.validation.domain.Foo;
import com.panlingxiao.spring.validation.domain.Person;
import com.panlingxiao.spring.validation.domain.Point;
import com.panlingxiao.spring.validation.editor.PointEditor;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Created by panlingxiao on 2016/6/1.
 */
public class TestBeanWrapper {

    /**
     * 使用BeanWrapperImpl设置基本属性
     */
    @Test
    public void testSetBasicPropertyValue(){
        Person person = new Person();
        BeanWrapperImpl wrapper = new BeanWrapperImpl(person);
        wrapper.setPropertyValue("id", "1");
        Assert.assertEquals(1, person.getId().intValue());

        //设置BeanWrapper自动创建内联属性，否则会引发NullValueInNestedPathException
        wrapper.setAutoGrowNestedPaths(true);
        wrapper.setPropertyValue("nums[0]", "123");
        wrapper.setPropertyValue("nums[1]", "456");
        System.out.println(person.getNums());

        wrapper.setPropertyValue("boo.foo.x", "1");
        System.out.println(person.getBoo().getFoo().getX());

        //注册自定义的PropertyEditor到BeanWrapper中
        wrapper.registerCustomEditor(Point.class,new PointEditor());
        //BeanWrapper通过注册的PropertyEditor完成数据类型的转换
        wrapper.setPropertyValue("point","1;2");

    }


    @Test
    public void testTypeConverter(){
        Person person = new Person();
        BeanWrapperImpl wrapper = new BeanWrapperImpl(person);
        Foo foo = wrapper.convertIfNecessary("1;2",Foo.class);
        Assert.assertEquals(1,foo.getX());
        Assert.assertEquals(2,foo.getY());

    }



    @Test
    public void testFindEditorByConvention(){
        Boo boo = new Boo();
        BeanWrapperImpl wrapper = new BeanWrapperImpl(boo);
        wrapper.setPropertyValue("foo","1;2");
        System.out.println(boo.getFoo());
    }


}
