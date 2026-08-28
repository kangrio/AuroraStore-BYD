package org.microg.gms.common;

public enum GmsService {
    PO_TOKENS(285, "com.google.android.gms.potokens.service.START");
    public int SERVICE_ID;
    public String ACTION;
    public String[] SECONDARY_ACTIONS;

    GmsService(int serviceId, String... actions) {
    }
}
