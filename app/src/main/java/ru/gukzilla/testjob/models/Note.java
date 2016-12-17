package ru.gukzilla.testjob.models;

/**
 * Created by Evgeniy on 15.12.2016.
 */

public class Note {

    String _id;
    String prev = null;
    String next = null;
    String name = null;

    public Note(String _id) {
        this._id = _id;
    }

    public Note(String _id, String prev, String next, String name) {
        this._id = _id;
        this.prev = prev;
        this.next = next;
        this.name = name;
    }

    public String get_id() {
        return _id;
    }

    public String getPrev() {
        return prev;
    }

    public void setPrev(String prev) {
        this.prev = prev;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
