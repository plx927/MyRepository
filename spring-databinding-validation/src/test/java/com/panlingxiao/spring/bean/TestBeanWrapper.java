package com.panlingxiao.spring.bean;

import com.panlingxiao.spring.validation.domain.Person;
import org.junit.Test;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessor;

/**
 * Created by panlingxiao on 2016/6/1.
 */
public class TestBeanWrapper {

    /**
     * 使用BeanWrapperImpl设置基本属性
     */
    @Test
    public void testSetBasicPropertyValue(){
        PropertyAccessor pa = new BeanWrapperImpl(new Person());
        pa.setPropertyValue("id","1");
    }
}
