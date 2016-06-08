package com.panlingxiao.spring.bean;

import com.panlingxiao.spring.validation.domain.Boo;
import com.panlingxiao.spring.validation.domain.Person;
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
        wrapper.setAutoGrowNestedPaths(true);
        wrapper.setPropertyValue("nums[0]", "123");
        wrapper.setPropertyValue("nums[1]", "123");
        System.out.println(person.getNums());
        wrapper.setPropertyValue("boo.foo.x", "1");
        System.out.println(person.getBoo().getFoo().getX());
    }

    @Test
    public void testFindEditorByConvention(){
        Boo boo = new Boo();
        BeanWrapperImpl wrapper = new BeanWrapperImpl(boo);
        wrapper.setPropertyValue("foo","1;2");
        System.out.println(boo.getFoo());
    }


}
