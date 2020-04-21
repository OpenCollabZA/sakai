/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wits.elsi.user;

/**
 *
 * @author davidwaf
 */
public class Unit {

    private String areaCode;
    private String studentNumber;
    private String unitDescription;
    private String courseCode;
    private String facultyCode;
    private String locationCode;
    private String area;
    private String academicSession;
    private String yearOfStudy;
    private String faculty;
    private String teachingPeriod;
    private String unitClass;
    private String unitCode;

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getUnitDescription() {
        return unitDescription;
    }

    public void setUnitDescription(String unitDescription) {
        this.unitDescription = unitDescription;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getFacultyCode() {
        return facultyCode;
    }

    public void setFacultyCode(String facultyCode) {
        this.facultyCode = facultyCode;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getAcademicSession() {
        return academicSession;
    }

    public void setAcademicSession(String academicSession) {
        this.academicSession = academicSession;
    }

    public String getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(String yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getTeachingPeriod() {
        return teachingPeriod;
    }

    public void setTeachingPeriod(String teachingPeriod) {
        this.teachingPeriod = teachingPeriod;
    }

    public String getUnitClass() {
        return unitClass;
    }

    public void setUnitClass(String unitClass) {
        this.unitClass = unitClass;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    @Override
    public String toString() {
        return "Unit{" + "areaCode=" + areaCode + ", studentNumber=" + studentNumber + ", unitDescription=" + unitDescription + ", courseCode=" + courseCode + ", facultyCode=" + facultyCode + ", locationCode=" + locationCode + ", area=" + area + ", academicSession=" + academicSession + ", yearOfStudy=" + yearOfStudy + ", faculty=" + faculty + ", teachingPeriod=" + teachingPeriod + ", unitClass=" + unitClass + ", unitCode=" + unitCode + '}';
    }
}
