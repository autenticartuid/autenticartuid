package com.autenticar.tuid.services.utils;

import com.autenticar.tuid.model.interfaces.UserExclude;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/**
 * Created by mbatto on 30/11/2016.
 */

public class UserExclusionStrategy implements ExclusionStrategy {

    public boolean shouldSkipField(FieldAttributes f) {
        return f.getAnnotation(UserExclude.class) != null;
    }

    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }

}