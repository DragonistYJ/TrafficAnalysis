package love.mmjj.server.bean;

import java.io.Serializable;

public class Signaling implements Serializable {
    private long timestamp;
    private String imsi;
    private String lac;
    private String cell;
    private String phone;

    public Signaling(long timestamp, String imsi, String lac, String cell, String phone) {
        this.timestamp = timestamp;
        this.imsi = imsi;
        this.lac = lac;
        this.cell = cell;
        this.phone = phone;
    }

    public Signaling() {
    }

    @Override
    public String toString() {
        return "Signaling{" +
                "timestamp=" + timestamp +
                ", imsi='" + imsi + '\'' +
                ", lac='" + lac + '\'' +
                ", cell='" + cell + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getLac() {
        return lac;
    }

    public void setLac(String lac) {
        this.lac = lac;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
