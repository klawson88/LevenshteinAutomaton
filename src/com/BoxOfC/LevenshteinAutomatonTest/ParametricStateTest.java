/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
