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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;



/**
 * A class representing a Position (non-deterministic Levenshstein automate State).
 
 * @author Kevin
 */
public class Position implements Comparable<Position>
{
    //An int representing this position's boundary (next index in the parent automaton's String to be processed)
    private final int I;
    
    //An int representing this position's presumed executed edit operation count
    private final int E;
    
    //Enum containing fields collectively defining the set of relationship types a 
    //Position's edit operation count can have with a defined maximum edit operation count
    private static enum EditDistanceRelationType { AT_MAX, NOT_AT_MAX };
    
    //Enum containing fields collectively defining the categories of relevant subword 
    //meta-sizes that are of interest when executing transitions on Positions
    private static enum StateRelevantSubwordSizeType { ATLEAST_TWO, ONE, ZERO }
    
    //Enum containing fields collectively defining the set of index-location categories of interest 
    //a char can have with those in a Position's relevant subword (of the parent automaton's String)
    private static enum RelevantSubwordHitIndexType { FIRST_INDEX, TRAILING_INDEX, NO_INDEX };

    
    //Enum containing fields collectively defining the set of possible Position transitions
    private static enum ElementaryTransitionTerm 
    {
        //Fields collectively defining the set of possible Position transition operations
        MATCH(1, 0), INSERTION(0, 1), SUBSTITUTION(1, 1), DELETION(0, 0), FAILURE(0, 0);
        
        //An int denoting the boundary difference between an ElementaryTransitionTerm's operand and result Positions (not relevant for DELETION)
        private final int I_OFFSET;
        
        //An int denoting the executed edit operation difference between an ElementaryTransitionTerm operand and result Positions (not relevant for DELETION)
        private final int E_OFFSET;
        
        

        /**
         * Construct an ElementaryTransitionTerm
         
         * @param iOffset       an int denoting the boundary difference between the to-be-created term's operand and result positions
         * @param eOffset       an int denoting the executed edit distance difference between the to-be-created term's operand and result positions
         */
        private ElementaryTransitionTerm(int iOffset, int eOffset){ I_OFFSET = iOffset; E_OFFSET = eOffset;}
    
        
        
        /**
         * Carries out a Position transition operation.
         
         * @param p             the Position that the transition operation is to be based off of
         * @param hitIndex      an int denoting the index in {@code p}'s relevant subword that
         *                      contains the first occurance of the processing/transition char 
         * @return              a Position
         */
        public Position execute(Position p, int hitIndex)
        {
            if(!this.equals(FAILURE))
            {
                int newI = p.getI() + (this.equals(DELETION) ? hitIndex + 1 : I_OFFSET);        //for deletion, newI represents the # of boundaries to hitIndex (0-based arrays, so we require +1)
                int newE = p.getE() + (this.equals(DELETION) ? (hitIndex + 1) - 1 : E_OFFSET);  //for deletion, newE represents the # of boundaries up to but not including hitIndex (0-based arrays, so simply hitIndex)
                return new Position(newI, newE); 
            }
            else    return null;
        }
    }
    
    
    //Arrays of ElementaryTransitionTerms which collectively represent all possible Position transitions
    private static final ElementaryTransitionTerm[] MATCH_TRANSITION = new ElementaryTransitionTerm[] {ElementaryTransitionTerm.MATCH};
    private static final ElementaryTransitionTerm[] INSERTION_SUBSTITUTION_DELETION_TRANSITION = new ElementaryTransitionTerm[] {ElementaryTransitionTerm.INSERTION, ElementaryTransitionTerm.SUBSTITUTION, ElementaryTransitionTerm.DELETION};
    private static final ElementaryTransitionTerm[] INSERTION_SUBSTITUTION_TRANSITION = new ElementaryTransitionTerm[] {ElementaryTransitionTerm.INSERTION, ElementaryTransitionTerm.SUBSTITUTION};
    private static final ElementaryTransitionTerm[] INSERTION_TRANSITION = new ElementaryTransitionTerm[] {ElementaryTransitionTerm.INSERTION};
    private static final ElementaryTransitionTerm[] FAILURE_TRANSITION = new ElementaryTransitionTerm[] {ElementaryTransitionTerm.FAILURE};
    /////
    
    
    
    /**
     * Constructs a Position.
     
     * @param I     an int representing the desired boundary of the Position
     * @param E     an int representing the desired presumed executed edit operation count of the position
     */
    public Position(int I, int E)
    {
        this.I = I;
        this.E = E;      
    }

    
    
    /**
     * Returns this Position's boundary.
     
     * @return      an int representing this Position's boundary 
     */
    public int getI()
    {
        return I;
    }
    
    
    
    /**
     * Returns this Position's presumed executed edit operation count.
     
     * @return      an int representing this Position's presumed executed edit operation count
     */
    public int getE()
    {
        return E;
    }
    

    
    /**
     * Procures the transition appropriate for a given set of processing circumstances.
     
     * @param edRelationType        the EditDistanceRelationType enum field representing the relationship between this 
     *                              Position's edit operation count and a defined maximum edit operation count
     * @param sRSSizeType           the StateRelevantSubwordSizeType enum field representing this Position's relevant subword meta-size
     * @param rsHitIndexType        the RelevantSubwordHitIndexType enum field representing the index-location of the
     *                              first char in this Position's relevant subword that is equal to a given char 
     * @return                      an array of ElementaryTransitionTerms collectively representing a Position transition
     */
    private ElementaryTransitionTerm[] procureTransition(EditDistanceRelationType edRelationType, StateRelevantSubwordSizeType sRSSizeType, RelevantSubwordHitIndexType rsHitIndexType)
    {
        switch(edRelationType)
        {
            case NOT_AT_MAX:        //we are allowed to execute more edit operations 
            {
                switch(sRSSizeType)                 
                {
                    case ATLEAST_TWO:               //if there are at least one boundary in the automaton's String after this position's boundary
                    {
                        switch(rsHitIndexType)
                        {
                            case FIRST_INDEX:       return MATCH_TRANSITION;
                            case TRAILING_INDEX:    return INSERTION_SUBSTITUTION_DELETION_TRANSITION;
                            default:                return INSERTION_SUBSTITUTION_TRANSITION;
                        }
                    }
                    case ONE:                       //if this position's boundary is the last boundary in the automaton's String
                    {
                        switch(rsHitIndexType)
                        {
                            case FIRST_INDEX:       return MATCH_TRANSITION;
                            default:                return INSERTION_SUBSTITUTION_TRANSITION;
                        }
                    }
                    default:                        return INSERTION_TRANSITION;     //there are no more boundaries in the automaton's String
                }
            }
            default:                //we are NOT allowed to execute more edit operations 
            {
                switch(sRSSizeType)
                {
                    case ZERO:                      return FAILURE_TRANSITION;   //there are no more boundaries in the automaton's String
                    default:                        //there is at least one boundary left in this automaton's String
                    {
                        switch(rsHitIndexType)
                        {
                            case FIRST_INDEX:       return MATCH_TRANSITION;
                            default:                return FAILURE_TRANSITION;
                        }   
                    }   
                }
            }
        }
    }
    
    
    
    
    /**
     * Carries out a transition on this Position (called by transition to carry out the actual operations).
     
     * @param maxEditDistance           an int denoting the maximum edit operation count
     * @param relevantSubwordSize       an int denoting this Position's relevant subword size
     * @param hitIndex                  an int denoting the first index of an occurance of the
     *                                  processing/transition char
     * @return                          the State resulting from executing atransition on this position,
     *                                  or null if no such state exists
     */
    public State transitionInternal(int maxEditDistance, int relevantSubwordSize, int hitIndex)
    {
        //Determine the EditDistanceRelationType representing the relationship between E and maxEditDistance
        EditDistanceRelationType edRelationType = (E < maxEditDistance ? EditDistanceRelationType.NOT_AT_MAX : EditDistanceRelationType.AT_MAX);
        
        //Determine the StateRelevantSubwordSizeType representing the size of this Position's relevant subword
        StateRelevantSubwordSizeType stateRSSizeType = (relevantSubwordSize >= 2 ? StateRelevantSubwordSizeType.ATLEAST_TWO 
                                                    : (relevantSubwordSize == 1 ? StateRelevantSubwordSizeType.ONE : StateRelevantSubwordSizeType.ZERO));
                
        //Determine the RelevantSubwordHitIndexType representing the category of location index-types hitIndex falls into
        RelevantSubwordHitIndexType rsHitIndexType =  (hitIndex == 0 ? RelevantSubwordHitIndexType.FIRST_INDEX 
                                                    : (hitIndex > 0 ? RelevantSubwordHitIndexType.TRAILING_INDEX : RelevantSubwordHitIndexType.NO_INDEX));
        
        //Use edRelationType, stateRSSizeType, and rsHitIndexType to determine the appropriate transition for this Position
        ElementaryTransitionTerm[] elementaryTransition = procureTransition(edRelationType, stateRSSizeType, rsHitIndexType);

        //HashSet which will store the result of excecuting the ElementaryTransitionTerms in elementaryTransition on this Position
        HashSet<Position> possibleNewPositionHashSet = new HashSet<Position>(); 
        
        //Loop through the ElementaryTransitionTerms in elementaryTransition, executing each on this Position
        //and storing each resulting Position in possibleNewPositionHashSet, provided it is a valid position
        for(ElementaryTransitionTerm currentTerm : elementaryTransition)
        {
            Position transitionPosition = currentTerm.execute(this, hitIndex);
            if(transitionPosition != null) possibleNewPositionHashSet.add(transitionPosition);
        }
        /////
 
        if(!possibleNewPositionHashSet.isEmpty())       //If the transition execution resulted in at least one valid Position, produce an
        {                                                   //array version of the result set, sort it and use it to create and return a State
            Position[] positionArray = possibleNewPositionHashSet.toArray(new Position[possibleNewPositionHashSet.size()]);
            Arrays.sort(positionArray);
            return new State(positionArray);
        }
        else
            return null;
    }
    
    
    
    /**
     * Procures data necessary for the execution of a transition on this Position.
     
     * @param maxEditDistance                                       an int denoting the maximum edit operation count
     * @param relevantSubwordStartIndex                             an int denoting the index in the parent State's relevant subword
     *                                                              that this Position's relevant subword starts from                     
     * @param parentStateRelevantSubwordCharacteristicVector        an AugBitSet representing the parent State's relevant subword 
     *                                                              characteristic vector (created with respect to the processing/transitioning char)
     * @return                                                      an array containing:
     *                                                                  - an int of this Position's relevant subword size (index 0) 
     *                                                                  - an int of the first index in this Position's relevant subword
     *                                                                    containing the first occurrence of the processing/transition char (index 1)
     */
    public int[] procurePositionTransitionData(int maxEditDistance, int relevantSubwordStartIndex, AugBitSet parentStateRelevantSubwordCharacteristicVector)
    {
        //int which will contain the value of this Position's relevant subword size
        int relevantSubwordSize;
        
        //int which will denote the index of the first occurance of the char
        //that the transition is being carried out for in this Position's relevant subword 
        int firstHitIndex;
        
        //AugBitSet which will represent the characteristic vector of this Position's relevant subowrd
        AugBitSet relevantSubwordCharacteristicVector;
        
        if(relevantSubwordStartIndex < parentStateRelevantSubwordCharacteristicVector.getRelevantBitSetSize())
        {
            relevantSubwordSize = Math.min(maxEditDistance - E + 1, parentStateRelevantSubwordCharacteristicVector.getRelevantBitSetSize() - relevantSubwordStartIndex);
            
            relevantSubwordCharacteristicVector = parentStateRelevantSubwordCharacteristicVector.get(relevantSubwordStartIndex, 
                                                                                                       relevantSubwordStartIndex + relevantSubwordSize);
            firstHitIndex = relevantSubwordCharacteristicVector.nextSetBit(0);
        }
        else
        {
            relevantSubwordSize = 0;
            firstHitIndex = -1;
        }

        return new int[]{relevantSubwordSize, firstHitIndex};
    }
    
    

    /**
     * Carries out a transition on this Position.
     
     * @param maxEditDistance                                       an int denoting the maximum edit operation count
     * @param parentStateRelevantSubwordLocationIndex               an int of the index in the parent State's relevant subword that this Position's relevant subword starts from
     * @param parentStateRelevantSubwordCharacteristicVector        an AugBitSet representation of the parent State's relevant subword characteristic vector 
     *                                                              (created with respect to the processing/transition char)
     * @return 
     */
    public State transition(int maxEditDistance, int parentStateRelevantSubwordLocationIndex, AugBitSet parentStateRelevantSubwordCharacteristicVector)
    {
        //Procure an array containing this Position's relevant subword size (index 0) and the index in the 
        //Position's relevant subword containing the first occurance of the processing/transition char (index 1)
        int[] positionTransitionDataArray = procurePositionTransitionData(maxEditDistance, parentStateRelevantSubwordLocationIndex, parentStateRelevantSubwordCharacteristicVector);
        
        //Carry out the actual transition operations
        return transitionInternal(maxEditDistance, positionTransitionDataArray[0], positionTransitionDataArray[1]);
    }
    
    
    
    /**
     * Determines if this Positions subsumes another.
     * 
     * Susumption is defined as a quality of a pair of positions which have a difference in
     * executed edit operation count. If the difference is enough to cover a possible transition
     * from the one with less executed edit operations to the one with more executed edit operations,
     * then the former Position is said to "subsume" the latter.
     
     * @param p     a Position
     * @return      true if this position subsumes {@code p}, false otherwise
     */
    public boolean subsumes(Position p)
    { 
        return (this.E < p.E && !(Math.abs(p.I - this.I) > (p.E - this.E)));
    }

    
    
    /**
     * Compares this Position with another.
     
     * @param p2        a Position
     * @return          0 if this Position and {@code p} have equal boundaries and executed operation counts,
     *                  -1 if this Position has a boundary less than that of {@code p} or if their boundaries
     *                  are equal and this Position has an executed operation count less than that of {@code p};
     *                  1 otherwise
     */
    @Override
    public int compareTo(Position p2) 
    {
        if(this.I == p2.I)
            return Integer.compare(this.E, p2.E);
        else if(this.I < p2.I)
            return -1;
        else
            return 1;
    }

    
    
     /**
     * Determines the equality of this Position with another object.
     *
     * A Position is equal to another object iff the argument object is also a Position and 
     * has a boundary and presumed edit operation count equal to those of this Position.
     
     * @param obj       an Object
     * @return          true if {@code obj} is a Position and boundary and presumed edit
     *                  operation count are equal to those of this position; false otherwise

     */
    public boolean equals(Object obj)
    {
        boolean areEqual = (this == obj);
        
        if(!areEqual && obj != null && obj.getClass().equals(Position.class))
        {
            Position ps = (Position)obj;
            areEqual = (this.I == ps.I && this.E == ps.E);
        }
        
        return areEqual;
    }
    
    

    /* Returns a hashCode value for this Position. This value is based on
     * the Position's boundary and presumed executed edit operation count.
     
     * @return      an int of the hash code value of this Position
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + this.I;
        hash = 13 * hash + this.E;
        return hash;
    }
    
    
   
    /**
     * Returns a String representation of this Position.
     
     * @return  a String containing this position's boundary  
     *          and presumed executed edit operation count 
     */
    @Override
    public String toString()
    {
        return this.I + "(#" + this.E + ")";
    }
}