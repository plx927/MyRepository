package com.panlingxiao.spring.databinder;
import com.panlingxiao.spring.validation.editor.PointEditor;
import com.panlingxiao.spring.validation.domain.*;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.*;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.validation.DataBinder;

import java.beans.*;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;

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
        pvs.addPropertyValue(new PropertyValue("age", age));
        pvs.addPropertyValue("nums[1]", "123");
        pvs.add("aa", "bb");
        dataBinder.bind(pvs);
        Assert.assertEquals(name, person.getName());
        Assert.assertEquals(Integer.parseInt(age),person.getAge());
        Assert.assertEquals(2,person.getNums().size());
        Assert.assertEquals(123, person.getNums().get(1).intValue());
        Assert.assertNull(person.getNums().get(0));
    }


    /**
     * 在Spring3.x中，所有的具体实现都由BeanWrapper来完成,在Spring4.2后，新引入了AbstractNestablePropertyAccessor
     *
     */
    @Test
    public void testBindingNestedProperty(){
        Customer customer = new Customer();
        BeanWrapperImpl wrapper = new BeanWrapperImpl(customer);
        wrapper.setPropertyValue("firstName", "hello");
        wrapper.setPropertyValue(new PropertyValue("surname", "world"));
        Assert.assertEquals("hello", customer.getFirstName());
        Assert.assertEquals("world", customer.getSurname());

        //设置自动创建对象属性，否则会引发NullValueInNestedPathException
        wrapper.setAutoGrowNestedPaths(true);
        wrapper.setPropertyValue("address.street", "aa");
        wrapper.setPropertyValue("address.code", "123456");
    }

    @Test
    public void testRegisterPropertyEditorToDataBinder(){
        Person person = new Person();
        DataBinder dataBinder = new DataBinder(person);
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.add("birthday", "2016-05-29");
        dataBinder.bind(pvs);
    }

    /**
     * BeanWrapper默认没有提供PropertyEditor完成String到Data的转换
     * 从这里可以看到，BeanWrapper除了提供对JavaBean正常的访问操作，同时还对PropertyEditor的管理功能。
     *
     */
    @Test
    public void testRegisterPropertyEditorToBeanWrapper(){
        BeanWrapperImpl person = new BeanWrapperImpl(new Person());
        person.registerCustomEditor(Date.class, new CustomDateEditor(DateFormat.getDateInstance(), true));
        person.setPropertyValue("birthday", "2016-05-29");

    }

    @Test
    public void testPropertyEditorManager() throws Exception {
        PropertyEditorManager.registerEditor(Date.class,MyCustomerDatePropertyEditor.class);
        BeanWrapper wrapper = new BeanWrapperImpl(new Person());
        PropertyDescriptor pd = wrapper.getPropertyDescriptor("birthday");
        PropertyEditor propertyEditor = PropertyEditorManager.findEditor(pd.getPropertyType());
        propertyEditor.setAsText("2016-06-01");
        pd.getWriteMethod().invoke(wrapper.getWrappedInstance(), propertyEditor.getValue());
        System.out.println(Arrays.asList(PropertyEditorManager.getEditorSearchPath()));
    }

    /**
     * 测试PropertyEditorManager自动搜索机制
     * Spring只是实现了PropertyEditorManager的查找机制中的第二个规范，
     * 但是没有使用PropertyEditorManager来完成真正的查找任务。
     */
    @Test
    public void testPropertyEditorManagerAutoSerach(){
        BeanWrapper beanWrapper = new BeanWrapperImpl(new Boo());
        beanWrapper.setPropertyValue("foo", "1;2");
        Boo boo = (Boo) beanWrapper.getWrappedInstance();
        Assert.assertEquals(1, boo.getFoo().getX());
        Assert.assertEquals(2, boo.getFoo().getY());
    }

    /**
     * 在PropertyEditorManager中注册对于Spring中无效的。
     */
    @Test
    public void testPropertyEditorManagerAutoSearch2(){
        PropertyEditorManager.registerEditor(Point.class, PointEditor.class);
        BeanWrapper wrapper = new BeanWrapperImpl(new Circle());
        wrapper.setPropertyValue("point", "1;2");
    }


    /**
     * 自定义BeanInfo信息
     */
    @Test
    public void testCustomBeanInfo() throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(Circle.class);
        System.out.println(beanInfo.getClass().getName());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        Assert.assertEquals(1,propertyDescriptors.length);
        Circle circle = new Circle();
        propertyDescriptors[0].createPropertyEditor(circle).setAsText("1;2");
        Assert.assertEquals(1, circle.getPoint().getX());
        Assert.assertEquals(2, circle.getPoint().getY());
    }


    @Test
    public void testCustomPropertyEditor(){
        BeanWrapper wrapper = new BeanWrapperImpl(new Circle());
        PropertyDescriptor pd = wrapper.getPropertyDescriptor("point");
        pd.createPropertyEditor(wrapper.getWrappedInstance());

    }


    public static  class MyCustomerDatePropertyEditor extends CustomDateEditor{

        public MyCustomerDatePropertyEditor(){
            this(DateFormat.getDateInstance(), true);
        }

        public MyCustomerDatePropertyEditor(DateFormat dateFormat, boolean allowEmpty) {
            super(dateFormat, allowEmpty);
        }
    }








}
