package com.lovingapp.loving.exception;

public class BulkResourceAlreadyExistsException extends RuntimeException {

    private final String resource;
    private final int count;

    public BulkResourceAlreadyExistsException(String resource, int count) {
        super(resource + " already exists for " + count + " items");
        this.resource = resource;
        this.count = count;
    }

    public String getResource() {
        return resource;
    }

    public int getCount() {
        return count;
    }
}
