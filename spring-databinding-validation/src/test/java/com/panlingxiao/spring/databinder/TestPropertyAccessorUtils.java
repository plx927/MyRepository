package com.panlingxiao.spring.databinder;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.PropertyAccessorUtils;

/**
 * Created by panlingxiao on 2016/5/28.
 */
public class TestPropertyAccessorUtils {


    @Test
    public void testGetPropertyName() {
        Assert.assertEquals("name", PropertyAccessorUtils.getPropertyName("name"));
        Assert.assertEquals("person.name", PropertyAccessorUtils.getPropertyName("person.name"));
        Assert.assertEquals("map", PropertyAccessorUtils.getPropertyName("map[abc]"));
        Assert.assertEquals("list", PropertyAccessorUtils.getPropertyName("list[0]"));
    }

    @Test
    public void testGetFirstNestedPropertySeparatorIndex() {
        int index = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex("address.name");
        Assert.assertEquals(7, index);
        index = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex("map[abc.aa].bb");
        Assert.assertEquals(11, index);
    }


}
