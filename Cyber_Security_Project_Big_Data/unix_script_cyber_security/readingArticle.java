
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class readingArticle {
	public static int flag = 0;
	public static void updatefile(String company, String logfilename, String body, String name){
		try{
			if(!body.trim().isEmpty()){

				String path = company +"/" + logfilename;
				
				File file = new File(path);
				file.getParentFile().mkdirs();
				PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
				writer.println(name+","+body);
				writer.close();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	public static void updatefile(String company, String logfilename, String body){
		try{
			if(!body.trim().isEmpty()){

				String path = company +"/" + logfilename;
				
				File file = new File(path);
				file.getParentFile().mkdirs();
				PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
				writer.println(body);
				writer.close();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void outputfiles(String fileName, String output, String hashname, String type) {
		String line = null;

		HashMap<String, Integer> hash = new HashMap<String, Integer>();
		try {

			FileReader fileReader_hash = new FileReader(hashname);

			BufferedReader bufferedReader_hash = new BufferedReader(fileReader_hash);

			while ((line = bufferedReader_hash.readLine()) != null) {
				String[] val = line.split("\\s");
				if(Integer.parseInt(val[1].trim()) > 8){
					hash.put(val[0], 0);
				}
			}
			// System.out.println("size of hash: "+hash.size());
			File file = new File(fileName);
			listFilesForFolder(file, type, hash,output);
// 			Files.walk(Paths.get(fileName)).forEach(filePath -> {

// 				if (Files.isRegularFile(filePath)) {
// 					// System.out.println("original");
// 					// System.out.println(hash.toString());
// 					HashMap<String, Integer> temp_hash = new HashMap<String, Integer>(hash);
// 					String path = filePath.toString();
// 					// If condition to ignore .DS_Store files usually present in mac folders
// 					if (!path.contains(".DS_Store")) {

// 						System.out.println("added data of file : " + path);
// 						try {
// 							String line1 = "";
// 							FileReader fileReader = new FileReader(path);
// 							BufferedReader bufferedReader = new BufferedReader(fileReader);
// 							while ((line1 = bufferedReader.readLine()) != null) {

// 								String[] arr = line1.split(" ");
// 								for (String i : arr) {
// 									if (temp_hash.containsKey(i)) {
// 										int count = temp_hash.get(i);
// 										count++;
// 										// System.out.println("updating "+i+" to
// 										// "+count);
// 										temp_hash.put(i, count);
// 									}
// 								}
// 							}
// 							bufferedReader.close();
// 							fileReader.close();
// 						} catch (Exception e) {
// 							// TODO Auto-generated catch block
// 							e.printStackTrace();
// 						}

// 						// System.out.println(temp_hash.toString());
// 						//
// 						// just for Naive Bayesian
// 						String body_nb = "";
// 						// for(Map.Entry<String, Integer> i :
// 						// temp_hash.entrySet()){
// 						// body +=i.getValue()+" ";
// 						// }
// 						// for Naive Bayesian(body_nb) and SVM(body_svm)
// 						// classification
// 						String body_svm = "";
// 						int counter = 0;
// 						for (Map.Entry<String, Integer> i : temp_hash.entrySet()) {
// 							body_nb += i.getValue() + " ";
// 							counter++;
// 							int value = i.getValue();
// 							if (value != 0)
// 								body_svm += counter + ":" + value + " ";
// 						}
// 						path = path.substring(path.indexOf("/")+6,path.length());
// 						if(path.contains("nyarticle")){
// 							path = path.replace("nyarticle", "1");
// 						}
// 						else{
// 							path = path.replace("article", "0");
// 						}
// 						path = path.replaceAll("[/-]", "");
// //						System.out.println(path);
						
// 						if(body_svm != "" && type == "training"){
// //							System.out.println("");
// 							if(flag < 50){
// 								updatefile(output, "naivedata", "1,"+body_nb);
// 								updatefile(output, "svmdata", "1 "+body_svm);
// 								System.out.println(flag++);
// 							}
// 							else{
// 								updatefile(output, "naivedata", "0,"+body_nb);
// 								updatefile(output, "svmdata", "0 "+body_svm);
// 								System.out.println(flag++);
// 							}
// 						}
// 						if(body_svm != "" && type == "test"){
// //							System.out.println("");
// 							updatefile(output, "data", body_nb, path);

// 						}
// 					}
// 				}
// 			});

			// Always close files.
			bufferedReader_hash.close();
			fileReader_hash.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + fileName + "'");
		} catch (IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");
			// ex.printStackTrace();
		}
	}
	public static void listFilesForFolder(final File folder, String type, HashMap<String, Integer> hash, String output) {
		File temp = folder;
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry,type,hash,output);
	        } else {
	            
					// System.out.println("original");
					// System.out.println(hash.toString());
					HashMap<String, Integer> temp_hash = new HashMap<String, Integer>(hash);
					String path = fileEntry.getPath();
					// If condition to ignore .DS_Store files usually present in mac folders
					if (!path.contains(".DS_Store")) {

						System.out.println("added data of file : " + path);
						try {
							String line1 = "";
							FileReader fileReader = new FileReader(path);
							BufferedReader bufferedReader = new BufferedReader(fileReader);
							while ((line1 = bufferedReader.readLine()) != null) {

								String[] arr = line1.split(" ");
								for (String i : arr) {
									if (temp_hash.containsKey(i)) {
										int count = temp_hash.get(i);
										count++;
										// System.out.println("updating "+i+" to
										// "+count);
										temp_hash.put(i, count);
									}
								}
							}
							bufferedReader.close();
							fileReader.close();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						// System.out.println(temp_hash.toString());
						//
						// just for Naive Bayesian
						String body_nb = "";
						// for(Map.Entry<String, Integer> i :
						// temp_hash.entrySet()){
						// body +=i.getValue()+" ";
						// }
						// for Naive Bayesian(body_nb) and SVM(body_svm)
						// classification
						String body_svm = "";
						int counter = 0;
						for (Map.Entry<String, Integer> i : temp_hash.entrySet()) {
							body_nb += i.getValue() + " ";
							counter++;
							int value = i.getValue();
							if (value != 0)
								body_svm += counter + ":" + value + " ";
						}
						// System.out.println(path);
						path = path.substring(path.indexOf("/")+1,path.length());
						if(path.contains("nyarticle")){
							path = path.replace("nyarticle", "1");
						}
						else{
							path = path.replace("article", "0");
						}
						path = path.replaceAll("[/-]", "");
						// System.out.println(path);
						if(body_svm != "" && type == "training"){
//							System.out.println("");
							if(flag < 50){
								updatefile(output, "naivedata", "1,"+body_nb);
								updatefile(output, "svmdata", "1 "+body_svm);
								System.out.println(flag++);
							}
							else{
								updatefile(output, "naivedata", "0,"+body_nb);
								updatefile(output, "svmdata", "0 "+body_svm);
								System.out.println(flag++);
							}
						}
						if(body_svm != "" && type == "test"){
//							System.out.println("");
							updatefile(output, "data", body_nb, path);

						}
					}
				}

	        }
	    
	}
	public static void main(String[] args) {
//		outputfiles("merging doc/sony", "dictionary/sony");
		System.out.println("Creating input for classification...");
		outputfiles("sony_test", args[1]+"/training", args[2],"training");
		System.out.println("Creating input for classification...");
		outputfiles(args[0], args[1]+"/test", args[2],"test");
		System.out.println("input for classification ready!");
	}
}

