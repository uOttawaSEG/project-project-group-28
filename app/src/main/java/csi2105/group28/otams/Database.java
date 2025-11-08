package csi2105.group28.otams;
import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Database {
    private DatabaseReference otamsroot;
    private ArrayList<String> requestsS, rejectedS, requestsT, rejectedT;
    private ArrayList<User> requestsSU, rejectedSU, requestsTU, rejectedTU;
    private ArrayList<AppointmentSlots> booked, available;

    public Database() {
        //initializing list for request and rejected users
        requestsS = new ArrayList<>();
        rejectedS = new ArrayList<>();
        requestsT = new ArrayList<>();
        rejectedT = new ArrayList<>();
        requestsSU = new ArrayList<>();
        rejectedSU = new ArrayList<>();
        requestsTU = new ArrayList<>();
        rejectedTU = new ArrayList<>();
        booked = new ArrayList<>();
        available = new ArrayList<>();

        //set the reference of the firebase to the users(where students and tutor info are stored)
        otamsroot = FirebaseDatabase.getInstance().getReference("otams");
    }

    public void listenToReq(){
        otamsroot.child("Administrator").child("admin@mail@com").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestsS.clear();
                rejectedS.clear();
                requestsT.clear();
                rejectedT.clear();
                for (DataSnapshot children : snapshot.getChildren()) {

                    if (children.getKey().equals("Requests")) { // looking through requests
                        if (children.hasChildren()) {
                            for (DataSnapshot req : children.getChildren()) {
                                if (req.getKey().equals("Tutor")) {                   //finding tutors,if they exists and getting their username
                                    if (req.hasChildren()) {
                                        for (DataSnapshot reqTutor : req.getChildren()) {
                                            Tutor obj = reqTutor.getValue(Tutor.class);
                                            requestsTU.add(obj);
                                        }
                                    }
                                } else if (req.getKey().equals("Student")) {//finding Students,if they exists and getting their username
                                    if (req.hasChildren()) {
                                        for (DataSnapshot reqStudent : req.getChildren()) {
                                            Student obj = reqStudent.getValue(Student.class);
                                            requestsSU.add(obj);
                                        }
                                    }
                                }
                            }
                        }
                    } else if (children.getKey().equals("Rejected")) {// looking through rejected requests
                        if (children.hasChildren()) {
                            for (DataSnapshot rej : children.getChildren()) {
                                if (rej.getKey().equals("Tutor")) {//finding tutors,if they exists and getting their username
                                    if (rej.hasChildren()) {
                                        for (DataSnapshot rejTutor : rej.getChildren()) {
                                            Tutor obj = rejTutor.getValue(Tutor.class);
                                            rejectedTU.add(obj);
                                        }
                                    }
                                } else if (rej.getKey().equals("Student")) {//finding Students,if they exists and getting their username
                                    if (rej.hasChildren()) {
                                        for (DataSnapshot rejStudent : rej.getChildren()) {
                                            Student obj = rejStudent.getValue(Student.class);
                                            rejectedSU.add(obj);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void listenToReqNames(){
        otamsroot.child("Administrator").child("admin@mail@com").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestsS.clear();
                rejectedS.clear();
                requestsT.clear();
                rejectedT.clear();
                for (DataSnapshot children : snapshot.getChildren()) {

                    if (children.getKey().equals("Requests")) { // looking through requests
                        if (children.hasChildren()) {
                            for (DataSnapshot req : children.getChildren()) {
                                if (req.getKey().equals("Tutor")) {                   //finding tutors,if they exists and getting their username
                                    if (req.hasChildren()) {
                                        for (DataSnapshot reqTutor : req.getChildren()) {
                                            String obj = reqTutor.getKey();
                                            requestsT.add(obj);
                                        }
                                    }
                                } else if (req.getKey().equals("Student")) {//finding Students,if they exists and getting their username
                                    if (req.hasChildren()) {
                                        for (DataSnapshot reqStudent : req.getChildren()) {
                                            String obj = reqStudent.getKey();
                                            requestsS.add(obj);
                                        }
                                    }
                                }
                            }
                        }
                    } else if (children.getKey().equals("Rejected")) {// looking through rejected requests
                        if (children.hasChildren()) {
                            for (DataSnapshot rej : children.getChildren()) {
                                if (rej.getKey().equals("Tutor")) {//finding tutors,if they exists and getting their username
                                    if (rej.hasChildren()) {
                                        for (DataSnapshot rejTutor : rej.getChildren()) {
                                            String obj = rejTutor.getKey();
                                            rejectedT.add(obj);
                                        }
                                    }
                                } else if (rej.getKey().equals("Student")) {//finding Students,if they exists and getting their username
                                    if (rej.hasChildren()) {
                                        for (DataSnapshot rejStudent : rej.getChildren()) {
                                            String obj = rejStudent.getKey();
                                            rejectedS.add(obj);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void listenToTutorSlots(String tutorUname){
        otamsroot.child("Slots").child(tutorUname).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                booked.clear();
                available.clear();
                for (DataSnapshot children : snapshot.getChildren()) {

                    if (children.getKey().equals("Booked")) { // looking through requests
                        if (children.hasChildren()) {
                            for (DataSnapshot bookedSlots : children.getChildren()) {
                                AppointmentSlots obj = bookedSlots.getValue(AppointmentSlots.class);
                                booked.add(obj);
                            }
                        }
                    } else if (children.getKey().equals("Available")) {// looking through rejected requests
                        if (children.hasChildren()) {

                            for (DataSnapshot availableSlots : children.getChildren()) {
                                AppointmentSlots obj = availableSlots.getValue(AppointmentSlots.class);
                                available.add(obj);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void listenToAllSlots(String tutorUname){
        otamsroot.child("Slots").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                available.clear();
                booked.clear();
                for (DataSnapshot children : snapshot.getChildren()) {
                    if (children.hasChildren()) {
                        for (DataSnapshot slots : children.getChildren()) {
                            if (slots.getKey().equals("Available")) {// looking through rejected requests
                                if (slots.hasChildren()) {
                                    for (DataSnapshot availableSlots : slots.getChildren()) {
                                        AppointmentSlots obj = availableSlots.getValue(AppointmentSlots.class);
                                        available.add(obj);
                                    }
                                }
                            }else if (slots.getKey().equals("Booked")) {// looking through rejected requests
                                if (slots.hasChildren()) {
                                    for (DataSnapshot bookedSlots : slots.getChildren()) {
                                        AppointmentSlots obj =bookedSlots.getValue(AppointmentSlots.class);
                                        booked.add(obj);
                                    }
                                }
                            }
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    public ArrayList<User> getRequests(){
        ArrayList<User> req = new ArrayList<User>();
        req.addAll(requestsSU);
        req.addAll(requestsTU);
        return req;
    }

    public ArrayList<User> getRejected(){
        ArrayList<User> rej = new ArrayList<User>();
        rej.addAll(rejectedSU);
        rej.addAll(rejectedTU);
        return rej;
    }

    public ArrayList<AppointmentSlots> getBooked(){
        return booked;
    }
    public ArrayList<AppointmentSlots> getBooked(String uname){
        ArrayList<AppointmentSlots> bookedS = new ArrayList<AppointmentSlots>();
        for(AppointmentSlots myslots: booked){
            if(myslots.getStudentUname().equals(uname)) {
                bookedS.add(myslots);
            }
        }
        return bookedS;
    }
    public ArrayList<AppointmentSlots> getAvailable(){
        return available;
    }

    public void addValue(String reference,Object obj){
        DatabaseReference ref =FirebaseDatabase.getInstance().getReference(reference);
        ref.setValue(obj);
    }
    public void pushValue(String reference,Object obj){
        DatabaseReference ref =FirebaseDatabase.getInstance().getReference(reference);
        ref.push().setValue(obj);
    }

    public void moveValue(String reference, String destination, User user){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(reference);
        DatabaseReference dest = FirebaseDatabase.getInstance().getReference(destination);
        dest.setValue(user);
        ref.removeValue();
    }

    /*
     * Checks if the user is has yet to be approved or was rejected by the admin
     * @param usermail(String) is the email of the user
     * @param requestmsg(TextView) is the view to show the user a message
     * @return found(Boolean) that is true if the user yet to be approved or was rejected
     */
    public int isPendingOrRejected(String usermail, String usertype) {
        String username = usermail.replace(".", "@");
        int found = 0;
        if (usertype.equals("Student")) {
            for (String request : requestsS) {
                if (request.equals(username)) {
                    found = 1;
                    break;
                }
            }
            for (String rejects : rejectedS) {
                if (rejects.equals(username)) {
                    found = 2;
                    break;
                }
            }
        } else if (usertype.equals("Tutor")) {
            for (String request : requestsT) {
                if (request.equals(username)) {
                    found = 1;
                    break;
                }
            }
            for (String rejects : rejectedT) {
                if (rejects.equals(username)) {
                    found = 2;
                    break;
                }
            }
        }
        return found;
    }

    // creates a new storage for the user in firebase
}