import java.util.Collection;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;
import java.io.File;

import com.infiauto.datastr.auto.DictionaryAutomaton;
import com.infiauto.datastr.auto.LevenshteinAutomaton;


class CausesChallengeAutomaton {
    
    public static void main(String[] args) throws Exception {
        
        Scanner s = new Scanner(new File(args[0]));
        ArrayList<String> wordList = new ArrayList<String>();
        
        wordList.ensureCapacity(270000);

        while (s.hasNext()) {
            wordList.add(s.next());
        }

        s.close();

        WordNetwork wordNetwork = new WordNetwork(wordList);
        
        System.out.println(
            wordNetwork.discoverNetwork("causes").size()
        );
    }
      
}

class WordNetwork {
    
    private DictionaryAutomaton  dictionary;
    private LevenshteinAutomaton automaton;
    
    static public final int FRIENDSHIP = 1;
    
    public WordNetwork(List<String> words) throws Exception { 
        
        dictionary = new DictionaryAutomaton(words);
        automaton  = new LevenshteinAutomaton(FRIENDSHIP);
    }
    
    public ArrayList<String> discoverNetwork(String word) throws Exception {
        
        ArrayList<String> network = new ArrayList<String>();
        ArrayList<String> buffer  = new ArrayList<String>();
                                                            
        network.ensureCapacity(80000);
        buffer.ensureCapacity(40000);
        
        buffer.add(word);
        
        String             friend;
        
        while (!buffer.isEmpty()) {
            friend = buffer.get(0);
            buffer.remove(0);
            if (network.contains(friend)) continue;
            buffer.addAll(discoverFriends(friend));
            network.add(friend);
            System.out.println(network.size());
        }
        
        return network;
    }
    
    public Collection<String> discoverFriends(String word) throws Exception {
        return automaton.recognize(word, dictionary);
    }    
}