package org.coordinate.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.io.Serial;
import java.io.Serializable;


@Named("areaCheckService")
@ApplicationScoped
public class AreaCheck implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Check points if they are in the area?
     * @param x coordinate
     * @param y coordinate
     * @param r radio
     * @return result
     */
    public boolean checkHit(double x, double y, double r) {
        if (x <= 0 && x >= -r && y >= 0 && y <= r) {
            return true;
        }

        if (x <= 0 && x >= -r && y <= 0 && y>= -r/2) {
            if (y >= -0.5 * x - r/2) {
                return true;
            }
        }

        if (x >= 0 && y <= 0) {
            return x * x + y * y <= (r / 2) * (r / 2);
        }
        return false;
    }
}
