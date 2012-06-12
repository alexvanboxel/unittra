package org.unittra;

public @interface Reference {
    String system();
    String[] id();
    String description() default "";
}
