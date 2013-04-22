/**
 * LevenshteinAutomaton is a fast and comprehensive Java library capable
 * of performing automaton and non-automaton based Levenshtein distance
 * determination and neighbor calculations.
 * 
 *  Copyright (C) 2012 Kevin Lawson <Klawson88@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (version 3) as 
 * published by the Free Software Foundation. Licensing for proprietary 
 * software is available at a cost, inquire for more details. 
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.BoxOfC.LevenshteinAutomaton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;



/**
 * A class representing a collection of Positions that are 
 * subsubmed by a common base Position and do not subsume each other.
 
 * @author Kevin
 */
public class State
{
    //An array of Positions representing the boundary and edit count combinations that can result from 
    //using the containing automaton to process a substring of the given String against a sequence of characters
    private final Position[] memberPositionArray;

    
    
    /**
     * Constructs a State from a sorted array of Positions. Positions in the array 
     * are assumed to satisfy the requirements of membership in a state (see class description).
     
     * @param memberPositionArray       an array of Positions (sorted in ascending order) that
     *                                  will become members of the to-be-created state
     */
    public State(Position[] memberPositionArray)
    {
        this.memberPositionArray = memberPositionArray;
    }
    
    
    
    /**
     * Constructs a State from an existing State and a Position that is not
     * a member, but satisfies the requirements of membership in the State.
     
     * @param s     a State
     * @param p     a Position that satisfies the membership requirements of {@code s}
     */
    public State(State s, Position p)
    {
        //Calculate to-be-created State's member Position array size (simply an increment of that of the argument state)
        int newMemberPositionArrayLength = s.memberPositionArray.length + 1;
        
        //Copy the members of the argument State in to the member array of this State, adding the argument position at the end
        memberPositionArray = Arrays.copyOf(s.memberPositionArray, newMemberPositionArrayLength);
        memberPositionArray[newMemberPositionArrayLength - 1] = p;
        /////
        
        //Sort the (possibly out of order) Positions in this State's member array
        //(p is not guarenteed to be greater than the members of S)
        Arrays.sort(memberPositionArray);
    }
    
    
    
    /**
     * Creates a State from the Positions serving as members in a collection of States.
     
     * @param stateCollection       a Collection of States
     */
    public State(Collection<State> stateCollection)
    {   
        //HashSet which will contain Positions that will be members of the to-be-created State
        HashSet<Position> prospectiveMemberPositionCollection = new HashSet<Position>();
        
        //Loop through the positions in each state, adding them to prospectiveMemberPositionCollection
        for(State state : stateCollection) prospectiveMemberPositionCollection.addAll(Arrays.asList(state.memberPositionArray));
        
        //Iterate through prospectiveMemberPositionCollection, removing Positions
        //which are subsubmed by at least one other Position in the set
        Iterator it1 = prospectiveMemberPositionCollection.iterator();
        while(it1.hasNext())
        {
            //Processing position which is assumed to be a susumption target of another Position in the set
            Position position1 = (Position)it1.next();  
            
            //Iterate through prospectiveMemberPositionCollection again, checking for the presence 
            //of a Position which subsumes position1. If such a state exists, remove position1
            //from the set (no longer satisfies membership requirements).
            Iterator it2 = prospectiveMemberPositionCollection.iterator();
            while(it2.hasNext())
            {
                Position position2 = (Position)it2.next();
                
                if(position2.subsumes(position1))
                {
                    it1.remove();
                    break;
                }
            }
            /////
        }
        /////
        
        //Convert prospectiveMemberPositionCollection to an array and sort it
        memberPositionArray = prospectiveMemberPositionCollection.toArray(new Position[prospectiveMemberPositionCollection.size()]);
        Arrays.sort(memberPositionArray);
        /////
    }
    
    
    
    /**
     * Retrieves the smallest boundary value held by a Position in this State's member set.
     * 
     * @return      an int of the smallest boundary value held by a Position in this State's member set
     */
    public int getMinimalBoundary()
    {
        return memberPositionArray[0].getI();
    }
    
    
    
    /**
     * Returns this State's member positions.
     
     * @return      an array of Positions contained in this State
     */
    public Position[] getMemberPositions()
    {
        return memberPositionArray;
    }
    
    
    
    /**
     * Procures an object representation of the characteristic vector of this State's relevant subword.
     
     * @param maxEditDistance       an int of the maximum number of edit operations desired
     * @param automatonString       the String containing the char sequence that the characteristic vector is to be based off
     * @param letter                the char that the characteristic vector is to be based off of
     * @return                      an AugBitSet representation of the characteristic vector of this State's relevant subword
     */
    public AugBitSet getRelevantSubwordCharacteristicVector(int maxEditDistance, String automatonString, char letter)
    {
        //Isolate the substring in automatonString that serves as this State's relevant subword
        int minimalPositionBoundary = memberPositionArray[0].getI();
        int relevantSubwordSize = Math.min(2 * maxEditDistance + 1, automatonString.length() - minimalPositionBoundary);
        String relevantSubword = automatonString.substring(minimalPositionBoundary, minimalPositionBoundary + relevantSubwordSize);
        /////

        //Create an AugBitSet that will be used represent the characteristic vector of relevantSubword
        AugBitSet relevantSubwordCharacteristicVector = new AugBitSet(relevantSubwordSize);
        
        //Loop through the chars in relevantSubword, setting the corresponding bit in
        //relevantSubwordCharacteristicVector to true if it is equal to letter, and false otherwise
        for(int i = 0; i < relevantSubwordSize; i++)
        {
            if(relevantSubword.charAt(i) == letter)
                relevantSubwordCharacteristicVector.set(i);
        }
        /////

        return relevantSubwordCharacteristicVector;
    }
    
    

    /**
     * Returns the State resulting from the processing of this State
     * and the characteristic vector of its relevant subword.
     
     * @param maxEditDistance                           an int of the maximum number of edit operations desired
     * @param relevantSubwordCharacteristicVector       an AugBitSet representation of the characteristic vector of this State's relevant subword
     * @return                                          the State resulting from the transitions of each of this State's member Positions 
     *                                                  (with regards to the arguments) or null if none of these transitions yields a non-failure State
     */
    public State transition(int maxEditDistance, AugBitSet relevantSubwordCharacteristicVector)
    {
        //HashSet which will hold the States resulting from transitioning each Position in memberPositionArray
        HashSet<State> newStateHashSet = new HashSet<State>();
        
        //The int which will be used to determine the index in relevantSubwordCharacteristicVector
        //that the relevant subwords of positions in memberPositionArray start at
        int boundaryOffset = memberPositionArray[0].getI();

        //Perform a transition on each member position, adding the resulting
        //State to newStateHashSet provided it isn't a failure state
        for(Position p : memberPositionArray)
        {
            State transitionState = p.transition(maxEditDistance, p.getI() - boundaryOffset, relevantSubwordCharacteristicVector);
            if(transitionState != null) newStateHashSet.add(transitionState);
        }
        /////

        return (newStateHashSet.isEmpty() ? null : new State(newStateHashSet));
    }
    
    
    
    /**
     * Returns the State resulting from the processing of this State, 
     * its relevant subword (in regards to a given String) and a given character.
     
     * @param maxEditDistance       an int of the maximum number of edit operations desired
     * @param automatonString       the String that the transition is to be based off of
     * @param letter                the letter that the transition is to be based off of
     * @return                      the State resulting from the transitions of each of this State's member Positions 
     *                             (with regards to the arguments) or null if none of these transitions yields a non-failure State
     */
    public State transition(int maxEditDistance, String automatonString, char letter)
    {
        return transition(maxEditDistance, getRelevantSubwordCharacteristicVector(maxEditDistance, automatonString, letter));
    }

    
    
    /**
     * Returns a String representation of this State.
     
     * @return      a String consisting of the String representation
     *              of this State's member positions
     */
    @Override
    public String toString()
    {
        String returnString = memberPositionArray[0].toString();
        
        int memberPositionCount = memberPositionArray.length;
        for(int i = 1; i < memberPositionCount; i++)
            returnString += " " + memberPositionArray[i].toString();
        
        return returnString;
    }
    
    
    
    /**
     * Determines if a given Position satisfies State membership requirements with every Position in a State. 
     
     * @param state                         a State
     * @param prospectivePosition           a Position which is subsumbed by a base position that
     *                                      also subsumes all members in {@code stateMemberPositionArray}
     * @return                              true if {@code prospectivePosition} is not subsumbed by
     *                                      any Positions in {@code stateMemberPositionArray}, false otherwise
     */
    public static boolean canBeState(State state, Position prospectivePosition)
    {
        //Loop through the Positions in state's member Position array, determining
        //(and returning false if) any subsume prospectivePosition
        for(Position currentMemberPosition : state.memberPositionArray)
        {
            if(currentMemberPosition.subsumes(prospectivePosition))
                return false;
        }
        /////
        
        return true;
    }
    
    
    
    /**
     * Determines the difference between the minimal boundary values contained by Positions in two States.
     
     * @param state1        a State
     * @param state2        a State
     * @return              an int denoting the difference between the minimal boundary
     *                      value of a Position in {@code state1} with that of {@code state2}
     */
    public static int getMinimumBoundariesDifference(State state1, State state2)
    {
        return Math.abs(state1.memberPositionArray[0].getI() - state2.memberPositionArray[0].getI());
    }
}
