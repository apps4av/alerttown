/*
Copyright (c) 2012, Apps4Av Inc. (apps4av.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.alerttown.app;


/**
 * @author zkhan
 * A class that deals with projected distances and bearings between two points
 * Uses rhumb line
 */
public class Projection {

    /**
     *
     */
    private double mBearing;
    /**
     * 
     */
    private double mDistance;

    private double mLon1;
    private double mLon2;
    private double mLat1;
    private double mLat2;       

    /**
     * @param lon1 Longitude of point 1
     * @param lat1 Latitude of point 1
     * @param lon2 Longitude of point 2
     * @param lat2 Latitude of point 2
     *  Great circle
     */
    public Projection(double lon1, double lat1, double lon2, double lat2) {
        
        /*
         * Save these for track
         */
        mLon1 = Math.toRadians(lon2);
        mLon2 = Math.toRadians(lon1);
        mLat1 = Math.toRadians(lat2);
        mLat2 = Math.toRadians(lat1);       
        
        //http://www.movable-type.co.uk/scripts/latlong.html
        lat1 = mLat2;
        lat2 = mLat1;
        lon2 = mLon1;
        lon1 = mLon2;
        double dLon = lon2 - lon1;
                   
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
       
        double dLat = lat2 - lat1;
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        mDistance = 3963.1676 * c;
        mBearing = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }


    /**
     * @return
     */
    public String getGeneralDirectionFrom(double declination) {
        
        String dir;
        final double DIR_DEGREES = 15;
        double bearing = mBearing;
        
        /*
         * These are important to show for traffic controller communications.
         */
        if(bearing > (DIR_DEGREES) && bearing <= (90 - DIR_DEGREES)) {
            dir = "SW of";
        }
        else if(bearing > (90 - DIR_DEGREES) && bearing <= (90 + DIR_DEGREES)) {
            dir = "W  of";
        }
        else if(bearing > (90 + DIR_DEGREES) && bearing <= (180 - DIR_DEGREES)) {
            dir = "NW of";
        }
        else if(bearing > (180 - DIR_DEGREES) && bearing <= (180 + DIR_DEGREES)) {
            dir = "N  of";
        }
        else if(bearing > (180 + DIR_DEGREES) && bearing <= (270 - DIR_DEGREES)) {
            dir = "NE of";
        }
        else if(bearing > (270 - DIR_DEGREES) && bearing <= (270 + DIR_DEGREES)) {
            dir = "E  of";
        }
        else if(bearing > (270 + DIR_DEGREES) && bearing <= (360 - DIR_DEGREES)) {
            dir = "SE of";
        }
        else {
            dir = "S  of";
        }

        return(dir);
    }
    
    
    public double getDistance() {
        return mDistance;
    }
}
