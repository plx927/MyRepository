package com.panlingxiao.spring.bean;

import com.panlingxiao.spring.validation.domain.Address;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.convert.TypeDescriptor;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by panlingxiao on 2016/5/30.
 */
public class TestTypeDescriptor {

    @Test
    public void testTypeDescriptor1() throws NoSuchFieldException {

        TypeDescriptor td = TypeDescriptor.valueOf(Address.class);
        Assert.assertEquals(Address.class,td.getObjectType());

        Field field1 = TestBean.class.getDeclaredField("strs");
        Field field2 = TestBean.class.getDeclaredField("list");


        TypeDescriptor td1 = new TypeDescriptor(field1);
        TypeDescriptor td2 = new TypeDescriptor(field2);


        Assert.assertEquals(String.class,td1.getElementTypeDescriptor().getObjectType());
        Assert.assertEquals(null, td2.getElementTypeDescriptor());






    }


    @Data
    static class TestBean{
        private List<String> strs;
        private List list;
    }


}
