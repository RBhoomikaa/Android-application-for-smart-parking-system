package com.app.smartparking.model;

import java.io.Serializable;

public class Parking implements Serializable {
    String parkingId;
    String placeId, slotId;
    String vehicleNo, vehicleMobileNo;
    String timeIn, timeOut;
    String status;

    public @interface Status {
        String BOOKED = "Booked";
        String IN = "Park In";
        String OUT = "Park Out";
        String CANCELED = "Canceled";
    }

    public Parking() {
    }

    public Parking(String placeId) {
        this.placeId = placeId;
    }

    public String getParkingId() {
        return parkingId;
    }

    public void setParkingId(String parkingId) {
        this.parkingId = parkingId;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }

    public String getVehicleMobileNo() {
        return vehicleMobileNo;
    }

    public void setVehicleMobileNo(String vehicleMobileNo) {
        this.vehicleMobileNo = vehicleMobileNo;
    }

    public String getTimeIn() {
        return timeIn;
    }

    public void setTimeIn(String timeIn) {
        this.timeIn = timeIn;
    }

    public String getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(String timeOut) {
        this.timeOut = timeOut;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(@Status String status) {
        this.status = status;
    }
}
