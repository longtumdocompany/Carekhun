package co.app.longtumdo.carekhun;

/**
 * Created by suttipong on 9/24/2017.
 */

public class FirebaseQueryRepair {

    public String idCustomer;
    public String toEmail;
    public String subjectEmail;
    public String messageEmail;

    public FirebaseQueryRepair(String idCustomer, String toEmail, String subjectEmail, String messageEmail) {
        this.idCustomer = idCustomer;
        this.toEmail = toEmail;
        this.subjectEmail = subjectEmail;
        this.messageEmail = messageEmail;
    }

    public String getIdCustomer() {
        return idCustomer;
    }

    public void setIdCustomer(String idCustomer) {
        this.idCustomer = idCustomer;
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public String getSubjectEmail() {
        return subjectEmail;
    }

    public void setSubjectEmail(String subjectEmail) {
        this.subjectEmail = subjectEmail;
    }

    public String getMessageEmail() {
        return messageEmail;
    }

    public void setMessageEmail(String messageEmail) {
        this.messageEmail = messageEmail;
    }
}
