package csi2105.group28.otams;

import java.util.ArrayList;
import java.util.List;

public class Tutor extends User {

    private String highestDegree;
    private ArrayList<String> coursesOffered;
    private List<TutorAvailabilityActivity> totalappointmentslots; // not implemented yet
    private List<BookedAppointments> sessions; // not implemented yet

    private Integer numberOfRatings;
    private Double rating;

    // Empty constructor
    public Tutor() {
        super();
        setStatus("pending"); // default new tutor status
        this.coursesOffered = new ArrayList<>();
        this.totalappointmentslots = new ArrayList<>();
        this.sessions = new ArrayList<>();
        numberOfRatings=0;
        rating=0.0;
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

    public void setRating(double rating){
        this.rating=rating;
    }
    public void setNumberOfRatings(int numberOfRatings){
        this.numberOfRatings=numberOfRatings;
    }

    public double getRating(){
        return rating;
    }

    public int getNumberOfRatings() {
        return numberOfRatings;
    }

    public void addRating(int rating){
        this.rating=((this.rating*numberOfRatings)+rating)/(numberOfRatings+1);
        numberOfRatings++;
    }
}
