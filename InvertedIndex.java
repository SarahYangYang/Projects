package p2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class InvertedIndex {

	Hashtable<String, LinkedList<Integer>> postingIndex; // For storing the
															// posting index
	String pathOfIndex; // For storing the path of the Lucene index
	String inputFile; // For storing the input file name
	String outputFile; // For storing the output file name

	InvertedIndex() {
		postingIndex = new Hashtable<String, LinkedList<Integer>>();
	}

	void constructPostingList() throws IOException {
		IndexReader iReader = DirectoryReader.open(FSDirectory.open(Paths.get(pathOfIndex)));
		Fields fields = MultiFields.getFields(iReader);
		Iterator<String> fieldIt = fields.iterator();

		// Read terms and document IDs from each field
		while (fieldIt.hasNext()) {
			String fieldName = fieldIt.next();
			if (fieldName.equals("_version_") || fieldName.equals("id"))
				continue;
			Terms terms = fields.terms(fieldName);
			TermsEnum termIt = terms.iterator();
			BytesRef term = null;
			while ((term = termIt.next()) != null) {
				String termName = term.utf8ToString();
				LinkedList<Integer> postingList;
				// For each term, insert the Lucene postings into the index.
				if (postingIndex.containsKey(termName)) {
					// If the term already exists in the index, get the posting
					// list from the index.
					postingList = postingIndex.get(termName);
				} else {
					// If the term does not exists, create a new posting list
					// and insert into the index.
					postingList = new LinkedList<Integer>();
					postingIndex.put(termName, postingList);
				}
				PostingsEnum postingIt = MultiFields.getTermDocsEnum(iReader, fieldName, term);
				int docID = PostingsEnum.NO_MORE_DOCS;
				while ((docID = postingIt.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
					postingList.add(docID);
				}
			}
		}

		// Sort each posting list
		for (LinkedList<Integer> l : postingIndex.values()) {
			Collections.sort(l);
		}

		iReader.close();
	}

	LinkedList<Integer> getPostingList(String term) {
		return postingIndex.get(term);
	}

	LinkedList<Integer> DaatAND(ArrayList<String> terms, IntObj numOfComp) {
		LinkedList<Integer> result = new LinkedList<Integer>();

		// Obtain the postings for each term from the index, create a hash table
		// where the key is each document ID and the value is the string
		// containing all the terms that is contained in the document.
		Hashtable<Integer, ArrayList<String>> arrays = new Hashtable<Integer, ArrayList<String>>();
		for (String term : terms) {
			LinkedList<Integer> postings = postingIndex.get(term);
			for (Integer docID : postings) {
				ArrayList<String> array;
				if (arrays.containsKey(docID)) {
					array = arrays.get(docID);
				} else {
					array = new ArrayList<String>();
					arrays.put(docID, array);
				}
				array.add(term);
			}
		}

		// Perform the intersection for each document ID, and insert the
		// document ID into the resultant posting if all terms appear.
		numOfComp.value = 0;
		for (Integer docID : arrays.keySet()) {
			boolean i = true;
			ArrayList<String> array = arrays.get(docID);
			for (String term : terms) {
				numOfComp.value++;
				if (!array.contains(term)) {
					i = false;
					break;
				}
			}
			if (i == true) {
				result.add(docID);
			}
		}
		Collections.sort(result);

		return result;
	}

	LinkedList<Integer> DaatOR(ArrayList<String> terms, IntObj numOfComp) {
		LinkedList<Integer> result = new LinkedList<Integer>();

		Hashtable<Integer, ArrayList<String>> arrays = new Hashtable<Integer, ArrayList<String>>();
		for (String term : terms) {
			LinkedList<Integer> postings = postingIndex.get(term);
			for (Integer docID : postings) {
				ArrayList<String> array;
				if (arrays.containsKey(docID)) {
					array = arrays.get(docID);
				} else {
					array = new ArrayList<String>();
					arrays.put(docID, array);
				}
				array.add(term);
			}
		}

		// Perform the union for each document ID, and insert the
		// document ID into the resultant posting if at least one term appears.
		numOfComp.value = 0;
		for (Integer docID : arrays.keySet()) {
			boolean i = false;
			ArrayList<String> array = arrays.get(docID);
			for (String term : terms) {
				numOfComp.value++;
				if (array.contains(term)) {
					i = true;
					break;
				}
			}
			if (i == true) {
				result.add(docID);
			}
		}
		Collections.sort(result);

		return result;
	}

	LinkedList<Integer> TaatAND(ArrayList<String> terms, IntObj numOfComp) {
		LinkedList<Integer> result = new LinkedList<Integer>();

		numOfComp.value = 0;
		Hashtable<Integer, Integer> partialScore = new Hashtable<Integer, Integer>();
		// Update the partial score of each document for each term
		for (String term : terms) {
			LinkedList<Integer> postings = postingIndex.get(term);
			for (Integer docID : postings) {
				numOfComp.value++;
				if (partialScore.containsKey(docID)) {
					partialScore.replace(docID, partialScore.get(docID) + 1);
				} else {
					partialScore.put(docID, 1);
				}
			}
		}

		// Add the document into the resultant posting if the partial score
		// equals to the number of terms.
		int numOfTerms = terms.size();
		for (Integer docID : partialScore.keySet()) {
			if (partialScore.get(docID) == numOfTerms) {
				result.add(docID);
			}
		}
		Collections.sort(result);

		return result;
	}

	LinkedList<Integer> TaatOR(ArrayList<String> terms, IntObj numOfComp) {
		LinkedList<Integer> result = new LinkedList<Integer>();

		numOfComp.value = 0;
		Hashtable<Integer, Integer> partialScore = new Hashtable<Integer, Integer>();
		for (String term : terms) {
			LinkedList<Integer> postings = postingIndex.get(term);
			for (Integer docID : postings) {
				numOfComp.value++;
				if (partialScore.containsKey(docID)) {
					partialScore.replace(docID, partialScore.get(docID) + 1);
				} else {
					partialScore.put(docID, 1);
				}
			}
		}

		// Add the document into the resultant posting if the partial score is
		// larger than zero.
		for (Integer docID : partialScore.keySet()) {
			if (partialScore.get(docID) > 0) {
				result.add(docID);
			}
		}
		Collections.sort(result);

		return result;
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		InvertedIndex ii = new InvertedIndex();
		ii.pathOfIndex = args[0];
		ii.outputFile = args[1];
		ii.inputFile = args[2];
		ii.constructPostingList();

		BufferedReader br = new BufferedReader(new FileReader(ii.inputFile));
		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ii.outputFile), "UTF-8"));

		String line = null;
		while ((line = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line, " ");
			ArrayList<String> terms = new ArrayList<String>();
			while (st.hasMoreTokens()) {
				terms.add(st.nextToken());
			}

			LinkedList<Integer> postingList;
			IntObj numOfComp = new IntObj();
			// get postings
			for (String term : terms) {
				out.write("GetPostings\n");
				out.write(term + "\n");
				out.write("Postings list: ");
				postingList = ii.getPostingList(term);
				if (postingList == null) {
					out.write("empty\n");
					continue;
				}
				out.write(postingList.toString());
				out.write("\n");
			}

			// taat And
			out.write("TaatAnd\n");
			out.write(terms.toString() + "\n");
			numOfComp.value = -1;
			postingList = ii.TaatAND(terms, numOfComp);
			out.write("Results: " + postingList.toString() + "\n");
			out.write("Number of documents in results: " + String.valueOf(postingList.size()) + "\n");
			out.write("Number of comparisons: " + String.valueOf(numOfComp.value) + "\n");

			// taat Or
			out.write("TaatOr\n");
			out.write(terms.toString() + "\n");
			numOfComp.value = -1;
			postingList = ii.TaatOR(terms, numOfComp);
			out.write("Results: " + postingList.toString() + "\n");
			out.write("Number of documents in results: " + String.valueOf(postingList.size()) + "\n");
			out.write("Number of comparisons: " + String.valueOf(numOfComp.value) + "\n");

			// daat And
			out.write("DaatAnd\n");
			out.write(terms.toString() + "\n");
			numOfComp.value = -1;
			postingList = ii.DaatAND(terms, numOfComp);
			out.write("Results: " + postingList.toString() + "\n");
			out.write("Number of documents in results: " + String.valueOf(postingList.size()) + "\n");
			out.write("Number of comparisons: " + String.valueOf(numOfComp.value) + "\n");

			// daat Or
			out.write("DaatOr\n");
			out.write(terms.toString() + "\n");
			numOfComp.value = -1;
			postingList = ii.DaatOR(terms, numOfComp);
			out.write("Results: " + postingList.toString() + "\n");
			out.write("Number of documents in results: " + String.valueOf(postingList.size()) + "\n");
			out.write("Number of comparisons: " + String.valueOf(numOfComp.value) + "\n");
		}

		br.close();
		out.close();

	}

}
