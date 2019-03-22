package com.example.unpasscanner.models;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

public class SerializableMahasiswa extends ListMahasiswa implements Serializable {
    private ListMahasiswa list;

    public SerializableMahasiswa(String nim, String nama, String mac_user) {
        list = new ListMahasiswa(nim,nama,mac_user);
    }

    @Override
    public String getNim() {
        return list.getNim();
    }

    @Override
    public String getNama() {
        return list.getNama();
    }

    @Override
    public String getMac_user() {
        return list.getMac_user();
    }

    // serialization support

    private static final long serialVersionUID = 1L;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(list.getNim());
        out.writeUTF(list.getNama());
        out.writeUTF(list.getMac_user());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        list = new ListMahasiswa(in.readUTF(), in.readUTF(), in.readUTF());
    }

    private void readObjectNoData() throws ObjectStreamException {
        // nothing to do
    }
}
