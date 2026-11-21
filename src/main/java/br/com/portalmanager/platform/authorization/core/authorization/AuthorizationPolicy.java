package br.com.portalmanager.platform.authorization.core.authorization;

public enum AuthorizationPolicy {

    OPEN(0),
    DEV(1),
    TST(2),
    ADM(3),

    OPERATION(10),
    AUDIT(20),
    OWNER(30);

    private final int level;
    private static final String ORWER_PORTAL_GROUP = "PM5_ORWER";
    private static final String AUDIT_PORTAL_GROUP = "PM5_AUDIT";
    private static final String OPERATION_PORTAL_GROUP = "PM5_OPERATION";


    AuthorizationPolicy(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }


    public static AuthorizationPolicy resolveEnvironment(String groupName) {
        if (groupName == null) return DEV;

        if (groupName.contains("-ADM_")) return ADM;
        if (groupName.contains("-TST_")) return TST;

        return DEV;
    }

}