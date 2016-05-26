package com.panlingxiao.spring.validation.validation;

import com.panlingxiao.spring.validation.domain.Address;
import com.panlingxiao.spring.validation.domain.Customer;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Created by panlingxiao on 2016/5/27.
 */
public class CustomerValidator implements Validator {

    private final AddressValidator addressValidator;

    public CustomerValidator(AddressValidator addressValidator) {
        if (addressValidator == null) {
            throw new IllegalArgumentException("The supplied [Validator] is " +
                    "required and must not be null.");
        }
        if (!addressValidator.supports(Address.class)) {
            throw new IllegalArgumentException("The supplied [Validator] must  support the validation of [Address] instances.");
        }
        this.addressValidator = addressValidator;
    }




    /**
     * This Validator validates Customer instances, and any subclasses of Customer too
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return Customer.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "field.required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "surname", "field.required");
        Customer customer = (Customer) target;
        //源码分析这里为什么要设置NestedPath
        try {
            errors.pushNestedPath("address");
            ValidationUtils.invokeValidator(this.addressValidator, customer.getAddress(), errors);
        } finally {
            errors.popNestedPath();
        }
    }
}
