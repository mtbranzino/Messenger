package com.huntloc.eoc;

/**
 * Created by dmoran on 9/14/2017.
 */

public class Contact {
    private String GroupName;
    private String ContactName;
    private String ContactNumber;

    public String getGroupName() {
        return GroupName;
    }

    public void setGroupName(String groupName) {
        GroupName = groupName;
    }

    public String getContactName() {
        return ContactName;
    }

    public void setContactName(String contactName) {
        ContactName = contactName;
    }

    public String getContactNumber() {
        return ContactNumber;
    }

    public void setContactNumber(String contactNumber) {
        ContactNumber = contactNumber;
    }

    public Contact(String groupName, String contactName, String contactNumber) {
        GroupName = groupName;
        ContactName = contactName;
        ContactNumber = contactNumber;
    }
}
