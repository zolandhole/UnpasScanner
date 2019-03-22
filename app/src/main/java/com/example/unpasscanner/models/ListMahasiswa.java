package com.example.unpasscanner.models;

public class ListMahasiswa {
    private String nim,nama,mac_user;

    public ListMahasiswa() {
    }

    public ListMahasiswa(String nim, String nama, String mac_user) {
        this.nim = nim;
        this.nama = nama;
        this.mac_user = mac_user;
    }

    public String getNim() {
        return nim;
    }

    public void setNim(String nim) {
        this.nim = nim;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getMac_user() {
        return mac_user;
    }

    public void setMac_user(String mac_user) {
        this.mac_user = mac_user;
    }
}
