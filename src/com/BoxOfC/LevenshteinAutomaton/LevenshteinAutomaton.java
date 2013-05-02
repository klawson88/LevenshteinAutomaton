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

import com.BoxOfC.MDAG.MDAG;
import com.BoxOfC.MDAG.MDAGNode;
import com.BoxOfC.MDAG.SimpleMDAGNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;
import java.util.TreeSet;



/**
 * A utility class that can perform Levenshtein related functions
 * (edit distance calculation, fuzzy search, etc).
 
 * @author Kevin
 */
public class LevenshteinAutomaton 
{
    //A HashMap of Integers to HashMaps which will contain the transition HashMaps created for various edit distances
    private static final HashMap<Integer, HashMap<ParametricState, HashMap<AugBitSet, ParametricState>>> transitionHashMapContainerHashMap = new HashMap<>();
    
    //The State that will serve as the initial state that all automaton operations will start from
    private static final State initialState = new State(new Position[] {new Position(0, 0, false)});
    
    

   /**
     * Computes the edit distance between two Strings.
     
     * @param str1      a String
     * @param str2      a String
     * @return          an int of the edit distance between {@code str1} and {@code str2}
     */
    public static int computeEditDistance(String str1, String str2)
    {
        //Place the argument string lengths in local variables to bypass repeated method calls
        int str1Length = str1.length();
        int str2Length = str2.length();
        /////

        if (str1.equals(str2))       return 0;               //Strings are equal, no edits needed
        else if (str1Length == 0)    return str2.length();   //str1 is empty, edits are simply insertions of the chars of str2
        else if (str2Length == 0)    return str1.length();   //str2 is empty, edits are simply the insertions of the chars of str1
        
        //Normalize the cases of the argument string chars; we're going 
        //to have to do some actual work if we've reached this point
        //str1 = str1.toLowerCase();
        //str2 = str2.toLowerCase();
        /////

        //Create arrays representing columns of the edit-distance matrix this method creates. The
        //cells will contain the edit distances between 0-based str2 substrings of increasing size and 
        //the str1 substring bounded by its (str1's) previous and currently processing chars respectively.
        int[] ancestorMatrixCol = new int[str2Length + 1];
        int[] previousMatrixCol = new int[str2Length + 1];
        int[] currentMatrixCol = new int[str2Length + 1];
        /////
 
        //Calculate the edit distances for the first matrix column. The first COLUMN of the
        //matrix is associated with scenarios in which str1 is empty, in which case the
        //edit distance between it and a given str2 substring (str2.substring(0, index)) is simply "index"
        for (int i = 0; i < previousMatrixCol.length; i++) previousMatrixCol[i] = i;

        //Loop through the chars in str1, calculating the optimal edit distances between
        //the str1 substring bounded by each char, and all the 0-based str2 substrings.
        //In other words, loop through and fill the columns, top to bottom, of the edit distance matrix
        for (int i = 0; i < str1Length; i++)
        {
            //The first ROW of the matrix is associated with scenarios in which str2 is empty, in which case
            //the edit distance is between it and str1.substring(0, i) is simply i. The value is offset by 1  
            //to take in to account the first COLUMN of the matrix which corresponds to empty str1 scenarios.
            currentMatrixCol[0] = i + 1;

            //Loop through the chars in str2, calculating the optimal edit distances between 
            //the 0-based str2 substring bounded by each char and str1.substring(0, i).
            //In other words, fill from top to bottom, the matrix column indexed by i.
            for (int j = 0; j < str2Length; j++)
            {
                //Take the edit distance (contained in the neighboring upper matrix cell) calculated between the previous str2 
                //substring and the current str1 substring and increment it to represent a hypothetical necessary deletion in str2
                int deletionCost = currentMatrixCol[j] + 1;
                
                //Take the edit distance (contained in the neighboring left matrix cell) calculated between the current str2 substring 
                //and the previous str1 substring and increment it to represent a hypothetical necessary insertion to str2
                int insertionCost = previousMatrixCol[j + 1] + 1;
                
                //Determine the edit distance between the currently processing chars in str1 and str2
                int curCharEditDistance = (str1.charAt(i) == str2.charAt(j) ? 0 : 1);
                
                //Take the edit distance calculated between the previous str2 and str1 substrings and
                //increment it only if their currently processing chars differ (hypothetical necessary substitution)
                int substitutionCost = previousMatrixCol[j] + curCharEditDistance;
                
                //Determine the smallest edit operation cost among those currently associated with a deletion, insertion and substitution
                int minEditOperationCost = Math.min(deletionCost, insertionCost);
                minEditOperationCost = Math.min(minEditOperationCost, substitutionCost);
                /////
                
                //If the previous and currently processing chars of both strings are transposed, determine the smallest
                //edit operation cost between minEditOperationCost and that associated with a hypothetical transposition
                if(i > 0 && j > 0 && str1.charAt(i) == str2.charAt(j - 1) && str1.charAt(i - 1) == str2.charAt(j))
                    minEditOperationCost = Math.min(minEditOperationCost, ancestorMatrixCol[j-1] + curCharEditDistance);
                
                currentMatrixCol[j + 1] = minEditOperationCost;
            }
            /////

            //Copy the elements of currentMatrixCol to previousMatrixCol, priming them for the next iteration
            for (int j = 0; j < previousMatrixCol.length; j++)
            {
                ancestorMatrixCol[j] = previousMatrixCol[j];
                previousMatrixCol[j] = currentMatrixCol[j];
            }
               
        }
        /////
 
        return currentMatrixCol[str2Length];
    }
    
    
    
    /**
     * Determines if two Strings are within a specified edit distance of one another, using a dynamic programming approach.
     
     * @param maxEditDistance       an int denoting the maximum amount of edit operations allowed
     *                              to be used to (hypothetically) turn one String in to another
     * @param str1                  a String
     * @param str2                  a String    
     * @return                      true if {@code str1} and {@code str2} are within {@code maxEditDistance}
     *                              edit operations of each other; false otherwise     
     */
    public static boolean isWithinEditDistanceNonAutomaton(int maxEditDistance, String str1, String str2)
    {
        return (computeEditDistance(str1, str2) <= maxEditDistance);
    }
    
    
    
    /**
     * Determines if two Strings are within a specified edit distance of one another. 
     * This method is too slow to use in production: use isWithinEditDistanceNonAutomaton instead.
     
     * @param maxEditDistance       an int denoting the maximum amount of edit operations allowed
     *                              to be used to (hypothetically) turn one String in to another
     * @param str1                  a String
     * @param str2                  a String    
     * @return                      true if {@code str1} and {@code str2} are within {@code maxEditDistance}
     *                              edit operations of each other; false otherwise     
     */
    public static boolean isWithinEditDistance(int maxEditDistance, String str1, String str2)
    {
        State currentState = initialState;
        int automatonStringCharCount = str1.length();
        int dictionaryStringCharCount = str2.length();
        
        //Loop through the chars in str2, using each along with maxEditDistance and automatonString
        //to execute a transition on currentState until either a failure or accept State is reached
        for(int i = 0; i < dictionaryStringCharCount; i++)
        {           
            currentState = currentState.transition(maxEditDistance, str1, str2.charAt(i));
            if(currentState == null) return false;
        }
        /////
        
        return isAcceptState(currentState, automatonStringCharCount, maxEditDistance);
    }
    
    
    
    /**
     * Procures with a collection of commonly based States and a given position, 
     * those States which can be formed using the given position and the set 
     * of member positions appearing in the aforementioned State collection.
     
     * @param basedStateLinkedList      a LinkedList of States collectively based by a given Position
     * @param p                         a Position subsumed by the same Position which bases 
     *                                  each of the elements in {@code basedStateLinkedList}    
     */
    private static void procureNewBasedStates(LinkedList<State> basedStateLinkedList, Position p, int maxEditDistance)
    {
        //LinkedList which will hold all the States based by basePosition that contain currentPosition
        LinkedList<State> newBasedStateLinkedList = new LinkedList<State>();

        //Add a State with currentPosition as its sole member to newBasedLinkedList
        newBasedStateLinkedList.add(new State(new Position[] {p}));

        //Loop through the States in basedStateLinkedList, and for each, determine if its 
        //members Positions can form a State with currentPosition. If so, create a new State
        //with these set of Positions as members and add it to newBasedLinkedList
        for(State currentState : basedStateLinkedList)
        {
            if(State.canBeState(currentState, p, maxEditDistance))
                newBasedStateLinkedList.add(new State(currentState, p));
        }
        /////

        //Add all the newly created states to basedStateLinkedList
        basedStateLinkedList.addAll(newBasedStateLinkedList);
    }
    
    
    
    /**
     * Procures the collection of states that are based by a position,
     * given a maximum relevant subword size of an automaton state and
     * the max edit distance of an automaton.
     
     * @param basePosition                  the Position that State procurement will be based on
     * @param maxRelevantSubwordSize        an int denoting the max relevant subword size of a State
     *                                      in a Levenshtein automaton (based on {@code maxEditDistance}
     * @param maxEditDistance               an int denoting the maximum amount of edit 
     *                                      operations allowed by a specific automaton
     * @return                              a LinkedList of States that are based by basePosition
     */
    private static LinkedList<State> procureBasedStates(Position basePosition, int maxRelevantSubwordSize, int maxEditDistance)
    {
        //LinkedList which will hold all the States that are based by basePosition
        LinkedList<State> basedStateLinkedList = new LinkedList<State>();
        basedStateLinkedList.add(new State(new Position[] {basePosition}));

        //Loop through the set of all possible edit distances and boundaries to create 
        //Positions which will be used to creates States that are based by basePosition
        for(int e = 1; e <= maxEditDistance; e++)
        {  
            for(int i = 0; i < maxRelevantSubwordSize; i++)
            {
                //Create a standard position from the current processing edit distance and boundary. If it is
                //subsumed by basePosition, use it to insert States in to basedStateLinkedList that can be formed
                //solely by it and the set of Positions appearing as members in the elements of basedStateLinkedList 
                Position currentPosition = new Position(i, e, false);
                if(basePosition.subsumes(currentPosition, maxEditDistance))
                    procureNewBasedStates(basedStateLinkedList, currentPosition, maxEditDistance);
                /////
                
                //If the t-position with the current processing edit distance and boundary is subsumed by basePosition,
                //use it to insert States in to basedStateLinkedList in a manner identical to that used above
                if(i <= (maxRelevantSubwordSize - 2))
                {
                    Position currentTPosition = new Position(i, e, true);   
                    if(basePosition.subsumes(currentPosition, maxEditDistance))
                        procureNewBasedStates(basedStateLinkedList, currentTPosition, maxEditDistance);
                }
                /////
            }
        }
        /////
        
        return basedStateLinkedList;
    }
    
    
    
    /**
     * Procures the collection of ParametricStates collectively 
     * representing the forms of every State in a Levenshtein automaton.
     
     * @param maxRelevantSubwordSize        an int denoting the max relevant subword size of a State
     *                                      in a Levenshtein automaton (based on {@code maxEditDistance}
     * @param maxEditDistance               an int denoting the maximum amount of edit 
     *                                      operations allowed by specific automaton
     * @return                              a HashSet of Parametric states collectively representing the forms of every
     *                                      State in the automaton defined by maxRelevantSubwordSize and maxEditDistance
     */
    private static HashSet<ParametricState> procureParametricStates(int maxRelevantSubwordSize, int maxEditDistance)
    {
        //HashSet which will contain ParametricState objects representing the forms of every 
        //state in the automaton defined by maxRelevantSubwordSize and maxEditDistance
        HashSet<ParametricState> parametricStateHashSet = new HashSet<ParametricState>();
        
        //Loop through the possible relevant subword sizes, collectively using them 
        //to create the set of all possible States, and in turn, ParametricStates
        //in the automaton defined by maxRelevantSubwordSize and maxEditDistance
        for(int i = 0; i < maxRelevantSubwordSize; i++)                         
        {
            //Create a base state from the current relevant subword size
            Position basePosition = new Position(i, 0, false);
            
            //Get the States based by basePosition
            LinkedList<State> basedStateLinkedList = procureBasedStates(basePosition, maxRelevantSubwordSize, maxEditDistance);
            
            //Add the parametric versions of each state in basedStateLinkedList to parametricStateHashSet
            for(State currentNewState : basedStateLinkedList) parametricStateHashSet.add(new ParametricState(currentNewState));
        }
        /////
        
        return parametricStateHashSet;
    }

    
    
    /**
     * Produces all the binary permutations with bit counts less than or equal to a given value.
     
     * @param maxBitCount       an int denoting the maximum bit count of a permutation to be produced
     * @return                  an ArrayList of AugBitSets that collectively represent the set of 
     *                          binary permutations of bit counts from 0 to {@code maxBitCount}
     */
    private static ArrayList<AugBitSet> produceBinaryPermutations(int maxBitCount)
    {
        //Determine the total number of permutations to be produced
        int totalPermutationCount = 0;
        for(int i = 1; i < maxBitCount + 1; i++) totalPermutationCount += Math.pow(2, i);
        /////
        
        //ArrayList of AugBitSets which will each represent a binary permutation. "+1" accomodates the empty permutation
        ArrayList<AugBitSet> binaryPermutationContainerArrayList = new ArrayList<AugBitSet>(totalPermutationCount + 1);   
        
        //Add the AugBitSet representstions of the permutations {0} and {1} to binaryPermutationContainerArrayList
        for(int i = 0; i <= 1; i++)
        {
            binaryPermutationContainerArrayList.add(new AugBitSet(1));
            binaryPermutationContainerArrayList.get(binaryPermutationContainerArrayList.size() - 1).set(0, (i == 0 ? false : true));
        }
        /////
        
        //Loop through the AugBitSets in binaryPermutationContainerArrayList, using each to create
        //new permutations (by separately adding a and 1 to the end of each) that are added to the end of
        //the ArrayList. This is done until one is encountered that has a size equal to maxRelevantSubwordSize
        for(int i = 0; binaryPermutationContainerArrayList.get(i).getRelevantBitSetSize() < maxBitCount; i++)
        {
            //Loop through the set [0,1], using each value to create a new binary permutation
            //by appending the value to the end of a copy of the permutation located at i
            for(int j = 0; j <= 1; j++)
            {
                AugBitSet currentPermutationBitSet = binaryPermutationContainerArrayList.get(i);
                AugBitSet newPermutationBitSet = new AugBitSet(currentPermutationBitSet.getRelevantBitSetSize() + 1);
                
                newPermutationBitSet.xor(currentPermutationBitSet);     //copies the relevant bits in currentPermutationBitSet
                newPermutationBitSet.set(newPermutationBitSet.getRelevantBitSetSize() - 1, (j == 0 ? false : true));
                
                binaryPermutationContainerArrayList.add(newPermutationBitSet);  
            }
        }
        /////
        
        binaryPermutationContainerArrayList.add(new AugBitSet(0));  //Add the empty permutation as well
        return binaryPermutationContainerArrayList;
    }
    
    

    /**
     * Creates a Map containing the transition relationships between the parameterized
     * versions of States in an automaton defined by a given max edit distance.
     
     * @param maxEditDistance       an int denoting the maximum amount of edit 
     *                              operations allowed by specific automaton
     */
    private static void createParametricStateTransitionMap(int maxEditDistance)
    {
        if(!transitionHashMapContainerHashMap.containsKey(maxEditDistance))     //if the transition map for maxEditDistance does not yet exist
        {
            //HashMap which will store the transition relationships between the parametrized versions of States in the automaton described by maxEditDistance
            HashMap<ParametricState, HashMap<AugBitSet, ParametricState>> transitionHashMap = new HashMap<ParametricState, HashMap<AugBitSet, ParametricState>>();
        
            int maxRelevantSubwordSize = 2 * maxEditDistance + 1;
            
            //Procure the set of ParametricStates collectively describing the forms of all the States in the automaton defined by maxRelevantSubwordSize and maxEditDistance
            HashSet<ParametricState> parametricStateHashSet = procureParametricStates(maxRelevantSubwordSize, maxEditDistance);
            
            //Produce all the possible characteristic vectors of sizes 0 to maxEditDistance
            ArrayList<AugBitSet> characteristicVectorPermutationContainerArrayList = produceBinaryPermutations(maxRelevantSubwordSize);

            //Loop through possible characteristic vectors and ParametricStates, using them to
            //create transition relationships that will be represented and stored in transitionHashMap
            for(AugBitSet characteristicVector : characteristicVectorPermutationContainerArrayList)
            {
                for(ParametricState currentParametricState : parametricStateHashSet)
                {
                    //If States of the form described by currentParametricState have Position 
                    //sets that can fit in a relevant subword of the form descirbed by characteristicVector
                    if(currentParametricState.getLargestPositionOffset() <= characteristicVector.getRelevantBitSetSize())
                    {
                        //Use currentParametricState to create a dummy state with a minimum boundary of zero
                        State currentConcreteState = currentParametricState.createActualState(0);
                        
                        //Execute a transiton on currentConcreteState given maxEditDistance and characteristicVector
                        State transitionState = currentConcreteState.transition(maxEditDistance, characteristicVector);

                        ParametricState transitionParametricState = null;

                        //If the transition yields a non-failure State, get the result State's ParametricState
                        if(transitionState != null)
                        {
                            int minimumBoundariesDifference = State.getMinimumBoundariesDifference(currentConcreteState, transitionState);
                            transitionParametricState = new ParametricState(transitionState, minimumBoundariesDifference);
                        }
                        /////

                        //Store the transition relationship between currentParametricState, 
                        //characteristicVector, and transitionParametricState in transitionHashMap
                        if(!transitionHashMap.containsKey(currentParametricState))
                            transitionHashMap.put(currentParametricState, new HashMap<AugBitSet, ParametricState>());

                        transitionHashMap.get(currentParametricState).put(characteristicVector, transitionParametricState); 
                        /////
                    }
                    /////
                }
            }

            transitionHashMapContainerHashMap.put(maxEditDistance, transitionHashMap);
        }
        /////

    }
    
    

    /**
     * Creates an array of objects collectively representing a processing step in an automaton-based fuzzy search.
     
     * @param transitionPathString                  the sequence of characters used to for transition to the to-be-saved processing step
     * @param mdagTransitionPathEndNode             the MDAGNode of interest in the to-be-saved processing step
     * @param lAutomatonTransitionPathEndState      the State of interest in the to-be-saved processing step
     * @return                                      an array containing {@code transitionPathString}, {@code mdagTransitionPathEndNode},
     *                                              and {@code lAutomatonTransitionPathEndState}
     */
    private static Object[] createProcessingStepStackEntry(String transitionPathString, Object mdagTransitionPathEndNode, State lAutomatonTransitionPathEndState)
    {
        Object[] processingStepDataArray = new Object[3];
        processingStepDataArray[0] = transitionPathString;
        processingStepDataArray[1] = mdagTransitionPathEndNode;
        processingStepDataArray[2] = lAutomatonTransitionPathEndState;

        return processingStepDataArray;
    }
    
    
    
    /**
     * Creates an array of objects collectively representing a processing step in a tabled-based fuzzy search.
     
     * @param transitionPathString                              the sequence of characters used to for transition to the to-be-saved processing step
     * @param mdagTransitionPathEndNode                         the MDAGNode of interest in the to-be-saved processing step
     * @param lAutomatonTransitionPathEndState                  the State of interest in the to-be-saved processing step
     * @param lAutomatonTransitionPathEndParametricState        the ParametricState of interest in the to-be-saved processing step
     * @return                                                  an array containing {@code transitionPathString}, {@code mdagTransitionPathEndNode},
     *                                                          and {@code lAutomatonTransitionPathEndState}
     */
    private static Object[] createProcessingStepStackEntry(String transitionPathString, Object mdagTransitionPathEndNode,
            State lAutomatonTransitionPathEndState, ParametricState lAutomatonTransitionPathEndParametricState)
    {
        Object[] processingStepDataArray = new Object[4];
        processingStepDataArray[0] = transitionPathString;
        processingStepDataArray[1] = mdagTransitionPathEndNode;
        processingStepDataArray[2] = lAutomatonTransitionPathEndState;
        processingStepDataArray[3] = lAutomatonTransitionPathEndParametricState;
        
        return processingStepDataArray;
    }
    
    
    
     /**
     * Searches a collection of Strings for those which are within a given edit distance from a particular string.
     * This version of fuzzy search traverses through the States of a Levenshtein automata,
     * and will be the slowest of all the fuzzy search methods in most use cases
     
     * @param maxEditDistance       an int denoting the maximum amount of edit operations that can separate
     *                              a String in the to-be-searched collection with the String of interest
     * @param automatonString       the String that all edit-distance calculations are to be carried out in relation to
     * @param mdag                  an MDAG containing the set of Strings to be processed against {@code automatonString}
     * @return                      a LinkedList containing all the Strings in {@code mdag} that are at most
     *                              {@code maxEditDistance} away from {@code automatonString}
     */
    public static LinkedList<String> iterativeFuzzySearch(int maxEditDistance, String automatonString, MDAG mdag)
    {
        //LinkedList which will contain Strings in mdag that are within maxEditDistance of automatonString
        LinkedList<String> resultStringLinkedList = new LinkedList<String>();
        
        //Stack to store collections of objects which collectively represent steps in the search process
        Stack<Object[]> processingStepStack = new Stack<Object[]>();
        
        boolean mdagIsSimplified = mdag.getSourceNode().getClass().equals(SimpleMDAGNode.class);
        SimpleMDAGNode[] simpleMDAGArray = mdag.getSimpleMDAGArray();
        
        //Push onto processingStepStack collection of objects which represent the start step of the search process
        processingStepStack.push(createProcessingStepStackEntry("", mdag.getSourceNode(), initialState));
        
        //Retrieve the set of characters composing the Strings in mdag
        TreeSet<Character> charTreeSet = mdag.getTransitionLabelSet();
        int charCount = charTreeSet.size();
        /////

        //Iterate through charTreeSet, inserting each char in to charArray
        int counter = 0;
        char[] charArray = new char[charCount];
        for(Character c : charTreeSet) charArray[counter++] = c.charValue();
        /////
        
        //Transition through the MDAG and automaton in-sync, adding to resultStringLinkedList
        //the char sequences that lead to both an accept node (MDAG) and accept state (automaton)
        while(!processingStepStack.isEmpty())
        {
            //Pop the processing step at the top of the stack and re-cast its contents
            Object[] currentProcessingStepDataArray = processingStepStack.pop();  
            
            Object currentNodeObj = currentProcessingStepDataArray[1];
            State currentState = (State)currentProcessingStepDataArray[2];
            /////

            //Loop through the chars in charArray, using each to transition the node & state in the
            //processing step at the top of the stack. If both the node and state have valid
            //transitions on a particular char, push the resulting transition String, node,
            //and State on the top of the stack
            for(int i = 0; i < charCount; i++)
            {
                char currentChar = charArray[i];
                
                //Execute a transition on the current MDAG node using currentChar
                Object transitionNode = (mdagIsSimplified ? ((SimpleMDAGNode)currentNodeObj).transition(simpleMDAGArray, currentChar) : ((MDAGNode)currentNodeObj).transition(currentChar));
                
                if(transitionNode != null)
                {
                    //Execute a transition on the current automaton State using currentChar
                    State transitionState = currentState.transition(maxEditDistance, automatonString, currentChar);

                    if(transitionState != null)
                    {
                        //Push the resulting processing step on to the top of processingStepStack
                        String transitionPathString = (String)currentProcessingStepDataArray[0] + currentChar;
                        processingStepStack.push(createProcessingStepStackEntry(transitionPathString, transitionNode, transitionState));
                        /////

                        //If both transitionNode and transitionState are "accepting", add the sequence of chars that lead to them to resultStringLinkedList
                        if(MDAG.isAcceptNode(transitionNode) && isAcceptState(transitionState, automatonString.length(), maxEditDistance))
                            resultStringLinkedList.add(transitionPathString);
                    }
                }
            }
            ///// 
        }
        /////
        
        return resultStringLinkedList;
    }
    
    

    /**
     * Searches a collection of Strings for those which are within a given edit distance from a particular String.
     * 
     * This version of fuzzy search uses a pre-computed map of transitions, and is the fastest method for max 
     * edit distances less than or equal 2. Greater max edit distances require huge amounts of memory and 
     * are not guaranteed to be faster than a non-automaton approach, so use in those cases is discouraged.
     
     * @param maxEditDistance       an int denoting the maximum amount of edit operations that can separate
     *                              a String in the to-be-searched collection with the String of interest
     * @param automatonString       the String that all edit-distance calculations are to be carried out in relation to
     * @param mdag                  an MDAG containing the set of Strings to be processed against {@code automatonString}
     * @return                      a LinkedList containing all the Strings in {@code mdag} that are at most
     *                              {@code maxEditDistance} away from {@code automatonString}
     */
    public static LinkedList<String> tableFuzzySearch(int maxEditDistance, String automatonString, MDAG mdag)
    {
        //LinkedList which will contain Strings in mdag that are within maxEditDistance of automatonString
        LinkedList<String> resultStringLinkedList = new LinkedList<String>();
        
        //HashMap containing the transition relationships between the parametric states of an automaton with maxEditDistance
        createParametricStateTransitionMap(maxEditDistance);
        HashMap<ParametricState, HashMap<AugBitSet, ParametricState>> transitionHashMap = transitionHashMapContainerHashMap.get(maxEditDistance);
        
        //Stack to store collections of objects which collectively represent steps in the search process
        Stack<Object[]> processingStepStack = new Stack<Object[]>();

        boolean mdagIsSimplified = mdag.getSourceNode().getClass().equals(SimpleMDAGNode.class);
        SimpleMDAGNode[] simpleMDAGArray = mdag.getSimpleMDAGArray();
        
        //Push onto processingStepStack the collection of objects which represent the start step of the search process
        processingStepStack.push(createProcessingStepStackEntry("", mdag.getSourceNode(), initialState, new ParametricState(initialState)));
        
        //Retrieve the set of characters composing the Strings in mdag
        TreeSet<Character> charTreeSet = mdag.getTransitionLabelSet();
        int charCount = charTreeSet.size();
        /////
        
        //Iterate through charTreeSet, inserting each char in to charArray
        int counter = 0;
        char[] charArray = new char[charCount];
        for(Character c : charTreeSet) charArray[counter++] = c.charValue();
        /////
        
        //Transition through the MDAG and the automaton represented by transitionHashMap in-sync, adding to 
        //resultStringLinkedList the char sequences that lead to both an accept node (MDAG) and accept state (transition map)
        while(!processingStepStack.isEmpty())
        {
            //Pop the processing step at the top of the stack and re-cast its contents
            Object[] currentProcessingStepDataArray = processingStepStack.pop(); 
            
            Object currentNodeObj = currentProcessingStepDataArray[1];
            State currentState = (State)currentProcessingStepDataArray[2];
            ParametricState currentParametricState = (ParametricState)currentProcessingStepDataArray[3];
            /////
            
            //Loop through the chars in charArray, using each to transition the node & state in the
            //processing step at the top of the stack. If both the node and state have valid
            //transitions on a particular char, push the resulting transition String, node,
            //and State on the top of the stack
            for(int i = 0; i < charCount; i++)
            {
                char currentChar = charArray[i];

                //Execute a transition on the current MDAG node using currentChar     
                Object transitionNode = (mdagIsSimplified ? ((SimpleMDAGNode)currentNodeObj).transition(simpleMDAGArray, currentChar) : ((MDAGNode)currentNodeObj).transition(currentChar));
                
                if(transitionNode != null)
                {
                    //Get currentState's relevant subword (with respect to currentChar) and  
                    //use it to get the parametric version of the transition's result State
                    AugBitSet rscv = currentState.getRelevantSubwordCharacteristicVector(maxEditDistance, automatonString, currentChar);  
                    ParametricState transitionParametricState = transitionHashMap.get(currentParametricState).get(rscv);
                    /////

                    if(transitionParametricState != null)
                    {
                        //Use transitionParametricState to create the actual State that is the result of the transition
                        State transitionState = transitionParametricState.createActualState(currentState.getMinimalBoundary() + transitionParametricState.getTransitionBoundaryOffset());
                        
                        String transitionPathString = (String)currentProcessingStepDataArray[0] + currentChar;
                        
                        //Push the resulting processing step on to the top of processingStepStack
                        processingStepStack.push(createProcessingStepStackEntry(transitionPathString, transitionNode, transitionState, transitionParametricState));

                        //If both transitionNode and transitionState are "accepting", add the sequence of chars that lead to them to resultStringLinkedList
                        if(MDAG.isAcceptNode(transitionNode) && isAcceptState(transitionState, automatonString.length(), maxEditDistance))
                            resultStringLinkedList.add(transitionPathString);
                    }
                }
            }
            /////
        }
        /////
 
        return resultStringLinkedList;
    }

    
    
    /**
     * Searches a collection of Strings for those which are within a given edit distance from a particular String.
     * 
     * This version of fuzzy search uses a dynamic programming approach, and its usage
     * is advised for edit distances of 3 or greater (at those edit distances, in most cases,
     * it is faster than an automaton based approach and does not require a large amount of memory)
     
     * @param maxEditDistance       an int denoting the maximum amount of edit operations that can separate
     *                              a String in the to-be-searched collection with the String of interest
     * @param pertinentStr          the String that all edit-distance calculations are to be carried out in relation to
     * @param strCollection         a Collection containing the set of Strings to be processed against {@code pertinentStr}
     * @return                      a LinkedList containing all the Strings in {@code mdag} that are at most
     *                              {@code maxEditDistance} away from {@code automatonString}
     */
    public static LinkedList<String> fuzzySearchNonAutomaton(int maxEditDistance, String pertinentStr, Collection<String> strCollection)
    {
        LinkedList<String> resultStringLinkedList = new LinkedList<String>();

        //Loop through the Strings in strCollection, computing the edit distance between each and
        //pertinentStr and adding those to resultStringLinkedList with a distance <= maxEditDistance
        for(String currentString : strCollection)
        {
            if(computeEditDistance(pertinentStr, currentString) <= maxEditDistance)
                resultStringLinkedList.add(currentString);
        }
        /////
        
        return resultStringLinkedList;
    }
    
    
    
    /**
     * Determines if a Position is accepting.
     
     * @param p                             a Position
     * @param automatonStringLength         the String that the parent automaton is carrying 
     *                                      all edit distance calculations in relation to
     * @param automatonMaxEditDistance      an int denoting the max edit distance of {@code p}'s parent automaton
     * @return                              true if {@code p} is an accepting position; false otherwise
     */
    private static boolean isAcceptingPosition(Position p, int automatonStringLength, int automatonMaxEditDistance)
    {
        return (automatonStringLength - p.getI() <= automatonMaxEditDistance - p.getE());
    }
    
        
    
     /**
     * Determines if a State is accepting.
     
     * @param s                             a State
     * @param automatonStringLength         the String that the parent automaton is carrying 
     *                                      all edit distance calculations in relation to
     * @param automatonMaxEditDistance      an int denoting the max edit distance of {@code s}'s parent automaton
     * @return                              true if {@code s} is an accepting State; false otherwise
     */
    public static boolean isAcceptState(State s, int automatonStringLength, int automatonMaxEditDistance)
    {
        for(Position p : s.getMemberPositions())
        {
            if(isAcceptingPosition(p, automatonStringLength, automatonMaxEditDistance)) return true;
        }
     
        return false;
    }
    
    
    
    /**
     * Prints the contents of the transition map created for a particular maximum edit distance.
     
     * @param maxEditDistance       an int denoting the edit distance related to the transition map to-be-printed
     */
    public static void printTransitionMap(int maxEditDistance)
    {
        HashMap<ParametricState, HashMap<AugBitSet, ParametricState>> transitionHashMap = transitionHashMapContainerHashMap.get(maxEditDistance);
        
        if(transitionHashMap != null)       //if a transition map was created for maxEditDistance
        {
            System.out.println("# of parametric states : " + transitionHashMap.size());
        
            //Print out the contents of each HashMap<AugBitSet, ParametricState> keyed by each Parametric state in transitionHashMap
            for(Map.Entry<ParametricState, HashMap<AugBitSet, ParametricState>> transitionColumn : transitionHashMap.entrySet())
            {
                System.out.println(transitionColumn.getKey() + "\n");

                for(Map.Entry<AugBitSet, ParametricState> transitionEntry : transitionColumn.getValue().entrySet())
                    System.out.println(transitionEntry.getKey().toString() + "\t" + transitionEntry.getValue());
            }
            /////
        }
    }
}
