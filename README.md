##About

LevenshteinAutomaton is a fast and comprehensive Java library capable of performing automaton and non-automaton 
based Damerau-Levenshtein distance determination and neighbor calculations. The collection of related utilities affords developers 
the luxury of choosing the most appropriate edit distance-related procedure for their needs at any given time.
 
The library is capable of the following:

- Edit-distance determination (via dynamic programming) 
- Edit-distance neighbor determination (via dynamic programming)
- Edit-distance neighbor dictionary search (via table, dynamic programming, or automaton traversal)
 
The code well structured, easy to follow, and extensively commented for the benefit of developers 
seeking to understand the implemented data structures and algorithms, as well as developers seeking
to add homogeneous, functionality-extending code with ease.

The code has also been fully tested for correct functionality and performance.

##How to use

    //Determine the edit distance between two words
    int editDistance = LevenshteinAutomaton.computeEditDistance("tree", "trees"); //1
    
    //Determine if two strings are within a given edit distance of each other
	boolean areLDNeighbors = LevenshteinAutomaton.areWithinEditDistanceNonAutomaton(1, "tree", "trees"); //true
	
	//Create dictionary-containing data structures
	ArrayList<String> myArrayList = new ArrayList(Arrays.asList(new String[]{"bush", "bushes", "tree", "trees"}));
	MDAG myMDAG = new MDAG(myArrayList);
	/////
	
	//Detemine via table all of the Strings in the our dictionary that are within a edit distance 2 from "tree"  (ideal use cases: edit distances <= 2)
	LinkedList<String> ldNeighborsLinkedList = LevenshteinAutomaton.tableFuzzySearch(2, "tree", myMDAG); //"tree", "trees"
	
	//Detemine via dynamic programming all of the Strings in the our dictionary that are within a edit distance  2 from "tree" (ideal use cases: low memory capacity, edit distances >= 2)
	LinkedList<String> ldNeighborsLinkedList = LevenshteinAutomaton.fuzzySearchNonAutomaton(2, "tree", myArrayList); //"tree", "trees"
	
	//Detemine via automaton traversal all of the Strings in the our dictionary that are within a edit distance  2 from "tree"
	LinkedList<String> ldNeighborsLinkedList = LevenshteinAutomaton.iterativeFuzzySearch(2, "tree", myMDAG); //"tree", "trees"
	
##Repo contents

- **src**: Contains the source code for unit & integration tests as well as modified LevensheinAutomaton code with exclusive debugging methods and permissive access modifiers on existing methods to facilitate testing	
- **dist**: Contains the test suite, test library, and MDAG jars
- **final**: Contains src and dist folders housing production-ready LevenshteinAutomaton source and jar files respectively (as well as necessary MDAG jar file)

##Licensing and usage information

LevenshteinAutomaton is licensed under the Apache License, Version 2.0.

Informally, It'd be great to be notified of any derivatives or forks (or even better, issues or refactoring points that may inspire one)!

More informally, it'd **really** be great to be notified any uses in open-source, educational, or (if granted a license) commercial contexts.
Help me build my portfolio, if you found the library helpful it only takes an e-mail!

##References

- Fast String Correction with Levenshtein-Automata (2002) by Klaus Schulz , Stoyan Mihov
  (http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.16.652)
  (Presented algorithm used to create automaton-traversal and table-based dictionary search implementations)
  
- Damn Cool Algorithms: Levenshtein Automata by Nick Johnson
  (http://blog.notdot.net/2010/07/Damn-Cool-Algorithms-Levenshtein-Automata)
  (Layman's explanation of Levenshtein automata and related search algorithms)
  
- Lucene's FuzzyQuery is 100 times faster in 4.0 by Mike Mccandless
  (http://blog.mikemccandless.com/2011/03/lucenes-fuzzyquery-is-100-times-faster.html)
  (Inspiration for the creation of the library)