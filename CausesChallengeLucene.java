import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.List;
import java.io.File;

import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.document.Document;
import org.apache.lucene.store.instantiated.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.document.TextField;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.Version;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Collector; 
import org.apache.lucene.search.Scorer;
import org.apache.lucene.index.IndexReader.AtomicReaderContext;

class CausesChallengeLucene {
    
    public static void main(String[] args) throws Exception {
        
        Scanner s = new Scanner(new File(args[0]));
        ArrayList<String> wordList = new ArrayList<String>(280000);

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
    
    private IndexSearcher searcher;
    
    static public final int    FRIENDSHIP = 1;
    static public final String WORD_FIELD = "word";
    
    public WordNetwork(List<String> words) throws Exception { 
        
        InstantiatedIndex index = new InstantiatedIndex();
        InstantiatedIndexReader reader = new InstantiatedIndexReader(index);
        InstantiatedIndexWriter writer = new InstantiatedIndexWriter(index);
        
        Document doc;
        
        for ( String word : words) {
            doc = new Document();
            doc.add(new StringField(WORD_FIELD, word));
            writer.addDocument(doc);
        }
        
        writer.commit();
        searcher = new IndexSearcher(reader);
    }
    
    public ArrayList<String> discoverNetwork(String word) throws Exception {
        
        HashSet<String> network = new HashSet<String>(80000);
        ArrayList<String> buffer  = new ArrayList<String>(80000);
        
        buffer.add(word);
        
        ArrayList<String> friends;
        String            friend;
        
        while (!buffer.isEmpty()) {
            friend = buffer.get(0);
            buffer.remove(0);
            if (network.contains(friend)) continue;
            friends = discoverFriends(friend);
            buffer.addAll(friends);
            network.add(friend);
            // System.out.println(network.size());
        }
        
        return network;
    }
    
    public ArrayList<String> discoverFriends(String word) throws Exception {
        
        final ArrayList<String> results = new ArrayList<String>();
        
        FuzzyQuery query = new FuzzyQuery(new Term(WORD_FIELD, word), FRIENDSHIP);

        // Search for the query
        searcher.search(new ConstantScoreQuery(query), new Collector() {
           private int docBase;

           // ignore scorer
           public void setScorer(Scorer scorer) { }

           // accept docs out of order (for a BitSet it doesn't matter)
           public boolean acceptsDocsOutOfOrder() {
               return true;
           }

           public void collect(int docId) {
               try {
                results.add(searcher.doc( docId ).get(WORD_FIELD));
               } catch (Exception e) { }
           }

           public void setNextReader(AtomicReaderContext context) {
               this.docBase = context.docBase;
           }  
         });
        
        return results;
    }    
}