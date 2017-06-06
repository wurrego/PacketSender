package vt.wurrego;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import vt.wurrego.utils.CommonEnums;
import vt.wurrego.utils.Logger;

import java.util.Random;

/**
 * Created by wurrego on 5/7/17.
 */
public class StateMachine {
    private CommonEnums.StateStatus state;
    private RealMatrix probTransitionMatrix;
    private RealMatrix state_OFF_OneHot;
    private RealMatrix state_IDLE_OneHot;
    private RealMatrix state_LOW_OneHot;
    private RealMatrix state_HIGH_OneHot;
    private int number_States;
    private String TAG;

    public StateMachine() {

        // debug parameters
        TAG = " [" + this.getClass().getSimpleName() + "] ";

        this.state = CommonEnums.StateStatus.OFF;

        // Create the state matrices
        this.number_States = 4;

        double[][] state1Data = { {1.0, 0.0, 0.0, 0.0} };
        this.state_OFF_OneHot = MatrixUtils.createRealMatrix(state1Data);

        double[][] state2Data = { {0.0, 1.0, 0.0, 0.0} };
        this.state_IDLE_OneHot = MatrixUtils.createRealMatrix(state2Data);

        double[][] state3Data = { {0.0, 0.0, 1.0, 0.0} };
        this.state_LOW_OneHot = MatrixUtils.createRealMatrix(state3Data);

        double[][] state4Data = { {0.0, 0.0, 0.0, 1.0} };
        this.state_HIGH_OneHot = MatrixUtils.createRealMatrix(state4Data);

        // Create the transition probability matrix
        double[][] pData = { {0.7, 0.3, 0.0, 0.0}, {0.05, 0.78, 0.16, 0.01}, {0.0, 0.2, 0.75, 0.05}, {0.0, 0.05, 0.2, 0.75}};
        this.probTransitionMatrix = MatrixUtils.createRealMatrix(pData);
    }

    /**
     * get_onTime - gets time on for # of iterations
     * @param iterations
     * @return
     */
    public double  get_onTime(int iterations) {
        double onTime = 0.0;

        RealMatrix pDist = state_OFF_OneHot.multiply(probTransitionMatrix.power(iterations));

        onTime = pDist.getEntry(0,2) + pDist.getEntry(0,3);

        return onTime;
    }

    /**
     * get_offTime - gets time off for # of iterations
     * @param iterations
     * @return double
     */
    public double  get_offTime(int iterations) {
        double offTime = 0.0;

        RealMatrix pDist = state_OFF_OneHot.multiply(probTransitionMatrix.power(iterations));

        offTime = pDist.getEntry(0,0) + pDist.getEntry(0,1);

        return offTime;
    }

    /**
     * get_State - returns the current state
     */
    public CommonEnums.StateStatus get_State() {
        return this.state;
    }

    /**
     * set_State - updates the current state to provided value
     * @param newState
     */
    public void set_State(CommonEnums.StateStatus newState) {
        this.state = newState;
    }

    /**
     * nextState - decides next state
     * @return CommonEnums.StateMachine
     */
    public CommonEnums.StateStatus get_nextState() {
        CommonEnums.StateStatus nextState = this.state;
        RealMatrix pDist;

        switch (this.state) {

            case OFF:
                pDist = state_OFF_OneHot.multiply(probTransitionMatrix.power(1));
                break;

            case IDLE:
                pDist = state_IDLE_OneHot.multiply(probTransitionMatrix.power(1));
                break;

            case LOW:
                pDist = state_LOW_OneHot.multiply(probTransitionMatrix.power(1));
                break;

            case HIGH:
                pDist = state_HIGH_OneHot.multiply(probTransitionMatrix.power(1));
                break;


            default:
                Logger.log( TAG , "System in Unknown State." );
                nextState = CommonEnums.StateStatus.OFF;
                return nextState;
        }

        // roll dice and get next state probabilities
        RealVector diceRoll = generateRandomVector(number_States);
        RealVector p_nextState = diceRoll.ebeMultiply(pDist.getRowVector(0));
        int nextStateIndex = p_nextState.getMaxIndex();

        nextState = CommonEnums.StateStatus.getValue(nextStateIndex);

        return nextState;
    }

    /**
     * generateRandomVector - creates vector of specified size containing random ints
     * @param size
     * @return
     */
    private RealVector generateRandomVector(int size){
        RealVector randVector = new ArrayRealVector(size);

        Random random = new Random();

        for (int i = 0; i < size; i++)
        {
            randVector.setEntry(i, random.nextInt(1000));

        }

        return randVector;
    }
}
