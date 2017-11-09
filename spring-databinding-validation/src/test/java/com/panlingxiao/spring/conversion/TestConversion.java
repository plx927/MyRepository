package com.panlingxiao.spring.conversion;

import com.panlingxiao.spring.validation.domain.Circle;
import com.panlingxiao.spring.validation.domain.Person;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;

import javax.persistence.Id;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Created by panlingxiao on 2016/5/30.
 */
public class TestConversion {

    /**
     * 测试自定义Converter
     */
    @Test
    public void testConveter(){
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("conv-service-app-context.xml");
        Circle circle = ctx.getBean("circle", Circle.class);
        Assert.assertEquals(1,circle.getPoint().getX());
        Assert.assertEquals(2,circle.getPoint().getY());
    }



    @Test
    public void testConversionFactory() {

    }

    /**
     * ApplicationContext会自动寻找name为
     * {@see org.springframework.context.ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME}
     * 的ConversionService的实现。
     */
    @Test
    public void testDefaultConversionService() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("conversion-test.xml");
    }

    /**
     * 将一个List装换成Array
     */
    @Test
    public void testConvertListToArray() {
        List<Integer> input = Arrays.asList(1, 2, 3);
        ConversionService cs = new DefaultConversionService();
        List<String> output = (List<String>) cs.convert(input,
                TypeDescriptor.forObject(input), // List<Integer> type descriptor
                TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(String.class)));
        Assert.assertEquals(3, output.size());
        Assert.assertEquals("1", output.get(0));
        Assert.assertEquals("2", output.get(1));
        Assert.assertEquals("3", output.get(2));
    }

    @Test
    public void testConvertArrayToCollection() {
        String[] input = {"1", "2", "3"};
        DefaultConversionService cs = new DefaultConversionService();
        Object output = cs.convert(input, TypeDescriptor.valueOf(String[].class),
                TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(Integer.class)));
        Assert.assertEquals(true, List.class.isAssignableFrom(output.getClass()));
        Assert.assertEquals(1, output.getClass().getTypeParameters().length);
        List<Integer> ids = (List<Integer>) output;
        Assert.assertArrayEquals(new Integer[]{1, 2, 3}, ids.toArray());
    }

    /**
     * 将字符串装成List<Person>
     */
    @Test
    public void testConvertArrayToPersons() {
        DefaultConversionService cs = new DefaultConversionService();
        cs.addConverter(String.class, Person.class, new MyStringToPersonConverter());
        String[] input = {"1", "2", "3"};
        List<Person> persons = (List<Person>) cs.convert(input, TypeDescriptor.valueOf(String[].class),
                TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(Person.class)));
        Assert.assertEquals(1, persons.get(0).getId().intValue());
        Assert.assertEquals(2, persons.get(1).getId().intValue());
        Assert.assertEquals(3, persons.get(2).getId().intValue());
    }

    @Test
    public void testConvertArrayToPersons2() {
        DefaultConversionService cs = new DefaultConversionService();
        cs.addConverter(new MyStringToEntityConverter());
        String[] input = {"1", "2", "3"};
        List<Person> persons = (List<Person>) cs.convert(input, TypeDescriptor.valueOf(String[].class),
                TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(Person.class)));
        System.out.println(persons);
    }


    static class MyStringToPersonConverter implements Converter<String, Person> {
        @Override
        public Person convert(String source) {
            Integer id = Integer.parseInt(source);
            Person person = new Person();
            person.setId(id);
            return person;
        }
    }

    @Test
    public void testDefaultFormattingConversionService() {
        DefaultFormattingConversionService dc = new DefaultFormattingConversionService();
        Assert.assertEquals(true, dc.canConvert(TypeDescriptor.valueOf(String.class), TypeDescriptor.valueOf(Date.class)));
        dc.convert("1989-09-27", TypeDescriptor.valueOf(String.class), TypeDescriptor.valueOf(Date.class));
    }

    static class MyStringToEntityConverter implements ConditionalGenericConverter {

        @Override
        public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
            return sourceType.getObjectType() == String.class && targetType.getObjectType() == Person.class;
        }


        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return Collections.singleton(new ConvertiblePair(String.class, Person.class));
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            Integer id = Integer.parseInt(source.toString());
            Annotation[] annotations = targetType.getAnnotations();
            if (null != annotations) {
                for (Annotation annotation : annotations) {
                    if (annotation.annotationType() == Id.class) {
                        Person person = new Person();
                        person.setId(id);
                        return person;
                    }
                }
            }
            return null;
        }


    }


}
