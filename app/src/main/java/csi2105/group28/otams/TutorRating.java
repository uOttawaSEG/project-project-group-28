package csi2105.group28.otams;

/*
 *
 * Model class to store tutor ratings from students
 * Stores 5-star rating and optional comment (max 150 words)
 */
public class TutorRating {
    private String studentUsername;
    private String studentName;
    private String tutorUsername;
    private String tutorName;
    private int rating; // 1-5 stars
    private String comment; // max 150 words
    private String sessionKey; // reference to the session that was rated
    private long timestamp; // when the rating was submitted
    private String sessionDate; // date of the session
    private String course; // course that was tutored

    // Empty constructor for Firebase
    public TutorRating() {
    }

    /**
     * Constructor for creating a new tutor rating
     * @param studentUsername username of the student giving the rating
     * @param studentName name of the student
     * @param tutorUsername username of the tutor being rated
     * @param tutorName name of the tutor
     * @param rating star rating (1-5)
     * @param comment optional comment (max 150 words)
     * @param sessionKey unique key for the session
     * @param sessionDate date of the tutoring session
     * @param course course that was tutored
     */
    public TutorRating(String studentUsername, String studentName, String tutorUsername,
                       String tutorName, int rating, String comment, String sessionKey,
                       String sessionDate, String course) {
        this.studentUsername = studentUsername;
        this.studentName = studentName;
        this.tutorUsername = tutorUsername;
        this.tutorName = tutorName;
        this.rating = rating;
        this.comment = comment;
        this.sessionKey = sessionKey;
        this.timestamp = System.currentTimeMillis();
        this.sessionDate = sessionDate;
        this.course = course;
    }

    // Getters and setters
    public String getStudentUsername() {
        return studentUsername;
    }

    public void setStudentUsername(String studentUsername) {
        this.studentUsername = studentUsername;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getTutorUsername() {
        return tutorUsername;
    }

    public void setTutorUsername(String tutorUsername) {
        this.tutorUsername = tutorUsername;
    }

    public String getTutorName() {
        return tutorName;
    }

    public void setTutorName(String tutorName) {
        this.tutorName = tutorName;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating >= 1 && rating <= 5) {
            this.rating = rating;
        }
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(String sessionDate) {
        this.sessionDate = sessionDate;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }
}
