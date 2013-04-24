/**
 * LevenshteinAutomaton is a fast and comprehensive Java library capable
 * of performing automaton and non-automaton based Levenshtein distance
 * determination and neighbor calculations.
 * 
 *  Copyright (C) 2012 Kevin Lawson <Klawson88@gmail.com>
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.BoxOfC.LevenshteinAutomaton;

import java.util.Arrays;



/**
 * A class capable of representing groups of {@link com.BoxOfC.LevenshteinAutomaton.State}s
 * which contain positions that are related in a specific way.
 
 * @author Kevin
 */
public class ParametricState
{
    //Array of ints containing the relative boundary values of positions in a State of this form.
    //This array, like the State position array it will be based off, will contain values in ascending order.
    private final int memberPositionBoundaryOffsetArray[];
    
    //Array of ints containing the edit distance values of positions in a State of this form. Though this array
    //is based off a State position array, its values may not necessarily be stored in ascending order.
    private final int memberPositionEArray[];
    
    //Int denoting the difference between the minimal boundaries of a State of this form and 
    //that of a given State which has transitioned such a State. This value is dependant on 
    //the transitioning state as well as the characterisitc vector of its relevant subword.
    private int transitionBoundaryOffset = 0;
    
    
    
    /**
     * Constructs a ParametricState from a concrete State.
     
     * @param state     a State object
     */
    public ParametricState(State state)
    {
        Position[] stateMemberPositionArray = state.getMemberPositions();
        
        int memberPositionCount = stateMemberPositionArray.length;
        memberPositionBoundaryOffsetArray = new int[memberPositionCount];
        memberPositionEArray = new int[memberPositionCount];
        
        //Loop through stateMemberPositionArray (which contains positions sorted in ascending order),
        //inserting the relative (from the minimal boundary) boundaries and absolute edit distance values
        //in to the corresponding indices of the relevant arrays 
        for(int i = 0; i < memberPositionCount; i++)
        {
            memberPositionBoundaryOffsetArray[i] = stateMemberPositionArray[i].getI() - stateMemberPositionArray[0].getI();
            memberPositionEArray[i] = stateMemberPositionArray[i].getE();
        }
        /////
    }
    
    
    
    /**
     * Constructs a ParametricState from a State and sets its transition boundary offset.
     * @param state                         a State object
     * @param transitionBoundaryOffset      an int of the difference between the minimal boundary of {@code state}
     *                                      and that of a State that would be the result of an instantiation 
     *                                      of the to-be-created ParametricState
     */
    public ParametricState(State state, int transitionBoundaryOffset)
    {
        this(state);
        this.transitionBoundaryOffset = transitionBoundaryOffset;
    }
    
    
    
    /**
     * Returns the largest relative boundary of a position in a State of this form.
     
     * @return      an int denoting the largest boundary of a position in a State of this form
     */
    public int getLargestPositionOffset()
    {
        return memberPositionBoundaryOffsetArray[memberPositionBoundaryOffsetArray.length - 1];
    }
    
    
    
    /**
     * Returns the this ParametricState's transition boundary offset.
     
     * @return      an int denoting the difference between the minimal boundaries of a State of this form
     *              and that of a State that has has a transition to it (this value is only of use when
     *              dealing with a State of the same form as that which was used to create this ParametricState)
     */
    public int getTransitionBoundaryOffset()
    {
        return transitionBoundaryOffset;
    }
    
    
    
    /**
     * Creates a State with Positions which that satisfy the relationships defined by this ParametricState.
     
     * @param minimalBoundary       an int of the desired value of the to-be-created State's minimal boundary 
     *                              (all other boundaries in the State will be based off this)       
     * @return                      a State containing member positions which satisfy the relationships defined by this ParametricState
     */
    public State createActualState(int minimalBoundary)
    {
        //Define the number of member Positions in the to-be-created State 
        //(since a state of identical form was used to create this ParametricState,
        //this number is simply the size either of the array fields)
        int memberPositionCount = memberPositionBoundaryOffsetArray.length;
        
        //Create an array to hold the to-be-created Positions that will serve as members of the to-be-created State
        Position[] actualStateMemberPositionArray = new Position[memberPositionCount];
        
        //Loop through the memberPositionBoundaryOffsetArray and memmberPositionEArray,
        //using the values in the corresponding indices in both (along with minimalBoundary)
        //to create Positions to be inserted in to the corresponding index in stateMemberPositionArray
        for(int i = 0; i < memberPositionCount; i++)
        {
            int currentBoundaryOffset = memberPositionBoundaryOffsetArray[i];
            int currentE = memberPositionEArray[i];
            
            actualStateMemberPositionArray[i] = new Position(minimalBoundary + currentBoundaryOffset, currentE);
        }
        /////
        
        return new State(actualStateMemberPositionArray);
    }
    
    
    
    /**
     * Determines the equality of this ParametricState with another object.
     *
     * A ParametricState is equal to another object iff the argument object is also
     * a ParametricState and has field arrays that are equal to those of this ParametricState.
     
     * @param obj       an Object
     * @return          true if {@code obj} is a ParametricState and its field arrays
     *                  are equal to those of this ParametricState; false otherwise
     */
    @Override
    public boolean equals(Object obj)
    {
        boolean areEqual = (this == obj);
        
        if(!areEqual && obj != null && obj.getClass().equals(ParametricState.class))
        {
           ParametricState pState = (ParametricState)obj;
            
            areEqual = (Arrays.equals(this.memberPositionBoundaryOffsetArray, pState.memberPositionBoundaryOffsetArray)
                        && Arrays.equals(this.memberPositionEArray, pState.memberPositionEArray));
        }
        
        return areEqual;
    }

    
    
    /**
     * Returns the hash code value of this ParametricState.
     
     * @return      an int of the hash code value of this ParametricState
     */
    @Override
    public int hashCode() 
    {
        int hash = 7;
        hash = 61 * hash + Arrays.hashCode(this.memberPositionBoundaryOffsetArray);
        hash = 61 * hash + Arrays.hashCode(this.memberPositionEArray);
        return hash;
    }
    
    
    
    /**
     * Returns a String representation of this ParametricState.
     
     * @return      a String of this ParametricState's parametric positions,
     *              as well as its transitionBoundaryOffset (if it != 0)
     */
    @Override
    public String toString()
    {
        String returnString = "i(#" + memberPositionEArray[0] +  ")";
        
        int memberPositionCount = memberPositionBoundaryOffsetArray.length;
        for(int i = 1; i < memberPositionCount; i++)
            returnString += " (i + " + memberPositionBoundaryOffsetArray[i] + ")(#" + memberPositionEArray[i] + ")";
         
        if(transitionBoundaryOffset != 0) returnString += " " + transitionBoundaryOffset;
        
        return returnString;
    }
}