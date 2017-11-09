package com.panlingxiao.spring.validation.validation;

import com.panlingxiao.spring.validation.domain.Address;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Created by panlingxiao on 2016/5/27.
 */
public class AddressValidator implements Validator{
    @Override
    public boolean supports(Class<?> clazz) {
        return Address.class.isAssignableFrom(clazz);
    }


    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors,"street","street.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors,"code","code.empty");
    }
}
