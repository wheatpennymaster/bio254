import java.util.ArrayList;

/**
 * This class keeps track of a GO and its frequency of appearance
 * 
 * @author Alex Peng
 * 
 * Assignment: Project 2
 * 
 * Date: 04/18/2017
 *
 */
public class GO implements Comparable<GO>{
	private String GO,func;
	private ArrayList<String> seqs = new ArrayList<String>();
	private int freq;
	
	public GO (String GO, String f, String s){
		this.GO = GO;
		this.func = f;
		seqs.add(s);
		freq = 1;
	}

	public void incrementFreq(){
		freq++;
	}

	//getters
	public String getGO(){return this.GO;}
	public String getFunc(){return this.func;}
	public String getFirstSeq(){return seqs.get(0);}
	public ArrayList<String> getSeqs(){return this.seqs;}
	public int getFreq(){return freq;}
	///
	
	public void addSeq(String sq){
		seqs.add(sq);
	}
	
	public int compareTo(GO w) {
		return this.GO.compareTo(w.GO);		
	}
	
	public String toString(){
		String frequency = freq+"";
		for(int length = frequency.length(); length < 6; length++)
			frequency = " " + frequency;
		
		return frequency + "  " + GO;
	}
}
