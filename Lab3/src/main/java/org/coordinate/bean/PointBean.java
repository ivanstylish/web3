package org.coordinate.bean;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.coordinate.service.AreaCheck;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Named("PointBean")
@SessionScoped
public class PointBean implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Double x;
    private Double y;
    private Double r = 2.0;

    private List<Double> selectedXValues = new ArrayList<>();

    @Inject
    private AreaCheck areaCheckService;

    @Inject
    private ResultBean resultBean;

    @PostConstruct
    public void init() {
    }

    public void keepValues() {
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> params = fc.getExternalContext().getRequestParameterMap();

        if (params.containsKey("pointForm:rValue")) {
            try {
                String val = params.get("pointForm:rValue");
                if (val != null && !val.isEmpty()) {
                    this.r = Double.valueOf(val);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (params.containsKey("pointForm:hiddenX")) {
            String hx = params.get("pointForm:hiddenX");
            if (hx != null && !hx.isEmpty()) {
                try {
                    this.x = Double.valueOf(hx);

                    String hy = params.get("pointForm:hiddenY");
                    if (hy != null && !hy.isEmpty()) {
                        this.y = Double.valueOf(hy);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String checkPoint() {
        keepValues();

        System.out.println("=== checkPoint called ===");
        System.out.println("x = " + x);
        System.out.println("y = " + y);
        System.out.println("r = " + r);
        System.out.println("selectedXValues = " + selectedXValues);

        if (this.x != null && this.y != null) {
            processSinglePoint(this.x);
            this.x = null;
            return null;
        }

        if (selectedXValues == null || selectedXValues.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("pointForm:xCheckboxes",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select at least one X", null));
            return null;
        }

        if (this.y == null) {
            FacesContext.getCurrentInstance().addMessage("pointForm:yInput",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please enter Y coordinate", null));
            return null;
        }

        for (Object val : selectedXValues) {
            Double xVal = null;
            if (val instanceof Double) {
                xVal = (Double) val;
            } else if (val instanceof String) {
                try {
                    xVal = Double.parseDouble((String) val);
                } catch (NumberFormatException ignored) {}
            }

            if (xVal != null) {
                processSinglePoint(xVal);
            }
        }

        return null;
    }

    private void processSinglePoint(Double xVal) {
        System.out.println("processSinglePoint: x=" + xVal + ", y=" + y + ", r=" + r);

        if (validate(xVal, y, r)) {
            boolean hit = areaCheckService.checkHit(xVal, y, r);
            System.out.println("Validation passed, hit = " + hit);
            resultBean.addResult(xVal, y, r, hit);
        } else {
            System.out.println("Validation failed!");
        }
    }

    private boolean validate(Double curX, Double curY, Double curR) {
        boolean isValid = curX != null && curY != null && curR != null
                && curY >= -3 && curY <= 3;
        System.out.println("Validation: x=" + curX + ", y=" + curY + ", r=" + curR + " -> " + isValid);
        return isValid;
    }

    // Getters and Setters
    public Double getX() { return x; }
    public void setX(Double x) { this.x = x; }

    public Double getY() { return y; }
    public void setY(Double y) { this.y = y; }

    public Double getR() { return r; }
    public void setR(Double r) { this.r = r; }

    public List<Double> getSelectedXValues() {
        return selectedXValues;
    }

    public void setSelectedXValues(List<Double> selectedXValues) {
        this.selectedXValues = selectedXValues;
    }
}