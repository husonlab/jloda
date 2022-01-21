/*
 * FastMultiLayerMethodOptions.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jloda.graph.fmm;

/**
 * implementation of the fast multilayer method
 * Original C++ author: Stefan Hachul, original license: GPL
 * Reimplemented in Java by Daniel Huson, 3.2021
 */
public class FastMultiLayerMethodOptions {
    private float unitEdgeLength;

    //setting low level options
    //setting general options
    private int randSeed;

    public enum EdgeLengthMeasurement {BoundingCircle, MidPoint}

    private EdgeLengthMeasurement edgeLengthMeasurement;

    public enum AllowedPositions {Integer, All}

    private AllowedPositions allowedPositions;

    //setting options for the divide and conquer step
    private int stepsForRotatingComponents;

    //setting options for the multilevel step
    private int minGraphSize;

    public enum GalaxyChoice {
        UniformProb,
        NonUniformProbLowerMass,
        NonUniformProbHigherMass
    }

    private GalaxyChoice galaxyChoice;
    private int numberRandomTries;

    public enum MaxIterChange {Constant, LinearlyDecreasing, RapidlyDecreasing}

    private MaxIterChange maxIterChange;
    private int maxIterFactor;

    public enum InitialPlacementMultiLayer {Advanced}

    private InitialPlacementMultiLayer initialPlacementMult;
    private boolean mSingleLevel;

    //setting options for the force calculation step
    public enum ForceModel {FruchtermanReingold, New, Eades}

    private ForceModel forceModel;
    private int springStrength;
    private int repForcesStrength;

    public enum RepulsiveForcesCalculation {Exact, GridApproximation /* , MultipoleMethod*/}

    private RepulsiveForcesCalculation repulsiveForcesCalculation;

    public enum StopCriterion {FixedIterationsOrThreshold, FixedIterations, Threshold}

    private StopCriterion stopCriterion;
    private float threshold;
    private int fixedIterations;
    private float forceScalingFactor;
    private boolean coolTemperature;
    private float coolValue;

    public enum InitialPlacementForces {
        UniformGrid, RandomTime, RandomRandIterNr, KeepPositions
    }

    private InitialPlacementForces initialPlacementForces;

    //setting options for postprocessing
    private boolean resizeDrawing;
    private int resizingScalar;
    private int fineTuningIterations;
    private float fineTuneScalar;
    private boolean adjustPostRepStrengthDynamically;
    private float postSpringStrength;
    private float postStrengthOfRepForces;

    //setting options for different repulsive force calculation methods
    private int frGridQuotient;

    // options added by DHH
    private boolean useSimpleAlgorithmForChainsAndCycles;
    private int numberOfChainSmoothingRounds = 0;

    public FastMultiLayerMethodOptions() {
        initialize();
    }

    public void initialize() {
        setUnitEdgeLength(100);

        setRandSeed(666);
        setEdgeLengthMeasurement(EdgeLengthMeasurement.BoundingCircle);
        setAllowedPositions(AllowedPositions.Integer);

        setStepsForRotatingComponents(10);

        //setting options for the multilevel step
        setMinGraphSize(50);
        setGalaxyChoice(GalaxyChoice.NonUniformProbLowerMass);
        setNumberRandomTries(20);
        setMaxIterChange(MaxIterChange.LinearlyDecreasing);
        setMaxIterFactor(10);
        setInitialPlacementMult(InitialPlacementMultiLayer.Advanced);
        setMSingleLevel(false);

        //setting options for the force calculation step
        setForceModel(ForceModel.New);
        setSpringStrength(1);
        setRepForcesStrength(1);
        setRepulsiveForcesCalculation(RepulsiveForcesCalculation.GridApproximation);
        setStopCriterion(StopCriterion.FixedIterationsOrThreshold);
        setThreshold(0.01f);
        setFixedIterations(30);
        setForceScalingFactor(0.05f);
        setCoolTemperature(false);
        setCoolValue(0.99f);
        setInitialPlacementForces(InitialPlacementForces.RandomRandIterNr);

        //setting options for postprocessing
        setResizeDrawing(true);
        setResizingScalar(1);
        setFineTuningIterations(20);
        setFineTuneScalar(0.2f);
        setAdjustPostRepStrengthDynamically(true);
        setPostSpringStrength(2.0f);
        setPostStrengthOfRepForces(0.01f);

        //setting options for different repulsive force calculation methods
        setFrGridQuotient(2);

        setUseSimpleAlgorithmForChainsAndCycles(true);
    }

    public float getUnitEdgeLength() {
        return unitEdgeLength;
    }

    public void setUnitEdgeLength(float unitEdgeLength) {
        this.unitEdgeLength = unitEdgeLength;
    }


    public int getRandSeed() {
        return randSeed;
    }

    public void setRandSeed(int randSeed) {
        this.randSeed = randSeed;
    }

    public EdgeLengthMeasurement getEdgeLengthMeasurement() {
        return edgeLengthMeasurement;
    }

    public void setEdgeLengthMeasurement(EdgeLengthMeasurement edgeLengthMeasurement) {
        this.edgeLengthMeasurement = edgeLengthMeasurement;
    }

    public AllowedPositions getAllowedPositions() {
        return allowedPositions;
    }

    public void setAllowedPositions(AllowedPositions allowedPositions) {
        this.allowedPositions = allowedPositions;
    }

    public int getStepsForRotatingComponents() {
        return stepsForRotatingComponents;
    }

    public void setStepsForRotatingComponents(int stepsForRotatingComponents) {
        this.stepsForRotatingComponents = stepsForRotatingComponents;
    }

    public int getMinGraphSize() {
        return minGraphSize;
    }

    public void setMinGraphSize(int minGraphSize) {
        this.minGraphSize = minGraphSize;
    }

    public GalaxyChoice getGalaxyChoice() {
        return galaxyChoice;
    }

    public void setGalaxyChoice(GalaxyChoice galaxyChoice) {
        this.galaxyChoice = galaxyChoice;
    }

    public int getNumberRandomTries() {
        return numberRandomTries;
    }

    public void setNumberRandomTries(int numberRandomTries) {
        this.numberRandomTries = numberRandomTries;
    }

    public MaxIterChange getMaxIterChange() {
        return maxIterChange;
    }

    public void setMaxIterChange(MaxIterChange maxIterChange) {
        this.maxIterChange = maxIterChange;
    }

    public int getMaxIterFactor() {
        return maxIterFactor;
    }

    public void setMaxIterFactor(int maxIterFactor) {
        this.maxIterFactor = maxIterFactor;
    }

    public InitialPlacementMultiLayer getInitialPlacementMult() {
        return initialPlacementMult;
    }

    public void setInitialPlacementMult(InitialPlacementMultiLayer initialPlacementMult) {
        this.initialPlacementMult = initialPlacementMult;
    }

    public boolean isMSingleLevel() {
        return mSingleLevel;
    }

    public void setMSingleLevel(boolean mSingleLevel) {
        this.mSingleLevel = mSingleLevel;
    }

    public ForceModel getForceModel() {
        return forceModel;
    }

    public void setForceModel(ForceModel forceModel) {
        this.forceModel = forceModel;
    }

    public int getSpringStrength() {
        return springStrength;
    }

    public void setSpringStrength(int springStrength) {
        this.springStrength = springStrength;
    }

    public int getRepForcesStrength() {
        return repForcesStrength;
    }

    public void setRepForcesStrength(int repForcesStrength) {
        this.repForcesStrength = repForcesStrength;
    }

    public RepulsiveForcesCalculation getRepulsiveForcesCalculation() {
        return repulsiveForcesCalculation;
    }

    public void setRepulsiveForcesCalculation(RepulsiveForcesCalculation repulsiveForcesCalculation) {
        this.repulsiveForcesCalculation = repulsiveForcesCalculation;
    }

    public StopCriterion getStopCriterion() {
        return stopCriterion;
    }

    public void setStopCriterion(StopCriterion stopCriterion) {
        this.stopCriterion = stopCriterion;
    }

    public float getThreshold() {
        return threshold;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    public int getFixedIterations() {
        return fixedIterations;
    }

    public void setFixedIterations(int fixedIterations) {
        this.fixedIterations = fixedIterations;
    }

    public float getForceScalingFactor() {
        return forceScalingFactor;
    }

    public void setForceScalingFactor(float forceScalingFactor) {
        this.forceScalingFactor = forceScalingFactor;
    }

    public boolean isCoolTemperature() {
        return coolTemperature;
    }

    public void setCoolTemperature(boolean coolTemperature) {
        this.coolTemperature = coolTemperature;
    }

    public float getCoolValue() {
        return coolValue;
    }

    public void setCoolValue(float coolValue) {
        this.coolValue = coolValue;
    }

    public InitialPlacementForces getInitialPlacementForces() {
        return initialPlacementForces;
    }

    public void setInitialPlacementForces(InitialPlacementForces initialPlacementForces) {
        this.initialPlacementForces = initialPlacementForces;
    }

    public boolean isResizeDrawing() {
        return resizeDrawing;
    }

    public void setResizeDrawing(boolean resizeDrawing) {
        this.resizeDrawing = resizeDrawing;
    }

    public int getResizingScalar() {
        return resizingScalar;
    }

    public void setResizingScalar(int resizingScalar) {
        this.resizingScalar = resizingScalar;
    }

    public int getFineTuningIterations() {
        return fineTuningIterations;
    }

    public void setFineTuningIterations(int fineTuningIterations) {
        this.fineTuningIterations = fineTuningIterations;
    }

    public float getFineTuneScalar() {
        return fineTuneScalar;
    }

    public void setFineTuneScalar(float fineTuneScalar) {
        this.fineTuneScalar = fineTuneScalar;
    }

    public boolean isAdjustPostRepStrengthDynamically() {
        return adjustPostRepStrengthDynamically;
    }

    public void setAdjustPostRepStrengthDynamically(boolean adjustPostRepStrengthDynamically) {
        this.adjustPostRepStrengthDynamically = adjustPostRepStrengthDynamically;
    }

    public float getPostSpringStrength() {
        return postSpringStrength;
    }

    public void setPostSpringStrength(float postSpringStrength) {
        this.postSpringStrength = postSpringStrength;
    }

    public float getPostStrengthOfRepForces() {
        return postStrengthOfRepForces;
    }

    public void setPostStrengthOfRepForces(float postStrengthOfRepForces) {
        this.postStrengthOfRepForces = postStrengthOfRepForces;
    }

    public int getFrGridQuotient() {
        return frGridQuotient;
    }

    public void setFrGridQuotient(int frGridQuotient) {
        this.frGridQuotient = frGridQuotient;
    }

    public boolean isUseSimpleAlgorithmForChainsAndCycles() {
        return useSimpleAlgorithmForChainsAndCycles;
    }

    public void setUseSimpleAlgorithmForChainsAndCycles(boolean useSimpleAlgorithmForChainsAndCycles) {
        this.useSimpleAlgorithmForChainsAndCycles = useSimpleAlgorithmForChainsAndCycles;
    }

    public int getNumberOfChainSmoothingRounds() {
        return numberOfChainSmoothingRounds;
    }

    public void setNumberOfChainSmoothingRounds(int numberOfChainSmoothingRounds) {
        this.numberOfChainSmoothingRounds = numberOfChainSmoothingRounds;
    }

    public static FastMultiLayerMethodOptions getDefaultForMicrobialGenomes() {
        var options = new FastMultiLayerMethodOptions();
        //options.setRepulsiveForcesCalculation(FMMMOptions.RepulsiveForcesCalculation.Exact);
        options.setGalaxyChoice(FastMultiLayerMethodOptions.GalaxyChoice.NonUniformProbHigherMass);
        options.setAllowedPositions(FastMultiLayerMethodOptions.AllowedPositions.All);
        options.setUnitEdgeLength(1.0f);
        options.setStepsForRotatingComponents(50);

        options.setUseSimpleAlgorithmForChainsAndCycles(false);
        options.setNumberOfChainSmoothingRounds(100);

        options.setMaxIterChange(FastMultiLayerMethodOptions.MaxIterChange.LinearlyDecreasing);
        //options.setFixedIterations(30);
        //options.setFineTuningIterations(20);
        // options.setNmPrecision(4);
        return options;

    }
}
