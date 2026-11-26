package com.shopmate.product.dto;

import com.shopmate.product.model.Category;

public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;


    public static CategoryResponse from (Category c){
        CategoryResponse r=new CategoryResponse();
        r.id=c.getId();
        r.name=c.getName();
        r.slug=c.getSlug();
        return r;

    }
    public  Long getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public String getSlug(){
        return slug;
    }

}
