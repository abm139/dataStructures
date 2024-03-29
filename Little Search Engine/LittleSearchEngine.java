package lse;

import java.io.*;
import java.util.*;

/**
 * This class builds an index of keywords. Each keyword maps to a set of pages in
 * which it occurs, with frequency of occurrence in each page.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in 
	 * DESCENDING order of frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash set of all noise words.
	 */
	HashSet<String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashSet<String>(100,2.0f);
	}
	
	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeywordsFromDocument(String docFile) 
	throws FileNotFoundException {
		HashMap <String, Occurrence> map = new HashMap<String, Occurrence>();
		Scanner input = new Scanner (new File (docFile));
		
		while (input.hasNext()) 
		{
			String word = getKeyword(input.next());
			if(word == null)
			{
				continue;
			}

			Occurrence occ = map.get(word);
			if (occ != null)
			{
				occ.frequency++;
			}
			else 
			{
				map.put(word, new Occurrence(docFile, 1));
			}
			
		}
		input.close();
		return map;
	}

	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeywords(HashMap<String,Occurrence> kws) {
		for (String k : kws.keySet())
		{
			ArrayList<Occurrence> occ = new ArrayList<Occurrence>();
			if (keywordsIndex.containsKey(k))
			{
				occ = keywordsIndex.get(k);
			}
			occ.add(kws.get(k));
			insertLastOccurrence(occ);
			keywordsIndex.put(k, occ);
		}
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * trailing punctuation, consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyword(String word) {
		word = removeEndPunct(word);
		for (int i = word.length() - 1; i >= 0; i--) 
		{

            if(Character.isLetter(word.charAt(i))) 
            {
                continue;
            }
            else 
            {
            	return null;
            }
		}
		word = word.toLowerCase();
        if(noiseWords.contains(word)) 
        {
            return null;
        }		
		return word;
	}

    private String removeEndPunct(String word) {
        for (int i = word.length() - 1; i >= 0; i--) 
        {
            if (word.charAt(i) == '.' || word.charAt(i) == ',' || word.charAt(i) == '?' || word.charAt(i) == ':' || word.charAt(i) == ';' || word.charAt(i) == '!' ) 
            {
                word = word.substring(0, i);
            } 
            else 
            {
                break;
            }
        }
        return word;
    }
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion is done by
	 * first finding the correct spot using binary search, then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		if(occs.size() == 1)
		{
			 return null;
		}
		
		Occurrence insert = occs.get(occs.size() - 1);
		
		occs.remove(insert);
		
		int l = 0; int r = occs.size()-1; int m=0;
		ArrayList<Integer> seq = new ArrayList<Integer>();
		
		while(l<=r) 
		{
			m = (l+(r-1))/2;
			seq.add(m);
			if(occs.get(m).frequency < insert.frequency)
			{
				r = m-1;
			}
			else 
			{
				l = m+1;
			}
		}
	     
	     if(insert.frequency < occs.get(m).frequency)
	     {
	    	 	m++;
	     }
	     occs.add(occs.get(occs.size()-1));
	     int i = occs.size() - 2;
	     while(i > m)
	     {
	    	 	occs.set(i, occs.get(i-1));
	    	 	i--;
	     }
	     occs.set(m, insert);
	     
		return seq;
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.add(word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeywordsFromDocument(docFile);
			mergeKeywords(kws);
		}
		sc.close();
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of document frequencies. (Note that a
	 * matching document will only appear once in the result.) Ties in frequency values are broken
	 * in favor of the first keyword. (That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2
	 * also with the same frequency f1, then doc1 will take precedence over doc2 in the result. 
	 * The result set is limited to 5 entries. If there are no matches at all, result is null.
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matches, returns null.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<Occurrence> OccW1 = keywordsIndex.get(kw1);
		ArrayList<Occurrence> OccW2 = keywordsIndex.get(kw2);
		if(OccW1 == null && OccW2 == null)
			return null;
		else if(OccW2 == null)
		{
			for(int i = 0; i < 5 && i < OccW1.size(); i++)
				result.add(OccW1.get(i).document);
			return result;
		}
		else if(OccW1 == null)
		{
			for(int i = 0; i < 5 && i < OccW2.size(); i++)
				result.add(OccW2.get(i).document);
			return result;
		}
		int c = 0; int i1 = 0; int i2 = 0;
		Occurrence Occ1 = null; Occurrence Occ2 = null;
		while(i1 < OccW1.size() && i2 < OccW2.size() && c < 5)
		{
			Occ1 = OccW1.get(i1);
			while(i2 < OccW2.size() && c < 5)
			{
				Occ2 = OccW2.get(i2);
				if(Occ1.frequency < Occ2.frequency)
				{
					if(result.contains(Occ2.document))
						continue;
					result.add(Occ2.document);
					c++;
					if(i2 == OccW2.size() - 1)
						i1--;
				}
				else
				{
					if(result.contains(Occ1.document))
						break;
					result.add(Occ1.document);
					c++;
					break;
				}
				i2++;
			}
			i1++;
		}
		if (c < 5)
		{
			if(i1 == OccW1.size())
			{
				for(;c != 5 && i2 < OccW2.size();i2++, c++)
				{
					if(!result.contains(OccW2.get(i2).document))
						result.add(OccW2.get(i2).document);
				}
			}
			else
			{
				for(;c != 5 && i1 < OccW1.size();i1++, c++)
				{
					if(!result.contains(OccW1.get(i1).document))
						result.add(OccW1.get(i1).document);
					i1++;
					c++;
				}
			}
				
		}
		return result;
	}

}