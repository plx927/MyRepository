package com.panlingxiao.spring.validation.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by panlingxiao on 2016/5/27.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    private String code;
    private String street;
}
