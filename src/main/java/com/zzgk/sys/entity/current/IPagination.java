package com.zzgk.sys.entity.current;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页类
 * @param <T>
 */
public class IPagination<T>{
    //当前页数
    private int pager;
    //总页数
    private int pages;
    //每页条数
    private int size;
    //总条数
    private int total;
    private List<T> list=new ArrayList<>();

    public IPagination(){

    }

    public IPagination(int pager,int size) {
        if(pager>=1&&size>=1) {
            this.pager=pager;
            this.size=size;
        }
    }

    public static IPagination create(int pager,int size) {
        return new IPagination(pager,size);
    }

    public int getPager() {
        return pager == 0 ? 1 : pager;
    }

    public void setPager(int pager) {
        this.pager = pager;
    }

    public int getPages() {
        return Double.valueOf(Math.ceil((double) total / (double) size)).intValue();
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
