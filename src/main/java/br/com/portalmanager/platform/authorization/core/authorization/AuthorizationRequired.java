package br.com.portalmanager.platform.authorization.core.authorization;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthorizationRequired {

    AuthorizationPolicy policy() default AuthorizationPolicy.OPEN;

}