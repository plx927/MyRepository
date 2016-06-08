package com.panlingxiao.spring.validation.domain;

import com.panlingxiao.spring.validation.annotation.MyDate;
import lombok.Data;

import java.util.Date;

/**
 * Created by panlingxiao on 2016/6/5.
 */
@Data
public class Contact {

    @MyDate
    private Date date;
}
