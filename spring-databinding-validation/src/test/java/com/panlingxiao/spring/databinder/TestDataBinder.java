package com.panlingxiao.spring.databinder;
import com.panlingxiao.spring.validation.domain.Customer;
import com.panlingxiao.spring.validation.domain.Person;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.*;
import org.springframework.validation.DataBinder;

/**
 * Created by panlingxiao on 2016/5/27.
 */
public class TestDataBinder {


    /**
     * DataBinder完成真正数据绑定的是BeanWrapper
     *
     * The BeanWrapper usually isn’t used by application code directly,
     * but by the DataBinder and the BeanFactory.
     *
     */
    @Test
    public void testBasicBinding(){
        Person person = new Person();
        DataBinder dataBinder = new DataBinder(person);

        //设置对于未知的属性是否进行忽略操作
        //dataBinder.setIgnoreUnknownFields(false);

        MutablePropertyValues pvs = new MutablePropertyValues();
        String name = "Tom";
        String age = "30";
        pvs.addPropertyValue(new PropertyValue("name", name));
        pvs.addPropertyValue(new PropertyValue("age",age));
        pvs.add("aa","bb");
        dataBinder.bind(pvs);
        Assert.assertEquals(name, person.getName());
        Assert.assertEquals(Integer.parseInt(age),person.getAge());

    }

    @Test
    public void testBindingNestedProperty(){
        Customer customer = new Customer();
        BeanWrapperImpl wrapper = new BeanWrapperImpl(customer);
        wrapper.setPropertyValue("firstName","hello");
        wrapper.setPropertyValue(new PropertyValue("surname", "world"));
        Assert.assertEquals("hello", customer.getFirstName());
        Assert.assertEquals("world", customer.getSurname());

        wrapper.setAutoGrowNestedPaths(true);
        wrapper.setPropertyValue("address.street","aa");
        wrapper.setPropertyValue("address.code","123456");
    }







}
