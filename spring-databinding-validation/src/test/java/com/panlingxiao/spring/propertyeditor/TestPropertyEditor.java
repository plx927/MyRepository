package com.panlingxiao.spring.propertyeditor;

import com.panlingxiao.spring.validation.domain.Boo;
import com.panlingxiao.spring.validation.domain.Circle;
import com.panlingxiao.spring.validation.domain.Foo;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by panlingxiao on 2016/6/1.
 */
public class TestPropertyEditor {

    /**
     * 源码分析{@link org.springframework.beans.factory.config.CustomEditorConfigurer}工作机制
     */
    @Test
    public void testCustomEditorConfigurer() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("prop-editor-app-context.xml");
        Circle circle = applicationContext.getBean("circle", Circle.class);
        System.out.println(circle.getPoint());
    }

    /**
     * 通过约定机制来自动查询PropertyEditor完成类型转换
     */
    @Test
    public void testPropertyEditorByConvention(){
        ApplicationContext ctx = new ClassPathXmlApplicationContext("prop-convention-app-context.xml");
        Boo boo = ctx.getBean("boo", Boo.class);
        System.out.println("foo.x: "+boo.getFoo().getX()+",foo.y: "+boo.getFoo().getY());
    }


}
