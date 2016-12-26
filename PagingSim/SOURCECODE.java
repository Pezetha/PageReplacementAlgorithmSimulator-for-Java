package pgSim_Final;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import javax.swing.JFileChooser;


public class PagingSimulator {
 
	
	public static void main(String[] args) throws IOException {
		
		ArrayList<Integer> refList = new ArrayList<Integer>();	//creating new ArrayList for requests
	  
		try {
			Scanner filein = new Scanner(new File("C:/Users/*USERNAME*/Desktop/text.txt")); //-----------> CHANGE LOCATION OF YOUR .txt FILE WITH PARAMETERS IN IT
		//	Scanner filein = new Scanner(getRefFile()); 
			while(filein.hasNext()) {
				String refString = filein.next();
				int ref = Integer.parseInt(refString);
				refList.add(ref);					//add the integers to ArrayList  
			}
	   
		filein.close();
	   
		} 	catch (FileNotFoundException e) {
				System.err.println("FileNotFoundException: " + e.getMessage());
				System.exit( 0 );
			}
			catch(NullPointerException e){
				System.out.println("Dann halt nicht ;) - RESTART"); 
				System.exit( 0 );
			}
	  
		//display the size  
		System.out.println("Anzahl der Anfragen: " + refList.size() + "\n");

		//printing the read reference string via iterator
		Iterator<Integer> iter = refList.iterator();
		while (iter.hasNext())
			System.out.print(iter.next() + " ");
	  
		//converting the ArrayList to an Array
		Integer references[] = new Integer[refList.size()];
		references = refList.toArray(references);
	    
	    	//variable for restart 
	    	String ask;
	    
	   	Scanner sc = new Scanner(System.in);
	    
	   	do {
		    System.out.println("\n--------------------------------------------------------------------------------");
		    System.out.println("\nSeitenersetzungsstrategie auswählen: ");
		    System.out.print("\n1 -> FIFO\n2 -> LRU\n3 -> LFU\n4 -> Clock/Second Chance\n\nAuswahl:  ");
		  
		    String ch = sc.next();
		  
			switch(ch) {
				case "1": fifo(references,refList.size());
				 break;
				case "2": lru(references,refList.size());
				 break;
				case "3": lfu(references,refList.size()); 
				 break;
				case "4": clockSC(references,refList.size());
				 break;
		     
				default: System.out.print("\n---------------------------- Ungültige Eingabe - RESTART -----------------------");
			}
		    	System.out.println("\n--------------------------------------------------------------------------------");
			System.out.print("\nNeustart mit gleichen Anfragen? (Yes = y / No = anything else):  ");
			ask = sc.next();
	   	} while(ask.matches("y"));
	   	sc.close();
	    	System.out.println("\n-------------------------------------- END -------------------------------------");  
	}	
 
 
	/**-----------------------------------------FIFO (100% WORKING)--------------------------------------------------------------**/

	public static void fifo(Integer[] references, int refCount) throws IOException {
  	  
		int [] frame = new int[16];							//array for frames
	    	int i, k, avail, countM = 0, countH = 0;		//countM = miss count, countH = hit count
	    	int missMarker;									//marker for the replaced page (only relevant for printing process)
	   	System.out.print("\n-------------- FIFO ------------------------------------------------------------");
	    	System.out.print("\nAnzahl der Seiten (max: 16): ");

		int nopg = chNopg();								//nopg = number of pages
		
		System.out.println("\nx  = Ersetzte Seite");
		
		//display the page numbers
	  	System.out.print("\nSeite ->|");
	  	for(i = 1; i <= nopg; i++) {
	  		System.out.print(" " + i);
	  		if(i < 10)
	  			System.out.print(" ");
	  		System.out.print("|");
	  	}
		System.out.println("\n");
		
		int j = 0 ;							//counter for determining the "first in"
			  
		for(i = 0; i < nopg; i++)
			frame[i] = -1;					//mark all unused array spaces
		
  		for(i = 0; i < refCount; i++) {//----------------------------START--------------------------					
		  
  			missMarker = -1;
			System.out.printf("%d:\t|", references[i]);	//print current ref
		  	avail = 0;
		  	
		  	for(k = 0; k < nopg; k++)
				if(frame[k] == references[i]){			//check all frames for current reference
					avail = 1;							//no fault determination
					countH++;							//+1 HIT
				}
		  	
		  	if(avail == 0){								//proceed when fault occurs
				frame[j] = references[i];				//replacing page with last "first in"
				missMarker = j;
				j = (j + 1) % nopg;						//determine the next "first in" to replace (by using modulo with the number of pages))
				countM++;								//+1 MISS
		  	}		
		  	
			for(k = 0; k < nopg; k++){					//print frames
				if(frame[k] != -1){
					if(k == missMarker)
						System.out.print("x");
					else
						System.out.print(" ");
					
					if(frame[k] > 9)
						System.out.printf("%d|", frame[k]);
					else
						System.out.printf("%d |", frame[k]);
				}
				else
					System.out.print(" - |");
			}
			System.out.print("\t");
			if(avail == 0)
				System.out.print("--->F");
			System.out.print("\tHit: " + countH + "   Miss: " + countM + "\n");   
		       
  	    } //-----------------------------------------END----------------------------------
  		
		hmRatio(countH, countM);						//display Hit/Miss ratio
	}
	
 
	/**----------------------------------------LRU (100% WORKING)----------------------------------------------------------**/
	
	public static void lru(Integer[] references, int refCount) throws IOException {
	    
		int [] frame = new int[16];
		int [] used = new int[16];
		int index = 0;
		int i, j, k, temp;
		int flag = 0, countM = 0, countH = 0, prevCountM = 0;
	        int missMarker;								//marker for the replaced page (only relevant for printing process)
	  
		System.out.print("\n--------------- LRU ------------------------------------------------------------");
		System.out.print("\nAnzahl der Seiten (max: 16): ");

		int nopg = chNopg();							//nopg = number of pages
	  
		System.out.println("\nx  = Ersetzte Seite");
		
		//display the page numbers
	  	System.out.print("\nSeite ->|");
	  	for(i = 1; i <= nopg; i++) {
	  		System.out.print(" " + i);
	  		if(i < 10)
	  			System.out.print(" ");
	  		System.out.print("|");
	  	}
		System.out.print("\n");
		
		for(i = 0; i < nopg; i++)
			frame[i] = -1;								//mark all unused array spaces
		
		for(i = 0; i < refCount; i++){	//---------------------------------Start--------------------------------
		   
			flag = 0;
  			missMarker = -1;
			  
			for(j = 0; j < nopg; j++) {					
			    
				if(frame[j] == references[i]){			//no fault
					System.out.printf("\n%d: ", references[i]);
					System.out.print("\t|");
					flag = 1;							// flag to 1 -> hit occurs
					countH++;							//+1 HIT
					break;
			    }
			}									  
			   
			if(flag == 0){								//fault occurs (if flag stays 0)		
				  
				for(j = 0; j < nopg; j++)
					used[j] = 0;						//all unused initially
				   										//moving through pages and searching recently used pages    
				try {
					for(j = 0, temp = i-1; j < nopg-1; j++, temp--){
						for(k = 0; k < nopg; k++){
							if(frame[k] == references[temp])
							used[k] = 1;				//mark the recently used pages											
						} 
					}
				}	catch(ArrayIndexOutOfBoundsException e){System.out.println("test");}
				    
				for(j = 0; j < nopg; j++)
					if(used[j] == 0)					//for "last recently used" -> unmarked frame
					index = j;							//index = not allowing all frames being replaced with same value 
					
					frame[index] = references[i];		//replace LRU frame (frame[index]) with current reference (page[i])
					missMarker = index;
					System.out.printf("\n%d:\t|", references[i]);
					countM++;							//+1 MISS
			}
			
			for(k = nopg-1; k >= 0; k--){				//print frames (counting down because frames are mirrored)
				if(frame[k] != -1){
					if(k == missMarker)
						System.out.print("x");
					else
						System.out.print(" ");
					
					if(frame[k] > 9)
						System.out.printf("%d|", frame[k]);
					else
						System.out.printf("%d |", frame[k]);
				}
				else
					System.out.print(" - |");
			}

			if(countM > prevCountM){
				System.out.print("\t--->F\tHit: " + countH + "   Miss: " + countM);
				prevCountM++;
			}
			   
			else
				System.out.print("\t\tHit: " + countH + "   Miss: " + countM);
		   
		} // -----------------------------------------------END-------------------------------

	   	System.out.println();
		hmRatio(countH, countM);						//display Hit/Miss ratio
	}
				 
					
	/**----------------------------------------LFU (100% WORKING)----------------------------------------------------------**/
		 
	public static void lfu(Integer[] references, int refCount) throws IOException {
		  	  
		int [] frame = new int[16];
		int [] count = new int[65];						//number of uses of reference value (i.e.: count[3] = numbers of 3's used )
		int [] time = new int[65];			
		int least, minTime, temp = 0;
		
		int countM = 0, prevCountM = 0, countH = 0, maxRef = 0;
		boolean flag;
	   	int missMarker;									//marker for the replaced page (only relevant for printing process)
	   	
	   	for(int n = 0; n < refCount; n++){
			if(references[n] > maxRef){
				maxRef = references[n];
			}
		}
	   	
		System.out.print("\n--------------- LFU ------------------------------------------------------------");
				
		if(maxRef > 65){		//-----------------------NEW-------------------------
			System.out.println("\n\nError - Zu hohe Anfragewerte! (erlaubte Anfragenwerte: 0-64) ");
			return;
			}
		
		System.out.print("\nAnzahl der Seiten (max: 16): ");

		int nopg = chNopg();							//nopg = number of pages
		
		System.out.println("\nx  = Ersetzte Seite");
			
	  	System.out.print("\nSeite ->|");				//display the page numbers
	  	for(int i = 1; i <= nopg; i++) {
	  		System.out.print(" " + i);
	  		if(i < 10)
	  			System.out.print(" ");
	  		System.out.print("|");
	  	}
		System.out.print("\n");
		
		for(int i = 0; i < nopg; i++) {
			frame[i] = -1;								//mark all unused array spaces
		}
		for(int i = 0; i < nopg; i++) {					//all references unused initially
			count[i] = 0;
		}
		
		System.out.println();
		
		for(int i = 0; i < refCount; i++) { //-------------------START---------------------------
			
			count[references[i]]++;						//------number of uses of current reference value(!)
			time[references[i]] = i;					//------arrival time of current reference value(!)
			flag = true;
			least = frame[0];							//------value in current first frame - setting up a start point for going through frames    
  			missMarker = -1;
			
			for(int j = 0; j < nopg; j++) {
				
				if(frame[j] == -1 || frame[j] == references[i]) {
					
					if(frame[j] != -1)					//no fault occurs
						countH++;						//+1 HIT
					
					if(frame[j] == -1) {				//fault from initializing a frame
						countM++;						//+1 MISS
						missMarker = j;
					}
								
					flag = false;
					frame[j] = references[i];
					System.out.printf("%d:\t|", frame[j]);
					
					break;
				}
				//going through all used-counter of current frames (from left to right)
				if(count[least] > count[frame[j]])		//searching for the LEAST used reference value
					least = frame[j];					//replacing "least" with the least used reference value of current frames 
			}
			
			if(flag) {									//"real" fault occurs (WITHOUT faults from initializing a frame)
				minTime = refCount;						//minTime = max. amount of references
				for(int j = 0; j < nopg; j++) {
					if(count[frame[j]] == count[least] && time[frame[j]] < minTime) {	 													//(filtering for lowest or stalemate use-counts) && (determine "First In" reference value)
						temp = j;						//mark a earlier arrival time -> newest temp = earliest arrival time (i)
						minTime = time[frame[j]];		//filtering for earliest arrival time 
					}
				}
				count[frame[temp]] = 0;					//resetting least frequently used reference value to "unused"
				frame[temp] = references[i];			//replacing LFU frame with current reference
				System.out.printf("%d:\t|", frame[temp]);
				countM++;								//+1 MISS
				missMarker = temp;
			}
			
			for(int j = 0; j < nopg; j++){				//print frames
				if(frame[j] != -1){
					if(j == missMarker)
						System.out.print("x");
//					System.out.print("x(" + count[frame[j]] + ")");	
					else
						System.out.print(" ");
//					System.out.print("(" + count[frame[j]] + ")");	
										
					if(frame[j] > 9)
						System.out.printf("%d|", frame[j]);
					else
						System.out.printf("%d |", frame[j]);
				}
				else
					System.out.print(" - |");
			}
			
			if(countM > prevCountM){
				System.out.print("\t--->F\tHit: " + countH + "   Miss: " + countM);
				prevCountM++;
			}
			   
			else
				{System.out.print("\t\tHit: " + countH + "   Miss: " + countM);}
			
			System.out.println();
		}//-------------------------------------END----------------------------------------
		
		hmRatio(countH, countM);
	}

					
	/**-----------------------------------------Clock/Second Chance (100% WORKING)--------------------------------------------------------------**/	

	public static void clockSC(Integer[] references, int refCount) throws IOException {
		
		int [] frame = new int[16];					//array for frames
		int i, j, k;								//counting variables
		int avail, countM = 0, countH = 0;			//avail = sign if request was successful; countM = Misses; countH = Hits
		int marker = 0;								//marker for the most recently accessed/replaced page
		int vict = -1;								//the "victim page" to be replaced...
													//...(initialized with -1 so that the very first request starts with frame[0] since it's incremented by 1)

		System.out.print("\n--------------- Clock/Second Chance ---------------------------------------------");
		System.out.print("\nAnzahl der Seiten (max: 16): ");

		int nopg = chNopg();						//nopg = number of pages
		
		byte[] scBit = new byte[nopg]; 				//scBit = Second Chance-Bit
		
		System.out.println("\n *  = Markierungs-Bit\n(1) = Second Chance-Bit");
		
		System.out.print("\nSeite ->|");			//display the page numbers
		for(i = 1; i <= nopg; i++) {
			System.out.print("  " + i);
			if(i < 10)
				System.out.print(" ");
			System.out.print(" |");
		}
		
		for(i = 0; i < nopg; i++)					//marks all unused array spaces
			frame[i] = -1;					
		
		System.out.print("\n\n");
		
		for(i = 0; i < refCount; i++) {//----------------------------START-------------------------------
			
			System.out.printf("%d:\t|", references[i]);	//display current reference
			avail = 0;
			for(j = 0; j < nopg; j++)
				if(frame[j] == references[i]){			//check all frames for current reference
					avail = 1;							//search successful, no fault
					countH++;							//+1 HIT
					scBit[j] = 1;						//set Second Chance-Bit to 1
					marker = j;							//set marker to accessed page
				}	
					
			if(avail == 0) {							//search unsuccessful, fault occurs
				countM++;								//+1 MISS	
				vict = marker;							//use the marker as the starting point of the following search
					
				boolean cont = true;
				do {									//do-while loop: progress through the pages until the victim page is found
					vict = (vict + 1) % nopg;
					for(k = 0; k < nopg; k++){				
						if(frame[k] == -1){				//check for empty pages to be replaced before the others
							vict = k;
							break;
						}
					}
					if(scBit[vict] == 1)				//Second Chance: if the SC-Bit is 1, the page is skipped...		
						scBit[vict] = 0;				//...and the SC-Bit is set to 0
					else 
						cont = false;					//first page from the marker without a SC is found and will be replaced
				} while(cont);	
				frame[vict] = references[i];			//replace page
				marker = vict;							//set marker to replaced page
			}
			
			for(j = 0; j < nopg; j++){					//display frames
				if(frame[j] != -1){
					if(j == marker)						//display location of the marker
						System.out.print("*");
					else
						System.out.print(" ");
					System.out.printf("%d(" + scBit[j] + ")|", frame[j]);
				}
				else
					System.out.print("  -  |");
			}
			
			System.out.print("\t");
			if(avail == 0)								//display fault, if occurred
				System.out.print("--->F");
			
			System.out.print("\tHit: " + countH);
			if(countH < 10) 
				System.out.print(" ");
			System.out.print("   Miss: " + countM + "\n");
		} //-----------------------------------------END----------------------------------
		
		hmRatio(countH, countM);						//display Hit/Miss ratio
	}


	//Method for manually choosing a file
	public static File getRefFile() { 
		JFileChooser fileSelect = new JFileChooser();
		fileSelect.setDialogTitle("Wählen sie einen Textdatei aus: ");
		int select = fileSelect.showOpenDialog(null);
		  
		if (select != JFileChooser.APPROVE_OPTION)
			return null;
		else
			return fileSelect.getSelectedFile();
	}
	
	//Method for choosing the number of pages
	public static int chNopg() {
		Scanner sc = new Scanner(System.in);
		int nopg = 0;
		do {
			while (!sc.hasNextInt()) {
				System.out.println("\nKeine Zahl eingegeben! Bitte erneut eingeben:");    	 
				sc.next();
			}
			nopg = sc.nextInt();
			if(nopg < 1 || nopg > 16)
				System.out.println("\nUngültige Zahl! Bitte erneut eingeben:");
		} while(nopg < 1 || nopg > 16);
		System.out.println("Seitenanzahl \"" + nopg + "\" akzeptiert.");
		return nopg;
	}
	
	//Method for calculating and displaying the Hit/Miss ratio
	public static void hmRatio(int hit, int miss) {
		double hRatio = (double)(hit*100)/(hit+miss);
		double mRatio = (double)(miss*100)/(hit+miss);
		System.out.print("\n Hit-Rate: " + myRound(hRatio, 2) + " %");
		System.out.print("\nMiss-Rate: " + myRound(mRatio, 2) + " %\n");
	}
	
	//Method for rounding
	public static double myRound(double wert, int stellen) {
		return  Math.round(wert * Math.pow(10, stellen)) / Math.pow(10, stellen);
	}
 
}
