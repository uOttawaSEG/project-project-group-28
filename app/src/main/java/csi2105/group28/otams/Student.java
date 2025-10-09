package csi2105.group28.otams;

import java.util.ArrayList;

public class Student extends User {

    private String programOfStudy;
    private ArrayList<String> coursesOffered;

    /*
     * Constructor for the Student class.
     * @param firstName is the first name of the student
     * @param lastName is the last name of the student
     * @param email is the email of the student
     * @param password is the password of the student
     * @param phoneNum is the phone number of the student
     * @param programOfStudy is the program of study for the student
     * @param coursesOffered is a list of courses offered by the student
     */
    public Student(String firstName, String lastName, String email, String password, String phoneNum, String programOfStudy, ArrayList<String> coursesOffered) {
        super("Student", firstName, lastName, email, password, phoneNum);
        this.programOfStudy = programOfStudy;
        this.coursesOffered = coursesOffered;
    }

    /*
     * Getter for the program of study.
     * @return the program of study of the student
     */
    public String getProgramOfStudy() {
        return programOfStudy;
    }

    /*
     * Setter for the program of study.
     * @param programOfStudy is the new program of study for the student
     */
    public void setProgramOfStudy(String programOfStudy) {
        this.programOfStudy = programOfStudy;
    }

    /*
     * Getter for the courses offered.
     * @return the list of courses offered by the student
     */
    public ArrayList<String> getCoursesOffered() {
        return coursesOffered;
    }

    /*
     * Setter for the courses offered.
     * @param coursesOffered is the new list of courses offered by the student
     */
    public void setCoursesOffered(ArrayList<String> coursesOffered) {
        this.coursesOffered = coursesOffered;
    }

    /*
     * Method to add a course to the list of courses offered.
     * @param course is the course to be added
     */
    public void addCourse(String course) {
        this.coursesOffered.add(course);
    }
}
