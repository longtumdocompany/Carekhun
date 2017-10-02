package co.app.longtumdo.carekhun;

/**
 * Created by Suttipong.k on 9/25/2017.
 */

public class FirebaseQueryQRCode {

    public String customerId;           //รหัสลูกค้า
    public String message;              //ข้อความ QR Code
    //*******************************************************************
    public String name;                 //ชื่อ
    public String myname;               //ชื่อจริง
    public String surname;              //นามสกุล
    public String address;              //ที่อยู่
    public String tel;                  //เบอร์โทร
    public String emailAddress;         //อีเมลล์
    public String takeCareType;         //คนพิการ , ผู้สูงอายุ , อัมพาต
    public String timeFallDetection;    //เวลาในการล้ม


    public FirebaseQueryQRCode(String customerId, String message, String name, String myname, String surname, String address, String tel, String emailAddress, String takeCareType, String timeFallDetection) {
        this.customerId = customerId;
        this.message = message;
        this.name = name;
        this.myname = myname;
        this.surname = surname;
        this.address = address;
        this.tel = tel;
        this.emailAddress = emailAddress;
        this.takeCareType = takeCareType;
        this.timeFallDetection = timeFallDetection;
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

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
