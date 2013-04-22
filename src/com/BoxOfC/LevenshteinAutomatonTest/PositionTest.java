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

package com.BoxOfC.LevenshteinAutomatonTest;

import com.BoxOfC.LevenshteinAutomaton.AugBitSet;
import com.BoxOfC.LevenshteinAutomaton.Position;
import com.BoxOfC.LevenshteinAutomaton.State;
import java.util.ArrayList;
import java.util.Arrays;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;



/**
 *
 * @author Kevin
 */
@Test(enabled = false)
public class PositionTest 
{
    @DataProvider(name = "transitionInternalTestDp")
    public Object[][] transitionInternalTestDataProvider()
    {
        int numberOfTests = 1000;
        int maxE = 10;
        
        Object[][] newArgArrayContainerArray = new Object[numberOfTests][1];
        
        for(int i = 0; i < numberOfTests; i++)
        {
            int randomN = (int)(Math.random() * maxE);
            int randomI = (int)(Math.random() * (2 * randomN + 1));
            
            newArgArrayContainerArray[i] = new Object[]{new Position(randomI, randomN)};
        }
        
        
        return newArgArrayContainerArray;
    }
    
    public void matchTransitionAssertion(Position p, State s)
    {
        assert s.getMemberPositions()[0].equals(new Position(p.getI() + 1, p.getE())); 
    }
    
    public void insertionTransitionAssertion(Position p, State s)
    {
        assert s.getMemberPositions()[0].equals(new Position(p.getI(), p.getE() + 1)); 
    }
    
    public void defaultTransitionAssertion(Position p, State s)
    {
        Position assertPos0 = new Position(p.getI(), p.getE() + 1);
        Position assertPos1 = new Position(p.getI() + 1, p.getE() + 1);
        Position[] memberPositionArray = s.getMemberPositions();

        assert memberPositionArray[0].equals(assertPos0);
        assert memberPositionArray[1].equals(assertPos1);
    }
    
    public void deletionTransitionAssertion(Position p, State s, int hitIndex)
    {
        Position assertPos0 = new Position(p.getI(), p.getE() + 1);
        Position assertPos1 = new Position(p.getI() + 1, p.getE() + 1);
        Position assertPos2 = new Position(p.getI() + hitIndex + 1, p.getE() + (hitIndex + 1) - 1);
        Position[] memberPositionArray = s.getMemberPositions();

        assert memberPositionArray[0].equals(assertPos0);
        assert memberPositionArray[1].equals(assertPos1);
        assert memberPositionArray[2].equals(assertPos2);
    }
    
    
    @Test(dataProvider = "transitionInternalTestDp")
    public void transitionInternalTest(Position p)
    {
        
        for(int mdi = p.getE() + 1; mdi >= p.getE(); mdi--)
        {
            
            for(int rsi = 0; rsi <= 2; rsi++)
            {
                for(int hii = -1; hii <= 4; hii++)
                {
                    State s =  p.transitionInternal(mdi, rsi, hii);
                    
                    if(p.getE() < mdi)
                    {
                        switch(rsi)
                        {
                            case 0:             insertionTransitionAssertion(p, s); 
                                                break;
                            case 1:
                            {
                                switch(hii)
                                {
                                    case 0:     matchTransitionAssertion(p, s); break;
                                    default:    defaultTransitionAssertion(p, s); break;
                                }
                                break;
                            }
                            case 2:
                            {
                                switch(hii)
                                {
                                    case -1:    defaultTransitionAssertion(p, s); break;
                                    case 0:     matchTransitionAssertion(p, s); break;
                                    case 1:     deletionTransitionAssertion(p, s, hii); break;
                                }
                                break;
                            }
                        }
                    }
                    else
                    {
                        switch(rsi)
                        {
                            case 0:             assert (s == null);
                                                break;
                            case 1:
                            {
                                switch(hii)
                                {
                                    case 0:     matchTransitionAssertion(p, s); break;
                                    default:    assert (s == null); break;
                                }
                                break;
                            }
                            case 2:
                            {
                                switch(hii)
                                {
                                    case 0:     matchTransitionAssertion(p, s); break;
                                    default:    assert (s == null); break;
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        
    }
    
    
    @DataProvider(name = "getPositionTransitionDataDP")
    public Object[][] getPositionTransitionDataDataProvider()
    {
        int testCount = 1000;
        
        int maxN = 100;
        Object[][] argArrayContainerArray = new Object[testCount][];

        
        for(int i = 0; i < testCount; i++)
        {
            int randomN = (int)(Math.random() * maxN);
            int rsSize = 2 * randomN + 1;
            
            AugBitSet characteristicVec = new AugBitSet(rsSize);
            for(int j = 0; j < rsSize; j++) characteristicVec.set(j, ((int)(Math.random() * 2)) == 1);
            
            
            int randomStartIndex = (int)(Math.random() * rsSize) + (i % 2 == 0 ? 1 : 0);
            
            argArrayContainerArray[i] = new Object[]{randomN, randomStartIndex, characteristicVec};
        }
        
        return argArrayContainerArray;
        
    }
    
    
    
    @Test(dataProvider = "getPositionTransitionDataDP")
    public void getPositionTransitionDataTest(int maxEditDistance, int positionStartIndex, AugBitSet stateRSCharacteristicVector)
    {
        int stateRSSize = stateRSCharacteristicVector.getRelevantBitSetSize();
        
        int randomE = (int)(Math.random() * maxEditDistance);
        
        int[] transitionDataArray = (new Position(positionStartIndex, randomE)).procurePositionTransitionData(maxEditDistance, positionStartIndex, stateRSCharacteristicVector);
        
        assert transitionDataArray[0] == (positionStartIndex == stateRSSize ? 0 : Math.min(maxEditDistance - randomE + 1, stateRSSize - positionStartIndex));
        assert transitionDataArray[1] == (positionStartIndex == stateRSSize ? -1 : stateRSCharacteristicVector.get(positionStartIndex, positionStartIndex + Math.min(maxEditDistance - randomE + 1, stateRSCharacteristicVector.getRelevantBitSetSize() - positionStartIndex)).nextSetBit(0));
    }
    
    
    @Test
    public void subsumesTest()
    {
        
        for(int i = 0; i < 1000; i++)
        {
            int randomN1 = (int)(Math.random() * 10) + 1;
            int randomE1 = (int)(Math.random() * randomN1);
            int randomI1 = 2 * randomN1 + 1;
            
            int offset = (int)(Math.random() * randomE1) + 1;
            
            int randomE2 = randomE1 + offset;
            int randomI2 = randomI1 + (int)(Math.random() * offset);
            
            int randomE3 = randomE1 + offset;
            int randomI3 = randomI1 + offset + (int)(Math.random() * 10) + 1;
            
            Position p1 = new Position(randomI1, randomE1);
            Position p2 = new Position(randomI2, randomE2);
            Position p3 = new Position(randomI3, randomE3);
           
            assert (!p1.subsumes(p1) && p1.subsumes(p2) && !p2.subsumes(p1) && !p1.subsumes(p3));
        }        
    }
    
    
    @Test
    public void compareToTest()
    {
        
        for(int i = 0; i < 1000; i++)
        {
            int randomN1 = (int)(Math.random() * 10) + 1;
            int randomE1 = (int)(Math.random() * randomN1);
            int randomI1 = 2 * randomN1 + 1;
            
            int randomE2 = randomE1 + (int)(Math.random() * 5);
            int randomI2 = randomI1 - ((int)(Math.random() * 10) + 1);
            
            int randomE3 = randomE1- 1;
            int randomI3 = randomI1;
            
            Position p1 = new Position(randomI1, randomE1);
            Position p2 = new Position(randomI2, randomE2);
            Position p3 = new Position(randomI3, randomE3);
           
            assert (p1.compareTo(p2) > 0 && p2.compareTo(p1) < 0 && p3.compareTo(p1) < 0);
        }        
    }
    
    @Test
    public void equalsTest()
    {
        for(int i = 0; i < 1000; i++)
        {
            int eqN1 = (int)(Math.random() * 1000) + 1;
            int eqE1 = (int)(Math.random() * eqN1);
            int eqI1 = 2 * eqN1 + 1;
            
            int eqE2 = eqE1;
            int eqI2 = eqI1;
            
            assert (new Position(eqI1, eqE1).equals(new Position(eqI2, eqE2)));
        }
        
        for(int i = 0; i < 1000; i++)
        {
            int randomN1 = (int)(Math.random() * 1000) + 1;
            int randomE1 = (int)(Math.random() * randomN1);
            int randomI1 = 2 * randomN1 + 1;
            
            int randomE2 = randomE1;
            int randomI2 = randomI1;
            
            if(i % 2 == 0)
                randomE2 += (int)(Math.random() * 5) + 1;
            else
                randomI2 += + (int)(Math.random() * 5) + 1;

            assert !(new Position(randomI1, randomE1).equals(new Position(randomI2, randomE2)));
        }        
    }
}
