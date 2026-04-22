package br.com.brunobs.auth.security;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthorizationRequired {

    AuthorizationPolicy policy() default AuthorizationPolicy.OPEN;

}