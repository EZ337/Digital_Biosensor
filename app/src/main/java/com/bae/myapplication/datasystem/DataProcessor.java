package com.bae.myapplication.datasystem;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;

/**
 * Responsible for handling data processing
 */
public class DataProcessor {

    /**
     * Color of each sampled point.
     */
    private final List<Integer> sampleColors;

    /**
     * rPoints of each sampled point.
     */
    private ArrayList<Double> rPoints = new ArrayList<>();
    private ArrayList<Double> dataPoints = new ArrayList<>();

    private double avg_rval;
    private double avg_gval;
    private double avg_bval;
    private double avg_rpoint;
    private double rPointSTD;

    /**
     * ratio of the rPoint compared to the control sample.
     */
    private double comparativeValue;
    private double transformedValue;


    public DataProcessor() {
        sampleColors = new ArrayList<>();
    }

    /**
     * Add a sample color to the list.
     *
     * @param sampleColor color (int) to add.
     */
    public void addSampleColor(int sampleColor) {
        sampleColors.add(sampleColor);
    }

    /**
     * Calculates the values after all the data points are installed.
     */
    public void start() {
        // Calculate rPoints
        // Do (r + g) / b
        for (int i = 0; i < sampleColors.size(); i++) {
            int r = Color.red(sampleColors.get(i));
            int g = Color.green(sampleColors.get(i));
            int b = Color.blue(sampleColors.get(i));
//            double rPoint = (double) (r + g) / b;
//            double rPoint = (double) r / (r + g + b);
            double rPoint = (double) (r + g + b) / r;
            rPoints.add(rPoint);    // May be problematic since it is irrelevant now


            //int color = sampleColors.get(i);
//            alpha = (double) r/g;
//            beta = alpha * Math.exp(alpha);
//            dataPoints.add(alpha);
        }

        // Calculate r,g,b
        for (int i = 0; i < sampleColors.size(); i++) {
            int color = sampleColors.get(i);
            avg_rval += Color.red(color);
            avg_gval += Color.green(color);
            avg_bval += Color.blue(color);
        }
        int size = sampleColors.size();
        avg_rval /= size;
        avg_gval /= size;
        avg_bval /= size;

        // Calculate average R Point
        for (Double d : rPoints) {
            avg_rpoint += d;
        }
        avg_rpoint /= rPoints.size();

        // Calculate R Point STD
        double mean = getAvgRPoint();
        double squaredDiffs = 0;
        for (Double d : rPoints) {
            squaredDiffs += Math.pow(d - mean, 2);
        }
        rPointSTD = squaredDiffs / rPoints.size();
    }


    /**
     * @return Average red value of each sampling point.
     */
    public double getAvgRValue() {
        return avg_rval;
    }

    /**
     * @return Average green value of each sampling point.
     */
    public double getAvgGValue() {
        return avg_gval;
    }

    /**
     * @return Average blue value of each sampling point.
     */
    public double getAvgBValue() {
        return avg_bval;
    }


    /**
     * @return Individual rPoints.
     */
    public ArrayList<Double> getRPoints() {
        return rPoints;
    }

    /**
     * @return Datapoints by Alpha Value
     */
    public ArrayList<Double> getDataPoints() {
        return dataPoints;
    }

    /**
     * Calculate the average rPoint value among the sampled rPoints
     *
     * @return The average rPoint value of each sampling point.
     */
    public double getAvgRPoint() {
        return avg_rpoint;
    }

    /**
     * @return The STD of rPoints.
     */
    public double getRPointSTD() {
        return rPointSTD;
    }

    /**
     * Setter for comparativeValue
     *
     * @param val
     */
    public void setComparativeValue(double val) {
        comparativeValue = val;
    }

    /**
     * @return Comparative Value
     */
    public double getComparativeValue() {
        return comparativeValue;
    }

    public void setTransformedValue(double val) {
        transformedValue = val;
    }

    public double getTransformedValue() {
        return transformedValue;
    }
}
