package androidx.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
public @interface NonNull {}
