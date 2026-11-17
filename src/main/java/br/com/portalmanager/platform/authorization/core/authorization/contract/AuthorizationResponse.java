package br.com.portalmanager.platform.authorization.core.authorization.contract;

public class AuthorizationResponse {

    private boolean allowed;
    private UserContext context;

    public AuthorizationResponse() {
    }

    public AuthorizationResponse(boolean allowed, UserContext context) {
        this.allowed = allowed;
        this.context = context;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public UserContext getContext() {
        return context;
    }

    public void setContext(UserContext context) {
        this.context = context;
    }
}