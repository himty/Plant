package com.plant.plant;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by jenny on 6/20/2017.
 */

public class Plant implements Serializable {
    //oh boy the bad coding practices
    public String name;
    public String species;
    public String imageDir;
    public String startDay;
    public Integer wateringInterval;
    public String notes;

    //warning: some may be null
    public Plant(String name_, String species_, String imageDir_, String startDay_,
                 Integer wateringInterval_, String notes_) {
        name = name_;
        species = species_;
        imageDir = imageDir_;
        startDay = startDay_;
        wateringInterval = wateringInterval_;
        notes = notes_;
    }
}
