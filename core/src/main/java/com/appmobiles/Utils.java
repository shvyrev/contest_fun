package com.appmobiles;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

public class Utils {

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> not(Predicate<? super T> target) {
        Objects.requireNonNull(target);
        return (Predicate<T>)target.negate();
    }

    public static String rndUUID(){
        return UUID.randomUUID().toString().replace("-", "");
    }

}
