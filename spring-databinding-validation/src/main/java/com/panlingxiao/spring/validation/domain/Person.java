package com.panlingxiao.spring.validation.domain;

import com.panlingxiao.spring.validation.annotation.MyDate;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Id;
import java.util.Date;
import java.util.List;

/**
 * Created by panlingxiao on 2016/5/26.
 */
@Data
public class Person {
    @Id
    private Integer id;
    private  String name;
    private int age;
    private Date birthday;
    private List<Integer> nums;
    private Boo boo;
    private Point point;
}
