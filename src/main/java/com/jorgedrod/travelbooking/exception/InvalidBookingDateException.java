package com.jorgedrod.travelbooking.exception;

public class InvalidBookingDateException extends RuntimeException{

    public InvalidBookingDateException(String message){
        super(message);
    }
}
