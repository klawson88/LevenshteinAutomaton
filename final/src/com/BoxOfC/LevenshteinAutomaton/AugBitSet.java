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

import java.util.BitSet;



/**
 * A BitSet which allows for the establishment of a subset
 * that size, equality, and hashing operations are based off of.

 * @author Kevin
 */
public class AugBitSet extends BitSet
{
    //Int which stores the size of the set of "relevant" bits, which is defined as
    //the set of bits in [0, rbSetSize). rbSetSize = max(initialSize, maxSetBitIndex)
    //where:        initialSize: explicit relevant bit set size (optionally set in constructor, default value of 0)
    //              maxSetBitIndex: maximum index containing a bit which has been set (to either true or false)
    private int relevantBitSetSize = 0;
    
    
    
    /**
     * Constructs an AugBitSet object.
     */
    public AugBitSet()
    {  
        super();  
    }  
   
    
   
    /**
     * Constructs an AugBitSet object with an explicit relevant bit set size.
     
     * @param relevantBitSetSize        an int denoting the initial desired size of the relevant bit set
     */
    public AugBitSet(int relevantBitSetSize)
    {  
        super(relevantBitSetSize);  
        this.relevantBitSetSize = relevantBitSetSize; 

    } 
   
   
   
   /**
    * Constructs an AugBitSet that is structurally equivalent to another.
    
    * @param bs     an object of a class in the BitSet class hierarchy
    */
   private AugBitSet(BitSet bs)
   {
       super();
       super.xor(bs);
   }
   
   
   
   /**
    * Creates an AugBitSet object that has its set of initial bits
    * set to values identical to those in a range in this AugBitSet.
    
    * @param fromIndex      an int denoting the start of the bit range of interest
    * @param toIndex        an int denoting one past the end of the bit range of interest
    * @return               an AugBitSet object that has the values of bits in the range 
    *                       [0, (toIndex - fromIndex)) identical to bits in the range [fromIndex, toIndex) of this AugBitSet
    */
   @Override
   public AugBitSet get(int fromIndex, int toIndex)
   {
       return new AugBitSet(super.get(fromIndex, toIndex));
   }
   
   
   
   /**
    * Sets the value of a bit at a given index to true.
    
    * @param bitIndex       an int denoting the operation's target index
    */
   @Override
   public void set(int bitIndex)
   {  
      relevantBitSetSize = Math.max(relevantBitSetSize, bitIndex);  
      super.set(bitIndex);  
   }
   
   
   
   /**
    * Sets the bit at a given index to a desired boolean value.
    
    * @param bitIndex       an int denoting the operation's target index
    * @param value          the desired boolean value of the bit at {@code bitIndex}
    */
   @Override
   public void set(int bitIndex, boolean value)
   {  
      relevantBitSetSize = Math.max(relevantBitSetSize, bitIndex);  
      super.set(bitIndex, value);  
   }
   
   
   
   /**
    * Retrieves the relevant bit set size of this AugBitSet.
    
    * @return       an int denoting the relevant bit size of this object
    */
   public int getRelevantBitSetSize()
   {  
      return relevantBitSetSize;  
   }
   
   
   
   /**
    * Determines the equivalence of this AugBitSet with another.
    * 
    * Two AugBitSets are equivalent iff their relevant bit set sizes are equal
    * and each corresponding index inside the sets contain equivalent values.
    
    * @return       true if obj is an AugBitSet object, its relevant bit set size
    *               is equal to that of this AugBitSet, and corresponding indices
    *               inside the two sets contain equal values; false otherwise 
    */
    @Override
    public boolean equals(Object obj)
    {
        boolean areEqual = (this == obj);
        
        if(!areEqual && obj != null && obj.getClass().equals(AugBitSet.class))
        {
            AugBitSet abs = (AugBitSet)obj;
            
            if(this.relevantBitSetSize == abs.relevantBitSetSize)
            {
                int i = 0;
                while(i < this.relevantBitSetSize && this.get(i) == abs.get(i)){i++;}            
                areEqual = (i == relevantBitSetSize);
            }
        }
        
        return areEqual;
    }

    
    
    /**
     * Returns a hashCode value for this AugBitSet. This value is based
     * on the values and indices of bits in its relevant bit set.
     
     * @return      an int of the hash code value of this AugBitSet
     */
    @Override
    public int hashCode() 
    {
        int hash = 7;
        
        for(int i = 0; i < relevantBitSetSize; i++) 
            hash = 83 * hash + (super.get(i) ? 1231 : 1237) + (i + 53);
        
        return hash;   
    }
    
    
    
    /**
     * Returns a String representation of this AugBitSet.
     
     * @return      a String of the binary values of the bits
     *              in this AugBitSet's relevant bit set, enclosed by "{}"
     */
    @Override
    public String toString()
    {
        StringBuilder strBuilder = new StringBuilder("{");
        for(int i = 0; i < relevantBitSetSize; i++) strBuilder.append(super.get(i) ? "1" : "0");
        return strBuilder.append("}").toString();
    }
}