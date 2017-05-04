import java.util.Formatter;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

/*
 * This program is used find protein primary sequence and its Gene Ontology
 * 
 * Author: Alexander Peng 
 *
 * Date: 2017.3.30
 */


//START of class PPSTGO
public class PPSTGO{

	private Scanner input;
	private String orgType;

	//START of main method 
	public static void main(String[] args){
		//System.out.println("testing 0");
		if(args.length == 2){
			PPSTGO ppstgo = new PPSTGO(args);

			ppstgo.find();

			ppstgo.input.close();
			System.out.println("done");
		}
		else if(args.length == 1){
			String[] in = {args[0],"ALL"};
			PPSTGO ppstgo = new PPSTGO(in);

			ppstgo.find();

			ppstgo.input.close();
			System.out.println("done");
		}
		else{
			System.out.println("Error: Please Check Input.");
		}

	}//END of main method 

	//START of CONSTRUCTOR 
	public PPSTGO (String[] args){
		orgType = args[1];
		//set input source 
		try{
			input = new Scanner(new File(args[0]));
		}
		catch(FileNotFoundException e){
			System.out.println(e);
		}
	}//END of CONSTRUCTOR constructor 

	//START of find
	public void find(){
		try{
			Scanner org;
			Formatter output =  new Formatter("PPSTGO_"+orgType+"_output.txt");
			while(this.input.hasNextLine()){
				org = new Scanner(this.input.nextLine());


				if(orgType.matches("ALL") || org.next().matches("OC") && org.next().matches(orgType+";")){//find organism type 
					//System.out.println("found");
					org = new Scanner(this.input.nextLine());
					String next = org.next();

					boolean hasGO = false;
					while(!next.matches("SQ")){//find the sequence 


						if(next.matches("DR") && org.hasNext() && org.next().matches("GO;")){
							String goAnn = org.next();//the GO annotation 
							next = org.next();//the GO type e.g. function 
							if(next.contains("F:")){
								hasGO = true;
								output.format(goAnn+" "+next);

								while(!next.endsWith(";")){
									next = org.next();
									output.format(next+" ");
								}
								output.format("%n");
							}
						}
						org = new Scanner(this.input.nextLine());
						next = org.next();
					}

					if(hasGO){//only store sequence if there is GO function 
						org = new Scanner(this.input.nextLine());
						while(!next.matches("//")){
							output.format(next);
							if(!org.hasNext())
								org = new Scanner(this.input.nextLine());
							next = org.next();
						}
						output.format("%n%n");
					}

				}
			}//end of while
			output.close();
		}//end of try
		catch(FileNotFoundException e){
			System.out.println(e);
		}
	}//END of find

}//END of class PPSTGO

