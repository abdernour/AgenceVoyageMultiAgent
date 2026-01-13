package com.agencevoyage.ontology.predicates;

import jade.content.Predicate;
import com.agencevoyage.ontology.concepts.Hotel;

public class HotelInfo implements Predicate {
    private Hotel hotel;

    public HotelInfo() {}

    public HotelInfo(Hotel hotel) {
        this.hotel = hotel;
    }

    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }
}