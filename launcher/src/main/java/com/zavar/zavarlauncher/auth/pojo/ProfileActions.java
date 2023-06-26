package com.zavar.zavarlauncher.auth.pojo;

public class ProfileActions {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ProfileActions.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        if (sb.charAt((sb.length() - 1)) == ',') {
            sb.setCharAt((sb.length() - 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }
}
