package com.huntloc.eoc;

/**
 * Created by dmoran on 9/14/2017.
 */

public class Group {
    public String getGroupName() {
        return GroupName;
    }
    public void setGroupName(String groupName) {
        GroupName = groupName;
    }
    public Group(String name, String message) {
        GroupName = name;
        Message = message;
    }
    private String GroupName;
    public String getMessage() {
        return Message;
    }
    public void setMessage(String message) {
        Message = message;
    }
    private String Message;
    @Override
    public String toString() {
        return GroupName;
    }
}
