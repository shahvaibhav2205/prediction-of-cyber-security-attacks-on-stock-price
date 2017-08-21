import java.io.File;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

public class CountArticles {
	
	public static HashMap<String, int []> count(String path) throws Exception {
	
		File folder = new File(path);
		File [] files = folder.listFiles();
		//System.out.println(folder.isDirectory());
		RandomAccessFile f = null;
		HashMap<String,  int []> dates = new HashMap<String, int []>();
		for(File file : files) {
			
			if(file.isFile() && !file.isHidden()) {
				System.out.println("Reading " + file.getName());
				try
				{
					f = new RandomAccessFile(file, "r");
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				String s, t;
				int c = 0;
				while((s = f.readLine()) != null)
				{	
					s = s.replaceAll("\\(+", "");
					s = s.replaceAll("\\)", "");
					
					t = s.substring(0,8);
					s = s.substring(8);
					
					if(dates.containsKey(t)) {
						if(s.contains("1.0")) {
							
							c++;
							int[] a = dates.get(t);
							int [] b = new int[2];
							b[0] = a[0];
							b[1] = a[1] + 1;
							dates.put(t, b);
						}
						else {
							
							c++;
							int[] a = dates.get(t);
							int [] b = new int[2];
							b[0] = a[0] + 1;
							b[1] = a[1];
							dates.put(t, b);
						}
					}
					else {
						c++;
						if(s.contains("1.0")) {
							int [] a = new int[2];
							a[0] = 0;
							a[1] = 1;
							
							dates.put(t, a);
						}
						else {
							int [] a = new int[2];
							a[0] = 1;
							a[1] = 0;
							dates.put(t, a);
						}
					}
				}
				//System.out.println(c + "hi there");
			}
		}
		
		return dates;
	}
	
	public static TreeMap<Date, Double> price(String path) throws Exception {
		RandomAccessFile stock = null;
		try
		{
			stock = new RandomAccessFile(path, "r");
			stock.readLine();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		TreeMap<Date, Double> stock_price = new TreeMap<Date, Double>();
		Double price;
		String st;
		while((st = stock.readLine()) != null) {
			String [] temp = st.split(",");
			st = temp[0];
			DateFormat dateFormatStock = new SimpleDateFormat("yyyyMMdd");
			Date s = dateFormatStock.parse(st);
			price = Double.parseDouble(temp[4]);
			stock_price.put(s, price);
		}
		return stock_price;
	}
	
	public static void main(String [] args) throws Exception{
		RandomAccessFile file = new RandomAccessFile(args[1]+ "count.csv", "rw");
		file.write("Date, Cyber Article, Stock Price \n".getBytes());
		HashMap<String, int []> dates = count(args[0]);
		//         /Users/Kaushal/Documents/test/data
		TreeMap<Date, Double> price = price(args[2]);
		 Set<String> x = dates.keySet();
		 for(String k:x) {
			 String m = new String();
			 int [] a = dates.get(k);
			 m += k;
			 m += "," + a[1];			 
			 DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			 Date t = dateFormat.parse(k);
			 if(price.containsKey(t)) {
				 m += "," + price.get(t);
				 //System.out.println(a[1]);
			 }
			 else {
				 //System.out.println(a[1]);
				 m += "," + price.get(price.floorKey(t));
			 }
			 m += "\n";
			 file.write(m.getBytes());
		 }
		 file.close();
		 System.out.println("Output CSV file is saved in " + args[1]);
	}
	
}



