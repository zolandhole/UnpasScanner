package com.example.unpasscanner.models;

public class Ruangan {
    private int idRuangan;
    private String kodeRuangan="", ruangan="";

    public Ruangan(){}
    public Ruangan(int idRuangan, String kodeRuangan, String ruangan) {
        this.idRuangan = idRuangan;
        this.kodeRuangan = kodeRuangan;
        this.ruangan = ruangan;
    }

    public int getIdRuangan() {
        return idRuangan;
    }

    public void setIdRuangan(int idRuangan) {
        this.idRuangan = idRuangan;
    }

    public String getKodeRuangan() {
        return kodeRuangan;
    }

    public void setKodeRuangan(String kodeRuangan) {
        this.kodeRuangan = kodeRuangan;
    }

    public String getRuangan() {
        return ruangan;
    }

    public void setRuangan(String ruangan) {
        this.ruangan = ruangan;
    }
}
