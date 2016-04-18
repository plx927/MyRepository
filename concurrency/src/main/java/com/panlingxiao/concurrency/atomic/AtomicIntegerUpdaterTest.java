package com.panlingxiao.concurrency.atomic;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * Created by panlingxiao on 2016/4/18.
 * <p/>
 * 原子化更新对象的Integer属性
 */
public class AtomicIntegerUpdaterTest {

    public static void main(String[] args) {
        Person person = new Person(10);
        System.out.println(person.getAge());

        person.grow();

        System.out.println(person.getAge());
    }


    private static class Person {

        public  Person(int age){
            this.age = age;
        }

        //被更新的属性必须使用volatile关键字修饰
        private volatile  int age;

        AtomicIntegerFieldUpdater<Person> ageUpdater = AtomicIntegerFieldUpdater.newUpdater(Person.class, "age");

        public int getAge(){
            return age;
        }

        public void grow(){
            ageUpdater.addAndGet(this,1);
        }
    }
}
