package csi2105.group28.otams;

import java.io.Serializable;

public class Student extends User {

    private String programOfStudy;

    // Empty constructor for Firebase or serialization
    public Student() {
        super(); // Calls empty constructor of User
        setStatus("pending"); // default new student status
    }

    /**
     * Regular constructor
     */
    public Student(String firstName, String lastName, String email, String password, String phoneNum, String programOfStudy) {
        super("Student", firstName, lastName, email, password, phoneNum);
        this.programOfStudy = programOfStudy;
        setStatus("pending"); // default new student status
    }

    public String getProgramOfStudy() {
        return programOfStudy;
    }

    public void setProgramOfStudy(String programOfStudy) {
        this.programOfStudy = programOfStudy;
    }
}
