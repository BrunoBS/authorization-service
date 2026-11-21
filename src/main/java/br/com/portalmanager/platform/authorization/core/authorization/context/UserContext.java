package br.com.portalmanager.platform.authorization.core.authorization.context;

public class UserContext {

    private static final ThreadLocal<AuthorizationContext> CONTEXT = new ThreadLocal<>();

    public static void set(AuthorizationContext session) {
        CONTEXT.set(session);
    }

    public static AuthorizationContext get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }


}