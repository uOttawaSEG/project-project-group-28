package csi2105.group28.otams;

import java.util.ArrayList;
import java.util.List;

public class Tutor extends User {

    private String highestDegree;
    private ArrayList<String> coursesOffered;
    private List<AppointmentSlots> totalappointmentslots; // not implemented yet
    private List<BookedAppointments> sessions; // not implemented yet

    // Empty constructor
    public Tutor() {
        super();
        setStatus("pending"); // default new tutor status
        this.coursesOffered = new ArrayList<>();
        this.totalappointmentslots = new ArrayList<>();
        this.sessions = new ArrayList<>();
    }

    /**
     * Constructor for the Tutor class.
     */
    public Tutor(String firstName, String lastName, String email, String password, String phoneNum, String highestDegree, ArrayList<String> coursesOffered) {
        super("Tutor", firstName, lastName, email, password, phoneNum);
        this.highestDegree = highestDegree;
        this.coursesOffered = coursesOffered;
        this.totalappointmentslots = new ArrayList<>();
        this.sessions = new ArrayList<>();
        setStatus("pending"); // default new tutor status
    }

    public String getHighestDegree() {
        return highestDegree;
    }

    public void setHighestDegree(String highestDegree) {
        this.highestDegree = highestDegree;
    }

    public ArrayList<String> getCoursesOffered() {
        return coursesOffered;
    }

    public void setCoursesOffered(ArrayList<String> coursesOffered) {
        this.coursesOffered = coursesOffered;
    }

    public void addCourse(String course) {
        this.coursesOffered.add(course);
    }

    public void removeCourse(String course) {
        this.coursesOffered.remove(course);
    }

    public boolean isCourseOffered(String course) {
        return this.coursesOffered.contains(course);
    }
}
