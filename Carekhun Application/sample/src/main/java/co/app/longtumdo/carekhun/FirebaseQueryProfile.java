package co.app.longtumdo.carekhun;

/**
 * Created by TOPPEE on 8/19/2017.
 */

public class FirebaseQueryProfile {

    public String name;                 //ชื่อ
    public String myname;               //ชื่อจริง
    public String surname;              //นามสกุล
    public String address;              //ที่อยู่
    public String tel;                  //เบอร์โทร
    public String emailAddress;         //อีเมลล์
    public String takeCareType;         //คนพิการ , ผู้สูงอายุ , อัมพาต
    public String timeFallDetection;    //เวลาในการล้ม

    public FirebaseQueryProfile() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMyname() {
        return myname;
    }

    public void setMyname(String myname) {
        this.myname = myname;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getTakeCareType() {
        return takeCareType;
    }

    public void setTakeCareType(String takeCareType) {
        this.takeCareType = takeCareType;
    }

    public String getTimeFallDetection() {
        return timeFallDetection;
    }

    public void setTimeFallDetection(String timeFallDetection) {
        this.timeFallDetection = timeFallDetection;
    }
}

