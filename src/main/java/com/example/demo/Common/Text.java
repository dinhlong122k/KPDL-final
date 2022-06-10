package com.example.demo.Common;

import javax.persistence.*;

public class Text {

    private int id;

    //text original
    private String text_hash;

    //text hashed text
    private String hash_text;

    private String private_key;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHash_text() {
        return hash_text;
    }

    public void setHash_text(String hash_text) {
        this.hash_text = hash_text;
    }

    public String getPrivate_key() {
        return private_key;
    }

    public void setPrivate_key(String private_key) {
        this.private_key = private_key;
    }

    public String getText_hash() {
        return text_hash;
    }

    public void setText_hash(String text_hash) {
        this.text_hash = text_hash;
    }
}
