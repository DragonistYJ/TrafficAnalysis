package bean;

import java.io.Serializable;
import java.util.Objects;

public class Signaling implements Serializable {
    private String _id;
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

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Signaling signaling = (Signaling) o;
        return timestamp == signaling.timestamp &&
                Objects.equals(imsi, signaling.imsi) &&
                Objects.equals(lac, signaling.lac) &&
                Objects.equals(cell, signaling.cell) &&
                Objects.equals(phone, signaling.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, imsi, lac, cell, phone);
    }

    @Override
    public String toString() {
        return "Signaling{" +
                "_id='" + _id + '\'' +
                ", timestamp=" + timestamp +
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
