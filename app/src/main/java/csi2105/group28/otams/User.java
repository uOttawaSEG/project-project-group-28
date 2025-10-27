/* @author: group28
 * CSI2105:intro to software engineering
 * Project
 * Due: Oct 13 2025
 * a class containing all the attributes about the users of an otams
 */

package csi2105.group28.otams;

import java.io.Serializable;

public class User implements Serializable {

    private String email, password, userType, username, firstName, lastName, phoneNum;
    private String status; // new field to track approval: "pending", "approved", "rejected"

    /*
     * Constructor for the User class. creates a firebase username by replacing the '.' in the email with an '@'
     * @param usertype is the type of user: Student or Tutor
     * @param firstName is the first name of the user
     * @param lastName is the last name of the user
     * @param email is the email of the user
     * @param password is their password
     * @param phoneNum is the phone number of the user
     */
    public User(String userType, String firstName, String lastName, String email, String password, String phoneNum) {
        this.userType = userType;
        setEmail(email);
        setPassword(password);
        setFirstName(firstName);
        setLastName(lastName);
        setPhoneNum(phoneNum);
        username = this.email.replace(".", "@"); //creates a firebase username by replacing the '.' in the email with an '@'
        this.status = "pending"; // default status for new users
    }

    //temp constructor until implementation
    public User(String userType, String email, String password) {
        this.userType = userType;
        setEmail(email);
        setPassword(password);
        username = this.email.replace(".", "@"); //creates a firebase username by replacing the '.' in the email with an '@'
        this.status = "pending"; // default status
    }

    /*
     * Empty constructor class for firebase to create a class
     */
    public User() {
        this.status = "pending"; // default status
    }

    /*
     *@return the email of the user
     */
    public String getEmail() {
        return email;
    }

    /*sets the email of the user if it matches specifications
     *@param email (String) is the email of the user
     *@throws IllegalArgumentException if the email is <6, contains multiple '@', contains no '.' after an '@', if it starts with '@'
     */
    private void setEmail(String email) {
        boolean valid = false, first = false;
        if (email.length() < 6) {
            throw new IllegalArgumentException("email");
        }

        for (int i = 0; i < email.length(); i++) {
            if (email.charAt(i) == '@' && !first) {
                if (i == 0) {
                    throw new IllegalArgumentException("email");
                }
                first = true;
            } else if (email.charAt(i) == '@' && first) {
                throw new IllegalArgumentException("email");
            }
            if (first & email.charAt(i) == '.') {
                valid = true;
            }
        }
        if (valid) {
            this.email = email;
        } else {
            throw new IllegalArgumentException("email");
        }
    }

    /*
     *@return the password of the user
     */
    public String getPassword() {
        return password;
    }

    /*sets the password of the user if it matches specifications
     *@param password (String) is the password of the user
     *@throws IllegalArgumentException if the password is <7, amd does not contain a letter and number
     */
    public void setPassword(String password) {
        if(password==null){
            this.password=null;
        }else {
            if (password.length() < 7) {
                throw new IllegalArgumentException("passwordl");
            }
            boolean letter = false, number = false;
            for (int i = 0; i < password.length(); i++) {
                if (!letter && Character.isLetter(password.charAt(i))) {
                    letter = true;
                }
                if (!number && Character.isDigit(password.charAt(i))) {
                    number = true;
                }
            }

            if (letter & number) {
                this.password = password;
            } else {
                throw new IllegalArgumentException("passwordc");
            }
        }
    }

    /*
     *@return the type of the user
     */
    public String getUserType() {
        return userType;
    }

    /* sets the usertype of the use
     *@param userType (String) is the type of the user
     */
    public void setUserType(String userType) {
        this.userType = userType;
    }

    /*
     *@return the firebase username of the user
     */
    public String getUsername() {
        return username;
    }

    /*sets the firebase username of the user
     *@param username (String) is the firebase username of the user.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /*sets the first name of the user if it matches specifications
     *@param firstName (String) is the first name of the user
     *@throws IllegalArgumentException if the name is blank, too long or contains characters beyond A-Z, a-z or -
     */
    private void setFirstName(String firstName) {

        if (firstName.isEmpty() || firstName.length() > 50) {
            throw new IllegalArgumentException("FirstName");
        }

        if (!firstName.matches("[a-zA-Z-]*")) {
            throw new IllegalArgumentException("FirstName");
        }
        this.firstName = firstName;
    }
    /*
     *@return the first name of the user
     */
    public String getFirstName() {
        return firstName;
    }

    /*sets the first name of the user if it matches specifications
     *@param lastName (String) is the first name of the user
     *@throws IllegalArgumentException if the name is blank, too long or contains characters beyond A-Z, a-z or -
     */
    private void setLastName(String lastName) {

        if (lastName.isEmpty() || lastName.length() > 50) {
            throw new IllegalArgumentException("LastName");
        }

        if (!lastName.matches("[a-zA-Z-]*")) {
            throw new IllegalArgumentException("LastName");
        }
        this.lastName = lastName;
    }

    /*
     *@return the last name of the user
     */
    public String getLastName() {
        return lastName;
    }
    /*sets the phone number of the user if it matches specifications
     *@param phoneNum (String) is the phone number of the user
     *@throws IllegalArgumentException if the phone number <10, >14
     */
    private void setPhoneNum(String phoneNum) {
        boolean valid=true;
        if (phoneNum.length()<10 || phoneNum.length()>17){
            throw new IllegalArgumentException("PhoneNum");
        }
        for(int i=0; i<phoneNum.length(); i++){
            if(phoneNum.charAt(i)!='-' && !Character.isDigit(phoneNum.charAt(i))){
                valid=false;
            }
        }

        if (valid){
            this.phoneNum=phoneNum;
        }else{
            throw new IllegalArgumentException("PhoneNum");
        }
    }

    /*
     *@return the phone number of the user
     */
    public String getPhoneNum() {
        return phoneNum;
    }

    /*
     *@return the approval status of the user
     */
    public String getStatus() {
        return status;
    }

    /*
     *@set the approval status of the user
     *@param status (String) can be "pending", "approved", or "rejected"
     */
    public void setStatus(String status) {
        this.status = status;
    }

}
