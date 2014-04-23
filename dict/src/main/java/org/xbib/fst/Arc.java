
package org.xbib.fst;

/**
 * The fst's arc implementation.
 */
public class Arc {

    // Arc's weight
    private float weight;
    // input label
    private int iLabel;
    // output label
    private int oLabel;
    // next state's id
    private State nextState;

    /**
     * Default Constructor
     */
    public Arc() {
    }

    /**
     * Arc Constructor
     *
     * @param iLabel    the input label's id
     * @param oLabel    the output label's id
     * @param weight    the arc's weight
     * @param nextState the arc's next state
     */
    public Arc(int iLabel, int oLabel, float weight, State nextState) {
        this.weight = weight;
        this.iLabel = iLabel;
        this.oLabel = oLabel;
        this.nextState = nextState;
    }

    /**
     * Get the arc's weight
     */
    public float getWeight() {
        return weight;
    }

    /**
     * Set the arc's weight
     */
    public void setWeight(float weight) {
        this.weight = weight;
    }

    /**
     * Get the input label's id
     */
    public int getInputLabel() {
        return iLabel;
    }

    /**
     * Set the input label's id
     *
     * @param iLabel the input label's id to set
     */
    public void setInputLabel(int iLabel) {
        this.iLabel = iLabel;
    }

    /**
     * Get the output label's id
     */
    public int getOutputLabel() {
        return oLabel;
    }

    /**
     * Set the output label's id
     *
     * @param oLabel the output label's id to set
     */
    public void setOutputLabel(int oLabel) {
        this.oLabel = oLabel;
    }

    /**
     * Get the next state
     */
    public State getNextState() {
        return nextState;
    }

    /**
     * Set the next state
     *
     * @param nextState the next state to set
     */
    public void setNextState(State nextState) {
        this.nextState = nextState;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Arc other = (Arc) obj;
        if (iLabel != other.iLabel) {
            return false;
        }
        if (nextState == null) {
            if (other.nextState != null) {
                return false;
            }
        } else if (nextState.getId() != other.nextState.getId()) {
            return false;
        }
        if (oLabel != other.oLabel) {
            return false;
        }
        if (!(weight == other.weight)) {
            if (Float.floatToIntBits(weight) != Float
                    .floatToIntBits(other.weight)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "(" + iLabel + ", " + oLabel + ", " + weight + ", " + nextState + ")";
    }
}
