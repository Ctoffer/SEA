package de.ctoffer.assistance.context;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Require {
    Context[] value();
}
