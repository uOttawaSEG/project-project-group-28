package csi2105.group28.otams;

public class Student extends User {

    private String programOfStudy;

    /*
     * Constructor for the Student class.
     * @param firstName is the first name of the student
     * @param lastName is the last name of the student
     * @param email is the email of the student
     * @param password is the password of the student
     * @param phoneNum is the phone number of the student
     * @param programOfStudy is the program of study for the student
     */

    /**
     * Getter for the program of study.
     * @return the program of study of the student
     */
    public String getProgramOfStudy() {
        return programOfStudy;
    }

    /**
     * Setter for the program of study.
     * @param programOfStudy is the new program of study for the student
     */
    public void setProgramOfStudy(String programOfStudy) {
        this.programOfStudy = programOfStudy;
    }
}
