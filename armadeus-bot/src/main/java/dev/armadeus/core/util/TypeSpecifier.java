package dev.armadeus.core.util;


import com.google.gson.reflect.TypeToken;
import groovy.lang.Tuple3;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class TypeSpecifier {

    public static final Type TUPLE_LIST = TypeToken.getParameterized(ArrayList.class, TypeToken.getParameterized(Tuple3.class, String.class, String.class).getType()).getType();

}
