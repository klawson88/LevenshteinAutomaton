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

import org.testng.TestNG;



/**
 *
 * @author Kevin
 */
public class Main
{
        /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestNG test = new TestNG();
        test.setTestClasses(new Class[]{/*PositionTest.class, StateTest.class, ParametricStateTest.class,*/ LevenshteinAutomatonTest.class});
        test.run();
       
    }
}
