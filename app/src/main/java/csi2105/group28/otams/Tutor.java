/* @author: group28
 * CSI2105:intro to software engineering
 * Project
 * Due: Oct 13 2025 (Deliverable 1)
 * a class containing all attributes for the tutors of otams
 * If anything is not clear msg me on Discord: @ShadownSniper
 */

package csi2105.group28.otams; 
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Collections; 



public class Tutor extends User 
{
   private String highestDegree;
   private ArrayList<String> coursesOffered;
   private List<appointmentSlots> totalappointmentslots; // List of appointments/availability for this tutor (not implemented yet as how we handle slots is not implemented yet).
    // A relation between Tutor and appointmentSlots is established, as a tutor can have as many Appointment slots as they want. They can even choose not to have any.
   private List<bookedAppointments> sessions; // List of booked appointments for this tutor.
    // A relation between Tutor and bookedAppointments is established, as a tutor can have as many booked appointments as they want. They can even choose not to have any.


    /**
     * Constructor for the Tutor class.
     * @param firstName is the first name of the tutor
     * @param lastName is the last name of the tutor
     * @param email is the email of the tutor
     * @param password is the password of the tutor
     * @param phoneNum is the phone number of the tutor
     * @param highestDegree is the highest degree obtained by the tutor
     * @param coursesOffered is a list of courses offered by the tutor
     */
    public Tutor(String firstName, String lastName, String email, String password, String phoneNum, String highestDegree, ArrayList<String> coursesOffered) {
         super("Tutor", firstName, lastName, email, password, phoneNum);
         this.highestDegree = highestDegree;
         this.coursesOffered = coursesOffered;
         this.totalappointmentslots = new ArrayList<appointmentSlots>();
         this.sessions = new ArrayList<bookedAppointments>();
    }

    public Tutor()
    {
       super();
        this.coursesOffered = new ArrayList<String>();
        this.totalappointmentslots = new ArrayList<appointmentSlots>();
        this.sessions = new ArrayList<bookedAppointments>();
    }

    /**
     * Getter for the highest degree.
     * @return the highest degree of the tutor
     */

    public String getHighestDegree() 
    {
        return highestDegree;
    }

    /**
     * Setter for the highest degree.
     * @param highestDegree is the new highest degree of the tutor
     */
    public void setHighestDegree(String highestDegree)
    {
        this.highestDegree = highestDegree; // Mostly likely to go unused as highest degree is not likely to change, also not sure if we will allow tutors to change it
    }
    /**
     * Getter for the courses offered.
     * @return the list of courses offered by the tutor
     */ 
    public ArrayList<String> getCoursesOffered()
    {
        return coursesOffered;
    }
    /**
     * Setter for the courses offered.
     * @param coursesOffered is the new list of courses offered by the tutor
     */
    public void setCoursesOffered(ArrayList<String> coursesOffered)
    {
        this.coursesOffered = coursesOffered;
    }   

    /**
     * Method to add a course to the list of courses offered.
     * @param course is the course to be added
     */
    public void addCourse(String course)
    {
        this.coursesOffered.add(course);
    }
    /**
     * Method to remove a course from the list of courses offered.
     * @param course is the course to be removed
     */
    public void removeCourse(String course)
    {
        this.coursesOffered.remove(course);
    }   
    /**
     * Method to check if a course is offered by the tutor.
     * @param course is the course to be checked
     * @return true if the course is offered by the tutor, false otherwise
     */
    public boolean isCourseOffered(String course)
    {
        return this.coursesOffered.contains(course);
    }
}







    

