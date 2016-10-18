package ssinha7_project2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class BooleanRetrieval {
	static int counterDAATOr = 0;
	static int counterDAATAnd = 0;
	private static String inputfile;
	private static PrintWriter outputfile;

	private static void PrintingPostings(String query, HashMap<String, TreeSet<Integer>> Map) {

		String querySplit[] = query.split(" "); // display the posting of all
												// query terms
		for (int queryterm = 0; queryterm < querySplit.length; queryterm++) {

			TreeSet<Integer> termtree = Map.get(querySplit[queryterm]);
			ArrayList<Integer> termposting = new ArrayList<Integer>();
			termposting.addAll(termtree);

			String formattedlist = termposting.toString().replace("[", "").replace(",", "").replace("]", "").trim();
			outputfile.write("GetPostings\n" + querySplit[queryterm] + "\nPostings list: " + formattedlist + "\n");
		}
	}

	public static void PrintingTaatOR(String query, ArrayList<Integer> termTAATOr, int size, int count) {
		String formattedlist; // display the result of TAATOR of all query terms
		if (size == 0)
			formattedlist = "empty";
		else
			formattedlist = termTAATOr.toString().replace("[", "").replace(",", "").replace("]", "").trim();
		outputfile.println("TaatOr\n" + query + "\nResults: " + formattedlist + "\nNumber of documents in results: "
				+ termTAATOr.size() + "\nNumber of comparisons: " + count);
	}

	public static void PrintingTaatAND(String query, ArrayList<Integer> termTAATAnd, int size, int count) {
		String formattedlist; // display the result of TAATAND of all query
								// terms
		if (size == 0)
			formattedlist = "empty";
		else
			formattedlist = termTAATAnd.toString().replace("[", "").replace(",", "").replace("]", "").trim();
		outputfile.println("TaatAnd\n" + query + "\nResults: " + formattedlist + "\nNumber of documents in results: "
				+ termTAATAnd.size() + "\nNumber of comparisons: " + count);
	}

	public static void PrintingDaatOr(String query, ArrayList<Integer> dAATOr, int size, int count) {
		String formattedlist; // display the result of DAATOR of all query terms
		if (size == 0)
			formattedlist = "empty";
		else
			formattedlist = dAATOr.toString().replace("[", "").replace(",", "").replace("]", "").trim();
		outputfile.println("DaatOr\n" + query + "\nResults: " + formattedlist + "\nNumber of documents in results: "
				+ size + "\nNumber of comparisons: " + count);
	}

	private static void PrintingDaatAnd(String query, ArrayList<Integer> dAATAnd, int size, int count) {
		String formattedlist; // display the result of DAATAND of all query
								// terms
		if (size == 0)
			formattedlist = "empty";
		else
			formattedlist = dAATAnd.toString().replace("[", "").replace(",", "").replace("]", "").trim();
		outputfile.println("DaatAnd\n" + query + "\nResults: " + formattedlist + "\nNumber of documents in results: "
				+ size + "\nNumber of comparisons: " + count);

	}

	public static void main(String args[]) throws Exception {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(args[0]))); // gets
																							// index
																							// file
																							// from
																							// args[0]
		String outputpath = args[1]; // gets outputfile from args[1]
		outputfile = new PrintWriter(outputpath, "UTF-8");

		inputfile = args[2]; // gets input query file from args[2]
		BufferedReader br = new BufferedReader(new FileReader(BooleanRetrieval.inputfile));
		String inputline = null;

		HashMap<String, TreeSet<Integer>> invertedIndex = new HashMap<String, TreeSet<Integer>>();
		ArrayList<String> languages = new ArrayList<String>();
		languages.add("text_nl");
		languages.add("text_fr");
		languages.add("text_de");
		languages.add("text_ja");
		languages.add("text_ru");
		languages.add("text_pt");
		languages.add("text_es");
		languages.add("text_it");
		languages.add("text_da");
		languages.add("text_no");
		languages.add("text_sv");

		for (String lang : languages) {
			Terms terms = MultiFields.getTerms(reader, lang); // get all terms
																// of this field
			TermsEnum termsenum = terms.iterator();
			BytesRef term = null;
			while ((term = termsenum.next()) != null) {
				PostingsEnum postingsEnum = MultiFields.getTermDocsEnum(reader, lang, term);//getting docs ids as per the term
				TreeSet<Integer> temp = new TreeSet<>();

				while (postingsEnum.nextDoc() != PostingsEnum.NO_MORE_DOCS) {

					if (invertedIndex.containsKey(term.utf8ToString())) {
						//if term already present in the dictionary
						temp = invertedIndex.get(term.utf8ToString());
						temp.add(postingsEnum.docID());

					} else {
						temp.add(postingsEnum.docID());
					}
					invertedIndex.put(term.utf8ToString(), temp);
				}
			}
		}
		while ((inputline = br.readLine()) != null) {
			PrintingPostings(inputline, invertedIndex);
			TAATAnd(inputline, invertedIndex);
			TAATOr(inputline, invertedIndex);
			DAATAnd(inputline, invertedIndex);
			DAATOr(inputline, invertedIndex);
		}
		br.close();
		outputfile.close();

	}

	public static ArrayList<ArrayList> sortPostingList(String query, HashMap<String, TreeSet<Integer>> Map) {
		ArrayList<ArrayList> postingArrayList = new ArrayList<ArrayList>();
		String querySplit[] = query.split(" ");
		for (int queryterm = 0; queryterm < querySplit.length; queryterm++) {

			TreeSet<Integer> termtree = Map.get(querySplit[queryterm]);
			ArrayList<Integer> termpostings = new ArrayList<Integer>();
			termpostings.addAll(termtree);
			postingArrayList.add(termpostings);
		}

		Collections.sort(postingArrayList, new Comparator<ArrayList>() {
			public int compare(ArrayList a1, ArrayList a2) {
				return a1.size() - a2.size(); // arranging posting according to size (small to large) to optimize
			}
		});
		return postingArrayList;
	}

	public static int findMin(ArrayList<ArrayList> postingArraylist) {
		// System.out.println("in min" + postingArraylist);
		ArrayList<Integer> postingPointers = new ArrayList<Integer>();
		int min = -1;
		ArrayList<Integer> currentPosting = new ArrayList<Integer>();
		for (int i = 0; i < postingArraylist.size(); i++) { // removal of element having posting list size zero
			if (postingArraylist.get(i).size() == 0) {
				postingArraylist.remove(i);
			}
		}

		for (int i = 0; i < postingArraylist.size(); i++) { 
			// finding the current posting  pointers of the posting list
			if (postingArraylist.get(i).size() > 0) {
				currentPosting = postingArraylist.get(i);
				postingPointers.add(currentPosting.get(0));
			}
		}
		if (postingPointers.size() > 1) {
			min = postingPointers.get(0);
			for (int i = 1; i < postingPointers.size(); i++) {
				counterDAATOr++;
				if ((postingPointers.get(i)) < min) {// find min of the posting
														// pointers
					min = postingPointers.get(i);
				}
			}

		} else if (postingPointers.size() == 1) {// find min of the posting
													// pointers if only one
													// pointer exists
			min = postingPointers.get(0);
		}
		return min;

	}

	public static ArrayList<Integer> TAATAnd(String query, HashMap<String, TreeSet<Integer>> Map) {
		String querySplit[] = query.split(" ");
		ArrayList<ArrayList> postingArraylist = sortPostingList(query, Map);//call to sort all posting list as per size
		ArrayList<Integer> temp = new ArrayList<Integer>();
		temp.addAll(postingArraylist.get(0));// get elements of first posting
												// and treating it as
												// intermediate result
		ArrayList<Integer> termTAATAnd = new ArrayList<Integer>();
		int count = 0;
		for (int queryterm = 1; queryterm < postingArraylist.size(); queryterm++) {
			ArrayList<Integer> termpostings = new ArrayList<Integer>();
			termpostings.addAll(postingArraylist.get(queryterm));
			int i = 0, j = 0;
			while (i < temp.size() && j < termpostings.size()) {
				count++;

				if (temp.get(i).equals(termpostings.get(j))) {
					termTAATAnd.add(temp.get(i));// adding in TAATAND list when
													// elements are equal
					i++;
					j++;
				} else if (temp.get(i) < termpostings.get(j)) {
					i++;
				} else if (i == (temp.size() - 1) && !temp.get(i).equals(termpostings.get(j))) {
					//when intermediate list has last element and comparing rest of elements from the new posting list
					j++;
				} else if (i < temp.size() && temp.get(i) > termpostings.get(j)) {
					//when intermediate and-list has doc ids of value greater than doc ids from the new posting list
					j++;
				}
			}
			temp.removeAll(temp);
			temp.addAll(termTAATAnd); // adding intermediate result from termTAATAnd to temp for next
										// round
			termTAATAnd.removeAll(termTAATAnd);
		}
		termTAATAnd.addAll(temp);
		
			PrintingTaatAND(query, termTAATAnd, termTAATAnd.size(), count);
		return termTAATAnd;

	}

	public static ArrayList<Integer> TAATOr(String query, HashMap<String, TreeSet<Integer>> Map) {
		String querySplit[] = query.split(" ");
		ArrayList<ArrayList> postingArraylist = sortPostingList(query, Map);//call to sort all posting list as per size
		ArrayList<Integer> temp = new ArrayList<Integer>();
		temp.addAll(postingArraylist.get(0));// get elements of first posting and treating it as intermediate result
		ArrayList<Integer> termTAATOr = new ArrayList<Integer>();
		int count = 0;
		for (int queryterm = 1; queryterm < postingArraylist.size(); queryterm++) {
			ArrayList<Integer> termpostings = new ArrayList<Integer>();
			termpostings.addAll(postingArraylist.get(queryterm));
			int i = 0, j = 0;
			while (i < temp.size() && j < termpostings.size()) {
				Collections.sort(termTAATOr);
				if (temp.get(i).equals(termpostings.get(j))) {//when repeated doc id in the postings
					count++;
					if (!(i == (temp.size() - 1))) {
						i++;
						j++;
					} else {
						j++;
					}

				} else if (temp.get(i) < termpostings.get(j) && !(i == (temp.size() - 1))) {
					i++;
					count++;
				} else if (temp.get(i) > termpostings.get(j)) {
					termTAATOr.add(termpostings.get(j));
					j++;
					count++;
				} else if (i == (temp.size() - 1) && !temp.get(i).equals(termpostings.get(j))) {
					termTAATOr.add(termpostings.get(j));
					j++;

				}
			}
			temp.addAll(termTAATOr);  //adding unique doc ids to the intermediate TAATOR list 
			Collections.sort(temp);
			termTAATOr.removeAll(termTAATOr);
		}
		Collections.sort(temp);
		termTAATOr = temp;
		PrintingTaatOR(query, termTAATOr, termTAATOr.size(), count);
		return termTAATOr;
	}

	public static ArrayList<Integer> DAATAnd(String query, HashMap<String, TreeSet<Integer>> Map) {
		String querySplit[] = query.split(" ");
		ArrayList<ArrayList> postingArraylist = new ArrayList<ArrayList>();
		postingArraylist = sortPostingList(query, Map);//call to sort all posting list as per size
		ArrayList<Integer> postingPointers = new ArrayList<Integer>();
		ArrayList<Integer> DAATAnd = new ArrayList<Integer>();

		for (int queryterm = 0; queryterm < postingArraylist.size(); queryterm++) {
			ArrayList<Integer> termPostings = new ArrayList<Integer>();
			termPostings.addAll(postingArraylist.get(queryterm));
			postingPointers.add(termPostings.get(0));
		}
		int max = 0;
		counterDAATAnd = 0;
		while (postingArraylist.size() > 0) {
			max = findMax(postingPointers);// finding the maximum of the pointers of the posting lists
						outerloop: for (int i = 0; i < postingArraylist.size(); i++) {
				ArrayList<Integer> currentPosting = new ArrayList<Integer>();
				currentPosting = postingArraylist.get(i);
				if (currentPosting.size() > 0 && currentPosting.get(0) == max) {
					//when all the posting pointers are at max ie same, remove the same doc id from posting list of all the terms
					if (findEqual(postingPointers) == 1) {
						counterDAATAnd++;
						DAATAnd.add(postingPointers.get(0));
						for (int p = 0; p < postingArraylist.size(); p++) {

							ArrayList<Integer> samePosting = new ArrayList<Integer>();
							samePosting = postingArraylist.get(p);
							//increment posting pointer after deleting the current max element
							if (samePosting.size() > 1) {
								postingPointers.remove(p);
								postingPointers.add(p, samePosting.get(1));
							}
							samePosting.remove(0);
						}
						break outerloop;
					}

				}
				while (currentPosting.size() > 0 && currentPosting.get(0) < max) {
					//deleting all the doc ids from posting list which are smaller than the max pointer
					counterDAATAnd++;
					if (postingPointers.get(i) != 0)
						postingPointers.remove(i);
					if (currentPosting.size() > 1)
						postingPointers.add(i, currentPosting.get(1));
					currentPosting.remove(0);

				}

				if (currentPosting.size() == 0) {
					//removing the posting list which contains no doc ids
					postingArraylist.remove(i);
					postingArraylist.removeAll(postingArraylist);
				}
			}
		}
		PrintingDaatAnd(query, DAATAnd, DAATAnd.size(), counterDAATAnd);
		return DAATAnd;

	}

	public static int findMax(ArrayList<Integer> postingPointers) {
		int max = postingPointers.get(0);
		if (findEqual(postingPointers) == 1) {
			//if single posting list is presents return its pointer as max
			return max;
		} else {

			for (int i = 1; i < postingPointers.size(); i++) {
				counterDAATAnd++;
				if ((postingPointers.get(i)) > max) {
					//find max of pointers of posting list return its pointer as max
					max = postingPointers.get(i);
				}
			}
			return max;
		}

	}

	public static int findEqual(ArrayList<Integer> postingPointers) {

		int equality = 0;
		int temp = postingPointers.get(0);
		for (int i = 1; i < postingPointers.size(); i++) {
			if ((postingPointers.get(i)) == temp)
				equality = equality + 1;
		}
		//if all the pointers of posting list are equal return 1
		
		if (equality == postingPointers.size() - 1)
			return 1;
		else
			return 0;
	}

	public static ArrayList<Integer> DAATOr(String query, HashMap<String, TreeSet<Integer>> Map) {
		String querySplit[] = query.split(" ");
		ArrayList<ArrayList> postingArraylist = new ArrayList<ArrayList>();
		postingArraylist = sortPostingList(query, Map);//call to sort all posting list as per size
		ArrayList<Integer> postingPointers = new ArrayList<Integer>();
		ArrayList<Integer> DAATOr = new ArrayList<Integer>();

		for (int queryterm = 0; queryterm < postingArraylist.size(); queryterm++) {
			//forming the posting pointers from the posting list
			ArrayList<Integer> termPostings = new ArrayList<Integer>();
			termPostings.addAll(postingArraylist.get(queryterm));
			postingPointers.add(termPostings.get(0));
		}

		counterDAATOr = 0;

		while (postingArraylist.size() > 1) {
			int min = findMin(postingArraylist);
			if (min == -1) {
				break;
			}
			DAATOr.add(min);
			// minimum of all the posting list pointers is added to the DAAT OR result
			counterDAATOr++;
			for (int i = 0; i < postingArraylist.size(); i++) {
				ArrayList<Integer> currentPosting = new ArrayList<Integer>();
				currentPosting = postingArraylist.get(i);
				if (currentPosting.size() > 0 && currentPosting.get(0) == min) {
					currentPosting.remove(0); //remove the minimum doc id from the posting list
				}
			}
		}
		if (postingArraylist.size() == 1) {
			// all the doc ids of last posting list is added to the DAAT OR result
			
			ArrayList<Integer> currentPosting = new ArrayList<Integer>();
			currentPosting = postingArraylist.get(0);
			if (currentPosting.size() > 0) {
				DAATOr.addAll(currentPosting);

			}
		}
		PrintingDaatOr(query, DAATOr, DAATOr.size(), counterDAATOr);
		return DAATOr;

	}

}