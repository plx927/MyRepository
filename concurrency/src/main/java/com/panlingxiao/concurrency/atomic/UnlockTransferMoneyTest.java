package com.panlingxiao.concurrency.atomic;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * Created by panlingxiao on 2016/5/6.
 *
 */
public class UnlockTransferMoneyTest {

    static  class TransferAccountService{

        public void transfer(Account src,Account dest,long money){
            final long balance = src.getBlance();
            if(balance>=money){
                if(src.decrMoney(balance,balance-money)){
                    dest.incrMoney(money);
                }else{
                    transfer(src,dest,money);
                }
            }else{
                System.out.println(String.format("%s账号余额不足，余额为:%d",src.getId(),src.getBlance()));
            }
        }
    }

    static  class Account{

        private  volatile long balance = 0L;
        private AtomicLongFieldUpdater blanceUpdater =  AtomicLongFieldUpdater.newUpdater(Account.class,"balance");
        private String id;

        public Account(String id,long balance){
            this.id = id;
            this.balance = balance;
        }

        //返回当前账户的余额
        public long getBlance(){
            return balance;
        }

        public boolean decrMoney(long expect, long update){
            return blanceUpdater.compareAndSet(this,expect,update);
        }

        public long incrMoney(long money) {
            return blanceUpdater.addAndGet(this, money);
        }

        public String getId() {
            return id;
        }
    }


    public static void main(String[] args) {
        final Account a1 = new Account("A",100);
        final Account a2 = new Account("B",200);
        final Account a3 = new Account("C",300);

        final TransferAccountService transferAccountService = new TransferAccountService();


        new Thread(){
            @Override
            public void run() {
                transferAccountService.transfer(a1,a2,80);
            }
        }.start();

        new Thread(){
            @Override
            public void run() {
                transferAccountService.transfer(a1,a3,80);
            }
        }.start();

    }
}
