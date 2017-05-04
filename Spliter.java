import java.util.ArrayList;
import java.util.Formatter;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

/*
 * This program is used split protein primary sequence and its Gene Ontology into separate files
 * 
 * Author: Alexander Peng 
 *
 * Date: 2017.4.18
 */


//START of class Spliter
public class Spliter{

	private Scanner input;
	private String fileName;
	private int repeats, maxLen, numGO;//min # of repeats for each GO, and max protein sequence length

	//START of main method 
	public static void main(String[] args){
		if(args.length == 3 || args.length == 4){
			Spliter s = new Spliter(args); 

			s.split();

			s.input.close();
			System.out.println("done");
		}
		else{
			System.out.println("Error: Please Check Input.");
		}

	}//END of main method 

	//START of CONSTRUCTOR 
	public Spliter (String[] args){
		//set input source 
		try{
			fileName = args[0];
			input = new Scanner(new File(fileName));
			repeats = Integer.parseInt(args[1]);
			maxLen = Integer.parseInt(args[2]);
			if(args.length ==4)
				numGO = Integer.parseInt(args[3]);
			else
				numGO = 100;//default is having max of 100 functions 
		}
		catch(FileNotFoundException e){
			System.out.println(e);
		}
	}//END of CONSTRUCTOR constructor 

	//START of split
	public void split(){
		try{
			Scanner org;
			String subs = fileName.substring(0,fileName.length()-4);
			Formatter seqOut =  new Formatter(subs+"_sequence.txt");//output file for sequence of protein
			Formatter goOut =  new Formatter(subs+"_go.txt");//output file for GO annotation of protein
			Formatter lenOut =  new Formatter(subs+"_length.txt");//output file for length of protein
			ArrayBST<GO> tree = new ArrayBST<GO>();
			GO currentGO;

			int counter = 0;

			//Put data into an AVL array BST tree
			String next = "";
			String[][] goTemp = new String[100][2];//assuming each protein has less than 100 functions; stores # and function
			while(input.hasNext()){
				org = new Scanner (input.nextLine());
				if(org.hasNext())
					next = org.next();
				counter = 0;
				while(next.startsWith("GO:")){
					goTemp[counter][0] = next.substring(0, next.length()-1);// gets rid of ";"

					goTemp[counter][1] = org.next()+" ";
					while(org.hasNext())
						goTemp[counter][1] += org.next();
					goTemp[counter][1] = goTemp[counter][1].trim();
					goTemp[counter][1] = goTemp[counter][1].substring(0, goTemp[counter][1].length()-1);// gets rid of ";"

					org = new Scanner (input.nextLine()); 
					next = org.next();
					counter++;
				}//END while

				if(counter <= numGO)
					for(int i = 0; i<counter; i++){
						currentGO = new GO(goTemp[i][0], goTemp[i][1], next);
						tree.insert(currentGO);
					}//END for

			}//END while

			//traverse tree and write output files 
			int treeSize = tree.size();
			for(int i = 1; i <= treeSize; i++){
				currentGO = tree.get(i);
				if(currentGO != null && currentGO.getFreq() >= repeats){
					ArrayList<String> seqs = currentGO.getSeqs();
					for(String seq : seqs){
						int seqLen = seq.length();
						if(seqLen <= maxLen){
							seqOut.format(seq+"%n%n");
							goOut.format(currentGO.getGO()+" "+currentGO.getFunc()+"%n%n");
							lenOut.format(seqLen+"%n%n");
						}//END if
					}//END for
				}//END if
			}//END for
			seqOut.close();
			goOut.close();
			lenOut.close();
		}//end of try
		catch(FileNotFoundException e){
			System.out.println(e);
		}
	}//END of split

}//END of class Spliter

