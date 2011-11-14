# Causes Puzzle

## Answer

Before anything else, the answer is **78482** and the resulting code is *CausesChallengeTrie.java*.

## Summary of my Though Process

### First Attempt: The Ruby Way

I first went to the Wikipedia page and learned how the levenshtein distance is calculated.

I went straight to my editor to and started implementing the standard (Robert-Fisher) algorithm in Ruby. I did not do any research, I wanted to see how a straight forward, head-on approach would turn out. I saw the puzzle's data set is over 260000 words long but I went ahead.

This resulted in *causes_challenge.rb*. The code is pretty basic and crude. As expected this first approach did not work out. While correctly written, the performance using either ruby 1.9 or jruby is sub-optimal for this problem. I didn't wait for it to complete.

### Second Attempt: The Java Way

I had to find a better algorithm and switch to a faster language.

My language of choice for compute intensive tasks is Java (I never got to working with C/C++) so I switched to Java. 

I had to go online to find a new approach/algorithm to this problem (I didn't study CS, I have to look this stuff up).

On Stackoverflow, I found someone with a [similar need](http://stackoverflow.com/questions/4868969/implementing-a-simple-trie-for-efficient-levenshtein-distance-calculation-java) and along with his solution, using Trie structures. I found it elegant and I was happy to understand the logic and the trie structure quickly. I implemented it in Java. 

This resulted in *CausesChallengeTrie.java*. In my first test I solved the problem in 12 minutes. Too slow (I have all weekend). So I went back online and looked for alternative approaches.

I should mention that I'm running all my tests on my older 1.8ghz Macbook Air, I do not have very performant hardware.

I had read about upcoming Lucene 4.0's greatly improved fuzzy search performance. Lucene will soon be using Levenshtein Automata. I didn't and still don't know how they work, but on paper they can find a leveinstein distance very quickly.

I couldn't find a standalone implementation of Levenshtein Automata so I first tried using Lucene.

This resulted in *CausesChallengeLucene.java*. This didn't work well enough. I got the correct answer but it was taking over 15 minutes because of Lucene's overhead.

I did find a [standalone implementation](http://www.infiauto.com/projects/datastr/levenshtein.html) of Levenshtein Automata. 

This resulted in *CausesChallengeAutomata.java*. It worked but again it was too slow. I tweeked the automaton's source trying to make it perform better, I got some results but I was still over 10 minutes. I was not motivated to tweak the code further.

#### The Holy Grail

Well maybe not quite the holy grail, but good enough for me. I went back to the more elegant *CausesChallengeTrie.java*. I read up on writing performant Java code and how to tweak the JVM for my problem at hand.

After a few iterations, making subtle changes, I brought the execution time down from 12 minutes to 1.5 minutes. That is an improvement of 800%. I stopped there. That is good enough for me.

## Running This

The Java Trie version can be compiled:

`javac -O CausesChallengeTrie.java`

and run:

`time java -Xms720m -Xmx720m -server -XX:MaxNewSize=60m -XX:NewSize=60m -XX:+UseParNewGC -XX:+CMSParallelRemarkEnabled -XX:MaxPermSize=48M -XX:PermSize=48M -XX:+AggressiveOpts -XX:+OptimizeStringConcat  -XX:MaxTenuringThreshold=0 -XX:NewRatio=2048 -XX:SoftRefLRUPolicyMSPerMB=10000 CausesChallengeTrie challenge_word_list.tmp.txt`