/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wits.elsi.user;

import java.util.List;

/**
 *
 * @author davidwaf
 */
public class StudentUnits {

    private List<Unit> objects;
    private String totalCount;

    public List<Unit> getObjects() {
        return objects;
    }

    public void setObjects(List<Unit> objects) {
        this.objects = objects;
    }

    public String getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(String totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public String toString() {
        return "Student{" + "objects=" + objects + ", totalCount=" + totalCount + '}';
    }
}
