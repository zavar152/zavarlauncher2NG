package com.zavar.zavarlauncher.auth.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class AuthProfileResponse {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("skins")
    @Expose
    private List<Skin> skins;
    @SerializedName("capes")
    @Expose
    private List<Cape> capes;
    @SerializedName("profileActions")
    @Expose
    private ProfileActions profileActions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Skin> getSkins() {
        return skins;
    }

    public void setSkins(List<Skin> skins) {
        this.skins = skins;
    }

    public List<Cape> getCapes() {
        return capes;
    }

    public void setCapes(List<Cape> capes) {
        this.capes = capes;
    }

    public ProfileActions getProfileActions() {
        return profileActions;
    }

    public void setProfileActions(ProfileActions profileActions) {
        this.profileActions = profileActions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(AuthProfileResponse.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        sb.append("skins");
        sb.append('=');
        sb.append(((this.skins == null)?"<null>":this.skins));
        sb.append(',');
        sb.append("capes");
        sb.append('=');
        sb.append(((this.capes == null)?"<null>":this.capes));
        sb.append(',');
        sb.append("profileActions");
        sb.append('=');
        sb.append(((this.profileActions == null)?"<null>":this.profileActions));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }
}
