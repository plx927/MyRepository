package com.panlingxiao.spring.validation;

import com.panlingxiao.spring.validation.domain.Address;
import com.panlingxiao.spring.validation.domain.Customer;
import com.panlingxiao.spring.validation.domain.Person;
import com.panlingxiao.spring.validation.validation.AddressValidator;
import com.panlingxiao.spring.validation.validation.CustomerValidator;
import com.panlingxiao.spring.validation.validation.PersonValidator;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.AssertionErrors;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.List;

/**
 * Created by panlingxiao on 2016/5/27.
 */
public class TestValidation {

    @Test
    public void testPersonValidator(){
        PersonValidator personValidator = new PersonValidator();
        Person person = new Person();
        person.setAge(-100);
        if( personValidator.supports(Person.class)){
            DataBinder dataBinder = new DataBinder(person);
            Errors errors = dataBinder.getBindingResult();
            personValidator.validate(person, errors);
            Assert.assertEquals(true, errors.hasErrors());
            Assert.assertEquals(2,errors.getFieldErrorCount());
            Assert.assertEquals(2, errors.getErrorCount());
            List<FieldError> fieldErrors = errors.getFieldErrors();
            for(FieldError fieldError : fieldErrors){
                System.out.println("field:"+fieldError.getField()+",rejectedValue:"+fieldError.getRejectedValue());
            }
        }else{
            AssertionErrors.fail("The class is not support");
        }
    }

    @Test
    public void testCustomerValidator(){
        CustomerValidator customerValidator = new CustomerValidator(new AddressValidator());
        Customer customer = new Customer();
        customer.setAddress(new Address());
        DataBinder dataBinder = new DataBinder(customer);
        BindingResult errors = dataBinder.getBindingResult();
        customerValidator.validate(customer, errors);
        //errors.getFieldValue("abc");

    }




}
