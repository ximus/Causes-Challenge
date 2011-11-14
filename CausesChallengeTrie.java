import java.util.ArrayList;
import java.util.Scanner;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.io.File;

class CausesChallengeTrie {
    
    public static void main(String[] args) throws Exception {
        
        // Get the input data set from file
        Scanner s = new Scanner(new File(args[0]));
        ArrayList<String> wordList = new ArrayList<String>();
        
        // I know how many words are in the data set
        wordList.ensureCapacity(264061);

        while (s.hasNext()) {
            wordList.add(s.next());
        }

        s.close();
        
        // This is the main workhorse
        WordNetwork wordNetwork = new WordNetwork(wordList);
        
        System.out.println(
            wordNetwork.discoverNetwork("causes").size()
        );
    }

      
}

class WordNetwork {
    
    // The tree data structure holding the data set
    private final Trie theTrie;
    
    // Words of friends if their edit distance is 1
    static public final int FRIENDSHIP = 1;
    
    public WordNetwork(List<String> words) { 
        
        // Create the trie
        theTrie = new Trie();
        
        // Populate it with the input data set
        for ( String word : words) {
            theTrie.insert(word);
        }
    }
    
    /**
     * Discover a word's network.
     * 
     * @param String word - The word who's network you seek to discover
     */
    public HashSet<String> discoverNetwork(String word) {
        
        // Holds the resulting network of word's friends, I guess it should be about 80 000 in length
        HashSet<String> network   = new HashSet<String>(80000);
        // As we discover new friends, we queue them in this buffer before we go discover their friends
        ArrayList<String> buffer  = new ArrayList<String>(80000);
        
        // We begin our search with `word`
        buffer.add(word);
        
        String friend;
        
        // While there are friends who's friends we have not discovered, discover them
        while (!buffer.isEmpty()) {
            friend = buffer.get(0);
            buffer.remove(0);
            // We don't want to *re*-discover anyone's friends 
            if (network.contains(friend)) continue;
            // Discover his friends and append them to the buffer
            buffer.addAll(discoverFriends(friend));
            // And add him to word's network
            network.add(friend);
            // System.out.println(network.size());
        }
        
        return network;
    }
    
    // [Performance] Cache the results set instance of `discoverFriends`
    private final HashSet<String> _df_resultsCache = new HashSet<String>(30);
    
    /**
     * Find a word's friends. Initializes the trie traversal in search word's of friends.
     *
     * This method is called *alot*, it is optimized for performance, not readability.
     * 
     * @param String word - The word who's friends you seek to discover
     */
    public HashSet<String> discoverFriends(final String word) {
        
        // Word's discovered friends will be stored here.
        _df_resultsCache.clear();
        
        final int iWordLength   = word.length();
        final int[] currentRow  = new int[iWordLength + 1];
        
        final ArrayList<Character> chars = new ArrayList<Character>(iWordLength);
        
        for (int i = 0; i < iWordLength; i++) {
            // Populate `chars`
            chars.add(word.charAt(i));
            // Populate initial row
            currentRow[i] = i;
        }
        currentRow[iWordLength] = iWordLength; // currentRow's length is word's length + 1, this last one doesn't fit in the previous common loop, this is still faster than having two seperate loops

        for (Character c : theTrie.root.children.keySet()) {
            traverseTrie(theTrie.root.children.get(c), c, chars, currentRow, _df_resultsCache);
        } 
        
        return _df_resultsCache;
    }

    /**
     * Recursive helper function. Traverses theTrie in search of the minimum Levenshtein Distance.
     * 
     * @param TrieNode node - the current TrieNode
     * @param char letter - the current character of the current word we're working with
     * @param ArrayList<Character> word - an array representation of the current word
     * @param int[] previousRow - a row in the Levenshtein Distance matrix
     */
    private void traverseTrie(final TrieNode node, final char letter, final ArrayList<Character> word, final int[] previousRow, final HashSet<String> results) {
        
        final int size = previousRow.length;
        final int[] currentRow = new int[size];
        currentRow[0] = previousRow[0] + 1;

        int minimumElement = currentRow[0];
        int insertCost, deleteCost, replaceCost;

        for (int i = 1; i < size; i++) {

            insertCost = currentRow[i - 1] + 1;
            deleteCost = previousRow[i] + 1;

            if (word.get(i - 1) == letter) {
                replaceCost = previousRow[i - 1];
            } else {
                replaceCost = previousRow[i - 1] + 1;
            }
            
            // Get the minimum between insertCost, deleteCost and replaceCost
            currentRow[i] = Math.min(Math.min(insertCost, deleteCost), replaceCost);
            
            if (currentRow[i] < minimumElement) {
                minimumElement = currentRow[i];
            }
        }

        if (currentRow[size - 1] <= FRIENDSHIP && node.word != null) {
            results.add(node.word);
        }

        if (minimumElement <= FRIENDSHIP) {
            for (Character c : node.children.keySet()) {
                traverseTrie(node.children.get(c), c, word, currentRow, results);
            }
        }
    }
    
    private final class Trie {

        public TrieNode root;

        public Trie() {
            this.root = new TrieNode();
        }

        public void insert(String word) {

            int length = word.length();
            TrieNode current = this.root;

            if (length == 0) {
                current.word = word;
            }
            for (int index = 0; index < length; index++) {

                char letter = word.charAt(index);
                TrieNode child = current.getChild(letter);

                if (child != null) {
                    current = child;
                } else {
                    current.children.put(letter, new TrieNode());
                    current = current.getChild(letter);
                }
                if (index == length - 1) {
                    current.word = word;
                }
            }
        }
    }

    private final class TrieNode {

        public final int ALPHABET = 26;

        public String word;
        public final Map<Character, TrieNode> children;

        public TrieNode() {
            this.word = null;
            children = new HashMap<Character, TrieNode>(ALPHABET);
        }

        public TrieNode getChild(char letter) {

            if (children != null) {
                if (children.containsKey(letter)) {
                    return children.get(letter); 
                }
            }
            return null;
        }
    }
    
}