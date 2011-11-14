# encoding: UTF-8

require 'open-uri'
require 'benchmark'

# Network of friends of 'Causes' using Levenstein distance.
  
## Discover the network of a word
# input: a word
# output: the word's network of friends
# I tried two versions. 
# This version uses a simple loop. It is slightly slower but prevents the call stack from blowing up.
def network_of(word)
  netowrk = []
  buffer  = []
  buffer << word
  while current = buffer.pop
    friends_of(current).each do |friend|
      next if netowrk.include?(friend)
      buffer  << friend
      netowrk << friend
    end
    puts "Current size is #{netowrk.size}"
  end
  netowrk
end 

# This version is recursive. The call stack will blow up unless you raise its ceiling (ulimit -s XXXXX on unix)
# def network_of(word)
#   ret = []
#   friends_of(word).each do |friend|
#     next if ret.include?(friend)    
#     ret << friend
#     network_of(friend)
#   end
#   ret
# end

## Discover a word's friends
# input: a word
# ouput: an array the word's friends
def friends_of(word)
  ret = []
  for candidate in bounding_words_of(word)
    ret << candidate if friends?(word, candidate)
  end
  ret
end


## Find out if two words are friends.
# Words are friends if their Levenshtein distance is equal to one.
# I tried two versions of the same algorithm, the second is slightly more compute and memory efficient.

## FIRST VERSION - My implementation of the Wikepedia pseudo code (http://en.wikipedia.org/wiki/Levenshtein_distance)

# require 'matrix'
#
# class Matrix
#   def []=(i, j, x)
#     @rows[i][j] = x
#   end
# end
#
# def friends?(a, b)
#   matrix = Matrix.build(a.length+1, b.length+1) do |i, j|
#     # Fill the matrix with default values
#     i == 0 ? j : (j == 0 ? i : 0)
#   end
#   
#   for j in 1..b.length
#     for i in 1..a.length
#       matrix[i,j] = if a[i-1] == b[j-1]
#         matrix[i-1,j-1]            # letters are the same
#       else
#         [
#           matrix[i-1,j]   + 1,     # a deletion
#           matrix[i,j-1]   + 1,     # an insertion
#           matrix[i-1,j-1] + 1      # a subsitution
#         ].min
#       end
#     end
#   end
#   # puts "Words #{a} and #{b} have a Lev. distance of #{matrix[a.length, b.length]}"
#   matrix[a.length, b.length] == 1
# end

## SECOND VERSION - Found here https://github.com/threedaymonk/text/blob/master/lib/text/levenshtein.rb

def friends?(a, b)
  s = a.unpack('U*')
  t = b.unpack('U*')
  n = s.length
  m = t.length
  return m if (0 == n)
  return n if (0 == m)

  d = (0..m).to_a
  x = nil

  (0...n).each do |i|
    e = i+1
    (0...m).each do |j|
      cost = (s[i] == t[j]) ? 0 : 1
      x = [
        d[j+1] + 1, # insertion
        e + 1,      # deletion
        d[j] + cost # substitution
      ].min
      d[j] = e
      e = x
    end
    d[m] = x
  end

  return x == 1                                          
end

## Helper function. Slice the data set so that we don't always loop over it entirely
# By definition, for any two words a and b with an edit distance of n, -n >= lenght(a) - length(b) >= n.
# For a given word, this function will return words which *we can hope* to be friends of word because
# only the words who's length are +/- 1 another word's length can potentially be friends.
# This prevents us from looping over the entire data set every time.
# input: a word
# ouput: an array of words of length +-1, extracted from the word list 
def bounding_words_of(word)
  # Build and cache the index list on first invoke
  cache_bounds! unless defined? @_bw_indices 
  # return words of length +-1 of word
  @list.slice(
    @_bw_indices[word.length-1][0],
    @_bw_indices[word.length+1][1]
  )
end

def cache_bounds!
  # sort the list by length
  @list.sort_by!(&:length)
  # results will be cached in here
  # structure is { (word length) => [begin, end], ... }
  # where begin and end are indices of @list
  @_bw_indices = Hash.new
  @list.each_index do |i|
    if @_bw_indices[@list[i].length].nil? # New le
      @_bw_indices[@list[i].length]      = []
      @_bw_indices[@list[i].length][0]   = i
      @_bw_indices[@list[i-1].length][1] = i-1 if @_bw_indices[@list[i-1].length]
    end
  end
end


## Execution

list_cache_filename = 'challenge_word_list.tmp.txt'
list_gist_url = 'https://raw.github.com/causes/puzzles/master/word_friends/word.list'

# Get the data set.
# If the data set is not found locally, download it.
if !File.exists?(list_cache_filename) || File.zero?(list_cache_filename)
  cache = File.open(list_cache_filename, 'w+')
  puts "Downloading the list of words..."
  raw_list = open(list_gist_url).read
  @list = raw_list.split("\n")
  cache.write(raw_list)
  puts "Done. Caching it as '#{list_cache_filename}' in the current directory ."
  cache.close
end

if !@list
  @list = File.open(list_cache_filename, 'r').read.split("\n")
end

# Run
puts Benchmark.measure { 
  network_of('causes').length 
}