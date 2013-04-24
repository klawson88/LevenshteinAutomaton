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

package com.BoxOfC.LevenshteinAutomatonTest;

import com.BoxOfC.LevenshteinAutomaton.ParametricState;
import com.BoxOfC.LevenshteinAutomaton.Position;
import com.BoxOfC.LevenshteinAutomaton.State;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;



/**
 *
 * @author Kevin
 */
public class ParametricStateTest 
{
    @DataProvider(name = "parametricStateTestDP")
    public Object[][] parametricStateTestDataProvider()
    {
        int testCount = 1000;
        
        Object[][] argArrayContainerArray = new Object[testCount][];
        
        for(int j = 0; j < testCount; j++)
        {
            int randomMemberCount = (int)(Math.random() * 100) + 1;
            
            ArrayList<Position> memberPositionArrayList = new ArrayList<Position>(100);
            int offset = (int)(Math.random() * 5) + 1;
            int baseE = (int)(Math.random() * 5);
            int baseI = (int)(Math.random() * 10);
            int originalBase = baseI + offset;

            for(int i = 0; i < randomMemberCount; i++)
            {
                int E =  baseE + (i % 2 == 0 ? (int)(Math.random() * offset) : 0);
                baseI += offset;

                memberPositionArrayList.add(new Position(baseI, E));
            }
            
            Collections.sort(memberPositionArrayList);
            State s = new State(memberPositionArrayList.toArray(new Position[0]));
            
            argArrayContainerArray[j] = new Object[]{originalBase, s};
        }
        
        return argArrayContainerArray;
    }
    
    @Test(dataProvider = "parametricStateTestDP")
    public void  parametricStateTest(int originalBase, State s)
    {
        ParametricState ts = new ParametricState(s);      
        State s1 = ts.createActualState(originalBase);
        
        assert Arrays.equals(s.getMemberPositions(), s1.getMemberPositions());
    }
    
}
